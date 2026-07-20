#!/usr/bin/env python3
"""Regenerate project-owned assets in a temporary directory and verify hashes."""
from pathlib import Path
import hashlib
import os
import re
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "ASSET_MANIFEST.sha256"
GENERATOR = ROOT / "tools/generate_assets.py"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> int:
    expected: dict[str, str] = {}
    for line_number, raw_line in enumerate(MANIFEST.read_text(encoding="utf-8").splitlines(), 1):
        if not raw_line.strip():
            continue
        match = re.fullmatch(r"([0-9a-f]{64})  (.+)", raw_line.strip())
        if not match:
            print(f"Invalid asset manifest line {line_number}", file=sys.stderr)
            return 1
        expected[match.group(2)] = match.group(1)

    with tempfile.TemporaryDirectory(prefix="msa-bee-assets-") as temp:
        temp_root = Path(temp)
        environment = os.environ.copy()
        environment["MSA_BEE_ASSET_ROOT"] = str(temp_root)
        subprocess.run([sys.executable, str(GENERATOR)], check=True, env=environment)

        mismatches: list[str] = []
        for relative_path, expected_hash in expected.items():
            generated = temp_root / relative_path
            if not generated.is_file():
                mismatches.append(f"missing generated asset: {relative_path}")
                continue
            actual_hash = sha256(generated)
            if actual_hash != expected_hash:
                mismatches.append(
                    f"hash mismatch: {relative_path}: expected={expected_hash} actual={actual_hash}"
                )

        generated_files = {
            path.relative_to(temp_root).as_posix()
            for path in temp_root.rglob("*")
            if path.is_file()
        }
        unexpected = generated_files - set(expected)
        if unexpected:
            mismatches.extend(f"untracked generated asset: {path}" for path in sorted(unexpected))

        if mismatches:
            print("Generated asset verification failed:", file=sys.stderr)
            for mismatch in mismatches:
                print(f"- {mismatch}", file=sys.stderr)
            return 1

    print("generated-assets-reproducibility-ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
