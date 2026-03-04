package com.llego.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.llego.business.orders.ui.screens.ConfirmationType
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Combo
import com.llego.shared.data.model.Product

@Stable
class AppNavigatorState {
    var showProfile by mutableStateOf(false)
    var showBranchesManagement by mutableStateOf(false)
    var showStatistics by mutableStateOf(false)
    var showInvitations by mutableStateOf(false)
    var showDeliveryManagement by mutableStateOf(false)
    var showAddProduct by mutableStateOf(false)
    var showAddShowcase by mutableStateOf(false)
    var productToEdit by mutableStateOf<Product?>(null)
    var showProductDetail by mutableStateOf(false)
    var productToView by mutableStateOf<Product?>(null)
    var showProductSearch by mutableStateOf(false)
    var showAddCombo by mutableStateOf(false)
    var comboToEdit by mutableStateOf<Combo?>(null)
    var showComboDetail by mutableStateOf(false)
    var comboToView by mutableStateOf<Combo?>(null)
    var showOrderDetail by mutableStateOf(false)
    var selectedOrderId by mutableStateOf<String?>(null)
    var selectedHomeTabIndex by mutableIntStateOf(0)
    var branchCreateBusinessId by mutableStateOf<String?>(null)
    var branchToEdit by mutableStateOf<Branch?>(null)
    var businessToEdit by mutableStateOf<Business?>(null)

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

    fun canConsumeBackPress(): Boolean {
        return showMapSelection ||
            confirmationType != null ||
            showProductSearch ||
            showOrderDetail ||
            showProductDetail ||
            showAddProduct ||
            showComboDetail ||
            showAddCombo ||
            showAddShowcase ||
            showStatistics ||
            branchToEdit != null ||
            branchCreateBusinessId != null ||
            businessToEdit != null ||
            showBranchesManagement ||
            showDeliveryManagement ||
            showInvitations ||
            showProfile
    }

    fun consumeBackPress(): Boolean {
        return when {
            showMapSelection -> {
                closeMapSelection()
                true
            }

            confirmationType != null -> {
                dismissConfirmation()
                true
            }

            showProductSearch -> {
                showProductSearch = false
                true
            }

            showOrderDetail -> {
                showOrderDetail = false
                selectedOrderId = null
                true
            }

            showProductDetail -> {
                showProductDetail = false
                productToView = null
                true
            }

            showAddProduct -> {
                showAddProduct = false
                productToEdit = null
                true
            }

            showComboDetail -> {
                showComboDetail = false
                comboToView = null
                true
            }

            showAddCombo -> {
                showAddCombo = false
                comboToEdit = null
                true
            }

            showAddShowcase -> {
                showAddShowcase = false
                true
            }

            showStatistics -> {
                showStatistics = false
                true
            }

            branchToEdit != null -> {
                branchToEdit = null
                true
            }

            branchCreateBusinessId != null -> {
                branchCreateBusinessId = null
                true
            }

            businessToEdit != null -> {
                businessToEdit = null
                true
            }

            showBranchesManagement -> {
                showBranchesManagement = false
                true
            }

            showDeliveryManagement -> {
                showDeliveryManagement = false
                true
            }

            showInvitations -> {
                showInvitations = false
                true
            }

            showProfile -> {
                showProfile = false
                true
            }

            else -> false
        }
    }

    fun resetForNewSession(homeTabIndex: Int = 0) {
        showProfile = false
        showBranchesManagement = false
        showStatistics = false
        showInvitations = false
        showDeliveryManagement = false
        showAddProduct = false
        showAddShowcase = false
        productToEdit = null
        showProductDetail = false
        productToView = null
        showProductSearch = false
        showAddCombo = false
        comboToEdit = null
        showComboDetail = false
        comboToView = null
        showOrderDetail = false
        selectedOrderId = null
        selectedHomeTabIndex = homeTabIndex
        branchCreateBusinessId = null
        branchToEdit = null
        businessToEdit = null
        dismissConfirmation()
        closeMapSelection()
    }

    private fun toSaveMap(): Map<String, Any?> {
        val shouldRestoreAddProduct = showAddProduct && productToEdit == null
        val shouldRestoreAddCombo = showAddCombo && comboToEdit == null

        return mapOf(
            KEY_SHOW_PROFILE to showProfile,
            KEY_SHOW_BRANCHES_MANAGEMENT to showBranchesManagement,
            KEY_SHOW_STATISTICS to showStatistics,
            KEY_SHOW_INVITATIONS to showInvitations,
            KEY_SHOW_DELIVERY_MANAGEMENT to showDeliveryManagement,
            KEY_SHOW_ADD_PRODUCT to shouldRestoreAddProduct,
            KEY_SHOW_ADD_SHOWCASE to showAddShowcase,
            KEY_SHOW_PRODUCT_DETAIL to false,
            KEY_SHOW_PRODUCT_SEARCH to showProductSearch,
            KEY_SHOW_ADD_COMBO to shouldRestoreAddCombo,
            KEY_SHOW_COMBO_DETAIL to false,
            KEY_SHOW_ORDER_DETAIL to showOrderDetail,
            KEY_SELECTED_ORDER_ID to selectedOrderId,
            KEY_SELECTED_HOME_TAB_INDEX to selectedHomeTabIndex,
            KEY_BRANCH_CREATE_BUSINESS_ID to branchCreateBusinessId,
            KEY_CONFIRMATION_TYPE to confirmationType?.name,
            KEY_CONFIRMATION_ORDER_NUMBER to confirmationOrderNumber
        )
    }

