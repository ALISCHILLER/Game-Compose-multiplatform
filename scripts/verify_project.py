#!/usr/bin/env python3
"""Deterministic offline quality gates for the MSA Bee release source tree."""
from __future__ import annotations

from pathlib import Path
import hashlib
import json
import re
import stat
import struct
import sys
import wave
import xml.etree.ElementTree as ET
import zlib

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []
WARNINGS: list[str] = []


def parse_gradle_properties() -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


GRADLE_PROPERTIES = parse_gradle_properties()
EXPECTED_VERSION = GRADLE_PROPERTIES.get("msa.app.version", "")
EXPECTED_ANDROID_VERSION_CODE = GRADLE_PROPERTIES.get("msa.android.versionCode", "")
EXPECTED_APPLICATION_ID = "com.msa.bee"
EXPECTED_IOS_BUILD_NUMBER = EXPECTED_ANDROID_VERSION_CODE
EXPECTED_SNAPSHOT_SCHEMA = "4"
OFFICIAL_EMAIL = "solimaniali90@gmail.com"
OFFICIAL_GITHUB = "https://github.com/ALISCHILLER"
EXPECTED_GRADLE_DISTRIBUTION_SHA256 = "6f74b601422d6d6fc4e1f9a1ab6522f642c2fdcbc15ae33ebd30ba3d7198e854"
EXPECTED_GRADLE_WRAPPER_JAR_SHA256 = "7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172"


def fail(message: str) -> None:
    ERRORS.append(message)


def warn(message: str) -> None:
    WARNINGS.append(message)


def relative(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require_fragment(path: str, fragment: str, label: str | None = None) -> None:
    target = ROOT / path
    if not target.is_file():
        fail(f"Required file missing: {path}")
        return
    if fragment not in target.read_text(encoding="utf-8", errors="replace"):
        fail(f"{label or 'Required content'} missing in {path}: {fragment}")


def validate_png(path: Path) -> None:
    data = path.read_bytes()
    if not data.startswith(b"\x89PNG\r\n\x1a\n"):
        fail(f"Invalid PNG signature: {relative(path)}")
        return

    offset = 8
    saw_ihdr = False
    saw_iend = False
    try:
        while offset < len(data):
            if offset + 12 > len(data):
                raise ValueError("truncated chunk")
            length = struct.unpack(">I", data[offset:offset + 4])[0]
            chunk_type = data[offset + 4:offset + 8]
            chunk_data_start = offset + 8
            chunk_data_end = chunk_data_start + length
            crc_end = chunk_data_end + 4
            if crc_end > len(data):
                raise ValueError("truncated chunk payload")
            expected_crc = struct.unpack(">I", data[chunk_data_end:crc_end])[0]
            actual_crc = zlib.crc32(chunk_type)
            actual_crc = zlib.crc32(data[chunk_data_start:chunk_data_end], actual_crc) & 0xFFFFFFFF
            if expected_crc != actual_crc:
                raise ValueError(f"CRC mismatch in {chunk_type.decode('ascii', errors='replace')}")
            if chunk_type == b"IHDR":
                saw_ihdr = True
                width, height = struct.unpack(">II", data[chunk_data_start:chunk_data_start + 8])
                if width <= 0 or height <= 0:
                    raise ValueError("invalid dimensions")
            if chunk_type == b"IEND":
                saw_iend = True
                break
            offset = crc_end
        if not saw_ihdr or not saw_iend:
            raise ValueError("missing IHDR or IEND")
    except Exception as error:
        fail(f"Invalid PNG: {relative(path)}: {error}")


def validate_ico(path: Path) -> None:
    data = path.read_bytes()
    try:
        if len(data) < 6:
            raise ValueError("truncated header")
        reserved, icon_type, count = struct.unpack("<HHH", data[:6])
        if reserved != 0 or icon_type != 1 or count <= 0:
            raise ValueError("invalid ICONDIR")
        directory_end = 6 + count * 16
        if directory_end > len(data):
            raise ValueError("truncated directory")
        for index in range(count):
            entry = data[6 + index * 16: 6 + (index + 1) * 16]
            _, _, _, _, planes, bit_count, size, offset = struct.unpack("<BBBBHHII", entry)
            if planes != 1 or bit_count != 32 or size <= 0:
                raise ValueError("invalid image entry")
            if offset < directory_end or offset + size > len(data):
                raise ValueError("image payload outside file")
            if not data[offset:offset + size].startswith(b"\x89PNG\r\n\x1a\n"):
                raise ValueError("ICO image is not PNG encoded")
    except Exception as error:
        fail(f"Invalid ICO: {relative(path)}: {error}")


def validate_icns(path: Path) -> None:
    data = path.read_bytes()
    try:
        if len(data) < 8 or data[:4] != b"icns":
            raise ValueError("invalid header")
        declared_size = struct.unpack(">I", data[4:8])[0]
        if declared_size != len(data):
            raise ValueError("declared size mismatch")
        offset = 8
        chunks = 0
        while offset < len(data):
            if offset + 8 > len(data):
                raise ValueError("truncated chunk")
            chunk_size = struct.unpack(">I", data[offset + 4:offset + 8])[0]
            if chunk_size < 8 or offset + chunk_size > len(data):
                raise ValueError("invalid chunk size")
            payload = data[offset + 8:offset + chunk_size]
            if not payload.startswith(b"\x89PNG\r\n\x1a\n"):
                raise ValueError("ICNS image is not PNG encoded")
            chunks += 1
            offset += chunk_size
        if chunks < 4:
            raise ValueError("insufficient icon representations")
    except Exception as error:
        fail(f"Invalid ICNS: {relative(path)}: {error}")


def parse_string_resources(path: Path) -> set[str]:
    root = ET.parse(path).getroot()
    return {
        item.attrib["name"]
        for item in root.findall("string")
        if "name" in item.attrib
    }




def srgb_luminance(rgb: tuple[int, int, int]) -> float:
    channels = []
    for channel in rgb:
        normalized = channel / 255.0
        channels.append(normalized / 12.92 if normalized <= 0.04045 else ((normalized + 0.055) / 1.055) ** 2.4)
    return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2]


