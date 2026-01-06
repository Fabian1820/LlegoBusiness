# Plan de Integración Backend - Llego Business App

## 📋 Estado Actual

### ✅ Ya Implementado
- Apollo GraphQL Client configurado (v4.3.3)
- Schema descargado del backend
- Queries de productos (`GetProducts`, `GetProductsByIds`)
- ProductRepository con queries funcionando
- ProductMapper para conversión GraphQL → Local
- NetworkImage para cargar imágenes desde URLs
- Permisos Android (INTERNET, ACCESS_NETWORK_STATE)

### ⚠️ Usando Mock Data Actualmente
- **AuthRepository**: Login/Register con usuarios simulados
- **BusinessProfile**: Datos hardcodeados
- **User data**: Mock data en memoria
- **Branches**: No implementado
- **Categories**: No implementado

---

## 🎯 Plan de Integración Paso a Paso

### Fase 1: Autenticación y Usuarios ✨

#### 1.1 Modelos de Datos de Autenticación

**Archivos a crear:**
- `shared/data/model/AuthModels.kt` - Modelos para auth
  - `LoginInput`, `RegisterInput`, `SocialLoginInput`
  - `AuthResponse` (accessToken, tokenType, user)
  - `UpdateUserInput`, `AddBranchToUserInput`

**Cambios según nueva API:**
- User ahora tiene `businessIds: [String]` y `branchIds: [String]`
- User tiene `authProvider`, `providerUserId` para Google/Apple auth
- User tiene `avatarUrl` (presigned URL) en lugar de solo `avatar`

#### 1.2 GraphQL Queries/Mutations para Auth

**Archivos a crear:**
```
composeApp/src/commonMain/graphql/
├── auth/
│   ├── Login.graphql
│   ├── Register.graphql
│   ├── LoginWithGoogle.graphql
│   ├── Me.graphql                    # Query para obtener usuario actual
│   └── UpdateUser.graphql
└── users/
    ├── AddBranchToUser.graphql
    ├── RemoveBranchFromUser.graphql
    └── DeleteUser.graphql
```

**Contenido de Login.graphql:**
```graphql
mutation Login($input: LoginInput!) {
  login(input: $input) {
    accessToken
    tokenType
    user {
      id
      name
      email
      phone
      role
      avatar
      businessIds
      branchIds
      createdAt
      authProvider
      avatarUrl
    }
  }
}
```

**Contenido de Register.graphql:**
```graphql
mutation Register($input: RegisterInput!) {
  register(input: $input) {
    accessToken
    tokenType
    user {
      id
      name
      email
      phone
      role
      avatar
      businessIds
      branchIds
      createdAt
      avatarUrl
    }
  }
}
```

**Contenido de LoginWithGoogle.graphql:**
```graphql
mutation LoginWithGoogle($input: SocialLoginInput!, $jwt: String) {
  loginWithGoogle(input: $input, jwt: $jwt) {
    accessToken
    tokenType
    user {
      id
      name
      email
      phone
      role
      createdAt
      avatarUrl
    }
  }
}
```

**Contenido de Me.graphql:**
```graphql
query Me($jwt: String!) {
  me(jwt: $jwt) {
    id
    name
    email
    phone
    role
    avatarUrl
    businessIds
    branchIds
    createdAt
  }
}
```

#### 1.3 AuthRepository Refactor

**Archivo:** `shared/data/repositories/AuthRepository.kt`

**Cambios principales:**
```kotlin
class AuthRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val response = client.mutation(
                LoginMutation(
                    input = LoginInput(email = email, password = password)
                )
            ).execute()

            response.data?.login?.let { authResponse ->
                // Guardar token
                tokenManager.saveToken(authResponse.accessToken)

                // Convertir a modelo local
                val user = authResponse.user.toDomain()
                AuthResult.Success(user)
            } ?: AuthResult.Error("No se recibió respuesta del servidor")
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de red")
        }
    }

    suspend fun register(input: RegisterInput): AuthResult<User> { ... }

    suspend fun loginWithGoogle(idToken: String, nonce: String?): AuthResult<User> { ... }

    suspend fun getCurrentUser(): AuthResult<User> {
        val token = tokenManager.getToken() ?: return AuthResult.Error("No autenticado")
        // Usar MeQuery con el token
    }

    suspend fun updateUser(input: UpdateUserInput): AuthResult<User> { ... }
}
```

#### 1.4 TokenManager (Nuevo)

**Archivo a crear:** `shared/data/auth/TokenManager.kt`

```kotlin
expect class TokenManager {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
```

