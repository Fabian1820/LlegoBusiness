package com.llego.business.delivery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.delivery.data.model.BranchDeliveryRequest
import com.llego.business.delivery.data.model.DeliveryRequestStatus
import com.llego.business.delivery.data.model.LinkedDriverSummary
import com.llego.business.delivery.data.repository.DeliveryLinkRepository
import com.llego.shared.data.model.Branch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class DeliveryLinkUiState(
    val activeBranchId: String? = null,
    val isEntryPointLoading: Boolean = false,
    val entryPointQueryFailed: Boolean = false,
    val pendingRequestCount: Int = 0,
    val pendingRequestBranchIds: Set<String> = emptySet(),
    val suggestedEntryBranchId: String? = null,
    val branchUsesAppMessaging: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdatingDeliveryMode: Boolean = false,
    val actionRequestId: String? = null,
    val pendingRequests: List<BranchDeliveryRequest> = emptyList(),
    val linkedDrivers: List<LinkedDriverSummary> = emptyList(),
    val processedRequests: List<BranchDeliveryRequest> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val hasPendingRequests: Boolean get() = pendingRequestCount > 0
}

class DeliveryLinkViewModel(
    private val repository: DeliveryLinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryLinkUiState())
    val uiState: StateFlow<DeliveryLinkUiState> = _uiState.asStateFlow()

    fun loadEntryPointForBranches(
        branches: List<Branch>,
        currentBranchId: String?
    ) {
        if (branches.isEmpty()) {
            _uiState.update {
                it.copy(
                    isEntryPointLoading = false,
                    entryPointQueryFailed = false,
                    pendingRequestCount = 0,
                    pendingRequestBranchIds = emptySet(),
                    suggestedEntryBranchId = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isEntryPointLoading = true) }

            val branchResults = coroutineScope {
                branches.map { branch ->
                    async {
                        val result = repository.getBranchLinkRequests(
                            branchId = branch.id,
                            status = DeliveryRequestStatus.PENDING
                        )
                        val pendingCount = result.getOrElse { emptyList() }.size
                        Triple(branch.id, pendingCount, result.isFailure)
                    }
                }.awaitAll()
            }

            val pendingCounts = branchResults.associate { it.first to it.second }
            val failedBranchCount = branchResults.count { it.third }

            val pendingBranchIds = pendingCounts
                .filterValues { it > 0 }
                .keys
                .toSet()

            val currentBranch = branches.firstOrNull { it.id == currentBranchId }
            val firstOwnDeliveryBranch = branches.firstOrNull { !it.useAppMessaging }
            val firstPendingBranch = branches.firstOrNull { it.id in pendingBranchIds }

            val suggestedBranchId = when {
                currentBranch != null &&
                    (!currentBranch.useAppMessaging || currentBranch.id in pendingBranchIds) -> currentBranch.id
                firstPendingBranch != null -> firstPendingBranch.id
                firstOwnDeliveryBranch != null -> firstOwnDeliveryBranch.id
                else -> currentBranchId
            }

            _uiState.update {
                it.copy(
                    isEntryPointLoading = false,
                    entryPointQueryFailed = failedBranchCount > 0,
                    pendingRequestCount = pendingCounts.values.sum(),
                    pendingRequestBranchIds = pendingBranchIds,
                    suggestedEntryBranchId = suggestedBranchId,
                    branchUsesAppMessaging = currentBranch?.useAppMessaging ?: true
                )
            }
        }
    }

    fun loadEntryPoint(branchId: String?, branchUsesAppMessaging: Boolean) {
        if (branchId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isEntryPointLoading = false,
                    entryPointQueryFailed = false,
                    pendingRequestCount = 0,
                    pendingRequestBranchIds = emptySet(),
                    suggestedEntryBranchId = null,
                    branchUsesAppMessaging = branchUsesAppMessaging
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEntryPointLoading = true,
                    branchUsesAppMessaging = branchUsesAppMessaging
                )
            }

            repository.getBranchLinkRequests(
                branchId = branchId,
                status = DeliveryRequestStatus.PENDING
            ).onSuccess { pending ->
                _uiState.update {
                    it.copy(
                        isEntryPointLoading = false,
                        entryPointQueryFailed = false,
                        pendingRequestCount = pending.size,
                        pendingRequestBranchIds = if (pending.isNotEmpty()) setOf(branchId) else emptySet(),
                        suggestedEntryBranchId = if (pending.isNotEmpty()) branchId else null,
                        branchUsesAppMessaging = branchUsesAppMessaging
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isEntryPointLoading = false,
                        entryPointQueryFailed = true,
                        pendingRequestCount = 0,
                        pendingRequestBranchIds = emptySet(),
                        suggestedEntryBranchId = null,
                        branchUsesAppMessaging = branchUsesAppMessaging
                    )
                }
            }
        }
    }

    fun loadManagementData(
        branchId: String,
        branchUsesAppMessaging: Boolean,
        isManualRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                val isBranchChanged = state.activeBranchId != branchId
                state.copy(
                    activeBranchId = branchId,
                    isLoading = !isManualRefresh,
                    isRefreshing = isManualRefresh,
                    branchUsesAppMessaging = branchUsesAppMessaging,
                    pendingRequestCount = if (isBranchChanged) 0 else state.pendingRequestCount,
                    pendingRequests = if (isBranchChanged) emptyList() else state.pendingRequests,
                    linkedDrivers = if (isBranchChanged) emptyList() else state.linkedDrivers,
                    processedRequests = if (isBranchChanged) emptyList() else state.processedRequests,
                    errorMessage = null
                )
            }

            repository.getBranchLinkRequests(branchId)
                .onSuccess { requests ->
                    val pending = requests.filter { it.status == DeliveryRequestStatus.PENDING }
                    val accepted = requests.filter { it.status == DeliveryRequestStatus.ACCEPTED }
                    val processed = requests.filter { it.status != DeliveryRequestStatus.PENDING }

                    val linkedDrivers = accepted
                        .asSequence()
                        .mapNotNull { request ->
                            request.deliveryPerson?.let {
                                LinkedDriverSummary(
                                    requestId = request.id,
                                    linkedAt = request.respondedAt ?: request.updatedAt,
                                    deliveryPerson = it
                                )
                            }
                        }
                        .distinctBy { it.deliveryPerson.id }
                        .toList()

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            pendingRequestCount = pending.size,
                            pendingRequests = pending,
                            linkedDrivers = linkedDrivers,
                            processedRequests = processed
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            pendingRequestCount = 0,
                            pendingRequests = emptyList(),
                            linkedDrivers = emptyList(),
                            processedRequests = emptyList(),
                            errorMessage = throwable.message ?: "No fue posible cargar la gestion de choferes"
                        )
                    }
                }
        }
    }

    fun respondToRequest(
        branchId: String,
        requestId: String,
        accept: Boolean,
        onDeliveryModeEnabled: (() -> Unit)? = null
    ) {
        val currentUsesAppMessaging = _uiState.value.branchUsesAppMessaging

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    actionRequestId = requestId,
                    errorMessage = null,
                    successMessage = null
                )
            }

            repository.respondBranchLinkRequest(
                requestId = requestId,
                accept = accept
            ).onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        actionRequestId = null,
                        errorMessage = throwable.message ?: "No fue posible responder la solicitud"
                    )
                }
                return@launch
            }

            var updatedBranchMode = currentUsesAppMessaging
            if (accept && currentUsesAppMessaging) {
                repository.enableOwnDeliveryForBranch(branchId)
                    .onSuccess {
                        updatedBranchMode = false
                        onDeliveryModeEnabled?.invoke()
                    }
                    .onFailure { throwable ->
                        _uiState.update { state ->
                            state.copy(
                                actionRequestId = null,
                                errorMessage = throwable.message
                                    ?: "Solicitud aceptada, pero no se pudo activar delivery propio"
                            )
                        }
                        loadManagementData(
                            branchId = branchId,
                            branchUsesAppMessaging = currentUsesAppMessaging,
                            isManualRefresh = true
                        )
                        loadEntryPoint(
                            branchId = branchId,
                            branchUsesAppMessaging = currentUsesAppMessaging
                        )
                        return@launch
                    }
            }

            _uiState.update { state ->
                state.copy(
                    actionRequestId = null,
                    branchUsesAppMessaging = updatedBranchMode,
                    successMessage = if (accept) {
                        if (currentUsesAppMessaging && !updatedBranchMode) {
                            "Solicitud aceptada y delivery propio activado automaticamente"
                        } else {
                            "Solicitud aceptada correctamente"
                        }
                    } else {
                        "Solicitud rechazada"
                    }
                )
            }

            loadManagementData(
                branchId = branchId,
                branchUsesAppMessaging = updatedBranchMode,
                isManualRefresh = true
            )
            loadEntryPoint(
                branchId = branchId,
                branchUsesAppMessaging = updatedBranchMode
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun disableOwnDeliveryForBranch(
        branchId: String,
        onDeliveryModeChanged: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isUpdatingDeliveryMode = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            repository.disableOwnDeliveryForBranch(branchId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isUpdatingDeliveryMode = false,
                            branchUsesAppMessaging = true,
                            successMessage = "Delivery propio desactivado. Ahora usas mensajeria de la app."
                        )
                    }

                    onDeliveryModeChanged?.invoke()

                    loadManagementData(
                        branchId = branchId,
                        branchUsesAppMessaging = true,
                        isManualRefresh = true
                    )
                    loadEntryPoint(
                        branchId = branchId,
                        branchUsesAppMessaging = true
                    )
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isUpdatingDeliveryMode = false,
                            errorMessage = throwable.message
                                ?: "No fue posible desactivar el delivery propio"
                        )
                    }
                }
        }
    }
}
