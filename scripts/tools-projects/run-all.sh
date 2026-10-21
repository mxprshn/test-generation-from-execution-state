#!/bin/bash
# run-all.sh LANE_PORT FILTER  -- debugger check of every candidate (sequential); skips ids with results
port=$1; filter=${2:-.}
cd ~/newcases
grep -v '^#' tools/candidates.tsv | grep -E "$filter" | while IFS=$'\t' read id proj mod cls meth sig test ev; do
  [ -s ~/newcases/results/$id.json ] && continue
  dir=~/newcases/src/$proj/$mod
  case $proj in
    pdfbox-*) extra=(-Drat.skip=true -Danimal.sniffer.skip=true) ;;
    petclinic) extra=(-Dcheckstyle.skip -Dspring-javaformat.skip=true) ;;
    lab-*) extra=("-DargLine=-Dmicronaut.http.client.read-timeout=300s -Dconsul.client.registration.enabled=false -Dconsul.client.enabled=false -Dtracing.zipkin.enabled=false") ;;
    *) extra=() ;;
  esac
  echo "== $id"
  tools/probe.sh "$id" "$port" "$cls" "$meth" "$sig" env11.sh "$dir" -- "${extra[@]}" -DforkCount=1 -Dtest="$test" \
    -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false surefire:test < /dev/null
done
echo ALLDONE
