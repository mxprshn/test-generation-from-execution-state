"""Generate one cell: a case, a mode and a run id.

    workspace   exported project commit, hidden tests removed, a single-commit git repository;
                dump/rendered/rendered-raw also get the case's workspace-files/ (resources the
                recorded data refer to) and, for dump/rendered, the execution input under carving/
    scenario    the driving scenario must run in the workspace (otherwise an infrastructure failure;
                if it runs only without workspace-files/, the input is a method failure)
    generation  Pi with the rendered prompts (baseline, dump, rendered), or the preliminary test
                installed as the output class (rendered-raw, no agent)
    result      result.diff: the generated class (and workspace-files/) against the original commit

Status: completed (the agent finished and left the test class), timed_out, failed, method_failed
(the mode has no input for the case). Whether the test compiles and passes is decided later, on a
clean copy, by evaluation.evaluate (verify).

Output: <results>/cells/<case>/<mode>/<run-id>/ summary.json, result.diff, prompts/, artifacts/.
"""
from __future__ import annotations

import json
import re
import shutil
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from runner import agent as A
from runner import workspace as W
from runner.cases import Case, load_case, load_projects, project_checkout
from runner.config import AGENT_COMMANDS, AGENT_MODES, Config
from runner.process import resolve_argv, run
from runner.prompts import EXECUTION_INPUT, render


def now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


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


MAVEN_OPTIONS_WITH_VALUE = {"-pl", "--projects", "-f", "--file", "-s", "--settings"}


def spotless_apply_argv(build_argv: list[str], build_output: str, output_test_file: str) -> list[str] | None:
    """`spotless:apply` limited to the output file, if the build failed on spotless format violations."""
    if "spotless-maven-plugin" not in build_output or "format violations" not in build_output:
        return None
    if not build_argv or Path(build_argv[0]).name not in ("mvn", "mvnw"):
        return None
    kept, take_value = [build_argv[0]], False
    for token in build_argv[1:]:
        if take_value:
            kept.append(token)
            take_value = False
        elif token in MAVEN_OPTIONS_WITH_VALUE:
            kept.append(token)
            take_value = True
        elif token.startswith("-"):
            kept.append(token)
    name = Path(output_test_file).name.replace(".", "[.]")
    return kept + ["spotless:apply", f"-DspotlessFiles=.*{name}"]


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
    tmp = cfg.work_dir / "cells" / f"{case_id}-{mode}-{run_id}"
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


def _generate(cfg: Config, case: Case, mode: str, checkout: Path, commit: str, cell: Path, tmp: Path,
              summary: dict[str, Any], api_key_env: str) -> dict[str, Any]:
    source = method_input(case, mode)
    if mode != "baseline" and source is None:
        summary["status"] = "method_failed"
        summary["error"] = f"no {'call dump' if mode == 'dump' else 'preliminary test'} for this case"
        return summary

    ws = W.export_commit(checkout, commit, tmp / "workspace")
    summary["hidden_paths_removed"] = W.hide_paths(ws, case.hidden_paths)
    support = W.install_tree(case.workspace_files, ws) if mode != "baseline" else []
    summary["support_files"] = support
    execution_input = EXECUTION_INPUT.get(mode)
    if execution_input:
        (ws / execution_input).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, ws / execution_input)
    (ws / case.output_test_file).parent.mkdir(parents=True, exist_ok=True)
    W.init_repo(ws)
    env = cfg.command_env()

    summary["phase"] = "scenario-check"
    check = run(resolve_argv(case.commands["scenario"], ws), ws, env, cfg.command_timeout, "scenario")
    _write(cell / "artifacts" / "scenario-check.log", check.stdout[-20000:] + check.stderr[-5000:])
    summary["scenario_check"] = check.as_dict()
    if not check.ok:
        if support:
            for rel in support:
                (ws / rel).unlink(missing_ok=True)
            again = run(resolve_argv(case.commands["scenario"], ws), ws, env, cfg.command_timeout, "scenario")
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
        prompts = render(cfg.prompts_dir, case, mode, commands)
        _write(cell / "prompts" / "system.txt", prompts["system"])
        _write(cell / "prompts" / "task.txt", prompts["task"])
        res = A.run_agent(cfg, ws, case.output_test_file, commands, prompts, tmp / "agent", api_key_env)
        _jsonl(cell / "artifacts" / "session.jsonl", res.events)
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
        # the project's build may check code format (spotless); the agent modes format their test
        # themselves, so the preliminary test is formatted the same way before the diff is taken
        build = run(resolve_argv(case.commands["build"], ws), ws, env, cfg.command_timeout, "build")
        argv = None if build.ok else spotless_apply_argv(case.commands["build"], build.stdout + build.stderr,
                                                          case.output_test_file)
        if argv:
            fmt = run(resolve_argv(argv, ws), ws, env, cfg.command_timeout, "format")
            summary["formatted"] = fmt.ok
            if fmt.ok:
                _write(cell / "artifacts" / "generated" / "v2-formatted.txt", output.read_text(encoding="utf-8"))
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
    # Pi died before doing anything (provider unreachable, bad configuration)
    return summary.get("phase") == "agent" and not summary.get("tool_calls") and summary.get("agent_exit_code") != 0
