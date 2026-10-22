#!/usr/bin/env bash
# One lane of the tools run: run-lane.sh <config.yaml> <logname> [extra args of scripts/run.py]
# Writes logs/<logname>.log and logs/<logname>.done (exit code). Refuses to start unless the repository
# is at a clean checkout of the tag tools-run-1 (or $TAG). No model, no API keys.
set -u
CFG=$(readlink -f "$1"); NAME=$2; shift 2
TR=/<home>/repos/tools-run; REPO=/<home>/repos/test-generation-from-execution-state
TAG=${TAG:-tools-run-1b}
LOG=$TR/logs/$NAME.log; DONE=$TR/logs/$NAME.done
mkdir -p $TR/logs $TR/tmp; rm -f "$DONE"
{
if grep -q "^jdk: 11" "$CFG"; then
  export JAVA_HOME=/<home>/newcases/jdks/jdk-11.0.32.1+1 HOME=/<home>/repos/tools-projects/home
else
  export JAVA_HOME=/<root>/jdk21 HOME=/<root>/home
fi
export PATH=$JAVA_HOME/bin:/<root>/maven/bin:/usr/local/bin:/usr/bin:/bin
export TMPDIR=$TR/tmp TESTGEN_PORT_LOCKS=/<tmp>/testgen-debug-ports
cd "$REPO"
want=$(git rev-parse "$TAG^{commit}"); have=$(git rev-parse HEAD)
if [ "$want" != "$have" ] || [ -n "$(git status --porcelain)" ]; then
  echo "not at clean $TAG ($have)"; echo 2 > "$DONE"; exit 2
fi
echo "$(date -Is) start $NAME config=$CFG commit=$have tag=$TAG"
python3 scripts/run.py --config "$CFG" "$@"
rc=$?
echo "$(date -Is) end $NAME exit $rc"
echo $rc > "$DONE"
} >> "$LOG" 2>&1
