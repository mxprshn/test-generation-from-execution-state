"""MicroTestCarver (Deljouyi and Zaidman, SCAM 2023) as a mode without an agent.

The tool records the scenario with its modified BTrace and generates unit tests from the trace with its
Velocity templates; the replication package of the authors is used with build-only patches
(external/mtc/*.patch). The target comes from case.yaml, the workload is the case's scenario command; the
generated class of the target becomes the output test file.

Constraints of the tool that are kept (docs/tools.md): the modified BTrace records only on JDK 11 (the JVM
aborts on retransformation on JDK 17+), so projects whose tests need a newer JDK cannot be recorded; its
bytecode fails verification, so the scenario JVM runs with -XX:-BytecodeVerificationRemote; the abort is
intermittent on JDK 11 as well, so the recording is retried up to three times.
"""
from __future__ import annotations

import re
import shutil
from pathlib import Path
from typing import Any

from runner.tools.common import (ADD_OPENS, HAMCREST, first_exception, JUNIT_JUPITER, MOCKITO, MOCKITO_JUPITER, ToolContext,
                                 ToolError, add_test_dependencies, drop_unused_imports, maven_module_root,
                                 output_class, record_argv, run_step, target_module, tool_env, uses_junit5,
                                 write_output)

RECORD_RETRIES = 3


def _dist(ctx: ToolContext) -> Path:
    return ctx.ext / "mtc/btrace/btrace-dist/build/resources/main/v2.3.0-SNAPSHOT"


def _carver_jar(ctx: ToolContext) -> Path:
    return ctx.ext / "mtc/carver/build/libs/carving-1.0-SNAPSHOT-all.jar"


def compile_probe(ctx: ToolContext, method_pattern: str) -> Path:
    """The authors' CallTrace script with the class and method patterns filled in."""
    source = (ctx.ext / "mtc/carver/src/main/java/trace/CallTrace.java").read_text(encoding="utf-8")
    class_pattern = "/^" + re.escape(ctx.case.target["class"]).replace("\\", "\\\\") + "$/"
    source = source.replace('"${packageName}/"', f'"{class_pattern}"')
    source = source.replace('"${methodName}/"', f'"{method_pattern}"')
    source = source.replace('"{methodName}/"', f'"{method_pattern}"')
    probe = ctx.work / "probe"
    shutil.rmtree(probe, ignore_errors=True)
    probe.mkdir(parents=True)
    (probe / "CallTrace.java").write_text(source, encoding="utf-8")
    rc = run_step([str(_dist(ctx) / "bin/btracec"), "-cp", str(_dist(ctx) / "libs/btrace-client.jar"),
                   "CallTrace.java"], probe, tool_env(ctx.env, ctx.jdk17), ctx.log(), 300)
    compiled = probe / "trace" / "CallTrace.class"
    if rc or not compiled.is_file():
        raise ToolError("cannot compile the BTrace probe")
    return compiled


def record(ctx: ToolContext, probe: Path, trace: Path, log: Path, timeout: float) -> tuple[int, Path]:
    agent = (
        "-Xshare:off -XX:+UnlockDiagnosticVMOptions -XX:-BytecodeVerificationRemote "
        f"-javaagent:{_dist(ctx) / 'libs/btrace-agent.jar'}=scriptOutputFile={trace},trusted=true,script={probe} "
        "--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED "
        "--add-exports java.base/jdk.internal.reflect=ALL-UNNAMED "
        f"{ADD_OPENS}"
    )
    env = dict(ctx.env)
    env.pop("JDK_JAVA_OPTIONS", None)
    # -Djacoco.skip: with verification disabled the JVM aborts when JaCoCo and BTrace both rewrite a class
    rc = run_step(record_argv(ctx, ctx.jdk11, agent, ["-Djacoco.skip"]), ctx.workspace, env, log, timeout)
    # BTrace writes the object pool to ./pool of the test JVM (the module directory)
    pools = [p for p in ctx.workspace.rglob("pool") if p.is_dir() and any(p.glob("*.xml"))]
    return rc, (pools[0] if pools else ctx.workspace / "pool")


def generate(ctx: ToolContext, sources: Path, classes: list[Path], trace: Path, pool: Path) -> Path:
    resources = ctx.work / "parse-resources" / "trace-output"
    shutil.rmtree(resources.parent, ignore_errors=True)
    resources.mkdir(parents=True)
    shutil.copy2(trace, resources / "trace.btrace")
    shutil.copytree(pool, resources / "pool")
    out = ctx.work / "generate"
    shutil.rmtree(out, ignore_errors=True)
    (out / "test-output").mkdir(parents=True)
    classpath = ":".join([str(resources.parent), str(_carver_jar(ctx)), *map(str, classes)])
    mark = ctx.log().stat().st_size if ctx.log().is_file() else 0
    rc = run_step([str(ctx.jdk17 / "bin/java"), *ADD_OPENS.split(), "-cp", classpath, "Starter",
                   "parser", "-f", "trace.btrace", "-i", str(sources), "-p", "pool", "-t", "xml", "parse"],
                  out, tool_env(ctx.env, ctx.jdk17), ctx.log(), 1800)
    if rc:
        raise ToolError(f"MicroTestCarver generation failed: {first_exception(ctx.log(), mark) or f'exit code {rc}'}")
    return out / "test-output"


