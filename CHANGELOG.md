# Changelog

## 1.7.1 — JVM compile hotfix

- Import `androidx.compose.ui.graphics.graphicsLayer` for the programmatic bee emblem so `rotationZ` compiles on JVM and other Compose targets.
- Capture `BoxWithConstraints.maxWidth` before entering the nested `ColumnScope` in the settings overlay, eliminating the ambiguous implicit-receiver compilation error.
- Bump Android and iOS build numbers to `12`.

## 1.7.0 — Persistent settings and audio polish

- Add a responsive in-game settings panel accessible from Start, HUD, and Game Over.
- Persist master sound, music/effects switches, independent volumes, Reduced Motion, and gameplay-hint preferences on every target.
- Pause gameplay and audio safely while settings are open, then resume without simulation time debt.
- Add a deterministic project-owned score chime and per-channel volume control to Android, iOS, Desktop, JS, and Wasm audio adapters.
- Add confirmed progress reset and default-settings restoration.
- Add checksum-protected settings encoding, controller tests, platform storage tests, Koin graph coverage, and UI test coverage.

## 1.6.0 — Final release hardening and state integrity

- Persist the pre-round best-score baseline in Snapshot schema 4 so New Record feedback survives process recreation.
- Migrate Snapshot schemas 1, 2 and 3 conservatively without false record celebrations.
- Persist score changes immediately to reduce the risk of losing a newly earned best score.
- Persist the active snapshot when the shared UI host is disposed.
- Fix Desktop release artifact collection for MSI, DMG and DEB outputs.
- Add suffix-filtered deterministic artifact checksums.
- Run UI quality verification in CI, security and release preflight workflows.
- Extend pure Kotlin tests and audits for schema 4, score durability and restored record state.

## 1.5.0 — Deep UI and visual-system refinement

- Introduced a centralized Material 3 typography, shape, color, contrast, and elevation system.
- Rebuilt the start experience with a programmatic bee mascot, offline-mode badge, saved-best metric, touch/keyboard instruction cards, and privacy note.
- Rebuilt Game Over with an explicit round-start-based new-record state, expressive mascot mood, highlighted result cards, clearer restart hierarchy, and large-text-safe scrolling. Tied scores no longer trigger a false celebration.
- Replaced the two-card HUD with a bounded premium score bar and Reduced-Motion-aware score pulse.
- Added a localized, single-live-region `+1` score feedback treatment when a pipe is cleared, avoiding duplicate screen-reader announcements from the HUD.
- Added layered sky, sun, clouds, hills, shrubs, grass, ground texture, and restrained parallax without external image assets.
- Refined pipes with rims, bevels, highlights, shadows, and cap details; refined the bee with gradients, antennae, face, legs, stinger, wings, shadow, and optional motion trail.
- Kept all decorative motion disabled or reduced when the platform Reduced Motion preference is active.
- Extended responsive policy with mascot, spacing, action-width, title-width, panel-radius, and control-hint decisions.
- Added localized English/Persian UI copy and strict locale key parity checks.
- Added `scripts/verify_ui.py` for automated WCAG contrast, localization, source-structure, safe-area, scroll-safety, and scalable-button gates.
- Added Compose UI coverage for previous-best display and new-record state.
- Bumped Android and iOS build numbers to `9`.

## 1.4.0 — Complete responsive layout

- Added a platform-independent responsive layout classifier for compact portrait, compact landscape, medium portrait, medium landscape, and expanded windows.
- Rebuilt start and game-over overlays with horizontal low-height landscape layouts, bounded tablet/desktop widths, safe-area padding, and scrollable large-text content.
- Added a compact single-bar HUD for narrow phones and short landscape windows while retaining bounded two-card HUDs on larger screens.
- Added a bottom-anchored, non-stretching landscape camera with bounded zoom so gameplay no longer collapses into a tiny portrait strip.
- Kept the bee start region visible and the ground aligned across ultra-wide and low-height windows.
- Added pure layout and camera tests plus Compose UI coverage for small phones, tablets, low-height landscape, and 200% font scale.
- Added `scripts/run_responsive_audit.sh`, integrated it into production preflight, and documented the supported viewport matrix.
- Bumped Android and iOS build numbers to `8`.

## 1.3.0 — Production hardening

