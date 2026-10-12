"""Evaluate one generation cell: verify, RQ1 (trace vs reference), RQ2 (PIT), quality.

Input: <results>/cells/<case>/<mode>/<run-id>/ (summary.json, result.diff) written by the runner.
Output: eval.json in the same directory, plus eval-artifacts/ (candidate trace, mutations.xml,
verification logs), so that metrics can be recomputed without rerunning the tests.

Every step starts from a fresh copy of the compiled project checkout with the hidden tests removed
and the cell's result.diff applied: no execution input, no files of the agent run.

    python3 -m evaluation.evaluate --config config.yaml [--cases ...] [--run-ids ...] [--steps rq1 ...] [--force]
"""
from __future__ import annotations

import argparse
import json
import shutil
import subprocess
from pathlib import Path
from typing import Any

from evaluation import compare as C
from evaluation import mutation as M
from evaluation import quality as Q
from evaluation import trace as T
from evaluation.reference import load_reference
from runner.cases import Case, case_ids, load_case, project_checkout
from runner.config import EVAL_STEPS, MODES, Config, load_config
from runner.process import resolve_argv, run


def record_path(cell: Path) -> Path:
    return cell / "eval.json"


def load_record(cell: Path) -> dict[str, Any]:
    p = record_path(cell)
    s = json.loads((cell / "summary.json").read_text())
    if p.is_file():
        rec = json.loads(p.read_text())
        rec["escape_check"] = s.get("escape_check")  # the check may run after the first evaluation
        return rec
    return {k: s.get(k) for k in ("case_id", "project", "group", "mode", "run_id", "status", "error",
                                  "model", "thinking", "agent_seconds", "tool_calls", "usage",
                                  "escape_check")}


def save_record(cell: Path, rec: dict[str, Any]) -> None:
    record_path(cell).write_text(json.dumps(rec, indent=1, default=str))


def _keep(src: Path, dest: Path) -> None:
    if not src.exists():
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists():
        shutil.rmtree(dest) if dest.is_dir() else dest.unlink()
    shutil.copytree(src, dest) if src.is_dir() else shutil.copy2(src, dest)


def result_workspace(cfg: Config, case: Case, cell: Path, tag: str) -> Path:
    """Copy of the compiled checkout, hidden tests removed, the cell's diff applied."""
    ws = T.clone_checkout(project_checkout(cfg, case.project),
                          cfg.work_dir / "ws" / f"{case.id}-{cell.parent.name}-{cell.name}-{tag}")
    T.remove_paths(ws, case.hidden_paths)
    res = subprocess.run(["git", "apply", "--whitespace=nowarn", str(cell / "result.diff")],
                         cwd=ws, capture_output=True, text=True)
    if res.returncode != 0:
        shutil.rmtree(ws, ignore_errors=True)
        raise RuntimeError(f"git apply failed: {res.stderr[-800:]}")
    return ws


def has_test(rec: dict[str, Any], cell: Path) -> bool:
    return rec.get("status") != "method_failed" and (cell / "result.diff").is_file() \
        and bool((cell / "result.diff").read_text().strip())


def passes(rec: dict[str, Any]) -> bool:
    return bool((rec.get("verify") or {}).get("passes"))


def eval_verify(cfg: Config, case: Case, cell: Path, rec: dict[str, Any]) -> dict[str, Any]:
    """Independent check of the generated test on a clean copy: `build` + `generated_test`.
    This is the compile/pass of the paper; a test left by an agent that timed out or failed is
    evaluated the same way (the runner status is kept in the record)."""
    if not has_test(rec, cell):
        return {"applies": False, "compiles": None, "passes": None,
                "note": "method_failed" if rec.get("status") == "method_failed" else "no generated test"}
    out: dict[str, Any] = {"applies": False, "compiles": None, "passes": None}
    try:
        ws = result_workspace(cfg, case, cell, "verify")
    except RuntimeError as e:
        out["note"] = str(e)[:500]
        return out
    logs = cell / "eval-artifacts" / "verify"
    logs.mkdir(parents=True, exist_ok=True)
    try:
        out["applies"] = True
        for name in ("build", "generated_test"):
            r = run(resolve_argv(case.commands[name], ws), ws, cfg.command_env(), cfg.command_timeout, name)
            (logs / f"{name}.log").write_text(r.stdout[-20000:] + r.stderr[-5000:])
            out[f"{name}_exit_code"] = "timeout" if r.timed_out else r.exit_code
            if name == "build":
                out["compiles"] = r.ok
                if not r.ok:
                    break
            else:
                out["passes"] = r.ok
    finally:
        shutil.rmtree(ws, ignore_errors=True)
    return out


