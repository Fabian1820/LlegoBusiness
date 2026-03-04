package com.llego.business.products.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.products.ui.viewmodel.ShowcaseViewModel
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.model.ShowcasesResult
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.launch

private const val PRODUCTS_LOCAL_PAGE_SIZE = 20
private const val ALL_PRODUCTS_CATEGORY_KEY = "__all__"

@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    comboViewModel: ComboViewModel,
    showcaseViewModel: ShowcaseViewModel,
    branchId: String?,
    branchTipos: Set<BranchTipo> = emptySet(),
    searchQuery: String = "",
    onNavigateToAddProduct: (Product?) -> Unit,
    onNavigateToAddShowcase: () -> Unit,
    onNavigateToAddCombo: (com.llego.shared.data.model.Combo?) -> Unit,
    onNavigateToProductDetail: (Product) -> Unit,
    onNavigateToComboDetail: (com.llego.shared.data.model.Combo) -> Unit,
    modifier: Modifier = Modifier
) {
    val productsState by viewModel.productsState.collectAsState()
    val combosState by comboViewModel.combosState.collectAsState()
    val showcasesState by showcaseViewModel.showcasesState.collectAsState()
    val productCategoriesState by viewModel.productCategoriesState.collectAsState()
    val categories: List<ProductCategory> = productCategoriesState.categories
    val scope = rememberCoroutineScope()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<Product?>(null) }
    var deleteComboCandidate by remember { mutableStateOf<com.llego.shared.data.model.Combo?>(null) }
    var visibleItemsByCategory by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showCreateMenu by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(branchId) {
        if (branchId != null) {
            viewModel.loadProducts(branchId = branchId)
            comboViewModel.loadCombos(branchId = branchId)
            showcaseViewModel.loadShowcases(branchId = branchId, activeOnly = false)
        } else {
            viewModel.loadProducts()
            comboViewModel.loadCombos()
        }
    }

    androidx.compose.runtime.LaunchedEffect(branchId, branchTipos) {
        viewModel.loadProductCategories(
            branchId = branchId,
            branchTipos = branchTipos
        )
    }

    androidx.compose.runtime.LaunchedEffect(categories, selectedCategoryId) {
        if (selectedCategoryId != null && categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = null
        }
    }

    val products = when (val state = productsState) {
        is ProductsResult.Success -> state.products
        else -> emptyList()
    }

    val combos = when (val state = combosState) {
        is com.llego.shared.data.model.CombosResult.Success -> state.combos
        else -> emptyList()
    }

    val showcases = when (val state = showcasesState) {
        is ShowcasesResult.Success -> state.showcases
        else -> emptyList()
    }

    val filteredProducts = products.filter { product ->
        selectedCategoryId == null || product.categoryId == selectedCategoryId
    }
    val categoryNameById = categories.associate { it.id to it.name }
    val normalizedSearchQuery = searchQuery.trim().lowercase()
    val searchedProducts = if (normalizedSearchQuery.isBlank()) {
        filteredProducts
    } else {
        filteredProducts.filter { product ->
            product.name.contains(normalizedSearchQuery, ignoreCase = true) ||
                product.description.contains(normalizedSearchQuery, ignoreCase = true)
        }
    }

    val searchedCombos = if (normalizedSearchQuery.isBlank()) {
        combos
    } else {
        combos.filter { combo ->
            combo.name.contains(normalizedSearchQuery, ignoreCase = true) ||
                combo.description.contains(normalizedSearchQuery, ignoreCase = true)
        }
    }

    val searchedShowcases = if (normalizedSearchQuery.isBlank()) {
        showcases
    } else {
        showcases.filter { showcase ->
            showcase.title.contains(normalizedSearchQuery, ignoreCase = true) ||
                (showcase.description?.contains(normalizedSearchQuery, ignoreCase = true) == true) ||
                (showcase.items?.any { it.name.contains(normalizedSearchQuery, ignoreCase = true) } == true)
        }
    }

    val categoryKey = selectedCategoryId ?: ALL_PRODUCTS_CATEGORY_KEY
    val visibleCount = visibleItemsByCategory[categoryKey] ?: PRODUCTS_LOCAL_PAGE_SIZE
    val paginatedProducts = if (normalizedSearchQuery.isBlank()) {
        searchedProducts.take(visibleCount)
    } else {
        searchedProducts
    }
    val canLoadMore = normalizedSearchQuery.isBlank() && paginatedProducts.size < searchedProducts.size

    androidx.compose.runtime.LaunchedEffect(categoryKey) {
        if (!visibleItemsByCategory.containsKey(categoryKey)) {
            visibleItemsByCategory = visibleItemsByCategory + (categoryKey to PRODUCTS_LOCAL_PAGE_SIZE)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = productsState) {
            is ProductsResult.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProductsResult.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error al cargar productos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadProducts(branchId = branchId) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is ProductsResult.Success -> {
                if (paginatedProducts.isEmpty() && searchedCombos.isEmpty() && searchedShowcases.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryId == null,
                                    onClick = { selectedCategoryId = null },
                                    label = { Text("Todas") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(category.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (normalizedSearchQuery.isBlank()) {
                                    "No hay productos para mostrar"
                                } else {
                                    "No hay resultados para \"$searchQuery\""
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 6.dp,
                            end = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "category_filters") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryId == null,
                                        onClick = { selectedCategoryId = null },
                                        label = { Text("Todas") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                items(categories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryId == category.id,
                                        onClick = { selectedCategoryId = category.id },
                                        label = { Text(category.name) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                        items(searchedShowcases, key = { "showcase_${it.id}" }) { showcase ->
                            ShowcaseRow(
                                showcase = showcase,
                                onToggleAvailability = { isActive ->
                                    scope.launch {
                                        showcaseViewModel.toggleAvailability(showcase.id, isActive)
                                        if (branchId != null) {
                                            showcaseViewModel.loadShowcases(branchId = branchId, activeOnly = false)
                                        }
                                    }
                                }
                            )
                        }
                        items(searchedCombos, key = { "combo_${it.id}" }) { combo ->
                            ComboRow(
                                combo = combo,
                                onEdit = { onNavigateToAddCombo(combo) },
                                onDelete = { deleteComboCandidate = combo },
                                onToggleAvailability = { availability ->
                                    scope.launch {
                                        comboViewModel.toggleAvailability(combo.id, availability)
                                        comboViewModel.loadCombos(branchId = branchId)
                                    }
                                },
                                onViewDetail = { onNavigateToComboDetail(combo) }
                            )
                        }
                        items(paginatedProducts, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                categoryNameById = categoryNameById,
                                onEdit = { onNavigateToAddProduct(product) },
                                onDelete = { deleteCandidate = product },
                                onToggleAvailability = { availability ->
                                    scope.launch {
                                        viewModel.updateProductWithImagePath(
                                            productId = product.id,
                                            availability = availability
                                        )
                                        viewModel.loadProducts(branchId = branchId)
                                    }
                                },
                                onViewDetail = { onNavigateToProductDetail(product) }
                            )
                        }
                        if (canLoadMore) {
                            item {
                                Button(
                                    onClick = {
                                        val updatedCount = (visibleItemsByCategory[categoryKey]
                                            ?: PRODUCTS_LOCAL_PAGE_SIZE) + PRODUCTS_LOCAL_PAGE_SIZE
                                        visibleItemsByCategory = visibleItemsByCategory + (
                                            categoryKey to updatedCount
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cargar mas")
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { showCreateMenu = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar producto o combo")
        }

        if (showCreateMenu) {
            AlertDialog(
                onDismissRequest = { showCreateMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "¿Qué deseas crear?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CreateTypeOption(
                            icon = Icons.Default.Add,
                            title = "Producto",
                            subtitle = "Ficha completa con precio, imagen y categoría",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                showCreateMenu = false
                                onNavigateToAddProduct(null)
                            }
                        )
                        CreateTypeOption(
                            icon = Icons.Default.Storefront,
                            title = "Vitrina",
                            subtitle = "Publica una foto y recibe pedidos por descripción",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = {
                                showCreateMenu = false
                                onNavigateToAddShowcase()
                            }
                        )
                        CreateTypeOption(
                            icon = Icons.Default.Restaurant,
                            title = "Combo",
                            subtitle = "Agrupa productos con opciones y precio final",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                showCreateMenu = false
                                onNavigateToAddCombo(null)
                            }
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showCreateMenu = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        deleteCandidate?.let { product ->
            AlertDialog(
                onDismissRequest = { deleteCandidate = null },
                title = { Text("Eliminar producto") },
                text = { Text("Confirmas eliminar \"${product.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.deleteProductBlocking(product.id)
                                viewModel.loadProducts(branchId = branchId)
                            }
                            deleteCandidate = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteCandidate = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        deleteComboCandidate?.let { combo ->
            AlertDialog(
                onDismissRequest = { deleteComboCandidate = null },
                title = { Text("Eliminar combo") },
                text = { Text("Confirmas eliminar \"${combo.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                comboViewModel.deleteComboBlocking(combo.id)
                                comboViewModel.loadCombos(branchId = branchId)
                            }
                            deleteComboCandidate = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteComboCandidate = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun CreateTypeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.infoCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    categoryNameById: Map<String, String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = product.imageUrl.takeIf { it.isNotBlank() } ?: product.image
    val resolvedCategoryName = categoryNameById[product.categoryId]

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewDetail() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(LlegoCustomShapes.infoCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (resolvedCategoryName != null) {
                    Text(
                        text = resolvedCategoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Fila con precio y botones de acciÃ³n
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.currency} ${formatPrice(product.price)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onToggleAvailability(!product.availability) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (product.availability) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Cambiar disponibilidad",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseRow(
    showcase: com.llego.shared.data.model.Showcase,
    onToggleAvailability: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstItemLabel = showcase.items?.firstOrNull()?.name
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(LlegoCustomShapes.infoCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (showcase.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = showcase.imageUrl,
                        contentDescription = showcase.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = showcase.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "VITRINA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    text = showcase.description?.takeIf { it.isNotBlank() }
                        ?: "Pedidos por descripcion manual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (firstItemLabel != null) "Incluye: $firstItemLabel" else "Sin productos publicados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onToggleAvailability(!showcase.isActive) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (showcase.isActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Cambiar disponibilidad",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val rounded = (price * 100).toInt() / 100.0
    val intPart = rounded.toInt()
    val decimalPart = ((rounded - intPart) * 100).toInt()
    return "${intPart}.${decimalPart.toString().padStart(2, '0')}"
}


@Composable
private fun ComboRow(
    combo: com.llego.shared.data.model.Combo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = combo.imageUrl ?: combo.image

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewDetail() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(LlegoCustomShapes.infoCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = combo.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (combo.representativeProducts.isNotEmpty()) {
                    // Mostrar composiciÃ³n de productos representativos
                    val firstProduct = combo.representativeProducts.first()
                    AsyncImage(
                        model = firstProduct.imageUrl,
                        contentDescription = combo.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = combo.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "COMBO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (combo.description.isNotBlank()) {
                    Text(
                        text = combo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (combo.savings > 0) {
                    Text(
                        text = "Ahorro: ${formatPrice(combo.savings)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (combo.basePrice != combo.finalPrice) {
                            Text(
                                text = formatPrice(combo.basePrice),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = formatPrice(combo.finalPrice),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onToggleAvailability(!combo.availability) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (combo.availability) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Cambiar disponibilidad",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
