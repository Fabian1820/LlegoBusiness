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

class InvitationViewModel(
    private val repository: InvitationRepository
) : ViewModel() {
    
    private val _generateState = MutableStateFlow<InvitationUiState>(InvitationUiState.Idle)
    val generateState: StateFlow<InvitationUiState> = _generateState.asStateFlow()
    
    private val _invitationsState = MutableStateFlow<InvitationListState>(InvitationListState.Idle)
    val invitationsState: StateFlow<InvitationListState> = _invitationsState.asStateFlow()
    
    private val _redeemState = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val redeemState: StateFlow<RedeemState> = _redeemState.asStateFlow()
    
    fun generateInvitationCode(
        input: GenerateInvitationInput
    ) {
        viewModelScope.launch {
            println("InvitationViewModel: Iniciando generación de código - businessId=${input.businessId}, tipo=${input.invitationType}, branchId=${input.branchId}")
            _generateState.value = InvitationUiState.Loading

            repository.generateInvitationCode(input)
                .onSuccess { invitation ->
                    println("InvitationViewModel: Código generado exitosamente - code=${invitation.code}, id=${invitation.id}")
                    _generateState.value = InvitationUiState.Success(invitation)
                }
                .onFailure { error ->
                    println("InvitationViewModel: Error al generar código - ${error.message}")
                    error.printStackTrace()
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
        viewModelScope.launch {
            _redeemState.value = RedeemState.Loading
            
            repository.acceptInvitationCode(code)
                .onSuccess { invitation ->
                    _redeemState.value = RedeemState.Success(invitation)
                }
                .onFailure { error ->
                    _redeemState.value = RedeemState.Error(
                        error.message ?: "Error redeeming invitation code"
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
