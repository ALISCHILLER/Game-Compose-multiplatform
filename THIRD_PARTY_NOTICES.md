# Third-Party Notices

MSA Bee does not ship third-party image, font, music, or sound assets. Gameplay artwork is programmatic, and launcher icons/audio are original deterministic generated assets documented in:

- `ASSET_PROVENANCE.md`
- `ASSET_MANIFEST.sha256`

The application uses open-source build and runtime dependencies. Direct declarations and their primary licenses are listed in `DEPENDENCY_LICENSES.md`.

A public release must also capture and review the resolved dependency graph:

```bash
./gradlew :composeApp:dependencies > dependency-report.txt
```

Resolved artifacts, embedded notices, and transitive licenses remain governed by their upstream copyright and license terms. The release workflow archives the dependency report with signed Android output. Equivalent reports should be retained for every distributed target.

No project-owned source or generated asset is relicensed by this notice.
