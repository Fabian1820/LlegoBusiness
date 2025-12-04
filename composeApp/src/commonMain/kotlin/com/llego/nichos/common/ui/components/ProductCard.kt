package com.llego.nichos.common.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.nichos.common.data.model.*
import com.llego.nichos.common.utils.getProductImage
import com.llego.nichos.common.utils.getCategoryDisplayName
import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.shared.data.model.BusinessType
import llegobusiness.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Card de producto genérico unificado basado en el diseño de MenuItemCard
 * Se adapta según el tipo de negocio mostrando atributos específicos del nicho
 */
@Composable
fun ProductCard(
    product: Product,
    businessType: BusinessType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isAvailable) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del producto con diseño premium (reutiliza imágenes de restaurante)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false
                    )
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(getProductImage(product.id)),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )

                // Overlay con gradiente sutil si no está disponible
                if (!product.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "No disponible",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Información del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nombre
                AnimatedTextWithUnderline(
                    text = product.name,
                    isUnavailable = !product.isAvailable,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Badge de categoría (obtiene el nombre correcto según el businessType)
                val categoryDisplayName = getCategoryDisplayNameForProduct(product.category, businessType)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = categoryDisplayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Descripción
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Atributos específicos por nicho
                when (businessType) {
                    BusinessType.RESTAURANT -> {
                        RestaurantProductAttributes(product)
                    }
                    BusinessType.MARKET, BusinessType.AGROMARKET -> {
                        MarketProductAttributes(product)
                    }
                    BusinessType.CLOTHING_STORE -> {
                        ClothingProductAttributes(product)
                    }
                    BusinessType.PHARMACY -> {
                        PharmacyProductAttributes(product)
                    }
                }

                // Precio y tiempo/stock con fondo
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.getDisplayPrice(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        when (businessType) {
                            BusinessType.RESTAURANT -> {
                                if (product.preparationTime != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Timer,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "${product.preparationTime} min",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                            BusinessType.MARKET, BusinessType.AGROMARKET -> {
                                if (product.stock != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Inventory,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "Stock: ${product.stock}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }

                // Estado de disponibilidad y botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (product.isAvailable) Color(0xFF4CAF50)
                                    else Color(0xFFE53935)
                                )
                        )
                        Text(
                            text = if (product.isAvailable) "Disponible" else "No disponible",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                            ),
                            color = if (product.isAvailable) Color(0xFF4CAF50)
                            else Color(0xFFE53935)
                        )
                    }

                    // Botones de acción
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Toggle disponibilidad
                        IconButton(
                            onClick = onToggleAvailability,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (product.isAvailable)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Cambiar disponibilidad",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Editar
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Eliminar
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Atributos específicos para restaurante
 */
@Composable
private fun RestaurantProductAttributes(product: Product) {
    // No se muestran atributos adicionales aquí, el tiempo de preparación va en el precio
}

/**
 * Atributos específicos para mercado/agromercado
 */
@Composable
private fun MarketProductAttributes(product: Product) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marca
        if (product.brand != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = product.brand,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
        // Unidad
        if (product.unit != null && product.unit != ProductUnit.UNIT) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = product.unit.getDisplayName(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

/**
 * Atributos específicos para tienda de ropa
 */
@Composable
private fun ClothingProductAttributes(product: Product) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Tallas disponibles
        if (!product.sizes.isNullOrEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tallas:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                )
                product.sizes.take(5).forEach { size ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = size,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                if (product.sizes.size > 5) {
                    Text(
                        text = "+${product.sizes.size - 5}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        // Colores disponibles
        if (!product.colors.isNullOrEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Colores:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                )
                product.colors.take(6).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(color.hexCode))
                            .border(
                                width = 1.dp,
                                color = Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
                if (product.colors.size > 6) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${product.colors.size - 6}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Género
        if (product.gender != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = product.gender.getDisplayName(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

/**
 * Atributos específicos para farmacia
 */
@Composable
private fun PharmacyProductAttributes(product: Product) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Nombre genérico
        if (product.genericName != null) {
            Text(
                text = "Genérico: ${product.genericName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
        }

        // Requiere receta
        if (product.requiresPrescription) {
            Surface(
                color = Color(0xFFFF6B6B).copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                    Text(
                        text = "Requiere receta",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
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
 * Obtiene el nombre de categoría para mostrar en el card
 */
fun getCategoryDisplayNameForProduct(category: String, businessType: BusinessType): String {
    val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
    val categoryLower = category.lowercase()
    
    // 1. Buscar coincidencia exacta por displayName
    val exactMatch = categories.find { 
        it.displayName.equals(category, ignoreCase = true)
    }
    if (exactMatch != null) {
        return exactMatch.displayName
    }
    
    // 2. Buscar coincidencia por ID
    val idMatch = categories.find { 
        it.id.equals(category, ignoreCase = true) ||
        it.id.replace("_", " ").equals(categoryLower, ignoreCase = true)
    }
    if (idMatch != null) {
        return idMatch.displayName
    }
    
    // 3. Intentar mapear desde el texto de la categoría al ID
    val categoryId = com.llego.nichos.common.utils.mapToCategoryId(category, businessType)
    if (categoryId != null) {
        return categories.find { it.id == categoryId }?.displayName ?: category
    }
    
    // 4. Buscar coincidencia parcial (contiene)
    val partialMatch = categories.find { 
        categoryLower.contains(it.displayName.lowercase()) ||
        it.displayName.lowercase().contains(categoryLower)
    }
    if (partialMatch != null) {
        return partialMatch.displayName
    }
    
    // 5. Fallback: usar la categoría tal cual (puede ser un nombre válido)
    return category
}

/**
 * Texto animado con subrayado para productos no disponibles
 */
@Composable
private fun AnimatedTextWithUnderline(
    text: String,
    isUnavailable: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isUnavailable) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseOutCubic
        ),
        label = "underline_animation"
    )

    Text(
        text = text,
        style = style.copy(
            textDecoration = if (isUnavailable) TextDecoration.LineThrough else TextDecoration.None,
            color = if (isUnavailable) {
                MaterialTheme.colorScheme.error.copy(alpha = animatedAlpha)
            } else {
                style.color ?: MaterialTheme.colorScheme.onSurface
            }
        ),
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}
