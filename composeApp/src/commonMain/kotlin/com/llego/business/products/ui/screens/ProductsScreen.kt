package com.llego.business.products.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.llego.business.orders.ui.components.iOSStylePicker
import com.llego.business.orders.ui.components.iOSStylePickerOption
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.products.ui.viewmodel.ShowcaseViewModel
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.Showcase
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.model.ShowcasesResult
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private enum class ProductTypeFilter(val displayName: String) {
    ALL("Todos"),
    PRODUCT("Producto"),
    COMBO("Combo"),
    SHOWCASE("Vitrina")
}

private const val IMAGE_LOAD_BATCH_SIZE = 4
// MVP: la vitrina queda deshabilitada temporalmente en el frontend.
private const val SHOWCASE_FRONTEND_ENABLED = false
private val ACTIVE_PRODUCT_TYPE_FILTERS = listOf(
    ProductTypeFilter.ALL,
    ProductTypeFilter.PRODUCT,
    ProductTypeFilter.COMBO
)

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
    onNavigateToShowcaseDetail: (Showcase) -> Unit,
    onNavigateToEditShowcase: (Showcase) -> Unit,
    onNavigateToAddCombo: (com.llego.shared.data.model.Combo?) -> Unit,
    onNavigateToProductDetail: (Product) -> Unit,
    onNavigateToComboDetail: (com.llego.shared.data.model.Combo) -> Unit,
    modifier: Modifier = Modifier
) {
    val productsState by viewModel.productsState.collectAsState()
    val combosState by comboViewModel.combosState.collectAsState()
    val showcasesState by showcaseViewModel.showcasesState.collectAsState()
    val productCategoriesState by viewModel.productCategoriesState.collectAsState()
    val isLoadingMoreProducts by viewModel.isLoadingMoreProducts.collectAsState()
    val loadMoreProductsError by viewModel.loadMoreProductsError.collectAsState()
    val categories: List<ProductCategory> = productCategoriesState.categories
    val selectedCategoryId by viewModel.selectedProductsCategoryId.collectAsState()
    val selectedTypeFilterKey by viewModel.selectedProductsTypeFilter.collectAsState()
    val selectedTypeFilter = ProductTypeFilter.entries.firstOrNull {
        it.name == selectedTypeFilterKey
    }?.takeIf { SHOWCASE_FRONTEND_ENABLED || it != ProductTypeFilter.SHOWCASE }
        ?: ProductTypeFilter.ALL
    val normalizedSearchQuery = searchQuery.trim().lowercase()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var deleteCandidate by remember { mutableStateOf<Product?>(null) }
    var deleteComboCandidate by remember { mutableStateOf<com.llego.shared.data.model.Combo?>(null) }
    var deleteShowcaseCandidate by remember { mutableStateOf<Showcase?>(null) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var updatingProductIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var updatingComboIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var updatingShowcaseIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var previousVisibleCombosCount by remember { mutableStateOf(0) }
    var maxUnlockedImageIndex by remember { mutableStateOf(IMAGE_LOAD_BATCH_SIZE - 1) }
    val visibleLazyIndexRange by remember(listState) {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val firstVisible = visibleItems.firstOrNull()?.index ?: 0
            val lastVisible = visibleItems.lastOrNull()?.index ?: -1
            firstVisible to lastVisible
        }
    }

    androidx.compose.runtime.LaunchedEffect(branchId) {
        if (branchId != null) {
            comboViewModel.ensureCombosLoaded(branchId = branchId)
            if (SHOWCASE_FRONTEND_ENABLED) {
                showcaseViewModel.ensureShowcasesLoaded(branchId = branchId, activeOnly = false)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(branchId, selectedCategoryId, normalizedSearchQuery) {
        val shouldLoadAllProducts = normalizedSearchQuery.isNotBlank() || selectedCategoryId != null
        if (shouldLoadAllProducts) {
            viewModel.ensureAllProductsLoaded(
                branchId = branchId,
                categoryId = selectedCategoryId,
                first = 100
            )
        } else {
            viewModel.ensureProductsLoaded(
                branchId = branchId,
                categoryId = selectedCategoryId,
                first = ProductViewModel.DEFAULT_PRODUCTS_PAGE_SIZE
            )
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
            viewModel.setSelectedProductsCategoryId(null)
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        branchId,
        selectedCategoryId,
        selectedTypeFilter,
        normalizedSearchQuery
    ) {
        if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
            listState.scrollToItem(0)
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        branchId,
        selectedCategoryId,
        selectedTypeFilter,
        normalizedSearchQuery
    ) {
        maxUnlockedImageIndex = IMAGE_LOAD_BATCH_SIZE - 1
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

    val productSuccessState = productsState as? ProductsResult.Success
    val supportsInfiniteLoad = normalizedSearchQuery.isBlank() &&
        selectedCategoryId == null &&
        (selectedTypeFilter == ProductTypeFilter.ALL || selectedTypeFilter == ProductTypeFilter.PRODUCT)
    val canLoadMore = productSuccessState?.hasNextPage == true &&
        supportsInfiniteLoad &&
        loadMoreProductsError == null

    val categoryFilterActive = selectedCategoryId != null
    val visibleProducts = when (selectedTypeFilter) {
        ProductTypeFilter.ALL,
        ProductTypeFilter.PRODUCT -> searchedProducts
        ProductTypeFilter.COMBO,
        ProductTypeFilter.SHOWCASE -> emptyList()
    }
    val visibleCombos = when {
        categoryFilterActive -> emptyList()
        selectedTypeFilter == ProductTypeFilter.ALL || selectedTypeFilter == ProductTypeFilter.COMBO -> searchedCombos
        else -> emptyList()
    }
    val visibleShowcases = when {
        !SHOWCASE_FRONTEND_ENABLED -> emptyList()
        categoryFilterActive -> emptyList()
        selectedTypeFilter == ProductTypeFilter.ALL || selectedTypeFilter == ProductTypeFilter.SHOWCASE -> searchedShowcases
        else -> emptyList()
    }
    val showcaseFilterEnabled = SHOWCASE_FRONTEND_ENABLED && !categoryFilterActive &&
        (selectedTypeFilter == ProductTypeFilter.ALL || selectedTypeFilter == ProductTypeFilter.SHOWCASE)
    val isShowcaseOnlyFilter = SHOWCASE_FRONTEND_ENABLED && selectedTypeFilter == ProductTypeFilter.SHOWCASE
    val showcasesErrorMessage = if (SHOWCASE_FRONTEND_ENABLED) {
        (showcasesState as? ShowcasesResult.Error)?.message
    } else {
        null
    }
    val isShowcasesLoading = SHOWCASE_FRONTEND_ENABLED && showcasesState is ShowcasesResult.Loading
    val canRetryShowcases = SHOWCASE_FRONTEND_ENABLED && branchId != null
    val hasVisibleItems = visibleProducts.isNotEmpty() || visibleCombos.isNotEmpty() || visibleShowcases.isNotEmpty()

    androidx.compose.runtime.LaunchedEffect(branchId) {
        previousVisibleCombosCount = 0
    }

    androidx.compose.runtime.LaunchedEffect(
        visibleCombos.size,
        selectedCategoryId,
        selectedTypeFilter
    ) {
        val combosVisibleInCurrentFilter =
            selectedCategoryId == null &&
                (selectedTypeFilter == ProductTypeFilter.ALL || selectedTypeFilter == ProductTypeFilter.COMBO)
        val combosJustAppeared = previousVisibleCombosCount == 0 && visibleCombos.isNotEmpty()
        if (
            combosVisibleInCurrentFilter &&
            combosJustAppeared &&
            (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)
        ) {
            listState.scrollToItem(0)
        }
        previousVisibleCombosCount = visibleCombos.size
    }

    androidx.compose.runtime.LaunchedEffect(
        listState,
        canLoadMore,
        isLoadingMoreProducts,
        visibleProducts.size,
        visibleCombos.size,
        visibleShowcases.size
    ) {
        if (!canLoadMore) return@LaunchedEffect
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex to totalItems
        }.collectLatest { (lastVisibleIndex, totalItems) ->
            if (canLoadMore && !isLoadingMoreProducts && totalItems > 0 && lastVisibleIndex >= totalItems - 4) {
                viewModel.loadMoreProducts()
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        listState,
        visibleProducts.size,
        visibleCombos.size,
        visibleShowcases.size
    ) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.collectLatest { lastVisibleIndex ->
            if (lastVisibleIndex < 0) return@collectLatest
            val chunkIndex = lastVisibleIndex / IMAGE_LOAD_BATCH_SIZE
            val nextUnlockedIndex = ((chunkIndex + 1) * IMAGE_LOAD_BATCH_SIZE) - 1
            if (nextUnlockedIndex > maxUnlockedImageIndex) {
                maxUnlockedImageIndex = nextUnlockedIndex
            }
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
                        Button(
                            onClick = {
                                viewModel.loadProducts(
                                    branchId = branchId,
                                    first = ProductViewModel.DEFAULT_PRODUCTS_PAGE_SIZE,
                                    force = true
                                )
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is ProductsResult.Success -> {
                val shouldRenderShowcasesLoadingState = isShowcaseOnlyFilter && isShowcasesLoading
                val shouldRenderShowcasesErrorState =
                    isShowcaseOnlyFilter && !isShowcasesLoading && showcasesErrorMessage != null

                Column(modifier = Modifier.fillMaxSize()) {
                    ProductsFilters(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        selectedTypeFilter = selectedTypeFilter,
                        onCategorySelected = { viewModel.setSelectedProductsCategoryId(it) },
                        onTypeSelected = { viewModel.setSelectedProductsTypeFilter(it.name) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (!hasVisibleItems) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                shouldRenderShowcasesLoadingState -> {
                                    CircularProgressIndicator()
                                }

                                shouldRenderShowcasesErrorState -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = showcasesErrorMessage
                                                ?: "Error al cargar vitrinas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(
                                            enabled = canRetryShowcases,
                                            onClick = {
                                                branchId?.let { currentBranchId ->
                                                    showcaseViewModel.loadShowcases(
                                                        branchId = currentBranchId,
                                                        activeOnly = false,
                                                        force = true
                                                    )
                                                }
                                            }
                                        ) {
                                            Text("Reintentar")
                                        }
                                    }
                                }

                                else -> {
                                    Text(
                                        text = if (normalizedSearchQuery.isBlank()) {
                                            "No hay elementos para mostrar"
                                        } else {
                                            "No hay resultados para \"$searchQuery\""
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        val showShowcasesErrorRow = showcaseFilterEnabled && showcasesErrorMessage != null
                        val showShowcasesLoadingRow =
                            showcaseFilterEnabled && isShowcasesLoading && visibleShowcases.isEmpty()
                        val comboStartIndex = 0
                        val showcaseStartIndex = visibleCombos.size +
                            (if (showShowcasesErrorRow) 1 else 0) +
                            (if (showShowcasesLoadingRow) 1 else 0)
                        val productStartIndex = showcaseStartIndex + visibleShowcases.size
                        val (visibleLazyStartIndex, visibleLazyEndIndex) = visibleLazyIndexRange

                        fun shouldLoadImageForIndex(index: Int): Boolean {
                            if (index > maxUnlockedImageIndex) return false
                            if (visibleLazyEndIndex < 0) return index <= maxUnlockedImageIndex
                            val bufferedStart = (visibleLazyStartIndex - IMAGE_LOAD_BATCH_SIZE).coerceAtLeast(0)
                            val bufferedEnd = visibleLazyEndIndex + IMAGE_LOAD_BATCH_SIZE
                            return index in bufferedStart..bufferedEnd
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(visibleCombos, key = { _, combo -> "combo_${combo.id}" }) { index, combo ->
                                ComboRow(
                                    combo = combo,
                                    isAvailabilityUpdating = updatingComboIds.contains(combo.id),
                                    shouldLoadImage = shouldLoadImageForIndex(comboStartIndex + index),
                                    onEdit = { onNavigateToAddCombo(combo) },
                                    onDelete = { deleteComboCandidate = combo },
                                    onToggleAvailability = { availability ->
                                        if (updatingComboIds.contains(combo.id)) return@ComboRow
                                        scope.launch {
                                            updatingComboIds = updatingComboIds + combo.id
                                            try {
                                                comboViewModel.toggleAvailability(combo.id, availability)
                                            } finally {
                                                updatingComboIds = updatingComboIds - combo.id
                                            }
                                        }
                                    },
                                    onViewDetail = { onNavigateToComboDetail(combo) }
                                )
                            }
                            if (showShowcasesErrorRow) {
                                item(key = "showcases_error") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = showcasesErrorMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(
                                            enabled = canRetryShowcases,
                                            onClick = {
                                                branchId?.let { currentBranchId ->
                                                    showcaseViewModel.loadShowcases(
                                                        branchId = currentBranchId,
                                                        activeOnly = false,
                                                        force = true
                                                    )
                                                }
                                            }
                                        ) {
                                            Text("Reintentar")
                                        }
                                    }
                                }
                            }
                            if (showShowcasesLoadingRow) {
                                item(key = "showcases_loading") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                            itemsIndexed(visibleShowcases, key = { _, showcase -> "showcase_${showcase.id}" }) { index, showcase ->
                                ShowcaseRow(
                                    showcase = showcase,
                                    isAvailabilityUpdating = updatingShowcaseIds.contains(showcase.id),
                                    shouldLoadImage = shouldLoadImageForIndex(showcaseStartIndex + index),
                                    onEdit = { onNavigateToEditShowcase(showcase) },
                                    onDelete = { deleteShowcaseCandidate = showcase },
                                    onViewDetail = { onNavigateToShowcaseDetail(showcase) },
                                    onToggleAvailability = { isActive ->
                                        if (updatingShowcaseIds.contains(showcase.id)) return@ShowcaseRow
                                        scope.launch {
                                            updatingShowcaseIds = updatingShowcaseIds + showcase.id
                                            try {
                                                showcaseViewModel.toggleAvailability(showcase.id, isActive)
                                            } finally {
                                                updatingShowcaseIds = updatingShowcaseIds - showcase.id
                                            }
                                        }
                                    }
                                )
                            }
                            itemsIndexed(visibleProducts, key = { _, product -> product.id }) { index, product ->
                                ProductRow(
                                    product = product,
                                    categoryNameById = categoryNameById,
                                    isAvailabilityUpdating = updatingProductIds.contains(product.id),
                                    shouldLoadImage = shouldLoadImageForIndex(productStartIndex + index),
                                    onEdit = { onNavigateToAddProduct(product) },
                                    onDelete = { deleteCandidate = product },
                                    onToggleAvailability = { availability ->
                                        if (updatingProductIds.contains(product.id)) return@ProductRow
                                        scope.launch {
                                            updatingProductIds = updatingProductIds + product.id
                                            try {
                                                viewModel.toggleProductAvailability(
                                                    productId = product.id,
                                                    availability = availability
                                                )
                                            } finally {
                                                updatingProductIds = updatingProductIds - product.id
                                            }
                                        }
                                    },
                                    onViewDetail = { onNavigateToProductDetail(product) }
                                )
                            }
                            if (isLoadingMoreProducts) {
                                item(key = "loading_more_products") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                            if (!isLoadingMoreProducts &&
                                loadMoreProductsError != null &&
                                supportsInfiniteLoad &&
                                productSuccessState?.hasNextPage == true
                            ) {
                                item(key = "loading_more_products_error") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = loadMoreProductsError ?: "No se pudo cargar más productos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(
                                            onClick = { viewModel.loadMoreProducts(force = true) }
                                        ) {
                                            Text("Reintentar")
                                        }
                                    }
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
                        if (SHOWCASE_FRONTEND_ENABLED) {
                            // MVP: no mostrar creación de vitrinas.
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
                        }
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
                                viewModel.loadProducts(
                                    branchId = branchId,
                                    first = ProductViewModel.DEFAULT_PRODUCTS_PAGE_SIZE,
                                    force = true
                                )
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
                                comboViewModel.loadCombos(branchId = branchId, force = true)
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

        deleteShowcaseCandidate?.let { showcase ->
            AlertDialog(
                onDismissRequest = { deleteShowcaseCandidate = null },
                title = { Text("Eliminar vitrina") },
                text = { Text("Confirmas eliminar \"${showcase.title}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                showcaseViewModel.deleteShowcase(showcase.id)
                            }
                            deleteShowcaseCandidate = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteShowcaseCandidate = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProductsFilters(
    categories: List<ProductCategory>,
    selectedCategoryId: String?,
    selectedTypeFilter: ProductTypeFilter,
    onCategorySelected: (String?) -> Unit,
    onTypeSelected: (ProductTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    val selectedCategoryName = categories
        .firstOrNull { it.id == selectedCategoryId }
        ?.name
        ?: "Todas"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        iOSStylePicker(
            label = "Categoria",
            selectedValue = selectedCategoryName,
            isExpanded = showCategoryPicker,
            onToggle = { showCategoryPicker = !showCategoryPicker },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                iOSStylePickerOption(
                    text = "Todas",
                    isSelected = selectedCategoryId == null,
                    onClick = {
                        onCategorySelected(null)
                        showCategoryPicker = false
                    }
                )
                categories.forEach { category ->
                    iOSStylePickerOption(
                        text = category.name,
                        isSelected = selectedCategoryId == category.id,
                        onClick = {
                            onCategorySelected(category.id)
                            showCategoryPicker = false
                        }
                    )
                }
            }
        }

        iOSStylePicker(
            label = "Tipo",
            selectedValue = selectedTypeFilter.displayName,
            isExpanded = showTypePicker,
            onToggle = { showTypePicker = !showTypePicker },
            modifier = Modifier.weight(1f)
        ) {
            // MVP: ocultar el filtro de vitrinas en la UI.
            ACTIVE_PRODUCT_TYPE_FILTERS.forEach { filter ->
                iOSStylePickerOption(
                    text = filter.displayName,
                    isSelected = selectedTypeFilter == filter,
                    onClick = {
                        onTypeSelected(filter)
                        showTypePicker = false
                    }
                )
            }
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
    isAvailabilityUpdating: Boolean,
    shouldLoadImage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = product.imageUrlMuyBaja.takeIf { it.isNotBlank() }
        ?: product.imageUrlBaja.takeIf { it.isNotBlank() }
        ?: product.imageUrl.takeIf { it.isNotBlank() }
        ?: product.imageUrlOriginal.takeIf { it.isNotBlank() }
        ?: product.image
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
                if (imageUrl.isNotBlank() && shouldLoadImage) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (!product.availability) {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }
                            )
                        } else {
                            null
                        }
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!product.availability) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.38f))
                    )
                    Text(
                        text = "No disponible",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
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
                            enabled = !isAvailabilityUpdating,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isAvailabilityUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
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
}

@Composable
private fun ShowcaseRow(
    showcase: Showcase,
    isAvailabilityUpdating: Boolean,
    shouldLoadImage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetail: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val showcaseItemsCount = showcase.items?.size ?: 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewDetail() }
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp),
                color = MaterialTheme.colorScheme.tertiary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "VITRINA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(LlegoCustomShapes.infoCard)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (showcase.imageUrl.isNotBlank() && shouldLoadImage) {
                        AsyncImage(
                            model = showcase.imageUrl,
                            contentDescription = showcase.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = if (!showcase.isActive) {
                                ColorFilter.colorMatrix(
                                    ColorMatrix().apply { setToSaturation(0f) }
                                )
                            } else {
                                null
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!showcase.isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.38f))
                        )
                        Text(
                            text = "No disponible",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = showcase.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = showcase.description?.takeIf { it.isNotBlank() }
                            ?: "Pedidos por descripcion manual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (showcaseItemsCount > 0) {
                            "$showcaseItemsCount productos en la vitrina"
                        } else {
                            "Sin productos en la vitrina"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            onClick = { onToggleAvailability(!showcase.isActive) },
                            enabled = !isAvailabilityUpdating,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isAvailabilityUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    if (showcase.isActive) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
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
    isAvailabilityUpdating: Boolean,
    shouldLoadImage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Solo usar imageUrl (imagen propia del combo), no usar combo.image como fallback
    val imageUrl = combo.imageUrl

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
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
                if (!imageUrl.isNullOrBlank() && shouldLoadImage) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = combo.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (!combo.availability) {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }
                            )
                        } else {
                            null
                        }
                    )
                } else if (combo.representativeProducts.isNotEmpty()) {
                    // Mostrar composición de productos como iconitos redondos superpuestos
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        combo.representativeProducts.take(3).forEach { product ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(2.dp)
                            ) {
                                if (product.imageUrl.isNotBlank() && shouldLoadImage) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        colorFilter = if (!combo.availability) {
                                            ColorFilter.colorMatrix(
                                                ColorMatrix().apply { setToSaturation(0f) }
                                            )
                                        } else {
                                            null
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = product.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (combo.representativeProducts.size > 3) {
                        Text(
                            text = "+${combo.representativeProducts.size - 3}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!combo.availability) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.38f))
                    )
                    Text(
                        text = "No disponible",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
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
                if (combo.description.isNotBlank()) {
                    Text(
                        text = combo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (combo.discountType == com.llego.shared.data.model.DiscountType.PERCENTAGE) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${combo.discountValue.toInt()}% OFF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (combo.startingSavings > 0) {
                        Text(
                            text = "Ahorra desde ${formatPrice(combo.startingSavings)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Desde ${formatPrice(combo.startingFinalPrice)}",
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
                            enabled = !isAvailabilityUpdating,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isAvailabilityUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
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
}
