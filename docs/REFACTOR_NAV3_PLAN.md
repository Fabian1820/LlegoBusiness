# Plan de Refactorizacion por Fases (Navigation 3 - KMP)

Objetivo
- Migrar la navegacion manual a Navigation 3 y dejar una arquitectura pareja para todas las features, sin romper el MVP.
- Mantener la app funcional al final de cada fase.

Principios
- Cambios pequenos y verificables por fase.
- Mantener un unico source-of-truth del backstack (state-driven).
- Evitar logica de datos en Composables; mover a ViewModels o use-cases.
- Eliminar duplicados y archivos no usados.

Estado actual (resumen)
- Navegacion manual centralizada en `composeApp/src/commonMain/kotlin/com/llego/app/App.kt`.
- Varias pantallas y repositorios muy grandes.
- Dependencias ya incluyen `androidx.navigation.compose`, pero no se usa.
- KMP listo para MVP, todos los features deben quedar parejos.

Diagnostico inicial (hallazgos)
- Arquitectura: proyecto KMP en un solo modulo `composeApp` con paquetes `app`, `business`, `shared`; hay estructura por feature pero sin limites claros de capa (UI ↔ data ↔ network).
- Navegacion y flujo de sesion centralizados en `App.kt` con muchos `mutableStateOf` y banderas; esto escala mal.
- Logica de datos dentro de Composables y formularios muy grandes dentro de pantallas.
- Repositorios mezclan: acceso a red + mapeo + estado global (StateFlow) + logging extenso.
- Configuracion de backend duplicada: `GraphQLClient` hardcodea la URL aunque existe `BackendConfig`.
- Archivos mas grandes (lineas aprox.):
  - `composeApp/src/commonMain/kotlin/com/llego/business/branches/ui/screens/BranchesManagementScreen.kt` ~2400
  - `composeApp/src/commonMain/kotlin/com/llego/business/orders/ui/components/OrderDetailSections.kt` ~1419
  - `composeApp/src/commonMain/kotlin/com/llego/business/tutorials/ui/screens/TutorialsScreen.kt` ~1056
  - `composeApp/src/commonMain/kotlin/com/llego/business/wallet/ui/components/WalletSheets.kt` ~963
  - `composeApp/src/commonMain/kotlin/com/llego/business/orders/ui/screens/OrdersScreen.kt` ~956
  - `composeApp/src/commonMain/kotlin/com/llego/business/analytics/ui/screens/StatisticsScreen.kt` ~939
  - `composeApp/src/commonMain/kotlin/com/llego/shared/data/repositories/BusinessRepository.kt` ~893
  - `composeApp/src/commonMain/kotlin/com/llego/business/chats/ui/screens/ChatDetailScreen.kt` ~866
  - `composeApp/src/commonMain/kotlin/com/llego/business/orders/data/mappers/OrderMappers.kt` ~835 (71 funciones)
  - `composeApp/src/commonMain/kotlin/com/llego/business/profile/ui/components/ProfileSections.kt` ~761
  - `composeApp/src/commonMain/kotlin/com/llego/business/wallet/data/repository/WalletRepository.kt` ~747
  - `composeApp/src/commonMain/kotlin/com/llego/shared/ui/business/RegisterBusinessScreen.kt` ~682
  - `composeApp/src/commonMain/kotlin/com/llego/app/App.kt` ~599
- Codigo potencialmente inactivo / duplicado:
  - `composeApp/src/commonMain/kotlin/com/llego/business/home/config/HomeTabsConfig.kt` sin referencias.
  - `composeApp/src/commonMain/kotlin/com/llego/business/tutorials/ui/screens/TutorialsScreen.kt` sin referencias.
  - `composeApp/src/commonMain/kotlin/com/llego/business/orders/ui/screens/OrderDetailDialog.kt` sin referencias (solo comentario en OrdersScreen).
  - `composeApp/src/commonMain/kotlin/com/llego/shared/ui/components/molecules/MapLocationPicker.kt` no se usa; se usa `MapLocationPickerReal`.
  - `androidx.navigation.compose` esta en `composeApp/build.gradle.kts`, pero no hay `NavHost` ni `composable()`.
- Malas practicas / riesgos detectados:
  - UI creando repositorios/servicios directamente:
    - `BranchesManagementScreen` crea `BusinessRepository` y usa `TokenManager` en Composable.
    - `RegisterBusinessScreen` usa `PaymentMethodsRepository` e `ImageUploadServiceFactory` desde la UI.
  - Exceso de estado local en `App.kt` con banderas de navegacion; dificil de mantener y testear.
  - Logging `println` masivo en repositorios (p.ej. `BusinessRepository`, `OrdersViewModel`).
  - Mappers grandes en un solo archivo (`OrderMappers.kt`) y mapeos incrustados en repositorios (`AuthRepository`).
  - `GraphQLClient` ignora `BackendConfig` (fuente de verdad duplicada).
  - Tests casi inexistentes: solo `ComposeAppCommonTest` de ejemplo.

