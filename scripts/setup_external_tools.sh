#!/usr/bin/env bash
# Install the external test generators compared with the agent modes (modes pankti, rick, microtestcarver;
# docs/tools.md) into <dir>: pinned upstream commits, the patches of external/, the JDKs and build tools
# they need, and the test dependencies of their generated tests in the config's Maven repository.
#
#   scripts/setup_external_tools.sh <config.yaml> [<dir>]     (<dir>: default <paths.tools>/external)
#
# A config of these modes sets paths.external_tools to <dir>. The default setup (scripts/setup.sh) does
# not run this script and is not changed by it. Linux x86_64 only (the JDK downloads are pinned for it);
# needs git, curl, patch, mvn and python3 with venv.
# Downloads are checksum-verified; rerunning skips what is already installed.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG="${1:?usage: scripts/setup_external_tools.sh <config.yaml> [<dir>]}"
cd "$ROOT"

eval "$(python3 - "$CONFIG" <<'EOF'
import shlex, sys
from runner.config import load_config
c = load_config(sys.argv[1])
for k, v in {"TOOLS": c.tools_dir, "M2_REPO": c.maven_repository, "CFG_EXT": c.external_tools_dir}.items():
    print(f"{k}={shlex.quote(str(v))}")
EOF
)"
EXT="${2:-$CFG_EXT}"
mkdir -p "$EXT"
EXT="$(cd "$EXT" && pwd)"

[ "$(uname -s)-$(uname -m)" = "Linux-x86_64" ] || { echo "external tools: Linux x86_64 only"; exit 1; }

PANKTI_URL=https://github.com/ASSERT-KTH/pankti.git
PANKTI_COMMIT=c755a1339397fc3d220f0b7aec6062a574e9c4ef
MTC_URL=https://github.com/amirdeljouyi/SCAM-2023-microtestcarver-replication.git
MTC_COMMIT=88af3ad674d7e176d2b7747d921568f8a58b897f
JDK17_VERSION=jdk-17.0.20.1+1
JDK17_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.20.1%2B1/OpenJDK17U-jdk_x64_linux_hotspot_17.0.20.1_1.tar.gz
JDK17_SHA=3808d1d15e3ec6bd5b84057fb5d84c33d8a1536a258146bcea2e603fc726e08e
JDK8_URL=https://corretto.aws/downloads/resources/8.462.08.1/amazon-corretto-8.462.08.1-linux-x64.tar.gz
JDK8_SHA=b30a150ba1eacb54595fca0675eb3b1670e36062a2cc489fe055f7b713efa115
GRADLE_URL=https://services.gradle.org/distributions/gradle-7.6-bin.zip
GRADLE_SHA=7ba68c54029790ab444b39d7e293d3236b2632631fb5f2e012bb28b4ff669e4b
GLOWROOT_URL=https://github.com/glowroot/glowroot/releases/download/v0.14.0/glowroot-0.14.0-dist.zip
GLOWROOT_SHA=e404f18083a963a195cdd7328f561992ef7ae938e625dc8349737ae1fd6700d6
NUMPY=1.26.4
PANDAS=2.2.2
# test dependencies the generated tests (or the tool's additions to the module pom) need, offline later
TEST_DEPS=(com.thoughtworks.xstream:xstream:1.4.12 org.junit.jupiter:junit-jupiter:5.10.3
           org.mockito:mockito-core:5.11.0 org.mockito:mockito-junit-jupiter:5.11.0 org.hamcrest:hamcrest:2.2)

TMP_DIRS=()
trap 'rm -rf "${TMP_DIRS[@]:-}"' EXIT
fetch() {  # fetch <url> <sha256> <dest-file>
  local tmp; tmp=$(mktemp -d "$EXT/.dl.XXXXXX"); TMP_DIRS+=("$tmp")
  curl -fsSL "$1" -o "$tmp/f"
  [ "$(sha256sum "$tmp/f" | cut -d' ' -f1)" = "$2" ] || { echo "checksum mismatch: $1"; exit 1; }
  mv "$tmp/f" "$3"
}
unzip_into() {  # unzip_into <zip> <dir>   (python: unzip(1) is not required; keeps the executable bits)
  python3 - "$1" "$2" <<'PY'
import os, sys, zipfile
with zipfile.ZipFile(sys.argv[1]) as z:
    for i in z.infolist():
        p = z.extract(i, sys.argv[2])
        mode = (i.external_attr >> 16) & 0o777
        if mode:
            os.chmod(p, mode)
PY
}
untar_into() {  # untar_into <url> <sha> <dest-dir>   (strips the top directory)
  [ -d "$3" ] && return 0
  local tmp; tmp=$(mktemp -d "$EXT/.x.XXXXXX"); TMP_DIRS+=("$tmp")
  fetch "$1" "$2" "$tmp/a.tgz"
  mkdir "$tmp/d" && tar -xzf "$tmp/a.tgz" -C "$tmp/d" --strip-components 1
  mv "$tmp/d" "$3"
}

