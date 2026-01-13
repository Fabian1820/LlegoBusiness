package com.llego.business.products.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
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
import com.llego.business.products.config.ProductCategoryProvider
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    branchId: String?,
    onNavigateToAddProduct: (Product?) -> Unit,
    onNavigateToProductDetail: (Product) -> Unit,
    onNavigateToProductSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val productsState by viewModel.productsState.collectAsState()
    val categories = remember { ProductCategoryProvider.getCategories() }
    val scope = rememberCoroutineScope()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<Product?>(null) }

    androidx.compose.runtime.LaunchedEffect(branchId) {
        if (branchId != null) {
            viewModel.loadProducts(branchId = branchId)
        } else {
            viewModel.loadProducts()
        }
    }

    val products = when (val state = productsState) {
        is ProductsResult.Success -> state.products
        else -> emptyList()
    }

    val filteredProducts = products.filter { product ->
        selectedCategoryId == null || product.categoryId == selectedCategoryId
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }

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
                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay productos para mostrar")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                ProductRow(
                                    product = product,
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
                            item { Spacer(modifier = Modifier.height(96.dp)) }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = onNavigateToProductSearch,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar productos")
            }
            FloatingActionButton(
                onClick = { onNavigateToAddProduct(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
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
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = product.imageUrl?.takeIf { it.isNotBlank() } ?: product.image
    val categoryName = ProductCategoryProvider.getCategoryDisplayName(product.categoryId)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .clip(RoundedCornerShape(12.dp))
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${product.currency} ${formatPrice(product.price)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = { onToggleAvailability(!product.availability) }) {
                    Icon(
                        if (product.availability) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Cambiar disponibilidad",
                        tint = MaterialTheme.colorScheme.secondary
                    )
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
