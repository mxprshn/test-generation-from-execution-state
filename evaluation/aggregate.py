"""Aggregate evaluation records into tables (Markdown + CSV).

Usage: python3 -m evaluation.aggregate --config config.yaml [--out <results>/report]

Only cells whose directory name is a run id of the config are read (cells put aside by the runner
are not). Applicability and quality are reported separately: a cell without input for its mode
(method_failed) counts in applicability only, never as a zero in quality.

Tables:
  * applicability      : per mode, cases with input, cells generated, verified (compiles, passes),
                         with an escape finding and with denied actions only (escape check),
                         evaluation errors;
  * rq1_by_mode        : recall at levels S, A (main), B, C as the mean over cases and the mean over
                         projects (each case: mean over rounds); cells whose test did not pass count 0;
  * rq1_paired         : every mode against baseline on the cases where both have input: mean
                         difference per level, bootstrap 95% interval, Wilcoxon signed-rank p-value,
                         wins / ties / losses;
  * rq1_by_group, rq1_by_project : the per-case means split by group and by project;
  * rq1_pooled         : scenarios pooled over cases (reference only: large cases dominate);
  * rq2_by_mode        : oracle strength (killed / covered, mean over cases) and gain over baseline on
                         the same (case, round): a baseline without a passing test has killed nothing;
  * rq3_by_mode        : compile/pass, tests run, cost, time, tool calls, preservation (rendered);
  * quality_by_mode    : mock statistics and readability indicators;
  * debugger_by_mode    : autonomous mode: tool calls by tool, JVM runs, stops (in the target
                         method), plan revisions (breakpoints changed after the first stop), the
                         size of the debugger answers, tokens including cache reads (medians);
  * escape_check_by_mode (escape / denied only) and the cells with an escape finding; cells whose RQ1 ran into an error or that run the
    scenario (runs_scenario).
"""
from __future__ import annotations

import argparse
import csv
import json
import statistics
from collections import defaultdict
from pathlib import Path
from typing import Any

from evaluation import mutation as M
from evaluation import stats as ST
from runner import escape_check as X
from runner.cases import case_rows
from runner.config import Config, load_config

MODES = ["baseline", "rendered-raw", "rendered", "dump", "autonomous"]
LEVELS = (("structure", "S"), ("io", "A"), ("state", "B"), ("strict", "C"))


def load(cfg: Config) -> tuple[list[dict[str, Any]], dict[str, dict[str, str]]]:
    cases = {r["id"]: r for r in case_rows(cfg.cases_dir)}
    rounds = {cfg.run_id(r): str(r) for r in cfg.rounds}
    recs = []
    for p in sorted(cfg.cells_dir.glob("*/*/*/eval.json")):
        if p.parent.name not in rounds:  # <run-id>.infra-*, <run-id>.incomplete-*: not cells
            continue
        r = json.loads(p.read_text())
        if r.get("case_id") in cases and r.get("run_id") == p.parent.name:
            r["group"] = cases[r["case_id"]]["group"]
            r["project"] = cases[r["case_id"]]["project"]
            r["round"] = rounds[r["run_id"]]
            recs.append(r)
    return recs, cases


def _fmt(x: Any) -> str:
    if x is None:
        return "—"
    if isinstance(x, float):
        return f"{x:.3f}" if abs(x) < 1 else f"{x:.2f}"
    return str(x)


def _mean(xs: list[float]) -> float | None:
    xs = [x for x in xs if x is not None]
    return statistics.mean(xs) if xs else None


def has_input(r: dict[str, Any]) -> bool:
    return r.get("status") != "method_failed"


def rq1_value(r: dict[str, Any], level: str) -> float | None:
    """Recall of a cell at a level; a cell whose test did not pass verification scores 0.
    None when the case has no reference scenarios or the cell has no input for its mode."""
    q = r.get("rq1")
    if not has_input(r) or not q or not q.get("scenarios"):
        return None
    return q.get(f"recall_{level}")


