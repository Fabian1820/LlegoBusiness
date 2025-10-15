package com.llego.nichos.restaurant.data.repository

import com.llego.nichos.restaurant.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * Repositorio para datos del Restaurante
 * Por ahora usa datos mock, preparado para integración con backend
 */
class RestaurantRepository {

    // StateFlows para datos reactivos
    private val _orders = MutableStateFlow(getMockOrders())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    private val _menuItems = MutableStateFlow(getMockMenuItems())
    val menuItems: Flow<List<MenuItem>> = _menuItems.asStateFlow()

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

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        delay(500)
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }

        if (index != -1) {
            currentOrders[index] = currentOrders[index].copy(
                status = newStatus,
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

    // ==================== MENU ITEMS ====================

    suspend fun getMenuItems(): List<MenuItem> {
        delay(500)
        return _menuItems.value
    }

    suspend fun getMenuItemsByCategory(category: MenuCategory): List<MenuItem> {
        delay(300)
        return _menuItems.value.filter { it.category == category }
    }

    suspend fun getMenuItemById(itemId: String): MenuItem? {
        delay(200)
        return _menuItems.value.find { it.id == itemId }
    }

    suspend fun addMenuItem(item: MenuItem): Boolean {
        delay(500)
        val currentItems = _menuItems.value.toMutableList()
        currentItems.add(item)
        _menuItems.value = currentItems
        return true
    }

    suspend fun updateMenuItem(item: MenuItem): Boolean {
        delay(500)
        val currentItems = _menuItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == item.id }

        if (index != -1) {
            currentItems[index] = item
            _menuItems.value = currentItems
            return true
        }
        return false
    }

    suspend fun deleteMenuItem(itemId: String): Boolean {
        delay(500)
        val currentItems = _menuItems.value.toMutableList()
        val removed = currentItems.removeAll { it.id == itemId }
        if (removed) {
            _menuItems.value = currentItems
        }
        return removed
    }

    suspend fun toggleItemAvailability(itemId: String): Boolean {
        delay(300)
        val currentItems = _menuItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }

        if (index != -1) {
            currentItems[index] = currentItems[index].copy(
                isAvailable = !currentItems[index].isAvailable
            )
            _menuItems.value = currentItems
            return true
        }
        return false
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
                        menuItem = getMockMenuItems()[0], // Ropa Vieja
                        quantity = 2,
                        specialInstructions = "Poco picante",
                        subtotal = 25.0
                    ),
                    OrderItem(
                        menuItem = getMockMenuItems()[6], // Mojito
                        quantity = 2,
                        specialInstructions = null,
                        subtotal = 10.0
                    )
                ),
                status = OrderStatus.PENDING,
                createdAt = "2024-10-06T12:30:00",
                updatedAt = "2024-10-06T12:30:00",
                total = 35.0,
                paymentMethod = PaymentMethod.CASH,
                specialNotes = "Tocar el timbre dos veces",
                estimatedTime = 45
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
                        menuItem = getMockMenuItems()[1], // Moros y Cristianos
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockMenuItems()[2], // Lechón Asado
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
                        menuItem = getMockMenuItems()[4], // Flan de Caramelo
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
                        menuItem = getMockMenuItems()[0], // Ropa Vieja
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 12.50
                    ),
                    OrderItem(
                        menuItem = getMockMenuItems()[3], // Tostones
                        quantity = 2,
                        specialInstructions = "Extra crujientes",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = getMockMenuItems()[5], // Guarapo
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
            )
        )
    }

    private fun getMockMenuItems(): List<MenuItem> {
        return listOf(
            // Platos Principales
            MenuItem(
                id = "ITEM001",
                name = "Ropa Vieja",
                description = "Carne de res desmenuzada en salsa criolla con pimientos y cebolla",
                price = 12.50,
                category = MenuCategory.MAIN_COURSES,
                imageUrl = null,
                isAvailable = true,
                preparationTime = 25,
                allergens = listOf("Gluten"),
                isVegetarian = false,
                calories = 450
            ),
            MenuItem(
                id = "ITEM002",
                name = "Moros y Cristianos",
                description = "Arroz con frijoles negros al estilo cubano",
                price = 8.00,
                category = MenuCategory.MAIN_COURSES,
                isAvailable = true,
                preparationTime = 20,
                isVegetarian = true,
                isVegan = true,
                calories = 320
            ),
            MenuItem(
                id = "ITEM003",
                name = "Lechón Asado",
                description = "Cerdo asado marinado con mojo criollo",
                price = 15.00,
                category = MenuCategory.MAIN_COURSES,
                isAvailable = true,
                preparationTime = 30,
                calories = 520
            ),

            // Acompañamientos
            MenuItem(
                id = "ITEM004",
                name = "Tostones",
                description = "Plátanos verdes fritos y aplastados",
                price = 4.00,
                category = MenuCategory.SIDES,
                isAvailable = true,
                preparationTime = 10,
                isVegetarian = true,
                isVegan = true,
                calories = 180
            ),

            // Postres
            MenuItem(
                id = "ITEM005",
                name = "Flan de Caramelo",
                description = "Postre cremoso de huevo con caramelo",
                price = 4.00,
                category = MenuCategory.DESSERTS,
                isAvailable = true,
                preparationTime = 5,
                allergens = listOf("Huevo", "Lácteos"),
                isVegetarian = true,
                calories = 280
            ),

            // Bebidas
            MenuItem(
                id = "ITEM006",
                name = "Guarapo",
                description = "Jugo natural de caña de azúcar",
                price = 3.00,
                category = MenuCategory.BEVERAGES,
                isAvailable = true,
                preparationTime = 5,
                isVegetarian = true,
                isVegan = true,
                calories = 120
            ),
            MenuItem(
                id = "ITEM007",
                name = "Mojito Cubano",
                description = "Ron blanco, hierbabuena, limón, azúcar y soda",
                price = 5.00,
                category = MenuCategory.BEVERAGES,
                isAvailable = true,
                preparationTime = 5,
                isVegetarian = true,
                calories = 150
            ),

            // Entradas
            MenuItem(
                id = "ITEM008",
                name = "Ensalada Mixta",
                description = "Lechuga, tomate, pepino, zanahoria con vinagreta",
                price = 5.50,
                category = MenuCategory.APPETIZERS,
                isAvailable = true,
                preparationTime = 10,
                isVegetarian = true,
                isVegan = true,
                calories = 80
            ),
            MenuItem(
                id = "ITEM009",
                name = "Croquetas de Jamón",
                description = "Croquetas caseras de jamón (6 unidades)",
                price = 6.00,
                category = MenuCategory.APPETIZERS,
                isAvailable = true,
                preparationTime = 15,
                allergens = listOf("Gluten", "Lácteos"),
                calories = 320
            ),

            // Sopas
            MenuItem(
                id = "ITEM010",
                name = "Ajiaco Criollo",
                description = "Sopa tradicional cubana con viandas y carnes",
                price = 9.00,
                category = MenuCategory.MAIN_COURSES,
                isAvailable = true,
                preparationTime = 20,
                calories = 380
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

        fun getInstance(): RestaurantRepository {
            return instance ?: RestaurantRepository().also { instance = it }
        }
    }
}
