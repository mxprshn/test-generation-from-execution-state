"""RQ2: PIT mutation testing of generated test classes, scoped to the target class.

Two metrics (both reported, explicitly named):
  * oracle strength of a mode   = killed / covered mutants, tests of this mode only;
  * gain over baseline          = mutants killed by (baseline + mode) minus mutants killed by baseline,
                                  computed from per-mutant results of separate PIT runs (union of killed sets).

Classpath: `mvn dependency:build-classpath` (test scope) of the module that holds the generated test,
plus its target/classes and target/test-classes. The generated test is compiled into a separate
directory with javac. JUnit 4 projects need the vintage engine on the classpath (PIT junit5 plugin
runs through the JUnit Platform); it is added when found in the local Maven repository.
"""
from __future__ import annotations

import glob
import json
import os
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any

# Local Maven repository: PIT jars, JUnit Platform launcher and vintage engine (scripts/setup.sh fetches
# PIT); set from the config by evaluation.evaluate
M2 = Path.home() / ".m2" / "repository"
PIT_JARS = [
    "org/pitest/pitest/1.15.8/pitest-1.15.8.jar",
    "org/pitest/pitest-entry/1.15.8/pitest-entry-1.15.8.jar",
    "org/pitest/pitest-command-line/1.15.8/pitest-command-line-1.15.8.jar",
    "org/pitest/pitest-junit5-plugin/1.2.1/pitest-junit5-plugin-1.2.1.jar",
    "org/apache/commons/commons-text/1.11.0/commons-text-1.11.0.jar",
    "org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar",
]


def _java(tool: str) -> str:
    return str(Path(os.environ["JAVA_HOME"]) / "bin" / tool)


def pit_classpath() -> str:
    jars = [M2 / j for j in PIT_JARS]
    missing = [str(j) for j in jars if not j.is_file()]
    if missing:
        raise FileNotFoundError(f"PIT jars missing: {missing}")
    return ":".join(str(j) for j in jars)


def module_dir_for(workspace: Path, test_file: str) -> Path:
    """Directory of the Maven module that contains the test file (nearest pom.xml)."""
    p = (workspace / test_file).parent
    while p != workspace and not (p / "pom.xml").is_file():
        p = p.parent
    return p


CP_PROBE = r"""
import java.nio.file.*;
public class ClasspathProbe {
    @org.junit.jupiter.api.Test public void dump() throws Exception { write(); }
    @org.junit.Test public void dump4() throws Exception { write(); }
    static void write() throws Exception {
        String out = System.getProperty("classpath.probe.out");
        if (out == null) out = System.getenv("CLASSPATH_PROBE_OUT");
        // Java 8 API only: some projects (jsoup) check test sources with animal-sniffer
        Files.write(Paths.get(out), System.getProperty("java.class.path").getBytes("UTF-8"));
    }
}
"""


def classpath_from_surefire(workspace: Path, scenario_argv: list[str], out: Path, env: dict[str, str],
                            timeout: int = 1800) -> str | None:
    """Run the case's own scenario command with -Dtest replaced by a probe test that writes the
    surefire classpath. Works wherever the scenario command works (reactor siblings included)."""
    cp_out = out / "surefire-cp.txt"
    if cp_out.is_file():
        return cp_out.read_text().strip()
    # the probe is a helper class, not a test under evaluation: skip format checks (Klaw: spotless)
    argv = [a for a in scenario_argv if a != "-q"] + ["-Dspotless.check.skip=true", "-Dspotless.skip=true"]
    test_arg = next((a for a in argv if a.startswith("-Dtest=")), None)
    if test_arg is None:
        return None
    test_cls = test_arg.split("=", 1)[1].split("#")[0].split(",")[0]
    hits = [p for p in workspace.rglob(f"{test_cls.split('.')[-1]}.java") if "/src/test/" in str(p) or "/tst/" in str(p)]
    if not hits:
        return None
    probe_dir = hits[0].parent
    pkg_line = next((l for l in hits[0].read_text().splitlines() if l.strip().startswith("package ")), "")
    probe = probe_dir / "ClasspathProbe.java"
    src = CP_PROBE
    if "junit.jupiter" not in hits[0].read_text():
        src = src.replace('    @org.junit.jupiter.api.Test public void dump() throws Exception { write(); }\n', "")
    else:
        src = src.replace('    @org.junit.Test public void dump4() throws Exception { write(); }\n', "")
    probe.write_text(pkg_line + "\n" + src)
    argv = [a if not a.startswith("-Dtest=") else "-Dtest=ClasspathProbe" for a in argv]
    if argv and argv[0].startswith("./"):
        argv[0] = str(workspace / argv[0][2:])
    e = dict(env)
    e["CLASSPATH_PROBE_OUT"] = str(cp_out)
    out.mkdir(parents=True, exist_ok=True)
    res = subprocess.run(argv, cwd=workspace, env=e, capture_output=True, text=True, timeout=timeout)
    probe.unlink(missing_ok=True)
    if not cp_out.is_file():
        (out / "cp-probe.log").write_text((res.stdout or "")[-6000:] + (res.stderr or "")[-3000:])
        return None
    return cp_out.read_text().strip()