def case_scores(recs: list[dict[str, Any]]) -> dict[tuple[str, str], dict[str, float]]:
    """(mode, case) -> level -> mean over rounds."""
    acc: dict[tuple[str, str], dict[str, list]] = defaultdict(lambda: defaultdict(list))
    for r in recs:
        for level, _ in LEVELS:
            v = rq1_value(r, level)
            if v is not None:
                acc[(r["mode"], r["case_id"])][level].append(v)
    return {k: {lvl: statistics.mean(v) for lvl, v in d.items()} for k, d in acc.items()}


def applicability_rows(recs: list[dict[str, Any]], cases: dict[str, dict[str, str]]) -> list[dict[str, Any]]:
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        by[r["mode"]].append(r)
    out = []
    for mode, rs in by.items():
        with_input = {r["case_id"] for r in rs if has_input(r)}
        verified = [r for r in rs if has_input(r)]
        out.append({
            "key": mode, "cases": len({r["case_id"] for r in rs}), "cases_with_input": len(with_input),
            "applicability": len(with_input) / len(cases) if cases else None,
            "cells": len(rs), "cells_with_input": len(verified),
            "compiles": sum(1 for r in verified if (r.get("verify") or {}).get("compiles")),
            "passes": sum(1 for r in verified if (r.get("verify") or {}).get("passes")),
            "pass_rate": (sum(1 for r in verified if (r.get("verify") or {}).get("passes")) / len(verified)
                          if verified else None),
            "runner_timed_out": sum(1 for r in rs if r.get("status") == "timed_out"),
            "escape": sum(1 for r in rs if X.check_status(r.get("escape_check")) == "escape"),
            "denied_only": sum(1 for r in rs if X.check_status(r.get("escape_check")) == "denied_only"),
            "runs_scenario": sum(1 for r in rs if (r.get("rq1") or {}).get("runs_scenario")),
            "rq1_missing": sum(1 for r in verified if not r.get("rq1")),
        })
    return out


def rq1_rows(recs: list[dict[str, Any]], key=lambda r: r["mode"]) -> list[dict[str, Any]]:
    """Mean over cases and mean over projects (each case: mean over rounds) per key."""
    scores = case_scores(recs)
    meta = {(r["mode"], r["case_id"]): r for r in recs}
    groups: dict[Any, list[tuple[str, str]]] = defaultdict(list)
    for mc in scores:
        groups[key(meta[mc])].append(mc)
    out = []
    for k, mcs in groups.items():
        row: dict[str, Any] = {"key": k, "cases": len(mcs)}
        for level, short in LEVELS:
            vals = [scores[mc][level] for mc in mcs if level in scores[mc]]
            row[f"{short}_case_mean"] = _mean(vals)
            per_project: dict[str, list] = defaultdict(list)
            for mc in mcs:
                if level in scores[mc]:
                    per_project[meta[mc]["project"]].append(scores[mc][level])
            row[f"{short}_project_mean"] = _mean([statistics.mean(v) for v in per_project.values()])
        out.append(row)
    return out


def rq1_paired_rows(recs: list[dict[str, Any]], key=lambda r: "all") -> list[dict[str, Any]]:
    """Each mode against baseline, on the cases where both have input and a reference."""
    scores = case_scores(recs)
    meta = {(r["mode"], r["case_id"]): r for r in recs}
    out = []
    modes = sorted({m for m, _ in scores if m != "baseline"}, key=lambda m: MODES.index(m) if m in MODES else 99)
    for mode in modes:
        groups: dict[Any, list[str]] = defaultdict(list)
        for (m, cid) in scores:
            if m == mode and ("baseline", cid) in scores:
                groups[key(meta[(m, cid)])].append(cid)
        for g, cids in sorted(groups.items(), key=lambda kv: str(kv[0])):
            for level, short in LEVELS:
                d = [scores[(mode, c)][level] - scores[("baseline", c)][level] for c in cids
                     if level in scores[(mode, c)] and level in scores[("baseline", c)]]
                if not d:
                    continue
                w = ST.wilcoxon(d)
                lo, hi = ST.bootstrap_ci(d)
                out.append({"key": f"{mode} vs baseline", "subset": g, "level": short, "cases": len(d),
                            "mode_mean": _mean([scores[(mode, c)][level] for c in cids if level in scores[(mode, c)]]),
                            "baseline_mean": _mean([scores[("baseline", c)][level] for c in cids
                                                    if level in scores[("baseline", c)]]),
                            "mean_diff": statistics.mean(d), "ci95_low": lo, "ci95_high": hi,
                            "wins": sum(1 for x in d if x > 0), "ties": sum(1 for x in d if x == 0),
                            "losses": sum(1 for x in d if x < 0), "wilcoxon_p": w["p"], "p_method": w["method"]})
    return out


