# Design Document: Apple UI Audit & Improvement

## Overview

Este documento detalla el diseño de mejoras para la aplicación Llego Business, aplicando los 12 principios de diseño Apple UI moderno. El objetivo es elevar la calidad visual y de experiencia de usuario manteniendo la identidad de marca Llego.

### Principios de Diseño Apple UI

1. **Claridad Visual** - Una pantalla = una intención principal
2. **Jerarquía Marcada** - Usando tamaño, peso, espaciado, contraste y posición
3. **Tipografía como Estructura** - Guía silenciosa, no decoración
4. **Espacio Negativo** - Aire como elemento de diseño premium
5. **Microanimaciones Explicativas** - Animaciones que explican, no impresionan
6. **Consistencia Obsesiva** - Todo diseñado por una sola mente
7. **Controles Discretos** - UI ligera, no invasiva
8. **Color con Propósito** - Color = significado, no decoración
9. **Iconografía Simple** - Geométrica, consistente, universal
10. **Profundidad Elegante** - Capas sutiles sin skeuomorfismo
11. **Menos Opciones Visibles** - Esencial primero, avanzado después
12. **Accesibilidad Integrada** - Funciona bien en todas las condiciones

---

## Architecture

### Design System Structure

```
shared/ui/theme/
├── Color.kt          # Paleta de colores refinada
├── Typography.kt     # Sistema tipográfico Apple-like
├── Shape.kt          # Radios de borde consistentes
├── Elevation.kt      # Sombras sutiles
└── Theme.kt          # Configuración del tema
```

### Component Hierarchy

```
shared/ui/components/
├── atoms/
│   ├── LlegoButton.kt       # Botones discretos
│   ├── LlegoTextField.kt    # Inputs limpios
│   └── LlegoIcon.kt         # Iconos consistentes
├── molecules/
│   ├── LlegoCard.kt         # Cards con elevación sutil
│   ├── LlegoListItem.kt     # Items de lista limpios
│   └── LlegoChip.kt         # Chips de filtro
└── organisms/
    ├── LlegoTopBar.kt       # Barra superior consistente
    ├── LlegoBottomNav.kt    # Navegación inferior
    └── LlegoSheet.kt        # Bottom sheets con blur
```

---

## Components and Interfaces

### 1. Design System Foundation

#### Typography System (Apple-like)

```kotlin
// Escala tipográfica refinada
object AppleTypography {
    // Display - Para títulos de pantalla
    val displayLarge = 34.sp, Bold, -0.25 letterSpacing
    val displayMedium = 28.sp, Bold, 0 letterSpacing
    val displaySmall = 24.sp, SemiBold, 0 letterSpacing
    
    // Title - Para secciones
    val titleLarge = 22.sp, SemiBold, 0 letterSpacing
    val titleMedium = 17.sp, SemiBold, 0 letterSpacing
    val titleSmall = 15.sp, SemiBold, 0 letterSpacing
    
    // Body - Para contenido
    val bodyLarge = 17.sp, Regular, 0 letterSpacing
    val bodyMedium = 15.sp, Regular, 0 letterSpacing
    val bodySmall = 13.sp, Regular, 0 letterSpacing
    
    // Label - Para botones y captions
    val labelLarge = 15.sp, Medium, 0.5 letterSpacing
    val labelMedium = 13.sp, Medium, 0.5 letterSpacing
    val labelSmall = 11.sp, Medium, 0.5 letterSpacing
}
```

#### Color System (Restringido)

```kotlin
// Colores con propósito
object AppleColors {
    // Brand (usar con moderación)
    val primary = Color(0xFF023133)      // Teal - Solo acciones principales
    val secondary = Color(0xFFE1C78E)    // Beige - Acentos secundarios
    val accent = Color(0xFFB2D69A)       // Verde - Estados de éxito
    
    // Neutrals (uso principal)
    val background = Color(0xFFF8F9FA)   // Fondo general
    val surface = Color(0xFFFFFFFF)      // Cards y superficies
    val surfaceVariant = Color(0xFFF3F3F3) // Fondos secundarios
    
    // Text
    val onBackground = Color(0xFF1B1B1B) // Texto principal
    val onSurfaceVariant = Color(0xFF6B7280) // Texto secundario
    val onSurfaceTertiary = Color(0xFF9CA3AF) // Texto terciario
    
    // Status (consistentes)
    val success = Color(0xFF34C759)      // Verde iOS
    val warning = Color(0xFFFF9500)      // Naranja iOS
    val error = Color(0xFFFF3B30)        // Rojo iOS
    val info = Color(0xFF007AFF)         // Azul iOS
}
```

#### Shape System (Consistente)

