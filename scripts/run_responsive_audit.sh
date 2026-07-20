#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/msa-bee-responsive.XXXXXX")"
trap 'rm -rf "$TMP_DIR"' EXIT

command -v kotlinc >/dev/null 2>&1 || {
  echo "kotlinc is required for the responsive audit." >&2
  exit 1
}
command -v kotlin >/dev/null 2>&1 || {
  echo "kotlin runner is required for the responsive audit." >&2
  exit 1
}

cat > "$TMP_DIR/ResponsiveAudit.kt" <<'KOTLIN'
package com.msa.compose_kmm.ui

import com.msa.compose_kmm.domain.GameConfig
import kotlin.math.abs

private data class Case(
    val name: String,
    val width: Float,
    val height: Float,
    val fontScale: Float,
    val expectedClass: ResponsiveWindowClass,
    val horizontalOverlay: Boolean,
    val compactHud: Boolean
)

fun main() {
    val cases = listOf(
        Case("small-phone-portrait", 320f, 568f, 1f, ResponsiveWindowClass.CompactPortrait, false, true),
        Case("phone-portrait", 393f, 852f, 1f, ResponsiveWindowClass.CompactPortrait, false, false),
        Case("phone-landscape-low", 852f, 393f, 1f, ResponsiveWindowClass.CompactLandscape, true, true),
        Case("phone-landscape-large-text", 852f, 393f, 2f, ResponsiveWindowClass.CompactLandscape, true, true),
        Case("tablet-portrait", 800f, 1280f, 1f, ResponsiveWindowClass.MediumPortrait, false, false),
        Case("tablet-landscape", 1280f, 800f, 1f, ResponsiveWindowClass.Expanded, true, false),
        Case("desktop-low-height", 1200f, 420f, 1f, ResponsiveWindowClass.CompactLandscape, true, true),
        Case("desktop-expanded", 1440f, 900f, 1f, ResponsiveWindowClass.Expanded, true, false)
    )

    cases.forEach { case ->
        val spec = calculateResponsiveLayout(case.width, case.height, case.fontScale)
        check(spec.windowClass == case.expectedClass) {
            "${case.name}: expected ${case.expectedClass}, actual ${spec.windowClass}"
        }
        check(spec.useHorizontalOverlay == case.horizontalOverlay) {
            "${case.name}: horizontal overlay mismatch"
        }
        check(spec.useCompactHud == case.compactHud) {
            "${case.name}: compact HUD mismatch"
        }
        check(spec.panelMaxWidthDp <= case.width || case.width < 440f) {
            "${case.name}: panel maximum is not bounded for the viewport"
        }
    }

    listOf(
        800f to 320f,
        852f to 393f,
        1024f to 420f,
        1280f to 400f
    ).forEach { (width, height) ->
        val viewport = calculateGameViewport(
            canvasWidth = width,
            canvasHeight = height,
            worldWidth = GameConfig.WORLD_WIDTH,
            worldHeight = GameConfig.WORLD_HEIGHT
        )
        val groundScreenY = GameConfig.WORLD_HEIGHT * viewport.scale + viewport.offsetY
        val beeStartScreenY = GameConfig.BEE_START_Y * viewport.scale + viewport.offsetY
        check(abs(groundScreenY - height) < 0.01f) {
            "Ground is not bottom anchored for ${width}x$height"
        }
        check(beeStartScreenY in 36f..height) {
            "Bee start area is outside the useful viewport for ${width}x$height: $beeStartScreenY"
        }
        check(viewport.scale > height / GameConfig.WORLD_HEIGHT) {
            "Landscape zoom was not applied for ${width}x$height"
        }
    }

    println("responsive-layout-audit-ok cases=${cases.size} landscapeViewports=4")
}
KOTLIN

kotlinc -J-Dkotlin.daemon.enabled=false \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameConfig.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameViewport.kt" \
  "$ROOT/composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/ResponsiveLayout.kt" \
  "$TMP_DIR/ResponsiveAudit.kt" \
  -d "$TMP_DIR/responsive-audit.jar"

kotlin -classpath "$TMP_DIR/responsive-audit.jar" \
  com.msa.compose_kmm.ui.ResponsiveAuditKt
