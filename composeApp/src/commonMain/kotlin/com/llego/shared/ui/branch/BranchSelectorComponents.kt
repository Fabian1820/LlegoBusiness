package com.llego.shared.ui.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo

/**
 * Avatar del negocio: círculo con inicial o foto.
 * Matches Pencil design: 48dp rounded square with colored background + white letter.
 */
@Composable
internal fun BusinessAvatar(
    avatarUrl: String?,
    name: String,
    size: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    val sizeDp = size.dp
    val hasPhoto = !avatarUrl.isNullOrBlank()

    Surface(
        modifier = Modifier.size(sizeDp),
        shape = RoundedCornerShape(12.dp),
        color = if (hasPhoto) Color.Transparent else backgroundColor,
        shadowElevation = 0.dp
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Branch row item matching Pencil: map-pin icon in colored circle + name + address + status dot + chevron.
 */
@Composable
internal fun BranchRow(
    branch: Branch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Map pin icon in colored circle
        val typeColor = branchTypeColor(branch.tipos.firstOrNull())
        val hasPhoto = !branch.avatarUrl.isNullOrBlank()

        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (hasPhoto) Color.Transparent else typeColor.copy(alpha = 0.12f),
            shadowElevation = 0.dp
        ) {
            if (hasPhoto) {
                AsyncImage(
                    model = branch.avatarUrl,
                    contentDescription = branch.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = branch.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!branch.address.isNullOrBlank()) {
                Text(
                    text = branch.address,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Status dot
        val isActive = branch.status == "active"
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color(0xFF4CAF50)
                    else Color(0xFFFF9800)
                )
        )

        // Paused label if not active
        if (!isActive) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Pausado",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = Color(0xFFE65100)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Seleccionar",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun GroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
internal fun BranchTypeChip(tipo: BranchTipo) {
    val color = branchTypeColor(tipo)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = branchTypeLabel(tipo),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = color
        )
    }
}

@Composable
internal fun AddBranchRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Agregar sucursal",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    )
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
internal fun branchTypeColor(tipo: BranchTipo?): Color {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> MaterialTheme.colorScheme.secondary
        BranchTipo.TIENDA -> MaterialTheme.colorScheme.primary
        BranchTipo.DULCERIA -> MaterialTheme.colorScheme.tertiary
        BranchTipo.CAFE -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.primary
    }
}

internal fun branchTypeIcon(tipo: BranchTipo?): ImageVector {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> Icons.Outlined.Restaurant
        BranchTipo.TIENDA -> Icons.Outlined.Storefront
        BranchTipo.DULCERIA -> Icons.Outlined.Cake
        BranchTipo.CAFE -> Icons.Outlined.LocalCafe
        null -> Icons.Outlined.Store
    }
}

internal fun branchTypeLabel(tipo: BranchTipo): String {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> "Restaurante"
        BranchTipo.TIENDA -> "Tienda"
        BranchTipo.DULCERIA -> "Dulcería"
        BranchTipo.CAFE -> "Cafe"
    }
}
