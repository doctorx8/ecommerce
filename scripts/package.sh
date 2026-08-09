#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="/opt/homebrew/opt/openjdk@21/bin:${PATH:-}"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"

echo "==> Building frontend"
cd "$ROOT/frontend"
npm ci
npm run build

echo "==> Copying UI into Spring Boot static/"
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -R "$ROOT/frontend/dist/." "$ROOT/backend/src/main/resources/static/"

echo "==> Building Spring Boot jar"
cd "$ROOT/backend"
mvn -q -DskipTests package

OUT="$ROOT/dist"
mkdir -p "$OUT"
cp "$ROOT/backend/target/store-api-1.0.0.jar" "$OUT/karwan-store.jar"

echo ""
echo "Package ready: $OUT/karwan-store.jar"
echo "Run with MySQL available:"
echo "  java -jar dist/karwan-store.jar"
echo ""
echo "Or deploy everywhere with Docker:"
echo "  docker compose up -d --build"
