# Dependency Licenses

MSA Bee source code and project-owned generated assets use the project license in `LICENSE.md`. Open-source dependencies remain governed by their own upstream licenses.

| Direct component | Declared version | Primary license | Upstream |
|---|---:|---|---|
| Kotlin / Kotlin Standard Library | 2.3.21 | Apache-2.0 | JetBrains Kotlin |
| Kotlin Coroutines | 1.10.2 | Apache-2.0 | Kotlinx Coroutines |
| Compose Multiplatform Runtime/UI/Foundation | 1.10.3 | Apache-2.0 | JetBrains Compose Multiplatform |
| Compose Multiplatform Material3 | 1.10.0-alpha05 | Apache-2.0 | JetBrains Compose Multiplatform |
| AndroidX Compose UI Test | 1.10.5 | Apache-2.0 | AndroidX |
| Android Gradle Plugin | 8.11.2 | Apache-2.0 | Android Open Source Project |
| AndroidX Activity Compose | 1.13.0 | Apache-2.0 | AndroidX |
| AndroidX Lifecycle Compose | 2.10.0 | Apache-2.0 | AndroidX Multiplatform |
| AndroidX Media3 ExoPlayer | 1.10.1 | Apache-2.0 | AndroidX Media3 |
| AndroidX Test Runner | 1.6.2 | Apache-2.0 | AndroidX Test |
| AndroidX Test Ext JUnit | 1.2.1 | Apache-2.0 | AndroidX Test |
| Koin Core, Android, and Compose | 4.2.1 | Apache-2.0 | InsertKoinIO Koin |
| Gradle Wrapper | 8.14.5 | Apache-2.0 | Gradle |

The table covers direct declarations in `gradle/libs.versions.toml`. A public release must also archive and review the resolved transitive graph:

```bash
./gradlew :composeApp:dependencies > dependency-report.txt
```

Release maintainers must preserve notices shipped with third-party binaries and update this file whenever the version catalog changes.
