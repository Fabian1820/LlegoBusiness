# Refactor Roadmap (Android + iOS First)

Fecha base: 2026-02-08
Objetivo: mejorar rendimiento de sync/build y uniformidad arquitectonica sin romper flujos de Android/iOS.

## Principios de ejecucion

1. Un PR por fase, sin mezclar objetivos.
2. Mantener comportamiento funcional en cada paso.
3. Priorizar Android/iOS; Desktop queda desacoplado del camino principal.
4. Validar manualmente al cierre de cada fase.

## Validacion manual obligatoria por PR

1. Android build: `.\gradlew.bat :composeApp:assembleDebug`
2. Android smoke:
   - login
   - seleccion de sucursal
   - home
   - pedidos
   - productos
   - logout/login
3. iOS (en Mac):
   - `./gradlew :composeApp:podspec`
   - ejecutar `iosApp` en Xcode
   - repetir smoke equivalente

## Baseline inicial (Fase 0)

Mediciones locales tomadas antes del refactor estructural:

1. `:composeApp:help --configuration-cache`
   - run1: 14.19s
   - run2: 1.41s
2. `:composeApp:compileDebugKotlinAndroid`
   - run1: 9.68s
   - run2: 1.97s
3. `:composeApp:compileDebugKotlinAndroid --rerun-tasks`
   - run forzada: 48.78s

Notas:

1. Estas metricas dependen de cache/daemon y del host.
2. La comparacion valida sera "antes/despues" en el mismo entorno.

## PR-01 (completado en esta iteracion): Quick wins Gradle/KMP

Objetivo: reducir overhead de configuracion en Windows y estabilizar la experiencia de sync.

Cambios:

1. `kotlin.native.ignoreDisabledTargets=true`
2. `kotlin.native.enableKlibsCrossCompilation=false`
3. `org.gradle.parallel=true`

Estado: completado.

## PR-02 (completado en esta iteracion): Congelar composition root (sin cambiar UI)

Objetivo: unificar creacion de dependencias y evitar instancias dispersas.

Cambios:

1. Crear `AppContainer` en `commonMain` para:
   - `TokenManager`
   - `GraphQLClient` init
   - repositorios principales
   - factories de ViewModel
2. Consumir `AppContainer` desde:
   - `composeApp/src/androidMain/kotlin/com/llego/app/MainActivity.kt`
   - `composeApp/src/iosMain/kotlin/com/llego/app/MainViewController.kt`
   - `composeApp/src/jvmMain/kotlin/com/llego/app/main.kt`
3. Mantener contratos de pantalla sin cambios funcionales.

Riesgo: medio.

Estado: completado.

## PR-03 (completado en esta iteracion): Eliminar ViewModels creados en Composables

Objetivo: corregir lifecycle/state ownership y uniformar arquitectura.

Cambios:

1. `BranchSelectorScreen` recibe `BranchSelectorViewModel` por parametro.
2. `BranchesManagementScreen` recibe `BranchesManagementViewModel` por parametro.
3. Quitar `remember { ...ViewModel() }` en pantallas.
4. Instanciar ViewModels en entrypoints usando el container.

Riesgo: medio.

Estado: completado.

## PR-04 (completado en esta iteracion): Refactor de navegacion sin cambiar UX

Objetivo: bajar complejidad de `App.kt` y reducir recompilaciones por acoplamiento.

Cambios:

1. Extraer estado de navegacion a `AppNavigatorState`.
2. Separar flujos:
   - `AuthFlow`
   - `BranchSelectionFlow`
   - `MainBusinessFlow`
3. Mantener mismos destinos y orden actual.

Riesgo: medio.

Estado: completado.

Validacion tecnica ejecutada (Windows):

1. `:composeApp:compileDebugKotlinAndroid` -> OK
2. `:composeApp:compileKotlinJvm` -> OK
3. `:composeApp:assembleDebug` -> OK
4. `:composeApp:compileKotlinIosArm64` -> tarea configurada, compilacion iOS nativa omitida por host no-Mac (esperado por plugin)
5. `:composeApp:compileKotlinIosSimulatorArm64` -> tarea configurada, compilacion iOS nativa omitida por host no-Mac (esperado por plugin)

## PR-05 (completado en esta iteracion): Optimizacion Apollo (fragments y reduccion de codigo generado)

Objetivo: reducir codigo generado y costo de compilacion.

Cambios:

1. Crear fragments reutilizables para entidades grandes:
   - Order
   - Business
   - Branch
   - User
