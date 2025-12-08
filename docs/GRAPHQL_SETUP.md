# GraphQL Integration Guide (Kotlin Multiplatform)

This guide documents the GraphQL setup for the Llego project and serves as a reference for implementing GraphQL in a fresh KSL/KMP project from scratch.

## 1. Versions & Dependencies required

Add these specific versions to your `libs.versions.toml`. These are the versions currently validated in this project.

```toml
[versions]
apollo = "4.3.3"  # Latest stable version as of implementation
ktor = "3.0.1"    # For network transport (if using Ktor engine)

[libraries]
# Apollo Runtime (Core)
apollo-runtime = { module = "com.apollographql.apollo:apollo-runtime", version.ref = "apollo" }

# Optional: SQLite cache normalization (if needed later)
apollo-normalized-cache = { module = "com.apollographql.apollo:apollo-normalized-cache", version.ref = "apollo" }

[plugins]
# The Apollo Gradle Plugin
apolloGraphQl = { id = "com.apollographql.apollo", version.ref = "apollo" }
```

## 2. Gradle Configuration

In your module's `build.gradle.kts` (e.g., `composeApp/build.gradle.kts`), apply the plugin and configure dependencies.

### Apply Plugin
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.apolloGraphQl) // <--- Apply this
}
```

### Add Dependencies (CommonMain)
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.apollo.runtime)
        }
    }
}
```

### Configure Apollo Block
Add this configuration block at the end of your `build.gradle.kts`:

```kotlin
apollo {
    service("service") {
        packageName.set("com.llego.multiplatform.graphql")
        
        // Tells Apollo to generate Kotlin models
        generateKotlinModels.set(true)
        
        // Optional: introspection configuration to download schema via Gradle
        introspection {
            endpointUrl.set("https://llegobackend-production.up.railway.app/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
    }
}
```

## 3. Obtaining the Schema (The missing link)

You need the `schema.graphqls` file for Apollo to generate code. You can download it using the Introspection API.

### Option A: Using the Gradle Task (Recommended)
If you added the `introspection` block above, simply run:
```bash
./gradlew downloadApolloSchema
```

### Option B: Using cURL (Manual Method)
If you want to manually download the schema without Gradle configuration, run this in your terminal:

```bash
# Create directory first
mkdir -p composeApp/src/commonMain/graphql

# Download schema
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"query": "query IntrospectionQuery { __schema { queryType { name } mutationType { name } subscriptionType { name } types { ...FullType } directives { name description locations args { ...InputValue } } } } fragment FullType on __Type { kind name description fields(includeDeprecated: true) { name description args { ...InputValue } type { ...TypeRef } isDeprecated deprecationReason } inputFields { ...InputValue } interfaces { ...TypeRef } enumValues(includeDeprecated: true) { name description isDeprecated deprecationReason } possibleTypes { ...TypeRef } } fragment InputValue on __InputValue { name description type { ...TypeRef } defaultValue } fragment TypeRef on __Type { kind name ofType { kind name ofType { kind name ofType { kind name ofType { kind name ofType { kind name ofType { kind name ofType { kind name } } } } } } }"}' \
  https://llegobackend-production.up.railway.app/graphql \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['data'])" > composeApp/src/commonMain/graphql/schema.json
```
*Note: Apollo 4.x supports both JSON and SDL (.graphqls). The method above gets JSON. A simpler specialized tool like `get-graphql-schema` is often easier.*

**Simplest Manual Way (using `get-graphql-schema` npm tool):**
```bash
npx get-graphql-schema https://llegobackend-production.up.railway.app/graphql > composeApp/src/commonMain/graphql/schema.graphqls
```

## 4. Defining Operations

Create your query files in the same directory: `src/commonMain/graphql/`.

**Example: `GetHomeData.graphql`**
```graphql
query GetHomeData {
  products {
    id
    name
    shop
    price
    imageUrl
  }
  stores {
    id
    name
    logoUrl
  }
}
```

## 5. Implementation in Code

After syncing Gradle (which generates the classes), use them in your KMP code.

**1. Create the Client**
```kotlin
// composeApp/src/commonMain/kotlin/.../network/GraphQLClient.kt
object GraphQLClient {
    val apolloClient = ApolloClient.Builder()
        .serverUrl("https://llegobackend-production.up.railway.app/graphql")
        .build()
}
```

**2. Use in Repository**
```kotlin
// composeApp/src/commonMain/kotlin/.../repositories/HomeRepository.kt
class HomeRepository {
    private val client = GraphQLClient.apolloClient

    suspend fun getData(): HomeData {
        // execute() performs the network request
        val response = client.query(GetHomeDataQuery()).execute()
        
        // Map generated classes to your domain models
        return response.data?.let { data ->
            HomeData(
                products = data.products.map { it.toDomain() },
                stores = data.stores.map { it.toDomain() }
            )
        } ?: throw Exception("No data")
    }
}
```

## Troubleshooting Common Issues

1.  **"Unresolved reference: GetHomeDataQuery"**:
    *   Did you build the project? (`./gradlew build`)
    *   Do you have `schema.graphqls` AND a `.graphql` query file?
    *   Are the files in `src/commonMain/graphql`?

2.  **Schema too old**:
    *   If backend changes, re-run `./gradlew downloadApolloSchema`.

3.  **Network Errors**:
    *   Ensure the Android manifest has Internet permissions:
        `<uses-permission android:name="android.permission.INTERNET" />`