def contrast_ratio(first: tuple[int, int, int], second: tuple[int, int, int]) -> float:
    first_luminance = srgb_luminance(first)
    second_luminance = srgb_luminance(second)
    lighter = max(first_luminance, second_luminance)
    darker = min(first_luminance, second_luminance)
    return (lighter + 0.05) / (darker + 0.05)


def parse_argb_color(source: str, symbol: str) -> tuple[int, int, int] | None:
    match = re.search(rf"val\s+{re.escape(symbol)}\s*=\s*Color\(0x(?:[0-9A-Fa-f]{{2}})?([0-9A-Fa-f]{{6}})\)", source)
    if not match:
        return None
    value = match.group(1)
    return tuple(int(value[index:index + 2], 16) for index in (0, 2, 4))


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


for generated_path in ROOT.rglob("*"):
    if generated_path.name == "__pycache__" or generated_path.suffix.lower() in {".pyc", ".pyo"}:
        fail(f"Generated Python cache must not be shipped: {relative(generated_path)}")

ignored_parts = {".git", ".gradle", "build", "node_modules", ".idea", ".kotlin", "__pycache__"}
all_files = [
    path for path in ROOT.rglob("*")
    if path.is_file() and not ignored_parts.intersection(path.parts)
]

# Parse structured resources.
for path in [p for p in all_files if p.suffix.lower() in {".xml", ".xcprivacy", ".plist"}]:
    try:
        ET.parse(path)
    except Exception as error:
        fail(f"Invalid XML: {relative(path)}: {error}")

for path in [p for p in all_files if p.suffix.lower() == ".json"]:
    try:
        json.loads(path.read_text(encoding="utf-8"))
    except Exception as error:
        fail(f"Invalid JSON: {relative(path)}: {error}")

try:
    import yaml  # type: ignore
except ImportError:
    warn("PyYAML is unavailable; full YAML parsing was skipped (basic workflow checks still ran).")
else:
    for path in [p for p in all_files if p.suffix.lower() in {".yml", ".yaml"}]:
        try:
            yaml.safe_load(path.read_text(encoding="utf-8"))
        except Exception as error:
            fail(f"Invalid YAML: {relative(path)}: {error}")

for workflow in (ROOT / ".github/workflows").glob("*.yml"):
    text = workflow.read_text(encoding="utf-8")
    if "\t" in text:
        fail(f"Tab indentation found in workflow: {relative(workflow)}")
    if not re.search(r"(?m)^jobs:\s*$", text):
        fail(f"Workflow has no jobs section: {relative(workflow)}")
    for action_ref in re.findall(r"(?m)^\s*-?\s*uses:\s*([^\s#]+)", text):
        if action_ref.startswith("./") or action_ref.startswith("docker://"):
            continue
        if not re.fullmatch(r"[^@]+@[0-9a-f]{40}", action_ref):
            fail(f"GitHub Action is not pinned to a full commit SHA in {relative(workflow)}: {action_ref}")

# Validate binary assets.
for path in [p for p in all_files if p.suffix.lower() == ".png"]:
    validate_png(path)
for path in [p for p in all_files if p.suffix.lower() == ".ico"]:
    validate_ico(path)
for path in [p for p in all_files if p.suffix.lower() == ".icns"]:
    validate_icns(path)

