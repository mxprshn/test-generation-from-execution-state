"""Pankti and RICK (Tiwari, Monperrus, Baudry; KTH) as modes without an agent.

The tool records the driving scenario with its own instrumentation (a Glowroot plugin generated for the
target method) and generates the test class with its own generator. Nothing is chosen by hand: the target
comes from case.yaml, the workload is the case's scenario command, the generated class becomes the case's
output test file and the recorded object files go to the module's test resources.

Deviations from the upstream tools: external/pankti.patch and the source fixes of place_outputs, listed
with their reasons in docs/tools.md. Constraints of the tools that are kept: only public, non-static,
non-abstract methods are candidates (pankti-extract); RICK generates tests only for methods with
mockable invocations; Glowroot 0.14 cannot weave JDK 21 classes, so the scenario of a JDK 21 project is
recorded on JDK 17 (projects on JDK 11 are recorded on their own JDK).
"""
from __future__ import annotations

import csv
import re
import shutil
from pathlib import Path
from typing import Any

from runner.tools.common import (ADD_OPENS, first_exception, JUNIT_JUPITER, MOCKITO, MOCKITO_JUPITER, XSTREAM, ToolContext,
                                 ToolError, add_test_dependencies, drop_unused_imports, erase,
                                 maven_module_root, output_class, pin_mockito, record_argv, run_step,
                                 signature_params, target_module, test_resources_dir, tool_env, uses_junit5,
                                 write_output)

SOURCE_COMPLIANCE = "17"  # Spoon language level of the analysed sources (JGit needs 17)

# Types the generators use without importing them (upstream defect).
IMPLICIT_IMPORTS = {
    "Field": "java.lang.reflect.Field",
    "Assertions": "org.junit.jupiter.api.Assertions",
    "InOrder": "org.mockito.InOrder",
}


def record_java_home(ctx: ToolContext) -> Path:
    """The JDK of the recording JVM: the JDK the generated test will run on (the objects are serialized
    with the fields of that JDK's classes), except JDK 21, which Glowroot 0.14 cannot weave: JDK 17."""
    return ctx.cfg.java_home if ctx.cfg.jdk <= 17 else ctx.jdk17


def _jars(ctx: ToolContext) -> dict[str, Path]:
    pk = ctx.ext / "pankti"
    return {
        "extract": pk / "pankti-extract/target/pankti-extract-1.0-SNAPSHOT-jar-with-dependencies.jar",
        "pankti": pk / "pankti-generate/target/pankti-generate-1.0-SNAPSHOT-jar-with-dependencies.jar",
        "rick": pk / "rick/target/rick-1.0-SNAPSHOT-jar-with-dependencies.jar",
    }


def select_target_row(extracted_csv: Path, target: dict[str, Any]) -> dict[str, str] | None:
    """The row of pankti-extract for the target method (class, name and erased parameter types)."""
    wanted = [erase(p) for p in signature_params(target.get("signature", ""))]
    with extracted_csv.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))
    for row in rows:
        if row["parent-FQN"] != target["class"] or row["method-name"] != target["method"]:
            continue
        inner = row["param-list"].strip()
        if inner.startswith("[") and inner.endswith("]"):
            inner = inner[1:-1]
        params = [erase(p) for p in inner.split(",") if p.strip()]
        if params == wanted:
            return row
    return None


def _write_single_row_csv(extracted_csv: Path, row: dict[str, str], out: Path) -> None:
    with extracted_csv.open(newline="", encoding="utf-8") as handle:
        header = next(csv.reader(handle))
    with out.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=header)
        writer.writeheader()
        writer.writerow(row)


def drop_unused_expected_object(source: str) -> str:
    """Pankti declares `expectedObject` and, for strings and primitives, asserts on the literal instead;
    remove the unused declaration and the string it was deserialized from (strict ecj settings of JGit
    reject the unused local)."""
    methods = re.split(r"(?=\n    @Test)", source)
    out = []
    for method in methods:
        if len(re.findall(r"\bexpectedObject\b", method)) == 1:
            method = re.sub(r"\n\s*[\w.<>\[\]]+ expectedObject = deserializeObjectFrom"
                            r"(?:String\(returnedObjectStr\)|File\(\"[^\"]*\"\));", "", method)
            if len(re.findall(r"\breturnedObjectStr\b", method)) == 1:
                method = re.sub(r"\n\s*String returnedObjectStr = [^;]*;", "", method, flags=re.S)
        out.append(method)
    return "".join(out)


