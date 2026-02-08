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

@Composable
internal fun BusinessAvatar(
    avatarUrl: String?,
    name: String,
    size: Int
) {
    val sizeDp = size.dp
    val hasPhoto = !avatarUrl.isNullOrBlank()

    Surface(
        modifier = Modifier.size(sizeDp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.2f),
        shadowElevation = 0.dp
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
internal fun BranchAvatar(branch: Branch) {
    val hasPhoto = !branch.avatarUrl.isNullOrBlank()
    val typeColor = branchTypeColor(branch.tipos.firstOrNull())

    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (hasPhoto) Color.Transparent else typeColor.copy(alpha = 0.1f),
        shadowElevation = 0.dp
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = branch.avatarUrl,
                contentDescription = branch.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = branchTypeIcon(branch.tipos.firstOrNull()),
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun GroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
internal fun BranchRow(
    branch: Branch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BranchAvatar(branch = branch)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = branch.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                branch.tipos.take(2).forEach { tipo ->
                    BranchTypeChip(tipo = tipo)
                }
            }

            if (!branch.address.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (branch.status == "active") Color(0xFF34C759)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Seleccionar",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
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
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Agregar sucursal",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 78.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    )
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            fontSize = 13.sp
        ),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
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