for path in [p for p in all_files if p.suffix.lower() == ".wav"]:
    try:
        with wave.open(str(path), "rb") as audio:
            if audio.getnchannels() != 1:
                fail(f"WAV is not mono: {relative(path)}")
            if audio.getsampwidth() != 2:
                fail(f"WAV is not 16-bit PCM: {relative(path)}")
            if audio.getframerate() != 22_050:
                fail(f"Unexpected WAV sample rate: {relative(path)}")
            if audio.getnframes() <= 0:
                fail(f"Empty WAV: {relative(path)}")
    except Exception as error:
        fail(f"Invalid WAV: {relative(path)}: {error}")

# Scan source/configuration for local paths, placeholders and likely embedded secrets.
text_extensions = {
    ".kt", ".kts", ".md", ".xml", ".yml", ".yaml", ".toml", ".properties",
    ".html", ".css", ".swift", ".xcconfig", ".plist", ".py", ".example"
}
for path in [p for p in all_files if p.suffix.lower() in text_extensions or p.name == ".env.example"]:
    text = path.read_text(encoding="utf-8", errors="replace")
    if re.search(r"file:///([A-Za-z]:|home/|Users/)", text):
        fail(f"Local filesystem URL found: {relative(path)}")
    if path != Path(__file__).resolve() and (
        "TODO()" in text or "// existing code" in text or "// unchanged code" in text
    ):
        fail(f"Incomplete-code marker found: {relative(path)}")
    has_quoted_secret = (
        re.search(r'(?i)(password|api[_-]?key|secret)\s*[=:]\s*"[^"${}<>]{12,}"', text) is not None
        or re.search(r"(?i)(password|api[_-]?key|secret)\s*[=:]\s*'[^'${}<>]{12,}'", text) is not None
    )
    if has_quoted_secret:
        fail(f"Possible embedded secret found: {relative(path)}")
    if path != Path(__file__).resolve():
        if "-----BEGIN PRIVATE KEY-----" in text or "-----BEGIN RSA PRIVATE KEY-----" in text:
            fail(f"Private key material found: {relative(path)}")
        if re.search(r"\bgh[opusr]_[A-Za-z0-9_]{30,}\b", text):
            fail(f"Possible GitHub token found: {relative(path)}")
        if re.search(r"\bAKIA[0-9A-Z]{16}\b", text):
            fail(f"Possible AWS access key found: {relative(path)}")

required = [
    ".env.example", "README.md", "SETUP.md", "ARCHITECTURE.md", "LICENSE.md",
    "CHANGELOG.md", "PRIVACY.md", "RELEASE_CHECKLIST.md", "WEB_DEPLOYMENT.md",
    "THIRD_PARTY_NOTICES.md", "DEPENDENCY_LICENSES.md", "SECURITY.md", "CONTRIBUTING.md",
    "ASSET_PROVENANCE.md", "ASSET_MANIFEST.sha256", "iosApp/iosApp/PrivacyInfo.xcprivacy",
    "gradlew", "gradlew.bat", "scripts/hash_artifacts.py", "scripts/verify_project.py",
    "scripts/verify_generated_assets.py", "scripts/verify_release_tag.py", "scripts/run_engine_audit.sh",
    "scripts/EngineAudit.kt", "scripts/ControllerAudit.kt", "scripts/run_controller_audit.sh",
    "scripts/run_production_preflight.sh", "scripts/run_common_pure_tests.sh",
    "tools/generate_assets.py",
    "composeApp/src/webMain/resources/bootstrap.js",
    "composeApp/src/androidMain/res/values-v31/themes.xml",
    "composeApp/src/jvmTest/kotlin/com/msa/compose_kmm/di/KoinGraphTest.kt",
    "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties",
    ".github/workflows/ci.yml", ".github/workflows/security.yml",
    ".github/workflows/release.yml", ".github/dependabot.yml",
]
for required_path in required:
    if not (ROOT / required_path).is_file():
        fail(f"Required file missing: {required_path}")

# Official identity and Kotlin package policy.
email_pattern = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
for path in [p for p in all_files if p.suffix.lower() in text_extensions]:
    if path == Path(__file__).resolve():
        continue
    text = path.read_text(encoding="utf-8", errors="replace")
    for email in email_pattern.findall(text):
        if email != OFFICIAL_EMAIL:
            fail(f"Unauthorized email identity found in {relative(path)}: {email}")

for path in [p for p in all_files if p.suffix == ".kt"]:
    text = path.read_text(encoding="utf-8", errors="replace")
    package_match = re.search(r"(?m)^package\s+([A-Za-z0-9_.]+)\s*$", text)
    if not package_match:
        fail(f"Kotlin package declaration missing: {relative(path)}")
    elif not package_match.group(1).startswith("com.msa"):
        fail(f"Kotlin package does not use com.msa prefix: {relative(path)}")

