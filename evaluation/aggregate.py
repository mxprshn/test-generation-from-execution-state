"""Aggregate evaluation records into tables (Markdown + CSV).

Usage: python3 -m evaluation.aggregate --config config.yaml [--out <results>/report]

Tables:
  * rq1_by_mode      : recall at levels S (structure), A (io: arguments + result, main data level),
                       B (state: + receiver without dependencies), C (strict), mean and min-max over rounds,
                       scenarios reproduced in all rounds; overall (micro), mean over projects;
  * rq1_by_group     : the same split by group (direct / indirect call);
  * rq1_by_project   : recall per project and mode;
  * rq2_by_mode      : oracle strength and gain over baseline (union of killed mutants);
  * rq3_by_mode      : compile/pass, cost, time, tool calls, preservation of the preliminary test;
  * quality_by_mode  : mock statistics and readability indicators.
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
from runner.cases import case_rows
from runner.config import Config, load_config

MODES = ["baseline", "rendered-raw", "rendered", "dump"]


def load(cfg: Config) -> tuple[list[dict[str, Any]], dict[str, dict[str, str]]]:
    cases = {r["id"]: r for r in case_rows(cfg.cases_dir)}
    rounds = {cfg.run_id(r): str(r) for r in cfg.rounds}
    recs = []
    for p in sorted(cfg.cells_dir.glob("*/*/*/eval.json")):
        r = json.loads(p.read_text())
        if r.get("case_id") in cases and r.get("run_id") in rounds:
            r["group"] = cases[r["case_id"]]["group"]
            r["project"] = cases[r["case_id"]]["project"]
            r["round"] = rounds[r["run_id"]]
            recs.append(r)
    return recs, cases


def _fmt(x: Any, pct: bool = False) -> str:
    if x is None:
        return "—"
    if isinstance(x, float):
        return f"{x * 100:.0f}%" if pct else f"{x:.2f}"
    return str(x)


def rq1_rows(recs: list[dict[str, Any]], key=lambda r: r["mode"]) -> list[dict[str, Any]]:
    by: dict[Any, dict[str, list]] = defaultdict(lambda: defaultdict(list))
    for r in recs:
        q = r.get("rq1")
        if not q or not q.get("scenarios"):
            continue
        by[key(r)][r["round"]].append(r)
    out = []
    for k, rounds in sorted(by.items(), key=lambda kv: str(kv[0])):
        per_round = {}
        for rd, rs in rounds.items():
            n = sum(x["rq1"]["scenarios"] for x in rs)
            sh = sum(x["rq1"]["structure_hits"] for x in rs)
            dh = sum(x["rq1"].get("io_hits", x["rq1"]["data_hits"]) for x in rs)
            bh = sum(x["rq1"].get("state_hits", 0) for x in rs)
            th = sum(x["rq1"].get("strict_hits", 0) for x in rs)
            prec = [x["rq1"]["precision_data"] for x in rs if x["rq1"].get("precision_data") is not None]
            proj: dict[str, list] = defaultdict(list)
            for x in rs:
                proj[x["project"]].append(x["rq1"]["data_hits"] / x["rq1"]["scenarios"])
            per_round[rd] = {
                "scenarios": n, "recall_structure": sh / n, "recall_io": dh / n, "recall_state": bh / n,
                "recall_strict": th / n, "recall_data": dh / n,
                "precision_data": statistics.mean(prec) if prec else None,
                "recall_data_project_mean": statistics.mean(statistics.mean(v) for v in proj.values()),
                "tests_ok": sum(1 for x in rs if x["rq1"].get("candidate_invocations") is not None),
                "cells": len(rs),
            }
        vals = list(per_round.values())
        row = {"key": k, "rounds": len(vals), "scenarios": vals[0]["scenarios"]}
        for f in ("recall_structure", "recall_io", "recall_state", "recall_strict", "precision_data",
                  "recall_data_project_mean"):
            xs = [v[f] for v in vals if v[f] is not None]
            row[f] = statistics.mean(xs) if xs else None
            row[f + "_min"] = min(xs) if xs else None
            row[f + "_max"] = max(xs) if xs else None
        row["tests_ok"] = f"{sum(v['tests_ok'] for v in vals)}/{sum(v['cells'] for v in vals)}"
        out.append(row)
    return out


def stable_hits(recs: list[dict[str, Any]]) -> dict[str, int]:
    """Reference scenarios reproduced at level A in every round, per mode."""
    out: dict[str, int] = {}
    by: dict[tuple[str, str], list] = defaultdict(list)
    for r in recs:
        if r.get("rq1") and r["rq1"].get("scenarios"):
            by[(r["mode"], r["case_id"])].append(set(r["rq1"].get("io_hit_ids") or []))
    for (mode, _), sets in by.items():
        if len(sets) > 1:
            out[mode] = out.get(mode, 0) + len(set.intersection(*sets))
    return out


def rq2_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    base: dict[tuple[str, str], dict[str, str]] = {}
    for r in recs:
        if r["mode"] == "baseline" and isinstance(r.get("rq2"), dict) and "mutants" in r["rq2"]:
            base[(r["case_id"], r["round"])] = r["rq2"]["mutants"]
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        q = r.get("rq2")
        if not isinstance(q, dict) or "mutants" not in q:
            continue
        g = None
        b = base.get((r["case_id"], r["round"])) or base.get((r["case_id"], "1"))
        if b is not None and r["mode"] != "baseline":
            g = M.gain_over_baseline(b, q["mutants"])
        by[r["mode"]].append((q, g))
    out = []
    for mode, items in by.items():
        killed = sum(q["killed"] for q, _ in items)
        covered = sum(q["covered"] for q, _ in items)
        gains = [g for _, g in items if g]
        out.append({
            "key": mode, "cells": len(items),
            "oracle_strength": killed / covered if covered else None,
            "reach": statistics.mean(q["reach"] for q, _ in items if q.get("reach") is not None) if items else None,
            "additional_killed": sum(g["additional"] for g in gains) if gains else None,
            "cases_with_gain": sum(1 for g in gains if g["additional"] > 0) if gains else None,
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
            xs = [f(q) for q in qs if f(q) is not None]
            return statistics.mean(xs) if xs else None
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


def escape_rows(recs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Escape check per mode: cells checked and flagged, by kind of finding."""
    by: dict[str, list] = defaultdict(list)
    for r in recs:
        by[r["mode"]].append(r.get("escape_check"))
    out = []
    for mode, checks in by.items():
        done = [c for c in checks if c]
        flagged = [c for c in done if c.get("status") == "flagged"]
        kinds: dict[str, int] = defaultdict(int)
        for c in flagged:
            for k in {f["kind"] for f in c.get("findings") or []}:
                kinds[k] += 1
        out.append({"key": mode, "cells": len(checks), "checked": len(done), "flagged": len(flagged),
                    "kinds": "; ".join(f"{k}: {v}" for k, v in sorted(kinds.items())) or None})
    return out


