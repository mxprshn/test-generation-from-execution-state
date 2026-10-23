"""Code style of the generated tests (Table 3 of the paper): naturalness, reflection, objects loaded from XML.

A post-hoc analysis over finished results directories, not an evaluation step of the pipeline: it does
not run any build, it does not write into a results directory and it is not part of any cell's input
hashes (evaluation/inputs.py).

Unit of count: a test method (@Test, @ParameterizedTest, @RepeatedTest; JUnit 4 and 5). Every built test
is measured, also one that does not compile or fails; `passes` records the verify outcome of its cell.

* Naturalness (Hindle et al., ICSE 2012): for each case a trigram language model of the project's tests
  is built with interpolated Kneser–Ney smoothing (discount 0.75); the corpus is every Java test source of
  the project at the pinned commit (src/test/java*, tst/) except the hidden paths of the case (the
  developers' tests of the target class) and except generated tests. Comments, imports and the package
  line are dropped; string, character and number literals are replaced by <STR>, <CHR>, <NUM>; tokens
  seen once in the corpus are <UNK>. The naturalness of a test method is its mean -log2 p(token | two
  previous tokens) in bits per token: lower means closer to the project's tests. The class-level value
  (the whole test file) is recorded as a robustness check.
* Reflection: the method, its @Before/@BeforeEach/@BeforeClass/@BeforeAll methods and the helper methods
  it calls (three levels) call getDeclaredField/getField/getDeclaredMethod/getMethod/getDeclaredConstructor
  (and plural forms), setAccessible, invoke, Class.forName, ReflectionTestUtils, Whitebox or FieldUtils.
* Objects from XML: the same code calls deserializeObjectFrom* or fromXML (XStream, as in the tests of
  Pankti and RICK).

The definitions are those of the earlier study of the paper (tool comparison on 18 cases), ported to the
layout of this repository. One deviation: test roots are src/test/java*, tst/ only; the earlier regex also
took .java files under src/test/resources and tst-rsrc, which in checkstyle (3302 input files of checks)
and liquibase are not tests. For jsoup, jgit and mybatis both give the same corpus.

    python3 -m evaluation.code_style --out DIR --results v2-release=PATH autonomous-v2=PATH ... \
        [--modes-from NAME=mode,mode ...] [--jobs N]
"""
from __future__ import annotations

import argparse
import csv
import json
import math
import multiprocessing as mp
import re
import statistics
import subprocess
import sys
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any

from evaluation import quality as Q
from runner.cases import case_rows, load_case, load_projects

ROOT = Path(__file__).resolve().parent.parent

# --- tokens (as in the earlier study) -------------------------------------------------------------
TOK = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|[A-Za-z_$][\w$]*|\d[\w.]*|>>>=|<<=|>>=|>>>'
                 r'|[-+*/%&|^!=<>]=|&&|\|\||\+\+|--|->|::|\S')