def rq1_pooled_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Scenarios pooled over cases and rounds (reference only)."""
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        if rq1_value(r, "structure") is not None:
            by[r["mode"]].append(r["rq1"])
    out = []
    for mode, qs in by.items():
        n = sum(q["scenarios"] for q in qs)
        row = {"key": mode, "cells": len(qs), "scenarios": n}
        for level, short in LEVELS:
            row[f"{short}_pooled"] = sum(q.get(f"{level}_hits", 0) for q in qs) / n if n else None
        out.append(row)
    return out


def rq2_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Oracle strength per mode (mean over cells with PIT results) and gain over the baseline test of
    the same case and round. A baseline cell that has no PIT result because its test did not pass
    has killed nothing; a baseline cell missing for another reason leaves the pair out."""
    base: dict[tuple[str, str], dict[str, str] | None] = {}
    for r in recs:
        if r["mode"] != "baseline":
            continue
        q = r.get("rq2")
        if isinstance(q, dict) and "mutants" in q:
            base[(r["case_id"], r["round"])] = q["mutants"]
        elif r.get("verify") is not None and not (r.get("verify") or {}).get("passes"):
            base[(r["case_id"], r["round"])] = {}
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        q = r.get("rq2")
        if not isinstance(q, dict) or "mutants" not in q:
            continue
        g = None
        b = base.get((r["case_id"], r["round"]))
        if b is not None and r["mode"] != "baseline":
            g = M.gain_over_baseline(b, q["mutants"])
        by[r["mode"]].append((q, g))
    out = []
    for mode, items in by.items():
        strengths = [q["oracle_strength"] for q, _ in items if q.get("oracle_strength") is not None]
        gains = [g for _, g in items if g is not None]
        out.append({
            "key": mode, "cells": len(items),
            "oracle_strength_mean": _mean(strengths),
            "reach_mean": _mean([q.get("reach") for q, _ in items]),
            "pairs_with_baseline": len(gains),
            "additional_killed": sum(g["additional"] for g in gains) if gains else None,
            "cells_with_gain": sum(1 for g in gains if g["additional"] > 0) if gains else None,
        })
    return out


def rq3_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        by[r["mode"]].append(r)
    out = []
    for mode, rs in by.items():
        def med(f):
            xs = [f(r) for r in rs if f(r) is not None]
            return statistics.median(xs) if xs else None
        pres = [r["quality"]["preservation"] for r in rs if (r.get("quality") or {}).get("preservation")]
        out.append({
            "key": mode, "cells": len(rs),
            "method_failed": sum(1 for r in rs if r.get("status") == "method_failed"),
            "runner_completed": sum(1 for r in rs if r.get("status") == "completed"),
            "compiles": sum(1 for r in rs if (r.get("verify") or {}).get("compiles")),
            "passes": sum(1 for r in rs if (r.get("verify") or {}).get("passes")),
            "median_tests_run": med(lambda r: (r.get("verify") or {}).get("tests_run")),
            "median_seconds": med(lambda r: r.get("agent_seconds")),
            "median_tool_calls": med(lambda r: r.get("tool_calls") if isinstance(r.get("tool_calls"), (int, float)) else None),
            "median_cost": med(lambda r: (r.get("usage") or {}).get("cost_usd")),
            "median_changed_share": statistics.median(p["changed_share"] for p in pres) if pres else None,
            "target_calls_kept": sum(1 for p in pres if p["target_calls"][1] >= p["target_calls"][0]) if pres else None,
            "assertions_kept": sum(1 for p in pres if p["assertions"][1] >= p["assertions"][0]) if pres else None,
        })
    return out


