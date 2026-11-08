#!/usr/bin/env bash
# Appends a check line to RUN-LOG.md every 10 minutes while any worker runs.
KR=/<root>/komar-run
while pgrep -f "scripts/worker.sh [0-9]" >/dev/null; do sleep 600; python3 $KR/scripts/check.py >> $KR/RUN-LOG.md; done
echo "- $(date -Is) all workers ended" >> $KR/RUN-LOG.md
