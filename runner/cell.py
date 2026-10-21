"""Generate one cell: a case, a mode and a run id.

    workspace   exported project commit, hidden tests removed, a single-commit git repository;
                dump/rendered/rendered-raw also get the case's workspace-files/ (resources the
                recorded data refer to) and, for dump/rendered, the execution input under carving/
    scenario    the driving scenario must run in the workspace (otherwise an infrastructure failure;
                if it runs only without workspace-files/, the input is a method failure)
    generation  Pi with the rendered prompts (baseline, dump, rendered, autonomous: with the debugger
                tools of pi/debugger.ts over the driving scenario), or the preliminary test
                installed as the output class (rendered-raw, no agent)
    result      result.diff: the generated class (and workspace-files/) against the original commit

Status: completed (the agent finished and left the test class), timed_out, failed, method_failed
(the mode has no input for the case). Whether the test compiles and passes is decided later, on a
clean copy, by evaluation.evaluate (verify).

Output: <results>/cells/<case>/<mode>/<run-id>/ summary.json, result.diff, prompts/, artifacts/.
"""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from runner import agent as A
from runner import workspace as W
from runner.cases import Case, eval_command, load_case, load_projects, project_checkout
from runner.config import AGENT_COMMANDS, AGENT_MODES, Config, neutral_name
from runner.process import resolve_argv, run
from runner.prompts import EXECUTION_INPUT, render


def now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def debugger_input(case: Case) -> dict:
    """What pi/debugger.ts runs: the command of the scenario check (eval_maven_args included, e.g. the
    spotless check skipped), so the agent's own test file cannot break a debugged run on a format check."""
    return {"scenario": eval_command(case, "scenario"), "target": case.target}


def _write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def _jsonl(path: Path, rows: list[dict]) -> None:
    _write(path, "".join(json.dumps(r, default=str) + "\n" for r in rows))


def retarget_test_class(source: str, output_test_file: str) -> str:
    """Rename the top-level class and package of the preliminary test to match the output file."""
    path = Path(output_test_file)
    parts = list(path.parent.parts)
    package = None
    for root in ("java", "kotlin", "groovy"):
        if root in parts:
            index = len(parts) - 1 - parts[::-1].index(root)
            package = ".".join(parts[index + 1:])
            break
    match = re.search(r"\b(?:public\s+|final\s+|abstract\s+)*class\s+(\w+)", source)
    if match is None:
        raise ValueError("preliminary test declares no class")
    text = re.sub(rf"\b{re.escape(match.group(1))}\b", path.stem, source)
    pkg = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
    if package is not None:
        text = pkg.sub(f"package {package};", text, count=1) if pkg.search(text) else (
            f"package {package};\n\n{text}" if package else text)
    return text


def final_failed_commands(entries: list[dict]) -> list[str]:
    """Agent commands whose last run failed (for the record; the status does not depend on it)."""
    last: dict[str, dict] = {}
    for e in entries:
        if e.get("event") == "end" and e.get("name"):
            last[e["name"]] = e
    return sorted(n for n, e in last.items() if not e.get("succeeded"))


def method_input(case: Case, mode: str) -> Path | None:
    return {"dump": case.dump, "rendered": case.preliminary_test, "rendered-raw": case.preliminary_test}.get(mode)


def generate(cfg: Config, case_id: str, mode: str, run_id: str, api_key_env: str) -> dict[str, Any]:
    case = load_case(case_id, cfg.cases_dir)
    project = load_projects()[case.project]
    checkout = project_checkout(cfg, case.project)
    cell = cfg.cell_dir(case_id, mode, run_id)
    if cell.exists():
        raise FileExistsError(f"cell already exists: {cell}")
    cell.mkdir(parents=True)
    started = time.time()
    summary: dict[str, Any] = {
        "case_id": case.id, "project": case.project, "group": case.group, "mode": mode, "run_id": run_id,
        "status": None, "phase": "preparation", "error": None, "started": now(),
        "model": cfg.model if mode in AGENT_MODES else None, "thinking": cfg.thinking if mode in AGENT_MODES else None,
        "provider": cfg.provider_name if mode in AGENT_MODES else None,
        "api_key_env": api_key_env if mode in AGENT_MODES else None, "timeout": cfg.timeout,
        "commit": project.commit, "output_test_file": case.output_test_file,
    }
    from evaluation.inputs import generation_inputs
    summary["inputs"] = generation_inputs(cfg, case, mode)
    # the directory name reveals neither the case nor the mode (it shows in build output)
    tmp = cfg.work_dir / "cells" / neutral_name(case_id, mode, run_id)
    shutil.rmtree(tmp, ignore_errors=True)
    try:
        return _generate(cfg, case, mode, checkout, project.commit, cell, tmp, summary, api_key_env)
    except Exception as exc:
        summary["status"] = "failed"
        summary["error"] = f"{type(exc).__name__}: {exc}"
        return summary
    finally:
        summary["seconds"] = round(time.time() - started, 1)
        summary["finished"] = now()
        _write(cell / "summary.json", json.dumps(summary, indent=2, default=str))
        shutil.rmtree(tmp, ignore_errors=True)


