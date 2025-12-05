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
 * Card de producto genérico unificado - Diseño limpio y minimalista
 * Optimizado para mejor experiencia de usuario móvil
 */
@Composable
fun ProductCard(
    product: Product,
    businessType: BusinessType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier,
    onViewDetail: (() -> Unit)? = null  // Nueva callback para ver detalles
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewDetail ?: onEdit)  // Si hay onViewDetail, usar eso, sino usar onEdit
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Imagen del producto - más pequeña y limpia
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUrl.isNotEmpty()) {
                        NetworkImage(
                            url = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Si no tiene URL personalizada, usar imagen de recursos por defecto
                        androidx.compose.foundation.Image(
                            painter = painterResource(getProductImage(product.id)),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Información del producto - compacta
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 36.dp), // Espacio para el botón de editar
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Nombre del producto
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (!product.isAvailable)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )

                    // Precio
                    Text(
                        text = product.getDisplayPrice(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Variedades/modificadores (si existen)
                    if (product.varieties.isNotEmpty()) {
                        Text(
                            text = product.varieties.take(3).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Indicador sutil de disponibilidad (solo si no está disponible)
                    if (!product.isAvailable) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            )
                            Text(
                                text = "No disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }

            // Botón de editar - sutil en la esquina superior derecha
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
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
