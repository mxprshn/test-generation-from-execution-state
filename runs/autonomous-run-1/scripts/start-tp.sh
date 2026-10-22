#!/usr/bin/env bash
# Generation (verify, rq1, quality; no rq2/PIT) of the 31 tools-projects cases (JDK 11) in two lanes.
set -e
FR=/<home>/repos/full-run; cd $FR
( . /<home>/repos/tools-projects/env.sh
  cd /<home>/repos/test-generation-from-execution-state && python3 -m runner.debugger --config $FR/tp-lane-a.yaml )
nohup ./run-lane.sh tp-lane-a.yaml tp-lane-a >/dev/null 2>&1 &
sleep 3
nohup ./run-lane.sh tp-lane-b.yaml tp-lane-b >/dev/null 2>&1 &
echo "started tp-lane-a and tp-lane-b; logs in $FR/logs"
