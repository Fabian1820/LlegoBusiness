package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.components.menu.EmptyMenuView
import com.llego.nichos.common.utils.getProductImage
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuUiState
import com.llego.shared.data.model.BusinessType
import com.llego.nichos.common.ui.components.CategoryFilterChipsForRestaurant
import com.llego.nichos.common.ui.components.ProductCard
import com.llego.nichos.common.ui.components.getCategoryDisplayNameForProduct
import com.llego.nichos.common.data.model.Product
import llegobusiness.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

//Pantalla de Menú con diseño moderno y gestión CRUD
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    businessType: BusinessType = BusinessType.RESTAURANT,
    onNavigateToAddProduct: (com.llego.nichos.common.data.model.Product?) -> Unit = {},
    onNavigateToProductDetail: (com.llego.nichos.common.data.model.Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf<Product?>(null) }
    var showSearchCard by remember { mutableStateOf(false) }
    var animateContent by remember { mutableStateOf(false) }

    // Actualizar el tipo de negocio en el ViewModel cuando cambie
    LaunchedEffect(businessType) {
        viewModel.updateBusinessType(businessType)
    }

    // Animación de entrada idéntica a Perfil y Gestión
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        animateContent = true
    }

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
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = tween(600, easing = EaseOutCubic)
                                )
                    ) {
                        if (filteredProducts.isEmpty()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Filtros de categoría adaptados por nicho
                                CategoryFilterChipsForRestaurant(
                                    businessType = businessType,
                                    selectedCategory = selectedCategory,
                                    selectedCategoryId = selectedCategoryId,
                                    onCategorySelected = { viewModel.setCategory(it) },
                                    onCategoryIdSelected = { viewModel.setCategoryId(it) },
                                    onClearCategory = { viewModel.clearCategory() }
                                )
                                EmptyMenuView(
                                    hasFilter = selectedCategory != null || selectedCategoryId != null || searchQuery.isNotBlank(),
                                    onAddProduct = {
                                        onNavigateToAddProduct(null)
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Filtros de categoría como primer item (adaptados por nicho)
                            item {
                                CategoryFilterChipsForRestaurant(
                                    businessType = businessType,
                                    selectedCategory = selectedCategory,
                                    selectedCategoryId = selectedCategoryId,
                                    onCategorySelected = { viewModel.setCategory(it) },
                                    onCategoryIdSelected = { viewModel.setCategoryId(it) },
                                    onClearCategory = { viewModel.clearCategory() }
                                )
                            }

                            // Lista de productos
                            items(
                                items = filteredProducts,
                                key = { it.id }
                            ) { product ->
                                // Usar ProductCard unificado para todos los nichos
                                ProductCard(
                                    product = product,
                                    businessType = businessType,
                                    onEdit = {
                                        onNavigateToAddProduct(product)
                                    },
                                    onDelete = {
                                        showDeleteConfirmation = product
                                    },
                                    onToggleAvailability = {
                                        viewModel.toggleProductAvailability(product.id)
                                    },
                                    onViewDetail = {
                                        onNavigateToProductDetail(product)
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
                    onNavigateToAddProduct(null)
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
                filteredProducts = filteredProducts,
                onEditItem = { item ->
                    onNavigateToAddProduct(item)
                },
                onDeleteItem = { item ->
                    showDeleteConfirmation = item
                },
                onToggleAvailability = { itemId ->
                    viewModel.toggleProductAvailability(itemId)
                },
                businessType = businessType
            )
        }
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
                        viewModel.deleteProduct(item.id)
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

 // Chips de filtro por categoría simplificados con fade en ambos lados
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

 // Card de búsqueda animado que aparece desde abajo con resultados en tiempo real
@Composable
private fun SearchCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    filteredProducts: List<Product> = emptyList(),
    onEditItem: (Product) -> Unit = {},
    onDeleteItem: (Product) -> Unit = {},
    onToggleAvailability: (String) -> Unit = {},
    businessType: BusinessType = BusinessType.RESTAURANT
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    val sheetInteraction = remember { MutableInteractionSource() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = dismissInteraction,
                indication = null,
                onClick = onClose
            )
    ) {
        val minSheetHeight = 320.dp
        // Mantener siempre un pequeño espacio bajo la navbar superior (bordes redondeados)
        val topGap = 12.dp
        val preferredHeight = (maxHeight * 0.85f).coerceAtMost(maxHeight - topGap)
        // Altura fija independientemente del IME; el movimiento lo gestiona imePadding
        val sheetHeight = preferredHeight.coerceAtLeast(minSheetHeight)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime) // Manejar el teclado de forma responsiva
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .clickable(
                    interactionSource = sheetInteraction,
                    indication = null,
                    onClick = {}
                ), // Prevenir cierre al hacer click en el card
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
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
                            .weight(1f, fill = false)
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
                    // Resultados de búsqueda - contenido scrolleable para manejar el teclado
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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
                                    text = "${filteredProducts.size} producto${if (filteredProducts.size != 1) "s" else ""} encontrado${if (filteredProducts.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Lista de resultados scrolleable
                        if (filteredProducts.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
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
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(
                                    items = filteredProducts,
                                    key = { it.id }
                                ) { product ->
                                    CompactMenuItemCard(
                                        product = product,
                                        businessType = businessType,
                                        onEdit = {
                                            onEditItem(product)
                                            onClose()
                                        },
                                        onDelete = { onDeleteItem(product) },
                                        onToggleAvailability = { onToggleAvailability(product.id) }
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

 // Card compacto de producto para resultados de búsqueda - versión limpia
@Composable
private fun CompactMenuItemCard(
    product: Product,
    businessType: BusinessType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del producto más pequeña
                androidx.compose.foundation.Image(
                    painter = painterResource(getProductImage(product.id)),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Información del producto
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp), // Espacio para el botón de editar
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Nombre
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
                        text = "$${product.price}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Variedades/modificadores (si existen)
                    if (product.varieties.isNotEmpty()) {
                        Text(
                            text = product.varieties.take(3).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Indicador de no disponible (solo si aplica)
                    if (!product.isAvailable) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                            )
                            Text(
                                text = "No disponible",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }

            // Botón de editar sutil
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(28.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

 // Card de producto del menú
@Composable
private fun MenuItemCard(
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
                // Nombre y categoría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedTextWithUnderline(
                        text = product.name,
                        isUnavailable = !product.isAvailable,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Badge de categoría con colores Llego
                val categoryDisplayName2 = getCategoryDisplayNameForProduct(
                    product.category,
                    businessType
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = categoryDisplayName2,
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

                // Características dietéticas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.isVegetarian) {
                        DietaryBadge("🌱", "Vegetariano", Color(0xFF4CAF50))
                    }
                    if (product.isVegan) {
                        DietaryBadge("🥬", "Vegano", Color(0xFF8BC34A))
                    }
                    if (product.isGlutenFree) {
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
                            text = "$${product.price}",
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

 // Badge para características dietéticas
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
