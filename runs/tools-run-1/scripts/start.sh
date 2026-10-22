#!/usr/bin/env bash
# start.sh 70|31 : the two lanes of a group in the background (sleep 3: run.py stamps config-<second>.yaml)
set -e
TR=/<home>/repos/tools-run; cd $TR
case "$1" in 70) a=lane-a; b=lane-b ;; 31) a=tp-lane-a; b=tp-lane-b ;; *) echo "usage: start.sh 70|31"; exit 1 ;; esac
nohup ./run-lane.sh $a.yaml $a >/dev/null 2>&1 &
sleep 3
nohup ./run-lane.sh $b.yaml $b >/dev/null 2>&1 &
echo "started $a and $b; logs in $TR/logs"
