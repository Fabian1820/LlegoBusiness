package com.llego.shared.data.repositories

import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class BusinessRepositoryState {
    private val _currentBusiness = MutableStateFlow<Business?>(null)
    val currentBusiness: StateFlow<Business?> = _currentBusiness.asStateFlow()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _currentBranch = MutableStateFlow<Branch?>(null)
    val currentBranch: StateFlow<Branch?> = _currentBranch.asStateFlow()

    fun setCurrentBusiness(value: Business?) {
        _currentBusiness.value = value
    }

    fun setBusinesses(value: List<Business>) {
        _businesses.value = value
    }

    fun setBranches(value: List<Branch>) {
        _branches.value = value
    }

    fun setCurrentBranch(value: Branch?) {
        _currentBranch.value = value
    }

    fun clear() {
        _currentBusiness.value = null
        _businesses.value = emptyList()
        _branches.value = emptyList()
        _currentBranch.value = null
    }
}