def scenario_check(cfg: Config, case: Case, ws: Path, env: dict[str, str], logs: Path, summary: dict[str, Any]):
    """The project's prebuild commands (if any), then the harness's own run of the driving scenario, run
    like the evaluation runs it (eval_maven_args: format checks off). Returns the result of the scenario."""
    for i, argv in enumerate(load_projects()[case.project].prebuild, start=1):
        pre = run(resolve_argv(list(argv), ws), ws, env, cfg.command_timeout, "prebuild")
        _write(logs / f"prebuild-{i}.log", pre.stdout[-20000:] + pre.stderr[-5000:])
        summary.setdefault("prebuild", []).append(pre.as_dict())
        if not pre.ok:
            raise RuntimeError(f"driving scenario does not run: prebuild command {i} of the project failed")
    check = run(resolve_argv(eval_command(case, "scenario"), ws), ws, env, cfg.command_timeout, "scenario")
    _write(logs / "scenario-check.log", check.stdout[-20000:] + check.stderr[-5000:])
    summary["scenario_check"] = check.as_dict()
    return check


def check_case_scenario(cfg: Config, case_id: str, logs: Path) -> dict[str, Any]:
    """The preparation of a cell without the agent: exported commit, hidden tests removed, a fresh git
    repository, prebuild and the scenario check (scripts/check_scenarios.py)."""
    case = load_case(case_id, cfg.cases_dir)
    project = load_projects()[case.project]
    ws = cfg.work_dir / "check" / neutral_name(case_id, "scenario-check")
    shutil.rmtree(ws, ignore_errors=True)
    summary: dict[str, Any] = {"case_id": case_id, "started": now()}
    started = time.time()
    try:
        W.export_commit(project_checkout(cfg, case.project), project.commit, ws)
        summary["hidden_paths_removed"] = W.hide_paths(ws, case.hidden_paths)
        W.init_repo(ws)
        check = scenario_check(cfg, case, ws, cfg.command_env(), logs, summary)
        summary["ok"] = check.ok
    except Exception as exc:
        summary["ok"] = False
        summary["error"] = f"{type(exc).__name__}: {exc}"
    finally:
        shutil.rmtree(ws, ignore_errors=True)
        summary["seconds"] = round(time.time() - started, 1)
    return summary


