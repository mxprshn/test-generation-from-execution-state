"""Komar of the komar mode: the commands the agent may run, the run wrapper and the journal of a cell.

The agent writes Komar specs to komar/NAME.yaml in the workspace, runs the driving scenario under Komar
and queries the JSONL output with jq (prompts/task-komar.txt). pi/guard.ts accepts exactly two command
forms besides the named case commands, checked word by word (command_kind; the guard has the same
rules in TypeScript):

    komar run komar/NAME.yaml -o komar/NAME.jsonl -- <scenario command of the case>
    jq [options] 'FILTER' komar/NAME.jsonl ...

`komar` on the PATH of the agent's commands is a wrapper (this module, `wrapper`): it counts the
scenario runs of the cell and refuses one beyond `debug_runs`, runs the pinned Komar binary with its
agent jar, and journals every call to komar-log.jsonl. A spec with errors (Komar exit code 2, nothing
started) is journalled and does not count as a run. A spec with `calls: true` (expressions that call
methods of the program) is refused the same way: as with the debugger, no code of the program runs
for the observation.

Komar itself is installed by scripts/setup_komar.sh into paths.komar (default <tools>/komar):
bin/komar, komar-agent.jar, versions.json.
"""
from __future__ import annotations

import json
import os
import re
import shlex
import signal
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Any

from runner.config import ROOT, Config

SCRATCH = "komar"
NAME = r"[A-Za-z0-9_-][A-Za-z0-9._-]*"
SPEC = re.compile(rf"^{SCRATCH}/{NAME}\.yaml$")
OUTPUT = re.compile(rf"^{SCRATCH}/{NAME}\.jsonl$")
SAFE = re.compile(r"[A-Za-z0-9_@%+=:,./#-]")
JQ_FLAGS = re.compile(r"^-[crsnSej]+$")
JQ_LONG_FLAGS = {"--compact-output", "--raw-output", "--slurp", "--null-input", "--sort-keys", "--tab",
                 "--exit-status", "--join-output"}
JQ_MODULES = re.compile(r"\b(?:import|include)\b")
REFUSED = "komar: limit of {n} scenario runs reached; write the test from the data you have"


class KomarError(Exception):
    pass


def shell_words(command: str) -> list[str] | None:
    """The words bash reads from `command`, if bash reads them literally: plain words of safe characters,
    '...' and "..." without $, backquote, backslash or !; None for anything else (operators,
    expansions, globs, comments, line breaks)."""
    if re.search(r"[\r\n\u2028\u2029]", command):
        return None
    words: list[str] = []
    cur: str | None = None
    s = command.strip()
    i = 0
    while i < len(s):
        c = s[i]
        if c in " \t":
            if cur is not None:
                words.append(cur)
                cur = None
            i += 1
        elif c == "'":
            j = s.find("'", i + 1)
            if j < 0:
                return None
            cur = (cur or "") + s[i + 1:j]
            i = j + 1
        elif c == '"':
            j = s.find('"', i + 1)
            if j < 0 or re.search(r"[$`\\!]", s[i + 1:j]):
                return None
            cur = (cur or "") + s[i + 1:j]
            i = j + 1
        elif SAFE.match(c) and not (c == "#" and cur is None):
            cur = (cur or "") + c
            i += 1
        else:
            return None
    if cur is not None:
        words.append(cur)
    return words


def command_kind(command: str, scenario: list[str]) -> str | None:
    """"komar" or "jq" for a command of the two allowed forms, None otherwise."""
    words = shell_words(command)
    if not words:
        return None
    if words[0] == "komar":
        ok = (len(words) > 6 and words[1] == "run" and SPEC.match(words[2]) and words[3] == "-o"
              and OUTPUT.match(words[4]) and words[5] == "--" and words[6:] == list(scenario))
        return "komar" if ok else None
    if words[0] == "jq":
        i, null_input, rest = 1, False, words[1:]
        while i < len(words) and words[i].startswith("-"):
            w = words[i]
            if w in ("--arg", "--argjson"):
                if i + 2 >= len(words) or not re.match(r"^[A-Za-z_][A-Za-z0-9_]*$", words[i + 1]):
                    return None
                i += 3
                continue
            if not (JQ_FLAGS.match(w) or w in JQ_LONG_FLAGS):
                return None
            null_input = null_input or w == "--null-input" or (not w.startswith("--") and "n" in w)
            i += 1
        rest = words[i:]
        if not rest or JQ_MODULES.search(rest[0]):
            return None
        files = rest[1:]
        if not files and not null_input:
            return None
        return "jq" if all(OUTPUT.match(f) for f in files) else None
    return None


