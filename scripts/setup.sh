#!/usr/bin/env bash
# Install the tools into .tools/ (or paths.tools of the config) and prepare the subject projects.
#
#   scripts/setup.sh [config.yaml]
#
#   1. checks the JDK of the config (`jdk`, 21 by default; paths.java_home or JAVA_HOME), Maven 3.9, git,
#      curl, python3 with PyYAML (the cases of tools-projects.csv run on JDK 11: scripts/install_jdk11.sh);
#   2. Node.js 22 (checksum verified) and Pi (pinned version; its npm-shrinkwrap.json pins the
#      dependency tree) into <tools>/node and <tools>/pi, unless already there;
#   3. BTrace 2.2.6 into <tools>/btrace;
#   4. PIT 1.15.8 into the local Maven repository;
#   5. the JDI server of the autonomous mode (pi/debugger/JdiDapServer.java) into <tools>/debugger;
#   6. (cases of tools-projects.csv) JDK 11: scripts/install_jdk11.sh <dir>, then a config with `jdk: 11`,
#      paths.java_home = <dir>/jdk-11.0.32.1+1 and `case_list: tools-projects.csv`; the steps below are
#      the same (PIT, BTrace, the debugger compiled with that JDK);
#   7. clones the subject projects of the config's case list (cases.csv, or case_list of the config) at
#      the commits of projects.yaml into paths.projects and builds them
#      (scripts/prepare_projects.py): the case commands run offline afterwards; projects whose
#      build already succeeded at the pinned commit are not rebuilt (--rebuild to force).
#
# Downloads go to a temporary file and are moved into place only after the checksum and the
# extraction succeed, so an interrupted run leaves nothing half-installed; just run it again.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG="${1:-$ROOT/config.yaml}"
NODE_VERSION=22.17.0
PI_VERSION=0.84.1
BTRACE_VERSION=2.2.6
BTRACE_SHA256=aaa5ce8d5c4493775a11b2526d989d6e8444a14508de797fb0f99f9bcc574f82
# https://nodejs.org/dist/v22.17.0/SHASUMS256.txt
NODE_SHA256_linux_x64=0fa01328a0f3d10800623f7107fbcd654a60ec178fab1ef5b9779e94e0419e1a
NODE_SHA256_linux_arm64=3e99df8b01b27dc8b334a2a30d1cd500442b3b0877d217b308fd61a9ccfc33d4
NODE_SHA256_darwin_arm64=615dda58b5fb41fad2be43940b6398ca56554cbe05800953afadc724729cb09e
NODE_SHA256_darwin_x64=c39c8ec3cdadedfcc75de0cb3305df95ae2aecebc5db8d68a9b67bd74616d2ad
# PIT jars (path in the Maven repository, sha256)
PIT_JARS=(
  "org/pitest/pitest/1.15.8/pitest-1.15.8.jar c4f8024a2faf52f3cf5d205b47b1e18fe3cdc30503aaa9ecf75fe67cf3b0ff76"
  "org/pitest/pitest-entry/1.15.8/pitest-entry-1.15.8.jar 6cd71474c6bef7e05d7d53086253ded57c9f9a2e636ee99c39cfe96ec5131752"
  "org/pitest/pitest-command-line/1.15.8/pitest-command-line-1.15.8.jar f1126c5b834b3c8c09f5d6c44a4e38929001283886f2328beaf6e12132d0af2e"
  "org/pitest/pitest-junit5-plugin/1.2.1/pitest-junit5-plugin-1.2.1.jar 9d944887a583ebd4adc33d2d26b8b56639fd53fee93b4d068a90596705c072b5"
  "org/apache/commons/commons-text/1.11.0/commons-text-1.11.0.jar 2acf30a070b19163d5a480eae411a281341e870020e3534c6d5d4c8472739e30"
  "org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar 82f528cf718c7a3c2f30fc5bc784e3c6a0a10b17605dadb9e16c82ede11e6064"
  # JUnit 4 projects (JGit) run under PIT through the vintage engine on the JUnit Platform
  "org/junit/vintage/junit-vintage-engine/5.9.3/junit-vintage-engine-5.9.3.jar 6bc1b42282aed8b9eccdb40631cf45001af8ab3ad9d0d85cff86c45e85fc59d0"
  "org/junit/platform/junit-platform-launcher/1.9.3/junit-platform-launcher-1.9.3.jar 8515e91808a09c8c9af5e359185b5991b5e76b8adcf185b9a3dab4ec8e5ba8ff"
  "org/junit/platform/junit-platform-engine/1.9.3/junit-platform-engine-1.9.3.jar 0c39553d9a03510757227f5a1c6cc6530287b1a321ed6258450664874aa2a16a"
  "org/junit/platform/junit-platform-commons/1.9.3/junit-platform-commons-1.9.3.jar 8519157df813c210e85fc1414b74109e3d85f43d7092563ed704c43c48f0d5e6"
)

