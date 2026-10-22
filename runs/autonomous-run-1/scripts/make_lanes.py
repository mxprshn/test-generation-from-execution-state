"""Write the lane configs of the full run from the base configs (text substitution, comments kept)."""
import csv, json, os, re
FR = os.path.expanduser("~/repos/full-run")
REPO = os.path.expanduser("~/repos/test-generation-from-execution-state")
def ids(csvname):
    with open(f"{REPO}/cases/{csvname}") as h:
        rows = list(csv.DictReader(h))
    key = "id" if "id" in rows[0] else list(rows[0])[0]
    return [(r[key], r.get("project")) for r in rows]
LANES = {
    "config.yaml": ("cases.csv", "est-70.json", "work", [
        ("lane-a.yaml", "a", {"mybatis", "checkstyle", "jsoup"}, "5100-5149"),
        ("lane-b.yaml", "b", {"apollo", "jgit", "klaw", "liquibase"}, "5150-5199")]),
    "config-tools-projects.yaml": ("tools-projects.csv", "est-31.json", "work-tp", [
        ("tp-lane-a.yaml", "a", {"pdfbox"}, "5300-5349"),
        ("tp-lane-b.yaml", "b", {"graphhopper", "spring-petclinic", "lab-insurance"}, "5350-5399")]),
}
summary = {}
for base, (csvname, estf, work, lanes) in LANES.items():
    text = open(f"{FR}/{base}").read()
    est = json.load(open(f"{FR}/{estf}"))
    cases = [(c, est[c]["project"]) for c, _ in ids(csvname)]
    assert set(c for c, _ in cases) == set(est), (base, set(est) ^ set(c for c, _ in cases))
    seen = []
    for name, tag, projects, ports in lanes:
        mine = [c for c, p in cases if p in projects]
        seen += mine
        t = text
        t = re.sub(r"^# ", f"# LANE {name} (projects: {', '.join(sorted(projects))}; generation without rq2/PIT; 2 cells at once) of {base}.\n# ", t, count=1, flags=re.M)
        t = t.replace(f"  work: /<home>/repos/full-run/{work}\n", f"  work: /<home>/repos/full-run/{work}-{tag}\n")
        assert t.count("parallel_cases: 1\n") == 1, name
        t = t.replace("parallel_cases: 1\n", "parallel_cases: 2\n")  # 2 lanes x 2 cells = 4 at once, as in v2
        t = re.sub(r"^  debug_ports: .*$", f"  debug_ports: {ports}", t, flags=re.M)
        t = t.replace("  - rq1\n  - rq2\n  - quality\n", "  - rq1\n  - quality\n")
        lst = "cases:\n" + "".join(f"- {c}\n" for c in mine)
        assert t.count("cases: []\n") == 1, name
        t = t.replace("cases: []\n", lst)
        open(f"{FR}/{name}", "w").write(t)
        summary[name] = dict(cases=len(mine), projects=sorted(projects),
                             hours=round(sum(est[c]["case_s"] for c in mine) / 3600, 1))
    assert sorted(seen) == sorted(c for c, _ in cases), base
print(json.dumps(summary, indent=1))
