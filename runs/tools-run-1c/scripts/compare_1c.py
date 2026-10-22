"""tools-run-1c vs tools-run-1/1b: MicroTestCarver cells, cell by cell (outcome category, rq1, quality).
Usage: python3 compare_1c.py [--md]  (reads ~/repos/results/{tools-v2,tools-tools-projects,tools-mtc-1c-*})."""
import collections, json, sys
from pathlib import Path

R = Path.home() / "repos/results"
GROUPS = [("70 cases", R / "tools-v2", R / "tools-mtc-1c-v2"),
          ("31 cases", R / "tools-tools-projects", R / "tools-mtc-1c-tools-projects")]
ORDER = ["not recorded", "generator crash", "built, not compiling", "compiles, 0 tests", "compiles, failing", "passes"]


def load(cell: Path):
    s = json.loads((cell / "summary.json").read_text())
    e = json.loads((cell / "eval.json").read_text()) if (cell / "eval.json").is_file() else {}
    return s, e


def category(s, e) -> str:
    err = str(s.get("error") or "")
    v = e.get("verify") or {}
    if s.get("status") != "completed":
        if "generation failed" in err or "no test class generated" in err or "tool error" in err:
            return "generator crash"
        return "not recorded"
    if v.get("passes"):
        return "passes"
    if v.get("compiles") is False or v.get("compile") is False:
        return "built, not compiling"
    if (v.get("tests_run") or 0) == 0:
        return "compiles, 0 tests"
    return "compiles, failing"


def rq1(e):
    q = e.get("rq1") or {}
    return tuple(q.get(k) or 0.0 for k in ("recall_structure", "recall_io", "recall_state", "recall_strict"))


def main():
    md = "--md" in sys.argv
    for name, old_root, new_root in GROUPS:
        trans = collections.Counter(); old_c = collections.Counter(); new_c = collections.Counter()
        rows = []; sums = {"old": [0.0] * 4, "new": [0.0] * 4}; n = 0; missing = []
        for new_cell in sorted((new_root / "cells").glob("*/microtestcarver/r1")):
            case = new_cell.parts[-3]
            old_cell = old_root / "cells" / case / "microtestcarver" / "r1"
            if not (new_cell / "summary.json").is_file() or not (old_cell / "summary.json").is_file():
                missing.append(case)
                continue
            so, eo = load(old_cell); sn, en = load(new_cell)
            co, cn = category(so, eo), category(sn, en)
            trans[(co, cn)] += 1; old_c[co] += 1; new_c[cn] += 1; n += 1
            ro, rn = rq1(eo), rq1(en)
            for i in range(4):
                sums["old"][i] += ro[i]; sums["new"][i] += rn[i]
            qo = ((eo.get("quality") or {}).get("readability") or {}); qn = ((en.get("quality") or {}).get("readability") or {})
            if co != cn or ro != rn:
                rows.append((case, co, cn, ro, rn, str(sn.get("error") or "")[:90]))
        print(f"## {name}: {n} cells compared" + (f", missing {missing}" if missing else ""))
        print("| tools-run-1/1b → tools-run-1c | cells |\n|---|---|" if md else "transitions:")
        for (co, cn), k in sorted(trans.items(), key=lambda x: (ORDER.index(x[0][0]), ORDER.index(x[0][1]))):
            print(f"| {co} → {cn} | {k} |" if md else f"  {co:22} -> {cn:22} {k}")
        print("categories old:", {c: old_c[c] for c in ORDER if old_c[c]}, " new:", {c: new_c[c] for c in ORDER if new_c[c]})
        if n:
            print("mean S/A/B/C old: " + "/".join(f"{x / n:.3f}" for x in sums["old"]) +
                  "  new: " + "/".join(f"{x / n:.3f}" for x in sums["new"]))
        for case, co, cn, ro, rn, err in rows:
            print(f"  changed: {case}: {co} -> {cn}; rq1 {ro} -> {rn}; {err}")
        print()


if __name__ == "__main__":
    main()