# Portable wrapper and executable bit.
if (ROOT / "gradlew").exists():
    mode = (ROOT / "gradlew").stat().st_mode
    if not mode & stat.S_IXUSR:
        fail("gradlew is not executable")

wrapper_properties = ROOT / "gradle/wrapper/gradle-wrapper.properties"
if wrapper_properties.exists():
    wrapper_text = wrapper_properties.read_text(encoding="utf-8")
    if "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.14.5-bin.zip" not in wrapper_text:
        fail("Gradle wrapper URL is not the expected portable 8.14.5 distribution")
    if f"distributionSha256Sum={EXPECTED_GRADLE_DISTRIBUTION_SHA256}" not in wrapper_text:
        fail("Gradle distribution SHA-256 is missing or does not match the pinned 8.14.5 checksum")
    if "validateDistributionUrl=true" not in wrapper_text:
        fail("Gradle distribution URL validation is not enabled")

wrapper_jar = ROOT / "gradle/wrapper/gradle-wrapper.jar"
if wrapper_jar.is_file() and sha256(wrapper_jar) != EXPECTED_GRADLE_WRAPPER_JAR_SHA256:
    fail("Gradle wrapper JAR SHA-256 does not match the pinned Gradle 8.14.5 wrapper")

# Generated asset provenance and current-tree integrity.
asset_manifest = ROOT / "ASSET_MANIFEST.sha256"
manifest_paths: set[str] = set()
if asset_manifest.exists():
    for line_number, raw_line in enumerate(asset_manifest.read_text(encoding="utf-8").splitlines(), start=1):
        line = raw_line.strip()
        if not line:
            continue
        match = re.fullmatch(r"([0-9a-f]{64})  (.+)", line)
        if not match:
            fail(f"Invalid asset manifest line {line_number}")
            continue
        expected_hash, asset_path = match.groups()
        manifest_paths.add(asset_path)
        path = ROOT / asset_path
        if not path.is_file():
            fail(f"Asset manifest entry is missing: {asset_path}")
            continue
        if sha256(path) != expected_hash:
            fail(f"Asset hash mismatch: {asset_path}")

for filename in ("game_sound.wav", "game_over.wav", "jump.wav", "score.wav"):
    common = ROOT / "composeApp/src/commonMain/composeResources/files" / filename
    android = ROOT / "composeApp/src/androidMain/res/raw" / filename
    if common.is_file() and android.is_file() and common.read_bytes() != android.read_bytes():
        fail(f"Android and common audio differ: {filename}")

asset_generator = ROOT / "tools/generate_assets.py"
if asset_generator.is_file():
    generator_text = asset_generator.read_text(encoding="utf-8")
    if re.search(r"(?m)^\s*(?:from\s+PIL\s+import|import\s+PIL(?:\s|$))", generator_text):
        fail("Asset generator must use only the Python standard library")
    if "MSA_BEE_ASSET_ROOT" not in generator_text:
        fail("Asset generator does not support isolated reproducibility verification")

# Compose/Android resource parity.
common_default = ROOT / "composeApp/src/commonMain/composeResources/values/strings.xml"
common_fa = ROOT / "composeApp/src/commonMain/composeResources/values-fa/strings.xml"
if common_default.is_file() and common_fa.is_file():
    default_keys = parse_string_resources(common_default)
    fa_keys = parse_string_resources(common_fa)
    if default_keys != fa_keys:
        fail(f"Common string resource keys differ: missing-fa={sorted(default_keys-fa_keys)}, missing-default={sorted(fa_keys-default_keys)}")
    source_text = "\n".join(
        path.read_text(encoding="utf-8", errors="replace")
        for path in all_files if path.suffix == ".kt"
    )
    referenced_keys = set(re.findall(r"Res\.string\.([A-Za-z0-9_]+)", source_text))
    missing_keys = referenced_keys - default_keys
    if missing_keys:
        fail(f"Missing Compose string resources: {sorted(missing_keys)}")

android_default = ROOT / "composeApp/src/androidMain/res/values/strings.xml"
android_fa = ROOT / "composeApp/src/androidMain/res/values-fa/strings.xml"
if android_default.is_file() and android_fa.is_file():
    if parse_string_resources(android_default) != parse_string_resources(android_fa):
        fail("Android English/Persian string keys differ")

locale_config = ROOT / "composeApp/src/androidMain/res/xml/locales_config.xml"
if locale_config.is_file():
    locale_text = locale_config.read_text(encoding="utf-8")
    for locale in ('android:name="en"', 'android:name="fa"'):
        if locale not in locale_text:
            fail(f"Android locale configuration missing: {locale}")

