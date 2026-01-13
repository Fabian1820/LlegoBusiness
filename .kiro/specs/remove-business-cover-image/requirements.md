# Requirements Document

## Introduction

Este documento especifica los requisitos para adaptar el frontend a los cambios del backend que eliminan el campo `coverImage` del modelo `Business`. Las sucursales (Branch) mantienen su funcionalidad de cover image sin cambios. Adicionalmente, el backend ahora implementa herencia automática de avatar desde el negocio padre hacia las sucursales.

## Glossary

- **Business**: Entidad que representa un negocio en el sistema
- **Branch**: Entidad que representa una sucursal de un negocio
- **Cover_Image**: Imagen de portada/banner de un negocio o sucursal
- **Avatar**: Imagen de perfil/logo de un negocio o sucursal
- **GraphQL_Schema**: Esquema que define los tipos y operaciones disponibles en la API
- **ImageUploadService**: Servicio que maneja la subida de imágenes a S3
- **Avatar_Inheritance**: Comportamiento donde las sucursales heredan el avatar del negocio padre si no tienen uno propio

## Requirements

### Requirement 1: Eliminar campo coverImage del modelo Business

**User Story:** Como desarrollador, quiero eliminar las referencias a coverImage del modelo Business, para que el frontend esté sincronizado con los cambios del backend.

#### Acceptance Criteria

1. THE Business_Model SHALL NOT contain the coverImage field
2. THE Business_Model SHALL NOT contain the coverUrl field
3. WHEN a Business object is created or mapped, THE System SHALL NOT include coverImage or coverUrl properties

### Requirement 2: Actualizar esquema GraphQL

**User Story:** Como desarrollador, quiero actualizar el esquema GraphQL local, para que refleje la eliminación de coverImage en BusinessType.

#### Acceptance Criteria

1. THE GraphQL_Schema SHALL NOT define coverImage field in BusinessType
2. THE GraphQL_Schema SHALL NOT define coverUrl field in BusinessType
3. THE CreateBusinessInput SHALL NOT accept coverImage parameter
4. THE UpdateBusinessInput SHALL NOT accept coverImage parameter
5. WHEN GraphQL queries for Business are executed, THE System SHALL NOT request coverImage or coverUrl fields

### Requirement 3: Actualizar queries GraphQL de Business

**User Story:** Como desarrollador, quiero actualizar las queries GraphQL de Business, para que no soliciten campos que ya no existen.

#### Acceptance Criteria

1. THE GetBusiness query SHALL NOT request coverImage field
2. THE GetBusiness query SHALL NOT request coverUrl field
3. THE GetBusinesses query SHALL NOT request coverImage field
4. THE GetBusinesses query SHALL NOT request coverUrl field
5. THE RegisterBusiness mutation SHALL NOT send coverImage in businessInput
6. THE UpdateBusiness mutation SHALL NOT send coverImage in input
7. THE UpdateBusiness mutation response SHALL NOT request coverImage field

### Requirement 4: Eliminar funcionalidad de upload de cover de Business

**User Story:** Como desarrollador, quiero eliminar la funcionalidad de upload de cover image para Business, para que no se intente usar un endpoint que ya no existe.

#### Acceptance Criteria

1. THE ImageUploadService interface SHALL NOT define uploadBusinessCover method
2. THE Android ImageUploadService implementation SHALL NOT contain uploadBusinessCover method
3. THE iOS ImageUploadService implementation SHALL NOT contain uploadBusinessCover method
4. THE JVM ImageUploadService implementation SHALL NOT contain uploadBusinessCover method

### Requirement 5: Actualizar UI de registro de negocio

**User Story:** Como usuario, quiero que el formulario de registro de negocio no muestre la opción de subir portada, para evitar confusión con funcionalidad eliminada.

#### Acceptance Criteria

1. WHEN the RegisterBusinessScreen is displayed, THE System SHALL NOT show the business cover image upload component
2. THE RegisterBusinessScreen SHALL only show avatar upload for business images
3. WHEN creating a business, THE System SHALL NOT send coverImage in the CreateBusinessInput

### Requirement 6: Actualizar UI de perfil de negocio

**User Story:** Como usuario, quiero que la pantalla de perfil del negocio maneje correctamente la ausencia de cover image, para que la UI se muestre correctamente.

#### Acceptance Criteria

1. WHEN displaying the BannerWithLogoSection for a Business, THE System SHALL use a fallback gradient when coverUrl is null
2. THE BannerWithLogoSection component SHALL NOT show the "change cover" button when used for Business profile
3. THE RestaurantProfileScreen SHALL pass null for coverUrl parameter to BannerWithLogoSection
4. THE MarketProfileScreen SHALL pass null for coverUrl parameter to BannerWithLogoSection

### Requirement 7: Mantener funcionalidad de cover para Branch

**User Story:** Como desarrollador, quiero asegurar que la funcionalidad de cover image para sucursales permanezca intacta, para que las sucursales puedan seguir teniendo portadas.

#### Acceptance Criteria

1. THE Branch_Model SHALL continue to contain coverImage field
2. THE Branch_Model SHALL continue to contain coverUrl field
3. THE ImageUploadService SHALL continue to provide uploadBranchCover method
4. THE RegisterBusinessScreen SHALL continue to show cover image upload for branches
5. WHEN creating a branch, THE System SHALL continue to send coverImage if provided

### Requirement 8: Actualizar mappers de Business

**User Story:** Como desarrollador, quiero actualizar los mappers de Business, para que no intenten mapear campos que ya no existen.

#### Acceptance Criteria

1. WHEN mapping GraphQL Business response to domain model, THE BusinessMapper SHALL NOT attempt to map coverImage
2. WHEN mapping GraphQL Business response to domain model, THE BusinessMapper SHALL NOT attempt to map coverUrl
3. THE Business domain model SHALL have coverUrl property removed or set to null by default
