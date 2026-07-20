# Security Policy

## Supported version

Security fixes are applied to the latest release branch.

## Reporting

Report suspected vulnerabilities privately to [solimaniali90@gmail.com](mailto:solimaniali90@gmail.com) or through [ALISCHILLER](https://github.com/ALISCHILLER). Do not open a public issue containing credentials, signing material, exploit details, or personal data.

## Security posture

- No backend, network client, analytics, advertising, authentication, or remote telemetry
- No dangerous Android permission
- Android cleartext traffic disabled
- Android backup disabled
- Release signing material supplied only through environment variables or CI secrets
- Portable Gradle Wrapper with a pinned distribution SHA-256
- Deterministic generated-asset manifest and isolated regeneration verification
- Dependency review, CodeQL, dependency submission, and Dependabot workflows
- Offline scans for local paths, placeholder code, malformed resources, embedded secrets, version drift, and release-gate omissions
- iOS privacy manifest declaring no tracking or collected data and the app-only UserDefaults required reason

## Secret handling

Never commit:

- `.env`
- `*.jks`, `*.keystore`, `*.p12`, or provisioning profiles
- signing passwords or aliases
- API keys, tokens, or credentials
- exported store certificates

Use the variable names in `.env.example` and the CI secret names documented in [SETUP.md](SETUP.md). The example file contains placeholders only.

## Dependency and build-chain review

Every public release must archive the resolved Gradle dependency graph, review direct and transitive licenses, and preserve upstream notices. GitHub Action references should be reviewed during dependency updates.

## Privacy

The local data model and deletion behavior are documented in [PRIVACY.md](PRIVACY.md).

## Release gate

A source review alone is not sufficient. Follow [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) and do not call a target production-ready until its build, tests, runtime, signing, and distribution gates have evidence.
