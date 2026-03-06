package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.paint
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size
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
    coverPreviewUrl: String? = null,
    branchName: String? = null,
    onChangeAvatar: () -> Unit = {},
    onChangeCover: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val avatarInitial = branchName?.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val visibleCoverUrl = coverPreviewUrl?.takeIf { it.isNotBlank() } ?: coverUrl
    val context = LocalPlatformContext.current

    // Crear painter fuera del Box para poder usarlo en Modifier.paint()
    val coverPainter = if (!visibleCoverUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(visibleCoverUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .size(Size.ORIGINAL)
                .build()
        )
    } else null

    val coverState = coverPainter?.state

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clipToBounds()
            .then(
                if (coverPainter != null) {
                    Modifier.paint(
                        painter = coverPainter,
                        contentScale = ContentScale.FillBounds,
                        sizeToIntrinsics = false
                    )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                }
            )
    ) {
        // Indicador de carga superpuesto
        if (coverState is AsyncImagePainter.State.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Estado de error
        if (coverState is AsyncImagePainter.State.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Gradiente overlay sutil de abajo hacia arriba
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Botón editar portada — esquina inferior derecha
        if (onChangeCover != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .background(
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .clickable { onChangeCover() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Editar portada",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        // Botón back — esquina superior izquierda, superpuesto sobre portada
        if (onNavigateBack != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 8.dp),
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Avatar + badge de cámara, solapando la portada
    Box(
        modifier = Modifier
            .padding(start = 20.dp)
            .offset(y = (-48).dp)
    ) {
        Surface(
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface)
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                NetworkImage(
                    url = avatarUrl,
                    contentDescription = "Avatar de la sucursal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback con inicial del nombre
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Text(
                        text = avatarInitial,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
        }

        // Badge de cámara
        IconButton(
            onClick = onChangeAvatar,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .then(
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar avatar",
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