def scenario_text(scenario: list[str]) -> str:
    return " ".join(shlex.quote(a) for a in scenario)


# ---------------------------------------------------------------------------------------------
# installation and the cell set-up (runner side)

def installation(cfg: Config) -> dict[str, Path]:
    d = cfg.komar_dir
    paths = {"binary": d / "bin" / "komar", "jar": d / "komar-agent.jar", "versions": d / "versions.json"}
    missing = [str(p) for p in paths.values() if not p.is_file()]
    if missing:
        raise KomarError(f"Komar is not installed (scripts/setup_komar.sh): missing {', '.join(missing)}")
    return paths


def versions(cfg: Config) -> dict[str, Any] | None:
    path = cfg.komar_dir / "versions.json"
    return json.loads(path.read_text()) if path.is_file() else None


def prepare(cfg: Config, workspace: Path, agent_dir: Path, scenario: list[str]) -> Path:
    """The scratch directory, the wrapper on a PATH entry of its own and its configuration. Returns the
    directory to put first on PATH."""
    inst = installation(cfg)
    (workspace / SCRATCH).mkdir(exist_ok=True)
    config = agent_dir / "komar.json"
    config.write_text(json.dumps({
        "workspace": str(workspace), "binary": str(inst["binary"]), "jar": str(inst["jar"]),
        "scenario": scenario, "runs": cfg.debug_runs, "log": str(agent_dir / "komar-log.jsonl"),
        "state": str(agent_dir / "komar-runs"), "pids": str(agent_dir / "komar-pids.txt"),
    }, indent=2))
    bin_dir = agent_dir / "komar-bin"
    bin_dir.mkdir(exist_ok=True)
    wrapper = bin_dir / "komar"
    wrapper.write_text(f"#!/bin/sh\nPYTHONPATH={shlex.quote(str(ROOT))} exec {shlex.quote(sys.executable)} -m runner.komar "
                       f"{shlex.quote(str(config))} \"$@\"\n")
    wrapper.chmod(0o755)
    return bin_dir


def kill_leftovers(pids_file: Path) -> None:
    """Kill the process groups of Komar runs still alive (after a timeout of the agent)."""
    if not pids_file.is_file():
        return
    for line in pids_file.read_text().split():
        try:
            os.killpg(int(line), signal.SIGKILL)
        except (ValueError, ProcessLookupError, PermissionError, OSError):
            continue


def read_log(path: Path) -> list[dict[str, Any]]:
    rows = []
    if path.is_file():
        for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
            try:
                row = json.loads(line)
            except json.JSONDecodeError:
                continue
            if isinstance(row, dict):
                rows.append(row)
    return rows


def log_summary(rows: list[dict[str, Any]], commands: list[dict[str, Any]], run_limit: int) -> dict[str, Any]:
    """Counts of a cell: scenario runs, spec errors, refused runs, hits and diagnostics of the runs,
    output size, jq queries (from the guard's command log)."""
    runs = [r for r in rows if r.get("event") == "run"]
    diag: dict[str, int] = {}
    for r in runs:
        for level, n in (r.get("diag") or {}).items():
            diag[level] = diag.get(level, 0) + n
    jq = [e for e in commands if e.get("event") == "end" and e.get("name") == "jq"]
    return {
        "runs": len(runs), "run_limit": run_limit,
        "spec_errors": sum(1 for r in rows if r.get("event") == "spec_error"),
        "refused": sum(1 for r in rows if r.get("event") == "refused"),
        "runs_without_end": sum(1 for r in runs if not r.get("end")),
        "scenario_failed_runs": sum(1 for r in runs if (r.get("end") or {}).get("code") not in (0, None)),
        "hits": sum(sum((r.get("hits") or {}).values()) for r in runs),
        "diag": dict(sorted(diag.items())),
        "output_bytes": sum(int(r.get("output_bytes") or 0) for r in runs),
        "jq_queries": len(jq), "jq_failed": sum(1 for e in jq if not e.get("succeeded")),
    }


# ---------------------------------------------------------------------------------------------
# the wrapper (runs as `komar` in the agent's commands)

