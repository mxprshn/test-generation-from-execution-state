#!/usr/bin/env bash
# Install the tools into .tools/ (or paths.tools of the config) and prepare the subject projects.
#
#   scripts/setup.sh [config.yaml]
#
#   1. checks JDK 21 (paths.java_home or JAVA_HOME), Maven, git, python3 with PyYAML;
#   2. Node.js 22 and Pi (pinned version) into <tools>/node and <tools>/pi, unless already there;
#   3. BTrace 2.2.6 into <tools>/btrace;
#   4. PIT 1.15.8 into the local Maven repository;
#   5. clones the subject projects at the commits of projects.yaml into paths.projects and builds them
#      (scripts/prepare_projects.py): the case commands run offline afterwards.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG="${1:-$ROOT/config.yaml}"
NODE_VERSION=22.17.0
PI_VERSION=0.84.1
BTRACE_VERSION=2.2.6
BTRACE_SHA256=aaa5ce8d5c4493775a11b2526d989d6e8444a14508de797fb0f99f9bcc574f82
# PIT jars (path in the Maven repository, sha256)
PIT_JARS=(
  "org/pitest/pitest/1.15.8/pitest-1.15.8.jar c4f8024a2faf52f3cf5d205b47b1e18fe3cdc30503aaa9ecf75fe67cf3b0ff76"
  "org/pitest/pitest-entry/1.15.8/pitest-entry-1.15.8.jar 6cd71474c6bef7e05d7d53086253ded57c9f9a2e636ee99c39cfe96ec5131752"
  "org/pitest/pitest-command-line/1.15.8/pitest-command-line-1.15.8.jar f1126c5b834b3c8c09f5d6c44a4e38929001283886f2328beaf6e12132d0af2e"
  "org/pitest/pitest-junit5-plugin/1.2.1/pitest-junit5-plugin-1.2.1.jar 9d944887a583ebd4adc33d2d26b8b56639fd53fee93b4d068a90596705c072b5"
  "org/apache/commons/commons-text/1.11.0/commons-text-1.11.0.jar 2acf30a070b19163d5a480eae411a281341e870020e3534c6d5d4c8472739e30"
  "org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar 82f528cf718c7a3c2f30fc5bc784e3c6a0a10b17605dadb9e16c82ede11e6064"
)

cd "$ROOT"
eval "$(python3 - "$CONFIG" <<'EOF'
import shlex, sys
from runner.config import load_config
c = load_config(sys.argv[1])
for k, v in {"TOOLS": c.tools_dir, "JAVA_HOME": c.java_home, "M2_REPO": c.maven_repository,
             "PROJECTS": c.projects_dir}.items():
    print(f"{k}={shlex.quote(str(v))}")
EOF
)"
export JAVA_HOME PATH="$JAVA_HOME/bin:$PATH"

echo "== checks"
"$JAVA_HOME/bin/java" -version 2>&1 | head -1 | grep -q '"21' || { echo "JDK 21 required at $JAVA_HOME"; exit 1; }
command -v mvn >/dev/null || { echo "Maven (mvn) required on PATH"; exit 1; }
command -v git >/dev/null || { echo "git required"; exit 1; }
mkdir -p "$TOOLS"

echo "== Node.js $NODE_VERSION and Pi $PI_VERSION"
if [ ! -x "$TOOLS/node/bin/node" ]; then
  case "$(uname -s)-$(uname -m)" in
    Linux-x86_64) NODE_PLATFORM=linux-x64 ;;
    Linux-aarch64) NODE_PLATFORM=linux-arm64 ;;
    Darwin-arm64) NODE_PLATFORM=darwin-arm64 ;;
    Darwin-x86_64) NODE_PLATFORM=darwin-x64 ;;
    *) echo "unsupported platform $(uname -s)-$(uname -m)"; exit 1 ;;
  esac
  curl -fsSL "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-$NODE_PLATFORM.tar.gz" -o "$TOOLS/node.tgz"
  mkdir -p "$TOOLS/node" && tar -xzf "$TOOLS/node.tgz" -C "$TOOLS/node" --strip-components 1 && rm "$TOOLS/node.tgz"
fi
export PATH="$TOOLS/node/bin:$PATH"
if [ "$("$TOOLS/pi/bin/pi" --version 2>/dev/null | tail -1)" != "$PI_VERSION" ]; then
  npm install --global --prefix "$TOOLS/pi" --cache "$TOOLS/npm-cache" "@earendil-works/pi-coding-agent@$PI_VERSION"
fi
"$TOOLS/pi/bin/pi" --version | tail -1

echo "== BTrace $BTRACE_VERSION"
if [ ! -f "$TOOLS/btrace/libs/btrace-agent.jar" ]; then
  curl -fsSL "https://github.com/btraceio/btrace/releases/download/v$BTRACE_VERSION/btrace-v$BTRACE_VERSION-bin.tar.gz" -o "$TOOLS/btrace.tgz"
  echo "$BTRACE_SHA256  $TOOLS/btrace.tgz" | shasum -a 256 -c - >/dev/null
  mkdir -p "$TOOLS/btrace" && tar -xzf "$TOOLS/btrace.tgz" -C "$TOOLS/btrace" && rm "$TOOLS/btrace.tgz"
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
  echo "$sum  $M2_REPO/$path" | shasum -a 256 -c - >/dev/null || { echo "checksum mismatch: $path"; exit 1; }
done

echo "== projects in $PROJECTS"
python3 scripts/prepare_projects.py --config "$CONFIG"
echo "setup done"
