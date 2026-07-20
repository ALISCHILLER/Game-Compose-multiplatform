#!/usr/bin/env python3
"""Create deterministic SHA-256 manifests for release artifacts."""
from __future__ import annotations

from argparse import ArgumentParser
from pathlib import Path
import hashlib
import sys


def main() -> int:
    parser = ArgumentParser()
    parser.add_argument("paths", nargs="+", help="Files or directories to scan")
    parser.add_argument(
        "--suffix",
        action="append",
        default=[],
        help="Optional case-insensitive suffix filter; may be repeated, e.g. --suffix .msi",
    )
    parser.add_argument("--output", required=True, help="Manifest output path")
    args = parser.parse_args()

    suffixes = {value.lower() for value in args.suffix}
    root = Path.cwd()
    candidates: set[Path] = set()
    for raw_path in args.paths:
        path = Path(raw_path)
        if path.is_file():
            candidates.add(path)
        elif path.is_dir():
            candidates.update(item for item in path.rglob("*") if item.is_file())

    output = Path(args.output)
    candidates.discard(output)
    if suffixes:
        candidates = {path for path in candidates if path.suffix.lower() in suffixes}

    if not candidates:
        suffix_message = f" matching {sorted(suffixes)}" if suffixes else ""
        print(f"No release artifacts were found{suffix_message}.", file=sys.stderr)
        return 1

    lines = []
    for path in sorted(candidates, key=lambda item: item.as_posix()):
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        try:
            display = path.resolve().relative_to(root.resolve()).as_posix()
        except ValueError:
            display = path.resolve().as_posix()
        lines.append(f"{digest}  {display}")

    output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {len(lines)} checksums to {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
