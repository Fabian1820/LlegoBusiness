package com.llego.business.products.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.shared.data.model.Combo
import com.llego.shared.data.model.CombosResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

// Modelo temporal para slots en edicion
data class SlotEdit(
    val id: String = generateUUID(),
    var name: String = "",
    var description: String? = null,
    var minSelections: Int = 1,
    var maxSelections: Int = 1,
    var isRequired: Boolean = true,
    var displayOrder: Int = 0,
    val options: MutableList<OptionEdit> = mutableListOf()
)

data class OptionEdit(
    val id: String = generateUUID(),
    var productId: String = "",
    var productName: String = "",
    var isDefault: Boolean = false,
    var priceAdjustment: Double = 0.0
)

// Helper para generar IDs unicos multiplataforma
private fun generateUUID(): String {
    // Genera un ID unico usando numeros aleatorios
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
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val productsState by productViewModel.productsState.collectAsState()
    val imageUploadViewModel = remember { ImageUploadViewModel() }

    var name by remember { mutableStateOf(combo?.name ?: "") }
    var description by remember { mutableStateOf(combo?.description ?: "") }
    var comboImageState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var discountType by remember { mutableStateOf(combo?.discountType?.name ?: "NONE") }
    var discountValue by remember { mutableStateOf(combo?.discountValue?.toString() ?: "0") }
    var slots by remember { mutableStateOf<List<SlotEdit>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreatingNewSlot by remember { mutableStateOf(false) }
    var editingSlotIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(branchId) {
        productViewModel.loadProducts(branchId = branchId)
    }

    LaunchedEffect(combo) {
        if (combo != null && slots.isEmpty()) {
            slots = combo.slots.map { slot ->
                SlotEdit(
                    id = slot.id ?: generateUUID(),
                    name = slot.name,
                    description = slot.description,
                    minSelections = slot.minSelections,
                    maxSelections = slot.maxSelections,
                    isRequired = slot.isRequired,
                    displayOrder = slot.displayOrder,
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

    val imagePath = (comboImageState as? ImageUploadState.Success)?.s3Path
    val normalizedDiscountValue = normalizeDiscountValue(discountType, discountValue)
    val formValidationError = validateComboForm(
        comboName = name,
        discountType = discountType,
        discountValue = normalizedDiscountValue,
        slots = slots,
        availableProducts = availableProducts
    )
    val canSave = !isLoading && formValidationError == null

    fun saveCombo() {
        if (formValidationError != null) {
            errorMessage = formValidationError
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val discountVal = normalizedDiscountValue ?: 0.0
                val slotsData = slots.mapIndexed { index, slot ->
                    buildMap<String, Any> {
                        put("name", slot.name)
                        slot.description?.takeIf { it.isNotBlank() }?.let { put("description", it) }
                        put("minSelections", slot.minSelections)
                        put("maxSelections", slot.maxSelections)
                        put("isRequired", slot.isRequired)
                        put("displayOrder", index)
                        put(
                            "options",
                            slot.options.map { option ->
                                mapOf<String, Any>(
                                    "productId" to option.productId,
                                    "isDefault" to option.isDefault,
                                    "priceAdjustment" to option.priceAdjustment
                                )
                            }
                        )
                    }
                }

                val result = if (combo == null) {
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
                when (result) {
                    is CombosResult.Success -> onNavigateBack()
                    is CombosResult.Error -> errorMessage = result.message
                    is CombosResult.Loading -> errorMessage = "No se pudo completar la operacion"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (combo == null) "Crear combo" else "Editar combo",
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
        bottomBar = {
            if (!isKeyboardVisible) {
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
                            shape = LlegoCustomShapes.secondaryButton
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { saveCombo() },
                            modifier = Modifier.weight(1f),
                            enabled = canSave,
                            shape = LlegoCustomShapes.primaryButton
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (combo == null) "Crear combo" else "Guardar cambios")
                            }
                        }
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
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del combo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = LlegoCustomShapes.inputField
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = LlegoCustomShapes.inputField
                )

                ImageUploadPreview(
                    label = "Imagen del combo (opcional)",
                    uploadState = comboImageState,
                    onStateChange = { comboImageState = it },
                    uploadFunction = imageUploadViewModel::uploadProductImage,
                    size = ImageUploadSize.LARGE,
                    showSuccessFileName = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de descuento", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiscountTypeButton(
                        selected = discountType == "NONE",
                        label = "Sin dto",
                        onClick = {
                            discountType = "NONE"
                            discountValue = "0"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DiscountTypeButton(
                        selected = discountType == "PERCENTAGE",
                        label = "%",
                        onClick = { discountType = "PERCENTAGE" },
                        modifier = Modifier.weight(1f)
                    )
                    DiscountTypeButton(
                        selected = discountType == "FIXED",
                        label = "Cant. fija",
                        onClick = { discountType = "FIXED" },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^-?\\d{0,6}([\\.,]\\d{0,2})?$"))) {
                            discountValue = it
                        }
                    },
                    label = {
                        Text(
                            when (discountType) {
                                "PERCENTAGE" -> "Descuento (%)"
                                "FIXED" -> "Descuento (monto)"
                                else -> "Sin descuento"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = discountType != "NONE",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = LlegoCustomShapes.inputField
                )

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
                    OutlinedButton(
                        onClick = { isCreatingNewSlot = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = LlegoCustomShapes.secondaryButton
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar slot")
                    }
                }

                (errorMessage ?: formValidationError)?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscountTypeButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(38.dp),
            shape = LlegoCustomShapes.inputField,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(38.dp),
            shape = LlegoCustomShapes.inputField,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(label, maxLines = 1)
        }
    }
}

@Composable
private fun CompactProductSelectionCard(
    product: Product,
    selected: Boolean,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = product.imageUrl.takeIf { it.isNotBlank() } ?: product.image.takeIf { it.isNotBlank() }
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val titleColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = LlegoCustomShapes.inputField,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(LlegoCustomShapes.infoCard)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = product.name.take(1).ifBlank { "?" }.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = titleColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.currency} ${product.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            trailingContent()
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
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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

            // Informacion basica
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
                label = { Text("Descripcion (opcional)") },
                placeholder = { Text("Ej: Elige tu plato principal") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Configuracion de seleccion
            Text(
                "Configuracion",
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
                    label = { Text("Minimo") },
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
                    label = { Text("Maximo") },
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

            // Seleccion de productos
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
                    CompactProductSelectionCard(
                        product = product,
                        selected = isSelected,
                        onClick = {
                            selectedProducts = if (isSelected) {
                                selectedProducts - product.id
                            } else {
                                selectedProducts + product.id
                            }
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    checkmarkColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onPrimary
                                    },
                                    uncheckedColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            )
                        }
                    )
                }

                if (filteredProducts.size > 5) {
                    Text(
                        "Y ${filteredProducts.size - 5} productos mas...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Botones de accion
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
                        val minSelectionValue = minSel.toIntOrNull() ?: if (isRequired) 1 else 0
                        val defaultCount = minSelectionValue.coerceIn(0, selectedProducts.size)
                        val newSlot = SlotEdit(
                            name = slotName,
                            description = slotDescription.takeIf { it.isNotBlank() },
                            minSelections = minSelectionValue,
                            maxSelections = maxSel.toIntOrNull() ?: 1,
                            isRequired = isRequired,
                            options = selectedProducts.mapIndexed { index, productId ->
                                val product = availableProducts.find { it.id == productId }
                                OptionEdit(
                                    productId = productId,
                                    productName = product?.name ?: "",
                                    isDefault = index < defaultCount,
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
        border = if (isExpanded) {
            BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    slot.description?.takeIf { it.isNotBlank() }?.let { slotDescription ->
                        Text(
                            slotDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        if (slot.isRequired) "Obligatorio" else "Opcional",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (slot.isRequired) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider()

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
                                Text(
                                    option.productName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Checkbox(
                                        checked = option.isDefault,
                                        onCheckedChange = { checked ->
                                            option.isDefault = checked
                                            onUpdate()
                                        }
                                    )
                                    Text(
                                        "Por defecto",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            option.priceAdjustment -= 0.5
                                            onUpdate()
                                        }
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Reducir ajuste")
                                    }
                                    Text(
                                        formatPriceLocal(option.priceAdjustment),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    IconButton(
                                        onClick = {
                                            option.priceAdjustment += 0.5
                                            onUpdate()
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Aumentar ajuste")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    "Agregar mas productos",
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
                    CompactProductSelectionCard(
                        product = product,
                        selected = false,
                        onClick = {
                            val defaultCount = slot.options.count { it.isDefault }
                            slot.options.add(
                                OptionEdit(
                                    productId = product.id,
                                    productName = product.name,
                                    isDefault = defaultCount < slot.minSelections,
                                    priceAdjustment = 0.0
                                )
                            )
                            onUpdate()
                        },
                        trailingContent = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                if (availableToAdd.size > 3) {
                    Text(
                        "Y ${availableToAdd.size - 3} productos mas disponibles...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

private fun normalizeDiscountValue(discountType: String, rawValue: String): Double? {
    if (discountType == "NONE") return 0.0
    val parsed = rawValue.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return parsed
}

private fun validateComboForm(
    comboName: String,
    discountType: String,
    discountValue: Double?,
    slots: List<SlotEdit>,
    availableProducts: List<Product>
): String? {
    if (comboName.isBlank()) return "El nombre del combo es obligatorio"
    if (slots.isEmpty()) return "Debes agregar al menos un slot"
    if (discountValue == null) return "Valor de descuento invalido"

    when (discountType) {
        "NONE" -> if (discountValue != 0.0) return "Con descuento NONE el valor debe ser 0"
        "PERCENTAGE" -> if (discountValue < 0.0 || discountValue > 100.0) {
            return "El descuento porcentual debe estar entre 0 y 100"
        }
        "FIXED" -> if (discountValue < 0.0) {
            return "El descuento fijo no puede ser negativo"
        }
        else -> return "Tipo de descuento no soportado"
    }

    val productCurrencyMap = availableProducts.associate { it.id to it.currency.uppercase() }
    val comboCurrencies = mutableSetOf<String>()

    slots.forEachIndexed { index, slot ->
        if (slot.name.isBlank()) return "El slot ${index + 1} debe tener nombre"
        if (slot.minSelections < 0) return "El minimo de selecciones en ${slot.name} no puede ser negativo"
        if (slot.maxSelections <= 0) return "El maximo de selecciones en ${slot.name} debe ser mayor que 0"
        if (slot.minSelections > slot.maxSelections) return "En ${slot.name}, minimo no puede ser mayor que maximo"
        if (slot.isRequired && slot.minSelections < 1) return "El slot ${slot.name} es obligatorio y debe permitir al menos 1 seleccion"
        if (slot.options.isEmpty()) return "El slot ${slot.name} debe tener al menos una opcion"
        if (slot.maxSelections > slot.options.size) {
            return "En ${slot.name}, maximo no puede exceder la cantidad de opciones"
        }

        val duplicatedProduct = slot.options
            .groupBy { it.productId }
            .entries
            .firstOrNull { it.key.isBlank() || it.value.size > 1 }
        if (duplicatedProduct != null) {
            return if (duplicatedProduct.key.isBlank()) {
                "Hay una opcion sin producto en ${slot.name}"
            } else {
                "No se permite repetir el producto en el slot ${slot.name}"
            }
        }

        val defaultsCount = slot.options.count { it.isDefault }
        if (defaultsCount < slot.minSelections) {
            return "En ${slot.name}, opciones por defecto insuficientes para el minimo requerido"
        }
        if (defaultsCount > slot.maxSelections) {
            return "En ${slot.name}, opciones por defecto exceden el maximo permitido"
        }

        slot.options.forEach { option ->
            if (!option.priceAdjustment.isFinite()) {
                return "Ajuste de precio invalido en ${slot.name}"
            }
            productCurrencyMap[option.productId]?.let { comboCurrencies.add(it) }
        }
    }

    if (comboCurrencies.size > 1) {
        return "Todos los productos del combo deben tener la misma moneda"
    }

    return null
}

private fun formatPriceLocal(price: Double): String {
    val rounded = (price * 100).toInt() / 100.0
    val intPart = rounded.toInt()
    val decimalPart = ((rounded - intPart) * 100).toInt()
    return "${intPart}.${decimalPart.toString().padStart(2, '0')}"
}

