package com.llego.nichos.market.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.ui.components.NichoProfileCard
import com.llego.shared.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
            // Header con información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛒",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Mi Supermercado",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Supermercado • Verificado ✓",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Estadísticas del supermercado
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NichoProfileCard(
                    title = "Pedidos totales",
                    value = "350",
                    modifier = Modifier.weight(1f)
                )
                NichoProfileCard(
                    title = "Calificación",
                    value = "4.9 ⭐",
                    modifier = Modifier.weight(1f)
                )
            }

            // Opciones de configuración
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleMedium
            )

            ProfileOption(
                icon = Icons.Default.ShoppingCart,
                title = "Productos",
                subtitle = "Gestionar inventario y categorías",
                onClick = { /* TODO */ }
            )

            ProfileOption(
                icon = Icons.Default.Schedule,
                title = "Horarios",
                subtitle = "Configurar horarios de atención",
                onClick = { /* TODO */ }
            )

            ProfileOption(
                icon = Icons.Default.LocalShipping,
                title = "Entregas",
                subtitle = "Configurar zonas de entrega",
                onClick = { /* TODO */ }
            )

            ProfileOption(
                icon = Icons.Default.Settings,
                title = "Configuraciones",
                subtitle = "Preferencias del supermercado",
                onClick = { /* TODO */ }
            )

            ProfileOption(
                icon = Icons.Default.Logout,
                title = "Cerrar sesión",
                subtitle = "Salir de la aplicación",
                onClick = { showLogoutDialog = true }
            )
        }

        // Diálogo de confirmación de logout
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Cerrar sesión") },
                text = { Text("¿Estás seguro que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            showLogoutDialog = false
                        }
                    ) {
                        Text("Sí, cerrar sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}