2. Reescribir operaciones duplicadas para usar fragments.
3. Evaluar desactivar `generateDataBuilders` si no existe uso real.

Riesgo: medio (impacta mappers/typedefs generados).

Estado: completado.

Cambios ejecutados:

1. Se agregaron fragments reutilizables en `graphql/fragments` para:
   - `User` (`UserAuthFields`, `UserCoreFields`, `UserUpdateFields`, referencias)
   - `Business` (`BusinessCoreFields`, `BusinessOwnedFields`, `BusinessRoleFields`, etc.)
   - `Branch` (`BranchCoreFields`, `BranchUpdateFields`, `ScoredBranchCoreFields`, etc.)
   - `Order` (full/new/updated/pending/status/comments + subfragments)
2. Se migraron operaciones `auth`, `users`, `businesses`, `branches`, `orders`, `invitations` a uso de fragments.
3. Se ajusto Apollo a `generateDataBuilders=false`.
4. Se refactorizaron mappers/repositorios para el nuevo shape generado por Apollo:
   - wrappers `operationField.<fragmentField>`
   - mapeo centralizado de orders en `OrderFragmentMappers.kt`

Metricas de codigo generado Apollo (host local):

1. Antes PR-05: `30947` lineas (`332` archivos `.kt`)
2. Despues PR-05: `28927` lineas (`443` archivos `.kt`)
3. Delta lineas: `-2020` (menos codigo total generado)

Validacion tecnica ejecutada (Windows):

1. `:composeApp:generateServiceApolloSources` -> OK
2. `:composeApp:compileDebugKotlinAndroid` -> OK
3. `:composeApp:compileKotlinJvm` -> OK
4. `:composeApp:assembleDebug` -> OK
5. `:composeApp:compileKotlinIosArm64` / `:composeApp:compileKotlinIosSimulatorArm64` -> tareas configuradas, compilacion nativa iOS omitida por host no-Mac (esperado)

## PR-06 (completado en esta iteracion): Particion de repositorios grandes

Objetivo: mejorar mantenibilidad y evitar archivos "god class".

Cambios:

1. Partir `BusinessRepository` por dominio:
   - business queries/mutations
   - branch queries/mutations
2. Encapsular fallback N+1 con estrategia clara.
3. Estandarizar manejo de errores.

Riesgo: medio-alto.

Estado: completado.

Cambios ejecutados:

1. `BusinessRepository` paso a facade delgado que mantiene el contrato publico actual.
2. Se creo estado compartido en `BusinessRepositoryState` para centralizar `StateFlow`:
   - `currentBusiness`, `businesses`, `branches`, `currentBranch`
3. Se separo dominio en repositorios internos:
   - `BusinessDomainRepository` (operaciones de negocio)
   - `BranchDomainRepository` (operaciones de sucursal)
4. El fallback N+1 por `GetBranches` quedo encapsulado en `BranchDomainRepository.fallbackGetBranchesByIds`.
5. Manejo de errores quedo alineado por dominio (token, GraphQL, Apollo, unknown) sin cambiar contratos de retorno.

Validacion tecnica ejecutada (Windows):

1. `:composeApp:compileDebugKotlinAndroid` -> OK
2. `:composeApp:compileKotlinJvm` -> OK
3. `:composeApp:assembleDebug` -> OK
4. `:composeApp:compileKotlinIosArm64` / `:composeApp:compileKotlinIosSimulatorArm64` -> tareas configuradas, compilacion nativa iOS omitida por host no-Mac (esperado)

## PR-07 (completado en esta iteracion): Settings real y limpieza de mocks

Objetivo: quitar comportamiento mock en runtime productivo.

Cambios:

1. Reemplazar `SettingsRepository` mock por fuente real o feature flag explicito.
2. Evitar `collect` infinito repetido en `loadSettings`.
3. Alinear `SettingsViewModel` con el patron del resto.

Riesgo: medio.

Estado: completado.

Cambios ejecutados:

1. `SettingsRepository` quedo con modo real por defecto (`REAL_BRANCH`) y fallback mock desactivado por defecto.
2. Se mantuvo fallback mock como opcion explicita (`allowMockFallback`) para escenarios controlados.
3. `SettingsRepository.getInstance` ahora re-crea instancia cuando cambia la configuracion de fuente/fallback, evitando estados inconsistentes de singleton.
4. `SettingsViewModel` se refactorizo para:
   - observar `repository.settings` una sola vez en `init`
   - usar `loadSettings()` como refresh one-shot via `repository.getSettings()`
   - evitar `collect` infinito repetido dentro de `loadSettings`
   - reportar error si `updateSettings` falla (exception o `false`)