LEX = re.compile(r'"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'|/\*.*?\*/|//[^\n]*', re.S)
TEST_ROOT = re.compile(r"(^|/)(src/test/java[^/]*|tst)/")
TEST_ANN = re.compile(r"@(?:Test|ParameterizedTest|RepeatedTest)\b")


def strip(src: str) -> str:
    """Comments removed (outside literals); import and package lines dropped."""
    s = LEX.sub(lambda m: "" if m.group(0).startswith("/") else m.group(0), src)
    return "\n".join(l for l in s.split("\n") if not l.strip().startswith(("import ", "package ")))


def toks(src: str) -> list[str]:
    out = []
    for t in TOK.findall(strip(src)):
        if t[0] == '"':
            out.append("<STR>")
        elif t[0] == "'":
            out.append("<CHR>")
        elif t[0].isdigit():
            out.append("<NUM>")
        else:
            out.append(t)
    return out


class KN3:
    """Interpolated Kneser–Ney trigram model (as in the earlier study)."""

    def __init__(self, docs: list[list[str]], d: float = 0.75):
        self.d = d
        c1: Counter = Counter()
        for doc in docs:
            c1.update(doc)
        self.known = {w for w, n in c1.items() if n > 1}
        self.c3: Counter = Counter()
        self.c2ctx: Counter = Counter()
        self.n1p3: dict = defaultdict(set)
        self.cont: dict = defaultdict(set)
        self.cont2: dict = defaultdict(set)
        for doc in docs:
            w = ["<s>", "<s>"] + [self.m(t) for t in doc]
            for i in range(2, len(w)):
                a, b, x = w[i - 2], w[i - 1], w[i]
                self.c3[(a, b, x)] += 1
                self.c2ctx[(a, b)] += 1
                self.n1p3[(a, b)].add(x)
                self.cont[x].add(b)
                self.cont2[(b, x)].add(a)
        self.ncont = sum(len(v) for v in self.cont.values())
        self.V = len(self.cont) + 1
        self.cont2ctx: Counter = Counter()
        self.n1p2c: dict = defaultdict(int)
        for (b, _x), v in self.cont2.items():
            self.cont2ctx[b] += len(v)
            self.n1p2c[b] += 1

    def m(self, t: str) -> str:
        return t if t in self.known else "<UNK>"

    def p1(self, x: str) -> float:
        return (len(self.cont.get(x, ())) + 0.5) / (self.ncont + 0.5 * self.V)

    def p2(self, b: str, x: str) -> float:
        n = self.cont2ctx.get(b, 0)
        if not n:
            return self.p1(x)
        c = len(self.cont2.get((b, x), ()))
        return max(c - self.d, 0) / n + self.d * self.n1p2c[b] / n * self.p1(x)

    def p3(self, a: str, b: str, x: str) -> float:
        n = self.c2ctx.get((a, b), 0)
        if not n:
            return self.p2(b, x)
        return max(self.c3.get((a, b, x), 0) - self.d, 0) / n + self.d * len(self.n1p3[(a, b)]) / n * self.p2(b, x)

    def xent(self, doc: list[str]) -> float | None:
        w = ["<s>", "<s>"] + [self.m(t) for t in doc]
        if len(w) == 2:
            return None
        return -sum(math.log2(self.p3(w[i - 2], w[i - 1], w[i])) for i in range(2, len(w))) / (len(w) - 2)


# --- test methods ---------------------------------------------------------------------------------
NAME = re.compile(r"(?<![@\w])(\w+)\s*\(")


def test_methods(src: str) -> list[tuple[str, str]]:
    """(name, source text) of every test method; comments dropped, literals masked when matching braces."""
    t = LEX.sub(lambda m: "" if m.group(0).startswith("/") else m.group(0), src)
    masked = LEX.sub(lambda m: m.group(0)[0] + "x" * (len(m.group(0)) - 2) + m.group(0)[-1], t)
    out = []
    for m in TEST_ANN.finditer(masked):
        b = masked.find("{", m.end())
        if b < 0:
            continue
        head = masked[m.end():b]
        names = [n for n in NAME.findall(head) if n not in ("void",)]
        i, d = b + 1, 1
        while d and i < len(masked):
            d += {"{": 1, "}": -1}.get(masked[i], 0)
            i += 1
        out.append((names[0] if names else "?", t[m.start():i]))
    return out


REFL = re.compile(r"\.get(?:Declared)?(?:Field|Method|Constructor)s?\s*\(|\.setAccessible\s*\(|\.invoke\s*\("
                  r"|Class\.forName\s*\(|\b(?:ReflectionTestUtils|Whitebox|FieldUtils)\.\w+\s*\(")
DESER = re.compile(r"deserializeObjectFrom\w+\s*\(|fromXML\s*\(")
METHOD = re.compile(r"((?:@\w+(?:\([^)]*\))?\s*)*)(?:public |protected |private |static |final |synchronized "
                    r"|<[^>]+> )*[\w<>\[\],.? ]+\s+(\w+)\s*\([^;{]*\)\s*(?:throws [\w., ]+)?\{")


def _strip_simple(s: str) -> str:
    s = re.sub(r'"(?:\\.|[^"\\])*"', '""', s)
    s = re.sub(r"/\*.*?\*/", "", s, flags=re.S)
    return re.sub(r"//[^\n]*", "", s)


def reflection_xml(src: str) -> list[tuple[str, bool, bool]]:
    """(name, uses reflection, loads objects from XML) per test method; setup and called helpers count."""
    s = _strip_simple(src)
    ms = []
    for m in METHOD.finditer(s):
        i, d = m.end(), 1
        while d and i < len(s):
            d += {"{": 1, "}": -1}.get(s[i], 0)
            i += 1
        ms.append((m.group(1), m.group(2), s[m.end():i]))
    helpers = {n: b for a, n, b in ms if not TEST_ANN.search(a)}
    setup = "".join(b for a, n, b in ms if re.search(r"@Before(Each|All|Class)?\b", a))
    res = []
    for a, n, b in ms:
        if not TEST_ANN.search(a):
            continue
        body = b + setup
        seen: set[str] = set()
        frontier = [body]
        for _ in range(3):
            nxt = []
            for txt in frontier:
                for h in set(re.findall(r"\b(\w+)\s*\(", txt)):
                    if h in helpers and h not in seen:
                        seen.add(h)
                        nxt.append(helpers[h])
            frontier = nxt
        full = body + "".join(helpers[h] for h in seen)
        res.append((n, bool(REFL.search(full)), bool(DESER.search(full))))
    return res


# --- data -----------------------------------------------------------------------------------------
def git_files(checkout: Path, commit: str) -> list[str]:
    out = subprocess.run(["git", "-C", str(checkout), "ls-tree", "-r", "--name-only", commit],
                         check=True, capture_output=True, text=True).stdout
    return out.splitlines()


def git_show(checkout: Path, commit: str, path: str) -> str:
    return subprocess.run(["git", "-C", str(checkout), "show", f"{commit}:{path}"], check=True,
                          capture_output=True).stdout.decode("utf-8", errors="replace")


def hidden(path: str, hidden_paths: list[str]) -> bool:
    return any(path == h or path.startswith(h.rstrip("/") + "/") for h in hidden_paths)


def test_sources(files: list[str]) -> list[str]:
    return [f for f in files if f.endswith(".java") and TEST_ROOT.search(f) and "CarvedTest" not in f]


def cell_test(cell: Path, output_test_file: str) -> str | None:
    """Source of the generated test class of a cell (the file at output_test_file in result.diff)."""
    diff = cell / "result.diff"
    if not diff.is_file() or not diff.read_text().strip():
        return None
    files = Q.added_by_file(diff.read_text())
    if output_test_file in files:
        return files[output_test_file]
    java = [t for n, t in files.items() if n.endswith(".java")]
    return "\n".join(java) if java else None


def object_files(cell: Path) -> int:
    diff = cell / "result.diff"
    if not diff.is_file():
        return 0
    return sum(1 for n in Q.added_by_file(diff.read_text()) if not n.endswith((".java", "pom.xml")))


def collect_cells(results: dict[str, Path], modes_from: dict[str, set[str]]) -> list[dict[str, Any]]:
    """Cells r1/r2 of every results directory (set-aside cells such as r2.infra-* are skipped)."""
    cells = []
    for name, root in results.items():
        allowed = modes_from.get(name)
        for summ in sorted((root / "cells").glob("*/*/r[0-9]/summary.json")):
            cell = summ.parent
            mode = cell.parent.name
            if allowed is not None and mode not in allowed:
                continue
            ev = cell / "eval.json"
            rec = json.loads(ev.read_text()) if ev.is_file() else json.loads(summ.read_text())
            cells.append({"source": name, "case": cell.parent.parent.name, "mode": mode, "run": cell.name,
                          "cell": str(cell), "passes": bool((rec.get("verify") or {}).get("passes")),
                          "status": rec.get("status"),
                          "object_files_eval": (rec.get("quality") or {}).get("object_files")})
    return cells


def analyse_case(job: dict[str, Any]) -> list[dict[str, Any]]:
    case = load_case(job["case"])
    proj = load_projects()[case.project]
    checkout = Path(job["checkout"])
    files = git_files(checkout, proj.commit)
    corpus_paths = [f for f in test_sources(files) if not hidden(f, case.hidden_paths)]
    lm = KN3([toks(git_show(checkout, proj.commit, f)) for f in corpus_paths])
    rows = []
    sources = [("developers", "-", None, True, [git_show(checkout, proj.commit, f)
                                                for f in test_sources([p for p in files if hidden(p, case.hidden_paths)])])]
    for c in job["cells"]:
        src = cell_test(Path(c["cell"]), case.output_test_file)
        if src is not None:
            sources.append((c["mode"], c["run"], c, c["passes"], [src]))
    for mode, run, c, passes, srcs in sources:
        for k, src in enumerate(srcs):
            nat = test_methods(src)
            rx = reflection_xml(src)
            byname: dict[str, list] = defaultdict(list)
            for n, r, x in rx:
                byname[n].append((r, x))
            klass = lm.xent(toks(src))
            for idx, (n, text) in enumerate(nat):
                r, x = byname[n].pop(0) if byname.get(n) else (None, None)
                rows.append({"group": job["group"], "project": case.project, "case": case.id, "mode": mode,
                             "run": run, "file": k, "method": n, "index": idx,
                             "naturalness": lm.xent(toks(text)), "class_naturalness": klass,
                             "reflection": r, "xml": x, "passes": passes,
                             "object_files": object_files(Path(c["cell"])) if c else None,
                             "object_files_eval": c["object_files_eval"] if c else None,
                             "corpus_files": len(corpus_paths)})
            if not nat and c is not None:
                rows.append({"group": job["group"], "project": case.project, "case": case.id, "mode": mode,
                             "run": run, "file": k, "method": None, "index": None, "naturalness": None,
                             "class_naturalness": klass, "reflection": None, "xml": None, "passes": passes,
                             "object_files": object_files(Path(c["cell"])), "object_files_eval": c["object_files_eval"],
                             "corpus_files": len(corpus_paths)})
    return rows


# --- tables ---------------------------------------------------------------------------------------
MODE_ORDER = ["developers", "baseline", "dump", "rendered", "rendered-raw", "autonomous", "pankti", "rick",
              "microtestcarver"]


def _mean(xs):
    xs = [x for x in xs if x is not None]
    return statistics.mean(xs) if xs else None


def _median(xs):
    xs = [x for x in xs if x is not None]
    return statistics.median(xs) if xs else None


def summarise(rows: list[dict[str, Any]], keys: tuple[str, ...]) -> list[dict[str, Any]]:
    groups: dict[tuple, list] = defaultdict(list)
    for r in rows:
        groups[tuple(r[k] for k in keys)].append(r)
    out = []
    for key, rs in groups.items():
        ms = [r for r in rs if r["method"] is not None]
        nat = [r["naturalness"] for r in ms]
        cells = {(r["case"], r["run"]) for r in rs}
        per_case = defaultdict(list)
        for r in ms:
            per_case[r["case"]].append(r["naturalness"])
        row = dict(zip(keys, key))
        row.update({
            "cells_with_test": len(cells), "cases": len({r["case"] for r in rs}), "test_methods": len(ms),
            "naturalness_mean": _mean(nat), "naturalness_median": _median(nat),
            "naturalness_mean_over_cases": _mean([_mean(v) for v in per_case.values()]),
            "class_naturalness_mean": _mean([r["class_naturalness"] for r in {(r["case"], r["run"], r["file"]): r for r in rs}.values()]),
            "reflection_methods": sum(1 for r in ms if r["reflection"]),
            "reflection_pct": 100 * sum(1 for r in ms if r["reflection"]) / len(ms) if ms else None,
            "xml_methods": sum(1 for r in ms if r["xml"]),
            "xml_pct": 100 * sum(1 for r in ms if r["xml"]) / len(ms) if ms else None,
            "passing_test_methods": sum(1 for r in ms if r["passes"]),
            "naturalness_mean_passing": _mean([r["naturalness"] for r in ms if r["passes"]]),
        })
        out.append(row)
    order = {m: i for i, m in enumerate(MODE_ORDER)}
    return sorted(out, key=lambda r: tuple(order.get(r.get(k), 99) if k == "mode" else str(r.get(k)) for k in keys))


def dominance(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Per group: in how many cases the mean naturalness of a mode's methods is above (less natural) /
    below that of the autonomous mode; for the tools also against every agent mode at once."""
    per = defaultdict(lambda: defaultdict(list))
    for r in rows:
        if r["method"] is not None and r["naturalness"] is not None:
            per[(r["group"], r["case"])][r["mode"]].append(r["naturalness"])
    out = []
    groups = sorted({g for g, _ in per})
    for g in groups:
        cases = {c: {m: statistics.mean(v) for m, v in ms.items()} for (gg, c), ms in per.items() if gg == g}
        for mode in MODE_ORDER:
            if mode == "autonomous":
                continue
            both = [c for c, ms in cases.items() if mode in ms and "autonomous" in ms]
            row = {"group": g, "mode": mode, "cases_both": len(both),
                   "less_natural_than_autonomous": sum(1 for c in both if cases[c][mode] > cases[c]["autonomous"]),
                   "more_natural_than_autonomous": sum(1 for c in both if cases[c][mode] < cases[c]["autonomous"])}
            if mode in ("pankti", "rick", "microtestcarver"):
                agents = [m for m in ("baseline", "dump", "rendered", "autonomous")]
                have = [c for c, ms in cases.items() if mode in ms and any(a in ms for a in agents)]
                row["cases_with_agent_modes"] = len(have)
                row["less_natural_than_every_agent_mode"] = sum(
                    1 for c in have if cases[c][mode] > max(cases[c][a] for a in agents if a in cases[c]))
            out.append(row)
    return out


def write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    keys: list[str] = []
    for r in rows:
        for k in r:
            if k not in keys:
                keys.append(k)
    with path.open("w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=keys)
        w.writeheader()
        for r in rows:
            w.writerow({k: (round(v, 4) if isinstance(v, float) else v) for k, v in r.items()})


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--results", nargs="+", required=True, help="NAME=PATH of results directories")
    ap.add_argument("--modes-from", nargs="*", default=[], help="NAME=mode,mode: only these modes of NAME")
    ap.add_argument("--projects", nargs="+", required=True, help="directories holding the project checkouts")
    ap.add_argument("--out", required=True)
    ap.add_argument("--jobs", type=int, default=4)
    args = ap.parse_args()
    results = {n: Path(p) for n, p in (a.split("=", 1) for a in args.results)}
    modes_from = {n: set(m.split(",")) for n, m in (a.split("=", 1) for a in args.modes_from)}
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    group = {r["id"]: "70" for r in case_rows()}
    group.update({r["id"]: "31" for r in case_rows(case_list="tools-projects.csv")})
    projects = load_projects()
    cells = collect_cells(results, modes_from)
    dup = Counter((c["case"], c["mode"], c["run"]) for c in cells)
    if any(n > 1 for n in dup.values()):
        sys.exit(f"cells present in two results directories: {[k for k, n in dup.items() if n > 1][:5]}")
    by_case = defaultdict(list)
    for c in cells:
        by_case[c["case"]].append(c)
    jobs = []
    for case_id in sorted(group):
        proj = projects[load_case(case_id).project]
        checkout = next((Path(p) / proj.checkout_name for p in args.projects
                         if (Path(p) / proj.checkout_name / ".git").exists()), None)
        if checkout is None:
            sys.exit(f"no checkout of {proj.checkout_name} in {args.projects}")
        jobs.append({"case": case_id, "group": group[case_id], "checkout": str(checkout), "cells": by_case[case_id]})
    jobs.sort(key=lambda j: -len(git_files(Path(j["checkout"]), projects[load_case(j["case"]).project].commit)))
    rows: list[dict[str, Any]] = []
    with mp.Pool(args.jobs) as pool:
        for done, rs in enumerate(pool.imap_unordered(analyse_case, jobs), 1):
            rows.extend(rs)
            print(f"{done}/{len(jobs)} {rs[0]['case'] if rs else ''}", flush=True)
    rows.sort(key=lambda r: (r["group"], r["case"], MODE_ORDER.index(r["mode"]), r["run"], r["file"], r["index"] or 0))
    with (out / "methods.jsonl").open("w") as f:
        for r in rows:
            f.write(json.dumps(r) + "\n")
    write_csv(out / "by_mode.csv", summarise(rows, ("group", "mode")))
    write_csv(out / "by_project_mode.csv", summarise(rows, ("group", "project", "mode")))
    write_csv(out / "by_case_mode.csv", summarise(rows, ("group", "case", "mode")))
    write_csv(out / "dominance.csv", dominance(rows))
    mism = [(c["case"], c["mode"], c["run"], c["object_files_eval"], object_files(Path(c["cell"])))
            for c in cells if c["object_files_eval"] is not None and c["object_files_eval"] != object_files(Path(c["cell"]))]
    print(f"cells {len(cells)}, test methods {sum(1 for r in rows if r['method'] is not None)}, "
          f"object-file count mismatches with eval.json: {len(mism)} {mism[:5]}")


if __name__ == "__main__":
    main()
