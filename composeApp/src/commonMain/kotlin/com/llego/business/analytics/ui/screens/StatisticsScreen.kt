package com.llego.business.analytics.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.analytics.ui.components.DashboardMetricsSection
import com.llego.business.analytics.ui.components.PeriodSelector
import com.llego.business.analytics.ui.components.TopProductsSection
import com.llego.business.analytics.util.PeriodFilter
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de estadisticas del negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    ordersViewModel: OrdersViewModel,
    businessId: String,
    onNavigateBack: () -> Unit = {},
    embeddedInHome: Boolean = false
) {
    var animateContent by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.DAY) }
    val dashboardState by ordersViewModel.dashboardStatsState.collectAsState()

    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    LaunchedEffect(businessId, selectedPeriod) {
        if (businessId.isNotBlank()) {
            ordersViewModel.loadDashboardStats(
                businessId = businessId,
                period = selectedPeriod.toDashboardPeriod()
            )
        }
    }

    val retryLoad = {
        if (businessId.isNotBlank()) {
            ordersViewModel.loadDashboardStats(
                businessId = businessId,
                period = selectedPeriod.toDashboardPeriod(),
                forceRefresh = true
            )
        }
    }

    val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
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
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    DashboardMetricsSection(
                        statsState = dashboardState,
                        period = selectedPeriod,
                        onRetry = retryLoad,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    TopProductsSection(
                        statsState = dashboardState,
                        onRetry = retryLoad,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    if (embeddedInHome) {
        content(PaddingValues(0.dp))
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Estadisticas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}
