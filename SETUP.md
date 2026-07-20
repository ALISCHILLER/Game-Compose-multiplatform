# Setup

## Requirements

- JDK 17
- Git
- Python 3.10 or newer; no third-party Python package is required
- Android SDK 36 and a recent Android Studio for Android
- macOS and Xcode for iOS
- Chrome/Chromium for browser test tasks

## Clone and run offline gates

```bash
git clone <repository-url>
cd MSA-Bee
chmod +x gradlew
python scripts/verify_project.py
python scripts/verify_generated_assets.py
python scripts/verify_ui.py
python scripts/verify_release_tag.py v1.7.1
./gradlew --version
./gradlew :composeApp:verifyBuildIdentity
```

The Python verification scripts use the standard library only. `verify_generated_assets.py` regenerates every project-owned icon and sound in an isolated temporary directory and compares byte-for-byte SHA-256 hashes.

## Android

Debug and tests:

```bash
./gradlew \
  :composeApp:assembleDebug \
  :composeApp:lintDebug \
  :composeApp:connectedDebugAndroidTest
```

Unsigned release verification:

```bash
./gradlew \
  :composeApp:lintRelease \
  :composeApp:assembleRelease \
  :composeApp:bundleRelease
```

For signed APK/AAB output, provide all four values through environment variables or CI secrets:

```text
MSA_ANDROID_KEYSTORE_PATH
MSA_ANDROID_STORE_PASSWORD
MSA_ANDROID_KEY_ALIAS
MSA_ANDROID_KEY_PASSWORD
```

The repository intentionally contains no production keystore. A reference variable list is available in `.env.example`; do not commit a populated `.env` file.

Verify signed artifacts:

```bash
apksigner verify --verbose --print-certs app-release.apk
jarsigner -verify -strict -certs app-release.aab
```

## Desktop

Development:

```bash
./gradlew :composeApp:jvmRun :composeApp:jvmTest
```

Minified release package for the current operating system:

```bash
./gradlew :composeApp:packageReleaseDistributionForCurrentOS
```

Native packages must be built on their matching operating systems:

- Linux → DEB
- Windows → MSI
- macOS → DMG

Public Windows distribution requires Authenticode signing. Public macOS distribution requires Developer ID signing and notarization. Those credentials are intentionally external to this repository.

Desktop Reduced Motion can be forced for managed environments or tests:

```bash
-Dmsa.bee.reduceMotion=true
```

## Web

Development:

```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Tests and production artifacts:

```bash
./gradlew \
  :composeApp:jsBrowserTest \
  :composeApp:wasmJsBrowserTest \
  :composeApp:jsBrowserDistribution \
  :composeApp:wasmJsBrowserDistribution
```

Publish JS and Wasm as separate immutable artifacts. Use the JS build as the compatibility fallback when WasmGC support is not guaranteed. See [WEB_DEPLOYMENT.md](WEB_DEPLOYMENT.md).

## iOS

```bash
./gradlew \
  :composeApp:iosSimulatorArm64Test \
  :composeApp:linkDebugFrameworkIosSimulatorArm64 \
  :composeApp:linkReleaseFrameworkIosSimulatorArm64

open iosApp/iosApp.xcodeproj
```

Set `TEAM_ID` locally or in CI. App Store distribution additionally requires a valid signing certificate, provisioning profile, Release archive, export method, privacy answers, and device smoke tests.

The optional `ios-signed-archive` release job accepts these GitHub secrets when `build_ios_archive` is enabled manually:

```text
MSA_IOS_TEAM_ID
MSA_IOS_SIGNING_IDENTITY
MSA_IOS_PROVISIONING_PROFILE_SPECIFIER
MSA_IOS_CERTIFICATE_BASE64
MSA_IOS_CERTIFICATE_PASSWORD
MSA_IOS_PROVISIONING_PROFILE_BASE64
```

Unsigned simulator integration build:

```bash
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

## Regenerate project-owned assets

```bash
python tools/generate_assets.py
python scripts/verify_generated_assets.py
python scripts/verify_ui.py
```

The generator uses only the Python standard library and must reproduce `ASSET_MANIFEST.sha256` exactly.

## Complete offline production preflight

```bash
./scripts/run_production_preflight.sh
```

The preflight also compiles and executes the pure common engine, viewport, snapshot, migration, and controller test suite through `scripts/run_common_pure_tests.sh` without requiring Gradle dependency resolution.

## Comprehensive verification

```bash
python scripts/verify_project.py
python scripts/verify_generated_assets.py
python scripts/verify_ui.py
./gradlew clean \
  :composeApp:verifyBuildIdentity \
  :composeApp:compileKotlinMetadata \
  :composeApp:jvmTest \
  :composeApp:lintRelease \
  :composeApp:assembleDebug \
  :composeApp:assembleRelease \
  :composeApp:bundleRelease \
  :composeApp:jsBrowserTest \
  :composeApp:wasmJsBrowserTest \
  :composeApp:jsBrowserDistribution \
  :composeApp:wasmJsBrowserDistribution
```

Also run the platform-specific iOS, Android emulator, and Desktop packaging gates from [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).

## Troubleshooting

### Wrapper download fails

Confirm DNS and HTTPS access to `services.gradle.org`. Do not replace the portable wrapper URL with a local filesystem path.

### Dependency resolution fails

Confirm access to Google Maven, Maven Central, and the Gradle Plugin Portal.

### Browser tests cannot find Chrome

Install Chrome/Chromium and set the environment expected by Karma on the build agent.

### Audio is unavailable on Linux

Confirm that the system has a Java Sound mixer. The game degrades without crashing when playback is unavailable.

### Browser storage is unavailable

Private browsing or storage policy may reject `localStorage`. The store adapter fails safely; verify the product behavior in every supported browser mode.

## Dependency-free engine audit

With `kotlinc` available:

```bash
./scripts/run_engine_audit.sh
./scripts/run_controller_audit.sh
```

### Desktop reports a previous Gradle PID or loads an older Skiko version

Close Compose Hot Reload and every running MSA Bee window, then on Windows run:

```bat
gradlew.bat --stop
taskkill /F /IM java.exe
rmdir /S /Q .gradle
rmdir /S /Q build
rmdir /S /Q composeApp\build
gradlew.bat :composeApp:verifyBuildIdentity :composeApp:jvmTest :composeApp:jvmRun --no-daemon --stacktrace
```

Use `taskkill /PID <pid> /F` instead of terminating all Java processes when other Java applications must remain running. Confirm the selected runtime graph with:

```bat
gradlew.bat :composeApp:dependencyInsight --dependency kotlinx-coroutines-swing --configuration jvmRuntimeClasspath
gradlew.bat :composeApp:dependencyInsight --dependency skiko-awt --configuration jvmRuntimeClasspath
```

The resolved runtime must contain `kotlinx-coroutines-core` and `kotlinx-coroutines-swing` at the same version and a single selected `skiko-awt` version.


## Settings verification

Run the dependency-free settings audit:

```bash
./scripts/run_settings_audit.sh
```

Then verify on each target that master mute, independent channel volumes, Reduced Motion, gameplay hints, default restoration, and confirmed progress reset survive a relaunch.
