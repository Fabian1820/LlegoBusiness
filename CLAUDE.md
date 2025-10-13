# Llego Business App - Claude Documentation

## Proyecto Refactorizado con Arquitectura de Nichos

Este documento describe la implementación del sistema de delivery **Llego** para aplicaciones de **Negocios Multi-Nicho**, desarrollado con Kotlin Multiplatform siguiendo las especificaciones del sistema de diseño documentado en `LLEGO_DESIGN_SYSTEM.md`.

> **Nota Importante**: La app Driver es ahora independiente y no forma parte de este proyecto.

## 🏗️ Arquitectura del Proyecto

### Tecnologías Utilizadas
- **Kotlin Multiplatform Mobile (KMP)** 2.2.10
- **Compose Multiplatform** 1.8.2
- **Material 3 Design System**
- **Navigation Compose** 2.9.0 (org.jetbrains.androidx)
- **Kotlinx Serialization** para modelos de datos
- **StateFlow & ViewModel** para gestión de estado
- **Kotlinx Coroutines** para operaciones asíncronas

### Estructura del Proyecto Limpia y Escalable
```
LlegoBusiness/
├── composeApp/src/commonMain/kotlin/com/llego/
│   ├── app/                    # 🆕 Módulo de aplicación principal (antes "business")
│   │   └── App.kt             # Punto de entrada único
│   │
│   ├── nichos/                 # Sistema de nichos escalable
│   │   ├── common/            # Componentes compartidos entre nichos
│   │   │   └── ui/components/ # Componentes reutilizables
│   │   │
│   │   ├── restaurant/        # 🍽️ Nicho de Restaurante
│   │   │   ├── data/
│   │   │   │   ├── model/     # Order, MenuItem, RestaurantSettings
│   │   │   │   └── repository/# RestaurantRepository
│   │   │   └── ui/
│   │   │       ├── screens/   # Pantallas específicas
│   │   │       └── viewmodel/ # ViewModels específicos
│   │   │
│   │   ├── market/            # 🛒 Nicho de Supermercado
│   │   │   └── ui/screens/    # Pantallas específicas
│   │   │
│   │   └── pharmacy/          # 💊 Nicho de Farmacia (preparado)
│   │       └── ui/screens/    # Pantallas específicas (futuro)
│   │
│   └── shared/                 # Código compartido entre todos los nichos
│       ├── data/              # Modelos de datos y repositorios
│       │   ├── auth/          # Gestión de autenticación
│       │   ├── model/         # Modelos de datos (User, BusinessProfile, BusinessType)
│       │   └── repositories/  # Repositorios de datos (AuthRepository)
│       └── ui/                # Componentes UI compartidos
│           ├── auth/          # Pantallas y lógica de autenticación
│           ├── components/    # Componentes reutilizables
│           │   ├── atoms/     # LlegoButton, LlegoTextField
│           │   ├── molecules/ # UserTypeSelector
│           │   └── background/# CurvedBackground
│           ├── navigation/    # Sistema de navegación centralizado
│           └── theme/         # Sistema de diseño Llego
│
├── composeApp/src/androidMain/kotlin/com/llego/
│   └── app/                    # MainActivity (Android)
│
├── composeApp/src/iosMain/kotlin/com/llego/
│   └── app/                    # MainViewController (iOS)
│
├── composeApp/src/jvmMain/kotlin/com/llego/
│   └── app/                    # main.kt (Desktop)
│
└── gradle/libs.versions.toml   # Configuración de dependencias
```

### 🔄 Cambios Importantes en la Estructura (Octubre 2025)

**Reorganización Completada:**
- ✅ `business/` → `app/` - Nombre más claro y directo
- ✅ Eliminadas carpetas legacy: `business/`, `driver/`
- ✅ Estructura completamente limpia y organizada por nichos
- ✅ Todos los imports actualizados a `com.llego.app`
- ✅ Namespace de Android actualizado a `com.llego.app`
- ✅ Build exitoso y proyecto funcional

## 🎯 Sistema de Nichos