- Centralized application version and Android version code in `gradle.properties` to prevent module version drift and `unspecified` desktop packages.
- Replaced common `collectAsStateWithLifecycle()` usage with composition-scoped `collectAsState()` while retaining lifecycle pause/resume handling.
- Added deferred and immediate persistence durability modes across Android, iOS, Desktop, JS, and Wasm.
- Added a deferred checkpoint after every jump and immediate durable writes for start, restart, Game Over, pause, and disposal.
- Kept transition persistence in `GameController` and lifecycle persistence in `LifecycleResumeEffect` to avoid duplicate durable writes.
- Made primary buttons large-font safe with minimum height rather than a fixed height.
- Added compact-window UI tests at 200% font scale for start and restart actions.
- Made the web locale/direction bootstrap blocking so it runs before the Compose entry point.
- Bound Persian digit rendering to the active Persian resource set instead of treating every RTL locale as Persian.
- Added Android 12+ splash resources aligned with the dark game theme.
- Added deterministic Windows ICO, macOS ICNS, and Linux PNG package icons with structural validation.
- Required an explicit version for manually dispatched releases and validated it against `msa.app.version`.
- Hardened temporary iOS keychain cleanup and removed a duplicate Release artifact path.
- Expanded offline verification for build identity, package prefix, official identity, persistence durability, contrast, web bootstrap ordering, secrets, and release workflow versioning.
- Added independent controller and engine runtime audits plus a unified offline production preflight.
- Added a dependency-light runtime harness that compiles and executes the pure common engine, viewport, snapshot, migration, and controller tests with the local Kotlin toolchain.
- Kept `GameController` UI-host scoped through Koin so Activity/window recreation restores from durable state without leaking mutable state across hosts or tests.

## 1.2.2

- Fixed the Compose Desktop runtime crash caused by a missing `Dispatchers.Main` implementation by adding `kotlinx-coroutines-swing` to `jvmMain`.
- Added `--enable-native-access=ALL-UNNAMED` to Desktop JVM arguments to remove the Skiko restricted-native-access warning on recent JDKs.
- Added a JVM regression test that verifies `Dispatchers.Main` is installed and executes on the Swing event-dispatch thread.
- Bumped Android/iOS build numbers to `6`.

## 1.2.1

### Architecture and state

- Added pure Kotlin deterministic random generator with serializable state.
- Added immutable `GameSnapshot`, checksum-protected schema-v3 codec, strict validation, and schema-v1/schema-v2 migration.
- Added `GameController` with unidirectional `StateFlow` state.
- Added exact continuation of future pipe generation after restoration.
- Added full local restoration on Android, iOS, Desktop, JS, and Wasm.
- Removed Compose-dependent sprite code from the domain layer.
- Kept persisted snapshots compact and used non-blocking `SharedPreferences.apply()` on Android.

### Game engine and performance

- Added fixed 120 Hz nanosecond simulation independent of 30–240 Hz rendering.
- Added logical-world scaling, landscape/resize support, frame-delta clamp, and spiral-of-death protection.
- Corrected collision, score ordering, background/resume timing, and resize behavior.
- Suspended frame callbacks while the game is idle, over, or lifecycle-paused to avoid unnecessary CPU usage.

### UI, localization, and accessibility

- Replaced bitmap gameplay graphics with programmatic Compose artwork.
- Added English/Persian resources and automatic LTR/RTL behavior.
- Added safe-area handling, keyboard/pointer input, focus management, overlay pane semantics, hidden background semantics, score live regions, and large-text-safe overlays.
- Added Reduced Motion integration on Android, iOS, and Web plus a Desktop system property.
- Added adaptive and Android 13 monochrome launcher icons.

### Audio and assets

- Replaced unverifiable artwork and audio with original deterministic project-owned assets.
- Rewrote the asset generator using only the Python standard library.
- Added byte-for-byte isolated regeneration verification and SHA-256 manifest checks.
- Reduced common audio resources to approximately 400 KB.
- Improved target-specific audio lifecycle and ownership.

### Testing and release engineering

- Added engine, codec, migration, exact-continuation, collision, scoring, controller, localization, UI, platform persistence, Android startup, and accessibility tests.
- Added a dependency-free, repeatable `kotlinc` engine audit script.
- Expanded CI across Android, iOS, Linux, Windows, macOS, JS, and Wasm.
- Added minified Desktop release packaging on all host operating systems.
- Added signed Android APK/AAB hooks, signature verification, checksums, dependency reports, and an optional signed iOS archive/IPA workflow.
- Added CodeQL, dependency review, dependency submission, Dependabot, SHA-pinned GitHub Actions, release tag/version validation, and offline security/provenance preflights.
- Added iOS privacy manifest, release checklist, privacy policy, web deployment guide, direct dependency licenses, and reproducible asset provenance.
- Unified release identity at version `1.2.1` and bundle/application ID `com.msa.bee`.
- Aligned Compose Multiplatform 1.10.3 with Material3 1.10.0-alpha05 and Android Compose test artifacts 1.10.5.
- Added Web CSP/referrer hardening, isolated Koin graph testing, iOS x64 framework compilation, and portable signing-secret decoding.

## 1.0.1

- Stabilized portable Gradle wrapper and target startup.
- Added initial fixed-timestep engine and cross-platform CI.
