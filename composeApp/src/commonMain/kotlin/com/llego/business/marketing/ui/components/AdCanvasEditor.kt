package com.llego.business.marketing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.llego.business.marketing.util.toPngBytes
import com.llego.business.shared.ui.components.rememberImagePickerController
import com.llego.shared.data.model.AdPlacement
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Una capa de texto arrastrable dentro del lienzo. */
private data class TextLayer(
    val id: Long,
    val text: String,
    val offset: Offset,
    val colorHex: String,
    val fontSize: Float
)

private val TEXT_COLORS = listOf(
    "#FFFFFF", "#000000", "#FFD54F", "#FF7043", "#E53935", "#43A047", "#1E88E5", "#8E24AA"
)

/**
 * Editor estilo Canva: el negocio elige una imagen de fondo (obligatoria), arrastra
 * el avatar circular y capas de texto donde quiera, y al continuar el lienzo se
 * captura como una sola foto PNG (bytes) que el llamador sube como creativo.
 */
@Composable
fun AdCanvasEditor(
    businessAvatarUrl: String?,
    name: String,
    onName: (String) -> Unit,
    placement: String,
    onPlacement: (String) -> Unit,
    isBusy: Boolean,
    error: String?,
    onExported: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val picker = rememberImagePickerController()

    var backgroundUri by remember { mutableStateOf<String?>(null) }
    var avatarVisible by remember { mutableStateOf(businessAvatarUrl != null) }
    var avatarOffset by remember { mutableStateOf(Offset(24f, 24f)) }
    var textLayers by remember { mutableStateOf(listOf<TextLayer>()) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var nextId by remember { mutableStateOf(1L) }

    val selected = textLayers.firstOrNull { it.id == selectedId }

    fun updateSelected(transform: (TextLayer) -> TextLayer) {
        textLayers = textLayers.map { if (it.id == selectedId) transform(it) else it }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // -- Lienzo (WYSIWYG, lo que se exporta) --
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF15202B))
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
                .pointerInput(Unit) {
                    detectTapGestures { selectedId = null }
                }
        ) {
            if (backgroundUri != null) {
                AsyncImage(
                    model = backgroundUri,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Elige una imagen de fondo para empezar",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // Capas de texto
            textLayers.forEach { layer ->
                DraggableLayer(
                    offset = layer.offset,
                    selected = selectedId == layer.id,
                    onSelect = { selectedId = layer.id },
                    onDrag = { d ->
                        textLayers = textLayers.map {
                            if (it.id == layer.id) it.copy(offset = it.offset + d) else it
                        }
                    }
                ) {
                    Text(
                        layer.text.ifBlank { "Texto" },
                        color = hexColor(layer.colorHex),
                        fontSize = layer.fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Avatar circular del negocio
            if (avatarVisible && businessAvatarUrl != null) {
                DraggableLayer(
                    offset = avatarOffset,
                    selected = false,
                    onSelect = { selectedId = null },
                    onDrag = { d -> avatarOffset += d }
                ) {
                    AsyncImage(
                        model = businessAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -- Herramientas --
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { picker.pickImage { uri -> backgroundUri = uri } },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (backgroundUri == null) "Elegir fondo" else "Cambiar fondo")
            }
            OutlinedButton(
                onClick = {
                    val id = nextId
                    nextId += 1
                    textLayers = textLayers + TextLayer(
                        id = id,
                        text = "Tu texto",
                        offset = Offset(40f, 120f),
                        colorHex = "#FFFFFF",
                        fontSize = 22f
                    )
                    selectedId = id
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(4.dp))
                Text("Texto")
            }
        }

        if (businessAvatarUrl != null) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mostrar avatar del negocio", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = avatarVisible, onCheckedChange = { avatarVisible = it })
            }
        }

        // -- Edición de la capa de texto seleccionada --
        if (selected != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    OutlinedTextField(
                        value = selected.text,
                        onValueChange = { v -> updateSelected { it.copy(text = v) } },
                        label = { Text("Texto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TEXT_COLORS.forEach { hex ->
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(hexColor(hex))
                                    .border(
                                        width = if (selected.colorHex == hex) 3.dp else 1.dp,
                                        color = if (selected.colorHex == hex) MaterialTheme.colorScheme.primary
                                        else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { updateSelected { it.copy(colorHex = hex) } }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tamaño", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    updateSelected { it.copy(fontSize = (it.fontSize - 2f).coerceAtLeast(10f)) }
                                }
                            ) { Text("A-") }
                            Spacer(Modifier.size(6.dp))
                            Text("${selected.fontSize.roundToInt()}")
                            Spacer(Modifier.size(6.dp))
                            OutlinedButton(
                                onClick = {
                                    updateSelected { it.copy(fontSize = (it.fontSize + 2f).coerceAtMost(64f)) }
                                }
                            ) { Text("A+") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            textLayers = textLayers.filterNot { it.id == selectedId }
                            selectedId = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Eliminar texto")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -- Datos de la campaña --
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            label = { Text("Nombre interno") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Text("Tipo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditorChip("Negocio Destacado", placement == AdPlacement.DESTACADO) {
                onPlacement(AdPlacement.DESTACADO)
            }
            EditorChip("Oferta", placement == AdPlacement.OFERTA) {
                onPlacement(AdPlacement.OFERTA)
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    val bitmap = graphicsLayer.toImageBitmap()
                    onExported(bitmap.toPngBytes())
                }
            },
            enabled = backgroundUri != null && name.isNotBlank() && !isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continuar")
            }
        }
        if (backgroundUri == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Necesitas una imagen de fondo para continuar.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DraggableLayer(
    offset: Offset,
    selected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Offset) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .then(
                if (selected) Modifier.border(1.dp, Color.White, RoundedCornerShape(6.dp))
                else Modifier
            )
            .padding(4.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { onSelect() }
            }
    ) {
        content()
    }
}

@Composable
private fun EditorChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/** Convierte un hex (#RRGGBB / #AARRGGBB / #RGB) a Color. */
fun hexColor(hex: String): Color = try {
    val c = hex.removePrefix("#")
    when (c.length) {
        6 -> Color(("FF$c").toLong(16))
        8 -> Color(c.toLong(16))
        3 -> {
            val r = c[0]; val g = c[1]; val b = c[2]
            Color("FF$r$r$g$g$b$b".toLong(16))
        }
        else -> Color.Gray
    }
} catch (e: Exception) {
    Color.Gray
}
