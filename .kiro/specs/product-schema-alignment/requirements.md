# Requirements Document

## Introduction

Este documento define los requisitos para alinear completamente el schema GraphQL de productos con la implementación de la UI, eliminando todas las discrepancias y asegurando consistencia entre backend y frontend.

## Glossary

- **Product_System**: Sistema de gestión de productos que incluye el schema GraphQL, el modelo de dominio Kotlin y la UI
- **Schema**: Definición GraphQL del tipo ProductType y sus inputs (CreateProductInput, UpdateProductInput)
- **Domain_Model**: Clase de datos Kotlin Product en el frontend
- **UI**: Interfaz de usuario para crear y editar productos (AddProductScreen)
- **Repository**: Capa de acceso a datos que mapea entre GraphQL y el modelo de dominio

## Requirements

### Requirement 1: Alineación del campo weight

**User Story:** Como desarrollador, quiero que el campo weight sea consistente entre el schema GraphQL y la UI, para que no haya confusión sobre si es requerido u opcional.

#### Acceptance Criteria

1. WHEN el schema GraphQL define weight como String!, THEN el Domain_Model SHALL definir weight como String (no nullable)
2. WHEN CreateProductInput acepta weight como opcional (String = null), THEN la UI SHALL permitir crear productos sin especificar weight
3. WHEN un producto se crea sin weight, THEN el Repository SHALL enviar null al backend y el backend SHALL asignar un valor por defecto
4. WHEN un producto existente tiene weight, THEN la UI SHALL mostrar el valor actual al editar

### Requirement 2: Alineación del campo imageUrl

**User Story:** Como desarrollador, quiero que el campo imageUrl sea consistente entre el schema GraphQL y el modelo de dominio, para evitar errores de tipo.

#### Acceptance Criteria

1. WHEN el schema GraphQL define imageUrl como String! (requerido), THEN el Domain_Model SHALL definir imageUrl como String (no nullable)
2. WHEN un producto se crea, THEN el backend SHALL generar siempre un imageUrl válido (presigned URL)
3. WHEN el Repository mapea un producto desde GraphQL, THEN SHALL asignar imageUrl sin permitir valores null

### Requirement 3: Gestión de currency

**User Story:** Como usuario de negocio, quiero poder seleccionar la moneda al crear productos, para que pueda manejar productos en diferentes monedas según mi negocio.

#### Acceptance Criteria

1. WHEN un usuario crea un nuevo producto, THEN la UI SHALL permitir seleccionar la moneda de una lista predefinida
2. WHEN un usuario edita un producto existente, THEN la UI SHALL mostrar la moneda actual y permitir cambiarla
3. WHEN no se especifica moneda al crear, THEN el Product_System SHALL usar "USD" como valor por defecto
4. THE UI SHALL soportar al menos las monedas: USD, CUP, EUR

### Requirement 4: Mapeo de relaciones del schema

**User Story:** Como desarrollador, quiero que las relaciones category, branch y business del schema estén disponibles en el modelo de dominio cuando sean necesarias, para poder mostrar información completa en la UI.

#### Acceptance Criteria

1. WHEN el schema GraphQL incluye relaciones (category, branch, business), THEN el Domain_Model SHALL incluir campos opcionales para estas relaciones
2. WHEN se consultan productos, THEN el Repository SHALL poder solicitar las relaciones mediante fragments de GraphQL
3. WHEN las relaciones no se solicitan, THEN los campos relacionados en el Domain_Model SHALL ser null
4. WHEN se muestra el detalle de un producto, THEN la UI SHALL poder acceder a la información de category si está disponible

### Requirement 5: Validación consistente de campos requeridos

**User Story:** Como desarrollador, quiero que los campos requeridos sean validados consistentemente en toda la aplicación, para prevenir errores en tiempo de ejecución.

#### Acceptance Criteria

1. WHEN la UI valida un formulario de producto, THEN SHALL verificar que todos los campos requeridos por el schema estén presentes
2. WHEN un campo es requerido en el schema (String!), THEN la UI SHALL marcarlo como obligatorio visualmente
3. WHEN un campo es opcional en CreateProductInput, THEN la UI SHALL permitir enviarlo vacío o null
4. WHEN el Repository envía una mutación, THEN SHALL usar Optional.present() para campos con valor y Optional.absent() para campos null

### Requirement 6: Manejo de valores por defecto

**User Story:** Como desarrollador, quiero que los valores por defecto se manejen de forma explícita y consistente, para evitar comportamientos inesperados.

#### Acceptance Criteria

1. WHEN un campo tiene valor por defecto en el schema (ej: currency: String! = "USD"), THEN el Repository SHALL documentar este comportamiento
2. WHEN la UI no especifica un valor opcional, THEN el Repository SHALL enviar Optional.absent() y dejar que el backend aplique el default
3. WHEN se edita un producto, THEN la UI SHALL cargar todos los valores actuales incluyendo los que tienen defaults
4. THE Domain_Model SHALL documentar qué campos tienen valores por defecto en el backend