    private fun restoreFromMap(values: Map<String, Any?>) {
        showProfile = values[KEY_SHOW_PROFILE] as? Boolean ?: false
        showBranchesManagement = values[KEY_SHOW_BRANCHES_MANAGEMENT] as? Boolean ?: false
        showStatistics = values[KEY_SHOW_STATISTICS] as? Boolean ?: false
        showInvitations = values[KEY_SHOW_INVITATIONS] as? Boolean ?: false
        showDeliveryManagement = values[KEY_SHOW_DELIVERY_MANAGEMENT] as? Boolean ?: false
        showAddProduct = values[KEY_SHOW_ADD_PRODUCT] as? Boolean ?: false
        showAddShowcase = values[KEY_SHOW_ADD_SHOWCASE] as? Boolean ?: false
        showProductDetail = false
        showProductSearch = values[KEY_SHOW_PRODUCT_SEARCH] as? Boolean ?: false
        showAddCombo = values[KEY_SHOW_ADD_COMBO] as? Boolean ?: false
        showComboDetail = false
        showOrderDetail = values[KEY_SHOW_ORDER_DETAIL] as? Boolean ?: false
        selectedOrderId = values[KEY_SELECTED_ORDER_ID] as? String
        selectedHomeTabIndex = (values[KEY_SELECTED_HOME_TAB_INDEX] as? Number)?.toInt() ?: 0
        branchCreateBusinessId = values[KEY_BRANCH_CREATE_BUSINESS_ID] as? String

        productToEdit = null
        productToView = null
        comboToEdit = null
        comboToView = null
        branchToEdit = null
        businessToEdit = null
        showMapSelection = false
        mapSelectionTitle = ""
        mapSelectionInitialLat = 0.0
        mapSelectionInitialLng = 0.0
        mapSelectionCallback = null

        val confirmationTypeName = values[KEY_CONFIRMATION_TYPE] as? String
        confirmationType = confirmationTypeName?.let { name ->
            runCatching { ConfirmationType.valueOf(name) }.getOrNull()
        }
        confirmationOrderNumber = values[KEY_CONFIRMATION_ORDER_NUMBER] as? String ?: ""
    }

    companion object {
        private const val KEY_SHOW_PROFILE = "showProfile"
        private const val KEY_SHOW_BRANCHES_MANAGEMENT = "showBranchesManagement"
        private const val KEY_SHOW_STATISTICS = "showStatistics"
        private const val KEY_SHOW_INVITATIONS = "showInvitations"
        private const val KEY_SHOW_DELIVERY_MANAGEMENT = "showDeliveryManagement"
        private const val KEY_SHOW_ADD_PRODUCT = "showAddProduct"
        private const val KEY_SHOW_ADD_SHOWCASE = "showAddShowcase"
        private const val KEY_SHOW_PRODUCT_DETAIL = "showProductDetail"
        private const val KEY_SHOW_PRODUCT_SEARCH = "showProductSearch"
        private const val KEY_SHOW_ADD_COMBO = "showAddCombo"
        private const val KEY_SHOW_COMBO_DETAIL = "showComboDetail"
        private const val KEY_SHOW_ORDER_DETAIL = "showOrderDetail"
        private const val KEY_SELECTED_ORDER_ID = "selectedOrderId"
        private const val KEY_SELECTED_HOME_TAB_INDEX = "selectedHomeTabIndex"
        private const val KEY_BRANCH_CREATE_BUSINESS_ID = "branchCreateBusinessId"
        private const val KEY_CONFIRMATION_TYPE = "confirmationType"
        private const val KEY_CONFIRMATION_ORDER_NUMBER = "confirmationOrderNumber"

        val Saver: Saver<AppNavigatorState, Any> = mapSaver(
            save = { state -> state.toSaveMap() },
            restore = { values ->
                AppNavigatorState().apply {
                    restoreFromMap(values)
                }
            }
        )
    }
}

@Suppress("FunctionName")
@Composable
fun rememberAppNavigatorState(): AppNavigatorState =
    rememberSaveable(saver = AppNavigatorState.Saver) { AppNavigatorState() }
