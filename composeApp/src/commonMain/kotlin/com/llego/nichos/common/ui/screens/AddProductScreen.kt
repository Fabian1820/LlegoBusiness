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
    
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    
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
    var preparationTime by remember { mutableStateOf(existingProduct?.preparationTime?.toString() ?: "") }
    
    // Campos específicos para restaurante
    var isVegetarian by remember { mutableStateOf(existingProduct?.isVegetarian ?: false) }
    var isVegan by remember { mutableStateOf(existingProduct?.isVegan ?: false) }
    var isGlutenFree by remember { mutableStateOf(existingProduct?.isGlutenFree ?: false) }
    var allergens by remember { mutableStateOf(existingProduct?.allergens ?: emptyList()) }
    var allergensText by remember { mutableStateOf(existingProduct?.allergens?.joinToString(", ") ?: "") }
    var calories by remember { mutableStateOf(existingProduct?.calories?.toString() ?: "") }
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
                            if (name.isNotBlank() && price.toDoubleOrNull() != null && selectedCategoryId != null) {
                                val categoryString = if (businessType == BusinessType.RESTAURANT) {
                                    val menuCategory = MenuCategory.values().find { it.toCategoryId() == selectedCategoryId }
                                    menuCategory?.getDisplayName() ?: selectedCategoryId ?: ""
                                } else {
                                    categories.find { it.id == selectedCategoryId }?.displayName ?: selectedCategoryId ?: ""
                                }

                                val product = Product(
                                    id = existingProduct?.id ?: generateId(),
                                    name = name,
                                    description = description,
                                    price = price.toDouble(),
                                    imageUrl = existingProduct?.imageUrl ?: "",
                                    category = categoryString,
                                    isAvailable = isAvailable,
                                    brand = if (businessType in listOf(BusinessType.MARKET, BusinessType.AGROMARKET, BusinessType.PHARMACY)) brand.takeIf { it.isNotBlank() } else null,
                                    unit = if (businessType in listOf(BusinessType.MARKET, BusinessType.AGROMARKET)) selectedUnit else null,
                                    stock = stock.toIntOrNull(),
                                    preparationTime = if (businessType == BusinessType.RESTAURANT) preparationTime.toIntOrNull() else null,
                                    sizes = if (businessType == BusinessType.CLOTHING_STORE && selectedSizes.isNotEmpty()) selectedSizes else null,
                                    colors = if (businessType == BusinessType.CLOTHING_STORE && selectedColors.isNotEmpty()) selectedColors else null,
                                    material = if (businessType == BusinessType.CLOTHING_STORE) material.takeIf { it.isNotBlank() } else null,
                                    gender = if (businessType == BusinessType.CLOTHING_STORE) selectedGender else null,
                                    requiresPrescription = if (businessType == BusinessType.PHARMACY) requiresPrescription else false,
                                    genericName = if (businessType == BusinessType.PHARMACY) genericName.takeIf { it.isNotBlank() } else null,
                                    isVegetarian = if (businessType == BusinessType.RESTAURANT) isVegetarian else false,
                                    isVegan = if (businessType == BusinessType.RESTAURANT) isVegan else false,
                                    isGlutenFree = if (businessType == BusinessType.RESTAURANT) isGlutenFree else false,
                                    allergens = if (businessType == BusinessType.RESTAURANT) {
                                        allergensText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    } else emptyList(),
                                    calories = if (businessType == BusinessType.RESTAURANT) calories.toIntOrNull() else null
                                )
                                onSave(product)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && price.toDoubleOrNull() != null && selectedCategoryId != null,
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
                // Sección de Imagen (placeholder)
                ProductImageSection()

                // Información básica
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

                // Detalles específicos por nicho
                when (businessType) {
                    BusinessType.RESTAURANT -> RestaurantSpecificFields(
                        preparationTime = preparationTime,
                        onPreparationTimeChange = { preparationTime = it },
                        isVegetarian = isVegetarian,
                        onIsVegetarianChange = { isVegetarian = it },
                        isVegan = isVegan,
                        onIsVeganChange = { isVegan = it },
                        isGlutenFree = isGlutenFree,
                        onIsGlutenFreeChange = { isGlutenFree = it },
                        allergensText = allergensText,
                        onAllergensTextChange = { allergensText = it },
                        calories = calories,
                        onCaloriesChange = { calories = it },
                        isAvailable = isAvailable,
                        onIsAvailableChange = { isAvailable = it }
                    )
                    BusinessType.MARKET, BusinessType.AGROMARKET -> MarketSpecificFields(
                        brand = brand,
                        onBrandChange = { brand = it },
                        selectedUnit = selectedUnit,
                        onUnitChange = { selectedUnit = it },
                        showUnitDropdown = showUnitDropdown,
                        onShowUnitDropdownChange = { showUnitDropdown = it }
                    )
                    BusinessType.CLOTHING_STORE -> ClothingSpecificFields(
                        selectedSizes = selectedSizes,
                        onSizesChange = { selectedSizes = it },
                        selectedColors = selectedColors,
                        onColorsChange = { selectedColors = it },
                        material = material,
                        onMaterialChange = { material = it },
                        selectedGender = selectedGender,
                        onGenderChange = { selectedGender = it },
                        showGenderDropdown = showGenderDropdown,
                        onShowGenderDropdownChange = { showGenderDropdown = it }
                    )
                    BusinessType.PHARMACY -> PharmacySpecificFields(
                        genericName = genericName,
                        onGenericNameChange = { genericName = it },
                        requiresPrescription = requiresPrescription,
                        onRequiresPrescriptionChange = { requiresPrescription = it },
                        brand = brand,
                        onBrandChange = { brand = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductImageSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Imagen del Producto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { /* TODO: Implementar selector de imagen */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Toca para agregar imagen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantSpecificFields(
    preparationTime: String,
    onPreparationTimeChange: (String) -> Unit,
    isVegetarian: Boolean,
    onIsVegetarianChange: (Boolean) -> Unit,
    isVegan: Boolean,
    onIsVeganChange: (Boolean) -> Unit,
    isGlutenFree: Boolean,
    onIsGlutenFreeChange: (Boolean) -> Unit,
    allergensText: String,
    onAllergensTextChange: (String) -> Unit,
    calories: String,
    onCaloriesChange: (String) -> Unit,
    isAvailable: Boolean,
    onIsAvailableChange: (Boolean) -> Unit
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
                text = "Detalles del Restaurante",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = preparationTime,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onPreparationTimeChange(it) },
                    label = { Text("Tiempo de preparación (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onCaloriesChange(it) },
                    label = { Text("Calorías") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = allergensText,
                onValueChange = onAllergensTextChange,
                label = { Text("Alérgenos (separados por comas)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Ej: Gluten, Lácteos, Huevo") }
            )

            Divider()

            Text(
                text = "Características dietéticas",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
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
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text("Vegetariano", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = isVegetarian,
                    onCheckedChange = onIsVegetarianChange
                )
            }

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
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color(0xFF8BC34A)
                    )
                    Text("Vegano", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = isVegan,
                    onCheckedChange = onIsVeganChange
                )
            }

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
                        imageVector = Icons.Default.Grain,
                        contentDescription = null,
                        tint = Color(0xFFFFC107)
                    )
                    Text("Sin Gluten", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = isGlutenFree,
                    onCheckedChange = onIsGlutenFreeChange
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Disponible para ordenar",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isAvailable,
                    onCheckedChange = onIsAvailableChange
                )
            }
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