def calls_enabled(spec_text: str | None) -> bool:
    import yaml
    try:
        data = yaml.safe_load(spec_text or "")
    except yaml.YAMLError:
        return False  # Komar reports the syntax error itself
    return isinstance(data, dict) and data.get("calls") not in (None, False)


def _output_summary(path: Path) -> dict[str, Any]:
    out: dict[str, Any] = {"output_bytes": path.stat().st_size if path.is_file() else 0, "hits": {}, "diag": {},
                           "end": None}
    if not path.is_file():
        return out
    with path.open(encoding="utf-8", errors="replace") as f:
        for line in f:
            try:
                rec = json.loads(line)
            except json.JSONDecodeError:
                continue
            kind = rec.get("kind")
            if kind == "diag":
                level = str(rec.get("level"))
                out["diag"][level] = out["diag"].get(level, 0) + 1
            elif kind == "end":
                out["end"] = {k: rec.get(k) for k in ("reason", "code", "dropped", "when_errors")}
                out["hits"] = rec.get("hits") or {}
    return out


def _record_children(pid: int, pids_file: Path, stop: threading.Event) -> None:
    """Komar starts the scenario in a process group of its own: note those groups for kill_leftovers."""
    seen: set[str] = set()
    while not stop.is_set():
        try:
            children = Path(f"/proc/{pid}/task/{pid}/children").read_text().split()
        except OSError:
            return
        for c in children:
            if c not in seen:
                seen.add(c)
                with pids_file.open("a") as f:
                    f.write(f"{c}\n")
        stop.wait(0.5)


def wrapper(config_path: str, argv: list[str]) -> int:
    cfg = json.loads(Path(config_path).read_text())
    log = Path(cfg["log"])
    state = Path(cfg["state"])
    ws = Path(cfg["workspace"])

    def journal(entry: dict[str, Any]) -> None:
        with log.open("a", encoding="utf-8") as f:
            f.write(json.dumps({"timestamp": time.time(), **entry}) + "\n")

    command = "komar " + " ".join(shlex.quote(a) for a in argv)
    if command_kind(command, cfg["scenario"]) != "komar":
        journal({"event": "rejected", "argv": argv})
        print("komar: only `komar run komar/NAME.yaml -o komar/NAME.jsonl -- <scenario command>` is available",
              file=sys.stderr)
        return 2
    spec, output = argv[1], argv[3]
    spec_text = (ws / spec).read_text(encoding="utf-8", errors="replace") if (ws / spec).is_file() else None
    if calls_enabled(spec_text):
        journal({"event": "spec_error", "spec": spec, "spec_text": spec_text, "exit_code": 2, "seconds": 0,
                 "reason": "calls"})
        print(f"komar: {spec}: calls: true is not available here; values are read from fields", file=sys.stderr)
        return 2
    runs = int(state.read_text()) if state.is_file() else 0
    if runs >= int(cfg["runs"]):
        journal({"event": "refused", "spec": spec, "runs": runs, "limit": cfg["runs"]})
        print(REFUSED.format(n=cfg["runs"]), file=sys.stderr)
        return 1
    env = dict(os.environ, KOMAR_AGENT_JAR=cfg["jar"])
    started = time.time()
    proc = subprocess.Popen([cfg["binary"], *argv], cwd=ws, env=env, start_new_session=True)
    with Path(cfg["pids"]).open("a") as f:
        f.write(f"{proc.pid}\n")
    stop = threading.Event()
    watcher = threading.Thread(target=_record_children, args=(proc.pid, Path(cfg["pids"]), stop), daemon=True)
    watcher.start()
    try:
        code = proc.wait()
    except BaseException:
        os.killpg(proc.pid, signal.SIGTERM)
        raise
    finally:
        stop.set()
    seconds = round(time.time() - started, 1)
    if code == 2:  # the spec was rejected before anything started
        journal({"event": "spec_error", "spec": spec, "spec_text": spec_text, "exit_code": code, "seconds": seconds})
        return code
    runs += 1
    state.write_text(str(runs))
    journal({"event": "run", "run": runs, "spec": spec, "spec_text": spec_text, "output": output, "exit_code": code,
             "seconds": seconds, **_output_summary(ws / output)})
    return code


if __name__ == "__main__":
    raise SystemExit(wrapper(sys.argv[1], sys.argv[2:]))
