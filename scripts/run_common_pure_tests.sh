#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/msa-bee-common-tests.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

command -v kotlinc >/dev/null 2>&1 || {
  echo "kotlinc is required for the common pure-test audit." >&2
  exit 1
}
command -v kotlin >/dev/null 2>&1 || {
  echo "kotlin runner is required for the common pure-test audit." >&2
  exit 1
}

KOTLIN_HOME="$(cd "$(dirname "$(command -v kotlinc)")/.." && pwd)"
COROUTINES_JAR="$KOTLIN_HOME/lib/kotlinx-coroutines-core-jvm.jar"
KOTLIN_TEST_JAR="$KOTLIN_HOME/lib/kotlin-test.jar"

for jar in "$COROUTINES_JAR" "$KOTLIN_TEST_JAR"; do
  if [[ ! -f "$jar" ]]; then
    echo "Required Kotlin distribution library is missing: $jar" >&2
    exit 1
  fi
done

sanitize_test_source() {
  local source="$1"
  local destination="$2"
  sed '/import kotlin.test.Test/d; /^[[:space:]]*@Test[[:space:]]*$/d' "$source" > "$destination"
}

sanitize_test_source \
  "$ROOT/composeApp/src/commonTest/kotlin/com/msa/compose_kmm/ComposeAppCommonTest.kt" \
  "$TMP_DIR/ComposeAppCommonTest.kt"
sanitize_test_source \
  "$ROOT/composeApp/src/commonTest/kotlin/com/msa/compose_kmm/GamePersistenceTest.kt" \
  "$TMP_DIR/GamePersistenceTest.kt"
sanitize_test_source \
  "$ROOT/composeApp/src/commonTest/kotlin/com/msa/compose_kmm/GameSettingsTest.kt" \
  "$TMP_DIR/GameSettingsTest.kt"

cat > "$TMP_DIR/Runner.kt" <<'KOTLIN'
package com.msa.compose_kmm

fun main() {
    GameEngineTest().apply {
        gameStartsWithDeviceIndependentLogicalWorld()
        idleGameDoesNotAdvanceSimulation()
        startAndJumpApplyExpectedState()
        fixedTimestepProducesEqualResultAcrossCommonFrameRates()
        hugeFrameDelayIsClampedAndDoesNotCreateUnboundedCatchUp()
        beeEventuallyHitsGroundWithoutJumping()
        frequentJumpsCauseARealPipeCollisionBeforeReachingGround()
        controlledJumpsCanPassAPipeAndIncreaseScore()
        restartResetsRoundState()
        restartKeepsBestScoreAcrossRounds()
    }

    GameViewportTest().apply {
        portraitViewportIsCenteredAndBottomAligned()
        landscapeViewportZoomsWithoutStretchingAndKeepsGroundAnchored()
        ultraWideLandscapeKeepsTheBeeStartAreaVisible()
        invalidViewportInputReturnsSafeIdentityTransform()
    }

    ResponsiveLayoutTest().apply {
        smallPortraitUsesCompactVerticalLayout()
        lowHeightPhoneLandscapeUsesHorizontalOverlayAndCompactHud()
        portraitTabletUsesWiderCenteredPanelWithoutHorizontalOverlay()
        largeFontScaleMakesShortLandscapeCompact()
    }

    GamePersistenceTest().apply {
        snapshotRoundTripPreservesCompleteState()
        newRecordBaselineSurvivesSnapshotRoundTrip()
        malformedSnapshotIsRejected()
        restoreResetsTimingDebtAndContinuesSafely()
        restoredGameContinuesWithTheSameFuturePipeSequence()
        checksumRejectsAWellFormedButModifiedSnapshot()
        legacyV3SnapshotMigratesConservativelyToTheCurrentSchema()
        legacyV2SnapshotMigratesToTheCurrentSchema()
        legacyV1SnapshotMigratesToTheCurrentSchema()
        controllerLoadsSavedGameAndPersistsUserActions()
        controllerClearsAnInvalidStoredSnapshot()
        controllerCreatesADeferredCheckpointForEveryJump()
        controllerPersistsScoreChangesImmediately()
        resetProgressClearsSavedStateAndBestScore()
        controllerContainsStorageFailuresWithoutCrashingTheGame()
        controllerUsesImmediateDurabilityForCriticalTransitions()
    }

    GameSettingsTest().apply {
        settingsCodecRoundTripsEveryPreference()
        settingsCodecRejectsTampering()
        masterSoundControlsEffectiveVolumes()
        settingsControllerPersistsChangesAndRestoresDefaults()
        settingsControllerContainsStorageFailures()
    }

    println("common-pure-tests-runtime-ok")
}
KOTLIN

kotlinc -J-Dkotlin.daemon.enabled=false \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/Bee.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/BeeCollisionBounds.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/Game.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameConfig.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameRandom.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshotCodec.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameStateStore.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettings.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettingsCodec.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettingsStore.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameStatus.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/PipePair.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/GameController.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/SettingsController.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameViewport.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/ResponsiveLayout.kt" \
  "$TMP_DIR/ComposeAppCommonTest.kt" \
  "$TMP_DIR/GamePersistenceTest.kt" \
  "$TMP_DIR/GameSettingsTest.kt" \
  "$TMP_DIR/Runner.kt" \
  -classpath "$COROUTINES_JAR:$KOTLIN_TEST_JAR" \
  -d "$TMP_DIR/common-tests.jar"

kotlin -classpath "$TMP_DIR/common-tests.jar:$COROUTINES_JAR:$KOTLIN_TEST_JAR" \
  com.msa.compose_kmm.RunnerKt
