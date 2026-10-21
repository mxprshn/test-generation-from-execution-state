"""Reproduce the selection of the 31 tools-projects cases from the committed scan data.

    python3 scripts/tools-projects/selection_report.py PROJECTS_DIR > selection.md

For every target the scan watched (data/scan/*.tsv: invocations per test method, recorded with Scan.java
under a debugger), it applies the rules of pick.py and reports what became of the target:
  - no test method reaches it 1-13 times, or every such test names the method in its body: no scenario;
  - static or non-public (Scan marks them): out (Pankti and RICK select public instance methods);
  - a scenario exists: the candidate of data/candidates.tsv chosen for it, or "not taken" (the choice among
    possible targets was made by hand and its reasons were not recorded);
and lists the candidates dropped after the debugger check (data/dropped). PROJECTS_DIR holds the checkouts
(pdfbox, graphhopper, spring-petclinic) whose test sources pick.py reads.
"""
import collections
import os
import re
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
DATA = HERE / "data"
SCANS = {"pdfbox-all.tsv": ("pdfbox", ["pdfbox/src/test/java"]),
         "gh-core.tsv": ("graphhopper", ["core/src/test/java"]),
         "petclinic-all.tsv": ("spring-petclinic", ["src/test/java"])}


def body(test, roots):
    cls, meth = test.split("#")
    rel = cls.replace(".", "/") + ".java"
    for r in roots:
        p = os.path.join(r, rel)
        if os.path.exists(p):
            src = open(p, encoding="utf-8", errors="replace").read()
            m = re.search(r"void\s+" + re.escape(meth) + r"\s*\(", src)
            if not m:
                return src
            i = src.index("{", m.end())
            depth, j = 0, i
            while j < len(src):
                if src[j] == "{":
                    depth += 1
                elif src[j] == "}":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            return src[m.start():j + 1]
    return None


def main(projects: str) -> None:
    cands = [l.rstrip("\n").split("\t") for l in open(DATA / "candidates.tsv") if l.strip() and not l.startswith("#")]
    chosen = {(c[3] + "#" + c[4]): c for c in cands}
    out = ["| project | target (scanned) | outcome |", "|---|---|---|"]
    for scan, (proj, rel_roots) in SCANS.items():
        roots = [os.path.join(projects, proj, r) for r in rel_roots]
        rows = collections.defaultdict(list)
        flags = {}
        for line in open(DATA / "scan" / scan):
            if line.startswith("CAPPED"):
                continue
            n, rest = line.rstrip("\n").split("\t", 1)
            target, test = rest.split(" <- ")
            key = target.split("(")[0].split(" ")[0]
            flags[key] = flags.get(key, "") + (" static" if " static" in target else "") + (" nonpublic" if " nonpublic" in target else "")
            rows[key].append((int(n), test))
        for key in sorted(rows):
            name = key.split("#")[1]
            if "static" in flags[key] or "nonpublic" in flags[key]:
                outcome = "out: static or non-public"
            else:
                ok = [(n, t) for n, t in rows[key] if 1 <= n <= 13 and t not in ("?", "ERR")
                      and (b := body(t, roots)) is not None and not re.search(r"\b" + re.escape(name) + r"\s*\(", b)]
                if key in chosen:
                    outcome = f"case {chosen[key][0]} ({chosen[key][6]})"
                elif not ok:
                    outcome = "no scenario: no test reaches it 1-13 times without naming it"
                else:
                    outcome = (f"scenario possible ({len(ok)} tests), not taken; the reason was not recorded "
                               "(see docs/tools-projects.md, Limitations of the selection)")
            out.append(f"| {proj} | {key} | {outcome} |")
    for y in sorted((DATA / "dropped").glob("*.yaml")):
        out.append(f"| lab-insurance | {y.stem} | dropped after the debugger check (docs/tools-projects.md) |")
    print("\n".join(out))


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser("~/repos/tools-projects/projects"))
