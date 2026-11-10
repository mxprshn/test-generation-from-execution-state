#!/usr/bin/env bash
# One lane of a komar-mode run: run-lane.sh <config.yaml> <logname> <commit> [extra args of scripts/run.py]
# Writes logs/<logname>.log and logs/<logname>.done (exit code). Refuses to start unless the worktree
# ~/repos/tg-komar is a clean checkout of <commit>.
set -u
CFG=$(readlink -f "$1"); NAME=$2; WANT=$3; shift 3
KR=/<home>/repos/komar-compact-run; REPO=/<home>/repos/tg-komar-compact
LOG=$KR/logs/$NAME.log; DONE=$KR/logs/$NAME.done
rm -f "$DONE"
{
if grep -q "^jdk: 11" "$CFG"; then
  . /<home>/repos/tools-projects/env.sh
  export PATH=/<home>/ispras-eval/node22/bin:$PATH
else
  set -a; . /<home>/ispras-eval/secrets/<keys>.env; set +a
  export PATH=/<home>/ispras-eval/node22/bin:/<home>/ispras-eval/maven/bin:/<home>/ispras-eval/jdk21/bin:/usr/local/bin:/usr/bin:/bin
  export JAVA_HOME=/<home>/ispras-eval/jdk21 HOME=/<home>/ispras-eval/home TMPDIR=$KR/tmp
  mkdir -p "$TMPDIR"
fi
export TESTGEN_API_KEY="${TESTGEN_API_KEY:-${TESTGEN_API_KEY1:-}}"
export TESTGEN_PORT_LOCKS=/tmp/testgen-debug-ports
cd "$REPO"
have=$(git rev-parse HEAD); want=$(git rev-parse "$WANT^{commit}")
if [ "$want" != "$have" ] || [ -n "$(git status --porcelain)" ]; then
  echo "not at clean $WANT ($have)"; echo 2 > "$DONE"; exit 2
fi
echo "$(date -Is) start $NAME config=$CFG commit=$have"
python3 scripts/run.py --config "$CFG" "$@"
rc=$?
echo "$(date -Is) end $NAME exit $rc"
echo $rc > "$DONE"
} >> "$LOG" 2>&1
