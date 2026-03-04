package com.llego.shared.data.repositories

import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.CreateVariantListMutation
import com.llego.multiplatform.graphql.DeleteVariantListMutation
import com.llego.multiplatform.graphql.GetProductsByIdsQuery
import com.llego.multiplatform.graphql.GetProductsQuery
import com.llego.multiplatform.graphql.GetProductCategoriesQuery
import com.llego.multiplatform.graphql.GetBranchApplicableCategoriesQuery
import com.llego.multiplatform.graphql.GetVariantListsQuery
import com.llego.multiplatform.graphql.CreateProductMutation
import com.llego.multiplatform.graphql.UpdateVariantListMutation
import com.llego.multiplatform.graphql.UpdateProductMutation
import com.llego.multiplatform.graphql.DeleteProductMutation
import com.llego.multiplatform.graphql.type.CreateProductInput
import com.llego.multiplatform.graphql.type.CreateVariantListInput
import com.llego.multiplatform.graphql.type.UpdateProductInput
import com.llego.multiplatform.graphql.type.UpdateVariantListInput
import com.llego.multiplatform.graphql.type.VariantOptionInput
import com.llego.shared.data.mappers.toDomain
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.model.VariantList
import com.llego.shared.data.model.VariantOptionDraft
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
     * @param first Límite de productos a obtener (default: 100)
     */
    suspend fun getProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false,
        first: Int = 100
    ): ProductsResult {
        return try {
            val query = GetProductsQuery(
                branchId = Optional.presentIfNotNull(branchId),
                categoryId = Optional.presentIfNotNull(categoryId),
                availableOnly = Optional.present(availableOnly),
                first = Optional.present(first)
            )

            val response = client.query(query).execute()

            if (response.data != null) {
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
     * @param first Límite de productos a obtener (default: 100)
     */
    suspend fun getProductsByIds(ids: List<String>, first: Int = 100): ProductsResult {
        return try {
            val response = client.query(
                GetProductsByIdsQuery(
                    ids = Optional.present(ids),
                    first = Optional.present(first)
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

    suspend fun getProductCategories(branchTipos: Set<BranchTipo>): Result<List<ProductCategory>> {
        return try {
            val branchTypes = if (branchTipos.isEmpty()) {
                listOf<String?>(null)
            } else {
                branchTipos.map { it.name }.sorted()
            }

            val categories = mutableListOf<ProductCategory>()
            for (branchType in branchTypes) {
                val response = client.query(
                    GetProductCategoriesQuery(
                        branchType = Optional.presentIfNotNull(branchType)
                    )
                ).execute()

                if (response.hasErrors()) {
                    val errorMessage = response.errors?.joinToString(", ") { it.message }
                    return Result.failure(Exception(errorMessage ?: "Error al obtener categorías"))
                }

                val fetched = response.data?.productCategories?.map { it.toDomain() } ?: emptyList()
                categories += fetched
            }

            Result.success(
                categories
                    .distinctBy { it.id }
                    .sortedBy { it.name.lowercase() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApplicableCategoriesByBranch(branchId: String): Result<List<ProductCategory>> {
        return try {
            val token = tokenManager.getToken()
            val response = client.query(
                GetBranchApplicableCategoriesQuery(
                    branchId = branchId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString(", ") { it.message }
                Result.failure(Exception(errorMessage ?: "Error al obtener categorias de la sucursal"))
            } else {
                val categories = response.data?.branch?.applicableCategories
                    ?.map { it.toDomain() }
                    ?: emptyList()
                Result.success(categories.sortedBy { it.name.lowercase() })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVariantLists(branchId: String): Result<List<VariantList>> {
        return try {
            val token = tokenManager.getToken()
            val response = client.query(
                GetVariantListsQuery(
                    branchId = branchId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString(", ") { it.message }
                Result.failure(Exception(errorMessage ?: "Error al obtener listas de variantes"))
            } else {
                val variantLists = response.data?.variantLists?.map { it.toDomain() } ?: emptyList()
                Result.success(variantLists)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVariantList(
        branchId: String,
        name: String,
        description: String?,
        options: List<VariantOptionDraft>
    ): Result<VariantList> {
        return try {
            val token = tokenManager.getToken()
            val response = client.mutation(
                CreateVariantListMutation(
                    input = CreateVariantListInput(
                        branchId = branchId,
                        name = name,
                        description = Optional.presentIfNotNull(description),
                        options = options.map { option ->
                            VariantOptionInput(
                                id = Optional.presentIfNotNull(option.id),
                                name = option.name,
                                priceAdjustment = Optional.present(option.priceAdjustment)
                            )
                        }
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors
                    ?.joinToString(" | ") { it.message }
                    ?: "Error al crear lista de variantes"
                Result.failure(Exception(message))
            } else {
                val created = response.data?.createVariantList
                    ?: return Result.failure(Exception("Respuesta vacia al crear lista de variantes"))
                Result.success(created.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVariantList(
        variantListId: String,
        name: String? = null,
        description: String? = null,
        options: List<VariantOptionDraft>? = null
    ): Result<VariantList> {
        return try {
            val token = tokenManager.getToken()
            val response = client.mutation(
                UpdateVariantListMutation(
                    input = UpdateVariantListInput(
                        variantListId = variantListId,
                        name = Optional.presentIfNotNull(name),
                        description = Optional.presentIfNotNull(description),
                        options = Optional.presentIfNotNull(
                            options?.map { option ->
                                VariantOptionInput(
                                    id = Optional.presentIfNotNull(option.id),
                                    name = option.name,
                                    priceAdjustment = Optional.present(option.priceAdjustment)
                                )
                            }
                        )
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors
                    ?.joinToString(" | ") { it.message }
                    ?: "Error al actualizar lista de variantes"
                Result.failure(Exception(message))
            } else {
                val updated = response.data?.updateVariantList
                    ?: return Result.failure(Exception("Respuesta vacia al actualizar lista de variantes"))
                Result.success(updated.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVariantList(variantListId: String): Result<Boolean> {
        return try {
            val token = tokenManager.getToken()
            val response = client.mutation(
                DeleteVariantListMutation(
                    variantListId = variantListId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors
                    ?.joinToString(" | ") { it.message }
                    ?: "Error al eliminar lista de variantes"
                Result.failure(Exception(message))
            } else {
                Result.success(response.data?.deleteVariantList == true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        categoryId: String? = null,
        variantListIds: List<String>? = null
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
                        categoryId = Optional.presentIfNotNull(categoryId),
                        variantListIds = Optional.presentIfNotNull(variantListIds)
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
        image: String? = null,
        variantListIds: List<String>? = null
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
                        image = Optional.presentIfNotNull(image),
                        variantListIds = Optional.presentIfNotNull(variantListIds)
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

}
