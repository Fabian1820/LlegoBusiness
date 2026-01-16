# Implementation Plan: Product Schema Alignment

## Overview

Este plan implementa la alineación completa entre el schema GraphQL de productos y la UI, eliminando inconsistencias y agregando funcionalidad de selección de moneda.

## Tasks

- [x] 1. Actualizar modelo de dominio Product
  - Hacer imageUrl no nullable (String en lugar de String?)
  - Mantener weight como String no nullable
  - Mantener currency como String no nullable
  - Agregar campos opcionales para relaciones (category, branch, business)
  - Actualizar documentación de campos con defaults
  - _Requirements: 1.1, 2.1, 4.1_

- [x] 2. Crear enum y componente de selección de moneda
  - [x] 2.1 Crear enum SupportedCurrency
    - Definir USD, CUP, EUR, MLC con códigos y símbolos
    - Incluir displayName para cada moneda
    - _Requirements: 3.4_

  - [x] 2.2 Crear componente CurrencySelector
    - Implementar dropdown de selección de moneda
    - Usar ExposedDropdownMenuBox de Material3
    - Mostrar símbolo y nombre de cada moneda
    - _Requirements: 3.1, 3.2_

- [x] 3. Actualizar ProductFormData
  - Agregar campo currency: String
  - Mantener weight como String? (opcional en formulario)
  - Actualizar documentación
  - _Requirements: 3.1_

- [x] 4. Actualizar AddProductScreen
  - [x] 4.1 Agregar estado para currency
    - Inicializar con "USD" para productos nuevos
    - Inicializar con existingProduct.currency para edición
    - _Requirements: 3.1, 3.2, 3.3_

  - [x] 4.2 Integrar CurrencySelector en el formulario
    - Agregar selector después del campo de precio
    - Marcar como campo requerido (*)
    - Aplicar estilos consistentes con otros campos
    - _Requirements: 3.1, 3.2_

  - [x] 4.3 Actualizar validación de formulario
    - Incluir currency en validación de campos requeridos
    - Actualizar isSaveEnabled para verificar currency
    - _Requirements: 5.1, 5.2_

  - [x] 4.4 Actualizar callback onSave
    - Incluir currency en ProductFormData
    - Pasar currency seleccionado al guardar
    - _Requirements: 3.1_

- [x] 5. Actualizar App.kt para pasar currency
  - [x] 5.1 Actualizar llamada a createProductWithImagePath
    - Cambiar currency hardcodeado por form.currency
    - Pasar currency desde el formulario
    - _Requirements: 3.1_

  - [x] 5.2 Actualizar llamada a updateProductWithImagePath
    - Cambiar productToEdit!!.currency por form.currency
    - Permitir cambiar currency al editar
    - _Requirements: 3.2_

- [x] 6. Actualizar ProductRepository
  - [x] 6.1 Actualizar método createProduct
    - Cambiar parámetro currency de default "USD" a requerido
    - Documentar que weight null resulta en default del backend
    - Usar Optional.presentIfNotNull para weight
    - _Requirements: 1.3, 5.4, 6.2_

  - [x] 6.2 Actualizar método updateProduct
    - Permitir actualizar currency
    - Documentar comportamiento de campos opcionales
    - _Requirements: 3.2, 5.4_

  - [x] 6.3 Actualizar mappers toDomain()
    - Asegurar que imageUrl se mapea sin nullable
    - Asegurar que weight se mapea sin nullable
    - Asegurar que currency se mapea sin nullable
    - _Requirements: 1.1, 2.3_

- [x] 7. Checkpoint - Verificar compilación y funcionalidad básica
  - Asegurar que el código compila sin errores
  - Verificar que la UI muestra el selector de moneda
  - Verificar que se pueden crear productos con diferentes monedas
  - Preguntar al usuario si hay dudas o problemas

- [ ]* 8. Escribir tests unitarios
  - [ ]* 8.1 Tests para SupportedCurrency enum
    - Verificar que todas las monedas tienen código, símbolo y displayName
    - _Requirements: 3.4_

  - [ ]* 8.2 Tests para ProductFormData
    - Verificar que currency es requerido
    - Verificar que weight es opcional
    - _Requirements: 3.1_

  - [ ]* 8.3 Tests para validación de formulario
    - Verificar que campos requeridos se validan correctamente
    - Verificar que currency se incluye en validación
    - _Requirements: 5.1, 5.2_

- [ ]* 9. Escribir property-based tests
  - [ ]* 9.1 Property test: Weight default
    - **Property 1: Weight consistency**
    - **Validates: Requirements 1.3**
    - Generar productos sin weight
    - Verificar que backend asigna default
    - Verificar que producto retornado tiene weight no-null

  - [ ]* 9.2 Property test: ImageUrl generation
    - **Property 2: ImageUrl generation**
    - **Validates: Requirements 2.2, 2.3**
    - Generar productos con diferentes imágenes
    - Verificar que backend genera imageUrl
    - Verificar que imageUrl es no-null

  - [ ]* 9.3 Property test: Currency default
    - **Property 3: Currency default**
    - **Validates: Requirements 3.3**
    - Crear productos sin especificar currency en backend
    - Verificar que se usa "USD" como default

  - [ ]* 9.4 Property test: Currency selection
    - **Property 4: Currency selection**
    - **Validates: Requirements 3.1, 3.2**
    - Generar selecciones aleatorias de moneda
    - Verificar que producto se crea con moneda seleccionada
    - Verificar que moneda se preserva al editar

  - [ ]* 9.5 Property test: Optional field handling
    - **Property 5: Optional field handling**
    - **Validates: Requirements 5.3, 6.2**
    - Generar combinaciones aleatorias de campos opcionales
    - Verificar que backend maneja null correctamente
    - Verificar estructura válida del producto retornado

  - [ ]* 9.6 Property test: Required field validation
    - **Property 6: Required field validation**
    - **Validates: Requirements 5.1, 5.2**
    - Generar formularios con campos faltantes
    - Verificar que validación detecta campos requeridos
    - Verificar que save está deshabilitado cuando falta algo

- [ ] 10. Checkpoint final
  - Ejecutar todos los tests
  - Verificar que todas las propiedades pasan
  - Realizar pruebas manuales de creación y edición de productos
  - Verificar que no hay regresiones en funcionalidad existente

## Notes

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requisitos específicos que implementa
- Los checkpoints aseguran validación incremental
- Los property tests validan las propiedades de correctness del diseño
- Los unit tests validan componentes específicos y casos edge
