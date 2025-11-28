# Arquitectura Extensible por Nichos - Llego Business

## 📋 Estado Actual

### ✅ Lo que está bien
La arquitectura actual **SÍ es extensible**, pero actualmente todos los nichos comparten las mismas pantallas de restaurante de forma temporal.

### 🔧 Lo que necesita personalización

#### 1. **TopBar** (línea 44 en RestaurantHomeScreen.kt)
```kotlin
Text("Restaurante La Habana") // ❌ Hardcoded
```
**Problema**: Siempre muestra "Restaurante La Habana" sin importar el nicho.

**Solución**: Debe mostrar el nombre del negocio desde `authViewModel.currentUser.businessProfile.businessName`

#### 2. **BottomNavigation Tabs** (líneas 195-203 en RestaurantHomeScreen.kt)
```kotlin
enum class RestaurantTab(
    val title: String,
    val icon: ImageVector
) {
    ORDERS("Pedidos", Icons.Default.ShoppingCart),
    MENU("Menú", Icons.Default.Restaurant),      // ❌ Específico para restaurante
    WALLET("Wallet", Icons.Default.AccountBalanceWallet),
    TUTORIALS("Tutoriales", Icons.Default.School)
}
```

**Problema**: Los tabs son específicos para restaurante:
- "Menú" debería ser "Productos" para mercados
- "Productos" para agromercado
- "Stock" para tienda de ropa

---

## 🎯 Propuesta de Arquitectura Extensible

### Estrategia 1: **Componentes Parametrizados** (Recomendada)

Crear componentes genéricos que aceptan configuración por nicho.

#### Paso 1: Crear configuración por nicho

```kotlin
// nichos/common/config/BusinessConfig.kt
package com.llego.nichos.common.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.llego.shared.data.model.BusinessType

data class BusinessTabConfig(
    val title: String,
    val icon: ImageVector,
    val route: String
)

object BusinessConfigProvider {

    fun getTabsForBusiness(businessType: BusinessType): List<BusinessTabConfig> {
        return when (businessType) {
            BusinessType.RESTAURANT -> listOf(
                BusinessTabConfig("Pedidos", Icons.Default.ShoppingCart, "orders"),
                BusinessTabConfig("Menú", Icons.Default.Restaurant, "menu"),
                BusinessTabConfig("Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
                BusinessTabConfig("Tutoriales", Icons.Default.School, "tutorials")
            )

            BusinessType.MARKET -> listOf(
                BusinessTabConfig("Pedidos", Icons.Default.ShoppingCart, "orders"),
                BusinessTabConfig("Productos", Icons.Default.Inventory, "products"),
                BusinessTabConfig("Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
                BusinessTabConfig("Tutoriales", Icons.Default.School, "tutorials")
            )

            BusinessType.AGROMARKET -> listOf(
                BusinessTabConfig("Pedidos", Icons.Default.ShoppingCart, "orders"),
                BusinessTabConfig("Productos", Icons.Default.Grass, "products"),
                BusinessTabConfig("Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
                BusinessTabConfig("Tutoriales", Icons.Default.School, "tutorials")
            )

            BusinessType.CLOTHING_STORE -> listOf(
                BusinessTabConfig("Pedidos", Icons.Default.ShoppingCart, "orders"),
                BusinessTabConfig("Stock", Icons.Default.Checkroom, "stock"),
                BusinessTabConfig("Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
                BusinessTabConfig("Tutoriales", Icons.Default.School, "tutorials")
            )

            BusinessType.PHARMACY -> listOf(
                BusinessTabConfig("Pedidos", Icons.Default.ShoppingCart, "orders"),
                BusinessTabConfig("Medicinas", Icons.Default.Medication, "medicines"),
                BusinessTabConfig("Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
                BusinessTabConfig("Tutoriales", Icons.Default.School, "tutorials")
            )
        }
    }

    fun getBusinessTitle(businessType: BusinessType, businessName: String): String {
        return businessName // Usa el nombre del negocio del usuario
    }
}
```

#### Paso 2: Crear HomeScreen genérico

