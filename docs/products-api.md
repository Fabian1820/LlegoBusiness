# API de Productos

Documentacion para integracion frontend multiplataforma.

---

## Tipos GraphQL

### ProductType
```typescript
interface Product {
  id: string;
  branchId: string;
  name: string;
  description: string;
  price: float;
  currency: string;        // Default: "USD"
  weight: string;          // Peso/porcion ej: "250g"
  image: string;           // Path en S3
  availability: boolean;   // Default: true
  categoryId?: string;     // ID de ProductCategory
  createdAt: DateTime;
  
  // Campos computados (resolvers)
  imageUrl: string;        // Presigned URL
  category?: ProductCategory;
  branch?: Branch;
  business?: Business;     // Via branch
}
```

### ScoredProductType (para queries paginadas)
```typescript
interface ScoredProduct extends Product {
  score: float;            // Score de relevancia (0-1)
  distanceM?: float;       // Distancia en metros
  distanceKm?: float;      // Distancia en km (computed)
}
```

### ProductCategoryType
```typescript
interface ProductCategory {
  id: string;
  branchType: string;      // "restaurante" | "dulceria" | "tienda"
  name: string;            // Nombre de la categoria
  iconIos: string;         // SF Symbol name para iOS
  iconWeb: string;         // Material icon para web
  iconAndroid: string;     // Material icon para Android
  createdAt: DateTime;
}
```

---

## Endpoint REST - Upload de Imagen

```bash
POST /upload/product/image
Authorization: Bearer {jwt}
Content-Type: multipart/form-data

# Form: image=@hamburguesa.png
# Max: 10MB
# Formatos: JPEG, PNG, WebP, GIF
# Preserva transparencia (PNG, WebP)
```

**Response:**
```json
{
  "image_path": "products/6774abc123.png",
  "image_url": "https://s3.../products/6774abc123.png?..."
}
```

---

## Mutations

### Crear Producto
```graphql
mutation CreateProduct($input: CreateProductInput!, $jwt: String) {
  createProduct(input: $input, jwt: $jwt) {
    id
    name
    price
    currency
    weight
    availability
    imageUrl
  }
}
```

**Con branchId (recomendado):**
```json
{
  "jwt": "eyJhbG...",
  "input": {
    "branchId": "6774branch123",
    "name": "Hamburguesa Clasica",
    "description": "Hamburguesa con queso, lechuga y tomate",
    "price": 15.99,
    "image": "products/6774abc123.png",
    "currency": "USD",
    "weight": "250g",
    "categoryId": "cat_burgers"
  }
}
```

**Con businessId (asigna a primera sucursal):**
```json
{
  "jwt": "eyJhbG...",
  "input": {
    "businessId": "6774business456",
    "name": "Hamburguesa Clasica",
    "description": "Hamburguesa con queso",
    "price": 15.99,
    "image": "products/6774abc123.png"
  }
}
```

> Se requiere `branchId` o `businessId`. Si solo `businessId`, se asigna a la primera sucursal.

### Actualizar Producto
```graphql
mutation UpdateProduct($productId: String!, $input: UpdateProductInput!, $jwt: String) {
  updateProduct(productId: $productId, input: $input, jwt: $jwt) {
    id
    name
    price
    currency
    weight
    availability
    imageUrl
  }
}
```
```json
{
  "jwt": "eyJhbG...",
  "productId": "6774product789",
  "input": {
    "name": "Hamburguesa Premium",
    "price": 18.99,
    "weight": "300g",
    "availability": true
  }
}
```

> Al actualizar imagen, la anterior se elimina automaticamente de S3.

### Eliminar Producto
```graphql
mutation DeleteProduct($productId: String!, $jwt: String) {
  deleteProduct(productId: $productId, jwt: $jwt)
}
```
Response: `true` si exitoso. La imagen se elimina de S3.

---

## Queries

### Productos (Paginado con Scoring)
```graphql
query GetProducts(
  $first: Int,
  $after: String,
  $ids: [String!],
  $branchId: String,
  $categoryId: String,
  $availableOnly: Boolean,
  $branchTipo: BranchTipo,
  $radiusKm: Float,
  $jwt: String
) {
  products(
    first: $first,
    after: $after,
    ids: $ids,
    branchId: $branchId,
    categoryId: $categoryId,
    availableOnly: $availableOnly,
    branchTipo: $branchTipo,
    radiusKm: $radiusKm,
    jwt: $jwt
  ) {
    edges {
      node {
        id
        name
        description
        price
        currency
        weight
        availability
        imageUrl
        score
        distanceKm
        category { id name iconIos iconAndroid }
        branch { id name address }
        business { id name }
      }
      cursor
    }
    pageInfo {
      hasNextPage
      hasPreviousPage
      startCursor
      endCursor
      totalCount
    }
  }
}
```

**Filtros disponibles:**

| Filtro | Tipo | Descripcion |
|--------|------|-------------|
| first | Int | Cantidad de items (max 50) |
| after | String | Cursor para paginacion |
| ids | [String] | IDs especificos de productos |
| branchId | String | Filtrar por sucursal |
| categoryId | String | Filtrar por categoria |
| availableOnly | Boolean | Solo productos disponibles |
| branchTipo | BranchTipo | Filtrar por tipo de sucursal |
| radiusKm | Float | Radio maximo en km |

