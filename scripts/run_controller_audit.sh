#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/msa-bee-controller-audit.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

command -v kotlinc >/dev/null 2>&1 || {
  echo "kotlinc is required for the controller audit." >&2
  exit 1
}
command -v kotlin >/dev/null 2>&1 || {
  echo "kotlin runner is required for the controller audit." >&2
  exit 1
}

KOTLIN_HOME="$(cd "$(dirname "$(command -v kotlinc)")/.." && pwd)"
COROUTINES_JAR="$KOTLIN_HOME/lib/kotlinx-coroutines-core-jvm.jar"
if [[ ! -f "$COROUTINES_JAR" ]]; then
  echo "The Kotlin distribution does not include kotlinx-coroutines-core-jvm.jar." >&2
  exit 1
fi

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
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/GameController.kt" \
  "$ROOT/scripts/ControllerAudit.kt" \
  -classpath "$COROUTINES_JAR" \
  -d "$TMP_DIR/controller-audit.jar"

kotlin -classpath "$TMP_DIR/controller-audit.jar:$COROUTINES_JAR" com.msa.audit.ControllerAuditKt
