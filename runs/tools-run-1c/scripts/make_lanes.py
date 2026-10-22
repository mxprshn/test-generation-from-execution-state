"""Lane configs of tools-run-1c: MicroTestCarver only, with the full classpath of the target module for its
generator (tag tools-run-1c); round 1; no rq2. Derived from the base configs of tools-run-1 with the same
lanes (disjoint projects, 2 cells at once per lane); only the mode, the results and the work directories differ."""
import csv, json, os, yaml
TR0 = os.path.expanduser("~/repos/tools-run")
TR = os.path.expanduser("~/repos/tools-run-1c")
RES = os.path.expanduser("~/repos/results")
REPO = os.path.expanduser("~/repos/test-generation-from-execution-state")
LANES = {
    "base-70.yaml": ("cases.csv", "work", f"{RES}/tools-mtc-1c-v2", [
        ("lane-a.yaml", "a", {"mybatis", "checkstyle", "jsoup"}),
        ("lane-b.yaml", "b", {"apollo", "jgit", "klaw", "liquibase"})]),
    "base-31.yaml": ("tools-projects.csv", "work-tp", f"{RES}/tools-mtc-1c-tools-projects", [
        ("tp-lane-a.yaml", "a", {"pdfbox"}),
        ("tp-lane-b.yaml", "b", {"graphhopper", "spring-petclinic", "lab-insurance"})]),
}
import sys
sys.path.insert(0, REPO)
from runner.tools.common import ADD_OPENS
out = {}
for base, (csvname, work, results, lanes) in LANES.items():
    rows = list(csv.DictReader(open(f"{REPO}/cases/{csvname}")))
    seen = []
    for name, tag, projects in lanes:
        c = yaml.safe_load(open(f"{TR0}/{base}"))
        mine = [r["id"] for r in rows if r["project"] in projects]
        seen += mine
        c["once_modes"] = ["microtestcarver"]
        c["cases"] = mine
        c["parallel_cases"] = 2
        c["paths"]["results"] = results
        c["paths"]["work"] = f"{TR}/{work}-{tag}"
        c["env"]["JDK_JAVA_OPTIONS"] = ADD_OPENS
        header = (f"# LANE {name} of tools-run-1c (projects: {', '.join(sorted(projects))}): microtestcarver with the\n"
                  "# full classpath of the target module (docs/tools.md), round 1, verify/rq1/quality (no rq2), 2 cells at once.\n")
        open(f"{TR}/{name}", "w").write(header + yaml.safe_dump(c, sort_keys=False, width=120))
        out[name] = {"cases": len(mine), "projects": sorted(projects), "results": results}
    assert sorted(seen) == sorted(r["id"] for r in rows), base
print(json.dumps(out, indent=1))
