"""Summary of the tools run: per group x tool: cells, built/compiling/passing, S/A/B/C, failure reasons;
alongside autonomous/baseline/dump on the same cases (mean over cells, missing rq1 = 0)."""
import json, glob, collections, statistics as st, re, sys
R = "/<home>/repos/results"
def cells(root, mode, rounds=("r1", "r2")):
    out = {}
    for p in glob.glob(f"{R}/{root}/cells/*/{mode}/r[12]/eval.json"):
        run = p.split("/")[-2]
        if run not in rounds: continue
        e = json.load(open(p)); s = json.load(open(p.replace("eval.json", "summary.json")))
        out[(e["case_id"], run)] = (e, s)
    return out
def rq(e, k):
    return ((e.get("rq1") or {}).get(f"recall_{k}") or 0.0)
def reason(e, s):
    if s["status"] == "completed":
        v = e.get("verify") or {}
        if not v.get("compiles"): return "test does not compile"
        if not v.get("passes"): return "test fails"
        return "passes"
    o = str((s.get("tool") or {}).get("outcome") or s.get("error"))
    o = re.sub(r":.*", "", o) if o.startswith("tool error") else o
    return o
groups = {"70": ("tools-v2", ["autonomous-v2:autonomous", "v2-release:baseline", "v2-release:dump"]),
          "31": ("tools-tools-projects", ["autonomous-tools-projects:autonomous"])}
for g, (root, refs) in groups.items():
    print(f"=== group {g}")
    for tool in ("pankti", "rick", "microtestcarver"):
        c = cells(root, tool, ("r1",))
        if not c: continue
        n = len(c); built = sum(1 for e, s in c.values() if s["status"] == "completed")
        comp = sum(1 for e, s in c.values() if (e.get("verify") or {}).get("compiles"))
        pas = sum(1 for e, s in c.values() if (e.get("verify") or {}).get("passes"))
        m = [st.mean(rq(e, k) for e, s in c.values()) for k in ("structure", "io", "state", "strict")]
        reasons = collections.Counter(reason(e, s) for e, s in c.values())
        objs = [((e.get("quality") or {}).get("object_files") or 0) for e, s in c.values() if e.get("quality")]
        refl = sum(1 for e, s in c.values() if ((e.get("quality") or {}).get("readability") or {}).get("reflection"))
        secs = [s.get("seconds") or 0 for e, s in c.values()]
        print(f"{tool:16} cells {n:3} built {built:3} compile {comp:3} pass {pas:3}  S/A/B/C {m[0]:.3f}/{m[1]:.3f}/{m[2]:.3f}/{m[3]:.3f}  median {st.median(secs):.0f}s  with reflection {refl}  object files {sum(objs)}")
        for r, k in reasons.most_common(): print(f"    {k:3} {r[:120]}")
        cases = {cid for cid, _ in c}
        for ref in refs:
            rr, mode = ref.split(":")
            o = {k: v for k, v in cells(rr, mode).items() if k[0] in cases}
            if not o: continue
            mo = [st.mean(rq(e, k) for e, s in o.values()) for k in ("structure", "io", "state", "strict")]
            po = sum(1 for e, s in o.values() if (e.get("verify") or {}).get("passes"))
            print(f"    vs {mode:11} ({len(o)} cells) pass {po}  S/A/B/C {mo[0]:.3f}/{mo[1]:.3f}/{mo[2]:.3f}/{mo[3]:.3f}")
