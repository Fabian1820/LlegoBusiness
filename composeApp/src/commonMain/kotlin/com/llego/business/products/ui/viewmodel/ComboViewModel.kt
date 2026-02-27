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

    private val repository = ComboRepository(tokenManager)

    private val _combosState = MutableStateFlow<CombosResult>(CombosResult.Loading)
    val combosState: StateFlow<CombosResult> = _combosState.asStateFlow()

    /**
     * Carga todos los combos o combos filtrados
     */
    fun loadCombos(
        branchId: String? = null,
        availableOnly: Boolean = false
    ) {
        viewModelScope.launch {
            _combosState.value = CombosResult.Loading
            _combosState.value = repository.getCombos(
                branchId = branchId,
                availableOnly = availableOnly
            )
        }
    }

    /**
     * Recarga los combos
     */
    fun refresh() {
        loadCombos()
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
        slots: List<Map<String, Any>>
    ): CombosResult {
        _combosState.value = CombosResult.Loading
        val result = repository.createCombo(
            branchId = branchId,
            name = name,
            description = description,
            image = imagePath,
            discountType = discountType,
            discountValue = discountValue,
            slots = slots
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
        slots: List<Map<String, Any>>? = null
    ): CombosResult {
        _combosState.value = CombosResult.Loading
        val result = repository.updateCombo(
            comboId = comboId,
            name = name,
            description = description,
            image = imagePath,
            availability = availability,
            discountType = discountType,
            discountValue = discountValue,
            slots = slots
        )
        _combosState.value = result
        return result
    }

    /**
     * Elimina un combo y devuelve el resultado de la operación
     */
    suspend fun deleteComboBlocking(comboId: String): CombosResult {
        _combosState.value = CombosResult.Loading
        val result = repository.deleteCombo(comboId)
        _combosState.value = result
        return result
    }

    /**
     * Cambia la disponibilidad de un combo
     */
    suspend fun toggleAvailability(comboId: String, availability: Boolean): CombosResult {
        _combosState.value = CombosResult.Loading
        val result = repository.toggleAvailability(comboId, availability)
        _combosState.value = result
        return result
    }
}