**Implementaciones:**
- `TokenManager.android.kt` - SharedPreferences
- `TokenManager.ios.kt` - NSUserDefaults
- `TokenManager.desktop.kt` - Properties file

---

### Fase 2: Negocios y Sucursales 🏢

#### 2.1 Modelos de Datos

**Archivo a crear:** `shared/data/model/BusinessModels.kt`

```kotlin
@Serializable
data class Business(
    val id: String,
    val name: String,
    val type: String,
    val ownerId: String,
    val globalRating: Double,
    val avatar: String?,
    val coverImage: String?,
    val description: String?,
    val tags: List<String>,
    val isActive: Boolean,
    val createdAt: String,
    val avatarUrl: String?,
    val coverUrl: String?
)

@Serializable
data class Branch(
    val id: String,
    val businessId: String,
    val name: String,
    val address: String?,
    val coordinates: Coordinates,
    val phone: String,
    val schedule: Map<String, String>, // JSON como Map
    val managerIds: List<String>,
    val status: String,
    val avatar: String?,
    val coverImage: String?,
    val deliveryRadius: Double?,
    val facilities: List<String>,
    val createdAt: String,
    val avatarUrl: String?,
    val coverUrl: String?
)

@Serializable
data class Coordinates(
    val type: String = "Point",
    val coordinates: List<Double> // [lng, lat]
) {
    val latitude: Double get() = coordinates[1]
    val longitude: Double get() = coordinates[0]
}

// Inputs
data class CreateBusinessInput(
    val name: String,
    val type: String,
    val avatar: String?,
    val coverImage: String?,
    val description: String?,
    val tags: List<String>?
)

data class RegisterBranchInput(
    val name: String,
    val coordinates: CoordinatesInput,
    val phone: String,
    val schedule: Map<String, String>,
    val address: String?,
    val avatar: String?,
    val coverImage: String?,
    val deliveryRadius: Double?,
    val facilities: List<String>?
)

data class CoordinatesInput(
    val lat: Double,
    val lng: Double
)
```

#### 2.2 GraphQL Queries/Mutations

**Archivos a crear:**
```
composeApp/src/commonMain/graphql/
├── businesses/
│   ├── RegisterBusiness.graphql      # Crear negocio + sucursales
│   ├── UpdateBusiness.graphql
│   ├── GetBusinesses.graphql
│   └── GetBusiness.graphql
└── branches/
    ├── CreateBranch.graphql
    ├── UpdateBranch.graphql
    ├── GetBranches.graphql
    └── GetBranch.graphql
```

**Contenido de RegisterBusiness.graphql:**
```graphql
mutation RegisterBusiness(
  $business: CreateBusinessInput!
  $branches: [RegisterBranchInput!]!
  $jwt: String
) {
  registerBusiness(
    businessInput: $business
    branchesInput: $branches
    jwt: $jwt
  ) {
    id
    name
    type
    avatarUrl
    coverUrl
    description
    tags
    isActive
  }
}
```

#### 2.3 BusinessRepository (Nuevo)

**Archivo a crear:** `shared/data/repositories/BusinessRepository.kt`

```kotlin
class BusinessRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager
) {
    suspend fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ): Result<Business> { ... }

    suspend fun getBusinesses(): Result<List<Business>> { ... }

    suspend fun getBusiness(id: String): Result<Business> { ... }

    suspend fun updateBusiness(id: String, input: UpdateBusinessInput): Result<Business> { ... }

    suspend fun getBranches(businessId: String?): Result<List<Branch>> { ... }

    suspend fun createBranch(input: CreateBranchInput): Result<Branch> { ... }

    suspend fun updateBranch(id: String, input: UpdateBranchInput): Result<Branch> { ... }
}
```

---

### Fase 3: Productos (Ampliar lo existente) 📦

#### 3.1 Agregar Mutations a ProductRepository

**Archivo:** `shared/data/repositories/ProductRepository.kt`

**GraphQL Mutations a crear:**
```
composeApp/src/commonMain/graphql/products/
├── CreateProduct.graphql
├── UpdateProduct.graphql
└── DeleteProduct.graphql
```

**Nuevos métodos en ProductRepository:**
```kotlin
suspend fun createProduct(input: CreateProductInput): Result<Product> { ... }

suspend fun updateProduct(id: String, input: UpdateProductInput): Result<Product> { ... }

suspend fun deleteProduct(id: String): Result<Boolean> { ... }
```

---

### Fase 4: Upload de Imágenes 📸