def build_classpath(module: Path, workspace: Path, out: Path, env: dict[str, str], mvn: str = "mvn",
                    timeout: int = 1800) -> str:
    """Test-scope classpath of the module. With `-am` the sibling SNAPSHOT modules are reactor
    projects and resolve to their target/classes; the relative output name makes every reactor
    module write its own file, and the target module's file is read."""
    cp_file = out / "cp.txt"
    if cp_file.is_file():
        return cp_file.read_text().strip()
    if mvn.startswith("./"):
        mvn = str(workspace / mvn[2:])
    rel = os.path.relpath(module, workspace)
    name = "test-classpath.txt"
    args = [mvn, "-q", "-o", "-Dlicense.skip=true", "-Dexec.skip=true", "-Dspotless.check.skip=true",
            "-Dspotless.skip=true"]
    if rel != ".":
        args += ["-pl", rel, "-am"]
    args += ["org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath",
             f"-Dmdep.outputFile={name}", "-DincludeScope=test"]
    res = subprocess.run(args, cwd=workspace, env=env, capture_output=True, text=True, timeout=timeout)
    produced = module / name
    if not produced.is_file():
        raise RuntimeError(f"build-classpath failed for {rel}:\n{res.stdout[-1500:]}\n{res.stderr[-1500:]}")
    cp = produced.read_text().strip()
    # sibling modules may appear as SNAPSHOT jars that are not installed: map them to target/classes
    fixed = []
    for entry in cp.split(":"):
        if entry.endswith("-SNAPSHOT.jar") and not Path(entry).is_file():
            art = Path(entry).name.rsplit("-", 2)[0]
            hits = [p for p in workspace.glob(f"**/{art}/target/classes") if p.is_dir()]
            if hits:
                fixed.append(str(hits[0]))
                continue
        fixed.append(entry)
    cp = ":".join(fixed)
    cp_file.write_text(cp)
    return cp


def _version_key(v: str) -> tuple[int, ...]:
    return tuple(int(x) if x.isdigit() else 0 for x in re.split(r"[.-]", v))


def _platform_jar(art: str, ver: str) -> Path:
    return M2 / f"org/junit/platform/junit-platform-{art}/{ver}/junit-platform-{art}-{ver}.jar"


def _vintage_jar(ver: str) -> Path:
    return M2 / f"org/junit/vintage/junit-vintage-engine/{ver}/junit-vintage-engine-{ver}.jar"


def _platform_extras(cp: str) -> list[str]:
    """JUnit Platform launcher (and vintage engine for JUnit 4) matching the project's platform.

    A project on the JUnit Platform brings its platform version; the launcher of that version is
    added. A JUnit 4 project has no platform: the newest 1.x platform whose launcher and vintage
    engine (5.x) are both in the local repository is used (scripts/setup.sh fetches one). Versions
    are compared numerically. Without a vintage engine PIT finds no test engine at all, so that is
    an error, not an empty result."""
    extras: list[str] = []
    m = re.search(r"junit-platform-(?:engine|commons)/([0-9][0-9.]*)/", cp)
    ver = m.group(1) if m else None
    junit4_only = ver is None and "junit/junit/4" in cp
    if ver is None:
        cands = sorted((Path(p).name for p in glob.glob(str(M2 / "org/junit/platform/junit-platform-launcher/1.*"))),
                       key=_version_key, reverse=True)
        if junit4_only:
            # the vintage engine 5.N runs on the platform 1.N
            cands = [v for v in cands if glob.glob(str(M2 / f"org/junit/vintage/junit-vintage-engine/5.{v.split('.')[1]}.*/*.jar"))]
            if not cands:
                raise FileNotFoundError("JUnit 4 tests need a JUnit vintage engine 5.x with its platform 1.x in the "
                                        f"local Maven repository ({M2}); run scripts/setup.sh")
        ver = cands[0] if cands else None
    if ver:
        for art in ("launcher", "engine", "commons"):
            j = _platform_jar(art, ver)
            if j.is_file() and f"junit-platform-{art}/" not in cp:
                extras.append(str(j))
    if "junit/junit/4" in cp and "junit-vintage-engine" not in cp:
        major = int(ver.split(".")[1]) if ver else 10
        vint = sorted((Path(p).parent.name for p in
                       glob.glob(str(M2 / f"org/junit/vintage/junit-vintage-engine/5.{major}.*/*.jar"))),
                      key=_version_key)
        if vint:
            extras.append(str(_vintage_jar(vint[-1])))
        opentest = sorted(glob.glob(str(M2 / "org/opentest4j/opentest4j/1.*/*.jar")))
        if opentest and "opentest4j" not in cp:
            extras.append(opentest[-1])
    return extras


