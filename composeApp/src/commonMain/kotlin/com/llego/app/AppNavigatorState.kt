package com.llego.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.llego.business.orders.ui.screens.ConfirmationType
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Product

@Stable
class AppNavigatorState {
    var showProfile by mutableStateOf(false)
    var showBranchesManagement by mutableStateOf(false)
    var showStatistics by mutableStateOf(false)
    var showInvitations by mutableStateOf(false)
    var showDeliveryManagement by mutableStateOf(false)
    var showAddProduct by mutableStateOf(false)
    var productToEdit by mutableStateOf<Product?>(null)
    var showProductDetail by mutableStateOf(false)
    var productToView by mutableStateOf<Product?>(null)
    var showProductSearch by mutableStateOf(false)
    var showOrderDetail by mutableStateOf(false)
    var selectedOrderId by mutableStateOf<String?>(null)
    var selectedHomeTabIndex by mutableIntStateOf(0)
    var branchCreateBusinessId by mutableStateOf<String?>(null)
    var branchToEdit by mutableStateOf<Branch?>(null)

    var showMapSelection by mutableStateOf(false)
    var mapSelectionTitle by mutableStateOf("")
    var mapSelectionInitialLat by mutableStateOf(0.0)
    var mapSelectionInitialLng by mutableStateOf(0.0)
    var mapSelectionCallback by mutableStateOf<((Double, Double) -> Unit)?>(null)

    var confirmationType by mutableStateOf<ConfirmationType?>(null)
    var confirmationOrderNumber by mutableStateOf("")

    fun openMapSelection(
        title: String,
        lat: Double,
        lng: Double,
        callback: (Double, Double) -> Unit
    ) {
        mapSelectionTitle = title
        mapSelectionInitialLat = lat
        mapSelectionInitialLng = lng
        mapSelectionCallback = callback
        showMapSelection = true
    }

    fun closeMapSelection() {
        showMapSelection = false
        mapSelectionCallback = null
    }

    fun showConfirmation(type: ConfirmationType, orderNumber: String) {
        confirmationType = type
        confirmationOrderNumber = orderNumber
    }

    fun dismissConfirmation() {
        confirmationType = null
        confirmationOrderNumber = ""
    }

    fun resetForNewSession(homeTabIndex: Int = 0) {
        showProfile = false
        showBranchesManagement = false
        showStatistics = false
        showInvitations = false
        showDeliveryManagement = false
        showAddProduct = false
        productToEdit = null
        showProductDetail = false
        productToView = null
        showProductSearch = false
        showOrderDetail = false
        selectedOrderId = null
        selectedHomeTabIndex = homeTabIndex
        branchCreateBusinessId = null
        branchToEdit = null
        dismissConfirmation()
        closeMapSelection()
    }
}

@Suppress("FunctionName")
@Composable
fun rememberAppNavigatorState(): AppNavigatorState = remember { AppNavigatorState() }
