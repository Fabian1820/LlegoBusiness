package com.llego.nichos.agromarket.data.repository

import com.llego.nichos.common.data.model.*
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.model.MenuCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * Repositorio para datos del Agromercado
 * Por ahora usa datos mock, preparado para integración con backend
 */
class AgromarketRepository {

    private val _products = MutableStateFlow(getMockProducts())
    val products: Flow<List<Product>> = _products.asStateFlow()

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

    private fun getMockProducts(): List<Product> {
        return listOf(
            // Verduras
            Product(
                id = "AGRO001",
                name = "Zanahorias Orgánicas",
                description = "Zanahorias frescas cultivadas sin pesticidas",
                price = 2.80,
                imageUrl = "",
                category = "Verduras",
                isAvailable = true,
                brand = "Campo Orgánico",
                unit = ProductUnit.KG,
                stock = 35
            ),
            Product(
                id = "AGRO002",
                name = "Cebollas",
                description = "Cebollas amarillas, grandes y dulces",
                price = 1.90,
                imageUrl = "",
                category = "Verduras",
                isAvailable = true,
                brand = "Huerto Natural",
                unit = ProductUnit.KG,
                stock = 42
            ),
            Product(
                id = "AGRO003",
                name = "Ajos",
                description = "Ajos frescos, cabezas completas",
                price = 3.50,
                imageUrl = "",
                category = "Verduras",
                isAvailable = true,
                brand = "Huerto Natural",
                unit = ProductUnit.KG,
                stock = 20
            ),

            // Hortalizas
            Product(
                id = "AGRO004",
                name = "Espinacas",
                description = "Espinacas frescas, ricas en hierro",
                price = 2.50,
                imageUrl = "",
                category = "Hortalizas",
                isAvailable = true,
                brand = "Verde Fresco",
                unit = ProductUnit.KG,
                stock = 18
            ),
            Product(
                id = "AGRO005",
                name = "Acelgas",
                description = "Acelgas tiernas y frescas",
                price = 2.20,
                imageUrl = "",
                category = "Hortalizas",
                isAvailable = true,
                brand = "Verde Fresco",
                unit = ProductUnit.KG,
                stock = 15
            ),

            // Tubérculos
            Product(
                id = "AGRO006",
                name = "Papas",
                description = "Papas blancas, ideales para freír",
                price = 1.50,
                imageUrl = "",
                category = "Tubérculos",
                isAvailable = true,
                brand = "Raíces del Campo",
                unit = ProductUnit.KG,
                stock = 60
            ),
            Product(
                id = "AGRO007",
                name = "Boniato",
                description = "Boniatos dulces y nutritivos",
                price = 2.00,
                imageUrl = "",
                category = "Tubérculos",
                isAvailable = true,
                brand = "Raíces del Campo",
                unit = ProductUnit.KG,
                stock = 28
            ),
            Product(
                id = "AGRO008",
                name = "Yuca",
                description = "Yuca fresca, lista para cocinar",
                price = 1.80,
                imageUrl = "",
                category = "Tubérculos",
                isAvailable = true,
                brand = "Raíces del Campo",
                unit = ProductUnit.KG,
                stock = 32
            ),

            // Granos y Cereales
            Product(
                id = "AGRO009",
                name = "Maíz",
                description = "Maíz fresco en mazorca",
                price = 1.20,
                imageUrl = "",
                category = "Granos y Cereales",
                isAvailable = true,
                brand = "Cereales Premium",
                unit = ProductUnit.UNIT,
                stock = 45
            ),
            Product(
                id = "AGRO010",
                name = "Frijoles Rojos",
                description = "Frijoles rojos secos, 1kg",
                price = 2.50,
                imageUrl = "",
                category = "Granos y Cereales",
                isAvailable = true,
                brand = "Granos Selectos",
                unit = ProductUnit.KG,
                stock = 30
            ),
            Product(
                id = "AGRO011",
                name = "Lentejas",
                description = "Lentejas secas, 500g",
                price = 2.00,
                imageUrl = "",
                category = "Granos y Cereales",
                isAvailable = true,
                brand = "Granos Selectos",
                unit = ProductUnit.KG,
                stock = 25
            ),

            // Orgánicos
            Product(
                id = "AGRO012",
                name = "Tomates Orgánicos",
                description = "Tomates cultivados sin químicos",
                price = 3.50,
                imageUrl = "",
                category = "Orgánicos",
                isAvailable = true,
                brand = "Campo Orgánico",
                unit = ProductUnit.KG,
                stock = 22
            ),
            Product(
                id = "AGRO013",
                name = "Pepinos Orgánicos",
                description = "Pepinos frescos y crujientes",
                price = 2.80,
                imageUrl = "",
                category = "Orgánicos",
                isAvailable = true,
                brand = "Campo Orgánico",
                unit = ProductUnit.KG,
                stock = 20
            )
        )
    }

    companion object {
        private var instance: AgromarketRepository? = null

        fun getInstance(): AgromarketRepository {
            return instance ?: AgromarketRepository().also { instance = it }
        }
    }
}



