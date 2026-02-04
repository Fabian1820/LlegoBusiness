# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Build & Run Commands

All commands are run from the repo root. On Windows use `.\gradlew.bat` instead of `./gradlew`.

```bash
# Android debug APK
.\gradlew.bat :composeApp:assembleDebug

# Desktop (JVM) — runs the app directly
.\gradlew.bat :composeApp:run

# iOS — build the shared framework (app itself must be run via Xcode)
.\gradlew.bat :composeApp:build

# Full build (all targets)
.\gradlew.bat build

# Clean
.\gradlew.bat clean

# Regenerate Apollo GraphQL Kotlin models from the remote schema
.\gradlew.bat :composeApp:generateApolloService
```

There is no lint or test tooling configured. `commonTest` exists but contains only a placeholder test.

---

## Project Layout

Single-module KMP project. All shared source lives in `composeApp/src/commonMain/kotlin/com/llego/`.

```
com/llego/
├── app/                  # Entry point & top-level navigation (App.kt)
├── business/             # Feature modules (one directory per feature)
│   ├── orders/
│   ├── products/
│   ├── branches/
│   ├── invitations/
│   ├── wallet/
│   ├── analytics/
│   ├── profile/
│   ├── settings/
│   ├── home/
│   ├── tutorials/
│   ├── chats/
│   └── shared/           # UI helpers shared across business features
└── shared/               # Cross-feature infrastructure
    ├── data/
    │   ├── auth/         # TokenManager (expect/actual per platform)
    │   ├── network/      # GraphQLClient, BackendConfig
    │   ├── model/        # Domain data classes
    │   ├── mappers/      # Top-level DTO → domain mappers
    │   └── repositories/ # Shared repository interfaces + impls
    └── ui/
        ├── auth/         # LoginScreen, AuthViewModel
        ├── business/     # RegisterBusinessScreen
        ├── branch/       # BranchSelectorScreen
        ├── components/   # Reusable atoms & molecules (LlegoButton, etc.)
        ├── theme/        # Design system (Color, Typography, Shape, Elevation)
        ├── screens/      # Shared screens (e.g. MapSelectionScreen)
        └── upload/       # Image upload components & service
```

Platform-specific source sets follow the standard KMP layout:
- `androidMain/` — MainActivity, platform TokenManager, Google Maps, Play Services Auth
- `iosMain/` — iOS TokenManager (KeyChain), Darwin Ktor engine
- `jvmMain/` — Desktop entry point (`Main.kt`), JVM TokenManager, OkHttp Ktor engine

GraphQL operation definitions (.graphql files) live in `composeApp/src/commonMain/graphql/`. Apollo generates Kotlin types into the `com.llego.multiplatform.graphql` package at build time.

---

## Architecture Patterns

### Feature structure
Each feature under `business/` follows this internal layout:

```
feature/
├── data/
│   ├── model/            # Domain data classes for this feature
│   ├── mappers/          # GraphQL generated type → domain model
│   └── repository/       # Repository interface + implementation
├── ui/
│   ├── screens/          # Top-level screen composables
│   ├── components/       # Sub-screen composables (sections, sheets, dialogs)
│   └── viewmodel/        # ViewModels with StateFlow-based UI state
└── util/                 # Feature-local helpers
```

### Data flow
```
Composable  →  ViewModel (StateFlow / coroutines)  →  Repository  →  Apollo GraphQL
```

- ViewModels expose state via `StateFlow` and use sealed `UiState` classes (Loading / Success / Error pattern).
- Repositories are plain classes (no DI framework). They are instantiated manually in ViewModels or passed via constructor.
- Mappers are standalone functions — never embedded in repositories.

### Navigation
Navigation is currently **manual**: `App.kt` (~600 lines) drives the entire screen stack through `mutableStateOf` flags. A planned migration to Navigation 3 is documented in `docs/REFACTOR_NAV3_PLAN.md`. Do not add new navigation flags to `App.kt`; if new screens are needed, follow the Nav 3 plan.

### Platform abstraction — expect/actual
`TokenManager` is the primary example: a `expect class` in `commonMain` with `actual` implementations in each platform source set (Android uses EncryptedSharedPreferences, iOS uses KeyChain, JVM uses a properties file). `AuthViewModel`, `GoogleSignInHelper`, and `AppleSignInHelper` follow the same pattern.

### GraphQL
- Client is a singleton initialized once at app start: `GraphQLClient.initialize(tokenManager)`.
- An `AuthInterceptor` automatically attaches the JWT `Bearer` token to every request.
- `BackendConfig` holds the endpoint URL. `GraphQLClient` currently hard-codes it (known issue — use `BackendConfig` for any new code).
- Subscriptions (real-time orders) are managed by `SubscriptionManager`.

### Design system
Light-mode only. Theme entry point is `LlegoBusinessTheme` (wraps Material 3 `Theme`). Custom tokens are in `shared/ui/theme/`: `LlegoLightColorScheme`, `LlegoTypography`, `LlegoShapes`, `Elevation`.

---

## ViewModels & Dependency Wiring

There is **no DI framework** (no Hilt, Koin, etc.). ViewModels are constructed manually at each platform entry point:

- **Android**: `MainActivity` uses `viewModels { ... }` with custom factories.
- **Desktop**: `Main.kt` constructs them directly inside `remember { }`.

All ViewModels are gathered into `AppViewModels` (a plain data class) and passed down to `App`.

When adding a new ViewModel, wire it up in all three entry points (`MainActivity.kt`, `Main.kt`, and the iOS equivalent) and add it to `AppViewModels`.

---

## Known Issues / Constraints to Be Aware Of

- **Large files**: `BranchesManagementScreen` (~2400 lines), `OrderDetailSections` (~1419 lines), `OrderMappers` (~835 lines). Avoid making these larger; extract into sub-components.
- **Unused code**: `HomeTabsConfig.kt`, `TutorialsScreen.kt`, `OrderDetailDialog.kt`, and `MapLocationPicker.kt` (superseded by `MapLocationPickerReal`) have no live references. `androidx.navigation.compose` is in deps but unused.
- **Logging**: Scattered `println()` in repositories. Do not add more; the plan is to remove them.
- **UI creating repositories directly**: `BranchesManagementScreen` and `RegisterBusinessScreen` instantiate repositories inside Composables. New code should pass repositories through ViewModels only.

---

## Ongoing Refactor

`docs/REFACTOR_NAV3_PLAN.md` outlines a phased migration from manual navigation to Navigation 3. The phases are:

0. Route map & conventions
1. Structural cleanup (logging, dead code, separate UI from data)
2. Nav 3 infrastructure (`NavRoot`, `NavController`)
3. Auth + onboarding flows migrated
4. All feature screens migrated
5. Stabilization & tests

New work should align with these phases. Do not re-introduce patterns that the plan explicitly removes (manual nav flags, repos in Composables, duplicate backend URLs).