# Version coherence and release identity.
root_build = read("build.gradle.kts") if (ROOT / "build.gradle.kts").is_file() else ""
compose_build = read("composeApp/build.gradle.kts") if (ROOT / "composeApp/build.gradle.kts").is_file() else ""
xcconfig_text = read("iosApp/Configuration/Config.xcconfig") if (ROOT / "iosApp/Configuration/Config.xcconfig").is_file() else ""
if not re.fullmatch(r"\d+\.\d+\.\d+", EXPECTED_VERSION):
    fail(f"msa.app.version must use MAJOR.MINOR.PATCH: {EXPECTED_VERSION!r}")
if not EXPECTED_ANDROID_VERSION_CODE.isdigit() or int(EXPECTED_ANDROID_VERSION_CODE) <= 0:
    fail(f"msa.android.versionCode must be a positive integer: {EXPECTED_ANDROID_VERSION_CODE!r}")
for label, pattern, text, expected in (
    ("iOS MARKETING_VERSION", r'MARKETING_VERSION\s*=\s*([^\s]+)', xcconfig_text, EXPECTED_VERSION),
    ("iOS CURRENT_PROJECT_VERSION", r'CURRENT_PROJECT_VERSION\s*=\s*([^\s]+)', xcconfig_text, EXPECTED_IOS_BUILD_NUMBER),
):
    match = re.search(pattern, text)
    if not match or match.group(1) != expected:
        fail(f"Version mismatch for {label}: expected {expected}")

for fragment, message in (
    ('version = providers.gradleProperty("msa.app.version").get()', "Root project version is not sourced from gradle.properties"),
    ('val appVersion = providers.gradleProperty("msa.app.version").get()', "composeApp appVersion is not centralized"),
    ('val androidVersionCode = providers.gradleProperty("msa.android.versionCode").get().toInt()', "Android versionCode is not centralized"),
    ('version = appVersion', "composeApp project.version does not use appVersion"),
    ('versionCode = androidVersionCode', "Android versionCode does not use the centralized value"),
    ('versionName = appVersion', "Android versionName does not use appVersion"),
    ('packageVersion = appVersion', "Desktop packageVersion does not use appVersion"),
    ('implementation(libs.kotlinx.coroutines.swing)', "Desktop source set is missing kotlinx-coroutines-swing"),
    ('jvmArgs("--enable-native-access=ALL-UNNAMED")', "Desktop native access flag is missing"),
):
    target = root_build if fragment.startswith('version = providers') else compose_build
    if fragment not in target:
        fail(message)

if f'applicationId = "{EXPECTED_APPLICATION_ID}"' not in compose_build:
    fail(f"Android applicationId must be {EXPECTED_APPLICATION_ID}")
if f'bundleID = "{EXPECTED_APPLICATION_ID}"' not in compose_build:
    fail(f"Desktop macOS bundle ID must be {EXPECTED_APPLICATION_ID}")
for icon_path in (
    "src/jvmMain/resources/icons/msa-bee.icns",
    "src/jvmMain/resources/icons/msa-bee.ico",
    "src/jvmMain/resources/icons/msa-bee.png",
):
    if icon_path not in compose_build:
        fail(f"Desktop package icon is not configured: {icon_path}")
if "python3 scripts/verify_ui.py" not in read("scripts/run_production_preflight.sh"):
    fail("Offline production preflight does not execute the UI quality verification")
if "./scripts/run_common_pure_tests.sh" not in read("scripts/run_production_preflight.sh"):
    fail("Offline production preflight does not execute the common pure-test runtime suite")
if "./scripts/run_responsive_audit.sh" not in read("scripts/run_production_preflight.sh"):
    fail("Offline production preflight does not execute the responsive layout audit")
responsive_layout = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/ResponsiveLayout.kt")
for fragment in (
    "CompactPortrait",
    "CompactLandscape",
    "MediumPortrait",
    "MediumLandscape",
    "Expanded",
    "useHorizontalOverlay",
    "useCompactHud",
    "stackControlHints",
    "mascotSizeDp",
    "actionMaxWidthDp",
):
    if fragment not in responsive_layout:
        fail(f"Responsive layout policy is missing: {fragment}")
for required_ui_file in (
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameUiComponents.kt",
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/ScoreFeedback.kt",
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/SettingsOverlay.kt",
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameplayHintBanner.kt",
    "UI_SYSTEM.md",
):
    if not (ROOT / required_ui_file).is_file():
        fail(f"Required UI system file is missing: {required_ui_file}")

responsive_tests = read("composeApp/src/commonTest/kotlin/com/msa/compose_kmm/ComposeUiTest.kt")
for fragment in ("800.dp, height = 320.dp", "1024.dp, height = 420.dp", "fontScale = 2f"):
    if fragment not in responsive_tests:
        fail(f"Responsive Compose UI coverage is missing: {fragment}")
