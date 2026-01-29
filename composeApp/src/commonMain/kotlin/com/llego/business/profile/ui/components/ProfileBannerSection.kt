package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.utils.formatDouble
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes

// ============= BANNER SECTION =============

@Composable
fun BannerWithLogoSection(
    avatarUrl: String? = null,
    coverUrl: String? = null,
    onChangeAvatar: () -> Unit = {},
    onChangeCover: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Banner de fondo - Imagen o gradiente
        if (!coverUrl.isNullOrEmpty()) {
            // Mostrar imagen de portada desde el backend
            NetworkImage(
                url = coverUrl,
                contentDescription = "Portada del negocio",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback: gradiente por defecto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
        }

        // Boton para cambiar portada - solo mostrar si onChangeCover no es null
        if (onChangeCover != null) {
            IconButton(
                onClick = onChangeCover,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar portada",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Logo circular
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
                .offset(y = 48.dp)
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                if (!avatarUrl.isNullOrEmpty()) {
                    // Mostrar avatar desde el backend
                    NetworkImage(
                        url = avatarUrl,
                        contentDescription = "Logo del negocio",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback: icono por defecto
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Boton para cambiar avatar
            IconButton(
                onClick = onChangeAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(48.dp))
}

