"""mkcases.py: candidates.tsv + results/*.json + meta -> cases/<id>.yaml (our case.yaml format + extra fields)."""
import json, os, re, sys

HOME = os.path.expanduser("~/newcases")
PROJECTS = {
    "pdfbox-2.0.24": dict(name="pdfbox", url="https://github.com/apache/pdfbox.git", commit=None, jdk=11,
                          ref="2.0.24 (RICK Table 1: SHA 8876e8e; Pankti used 2.0.21)",
                          extra=["-Drat.skip=true", "-Danimal.sniffer.skip=true"], mvn="mvn"),
    "graphhopper-5.3": dict(name="graphhopper", url="https://github.com/graphhopper/graphhopper.git", commit=None, jdk=11,
                            ref="5.3 (RICK Table 1: SHA af5ac0b)", extra=[], mvn="mvn"),
    "petclinic": dict(name="spring-petclinic", url="https://github.com/spring-projects/spring-petclinic.git", commit=None, jdk=11,
                      ref="2.7.3 (MicroTestCarver Table I)",
                      extra=["-Dcheckstyle.skip", "-Dspring-javaformat.skip=true"], mvn="./mvnw"),
    "lab-insurance-79416dd": dict(name="lab-insurance", url="https://github.com/asc-lab/micronaut-microservices-poc.git",
                                  commit=None, jdk=11,
                                  ref="last commit on Java 8 / Micronaut 2.1 (MicroTestCarver Table I: Lab-Insurance 1.0.0)",
                                  extra=[], mvn="mvn"),
}


def git_commit(d):
    return os.popen(f"git -C {HOME}/src/{d} rev-parse HEAD").read().strip()


def tools_expected(cls_src, method, sig, d):
    """Pankti: public non-static instance method; RICK: plus a call on a field of a non-final project type
    (mockable); MTC: project runs on JDK 11 (all chosen projects do)."""
    return None


def main():
    rows = [l.rstrip("\n").split("\t") for l in open(f"{HOME}/tools/candidates.tsv") if l.strip() and not l.startswith("#")]
    meta = json.load(open(f"{HOME}/tools/meta.json"))
    os.makedirs(f"{HOME}/cases", exist_ok=True)
    summary = []
    for cid, proj, mod, cls, meth, sig, test, ev in rows:
        res_p, run_p = f"{HOME}/results/{cid}.json", f"{HOME}/results/{cid}.run.json"
        if not (os.path.exists(res_p) and os.path.exists(run_p)):
            continue
        res, run = json.load(open(res_p)), json.load(open(run_p))
        ok = (run["mvn_rc"] == 0 and res["hits_entry"] > 0 and res["hits_exit"] + res.get("exception_exits", 0) >= res["hits_entry"]
              and res["args_ok"] and res["this_ok"] and res["return_ok"] and not res["error"])
        m = meta.get(cid, {})
        P = PROJECTS[proj]
        moddir = "" if mod == "." else mod + "/"
        tcls, tmeth = test.split("#")
        src = moddir + "src/test/java/" + tcls.replace(".", "/") + ".java"
        simple = cls.rsplit(".", 1)[1]
        out_test = moddir + "src/test/java/" + cls.rsplit(".", 1)[0].replace(".", "/") + f"/{simple}CarvedTest.java"
        pl = [] if mod in (".", "") else ["-pl", mod]
        base = [P["mvn"], "-q", "-o"] + pl + P["extra"]
        surefire = ["-Dsurefire.failIfNoTests=false", "-DfailIfNoTests=false", "-Dsurefire.failIfNoSpecifiedTests=false", "test"]
        y = {
            "id": cid,
            "project": P["name"],
            "group": m.get("group", "indirect"),
            "target": {"class": cls, "method": meth, "signature": m["signature"]},
            "scenario": {"test": test, "source": src},
            "output_test_file": out_test,
            "hidden_paths": m.get("hidden_paths", []),
            "commands": {
                "build": base + ["-DskipTests", "test-compile"],
                "scenario": base + [f"-Dtest={tcls.rsplit('.', 1)[1]}#{tmeth}"] + surefire,
                "generated_test": base + [f"-Dtest={simple}CarvedTest"] + surefire,
            },
            "repo": {"url": P["url"], "commit": git_commit(proj), "version": P["ref"], "jdk": P["jdk"],
                     "prebuild": m.get("prebuild")},
            "jdk": P["jdk"],
            "tools_expected": m.get("tools_expected", {}),
            "evidence": ev,
            "debugger_check": {
                "ok": ok,
                "hits_entry": res["hits_entry"], "hits_exit": res["hits_exit"],
                "exception_exits": res.get("exception_exits", 0),
                "args_ok": res["args_ok"], "this_ok": res["this_ok"], "return_ok": res["return_ok"],
                "wall_s": run["wall_s"], "jvm_attach_s": res["wait_attach_s"], "debug_s": res["debug_s"],
                "jdk_used": "Temurin 11.0.32", "sample": res["samples"][:2],
            },
        }
        with open(f"{HOME}/cases/{cid}.yaml", "w") as f:
            f.write(to_yaml(y))
        summary.append((cid, ok, res["hits_entry"], run["wall_s"]))
    for s in summary:
        print(*s, sep="\t")


def q(s):
    s = str(s)
    if s == "" or re.search(r"[:#\[\]{},&*!|>'\"%@`]|^[-?]|\s$|^\s", s) or s in ("true", "false", "null", "yes", "no"):
        return json.dumps(s, ensure_ascii=False)
    return s


def to_yaml(v, ind=0):
    pad = "  " * ind
    out = []
    if isinstance(v, dict):
        for k, x in v.items():
            if isinstance(x, dict):
                out.append(f"{pad}{k}:")
                out.append(to_yaml(x, ind + 1))
            elif isinstance(x, list):
                if all(not isinstance(e, (dict, list)) for e in x) and k in ("build", "scenario", "generated_test"):
                    out.append(f"{pad}{k}: [{', '.join(q(e) for e in x)}]")
                elif not x:
                    out.append(f"{pad}{k}: []")
                else:
                    out.append(f"{pad}{k}:")
                    for e in x:
                        out.append(f"{pad}- {q(e)}")
            elif isinstance(x, bool):
                out.append(f"{pad}{k}: {'true' if x else 'false'}")
            elif x is None:
                out.append(f"{pad}{k}: null")
            elif isinstance(x, (int, float)):
                out.append(f"{pad}{k}: {x}")
            else:
                out.append(f"{pad}{k}: {q(x)}")
    return "\n".join(out) + ("\n" if ind == 0 else "")


if __name__ == "__main__":
    main()
