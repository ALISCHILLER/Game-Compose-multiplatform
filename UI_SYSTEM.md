# UI System

MSA Bee uses a small, purpose-built Material 3 visual system shared by Android, iOS, Desktop, JavaScript, and Wasm.

## Design goals

- The primary action is always obvious.
- Game information remains readable over a bright playfield.
- Compact landscape is playable rather than a compressed portrait layout.
- Large text expands controls instead of clipping labels.
- Persian and English preserve the same hierarchy without forcing one layout direction.
- Decorative motion respects Reduced Motion.
- All artwork remains project-owned and programmatic.

## Visual hierarchy

### Start

1. Offline arcade badge
2. Bee mascot
3. Game title and objective
4. Saved best score when available
5. Primary start action
6. Touch and keyboard instructions
7. Offline/no-ads/no-tracking note

### Gameplay

1. Bee, pipes, and collision space
2. Current score
3. Best score
4. Short localized `+1` feedback
5. Decorative sky and ground layers

### Settings

1. Master sound and independent music/effects controls
2. Separate volume sliders with immediate audio feedback state
3. Reduced Motion and gameplay-hint preferences
4. Restore-default and confirmed local-progress reset actions
5. Responsive one- or two-column layout with safe-area and large-text scrolling

### Game Over

1. Mascot mood and an explicit new-record badge only when the round exceeds the score that existed when it began
2. Result title and guidance
3. Current and best metrics
4. Primary replay action
5. Persistence reassurance

## Tokens

- Colors: `ui/Color.kt`
- Typography and shapes: `ui/GameTheme.kt`
- Responsive dimensions: `ui/ResponsiveLayout.kt`
- Shared panels, buttons, badges, metrics, control hints, mascot, and backdrop: `ui/GameUiComponents.kt`

## Accessibility

- Primary actions have at least a 58dp minimum height.
- The localized `+1` feedback owns the single polite live region for score changes; HUD metrics remain readable without producing duplicate announcements.
- The gameplay Canvas exposes a labeled jump action only while interactive.
- Background content is removed from semantics while modal overlays are visible.
- Overlays are traversal groups with pane titles.
- Scrollable panels keep actions reachable at 200% font scale.
- Required foreground/background combinations are checked by `scripts/verify_ui.py`.
- Settings use native Compose Switch and Slider semantics and remain keyboard/focus accessible.

## Motion

Reduced Motion disables decorative wing cycling, scene parallax, bee trails, score pulses, and rising score feedback while retaining essential gameplay movement.

## Verification

```bash
python scripts/verify_ui.py
./scripts/run_responsive_audit.sh
./scripts/run_common_pure_tests.sh
./scripts/run_settings_audit.sh
```

Full Compose UI and platform runtime verification still requires Gradle and the target platform toolchains.