Validacion tecnica ejecutada (Windows):

1. `:composeApp:compileDebugKotlinAndroid` -> OK
2. `:composeApp:compileKotlinJvm` -> OK
3. `:composeApp:assembleDebug` -> OK
4. `:composeApp:compileKotlinIosArm64` / `:composeApp:compileKotlinIosSimulatorArm64` -> tareas configuradas, compilacion nativa iOS omitida por host no-Mac (esperado)

## PR-08 (completado en esta iteracion): Desacoplar Desktop del camino mobile (opcional pero recomendado)

Objetivo: que Android/iOS no paguen costo de Desktop/HotReload en sync habitual.

Cambios:

1. Mover desktop a modulo separado o activarlo por propiedad Gradle.
2. Aplicar `compose-hot-reload` solo en perfil desktop-dev.

Riesgo: medio.

Estado: completado.

Cambios ejecutados:

1. `composeApp/build.gradle.kts` ahora activa Desktop solo con `-Pllego.enableDesktop=true`:
   - target `jvm()`
   - dependencias `jvmMain`
   - bloque `compose.desktop`
2. Plugin `compose-hot-reload` quedo en modo opt-in y se aplica solo si:
   - `llego.enableDesktop=true`
   - `llego.desktopDev=true`
3. Se agregaron defaults en `gradle.properties`:
   - `llego.enableDesktop=false`
   - `llego.desktopDev=false`
4. Se actualizo `README.md` con comandos de ejecucion Desktop usando propiedades Gradle.

Validacion tecnica ejecutada (Windows):

1. Perfil mobile (default):
   - `:composeApp:compileDebugKotlinAndroid` -> OK
   - `:composeApp:assembleDebug` -> OK
   - `:composeApp:compileKotlinIosArm64` / `:composeApp:compileKotlinIosSimulatorArm64` -> tareas configuradas, compilacion nativa iOS omitida por host no-Mac (esperado)
2. Verificacion de desacople:
   - `:composeApp:compileKotlinJvm` (sin flags) -> tarea no disponible (esperado)
3. Perfil desktop:
   - `:composeApp:compileKotlinJvm -Pllego.enableDesktop=true` -> OK
   - `:composeApp:compileKotlinJvm -Pllego.enableDesktop=true -Pllego.desktopDev=true` -> OK

## PR-09 (completado en esta iteracion): Limpieza final tecnica

Objetivo: consolidar y cerrar deuda de bajo riesgo.

Cambios:

1. Eliminar logs de debug (`println`/`Log.d`) no esenciales.
2. Depurar dependencias no usadas.
3. Actualizar `CLAUDE.md` y docs con estado real.

Riesgo: bajo.

Estado: completado.

Cambios ejecutados:

1. Se eliminaron logs de debug no esenciales (`println` / `Log.d`) en flujos de:
   - auth Android (`AuthViewModel.android`, `TokenManager.android`, `GoogleSignInHelper.android`, `AppleSignInHelper.android`)
   - `MainActivity`
   - pantallas/viewmodels comunes con trazas temporales (`LoginScreen`, `BranchSelectorScreen`, `RegisterBusinessScreen`, `InvitationViewModel`)
2. Se depuro dependencia no usada en `commonMain`:
   - removida `libs.androidx.navigation.compose` de `composeApp/build.gradle.kts` (no habia uso real de NavHost/NavController en el codigo actual)
   - removidos `androidx-navigation` (version) y `androidx-navigation-compose` (library alias) de `gradle/libs.versions.toml`

Validacion tecnica ejecutada (Windows):

1. `:composeApp:compileDebugKotlinAndroid` -> OK
2. `:composeApp:assembleDebug` -> OK
3. `:composeApp:compileKotlinIosArm64` / `:composeApp:compileKotlinIosSimulatorArm64` -> tareas configuradas, compilacion nativa iOS omitida por host no-Mac (esperado)
4. `:composeApp:compileKotlinJvm -Pllego.enableDesktop=true` -> OK

## Definicion de hecho global

1. Android e iOS ejecutan los flujos criticos sin regresion.
2. `App.kt` queda reducido y sin crecimiento de flags manuales.
3. Dependencias y creacion de ViewModels quedan uniformes.
4. Build/sync muestran mejora medible respecto a baseline.
