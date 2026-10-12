"""Trace the target method while a case command runs (BTrace, trusted mode).

The BTrace script (tracer/TargetTrace.java.tmpl) instruments only the target method (class, name and
exact type) and delegates to StateTrace (boot class path, tracer/StateTrace.java), which writes JSON
lines: enter (receiver and argument field graphs), call (signature of every outgoing call site in the
target body), return (result field graph) and throw (exit without a normal return).

The tracer is independent of the mechanism that recorded the execution data of the cases.

The agent is attached through JAVA_TOOL_OPTIONS, so it reaches every JVM the command starts. Only test
JVMs are recorded: the recorder stays silent in build launcher JVMs (Maven, the Maven wrapper), where
build plugins may load their own copy of the target class. Every trace file starts with a
{"ev":"jvm","main":...} line; read_trace() drops files of launcher JVMs as well.

Workspaces are copies of the compiled project checkouts (reflink where the file system supports it),
so Maven does not rebuild the project from scratch. The caller decides what goes into the copy.
"""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path
from typing import Any

HERE = Path(__file__).resolve().parent
TRACER_SRC = HERE / "tracer"
HELPER_SRC = TRACER_SRC / "StateTrace.java"
PRELOAD_SRC = TRACER_SRC / "TargetPreload.java"
SCRIPT_TMPL = TRACER_SRC / "TargetTrace.java.tmpl"
# BTrace 2.2.6 and the compiled helpers live under the tools directory (scripts/setup.sh, configure())
BTRACE_LIBS = HERE.parent / ".tools" / "btrace" / "libs"
BUILD = HERE.parent / ".tools" / "tracer-build"
DEFAULT_DEPTH = 5


def configure(tools_dir: Path) -> None:
    global BTRACE_LIBS, BUILD
    BTRACE_LIBS = Path(tools_dir) / "btrace" / "libs"
    BUILD = Path(tools_dir) / "tracer-build"

OPENS = [
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
]


def java_home() -> Path:
    value = os.environ.get("JAVA_HOME")
    if not value:
        raise RuntimeError("JAVA_HOME is not set (JDK 21)")
    return Path(value)


def _java(tool: str) -> str:
    return str(java_home() / "bin" / tool)


def _build_jar(src: Path, jar: Path, manifest: str | None = None) -> Path:
    if jar.is_file() and jar.stat().st_mtime >= src.stat().st_mtime:
        return jar
    out = BUILD / jar.stem
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)
    subprocess.run([_java("javac"), "--release", "11", "-d", str(out), str(src)], check=True)
    if manifest:
        mf = BUILD / f"{jar.stem}.mf"
        mf.write_text(manifest)
        subprocess.run([_java("jar"), "cfm", str(jar), str(mf), "-C", str(out), "."], check=True)
    else:
        subprocess.run([_java("jar"), "cf", str(jar), "-C", str(out), "."], check=True)
    return jar


def ensure_helper() -> Path:
    return _build_jar(HELPER_SRC, BUILD / "state-trace.jar")


def ensure_preload() -> Path:
    """Agent jar that loads the target class at start-up (see TargetPreload.java)."""
    return _build_jar(PRELOAD_SRC, BUILD / "target-preload.jar", "Premain-Class: TargetPreload\n")


def _strip_generics(t: str) -> str:
    out, depth = [], 0
    for ch in t:
        if ch == "<":
            depth += 1
        elif ch == ">":
            depth -= 1
        elif depth == 0:
            out.append(ch)
    return "".join(out).strip()


def _split_top(s: str) -> list[str]:
    parts, depth, cur = [], 0, []
    for ch in s:
        if ch == "<":
            depth += 1
        elif ch == ">":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    parts.append("".join(cur))
    return [p.strip() for p in parts if p.strip()]


def parse_signature(signature: str) -> tuple[list[str], str]:
    """'(a.B, int) -> c.D' -> (['a.B', 'int'], 'c.D')."""
    m = re.match(r"\s*\((.*)\)\s*->\s*(.+)\s*$", signature)
    if not m:
        raise ValueError(f"unsupported signature: {signature}")
    params = [_strip_generics(p) for p in _split_top(m.group(1))] if m.group(1).strip() else []
    return params, _strip_generics(m.group(2))