def compile_test(test_src: Path, cp: str, out: Path) -> Path:
    gen = out / "gen-classes"
    shutil.rmtree(gen, ignore_errors=True)
    gen.mkdir(parents=True)
    res = subprocess.run([_java("javac"), "-proc:none", "-nowarn", "-cp", cp, "-d", str(gen), str(test_src)],
                         capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"javac failed: {res.stderr[-2000:]}")
    return gen


def run_pit(workspace: Path, test_files: list[str], target_class: str, out: Path,
            env: dict[str, str], extra_jvm: list[str] | None = None, mvn: str = "mvn",
            scenario_argv: list[str] | None = None, timeout: int = 1800) -> dict[str, Any]:
    """Run PIT for one or more generated test files (union = one PIT run over all of them).

    The project classpath is taken from surefire (probe run of the scenario command); if that is not
    possible, from `dependency:build-classpath`."""
    out.mkdir(parents=True, exist_ok=True)
    module = module_dir_for(workspace, test_files[0])
    project_cp = classpath_from_surefire(workspace, scenario_argv, out, env, timeout) if scenario_argv else None
    if project_cp is None:
        project_cp = build_classpath(module, workspace, out, env, mvn, timeout)
    project_cp = ":".join(e for e in project_cp.split(":") if "surefire" not in Path(e).name and e)
    main_out, test_out = module / "target" / "classes", module / "target" / "test-classes"
    base_cp = ":".join([str(main_out), str(test_out), project_cp, *_platform_extras(project_cp)])
    gens, fqns = [], []
    for i, tf in enumerate(test_files):
        g = compile_test(workspace / tf, base_cp, out / f"t{i}")
        gens.append(str(g))
        src = (workspace / tf).read_text()
        pkg = re.search(r"^\s*package\s+([\w.]+)\s*;", src, re.M)
        fqns.append((pkg.group(1) + "." if pkg else "") + Path(tf).stem)
    cp_entries = [*gens, str(main_out), str(test_out), *project_cp.split(":"), *_platform_extras(project_cp)]
    report = out / "pit"
    shutil.rmtree(report, ignore_errors=True)
    cmd = [
        _java("java"), "-cp", ":".join([pit_classpath(), *cp_entries]),
        "org.pitest.mutationtest.commandline.MutationCoverageReport",
        "--reportDir", str(report), "--classPath", ",".join(cp_entries),
        "--targetClasses", target_class, "--targetTests", ",".join(fqns),
        "--sourceDirs", str(module / "src" / "main" / "java"),
        "--outputFormats", "XML", "--timestampedReports", "false",
        "--timeoutConst", "30000", "--timeoutFactor", "5.0", "--threads", "2",
    ]
    if extra_jvm:
        cmd += ["--jvmArgs", ",".join(extra_jvm)]
    # surefire runs tests from the module directory; tests open fixtures by relative paths
    # PIT has its own per-mutant timeouts; the whole run gets several command timeouts
    res = subprocess.run(cmd, capture_output=True, text=True, env=env, cwd=module, timeout=timeout * 4)
    (out / "pit-log.txt").write_text((res.stdout or "") + "\n--- STDERR ---\n" + (res.stderr or ""))
    xml = report / "mutations.xml"
    if not xml.is_file():
        raise RuntimeError(f"PIT produced no mutations.xml:\n{(res.stdout or '')[-1500:]}")
    return parse_mutations(xml)


def mutant_id(m: ET.Element) -> str:
    return "|".join(m.findtext(k) or "" for k in ("mutatedClass", "mutatedMethod", "methodDescription",
                                                    "lineNumber", "mutator", "indexes"))


def parse_mutations(xml: Path) -> dict[str, Any]:
    root = ET.parse(xml).getroot()
    per: dict[str, str] = {}
    for m in root.findall("mutation"):
        per[mutant_id(m)] = m.get("status", "UNKNOWN")
    return {"mutants": per, **score(per)}


KILLED = {"KILLED", "TIMED_OUT", "MEMORY_ERROR"}


def score(per: dict[str, str]) -> dict[str, Any]:
    total = len(per)
    killed = sum(1 for s in per.values() if s in KILLED)
    covered = sum(1 for s in per.values() if s != "NO_COVERAGE")
    return {"total": total, "covered": covered, "killed": killed,
            "oracle_strength": killed / covered if covered else None,
            "reach": covered / total if total else None}


def gain_over_baseline(baseline: dict[str, str], mode: dict[str, str]) -> dict[str, Any]:
    """Mutants killed by baseline ∪ mode versus baseline alone (same mutant ids)."""
    base_k = {k for k, s in baseline.items() if s in KILLED}
    mode_k = {k for k, s in mode.items() if s in KILLED}
    return {"baseline_killed": len(base_k), "union_killed": len(base_k | mode_k),
            "additional": len(mode_k - base_k), "total": len(set(baseline) | set(mode))}


def save(result: dict[str, Any], path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(result, indent=1))
