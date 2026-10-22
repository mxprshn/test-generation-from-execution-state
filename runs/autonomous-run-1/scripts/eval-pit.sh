#!/usr/bin/env bash
# Later pass: only the rq2 step (PIT) on the existing autonomous cells; no generation, no tracing.
#   ./eval-pit.sh lane-a.yaml lane-b.yaml          (70 study cases, JDK 21)
#   ./eval-pit.sh tp-lane-a.yaml tp-lane-b.yaml    (31 tools-projects cases, JDK 11)
# Uses python3 -m evaluation.evaluate --steps rq2: evaluate_cell runs only the listed step and skips
# steps already stored; rq2 depends on verify (stored), not on rq1 traces. Lanes of one group run in
# parallel here too (disjoint cases). Logs: logs/pit-<lane>.log, .done with the exit code.
set -u
FR=/<home>/repos/full-run; REPO=/<home>/repos/test-generation-from-execution-state
for c in "$@"; do
  name=pit-$(basename "$c" .yaml); cfg=$(readlink -f "$FR/$c")
  (
    if grep -q "^jdk: 11" "$cfg"; then . /<home>/repos/tools-projects/env.sh
    else export PATH=/<root>/maven/bin:/<root>/jdk21/bin:/usr/local/bin:/usr/bin:/bin
         export JAVA_HOME=/<root>/jdk21 HOME=/<root>/home TMPDIR=$FR/tmp; fi
    cd "$REPO"
    want=$(git rev-parse autonomous-run-1^{commit}); have=$(git rev-parse HEAD)
    [ "$want" = "$have" ] && [ -z "$(git status --porcelain)" ] || { echo "not at clean autonomous-run-1"; exit 2; }
    python3 -m evaluation.evaluate --config "$cfg" --modes autonomous --steps rq2 > "$FR/logs/$name.log" 2>&1
    echo $? > "$FR/logs/$name.done"
  ) &
  sleep 3
done
wait
