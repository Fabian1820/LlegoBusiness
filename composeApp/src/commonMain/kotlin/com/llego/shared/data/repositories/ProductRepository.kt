package com.llego.shared.data.repositories

import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.GetProductsByIdsQuery
import com.llego.multiplatform.graphql.GetProductsQuery
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.network.GraphQLClient
import com.apollographql.apollo.api.Optional

/**
 * Repositorio para gestionar productos usando GraphQL
 */
class ProductRepository {

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
                val products = response.data!!.products.map { it.toDomain() }
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
                val products = response.data!!.products.map { it.toDomain() }
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
     * Mapea un ProductType de GraphQL a un modelo de dominio Product
     */
    private fun GetProductsQuery.Product.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,
            price = price,
            currency = currency,
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString()
        )
    }

    /**
     * Mapea un ProductType de GraphQL a un modelo de dominio Product (para query de IDs)
     */
    private fun GetProductsByIdsQuery.Product.toDomain(): Product {
        return Product(
            id = id,
            branchId = branchId,
            name = name,
            description = description,
            weight = weight,
            price = price,
            currency = currency,
            image = image,
            availability = availability,
            categoryId = categoryId,
            createdAt = createdAt.toString()
        )
    }
}
