#!/usr/bin/env bash
# Stops komar-run-1: workers, lanes, run.py, then every process of the cells (agents, komar, Maven/surefire JVMs).
KR=/<root>/komar-run
echo 0 > $KR/target
self=$$
match() {  # pids whose command line matches $1 (extended regex), excluding this script
  ps -eo pid=,args= | awk -v s="$self" -v re="$1" '$1!=s && $0 ~ re && $0 !~ /komar-stop\.sh/ {print $1}'
}
kill_all() { local p; p=$(match "$1"); [ -n "$p" ] && kill $2 $p 2>/dev/null; }
kill_all 'komar-run/scripts/(checker|worker)\.sh'
kill_all 'komar-run/scripts/run-lane\.sh'
kill_all 'scripts/run\.py --config /<root>/komar-run/'
sleep 3
CELLS='komar-run/(work|tmp)|komar-run/komar/bin/komar|runner\.komar'
for i in 1 2 3; do kill_all "$CELLS" ""; sleep 5; done
kill_all "$CELLS" -9
kill_all 'scripts/run\.py --config /<root>/komar-run/' -9
sleep 2
echo "left: $(match "$CELLS|komar-run/scripts/(worker|run-lane)|run\.py --config /<root>/komar-run/" | wc -l)"
echo "java: $(pgrep -c java)"
uptime
