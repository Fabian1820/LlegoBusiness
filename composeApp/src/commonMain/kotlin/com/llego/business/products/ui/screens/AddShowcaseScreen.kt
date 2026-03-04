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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.products.ui.viewmodel.ShowcaseViewModel
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.ShowcaseDetectionResult
import com.llego.shared.data.model.ShowcaseItem
import com.llego.shared.data.model.ShowcasesResult
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import kotlinx.coroutines.launch

private data class EditableShowcaseItem(
    val id: String? = null,
    var name: String = "",
    var description: String = "",
    var price: String = "",
    var availability: Boolean = true
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddShowcaseScreen(
    viewModel: ShowcaseViewModel,
    branchId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showcaseImageState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isSaving by remember { mutableStateOf(false) }
    var isDetecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detectionMessage by remember { mutableStateOf<String?>(null) }
    var lastDetectedImagePath by remember { mutableStateOf<String?>(null) }

    val items = remember { mutableStateListOf<EditableShowcaseItem>() }

    val imagePath = (showcaseImageState as? ImageUploadState.Success)?.s3Path
    val localImagePath = (showcaseImageState as? ImageUploadState.Success)?.localUri

    val canSave = branchId != null &&
        title.isNotBlank() &&
        !imagePath.isNullOrBlank() &&
        !isSaving &&
        !isDetecting

    fun runAutomaticDetection(filePath: String) {
        scope.launch {
            isDetecting = true
            detectionMessage = null
            when (val result = viewModel.detectProductsFromShowcase(filePath)) {
                is ShowcaseDetectionResult.Success -> {
                    items.clear()
                    items.addAll(
                        result.products.map { product ->
                            EditableShowcaseItem(
                                name = product.name,
                                description = product.description.orEmpty(),
                                price = product.price?.toString().orEmpty(),
                                availability = true
                            )
                        }
                    )
                    detectionMessage = if (result.products.isEmpty()) {
                        "No detectamos productos en la foto. Puedes agregarlos manualmente."
                    } else {
                        "Detectamos ${result.products.size} productos. Revisalos antes de publicar."
                    }
                }

                is ShowcaseDetectionResult.Error -> {
                    detectionMessage = "No pudimos analizar la foto automaticamente. Puedes agregar productos manualmente."
                }

                is ShowcaseDetectionResult.Loading -> Unit
                is ShowcaseDetectionResult.Idle -> Unit
            }
            isDetecting = false
        }
    }

    LaunchedEffect(localImagePath) {
        val filePath = localImagePath
        if (!filePath.isNullOrBlank() && filePath != lastDetectedImagePath) {
            lastDetectedImagePath = filePath
            runAutomaticDetection(filePath)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva vitrina",
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
                            shape = LlegoCustomShapes.secondaryButton,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                val normalizedItems = items
                                    .mapNotNull { item ->
                                        val normalizedName = item.name.trim()
                                        if (normalizedName.isBlank()) return@mapNotNull null
                                        ShowcaseItem(
                                            id = item.id,
                                            name = normalizedName,
                                            description = item.description.trim().ifBlank { null },
                                            price = item.price.toDoubleOrNull(),
                                            availability = item.availability
                                        )
                                    }
                                    .ifEmpty { null }

                                scope.launch {
                                    isSaving = true
                                    errorMessage = null
                                    val result = viewModel.createShowcase(
                                        branchId = branchId.orEmpty(),
                                        title = title.trim(),
                                        imagePath = imagePath.orEmpty(),
                                        description = description.trim().ifBlank { null },
                                        items = normalizedItems
                                    )
                                    when (result) {
                                        is ShowcasesResult.Success -> onNavigateBack()
                                        is ShowcasesResult.Error -> errorMessage = result.message
                                        is ShowcasesResult.Loading -> Unit
                                    }
                                    isSaving = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = canSave,
                            shape = LlegoCustomShapes.primaryButton
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Publicar vitrina")
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.infoCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Los pedidos de la vitrina pueden llegar sin precio fijo. Cuando te escriban, confirmas precio y detalles antes de cerrar la venta.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titulo de la vitrina *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripcion (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = LlegoCustomShapes.inputField
            )

            ImageUploadPreview(
                label = "Foto de vitrina",
                uploadState = showcaseImageState,
                onStateChange = { state ->
                    showcaseImageState = state
                    if (state !is ImageUploadState.Success) {
                        lastDetectedImagePath = null
                    }
                },
                uploadFunction = imageUploadViewModel::uploadShowcaseImage,
                size = ImageUploadSize.LARGE,
                showSuccessFileName = false,
                modifier = Modifier.fillMaxWidth()
            )

            if (isDetecting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Analizando la foto para sugerirte productos...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (!detectionMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = detectionMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos sugeridos (opcional)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                TextButton(onClick = { items.add(EditableShowcaseItem()) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Agregar")
                }
            }

            if (items.isEmpty()) {
                Text(
                    text = "Puedes publicar la vitrina sin productos y aceptar pedidos por descripcion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                text = "Producto ${index + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            IconButton(onClick = { items.removeAt(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar producto",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { item.name = it },
                            label = { Text("Nombre *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = LlegoCustomShapes.inputField
                        )

                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { item.description = it },
                            label = { Text("Descripcion") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = LlegoCustomShapes.inputField
                        )

                        OutlinedTextField(
                            value = item.price,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                    item.price = input
                                }
                            },
                            label = { Text("Precio (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = LlegoCustomShapes.inputField
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Disponible")
                            Switch(
                                checked = item.availability,
                                onCheckedChange = { item.availability = it }
                            )
                        }
                    }
                }
            }

            if (branchId == null) {
                Text(
                    text = "Selecciona una sucursal antes de crear la vitrina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
        }
    }
}

