package com.llego.nichos.market.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.nichos.market.ui.components.profile.LogoutConfirmationDialog
import com.llego.nichos.market.ui.components.profile.MarketProfileHeader
import com.llego.nichos.market.ui.components.profile.MarketProfileOption
import com.llego.nichos.market.ui.components.profile.MarketProfileStat
import com.llego.nichos.market.ui.components.profile.MarketStatsRow
import com.llego.shared.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val profileStats = remember {
        listOf(
            MarketProfileStat(
                title = "Pedidos totales",
                value = "350"
            ),
            MarketProfileStat(
                title = "Calificación",
                value = "4.9 ⭐"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Supermercado") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MarketProfileHeader(
                emoji = "\uD83D\uDED2",
                marketName = "Mi Supermercado",
                statusLabel = "Supermercado • Verificado ✅"
            )

            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.titleMedium
            )

            MarketStatsRow(stats = profileStats)

            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleMedium
            )

            MarketProfileOption(
                icon = Icons.Default.ShoppingCart,
                title = "Productos",
                subtitle = "Gestionar inventario y categorías",
                onClick = { /* TODO: Implementar navegación */ }
            )

            MarketProfileOption(
                icon = Icons.Default.Schedule,
                title = "Horarios",
                subtitle = "Configurar horarios de atención",
                onClick = { /* TODO: Implementar navegación */ }
            )

            MarketProfileOption(
                icon = Icons.Default.LocalShipping,
                title = "Entregas",
                subtitle = "Configurar zonas de entrega",
                onClick = { /* TODO: Implementar navegación */ }
            )

            MarketProfileOption(
                icon = Icons.Default.Settings,
                title = "Configuraciones",
                subtitle = "Preferencias del supermercado",
                onClick = { /* TODO: Implementar navegación */ }
            )

            MarketProfileOption(
                icon = Icons.Default.Logout,
                title = "Cerrar sesión",
                subtitle = "Salir de la aplicación",
                onClick = { showLogoutDialog = true }
            )
        }

        LogoutConfirmationDialog(
            show = showLogoutDialog,
            onConfirm = {
                authViewModel.logout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
