# Design Document: Product Schema Alignment

## Overview

Este diseño aborda la alineación completa entre el schema GraphQL de productos y la implementación en la UI, eliminando inconsistencias en tipos de datos, campos opcionales/requeridos, y agregando funcionalidad faltante como la selección de moneda.

## Architecture

### Capas Afectadas

1. **Domain Model** (`Product.kt`)
   - Modelo de datos que representa un producto en el frontend
   - Debe reflejar fielmente el schema GraphQL

2. **Repository** (`ProductRepository.kt`)
   - Capa de acceso a datos que mapea entre GraphQL y el modelo de dominio
   - Maneja la conversión de tipos y valores opcionales

3. **UI** (`AddProductScreen.kt`, `App.kt`)
   - Formulario de creación/edición de productos
   - Validación de campos requeridos
   - Selección de moneda

4. **GraphQL Schema** (`schema.graphqls`)
   - Definición de tipos y mutaciones
   - Fuente de verdad para la estructura de datos

## Components and Interfaces

### 1. Product Domain Model (Actualizado)

```kotlin
@Serializable
data class Product(
    val id: String,
    val branchId: String,
    val name: String,
    val description: String,
    val weight: String,              // No nullable - siempre tiene valor (default: "")
    val price: Double,
    val currency: String,             // No nullable - siempre tiene valor (default: "USD")
    val image: String,
    val availability: Boolean,
    val categoryId: String?,          // Nullable - opcional
    val createdAt: String,
    val imageUrl: String,             // No nullable - siempre generado por backend
    
    // Relaciones opcionales (solo cuando se solicitan explícitamente)
    val category: ProductCategory? = null,
    val branch: Branch? = null,
    val business: Business? = null
)

@Serializable
data class ProductCategory(
    val id: String,
    val branchType: String,
    val name: String,
    val iconIos: String,
    val iconWeb: String,
    val iconAndroid: String
)
```

### 2. ProductFormData (Actualizado)

```kotlin
data class ProductFormData(
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String?,
    val weight: String?,              // Opcional en el formulario
    val currency: String,             // Ahora seleccionable
    val imagePath: String?,
    val availability: Boolean
)
```

### 3. Currency Selection Component

```kotlin
enum class SupportedCurrency(val code: String, val symbol: String, val displayName: String) {
    USD("USD", "$", "Dólar estadounidense"),
    CUP("CUP", "$", "Peso cubano"),
    EUR("EUR", "€", "Euro"),
    MLC("MLC", "$", "Moneda libremente convertible")
}

@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

### 4. Repository Methods (Actualizados)

```kotlin
suspend fun createProduct(
    name: String,
    description: String,
    price: Double,
    image: String,
    branchId: String? = null,
    businessId: String? = null,
    currency: String = "USD",        // Ahora viene de la UI
    weight: String? = null,          // Opcional - backend asigna default si es null
    categoryId: String? = null
): ProductsResult