cd "$ROOT"
python3 -c 'import yaml' 2>/dev/null || { echo "python3 with PyYAML required (pip install pyyaml)"; exit 1; }

TMP_DIRS=()
trap 'rm -rf "${TMP_DIRS[@]:-}"' EXIT

sha256_check() {  # sha256_check <expected> <file>
  local got
  if command -v sha256sum >/dev/null; then got=$(sha256sum "$2" | cut -d' ' -f1)
  else got=$(shasum -a 256 "$2" | cut -d' ' -f1); fi
  [ "$got" = "$1" ] || { echo "checksum mismatch: $2 (expected $1, got $got)"; return 1; }
}

eval "$(python3 - "$CONFIG" <<'EOF'
import shlex, sys
from runner.config import load_config
c = load_config(sys.argv[1])
for k, v in {"TOOLS": c.tools_dir, "JAVA_HOME": c.java_home, "M2_REPO": c.maven_repository,
             "PROJECTS": c.projects_dir, "JDK": c.jdk}.items():
    print(f"{k}={shlex.quote(str(v))}")
EOF
)"
export JAVA_HOME PATH="$JAVA_HOME/bin:$PATH"

echo "== checks"
"$JAVA_HOME/bin/java" -version 2>&1 | head -1 | grep -q "\"$JDK[.\"]" || { echo "JDK $JDK required at $JAVA_HOME"; exit 1; }
command -v mvn >/dev/null || { echo "Maven (mvn) required on PATH"; exit 1; }
MAVEN_VERSION=$(mvn -v 2>/dev/null | sed -n 's/^Apache Maven \([0-9.]*\).*/\1/p' | head -1)
case "$MAVEN_VERSION" in 3.9.*) ;; *) echo "Maven 3.9 required, found '${MAVEN_VERSION:-none}'"; exit 1 ;; esac
for tool in git curl tar; do command -v "$tool" >/dev/null || { echo "$tool required"; exit 1; }; done
if command -v sha256sum >/dev/null || command -v shasum >/dev/null; then :; else echo "sha256sum or shasum required"; exit 1; fi
echo "JDK: $("$JAVA_HOME/bin/java" -version 2>&1 | head -1); Maven $MAVEN_VERSION"
mkdir -p "$TOOLS"
rm -rf "$TOOLS"/.node.* "$TOOLS"/.btrace.*  # leftovers of an interrupted run

echo "== Node.js $NODE_VERSION and Pi $PI_VERSION"
if [ ! -x "$TOOLS/node/bin/node" ]; then
  case "$(uname -s)-$(uname -m)" in
    Linux-x86_64) NODE_PLATFORM=linux-x64 ;;
    Linux-aarch64) NODE_PLATFORM=linux-arm64 ;;
    Darwin-arm64) NODE_PLATFORM=darwin-arm64 ;;
    Darwin-x86_64) NODE_PLATFORM=darwin-x64 ;;
    *) echo "unsupported platform $(uname -s)-$(uname -m)"; exit 1 ;;
  esac
  node_sha_var="NODE_SHA256_${NODE_PLATFORM//-/_}"
  tmp=$(mktemp -d "$TOOLS/.node.XXXXXX"); TMP_DIRS+=("$tmp")
  curl -fsSL "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-$NODE_PLATFORM.tar.gz" -o "$tmp/node.tgz"
  sha256_check "${!node_sha_var}" "$tmp/node.tgz"
  mkdir "$tmp/node" && tar -xzf "$tmp/node.tgz" -C "$tmp/node" --strip-components 1
  rm -rf "$TOOLS/node" && mv "$tmp/node" "$TOOLS/node" && rm -rf "$tmp"
