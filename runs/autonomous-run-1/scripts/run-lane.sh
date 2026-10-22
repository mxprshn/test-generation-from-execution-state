#!/usr/bin/env bash
# One lane of the full run: run-lane.sh <config.yaml> <logname> [extra args of scripts/run.py]
# Writes logs/<logname>.log and logs/<logname>.done (exit code). Refuses to start unless the repository
# is at a clean checkout of the tag autonomous-run-1. nohup-friendly (start-70.sh / start-tp.sh).
set -u
CFG=$(readlink -f "$1"); NAME=$2; shift 2
FR=/<home>/repos/full-run; REPO=/<home>/repos/test-generation-from-execution-state
LOG=$FR/logs/$NAME.log; DONE=$FR/logs/$NAME.done
rm -f "$DONE"
{
if grep -q "^jdk: 11" "$CFG"; then            # tools-projects group: JDK 11 environment
  . /<home>/repos/tools-projects/env.sh
  export PATH=/<root>/node22/bin:$PATH
else                                           # the 70 cases of the study: JDK 21 environment (as run.sh)
  set -a; . /<root>/secrets/provider.env; set +a
  export PATH=/<root>/node22/bin:/<root>/maven/bin:/<root>/jdk21/bin:/usr/local/bin:/usr/bin:/bin
  export JAVA_HOME=/<root>/jdk21 HOME=/<root>/home TMPDIR=$FR/tmp
  mkdir -p "$TMPDIR"
fi
export TESTGEN_API_KEY="${TESTGEN_API_KEY:-${TESTGEN_API_KEY:-}}"
export TESTGEN_PORT_LOCKS=/<tmp>/testgen-debug-ports
cd "$REPO"
want=$(git rev-parse autonomous-run-1^{commit}); have=$(git rev-parse HEAD)
if [ "$want" != "$have" ] || [ -n "$(git status --porcelain)" ]; then
  echo "not at clean autonomous-run-1 ($have)"; echo 2 > "$DONE"; exit 2
fi
echo "$(date -Is) start $NAME config=$CFG commit=$have"
python3 scripts/run.py --config "$CFG" "$@"
rc=$?
echo "$(date -Is) end $NAME exit $rc"
echo $rc > "$DONE"
} >> "$LOG" 2>&1
