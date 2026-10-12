"""Reference traces of the driving scenarios, recorded twice (nondeterminism audit).

Output per case: <results>/references/<case>/run1.json, run2.json and audit.json (distinct scenarios
per run, paths that differ between the two runs, masked values, depth cuts, run1 vs run2 on every
level). Metrics compare candidate traces with run1.

    python3 -m evaluation.reference --config config.yaml [--cases ID ...] [--force]
"""
from __future__ import annotations

import argparse
import json
import shutil
from typing import Any

from evaluation import compare as C
from evaluation import trace as T
from runner.cases import case_ids, load_case, project_checkout
from runner.config import Config, load_config


def reference_exists(cfg: Config, case_id: str) -> bool:
    return (cfg.references_dir / case_id / "audit.json").is_file()


def record(cfg: Config, case_id: str, force: bool = False, reaudit: bool = False) -> dict[str, Any]:
    out = cfg.references_dir / case_id
    audit_file = out / "audit.json"
    if audit_file.is_file() and not force and not reaudit:
        return json.loads(audit_file.read_text())
    case = load_case(case_id, cfg.cases_dir)
    out.mkdir(parents=True, exist_ok=True)
    runs = cfg.reference_runs
    if reaudit:
        traces = [json.loads((out / f"run{i}.json").read_text()) for i in range(1, runs + 1)
                  if (out / f"run{i}.json").is_file()]
    else:
        traces = []
        for i in range(1, runs + 1):
            ws = T.clone_checkout(project_checkout(cfg, case.project), cfg.work_dir / "ws" / f"{case_id}-reference")
            try:
                T.remove_paths(ws, case.hidden_paths)
                t = T.trace_case_command(case, ws, "scenario", cfg.work_dir / "out" / case_id / f"reference-{i}",
                                         cfg.command_env(), cfg.command_timeout, cfg.trace_depth)
            finally:
                shutil.rmtree(ws, ignore_errors=True)
            (out / f"run{i}.json").write_text(json.dumps(t))
            traces.append(t)
        shutil.rmtree(cfg.work_dir / "out" / case_id, ignore_errors=True)
    nondet = C.load_nondet()
    audit: dict[str, Any] = {"case_id": case_id, "project": case.project, "depth": cfg.trace_depth, "runs": []}
    for t in traces:
        norm = C.Normaliser(nondet, case.project)
        sc = C.scenarios(t["invocations"], norm)
        audit["runs"].append({"exit_code": t["run"]["exit_code"], "seconds": t["run"]["seconds"],
                              "invocations": len(t["invocations"]), "scenarios": len(sc), "jvms": t.get("jvms"),
                              "masked": norm.masked, "cuts": norm.cuts, "errors": t["errors"][:5]})
    if len(traces) >= 2:
        a, b = traces[0]["invocations"], traces[1]["invocations"]
        diffs: dict[str, int] = {}
        if len(a) == len(b):
            audit_norm = C.Normaliser(nondet, case.project)
            for x, y in zip(a, b):
                for p in C.unstable_paths(x, y, norm=audit_norm):
                    diffs[p] = diffs.get(p, 0) + 1
        else:
            diffs["[invocation count]"] = 1
        audit["unstable_paths"] = sorted(diffs.items(), key=lambda kv: -kv[1])[:50]
        audit["run1_vs_run2"] = C.compare(a, b, nondet, case.project)
    audit_file.write_text(json.dumps(audit, indent=1))
    return audit


def load_reference(cfg: Config, case_id: str) -> list[dict[str, Any]]:
    p = cfg.references_dir / case_id / "run1.json"
    if not p.is_file():
        raise FileNotFoundError(f"no reference trace for {case_id}; run evaluation.reference")
    return json.loads(p.read_text())["invocations"]


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--cases", nargs="*")
    ap.add_argument("--force", action="store_true")
    ap.add_argument("--reaudit", action="store_true",
                    help="recompute audit.json from saved traces (after editing nondeterminism.json)")
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    failed = 0
    for cid in case_ids(cfg, a.cases):
        try:
            au = record(cfg, cid, a.force, a.reaudit)
            r = au["runs"][0]
            s = au.get("run1_vs_run2", {})
            print(f"{cid:62} rc={r['exit_code']} inv={r['invocations']} sc={r['scenarios']} cuts={r['cuts']} "
                  f"self S/A/B/C={s.get('recall_structure')}/{s.get('recall_io')}/{s.get('recall_state')}/"
                  f"{s.get('recall_strict')} unstable={len(au.get('unstable_paths', []))}", flush=True)
        except Exception as e:  # keep going
            failed += 1
            print(f"{cid:62} ERROR {type(e).__name__}: {e}", flush=True)
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
