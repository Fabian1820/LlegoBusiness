package com.llego.nichos.common.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.nichos.common.data.model.*
import com.llego.nichos.common.utils.mapToCategoryId
import com.llego.nichos.common.utils.mapToMenuCategory
import com.llego.nichos.common.utils.toCategoryId
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.nichos.restaurant.data.model.getDisplayName
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize

/**
 * Pantalla fullscreen para agregar/editar productos
 * Se adapta según el tipo de negocio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    businessType: BusinessType,
    onNavigateBack: () -> Unit,
    onSave: (Product) -> Unit,
    existingProduct: Product? = null,
    modifier: Modifier = Modifier
) {
    val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    // Tipo de producto (Individual o Varios)
    var selectedProductType by remember { mutableStateOf(existingProduct?.productType ?: ProductType.INDIVIDUAL) }

    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    
    // Estado de upload de imagen del producto
    var productImageState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    
    // El path de S3 se extrae del estado Success
    val selectedImageUrl = (productImageState as? ImageUploadState.Success)?.s3Path ?: ""
    
    // Inicializar categoría desde existingProduct
    var selectedCategoryId by remember(businessType, existingProduct?.category) { 
        mutableStateOf<String?>(
            existingProduct?.let { product ->
                // Si es restaurante, mapear MenuCategory a categoryId
                if (businessType == BusinessType.RESTAURANT) {
                    val menuCategory = com.llego.nichos.common.utils.mapToMenuCategory(product.category, businessType)
                    menuCategory?.toCategoryId()
                } else {
                    // Para otros nichos, buscar el categoryId desde la categoría
                    com.llego.nichos.common.utils.mapToCategoryId(product.category, businessType) 
                        ?: categories.find { it.displayName.equals(product.category, ignoreCase = true) }?.id
                }
            }
        )
    }

    // Campos específicos por nicho
    var brand by remember { mutableStateOf(existingProduct?.brand ?: "") }
    var selectedUnit by remember { mutableStateOf(existingProduct?.unit) }
    var stock by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "") }

    // Variantes del producto (para Individual)
    var variantGroups by remember { mutableStateOf(existingProduct?.variants ?: emptyList()) }

    var isAvailable by remember { mutableStateOf(existingProduct?.isAvailable ?: true) }

    // Campos para ropa
    var selectedSizes by remember { mutableStateOf<List<String>>(existingProduct?.sizes ?: emptyList()) }
    var selectedColors by remember { mutableStateOf<List<ProductColor>>(existingProduct?.colors ?: emptyList()) }
    var material by remember { mutableStateOf(existingProduct?.material ?: "") }
    var selectedGender by remember { mutableStateOf(existingProduct?.gender) }

    // Campos para farmacia
    var requiresPrescription by remember { mutableStateOf(existingProduct?.requiresPrescription ?: false) }
    var genericName by remember { mutableStateOf(existingProduct?.genericName ?: "") }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showSizeSelector by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Pantalla completamente fullscreen e independiente (como ChatDetailScreen)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingProduct != null) "Editar Producto" else "Nuevo Producto",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp,
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.secondary
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
                            // Validación según el tipo de producto
                            val isValid = if (selectedProductType == ProductType.MULTIPLE) {
                                // Para MULTIPLE solo requiere descripción
                                description.isNotBlank()
                            } else {
                                // Para INDIVIDUAL requiere nombre, precio y categoría
                                name.isNotBlank() && price.toDoubleOrNull() != null && selectedCategoryId != null
                            }

                            if (isValid) {
                                val categoryString = if (selectedProductType == ProductType.INDIVIDUAL && selectedCategoryId != null) {
                                    if (businessType == BusinessType.RESTAURANT) {
                                        val menuCategory = MenuCategory.values().find { it.toCategoryId() == selectedCategoryId }
                                        menuCategory?.getDisplayName() ?: selectedCategoryId ?: ""
                                    } else {
                                        categories.find { it.id == selectedCategoryId }?.displayName ?: selectedCategoryId ?: ""
                                    }
                                } else {
                                    existingProduct?.category ?: ""
                                }

                                val product = Product(
                                    id = existingProduct?.id ?: generateId(),
                                    name = if (selectedProductType == ProductType.INDIVIDUAL) name else existingProduct?.name ?: "Producto Múltiple",
                                    description = description,
                                    price = if (selectedProductType == ProductType.INDIVIDUAL) price.toDoubleOrNull() ?: 0.0 else 0.0,
                                    imageUrl = selectedImageUrl,
                                    category = categoryString,
                                    productType = selectedProductType,
                                    isAvailable = isAvailable,
                                    brand = if (selectedProductType == ProductType.INDIVIDUAL && businessType == BusinessType.MARKET) brand.takeIf { it.isNotBlank() } else null,
                                    unit = if (selectedProductType == ProductType.INDIVIDUAL && businessType == BusinessType.MARKET) selectedUnit else null,
                                    stock = if (selectedProductType == ProductType.INDIVIDUAL) stock.toIntOrNull() else null,
                                    preparationTime = null,  // Removido para MVP
                                    variants = if (selectedProductType == ProductType.INDIVIDUAL) variantGroups else emptyList(),
                                    sizes = null,
                                    colors = null,
                                    material = null,
                                    gender = null,
                                    requiresPrescription = false,
                                    genericName = null,
                                    // Campos de dieta removidos para MVP
                                    isVegetarian = false,
                                    isVegan = false,
                                    isGlutenFree = false,
                                    allergens = emptyList(),
                                    calories = null
                                )
                                onSave(product)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = if (selectedProductType == ProductType.MULTIPLE) {
                            description.isNotBlank()
                        } else {
                            name.isNotBlank() && price.toDoubleOrNull() != null && selectedCategoryId != null
                        },
                        shape = RoundedCornerShape(14.dp)
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
            // Contenido scrolleable de la pantalla
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de tipo de producto
                ProductTypeSelector(
                    selectedType = selectedProductType,
                    onTypeSelected = { selectedProductType = it }
                )

                // Sección de Imagen
                ImageUploadPreview(
                    label = "Imagen del Producto",
                    uploadState = productImageState,
                    onStateChange = { productImageState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadProductImage(uri, token)
                    },
                    size = ImageUploadSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )

                // Información básica - Simplificada para MULTIPLE
                if (selectedProductType == ProductType.MULTIPLE) {
                    // Modo VARIOS: Solo descripción
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Información del Producto",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Descripción *") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Describe los productos que se muestran en la imagen") }
                            )
                        }
                    }
                } else {
                    // Modo INDIVIDUAL: Campos completos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Información Básica",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre del producto *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Selector de categoría
                            ExposedDropdownMenuBox(
                                expanded = showCategoryDropdown,
                                onExpandedChange = { showCategoryDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = categories.find { it.id == selectedCategoryId }?.displayName ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoría *") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
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
                                    leadingIcon = { Text("$") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Stock (para todos excepto restaurante)
                                if (businessType != BusinessType.RESTAURANT) {
                                    OutlinedTextField(
                                        value = stock,
                                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) stock = it },
                                        label = { Text("Stock") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Detalles específicos por nicho (solo para modo INDIVIDUAL)
                if (selectedProductType == ProductType.INDIVIDUAL) {
                    when (businessType) {
                        BusinessType.RESTAURANT -> {
                            // Sección de variantes
                            VariantsSection(
                                variantGroups = variantGroups,
                                onVariantsChange = { variantGroups = it }
                            )

                            // Disponibilidad
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Disponible para ordenar",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = isAvailable,
                                        onCheckedChange = { isAvailable = it }
                                    )
                                }
                            }
                        }
                        BusinessType.MARKET, BusinessType.CANDY_STORE -> MarketSpecificFields(
                            brand = brand,
                            onBrandChange = { brand = it },
                            selectedUnit = selectedUnit,
                            onUnitChange = { selectedUnit = it },
                            showUnitDropdown = showUnitDropdown,
                            onShowUnitDropdownChange = { showUnitDropdown = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantsSection(
    variantGroups: List<ProductVariantGroup>,
    onVariantsChange: (List<ProductVariantGroup>) -> Unit
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Variantes y Opciones",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick = { showAddVariantDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar variante",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (variantGroups.isEmpty()) {
                Text(
                    text = "Agrega variantes como guarniciones, agregos, tamaños, etc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                variantGroups.forEach { group ->
                    VariantGroupItem(
                        group = group,
                        onDelete = {
                            onVariantsChange(variantGroups.filter { it.id != group.id })
                        }
                    )
                }
            }
        }
    }

    if (showAddVariantDialog) {
        AddVariantDialog(
            onDismiss = { showAddVariantDialog = false },
            onAdd = { newGroup ->
                onVariantsChange(variantGroups + newGroup)
                showAddVariantDialog = false
            }
        )
    }
}

@Composable
private fun VariantGroupItem(
    group: ProductVariantGroup,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "${group.options.size} opciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Mostrar las opciones
            group.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${option.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (option.priceModifier != 0.0) {
                        Text(
                            text = if (option.priceModifier > 0) "+$${option.priceModifier}" else "-$${-option.priceModifier}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddVariantDialog(
    onDismiss: () -> Unit,
    onAdd: (ProductVariantGroup) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var optionName by remember { mutableStateOf("") }
    var optionPrice by remember { mutableStateOf("") }
    var options by remember { mutableStateOf<List<VariantOption>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Agregar Variante",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nombre del grupo") },
                    placeholder = { Text("Ej: Guarniciones, Agregos, Tamaño") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Divider()

                Text(
                    text = "Opciones (${options.size})",
                    style = MaterialTheme.typography.labelLarge
                )

                if (options.isNotEmpty()) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (option.priceModifier != 0.0) {
                                Text(
                                    text = if (option.priceModifier > 0) "+$${option.priceModifier}" else "$${option.priceModifier}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(
                                onClick = { options = options.filter { it.id != option.id } },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = optionName,
                    onValueChange = { optionName = it },
                    label = { Text("Nombre de opción") },
                    placeholder = { Text("Ej: Extra queso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = optionPrice,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it == "-") optionPrice = it },
                    label = { Text("Precio adicional (opcional)") },
                    leadingIcon = { Text("$") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (optionName.isNotBlank()) {
                            val newOption = VariantOption(
                                id = "opt_${(0..999999).random()}",
                                name = optionName,
                                priceModifier = optionPrice.toDoubleOrNull() ?: 0.0
                            )
                            options = options + newOption
                            optionName = ""
                            optionPrice = ""
                        }
                    },
                    enabled = optionName.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar opción")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank() && options.isNotEmpty()) {
                        val newGroup = ProductVariantGroup(
                            id = "vg_${(0..999999).random()}",
                            name = groupName,
                            options = options
                        )
                        onAdd(newGroup)
                    }
                },
                enabled = groupName.isNotBlank() && options.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ProductTypeSelector(
    selectedType: ProductType,
    onTypeSelected: (ProductType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tipo de Producto",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProductType.entries.forEach { type ->
                val isSelected = selectedType == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTypeSelected(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    border = if (isSelected) {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (type == ProductType.INDIVIDUAL) {
                                Icons.Default.Restaurant
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = type.getDisplayName(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        if (selectedType == ProductType.MULTIPLE) {
            Text(
                text = "💡 Varios productos en una sola imagen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketSpecificFields(
    brand: String,
    onBrandChange: (String) -> Unit,
    selectedUnit: ProductUnit?,
    onUnitChange: (ProductUnit) -> Unit,
    showUnitDropdown: Boolean,
    onShowUnitDropdownChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detalles del Producto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = showUnitDropdown,
                onExpandedChange = onShowUnitDropdownChange
            ) {
                OutlinedTextField(
                    value = selectedUnit?.getDisplayName() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unidad de medida") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showUnitDropdown,
                    onDismissRequest = { onShowUnitDropdownChange(false) }
                ) {
                    ProductUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.getDisplayName()) },
                            onClick = {
                                onUnitChange(unit)
                                onShowUnitDropdownChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClothingSpecificFields(
    selectedSizes: List<String>,
    onSizesChange: (List<String>) -> Unit,
    selectedColors: List<ProductColor>,
    onColorsChange: (List<ProductColor>) -> Unit,
    material: String,
    onMaterialChange: (String) -> Unit,
    selectedGender: ClothingGender?,
    onGenderChange: (ClothingGender) -> Unit,
    showGenderDropdown: Boolean,
    onShowGenderDropdownChange: (Boolean) -> Unit
) {
    val availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    val basicColors = listOf(
        ProductColor("Negro", "#000000", 0),
        ProductColor("Blanco", "#FFFFFF", 0),
        ProductColor("Rojo", "#FF0000", 0),
        ProductColor("Azul", "#0000FF", 0),
        ProductColor("Verde", "#00FF00", 0),
        ProductColor("Amarillo", "#FFFF00", 0)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detalles de la Prenda",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Selector de género
            ExposedDropdownMenuBox(
                expanded = showGenderDropdown,
                onExpandedChange = onShowGenderDropdownChange
            ) {
                OutlinedTextField(
                    value = selectedGender?.getDisplayName() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Género") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showGenderDropdown,
                    onDismissRequest = { onShowGenderDropdownChange(false) }
                ) {
                    ClothingGender.entries.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender.getDisplayName()) },
                            onClick = {
                                onGenderChange(gender)
                                onShowGenderDropdownChange(false)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = material,
                onValueChange = onMaterialChange,
                label = { Text("Material") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Ej: Algodón 100%") }
            )

            // Selector de tallas
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tallas disponibles",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSizes) { size ->
                        FilterChip(
                            selected = selectedSizes.contains(size),
                            onClick = {
                                onSizesChange(
                                    if (selectedSizes.contains(size)) {
                                        selectedSizes - size
                                    } else {
                                        selectedSizes + size
                                    }
                                )
                            },
                            label = { Text(size) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            leadingIcon = if (selectedSizes.contains(size)) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Selector de colores
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Colores disponibles",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(basicColors) { color ->
                        val isSelected = selectedColors.any { it.hexCode == color.hexCode }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(color.hexCode))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onColorsChange(
                                        if (isSelected) {
                                            selectedColors.filter { it.hexCode != color.hexCode }
                                        } else {
                                            selectedColors + color
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (color.hexCode == "#FFFFFF") Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PharmacySpecificFields(
    genericName: String,
    onGenericNameChange: (String) -> Unit,
    requiresPrescription: Boolean,
    onRequiresPrescriptionChange: (Boolean) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detalles del Medicamento",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Marca / Laboratorio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = genericName,
                onValueChange = onGenericNameChange,
                label = { Text("Nombre genérico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Requiere receta médica",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = requiresPrescription,
                    onCheckedChange = onRequiresPrescriptionChange
                )
            }
        }
    }
}

/**
 * Helper para parsear color hex (multiplataforma)
 */
private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorLong = cleanHex.toLong(16)
        val alpha = if (cleanHex.length == 8) {
            ((colorLong shr 24) and 0xFF) / 255f
        } else {
            1f
        }
        val red = ((colorLong shr 16) and 0xFF) / 255f
        val green = ((colorLong shr 8) and 0xFF) / 255f
        val blue = (colorLong and 0xFF) / 255f

        Color(red, green, blue, alpha)
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Genera un ID único simple para KMP
 */
private fun generateId(): String {
    return "product_${(0..999999999).random()}"
}
