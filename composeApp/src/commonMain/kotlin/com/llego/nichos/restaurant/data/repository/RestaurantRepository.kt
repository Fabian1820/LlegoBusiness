package com.llego.nichos.restaurant.data.repository

import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.common.data.model.Product
import com.llego.nichos.common.data.model.toMenuItem
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.repositories.ProductRepository as GraphQLProductRepository
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.mappers.toLocalProducts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repositorio para datos del Restaurante
 * Ahora carga productos desde GraphQL backend
 */
class RestaurantRepository(
    tokenManager: TokenManager
) {

    private val graphQLRepository = GraphQLProductRepository(tokenManager)
    private val scope = CoroutineScope(Dispatchers.Default)

    // StateFlows para datos reactivos
    private val _orders = MutableStateFlow(getMockOrders())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: Flow<List<Product>> = _products.asStateFlow()

    private val _isLoadingProducts = MutableStateFlow(false)
    val isLoadingProducts: Flow<Boolean> = _isLoadingProducts.asStateFlow()

    init {
        // Cargar productos desde GraphQL al inicializar
        loadProductsFromBackend()
    }
    
    // Compatibilidad: mantener menuItems para código legacy
    @Deprecated("Usar products en su lugar", ReplaceWith("products"))
    val menuItems: Flow<List<MenuItem>> = _products.map { products ->
        products.map { it.toMenuItem() }
    }

    private val _settings = MutableStateFlow(getMockSettings())
    val settings: Flow<RestaurantSettings> = _settings.asStateFlow()

    // ==================== ORDERS ====================

    suspend fun getOrders(): List<Order> {
        delay(500) // Simular red
        return _orders.value
    }

    suspend fun getOrderById(orderId: String): Order? {
        delay(300)
        return _orders.value.find { it.id == orderId }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        estimatedTime: Int? = null
    ): Boolean {
        delay(500)
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }

        if (index != -1) {
            val currentOrder = currentOrders[index]
            currentOrders[index] = currentOrders[index].copy(
                status = newStatus,
                estimatedTime = estimatedTime ?: currentOrder.estimatedTime,
                updatedAt = getCurrentTimestamp()
            )
            _orders.value = currentOrders
            return true
        }
        return false
    }

    suspend fun updateOrderItems(orderId: String, items: List<OrderItem>, total: Double): Boolean {
        delay(500)
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }

        if (index != -1) {
            currentOrders[index] = currentOrders[index].copy(
                items = items,
                total = total,
                updatedAt = getCurrentTimestamp()
            )
            _orders.value = currentOrders
            return true
        }
        return false
    }

    suspend fun acceptOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, OrderStatus.PREPARING)
    }

    suspend fun startPreparingOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, OrderStatus.PREPARING)
    }

    suspend fun markOrderReady(orderId: String): Boolean {
        return updateOrderStatus(orderId, OrderStatus.READY)
    }

    suspend fun cancelOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    // ==================== PRODUCTS (unificado) ====================

    suspend fun getProducts(): List<Product> {
        delay(500)
        return _products.value
    }

    suspend fun getProductById(productId: String): Product? {
        delay(200)
        return _products.value.find { it.id == productId }
    }

    suspend fun addProduct(product: Product): Boolean {
        delay(500)
        val currentProducts = _products.value.toMutableList()
        currentProducts.add(product)
        _products.value = currentProducts
        return true
    }

    suspend fun updateProduct(product: Product): Boolean {
        delay(500)
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == product.id }

        if (index != -1) {
            currentProducts[index] = product
            _products.value = currentProducts
            return true
        }
        return false
    }

    suspend fun deleteProduct(productId: String): Boolean {
        delay(500)
        val currentProducts = _products.value.toMutableList()
        val removed = currentProducts.removeAll { it.id == productId }
        if (removed) {
            _products.value = currentProducts
        }
        return removed
    }

    suspend fun toggleProductAvailability(productId: String): Boolean {
        delay(300)
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == productId }

        if (index != -1) {
            currentProducts[index] = currentProducts[index].copy(
                isAvailable = !currentProducts[index].isAvailable
            )
            _products.value = currentProducts
            return true
        }
        return false
    }
    
    // ==================== COMPATIBILIDAD CON MENUITEM (legacy) ====================
    
    suspend fun getMenuItems(): List<MenuItem> {
        return _products.value.map { it.toMenuItem() }
    }

    suspend fun getMenuItemsByCategory(category: MenuCategory): List<MenuItem> {
        return _products.value
            .map { it.toMenuItem() }
            .filter { it.category == category }
    }

    suspend fun getMenuItemById(itemId: String): MenuItem? {
        return _products.value.find { it.id == itemId }?.toMenuItem()
    }

    suspend fun addMenuItem(item: MenuItem): Boolean {
        return addProduct(item.toProduct())
    }

    suspend fun updateMenuItem(item: MenuItem): Boolean {
        return updateProduct(item.toProduct())
    }

    suspend fun deleteMenuItem(itemId: String): Boolean {
        return deleteProduct(itemId)
    }

    suspend fun toggleItemAvailability(itemId: String): Boolean {
        return toggleProductAvailability(itemId)
    }

    // ==================== SETTINGS ====================

    suspend fun getSettings(): RestaurantSettings {
        delay(300)
        return _settings.value
    }

    suspend fun updateSettings(settings: RestaurantSettings): Boolean {
        delay(500)
        _settings.value = settings
        return true
    }

    // ==================== GRAPHQL INTEGRATION ====================

    /**
     * Carga productos desde el backend GraphQL
     */
    fun loadProductsFromBackend(branchId: String? = null) {
        scope.launch {
            _isLoadingProducts.value = true
            try {
                when (val result = graphQLRepository.getProducts(branchId = branchId)) {
                    is ProductsResult.Success -> {
                        // Convertir productos de GraphQL a productos locales
                        val localProducts = result.products.toLocalProducts()
                        _products.value = localProducts
                    }
                    is ProductsResult.Error -> {
                        println("Error cargando productos desde GraphQL: ${result.message}")
                        // Si falla, usar mock data como fallback
                        _products.value = getMockProducts()
                    }
                    is ProductsResult.Loading -> {
                        // Estado de carga
                    }
                }
            } catch (e: Exception) {
                println("Excepción cargando productos: ${e.message}")
                // Fallback a mock data en caso de error
                _products.value = getMockProducts()
            } finally {
                _isLoadingProducts.value = false
            }
        }
    }

    /**
     * Recarga los productos desde el backend
     */
    fun refreshProducts() {
        loadProductsFromBackend()
    }

    // ==================== MOCK DATA ====================

    private fun getMockOrders(): List<Order> {
        return listOf(
            Order(
                id = "ORD001",
                orderNumber = "#1234",
                customer = Customer(
                    name = "Juan Pérez",
                    phone = "+53 5555-1111",
                    address = "Calle 23 #456, Vedado",
                    coordinates = Coordinates(23.1136, -82.3666)
                ),
                items = listOf(
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[0], // Ropa Vieja
                        quantity = 2,
                        specialInstructions = "Poco picante por favor, sin cebolla y con extra de pimientos",
                        subtotal = 25.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[1], // Moros y Cristianos
                        quantity = 3,
                        specialInstructions = "Bien cocidos, con bastante ajo",
                        subtotal = 24.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[2], // Lechón Asado
                        quantity = 1,
                        specialInstructions = "Bien dorado y crujiente, con mojo extra",
                        subtotal = 15.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[3], // Tostones
                        quantity = 4,
                        specialInstructions = "Extra crujientes con bastante sal",
                        subtotal = 16.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[4], // Flan de Caramelo
                        quantity = 2,
                        specialInstructions = "Con extra de caramelo líquido",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[5], // Guarapo
                        quantity = 3,
                        specialInstructions = "Bien frío con hielo",
                        subtotal = 9.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[6], // Mojito
                        quantity = 2,
                        specialInstructions = "Con mucha hierbabuena y poco azúcar",
                        subtotal = 10.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[0], // Ropa Vieja (otro)
                        quantity = 1,
                        specialInstructions = "Para llevar en envase separado",
                        subtotal = 12.50
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[3], // Tostones (otro)
                        quantity = 2,
                        specialInstructions = "Sin sal, para niños",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[1], // Moros y Cristianos (otro)
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 8.0
                    )
                ),
                status = OrderStatus.PENDING,
                createdAt = "2024-10-06T12:30:00",
                updatedAt = "2024-10-06T12:30:00",
                total = 135.50,
                paymentMethod = PaymentMethod.CASH,
                specialNotes = "Tocar el timbre dos veces. Por favor incluir cubiertos desechables y servilletas extra. El pedido es para una reunión familiar, así que necesitamos que todo llegue bien caliente. Si falta algún ingrediente, llamar antes de preparar. Gracias!",
                estimatedTime = null
            ),
            Order(
                id = "ORD002",
                orderNumber = "#1235",
                customer = Customer(
                    name = "María González",
                    phone = "+53 5555-2222",
                    address = "Avenida 5ta #789, Miramar"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[1], // Moros y Cristianos
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[2], // Lechón Asado
                        quantity = 1,
                        specialInstructions = "Bien dorado",
                        subtotal = 15.0
                    )
                ),
                status = OrderStatus.PREPARING,
                createdAt = "2024-10-06T11:45:00",
                updatedAt = "2024-10-06T12:00:00",
                total = 23.0,
                paymentMethod = PaymentMethod.CARD,
                estimatedTime = 30
            ),
            Order(
                id = "ORD003",
                orderNumber = "#1236",
                customer = Customer(
                    name = "Carlos Rodríguez",
                    phone = "+53 5555-3333",
                    address = "Calle 17 #890, Centro Habana"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[4], // Flan de Caramelo
                        quantity = 3,
                        specialInstructions = null,
                        subtotal = 12.0
                    )
                ),
                status = OrderStatus.READY,
                createdAt = "2024-10-06T11:00:00",
                updatedAt = "2024-10-06T11:30:00",
                total = 12.0,
                paymentMethod = PaymentMethod.TRANSFER,
                estimatedTime = 15
            ),
            Order(
                id = "ORD004",
                orderNumber = "#1237",
                customer = Customer(
                    name = "Ana Martínez",
                    phone = "+53 5555-4444",
                    address = "Calle 10 #234, Playa"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[0], // Ropa Vieja
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 12.50
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[3], // Tostones
                        quantity = 2,
                        specialInstructions = "Extra crujientes",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[5], // Guarapo
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 3.0
                    )
                ),
                status = OrderStatus.PREPARING,
                createdAt = "2024-10-06T12:15:00",
                updatedAt = "2024-10-06T12:20:00",
                total = 23.50,
                paymentMethod = PaymentMethod.DIGITAL_WALLET,
                estimatedTime = 40
            ),
            Order(
                id = "ORD005",
                orderNumber = "#1238",
                customer = Customer(
                    name = "Lucia Ramirez",
                    phone = "+53 5555-5555",
                    address = "Calle 8 #120, Playa"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[7], // Ensalada Mixta
                        quantity = 1,
                        specialInstructions = "Sin tomate",
                        subtotal = 5.50
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[0], // Ropa Vieja
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 12.50
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[5], // Guarapo
                        quantity = 2,
                        specialInstructions = "Sin azucar",
                        subtotal = 6.0
                    ),
                    OrderItem(
                        menuItem = getMockProducts().map { it.toMenuItem() }[8], // Croquetas de Jamon
                        quantity = 1,
                        specialInstructions = "Bien doradas",
                        subtotal = 6.0
                    )
                ),
                status = OrderStatus.PENDING,
                createdAt = "2024-10-06T12:40:00",
                updatedAt = "2024-10-06T12:40:00",
                total = 30.0,
                paymentMethod = PaymentMethod.CARD,
                estimatedTime = null
            )
        )
    }

    private fun getMockProducts(): List<Product> {
        return listOf(
            // Platos Principales
            Product(
                id = "ITEM001",
                name = "Ropa Vieja",
                description = "Carne de res desmenuzada en salsa criolla con pimientos y cebolla",
                price = 12.50,
                imageUrl = "",
                category = "Platos Fuertes", // Mapea a MenuCategory.MAIN_COURSES
                isAvailable = true,
                preparationTime = 25,
                varieties = listOf("Con arroz blanco", "Con tostones", "Con yuca frita")
            ),
            Product(
                id = "ITEM002",
                name = "Moros y Cristianos",
                description = "Arroz con frijoles negros al estilo cubano",
                price = 8.00,
                imageUrl = "",
                category = "Platos Fuertes",
                isAvailable = true,
                preparationTime = 20,
                isVegetarian = true,
                isVegan = true,
                varieties = listOf("Porción regular", "Porción grande")
            ),
            Product(
                id = "ITEM003",
                name = "Lechón Asado",
                description = "Cerdo asado marinado con mojo criollo",
                price = 15.00,
                imageUrl = "",
                category = "Platos Fuertes",
                isAvailable = true,
                preparationTime = 30,
                varieties = listOf("Con mojo extra", "Con yuca", "Con ensalada", "Con papas")
            ),

            // Acompañamientos
            Product(
                id = "ITEM004",
                name = "Tostones",
                description = "Plátanos verdes fritos y aplastados",
                price = 4.00,
                imageUrl = "",
                category = "Agregos",
                isAvailable = true,
                preparationTime = 10,
                varieties = listOf("Con mojo", "Con ajo", "Sal solamente")
            ),

            // Postres
            Product(
                id = "ITEM005",
                name = "Flan de Caramelo",
                description = "Postre cremoso de huevo con caramelo",
                price = 4.00,
                imageUrl = "",
                category = "Postres",
                isAvailable = true,
                preparationTime = 5
            ),

            // Bebidas
            Product(
                id = "ITEM006",
                name = "Guarapo",
                description = "Jugo natural de caña de azúcar",
                price = 3.00,
                imageUrl = "",
                category = "Bebidas",
                isAvailable = true,
                preparationTime = 5
            ),
            Product(
                id = "ITEM007",
                name = "Mojito Cubano",
                description = "Ron blanco, hierbabuena, limón, azúcar y soda",
                price = 5.00,
                imageUrl = "",
                category = "Bebidas",
                isAvailable = true,
                preparationTime = 5
            ),

            // Entradas
            Product(
                id = "ITEM008",
                name = "Ensalada Mixta",
                description = "Lechuga, tomate, pepino, zanahoria con vinagreta",
                price = 5.50,
                imageUrl = "",
                category = "Entradas",
                isAvailable = true,
                preparationTime = 10
            ),
            Product(
                id = "ITEM009",
                name = "Croquetas de Jamón",
                description = "Croquetas caseras de jamón (6 unidades)",
                price = 6.00,
                imageUrl = "",
                category = "Entradas",
                isAvailable = true,
                preparationTime = 15
            ),

            // Sopas
            Product(
                id = "ITEM010",
                name = "Ajiaco Criollo",
                description = "Sopa tradicional cubana con viandas y carnes",
                price = 9.00,
                imageUrl = "",
                category = "Platos Fuertes",
                isAvailable = true,
                preparationTime = 20
            )
        )
    }

    private fun getMockSettings(): RestaurantSettings {
        return RestaurantSettings(
            businessHours = BusinessHours(
                monday = DaySchedule(true, "11:00", "22:00"),
                tuesday = DaySchedule(true, "11:00", "22:00"),
                wednesday = DaySchedule(true, "11:00", "22:00"),
                thursday = DaySchedule(true, "11:00", "23:00"),
                friday = DaySchedule(true, "11:00", "23:30"),
                saturday = DaySchedule(true, "12:00", "23:30"),
                sunday = DaySchedule(true, "12:00", "21:00")
            ),
            acceptedPaymentMethods = listOf(
                PaymentMethod.CASH,
                PaymentMethod.CARD,
                PaymentMethod.TRANSFER,
                PaymentMethod.DIGITAL_WALLET
            ),
            deliverySettings = DeliverySettings(
                isDeliveryEnabled = true,
                isPickupEnabled = true,
                deliveryRadius = 5.0,
                minimumOrderAmount = 10.0,
                deliveryFee = 2.0,
                freeDeliveryThreshold = 30.0,
                estimatedDeliveryTime = 45
            ),
            orderSettings = OrderSettings(
                autoAcceptOrders = false,
                maxOrdersPerHour = 20,
                prepTimeBuffer = 5,
                allowScheduledOrders = true,
                cancelationPolicy = "Los pedidos pueden cancelarse hasta 10 minutos después de realizados"
            ),
            notifications = NotificationSettings(
                newOrderSound = true,
                orderStatusUpdates = true,
                customerMessages = true,
                dailySummary = true
            )
        )
    }

    private fun getCurrentTimestamp(): String {
        // TODO: Implementar timestamp real
        return "2024-10-06T12:30:00"
    }

    companion object {
        private var instance: RestaurantRepository? = null

        fun getInstance(tokenManager: TokenManager): RestaurantRepository {
            return instance ?: RestaurantRepository(tokenManager).also { instance = it }
        }
    }
}
