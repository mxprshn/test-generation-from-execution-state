#!/usr/bin/env bash
# worker.sh <N> [tag]: runs cases of queue.txt ("<group> <project> <case>" per line), both rounds of a case
# with run-lane.sh and w<N>-<group>.yaml, until the queue is empty or N exceeds the number in `target`.
# Dispatch rule: the next case taken is the first in the queue whose project no other worker is running
# (running/w<M> holds the project of worker M). When every queued case is of a running project, the worker
# waits, unless only one project is left in the queue. A case whose run.py exits 2 or 3 (model probe
# failed, provider refused, worktree not at the tag) goes back to the queue and the worker stops.
set -u
N=$1; TAG=${2:-komar-run-1b}; KR=/<root>/komar-run
RUN=$KR/running; mkdir -p $RUN; rm -f $RUN/w$N
log() { echo "$(date -Is) w$N $*" >> $KR/logs/dispatch.log; }
take() {  # prints "<group> <project> <case>" and marks the project as running, or prints WAIT / nothing
  flock $KR/queue.lock python3 - "$KR" "$N" <<'EOF'
import os, sys
kr, n = sys.argv[1], sys.argv[2]
q = [l.split() for l in open(f"{kr}/queue.txt") if l.strip()]
if not q:
    sys.exit()
running = {open(f"{kr}/running/{f}").read().strip() for f in os.listdir(f"{kr}/running") if f != f"w{n}"}
pick = next((i for i, (_, p, _) in enumerate(q) if p not in running), None)
if pick is None and len({p for _, p, _ in q}) == 1:
    pick = 0
if pick is None:
    print("WAIT")
    sys.exit()
g, p, c = q.pop(pick)
open(f"{kr}/queue.txt", "w").write("".join(" ".join(x) + "\n" for x in q))
open(f"{kr}/running/w{n}", "w").write(p)
print(g, p, c, "|", ",".join(sorted(running | {p})))
EOF
}
while :; do
  [ "$N" -le "$(cat $KR/target)" ] || { log "stop: target $(cat $KR/target)"; exit 0; }
  item=$(take)
  [ -n "$item" ] || { log "stop: queue empty"; exit 0; }
  [ "$item" = WAIT ] && { sleep 20; continue; }
  set -- $item; G=$1; P=$2; C=$3; NOW=$5
  log "start $G $P $C concurrent-projects=$NOW"
  $KR/scripts/run-lane.sh $KR/w$N-$G.yaml w$N $TAG --cases $C
  rc=$(cat $KR/logs/w$N.done 2>/dev/null || echo 9)
  rm -f $RUN/w$N
  log "end $G $P $C rc=$rc"
  if [ "$rc" = 2 ] || [ "$rc" = 3 ]; then
    flock $KR/queue.lock sh -c "sed -i \"1i $G $P $C\" $KR/queue.txt"
    log "requeued $G $P $C, worker stops (rc=$rc)"; exit 1
  fi
done
