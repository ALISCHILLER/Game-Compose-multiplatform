#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/msa-bee-settings-audit.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

command -v kotlinc >/dev/null 2>&1 || {
  echo "kotlinc is required for the settings audit." >&2
  exit 1
}
command -v kotlin >/dev/null 2>&1 || {
  echo "kotlin runner is required for the settings audit." >&2
  exit 1
}

KOTLIN_HOME="$(cd "$(dirname "$(command -v kotlinc)")/.." && pwd)"
COROUTINES_JAR="$KOTLIN_HOME/lib/kotlinx-coroutines-core-jvm.jar"
if [[ ! -f "$COROUTINES_JAR" ]]; then
  echo "The Kotlin distribution does not include kotlinx-coroutines-core-jvm.jar." >&2
  exit 1
fi

kotlinc -J-Dkotlin.daemon.enabled=false \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettings.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettingsCodec.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettingsStore.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/SettingsController.kt" \
  "$ROOT/scripts/SettingsAudit.kt" \
  -classpath "$COROUTINES_JAR" \
  -d "$TMP_DIR/settings-audit.jar"

kotlin -classpath "$TMP_DIR/settings-audit.jar:$COROUTINES_JAR" com.msa.audit.SettingsAuditKt
