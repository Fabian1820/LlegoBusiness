# Llego Design System & Technical Documentation

**Documentación completa para el desarrollo de apps de delivery coherentes**

## 📋 Tabla de Contenidos

1. [Resumen del Proyecto](#resumen-del-proyecto)
2. [Arquitectura Técnica](#arquitectura-técnica)
3. [Sistema de Diseño](#sistema-de-diseño)
4. [Estructura de Código](#estructura-de-código)
5. [Integración GraphQL](#integración-graphql)
6. [Componentes UI](#componentes-ui)
7. [Navegación y Estado](#navegación-y-estado)
8. [Buenas Prácticas](#buenas-prácticas)
9. [Guías de Implementación](#guías-de-implementación)
10. [Configuración y Herramientas](#configuración-y-herramientas)

---

## 🎯 Resumen del Proyecto

**Llego** es una plataforma de delivery cubana implementada como app Kotlin Multiplatform. Este documento establece los estándares para desarrollar apps coherentes para el ecosistema Llego:

- **App Cliente** (actual): Para usuarios finales que realizan pedidos
- **App Negocio** (próxima): Para restaurantes y comercios
- **App Chofer** (próxima): Para conductores y repartidores

### Tecnologías Core

- **Kotlin Multiplatform Mobile (KMP)** 2.2.10
- **Compose Multiplatform** 1.8.2
- **Apollo GraphQL** 4.3.3
- **Material 3** Design System
- **Coil** 3.0.0 para carga de imágenes
- **Lifecycle ViewModel** para gestión de estado

---

## 🏗️ Arquitectura Técnica

### Stack Tecnológico Completo

```kotlin
// Dependencias principales
dependencies {
    // Compose Multiplatform
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Lifecycle & ViewModel
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // GraphQL
    api(libs.apollo.runtime)

    // Imagen loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor)

    // Android específico
    implementation("com.airbnb.android:lottie-compose:6.6.7")
}
```

### Targets de Plataforma

```kotlin
// Configuración de targets
kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
}
```

### Patrón de Arquitectura

**MVVM + Repository Pattern**

```
📁 composeApp/src/commonMain/kotlin/com/llego/multiplatform/
├── 📁 data/
│   ├── 📁 model/          # Data classes
│   ├── 📁 network/        # GraphQL client
│   └── 📁 repositories/   # Screen-based repositories
├── 📁 ui/
│   ├── 📁 components/     # Atomic design components
│   ├── 📁 screens/        # Screen composables
│   ├── 📁 state/          # UI state management
│   ├── 📁 theme/          # Design system
│   └── 📁 viewmodels/     # ViewModels
└── 📁 graphql/            # GraphQL queries
```

---

## 🎨 Sistema de Diseño

### Paleta de Colores

#### Colores Primarios

```kotlin
// Compose (Theme.kt)
val LightColors = lightColorScheme(
    primary = Color(2, 49, 51),              // #02313 - Teal oscuro principal
    onPrimary = Color.White,                 // Texto en primary
    secondary = Color(225, 199, 142),        // #E1C78E - Beige cálido
    tertiary = Color(124, 65, 43),          // #7C412B - Marrón
    background = Color(0xFFF3F3F3),          // Gris claro de fondo
    surface = Color(0xFFFFFFFF),             // Blanco para cards
    onBackground = Color(0xFF1B1B1B),        // Texto principal
    onSurfaceVariant = Color(19, 45, 47)     // Texto secundario
)
```

```swift
// SwiftUI (Theme.swift)
struct LlegoTheme {
    static let primaryColor = Color(red: 2/255, green: 49/255, blue: 51/255)
    static let secondaryColor = Color(red: 225/255, green: 199/255, blue: 142/255)
    static let tertiaryColor = Color(red: 124/255, green: 65/255, blue: 43/255)
    static let backgroundColor = Color(red: 243/255, green: 243/255, blue: 243/255)
    static let accentColor = Color(red: 178/255, green: 214/255, blue: 154/255)
}
```

#### Colores de Acento

```kotlin
onPrimaryContainer = Color(178, 214, 154)    // #B2D69A - Verde claro
onSecondaryContainer = Color(157, 205, 120)   // #9DCD78 - Verde medio
surfaceVariant = Color(236, 240, 233)        // #ECF0E9 - Fondo suave
```

### Tipografía

#### Escala Tipográfica

```kotlin
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    )
)
```

#### Guías de Uso SwiftUI

```swift
// Headers
.font(.system(size: 22-32, weight: .bold, design: .rounded))

// Body text
.font(.system(size: 16-18, weight: .medium))

// Captions
.font(.system(size: 13-14, weight: .medium))
```

### Formas y Elevaciones

#### Corner Radius

- **Cards principales**: 16-20px
- **Elementos pequeños**: 12-14px
- **Botones**: 28px para botones primarios
- **Elementos circulares**: CircleShape

#### Sombras y Elevación

```kotlin
// Compose
CardDefaults.cardElevation(defaultElevation = 4.dp)

shadow(
    color = Color.black.opacity(0.08),
    radius = 15,
    x = 0,
    y = 5
)
```

```swift
// SwiftUI
.shadow(color: Color.black.opacity(0.06), radius: 12, x: 0, y: 4)
```

### Componentes de Formas Personalizadas

```kotlin
// CurvedBottomShape para ProductCard
class CurvedBottomShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(
        path = Path().apply {
            // Implementación de forma curva personalizada
        }
    )
}
```

---

## 🧱 Estructura de Código

### Atomic Design Pattern

#### Atoms (Elementos Básicos)

```kotlin
📁 ui/components/atoms/
├── CartButton.kt           # Botón de carrito con badge animado
├── CounterControls.kt      # Controles +/- para cantidad
├── PillButton.kt          # Botones tipo píldora
├── CategoryItem.kt        # Items de categoría
├── CartItemCard.kt        # Card de item en carrito
├── CartSummaryCard.kt     # Resumen del carrito
└── CheckoutButton.kt      # Botón de checkout
```

**Ejemplo: CartButton**

```kotlin
@Composable
fun CartButton(
    icon: ImageVector,
    badgeCount: Int? = null,
    triggerBounce: Boolean = false,
    onClick: () -> Unit
) {
    // Implementación con animaciones spring
    val bounceScale by animateFloatAsState(
        targetValue = if (shouldBounce) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
}
```

#### Molecules (Componentes Compuestos)

```kotlin
📁 ui/components/molecules/
├── ProductCard.kt         # Card de producto con imagen y controles
├── StoreCard.kt          # Card de tienda/restaurante
└── SearchBar.kt          # Barra de búsqueda
```

**Ejemplo: ProductCard**

```kotlin
@Composable
fun ProductCard(
    imageUrl: String,
    name: String,
    shop: String,
    weight: String,
    price: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAddToCartAnimation: ((String, Offset) -> Unit)? = null
) {
    Card(
        shape = CurvedBottomShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Layout con imagen, información y controles
    }
}
```

#### Organisms (Secciones Complejas)

```kotlin
📁 ui/components/organisms/
├── ProductsSection.kt     # Sección completa de productos
├── StoresSection.kt      # Sección de tiendas
├── Section.kt            # Componente de sección genérico
└── SemicircularSlider.kt # Slider semicircular personalizado
```

### Background Components

```kotlin
📁 ui/components/background/
└── CurvedBackground.kt    # Fondo con curvas personalizadas
```

### Gestión de Estado

#### State Classes

```kotlin
// HomeScreenState.kt
data class HomeScreenState(
    val homeDataState: UiState<HomeData> = UiState.Idle,
    val categoriesState: UiState<List<CategoryData>> = UiState.Idle,
    val searchQuery: String = "",
    val selectedCategoryIndex: Int = 0,
    val productCounts: Map<Int, Int> = emptyMap(),
    val filteredProducts: List<Product> = emptyList(),
    val isInSeeMoreMode: Boolean = false,
    val isInCartMode: Boolean = false
)

// UiState sealed class
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String) : UiState<Nothing>()
}
```

#### Event Handling

```kotlin
// HomeScreenEvent.kt
sealed class HomeScreenEvent {
    object Refresh : HomeScreenEvent()
    data class SearchQueryChanged(val query: String) : HomeScreenEvent()
    data class CategorySelected(val categoryIndex: Int) : HomeScreenEvent()
    data class IncrementProduct(val productId: Int) : HomeScreenEvent()
    data class DecrementProduct(val productId: Int) : HomeScreenEvent()
    object SeeMoreClicked : HomeScreenEvent()
    object CartClicked : HomeScreenEvent()
    object RetryClicked : HomeScreenEvent()
}
```

---

## 🔌 Integración GraphQL

### Configuración del Cliente

```kotlin
// GraphQLClient.kt
object GraphQLClient {
    val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://llegobackend-production.up.railway.app/graphql")
        .build()
}
```

### Estructura de Queries

```graphql
# GetHomeData.graphql - Query unificada para pantalla principal
query GetHomeData {
  products {
    id
    name
    shop
    weight
    price
    imageUrl
  }
  stores {
    id
    name
    etaMinutes
    logoUrl
    bannerUrl
  }
}
```

### Patrón Repository Screen-Based

**❌ Evitar**: Repositorios por entidad individual
```kotlin
// NO hacer esto
class ProductRepository
class StoreRepository
```

**✅ Preferir**: Repositorios por pantalla
```kotlin
// SÍ hacer esto
class HomeRepository {
    suspend fun getHomeData(): HomeData {
        val response = apolloClient.query(GetHomeDataQuery()).execute()

        if (response.hasErrors()) {
            throw Exception("GraphQL errors: ${response.errors?.joinToString { it.message }}")
        }

        val data = response.data ?: throw Exception("No data received")

        return HomeData(
            products = data.products.map { /* mapping */ },
            stores = data.stores.map { /* mapping */ }
        )
    }
}
```

### Configuración Apollo

```kotlin
// build.gradle.kts
apollo {
    service("service") {
        packageName.set("com.llego.multiplatform.graphql")
        generateOptionalOperationVariables.set(false)
        generateKotlinModels.set(true)
        generateAsInternal.set(false)
        alwaysGenerateTypesMatching.set(listOf(".*"))
    }
}
```

---

## 🎮 Navegación y Estado

### Compose Navigation

```kotlin
// LlegoNavigation.kt
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderConfirmation : Screen("order_confirmation")
    object Profile : Screen("profile")
    object CategorySelection : Screen("category_selection")
}

val navigationItems = listOf(
    NavigationItem(Screen.Home.route, Icons.Default.Home, "Home"),
    NavigationItem("search", Icons.Default.Search, "Search"),
    NavigationItem(Screen.Cart.route, Icons.Default.ShoppingCart, "Cart"),
    NavigationItem(Screen.Checkout.route, Icons.Default.LocalShipping, "Checkout"),
    NavigationItem(Screen.Profile.route, Icons.Default.Person, "Profile")
)
```

### SwiftUI Navigation

```swift
// ContentView.swift
struct MainAppView: View {
    @Binding var selectedTab: Int

    var body: some View {
        TabView(selection: $selectedTab) {
            ComposeView()
                .tabItem {
                    Image(systemName: "house")
                    Text("Inicio")
                }
                .tag(0)

            CategorySelectionView()
                .tabItem {
                    Image(systemName: "square.grid.2x2")
                    Text("Categoría")
                }
                .tag(1)

            CheckoutView()
                .tabItem {
                    Image(systemName: "truck.box")
                    Text("Checkout")
                }
                .tag(2)

            ProfileView()
                .tabItem {
                    Image(systemName: "person")
                    Text("Cuenta")
                }
                .tag(3)
        }
        .accentColor(Color.llegoPrimary)
    }
}
```

### ViewModel Pattern

```kotlin
class HomeViewModel(
    private val homeRepository: HomeRepository = HomeRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.Refresh -> loadData()
            is HomeScreenEvent.IncrementProduct -> incrementProduct(event.productId)
            is HomeScreenEvent.DecrementProduct -> decrementProduct(event.productId)
            // ... más eventos
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Carga concurrente de datos
            val homeDataDeferred = async { loadHomeData() }
            val categoriesDeferred = async { loadCategoriesData() }

            // Actualización de estado
        }
    }
}
```

---

## 🎯 Componentes UI

### Animaciones

#### Spring Animations (Preferidas)

```kotlin
// Bounce effect para CartButton
animateFloatAsState(
    targetValue = if (shouldBounce) 1.4f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
)
```

```swift
// SwiftUI Spring Animations
.animation(.spring(response: 0.6, dampingFraction: 0.8), value: isSelected)
```

#### Easing Transitions

```swift
.animation(.easeInOut(duration: 0.3), value: isProcessingPayment)
```

### Carga de Imágenes

```kotlin
// Compose con Coil
SubcomposeAsyncImage(
    model = imageUrl,
    contentDescription = name,
    modifier = Modifier.size(84.dp),
    contentScale = ContentScale.Fit
) {
    when (painter.state) {
        is AsyncImagePainter.State.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        is AsyncImagePainter.State.Error -> {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Error loading image",
                tint = MaterialTheme.colorScheme.error
            )
        }
        else -> SubcomposeAsyncImageContent()
    }
}
```

### Payment Methods (Específico para Checkout)

```swift
let paymentMethods: [PaymentMethod] = [
    PaymentMethod(
        id: "cash_cup",
        name: "Efectivo CUP",
        description: "Pago al recibir el pedido",
        icon: "banknote",
        color: Color.llegoPrimary,
        currency: "CUP"
    ),
    PaymentMethod(
        id: "qvapay",
        name: "QvaPay",
        description: "Pago digital rápido y seguro",
        icon: "qrcode",
        color: Color(red: 0.2, green: 0.6, blue: 0.9),
        currency: "CUP/USD"
    ),
    // ... más métodos
]
```

---

## ✅ Buenas Prácticas

### Arquitectura

1. **Un Repository por Pantalla**: Evita múltiples llamadas de red
2. **Queries Unificadas**: Combina datos relacionados en una sola query GraphQL
3. **State Management**: Usa StateFlow para estado reactivo
4. **Error Handling**: Maneja siempre errores de GraphQL
5. **Mappeo de Dominio**: Convierte respuestas GraphQL a modelos de dominio

### UI/UX

1. **Animaciones Spring**: Usa animaciones naturales y fluidas
2. **Loading States**: Siempre muestra estados de carga
3. **Error States**: Proporciona retry options
4. **Consistencia Visual**: Mantén el design system
5. **Responsive Design**: Prueba en diferentes tamaños de pantalla

### Performance

1. **Image Caching**: Usa Coil con cache automático
2. **Lazy Loading**: Implementa lazy loading para listas largas
3. **State Hoisting**: Levanta estado solo cuando sea necesario
4. **Composition Optimization**: Evita recomposiciones innecesarias

### Código

```kotlin
// ✅ Buena práctica: Componente reutilizable con parámetros claros
@Composable
fun ProductCard(
    imageUrl: String,
    name: String,
    shop: String,
    weight: String,
    price: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
)

// ❌ Evitar: Componente con demasiadas responsabilidades
@Composable
fun MegaComponent() // que hace muchas cosas
```

---

## 📚 Guías de Implementación

### Para App de Negocios

#### Adaptaciones Específicas

1. **Dashboard de Pedidos**: Reemplaza HomeScreen con OrderDashboard
2. **Gestión de Productos**: Añadir ProductManagementScreen
3. **Analytics**: Integrar SalesAnalyticsScreen
4. **Configuración**: BusinessSettingsScreen

```kotlin
// Ejemplo de estructura para app de negocios
📁 business-app/src/commonMain/kotlin/com/llego/business/
├── 📁 ui/screens/
│   ├── DashboardScreen.kt
│   ├── OrderManagementScreen.kt
│   ├── ProductManagementScreen.kt
│   ├── AnalyticsScreen.kt
│   └── BusinessSettingsScreen.kt
├── 📁 data/repositories/
│   ├── BusinessRepository.kt
│   ├── OrderRepository.kt
│   └── AnalyticsRepository.kt
```

#### GraphQL Queries Específicas

```graphql
# GetBusinessData.graphql
query GetBusinessData($businessId: ID!) {
  business(id: $businessId) {
    id
    name
    orders {
      id
      status
      total
      customer {
        name
        phone
      }
    }
    products {
      id
      name
      stock
      sales
    }
    analytics {
      dailySales
      orderCount
      topProducts
    }
  }
}
```

### Para App de Choferes

#### Adaptaciones Específicas

1. **Mapa de Entregas**: MapScreen con rutas optimizadas
2. **Lista de Pedidos**: OrderQueueScreen
3. **Tracking**: DeliveryTrackingScreen
4. **Historial**: DeliveryHistoryScreen

```kotlin
// Ejemplo de estructura para app de choferes
📁 driver-app/src/commonMain/kotlin/com/llego/driver/
├── 📁 ui/screens/
│   ├── MapScreen.kt
│   ├── OrderQueueScreen.kt
│   ├── DeliveryTrackingScreen.kt
│   └── DriverProfileScreen.kt
├── 📁 data/repositories/
│   ├── DriverRepository.kt
│   ├── DeliveryRepository.kt
│   └── LocationRepository.kt
```

#### Navigation Específica

```swift
// SwiftUI para app de choferes
TabView(selection: $selectedTab) {
    MapView()
        .tabItem {
            Image(systemName: "map")
            Text("Mapa")
        }
        .tag(0)

    OrderQueueView()
        .tabItem {
            Image(systemName: "list.bullet")
            Text("Pedidos")
        }
        .tag(1)

    DeliveryTrackingView()
        .tabItem {
            Image(systemName: "location.circle")
            Text("En Ruta")
        }
        .tag(2)

    DriverProfileView()
        .tabItem {
            Image(systemName: "person.crop.circle")
            Text("Perfil")
        }
        .tag(3)
}
```

---

## ⚙️ Configuración y Herramientas

### Build Configuration

```kotlin
// build.gradle.kts común para todas las apps
android {
    namespace = "com.llego.multiplatform" // Cambiar según app
    compileSdk = 35

    defaultConfig {
        applicationId = "com.llego.multiplatform" // Cambiar según app
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### GraphQL Configuration

```kotlin
apollo {
    service("service") {
        packageName.set("com.llego.multiplatform.graphql") // Cambiar según app
        generateOptionalOperationVariables.set(false)
        generateKotlinModels.set(true)
        generateAsInternal.set(false)
        alwaysGenerateTypesMatching.set(listOf(".*"))
    }
}
```

### Comandos de Build

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run

# iOS (Xcode required)
./gradlew openXcode
```

### Testing

```kotlin
// Configuración de testing común
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.apollo.testing.support)
}
```

---

## 🔧 Tareas Personalizadas

```kotlin
// Tareas útiles para desarrollo
tasks.register("buildIOSFramework") {
    dependsOn("linkDebugFrameworkIosSimulatorArm64")
    doLast {
        println("iOS Framework built successfully")
    }
}

tasks.register("syncIOSApp") {
    dependsOn("linkDebugFrameworkIosSimulatorArm64")
    doLast {
        println("iOS App synchronized with shared framework")
    }
}
```

---

## 📱 Especificaciones de Plataforma

### iOS Específico

- **Deployment Target**: iOS 15.0+
- **Arquitecturas**: arm64, arm64-simulator
- **Framework**: Static framework para mejor performance
- **Navigation**: SwiftUI TabView con NavigationView

### Android Específico

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Architecture**: Universal APK con soporte arm64-v8a y armeabi-v7a
- **Navigation**: Compose Navigation con Bottom Navigation

### Desktop Específico

- **JVM Target**: 11
- **Supported OS**: Windows, macOS, Linux
- **Distribution**: DMG (macOS), MSI (Windows), DEB (Linux)

---

## 🚀 Próximos Pasos

### Para Implementar App de Negocios

1. Duplicar estructura base del proyecto
2. Cambiar `applicationId` y `namespace`
3. Implementar screens específicas de negocio
4. Crear queries GraphQL para datos de negocio
5. Adaptar navigation y theming
6. Implementar funcionalidades específicas (dashboard, analytics)

### Para Implementar App de Choferes

1. Duplicar estructura base del proyecto
2. Integrar Maps SDK (Google Maps / Apple Maps)
3. Implementar tracking de ubicación en tiempo real
4. Crear sistema de notificaciones push
5. Implementar funcionalidades específicas (rutas, entrega)

---

## 📞 Contacto y Soporte

- **Backend GraphQL**: `https://llegobackend-production.up.railway.app/graphql`
- **Package Namespace**: `com.llego.multiplatform`
- **Repository Pattern**: Screen-based para optimizar GraphQL queries

---

*Esta documentación debe actualizarse conforme evolucione el proyecto Llego. Mantener coherencia entre todas las apps del ecosistema es crucial para la experiencia de usuario y mantenibilidad del código.*