# Release Checklist

A release is approved only when every applicable gate below is evidenced by a successful command, CI run, or signed-store artifact. A checked source file is not a substitute for a successful build or runtime test.

## 1. Source and offline integrity

```bash
./scripts/run_production_preflight.sh

# Or run the individual gates:
python scripts/verify_project.py
python scripts/verify_generated_assets.py
python scripts/verify_ui.py
python scripts/verify_release_tag.py v1.7.1
./gradlew :composeApp:verifyBuildIdentity
```

- [ ] No local filesystem URL, placeholder code, or embedded secret is reported.
- [ ] Generated icons and audio match `ASSET_MANIFEST.sha256`.
- [ ] The release tag matches the Android, iOS, Desktop, and root project versions.
- [ ] `CHANGELOG.md`, `PRIVACY.md`, and dependency notices describe the release.

## 2. Common engine and tests

```bash
./gradlew clean \
  :composeApp:compileKotlinMetadata \
  :composeApp:jvmTest
```

- [ ] Engine, snapshot schema migration, deterministic random continuation from the persisted checkpoint, collision, scoring, restoration, localization, and common UI tests pass.

## 3. Android

```bash
./gradlew \
  :composeApp:lintRelease \
  :composeApp:connectedDebugAndroidTest \
  :composeApp:assembleRelease \
  :composeApp:bundleRelease
```

- [ ] Real `MainActivity` startup, Koin, primary interaction, and accessibility checks pass on an emulator.
- [ ] Signed APK passes `apksigner verify --verbose --print-certs`.
- [ ] Signed AAB passes `jarsigner -verify -strict -certs`.
- [ ] Cold start, rotation, background/foreground restoration, TalkBack, Persian RTL, English LTR, large font, reduced motion, and audio focus are manually smoke-tested.

## 4. iOS

```bash
./gradlew \
  :composeApp:iosSimulatorArm64Test \
  :composeApp:linkReleaseFrameworkIosSimulatorArm64

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO \
  TEAM_ID= \
  build
```

- [ ] Simulator tests and Release framework linking pass.
- [ ] A signed device archive and exported IPA are produced with the correct Apple team and provisioning profile. The optional `ios-signed-archive` workflow may be used after its six signing secrets are configured.
- [ ] VoiceOver, Dynamic Type, reduced motion, interruptions, route changes, relaunch restoration, and the privacy manifest are verified.

## 5. Desktop

Run on each matching operating system:

```bash
./gradlew :composeApp:jvmTest :composeApp:packageReleaseDistributionForCurrentOS
```

- [ ] Linux DEB, Windows MSI, and macOS DMG are built on their own operating systems.
- [ ] Packaged applications launch without a developer JDK.
- [ ] Keyboard focus, sound mixer availability, persistence, RTL, scaling, and window resizing are smoke-tested.
- [ ] Public Windows and macOS installers are code-signed; macOS artifacts are notarized before distribution.

## 6. Web

```bash
./gradlew \
  :composeApp:jsBrowserTest \
  :composeApp:wasmJsBrowserTest \
  :composeApp:jsBrowserDistribution \
  :composeApp:wasmJsBrowserDistribution
```

- [ ] JS fallback and Wasm distributions are deployed as separate immutable artifacts.
- [ ] Current Chrome, Firefox, Safari, Android browser, and iOS Safari are manually tested.
- [ ] Touch, keyboard, audio autoplay, refresh restoration, reduced motion, RTL/LTR, and cache invalidation are verified.

See `WEB_DEPLOYMENT.md` for deployment requirements.

## 7. Security, licenses, and provenance

- [ ] CodeQL, dependency review, Dependabot, and dependency submission workflows are green.
- [ ] All third-party GitHub Actions remain pinned to reviewed full commit SHAs.
- [ ] Release tag passes `python scripts/verify_release_tag.py v1.7.1`.
- [ ] Gradle dependency verification metadata and lock state are generated/reviewed in a trusted connected environment before a public release.
- [ ] The resolved Gradle dependency report is archived with the release.
- [ ] Direct and transitive dependency licenses are reviewed and notices are preserved.
- [ ] No signing key, provisioning profile, password, token, or `.env` file is included in an artifact.
- [ ] Checksums are generated and published for every distributable.

## 8. Production declaration

Only after all applicable gates pass may the release be called **Production-ready for the verified targets**. Until then, use one of these exact statuses:

- `Production candidate`
- `Build verification required`
- `Partially verified`
- `Not production-ready`

## Responsive and accessibility matrix

- [ ] `./scripts/run_responsive_audit.sh` and `python scripts/verify_ui.py` pass.
- [ ] Small phone portrait and low-height phone landscape smoke tests pass.
- [ ] Tablet/foldable portrait and landscape smoke tests pass.
- [ ] Start and restart actions remain reachable at 200% font scale.
- [ ] HUD remains readable without covering the active bee region.
- [ ] Start, gameplay, ordinary Game Over, and new-record Game Over are visually smoke-tested in English and Persian.
- [ ] Score pulse and +1 feedback are verified with Reduced Motion both enabled and disabled.
- [ ] Safe areas, display cutouts, keyboard focus, and window resizing are verified.
- [ ] Evidence is recorded against `RESPONSIVE_MATRIX.md`.

## Settings and audio

- [ ] Settings persistence survives app restart on every released target.
- [ ] Master mute, music/effects toggles, and both volume sliders affect the correct audio channel.
- [ ] Opening settings pauses an active flight and closing it resumes without a physics jump.
- [ ] Score, jump, and game-over effects respect the effects channel and selected volume.
- [ ] System Reduced Motion overrides the in-app preference where applicable.
- [ ] Reset progress requires confirmation and clears only progress, not user settings.
- [ ] Restore defaults changes only settings and preserves the best score.
