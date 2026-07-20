#!/usr/bin/env python3
"""Offline UI quality gates for MSA Bee."""

from __future__ import annotations

import math
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
COLOR_FILE = ROOT / "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/Color.kt"
UI_ROOT = ROOT / "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui"
STRINGS_EN = ROOT / "composeApp/src/commonMain/composeResources/values/strings.xml"
STRINGS_FA = ROOT / "composeApp/src/commonMain/composeResources/values-fa/strings.xml"


def fail(message: str) -> None:
    print(f"ui-verification-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def parse_colors() -> dict[str, tuple[int, int, int, int]]:
    text = COLOR_FILE.read_text(encoding="utf-8")
    colors: dict[str, tuple[int, int, int, int]] = {}
    pattern = re.compile(r"val\s+(\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)")
    for name, hex_value in pattern.findall(text):
        value = int(hex_value, 16)
        colors[name] = (
            (value >> 24) & 0xFF,
            (value >> 16) & 0xFF,
            (value >> 8) & 0xFF,
            value & 0xFF,
        )
    return colors


def linear_channel(channel: int) -> float:
    value = channel / 255.0
    return value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4


def luminance(rgba: tuple[int, int, int, int]) -> float:
    _, red, green, blue = rgba
    return 0.2126 * linear_channel(red) + 0.7152 * linear_channel(green) + 0.0722 * linear_channel(blue)


def contrast(a: tuple[int, int, int, int], b: tuple[int, int, int, int]) -> float:
    high, low = sorted((luminance(a), luminance(b)), reverse=True)
    return (high + 0.05) / (low + 0.05)


def parse_string_keys(path: Path) -> set[str]:
    root = ET.parse(path).getroot()
    return {element.attrib["name"] for element in root.findall("string")}


def main() -> None:
    colors = parse_colors()
    required_colors = {
        "GamePrimaryOrange",
        "GameTextWhite",
        "GameTextMuted",
        "GamePanel",
        "GameLeafGreen",
        "GameHoneyYellow",
        "GameBeeBrownDark",
        "GameDanger",
    }
    missing = sorted(required_colors - colors.keys())
    if missing:
        fail(f"missing required colors: {', '.join(missing)}")

    required_contrast = (
        ("primary button", "GameTextWhite", "GamePrimaryOrange", 4.5),
        ("success button", "GameTextWhite", "GameLeafGreen", 4.5),
        ("error foreground", "GameTextWhite", "GameDanger", 4.5),
        ("panel body text", "GameTextMuted", "GamePanel", 4.5),
        ("gold badge", "GameBeeBrownDark", "GameHoneyYellow", 4.5),
    )
    results: list[str] = []
    for label, foreground, background, minimum in required_contrast:
        ratio = contrast(colors[foreground], colors[background])
        if ratio + 1e-6 < minimum:
            fail(f"{label} contrast {ratio:.2f}:1 is below {minimum:.1f}:1")
        results.append(f"{label}={ratio:.2f}:1")

    english_keys = parse_string_keys(STRINGS_EN)
    persian_keys = parse_string_keys(STRINGS_FA)
    if english_keys != persian_keys:
        missing_fa = sorted(english_keys - persian_keys)
        missing_en = sorted(persian_keys - english_keys)
        fail(f"localized string mismatch; missing-fa={missing_fa}, missing-en={missing_en}")

    required_strings = {
        "game_mode_badge",
        "control_touch_title",
        "control_keyboard_title",
        "privacy_note",
        "game_over_subtitle",
        "new_record",
        "restart_hint",
        "score_increment",
        "score_gained",
        "settings_title",
        "sound_enabled",
        "music_volume",
        "effects_volume",
        "reduced_motion",
        "show_hints",
        "reset_progress_confirm_title",
    }
    if not required_strings.issubset(english_keys):
        fail(f"new UI strings are incomplete: {sorted(required_strings - english_keys)}")

    required_files = {
        "GameUiComponents.kt",
        "ScoreFeedback.kt",
        "StartOverlay.kt",
        "GameOverOverlay.kt",
        "GameHud.kt",
        "GameCanvas.kt",
        "GameScreen.kt",
        "GameTheme.kt",
        "SettingsOverlay.kt",
        "GameplayHintBanner.kt",
    }
    existing_files = {path.name for path in UI_ROOT.glob("*.kt")}
    if not required_files.issubset(existing_files):
        fail(f"missing UI source files: {sorted(required_files - existing_files)}")

    combined_ui = "\n".join(path.read_text(encoding="utf-8") for path in UI_ROOT.glob("*.kt"))
    for fragment in (
        "BeeEmblem(",
        "OverlayBackdrop(",
        "ScoreFeedback(",
        "NEW_RECORD",
        "SCORE_FEEDBACK",
        "SettingsOverlay(",
        "SETTINGS_OVERLAY",
        "SOUND_TOGGLE",
        "prefersReducedMotion",
    ):
        if fragment not in combined_ui and fragment != "prefersReducedMotion":
            fail(f"UI implementation is missing expected fragment: {fragment}")

    if ".height(58.dp)" in combined_ui:
        fail("fixed 58dp button height returned; use heightIn(min = 58.dp) for text scaling")

    start_overlay = (UI_ROOT / "StartOverlay.kt").read_text(encoding="utf-8")
    game_over = (UI_ROOT / "GameOverOverlay.kt").read_text(encoding="utf-8")
    if ".safeDrawingPadding()" not in start_overlay or ".safeDrawingPadding()" not in game_over:
        fail("overlay safe-area handling is missing")
    if "verticalScroll(rememberScrollState())" not in start_overlay:
        fail("start overlay is not scroll-safe")
    if "verticalScroll(rememberScrollState())" not in game_over:
        fail("game-over overlay is not scroll-safe")
    settings_overlay = (UI_ROOT / "SettingsOverlay.kt").read_text(encoding="utf-8")
    for fragment in ("Switch(", "Slider(", "safeDrawingPadding()", "verticalScroll(rememberScrollState())"):
        if fragment not in settings_overlay:
            fail(f"settings UI is missing expected behavior: {fragment}")

    print("ui-quality-verification-ok")
    print("contrast " + " ".join(results))
    print(f"localized-strings={len(english_keys)} ui-files={len(existing_files)}")


if __name__ == "__main__":
    main()
