#!/bin/bash
# scan.sh NAME PORT TARGETS ENV DIR -- mvn args...   -> ~/newcases/scan/NAME.tsv
name=$1; port=$2; targets=$3; envf=$4; dir=$5; shift 6
. ~/newcases/$envf
S=~/newcases/scan; mkdir -p $S
$HOME/ispras-eval/jdk21/bin/java -cp ~/newcases/tools Scan $port $S/$name.tsv "$targets" > $S/$name.scan.log 2>&1 &
pp=$!
sleep 1
cd "$dir"
t0=$(date +%s)
mvn -B -Dmaven.repo.local=$M2 "$@" \
  -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=n,address=127.0.0.1:$port,suspend=y" \
  > $S/$name.mvn.log 2>&1
echo "rc=$? wall=$(( $(date +%s)-t0 ))" > $S/$name.done
wait $pp