```kotlin
// Radios de borde estandarizados
object AppleShapes {
    val extraSmall = 8.dp   // Chips, badges
    val small = 12.dp       // Inputs, botones secundarios
    val medium = 16.dp      // Cards estándar
    val large = 20.dp       // Cards destacadas
    val extraLarge = 28.dp  // Sheets, modals
    val circular = 50%      // Avatares, indicadores
}
```

#### Elevation System (Sutil)

```kotlin
// Sombras muy sutiles
object AppleElevation {
    val none = 0.dp
    val subtle = 1.dp       // Cards en reposo
    val low = 2.dp          // Cards hover/focus
    val medium = 4.dp       // Dropdowns, popovers
    val high = 8.dp         // Modals, sheets
    
    // Opacidad de sombra: máximo 8%
}
```

### 2. Component Specifications

#### LlegoButton (Discreto)

```kotlin
// Botón primario - Relleno sutil
PrimaryButton {
    containerColor = primary
    contentColor = white
    shape = RoundedCornerShape(12.dp)
    elevation = 0.dp  // Sin sombra
    height = 50.dp
    horizontalPadding = 24.dp
}

// Botón secundario - Outline sutil
SecondaryButton {
    containerColor = transparent
    contentColor = primary
    borderColor = primary.copy(alpha = 0.3f)
    borderWidth = 1.dp
    shape = RoundedCornerShape(12.dp)
}

// Botón terciario - Solo texto
TertiaryButton {
    containerColor = transparent
    contentColor = primary
    // Sin borde
}
```

#### LlegoCard (Elevación Sutil)

```kotlin
// Card estándar
StandardCard {
    containerColor = white
    shape = RoundedCornerShape(16.dp)
    elevation = 1.dp
    shadowColor = black.copy(alpha = 0.06f)
    padding = 16.dp
}

// Card seleccionada
SelectedCard {
    containerColor = primary.copy(alpha = 0.05f)
    borderColor = primary.copy(alpha = 0.2f)
    borderWidth = 1.5.dp
}
```

#### LlegoTextField (Limpio)

```kotlin
// Input estándar
StandardInput {
    containerColor = surfaceVariant
    unfocusedBorderColor = transparent
    focusedBorderColor = primary.copy(alpha = 0.5f)
    shape = RoundedCornerShape(12.dp)
    padding = 16.dp
    labelStyle = bodySmall, onSurfaceVariant
}
```

---

## Data Models

### Spacing Constants

```kotlin
object AppleSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    
    // Espaciado entre secciones
    val sectionGap = 24.dp
    
    // Padding de pantalla
    val screenHorizontal = 20.dp
    val screenVertical = 16.dp
}
```

### Animation Constants

```kotlin
object AppleAnimations {
    // Duraciones
    val instant = 100.ms      // Feedback táctil
    val fast = 200.ms         // Transiciones rápidas
    val normal = 300.ms       // Transiciones estándar
    val slow = 500.ms         // Animaciones de entrada
    
    // Easings
    val easeOut = EaseOutCubic
    val easeIn = EaseInCubic
    val spring = Spring(dampingRatio = 0.8f, stiffness = 300f)
}
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Contrast Ratio Compliance

*For any* text color and background color combination defined in the Design_System, the contrast ratio SHALL be at least 4.5:1 for normal text and 3:1 for large text (18sp+ or 14sp+ bold).

**Validates: Requirements 12.1**

### Property 2: Touch Target Size Compliance

*For any* interactive component (Button, IconButton, Chip, ListItem), the minimum touch target size SHALL be 44dp x 44dp.

**Validates: Requirements 12.3**

---

## Screen-by-Screen Improvements

### LoginScreen

**Current Issues:**
- Gradient text puede reducir legibilidad
- Tips section agrega complejidad visual innecesaria
- Animaciones de entrada pueden ser más sutiles

**Improvements:**
```kotlin
// Antes: Gradient text
Text(
    style = MaterialTheme.typography.headlineMedium.copy(
        brush = Brush.horizontalGradient(...)
    )
)

