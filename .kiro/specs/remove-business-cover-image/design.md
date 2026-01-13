# Design Document: Remove Business Cover Image

## Overview

Este documento describe el diseño técnico para adaptar el frontend a la eliminación del campo `coverImage` del modelo `Business` en el backend. Los cambios afectan el modelo de datos, esquema GraphQL, queries/mutations, servicios de upload de imágenes, y componentes de UI. La funcionalidad de cover image para sucursales (Branch) permanece sin cambios.

## Architecture

### Capas Afectadas

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │RegisterBusiness │  │ ProfileScreens  │                   │
│  │    Screen       │  │ (Restaurant/    │                   │
│  │                 │  │  Market)        │                   │
│  │ ❌ Remove cover │  │ ❌ Remove cover │                   │
│  │    upload       │  │    display      │                   │
│  └─────────────────┘  └─────────────────┘                   │
├─────────────────────────────────────────────────────────────┤
│                     Service Layer                            │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              ImageUploadService                          ││
│  │  ❌ Remove uploadBusinessCover() method                  ││
│  │  ✅ Keep uploadBranchCover() method                      ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │  BusinessModels │  │ BusinessMappers │                   │
│  │                 │  │                 │                   │
│  │ ❌ coverImage   │  │ ❌ map coverUrl │                   │
│  │ ❌ coverUrl     │  │                 │                   │
│  └─────────────────┘  └─────────────────┘                   │
├─────────────────────────────────────────────────────────────┤
│                    GraphQL Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │  schema.graphqls│  │  .graphql files │                   │
│  │                 │  │                 │                   │
│  │ ❌ BusinessType │  │ ❌ GetBusiness  │                   │
│  │    coverImage   │  │ ❌ GetBusinesses│                   │
│  │    coverUrl     │  │ ❌ RegisterBiz  │                   │
│  │ ❌ Inputs       │  │ ❌ UpdateBiz    │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Data Models (BusinessModels.kt)

**Cambios en Business data class:**

```kotlin
// ANTES
data class Business(
    val id: String,
    val name: String,
    val ownerId: String,
    val globalRating: Double = 0.0,
    val avatar: String? = null,
    val coverImage: String? = null,  // ❌ ELIMINAR
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null     // ❌ ELIMINAR
)

// DESPUÉS
data class Business(
    val id: String,
    val name: String,
    val ownerId: String,
    val globalRating: Double = 0.0,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val avatarUrl: String? = null
)
```

**Cambios en CreateBusinessInput:**

```kotlin
// ANTES
data class CreateBusinessInput(
    val name: String,
    val avatar: String? = null,
    val coverImage: String? = null,  // ❌ ELIMINAR
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String>? = null
)

// DESPUÉS
data class CreateBusinessInput(
    val name: String,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String>? = null
)
```

**Cambios en UpdateBusinessInput:**

```kotlin
// ANTES
data class UpdateBusinessInput(
    val name: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,  // ❌ ELIMINAR
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String>? = null,
    val isActive: Boolean? = null
)

// DESPUÉS
data class UpdateBusinessInput(
    val name: String? = null,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String>? = null,
    val isActive: Boolean? = null
)
```

### 2. GraphQL Schema (schema.graphqls)

**Cambios en BusinessType:**

```graphql
# ELIMINAR estos campos de BusinessType:
type BusinessType {
  # ... otros campos ...
  # coverImage: String     ❌ ELIMINAR
  # coverUrl: String       ❌ ELIMINAR
}
```

**Cambios en Inputs:**

```graphql
# ELIMINAR coverImage de CreateBusinessInput:
input CreateBusinessInput {
  name: String!
  avatar: String = null
  # coverImage: String = null  ❌ ELIMINAR
  description: String = null
  # ...
}

# ELIMINAR coverImage de UpdateBusinessInput:
input UpdateBusinessInput {
  name: String = null
  avatar: String = null
  # coverImage: String = null  ❌ ELIMINAR
  description: String = null
  # ...
}
```

### 3. GraphQL Query Files

**GetBusiness.graphql:**
```graphql
query GetBusiness($id: String!, $jwt: String) {
  business(id: $id, jwt: $jwt) {
    id
    name
    description
    avatar
    # coverImage  ❌ ELIMINAR
    tags
    globalRating
    avatarUrl
    # coverUrl    ❌ ELIMINAR
  }
}
```