def flagged_cells(recs: list[dict[str, Any]]) -> str:
    rows = [r for r in recs if (r.get("escape_check") or {}).get("status") == "flagged"]
    if not rows:
        return "Flagged cells: none.\n"
    md = ["Flagged cells (evaluated as usual):", ""]
    for r in sorted(rows, key=lambda r: (r["case_id"], r["mode"], r["run_id"])):
        f = r["escape_check"]["findings"]
        md.append(f"- `{r['case_id']}` {r['mode']}/{r['run_id']}: "
                  + "; ".join(sorted({x['kind'] for x in f})) + f" (e.g. {f[0]['where']}: `{f[0]['evidence']}`)")
    return "\n".join(md) + "\n"


def write(rows: list[dict[str, Any]], name: str, out: Path, order=MODES) -> str:
    if not rows:
        return f"### {name}\n\n(no data)\n"
    rows = sorted(rows, key=lambda r: (order.index(r["key"]) if r["key"] in order else 99, str(r["key"])))
    cols = list(rows[0].keys())
    with (out / f"{name}.csv").open("w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=cols)
        w.writeheader()
        w.writerows(rows)
    md = [f"### {name}", "", "| " + " | ".join(cols) + " |", "|" + "---|" * len(cols)]
    for r in rows:
        md.append("| " + " | ".join(_fmt(r[c]) for c in cols) + " |")
    return "\n".join(md) + "\n"


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--out", default=None)
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    out = Path(a.out) if a.out else cfg.results_dir / "report"
    out.mkdir(parents=True, exist_ok=True)
    recs, _ = load(cfg)
    parts = [f"# Summary\n\nEvaluated cells: {len(recs)}.\n"]
    parts.append(write(rq1_rows(recs), "rq1_by_mode", out))
    parts.append(write(rq1_rows(recs, key=lambda r: f"{r['group']}/{r['mode']}"), "rq1_by_group", out, order=[]))
    parts.append(write(rq1_rows(recs, key=lambda r: f"{r['project']}/{r['mode']}"), "rq1_by_project", out, order=[]))
    parts.append(f"Scenarios reproduced in every round (level A keys): {stable_hits(recs)}\n")
    parts.append(write(rq2_rows(recs), "rq2_by_mode", out))
    parts.append(write(rq3_rows(recs), "rq3_by_mode", out))
    parts.append(write(quality_rows(recs), "quality_by_mode", out))
    parts.append(write(escape_rows(recs), "escape_check_by_mode", out))
    parts.append(flagged_cells(recs))
    (out / "report.md").write_text("\n".join(parts))
    print(out / "report.md")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