// Después: Color sólido con jerarquía clara
Text(
    text = "Bienvenido a Llego",
    style = AppleTypography.displayMedium,
    color = AppleColors.primary
)
Text(
    text = "Inicia sesión para gestionar tu negocio",
    style = AppleTypography.bodyMedium,
    color = AppleColors.onSurfaceVariant
)
```

**Cambios específicos:**
1. Remover gradient del título - usar color sólido primary
2. Simplificar o remover AppTipsSection
3. Reducir espaciado entre elementos (48dp → 32dp)
4. Suavizar animación de entrada del card (spring más suave)
5. Aumentar padding horizontal del card (24dp → 28dp)

### RestaurantHomeScreen

**Current Issues:**
- TopAppBar con múltiples iconos puede sentirse pesada
- Badge counts pueden ser muy prominentes
- Bottom navigation necesita refinamiento

**Improvements:**
```kotlin
// TopAppBar más limpia
TopAppBar(
    title = {
        Text(
            text = businessName,
            style = AppleTypography.titleLarge,
            color = AppleColors.onBackground
        )
    },
    actions = {
        // Iconos más sutiles, sin tint agresivo
        IconButton(onClick = onNavigateToChats) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                tint = AppleColors.onSurfaceVariant
            )
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = AppleColors.surface
    )
)
```

**Cambios específicos:**
1. Usar iconos Outlined en lugar de Filled para acciones secundarias
2. Reducir tamaño de badges (18dp → 14dp)
3. Simplificar bottom navigation - remover indicador de selección pesado
4. Usar colores más sutiles para items no seleccionados

### OrdersScreen

**Current Issues:**
- Filtros iOS-style ya están bien implementados
- Cards de pedidos pueden tener menos elementos visuales
- Status badges pueden ser más sutiles

**Improvements:**
```kotlin
// Status badge más sutil
Surface(
    shape = RoundedCornerShape(8.dp),
    color = statusColor.copy(alpha = 0.1f)
) {
    Text(
        text = status.displayName,
        style = AppleTypography.labelSmall,
        color = statusColor,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
```

**Cambios específicos:**
1. Reducir border width de status badges (1.5dp → 0dp, solo background)
2. Simplificar OrderCard - remover icono de moto si no es necesario
3. Reducir elevación de cards (2dp → 1dp)
4. Aumentar espaciado entre cards (12dp → 16dp)

### MenuScreen

**Current Issues:**
- FABs pueden ser más discretos
- Category chips están bien pero pueden refinarse
- Product cards necesitan jerarquía más clara

**Improvements:**
```kotlin
// FAB más discreto
FloatingActionButton(
    onClick = { ... },
    containerColor = AppleColors.primary,
    elevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp
    ),
    shape = CircleShape
) {
    Icon(
        Icons.Default.Add,
        modifier = Modifier.size(22.dp)
    )
}
```

**Cambios específicos:**
1. Reducir tamaño de FABs (56dp → 52dp)
2. Usar solo un FAB (combinar search + add en menú contextual)
3. Simplificar product cards - menos información visible
4. Mejorar empty state con ilustración más sutil

### RestaurantProfileScreen

**Current Issues:**
- Muchas secciones pueden abrumar
- Cards con demasiada información
- Iconos en headers pueden ser redundantes

**Improvements:**
```kotlin
// Sección más limpia
Column(
    modifier = Modifier.padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    Text(
        text = "Información del Negocio",
        style = AppleTypography.titleMedium,
        color = AppleColors.onBackground
    )
    // Sin icono en el header
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppleColors.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        // Contenido
    }
}
```

**Cambios específicos:**
1. Remover iconos de headers de sección
2. Usar progressive disclosure - colapsar secciones menos usadas
3. Reducir padding interno de cards (16dp → 14dp)
4. Simplificar social links section

### BranchesManagementScreen

**Current Issues:**
- Cards de sucursal con demasiados elementos
- Botones de acción muy prominentes
- Estado activo puede ser más sutil

**Improvements:**
```kotlin
// Card de sucursal más limpia
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (isActive) 
            AppleColors.primary.copy(alpha = 0.05f)
        else 
            AppleColors.surface
    ),
    border = if (isActive) 
        BorderStroke(1.dp, AppleColors.primary.copy(alpha = 0.2f))
    else 
        null
) {
    // Contenido simplificado
}
```

**Cambios específicos:**
1. Reducir prominencia del badge "ACTIVA"
2. Mover botones edit/delete a menú contextual (long press o swipe)
3. Simplificar información mostrada - solo nombre, dirección, estado
4. Usar indicador de estado más sutil (punto de color)

### WalletScreen

**Current Issues:**
- Balance cards pueden ser más limpias
- Quick actions grid puede simplificarse
- Demasiada información visible

**Improvements:**
```kotlin
// Balance card más limpia
Card(
    colors = CardDefaults.cardColors(
        containerColor = AppleColors.primary
    ),
    shape = RoundedCornerShape(20.dp)
) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "Balance",
            style = AppleTypography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = formatCurrency(balance),
            style = AppleTypography.displayLarge,
            color = Color.White
        )
    }
}
```

**Cambios específicos:**
1. Mostrar solo una moneda a la vez (selector de moneda arriba)
2. Reducir quick actions a 2 principales (Retirar, Historial)
3. Simplificar transaction items
4. Remover info cards redundantes

### SettingsScreen

**Current Issues:**
- Secciones con demasiado padding
- Switches pueden ser más sutiles
- Iconos en cada row pueden ser excesivos

**Improvements:**
```kotlin
// Setting row más limpia
Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Column {
        Text(
            text = title,
            style = AppleTypography.bodyLarge
        )
        Text(
            text = subtitle,
            style = AppleTypography.bodySmall,
            color = AppleColors.onSurfaceVariant
        )
    }
    Icon(
        Icons.Default.ChevronRight,
        tint = AppleColors.onSurfaceTertiary,
        modifier = Modifier.size(20.dp)
    )
}
```

**Cambios específicos:**
1. Remover iconos de cada setting row (solo chevron)
2. Usar dividers más sutiles entre items
3. Reducir padding de secciones
4. Simplificar switch styling

### AddProductScreen

**Current Issues:**
- Formulario puede sentirse largo
- Secciones de variantes complejas
- Bottom bar puede ser más sutil

**Improvements:**
```kotlin
// Bottom bar más limpia
Surface(
    color = AppleColors.surface,
    shadowElevation = 4.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancelar")
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Text("Guardar")
        }
    }
}
```

**Cambios específicos:**
1. Usar progressive disclosure para campos opcionales
2. Simplificar selector de tipo de producto
3. Reducir complejidad de sección de variantes
4. Mejorar image upload preview

### MapLocationPickerReal

**Current Issues:**
- Dialog fullscreen puede tener mejor transición
- Bottom bar puede ser más integrada
- Coordenadas pueden mostrarse de forma más limpia

**Improvements:**
```kotlin
// Bottom bar integrada con el mapa
Surface(
    color = AppleColors.surface.copy(alpha = 0.95f),
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        // Coordenadas en formato legible
        Text(
            text = "Ubicación seleccionada",
            style = AppleTypography.labelMedium,
            color = AppleColors.onSurfaceVariant
        )
        Text(
            text = formatAddress(lat, lng),
            style = AppleTypography.bodyLarge
        )
        // Botones
    }
}
```

**Cambios específicos:**
1. Usar blur effect en bottom bar
2. Mostrar dirección aproximada en lugar de coordenadas
3. Simplificar botones (solo Confirmar, X para cerrar)
4. Mejorar animación de entrada/salida

---

## Error Handling

### Visual Error States

```kotlin
// Error sutil en inputs
OutlinedTextField(
    isError = hasError,
    colors = OutlinedTextFieldDefaults.colors(
        errorBorderColor = AppleColors.error.copy(alpha = 0.5f),
        errorLabelColor = AppleColors.error
    )
)

