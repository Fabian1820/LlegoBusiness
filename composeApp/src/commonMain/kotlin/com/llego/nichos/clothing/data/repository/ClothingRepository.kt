package com.llego.nichos.clothing.data.repository

import com.llego.nichos.common.data.model.*
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * Repositorio para datos de Tienda de Ropa
 * Por ahora usa datos mock, preparado para integración con backend
 */
class ClothingRepository(
    @Suppress("UNUSED_PARAMETER") tokenManager: TokenManager
) {

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
            // Hombres
            Product(
                id = "CLO001",
                name = "Camisa Polo",
                description = "Camisa polo de algodón, cómoda y elegante",
                price = 25.00,
                imageUrl = "",
                category = "Hombres",
                isAvailable = true,
                sizes = listOf("S", "M", "L", "XL", "XXL"),
                colors = listOf(
                    ProductColor("Blanco", "#FFFFFF", 10),
                    ProductColor("Azul", "#2196F3", 8),
                    ProductColor("Negro", "#000000", 12)
                ),
                material = "Algodón 100%",
                gender = ClothingGender.MEN,
                stock = 30
            ),
            Product(
                id = "CLO002",
                name = "Pantalón Jeans",
                description = "Jeans clásicos, corte regular",
                price = 35.00,
                imageUrl = "",
                category = "Hombres",
                isAvailable = true,
                sizes = listOf("28", "30", "32", "34", "36", "38"),
                colors = listOf(
                    ProductColor("Azul Claro", "#64B5F6", 15),
                    ProductColor("Azul Oscuro", "#1976D2", 18)
                ),
                material = "Denim",
                gender = ClothingGender.MEN,
                stock = 33
            ),

            // Mujeres
            Product(
                id = "CLO003",
                name = "Vestido Casual",
                description = "Vestido cómodo para el día a día",
                price = 28.00,
                imageUrl = "",
                category = "Mujeres",
                isAvailable = true,
                sizes = listOf("XS", "S", "M", "L", "XL"),
                colors = listOf(
                    ProductColor("Rosa", "#F48FB1", 10),
                    ProductColor("Blanco", "#FFFFFF", 8),
                    ProductColor("Negro", "#000000", 12),
                    ProductColor("Azul", "#2196F3", 9)
                ),
                material = "Algodón y Poliéster",
                gender = ClothingGender.WOMEN,
                stock = 39
            ),
            Product(
                id = "CLO004",
                name = "Blusa Elegante",
                description = "Blusa de manga larga, perfecta para oficina",
                price = 22.00,
                imageUrl = "",
                category = "Mujeres",
                isAvailable = true,
                sizes = listOf("S", "M", "L", "XL"),
                colors = listOf(
                    ProductColor("Blanco", "#FFFFFF", 15),
                    ProductColor("Beige", "#D7CCC8", 12),
                    ProductColor("Negro", "#000000", 18)
                ),
                material = "Seda Sintética",
                gender = ClothingGender.WOMEN,
                stock = 45
            ),

            // Niños
            Product(
                id = "CLO005",
                name = "Camiseta Infantil",
                description = "Camiseta cómoda para niños, estampada",
                price = 12.00,
                imageUrl = "",
                category = "Niños",
                isAvailable = true,
                sizes = listOf("4", "6", "8", "10", "12"),
                colors = listOf(
                    ProductColor("Rojo", "#F44336", 20),
                    ProductColor("Azul", "#2196F3", 18),
                    ProductColor("Verde", "#4CAF50", 15),
                    ProductColor("Amarillo", "#FFEB3B", 12)
                ),
                material = "Algodón 100%",
                gender = ClothingGender.KIDS,
                stock = 65
            ),
            Product(
                id = "CLO006",
                name = "Pantalón Corto",
                description = "Pantalón corto deportivo para niños",
                price = 15.00,
                imageUrl = "",
                category = "Niños",
                isAvailable = true,
                sizes = listOf("4", "6", "8", "10", "12"),
                colors = listOf(
                    ProductColor("Negro", "#000000", 25),
                    ProductColor("Gris", "#757575", 20),
                    ProductColor("Azul", "#2196F3", 22)
                ),
                material = "Algodón y Poliéster",
                gender = ClothingGender.KIDS,
                stock = 67
            ),

            // Accesorios
            Product(
                id = "CLO007",
                name = "Gorra Deportiva",
                description = "Gorra ajustable, ideal para deportes",
                price = 8.00,
                imageUrl = "",
                category = "Accesorios",
                isAvailable = true,
                sizes = listOf("Única"),
                colors = listOf(
                    ProductColor("Negro", "#000000", 30),
                    ProductColor("Blanco", "#FFFFFF", 25),
                    ProductColor("Rojo", "#F44336", 20)
                ),
                material = "Algodón y Poliéster",
                gender = ClothingGender.UNISEX,
                stock = 75
            ),
            Product(
                id = "CLO008",
                name = "Bufanda de Lana",
                description = "Bufanda cálida, perfecta para invierno",
                price = 18.00,
                imageUrl = "",
                category = "Accesorios",
                isAvailable = true,
                sizes = listOf("Única"),
                colors = listOf(
                    ProductColor("Gris", "#757575", 15),
                    ProductColor("Azul", "#2196F3", 12),
                    ProductColor("Rojo", "#F44336", 10)
                ),
                material = "Lana 100%",
                gender = ClothingGender.UNISEX,
                stock = 37
            ),

            // Calzado
            Product(
                id = "CLO009",
                name = "Zapatillas Deportivas",
                description = "Zapatillas cómodas para caminar y correr",
                price = 45.00,
                imageUrl = "",
                category = "Calzado",
                isAvailable = true,
                sizes = listOf("38", "39", "40", "41", "42", "43", "44"),
                colors = listOf(
                    ProductColor("Blanco", "#FFFFFF", 20),
                    ProductColor("Negro", "#000000", 25),
                    ProductColor("Azul", "#2196F3", 18)
                ),
                material = "Cuero Sintético",
                gender = ClothingGender.UNISEX,
                stock = 63
            ),
            Product(
                id = "CLO010",
                name = "Sandalias",
                description = "Sandalias cómodas para el verano",
                price = 20.00,
                imageUrl = "",
                category = "Calzado",
                isAvailable = true,
                sizes = listOf("36", "37", "38", "39", "40", "41"),
                colors = listOf(
                    ProductColor("Negro", "#000000", 15),
                    ProductColor("Marrón", "#795548", 12),
                    ProductColor("Beige", "#D7CCC8", 10)
                ),
                material = "Cuero",
                gender = ClothingGender.UNISEX,
                stock = 37
            )
        )
    }

    companion object {
        private var instance: ClothingRepository? = null

        fun getInstance(tokenManager: TokenManager): ClothingRepository {
            return instance ?: ClothingRepository(tokenManager).also { instance = it }
        }
    }
}