```kotlin
// nichos/common/ui/screens/BusinessHomeScreen.kt
package com.llego.nichos.common.ui.screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    businessType: BusinessType,
    onNavigateToProfile: () -> Unit,
    onNavigateToChats: () -> Unit,
    chatsViewModel: ChatsViewModel,
    ordersViewModel: OrdersViewModel,
    // ViewModels genéricos para diferentes contenidos
    contentViewModel: Any, // MenuViewModel o ProductsViewModel
    settingsViewModel: SettingsViewModel
) {
    val currentUser = authViewModel.uiState.collectAsState().value.currentUser
    val businessName = currentUser?.businessProfile?.businessName ?: "Mi Negocio"
    val tabs = BusinessConfigProvider.getTabsForBusiness(businessType)

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        businessName, // ✅ Dinámico según el negocio
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                // ... resto del código igual
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon, // ✅ Icono específico por nicho
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title, // ✅ Texto específico por nicho
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Renderizar contenido según el tab seleccionado
            when (tabs[selectedTabIndex].route) {
                "orders" -> OrdersScreen(viewModel = ordersViewModel)
                "menu", "products", "stock", "medicines" -> {
                    // Aquí puedes usar diferentes pantallas según el nicho
                    when (businessType) {
                        BusinessType.RESTAURANT -> MenuScreen(viewModel = contentViewModel as MenuViewModel)
                        else -> ProductsScreen(viewModel = contentViewModel as ProductsViewModel)
                    }
                }
                "wallet" -> WalletScreen()
                "tutorials" -> TutorialsScreen()
            }
        }
    }
}
```

#### Paso 3: Actualizar App.kt para usar el componente genérico

```kotlin
// app/App.kt
if (isAuthenticated && currentBusinessType != null) {
    Box(modifier = Modifier) {
        when {
            showProfile -> {
                RestaurantProfileScreen(...)
            }
            else -> {
                // ✅ Usar BusinessHomeScreen genérico
                BusinessHomeScreen(
                    authViewModel = authViewModel,
                    businessType = currentBusinessType!!, // Pasamos el tipo de negocio
                    onNavigateToProfile = { showProfile = true },
                    onNavigateToChats = { showChats = true },
                    chatsViewModel = chatsViewModel,
                    ordersViewModel = ordersViewModel,
                    contentViewModel = when (currentBusinessType) {
                        BusinessType.RESTAURANT -> menuViewModel
                        else -> productsViewModel // Necesitarás crear este ViewModel
                    },
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
```

---

### Estrategia 2: **Pantallas Específicas por Nicho** (Más Control)

Si necesitas mayor personalización, puedes crear pantallas específicas:

```
nichos/
├── restaurant/
│   └── ui/screens/
│       └── RestaurantHomeScreen.kt
├── market/
│   └── ui/screens/
│       └── MarketHomeScreen.kt
├── agromarket/
│   └── ui/screens/
│       └── AgromarketHomeScreen.kt
├── clothing/
│   └── ui/screens/
│       └── ClothingStoreHomeScreen.kt
```

Y en App.kt:

```kotlin
when (currentBusinessType) {
    BusinessType.RESTAURANT -> RestaurantHomeScreen(...)
    BusinessType.MARKET -> MarketHomeScreen(...)
    BusinessType.AGROMARKET -> AgromarketHomeScreen(...)
    BusinessType.CLOTHING_STORE -> ClothingStoreHomeScreen(...)
    BusinessType.PHARMACY -> PharmacyHomeScreen(...)
}
```

---

## 🎨 Componentes Reutilizables por Nivel

### Nivel 1: **Atoms** (100% compartidos)
```
shared/ui/components/atoms/
├── LlegoButton.kt
├── LlegoTextField.kt
└── LlegoCard.kt
```
**No necesitan personalización**

### Nivel 2: **Molecules** (Parcialmente configurables)
```
nichos/common/ui/components/
├── BusinessTopBar.kt       // ✅ Acepta businessName como parámetro
├── BusinessBottomNav.kt    // ✅ Acepta tabs como parámetro
└── OrderCard.kt            // Común para todos
```
**Aceptan parámetros de configuración**

