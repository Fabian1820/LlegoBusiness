package com.llego.nichos.market.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.nichos.restaurant.ui.components.profile.*

/**
 * Pantalla de Perfil del Supermercado/Tienda
 *
 * Alineada con RestaurantProfileScreen y la lógica de registro
 * Muestra y permite editar:
 * - Información del negocio (nombre, descripción, tags)
 * - Información del propietario (nombre, email, teléfono)
 * - Información de la sucursal (dirección, teléfono, horarios, facilities)
 * - Ubicación en mapa
 *
 * NO muestra campos internos: role, authProvider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var showShareDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Datos del usuario, negocio y sucursal
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUser = authUiState.user
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Mi Perfil",
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
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner y Logo
            item {
                BannerWithLogoSection(
                    avatarUrl = currentBusiness?.avatarUrl,
                    coverUrl = null  // Business ya no tiene cover
                )
            }

            // Información del Negocio
            item {
                BusinessInfoSection(
                    business = currentBusiness,
                    branch = currentBranch
                )
            }

            // Información del Propietario (SIN role ni authProvider)
            item {
                UserInfoSection(user = currentUser)
            }

            // Información de la Sucursal
            item {
                BranchInfoSection(branch = currentBranch)
            }

            // Redes Sociales
            item {
                SocialLinksSection(socialMedia = currentBusiness?.socialMedia)
            }

            // Horarios
            item {
                BranchScheduleSection(branch = currentBranch)
            }

            // Mapa de Ubicación
            item {
                LocationMapSection(branch = currentBranch)
            }

            // Tags
            item {
                BusinessTagsSection(business = currentBusiness)
            }

            // Facilidades
            item {
                BranchFacilitiesSection(branch = currentBranch)
            }
        }

        // Diálogos
        if (showShareDialog) {
            ShareDialog(onDismiss = { showShareDialog = false })
        }

        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    authViewModel.logout()
                    showLogoutDialog = false
                }
            )
        }
    }
}
