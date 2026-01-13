package com.llego.business.orders.data.repository

import com.llego.business.orders.data.model.*
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para pedidos del negocio (MVP con datos mock).
 */
class OrdersRepository(
    tokenManager: TokenManager
) {

    private val _orders = MutableStateFlow(getMockOrders())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    // ==================== ORDERS ====================

    suspend fun getOrders(): List<Order> {
        delay(500)
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
            currentOrders[index] = currentOrder.copy(
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

    // ==================== MOCK DATA ====================

    private fun getMockOrders(): List<Order> {
        val menuItems = getMockMenuItems()
        return listOf(
            Order(
                id = "ORD001",
                orderNumber = "#1234",
                customer = Customer(
                    name = "Juan Perez",
                    phone = "+53 5555-1111",
                    address = "Calle 23 #456, Vedado",
                    coordinates = Coordinates(23.1136, -82.3666)
                ),
                items = listOf(
                    OrderItem(
                        menuItem = menuItems[0],
                        quantity = 2,
                        specialInstructions = "Poco picante por favor, sin cebolla y con extra de pimientos",
                        subtotal = 25.0
                    ),
                    OrderItem(
                        menuItem = menuItems[1],
                        quantity = 3,
                        specialInstructions = "Bien cocidos, con bastante ajo",
                        subtotal = 24.0
                    ),
                    OrderItem(
                        menuItem = menuItems[2],
                        quantity = 1,
                        specialInstructions = "Bien dorado y crujiente, con mojo extra",
                        subtotal = 15.0
                    ),
                    OrderItem(
                        menuItem = menuItems[3],
                        quantity = 4,
                        specialInstructions = "Extra crujientes con bastante sal",
                        subtotal = 16.0
                    ),
                    OrderItem(
                        menuItem = menuItems[4],
                        quantity = 2,
                        specialInstructions = "Con extra de caramelo liquido",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = menuItems[5],
                        quantity = 3,
                        specialInstructions = "Bien frio con hielo",
                        subtotal = 9.0
                    ),
                    OrderItem(
                        menuItem = menuItems[6],
                        quantity = 2,
                        specialInstructions = "Con mucha hierbabuena y poco azucar",
                        subtotal = 10.0
                    ),
                    OrderItem(
                        menuItem = menuItems[0],
                        quantity = 1,
                        specialInstructions = "Para llevar en envase separado",
                        subtotal = 12.50
                    ),
                    OrderItem(
                        menuItem = menuItems[3],
                        quantity = 2,
                        specialInstructions = "Sin sal, para ninos",
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = menuItems[1],
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
                specialNotes = "Tocar el timbre dos veces. Por favor incluir cubiertos desechables y servilletas extra.",
                estimatedTime = null
            ),
            Order(
                id = "ORD002",
                orderNumber = "#1235",
                customer = Customer(
                    name = "Maria Gonzalez",
                    phone = "+53 5555-2222",
                    address = "Avenida 5ta #789, Miramar"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = menuItems[1],
                        quantity = 1,
                        specialInstructions = null,
                        subtotal = 8.0
                    ),
                    OrderItem(
                        menuItem = menuItems[2],
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
                    name = "Carlos Rodriguez",
                    phone = "+53 5555-3333",
                    address = "Calle 17 #890, Centro Habana"
                ),
                items = listOf(
                    OrderItem(
                        menuItem = menuItems[4],
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
            )
        )
    }

    private fun getMockMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                id = "ITEM001",
                name = "Ropa Vieja",
                description = "Carne de res desmenuzada en salsa criolla con pimientos y cebolla",
                price = 12.50,
                category = MenuCategory.MAIN_COURSES,
                preparationTime = 25
            ),
            MenuItem(
                id = "ITEM002",
                name = "Moros y Cristianos",
                description = "Arroz con frijoles negros al estilo cubano",
                price = 8.00,
                category = MenuCategory.MAIN_COURSES,
                preparationTime = 20,
                isVegetarian = true,
                isVegan = true
            ),
            MenuItem(
                id = "ITEM003",
                name = "Lechon Asado",
                description = "Cerdo asado marinado con mojo criollo",
                price = 15.00,
                category = MenuCategory.MAIN_COURSES,
                preparationTime = 30
            ),
            MenuItem(
                id = "ITEM004",
                name = "Tostones",
                description = "Platanos verdes fritos y aplastados",
                price = 4.00,
                category = MenuCategory.SIDES,
                preparationTime = 10
            ),
            MenuItem(
                id = "ITEM005",
                name = "Flan de Caramelo",
                description = "Postre cremoso de huevo con caramelo",
                price = 4.00,
                category = MenuCategory.DESSERTS,
                preparationTime = 5
            ),
            MenuItem(
                id = "ITEM006",
                name = "Guarapo",
                description = "Jugo natural de cana de azucar",
                price = 3.00,
                category = MenuCategory.BEVERAGES,
                preparationTime = 5
            ),
            MenuItem(
                id = "ITEM007",
                name = "Mojito Cubano",
                description = "Ron blanco, hierbabuena, limon, azucar y soda",
                price = 5.00,
                category = MenuCategory.BEVERAGES,
                preparationTime = 5
            )
        )
    }

    private fun getCurrentTimestamp(): String {
        return "2024-10-06T12:30:00"
    }

    companion object {
        private var instance: OrdersRepository? = null

        fun getInstance(tokenManager: TokenManager): OrdersRepository {
            return instance ?: OrdersRepository(tokenManager).also { instance = it }
        }
    }
}
