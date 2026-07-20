# Contributing

1. Use JDK 17 and the checked-in Gradle Wrapper.
2. Keep domain code free of Compose and platform APIs.
3. Add tests for changes to physics, persistence, UI semantics, or platform adapters.
4. Run:

```bash
python3 scripts/verify_project.py
./gradlew :composeApp:jvmTest :composeApp:lintDebug :composeApp:assembleDebug
```

5. Do not add assets without provenance and redistribution permission.
6. Do not change `applicationId`, Bundle ID, signing, license, or public behavior without an explicit reviewed decision.