if "packageReleaseDistributionForCurrentOS" not in read(".github/workflows/ci.yml"):
    fail("CI does not build minified Desktop release packages")
if "packageReleaseDistributionForCurrentOS" not in read(".github/workflows/release.yml"):
    fail("Release workflow does not build minified Desktop release packages")
if "release_version:" not in read(".github/workflows/release.yml"):
    fail("Manual release workflow does not require an explicit version")
if 'verify_release_tag.py "v${RELEASE_VERSION}"' not in read(".github/workflows/release.yml"):
    fail("Manual release version is not validated against msa.app.version")

app_text = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/App.kt")
if "collectAsStateWithLifecycle" in app_text:
    fail("commonMain still uses collectAsStateWithLifecycle and can require a missing platform Main dispatcher")
if "controller.state.collectAsState()" not in app_text:
    fail("commonMain does not collect StateFlow with composition-scoped collectAsState")
if "settingsController.state.collectAsState()" not in app_text:
    fail("commonMain does not collect persisted game settings")
if "SettingsOverlay(" not in app_text:
    fail("Settings overlay is not integrated into the application root")
if "audioPlayer.setMusicVolume" not in app_text or "audioPlayer.setEffectsVolume" not in app_text:
    fail("Persisted audio volumes are not applied to the platform audio player")
if "SaveDurability.Immediate" not in app_text:
    fail("Lifecycle disposal does not request immediate state persistence")
di_source = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/di/KoinModule.kt")
if "factory { GameController(store = get()) }" not in di_source:
    fail("GameController must be UI-host scoped as a Koin factory")
if "single { SettingsController(store = get()) }" not in di_source:
    fail("SettingsController must be registered as a process-level singleton")

store_contract = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameStateStore.kt")
if "enum class SaveDurability" not in store_contract:
    fail("Persistence durability contract is missing")

settings_contract = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSettingsStore.kt")
if "interface GameSettingsStore" not in settings_contract:
    fail("Settings persistence contract is missing")
for settings_store in (
    "composeApp/src/androidMain/kotlin/com/msa/compose_kmm/data/AndroidGameSettingsStore.kt",
    "composeApp/src/iosMain/kotlin/com/msa/compose_kmm/data/IosGameSettingsStore.kt",
    "composeApp/src/jvmMain/kotlin/com/msa/compose_kmm/data/DesktopGameSettingsStore.kt",
    "composeApp/src/webMain/kotlin/com/msa/compose_kmm/data/BrowserGameSettingsStore.kt",
):
    if not (ROOT / settings_store).is_file():
        fail(f"Platform settings store is missing: {settings_store}")

audio_contract = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/AudioPlayer.kt")
for audio_api in ("setMusicVolume", "setEffectsVolume", "playScoreSound"):
    if audio_api not in audio_contract:
        fail(f"Audio settings API is missing: {audio_api}")

for documentation in ("README.md", "LICENSE.md", "SECURITY.md"):
    text = read(documentation)
    if OFFICIAL_EMAIL not in text:
        fail(f"Official email missing from {documentation}")
    if OFFICIAL_GITHUB not in text:
        fail(f"Official GitHub identity missing from {documentation}")

# Version catalog alignment for Compose Multiplatform 1.10.3.
catalog = read("gradle/libs.versions.toml") if (ROOT / "gradle/libs.versions.toml").is_file() else ""
for required_version in (
    'composeMultiplatform = "1.10.3"',
    'composeMaterial3 = "1.10.0-alpha05"',
    'jetpackCompose = "1.10.5"',
    'kotlinx-coroutines = "1.10.2"',
):
    if required_version not in catalog:
        fail(f"Compose dependency alignment missing: {required_version}")
if 'kotlinx-coroutines-swing = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-swing", version.ref = "kotlinx-coroutines" }' not in catalog:
    fail("Version catalog is missing the Swing Main dispatcher artifact")

# Web hardening: no inline script and a restrictive baseline CSP.
web_index = read("composeApp/src/webMain/resources/index.html") if (ROOT / "composeApp/src/webMain/resources/index.html").is_file() else ""
if "Content-Security-Policy" not in web_index or "object-src 'none'" not in web_index:
    fail("Web Content Security Policy is missing or incomplete")
if re.search(r"<script(?:\s[^>]*)?>\s*[^<\s]", web_index, re.I):
    fail("Inline JavaScript remains in index.html")
if 'src="bootstrap.js"' not in web_index:
    fail("External language bootstrap script is not referenced")
if 'defer src="bootstrap.js"' in web_index or 'async src="bootstrap.js"' in web_index:
    fail("Language bootstrap must execute before the Compose entry point")