Decision: Navigation 3 (Nav3)
- Estado-driven y backstack "user-owned".
- Encaja bien con el modelo actual de estado + reducer.

Fase 0 - Preparacion y baseline (sin cambios funcionales)
Objetivo: preparar el terreno y acotar riesgos.
Tareas:
- Crear un "mapa de rutas" (sealed class) para todas las pantallas y flujos.
- Definir estandar de navegacion (nombre de rutas, argumentos, backstack).
- Identificar pantallas overlay (dialogs, sheets) para tratarlas como UI local.
- Acordar convenciones de estado (UiState / UiEvent / UiAction).
Entregables:
- Documento de rutas y flujos.
- Lista de pantallas y argumentos necesarios.
Criterio de salida:
- Plan validado y rutas definidas.

Fase 1 - Limpieza estructural pre-Nav3
Objetivo: ordenar la base antes de tocar navegacion, sin cambios funcionales.
Tareas:
- Eliminar logging masivo en repositorios y viewmodels (o reducir a debug si se requiere).
- Unificar configuracion de backend: `GraphQLClient` debe usar `BackendConfig`.
- Confirmar y eliminar archivos sin uso (solo si es claro que no se usan).
- Reorganizar archivos grandes en subcomponentes manteniendo API estable.
- Separar UI de data: mover creacion de repositorios/servicios fuera de Composables a ViewModels.
Entregables:
- Codigo mas ordenado y estable, sin cambios visibles en UX.
Criterio de salida:
- App compila y funciona igual, con archivos mas limpios.

Fase 2 - Fundacion Navigation 3 (infraestructura minima)
Objetivo: introducir Nav3 sin alterar todo el flujo.
Tareas:
- Agregar dependencias Nav3 en KMP (commonMain + androidMain si aplica).
- Crear `NavRoot` con `NavDisplay` y un backstack inicial basico.
- Crear un `Navigator`/`NavController` propio (state holder) para backstack.
- Mantener App.kt funcional, pero delegar la decision de pantalla a `NavRoot`.
Entregables:
- `NavRoot` funcionando con 1-2 pantallas dummy.
Criterio de salida:
- App arranca y navega entre pantallas basicas sin romper login.

Fase 3 - Flujos principales (Auth + Onboarding + Home)
Objetivo: mover los flujos criticos al backstack.
Tareas:
- Mover flujo Auth (Login).
- Mover flujo RegisterBusiness / BranchSelector.
- Mover Home con tabs (Orders/Products/Wallet/Settings) como child navigation.
- Mantener overlays (confirmaciones) locales al screen que las usa.
Entregables:
- Login -> RegisterBusiness/BranchSelector -> Home con backstack real.
Criterio de salida:
- Navegacion de inicio a home funcional en Android/iOS/Desktop.

Fase 4 - Migracion de features (todas parejo)
Objetivo: migrar todas las pantallas de features a Nav3.
Tareas (iterativo por feature):
- Orders (lista, detalle, confirmaciones).
- Products (lista, detalle, add/edit, search).
- Branches (listado, create/edit, mapa).
- Invitations (dashboard, generate, join, listado).
- Analytics, Chats, Profile, Settings, Wallet, Tutorials (si aplica).
Entregables:
- Cada feature con rutas declaradas y navegacion consistente.
Criterio de salida:
- Ningun feature depende de flags en `App.kt`.

Fase 5 - Estabilizacion y pruebas
Objetivo: asegurar estabilidad post-refactor.
Tareas:
- Tests de ViewModels y mappers basicos.
- Verificacion manual de flujos principales.
- Ajustes menores y documentacion final.
Entregables:
- Tests minimos y documentacion actualizada.
Criterio de salida:
- MVP funcional en las tres plataformas.

Riesgos y mitigacion
- Nav3 en KMP todavia joven: iterar en fases pequenas y probar en cada target.
- Cambios transversales: no mezclar refactor de UI y navegacion en la misma tarea.
- Evitar refactors grandes sin pruebas manuales inmediatas.

Checklist de cada fase
- Compila en Android (y iOS/JVM si aplica).
- Flujos basicos navegables.
- Sin cambios de negocio no planeados.

Listo para iniciar
- Al aprobar este plan, comenzamos Fase 0 con el mapa de rutas.