def fix_source(source: str, target_class: str, out_class: str) -> str:
    simple = target_class.rsplit(".", 1)[-1]
    source = re.sub(rf"\bclass\s+{re.escape(simple)}Test\b", f"class {out_class}", source, count=1)
    source = drop_unused_imports(source)
    # the template imports the class under test from its own package; strict compilers (JGit) reject it
    return re.sub(rf"(?m)^import {re.escape(target_class)};\n", "", source)


def place_output(ctx: ToolContext, output: Path) -> dict[str, Any]:
    target_class = ctx.case.target["class"]
    generated = output / f"{target_class.rsplit('.', 1)[-1]}Test.java"
    if not generated.is_file():
        return {"generated": False, "support_files": []}
    write_output(ctx, fix_source(generated.read_text(encoding="utf-8"), target_class,
                                 output_class(ctx.output_test_file)))
    pom = maven_module_root(ctx.workspace, ctx.output_test_file) / "pom.xml"
    before = pom.read_text(encoding="utf-8")
    deps = [MOCKITO, MOCKITO_JUPITER, HAMCREST] + ([] if uses_junit5(pom) else [JUNIT_JUPITER])
    added = add_test_dependencies(pom, deps)
    support = [str(pom.relative_to(ctx.workspace))] if pom.read_text(encoding="utf-8") != before else []
    return {"generated": True, "support_files": support, "added_dependencies": added}


def _module_paths(ctx: ToolContext) -> tuple[Path, list[Path]]:
    target_class = ctx.case.target["class"]
    module = target_module(ctx.workspace, target_class)
    relative = target_class.replace(".", "/") + ".java"
    source_file = next((p for p in module.rglob(relative) if "test" not in p.relative_to(module).parts[:2]), None)
    if source_file is None:
        raise ToolError("source of the target class not found")
    sources = source_file
    for _ in target_class.split("."):
        sources = sources.parent
    return sources, [p for p in ctx.workspace.rglob("target/classes") if p.is_dir()]


def run(ctx: ToolContext, timeout: float) -> dict[str, Any]:
    meta = ctx.meta
    if not (_dist(ctx) / "libs/btrace-agent.jar").is_file() or not _carver_jar(ctx).is_file():
        raise ToolError(f"MicroTestCarver is not built in {ctx.ext} (scripts/setup_external_tools.sh)")
    method = ctx.case.target["method"]
    trace = ctx.work / "trace.btrace"
    # the trace covers the target method and the constructors of its class (the generator reuses the
    # recorded constructor arguments); if BTrace breaks the bytecode (VerifyError) it is narrowed to the
    # target method alone
    attempts = [("target-and-constructors", f"/^({re.escape(method)}|<init>)$/"), ("target", f"/^{re.escape(method)}$/")]
    rc, pool, verify, crashed = 0, ctx.workspace / "pool", False, False
    for scope, pattern in attempts:
        probe = compile_probe(ctx, pattern)
        for attempt in range(1, RECORD_RETRIES + 1):
            trace.unlink(missing_ok=True)
            name = f"record-{scope}-{attempt}.log"
            rc, pool = record(ctx, probe, trace, ctx.log(name), timeout)
            text = ctx.log(name).read_text(encoding="utf-8", errors="replace")
            crashed = "Rewriting method not allowed" in text
            verify = "java.lang.VerifyError" in text
            meta.setdefault("record_attempts", []).append(
                {"scope": scope, "attempt": attempt, "exit_code": rc, "verify_error": verify, "jvm_abort": crashed})
            if not crashed:
                break
            shutil.rmtree(pool, ignore_errors=True)
        if not verify:
            break
        shutil.rmtree(pool, ignore_errors=True)
    meta["trace_scope"] = scope
    meta["scenario_exit_code"] = rc
    if crashed:
        # every attempt aborted: the trace is incomplete and the object pool is gone
        meta["outcome"] = f"the recording JVM aborted under BTrace in all {RECORD_RETRIES} attempts"
        return {"generated": False, "support_files": []}
    text = trace.read_text(encoding="utf-8", errors="replace") if trace.is_file() else ""
    meta["recorded_target_invocations"] = len(re.findall(rf"#{re.escape(method)}(?:\(.*\))?:\{{", text))
    meta["recorded_pool_objects"] = len(list(pool.glob("*.xml"))) if pool.is_dir() else 0
    if meta["recorded_target_invocations"] == 0:
        record_text = ctx.log(f"record-{scope}-1.log").read_text(encoding="utf-8", errors="replace")
        if re.search(r"UnsupportedClassVersionError|compiled by a more recent version of the Java Runtime|"
                     r"release version \d+ not supported|invalid target release", record_text):
            meta["outcome"] = "not recordable: the project's tests need a newer JDK than 11"
        else:
            meta["outcome"] = "the target method was not recorded"
        shutil.rmtree(pool, ignore_errors=True)
        return {"generated": False, "support_files": []}
    if not pool.is_dir():
        meta["outcome"] = "the recording wrote no object pool"
        return {"generated": False, "support_files": []}
    sources, classes = _module_paths(ctx)
    output = generate(ctx, sources, classes, trace, pool)
    shutil.copytree(pool, ctx.work / "pool", dirs_exist_ok=True)
    shutil.rmtree(pool, ignore_errors=True)
    placed = place_output(ctx, output)
    meta["outcome"] = "generated" if placed["generated"] else "no test class generated"
    return placed
