#!/usr/bin/env bash
# Export the two results dirs of tools-run-1c into deterministic archives (twice, determinism check).
set -e
R=/<root>/results; TR=/<root>/tools-run-1c; OUT=$R/releases/tools-run-1c; S=/tmp/trun1c
REPO=/<root>/test-generation-from-execution-state
rm -rf $S; mkdir -p $OUT $S/x70/lane-configs $S/x31/lane-configs
python3 - <<'PY'
import json
pairs = json.load(open("/tmp/trun1/redactions.json"))
pairs += [["/<root>/tools-run-1c", "/<root>/tools-run-1c"]]
json.dump(pairs, open("/tmp/trun1c/redactions.json", "w"))
PY
cp $TR/lane-a.yaml $TR/lane-b.yaml $S/x70/lane-configs/
cp $TR/tp-lane-a.yaml $TR/tp-lane-b.yaml $S/x31/lane-configs/
cp $TR/RUN-LOG.md $S/x70/RUN-LOG.md; cp $TR/RUN-LOG.md $S/x31/RUN-LOG.md
cp /<root>/tools-run/external/versions.json $S/x70/tool-versions.json; cp $S/x70/tool-versions.json $S/x31/tool-versions.json
cd $REPO
for n in 1 2; do
  python3 scripts/export_results.py --results $R/tools-mtc-1c-v2 --archive $S/t70-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x70/RUN-LOG.md $S/x70/tool-versions.json --extra-dir lane-configs=$S/x70/lane-configs > $S/t70-$n.json
  python3 scripts/export_results.py --results $R/tools-mtc-1c-tools-projects --archive $S/t31-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x31/RUN-LOG.md $S/x31/tool-versions.json --extra-dir lane-configs=$S/x31/lane-configs > $S/t31-$n.json
done
sha256sum $S/t70-1.tar.gz $S/t70-2.tar.gz $S/t31-1.tar.gz $S/t31-2.tar.gz
cp $S/t70-1.tar.gz $OUT/tools-mtc-1c-v2.tar.gz; cp $S/t31-1.tar.gz $OUT/tools-mtc-1c-tools-projects.tar.gz
cd $OUT && sha256sum tools-mtc-1c-v2.tar.gz tools-mtc-1c-tools-projects.tar.gz > SHA256SUMS && cat SHA256SUMS && ls -la