def quality_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        if r.get("quality"):
            by[r["mode"]].append(r["quality"])
    out = []
    for mode, qs in by.items():
        def mean(f):
            return _mean([f(q) for q in qs])
        out.append({
            "key": mode, "cells": len(qs),
            "mocks": mean(lambda q: q["mocks"]["mocks"]),
            "stubbings": mean(lambda q: q["mocks"]["stubbings"]),
            "data_type_mocks": mean(lambda q: q["mocks"]["data_type_mocks"]),
            "test_methods": mean(lambda q: q["readability"]["test_methods"]),
            "assertions": mean(lambda q: q["readability"]["assertions"]),
            "tests_without_assertions": mean(lambda q: q["readability"]["tests_without_assertions"]),
            "reflection": mean(lambda q: q["readability"]["reflection"]),
            "generated_names": mean(lambda q: q["readability"]["generated_names"]),
            "casts": mean(lambda q: q["readability"]["casts"]),
            "fully_qualified_names": mean(lambda q: q["readability"]["fully_qualified_names"]),
        })
    return out


def debugger_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Medians over the cells of a mode with a debugger journal (summary.json: debugger)."""
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        if isinstance(r.get("debugger"), dict):
            by[r["mode"]].append(r)
    out = []
    for mode, rs in by.items():
        def med(f):
            xs = [f(r) for r in rs if f(r) is not None]
            return statistics.median(xs) if xs else None
        tools = sorted({t for r in rs for t in (r["debugger"].get("calls") or {})})
        row: dict[str, Any] = {
            "key": mode, "cells": len(rs),
            "cells_with_stop_in_target": sum(1 for r in rs if (r["debugger"].get("stops_in_target") or 0) > 0),
            "median_jvm_runs": med(lambda r: r["debugger"].get("jvm_runs")),
            "median_stops": med(lambda r: r["debugger"].get("stops")),
            "median_stops_in_target": med(lambda r: r["debugger"].get("stops_in_target")),
            "median_plan_revisions": med(lambda r: r["debugger"].get("plan_revisions")),
            "median_answer_chars": med(lambda r: r["debugger"].get("answer_chars")),
            "median_turns": med(lambda r: (r.get("usage") or {}).get("turns")),
            "median_input_tokens": med(lambda r: (r.get("usage") or {}).get("input_tokens")),
            "median_cache_read_tokens": med(lambda r: (r.get("usage") or {}).get("cache_read_tokens")),
            "median_output_tokens": med(lambda r: (r.get("usage") or {}).get("output_tokens")),
        }
        for t in tools:
            row[f"median_{t}"] = med(lambda r, t=t: (r["debugger"].get("calls") or {}).get(t, 0))
        out.append(row)
    return out


def dependency_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Receiver fields compared by presence only at level B, per mode."""
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        q = r.get("rq1") or {}
        if q.get("candidate_invocations") is not None:
            by[r["mode"]].append(q.get("dependency_fields_count", len(q.get("dependency_fields") or [])))
    return [{"key": m, "cells": len(xs), "masked_fields_mean": _mean(xs),
             "cells_with_masked_fields": sum(1 for x in xs if x)} for m, xs in by.items()]


