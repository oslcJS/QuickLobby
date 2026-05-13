#!/usr/bin/env bash
set -e
PROFILES=(paper-1.20.4 paper-1.21.4 paper-1.21.5 paper-1.21.6 paper-1.21.7 \
          paper-1.21.8 paper-26.1.1 paper-26.1.2)
mkdir -p dist
echo "==> Building default (1.20.4 baseline)"
mvn -q clean package
cp target/QuickEco-*.jar dist/QuickEco-universal.jar
for p in "${PROFILES[@]}"; do
  echo "==> Verifying compile against $p"
  mvn -q -P "$p" clean package
  cp target/QuickEco-*.jar "dist/QuickEco-${p#paper-}.jar"
done
echo "==> Done. Artifacts in dist/"
ls -la dist/
