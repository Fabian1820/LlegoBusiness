package com.llego.business.invitations.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.invitations.data.model.*
import com.llego.business.invitations.data.repository.InvitationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InvitationUiState {
    object Idle : InvitationUiState()
    object Loading : InvitationUiState()
    data class Success(val invitation: Invitation) : InvitationUiState()
    data class Error(val message: String) : InvitationUiState()
}

sealed class InvitationListState {
    object Idle : InvitationListState()
    object Loading : InvitationListState()
    data class Success(val invitations: List<Invitation>) : InvitationListState()
    data class Error(val message: String) : InvitationListState()
}

sealed class RedeemState {
    object Idle : RedeemState()
    object Loading : RedeemState()
    data class Success(val invitation: Invitation) : RedeemState()
    data class Error(val message: String) : RedeemState()
}

sealed class BusinessAccessListState {
    object Idle : BusinessAccessListState()
    object Loading : BusinessAccessListState()
    data class Success(val accesses: List<BusinessAccess>) : BusinessAccessListState()
    data class Error(val message: String) : BusinessAccessListState()
}

class InvitationViewModel(
    private val repository: InvitationRepository
) : ViewModel() {
    
    private val _generateState = MutableStateFlow<InvitationUiState>(InvitationUiState.Idle)
    val generateState: StateFlow<InvitationUiState> = _generateState.asStateFlow()
    
    private val _invitationsState = MutableStateFlow<InvitationListState>(InvitationListState.Idle)
    val invitationsState: StateFlow<InvitationListState> = _invitationsState.asStateFlow()
    
    private val _redeemState = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val redeemState: StateFlow<RedeemState> = _redeemState.asStateFlow()

    private val _businessAccessState = MutableStateFlow<BusinessAccessListState>(BusinessAccessListState.Idle)
    val businessAccessState: StateFlow<BusinessAccessListState> = _businessAccessState.asStateFlow()

    fun generateInvitationCode(
        input: GenerateInvitationInput
    ) {
        viewModelScope.launch {
            _generateState.value = InvitationUiState.Loading

            repository.generateInvitationCode(input)
                .onSuccess { invitation ->
                    _generateState.value = InvitationUiState.Success(invitation)
                }
                .onFailure { error ->
                    _generateState.value = InvitationUiState.Error(
                        error.message ?: "Error generating invitation code"
                    )
                }
        }
    }
    
    fun loadInvitations(businessId: String, activeOnly: Boolean = false) {
        viewModelScope.launch {
            _invitationsState.value = InvitationListState.Loading
            
            val result = if (activeOnly) {
                repository.getActiveInvitationsByBusiness(businessId)
            } else {
                repository.getInvitationsByBusiness(businessId)
            }
            
            result
                .onSuccess { invitations ->
                    _invitationsState.value = InvitationListState.Success(invitations)
                }
                .onFailure { error ->
                    _invitationsState.value = InvitationListState.Error(
                        error.message ?: "Error loading invitations"
                    )
                }
        }
    }
    
    fun redeemInvitationCode(code: String) {
        println("DEBUG InvitationViewModel: Iniciando canje de código: $code")
        
        viewModelScope.launch {
            _redeemState.value = RedeemState.Loading

            repository.acceptInvitationCode(code)
                .onSuccess { invitation ->
                    println("DEBUG InvitationViewModel: Respuesta recibida")
                    println("  - accessStatus: ${invitation.accessStatus}")
                    println("  - isUsable: ${invitation.isUsable}")
                    println("  - status: ${invitation.status}")
                    println("  - accessExpiresAt: ${invitation.accessExpiresAt}")
                    
                    // CORRECCIÓN: Cuando el backend acepta un código exitosamente,
                    // retorna status=USED e isUsable=false (porque ya no se puede reusar)
                    // pero accessStatus=ACTIVE indica que el acceso está activo.
                    // Solo debemos rechazar si accessStatus=EXPIRED
                    when {
                        invitation.accessStatus == AccessStatus.EXPIRED -> {
                            println("DEBUG InvitationViewModel: Acceso expirado")
                            _redeemState.value = RedeemState.Error(
                                "Tu acceso a este negocio ha expirado. Solicita un nuevo código al administrador."
                            )
                        }
                        invitation.accessStatus == AccessStatus.ACTIVE -> {
                            // Código aceptado exitosamente
                            // El backend marca status=USED e isUsable=false después de aceptar
                            println("DEBUG InvitationViewModel: Código aceptado exitosamente (accessStatus=ACTIVE)")
                            _redeemState.value = RedeemState.Success(invitation)
                        }
                        invitation.accessStatus == AccessStatus.PENDING -> {
                            // Código aceptado pero acceso pendiente
                            println("DEBUG InvitationViewModel: Código aceptado, acceso pendiente")
                            _redeemState.value = RedeemState.Success(invitation)
                        }
                        else -> {
                            println("DEBUG InvitationViewModel: Estado inesperado: ${invitation.accessStatus}")
                            _redeemState.value = RedeemState.Error(
                                "Estado de invitación inesperado. Contacta al administrador."
                            )
                        }
                    }
                }
                .onFailure { error ->
                    println("DEBUG InvitationViewModel: Error - ${error.message}")
                    
                    // Mejorar mensajes según el error del backend
                    val message = when {
                        error.message?.contains("ya tienes acceso", ignoreCase = true) == true -> {
                            "Ya tienes acceso a este negocio. Recarga la aplicación si no ves los negocios."
                        }
                        error.message?.contains("already has access", ignoreCase = true) == true -> {
                            "Ya tienes acceso a este negocio. Recarga la aplicación si no ves los negocios."
                        }
                        error.message?.contains("ya eres manager", ignoreCase = true) == true -> {
                            "Ya eres manager de esta sucursal."
                        }
                        error.message?.contains("ya fue usado", ignoreCase = true) == true -> {
                            "Este código ya fue utilizado."
                        }
                        error.message?.contains("revocado", ignoreCase = true) == true -> {
                            "Este código ha sido revocado."
                        }
                        error.message?.contains("no es valido", ignoreCase = true) == true -> {
                            "Código no válido. Verifica que lo hayas escrito correctamente."
                        }
                        error.message?.contains("not found", ignoreCase = true) == true -> {
                            "Código no encontrado. Verifica que lo hayas escrito correctamente."
                        }
                        else -> {
                            error.message ?: "Error al canjear el código"
                        }
                    }
                    
                    _redeemState.value = RedeemState.Error(message)
                }
        }
    }

    fun loadBusinessAccess(businessId: String) {
        viewModelScope.launch {
            _businessAccessState.value = BusinessAccessListState.Loading

            repository.getBusinessAccessByBusiness(businessId)
                .onSuccess { accesses ->
                    _businessAccessState.value = BusinessAccessListState.Success(accesses)
                }
                .onFailure { error ->
                    _businessAccessState.value = BusinessAccessListState.Error(
                        error.message ?: "Error loading business access"
                    )
                }
        }
    }

    fun revokeInvitation(invitationId: String, businessId: String) {
        viewModelScope.launch {
            repository.revokeInvitationCode(invitationId)
                .onSuccess {
                    // Reload invitations after revoking
                    loadInvitations(businessId)
                }
                .onFailure { error ->
                    _invitationsState.value = InvitationListState.Error(
                        error.message ?: "Error revoking invitation"
                    )
                }
        }
    }
    
    fun resetGenerateState() {
        _generateState.value = InvitationUiState.Idle
    }
    
    fun resetRedeemState() {
        _redeemState.value = RedeemState.Idle
    }
}