def escape_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Escape check per mode: cells with an escape (listed below the table) and cells with denied
    actions only (counted, with the kinds of refused actions)."""
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        by[r["mode"]].append(r.get("escape_check"))
    out = []
    for mode, checks in by.items():
        done = [c for c in checks if c]
        status = [X.check_status(c) for c in done]
        escape_kinds: dict[str, int] = defaultdict(int)
        denied: dict[str, int] = defaultdict(int)
        for c, st in zip(done, status):
            if st == "escape":
                for k in {f["kind"] for f in c.get("findings") or []
                          if (f.get("class") or X.finding_class(f.get("kind", ""))) == "escape"}:
                    escape_kinds[k] += 1
            for t, n in (c.get("denied_types") or {}).items():
                denied[t] += n
        out.append({"key": mode, "cells": len(checks), "checked": len(done),
                    "escape": status.count("escape"), "denied_only": status.count("denied_only"),
                    "escape_kinds": "; ".join(f"{k}: {v}" for k, v in sorted(escape_kinds.items())) or None,
                    "denied_actions": "; ".join(f"{k}: {v}" for k, v in sorted(denied.items())) or None})
    return out


def listed_cells(recs: list[dict[str, Any]]) -> str:
    md = []
    escaped = [r for r in recs if X.check_status(r.get("escape_check")) == "escape"]
    md.append("Cells with an escape finding (evaluated as usual):" if escaped else "Cells with an escape finding: none.")
    for r in sorted(escaped, key=lambda r: (r["case_id"], r["mode"], r["run_id"])):
        f = [x for x in r["escape_check"]["findings"]
             if (x.get("class") or X.finding_class(x.get("kind", ""))) == "escape"]
        md.append(f"- `{r['case_id']}` {r['mode']}/{r['run_id']}: "
                  + "; ".join(sorted({x['kind'] for x in f})) + f" (e.g. {f[0]['where']}: `{f[0]['evidence']}`)")
    scen = [r for r in recs if (r.get("rq1") or {}).get("runs_scenario")]
    md.append("")
    md.append("Cells whose test runs the scenario or calls the target mostly from other classes:" if scen
              else "Cells that run the scenario: none.")
    for r in sorted(scen, key=lambda r: (r["case_id"], r["mode"], r["run_id"])):
        md.append(f"- `{r['case_id']}` {r['mode']}/{r['run_id']}: " + "; ".join(r["rq1"].get("runs_scenario_reasons") or []))
    missing = [r for r in recs if has_input(r) and not r.get("rq1")]
    md.append("")
    md.append("Cells without an RQ1 result (evaluation error, see the case log):" if missing
              else "Cells without an RQ1 result: none.")
    for r in sorted(missing, key=lambda r: (r["case_id"], r["mode"], r["run_id"])):
        md.append(f"- `{r['case_id']}` {r['mode']}/{r['run_id']}: {'; '.join(r.get('transient_errors') or []) or 'not evaluated'}")
    return "\n".join(md) + "\n"


def write(rows: list[dict[str, Any]], name: str, out: Path, order=MODES) -> str:
    if not rows:
        return f"### {name}\n\n(no data)\n"
    rows = sorted(rows, key=lambda r: (order.index(r["key"]) if r["key"] in order else 99, str(r["key"]),
                                       str(r.get("subset", "")), str(r.get("level", ""))))
    cols = list(dict.fromkeys(c for r in rows for c in r))
    with (out / f"{name}.csv").open("w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=cols)
        w.writeheader()
        w.writerows(rows)
    md = [f"### {name}", "", "| " + " | ".join(cols) + " |", "|" + "---|" * len(cols)]
    for r in rows:
        md.append("| " + " | ".join(_fmt(r.get(c)) for c in cols) + " |")
    return "\n".join(md) + "\n"


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--out", default=None)
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    out = Path(a.out) if a.out else cfg.results_dir / "report"
    out.mkdir(parents=True, exist_ok=True)
    recs, cases = load(cfg)
    parts = [f"# Summary\n\nEvaluated cells: {len(recs)} of {len(cases)} cases. Means are over cases "
             "(each case: mean over rounds); a test that did not pass verification scores 0; cells "
             "without input for their mode are in `applicability` only.\n"]
    parts.append(write(applicability_rows(recs, cases), "applicability", out))
    parts.append(write(rq1_rows(recs), "rq1_by_mode", out))
    parts.append(write(rq1_paired_rows(recs), "rq1_paired", out))
    parts.append(write(rq1_paired_rows(recs, key=lambda r: r["group"]), "rq1_paired_by_group", out))
    parts.append(write(rq1_rows(recs, key=lambda r: f"{r['group']}/{r['mode']}"), "rq1_by_group", out, order=[]))
    parts.append(write(rq1_rows(recs, key=lambda r: f"{r['project']}/{r['mode']}"), "rq1_by_project", out, order=[]))
    parts.append(write(rq1_pooled_rows(recs), "rq1_pooled_reference_only", out))
    parts.append(write(dependency_rows(recs), "level_b_masked_fields", out))
    parts.append(write(rq2_rows(recs), "rq2_by_mode", out))
    parts.append(write(rq3_rows(recs), "rq3_by_mode", out))
    parts.append(write(quality_rows(recs), "quality_by_mode", out))
    parts.append(write(debugger_rows(recs), "debugger_by_mode", out))
    parts.append(write(escape_rows(recs), "escape_check_by_mode", out))
    parts.append(listed_cells(recs))
    (out / "report.md").write_text("\n".join(parts))
    print(out / "report.md")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
