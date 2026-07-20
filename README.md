# MSA Bee

MSA Bee is an offline cross-platform arcade game built with Kotlin Multiplatform and Compose Multiplatform.

## Targets

- Android
- iOS x64 Simulator, Arm64, and iOS Simulator Arm64
- Desktop JVM: Windows, macOS, Linux
- JavaScript Browser
- WebAssembly Browser

## Highlights

- Pure Kotlin deterministic game engine
- Fixed logical world of `360 × 640` with an adaptive, non-stretching landscape camera
- Fixed 120 Hz simulation independent of display refresh rate
- Immutable, versioned `GameSnapshot` state exposed through `StateFlow`
- Deterministic continuation from the latest persisted checkpoint, including future pipe generation
- Cross-platform checkpoint storage for Android recreation/relaunch, iOS relaunch, desktop restart, and browser refresh
- Touch, mouse, Space, Enter, and Arrow Up input
- English and Persian resources with automatic LTR/RTL behavior
- Full phone, tablet, foldable, desktop-resize, and low-height landscape layouts
- Cohesive Material 3 visual system with programmatic mascot art, polished results, layered scenery, and responsive control hints
- Reduced-Motion-aware score pulse, a single accessible localized +1 announcement, and an explicit round-based new-record celebration
- Safe-area support, keyboard focus, live-region score announcements, and overlay semantics
- System Reduced Motion support on Android, iOS, and Web, plus a documented Desktop property
- Persistent in-game settings for master sound, separate music/effects volume, Reduced Motion, gameplay hints, and local-progress reset
- Platform-specific audio adapters with lifecycle-safe ownership and a dedicated score chime
- Programmatic gameplay artwork and reproducible project-owned launcher icons/audio
- Common engine, persistence, UI, Android startup, and accessibility tests
- CI for Android, iOS, Linux, Windows, macOS, JavaScript, and Wasm
- Security, dependency-review, CodeQL, dependency-submission, SHA-pinned actions, release tag validation, and signed Android release workflows

## Architecture

```text
Pointer / keyboard input
          │
          ▼
Compose UI ───────────────┐
          │ actions       │ immutable StateFlow<GameSnapshot>
          ▼               │
GameController ───────────┘
          │
          ├── Game (pure Kotlin deterministic engine)
          ├── GameStateStore
          └── SettingsController / GameSettingsStore
                  ├── Android SharedPreferences
                  ├── iOS NSUserDefaults
                  ├── Desktop java.util.prefs
                  └── Browser localStorage
```

See [ARCHITECTURE.md](ARCHITECTURE.md), [RESPONSIVE_MATRIX.md](RESPONSIVE_MATRIX.md), [UI_SYSTEM.md](UI_SYSTEM.md), and [SETTINGS.md](SETTINGS.md).

## Quick start

Requirements: JDK 17, Android SDK 36 for Android, and Xcode on macOS for iOS.

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:jvmRun
./gradlew :composeApp:jsBrowserDevelopmentRun
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Full setup, signing, platform verification, and troubleshooting are documented in [SETUP.md](SETUP.md).

## Verification

Complete offline preflight:

```bash
./scripts/run_production_preflight.sh
```

Individual offline gates:

```bash
python scripts/verify_project.py
python scripts/verify_generated_assets.py
python scripts/verify_ui.py
./scripts/run_engine_audit.sh
./scripts/run_controller_audit.sh
./scripts/run_settings_audit.sh
./scripts/run_common_pure_tests.sh
./scripts/run_responsive_audit.sh
```

Primary Gradle gates:

```bash
./gradlew \
  :composeApp:verifyBuildIdentity \
  :composeApp:compileKotlinMetadata \
  :composeApp:jvmTest \
  :composeApp:lintRelease \
  :composeApp:assembleDebug \
  :composeApp:assembleRelease \
  :composeApp:bundleRelease
```

The pure Kotlin engine, schema-v1/schema-v2/schema-v3 migration, checksum-protected schema-v4 codec, persisted round-start record baseline, deterministic future random continuation from a persisted checkpoint, collision logic, scoring, and frame-rate independence have been compiled and executed independently. Full Gradle, device, browser, signing, and store verification must still pass in the intended release environments before declaring a target production-ready.

Use [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) as the release gate. The evidence completed in this delivery environment is recorded in [VERIFICATION_STATUS.md](VERIFICATION_STATUS.md).

## Assets and licensing

Gameplay graphics are rendered in Compose. Launcher icons and PCM audio are generated deterministically with Python standard-library code in `tools/generate_assets.py`.

See:

- [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md)
- [ASSET_MANIFEST.sha256](ASSET_MANIFEST.sha256)
- [DEPENDENCY_LICENSES.md](DEPENDENCY_LICENSES.md)
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)
- [LICENSE.md](LICENSE.md)

## Security and privacy

The application has no backend, analytics, advertising, authentication, or remote telemetry. Local storage contains only the game snapshot and user-selected game settings.

See [SECURITY.md](SECURITY.md) and [PRIVACY.md](PRIVACY.md).

## Web deployment

JS and Wasm are separate production artifacts. Deployment requirements and browser fallback expectations are in [WEB_DEPLOYMENT.md](WEB_DEPLOYMENT.md).

## Current release status

```text
Version: 1.7.1
Status: Production candidate
Required before production declaration: successful target builds, runtime smoke tests, signing, and release checklist evidence
```

## Author

Developed and maintained by  
[ALISCHILLER](https://github.com/ALISCHILLER) — **MSA**

Email: [solimaniali90@gmail.com](mailto:solimaniali90@gmail.com)
