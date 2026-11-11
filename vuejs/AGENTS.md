# Repository Guidelines

## Project Structure & Module Organization

This repository builds an IntelliJ IDEA–platform plugin (Mpx.js / Vue-derived support). It is a Gradle multi-module layout with source sets wired at the root.

- Source code (Kotlin/Java): `vuejs-*/src/`
  - Examples: `vuejs-backend/`, `vuejs-common/`, `vuejs-debugger/`, `vuejs-markdown/`, `vuejs-prettier/`
- Plugin descriptor: `vuejs-plugin/resources/META-INF/plugin.xml`
- Resources: `vuejs-*/resources/` and `resources/`
- Tests: `vuejs-tests/src/` and test data in `vuejs-tests/testData/`
- Alternative build metadata (if applicable): `BUILD.bazel`

## Build, Test, and Development Commands

Use the Gradle wrapper from the repo root:

- Build/compile: `./gradlew build` (assembles + runs verification tasks configured by Gradle)
- Run tests: `./gradlew test`
- Code checks: `./gradlew check` (aggregates verification tasks; prefer this before opening a PR)
- Run IDE for manual testing: `./gradlew runIde`
- Plugin verification (when available): `./gradlew verifyPlugin`

Tip for sandboxed environments: set `GRADLE_USER_HOME=$PWD/.gradle-user-home` to keep caches inside the repo.

## Coding Style & Naming Conventions

- Languages: Kotlin/Java. Prefer Kotlin for new code where consistent with existing modules.
- Indentation: follow IntelliJ code style; avoid mass reformatting unrelated files.
- Naming:
  - Packages are typically `org.jetbrains.mpxjs.*` (match the module you are touching).
  - Tests use `*Test.kt` (or `*Test.java`) and live under `vuejs-tests/src/`.

## Testing Guidelines

- Tests are JUnit-based (see suites under `vuejs-tests/src/`).
- Add or update test data under `vuejs-tests/testData/` when changing inspections/completions/refactorings.
- Keep tests deterministic and minimize IDE fixture setup where possible.

## Commit & Pull Request Guidelines

- Commit messages commonly use an issue key prefix (e.g. `WEB-12345 ...`) or a scoped prefix (e.g. `[vue] ...`, `feat: ...`); keep the subject short and imperative.
- PRs should include: a concise description, linked issue (if any), how to verify, and test results (`./gradlew check`). Include screenshots/GIFs for UI-visible changes.
