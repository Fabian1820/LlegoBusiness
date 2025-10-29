package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuUiState
import llegobusiness.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Helper para obtener la imagen del producto basada en su ID
 * Mapea de forma consistente cada producto a una imagen
 */
@Composable
private fun getProductImage(menuItemId: String): org.jetbrains.compose.resources.DrawableResource {
    // Lista de imágenes disponibles
    val images = listOf(
        Res.drawable.pizza,
        Res.drawable.spaggetti,
        Res.drawable.arrozblanco,
        Res.drawable.arrozmoro,
        Res.drawable.pastelfresa,
        Res.drawable.tresleches,
        Res.drawable.batidofresa,
        Res.drawable.batidomamey
    )

    // Usar el hashCode del ID para obtener siempre la misma imagen para el mismo producto
    val index = menuItemId.hashCode().let { if (it < 0) -it else it } % images.size
    return images[index]
}

/**
 * Pantalla de Menú con diseño moderno y gestión CRUD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredMenuItems by viewModel.filteredMenuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<MenuItem?>(null) }
    var showSearchCard by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Lista de productos con filtros integrados
            when (uiState) {
                is MenuUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MenuUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = (uiState as MenuUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.loadMenuItems() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is MenuUiState.Success -> {
                    if (filteredMenuItems.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filtros de categoría
                            SimplifiedCategoryFilterChips(
                                selectedCategory = selectedCategory,
                                onCategorySelected = { viewModel.setCategory(it) },
                                onClearCategory = { viewModel.clearCategory() }
                            )
                            EmptyMenuView(
                                hasFilter = selectedCategory != null || searchQuery.isNotBlank(),
                                onAddProduct = {
                                    selectedMenuItem = null
                                    showAddEditDialog = true
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Filtros de categoría como primer item
                            item {
                                SimplifiedCategoryFilterChips(
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { viewModel.setCategory(it) },
                                    onClearCategory = { viewModel.clearCategory() }
                                )
                            }

                            // Lista de productos
                            items(
                                items = filteredMenuItems,
                                key = { it.id }
                            ) { menuItem ->
                                MenuItemCard(
                                    menuItem = menuItem,
                                    onEdit = {
                                        selectedMenuItem = menuItem
                                        showAddEditDialog = true
                                    },
                                    onDelete = {
                                        showDeleteConfirmation = menuItem
                                    },
                                    onToggleAvailability = {
                                        viewModel.toggleItemAvailability(menuItem.id)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Espacio adicional para los botones flotantes
                            item {
                                Spacer(modifier = Modifier.height(120.dp))
                            }
                        }
                    }
                }
            }
        }

        // Botones flotantes circulares (search arriba, add abajo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Botón de búsqueda - más pequeño y circular
            FloatingActionButton(
                onClick = { showSearchCard = true },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar productos",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Botón de agregar - más pequeño y circular
            FloatingActionButton(
                onClick = {
                    selectedMenuItem = null
                    showAddEditDialog = true
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar producto",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Card de búsqueda animado
        AnimatedVisibility(
            visible = showSearchCard,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            SearchCard(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onClose = {
                    showSearchCard = false
                    viewModel.clearSearch()
                },
                filteredMenuItems = filteredMenuItems,
                onEditItem = { item ->
                    selectedMenuItem = item
                    showAddEditDialog = true
                },
                onDeleteItem = { item ->
                    showDeleteConfirmation = item
                },
                onToggleAvailability = { itemId ->
                    viewModel.toggleItemAvailability(itemId)
                }
            )
        }
    }

    // Diálogo de agregar/editar con animación
    AnimatedVisibility(
        visible = showAddEditDialog,
        enter = scaleIn(
            animationSpec = tween(300, easing = EaseOutCubic),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(
            animationSpec = tween(200, easing = EaseInCubic),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(200))
    ) {
        AddEditMenuItemDialog(
            menuItem = selectedMenuItem,
            onDismiss = { showAddEditDialog = false },
            onSave = { item ->
                if (selectedMenuItem == null) {
                    viewModel.addMenuItem(item)
                } else {
                    viewModel.updateMenuItem(item)
                }
                showAddEditDialog = false
            }
        )
    }

    // Confirmación de eliminación con colores Llego
    showDeleteConfirmation?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "¿Eliminar producto?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar \"${item.name}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMenuItem(item.id)
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Chips de filtro por categoría simplificados con fade en ambos lados
 */