### Nivel 3: **Organisms** (Específicos por nicho)
```
nichos/restaurant/ui/components/
└── MenuList.kt

nichos/market/ui/components/
└── ProductsList.kt

nichos/clothing/ui/components/
└── StockList.kt
```
**Completamente personalizados**

---

## 📊 Matriz de Personalización

| Componente | Restaurante | Mercado | Agromercado | Tienda Ropa | Pharmacy |
|------------|-------------|---------|-------------|-------------|----------|
| **TopBar Title** | Restaurante La Habana | Mercado El Ahorro | Agromercado La Finca | Tienda Moda Actual | Farmacia San José |
| **Tab 1** | Pedidos 🛒 | Pedidos 🛒 | Pedidos 🛒 | Pedidos 🛒 | Pedidos 🛒 |
| **Tab 2** | Menú 🍽️ | Productos 📦 | Productos 🌾 | Stock 👕 | Medicinas 💊 |
| **Tab 3** | Wallet 💰 | Wallet 💰 | Wallet 💰 | Wallet 💰 | Wallet 💰 |
| **Tab 4** | Tutoriales 📚 | Tutoriales 📚 | Tutoriales 📚 | Tutoriales 📚 | Tutoriales 📚 |

---

## 🚀 Implementación Recomendada (Pasos)

### Fase 1: **Parametrizar componentes existentes** (2-3 horas)
1. ✅ Crear `BusinessConfigProvider.kt`
2. ✅ Modificar `RestaurantHomeScreen` para aceptar configuración
3. ✅ Actualizar `App.kt` para pasar `businessType`

### Fase 2: **Crear componentes genéricos** (3-4 horas)
1. ✅ Extraer `BusinessTopBar.kt` genérico
2. ✅ Extraer `BusinessBottomNav.kt` genérico
3. ✅ Crear `BusinessHomeScreen.kt` que use estos componentes

### Fase 3: **Personalizar por nicho** (según necesidad)
1. ⏳ Crear `ProductsViewModel` para mercados
2. ⏳ Crear `ProductsScreen` específica
3. ⏳ Agregar lógica específica por nicho donde sea necesario

---

## 💡 Ventajas de esta Arquitectura

### ✅ Extensibilidad
- Agregar un nuevo nicho solo requiere:
  1. Agregar configuración en `BusinessConfigProvider`
  2. Opcional: crear pantallas específicas si se necesita

### ✅ Reutilización
- Los componentes comunes se mantienen en `nichos/common/`
- Solo duplicas código cuando realmente necesitas personalización diferente

### ✅ Mantenibilidad
- Cambios globales se hacen en un solo lugar
- Cambios específicos por nicho están aislados

### ✅ Flexibilidad
- Puedes usar componentes genéricos O específicos según la necesidad
- Mix & match según el caso de uso

---

## 📝 Ejemplo Completo de Uso

```kotlin
// En App.kt
BusinessHomeScreen(
    authViewModel = authViewModel,
    businessType = BusinessType.AGROMARKET, // 🌾 Agromercado
    // ... resto de parámetros
)

// Resultado automático:
// - TopBar: "Agromercado La Finca"
// - Tab 2: "Productos" con icono 🌾
// - Pantalla de productos genérica o específica
```

---

## 🔄 Migración desde Estado Actual

### Código Actual (Hardcoded)
```kotlin
Text("Restaurante La Habana") // ❌
MENU("Menú", Icons.Default.Restaurant) // ❌
```

### Código Nuevo (Extensible)
```kotlin
Text(businessName) // ✅ Dinámico
tabs[index].title // ✅ Configurado por nicho
```

---

## 📚 Recursos

- **Patrón Strategy**: Para comportamientos específicos por nicho
- **Patrón Factory**: Para crear componentes según BusinessType
- **Composition over Inheritance**: Preferir componentes configurables sobre herencia

---

**Conclusión**: La arquitectura actual **es extensible**, solo necesita implementar el patrón de configuración propuesto para aprovechar al máximo el sistema de nichos. Los componentes parametrizados (Estrategia 1) son la forma más eficiente de lograr personalización sin duplicar código.