if web_index.find('src="bootstrap.js"') > web_index.find('src="composeApp.js"'):
    fail("Language bootstrap is ordered after the Compose entry point")

# Locale-specific numbering must be driven by resources, not generic RTL direction.
localized_number_source = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/LocalizedNumber.kt")
if "LocalLayoutDirection" in localized_number_source:
    fail("Digit localization is incorrectly coupled to generic layout direction")
require_fragment(
    "composeApp/src/commonMain/composeResources/values/strings.xml",
    '<string name="use_persian_digits">false</string>',
    "Default Latin numbering resource",
)
require_fragment(
    "composeApp/src/commonMain/composeResources/values-fa/strings.xml",
    '<string name="use_persian_digits">true</string>',
    "Persian numbering resource",
)

# Accessibility contrast and scalable controls.
color_source = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/Color.kt")
white = parse_argb_color(color_source, "GameTextWhite")
for background_symbol in ("GamePrimaryOrange", "GameLeafGreen"):
    background = parse_argb_color(color_source, background_symbol)
    if white is None or background is None:
        fail(f"Unable to parse accessibility colors for {background_symbol}")
    elif contrast_ratio(white, background) < 4.5:
        fail(f"Text contrast is below WCAG AA for {background_symbol}")
ui_component_text = read("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameUiComponents.kt")
for overlay in ("StartOverlay.kt", "GameOverOverlay.kt"):
    overlay_text = read(f"composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/{overlay}")
    if ".height(58.dp)" in overlay_text or ".height(58.dp)" in ui_component_text:
        fail(f"Fixed-height primary action can clip large text: {overlay}")
if ".heightIn(min = 58.dp)" not in ui_component_text:
    fail("Large-text-safe minimum height is missing from the shared primary button")
require_fragment(
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameUiComponents.kt",
    "import androidx.compose.ui.graphics.graphicsLayer",
    "Bee emblem graphicsLayer import",
)
require_fragment(
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/SettingsOverlay.kt",
    "val availableWidth = maxWidth",
    "Settings BoxWithConstraints explicit width capture",
)
require_fragment(
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/SettingsOverlay.kt",
    "spec.useHorizontalOverlay && availableWidth >= 680.dp",
    "Settings explicit responsive width receiver",
)

# Android hardening and adaptive/monochrome icons.
manifest_path = ROOT / "composeApp/src/androidMain/AndroidManifest.xml"
if manifest_path.exists():
    manifest_text = manifest_path.read_text(encoding="utf-8")
    for required_fragment in (
        'android:name=".MyApplication"',
        'android:allowBackup="false"',
        'android:usesCleartextTraffic="false"',
        'android:exported="true"',
        'android:localeConfig="@xml/locales_config"',
        'android:supportsRtl="true"',
    ):
        if required_fragment not in manifest_text:
            fail(f"Android manifest hardening missing: {required_fragment}")
    if "<uses-permission" in manifest_text:
        fail("Android application unexpectedly requests a permission")

for icon_path in (
    "composeApp/src/androidMain/res/drawable/ic_launcher_monochrome.xml",
    "composeApp/src/androidMain/res/mipmap-anydpi-v33/ic_launcher.xml",
    "composeApp/src/androidMain/res/mipmap-anydpi-v33/ic_launcher_round.xml",
):
    if not (ROOT / icon_path).is_file():
        fail(f"Android monochrome/adaptive icon missing: {icon_path}")

# iOS identity, deployment target and privacy manifest.
if xcconfig_text:
    bundle_match = re.search(r"PRODUCT_BUNDLE_IDENTIFIER\s*=\s*([^\s]+)", xcconfig_text)
    if not bundle_match or bundle_match.group(1) != EXPECTED_APPLICATION_ID:
        fail(f"iOS bundle identifier must be {EXPECTED_APPLICATION_ID}")

pbxproj = read("iosApp/iosApp.xcodeproj/project.pbxproj") if (ROOT / "iosApp/iosApp.xcodeproj/project.pbxproj").is_file() else ""
if "IPHONEOS_DEPLOYMENT_TARGET = 16.0;" not in pbxproj:
    fail("iOS deployment target is not consistently set to 16.0")
require_fragment("iosApp/iosApp/PrivacyInfo.xcprivacy", "NSPrivacyTracking", "iOS privacy declaration")
require_fragment(
    "iosApp/iosApp/PrivacyInfo.xcprivacy",
    "NSPrivacyAccessedAPICategoryUserDefaults",
    "iOS UserDefaults required-reason category",
)
require_fragment(
    "iosApp/iosApp/PrivacyInfo.xcprivacy",
    "CA92.1",
    "iOS app-only UserDefaults required reason",
)
require_fragment("iosApp/iosApp/Info.plist", "<string>en</string>", "iOS English localization")
require_fragment("iosApp/iosApp/Info.plist", "<string>fa</string>", "iOS Persian localization")
if "PrivacyInfo.xcprivacy" in re.findall(r"membershipExceptions = \((.*?)\);", pbxproj, re.S):
    fail("iOS privacy manifest is excluded from synchronized target membership")