def add_missing_imports(source: str) -> tuple[str, list[str]]:
    added = []
    for simple, fqn in IMPLICIT_IMPORTS.items():
        if re.search(rf"\b{simple}\b", source) and f"import {fqn};" not in source:
            added.append(fqn)
    # RICK declares mocks by simple name but imports the type only when it is in the test's package
    package = re.search(r"^package ([\w.]+);", source, re.M)
    for simple in sorted(set(re.findall(r"Mockito\.mock\((\w+)\.class\)", source))):
        if re.search(rf"^import [\w.]+\.{simple};", source, re.M):
            continue
        qualified = re.search(rf"\b((?:[a-z_]\w*\.)+){simple}\b", source)
        if qualified and (not package or qualified.group(1).rstrip(".") != package.group(1)):
            fqn = qualified.group(1) + simple
            if fqn not in added:
                added.append(fqn)
    # RICK's argument matchers (any(), anyFloat(), eq(...)) are used unqualified
    if re.search(r"(?<![\w.])(any\w*|eq)\(", source) \
            and "import static org.mockito.ArgumentMatchers.*;" not in source:
        added.append("static org.mockito.ArgumentMatchers.*")
    if added:
        lines = "".join(f"import {fqn};\n" for fqn in added)
        source = re.sub(r"(package [^;]+;\n)", lambda m: m.group(1) + lines, source, count=1)
    return source, added


def fix_source(source: str, generated_class: str, out_class: str) -> tuple[str, list[str]]:
    """The generated class under the output class name, with the compile fixes of docs/tools.md."""
    source = re.sub(rf"\bclass\s+{re.escape(generated_class)}\b", f"class {out_class}", source, count=1)
    source, added = add_missing_imports(source)
    source = drop_unused_imports(source)
    source = drop_unused_expected_object(source)
    # unused private helpers are an error under JGit's ecj settings: make them package-private
    source = re.sub(r"\n    private (<T> T deserializeObjectFrom\w+\()", r"\n    \1", source)
    return source, added


def place_outputs(ctx: ToolContext, tool: str, output: Path) -> dict[str, Any]:
    """Install the generated class as the output test file, its object files as test resources and the
    test dependencies it needs in the module pom. Returns the files the cell's result consists of."""
    simple = ctx.case.target["class"].rsplit(".", 1)[-1]
    generated = sorted((output / "generated").rglob(f"Test{simple}PanktiGen.java"))
    if not generated:
        return {"generated": False, "support_files": []}
    source, added_imports = fix_source(generated[0].read_text(encoding="utf-8"), f"Test{simple}PanktiGen",
                                       output_class(ctx.output_test_file))
    write_output(ctx, source)
    module = maven_module_root(ctx.workspace, ctx.output_test_file)
    resources = test_resources_dir(module)
    support: list[str] = []
    object_data = output / "object-data"
    if object_data.is_dir():
        resources.mkdir(parents=True, exist_ok=True)
        for f in sorted(object_data.iterdir()):
            if f.is_file() and f.name in source:
                shutil.copy2(f, resources / f.name)
                support.append(str((resources / f.name).relative_to(ctx.workspace)))
    pom = module / "pom.xml"
    deps = [XSTREAM] + ([MOCKITO, MOCKITO_JUPITER] if tool == "rick" else [])
    if not uses_junit5(pom):
        deps.append(JUNIT_JUPITER)
    before = pom.read_text(encoding="utf-8")
    added = add_test_dependencies(pom, deps)
    if tool == "rick":
        pin_mockito(pom)
    if pom.read_text(encoding="utf-8") != before:
        support.append(str(pom.relative_to(ctx.workspace)))
    return {"generated": True, "support_files": support, "added_dependencies": added,
            "added_imports": added_imports, "object_files": sum(1 for s in support if s.endswith(".xml") and not s.endswith("pom.xml"))}


