package com.llego.shared.data.repositories

import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.CreateComboMutation
import com.llego.multiplatform.graphql.DeleteComboMutation
import com.llego.multiplatform.graphql.GetComboQuery
import com.llego.multiplatform.graphql.GetCombosQuery
import com.llego.multiplatform.graphql.ToggleComboAvailabilityMutation
import com.llego.multiplatform.graphql.UpdateComboMutation
import com.llego.multiplatform.graphql.type.CreateComboInput
import com.llego.multiplatform.graphql.type.ComboSlotInput
import com.llego.multiplatform.graphql.type.ComboOptionInput
import com.llego.multiplatform.graphql.type.ComboModifierInput
import com.llego.multiplatform.graphql.type.UpdateComboInput
import com.llego.multiplatform.graphql.type.DiscountType as GraphQLDiscountType
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.toDomain
import com.llego.shared.data.mappers.toDomainList
import com.llego.shared.data.model.CombosResult
import com.llego.shared.data.network.GraphQLClient

/**
 * Repositorio para gestionar combos usando GraphQL
 */
class ComboRepository(
    private val tokenManager: TokenManager
) {
    private val client = GraphQLClient.apolloClient

    /**
     * Obtiene todos los combos de una sucursal
     */
    suspend fun getCombos(
        branchId: String? = null,
        availableOnly: Boolean = false
    ): CombosResult {
        return try {
            if (branchId == null) {
                return CombosResult.Error("Branch ID is required")
            }
            
            val response = client.query(
                GetCombosQuery(
                    branchId = branchId,
                    availableOnly = Optional.present(availableOnly)
                )
            ).execute()

            if (response.data != null) {
                val combos = response.data!!.combosByBranch.toDomainList()
                CombosResult.Success(combos)
            } else {
                CombosResult.Error(response.errors?.firstOrNull()?.message ?: "Unknown error")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Obtiene un combo por ID
     */
    suspend fun getCombo(comboId: String): CombosResult {
        return try {
            val response = client.query(
                GetComboQuery(comboId = comboId)
            ).execute()

            if (response.data?.combo != null) {
                val combo = response.data!!.combo!!.toDomain()
                CombosResult.Success(listOf(combo))
            } else {
                CombosResult.Error("Combo not found")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Crea un nuevo combo
     */
    suspend fun createCombo(
        branchId: String,
        name: String,
        description: String,
        image: String? = null,
        discountType: String,
        discountValue: Double,
        slots: List<Map<String, Any>>
    ): CombosResult {
        return try {
            val token = tokenManager.getToken()
            
            // Convertir slots a formato GraphQL
            val slotInputs = slots.map { slot ->
                ComboSlotInput(
                    name = slot["name"] as String,
                    minSelections = Optional.presentIfNotNull(slot["minSelections"] as? Int),
                    maxSelections = Optional.presentIfNotNull(slot["maxSelections"] as? Int),
                    options = (slot["options"] as List<Map<String, Any>>).map { option ->
                        ComboOptionInput(
                            productId = option["productId"] as String,
                            isDefault = Optional.presentIfNotNull(option["isDefault"] as? Boolean),
                            priceAdjustment = Optional.presentIfNotNull(option["priceAdjustment"] as? Double),
                            availableModifiers = Optional.presentIfNotNull(
                                (option["availableModifiers"] as? List<Map<String, Any>>)?.map { modifier ->
                                    ComboModifierInput(
                                        name = modifier["name"] as String,
                                        priceAdjustment = Optional.present(modifier["priceAdjustment"] as Double)
                                    )
                                }
                            )
                        )
                    }
                )
            }
            
            val response = client.mutation(
                CreateComboMutation(
                    input = CreateComboInput(
                        branchId = branchId,
                        name = name,
                        description = description,
                        image = Optional.presentIfNotNull(image),
                        discountType = Optional.present(when (discountType) {
                            "PERCENTAGE" -> GraphQLDiscountType.PERCENTAGE
                            "FIXED" -> GraphQLDiscountType.FIXED
                            else -> GraphQLDiscountType.NONE
                        }),
                        discountValue = Optional.present(discountValue),
                        slots = slotInputs
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                CombosResult.Success(emptyList())
            } else {
                CombosResult.Error(response.errors?.firstOrNull()?.message ?: "Error al crear combo")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Actualiza un combo existente
     */
    suspend fun updateCombo(
        comboId: String,
        name: String? = null,
        description: String? = null,
        image: String? = null,
        availability: Boolean? = null,
        discountType: String? = null,
        discountValue: Double? = null,
        slots: List<Map<String, Any>>? = null
    ): CombosResult {
        return try {
            val token = tokenManager.getToken()
            
            // Convertir slots si están presentes
            val slotInputs = slots?.map { slot ->
                ComboSlotInput(
                    name = slot["name"] as String,
                    minSelections = Optional.presentIfNotNull(slot["minSelections"] as? Int),
                    maxSelections = Optional.presentIfNotNull(slot["maxSelections"] as? Int),
                    options = (slot["options"] as List<Map<String, Any>>).map { option ->
                        ComboOptionInput(
                            productId = option["productId"] as String,
                            isDefault = Optional.presentIfNotNull(option["isDefault"] as? Boolean),
                            priceAdjustment = Optional.presentIfNotNull(option["priceAdjustment"] as? Double),
                            availableModifiers = Optional.presentIfNotNull(
                                (option["availableModifiers"] as? List<Map<String, Any>>)?.map { modifier ->
                                    ComboModifierInput(
                                        name = modifier["name"] as String,
                                        priceAdjustment = Optional.present(modifier["priceAdjustment"] as Double)
                                    )
                                }
                            )
                        )
                    }
                )
            }
            
            val response = client.mutation(
                UpdateComboMutation(
                    input = UpdateComboInput(
                        comboId = comboId,
                        name = Optional.presentIfNotNull(name),
                        description = Optional.presentIfNotNull(description),
                        image = Optional.presentIfNotNull(image),
                        availability = Optional.presentIfNotNull(availability),
                        discountType = Optional.presentIfNotNull(
                            discountType?.let {
                                when (it) {
                                    "PERCENTAGE" -> GraphQLDiscountType.PERCENTAGE
                                    "FIXED" -> GraphQLDiscountType.FIXED
                                    else -> GraphQLDiscountType.NONE
                                }
                            }
                        ),
                        discountValue = Optional.presentIfNotNull(discountValue),
                        slots = Optional.presentIfNotNull(slotInputs)
                    ),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                CombosResult.Success(emptyList())
            } else {
                CombosResult.Error(response.errors?.firstOrNull()?.message ?: "Error al actualizar combo")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Elimina un combo
     */
    suspend fun deleteCombo(comboId: String): CombosResult {
        return try {
            val token = tokenManager.getToken()

            val response = client.mutation(
                DeleteComboMutation(
                    comboId = comboId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data?.deleteCombo == true) {
                CombosResult.Success(emptyList())
            } else {
                CombosResult.Error("Error al eliminar combo")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * Cambia la disponibilidad de un combo
     */
    suspend fun toggleAvailability(comboId: String, availability: Boolean): CombosResult {
        return try {
            val token = tokenManager.getToken()

            val response = client.mutation(
                ToggleComboAvailabilityMutation(
                    comboId = comboId,
                    availability = availability,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.data != null) {
                CombosResult.Success(emptyList())
            } else {
                CombosResult.Error("Error al cambiar disponibilidad")
            }
        } catch (e: ApolloException) {
            CombosResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            CombosResult.Error("Unexpected error: ${e.message}")
        }
    }
}
