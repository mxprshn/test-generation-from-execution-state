#!/usr/bin/env bash
# Generation (verify, rq1, quality; no rq2/PIT) of the 70 study cases in two lanes over disjoint projects.
# Builds the debugger server once, then starts both lanes in the background. Progress:
#   ./status.sh lane-a.yaml   ./status.sh lane-b.yaml   tail -f logs/lane-a.log
set -e
FR=/<home>/repos/full-run; cd $FR
( set -a; . /<root>/secrets/provider.env; set +a
  export JAVA_HOME=/<root>/jdk21 PATH=/<root>/jdk21/bin:/usr/bin:/bin
  cd /<home>/repos/test-generation-from-execution-state && python3 -m runner.debugger --config $FR/lane-a.yaml )
nohup ./run-lane.sh lane-a.yaml lane-a >/dev/null 2>&1 &
sleep 3   # scripts/run.py stamps config-<second>.yaml in the shared results dir
nohup ./run-lane.sh lane-b.yaml lane-b >/dev/null 2>&1 &
echo "started lane-a and lane-b; logs in $FR/logs"
