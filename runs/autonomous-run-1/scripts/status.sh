#!/usr/bin/env bash
# status.sh <lane config>: progress of a lane (reads status/ of the results dir only).
CFG=$(readlink -f "$1")
if grep -q "^jdk: 11" "$CFG"; then export JAVA_HOME=/<home>/newcases/jdks/jdk-11.0.32.1+1; else export JAVA_HOME=/<root>/jdk21; fi
cd /<home>/repos/test-generation-from-execution-state && python3 scripts/run.py --config "$CFG" --status
