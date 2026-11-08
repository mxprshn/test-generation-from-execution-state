#!/usr/bin/env bash
# Export the two results dirs of komar-run-1(b) into deterministic archives (twice, determinism check).
set -e
R=/<root>/results; KR=/<root>/komar-run; OUT=$R/releases/komar-run-1; S=/tmp/krun1
REPO=/<root>/test-generation-from-execution-state
rm -rf $S; mkdir -p $OUT $S/x70/run-configs $S/x31/run-configs
cp /tmp/trun1/redactions.json $S/redactions.json
cp $KR/w*-70.yaml $S/x70/run-configs/; cp $KR/w*-31.yaml $S/x31/run-configs/
for x in x70 x31; do cp $KR/RUN-LOG.md $S/$x/RUN-LOG.md; cp $KR/logs/dispatch.log $S/$x/dispatch.log; cp $KR/komar/versions.json $S/$x/komar-versions.json; done
cd $REPO
for n in 1 2; do
  python3 scripts/export_results.py --results $R/komar-v2 --archive $S/k70-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x70/RUN-LOG.md $S/x70/dispatch.log $S/x70/komar-versions.json --extra-dir run-configs=$S/x70/run-configs > $S/k70-$n.json
  python3 scripts/export_results.py --results $R/komar-tools-projects --archive $S/k31-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x31/RUN-LOG.md $S/x31/dispatch.log $S/x31/komar-versions.json --extra-dir run-configs=$S/x31/run-configs > $S/k31-$n.json
done
sha256sum $S/k70-1.tar.gz $S/k70-2.tar.gz $S/k31-1.tar.gz $S/k31-2.tar.gz
cp $S/k70-1.tar.gz $OUT/komar-v2.tar.gz; cp $S/k31-1.tar.gz $OUT/komar-tools-projects.tar.gz
cd $OUT && sha256sum komar-v2.tar.gz komar-tools-projects.tar.gz > SHA256SUMS && cat SHA256SUMS && ls -l