@Composable
private fun SimplifiedCategoryFilterChips(
    selectedCategory: MenuCategory?,
    onCategorySelected: (MenuCategory) -> Unit,
    onClearCategory: () -> Unit
) {
    val listState = rememberLazyListState()

    // Lista de categorías simplificadas
    val simplifiedCategories = listOf(
        MenuCategory.APPETIZERS,     // Entrantes
        MenuCategory.MAIN_COURSES,   // Platos Principales
        MenuCategory.DESSERTS,       // Postres
        MenuCategory.BEVERAGES,      // Bebidas
        MenuCategory.SPECIALS        // Sugerencias del Chef
    )


    Card(
        modifier = Modifier
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
                        selected = selectedCategory == null,
                        onClick = onClearCategory,
                        label = { Text("Todos") },
                        leadingIcon = if (selectedCategory == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Chips por categoría simplificada
                items(simplifiedCategories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category.getDisplayName(),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = if (selectedCategory == category) {
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
 * Card de búsqueda animado que aparece desde abajo con resultados en tiempo real
 */
@Composable
private fun SearchCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    filteredMenuItems: List<MenuItem> = emptyList(),
    onEditItem: (MenuItem) -> Unit = {},
    onDeleteItem: (MenuItem) -> Unit = {},
    onToggleAvailability: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .clickable(onClick = {}), // Prevenir cierre al hacer click en el card
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con título y botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buscar productos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar búsqueda",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Barra de búsqueda con colores Llego
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Buscar en el menú...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Ayuda o resultados de búsqueda
                if (searchQuery.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Escribe para buscar productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Busca por nombre, descripción o categoría",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    // Resultados de búsqueda
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header con contador de resultados
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${filteredMenuItems.size} producto${if (filteredMenuItems.size != 1) "s" else ""} encontrado${if (filteredMenuItems.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Lista de resultados
                        if (filteredMenuItems.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "No se encontraron productos",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Intenta con otras palabras clave",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = filteredMenuItems,
                                    key = { it.id }
                                ) { menuItem ->
                                    CompactMenuItemCard(
                                        menuItem = menuItem,
                                        onEdit = {
                                            onEditItem(menuItem)
                                            onClose()
                                        },
                                        onDelete = { onDeleteItem(menuItem) },
                                        onToggleAvailability = { onToggleAvailability(menuItem.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card compacto de producto para resultados de búsqueda
 */
@Composable
private fun CompactMenuItemCard(
    menuItem: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (menuItem.isAvailable) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen real del producto con diseño elegante
            androidx.compose.foundation.Image(
                painter = painterResource(getProductImage(menuItem.id)),
                contentDescription = menuItem.name,
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Información del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre con animación de subrayado
                AnimatedTextWithUnderline(
                    text = menuItem.name,
                    isUnavailable = !menuItem.isAvailable,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Categoría y precio
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de categoría
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (menuItem.category) {
                            MenuCategory.APPETIZERS, MenuCategory.MAIN_COURSES, MenuCategory.SPECIALS ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = menuItem.category.getDisplayName(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                            ),
                            color = when (menuItem.category) {
                                MenuCategory.APPETIZERS, MenuCategory.MAIN_COURSES, MenuCategory.SPECIALS ->
                                    MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }

                    // Precio
                    Text(
                        text = "$${menuItem.price}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle disponibilidad
                IconButton(
                    onClick = onToggleAvailability,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (menuItem.isAvailable)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (menuItem.isAvailable)
                            "Marcar como no disponible" else "Marcar como disponible",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Eliminar
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
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

/**
 * Card de producto del menú
 */
@Composable
private fun MenuItemCard(
    menuItem: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (menuItem.isAvailable) Color.White else Color(0xFFF5F5F5)
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
            // Imagen real del producto con diseño premium
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
                    painter = painterResource(getProductImage(menuItem.id)),
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )

                // Overlay con gradiente sutil si no está disponible
                if (!menuItem.isAvailable) {
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
                // Nombre y categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedTextWithUnderline(
                        text = menuItem.name,
                        isUnavailable = !menuItem.isAvailable,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Badge de categoría con colores Llego
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (menuItem.category) {
                        MenuCategory.APPETIZERS, MenuCategory.MAIN_COURSES, MenuCategory.SPECIALS ->
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when (menuItem.category) {
                            MenuCategory.APPETIZERS, MenuCategory.MAIN_COURSES, MenuCategory.SPECIALS ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Text(
                        text = menuItem.category.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = when (menuItem.category) {
                            MenuCategory.APPETIZERS, MenuCategory.MAIN_COURSES, MenuCategory.SPECIALS ->
                                MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }

                // Descripción
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Características dietéticas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (menuItem.isVegetarian) {
                        DietaryBadge("🌱", "Vegetariano", Color(0xFF4CAF50))
                    }
                    if (menuItem.isVegan) {
                        DietaryBadge("🥬", "Vegano", Color(0xFF8BC34A))
                    }
                    if (menuItem.isGlutenFree) {
                        DietaryBadge("🌾", "Sin Gluten", Color(0xFFFFC107))
                    }
                }

                // Precio y tiempo de preparación con fondo
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
                            text = "$${menuItem.price}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
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
                                    text = "${menuItem.preparationTime} min",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Estado de disponibilidad
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
                                    if (menuItem.isAvailable) Color(0xFF4CAF50)
                                    else Color(0xFFE53935)
                                )
                        )
                        Text(
                            text = if (menuItem.isAvailable) "Disponible" else "No disponible",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                            ),
                            color = if (menuItem.isAvailable) Color(0xFF4CAF50)
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
                                imageVector = if (menuItem.isAvailable)
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
 * Badge para características dietéticas
 */
@Composable
private fun DietaryBadge(emoji: String, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color
                ),
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8
            )
        }
    }
}

/**
 * Vista cuando no hay productos
 */
@Composable
private fun EmptyMenuView(
    hasFilter: Boolean,
    onAddProduct: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = if (hasFilter) {
                    "No hay productos con este filtro"
                } else {
                    "No hay productos en el menú"
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            if (hasFilter) {
                Text(
                    text = "Intenta con otro filtro o búsqueda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                Button(onClick = onAddProduct) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar primer producto")
                }
            }
        }
    }
}

/**
 * Diálogo para agregar o editar producto del menú
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMenuItemDialog(
    menuItem: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(menuItem?.name ?: "") }
    var description by remember { mutableStateOf(menuItem?.description ?: "") }
    var price by remember { mutableStateOf(menuItem?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(menuItem?.category ?: MenuCategory.MAIN_COURSES) }
    var preparationTime by remember { mutableStateOf(menuItem?.preparationTime?.toString() ?: "15") }
    var isAvailable by remember { mutableStateOf(menuItem?.isAvailable ?: true) }
    var isVegetarian by remember { mutableStateOf(menuItem?.isVegetarian ?: false) }
    var isVegan by remember { mutableStateOf(menuItem?.isVegan ?: false) }
    var isGlutenFree by remember { mutableStateOf(menuItem?.isGlutenFree ?: false) }

    var showCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (menuItem == null) "Agregar Producto" else "Editar Producto",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Precio y tiempo de preparación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = preparationTime,
                        onValueChange = { preparationTime = it },
                        label = { Text("Tiempo (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Selector de categoría
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = category.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (showCategoryDropdown)
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        MenuCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.getDisplayName()) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Divider()

                // Opciones dietéticas
                Text(
                    text = "Características dietéticas",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isVegetarian,
                        onCheckedChange = { isVegetarian = it }
                    )
                    Text("🌱 Vegetariano", modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isVegan,
                        onCheckedChange = { isVegan = it }
                    )
                    Text("🥬 Vegano", modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isGlutenFree,
                        onCheckedChange = { isGlutenFree = it }
                    )
                    Text("🌾 Sin Gluten", modifier = Modifier.weight(1f))
                }

                Divider()

                // Disponibilidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disponible para ordenar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = MenuItem(
                        id = menuItem?.id ?: "item_${(0..999999).random()}",
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category,
                        preparationTime = preparationTime.toIntOrNull() ?: 15,
                        isAvailable = isAvailable,
                        isVegetarian = isVegetarian,
                        isVegan = isVegan,
                        isGlutenFree = isGlutenFree
                    )
                    onSave(newItem)
                },
                enabled = name.isNotBlank() && description.isNotBlank() &&
                         price.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (menuItem == null) "Agregar" else "Guardar",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Texto con animación de subrayado para productos no disponibles
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