def run(ctx: ToolContext, tool: str, timeout: float) -> dict[str, Any]:
    """Run Pankti or RICK on the cell; returns {generated, support_files, outcome, ...} (meta in ctx)."""
    meta = ctx.meta
    log = ctx.log()
    jars = _jars(ctx)
    if not all(j.is_file() for j in jars.values()):
        raise ToolError(f"Pankti/RICK are not built in {ctx.ext} (scripts/setup_external_tools.sh)")
    env17 = tool_env(ctx.env, ctx.jdk17)
    m2 = f"-Dmaven.repo.local={ctx.ext / 'm2'}"
    project = ctx.workspace
    module = target_module(project, ctx.case.target["class"])
    meta["analysed_module"] = str(module.relative_to(project)) or "."
    # Spoon writes spoon.classpath*.tmp into the project it analyses: give it a copy of the module
    analysis = ctx.work / "project"
    shutil.copytree(module, analysis, symlinks=True, ignore=shutil.ignore_patterns(".git", "target", "build"))
    if module != project and (project / "pom.xml").is_file():
        shutil.copy2(project / "pom.xml", ctx.work / "pom.xml")  # the module pom inherits from it
    extract_dir = ctx.work / "extract"
    extract_dir.mkdir()
    mark = log.stat().st_size if log.is_file() else 0
    rc = run_step([str(ctx.jdk17 / "bin/java"), "-Xmx6g", f"-Dpankti.compliance={SOURCE_COMPLIANCE}",
                   "-jar", str(jars["extract"]), "--void", str(analysis)], extract_dir, env17, log, 1800)
    extracted = next(extract_dir.glob("extracted-methods-*.csv"), None)
    if rc != 0 or extracted is None:
        raise ToolError(f"pankti-extract failed: {first_exception(log, mark) or f'exit code {rc}'}")
    row = select_target_row(extracted, ctx.case.target)
    meta["target_extracted"] = row is not None
    if row is None:
        meta["outcome"] = "target method is not a candidate of pankti-extract (public, non-static, non-abstract)"
        return {"generated": False, "support_files": []}
    meta["has_mockable_invocations"] = row.get("has-mockable-invocations") == "true"
    if tool == "rick" and not meta["has_mockable_invocations"]:
        meta["outcome"] = "target method has no mockable invocations (RICK candidate rule)"
        return {"generated": False, "support_files": []}
    single = ctx.work / "target-method.csv"
    _write_single_row_csv(extracted, row, single)

    # the Glowroot plugin with an aspect for the target method (and, for RICK, its mockable calls)
    instrument = ctx.work / "pankti-instrument"
    shutil.copytree(ctx.ext / "pankti" / "pankti-instrument", instrument, ignore=shutil.ignore_patterns("target"))
    script = "instrument-mock.py" if tool == "rick" else "instrument.py"
    argv = [str(ctx.ext / "venv/bin/python"), script, str(single)] + ([ctx.case.target["class"]] if tool == "rick" else [])
    if run_step(argv, instrument, env17, log, 300):
        raise ToolError(f"{script} failed")
    if run_step(["mvn", "-q", "-B", "-o", "package", "-DskipTests", "-Dcheckstyle.skip", "-Dmaven.javadoc.skip", m2],
                instrument, env17, log, 900):
        raise ToolError("cannot build the Glowroot plugin")
    plugin = instrument / "target/pankti-instrument-1.0-SNAPSHOT-jar-with-dependencies.jar"
    home = ctx.work / "glowroot"
    shutil.copytree(ctx.ext / "glowroot", home)
    (home / "plugins").mkdir(exist_ok=True)
    shutil.copy2(plugin, home / "plugins" / plugin.name)

    storage = ctx.work / "object-data"
    storage.mkdir()
    agent = f"-javaagent:{home / 'glowroot.jar'} -Dpankti.storage.dir={storage} {ADD_OPENS}"
    env_rec = dict(ctx.env)
    env_rec.pop("JDK_JAVA_OPTIONS", None)
    record_home = record_java_home(ctx)
    meta["record_java_home"] = str(record_home)
    meta["scenario_exit_code"] = run_step(record_argv(ctx, record_home, agent), ctx.workspace, env_rec,
                                          ctx.log("record.log"), timeout)
    meta["recorded"] = {p.name: p.read_text().strip() for p in sorted(storage.glob("*-count.txt"))}
    if not (storage / "invoked-methods.csv").is_file():
        meta["outcome"] = "the target method was not recorded"
        return {"generated": False, "support_files": []}

    out = ctx.work / f"{tool}-generate"
    out.mkdir()
    mark = log.stat().st_size
    rc = run_step([str(ctx.jdk17 / "bin/java"), "-Xmx6g", f"-Dpankti.compliance={SOURCE_COMPLIANCE}",
                   "-jar", str(jars[tool]), str(analysis), str(storage / "invoked-methods.csv"), f"{storage}/"],
                  out, env17, log, 1800)
    if rc != 0:
        raise ToolError(f"{tool} generation failed: {first_exception(log, mark) or f'exit code {rc}'}")
    placed = place_outputs(ctx, tool, out / "output")
    meta["outcome"] = "generated" if placed["generated"] else "no test class generated"
    return placed
