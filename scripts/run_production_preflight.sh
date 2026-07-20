#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

VERSION="$(awk -F= '$1 == "msa.app.version" { print $2 }' gradle.properties | tr -d '[:space:]')"
if [[ -z "$VERSION" ]]; then
  echo "msa.app.version is missing from gradle.properties" >&2
  exit 1
fi

python3 scripts/verify_project.py
python3 scripts/verify_generated_assets.py
python3 scripts/verify_ui.py
python3 scripts/verify_release_tag.py "v$VERSION"
./scripts/run_common_pure_tests.sh
./scripts/run_engine_audit.sh
./scripts/run_controller_audit.sh
./scripts/run_settings_audit.sh
./scripts/run_responsive_audit.sh

echo "production-preflight-offline-ok version=$VERSION"
echo "Gradle, device, browser, signing and store gates still require their platform toolchains."
