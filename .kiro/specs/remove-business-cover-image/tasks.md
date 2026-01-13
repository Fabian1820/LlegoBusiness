# Implementation Plan: Remove Business Cover Image

## Overview

Este plan implementa la eliminación del campo coverImage del modelo Business en el frontend, siguiendo los cambios del backend. Las tareas están ordenadas para minimizar errores de compilación durante el proceso.

## Tasks

- [x] 1. Actualizar esquema GraphQL y queries
  - [x] 1.1 Actualizar schema.graphqls - eliminar coverImage y coverUrl de BusinessType
    - Eliminar `coverImage: String` de BusinessType
    - Eliminar `coverUrl: String` de BusinessType
    - Eliminar `coverImage` de CreateBusinessInput
    - Eliminar `coverImage` de UpdateBusinessInput
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 1.2 Actualizar GetBusiness.graphql
    - Eliminar `coverImage` del query
    - Eliminar `coverUrl` del query (si existe)
    - _Requirements: 3.1, 3.2_

  - [x] 1.3 Actualizar GetBusinesses.graphql
    - Eliminar `coverImage` del query
    - Eliminar `coverUrl` del query (si existe)
    - _Requirements: 3.3, 3.4_

  - [x] 1.4 Actualizar RegisterBusiness.graphql
    - Eliminar `coverImage` de la respuesta de la mutation
    - Eliminar `coverUrl` de la respuesta (si existe)
    - _Requirements: 3.5_

  - [x] 1.5 Actualizar UpdateBusiness.graphql
    - Eliminar `coverImage` de la respuesta de la mutation
    - Eliminar `coverUrl` de la respuesta (si existe)
    - _Requirements: 3.6, 3.7_

- [x] 2. Actualizar modelos de datos
  - [x] 2.1 Actualizar BusinessModels.kt - modelo Business
    - Eliminar propiedad `coverImage: String?`
    - Eliminar propiedad `coverUrl: String?`
    - _Requirements: 1.1, 1.2_

  - [x] 2.2 Actualizar BusinessModels.kt - CreateBusinessInput
    - Eliminar propiedad `coverImage: String?`
    - _Requirements: 2.3_

  - [x] 2.3 Actualizar BusinessModels.kt - UpdateBusinessInput
    - Eliminar propiedad `coverImage: String?`
    - _Requirements: 2.4_

- [x] 3. Actualizar mappers
  - [x] 3.1 Actualizar BusinessMappers.kt
    - Eliminar mapeo de `coverImage` en todas las funciones de mapeo de Business
    - Eliminar mapeo de `coverUrl` en todas las funciones de mapeo de Business
    - _Requirements: 8.1, 8.2, 8.3_

- [x] 4. Actualizar ImageUploadService
  - [x] 4.1 Actualizar ImageUploadService.kt (interfaz común)
    - Eliminar método `uploadBusinessCover()`
    - _Requirements: 4.1_

  - [x] 4.2 Actualizar ImageUploadService.android.kt
    - Eliminar implementación de `uploadBusinessCover()`
    - _Requirements: 4.2_

  - [x] 4.3 Actualizar ImageUploadService.ios.kt
    - Eliminar implementación de `uploadBusinessCover()`
    - _Requirements: 4.3_

  - [x] 4.4 Actualizar ImageUploadService.jvm.kt
    - Eliminar implementación de `uploadBusinessCover()`
    - _Requirements: 4.4_

- [x] 5. Checkpoint - Verificar compilación
  - Ejecutar `./gradlew compileKotlin` para verificar que no hay errores
  - Resolver cualquier referencia restante a coverImage/coverUrl de Business
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Actualizar UI de registro de negocio
  - [x] 6.1 Actualizar RegisterBusinessScreen.kt
    - Eliminar estado `businessCoverState`
    - Eliminar variable `businessCoverPath`
    - Eliminar componente ImageUploadPreview para cover de negocio
    - Eliminar `coverImage` del CreateBusinessInput en el onClick del botón
    - _Requirements: 5.1, 5.2, 5.3_

- [x] 7. Actualizar UI de perfil
  - [x] 7.1 Actualizar ProfileSections.kt - BannerWithLogoSection
    - Hacer `onChangeCover` opcional (nullable con default null)
    - Mostrar botón de cambiar cover solo si `onChangeCover` no es null
    - _Requirements: 6.2_

  - [x] 7.2 Actualizar RestaurantProfileScreen.kt
    - Pasar `coverUrl = null` a BannerWithLogoSection
    - No pasar `onChangeCover` (usar default null)
    - _Requirements: 6.3_

  - [x] 7.3 Actualizar MarketProfileScreen.kt
    - Pasar `coverUrl = null` a BannerWithLogoSection
    - No pasar `onChangeCover` (usar default null)
    - _Requirements: 6.4_

- [x] 8. Verificar funcionalidad de Branch
  - [x] 8.1 Verificar que Branch mantiene coverImage y coverUrl
    - Confirmar que BranchType en schema.graphqls mantiene coverImage y coverUrl
    - Confirmar que Branch model mantiene coverImage y coverUrl
    - _Requirements: 7.1, 7.2_

  - [x] 8.2 Verificar que uploadBranchCover sigue funcionando
    - Confirmar que ImageUploadService mantiene uploadBranchCover
    - Confirmar que RegisterBusinessScreen mantiene upload de cover para branch
    - _Requirements: 7.3, 7.4, 7.5_

- [x] 9. Final checkpoint - Compilación y verificación
  - Ejecutar `./gradlew build` para verificar compilación completa
  - Verificar que no hay warnings relacionados con campos eliminados
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Las tareas están ordenadas para minimizar errores de compilación intermedios
- El esquema GraphQL debe actualizarse primero para que Apollo genere el código correcto
- Después de actualizar el esquema, ejecutar `./gradlew generateApolloSources` si es necesario
- La funcionalidad de Branch NO debe modificarse - solo verificar que sigue funcionando
- Los checkpoints permiten validar el progreso antes de continuar
