# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

---

## Current Status (Post Refactor PR-01 to PR-08)

The project is Kotlin Multiplatform with Android + iOS as the primary path.
Desktop is now opt-in and decoupled from default mobile sync/build.

Key architectural state:
- Central composition root: `AppContainer`.
- Navigation state extracted to `AppNavigatorState`.
- `App.kt` uses flow composables (`AuthFlow`, `BranchSelectionFlow`, `MainBusinessFlow`).
- `BusinessRepository` is a facade split internally by domain.
- Apollo operations are fragment-driven and `generateDataBuilders=false`.
- Settings use real branch-backed source by default (mock fallback is explicit/optional).

---

## Build and Run Commands

Run commands from repo root. On Windows, use `.\gradlew.bat` instead of `./gradlew`.

### Android (default path)
```bash
.\gradlew.bat :composeApp:compileDebugKotlinAndroid
.\gradlew.bat :composeApp:assembleDebug
```

### iOS shared module
```bash
# On Mac only for real native validation
./gradlew :composeApp:podspec
./gradlew :composeApp:compileKotlinIosArm64
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Note: on non-Mac hosts, iOS native compile tasks are expected to be skipped.

### Desktop (opt-in)
Desktop tasks are disabled by default.

```bash
# Enable desktop target
.\gradlew.bat :composeApp:compileKotlinJvm -Pllego.enableDesktop=true

# Run desktop app
.\gradlew.bat :composeApp:run -Pllego.enableDesktop=true

# Enable desktop hot reload only for desktop dev profile
.\gradlew.bat :composeApp:run -Pllego.enableDesktop=true -Pllego.desktopDev=true
```

If `-Pllego.enableDesktop=true` is not set, JVM desktop tasks are intentionally unavailable.

### Apollo
```bash
# Regenerate GraphQL sources
.\gradlew.bat :composeApp:generateServiceApolloSources
```

---

## Project Layout

Main shared code lives in:
- `composeApp/src/commonMain/kotlin/com/llego`

Important app wiring files:
- `composeApp/src/commonMain/kotlin/com/llego/app/App.kt`
- `composeApp/src/commonMain/kotlin/com/llego/app/AppContainer.kt`
- `composeApp/src/commonMain/kotlin/com/llego/app/AppNavigatorState.kt`

Platform entry points:
- Android: `composeApp/src/androidMain/kotlin/com/llego/app/MainActivity.kt`
- iOS: `composeApp/src/iosMain/kotlin/com/llego/app/MainViewController.kt`
- Desktop: `composeApp/src/jvmMain/kotlin/com/llego/app/main.kt`

GraphQL definitions:
- `composeApp/src/commonMain/graphql`
- Reusable fragments: `composeApp/src/commonMain/graphql/fragments`

---

## Architecture and Conventions

### Dependency wiring
No DI framework is used.
Use `AppContainer` as the composition root for repositories and ViewModel factories.

Do:
- Create dependencies in `AppContainer`.
- Inject ViewModels from entry points and pass them down.

Do not:
- Instantiate ViewModels inside composables.
- Spread new repository construction across UI layers.

### Navigation
Navigation is still manual state-based, but centralized via `AppNavigatorState` and flow composables.

Do:
- Extend navigation through `AppNavigatorState` and existing flow structure.

Do not:
- Reintroduce scattered top-level navigation flags outside navigator state.

### Data layer
`BusinessRepository` is a facade over:
- `BusinessDomainRepository`
- `BranchDomainRepository`
- shared state in `BusinessRepositoryState`

Keep new business/branch logic in domain repositories, not in UI or mapper files.

### Settings
`SettingsRepository` defaults to real branch-backed mode.
Mock behavior is explicit and should not be the runtime default path.

### GraphQL
Apollo models are generated in package:
- `com.llego.multiplatform.graphql`

Use fragment-based queries/mutations where possible to reduce duplication and generated-code churn.

---

## Known Constraints

- Logging is still noisy in some flows (`println`, `Log.d`, `Log.e`); avoid adding more debug logs.
- No meaningful automated test suite is currently in place.
- iOS native validation requires a Mac + Xcode run.

---

## Practical Guidance for Changes

1. Prioritize Android/iOS behavior and build stability.
2. Keep Desktop changes behind `llego.enableDesktop`.
3. Prefer small, isolated refactors per area.
4. Preserve existing contracts for screens and ViewModels unless explicitly migrating them.
5. Update this document when architecture/build workflow changes.
