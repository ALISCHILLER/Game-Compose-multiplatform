# Verification Status

Date: 2026-07-18  
Release: 1.7.1

## Completed in the delivery environment

- Offline structure, resource, secret, version, manifest, workflow, and asset-provenance verification
- Deterministic regeneration and SHA-256 comparison of all generated icons and four audio cues, including the score chime
- Reproducible pure Kotlin game-engine compilation and runtime audit via `scripts/run_engine_audit.sh`
- Controller runtime audit covering StateFlow restoration and immediate persistence boundaries
- Settings runtime audit covering checksum round-trip, persisted channel controls, master mute, and default restoration
- Runtime execution of the pure common engine, viewport, snapshot, migration, gameplay controller, settings codec, and settings controller test suite via `scripts/run_common_pure_tests.sh`
- Responsive layout audit across eight phone/tablet/desktop viewport classes and four low-height landscape cameras
- Fixed-timestep equivalence at 30, 60, 90, 120, 144, 165, and 240 Hz
- Snapshot schema-v4 checksum round trip and schema-v1/schema-v2/schema-v3 migration
- Deterministic future random/pipe continuation from a persisted checkpoint
- Collision, score, best-score, restored round-baseline, viewport, and malformed-snapshot checks
- Desktop audio, state-store/codec, and reduced-motion adapter compilation
- SHA-pinned workflow and release tag/version validation
- Web CSP and external bootstrap validation
- Centralized Gradle property validation for Android, iOS, Desktop, and release tags
- Deferred/immediate persistence contract, immediate score durability, disposal persistence, and critical-transition tests
- Large-text-safe primary controls and automated color-contrast verification
- UI token, localized-string parity, safe-area, scroll-safety, scalable-button, and required-component verification via `scripts/verify_ui.py`
- Locale-resource-driven Persian digit selection, avoiding false Persian numbering for unrelated RTL locales
- Deterministic and structurally validated Windows ICO, macOS ICNS, and Linux PNG package icons
- Desktop release installer discovery/checksum hardening for MSI, DMG, and DEB outputs
- Suffix-filtered artifact hashing verified with a deterministic installer fixture
- Archive checksum and extracted-tree verification

## Implemented and statically reviewed, but not executed here

- Compose UI tests for primary actions, settings controls, saved-best presentation, explicit new-record and tied-record states, compact HUD, tablet, low-height landscape, and 200% font scale
- Android instrumented startup and accessibility tests
- Target-specific Gradle tests for Android, iOS, Desktop, JS, and Wasm
- Full Compose compilation of the redesigned UI

## Blocked in the delivery environment

The Gradle Wrapper could not download Gradle because the sandbox DNS could not resolve `services.gradle.org`:

```text
UnknownHostException: services.gradle.org
```

This prevents claiming successful full Gradle compilation in this environment. It is not evidence of a source-code build failure.

## Required external release evidence

Before calling any target production-ready, complete the applicable gates in `RELEASE_CHECKLIST.md`, including:

- Full Gradle metadata, test, lint, Android, Desktop, JS/Wasm, and iOS tasks
- Android emulator/device and accessibility smoke tests
- iOS simulator/device, VoiceOver, interruption, signing, archive, and IPA verification
- Windows, macOS, and Linux packaged runtime tests and signing/notarization where applicable
- Browser matrix tests for JS and Wasm, including fallback and audio-autoplay behavior
- Signed store artifacts, dependency/license reports, published checksums, and green security workflows

## Current classification

```text
Production candidate — Build verification required
```
