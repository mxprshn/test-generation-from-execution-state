#!/bin/bash
# probe.sh ID PORT CLASS METHOD SIG(-) ENV(env11.sh|env17.sh) DIR -- mvn args...
# Runs the scenario test under JDWP (debuggee connects to Probe) and writes results/ID.json.
id=$1; port=$2; cls=$3; meth=$4; sig=$5; envf=$6; dir=$7; shift 8
. ~/newcases/$envf
R=~/newcases/results; mkdir -p $R
cd ~/newcases/tools && [ Probe.class -nt Probe.java ] || $HOME/ispras-eval/jdk21/bin/javac Probe.java
$HOME/ispras-eval/jdk21/bin/java -cp ~/newcases/tools Probe $port "$cls" "$meth" "$sig" $R/$id.json > $R/$id.probe.log 2>&1 &
pp=$!
sleep 1
cd "$dir"
t0=$(date +%s)
mvn -B -o -Dmaven.repo.local=$M2 "$@" \
  -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=n,address=127.0.0.1:$port,suspend=y" \
  > $R/$id.mvn.log 2>&1
rc=$?
t1=$(date +%s)
wait $pp
echo "{\"id\":\"$id\",\"mvn_rc\":$rc,\"wall_s\":$((t1-t0)),\"tests\":\"$(grep -E 'Tests run:' $R/$id.mvn.log | tail -1 | sed 's/\[INFO\] //;s/"//g')\"}" > $R/$id.run.json
cat $R/$id.run.json; cat $R/$id.json 2>/dev/null; echo
