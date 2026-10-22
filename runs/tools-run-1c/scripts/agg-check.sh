#!/usr/bin/env bash
# Aggregate check: v2-release + autonomous-run-1 + tools-run-1 (pankti, rick) + tools-run-1c (microtestcarver).
# The microtestcarver cells of the tools-run-1 archives are superseded by tools-run-1c and removed after unpacking.
set -e
S=/tmp/trun1c; REPO=/<root>/test-generation-from-execution-state; REL=/<root>/results/releases
rm -rf $S/u $S/combined $S/combined31; mkdir -p $S/u/v2 $S/u/a70 $S/u/a31 $S/u/t70 $S/u/t31 $S/u/m70 $S/u/m31
tar -xzf /<root>/results/v2-release/results.tar.gz -C $S/u/v2
tar -xzf $REL/autonomous-run-1/autonomous-v2.tar.gz -C $S/u/a70
tar -xzf $REL/autonomous-run-1/autonomous-tools-projects.tar.gz -C $S/u/a31
tar -xzf $REL/tools-run-1/tools-v2.tar.gz -C $S/u/t70
tar -xzf $REL/tools-run-1/tools-tools-projects.tar.gz -C $S/u/t31
tar -xzf $REL/tools-run-1c/tools-mtc-1c-v2.tar.gz -C $S/u/m70
tar -xzf $REL/tools-run-1c/tools-mtc-1c-tools-projects.tar.gz -C $S/u/m31
rm -rf $S/u/t70/cells/*/microtestcarver $S/u/t31/cells/*/microtestcarver
cd $REPO
python3 scripts/combine_results.py $S/combined $S/u/v2 $S/u/a70 $S/u/t70 $S/u/m70
python3 scripts/combine_results.py $S/combined31 $S/u/a31 $S/u/t31 $S/u/m31
python3 - <<'PY'
import yaml
S="/tmp/trun1c"
c=yaml.safe_load(open("/<home>/repos/full-run/config.yaml"))
c["paths"]["results"]=S+"/combined"; c["paths"]["work"]=S+"/work"
c["modes"]=["baseline","dump","rendered","autonomous"]; c["once_modes"]=["rendered-raw","pankti","rick","microtestcarver"]
yaml.safe_dump(c,open(S+"/combined.yaml","w"),sort_keys=False)
t=yaml.safe_load(open("/<home>/repos/full-run/tp-lane-a.yaml"))
t["paths"]["results"]=S+"/combined31"; t["paths"]["work"]=S+"/work"; t["cases"]=[]
t["modes"]=["autonomous"]; t["once_modes"]=["pankti","rick","microtestcarver"]
yaml.safe_dump(t,open(S+"/combined31.yaml","w"),sort_keys=False)
PY
set +e
export JAVA_HOME=/<root>/ispras-eval/jdk21
python3 -m evaluation.aggregate --config $S/combined.yaml > $S/agg70.out 2>&1; echo "agg70 rc=$?"
export JAVA_HOME=/<root>/newcases/jdks/jdk-11.0.32.1+1
python3 -m evaluation.aggregate --config $S/combined31.yaml > $S/agg31.out 2>&1; echo "agg31 rc=$?"
