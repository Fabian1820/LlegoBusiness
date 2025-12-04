package com.llego.nichos.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.config.BusinessCategoryConfig
import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.shared.data.model.BusinessType

/**
 * Mapper para convertir MenuCategory a category ID genérico
 */
private fun MenuCategory.toCategoryId(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "entradas"
        MenuCategory.MAIN_COURSES -> "platos_fuertes"
        MenuCategory.DESSERTS -> "postres"
        MenuCategory.SIDES -> "agregos"
        MenuCategory.BEVERAGES -> "bebidas"
        MenuCategory.SPECIALS -> "especiales"
    }
}

/**
 * Mapper para convertir category ID genérico a MenuCategory
 */
private fun String.toMenuCategory(): MenuCategory? {
    return when (this) {
        "entradas" -> MenuCategory.APPETIZERS
        "platos_fuertes" -> MenuCategory.MAIN_COURSES
        "postres" -> MenuCategory.DESSERTS
        "agregos" -> MenuCategory.SIDES
        "bebidas" -> MenuCategory.BEVERAGES
        "especiales" -> MenuCategory.SPECIALS
        else -> null
    }
}

/**
 * Componente genérico de filtros de categorías adaptado por tipo de negocio
 * Diseño igual al filtro de pedidos (StatusFilterChips)
 */
@Composable
fun CategoryFilterChips(
    businessType: BusinessType,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    onClearCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
    val listState = rememberLazyListState()
    
    // Scroll automático cuando se selecciona una categoría
    LaunchedEffect(selectedCategoryId) {
        if (selectedCategoryId != null) {
            val index = categories.indexOfFirst { it.id == selectedCategoryId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        } else {
            // Si se deselecciona, scroll al inicio
            listState.animateScrollToItem(0)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        // Fade izquierdo
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 60f
                            )
                        )
                        // Fade derecho
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White
                                ),
                                startX = size.width - 80f,
                                endX = size.width
                            )
                        )
                    },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip "Todos"
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = onClearCategory,
                        label = {
                            Text(
                                text = "Todos",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = if (selectedCategoryId == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Chips de categorías
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        label = {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedCategoryId == category.id) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = if (selectedCategoryId == category.id) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Variante compatible con MenuCategory (para Restaurant) y String (para otros nichos)
 * Wrapper que convierte MenuCategory al sistema genérico de categorías
 */
@Composable
fun CategoryFilterChipsForRestaurant(
    businessType: BusinessType = BusinessType.RESTAURANT,
    selectedCategory: MenuCategory?,
    selectedCategoryId: String? = null,
    onCategorySelected: ((MenuCategory) -> Unit)? = null,
    onCategoryIdSelected: ((String) -> Unit)? = null,
    onClearCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveCategoryId = if (businessType == BusinessType.RESTAURANT) {
        selectedCategory?.toCategoryId()
    } else {
        selectedCategoryId
    }
    
    CategoryFilterChips(
        businessType = businessType,
        selectedCategoryId = effectiveCategoryId,
        onCategorySelected = { categoryId ->
            if (businessType == BusinessType.RESTAURANT) {
                categoryId.toMenuCategory()?.let { onCategorySelected?.invoke(it) }
            } else {
                onCategoryIdSelected?.invoke(categoryId)
            }
        },
        onClearCategory = onClearCategory,
        modifier = modifier
    )
}
