#!/usr/bin/env bash
# Builds SplitLedger (if needed) and starts it. Extra arguments are passed to the app.
#   ./scripts/run.sh                                   interactive
#   ./scripts/run.sh examples/sample-ledger.csv        open a saved ledger
#   ./scripts/run.sh --script examples/demo-trip.txt   run the demo
set -euo pipefail
cd "$(dirname "$0")/.."
JAR=target/splitledger-1.0.0.jar
if [ ! -f "$JAR" ]; then
  mvn -q -B package -DskipTests
fi
exec java -jar "$JAR" "$@"