fi
export PATH="$TOOLS/node/bin:$PATH"
if [ "$("$TOOLS/pi/bin/pi" --version 2>/dev/null | tail -1)" != "$PI_VERSION" ]; then
  rm -rf "$TOOLS/pi"
  npm install --global --prefix "$TOOLS/pi" --cache "$TOOLS/npm-cache" --no-fund --no-audit --no-update-notifier \
    "@earendil-works/pi-coding-agent@$PI_VERSION"
fi
"$TOOLS/pi/bin/pi" --version | tail -1
[ -f "$TOOLS/pi/lib/node_modules/@earendil-works/pi-coding-agent/npm-shrinkwrap.json" ] || {
  echo "Pi $PI_VERSION has no npm-shrinkwrap.json: its dependency tree is not pinned"; exit 1; }

echo "== BTrace $BTRACE_VERSION"
btrace_ok() { for j in agent boot client; do [ -f "$TOOLS/btrace/libs/btrace-$j.jar" ] || return 1; done; }
if ! btrace_ok; then
  tmp=$(mktemp -d "$TOOLS/.btrace.XXXXXX"); TMP_DIRS+=("$tmp")
  curl -fsSL "https://github.com/btraceio/btrace/releases/download/v$BTRACE_VERSION/btrace-v$BTRACE_VERSION-bin.tar.gz" -o "$tmp/btrace.tgz"
  sha256_check "$BTRACE_SHA256" "$tmp/btrace.tgz"
  mkdir "$tmp/btrace" && tar -xzf "$tmp/btrace.tgz" -C "$tmp/btrace"
  rm -rf "$TOOLS/btrace" && mv "$tmp/btrace" "$TOOLS/btrace" && rm -rf "$tmp"
  btrace_ok || { echo "unexpected BTrace archive layout: libs/btrace-{agent,boot,client}.jar missing"; exit 1; }
fi

echo "== PIT"
for entry in "${PIT_JARS[@]}"; do
  read -r path sum <<<"$entry"
  IFS=/ read -ra parts <<<"$path"
  n=${#parts[@]}
  group=$(IFS=.; echo "${parts[*]:0:n-3}")
  artifact="$group:${parts[n-3]}:${parts[n-2]}"
  [ -f "$M2_REPO/$path" ] || mvn -q -Dmaven.repo.local="$M2_REPO" \
    org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get -Dartifact="$artifact" -Dtransitive=false
  sha256_check "$sum" "$M2_REPO/$path"
done

# PIT runs the generated tests on the JUnit Platform launcher of the project's platform version; the
# builds of the tools-projects (cases/tools-projects.csv) do not download it
if python3 -c "import sys; from runner.config import load_config; sys.exit(load_config(sys.argv[1]).case_list != 'tools-projects.csv')" "$CONFIG"; then
  for v in 1.7.0 1.8.2; do
    [ -f "$M2_REPO/org/junit/platform/junit-platform-launcher/$v/junit-platform-launcher-$v.jar" ] || mvn -q -Dmaven.repo.local="$M2_REPO" \
      org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get -Dartifact=org.junit.platform:junit-platform-launcher:$v -Dtransitive=false
  done
fi

echo "== debugger"
python3 -m runner.debugger --config "$CONFIG"

echo "== projects in $PROJECTS"
python3 scripts/prepare_projects.py --config "$CONFIG" "${@:2}"
echo "setup done"
