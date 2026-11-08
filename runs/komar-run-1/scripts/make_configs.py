"""Write the configs of komar-run-1 from the smoke configs (settings of autonomous-run-1) and the case queue.
One config per worker and group (only work dir differs); cases are dispatched one by one (scripts/worker.sh)."""
import csv, json, os, re, sys
KR = os.path.expanduser("~/repos/komar-run"); REPO = os.path.expanduser("~/repos/tg-komar")
FR = os.path.expanduser("~/repos/full-run")
WORKERS = int(sys.argv[1]) if len(sys.argv) > 1 else 12
GROUPS = {"70": ("smoke-70.yaml", "cases.csv", "est-70.json", "/<root>/results/komar-v2"),
          "31": ("smoke-31.yaml", "tools-projects.csv", "est-31.json", "/<root>/results/komar-tools-projects")}
queue = []
for g, (smoke, csvname, estf, results) in GROUPS.items():
    rows = list(csv.DictReader(open(f"{REPO}/cases/{csvname}")))
    key = "id" if "id" in rows[0] else list(rows[0])[0]
    ids = [r[key] for r in rows]
    est = json.load(open(f"{FR}/{estf}"))
    assert set(ids) == set(est), g
    queue += [(est[c]["case_s"], g, est[c]["project"], c) for c in ids]
    t = open(f"{KR}/{smoke}").read()
    t = re.sub(r"^# .*\n", f"# komar-run-1, group {g}: settings of autonomous-run-1, mode komar, rounds 1-2, no PIT; one case per run.py call (scripts/worker.sh); tag komar-run-1b\n", t, count=1)
    t = re.sub(r"^rounds:\n- 1\n", "rounds:\n- 1\n- 2\n", t, flags=re.M)
    t = re.sub(r"^parallel_cases: \d+$", "parallel_cases: 1", t, flags=re.M)
    t = re.sub(r"^  results: .*$", f"  results: {results}", t, flags=re.M)
    t = re.sub(r"^cases:\n(- .*\n)+", "cases:\n" + "".join(f"- {c}\n" for c in ids), t, flags=re.M)
    assert "- 2\n" in t and f"  results: {results}" in t and t.count("\n- ") >= len(ids)
    for w in range(1, WORKERS + 1):
        open(f"{KR}/w{w}-{g}.yaml", "w").write(re.sub(r"^  work: .*$", f"  work: /<root>/komar-run/work/w{w}", t, flags=re.M))
# projects with the most estimated work first, longest cases first within a project: the dispatcher
# (scripts/worker.sh) takes the first case of a project not running, so the largest project stays busy
total = {}
for s_, _, p, _ in queue:
    total[p] = total.get(p, 0) + s_
queue.sort(key=lambda x: (-total[x[2]], -x[0], x[3]))
open(f"{KR}/queue.txt", "w").write("".join(f"{g} {p} {c}\n" for _, g, p, c in queue))
print(len(queue), "cases;", WORKERS, "worker configs per group")
