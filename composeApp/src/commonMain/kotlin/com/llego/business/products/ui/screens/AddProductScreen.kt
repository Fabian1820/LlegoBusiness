package com.llego.business.products.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.VariantList
import com.llego.shared.data.model.VariantOptionDraft
import com.llego.shared.data.model.extractFilename
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.CurrencySelector
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Datos normalizados del formulario de producto.
 *
 * @property name Nombre del producto (requerido)
 * @property description Descripción del producto (requerido)
 * @property price Precio del producto (requerido)
 * @property categoryId ID de la categoría del producto (opcional)
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
    val availability: Boolean,
    val variantListIds: List<String>
)

private data class VariantOptionEditorState(
    val id: String? = null,
    val name: String = "",
    val priceAdjustment: String = "0"
)

/**
 * Pantalla fullscreen para crear o editar productos usando el modelo GraphQL canonico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    branchId: String?,
    categories: List<ProductCategory> = emptyList(),
    variantLists: List<VariantList> = emptyList(),
    variantListsLoading: Boolean = false,
    variantListsError: String? = null,
    isSaving: Boolean = false,
    onNavigateBack: () -> Unit,
    onSave: (ProductFormData) -> Unit,
    onCreateVariantList: suspend (
        branchId: String,
        name: String,
        description: String?,
        options: List<VariantOptionDraft>
    ) -> Result<VariantList> = { _, _, _, _ ->
        Result.failure(Exception("No se pudo crear la lista de variantes"))
    },
    existingProduct: Product? = null,
    modifier: Modifier = Modifier
) {
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    val formKey = existingProduct?.id ?: "new:${branchId ?: "none"}"

    var name by remember(formKey) { mutableStateOf(existingProduct?.name ?: "") }
    var description by remember(formKey) { mutableStateOf(existingProduct?.description ?: "") }
    var price by remember(formKey) { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var weight by remember(formKey) { mutableStateOf(existingProduct?.weight ?: "") }
    var selectedCategoryId by remember(formKey) { mutableStateOf(existingProduct?.categoryId) }
    var currency by remember(formKey) { mutableStateOf(existingProduct?.currency ?: "USD") }
    var isAvailable by remember(formKey) { mutableStateOf(existingProduct?.availability ?: true) }
    var selectedVariantListIds by remember(formKey) {
        mutableStateOf(existingProduct?.variantListIds ?: emptyList())
    }

    var showCategoryDropdown by remember(formKey) { mutableStateOf(false) }
    var showVariantEditor by remember(formKey) { mutableStateOf(false) }
    var variantOperationLoading by remember(formKey) { mutableStateOf(false) }
    var variantFeedback by remember(formKey) { mutableStateOf<String?>(null) }
    var variantEditorMessage by remember(formKey) { mutableStateOf<String?>(null) }
    var variantName by remember(formKey) { mutableStateOf("") }
    var variantDescription by remember(formKey) { mutableStateOf("") }
    var variantOptions by remember(formKey) { mutableStateOf(listOf(VariantOptionEditorState())) }

    val initialImageState = remember(formKey, existingProduct?.image, existingProduct?.imageUrl) {
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
    var productImageState by remember(formKey) { mutableStateOf<ImageUploadState>(initialImageState) }

    val imagePath = (productImageState as? ImageUploadState.Success)?.s3Path
    val priceValue = price.toDoubleOrNull()
    val isSaveEnabled = branchId != null &&
        name.isNotBlank() &&
        priceValue != null &&
        currency.isNotBlank() &&
        !imagePath.isNullOrBlank() &&
        !isSaving
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )

    LaunchedEffect(variantFeedback) {
        if (!variantFeedback.isNullOrBlank()) {
            delay(3000)
            variantFeedback = null
        }
    }

    fun openVariantEditor() {
        variantEditorMessage = null
        variantName = ""
        variantDescription = ""
        variantOptions = listOf(VariantOptionEditorState())
        showVariantEditor = true
    }

    fun validateVariantEditor(): String? {
        if (variantName.isBlank()) return "El nombre de la lista es obligatorio."
        if (variantOptions.isEmpty()) return "Debes agregar al menos una opción."
        if (variantOptions.any { it.name.isBlank() || it.priceAdjustment.toDoubleOrNull() == null }) {
            return "Cada opción necesita nombre y ajuste válido."
        }
        return null
    }

    fun saveVariantListFromDialog() {
        val currentBranchId = branchId
        if (currentBranchId.isNullOrBlank()) {
            variantEditorMessage = "Selecciona una sucursal antes de crear listas."
            return
        }

        val validationError = validateVariantEditor()
        if (validationError != null) {
            variantEditorMessage = validationError
            return
        }

        val normalizedOptions = variantOptions.map {
            VariantOptionDraft(
                id = it.id,
                name = it.name.trim(),
                priceAdjustment = it.priceAdjustment.toDoubleOrNull() ?: 0.0
            )
        }

        coroutineScope.launch {
            variantOperationLoading = true
            variantEditorMessage = null
            val result = onCreateVariantList(
                currentBranchId,
                variantName.trim(),
                variantDescription.trim().ifBlank { null },
                normalizedOptions
            )

            result.onSuccess { created ->
                selectedVariantListIds = (selectedVariantListIds + created.id).distinct()
                variantFeedback = "Lista \"${created.name}\" creada y seleccionada."
                showVariantEditor = false
            }.onFailure { throwable ->
                variantEditorMessage = throwable.message ?: "No se pudo crear la lista de variantes."
            }

            variantOperationLoading = false
        }
    }

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
                                        availability = isAvailable,
                                        variantListIds = selectedVariantListIds
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = isSaveEnabled,
                            shape = LlegoCustomShapes.primaryButton
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text("Guardando...")
                            } else {
                                Text("Guardar")
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
                    uploadFunction = imageUploadViewModel::uploadProductImage,
                    size = ImageUploadSize.LARGE,
                    showSuccessFileName = false,
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
                                value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria (opcional)") },
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
                                        text = { Text(category.name) },
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Listas de variantes (opcional)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "Selecciona listas existentes o crea una nueva.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = { openVariantEditor() },
                                enabled = !variantOperationLoading && !branchId.isNullOrBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Text("Nueva lista")
                            }
                        }

                        if (variantOperationLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Creando lista...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!variantFeedback.isNullOrBlank()) {
                            Text(
                                text = variantFeedback.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        when {
                            branchId.isNullOrBlank() -> {
                                Text(
                                    text = "Selecciona una sucursal para gestionar listas de variantes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            variantListsLoading -> {
                                Text(
                                    text = "Cargando listas de variantes...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            variantLists.isEmpty() -> {
                                Text(
                                    text = "No hay listas de variantes creadas en esta sucursal.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                variantLists.forEach { variantList ->
                                    val isSelected = selectedVariantListIds.contains(variantList.id)
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = LlegoCustomShapes.infoCard,
                                        tonalElevation = if (isSelected) 1.dp else 0.dp,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = variantList.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                                val optionsSummary = if (variantList.options.isEmpty()) {
                                                    "Sin opciones"
                                                } else {
                                                    variantList.options.joinToString(", ") { option ->
                                                        formatVariantOptionSummary(
                                                            option.name,
                                                            option.priceAdjustment
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = optionsSummary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Switch(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    selectedVariantListIds = if (checked) {
                                                        (selectedVariantListIds + variantList.id).distinct()
                                                    } else {
                                                        selectedVariantListIds.filterNot { it == variantList.id }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        variantListsError?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
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

    if (showVariantEditor) {
        AlertDialog(
            onDismissRequest = {
                if (!variantOperationLoading) {
                    variantEditorMessage = null
                    showVariantEditor = false
                }
            },
            title = { Text("Nueva lista de variantes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!variantEditorMessage.isNullOrBlank()) {
                        Text(
                            text = variantEditorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = variantName,
                        onValueChange = { variantName = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !variantOperationLoading
                    )

                    OutlinedTextField(
                        value = variantDescription,
                        onValueChange = { variantDescription = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        enabled = !variantOperationLoading
                    )

                    Text(
                        text = "Opciones",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )

                    variantOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = option.name,
                                onValueChange = { value ->
                                    variantOptions = variantOptions.mapIndexed { optionIndex, current ->
                                        if (optionIndex == index) current.copy(name = value) else current
                                    }
                                },
                                label = { Text("Nombre") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !variantOperationLoading
                            )

                            OutlinedTextField(
                                value = option.priceAdjustment,
                                onValueChange = { value ->
                                    if (value.isEmpty() || value.toDoubleOrNull() != null) {
                                        variantOptions = variantOptions.mapIndexed { optionIndex, current ->
                                            if (optionIndex == index) current.copy(priceAdjustment = value) else current
                                        }
                                    }
                                },
                                label = { Text("Ajuste") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                enabled = !variantOperationLoading
                            )

                            IconButton(
                                onClick = {
                                    if (variantOptions.size > 1) {
                                        variantOptions = variantOptions.filterIndexed { optionIndex, _ ->
                                            optionIndex != index
                                        }
                                    }
                                },
                                enabled = !variantOperationLoading && variantOptions.size > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar opción"
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { variantOptions = variantOptions + VariantOptionEditorState() },
                        enabled = !variantOperationLoading
                    ) {
                        Text("Agregar opción")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveVariantListFromDialog() },
                    enabled = !variantOperationLoading
                ) {
                    if (variantOperationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp).height(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Crear lista")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        variantEditorMessage = null
                        showVariantEditor = false
                    },
                    enabled = !variantOperationLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatVariantOptionSummary(name: String, priceAdjustment: Double): String {
    if (priceAdjustment == 0.0) return name
    val sign = if (priceAdjustment > 0) "+" else "-"
    return "$name ($sign${formatDecimal(abs(priceAdjustment))})"
}

private fun formatDecimal(value: Double): String {
    val rounded = (value * 100).toInt() / 100.0
    val intPart = rounded.toInt()
    val decimalPart = ((rounded - intPart) * 100).toInt()
    return "${intPart}.${decimalPart.toString().padStart(2, '0')}"
}