### Concepto de Arquitectura
El proyecto está diseñado para soportar **múltiples nichos de negocio** de forma escalable:

- **Restaurant** 🍽️ - Para restaurantes y negocios de comida
- **Market/Grocery** 🛒 - Para supermercados y tiendas de abarrotes
- **Pharmacy** 💊 - Para farmacias (preparado para futuro)

Cada nicho tiene:
- Pantallas específicas personalizadas
- Flujos de trabajo únicos
- Misma arquitectura base compartida

### Ventajas del Sistema de Nichos
1. **Escalabilidad**: Agregar nuevos nichos es simple y directo
2. **Reutilización**: Componentes comunes compartidos entre nichos
3. **Mantenibilidad**: Código organizado por dominio de negocio
4. **Flexibilidad**: Cada nicho puede tener características únicas

## 🎨 Sistema de Diseño Implementado

### Paleta de Colores
- **Primario**: `#023133` (LlegoPrimary) - Verde petróleo oscuro
- **Secundario**: `#E1C78E` (LlegoSecondary) - Dorado suave
- **Superficie**: `#F8F9FA` (LlegoSurface) - Blanco roto
- **Error**: `#DC3545` (LlegoError) - Rojo de error

### Componentes de Diseño Mejorados

#### LoginScreen Moderno
- **Logo circular** con bordes redondeados y sombra
- **Header fijo** con fondo verde que no hace scroll
- **Card blanco** con contenido scrolleable
- **Título con fondo sutil** en color primario (alpha 0.08)
- **Campos de texto** con fondo gris claro (#EEEEEE)
- **Selector de nichos** con chips visuales
- **Botones sociales** solo en login (no en registro)
- **Link "Olvidaste tu contraseña"** funcional

#### Formulario de Login
- Email + Contraseña
- Validación en tiempo real
- Botones sociales (Google, Apple)
- Link de recuperación de contraseña

#### Formulario de Registro
Campos profesionales para negocios:
- Tipo de negocio (selector visual)
- Nombre del negocio
- Nombre del responsable
- Teléfono
- Dirección
- Email
- Contraseña
- Confirmar contraseña

## 🔐 Sistema de Autenticación Refactorizado

### Arquitectura MVVM
- **AuthViewModel**: Gestión de estado de autenticación
- **AuthManager**: Coordinador central de autenticación
- **AuthRepository**: Acceso a datos con mock data por nicho

### Flujo de Autenticación
1. **LoginScreen**: Pantalla moderna con selector de nicho
2. **Validación**: Email, contraseña y tipo de negocio
3. **Navegación**: Redirección automática según el nicho seleccionado
4. **Sesión**: Gestión persistente de sesión de usuario

### Tipos de Negocio (BusinessType)
```kotlin
enum class BusinessType {
    RESTAURANT,  // Restaurante
    GROCERY,     // Supermercado
    PHARMACY,    // Farmacia
    // Fácil agregar más nichos...
}
```

## 🧭 Sistema de Navegación Refactorizado

### Navegación por Nichos
- **LlegoNavigationController**: Controlador basado en `BusinessType`
- **Routes**: Rutas organizadas por nicho
- Navegación dinámica según tipo de negocio

### Rutas Implementadas

#### Autenticación (Compartidas)
- `Routes.Auth.LOGIN`
- `Routes.Auth.FORGOT_PASSWORD`
- `Routes.Auth.VERIFY_CODE`
- `Routes.Auth.RESET_PASSWORD`

#### Nicho Restaurant
- `Routes.Restaurant.DASHBOARD`
- `Routes.Restaurant.ORDERS`
- `Routes.Restaurant.ORDER_DETAIL`
- `Routes.Restaurant.MENU`
- `Routes.Restaurant.MENU_ITEM_DETAIL`
- `Routes.Restaurant.ANALYTICS`
- `Routes.Restaurant.PROFILE`
- `Routes.Restaurant.SETTINGS`
- `Routes.Restaurant.NOTIFICATIONS`

#### Nicho Market/Grocery
- `Routes.Market.DASHBOARD`
- `Routes.Market.ORDERS`
- `Routes.Market.ORDER_DETAIL`
- `Routes.Market.PRODUCTS`
- `Routes.Market.PRODUCT_DETAIL`
- `Routes.Market.INVENTORY`
- `Routes.Market.ANALYTICS`
- `Routes.Market.PROFILE`
- `Routes.Market.SETTINGS`
- `Routes.Market.NOTIFICATIONS`

#### Nicho Pharmacy (Preparado)
- `Routes.Pharmacy.DASHBOARD`
- `Routes.Pharmacy.ORDERS`
- `Routes.Pharmacy.MEDICINES`
- `Routes.Pharmacy.PRESCRIPTIONS`
- `Routes.Pharmacy.PROFILE`
- Y más...

### Helpers de Navegación
```kotlin
Routes.getDashboardRoute(businessType: BusinessType)
Routes.getProfileRoute(businessType: BusinessType)
```

## 📱 Pantallas Implementadas

### LoginScreen Moderna
- **Diseño mejorado** con logo fijo y scroll inteligente
- **Campos visuales** con fondo gris claro para mejor UX
- **Selector de nichos** con chips horizontales
- **Validación completa** de formularios
- **Textos en español** para mejor localización
- **Botones sociales** con íconos circulares

### RestaurantProfileScreen
- Información del restaurante
- Estadísticas (pedidos, calificación)
- Opciones de configuración (Menú, Horarios)
- Logout funcional con confirmación

### MarketProfileScreen
- Información del supermercado
- Estadísticas (pedidos, calificación)
- Opciones de configuración (Productos, Entregas)
- Logout funcional con confirmación

## 📊 Modelos de Datos Refactorizados

### Usuario Principal
```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val businessType: BusinessType,  // Ahora usa BusinessType
    val profileImage: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val businessProfile: BusinessProfile? = null
)
```

### Perfil de Negocio
```kotlin
data class BusinessProfile(
    val businessId: String,
    val businessName: String,
    val businessType: BusinessType,  // Restaurant, Grocery, Pharmacy, etc.
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val businessPhone: String,
    val description: String,
    val isVerified: Boolean,
    val operatingHours: OperatingHours,
    val deliveryRadius: Double,
    val averageRating: Double,
    val totalOrders: Int
)
```

### Mock Data por Nicho
- **Mock Restaurant**: "Restaurante La Havana" con datos específicos
- **Mock Market**: "Supermercado El Ahorro" con datos específicos
- **Mock Pharmacy**: "Farmacia San José" con datos específicos

## 🔧 Mejoras Técnicas Implementadas

### Refactor Mayor
1. **Eliminación de Driver**: App independiente, código eliminado
2. **Sistema de Nichos**: Arquitectura escalable por tipo de negocio
3. **Navegación Mejorada**: Basada en `BusinessType`
4. **LoginScreen Rediseñada**: Diseño moderno y profesional
5. **Archivos Legacy**: Movidos a .old para referencia

### Diseño UI/UX Mejorado
1. **Logo Circular**: Con sombra y bordes redondeados
2. **Scroll Inteligente**: Header fijo, contenido scrolleable
3. **Campos Mejorados**: Fondo gris claro (#EEEEEE) más visible
4. **Texto Reducido**: Chips de nichos con 14.sp
5. **Validación Visual**: Estados de error claros
6. **Elevaciones**: Sombras sutiles en botones y cards

### Consideraciones Técnicas
- Compilación exitosa con warnings menores
- Código compatible con KMP
- Preparado para animaciones futuras
- Estructura lista para backend integration

## 🏃‍♂️ Comandos de Desarrollo

### Compilación
```bash
# Compilar proyecto completo
./gradlew build

# Compilar solo commonMain (verificación rápida)
./gradlew composeApp:compileCommonMainKotlinMetadata

# Ejecutar en Android
./gradlew :composeApp:installDebug

# Ejecutar en Desktop
./gradlew :composeApp:run
```

### Estructura de Builds
- **Android**: Soporte completo con Material 3
- **iOS**: Framework generado (iosArm64, iosSimulatorArm64)
- **Desktop**: Aplicación nativa con distribución

## 📈 Estado Actual del Proyecto

### ✅ Completado Recientemente
- [x] Refactor completo a arquitectura de nichos
- [x] Sistema escalable para múltiples tipos de negocio
- [x] LoginScreen moderna con diseño mejorado
- [x] Pantallas de perfil específicas por nicho (Restaurant, Market)
- [x] Navegación dinámica basada en BusinessType
- [x] Formularios de login y registro profesionales
- [x] Campos de texto con mejor UX (fondo gris claro)
- [x] Selector visual de nichos con chips
- [x] Eliminación de código Driver (app independiente)
- [x] Mock data específico por cada nicho
- [x] Compilación exitosa del proyecto refactorizado
- [x] **Reorganización total de estructura** (Octubre 2025)
  - Renombrado `business/` → `app/` para mayor claridad
  - Eliminadas carpetas legacy (`business/`, `driver/`)
  - Estructura limpia y escalable por nichos
  - Todos los imports actualizados correctamente
  - Build exitoso verificado

### 🔄 Preparado para Implementación
- [ ] Pantallas de Dashboard por nicho
- [ ] Pantallas de Órdenes y gestión
- [ ] Pantallas específicas (Menú, Productos, Medicinas)
- [ ] Integración con backend GraphQL/REST
- [ ] Sistema de notificaciones
- [ ] Analytics por nicho
- [ ] Proceso de verificación de negocios
- [ ] Testing (Unit y UI tests)

### 🎯 Próximos Pasos Sugeridos
1. **Diseño de Dashboards**: Crear dashboards específicos por nicho
2. **Gestión de Órdenes**: Implementar flujo completo de pedidos
3. **Pantallas Específicas**:
   - Restaurant: Gestión de menú, categorías
   - Market: Inventario, productos
   - Pharmacy: Medicinas, prescripciones
4. **Backend Integration**: Conectar con APIs reales
5. **Testing Completo**: Unit tests y UI tests
6. **Deployment**: CI/CD y distribución

### 💡 Cómo Agregar un Nuevo Nicho

Es muy sencillo agregar un nuevo nicho al sistema:

1. **Agregar BusinessType**:
   ```kotlin
   enum class BusinessType {
       RESTAURANT,
       GROCERY,
       PHARMACY,
       FLOWER_SHOP  // Nuevo nicho
   }
   ```

2. **Crear carpeta de nicho**:
   ```
   nichos/flowershop/ui/screens/FlowerShopProfileScreen.kt
   ```

3. **Agregar rutas**:
   ```kotlin
   object FlowerShop {
       const val DASHBOARD = "flowershop/dashboard"
       const val PROFILE = "flowershop/profile"
       // ...
   }
   ```

4. **Actualizar navegación** en `App.kt`:
   ```kotlin
   BusinessType.FLOWER_SHOP -> FlowerShopProfileScreen(...)
   ```

5. **Agregar mock data** en `AuthRepository`

¡Y listo! El nuevo nicho está integrado.

## 🎉 Logros del Proyecto

- **Arquitectura Escalable**: Sistema de nichos fácil de extender
- **Código Limpio**: Separación clara de responsabilidades
- **Diseño Moderno**: LoginScreen profesional y atractivo
- **UX Mejorado**: Campos visuales, scroll inteligente, validación clara
- **Preparación Futura**: Estructura lista para backend y features avanzadas
- **Compilación Exitosa**: Proyecto funcional y estable
- **Flexibilidad**: Agregar nichos es rápido y simple

---

**Desarrollado con Kotlin Multiplatform + Compose Multiplatform + Material 3**
**Arquitectura: Multi-Nicho Escalable**
**Estado: ✅ ESTRUCTURA REORGANIZADA Y OPTIMIZADA - PROYECTO LIMPIO Y ESCALABLE**
**Última Actualización: 2025-10-13**