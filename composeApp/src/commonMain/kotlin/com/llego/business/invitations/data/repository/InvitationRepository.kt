package com.llego.business.invitations.data.repository

import com.apollographql.apollo.ApolloClient
import com.llego.multiplatform.graphql.AcceptInvitationCodeMutation
import com.llego.multiplatform.graphql.ActiveInvitationsByBusinessQuery
import com.llego.multiplatform.graphql.GenerateInvitationCodeMutation
import com.llego.multiplatform.graphql.InvitationByCodeQuery
import com.llego.multiplatform.graphql.InvitationsByBusinessQuery
import com.llego.multiplatform.graphql.RevokeInvitationCodeMutation
import com.llego.business.invitations.data.mappers.*
import com.llego.business.invitations.data.model.GenerateInvitationInput
import com.llego.business.invitations.data.model.Invitation
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.data.auth.TokenManager

class InvitationRepository(
    private val apolloClient: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager = TokenManager()
) {
    
    suspend fun generateInvitationCode(
        input: GenerateInvitationInput
    ): Result<Invitation> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token found"))
            
            println("InvitationRepository: Ejecutando mutación - businessId=${input.businessId}, tipo=${input.invitationType}")
            val response = apolloClient.mutation(
                GenerateInvitationCodeMutation(
                    input = input.toGraphQLInput(),
                    jwt = token
                )
            ).execute()

            println("InvitationRepository: Respuesta recibida - hasErrors=${response.hasErrors()}, data=${response.data}")

            if (response.hasErrors()) {
                val errorMsg = response.errors?.firstOrNull()?.message ?: "Error generating invitation code"
                println("InvitationRepository: Error en respuesta - $errorMsg")
                Result.failure(Exception(errorMsg))
            } else {
                val invitation = response.data?.generateInvitationCode?.toInvitation()
                if (invitation != null) {
                    println("InvitationRepository: Invitación creada - code=${invitation.code}")
                    Result.success(invitation)
                } else {
                    println("InvitationRepository: No data returned")
                    Result.failure(Exception("No data returned"))
                }
            }
        } catch (e: Exception) {
            println("InvitationRepository: Excepción - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun acceptInvitationCode(
        code: String
    ): Result<Invitation> {
        return try {
            val token = tokenManager.getToken()
            println("InvitationRepository.acceptInvitationCode: Iniciando...")
            println("InvitationRepository.acceptInvitationCode: code=${code.uppercase()}")
            println("InvitationRepository.acceptInvitationCode: token disponible=${token != null}, length=${token?.length ?: 0}")

            val response = apolloClient.mutation(
                AcceptInvitationCodeMutation(
                    input = com.llego.multiplatform.graphql.type.AcceptInvitationInput(
                        code = code.uppercase()
                    ),
                    jwt = com.apollographql.apollo.api.Optional.presentIfNotNull(token)
                )
            ).execute()

            println("InvitationRepository.acceptInvitationCode: Respuesta recibida")
            println("InvitationRepository.acceptInvitationCode: hasErrors=${response.hasErrors()}")
            println("InvitationRepository.acceptInvitationCode: errors=${response.errors}")
            println("InvitationRepository.acceptInvitationCode: data=${response.data}")
            println("InvitationRepository.acceptInvitationCode: exception=${response.exception}")

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message} (path: ${it.path}, extensions: ${it.extensions})" }
                println("InvitationRepository.acceptInvitationCode: GraphQL errors = $errors")
                val errorMsg = response.errors?.firstOrNull()?.message ?: "Error accepting invitation code"
                Result.failure(Exception(errorMsg))
            } else {
                println("InvitationRepository.acceptInvitationCode: response.data?.acceptInvitationCode = ${response.data?.acceptInvitationCode}")

                val invitation = response.data?.acceptInvitationCode?.toInvitation()
                if (invitation != null) {
                    println("InvitationRepository.acceptInvitationCode: Invitación aceptada exitosamente - id=${invitation.id}, code=${invitation.code}")
                    Result.success(invitation)
                } else {
                    println("InvitationRepository.acceptInvitationCode: No data returned (acceptInvitationCode es null)")
                    Result.failure(Exception("No data returned"))
                }
            }
        } catch (e: Exception) {
            println("InvitationRepository.acceptInvitationCode: Exception = ${e.message}")
            println("InvitationRepository.acceptInvitationCode: Exception type = ${e::class.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun revokeInvitationCode(
        invitationId: String
    ): Result<Boolean> {
        return try {
            val response = apolloClient.mutation(
                RevokeInvitationCodeMutation(
                    invitationId = invitationId
                )
            ).execute()
            
            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error revoking invitation code"))
            } else {
                val success = response.data?.revokeInvitationCode ?: false
                Result.success(success)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getInvitationByCode(code: String): Result<Invitation?> {
        return try {
            val response = apolloClient.query(
                InvitationByCodeQuery(code = code.uppercase())
            ).execute()
            
            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error fetching invitation"))
            } else {
                val invitation = response.data?.invitationByCode?.toInvitation()
                Result.success(invitation)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getInvitationsByBusiness(
        businessId: String
    ): Result<List<Invitation>> {
        return try {
            val response = apolloClient.query(
                InvitationsByBusinessQuery(
                    businessId = businessId
                )
            ).execute()
            
            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Error fetching invitations"
                // Si el error es sobre tipo desconocido, probablemente no hay invitaciones
                if (errorMessage.contains("unknown", ignoreCase = true) || 
                    errorMessage.contains("type", ignoreCase = true)) {
                    Result.success(emptyList())
                } else {
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val invitations = response.data?.invitationsByBusiness?.mapNotNull { 
                    try {
                        it.toInvitation()
                    } catch (e: Exception) {
                        null // Ignorar invitaciones que no se puedan parsear
                    }
                } ?: emptyList()
                Result.success(invitations)
            }
        } catch (e: Exception) {
            // Si hay un error de parsing, devolver lista vacía en lugar de fallar
            if (e.message?.contains("unknown", ignoreCase = true) == true ||
                e.message?.contains("type", ignoreCase = true) == true) {
                Result.success(emptyList())
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getActiveInvitationsByBusiness(
        businessId: String
    ): Result<List<Invitation>> {
        return try {
            val response = apolloClient.query(
                ActiveInvitationsByBusinessQuery(
                    businessId = businessId
                )
            ).execute()
            
            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message ?: "Error fetching active invitations"
                // Si el error es sobre tipo desconocido, probablemente no hay invitaciones
                if (errorMessage.contains("unknown", ignoreCase = true) || 
                    errorMessage.contains("type", ignoreCase = true)) {
                    Result.success(emptyList())
                } else {
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val invitations = response.data?.activeInvitationsByBusiness?.mapNotNull { 
                    try {
                        it.toInvitation()
                    } catch (e: Exception) {
                        null // Ignorar invitaciones que no se puedan parsear
                    }
                } ?: emptyList()
                Result.success(invitations)
            }
        } catch (e: Exception) {
            // Si hay un error de parsing, devolver lista vacía en lugar de fallar
            if (e.message?.contains("unknown", ignoreCase = true) == true ||
                e.message?.contains("type", ignoreCase = true) == true) {
                Result.success(emptyList())
            } else {
                Result.failure(e)
            }
        }
    }
}
