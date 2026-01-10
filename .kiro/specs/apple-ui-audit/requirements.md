# Requirements Document

## Introduction

Este documento define los requisitos para una auditoría y mejora completa de la interfaz de usuario de la aplicación Llego Business, aplicando los 12 principios de diseño Apple UI moderno. El objetivo es elevar la calidad visual y de experiencia de usuario manteniendo la identidad de marca Llego (colores primarios: teal #023133, beige #E1C78E, verde acento #B2D69A).

## Glossary

- **Design_System**: Sistema de diseño que define colores, tipografía, formas, elevaciones y componentes reutilizables
- **Screen**: Pantalla completa de la aplicación que representa una vista o funcionalidad específica
- **Component**: Elemento de UI reutilizable (botones, cards, inputs, etc.)
- **Visual_Hierarchy**: Organización visual que guía la atención del usuario hacia elementos importantes
- **Negative_Space**: Espacio vacío intencional que mejora legibilidad y sensación premium
- **Microanimation**: Animación sutil que proporciona feedback y continuidad visual
- **Apple_UI_Principles**: Los 12 principios de diseño Apple evaluados en este documento

## Pantallas a Evaluar

### Pantallas Identificadas en la Aplicación

1. **LoginScreen** - Pantalla de autenticación con OAuth (Google/Apple Sign-In)
2. **RegisterBusinessScreen** - Registro de nuevo negocio con formulario completo
3. **RestaurantHomeScreen** - Dashboard principal con navegación por tabs
4. **OrdersScreen** - Gestión de pedidos con filtros iOS-style
5. **MenuScreen** - Catálogo de productos con búsqueda
6. **MenuSearchScreen** - Búsqueda fullscreen de productos
7. **RestaurantProfileScreen** - Perfil del negocio editable
8. **BranchesManagementScreen** - Gestión de sucursales (seleccionar, editar, agregar, eliminar)
9. **WalletScreen** - Balance y transacciones financieras
10. **SettingsScreen** - Configuración de la aplicación
11. **AddProductScreen** - Formulario de creación/edición de productos
12. **OrderDetailScreen** - Detalle de pedido específico
13. **MapLocationPickerReal** - Selector de ubicación con Google Maps
14. **StatisticsScreen** - Métricas y estadísticas

---

## Requirements

### Requirement 1: Claridad Visual

**User Story:** Como usuario, quiero entender inmediatamente qué está pasando en cada pantalla, qué puedo hacer y cuál es la acción principal, para poder usar la app de forma intuitiva.

#### Acceptance Criteria

1. WHEN a user opens any screen, THE Screen SHALL display a single clear primary intention within the first 2 seconds of viewing
2. WHEN multiple actions are available, THE Screen SHALL visually distinguish the primary action from secondary actions through size, color, or position
3. WHEN a screen contains complex information, THE Design_System SHALL organize content in a logical flow from most to least important
4. THE Screen SHALL NOT display more than one primary call-to-action button in the main viewport

### Requirement 2: Jerarquía Visual Marcada

**User Story:** Como usuario, quiero que la información esté organizada visualmente de forma natural, para poder escanear y encontrar lo que necesito sin esfuerzo.

#### Acceptance Criteria

1. THE Design_System SHALL establish hierarchy using size, weight, spacing, contrast, and position rather than aggressive colors or oversized buttons
2. WHEN displaying titles, THE Typography SHALL use large size for context, medium for navigation, and small for details
3. THE Visual_Hierarchy SHALL follow the pattern: large title → light subtitle → clear primary action → discrete secondary actions
4. WHEN elements compete for attention, THE Design_System SHALL reduce visual weight of secondary elements

### Requirement 3: Tipografía como Estructura

**User Story:** Como usuario, quiero que la tipografía me guíe silenciosamente a través de la interfaz, organizando la información de forma clara.

#### Acceptance Criteria

1. THE Design_System SHALL use a neutral sans-serif font family (San Francisco equivalent) consistently across all screens
2. THE Typography SHALL maintain consistent line-height and letter-spacing across all text styles
3. THE Design_System SHALL limit typography variations to a maximum of 4 distinct styles per screen
4. WHEN text is used for navigation, THE Typography SHALL use medium weight and size
5. WHEN text is used for details, THE Typography SHALL use lighter weight and smaller size

### Requirement 4: Espacio Negativo Premium

**User Story:** Como usuario, quiero que la interfaz se sienta espaciosa y premium, reduciendo el estrés visual y mejorando la legibilidad.

#### Acceptance Criteria

1. THE Design_System SHALL use generous padding (minimum 16dp) between major content sections
2. WHEN in doubt between adding an element or leaving space, THE Screen SHALL leave the space empty
3. THE Component SHALL maintain consistent internal padding (minimum 12dp) for touch targets
4. THE Screen SHALL NOT crowd more than 3 major content blocks in the visible viewport without scrolling
5. THE Design_System SHALL use minimum 24dp spacing between unrelated content groups

### Requirement 5: Microanimaciones Explicativas

**User Story:** Como usuario, quiero que las animaciones me ayuden a entender las transiciones y el flujo de la aplicación, no solo a impresionarme.

#### Acceptance Criteria

1. WHEN navigating between screens, THE Microanimation SHALL show visual continuity (origin and destination)
2. WHEN a button is pressed, THE Component SHALL respond with subtle visual feedback within 100ms
3. WHEN content loads, THE Screen SHALL use smooth fade-in animations (300-600ms duration)
4. THE Microanimation SHALL NOT exceed 400ms for interactive feedback
5. WHEN elements appear or disappear, THE Microanimation SHALL use slide and fade combinations

### Requirement 6: Consistencia Obsesiva

**User Story:** Como usuario, quiero que toda la aplicación se sienta diseñada por una sola mente, con comportamientos predecibles.

#### Acceptance Criteria

1. THE Component SHALL behave identically across all screens (same button styles, same interactions)
2. THE Design_System SHALL use consistent icon styles (same weight, same corner radius)
3. THE Design_System SHALL maintain identical spacing rules between similar elements across all screens
4. THE Design_System SHALL use consistent border radius (8dp, 12dp, 16dp, 20dp, 28dp) across all components
5. THE Design_System SHALL use consistent shadow/elevation values across similar component types

### Requirement 7: Controles Discretos

**User Story:** Como usuario, quiero que los controles sean sutiles y no invasivos, permitiéndome enfocarme en el contenido.

#### Acceptance Criteria

1. THE Component SHALL use flat or softly filled buttons rather than heavy 3D effects
2. THE Design_System SHALL minimize shadow usage (maximum level 2-3 for most components)
3. THE Component SHALL use thin borders (1-1.5dp) when borders are necessary
4. WHEN displaying lists, THE Screen SHALL use clean layouts with contextual actions rather than button-heavy panels
5. THE Design_System SHALL avoid heavy visual weight in navigation elements

### Requirement 8: Color con Propósito

**User Story:** Como usuario, quiero que el color tenga significado y no sea solo decoración, ayudándome a identificar acciones y estados importantes.

#### Acceptance Criteria

1. THE Design_System SHALL use the accent color (teal #023133) exclusively for primary actions and key interactive elements
2. THE Design_System SHALL use neutral colors for structural elements and backgrounds
3. THE Screen SHALL NOT use more than 3 distinct colors (excluding neutrals) in any single view
4. WHEN indicating status, THE Design_System SHALL use consistent color coding (success=green, error=red, warning=orange)
5. THE Design_System SHALL maintain brand colors (teal, beige, green accent) without overwhelming the UI

### Requirement 9: Iconografía Simple y Geométrica

**User Story:** Como usuario, quiero iconos claros y universales que comuniquen su función sin ambigüedad.

#### Acceptance Criteria

1. THE Design_System SHALL use icons with consistent stroke weight across all instances
2. THE Design_System SHALL use rounded, geometric icon shapes
3. THE Design_System SHALL avoid detailed or decorative icons
4. THE Component SHALL use universally recognized symbols for common actions
5. THE Design_System SHALL maintain consistent icon sizes (20dp, 24dp, 32dp) based on context

### Requirement 10: Profundidad Elegante

**User Story:** Como usuario, quiero percibir capas y profundidad en la interfaz de forma sutil, sin efectos pesados o anticuados.

#### Acceptance Criteria

1. THE Design_System SHALL use subtle transparency and blur effects for overlays and modals
2. THE Design_System SHALL create hierarchy through layering rather than heavy shadows
3. THE Component SHALL use very soft shadows (opacity < 10%) for elevation
4. WHEN displaying modals or sheets, THE Screen SHALL use frosted glass effect or subtle backdrop blur
5. THE Design_System SHALL avoid skeuomorphic or realistic textures

### Requirement 11: Menos Opciones Visibles

**User Story:** Como usuario, quiero ver primero lo esencial y acceder a opciones avanzadas solo cuando las necesite.

#### Acceptance Criteria

1. THE Screen SHALL display only essential options in the primary view
2. WHEN advanced options exist, THE Screen SHALL hide them behind progressive disclosure (expandable sections, sheets)
3. THE Design_System SHALL simplify configuration screens to show only the most common settings first
4. THE Screen SHALL NOT display more than 5 primary action options in any single view
5. WHEN filtering or sorting, THE Component SHALL use collapsible or sheet-based interfaces

### Requirement 12: Accesibilidad Integrada

**User Story:** Como usuario, quiero que la aplicación funcione bien en diferentes condiciones (tamaños de texto, contraste, modo oscuro).

#### Acceptance Criteria

1. THE Design_System SHALL maintain minimum contrast ratio of 4.5:1 for all text
2. THE Design_System SHALL support dynamic type scaling without breaking layouts
3. THE Component SHALL have minimum touch target size of 44dp x 44dp
4. THE Design_System SHALL prepare color schemes for future Dark Mode support
5. THE Screen SHALL maintain readability and usability with large font settings enabled

---

## Evaluación por Pantalla

### Requirement 13: Auditoría LoginScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de login según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE LoginScreen SHALL display a single clear call-to-action for authentication
2. THE LoginScreen SHALL use generous vertical spacing between logo, welcome text, and auth buttons
3. THE LoginScreen SHALL animate the card entrance smoothly from bottom
4. THE LoginScreen SHALL maintain brand identity while following Apple UI principles
5. THE LoginScreen SHALL reduce visual complexity in the tips section

### Requirement 14: Auditoría RegisterBusinessScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de registro de negocio según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE RegisterBusinessScreen SHALL organize form sections with clear visual separation and headers
2. THE RegisterBusinessScreen SHALL use progressive disclosure for optional fields
3. THE RegisterBusinessScreen SHALL display image upload areas with clear visual affordance
4. THE RegisterBusinessScreen SHALL maintain consistent spacing between form groups (minimum 20dp)
5. THE RegisterBusinessScreen SHALL show validation feedback subtly without overwhelming the user

### Requirement 15: Auditoría RestaurantHomeScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla principal según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE RestaurantHomeScreen SHALL display the business name prominently with clear hierarchy
2. THE RestaurantHomeScreen SHALL use a clean bottom navigation with consistent icon styles
3. THE RestaurantHomeScreen SHALL show badge counts subtly without overwhelming the navigation
4. THE RestaurantHomeScreen SHALL maintain consistent spacing in the top app bar
5. THE RestaurantHomeScreen SHALL use the primary color sparingly for key actions only

### Requirement 16: Auditoría OrdersScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de pedidos según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE OrdersScreen SHALL display filters in a clean, iOS-style picker interface
2. THE OrdersScreen SHALL use subtle card elevation for order items
3. THE OrdersScreen SHALL show order status with color-coded badges that don't overwhelm
4. THE OrdersScreen SHALL animate filter changes smoothly
5. THE OrdersScreen SHALL maintain generous spacing between order cards

### Requirement 17: Auditoría MenuScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de menú según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE MenuScreen SHALL display category filters in a horizontally scrollable, clean chip design
2. THE MenuScreen SHALL use consistent product card layouts with clear hierarchy
3. THE MenuScreen SHALL position floating action buttons with appropriate spacing from edges
4. THE MenuScreen SHALL animate search overlay transitions smoothly
5. THE MenuScreen SHALL show product availability status subtly without heavy visual indicators

### Requirement 18: Auditoría MenuSearchScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de búsqueda de productos según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE MenuSearchScreen SHALL display a prominent search field with clear focus state
2. THE MenuSearchScreen SHALL show search results with consistent card styling
3. THE MenuSearchScreen SHALL animate the transition from/to the main menu smoothly
4. THE MenuSearchScreen SHALL provide clear empty state messaging when no results found
5. THE MenuSearchScreen SHALL allow easy dismissal with back gesture or button

### Requirement 19: Auditoría RestaurantProfileScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de perfil según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE RestaurantProfileScreen SHALL organize sections with clear visual separation
2. THE RestaurantProfileScreen SHALL use consistent card styling for all information sections
3. THE RestaurantProfileScreen SHALL display editable fields with subtle edit indicators
4. THE RestaurantProfileScreen SHALL maintain generous padding within each section
5. THE RestaurantProfileScreen SHALL use icons consistently across all section headers

### Requirement 20: Auditoría BranchesManagementScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de gestión de sucursales según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE BranchesManagementScreen SHALL display branch cards with clear active/inactive visual distinction
2. THE BranchesManagementScreen SHALL use subtle elevation and borders for the active branch card
3. THE BranchesManagementScreen SHALL organize branch information with consistent icon usage
4. THE BranchesManagementScreen SHALL position action buttons (edit, delete) discretely
5. THE BranchesManagementScreen SHALL animate branch selection feedback smoothly

### Requirement 21: Auditoría WalletScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de wallet según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE WalletScreen SHALL display balance prominently with clear currency distinction
2. THE WalletScreen SHALL organize quick actions in a clean grid layout
3. THE WalletScreen SHALL show transaction history with subtle visual hierarchy
4. THE WalletScreen SHALL use consistent card styling for all sections
5. THE WalletScreen SHALL animate balance and transaction updates smoothly

### Requirement 22: Auditoría SettingsScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de configuración según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE SettingsScreen SHALL group related settings in clearly labeled sections
2. THE SettingsScreen SHALL use consistent row styling for all setting items
3. THE SettingsScreen SHALL display switches and toggles with appropriate sizing
4. THE SettingsScreen SHALL maintain visual hierarchy between section titles and items
5. THE SettingsScreen SHALL use destructive styling only for dangerous actions

### Requirement 23: Auditoría AddProductScreen

**User Story:** Como diseñador, quiero evaluar y mejorar la pantalla de agregar producto según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE AddProductScreen SHALL organize form fields in logical groups with clear section headers
2. THE AddProductScreen SHALL use consistent input field styling throughout
3. THE AddProductScreen SHALL display the save/cancel actions in a fixed bottom bar
4. THE AddProductScreen SHALL show image upload area with clear visual affordance
5. THE AddProductScreen SHALL animate form validation feedback subtly

### Requirement 24: Auditoría MapLocationPickerReal

**User Story:** Como diseñador, quiero evaluar y mejorar el selector de ubicación según los 12 principios Apple UI.

#### Acceptance Criteria

1. THE MapLocationPickerReal SHALL display the map with clear visual boundaries
2. THE MapLocationPickerReal SHALL show selected coordinates in a clean, readable format
3. THE MapLocationPickerReal SHALL animate the fullscreen dialog transition smoothly
4. THE MapLocationPickerReal SHALL provide clear confirm/cancel actions in the bottom bar
5. THE MapLocationPickerReal SHALL maintain consistent styling with the rest of the app

---

## Plan de Implementación

### Fase 1: Design System Foundation
- Actualizar Typography.kt con escalas Apple-like
- Refinar Color.kt para uso más restringido del color
- Actualizar Shape.kt con radios consistentes
- Ajustar Elevation.kt para sombras más sutiles

### Fase 2: Componentes Base
- Refactorizar LlegoButton para estilos más discretos
- Actualizar LlegoTextField con diseño más limpio
- Crear componentes de card consistentes
- Estandarizar iconografía

### Fase 3: Pantallas de Autenticación y Registro
- LoginScreen
- RegisterBusinessScreen

### Fase 4: Pantallas Principales
- RestaurantHomeScreen
- OrdersScreen
- MenuScreen + MenuSearchScreen

### Fase 5: Pantallas Secundarias
- RestaurantProfileScreen
- BranchesManagementScreen
- WalletScreen
- SettingsScreen
- AddProductScreen

### Fase 6: Componentes Especiales
- MapLocationPickerReal
- OrderDetailScreen
- StatisticsScreen
- RestaurantHomeScreen
- OrdersScreen
- MenuScreen + MenuSearchScreen

### Fase 5: Pantallas Secundarias
- RestaurantProfileScreen
- WalletScreen
- SettingsScreen
- AddProductScreen

### Fase 6: Componentes Especiales
- MapLocationPickerReal
- OrderDetailScreen
- StatisticsScreen
