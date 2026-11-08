#!/usr/bin/env bash
# Starts the 4 workers and the checker of komar-run-1b, detached from the calling shell.
KR=/<root>/komar-run; cd $KR
for w in 1 2 3 4; do setsid scripts/worker.sh $w komar-run-1b </dev/null >/dev/null 2>&1 & sleep 5; done
setsid scripts/checker.sh </dev/null >/dev/null 2>&1 &