suspend fun updateProduct(
    productId: String,
    name: String? = null,
    description: String? = null,
    price: Double? = null,
    currency: String? = null,        // Ahora actualizable
    weight: String? = null,
    availability: Boolean? = null,
    categoryId: String? = null,
    image: String? = null
): ProductsResult
```

## Data Models

### GraphQL Schema Alignment

#### ProductType (Schema)
```graphql
type ProductType {
  id: String!
  branchId: String!
  name: String!
  description: String!
  weight: String!              # Requerido en respuesta
  price: Float!
  currency: String!            # Requerido en respuesta
  image: String!
  availability: Boolean!
  categoryId: String
  createdAt: DateTime!
  imageUrl: String!            # Requerido - presigned URL
  
  category: ProductCategoryType
  branch: BranchType
  business: BusinessType
}
```

#### CreateProductInput (Schema)
```graphql
input CreateProductInput {
  name: String!
  description: String!
  price: Float!
  image: String!
  branchId: String = null
  businessId: String = null
  currency: String! = "USD"    # Requerido con default
  weight: String = null        # Opcional - backend asigna default
  categoryId: String = null
}
```

### Mapping Strategy

| Schema Field | Domain Model | UI Form | Notes |
|--------------|--------------|---------|-------|
| weight: String! | weight: String | weight: String? | Backend asigna "" si null |
| imageUrl: String! | imageUrl: String | N/A | Siempre generado por backend |
| currency: String! | currency: String | currency: String | Seleccionable en UI, default "USD" |
| categoryId: String | categoryId: String? | categoryId: String? | Opcional |
| category | category: ProductCategory? | N/A | Solo cuando se solicita |

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Weight consistency
*For any* product created without specifying weight, the backend should assign a default value and the returned product should have a non-null weight field.
**Validates: Requirements 1.3**

### Property 2: ImageUrl generation
*For any* product created or updated, the backend should always generate a valid imageUrl (presigned URL) and the returned product should have a non-null imageUrl field.
**Validates: Requirements 2.2, 2.3**

### Property 3: Currency default
*For any* product created without specifying currency, the system should use "USD" as the default value.
**Validates: Requirements 3.3**

### Property 4: Currency selection
*For any* product being created or edited in the UI, the user should be able to select from the list of supported currencies (USD, CUP, EUR, MLC).
**Validates: Requirements 3.1, 3.2**

### Property 5: Optional field handling
*For any* optional field (weight, categoryId) sent as null in CreateProductInput, the repository should use Optional.absent() and the backend should handle it gracefully.
**Validates: Requirements 5.3, 6.2**

### Property 6: Required field validation
*For any* product form submission, the UI should validate that all required fields (name, description, price, image, categoryId, currency) are present before allowing save.
**Validates: Requirements 5.1, 5.2**

## Error Handling

### Validation Errors

1. **Missing Required Fields**
   - UI validates before submission
   - Shows inline error messages
   - Disables save button until valid

2. **Invalid Price Format**
   - UI restricts input to decimal numbers
   - Shows error if non-numeric value entered

3. **Missing Image**
   - UI requires image upload before enabling save
   - Shows placeholder when no image selected

### Backend Errors

1. **GraphQL Mutation Errors**
   - Repository catches ApolloException
   - Returns ProductsResult.Error with message
   - UI shows error toast/snackbar

2. **Network Errors**
   - Repository handles connection failures
   - Returns user-friendly error message
   - UI allows retry

## Testing Strategy

### Unit Tests

1. **Domain Model Tests**
   - Test serialization/deserialization
   - Test default values
   - Test nullable vs non-nullable fields

2. **Repository Mapping Tests**
   - Test GraphQL to Domain model conversion
   - Test Optional.present() vs Optional.absent() usage
   - Test error handling

3. **UI Validation Tests**
   - Test required field validation
   - Test currency selection
   - Test form state management

### Property-Based Tests

1. **Weight Default Property Test**
   - Generate random products without weight
   - Verify backend assigns default value
   - Verify returned product has non-null weight

2. **Currency Selection Property Test**
   - Generate random currency selections
   - Verify product is created with selected currency
   - Verify currency is preserved on edit

3. **Optional Field Property Test**
   - Generate random combinations of optional fields
   - Verify backend handles null values correctly
   - Verify returned product has valid structure

### Integration Tests

1. **Create Product Flow**
   - Test complete flow from UI to backend
   - Verify all fields are correctly mapped
   - Verify defaults are applied

2. **Edit Product Flow**
   - Test loading existing product
   - Test updating fields including currency
   - Verify changes are persisted

## Implementation Notes

### Phase 1: Domain Model Updates
- Update Product data class to match schema exactly
- Make imageUrl non-nullable
- Add optional relation fields (category, branch, business)

### Phase 2: Repository Updates
- Update mappers to handle new fields
- Ensure proper Optional usage for nullable inputs
- Add documentation for default values

### Phase 3: UI Updates
- Add CurrencySelector component
- Update AddProductScreen to include currency selection
- Update form validation logic
- Update App.kt to pass currency to repository

### Phase 4: Testing
- Write unit tests for new components
- Write property-based tests for correctness properties
- Perform integration testing

### Migration Considerations

- Existing products may have empty weight strings - backend should handle this
- Currency field is new in UI but has backend default - no migration needed
- imageUrl is already generated by backend - just need to make it non-nullable in model
