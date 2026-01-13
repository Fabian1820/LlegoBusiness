package com.llego.business.analytics.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de Estadísticas del Restaurante
 * Dashboard con métricas principales y gráficos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    ordersViewModel: OrdersViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var animateContent by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf<PeriodFilter>(PeriodFilter.DAY) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Estadísticas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de período
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Dashboard principal - Métricas clave
                item {
                    DashboardMetricsSection(
                        ordersViewModel = ordersViewModel,
                        period = selectedPeriod,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Gráficos
                item {
                    ChartsSection(
                        ordersViewModel = ordersViewModel,
                        period = selectedPeriod,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Productos más vendidos
                item {
                    TopProductsSection(
                        ordersViewModel = ordersViewModel,
                        period = selectedPeriod,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

            }
        }
    }
}

/**
 * Selector de período (Día/Semana/Mes)
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodFilter.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = {
                        Text(
                            period.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Sección de métricas principales del dashboard
 */
@Composable
private fun DashboardMetricsSection(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard Principal",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ventas del día
            MetricCard(
                title = "Ventas del ${period.displayName.lowercase()}",
                value = "$${getSalesForPeriod(ordersViewModel, period)}",
                icon = Icons.Default.AttachMoney,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            // Pedidos completados
            MetricCard(
                title = "Completados",
                value = "${getCompletedOrdersCount(ordersViewModel, period)}",
                icon = Icons.Default.CheckCircle,
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pedidos rechazados
            MetricCard(
                title = "Rechazados",
                value = "${getRejectedOrdersCount(ordersViewModel, period)}",
                icon = Icons.Default.Cancel,
                iconColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )

            // Calificación promedio
            MetricCard(
                title = "Calificación",
                value = "${getAverageRating(ordersViewModel)}",
                icon = Icons.Default.Star,
                iconColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

    }
}

/**
 * Card de métrica individual
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Card de comparativa con período anterior
 */
@Composable
private fun ComparisonCard(
    currentPeriod: PeriodFilter,
    currentValue: Double,
    previousValue: Double,
    modifier: Modifier = Modifier
) {
    val change = if (previousValue > 0) {
        ((currentValue - previousValue) / previousValue * 100).toInt()
    } else 0
    val isPositive = change >= 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Comparativa con ${currentPeriod.getPreviousPeriod().displayName.lowercase()} anterior",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = "${if (isPositive) "+" else ""}$change%",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                )
            }
            Icon(
                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Sección de gráficos
 */
@Composable
private fun ChartsSection(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gráficos",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ventas por ${period.displayName.lowercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                // Gráfico de barras simple con datos de prueba
                SalesBarChart(
                    data = getSalesChartData(ordersViewModel, period),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Sección de productos más vendidos
 */
@Composable
private fun TopProductsSection(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter,
    modifier: Modifier = Modifier
) {
    val topProducts = getTopProducts(ordersViewModel, period)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Productos Más Vendidos",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                topProducts.forEachIndexed { index, product ->
                    TopProductRow(
                        rank = index + 1,
                        productName = product.first,
                        quantity = product.second,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < topProducts.size - 1) {
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }
                }
            }
        }
    }
}

/**
 * Fila de producto en top productos
 */
@Composable
private fun TopProductRow(
    rank: Int,
    productName: String,
    quantity: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
            Text(
                text = productName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Text(
            text = "$quantity ventas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

/**
 * Sección de horarios pico
 */
@Composable
private fun PeakHoursSection(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter,
    modifier: Modifier = Modifier
) {
    val peakHours = getPeakHours(ordersViewModel, period)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Horarios Pico",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                peakHours.forEach { (hour, count) ->
                    PeakHourRow(
                        hour = hour,
                        orderCount = count,
                        maxCount = peakHours.maxOfOrNull { it.second } ?: 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Fila de horario pico con barra de progreso
 */
@Composable
private fun PeakHourRow(
    hour: String,
    orderCount: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = orderCount.toFloat() / maxCount.toFloat()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hour,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "$orderCount pedidos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

// Enums y funciones helper

enum class PeriodFilter(val displayName: String) {
    DAY("Día"),
    WEEK("Semana"),
    MONTH("Mes");

    fun getPreviousPeriod(): PeriodFilter {
        return when (this) {
            DAY -> DAY
            WEEK -> WEEK
            MONTH -> MONTH
        }
    }
}

/**
 * Gráfico de barras simple para ventas
 */
@Composable
private fun SalesBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val trackColor = primaryColor.copy(alpha = 0.12f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val averageValue = if (data.isNotEmpty()) data.map { it.second }.average() else 0.0
    val averageRatio = if (maxValue > 0) {
        (averageValue / maxValue).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = primaryColor,
                    label = "Ventas",
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    color = secondaryColor,
                    label = "Promedio",
                    modifier = Modifier.weight(1f)
                )
            }

            // Barras con grilla y promedio
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val barAreaHeight = maxHeight

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        HorizontalDivider(
                            color = gridColor,
                            thickness = 1.dp
                        )
                    }
                }

                if (averageRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomStart)
                            .offset(y = -(barAreaHeight * averageRatio))
                            .background(secondaryColor.copy(alpha = 0.6f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { (_, value) ->
                        val heightPercent = (value / maxValue).coerceIn(0.05, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(trackColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barAreaHeight * heightPercent)
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    primaryColor.copy(alpha = 0.85f),
                                                    primaryColor
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                data.forEach { (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "$${value.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Funciones helper para obtener datos (mock por ahora)
private fun getSalesForPeriod(ordersViewModel: OrdersViewModel, period: PeriodFilter): Double {
    // Mock data - en producción obtener de ordersViewModel
    return when (period) {
        PeriodFilter.DAY -> 1250.50
        PeriodFilter.WEEK -> 8750.75
        PeriodFilter.MONTH -> 35000.00
    }
}

private fun getSalesChartData(ordersViewModel: OrdersViewModel, period: PeriodFilter): List<Pair<String, Double>> {
    // Datos de prueba para el gráfico
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "00-06" to 120.0,
            "06-12" to 450.0,
            "12-18" to 380.0,
            "18-24" to 300.0
        )
        PeriodFilter.WEEK -> listOf(
            "Lun" to 1200.0,
            "Mar" to 1450.0,
            "Mié" to 1100.0,
            "Jue" to 1650.0,
            "Vie" to 1800.0,
            "Sáb" to 2100.0,
            "Dom" to 1450.0
        )
        PeriodFilter.MONTH -> listOf(
            "Sem 1" to 8500.0,
            "Sem 2" to 9200.0,
            "Sem 3" to 8800.0,
            "Sem 4" to 9500.0
        )
    }
}

private fun getCompletedOrdersCount(ordersViewModel: OrdersViewModel, period: PeriodFilter): Int {
    // Mock data
    return when (period) {
        PeriodFilter.DAY -> 45
        PeriodFilter.WEEK -> 320
        PeriodFilter.MONTH -> 1280
    }
}

private fun getRejectedOrdersCount(ordersViewModel: OrdersViewModel, period: PeriodFilter): Int {
    // Mock data
    return when (period) {
        PeriodFilter.DAY -> 3
        PeriodFilter.WEEK -> 18
        PeriodFilter.MONTH -> 72
    }
}

private fun getAverageRating(ordersViewModel: OrdersViewModel): String {
    // Mock data
    return "4.8"
}

private fun getTopProducts(ordersViewModel: OrdersViewModel, period: PeriodFilter): List<Pair<String, Int>> {
    // Datos de prueba más realistas
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "Pizza Margarita" to 25,
            "Hamburguesa Clásica" to 18,
            "Pasta Carbonara" to 15,
            "Ensalada César" to 12,
            "Sándwich Club" to 10
        )
        PeriodFilter.WEEK -> listOf(
            "Pizza Margarita" to 120,
            "Hamburguesa Clásica" to 95,
            "Pasta Carbonara" to 78,
            "Ensalada César" to 65,
            "Sándwich Club" to 52
        )
        PeriodFilter.MONTH -> listOf(
            "Pizza Margarita" to 485,
            "Hamburguesa Clásica" to 392,
            "Pasta Carbonara" to 315,
            "Ensalada César" to 268,
            "Sándwich Club" to 220
        )
    }
}

private fun getPeakHours(ordersViewModel: OrdersViewModel, period: PeriodFilter): List<Pair<String, Int>> {
    // Datos de prueba más realistas
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "12:00 - 13:00" to 12,
            "19:00 - 20:00" to 10,
            "13:00 - 14:00" to 8,
            "20:00 - 21:00" to 7,
            "18:00 - 19:00" to 6
        )
        PeriodFilter.WEEK -> listOf(
            "12:00 - 13:00" to 85,
            "19:00 - 20:00" to 72,
            "13:00 - 14:00" to 65,
            "20:00 - 21:00" to 58,
            "18:00 - 19:00" to 52
        )
        PeriodFilter.MONTH -> listOf(
            "12:00 - 13:00" to 342,
            "19:00 - 20:00" to 298,
            "13:00 - 14:00" to 268,
            "20:00 - 21:00" to 245,
            "18:00 - 19:00" to 218
        )
    }
}

