"""What the external test generators (pankti, rick, microtestcarver) share: the context of a cell, the
external tools directory, running a step, placing the generated class and its files in the workspace.

Ported from the tool harness of the earlier study (aithd-ppr, baselines/tool.py and baselines/mtc.py,
commits a804640, bb07971, d3661fd); docs/tools.md lists every deviation from the upstream tools.
"""
from __future__ import annotations

import os
import re
import subprocess
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from runner.cases import Case
from runner.config import Config

# Options the generated tests of Pankti and RICK need at test time on JDK 17+: XStream 1.4.12 deserializes
# the recorded objects by reflection into JDK classes. They are given to every JVM through
# JDK_JAVA_OPTIONS in the `env` of a config of these modes (docs/tools.md); the recording JVM gets them
# on its command line.
ADD_OPENS_PACKAGES = (
    "java.base/java.util", "java.base/java.lang", "java.base/java.lang.reflect", "java.base/java.text",
    "java.base/java.io", "java.base/java.util.concurrent", "java.base/java.util.concurrent.locks",
    "java.base/java.util.concurrent.atomic", "java.base/java.lang.ref", "java.base/java.net",
    "java.base/java.math", "java.base/java.nio", "java.base/java.nio.charset", "java.base/java.time",
    "java.base/sun.nio.cs", "java.base/java.security", "java.base/jdk.internal.misc",
    "java.base/jdk.internal.ref", "java.sql/java.sql", "java.desktop/java.awt.font",
)
ADD_OPENS = " ".join(f"--add-opens {p}=ALL-UNNAMED" for p in ADD_OPENS_PACKAGES)
TOOL_TEST_ENV = {"JDK_JAVA_OPTIONS": ADD_OPENS}

XSTREAM = ("com.thoughtworks.xstream", "xstream", "1.4.12")
# The generators emit JUnit 5 tests; JUnit 4 modules (JGit, PDFBox) get the Jupiter API and engine.
JUNIT_JUPITER = ("org.junit.jupiter", "junit-jupiter", "5.10.3")
MOCKITO = ("org.mockito", "mockito-core", "5.11.0")
MOCKITO_JUPITER = ("org.mockito", "mockito-junit-jupiter", "5.11.0")
HAMCREST = ("org.hamcrest", "hamcrest", "2.2")


class ToolError(Exception):
    pass


@dataclass
class ToolContext:
    cfg: Config
    case: Case
    workspace: Path          # the cell workspace (exported commit, hidden tests removed, scenario checked)
    work: Path               # scratch of the tool, outside the workspace
    env: dict[str, str]      # command environment of the config
    scenario: list[str]      # the scenario command as the evaluation runs it (eval_command)
    meta: dict[str, Any] = field(default_factory=dict)

    @property
    def ext(self) -> Path:
        return self.cfg.external_tools_dir

    @property
    def jdk17(self) -> Path:
        return self.ext / "jdk17"

    @property
    def jdk11(self) -> Path:
        return self.ext / "jdk11"

    @property
    def output_test_file(self) -> str:
        return self.case.output_test_file

    def log(self, name: str = "tool.log") -> Path:
        return self.work / name


def run_step(argv: list[str], cwd: Path, env: dict[str, str], log: Path, timeout: float) -> int:
    """Run one step of a tool, output appended to the log; -1 when it timed out."""
    log.parent.mkdir(parents=True, exist_ok=True)
    with log.open("ab") as handle:
        handle.write(f"\n$ {' '.join(argv)}\n".encode())
        handle.flush()
        process = subprocess.Popen(argv, cwd=cwd, env=env, stdout=handle, stderr=subprocess.STDOUT,
                                   stdin=subprocess.DEVNULL, start_new_session=True)
        try:
            return process.wait(timeout=timeout)
        except subprocess.TimeoutExpired:
            from runner.process import _kill_group
            _kill_group(process)
            handle.write(b"\n[timed out]\n")
            return -1


def first_exception(log: Path, since: int = 0) -> str | None:
    """The first exception line a step wrote to the log after byte `since` (for the reason of a failure)."""
    try:
        text = log.read_bytes()[since:].decode("utf-8", errors="replace")
    except OSError:
        return None
    import re as _re
    m = _re.search(r"^(?:Exception in thread \S+ )?([\w.$]+(?:Exception|Error)\b[^\n]{0,200})", text, _re.M)
    return m.group(1).strip() if m else None


def tool_env(base: dict[str, str], java_home: Path) -> dict[str, str]:
    """The command environment with another JDK first; no JDK_JAVA_OPTIONS (the tool JVMs set their own)."""
    env = dict(base)
    env["JAVA_HOME"] = str(java_home)
    env["PATH"] = f"{java_home / 'bin'}{os.pathsep}{env.get('PATH', '')}"
    env.pop("JDK_JAVA_OPTIONS", None)
    env.pop("JAVA_TOOL_OPTIONS", None)
    return env


