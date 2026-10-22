#!/usr/bin/env bash
# Aggregate check: v2-release + autonomous-run-1 + tools-run-1 archives combined, evaluation.aggregate.
set -e
S=/tmp/trun1; A=/tmp/arun1; REPO=/<home>/repos/test-generation-from-execution-state; OUT=/<home>/repos/results/releases/tools-run-1
rm -rf $S/u $S/combined $S/combined31; mkdir -p $S/u/v2 $S/u/a70 $S/u/a31 $S/u/t70 $S/u/t31
tar -xzf /<home>/repos/results/v2-release/results.tar.gz -C $S/u/v2
tar -xzf /<home>/repos/results/releases/autonomous-run-1/autonomous-v2.tar.gz -C $S/u/a70
tar -xzf /<home>/repos/results/releases/autonomous-run-1/autonomous-tools-projects.tar.gz -C $S/u/a31
tar -xzf $OUT/tools-v2.tar.gz -C $S/u/t70
tar -xzf $OUT/tools-tools-projects.tar.gz -C $S/u/t31
cd $REPO
python3 scripts/combine_results.py $S/combined $S/u/v2 $S/u/a70 $S/u/t70
python3 scripts/combine_results.py $S/combined31 $S/u/a31 $S/u/t31
python3 - <<'PY'
import yaml
S="/tmp/trun1"
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
export JAVA_HOME=/<root>/jdk21
python3 -m evaluation.aggregate --config $S/combined.yaml > $S/agg70.out 2>&1; echo "agg70 rc=$?"
export JAVA_HOME=/<home>/newcases/jdks/jdk-11.0.32.1+1
python3 -m evaluation.aggregate --config $S/combined31.yaml > $S/agg31.out 2>&1; echo "agg31 rc=$?"