// Error message
if (hasError) {
    Text(
        text = errorMessage,
        style = AppleTypography.labelSmall,
        color = AppleColors.error,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
    )
}
```

### Loading States

```kotlin
// Loading indicator sutil
CircularProgressIndicator(
    modifier = Modifier.size(20.dp),
    color = AppleColors.primary,
    strokeWidth = 2.dp
)
```

---

## Testing Strategy

### Unit Tests

- Verificar valores de constantes en Design System
- Verificar que colores de estado están definidos
- Verificar que shapes usan valores estándar

### Property-Based Tests

- **Property 1**: Contrast ratio compliance para todas las combinaciones de color
- **Property 2**: Touch target size compliance para todos los componentes interactivos

### Visual Regression Tests

- Capturas de pantalla de cada screen en diferentes estados
- Comparación antes/después de cambios

### Accessibility Tests

- Verificar contraste con herramientas automatizadas
- Verificar touch targets con Accessibility Scanner
- Verificar soporte de TalkBack/VoiceOver

---

## Implementation Priority

### Phase 1: Design System (Alta prioridad)
1. Typography.kt - Escala Apple-like
2. Color.kt - Paleta restringida
3. Shape.kt - Radios consistentes
4. Elevation.kt - Sombras sutiles

### Phase 2: Core Components (Alta prioridad)
1. LlegoButton - Estilos discretos
2. LlegoTextField - Diseño limpio
3. LlegoCard - Elevación sutil

### Phase 3: Main Screens (Media prioridad)
1. LoginScreen
2. RestaurantHomeScreen
3. OrdersScreen
4. MenuScreen

### Phase 4: Secondary Screens (Media prioridad)
1. RestaurantProfileScreen
2. BranchesManagementScreen
3. WalletScreen
4. SettingsScreen
5. AddProductScreen

### Phase 5: Special Components (Baja prioridad)
1. MapLocationPickerReal
2. OrderDetailScreen
3. StatisticsScreen
