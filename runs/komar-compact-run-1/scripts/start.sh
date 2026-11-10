#!/usr/bin/env bash
# Starts the 4 workers of komar-compact-run-1, detached from the calling shell.
KR=/<home>/repos/komar-compact-run; cd $KR
for w in 1 2 3 4; do setsid scripts/worker.sh $w komar-compact-run-1 </dev/null >/dev/null 2>&1 & sleep 5; done
