package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuUiState

/**
 * Pantalla de Menú con diseño moderno y gestión CRUD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredMenuItems by viewModel.filteredMenuItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<MenuItem?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedMenuItem = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Buscar productos...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    // Filtros de categoría
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategory(it) },
                        onClearCategory = { viewModel.clearCategory() },
                        categoriesWithCount = viewModel.getCategoriesWithCount()
                    )
                }
            }

            // Lista de productos
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
                        EmptyMenuView(
                            hasFilter = selectedCategory != null || searchQuery.isNotBlank(),
                            onAddProduct = {
                                selectedMenuItem = null
                                showAddEditDialog = true
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                    }
                                )
                            }

                            // Espacio adicional para el FAB
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de agregar/editar
    if (showAddEditDialog) {
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

    // Confirmación de eliminación
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
                Text("¿Eliminar producto?")
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar \"${item.name}\"? Esta acción no se puede deshacer.")
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
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Chips de filtro por categoría
 */
@Composable
private fun CategoryFilterChips(
    selectedCategory: MenuCategory?,
    onCategorySelected: (MenuCategory) -> Unit,
    onClearCategory: () -> Unit,
    categoriesWithCount: Map<MenuCategory, Int>
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
                } else null
            )
        }

        // Chips por categoría
        items(MenuCategory.values()) { category ->
            val count = categoriesWithCount[category] ?: 0
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = "${category.getDisplayName()} ($count)",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
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
    onToggleAvailability: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Imagen del producto (placeholder)
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = when (menuItem.category) {
                            MenuCategory.APPETIZERS -> Icons.Default.Restaurant
                            MenuCategory.SOUPS -> Icons.Default.LocalDining
                            MenuCategory.SALADS -> Icons.Default.Eco
                            MenuCategory.MAIN_COURSES -> Icons.Default.DinnerDining
                            MenuCategory.SIDES -> Icons.Default.FoodBank
                            MenuCategory.DESSERTS -> Icons.Default.Cake
                            MenuCategory.BEVERAGES -> Icons.Default.LocalCafe
                            MenuCategory.ALCOHOLIC -> Icons.Default.LocalBar
                            MenuCategory.KIDS_MENU -> Icons.Default.ChildCare
                            MenuCategory.SPECIALS -> Icons.Default.Star
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
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
                    Text(
                        text = menuItem.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (!menuItem.isAvailable)
                                TextDecoration.LineThrough else TextDecoration.None
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Badge de categoría
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = menuItem.category.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
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

                // Precio y tiempo de preparación
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "${menuItem.preparationTime} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
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
                            style = MaterialTheme.typography.labelSmall,
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
                    fontWeight = FontWeight.Bold
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
                         price.toDoubleOrNull() != null
            ) {
                Text(if (menuItem == null) "Agregar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