def compile_script(target_class: str, method: str, signature: str, out_dir: Path) -> Path:
    """Render and compile the BTrace script for one target; returns the .class path."""
    if not (BTRACE_LIBS / "btrace-agent.jar").is_file():
        raise RuntimeError(f"BTrace not found in {BTRACE_LIBS} (run scripts/setup.sh)")
    params, ret = parse_signature(signature)
    src = (SCRIPT_TMPL.read_text()
           .replace("__CLASS__", target_class)
           .replace("__METHOD__", method)
           .replace("__TYPE__", f"{ret} ({', '.join(params)})")
           .replace("__VOID__", "true" if ret == "void" else "false"))
    out_dir.mkdir(parents=True, exist_ok=True)
    java_file = out_dir / "TargetTrace.java"
    java_file.write_text(src)
    helper = ensure_helper()
    libs = [BTRACE_LIBS / "btrace-client.jar", BTRACE_LIBS / "btrace-agent.jar", BTRACE_LIBS / "btrace-boot.jar", helper]
    res = subprocess.run(
        [_java("java"), "-XX:+IgnoreUnrecognizedVMOptions",
         "--add-exports", "jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
         "-cp", ":".join(str(p) for p in libs), "org.openjdk.btrace.compiler.Compiler",
         "-cp", f"{BTRACE_LIBS / 'btrace-boot.jar'}:{helper}", "-d", str(out_dir), str(java_file)],
        capture_output=True, text=True)
    cls = out_dir / "TargetTrace.class"
    if not cls.is_file():
        raise RuntimeError(f"btrace compile failed:\n{res.stdout[-2000:]}\n{res.stderr[-2000:]}")
    return cls


def agent_options(script_class: Path, trace_dir: Path, depth: int = DEFAULT_DEPTH,
                  preload_class: str | None = None) -> str:
    helper = ensure_helper()
    preload = [f"-javaagent:{ensure_preload()}={preload_class}"] if preload_class else []
    agent = (f"-javaagent:{BTRACE_LIBS / 'btrace-agent.jar'}=noServer=true,stdout=false,trusted=true,"
             f"script={script_class},scriptOutputFile={trace_dir / 'btrace.log'}")
    return " ".join([*OPENS, f"-Xbootclasspath/a:{helper}", f"-Dstate.trace.dir={trace_dir}",
                     f"-Dstate.trace.depth={depth}", "-XX:+IgnoreUnrecognizedVMOptions", "-Xshare:off",
                     agent, *preload])


def clone_checkout(source: Path, dest: Path) -> Path:
    """Copy of a compiled checkout (keeps mtimes, so Maven reuses target/)."""
    if dest.exists():
        shutil.rmtree(dest)
    dest.parent.mkdir(parents=True, exist_ok=True)
    clone = ["cp", "-cRp"] if sys.platform == "darwin" else ["cp", "-a", "--reflink=auto"]
    res = subprocess.run([*clone, str(source), str(dest)], capture_output=True, text=True)
    if res.returncode != 0:
        shutil.rmtree(dest, ignore_errors=True)
        subprocess.run(["cp", "-Rp", str(source), str(dest)], check=True)
    return dest


def remove_paths(workspace: Path, rels: list[str]) -> None:
    """Remove hidden sources and their compiled classes from a copied checkout."""
    for rel in rels or []:
        p = workspace / rel
        if p.is_dir():
            shutil.rmtree(p)
        elif p.exists():
            p.unlink()
        if rel.endswith(".java"):
            stem = Path(rel).stem
            for cls in workspace.rglob(f"{stem}*.class"):
                if (cls.name == f"{stem}.class" or cls.name.startswith(f"{stem}$")) and "target" in cls.parts:
                    cls.unlink()


