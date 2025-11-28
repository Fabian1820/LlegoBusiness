package com.llego.nichos.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // Gradiente derecho
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF5F5F5)),
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
                label = { Text("Todos") },
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
                        style = MaterialTheme.typography.labelMedium
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

/**
 * Variante compatible con MenuCategory (para Restaurant)
 * Wrapper que convierte MenuCategory al sistema genérico de categorías
 */
@Composable
fun CategoryFilterChipsForRestaurant(
    businessType: BusinessType = BusinessType.RESTAURANT,
    selectedCategory: MenuCategory?,
    onCategorySelected: (MenuCategory) -> Unit,
    onClearCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryFilterChips(
        businessType = businessType,
        selectedCategoryId = selectedCategory?.toCategoryId(),
        onCategorySelected = { categoryId ->
            categoryId.toMenuCategory()?.let { onCategorySelected(it) }
        },
        onClearCategory = onClearCategory,
        modifier = modifier
    )
}
