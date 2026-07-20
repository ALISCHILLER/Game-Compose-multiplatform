#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/msa-bee-engine-audit.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

command -v kotlinc >/dev/null 2>&1 || {
  echo "kotlinc is required for the dependency-free engine audit." >&2
  exit 1
}

command -v kotlin >/dev/null 2>&1 || {
  echo "kotlin runner is required for the dependency-free engine audit." >&2
  exit 1
}

# Avoid embedding the Kotlin runtime into the audit JAR. This keeps the audit
# fast and prevents unnecessary compiler/runtime packaging work in CI.
kotlinc -J-Dkotlin.daemon.enabled=false \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/Bee.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/BeeCollisionBounds.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/Game.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameConfig.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameRandom.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshotCodec.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameStateStore.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameStatus.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/PipePair.kt" \
  "$ROOT/scripts/EngineAudit.kt" \
  -d "$TMP_DIR/engine-audit.jar"

kotlin -classpath "$TMP_DIR/engine-audit.jar" com.msa.audit.EngineAuditKt
