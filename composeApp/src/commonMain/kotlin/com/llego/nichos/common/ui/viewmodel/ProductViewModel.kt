package com.llego.nichos.common.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.repositories.ProductRepository
import com.llego.shared.data.upload.ImageUploadServiceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar productos usando GraphQL
 */
class ProductViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val repository = ProductRepository(tokenManager)
    private val uploadService = ImageUploadServiceFactory.create()

    private val _uploadState = MutableStateFlow<ImageUploadResult>(ImageUploadResult.Loading)
    val uploadState: StateFlow<ImageUploadResult> = _uploadState.asStateFlow()

    private val _productsState = MutableStateFlow<ProductsResult>(ProductsResult.Loading)
    val productsState: StateFlow<ProductsResult> = _productsState.asStateFlow()

    /**
     * Carga todos los productos
     */
    fun loadProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false
    ) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProducts(
                branchId = branchId,
                categoryId = categoryId,
                availableOnly = availableOnly
            )
        }
    }

    /**
     * Carga productos por IDs específicos
     */
    fun loadProductsByIds(ids: List<String>) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProductsByIds(ids)
        }
    }

    /**
     * Recarga los productos
     */
    fun refresh() {
        loadProducts()
    }

    // ============= CRUD OPERATIONS =============

    /**
     * Crea un nuevo producto con imagen
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param imageFilePath Path local de la imagen (se subirá primero)
     * @param branchId ID de la sucursal (opcional si se pasa businessId)
     * @param businessId ID del negocio (opcional si se pasa branchId)
     * @param currency Moneda (default: USD)
     * @param weight Peso del producto (opcional)
     * @param categoryId ID de la categoría (opcional)
     */
    fun createProduct(
        name: String,
        description: String,
        price: Double,
        imageFilePath: String,
        branchId: String? = null,
        businessId: String? = null,
        currency: String = "USD",
        weight: String? = null,
        categoryId: String? = null
    ) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading

            // Primero subir la imagen
            _uploadState.value = ImageUploadResult.Loading
            val uploadResult = uploadService.uploadProductImage(
                filePath = imageFilePath,
                token = tokenManager.getToken()
            )

            when (uploadResult) {
                is ImageUploadResult.Success -> {
                    _uploadState.value = uploadResult

                    // Crear el producto con la imagen subida
                    val result = repository.createProduct(
                        name = name,
                        description = description,
                        price = price,
                        image = uploadResult.response.imagePath,
                        branchId = branchId,
                        businessId = businessId,
                        currency = currency,
                        weight = weight,
                        categoryId = categoryId
                    )
                    _productsState.value = result
                }
                is ImageUploadResult.Error -> {
                    _uploadState.value = uploadResult
                    _productsState.value = ProductsResult.Error("Error al subir imagen: ${uploadResult.message}")
                }
                is ImageUploadResult.Loading -> {
                    // No debería llegar aquí
                }
            }
        }
    }

    /**
     * Actualiza un producto existente
     * @param productId ID del producto a actualizar
     * @param name Nuevo nombre (opcional)
     * @param description Nueva descripción (opcional)
     * @param price Nuevo precio (opcional)
     * @param imageFilePath Path local de la nueva imagen (opcional, se subirá si se proporciona)
     * @param currency Nueva moneda (opcional)
     * @param weight Nuevo peso (opcional)
     * @param availability Nueva disponibilidad (opcional)
     * @param categoryId Nuevo ID de categoría (opcional)
     */
    fun updateProduct(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        imageFilePath: String? = null,
        currency: String? = null,
        weight: String? = null,
        availability: Boolean? = null,
        categoryId: String? = null
    ) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading

            var imagePath: String? = null

            // Si se proporciona una nueva imagen, subirla primero
            if (imageFilePath != null) {
                _uploadState.value = ImageUploadResult.Loading
                val uploadResult = uploadService.uploadProductImage(
                    filePath = imageFilePath,
                    token = tokenManager.getToken()
                )

                when (uploadResult) {
                    is ImageUploadResult.Success -> {
                        _uploadState.value = uploadResult
                        imagePath = uploadResult.response.imagePath
                    }
                    is ImageUploadResult.Error -> {
                        _uploadState.value = uploadResult
                        _productsState.value = ProductsResult.Error("Error al subir imagen: ${uploadResult.message}")
                        return@launch
                    }
                    is ImageUploadResult.Loading -> {
                        // No debería llegar aquí
                    }
                }
            }

            // Actualizar el producto
            val result = repository.updateProduct(
                productId = productId,
                name = name,
                description = description,
                price = price,
                image = imagePath,
                currency = currency,
                weight = weight,
                availability = availability,
                categoryId = categoryId
            )
            _productsState.value = result
        }
    }

    /**
     * Elimina un producto
     * @param productId ID del producto a eliminar
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.deleteProduct(productId)
        }
    }

    /**
     * Sube una imagen de producto sin crear el producto
     * @param imageFilePath Path local de la imagen
     * @return ImageUploadResult con la respuesta del servidor
     */
    fun uploadProductImage(imageFilePath: String) {
        viewModelScope.launch {
            _uploadState.value = ImageUploadResult.Loading
            _uploadState.value = uploadService.uploadProductImage(
                filePath = imageFilePath,
                token = tokenManager.getToken()
            )
        }
    }
}