def _generate(cfg: Config, case: Case, mode: str, checkout: Path, commit: str, cell: Path, tmp: Path,
              summary: dict[str, Any], api_key_env: str) -> dict[str, Any]:
    source = method_input(case, mode)
    if mode not in ("baseline", "autonomous") and source is None:
        summary["status"] = "method_failed"
        summary["error"] = f"no {'call dump' if mode == 'dump' else 'preliminary test'} for this case"
        return summary

    ws = W.export_commit(checkout, commit, tmp / "workspace")
    summary["workspace"] = str(ws)  # for the escape check: absolute paths inside it are fine
    summary["hidden_paths_removed"] = W.hide_paths(ws, case.hidden_paths)
    support = W.install_tree(case.workspace_files, ws) if mode not in ("baseline", "autonomous") else []
    summary["support_files"] = support
    execution_input = EXECUTION_INPUT.get(mode)
    if execution_input and mode == "dump":
        summary["dump_files"] = len(W.split_dump(source, ws / execution_input))
    elif execution_input:
        (ws / execution_input).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, ws / execution_input)
    (ws / case.output_test_file).parent.mkdir(parents=True, exist_ok=True)
    W.init_repo(ws)
    env = cfg.command_env()

    summary["phase"] = "scenario-check"
    check = scenario_check(cfg, case, ws, env, cell / "artifacts", summary)
    if not check.ok:
        if support:
            for rel in support:
                (ws / rel).unlink(missing_ok=True)
            again = run(resolve_argv(eval_command(case, "scenario"), ws), ws, env, cfg.command_timeout, "scenario")
            summary["scenario_check_without_support_files"] = again.as_dict()
            if again.ok:
                summary["status"] = "method_failed"
                summary["error"] = "the files of the recorded data break the scenario build: " + ", ".join(support)
                return summary
        raise RuntimeError("driving scenario does not run in the prepared workspace")

    before = W.snapshot(ws)
    output = ws / case.output_test_file
    if mode in AGENT_MODES:
        summary["phase"] = "agent"
        commands = {n: case.commands[n] for n in AGENT_COMMANDS}
        prompts = render(cfg.prompts_dir, case, mode, commands, cfg.debug_runs)
        _write(cell / "prompts" / "system.txt", prompts["system"])
        _write(cell / "prompts" / "task.txt", prompts["task"])
        effective = A.effective_system_prompt(prompts["system"], ws)
        _write(cell / "prompts" / "system.effective.txt", effective)
        summary["prompt_sha256"] = {
            "system": hashlib.sha256(prompts["system"].encode()).hexdigest(),
            "system_effective": hashlib.sha256(effective.encode()).hexdigest(),
            "task": hashlib.sha256(prompts["task"].encode()).hexdigest(),
        }
        debugger = debugger_input(case) if mode == "autonomous" else None
        res = A.run_agent(cfg, ws, case.output_test_file, commands, prompts, tmp / "agent", api_key_env, debugger)
        _jsonl(cell / "artifacts" / "session.jsonl", res.events)
        if res.debug_log is not None:
            from runner import debugger as D
            _jsonl(cell / "artifacts" / "debug-log.jsonl", res.debug_log)
            for log in sorted((tmp / "agent").glob("scenario-debug-run*.log")):
                _write(cell / "artifacts" / log.name, log.read_text(encoding="utf-8", errors="replace")[-20000:])
            summary["debugger"] = D.journal_summary(res.debug_log, cfg.debug_runs)
        _jsonl(cell / "artifacts" / "agent-commands.jsonl", res.commands)
        _jsonl(cell / "artifacts" / "denied.jsonl", res.denied)
        _write(cell / "artifacts" / "pi-stderr.txt", res.stderr[-20000:])
        for i, text in enumerate(res.output_versions, start=1):
            _write(cell / "artifacts" / "generated" / f"v{i}.txt", text)
        summary["agent_seconds"] = round(res.seconds, 1)
        summary["agent_exit_code"] = res.exit_code
        summary["tool_calls"] = len(A.tool_calls(res.events))
        summary["usage"] = A.usage(res.events)
        summary["gateway_errors"], summary["ended_on_gateway_error"] = A.gateway_errors(res.events)
        summary["denied_actions"] = len(res.denied)
        summary["agent_commands_run"] = sum(1 for e in res.commands if e.get("event") == "end")
        summary["final_failed_agent_commands"] = final_failed_commands(res.commands)
        if res.timed_out:
            summary["status"] = "timed_out"
            summary["error"] = f"agent timed out after {cfg.timeout}s"
        elif res.exit_code != 0:
            summary["status"] = "failed"
            summary["error"] = f"agent exited with code {res.exit_code}"
        elif not output.is_file():
            summary["status"] = "failed"
            summary["error"] = "the agent left no test class"
        else:
            summary["status"] = "completed"
    else:  # rendered-raw: the preliminary test as is, renamed to the output class
        summary["phase"] = "install"
        text = retarget_test_class(source.read_text(encoding="utf-8"), case.output_test_file)
        output.write_text(text, encoding="utf-8")
        _write(cell / "artifacts" / "generated" / "v1.txt", text)
        # no post-processing: the test is evaluated as written, like the tests of the agent modes
        summary["status"] = "completed"

    summary["phase"] = "result"
    summary["output_exists"] = output.is_file()
    allowed = {case.output_test_file, *support, *([execution_input] if execution_input else [])}
    summary["unexpected_changes"] = W.unexpected_changes(before, W.snapshot(ws), allowed)
    if output.is_file():
        files = {case.output_test_file: output, **{rel: ws / rel for rel in support if (ws / rel).is_file()}}
        diff = W.result_diff(checkout, commit, files, tmp / "result")
        _write(cell / "result.diff", diff)
    summary["phase"] = "done"
    return summary


# ---------------------------------------------------------------------------------------------

INFRA_ERROR_MARKERS = ("driving scenario does not run", "WorkspaceError", "AgentError", "not a git checkout")
# Pi stderr when the provider could not be reached before the first model turn (pi/provider.ts, Node fetch)
PROVIDER_FAILURE_MARKERS = ("failed to fetch models", "model list unavailable", "Unknown provider",
                            "fetch failed", "ECONNRESET", "ECONNREFUSED", "ETIMEDOUT", "ENOTFOUND", "EAI_AGAIN",
                            "socket hang up")


def provider_unreachable(summary: dict[str, Any], cell: Path) -> bool:
    """Pi exited before any model turn and its stderr names a provider or network failure. A refusal or a
    failure of the model has at least one turn, so it is never taken for this."""
    if summary.get("agent_exit_code") in (0, None) or summary.get("tool_calls"):
        return False
    if ((summary.get("usage") or {}).get("turns") or 0) > 0:
        return False
    try:
        stderr = (cell / "artifacts" / "pi-stderr.txt").read_text(encoding="utf-8", errors="replace")
    except OSError:
        return False
    return any(m in stderr for m in PROVIDER_FAILURE_MARKERS)


def is_infra_failure(summary: dict[str, Any], cell: Path) -> bool:
    """A failure that says nothing about the model or the method: rerun the cell."""
    status = summary.get("status")
    if status not in ("failed", "timed_out"):
        return False
    if summary.get("ended_on_gateway_error"):
        return True
    if status == "timed_out" and (summary.get("gateway_errors") or 0) >= 3:
        return True
    if status != "failed":
        return False
    if summary.get("phase") in ("preparation", "scenario-check"):
        return True
    if any(m in str(summary.get("error") or "") for m in INFRA_ERROR_MARKERS):
        return True
    if provider_unreachable(summary, cell):  # whatever phase the summary was saved in
        return True
    # Pi died before doing anything (provider unreachable, bad configuration)
    return summary.get("phase") == "agent" and not summary.get("tool_calls") and summary.get("agent_exit_code") != 0
