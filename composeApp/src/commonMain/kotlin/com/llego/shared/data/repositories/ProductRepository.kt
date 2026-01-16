package com.llego.shared.data.repositories

import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.GetProductsByIdsQuery
import com.llego.multiplatform.graphql.GetProductsQuery
import com.llego.multiplatform.graphql.CreateProductMutation
import com.llego.multiplatform.graphql.UpdateProductMutation
import com.llego.multiplatform.graphql.DeleteProductMutation
import com.llego.multiplatform.graphql.type.CreateProductInput
import com.llego.multiplatform.graphql.type.UpdateProductInput
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.network.GraphQLClient
import com.apollographql.apollo.api.Optional
import com.llego.shared.data.auth.TokenManager

/**
 * Repositorio para gestionar productos usando GraphQL
 */
class ProductRepository(
    private val tokenManager: TokenManager
) {

    private val client = GraphQLClient.apolloClient

    /**
     * Obtiene todos los productos o productos filtrados
     * @param branchId ID de la sucursal (opcional)
     * @param categoryId ID de la categoría (opcional)
     * @param availableOnly Solo productos disponibles (default: false)
     */
    suspend fun getProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false
    ): ProductsResult {
        return try {
            val response = client.query(
                GetProductsQuery(
                    branchId = Optional.presentIfNotNull(branchId),
                    categoryId = Optional.presentIfNotNull(categoryId),
                    availableOnly = Optional.present(availableOnly)
                )
            ).execute()

            if (response.data != null) {
                // Extraer los nodos de la estructura paginada edges[].node
                val products = response.data!!.products.edges.map { it.node.toDomain() }
                ProductsResult.Success(products)
            } else {
                ProductsResult.Error(response.errors?.firstOrNull()?.message ?: "Unknown error")
            }
        } catch (e: ApolloException) {
            ProductsResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ProductsResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Obtiene productos por IDs específicos
     * @param ids Lista de IDs de productos
     */
    suspend fun getProductsByIds(ids: List<String>): ProductsResult {
        return try {
            val response = client.query(
                GetProductsByIdsQuery(
                    ids = Optional.present(ids)
                )
            ).execute()

            if (response.data != null) {
                // Extraer los nodos de la estructura paginada edges[].node
                val products = response.data!!.products.edges.map { it.node.toDomain() }
                ProductsResult.Success(products)
            } else {
                ProductsResult.Error(response.errors?.firstOrNull()?.message ?: "Unknown error")
            }
        } catch (e: ApolloException) {
            ProductsResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ProductsResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Mapea un ProductType de GraphQL (estructura paginada) a un modelo de dominio Product
     * 
     * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
     * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
     */
    private fun GetProductsQuery.Node.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,              // String! -> String (no nullable)
            price = price,
            currency = currency,          // String! -> String (no nullable)
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString(),
            imageUrl = imageUrl           // String! -> String (no nullable)
        )
    }

    /**
     * Mapea un ProductType de GraphQL a un modelo de dominio Product (para query de IDs)
     * 
     * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
     * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
     */
    private fun GetProductsByIdsQuery.Node.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,              // String! -> String (no nullable)
            price = price,
            currency = currency,          // String! -> String (no nullable)
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString(),
            imageUrl = imageUrl           // String! -> String (no nullable)
        )
    }

    // ============= CRUD OPERATIONS =============

    /**
     * Crea un nuevo producto
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param image Path de la imagen (después de upload)
     * @param currency Moneda del producto (requerido, debe ser seleccionado en la UI)
     * @param branchId ID de la sucursal (opcional si se pasa businessId)
     * @param businessId ID del negocio (opcional si se pasa branchId)
     * @param weight Peso del producto (opcional - si es null, el backend asigna "" como default)
     * @param categoryId ID de la categoría (opcional)
     * 
     * Nota: El parámetro weight es opcional. Si se pasa null, el backend asignará
     * un valor por defecto (""). El campo weight en el modelo Product siempre será
     * no-nullable ya que el backend garantiza un valor.
     */
    suspend fun createProduct(
        name: String,
        description: String,
        price: Double,
        image: String,
        currency: String,
        branchId: String? = null,
        businessId: String? = null,
        weight: String? = null,
        categoryId: String? = null
    ): ProductsResult {
        return try {
            val token = tokenManager.getToken()

            val response = client.mutation(
                CreateProductMutation(
                    input = CreateProductInput(
                        name = name,
                        description = description,
                        price = price,
                        image = image,
                        branchId = Optional.presentIfNotNull(branchId),
                        businessId = Optional.presentIfNotNull(businessId),
                        currency = Optional.present(currency),
                        weight = Optional.presentIfNotNull(weight),
                        categoryId = Optional.presentIfNotNull(categoryId)
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                val product = response.data!!.createProduct.toDomain()
                ProductsResult.Success(listOf(product))
            } else {
                ProductsResult.Error(response.errors?.firstOrNull()?.message ?: "Error al crear producto")
            }
        } catch (e: ApolloException) {
            ProductsResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ProductsResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Actualiza un producto existente
     * @param productId ID del producto a actualizar
     * @param name Nuevo nombre (opcional)
     * @param description Nueva descripción (opcional)
     * @param price Nuevo precio (opcional)
     * @param currency Nueva moneda (opcional - permite cambiar la moneda del producto)
     * @param weight Nuevo peso (opcional - si es null, no se actualiza; el backend mantiene el valor actual)
     * @param availability Nueva disponibilidad (opcional)
     * @param categoryId Nuevo ID de categoría (opcional)
     * @param image Nuevo path de imagen (opcional)
     * 
     * Nota: Todos los campos son opcionales. Si un campo es null, no se actualiza
     * y el backend mantiene el valor actual. Para campos opcionales en el schema
     * (como categoryId), pasar null explícitamente puede remover el valor.
     */
    suspend fun updateProduct(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        currency: String? = null,
        weight: String? = null,
        availability: Boolean? = null,
        categoryId: String? = null,
        image: String? = null
    ): ProductsResult {
        return try {
            val token = tokenManager.getToken()

            val response = client.mutation(
                UpdateProductMutation(
                    productId = productId,
                    input = UpdateProductInput(
                        name = Optional.presentIfNotNull(name),
                        description = Optional.presentIfNotNull(description),
                        price = Optional.presentIfNotNull(price),
                        currency = Optional.presentIfNotNull(currency),
                        weight = Optional.presentIfNotNull(weight),
                        availability = Optional.presentIfNotNull(availability),
                        categoryId = Optional.presentIfNotNull(categoryId),
                        image = Optional.presentIfNotNull(image)
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                val product = response.data!!.updateProduct.toDomain()
                ProductsResult.Success(listOf(product))
            } else {
                ProductsResult.Error(response.errors?.firstOrNull()?.message ?: "Error al actualizar producto")
            }
        } catch (e: ApolloException) {
            ProductsResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ProductsResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Elimina un producto
     * @param productId ID del producto a eliminar
     * @return ProductsResult con lista vacía si fue exitoso
     */
    suspend fun deleteProduct(productId: String): ProductsResult {
        return try {
            val token = tokenManager.getToken()

            val response = client.mutation(
                DeleteProductMutation(
                    productId = productId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null && response.data!!.deleteProduct) {
                ProductsResult.Success(emptyList())
            } else {
                ProductsResult.Error(response.errors?.firstOrNull()?.message ?: "Error al eliminar producto")
            }
        } catch (e: ApolloException) {
            ProductsResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ProductsResult.Error("Unexpected error: ${e.message}")
        }
    }

    // ============= MAPPERS =============

    /**
     * Mapea CreateProductMutation.CreateProduct a modelo de dominio
     * 
     * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
     * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
     */
    private fun CreateProductMutation.CreateProduct.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,              // String! -> String (no nullable)
            price = price,
            currency = currency,          // String! -> String (no nullable)
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString(),
            imageUrl = imageUrl           // String! -> String (no nullable)
        )
    }

    /**
     * Mapea UpdateProductMutation.UpdateProduct a modelo de dominio
     * 
     * Nota: Los campos weight, currency e imageUrl son no-nullable en el schema GraphQL
     * y por lo tanto siempre tienen valor. El mapeo directo garantiza la consistencia de tipos.
     */
    private fun UpdateProductMutation.UpdateProduct.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,              // String! -> String (no nullable)
            price = price,
            currency = currency,          // String! -> String (no nullable)
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString(),
            imageUrl = imageUrl           // String! -> String (no nullable)
        )
    }
}
