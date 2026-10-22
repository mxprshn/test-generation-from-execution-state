#!/bin/bash
export PATH=/<root>/maven/bin:/<root>/jdk21/bin:/usr/local/bin:/usr/bin:/bin
export JAVA_HOME=/<root>/jdk21
cd /<home>/repos/tg-tools-dev
for c in base-70 base-31; do echo "=== $c"; scripts/setup_external_tools.sh /<home>/repos/tools-run/$c.yaml || exit $?; done
