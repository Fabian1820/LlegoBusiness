package com.llego.business.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.CombosResult
import com.llego.shared.data.repositories.ComboRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar combos usando GraphQL
 */
class ComboViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private data class CombosQuery(
        val branchId: String?,
        val availableOnly: Boolean
    )

    private val repository = ComboRepository(tokenManager)

    private val _combosState = MutableStateFlow<CombosResult>(CombosResult.Loading)
    val combosState: StateFlow<CombosResult> = _combosState.asStateFlow()
    private var lastLoadedCombosQuery: CombosQuery? = null

    /**
     * Carga todos los combos o combos filtrados
     */
    fun loadCombos(
        branchId: String? = null,
        availableOnly: Boolean = false,
        force: Boolean = false
    ) {
        val previousQuery = lastLoadedCombosQuery
        val query = CombosQuery(
            branchId = branchId,
            availableOnly = availableOnly
        )

        if (!force &&
            query == lastLoadedCombosQuery &&
            _combosState.value is CombosResult.Success
        ) {
            return
        }

        viewModelScope.launch {
            val queryChanged = previousQuery != query
            if (_combosState.value !is CombosResult.Success || queryChanged) {
                _combosState.value = CombosResult.Loading
            }

            val result = repository.getCombos(
                branchId = branchId,
                availableOnly = availableOnly
            )
            _combosState.value = result
            if (result is CombosResult.Success) {
                lastLoadedCombosQuery = query
            }
        }
    }

    fun ensureCombosLoaded(
        branchId: String? = null,
        availableOnly: Boolean = false
    ) {
        loadCombos(
            branchId = branchId,
            availableOnly = availableOnly,
            force = false
        )
    }

    fun invalidateCombosCache() {
        lastLoadedCombosQuery = null
        _combosState.value = CombosResult.Loading
    }

    /**
     * Recarga los combos
     */
    fun refresh() {
        loadCombos(force = true)
    }

    /**
     * Crea un combo usando un path de imagen ya subido
     */
    suspend fun createComboWithImagePath(
        branchId: String,
        name: String,
        description: String,
        imagePath: String? = null,
        discountType: String,
        discountValue: Double,
        slots: List<Map<String, Any>>,
        giftOptions: List<String> = emptyList()
    ): CombosResult {
        invalidateCombosCache()
        _combosState.value = CombosResult.Loading
        val result = repository.createCombo(
            branchId = branchId,
            name = name,
            description = description,
            image = imagePath,
            discountType = discountType,
            discountValue = discountValue,
            slots = slots,
            giftOptions = giftOptions
        )
        _combosState.value = result
        return result
    }

    /**
     * Actualiza un combo usando un path de imagen ya subido
     */
    suspend fun updateComboWithImagePath(
        comboId: String,
        name: String? = null,
        description: String? = null,
        imagePath: String? = null,
        availability: Boolean? = null,
        discountType: String? = null,
        discountValue: Double? = null,
        slots: List<Map<String, Any>>? = null,
        giftOptions: List<String>? = null
    ): CombosResult {
        invalidateCombosCache()
        _combosState.value = CombosResult.Loading
        val result = repository.updateCombo(
            comboId = comboId,
            name = name,
            description = description,
            image = imagePath,
            availability = availability,
            discountType = discountType,
            discountValue = discountValue,
            slots = slots,
            giftOptions = giftOptions
        )
        _combosState.value = result
        return result
    }

    /**
     * Elimina un combo y devuelve el resultado de la operación
     */
    suspend fun deleteComboBlocking(comboId: String): CombosResult {
        invalidateCombosCache()
        _combosState.value = CombosResult.Loading
        val result = repository.deleteCombo(comboId)
        _combosState.value = result
        return result
    }

    /**
     * Cambia la disponibilidad de un combo
     */
    suspend fun toggleAvailability(comboId: String, availability: Boolean): CombosResult {
        val previousState = _combosState.value as? CombosResult.Success
        val previousCombos = previousState?.combos

        if (previousCombos == null) {
            val result = repository.toggleAvailability(comboId, availability)
            if (result is CombosResult.Success) {
                invalidateCombosCache()
            }
            return result
        }

        val optimisticCombos = previousCombos.map { combo ->
            if (combo.id == comboId) combo.copy(availability = availability) else combo
        }
        _combosState.value = CombosResult.Success(optimisticCombos)

        return when (val result = repository.toggleAvailability(comboId, availability)) {
            is CombosResult.Success -> {
                _combosState.value = CombosResult.Success(optimisticCombos)
                result
            }

            is CombosResult.Error -> {
                _combosState.value = CombosResult.Success(previousCombos)
                result
            }

            is CombosResult.Loading -> {
                _combosState.value = CombosResult.Success(previousCombos)
                CombosResult.Error("No se pudo actualizar disponibilidad")
            }
        }
    }
}
