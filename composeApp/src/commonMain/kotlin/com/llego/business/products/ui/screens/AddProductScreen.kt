package com.llego.business.products.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.products.config.ProductCategoryProvider
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.extractFilename
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.CurrencySelector
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Datos normalizados del formulario de producto.
 * 
 * @property name Nombre del producto (requerido)
 * @property description Descripción del producto (requerido)
 * @property price Precio del producto (requerido)
 * @property categoryId ID de la categoría del producto (requerido)
 * @property weight Peso del producto (opcional - el backend asigna "" como default si es null)
 * @property currency Código de moneda del producto (requerido - default "USD")
 * @property imagePath Ruta S3 de la imagen del producto (requerido)
 * @property availability Disponibilidad del producto (requerido - default true)
 */
data class ProductFormData(
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String?,
    val weight: String?,
    val currency: String,
    val imagePath: String?,
    val availability: Boolean
)

/**
 * Pantalla fullscreen para crear o editar productos usando el modelo GraphQL canonico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    branchId: String?,
    onNavigateBack: () -> Unit,
    onSave: (ProductFormData) -> Unit,
    existingProduct: Product? = null,
    modifier: Modifier = Modifier
) {
    val categories = remember { ProductCategoryProvider.getCategories() }
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var weight by remember { mutableStateOf(existingProduct?.weight ?: "") }
    var selectedCategoryId by remember { mutableStateOf(existingProduct?.categoryId) }
    var currency by remember { mutableStateOf(existingProduct?.currency ?: "USD") }
    var isAvailable by remember { mutableStateOf(existingProduct?.availability ?: true) }

    var showCategoryDropdown by remember { mutableStateOf(false) }

    val initialImageState = remember(existingProduct) {
        existingProduct?.let { product ->
            val displayUri = product.imageUrl.takeIf { it.isNotBlank() } ?: product.image
            if (displayUri.isNotBlank()) {
                ImageUploadState.Success(
                    localUri = displayUri,
                    s3Path = product.image,
                    filename = displayUri.extractFilename()
                )
            } else {
                ImageUploadState.Idle
            }
        } ?: ImageUploadState.Idle
    }
    var productImageState by remember { mutableStateOf<ImageUploadState>(initialImageState) }

    val imagePath = (productImageState as? ImageUploadState.Success)?.s3Path
    val priceValue = price.toDoubleOrNull()
    val isSaveEnabled = branchId != null &&
        name.isNotBlank() &&
        priceValue != null &&
        selectedCategoryId != null &&
        currency.isNotBlank() &&
        !imagePath.isNullOrBlank()
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingProduct != null) "Editar producto" else "Nuevo producto",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp,
                shape = LlegoCustomShapes.bottomSheet
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        shape = LlegoCustomShapes.secondaryButton,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            "Cancelar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val normalizedWeight = weight.trim().takeIf { it.isNotBlank() }
                            val normalizedDescription = description.trim()
                            val normalizedName = name.trim()

                            onSave(
                                ProductFormData(
                                    name = normalizedName,
                                    description = normalizedDescription,
                                    price = priceValue ?: 0.0,
                                    categoryId = selectedCategoryId,
                                    weight = normalizedWeight,
                                    currency = currency,
                                    imagePath = imagePath,
                                    availability = isAvailable
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isSaveEnabled,
                        shape = LlegoCustomShapes.primaryButton
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Imagen del producto",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                ImageUploadPreview(
                    label = "Imagen",
                    uploadState = productImageState,
                    onStateChange = { productImageState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadProductImage(uri, token)
                    },
                    size = ImageUploadSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.productCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Informacion basica",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre del producto *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = LlegoCustomShapes.inputField,
                            colors = textFieldColors
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripcion") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = LlegoCustomShapes.inputField,
                            colors = textFieldColors
                        )

                        ExposedDropdownMenuBox(
                            expanded = showCategoryDropdown,
                            onExpandedChange = { showCategoryDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = categories.find { it.id == selectedCategoryId }?.displayName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = LlegoCustomShapes.inputField,
                                colors = textFieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.displayName) },
                                        onClick = {
                                            selectedCategoryId = category.id
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                                label = { Text("Precio *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = LlegoCustomShapes.inputField,
                                colors = textFieldColors
                            )

                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Peso (opcional)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = LlegoCustomShapes.inputField,
                                colors = textFieldColors
                            )
                        }

                        CurrencySelector(
                            selectedCurrency = currency,
                            onCurrencySelected = { currency = it },
                            label = "Moneda *",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Disponibilidad",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = if (isAvailable) "Disponible" else "No disponible",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isAvailable,
                                onCheckedChange = { isAvailable = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (branchId == null) {
                    Text(
                        text = "Selecciona una sucursal antes de guardar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