def record_argv(ctx: ToolContext, java_home: Path, agent_options: str, extra: list[str] = ()) -> list[str]:
    """The scenario command with the tool's agent in the forked test JVM, which runs on java_home."""
    from runner.process import resolve_argv
    argv = resolve_argv([a for a in ctx.scenario if a != "-q"], ctx.workspace)
    return [argv[0], f"-Djvm={java_home}/bin/java", f"-Dmaven.surefire.debug={agent_options}",
            "-Denforcer.skip", *extra, *argv[1:]]


def target_module(project: Path, target_class: str) -> Path:
    """The Maven module whose production sources contain the target class."""
    relative = target_class.replace(".", "/") + ".java"
    candidates = [
        p for p in project.rglob(relative)
        if not any(part in ("test", "tst", "target", "build") or part.endswith(".test")
                   for part in p.relative_to(project).parts)
    ]
    if not candidates:
        raise ToolError(f"source of {target_class} not found")
    path = candidates[0].parent
    while path != project and not (path / "pom.xml").is_file():
        path = path.parent
    return path


def signature_params(signature: str) -> list[str]:
    match = re.match(r"\s*\((.*)\)\s*->", signature or "")
    if not match:
        return []
    return [p.strip() for p in match.group(1).split(",") if p.strip()]


def erase(type_name: str) -> str:
    return re.sub(r"<.*>", "", type_name).strip()


def maven_module_root(workspace: Path, output_test_file: str) -> Path:
    path = (workspace / output_test_file).parent
    while path != workspace and not (path / "pom.xml").is_file():
        path = path.parent
    return path


def test_resources_dir(module: Path) -> Path:
    pom = (module / "pom.xml").read_text(encoding="utf-8")
    match = re.search(r"<testResource>\s*<directory>([^<]+)</directory>", pom)
    return module / (match.group(1).strip().rstrip("/") if match else "src/test/resources")


def add_test_dependencies(pom: Path, deps: list[tuple[str, str, str]]) -> list[str]:
    text = pom.read_text(encoding="utf-8")
    added = []
    block = ""
    for group, artifact, version in deps:
        # only when the module does not declare it (the project's own version wins)
        if re.search(rf"<artifactId>{re.escape(artifact)}</artifactId>", text):
            continue
        exclusions = (
            "      <exclusions>\n"
            "        <exclusion><groupId>org.junit.jupiter</groupId><artifactId>*</artifactId></exclusion>\n"
            "        <exclusion><groupId>org.junit.platform</groupId><artifactId>*</artifactId></exclusion>\n"
            "      </exclusions>\n"
            if artifact == "mockito-junit-jupiter" else ""
        )
        block += (
            "    <dependency>\n"
            f"      <groupId>{group}</groupId>\n"
            f"      <artifactId>{artifact}</artifactId>\n"
            f"      <version>{version}</version>\n"
            "      <scope>test</scope>\n"
            f"{exclusions}"
            "    </dependency>\n"
        )
        added.append(f"{group}:{artifact}:{version}")
    if not block:
        return added
    management = [(m.start(), m.end()) for m in
                  re.finditer(r"<dependencyManagement>.*?</dependencyManagement>", text, re.S)]
    for match in re.finditer(r"<dependencies>\n?", text):
        if any(a <= match.start() < b for a, b in management):
            continue
        text = text[: match.end()] + block + text[match.end():]
        break
    else:
        raise ToolError(f"no <dependencies> section in {pom}")
    pom.write_text(text, encoding="utf-8")
    return added


def uses_junit5(pom: Path) -> bool:
    text = pom.read_text(encoding="utf-8")
    if "junit-jupiter" in text:
        return True
    parent = pom.parent.parent / "pom.xml"
    return parent.is_file() and "junit-jupiter" in parent.read_text(encoding="utf-8")


def pin_mockito(pom: Path) -> None:
    """Mockito < 5 cannot mock on JDK 21; pin the module's own Mockito to 5.x if it is older."""
    text = pom.read_text(encoding="utf-8")
    text = re.sub(
        r"(<artifactId>mockito-(?:core|junit-jupiter)</artifactId>\s*<version>)([0-4]\.[^<]+)(</version>)",
        lambda m: m.group(1) + MOCKITO[2] + m.group(3), text)
    pom.write_text(text, encoding="utf-8")


def drop_unused_imports(source: str) -> str:
    body = re.sub(r"(?m)^import [^;]+;\n", "", source)

    def keep(match: re.Match[str]) -> str:
        simple = match.group(1).rsplit(".", 1)[-1]
        return match.group(0) if re.search(rf"\b{re.escape(simple)}\b", body) else ""
    source = re.sub(r"(?m)^import (?!static )([\w.]+);\n", keep, source)
    return re.sub(r"(?m)^import static ([\w.]+);\n", keep, source)


def write_output(ctx: ToolContext, source: str) -> None:
    out = ctx.workspace / ctx.output_test_file
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(source, encoding="utf-8")


def output_class(output_test_file: str) -> str:
    return output_test_file.rsplit("/", 1)[-1].removesuffix(".java")