**GetBusinesses.graphql:**
```graphql
query GetBusinesses($ownerId: String, $jwt: String) {
  businesses(ownerId: $ownerId, jwt: $jwt) {
    id
    name
    avatar
    # coverImage  ❌ ELIMINAR
    globalRating
    isActive
    avatarUrl
    # coverUrl    ❌ ELIMINAR (si existe)
  }
}
```

**RegisterBusiness.graphql:**
```graphql
mutation RegisterBusiness(...) {
  registerBusiness(...) {
    id
    name
    avatar
    # coverImage  ❌ ELIMINAR
    avatarUrl
    # coverUrl    ❌ ELIMINAR
  }
}
```

**UpdateBusiness.graphql:**
```graphql
mutation UpdateBusiness(...) {
  updateBusiness(...) {
    id
    name
    description
    avatar
    # coverImage  ❌ ELIMINAR
    tags
    isActive
    avatarUrl
    # coverUrl    ❌ ELIMINAR
  }
}
```

### 4. ImageUploadService Interface

**Cambios en ImageUploadService.kt:**

```kotlin
// ELIMINAR este método de la interfaz:
interface ImageUploadService {
    suspend fun uploadUserAvatar(filePath: String, token: String?): ImageUploadResult
    suspend fun uploadBusinessAvatar(filePath: String, token: String?): ImageUploadResult
    // suspend fun uploadBusinessCover(filePath: String, token: String?): ImageUploadResult  ❌ ELIMINAR
    suspend fun uploadBranchAvatar(filePath: String, token: String?): ImageUploadResult
    suspend fun uploadBranchCover(filePath: String, token: String?): ImageUploadResult  // ✅ MANTENER
    suspend fun uploadProductImage(filePath: String, token: String?): ImageUploadResult
}
```

**Implementaciones a modificar:**
- `ImageUploadService.android.kt` - Eliminar `uploadBusinessCover()`
- `ImageUploadService.ios.kt` - Eliminar `uploadBusinessCover()`
- `ImageUploadService.jvm.kt` - Eliminar `uploadBusinessCover()`

### 5. Business Mappers (BusinessMappers.kt)

**Cambios en funciones de mapeo:**

```kotlin
// Todas las funciones que mapean Business deben eliminar referencias a coverImage/coverUrl

fun GetBusinessQuery.Business.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        // ... otros campos ...
        avatarUrl = avatar
        // coverUrl = coverImage  ❌ ELIMINAR esta línea
    )
}

// Similar para otras funciones de mapeo
```

### 6. UI Components

**RegisterBusinessScreen.kt:**

```kotlin
// ELIMINAR estado de cover del negocio:
// var businessCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }  ❌
// val businessCoverPath = (businessCoverState as? ImageUploadState.Success)?.s3Path  ❌

// ELIMINAR componente de upload de cover:
Row(...) {
    ImageUploadPreview(
        label = "Avatar",
        uploadState = businessAvatarState,
        // ... ✅ MANTENER
    )
    
    // ImageUploadPreview(  ❌ ELIMINAR TODO ESTE BLOQUE
    //     label = "Portada",
    //     uploadState = businessCoverState,
    //     uploadFunction = { uri, token ->
    //         imageUploadService.uploadBusinessCover(uri, token)
    //     },
    //     ...
    // )
}

// ELIMINAR coverImage del CreateBusinessInput:
val business = CreateBusinessInput(
    name = businessName,
    description = businessDescription.ifBlank { null },
    tags = businessTagsList,
    avatar = businessAvatarPath
    // coverImage = businessCoverPath  ❌ ELIMINAR
)
```

**ProfileSections.kt - BannerWithLogoSection:**

```kotlin
// Modificar la firma para hacer coverUrl opcional y remover onChangeCover para Business:
@Composable
fun BannerWithLogoSection(
    avatarUrl: String? = null,
    coverUrl: String? = null,  // Siempre será null para Business
    onChangeAvatar: () -> Unit = {},
    onChangeCover: (() -> Unit)? = null  // Hacer opcional, null para Business
) {
    // ...
    
    // Mostrar botón de cambiar cover solo si onChangeCover no es null
    if (onChangeCover != null) {
        IconButton(
            onClick = onChangeCover,
            // ...
        ) {
            // ...
        }
    }
}
```