# State, deterministic continuation and accessibility architecture.
require_fragment(
    "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt",
    f"CURRENT_SCHEMA_VERSION: Int = {EXPECTED_SNAPSHOT_SCHEMA}",
    "Snapshot schema",
)
for path, fragment, label in (
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt", "val randomState: Long", "Random state persistence"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt", "val roundStartBestScore: Int", "Round baseline persistence"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshot.kt", "val isNewRecord: Boolean", "Restorable new-record state"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshotCodec.kt", "LEGACY_SCHEMA_VERSION_3", "Snapshot v3 migration"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/GameSnapshotCodec.kt", "checksum", "Snapshot integrity checksum"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/GameController.kt", "StateFlow<GameSnapshot>", "Immutable state flow"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/presentation/GameController.kt", "durability = SaveDurability.Immediate", "Immediate score checkpoint"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/App.kt", "controller.persist(SaveDurability.Immediate)", "Dispose/lifecycle persistence"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/App.kt", "prefersReducedMotion()", "Reduced motion integration"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/ScoreFeedback.kt", "LiveRegionMode.Polite", "Single score feedback live region"),
    ("composeApp/src/commonMain/kotlin/com/msa/compose_kmm/ui/GameCanvas.kt", "clearAndSetSemantics", "Hidden canvas semantics behind overlays"),
):
    require_fragment(path, fragment, label)

# No obsolete bitmap gameplay resources or Compose-dependent domain sprite package.
for forbidden in (
    ROOT / "composeApp/src/commonMain/composeResources/drawable",
    ROOT / "composeApp/src/commonMain/kotlin/com/msa/compose_kmm/domain/sprite",
):
    if forbidden.exists():
        fail(f"Obsolete or unverified asset/code directory remains: {relative(forbidden)}")

# CI gates must exercise all declared targets and security checks.
ci_text = read(".github/workflows/ci.yml") if (ROOT / ".github/workflows/ci.yml").is_file() else ""
release_text = read(".github/workflows/release.yml") if (ROOT / ".github/workflows/release.yml").is_file() else ""
security_text = read(".github/workflows/security.yml") if (ROOT / ".github/workflows/security.yml").is_file() else ""
for fragment in (
    ":composeApp:connectedDebugAndroidTest",
    ":composeApp:iosSimulatorArm64Test",
    ":composeApp:linkDebugFrameworkIosX64",
    ":composeApp:jsBrowserTest",
    ":composeApp:wasmJsBrowserTest",
    "packageReleaseDistributionForCurrentOS",
    "scripts/verify_generated_assets.py",
    "scripts/verify_ui.py",
):
    if fragment not in ci_text:
        fail(f"CI coverage missing: {fragment}")
for fragment in (
    ":composeApp:bundleRelease",
    '"$APKSIGNER" verify',
    "jarsigner -verify",
    "packageReleaseDistributionForCurrentOS",
    "ios-signed-archive:",
    "-exportArchive",
    "Remove temporary iOS signing material",
    "Remove temporary Android signing material",
    "scripts/verify_generated_assets.py",
    "scripts/verify_ui.py",
    "scripts/verify_release_tag.py",
    "--suffix .deb",
    "--suffix .msi",
    "--suffix .dmg",
):
    if fragment not in release_text:
        fail(f"Release gate missing: {fragment}")
for fragment in (
    "dependency-review-action",
    "codeql-action",
    "scripts/verify_generated_assets.py",
    "scripts/verify_ui.py",
):
    if fragment not in security_text:
        fail(f"Security workflow gate missing: {fragment}")

for workflow_name, workflow_text in (("CI", ci_text), ("Release", release_text)):
    if "composeApp/build/compose/binaries/main/**" in workflow_text:
        fail(f"{workflow_name} workflow still uploads the non-release Desktop path")
    for suffix in ("**/*.deb", "**/*.msi", "**/*.dmg"):
        if suffix not in workflow_text:
            fail(f"{workflow_name} Desktop artifact path missing: {suffix}")

if ERRORS:
    print("Verification failed:")
    for error in ERRORS:
        print(f"- {error}")
    if WARNINGS:
        print("Warnings:")
        for warning in WARNINGS:
            print(f"- {warning}")
    sys.exit(1)

print("offline-structure-verification-ok")
print(f"verified-files={len(all_files)}")
print(f"verified-assets={len(manifest_paths)}")
if WARNINGS:
    for warning in WARNINGS:
        print(f"warning: {warning}")