echo "== JDKs and build tools"
untar_into "$JDK17_URL" "$JDK17_SHA" "$EXT/jdk17"
untar_into "$JDK8_URL" "$JDK8_SHA" "$EXT/jdk8"
"$ROOT/scripts/install_jdk11.sh" "$EXT" >/dev/null
[ -L "$EXT/jdk11" ] || ln -s "$EXT/jdk-11.0.32.1+1" "$EXT/jdk11"
if [ ! -x "$EXT/gradle/bin/gradle" ]; then
  tmp=$(mktemp -d "$EXT/.g.XXXXXX"); TMP_DIRS+=("$tmp")
  fetch "$GRADLE_URL" "$GRADLE_SHA" "$tmp/g.zip" && unzip_into "$tmp/g.zip" "$tmp" && mv "$tmp/gradle-7.6" "$EXT/gradle"
fi
if [ ! -f "$EXT/glowroot/glowroot.jar" ]; then
  tmp=$(mktemp -d "$EXT/.gr.XXXXXX"); TMP_DIRS+=("$tmp")
  fetch "$GLOWROOT_URL" "$GLOWROOT_SHA" "$tmp/g.zip" && unzip_into "$tmp/g.zip" "$tmp" && mv "$tmp/glowroot" "$EXT/glowroot"
fi
if [ ! -x "$EXT/venv/bin/python" ]; then
  python3 -m venv "$EXT/venv"
  "$EXT/venv/bin/pip" install -q "numpy==$NUMPY" "pandas==$PANDAS"
fi

echo "== Pankti and RICK ($PANKTI_COMMIT + external/pankti.patch)"
PK="$EXT/pankti"
if [ ! -f "$PK/.patched" ] || ! cmp -s "$PK/.patched" external/pankti.patch; then
  rm -rf "$PK" && git clone -q "$PANKTI_URL" "$PK" && git -C "$PK" checkout -q "$PANKTI_COMMIT"
  (cd "$PK" && patch -s -p1 --forward -i "$ROOT/external/pankti.patch")
  cp external/pankti.patch "$PK/.patched"
fi
JARS=("$PK/pankti-extract/target/pankti-extract-1.0-SNAPSHOT-jar-with-dependencies.jar"
      "$PK/pankti-generate/target/pankti-generate-1.0-SNAPSHOT-jar-with-dependencies.jar"
      "$PK/rick/target/rick-1.0-SNAPSHOT-jar-with-dependencies.jar")
built=1; for j in "${JARS[@]}"; do [ -f "$j" ] || built=0; done
if [ $built = 0 ]; then
  # the assembly of pankti-instrument fails now and then on a fresh build ("Error creating assembly
  # archive"); a second build succeeds
  for attempt in 1 2; do
    (cd "$PK" && JAVA_HOME="$EXT/jdk17" PATH="$EXT/jdk17/bin:$PATH" mvn -q -B install -DskipTests \
       -Dcheckstyle.skip -Dmaven.javadoc.skip "-Dmaven.repo.local=$EXT/m2") && break
    [ $attempt = 2 ] && { echo "cannot build Pankti/RICK"; exit 1; }
  done
  # the Glowroot plugin is built per cell, offline: resolve its build once here
  (cd "$PK/pankti-instrument" && JAVA_HOME="$EXT/jdk17" PATH="$EXT/jdk17/bin:$PATH" mvn -q -B package -DskipTests \
     -Dcheckstyle.skip -Dmaven.javadoc.skip "-Dmaven.repo.local=$EXT/m2" && rm -rf target)
fi

echo "== MicroTestCarver ($MTC_COMMIT + external/mtc/*.patch)"
MS="$EXT/mtc-src"
if [ ! -d "$MS/implementation" ] || [ "$(git -C "$MS" rev-parse HEAD)" != "$MTC_COMMIT" ]; then
  rm -rf "$MS" && git clone -q --filter=blob:none --no-checkout "$MTC_URL" "$MS"
  git -C "$MS" sparse-checkout set --no-cone /implementation/Modified-BTrace /implementation/MicroTestCarver
  git -C "$MS" checkout -q "$MTC_COMMIT"
