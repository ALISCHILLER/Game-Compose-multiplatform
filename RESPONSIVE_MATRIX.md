# Responsive Verification Matrix

MSA Bee uses one logical game world and adapts its camera, overlays, HUD, safe-area padding, and content density to the available window.

## Supported layout classes

| Class | Typical range | Overlay | HUD | Camera |
|---|---|---|---|---|
| Compact portrait | Small and standard phones | Vertical, scrollable | Compact below 360dp | Full-world fit |
| Compact landscape | Phones and low-height windows below 480dp | Horizontal, scrollable | Single compact bar | Bottom-anchored limited zoom |
| Medium portrait | Tablets and foldables from 600dp | Centered vertical panel up to 580dp | Centered up to 560dp | Full-world fit |
| Medium landscape | Landscape windows from 600dp | Horizontal panel | Centered bounded HUD | Adaptive fit |
| Expanded | Large tablets and desktop windows from 840dp | Horizontal panel up to 880dp | Centered up to 560dp | Adaptive fit |

Large font scale can promote a short window into compact-height behavior even when its raw height is above 480dp.

## Automated matrix

The offline audit verifies these logical dp viewports:

- `320 × 568` small phone portrait
- `393 × 852` standard phone portrait
- `852 × 393` low-height phone landscape
- `852 × 393` at 200% font scale
- `800 × 1280` tablet portrait
- `1280 × 800` tablet landscape
- `1200 × 420` low-height desktop window
- `1440 × 900` expanded desktop window

It additionally verifies the adaptive gameplay camera at:

- `800 × 320`
- `852 × 393`
- `1024 × 420`
- `1280 × 400`

For each landscape camera case, the ground remains bottom-aligned, the starting bee remains visible, the world is not stretched, and the playfield receives a bounded zoom instead of becoming a tiny portrait strip.

## Commands

```bash
./scripts/run_responsive_audit.sh
./scripts/run_common_pure_tests.sh
```

Compose UI tests additionally cover start/restart controls on compact portrait, low-height landscape, tablet portrait, tablet landscape, and 200% font scale.

## Required device evidence

Before a store release, run the applicable smoke tests on:

- one small Android phone
- one modern tall Android phone
- one Android tablet or foldable
- one iPhone in portrait and landscape
- one iPad
- Windows, macOS, and Linux resized to low-height and expanded windows
- JS and Wasm in Chromium, Firefox, and Safari where supported

Record screenshots or video for portrait, landscape, large text, display zoom, keyboard navigation, and safe-area behavior.

## Visual-density behavior

The same responsive policy also selects mascot size, title width, action width, content spacing, panel corner radius, score-card stacking, and control-hint stacking. This keeps the visual hierarchy stable instead of merely shrinking fonts on small screens. Decorative scenery never affects the logical `360 × 640` physics world.

## Settings panel

- Compact portrait uses one scrollable column.
- Low-height landscape and wide tablets use two bounded columns when space allows.
- All controls remain within safe drawing insets.
- Switches, sliders, reset confirmation, and the Done action remain reachable at 200% font scale.
- Opening settings during an active flight pauses the frame loop and audio without advancing physics time.