#### 4.1 ImageUploadService (Nuevo)

**Archivo a crear:** `shared/data/network/ImageUploadService.kt`

```kotlin
expect class ImageUploadService {
    suspend fun uploadUserAvatar(imageData: ByteArray, token: String): Result<ImageUploadResponse>
    suspend fun uploadBusinessAvatar(imageData: ByteArray, token: String): Result<ImageUploadResponse>
    suspend fun uploadBusinessCover(imageData: ByteArray, token: String): Result<ImageUploadResponse>
    suspend fun uploadBranchAvatar(imageData: ByteArray, token: String): Result<ImageUploadResponse>
    suspend fun uploadBranchCover(imageData: ByteArray, token: String): Result<ImageUploadResponse>
    suspend fun uploadProductImage(imageData: ByteArray, token: String): Result<ImageUploadResponse>
}

data class ImageUploadResponse(
    val imagePath: String,
    val imageUrl: String
)
```

**Implementaciones por plataforma:**
- `ImageUploadService.android.kt` - OkHttp o Ktor Client
- `ImageUploadService.ios.kt` - NSURLSession
- `ImageUploadService.desktop.kt` - Ktor Client

**Endpoints REST:**
```
POST https://llegobackend-production.up.railway.app/upload/user/avatar
POST https://llegobackend-production.up.railway.app/upload/business/avatar
POST https://llegobackend-production.up.railway.app/upload/business/cover
POST https://llegobackend-production.up.railway.app/upload/branch/avatar
POST https://llegobackend-production.up.railway.app/upload/branch/cover
POST https://llegobackend-production.up.railway.app/upload/product/image
```

---

### Fase 5: Actualizar Apollo Client con Auth 🔐

#### 5.1 GraphQLClient con Interceptor

**Archivo:** `shared/data/network/GraphQLClient.kt`

```kotlin
object GraphQLClient {
    private lateinit var tokenManager: TokenManager

    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    val apolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://llegobackend-production.up.railway.app/graphql")
            .addHttpHeader("Content-Type", "application/json")
            .addHttpInterceptor(object : HttpInterceptor {
                override suspend fun intercept(
                    request: HttpRequest,
                    chain: HttpInterceptorChain
                ): HttpResponse {
                    val token = tokenManager.getToken()
                    val newRequest = if (token != null) {
                        request.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    } else {
                        request
                    }
                    return chain.proceed(newRequest)
                }
            })
            .build()
    }

    fun close() {
        if (::apolloClient.isInitialized) {
            apolloClient.close()
        }
    }
}
```

---

### Fase 6: Actualizar Schema GraphQL 📄

#### 6.1 Actualizar schema.graphqls

**Comando para descargar schema actualizado:**
```bash
./gradlew downloadApolloSchema
```

O manualmente:
```bash
npx get-graphql-schema https://llegobackend-production.up.railway.app/graphql > composeApp/src/commonMain/graphql/schema.graphqls
```

**Verificar que incluya:**
- Mutations: `login`, `register`, `loginWithGoogle`
- Mutations: `registerBusiness`, `updateBusiness`, `createBranch`, `updateBranch`
- Mutations: `createProduct`, `updateProduct`, `deleteProduct`
- Mutations: `updateUser`, `addBranchToUser`, `removeBranchFromUser`, `deleteUser`
- Queries: `me`, `user`, `businesses`, `business`, `branches`, `branch`, `products`, `product`

---

### Fase 7: Actualizar UI y ViewModels 🎨

#### 7.1 LoginScreen y AuthViewModel

**Cambios en LoginScreen:**
- Agregar botón "Sign in with Google"
- Integrar Google Sign-In SDK (expect/actual)
- Manejar estados de carga y error
- Redirigir según `businessIds` y `branchIds` del usuario

**Cambios en AuthViewModel:**
```kotlin
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    val authState = authRepository.currentUser.stateIn(...)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    // Navegar según user.businessIds
                }
                is AuthResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) { ... }

    fun register(input: RegisterInput) { ... }
}
```

#### 7.2 Pantalla de Registro de Negocio (Nueva)

**Archivo a crear:** `shared/ui/business/RegisterBusinessScreen.kt`

**Flujo:**
1. Usuario ya está autenticado (tiene JWT)
2. Formulario para crear negocio:
   - Nombre del negocio
   - Tipo (RESTAURANT, MARKET, etc.)
   - Descripción
   - Tags
   - Upload avatar/cover
3. Formulario para crear primera sucursal:
   - Nombre
   - Dirección
   - Teléfono
   - Coordenadas (mapa)
   - Horarios
   - Radio de entrega