> `branchId` tiene prioridad sobre `branchTipo`. Sin `first` retorna todos.


### Producto por ID
```graphql
query GetProduct($id: String!, $jwt: String) {
  product(id: $id, jwt: $jwt) {
    id
    name
    description
    price
    currency
    weight
    availability
    categoryId
    imageUrl
    createdAt
    category { id name branchType iconIos iconWeb iconAndroid }
    branch { id name address phone }
    business { id name avatarUrl }
  }
}
```

### Buscar Productos (Vector Search)
```graphql
query SearchProducts(
  $query: String!,
  $first: Int,
  $after: String,
  $useVectorSearch: Boolean,
  $branchTipo: BranchTipo,
  $radiusKm: Float,
  $jwt: String
) {
  searchProducts(
    query: $query,
    first: $first,
    after: $after,
    useVectorSearch: $useVectorSearch,
    branchTipo: $branchTipo,
    radiusKm: $radiusKm,
    jwt: $jwt
  ) {
    edges {
      node {
        id
        name
        price
        currency
        imageUrl
        score
        distanceKm
      }
      cursor
    }
    pageInfo { hasNextPage endCursor totalCount }
  }
}
```
```json
{
  "query": "hamburguesa con queso",
  "first": 10,
  "useVectorSearch": true,
  "branchTipo": "RESTAURANTE"
}
```

### Categorias de Productos
```graphql
query GetProductCategories($branchType: String) {
  productCategories(branchType: $branchType) {
    id
    branchType
    name
    iconIos
    iconWeb
    iconAndroid
  }
}
```
```json
{ "branchType": "restaurante" }
```

### Categoria por ID
```graphql
query GetProductCategory($id: String!) {
  productCategory(id: $id) {
    id
    branchType
    name
    iconIos
    iconWeb
    iconAndroid
  }
}
```

---

## Paginacion Cursor-Based (Relay)

```graphql
# Primera pagina
query { 
  products(first: 10) { 
    edges { node { id name } cursor } 
    pageInfo { hasNextPage endCursor } 
  } 
}

# Siguiente pagina
query { 
  products(first: 10, after: "cursor_anterior") { 
    edges { node { id name } cursor } 
    pageInfo { hasNextPage endCursor } 
  } 
}
```

**PageInfo:**
| Campo | Tipo | Descripcion |
|-------|------|-------------|
| hasNextPage | Boolean | Hay mas resultados |
| hasPreviousPage | Boolean | Hay resultados anteriores |
| startCursor | String | Cursor del primer elemento |
| endCursor | String | Cursor del ultimo elemento |
| totalCount | Int | Total de resultados |

---

## Inputs Reference

### CreateProductInput
| Campo | Tipo | Requerido | Default |
|-------|------|-----------|---------|
| name | String | Si | - |
| description | String | Si | - |
| price | Float | Si | - |
| image | String | Si | - |
| branchId | String | No* | - |
| businessId | String | No* | - |
| currency | String | No | "USD" |
| weight | String | No | "" |
| categoryId | String | No | null |

> *Se requiere `branchId` o `businessId`.

### UpdateProductInput
| Campo | Tipo | Requerido |
|-------|------|-----------|
| name | String | No |
| description | String | No |
| price | Float | No |
| currency | String | No |
| weight | String | No |
| availability | Boolean | No |
| categoryId | String | No |
| image | String | No |

---

## Scoring y Cercania

Cuando el usuario tiene ubicacion actualizada:

1. Productos se ordenan por `score` (relevancia + cercania)
2. Se incluye `distanceM` (metros) y `distanceKm` (kilometros)
3. Se puede filtrar por `radiusKm`

**Activar scoring:**
```graphql
mutation {
  updateLocation(input: { longitude: -77.0428, latitude: -12.0464 }, jwt: "...")
}
```

---

## Permisos

Para crear/editar/eliminar productos:
- Usuario debe ser `ownerId` del negocio, O
- Usuario debe estar en `managerIds` de la sucursal

---

## Ejemplo Completo

### 1. Subir Imagen
```bash
curl -X POST "/upload/product/image" \
  -H "Authorization: Bearer {jwt}" \
  -F "image=@hamburguesa.png"
```

### 2. Crear Producto
```graphql
mutation {
  createProduct(
    input: {
      branchId: "6774branch123"
      name: "Hamburguesa Clasica"
      description: "Deliciosa hamburguesa con queso"
      price: 15.99
      currency: "USD"
      weight: "250g"
      image: "products/6774abc123.png"
      categoryId: "cat_hamburguesas"
    }
    jwt: "eyJhbG..."
  ) {
    id
    name
    price
    imageUrl
  }
}
```

### 3. Consultar Productos
```graphql
query {
  products(branchId: "6774branch123", availableOnly: true, first: 10) {
    edges {
      node {
        id name price currency weight imageUrl
        category { name iconIos }
      }
    }
    pageInfo { hasNextPage totalCount }
  }
}
```
