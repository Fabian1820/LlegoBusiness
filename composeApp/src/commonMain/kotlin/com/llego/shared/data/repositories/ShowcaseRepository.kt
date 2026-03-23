package com.llego.shared.data.repositories

import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.CreateShowcaseMutation
import com.llego.multiplatform.graphql.DeleteShowcaseMutation
import com.llego.multiplatform.graphql.ShowcasesByBranchQuery
import com.llego.multiplatform.graphql.ToggleShowcaseAvailabilityMutation
import com.llego.multiplatform.graphql.UpdateShowcaseMutation
import com.llego.multiplatform.graphql.type.CreateShowcaseInput
import com.llego.multiplatform.graphql.type.ShowcaseItemInput
import com.llego.multiplatform.graphql.type.UpdateShowcaseInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.toDomain
import com.llego.shared.data.model.ShowcaseItem
import com.llego.shared.data.model.ShowcasesResult
import com.llego.shared.data.network.GraphQLClient

class ShowcaseRepository(
    private val tokenManager: TokenManager
) {
    private val client = GraphQLClient.apolloClient

    suspend fun getShowcasesByBranch(
        branchId: String,
        activeOnly: Boolean = false
    ): ShowcasesResult {
        return try {
            val token = tokenManager.getToken()
            val response = client.query(
                ShowcasesByBranchQuery(
                    branchId = branchId,
                    activeOnly = Optional.present(activeOnly),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                ShowcasesResult.Success(
                    response.data!!.showcasesByBranch.map { it.toDomain() }
                )
            } else {
                ShowcasesResult.Error(
                    response.errors?.firstOrNull()?.message ?: "Error al cargar vitrinas"
                )
            }
        } catch (e: ApolloException) {
            ShowcasesResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShowcasesResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun createShowcase(
        branchId: String,
        title: String,
        imagePath: String,
        description: String? = null,
        items: List<ShowcaseItem>? = null
    ): ShowcasesResult {
        return try {
            val token = tokenManager.getToken()
            val showcaseItems = items?.map { item ->
                ShowcaseItemInput(
                    id = Optional.presentIfNotNull(item.id),
                    name = item.name,
                    description = Optional.presentIfNotNull(item.description),
                    price = Optional.presentIfNotNull(item.price),
                    availability = Optional.present(item.availability)
                )
            }

            val response = client.mutation(
                CreateShowcaseMutation(
                    input = CreateShowcaseInput(
                        branchId = branchId,
                        title = title,
                        image = imagePath,
                        description = Optional.presentIfNotNull(description),
                        items = Optional.presentIfNotNull(showcaseItems)
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                ShowcasesResult.Success(listOf(response.data!!.createShowcase.toDomain()))
            } else {
                ShowcasesResult.Error(
                    response.errors?.firstOrNull()?.message ?: "Error al crear vitrina"
                )
            }
        } catch (e: ApolloException) {
            ShowcasesResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShowcasesResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun toggleAvailability(showcaseId: String, isActive: Boolean): ShowcasesResult {
        return try {
            val token = tokenManager.getToken()
            val response = client.mutation(
                ToggleShowcaseAvailabilityMutation(
                    showcaseId = showcaseId,
                    isActive = isActive,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                ShowcasesResult.Success(emptyList())
            } else {
                ShowcasesResult.Error(
                    response.errors?.firstOrNull()?.message ?: "Error al actualizar vitrina"
                )
            }
        } catch (e: ApolloException) {
            ShowcasesResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShowcasesResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun updateShowcase(
        showcaseId: String,
        title: String? = null,
        imagePath: String? = null,
        description: String? = null,
        items: List<ShowcaseItem>? = null,
        isActive: Boolean? = null
    ): ShowcasesResult {
        return try {
            val token = tokenManager.getToken()
            val showcaseItems = items?.map { item ->
                ShowcaseItemInput(
                    id = Optional.presentIfNotNull(item.id),
                    name = item.name,
                    description = Optional.presentIfNotNull(item.description),
                    price = Optional.presentIfNotNull(item.price),
                    availability = Optional.present(item.availability)
                )
            }

            val response = client.mutation(
                UpdateShowcaseMutation(
                    showcaseId = showcaseId,
                    input = UpdateShowcaseInput(
                        title = Optional.presentIfNotNull(title),
                        image = Optional.presentIfNotNull(imagePath),
                        description = Optional.presentIfNotNull(description),
                        items = Optional.presentIfNotNull(showcaseItems),
                        isActive = Optional.presentIfNotNull(isActive)
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                ShowcasesResult.Success(listOf(response.data!!.updateShowcase.toDomain()))
            } else {
                ShowcasesResult.Error(
                    response.errors?.firstOrNull()?.message ?: "Error al actualizar vitrina"
                )
            }
        } catch (e: ApolloException) {
            ShowcasesResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShowcasesResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun deleteShowcase(showcaseId: String): ShowcasesResult {
        return try {
            val token = tokenManager.getToken()
            val response = client.mutation(
                DeleteShowcaseMutation(
                    showcaseId = showcaseId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data?.deleteShowcase == true) {
                ShowcasesResult.Success(emptyList())
            } else {
                ShowcasesResult.Error("Error al eliminar vitrina")
            }
        } catch (e: ApolloException) {
            ShowcasesResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShowcasesResult.Error("Unexpected error: ${e.message}")
        }
    }
}