4. Al enviar: `registerBusiness` mutation
5. Automáticamente el `businessId` se agrega al usuario
6. Redirigir a Dashboard del nicho correspondiente

#### 7.3 ProfileScreen Updates

**Cambios necesarios:**
- Cargar datos reales con `me` query
- Mostrar `businessIds` y `branchIds`
- Permitir editar perfil con `updateUser` mutation
- Upload de avatar usando ImageUploadService
- Mostrar negocios y sucursales del usuario

---

## 🔄 Flujos Completos a Implementar

### Flujo 1: Registro Completo
```
1. RegisterScreen → register mutation
   ↓
2. Usuario creado con businessIds: [], branchIds: []
   ↓
3. JWT guardado en TokenManager
   ↓
4. RegisterBusinessScreen → upload images + registerBusiness mutation
   ↓
5. Negocio y sucursales creados
   ↓
6. businessId agregado automáticamente a user.businessIds
   ↓
7. Navegación a Dashboard del nicho
```

### Flujo 2: Login Existente
```
1. LoginScreen → login mutation
   ↓
2. JWT guardado
   ↓
3. me query para obtener usuario completo
   ↓
4. Si user.businessIds.isEmpty() → RegisterBusinessScreen
   ↓
5. Si user.businessIds.isNotEmpty() → Dashboard del nicho
```

### Flujo 3: Crear Producto
```
1. MenuScreen → "Agregar Producto"
   ↓
2. ProductFormScreen → Formulario
   ↓
3. Upload imagen → POST /upload/product/image
   ↓
4. Recibir image_path
   ↓
5. createProduct mutation con image_path y branchId
   ↓
6. Producto creado → Actualizar lista
```

### Flujo 4: Gestionar Sucursales
```
1. ProfileScreen → Ver negocios
   ↓
2. BusinessDetailScreen → Ver sucursales
   ↓
3. BranchListScreen → Lista de sucursales
   ↓
4. Opción "Agregar acceso" → addBranchToUser mutation
   ↓
5. branchId agregado a user.branchIds
   ↓
6. Ahora puede crear productos en esa sucursal
```

---

## 📦 Dependencias Adicionales Necesarias

### En libs.versions.toml
```toml
[versions]
ktor = "3.0.1"
okhttp = "4.12.0"

[libraries]
# Para uploads (Android)
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }

# Para Ktor Client (iOS/Desktop uploads)
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }

# Google Sign-In (Android)
google-play-services-auth = "com.google.android.gms:play-services-auth:20.7.0"
```

---

## 🧪 Testing Strategy

### Tests a crear:
1. **AuthRepositoryTest** - Login, register, Google auth
2. **BusinessRepositoryTest** - CRUD de negocios y sucursales
3. **ProductRepositoryTest** - CRUD de productos
4. **TokenManagerTest** - Guardar/recuperar tokens
5. **ImageUploadServiceTest** - Upload de imágenes

---

## 📊 Métricas de Progreso

- [ ] Fase 1: Autenticación y Usuarios (0/4 tareas)
- [ ] Fase 2: Negocios y Sucursales (0/3 tareas)
- [ ] Fase 3: Productos Mutations (0/3 tareas)
- [ ] Fase 4: Upload de Imágenes (0/2 tareas)
- [ ] Fase 5: Apollo Client Auth (0/1 tarea)
- [ ] Fase 6: Schema Update (0/1 tarea)
- [ ] Fase 7: UI Updates (0/3 tareas)

**Total: 0/17 tareas completadas**

---

## 🚀 Orden Recomendado de Implementación

1. **TokenManager** (base para todo)
2. **Auth Models + GraphQL Mutations** (login, register)
3. **AuthRepository refactor** (usar GraphQL real)
4. **Apollo Client interceptor** (agregar JWT a requests)
5. **LoginScreen/RegisterScreen** updates (usar nuevo repo)
6. **Business Models + Mutations**
7. **BusinessRepository** implementation
8. **RegisterBusinessScreen** (flujo completo)
9. **ImageUploadService** (para avatars y productos)
10. **Product Mutations** (crear, editar, eliminar)
11. **ProfileScreen** updates (datos reales)
12. **Branch management** screens
13. **Google Sign-In** integration
14. **Testing completo**
15. **Optimización y cache**

---

**Autor:** Plan generado para Llego Business App
**Fecha:** 2025-12-30
**Backend:** `https://llegobackend-production.up.railway.app/graphql`
**Versión Apollo:** 4.3.3
