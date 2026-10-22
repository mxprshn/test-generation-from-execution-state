"""Lane configs of the tools run (modes pankti, rick, microtestcarver; round 1; no rq2) from base-70.yaml and
base-31.yaml: disjoint projects, 2 cells at once per lane (4 at once, as in the agent runs)."""
import csv, json, os, yaml
TR = os.path.expanduser("~/repos/tools-run")
REPO = os.path.expanduser("~/repos/test-generation-from-execution-state")
LANES = {
    "base-70.yaml": ("cases.csv", "work", [
        ("lane-a.yaml", "a", {"mybatis", "checkstyle", "jsoup"}),
        ("lane-b.yaml", "b", {"apollo", "jgit", "klaw", "liquibase"})]),
    "base-31.yaml": ("tools-projects.csv", "work-tp", [
        ("tp-lane-a.yaml", "a", {"pdfbox"}),
        ("tp-lane-b.yaml", "b", {"graphhopper", "spring-petclinic", "lab-insurance"})]),
}
import sys
sys.path.insert(0, REPO)
from runner.tools.common import ADD_OPENS
out = {}
for base, (csvname, work, lanes) in LANES.items():
    rows = list(csv.DictReader(open(f"{REPO}/cases/{csvname}")))
    seen = []
    for name, tag, projects in lanes:
        c = yaml.safe_load(open(f"{TR}/{base}"))
        mine = [r["id"] for r in rows if r["project"] in projects]
        seen += mine
        c["cases"] = mine
        c["parallel_cases"] = 2
        c["paths"]["work"] = f"{TR}/{work}-{tag}"
        c["env"]["JDK_JAVA_OPTIONS"] = ADD_OPENS
        header = (f"# LANE {name} of {base} (projects: {', '.join(sorted(projects))}): the external tools pankti, rick,\n"
                  "# microtestcarver (docs/tools.md), round 1, evaluation verify/rq1/quality (no rq2), 2 cells at once.\n")
        open(f"{TR}/{name}", "w").write(header + yaml.safe_dump(c, sort_keys=False, width=120))
        out[name] = {"cases": len(mine), "projects": sorted(projects)}
    assert sorted(seen) == sorted(r["id"] for r in rows), base
print(json.dumps(out, indent=1))