**RestaurantProfileScreen.kt y MarketProfileScreen.kt:**

```kotlin
// Pasar null para coverUrl y no pasar onChangeCover:
BannerWithLogoSection(
    avatarUrl = currentBusiness?.avatarUrl,
    coverUrl = null,  // Business ya no tiene cover
    onChangeAvatar = { /* ... */ }
    // onChangeCover no se pasa, usa default null
)
```

## Data Models

### Business (Actualizado)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | String | ID único del negocio |
| name | String | Nombre del negocio |
| ownerId | String | ID del propietario |
| globalRating | Double | Rating promedio (0-5) |
| avatar | String? | Path en S3 del avatar |
| description | String? | Descripción del negocio |
| socialMedia | Map<String, String>? | Redes sociales |
| tags | List<String> | Tags para búsqueda |
| isActive | Boolean | Estado activo |
| createdAt | String | Fecha de creación |
| avatarUrl | String? | URL presignada del avatar |

### Branch (Sin cambios)

La estructura de Branch permanece igual, manteniendo `coverImage` y `coverUrl`.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Business objects never contain cover fields

*For any* Business object created through any mapper function, the resulting object SHALL NOT have coverImage or coverUrl properties with non-null values.

**Validates: Requirements 1.1, 1.2, 1.3, 8.1, 8.2, 8.3**

### Property 2: CreateBusinessInput never includes coverImage

*For any* CreateBusinessInput object constructed in the application, the coverImage field SHALL NOT exist in the serialized GraphQL mutation.

**Validates: Requirements 2.3, 3.5, 5.3**

### Property 3: Banner fallback for Business without cover

*For any* Business displayed in BannerWithLogoSection where coverUrl is null, the component SHALL render a gradient fallback instead of an image.

**Validates: Requirements 6.1, 6.3, 6.4**

### Property 4: Branch cover functionality preserved

*For any* Branch object, the coverImage and coverUrl fields SHALL remain accessible and functional, and uploadBranchCover SHALL continue to work.

**Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**

## Error Handling

### Errores de Compilación

Después de eliminar los campos, el compilador de Kotlin detectará cualquier referencia restante a `coverImage` o `coverUrl` en el contexto de Business. Estos errores deben resolverse eliminando o actualizando el código afectado.

### Errores de GraphQL

Si el esquema local no coincide con el backend, Apollo generará errores de compilación. Regenerar el código con `./gradlew generateApolloSources` después de actualizar el esquema.

### Fallback de UI

El componente `BannerWithLogoSection` ya tiene un fallback a gradiente cuando `coverUrl` es null, por lo que no se requieren cambios adicionales para manejar la ausencia de cover.

## Testing Strategy

### Unit Tests

Los unit tests deben verificar:

1. **Modelo Business** - Verificar que la clase Business no tiene propiedades coverImage/coverUrl
2. **Mappers** - Verificar que las funciones de mapeo producen objetos Business sin cover fields
3. **CreateBusinessInput** - Verificar que no incluye coverImage en la serialización

### Property-Based Tests

Se utilizará la librería de property-based testing disponible en el proyecto para verificar las propiedades de correctness definidas arriba.

**Configuración:**
- Mínimo 100 iteraciones por test
- Cada test debe referenciar la propiedad del documento de diseño
- Formato de tag: **Feature: remove-business-cover-image, Property N: [descripción]**

### Integration Tests

1. **GraphQL Queries** - Verificar que las queries no solicitan campos eliminados
2. **UI Components** - Verificar que RegisterBusinessScreen no muestra upload de cover para business
3. **ImageUploadService** - Verificar que uploadBusinessCover no existe en la interfaz

### Manual Testing Checklist

- [ ] Registrar un nuevo negocio - no debe aparecer opción de cover
- [ ] Ver perfil de negocio existente - debe mostrar gradiente en lugar de cover
- [ ] Registrar sucursal - debe seguir mostrando opción de cover
- [ ] Ver perfil de sucursal - debe mostrar cover si existe
