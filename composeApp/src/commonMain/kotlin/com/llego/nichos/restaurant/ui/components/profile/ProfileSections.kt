package com.llego.nichos.restaurant.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User
import com.llego.shared.data.model.toBusinessType

// ============= BANNER SECTION =============

@Composable
fun BannerWithLogoSection(
    avatarUrl: String? = null,
    coverUrl: String? = null,
    onChangeAvatar: () -> Unit = {},
    onChangeCover: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Banner de fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Botón para cambiar portada
        IconButton(
            onClick = onChangeCover,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar portada",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Logo circular
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
                .offset(y = 55.dp)
        ) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 15.dp,
                border = BorderStroke(5.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🍽️", fontSize = 48.sp)
                }
            }
            
            // Botón para cambiar avatar
            IconButton(
                onClick = onChangeAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar logo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(55.dp))
}

// ============= BUSINESS INFO SECTION =============

@Composable
fun BusinessInfoSection(
    business: Business?,
    branch: Branch?,
    onSave: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var businessName by remember(business) { mutableStateOf(business?.name ?: "") }
    var category by remember(business) { mutableStateOf(business?.type?.toBusinessType()?.name ?: "") }
    var description by remember(business) { mutableStateOf(business?.description ?: "") }
    var address by remember(branch) { mutableStateOf(branch?.address ?: "") }
    
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingCategory by remember { mutableStateOf(false) }
    var isEditingDescription by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }

    val rating = business?.globalRating ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nombre y Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nombre editable
                if (isEditingName) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { isEditingName = false }) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { 
                                    businessName = business?.name ?: ""
                                    isEditingName = false 
                                }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true
                    )
                } else {
                    Text(
                        text = businessName.ifEmpty { "Sin nombre" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.Black,
                        modifier = Modifier.clickable { isEditingName = true }
                    )
                }

                // Dirección
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isEditingAddress = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = address.ifEmpty { "Agregar dirección" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (address.isEmpty()) Color.Gray else Color.DarkGray
                    )
                }
            }

            // Rating badge
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Categoría
        Text(
            text = category.ifEmpty { "Sin categoría" },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { isEditingCategory = true }
        )

        // Descripción
        Text(
            text = description.ifEmpty { "Agregar descripción" },
            style = MaterialTheme.typography.bodyMedium,
            color = if (description.isEmpty()) Color.Gray else Color.DarkGray,
            modifier = Modifier.clickable { isEditingDescription = true }
        )
    }
}

// ============= USER INFO SECTION =============

@Composable
fun UserInfoSection(
    user: User?,
    onSave: (String, String) -> Unit = { _, _ -> }
) {
    var userName by remember(user) { mutableStateOf(user?.name ?: "") }
    var userPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }

    ProfileSectionCard {
        SectionHeader(title = "Información del Propietario", emoji = "👤")

        EditableField(
            label = "Nombre",
            value = userName,
            onValueChange = { userName = it },
            isEditing = isEditingName,
            onEditClick = { isEditingName = true },
            onSaveClick = { isEditingName = false },
            onCancelClick = { userName = user?.name ?: ""; isEditingName = false },
            icon = Icons.Default.Person
        )

        ReadOnlyField(
            label = "Email",
            value = user?.email ?: "",
            icon = Icons.Default.Email
        )

        EditableField(
            label = "Teléfono",
            value = userPhone,
            onValueChange = { userPhone = it },
            isEditing = isEditingPhone,
            onEditClick = { isEditingPhone = true },
            onSaveClick = { isEditingPhone = false },
            onCancelClick = { userPhone = user?.phone ?: ""; isEditingPhone = false },
            icon = Icons.Default.Phone,
            placeholder = "Agregar teléfono"
        )

        ReadOnlyField(
            label = "Rol",
            value = when(user?.role) {
                "merchant" -> "Comerciante"
                "admin" -> "Administrador"
                else -> user?.role ?: ""
            },
            icon = Icons.Default.Badge
        )

        ReadOnlyField(
            label = "Autenticación",
            value = when(user?.authProvider) {
                "google" -> "Google"
                "apple" -> "Apple"
                "local" -> "Email/Contraseña"
                else -> user?.authProvider ?: ""
            },
            icon = Icons.Default.Security
        )
    }
}

// ============= BRANCH INFO SECTION =============

@Composable
fun BranchInfoSection(
    branch: Branch?,
    onSave: (String, String, String, Double?) -> Unit = { _, _, _, _ -> }
) {
    var branchName by remember(branch) { mutableStateOf(branch?.name ?: "") }
    var branchPhone by remember(branch) { mutableStateOf(branch?.phone ?: "") }
    var branchAddress by remember(branch) { mutableStateOf(branch?.address ?: "") }
    var deliveryRadius by remember(branch) { mutableStateOf(branch?.deliveryRadius?.toString() ?: "") }
    
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }
    var isEditingRadius by remember { mutableStateOf(false) }

    ProfileSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Información de la Sucursal", emoji = "🏪")
            StatusBadge(branch?.status)
        }

        EditableField(
            label = "Nombre",
            value = branchName,
            onValueChange = { branchName = it },
            isEditing = isEditingName,
            onEditClick = { isEditingName = true },
            onSaveClick = { isEditingName = false },
            onCancelClick = { branchName = branch?.name ?: ""; isEditingName = false },
            icon = Icons.Default.Store
        )

        EditableField(
            label = "Teléfono",
            value = branchPhone,
            onValueChange = { branchPhone = it },
            isEditing = isEditingPhone,
            onEditClick = { isEditingPhone = true },
            onSaveClick = { isEditingPhone = false },
            onCancelClick = { branchPhone = branch?.phone ?: ""; isEditingPhone = false },
            icon = Icons.Default.Phone
        )

        EditableField(
            label = "Dirección",
            value = branchAddress,
            onValueChange = { branchAddress = it },
            isEditing = isEditingAddress,
            onEditClick = { isEditingAddress = true },
            onSaveClick = { isEditingAddress = false },
            onCancelClick = { branchAddress = branch?.address ?: ""; isEditingAddress = false },
            icon = Icons.Default.LocationOn,
            placeholder = "Agregar dirección"
        )

        EditableField(
            label = "Radio de Entrega (km)",
            value = deliveryRadius,
            onValueChange = { deliveryRadius = it },
            isEditing = isEditingRadius,
            onEditClick = { isEditingRadius = true },
            onSaveClick = { isEditingRadius = false },
            onCancelClick = { deliveryRadius = branch?.deliveryRadius?.toString() ?: ""; isEditingRadius = false },
            icon = Icons.Default.DeliveryDining
        )
    }
}
