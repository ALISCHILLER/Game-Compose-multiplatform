#!/usr/bin/env python3
"""Fail a release when the Git tag and centralized project version differ."""
from __future__ import annotations

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]


def gradle_properties() -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def project_version() -> str:
    version = gradle_properties().get("msa.app.version", "").strip()
    if not version:
        raise SystemExit("Could not read msa.app.version from gradle.properties")
    return version


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: verify_release_tag.py <tag>")
    supplied = sys.argv[1].strip()
    normalized = supplied[1:] if supplied.startswith("v") else supplied
    expected = project_version()
    if normalized != expected:
        raise SystemExit(f"Release tag {supplied!r} does not match project version {expected!r}")
    print(f"release-tag-ok: {supplied}")


if __name__ == "__main__":
    main()
