package com.llego.business.products.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.shared.data.model.Combo
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.data.model.ImageUploadState
import kotlinx.coroutines.launch

// Modelo temporal para slots en edición
data class SlotEdit(
    val id: String = generateUUID(),
    var name: String = "",
    var minSelections: Int = 1,
    var maxSelections: Int = 1,
    val options: MutableList<OptionEdit> = mutableListOf()
)

data class OptionEdit(
    val id: String = generateUUID(),
    var productId: String = "",
    var productName: String = "",
    var isDefault: Boolean = false,
    var priceAdjustment: Double = 0.0
)

// Helper para generar IDs únicos multiplataforma
private fun generateUUID(): String {
    // Genera un ID único usando números aleatorios
    // Suficiente para IDs temporales en la UI
    val random1 = (0..999999999).random()
    val random2 = (0..999999999).random()
    return "temp-$random1-$random2"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComboScreen(
    viewModel: ComboViewModel,
    productViewModel: ProductViewModel,
    branchId: String,
    combo: Combo? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val productsState by productViewModel.productsState.collectAsState()
    val imageUploadViewModel = remember { ImageUploadViewModel() }

    var name by remember { mutableStateOf(combo?.name ?: "") }
    var description by remember { mutableStateOf(combo?.description ?: "") }
    var comboImageState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var discountType by remember { mutableStateOf(combo?.discountType?.name ?: "PERCENTAGE") }
    var discountValue by remember { mutableStateOf(combo?.discountValue?.toString() ?: "0") }
    var slots by remember { mutableStateOf<List<SlotEdit>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreatingNewSlot by remember { mutableStateOf(false) }
    var editingSlotIndex by remember { mutableStateOf<Int?>(null) }

    // Cargar productos al iniciar
    LaunchedEffect(branchId) {
        productViewModel.loadProducts(branchId = branchId)
    }

    // Inicializar slots desde combo existente
    LaunchedEffect(combo) {
        if (combo != null && slots.isEmpty()) {
            slots = combo.slots.map { slot ->
                SlotEdit(
                    name = slot.name,
                    minSelections = slot.minSelections,
                    maxSelections = slot.maxSelections,
                    options = slot.options.map { option ->
                        OptionEdit(
                            productId = option.productId,
                            productName = option.product?.name ?: "",
                            isDefault = option.isDefault,
                            priceAdjustment = option.priceAdjustment
                        )
                    }.toMutableList()
                )
            }
            // Inicializar estado de imagen si existe
            if (combo.image != null) {
                comboImageState = ImageUploadState.Success(
                    localUri = combo.image,
                    s3Path = combo.image,
                    filename = ""
                )
            }
        }
    }

    val availableProducts = when (val state = productsState) {
        is ProductsResult.Success -> state.products
        else -> emptyList()
    }
    
    // Obtener path de imagen desde el estado
    val imagePath = (comboImageState as? ImageUploadState.Success)?.s3Path

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (combo == null) "Crear Combo" else "Editar Combo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del combo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Imagen
            ImageUploadPreview(
                label = "Imagen del combo (opcional)",
                uploadState = comboImageState,
                onStateChange = { comboImageState = it },
                uploadFunction = imageUploadViewModel::uploadProductImage,
                size = ImageUploadSize.LARGE,
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de descuento
            Text("Tipo de descuento", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = discountType == "PERCENTAGE",
                    onClick = { discountType = "PERCENTAGE" },
                    label = { Text("Porcentaje") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = discountType == "FIXED",
                    onClick = { discountType = "FIXED" },
                    label = { Text("Cantidad fija") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Valor del descuento
            OutlinedTextField(
                value = discountValue,
                onValueChange = { discountValue = it },
                label = { Text(if (discountType == "PERCENTAGE") "Descuento (%)" else "Descuento (monto)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Sección de slots - Inline sin diálogos
            Text(
                "Slots del combo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Text(
                "Los slots son grupos de productos entre los que el cliente puede elegir",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Slots existentes
            slots.forEachIndexed { index, slot ->
                SlotInlineCard(
                    slot = slot,
                    availableProducts = availableProducts,
                    isExpanded = editingSlotIndex == index,
                    onToggleExpand = {
                        editingSlotIndex = if (editingSlotIndex == index) null else index
                    },
                    onDelete = {
                        slots = slots.toMutableList().apply { removeAt(index) }
                        if (editingSlotIndex == index) editingSlotIndex = null
                    },
                    onUpdate = {
                        slots = slots.toList()
                    }
                )
            }

            // Formulario para crear nuevo slot
            if (isCreatingNewSlot) {
                NewSlotInlineForm(
                    availableProducts = availableProducts,
                    onCancel = { isCreatingNewSlot = false },
                    onCreate = { newSlot ->
                        slots = slots + newSlot
                        isCreatingNewSlot = false
                    }
                )
            } else {
                // Botón para agregar nuevo slot
                OutlinedButton(
                    onClick = { isCreatingNewSlot = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Slot")
                }
            }

            // Mensaje de error
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Botón de guardar
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        
                        try {
                            val discountVal = discountValue.toDoubleOrNull() ?: 0.0
                            
                            // Convertir slots a formato Map para el repositorio
                            val slotsData = slots.map { slot ->
                                mapOf(
                                    "name" to slot.name,
                                    "minSelections" to slot.minSelections,
                                    "maxSelections" to slot.maxSelections,
                                    "options" to slot.options.map { option ->
                                        mapOf(
                                            "productId" to option.productId,
                                            "isDefault" to option.isDefault,
                                            "priceAdjustment" to option.priceAdjustment
                                        )
                                    }
                                )
                            }
                            
                            if (combo == null) {
                                viewModel.createComboWithImagePath(
                                    branchId = branchId,
                                    name = name,
                                    description = description,
                                    imagePath = imagePath,
                                    discountType = discountType,
                                    discountValue = discountVal,
                                    slots = slotsData
                                )
                            } else {
                                viewModel.updateComboWithImagePath(
                                    comboId = combo.id,
                                    name = name,
                                    description = description,
                                    imagePath = imagePath,
                                    discountType = discountType,
                                    discountValue = discountVal,
                                    slots = slotsData
                                )
                            }
                            
                            onNavigateBack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Error desconocido"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && name.isNotBlank() && slots.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (combo == null) "Crear Combo" else "Guardar Cambios")
                }
            }
        }
    }
}

@Composable
private fun SlotCard(
    slot: SlotEdit,
    onDelete: () -> Unit,
    onAddProduct: () -> Unit,
    onDeleteOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        slot.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        "Selecciona ${slot.minSelections} - ${slot.maxSelections}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onAddProduct, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar producto",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar slot",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (slot.options.isEmpty()) {
                Text(
                    "Sin productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                slot.options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.small
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                option.productName,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (option.isDefault) {
                                Text(
                                    "Por defecto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteOption(option.id) },
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
        }
    }
}




// Componente inline para crear nuevo slot
@Composable
private fun NewSlotInlineForm(
    availableProducts: List<Product>,
    onCancel: () -> Unit,
    onCreate: (SlotEdit) -> Unit
) {
    var slotName by remember { mutableStateOf("") }
    var slotDescription by remember { mutableStateOf("") }
    var minSel by remember { mutableStateOf("1") }
    var maxSel by remember { mutableStateOf("1") }
    var isRequired by remember { mutableStateOf(true) }
    var selectedProducts by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, availableProducts) {
        if (searchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nuevo Slot",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }
            }

            HorizontalDivider()

            // Información básica
            OutlinedTextField(
                value = slotName,
                onValueChange = { slotName = it },
                label = { Text("Nombre del slot *") },
                placeholder = { Text("Ej: Plato Fuerte, Bebidas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = slotDescription,
                onValueChange = { slotDescription = it },
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Ej: Elige tu plato principal") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Configuración de selección
            Text(
                "Configuración",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = minSel,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            minSel = it
                        }
                    },
                    label = { Text("Mínimo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                OutlinedTextField(
                    value = maxSel,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            maxSel = it
                        }
                    },
                    label = { Text("Máximo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Slot obligatorio",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isRequired,
                    onCheckedChange = { isRequired = it }
                )
            }

            // Selección de productos
            HorizontalDivider()

            Text(
                "Productos del slot *",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar productos") },
                placeholder = { Text("Escribe para buscar...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (selectedProducts.isNotEmpty()) {
                Text(
                    "${selectedProducts.size} producto${if (selectedProducts.size != 1) "s" else ""} seleccionado${if (selectedProducts.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Lista de productos seleccionables
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredProducts.take(5).forEach { product ->
                    val isSelected = selectedProducts.contains(product.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedProducts = if (isSelected) {
                                    selectedProducts - product.id
                                } else {
                                    selectedProducts + product.id
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    "${product.currency} ${product.price}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        }
                    }
                }

                if (filteredProducts.size > 5) {
                    Text(
                        "Y ${filteredProducts.size - 5} productos más...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Botones de acción
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val newSlot = SlotEdit(
                            name = slotName,
                            minSelections = minSel.toIntOrNull() ?: 1,
                            maxSelections = maxSel.toIntOrNull() ?: 1,
                            options = selectedProducts.mapIndexed { index, productId ->
                                val product = availableProducts.find { it.id == productId }
                                OptionEdit(
                                    productId = productId,
                                    productName = product?.name ?: "",
                                    isDefault = index == 0,
                                    priceAdjustment = 0.0
                                )
                            }.toMutableList()
                        )
                        onCreate(newSlot)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = slotName.isNotBlank() && 
                             selectedProducts.isNotEmpty() &&
                             minSel.toIntOrNull() != null && 
                             maxSel.toIntOrNull() != null &&
                             (minSel.toIntOrNull() ?: 0) <= (maxSel.toIntOrNull() ?: 0)
                ) {
                    Text("Crear Slot")
                }
            }
        }
    }
}

// Card expandible para slots existentes
@Composable
private fun SlotInlineCard(
    slot: SlotEdit,
    availableProducts: List<Product>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, availableProducts) {
        if (searchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header del slot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        slot.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "${slot.options.size} producto${if (slot.options.size != 1) "s" else ""} • Min: ${slot.minSelections}, Max: ${slot.maxSelections}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Contenido expandido
            if (isExpanded) {
                HorizontalDivider()

                // Productos actuales
                Text(
                    "Productos incluidos",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                slot.options.forEach { option ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    option.productName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                if (option.isDefault) {
                                    Text(
                                        "Por defecto",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    slot.options.removeAll { it.id == option.id }
                                    onUpdate()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Agregar más productos
                HorizontalDivider()

                Text(
                    "Agregar más productos",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar productos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                val currentProductIds = slot.options.map { it.productId }.toSet()
                val availableToAdd = filteredProducts.filter { !currentProductIds.contains(it.id) }

                availableToAdd.take(3).forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                slot.options.add(
                                    OptionEdit(
                                        productId = product.id,
                                        productName = product.name,
                                        isDefault = slot.options.isEmpty(),
                                        priceAdjustment = 0.0
                                    )
                                )
                                onUpdate()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${product.currency} ${product.price}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (availableToAdd.size > 3) {
                    Text(
                        "Y ${availableToAdd.size - 3} productos más disponibles...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