def run_command(argv: list[str], cwd: Path, env: dict[str, str], trace_dir: Path, script_class: Path,
                timeout: int = 1800, depth: int = DEFAULT_DEPTH, preload_class: str | None = None) -> dict[str, Any]:
    trace_dir.mkdir(parents=True, exist_ok=True)
    for old in trace_dir.glob("trace-*.jsonl"):
        old.unlink()
    env = dict(env)
    env["JAVA_TOOL_OPTIONS"] = agent_options(script_class, trace_dir, depth, preload_class)
    argv = list(argv)
    if argv and (cwd / argv[0]).is_file():
        argv[0] = str(cwd / argv[0])
    started = time.monotonic()
    log = trace_dir / "command.log"
    try:
        with log.open("wb") as handle:
            rc = subprocess.run(argv, cwd=cwd, env=env, stdout=handle, stderr=subprocess.STDOUT,
                                timeout=timeout).returncode
    except subprocess.TimeoutExpired:
        rc = "timeout"
    return {"exit_code": rc, "seconds": round(time.monotonic() - started, 1), "log": str(log)}


LAUNCHER_MAINS = ("org.codehaus.plexus.classworlds.", "org.apache.maven.wrapper.", "org.apache.maven.cli.",
                  "org.gradle.", "worker.org.gradle.")


def read_trace(trace_dir: Path) -> dict[str, Any]:
    """Assemble invocations from all JSON-lines files written in trace_dir (test JVMs only)."""
    invocations: dict[tuple[str, int], dict[str, Any]] = {}
    order: list[tuple[str, int]] = []
    errors: list[str] = []
    jvms: list[str] = []
    skipped: list[str] = []
    for f in sorted(trace_dir.glob("trace-*.jsonl")):
        lines = f.read_text(errors="replace").splitlines()
        head = next((ln for ln in lines if ln.strip()), "")
        try:
            first = json.loads(head) if head else {}
        except json.JSONDecodeError:
            first = {}
        if isinstance(first, dict) and first.get("ev") == "jvm":
            main = str(first.get("main") or "")
            if main.startswith(LAUNCHER_MAINS):
                skipped.append(main)
                continue
            jvms.append(main)
        for line in lines:
            if not line.strip():
                continue
            try:
                ev = json.loads(line)
            except json.JSONDecodeError:
                errors.append(f"bad json in {f.name}")
                continue
            if ev.get("ev") == "jvm":
                continue
            if ev.get("ev") == "error":
                errors.append(ev.get("msg", ""))
                continue
            key = (f.name, ev.get("id"))
            if ev["ev"] == "enter":
                invocations[key] = {"self": ev.get("self"), "args": ev.get("args", []), "calls": [],
                                    "outcome": None, "value": None, "thread": ev.get("thread")}
                order.append(key)
            elif key in invocations:
                inv = invocations[key]
                if ev["ev"] == "call":
                    inv["calls"].append(ev["sig"])
                elif ev["ev"] == "return" and inv["outcome"] is None:
                    inv["outcome"] = "return"
                    inv["value"] = ev.get("value")
                    inv["void"] = ev.get("void")
                elif ev["ev"] == "throw" and inv["outcome"] is None:
                    inv["outcome"] = "throw"
    result = [invocations[k] for k in order]
    for inv in result:
        if inv["outcome"] is None:
            inv["outcome"] = "throw"
    return {"invocations": result, "errors": errors, "files": len(list(trace_dir.glob("trace-*.jsonl"))),
            "jvms": jvms, "skipped_launcher_files": skipped}


def trace_case_command(case, workspace: Path, command: str, out_dir: Path, env: dict[str, str],
                       timeout: int = 1800, depth: int = DEFAULT_DEPTH) -> dict[str, Any]:
    """Trace one case command (`scenario` or `generated_test`) in a prepared workspace."""
    script = compile_script(case.target["class"], case.target["method"], case.target["signature"],
                            out_dir / "script")
    run = run_command(case.commands[command], workspace, env, out_dir / "trace", script,
                      timeout, depth, preload_class=case.target["class"])
    trace = read_trace(out_dir / "trace")
    trace["run"] = run
    return trace
