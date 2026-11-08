#!/usr/bin/env bash
# Aggregate komar-run-1b with v2-release and autonomous-run-1 (results dirs), then the paired comparison.
set -e
R=/<root>/results; S=/<root>/komar-run/tmp/agg; cd /<root>/test-generation-from-execution-state
rm -rf $S; mkdir -p $S
python3 scripts/combine_results.py $S/c70 $R/komar-v2 $R/v2-release $R/autonomous-v2
python3 scripts/combine_results.py $S/c31 $R/komar-tools-projects $R/autonomous-tools-projects
python3 - <<PY
import yaml
S="$S"
c=yaml.safe_load(open("/<root>/komar-run/w1-70.yaml")); c["paths"]["results"]=S+"/c70"; c["paths"]["work"]=S+"/work"
c["modes"]=["baseline","dump","rendered","autonomous","komar"]; c["once_modes"]=["rendered-raw"]; c["rounds"]=[1,2]
yaml.safe_dump(c,open(S+"/c70.yaml","w"),sort_keys=False)
t=yaml.safe_load(open("/<root>/komar-run/w1-31.yaml")); t["paths"]["results"]=S+"/c31"; t["paths"]["work"]=S+"/work"
t["modes"]=["autonomous","komar"]; t["once_modes"]=[]; t["rounds"]=[1,2]
yaml.safe_dump(t,open(S+"/c31.yaml","w"),sort_keys=False)
PY
JAVA_HOME=/<root>/jdk21 python3 -m evaluation.aggregate --config $S/c70.yaml --out $S/out70
JAVA_HOME=/<home>/newcases/jdks/jdk-11.0.32.1+1 python3 -m evaluation.aggregate --config $S/c31.yaml --out $S/out31
JAVA_HOME=/<root>/jdk21 python3 /<root>/komar-run/scripts/paired.py $S/c70.yaml $S/c31.yaml