fi
patched_copy() {  # patched_copy <upstream-dir> <patch> <dest>
  if [ -f "$3/.patched" ] && cmp -s "$3/.patched" "$2"; then return 0; fi
  rm -rf "$3" && mkdir -p "$(dirname "$3")" && cp -r "$1" "$3" && rm -rf "$3/.git" "$3/build" "$3/.gradle"
  (cd "$3" && patch -s -p1 --forward -i "$2") && cp "$2" "$3/.patched"
}
patched_copy "$MS/implementation/Modified-BTrace" "$ROOT/external/mtc/btrace-build.patch" "$EXT/mtc/btrace"
patched_copy "$MS/implementation/MicroTestCarver" "$ROOT/external/mtc/carver-build.patch" "$EXT/mtc/carver"
BT_DIST="$EXT/mtc/btrace/btrace-dist/build/resources/main/v2.3.0-SNAPSHOT"
export GRADLE_USER_HOME="$EXT/gradle-home"
# the distribution directory of BTrace is not always complete after the first build; build until it is
for attempt in 1 2; do
  [ -f "$BT_DIST/libs/btrace-agent.jar" ] && [ -f "$BT_DIST/libs/btrace-client.jar" ] && break
  (cd "$EXT/mtc/btrace" && JAVA_8_HOME="$EXT/jdk8" JAVA_9_HOME="$EXT/jdk11" JAVA_11_HOME="$EXT/jdk11" \
     JAVA_16_HOME="$EXT/jdk17" JAVA_17_HOME="$EXT/jdk17" JAVA_HOME="$EXT/jdk11" \
     "$EXT/gradle/bin/gradle" --no-daemon -q :btrace-dist:build -x test -x javadoc -x spotlessCheck -x spotlessJava)
done
[ -f "$BT_DIST/libs/btrace-client.jar" ] || { echo "cannot build the modified BTrace"; exit 1; }
CARVER_JAR="$EXT/mtc/carver/build/libs/carving-1.0-SNAPSHOT-all.jar"
if [ ! -f "$CARVER_JAR" ]; then
  mkdir -p "$EXT/mtc/carver/libs" && cp "$BT_DIST/libs/btrace-client.jar" "$EXT/mtc/carver/libs/"
  (cd "$EXT/mtc/carver" && JAVA_HOME="$EXT/jdk17" "$EXT/gradle/bin/gradle" --no-daemon -q shadowJar -x test)
fi

echo "== test dependencies of the generated tests -> $M2_REPO"
for a in "${TEST_DEPS[@]}"; do
  mvn -q -B dependency:get "-Dartifact=$a" "-Dmaven.repo.local=$M2_REPO" >/dev/null
done
# JUnit 4 modules that get the Jupiter engine (JGit, PDFBox) switch surefire to its JUnit Platform provider,
# which the projects' own offline builds never downloaded: fetch it for every surefire version in the repo
for v in $(ls "$M2_REPO/org/apache/maven/plugins/maven-surefire-plugin" 2>/dev/null); do
  mvn -q -B dependency:get "-Dartifact=org.apache.maven.surefire:surefire-junit-platform:$v" \
    "-Dmaven.repo.local=$M2_REPO" >/dev/null
done

python3 - "$EXT" <<EOF
import json, sys, subprocess
ext = sys.argv[1]
def java(home):
    r = subprocess.run([f"{home}/bin/java", "-version"], capture_output=True, text=True)
    return r.stderr.splitlines()[0] if r.stderr else None
json.dump({
    "pankti": {"url": "$PANKTI_URL", "commit": "$PANKTI_COMMIT", "patch": "external/pankti.patch"},
    "microtestcarver": {"url": "$MTC_URL", "commit": "$MTC_COMMIT",
                        "patches": ["external/mtc/btrace-build.patch", "external/mtc/carver-build.patch"]},
    "glowroot": "0.14.0", "gradle": "7.6", "numpy": "$NUMPY", "pandas": "$PANDAS",
    "jdk17": java(f"{ext}/jdk17"), "jdk11": java(f"{ext}/jdk11"), "jdk8": java(f"{ext}/jdk8"),
    "test_dependencies": "${TEST_DEPS[*]}".split(),
}, open(f"{ext}/versions.json", "w"), indent=2)
EOF
echo "external tools installed in $EXT"
