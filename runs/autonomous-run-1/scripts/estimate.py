import csv, re, json, glob, os, statistics as st, sys
from datetime import datetime
R = os.path.expanduser("~/repos/results")
def ts(s): return datetime.fromisoformat(s)
def v2_times(case):
    """dump/baseline: agent seconds, eval seconds (all steps), PIT total seconds, per round."""
    lines = open(f"{R}/v2-release/logs/{case}.log").read().splitlines()
    gen, ev = {}, {}
    last = None
    for l in lines:
        t, _, msg = l.partition(" ")
        m = re.match(r"(baseline|dump)/(r\d): completed ([\d.]+)s", msg)
        if m:
            gen[(m[1], m[2])] = float(m[3]); last = ((m[1], m[2]), ts(t)); continue
        m = re.match(r"(baseline|dump)/(r\d): verify", msg)
        if m and last and last[0] == (m[1], m[2]) and (m[1], m[2]) not in ev:
            ev[(m[1], m[2])] = (ts(t) - last[1]).total_seconds()
    pit = []
    for p in glob.glob(f"{R}/v2-release/cells/{case}/dump/r*/eval-artifacts/pit-log.txt"):
        m = re.search(r"Total\s*:\s*(\d+) seconds", open(p, errors="replace").read())
        if m: pit.append(int(m[1]))
    dump_gen = [v for (mo, _), v in gen.items() if mo == "dump"] or [v for v in gen.values()]
    dump_ev = [v for (mo, _), v in ev.items() if mo == "dump"] or list(ev.values()) or [120]
    return st.mean(dump_gen), st.mean(dump_ev), (st.mean(pit) if pit else 0)
def est(sweep, has_v2):
    out = {}
    for r in csv.DictReader(open(sweep)):
        c, proj, run = r["case"], r["project"], float(r["run_total_s"] or 60)
        if has_v2:
            g, e, pit = v2_times(c)
            agent = min(600, g + run)
            ev = max(30, e - pit - 30)
        else:
            agent = min(600, 150 + run)       # opus trials on the group: 86-121 s with runs of ~25-35 s
            ev = 90                          # verify + rq1 (trace) + quality, short tests on JDK 11
        out[c] = dict(project=proj, cell_s=agent + ev + 15, case_s=2 * (agent + ev + 15))
    return out
if __name__ == "__main__":
    which = sys.argv[1]
    d = est(f"{R}/debug-sweep/sweep-70.csv", True) if which == "70" else est(f"{R}/debug-sweep/sweep-31.csv", False)
    print(json.dumps(d))
