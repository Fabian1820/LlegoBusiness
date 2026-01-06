package com.llego.nichos.market.data.repository

import com.llego.nichos.common.data.model.*
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.repositories.ProductRepository as GraphQLProductRepository
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.mappers.toLocalProducts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repositorio para datos del Mercado
 * Ahora carga productos desde GraphQL backend
 */
class MarketRepository(
    tokenManager: TokenManager
) {

    private val graphQLRepository = GraphQLProductRepository(tokenManager)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: Flow<List<Product>> = _products.asStateFlow()

    private val _isLoadingProducts = MutableStateFlow(false)
    val isLoadingProducts: Flow<Boolean> = _isLoadingProducts.asStateFlow()

    init {
        loadProductsFromBackend()
    }

    suspend fun getProducts(): List<Product> {
        delay(500) // Simular red
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

    // Convierte Product a MenuItem para compatibilidad con MenuScreen
    fun getProductsAsMenuItems(): List<MenuItem> {
        return _products.value.map { product ->
            MenuItem(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                category = MenuCategory.MAIN_COURSES, // Default para compatibilidad
                imageUrl = product.imageUrl,
                isAvailable = product.isAvailable,
                preparationTime = 0
            )
        }
    }

    // ==================== GRAPHQL INTEGRATION ====================

    fun loadProductsFromBackend(branchId: String? = null) {
        scope.launch {
            _isLoadingProducts.value = true
            try {
                when (val result = graphQLRepository.getProducts(branchId = branchId)) {
                    is ProductsResult.Success -> {
                        _products.value = result.products.toLocalProducts()
                    }
                    is ProductsResult.Error -> {
                        println("Error cargando productos Market: ${result.message}")
                        _products.value = getMockProducts()
                    }
                    is ProductsResult.Loading -> {}
                }
            } catch (e: Exception) {
                println("Excepción Market: ${e.message}")
                _products.value = getMockProducts()
            } finally {
                _isLoadingProducts.value = false
            }
        }
    }

    fun refreshProducts() {
        loadProductsFromBackend()
    }

    // ==================== MOCK DATA ====================

    private fun getMockProducts(): List<Product> {
        return listOf(
            // Frutas y Verduras
            Product(
                id = "MKT001",
                name = "Tomates Rojos",
                description = "Tomates frescos y jugosos, perfectos para ensaladas",
                price = 2.50,
                imageUrl = "",
                category = "Frutas y Verduras", // Se mapea a "frutas_verduras"
                isAvailable = true,
                brand = "Frescos del Campo",
                unit = ProductUnit.KG,
                stock = 45
            ),
            Product(
                id = "MKT002",
                name = "Plátanos",
                description = "Plátanos maduros, ideales para consumo diario",
                price = 1.80,
                imageUrl = "",
                category = "Frutas y Verduras",
                isAvailable = true,
                brand = "Tropical",
                unit = ProductUnit.KG,
                stock = 32
            ),
            Product(
                id = "MKT003",
                name = "Lechuga",
                description = "Lechuga fresca y crujiente",
                price = 1.50,
                imageUrl = "",
                category = "Frutas y Verduras",
                isAvailable = true,
                brand = "Verde Natural",
                unit = ProductUnit.UNIT,
                stock = 28
            ),

            // Carnes y Pescados
            Product(
                id = "MKT004",
                name = "Pollo Entero",
                description = "Pollo fresco, listo para cocinar",
                price = 8.50,
                imageUrl = "",
                category = "Carnes y Pescados",
                isAvailable = true,
                brand = "Carnes Premium",
                unit = ProductUnit.KG,
                stock = 15
            ),
            Product(
                id = "MKT005",
                name = "Carne Molida",
                description = "Carne de res molida, 80/20",
                price = 12.00,
                imageUrl = "",
                category = "Carnes y Pescados",
                isAvailable = true,
                brand = "Carnes Premium",
                unit = ProductUnit.KG,
                stock = 8
            ),

            // Lácteos
            Product(
                id = "MKT006",
                name = "Leche Entera",
                description = "Leche fresca pasteurizada, 1 litro",
                price = 3.50,
                imageUrl = "",
                category = "Lácteos",
                isAvailable = true,
                brand = "Lácteos del Valle",
                unit = ProductUnit.LITER,
                stock = 24
            ),
            Product(
                id = "MKT007",
                name = "Queso Gouda",
                description = "Queso gouda en lonchas, 200g",
                price = 5.00,
                imageUrl = "",
                category = "Lácteos",
                isAvailable = true,
                brand = "Quesos Selectos",
                unit = ProductUnit.PACK,
                stock = 18
            ),

            // Bebidas
            Product(
                id = "MKT008",
                name = "Agua Mineral",
                description = "Agua mineral natural, 1.5L",
                price = 1.20,
                imageUrl = "",
                category = "Bebidas",
                isAvailable = true,
                brand = "Agua Pura",
                unit = ProductUnit.LITER,
                stock = 50
            ),
            Product(
                id = "MKT009",
                name = "Refresco Cola",
                description = "Refresco de cola, 2L",
                price = 2.50,
                imageUrl = "",
                category = "Bebidas",
                isAvailable = true,
                brand = "Cola Fizz",
                unit = ProductUnit.LITER,
                stock = 35
            ),

            // Limpieza
            Product(
                id = "MKT010",
                name = "Detergente Líquido",
                description = "Detergente para ropa, 2L",
                price = 6.50,
                imageUrl = "",
                category = "Limpieza",
                isAvailable = true,
                brand = "Limpia Total",
                unit = ProductUnit.LITER,
                stock = 22
            ),
            Product(
                id = "MKT011",
                name = "Papel Higiénico",
                description = "Papel higiénico suave, 12 rollos",
                price = 4.50,
                imageUrl = "",
                category = "Limpieza",
                isAvailable = true,
                brand = "Suave Plus",
                unit = ProductUnit.PACK,
                stock = 30
            ),

            // Despensa
            Product(
                id = "MKT012",
                name = "Arroz Blanco",
                description = "Arroz de grano largo, 1kg",
                price = 2.00,
                imageUrl = "",
                category = "Despensa",
                isAvailable = true,
                brand = "Arroz Selecto",
                unit = ProductUnit.KG,
                stock = 40
            ),
            Product(
                id = "MKT013",
                name = "Frijoles Negros",
                description = "Frijoles negros secos, 500g",
                price = 1.80,
                imageUrl = "",
                category = "Despensa",
                isAvailable = true,
                brand = "Granos Premium",
                unit = ProductUnit.KG,
                stock = 25
            )
        )
    }

    companion object {
        private var instance: MarketRepository? = null

        fun getInstance(tokenManager: TokenManager): MarketRepository {
            return instance ?: MarketRepository(tokenManager).also { instance = it }
        }
    }
}

