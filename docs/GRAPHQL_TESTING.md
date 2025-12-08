# GraphQL - Guía de Pruebas

## ✅ Implementación Completada

La integración de GraphQL está completamente configurada y lista para usar:

### 📦 Componentes Implementados

1. **Configuración de Apollo GraphQL 4.3.3**
   - Plugin y dependencias configuradas
   - Backend URL: `https://llegobackend-production.up.railway.app/graphql`

2. **Schema GraphQL descargado**
   - Archivo: `composeApp/src/commonMain/graphql/schema.graphqls`
   - Generado desde el backend de Railway

3. **Queries GraphQL**
   - `GetProducts.graphql` - Obtener productos con filtros opcionales
   - `GetProductsByIds.graphql` - Obtener productos por IDs específicos

4. **Cliente GraphQL**
   - Ubicación: `com.llego.shared.data.network.GraphQLClient`
   - Apollo Client configurado y listo

5. **Modelo de Dominio**
   - `Product` - Modelo de producto
   - `ProductsResult` - Estados de resultado (Success, Error, Loading)

6. **Repositorio**
   - `ProductRepository` - Métodos para consultar productos
   - Manejo de errores incluido

7. **ViewModel**
   - `ProductViewModel` - Gestión de estado de productos
   - Soporte para StateFlow

8. **Pantalla de Prueba**
   - `ProductsTestScreen` - UI completa para ver productos del backend

## 🔧 Cómo Usar GraphQL en el Proyecto

### Opción 1: Integrar la Pantalla de Prueba

Agrega esto a tu navegación en `App.kt`:

```kotlin
import com.llego.nichos.common.ui.screens.ProductsTestScreen

// En el bloque de navegación, agrega:
var showGraphQLTest by remember { mutableStateOf(false) }

// Dentro del when:
showGraphQLTest -> {
    ProductsTestScreen(
        onNavigateBack = { showGraphQLTest = false }
    )
}
```

Y agrega un botón en alguna pantalla para acceder:

```kotlin
Button(onClick = { showGraphQLTest = true }) {
    Text("Probar GraphQL")
}
```

### Opción 2: Usar el Repository Directamente

En cualquier ViewModel o pantalla:

```kotlin
import com.llego.shared.data.repositories.ProductRepository
import com.llego.shared.data.model.ProductsResult

val repository = ProductRepository()

// En una coroutine:
viewModelScope.launch {
    when (val result = repository.getProducts()) {
        is ProductsResult.Success -> {
            println("Productos: ${result.products}")
        }
        is ProductsResult.Error -> {
            println("Error: ${result.message}")
        }
        is ProductsResult.Loading -> {
            println("Cargando...")
        }
    }
}
```

### Opción 3: Usar el ViewModel

```kotlin
import com.llego.nichos.common.ui.viewmodel.ProductViewModel

val viewModel = viewModel { ProductViewModel() }

// Observar estado
val productsState by viewModel.productsState.collectAsState()

// Cargar productos
LaunchedEffect(Unit) {
    viewModel.loadProducts()
}

// Con filtros
viewModel.loadProducts(
    branchId = "branch123",
    categoryId = "cat456",
    availableOnly = true
)
```

## 📊 Queries Disponibles

### GetProducts

```graphql
query GetProducts($branchId: String, $categoryId: String, $availableOnly: Boolean = false) {
  products(branchId: $branchId, categoryId: $categoryId, availableOnly: $availableOnly) {
    id
    branchId
    name
    description
    weight
    price
    currency
    image
    availability
    categoryId
    createdAt
  }
}
```

### GetProductsByIds

```graphql
query GetProductsByIds($ids: [String!]) {
  products(ids: $ids) {
    id
    name
    price
    # ... todos los campos
  }
}
```

## 🚀 Agregar Nuevas Queries

Para agregar nuevas queries GraphQL:

1. **Crear archivo `.graphql`** en `composeApp/src/commonMain/graphql/`:
   ```graphql
   query GetBranches {
     branches {
       id
       name
       address
     }
   }
   ```

2. **Compilar el proyecto**:
   ```bash
   ./gradlew composeApp:compileCommonMainKotlinMetadata
   ```

3. **Usar la query generada**:
   ```kotlin
   val response = GraphQLClient.apolloClient
       .query(GetBranchesQuery())
       .execute()
   ```

## 🔍 Verificar Schema

Para actualizar el schema si el backend cambia:

```bash
npx get-graphql-schema https://llegobackend-production.up.railway.app/graphql > composeApp/src/commonMain/graphql/schema.graphqls
```

Luego recompila el proyecto para regenerar las clases.

## 📁 Estructura de Archivos

```
composeApp/src/commonMain/
├── graphql/
│   ├── schema.graphqls              # Schema del backend
│   ├── GetProducts.graphql          # Query de productos
│   └── GetProductsByIds.graphql     # Query por IDs
│
└── kotlin/com/llego/
    ├── shared/data/
    │   ├── model/
    │   │   └── Product.kt           # Modelo de dominio
    │   ├── network/
    │   │   └── GraphQLClient.kt     # Cliente Apollo
    │   └── repositories/
    │       └── ProductRepository.kt # Repositorio de productos
    │
    └── nichos/common/ui/
        ├── viewmodel/
        │   └── ProductViewModel.kt  # ViewModel
        └── screens/
            └── ProductsTestScreen.kt # Pantalla de prueba
```

## ✅ Estado de Compilación

- ✅ Schema descargado correctamente
- ✅ Queries GraphQL creadas
- ✅ Apollo Client configurado
- ✅ Repository implementado
- ✅ ViewModel creado
- ✅ Pantalla de prueba lista
- ✅ Proyecto compila sin errores

## 🎯 Próximos Pasos

1. Integrar `ProductsTestScreen` en la navegación (opcional)
2. Usar `ProductRepository` en pantallas reales de productos
3. Agregar mutations para crear/actualizar productos
4. Implementar cache con `apollo-normalized-cache` si es necesario

---

**Desarrollado con Apollo GraphQL 4.3.3 + Kotlin Multiplatform**