def eval_rq1(cfg: Config, case: Case, cell: Path, rec: dict[str, Any]) -> dict[str, Any]:
    ref = load_reference(cfg, case.id)
    candidate, note = None, None
    if passes(rec):
        ws = result_workspace(cfg, case, cell, "rq1")
        out_dir = cfg.work_dir / "out" / case.id / cell.parent.name / cell.name
        try:
            t = T.trace_case_command(case, ws, "generated_test", out_dir, cfg.command_env(),
                                     cfg.command_timeout, cfg.trace_depth)
            _keep(out_dir / "trace", cell / "eval-artifacts" / "trace")
            if t["run"]["exit_code"] == 0:
                candidate = t["invocations"]
            else:
                note = f"generated test exit code {t['run']['exit_code']} under the tracer"
        finally:
            shutil.rmtree(ws, ignore_errors=True)
            shutil.rmtree(out_dir, ignore_errors=True)
    else:
        v = rec.get("verify") or {}
        note = f"not run: verify compiles={v.get('compiles')} passes={v.get('passes')}"
    rq1 = C.compare(ref, candidate, C.load_nondet(), case.project)
    rq1["note"] = note
    rq1["candidate_invocations"] = None if candidate is None else len(candidate)
    return rq1


def eval_rq2(cfg: Config, case: Case, cell: Path, rec: dict[str, Any]) -> dict[str, Any]:
    if not passes(rec):
        return {"note": "not evaluated: the generated test does not pass verification"}
    ws = result_workspace(cfg, case, cell, "pit")
    pit_out = cfg.work_dir / "pit" / case.id / cell.parent.name / cell.name
    try:
        return M.run_pit(ws, [case.output_test_file], case.target["class"], pit_out, cfg.command_env(),
                         mvn=case.commands["build"][0], scenario_argv=case.commands["scenario"])
    except Exception as e:
        return {"error": f"{type(e).__name__}: {e}"[:1500]}
    finally:
        _keep(pit_out / "pit" / "mutations.xml", cell / "eval-artifacts" / "mutations.xml")
        _keep(pit_out / "pit-log.txt", cell / "eval-artifacts" / "pit-log.txt")
        shutil.rmtree(ws, ignore_errors=True)
        shutil.rmtree(pit_out, ignore_errors=True)


def eval_quality(cfg: Config, case: Case, cell: Path, rec: dict[str, Any]) -> dict[str, Any] | None:
    if not has_test(rec, cell):
        return None
    diff = (cell / "result.diff").read_text()
    src = "\n".join(line[1:] for line in diff.splitlines() if line.startswith("+") and not line.startswith("+++"))
    q: dict[str, Any] = {"mocks": Q.mock_stats(src), "readability": Q.readability(src)}
    if rec.get("mode") == "rendered" and case.preliminary_test is not None:
        q["preservation"] = Q.preservation(case.preliminary_test.read_text(), src, case.target["method"])
    return q


STEPS = {"verify": eval_verify, "rq1": eval_rq1, "rq2": eval_rq2, "quality": eval_quality}


def evaluate_cell(cfg: Config, cell: Path, steps: list[str] | None = None, force: list[str] | None = None) -> dict[str, Any]:
    """Run the missing evaluation steps of one cell (the forced ones again); returns the record."""
    rec = load_record(cell)
    case = load_case(rec["case_id"], cfg.cases_dir)
    for step in steps or cfg.eval_steps:
        if step in rec and step not in (force or []):
            continue
        rec[step] = STEPS[step](cfg, case, cell, rec)
        save_record(cell, rec)
    return rec


def cells(cfg: Config, cases: list[str], run_ids: list[str] | None, modes: list[str] | None) -> list[Path]:
    out = []
    for cid in cases:
        for s in sorted((cfg.cells_dir / cid).glob("*/*/summary.json")):
            cell = s.parent
            if (run_ids and cell.name not in run_ids) or (modes and cell.parent.name not in modes):
                continue
            out.append(cell)
    return out


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--cases", nargs="*")
    ap.add_argument("--run-ids", nargs="*")
    ap.add_argument("--modes", nargs="*", choices=list(MODES))
    ap.add_argument("--steps", nargs="*", choices=list(EVAL_STEPS))
    ap.add_argument("--force", action="store_true", help="recompute the selected steps")
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    steps = a.steps or cfg.eval_steps
    failed = 0
    for cell in cells(cfg, case_ids(cfg, a.cases), a.run_ids, a.modes):
        try:
            evaluate_cell(cfg, cell, steps, steps if a.force else None)
            print(f"ok   {cell}", flush=True)
        except Exception as e:
            failed += 1
            print(f"FAIL {cell}: {type(e).__name__}: {e}", flush=True)
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
