package com.llego.app

import com.llego.business.invitations.data.repository.InvitationRepository
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.delivery.data.repository.DeliveryLinkRepository
import com.llego.business.delivery.ui.viewmodel.DeliveryLinkViewModel
import com.llego.business.branches.ui.viewmodel.BranchesManagementViewModel
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.data.repositories.AuthRepository
import com.llego.shared.data.repositories.BusinessRepository
import com.llego.shared.data.repositories.PaymentMethodsRepository
import com.llego.shared.data.repositories.ProductRepository
import com.llego.shared.data.repositories.ComboRepository
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.branch.BranchSelectorViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

/**
 * Composition root centralizado para las dependencias de la app.
 *
 * Nota:
 * - En Android, TokenManager.initialize(context) debe ejecutarse antes de crear AppContainer.
 * - GraphQLClient se inicializa con el mismo TokenManager compartido por toda la app.
 */
class AppContainer(
    val tokenManager: TokenManager = TokenManager()
) {
    init {
        GraphQLClient.initialize(tokenManager)
    }

    // Repositorios principales
    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager = tokenManager)
    }
    val businessRepository: BusinessRepository by lazy {
        BusinessRepository(tokenManager = tokenManager)
    }
    val productRepository: ProductRepository by lazy {
        ProductRepository(tokenManager = tokenManager)
    }
    val comboRepository: ComboRepository by lazy {
        ComboRepository(tokenManager = tokenManager)
    }
    val paymentMethodsRepository: PaymentMethodsRepository by lazy {
        PaymentMethodsRepository(tokenManager = tokenManager)
    }
    val invitationRepository: InvitationRepository by lazy {
        InvitationRepository(tokenManager = tokenManager)
    }
    val deliveryLinkRepository: DeliveryLinkRepository by lazy {
        DeliveryLinkRepository(tokenManager = tokenManager)
    }

    // Factories de ViewModel
    fun createAuthViewModel(): AuthViewModel = AuthViewModel()

    fun createOrdersViewModel(): OrdersViewModel = OrdersViewModel(tokenManager)

    fun createProductViewModel(): ProductViewModel = ProductViewModel(tokenManager)

    fun createComboViewModel(): ComboViewModel = ComboViewModel(tokenManager)

    fun createSettingsViewModel(): SettingsViewModel = SettingsViewModel(tokenManager)

    fun createRegisterBusinessViewModel(): RegisterBusinessViewModel =
        RegisterBusinessViewModel(tokenManager)

    fun createInvitationViewModel(): InvitationViewModel =
        InvitationViewModel(invitationRepository)

    fun createDeliveryLinkViewModel(): DeliveryLinkViewModel =
        DeliveryLinkViewModel(deliveryLinkRepository)

    fun createBranchSelectorViewModel(): BranchSelectorViewModel =
        BranchSelectorViewModel(businessRepository)

    fun createBranchesManagementViewModel(): BranchesManagementViewModel =
        BranchesManagementViewModel(businessRepository)

    fun createAppViewModels(authViewModel: AuthViewModel = createAuthViewModel()): AppViewModels =
        AppViewModels(
            tokenManager = tokenManager,
            auth = authViewModel,
            orders = createOrdersViewModel(),
            products = createProductViewModel(),
            combos = createComboViewModel(),
            settings = createSettingsViewModel(),
            registerBusiness = createRegisterBusinessViewModel(),
            invitations = createInvitationViewModel(),
            deliveryLinks = createDeliveryLinkViewModel(),
            branchSelector = createBranchSelectorViewModel(),
            branchesManagement = createBranchesManagementViewModel()
        )
}
