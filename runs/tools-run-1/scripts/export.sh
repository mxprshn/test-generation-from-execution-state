#!/usr/bin/env bash
# Export the two results dirs of tools-run-1 into deterministic archives (twice, determinism check).
set -e
R=/<home>/repos/results; TR=/<home>/repos/tools-run; OUT=$R/releases/tools-run-1; S=/tmp/trun1
REPO=/<home>/repos/test-generation-from-execution-state
rm -rf $S; mkdir -p $OUT $S/x70/lane-configs $S/x31/lane-configs
python3 - <<'PY'
import json
pairs = json.load(open("/tmp/arun1/redactions.json"))
pairs += [["/<home>/repos/tools-run", "/<root>/tools-run"], ["/<home>/repos/results", "/<root>/results"]]
json.dump(pairs, open("/tmp/trun1/redactions.json", "w"))
moved = {"set_aside": ["jsoup-consume-css-identifier/microtestcarver/r1"],
         "cause": "adapter defect: all three recordings aborted under BTrace and the adapter then read the removed object pool (FileNotFoundError); fixed in a3487b0 (tag tools-run-1b), the cell regenerated on tools-run-1b; the old cell (r1.infra-adapter-1792684535) is not in the archive",
         "moved_at": "2026-10-22T15:55:35Z", "first_commit": "ce918715d0b65f0b201a29adcb0aac47bd4a17df",
         "rerun_commit": "a3487b0", "rerun_config": "rerun-1b.yaml"}
json.dump(moved, open("/tmp/trun1/x70/moved.json", "w"), indent=1)
PY
cp $TR/lane-a.yaml $TR/lane-b.yaml $TR/rerun-1b.yaml $S/x70/lane-configs/
cp $TR/tp-lane-a.yaml $TR/tp-lane-b.yaml $S/x31/lane-configs/
cp $TR/RUN-LOG.md $S/x70/RUN-LOG.md; cp $TR/RUN-LOG.md $S/x31/RUN-LOG.md
cp $TR/external/versions.json $S/x70/tool-versions.json; cp $TR/external/versions.json $S/x31/tool-versions.json
cd $REPO
for n in 1 2; do
  python3 scripts/export_results.py --results $R/tools-v2 --archive $S/t70-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x70/moved.json $S/x70/RUN-LOG.md $S/x70/tool-versions.json --extra-dir lane-configs=$S/x70/lane-configs > $S/t70-$n.json
  python3 scripts/export_results.py --results $R/tools-tools-projects --archive $S/t31-$n.tar.gz --redact-file $S/redactions.json \
    --extra $S/x31/RUN-LOG.md $S/x31/tool-versions.json --extra-dir lane-configs=$S/x31/lane-configs > $S/t31-$n.json
done
sha256sum $S/t70-1.tar.gz $S/t70-2.tar.gz $S/t31-1.tar.gz $S/t31-2.tar.gz
cp $S/t70-1.tar.gz $OUT/tools-v2.tar.gz; cp $S/t31-1.tar.gz $OUT/tools-tools-projects.tar.gz
cd $OUT && sha256sum tools-v2.tar.gz tools-tools-projects.tar.gz > SHA256SUMS && cat SHA256SUMS && ls -la
