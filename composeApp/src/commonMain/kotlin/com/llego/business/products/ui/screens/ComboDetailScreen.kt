package com.llego.business.products.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.llego.shared.data.model.Combo
import com.llego.shared.ui.theme.LlegoCustomShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboDetailScreen(
    combo: Combo,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // Solo usar imageUrl (imagen propia del combo), no usar combo.image como fallback
    val imageUrl = combo.imageUrl

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del combo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Imagen",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "COMBO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = combo.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(LlegoCustomShapes.infoCard),
                            contentScale = ContentScale.Crop
                        )
                    } else if (combo.representativeProducts.isNotEmpty()) {
                        // Mostrar composición de productos como iconitos redondos
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(LlegoCustomShapes.infoCard)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-16).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                combo.representativeProducts.take(4).forEachIndexed { index, product ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(4.dp)
                                    ) {
                                        if (product.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = product.imageUrl,
                                                contentDescription = product.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(androidx.compose.foundation.shape.CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = product.name.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (combo.representativeProducts.size > 4) {
                                Text(
                                    text = "+${combo.representativeProducts.size - 4}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = LlegoCustomShapes.infoCard
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    "Sin imagen",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Información básica
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    DetailRow(label = "Nombre", value = combo.name)
                    
                    if (combo.description.isNotBlank()) {
                        DetailRow(label = "Descripcion", value = combo.description)
                    }

                    DetailRow(
                        label = "Disponibilidad",
                        value = if (combo.availability) "Disponible" else "No disponible"
                    )
                }
            }

            // Precios y descuento
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
                        text = "Precios",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (combo.basePrice != combo.finalPrice) {
                        DetailRow(
                            label = "Precio base",
                            value = formatPrice(combo.basePrice),
                            valueStyle = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    DetailRow(
                        label = "Precio final",
                        value = formatPrice(combo.finalPrice),
                        valueColor = MaterialTheme.colorScheme.primary,
                        valueStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if (combo.savings > 0) {
                        DetailRow(
                            label = "Ahorro",
                            value = formatPrice(combo.savings),
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            valueStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    HorizontalDivider()

                    DetailRow(
                        label = "Tipo de descuento",
                        value = when (combo.discountType) {
                            com.llego.shared.data.model.DiscountType.NONE ->
                                "Sin descuento"
                            com.llego.shared.data.model.DiscountType.PERCENTAGE -> 
                                "${combo.discountValue.toInt()}% de descuento"
                            com.llego.shared.data.model.DiscountType.FIXED -> 
                                "Descuento fijo de ${formatPrice(combo.discountValue)}"
                        }
                    )
                }
            }

            // Slots
            if (combo.slots.isNotEmpty()) {
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
                            text = "Opciones del combo",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        combo.slots.forEach { slot ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider()
                                
                                Text(
                                    slot.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    "Selecciona ${slot.minSelections} - ${slot.maxSelections} opciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                slot.options.forEach { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "•",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    option.product?.name ?: "Producto",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            if (option.isDefault) {
                                                Text(
                                                    "  Por defecto",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        if (option.priceAdjustment != 0.0) {
                                            Text(
                                                "+${formatPrice(option.priceAdjustment)}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = valueStyle.copy(color = valueColor),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}


private fun formatPrice(price: Double): String {
    val rounded = (price * 100).toInt() / 100.0
    val intPart = rounded.toInt()
    val decimalPart = ((rounded - intPart) * 100).toInt()
    return "${intPart}.${decimalPart.toString().padStart(2, '0')}"
}
