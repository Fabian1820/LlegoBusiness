package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.DeliveryPersonInfo
import com.llego.business.orders.data.model.VehicleType

/**
 * SecciÃ³n de informaciÃ³n del repartidor
 * Requirements: 10.4
 * 
 * @param deliveryPerson InformaciÃ³n del repartidor
 * @param onCallDeliveryPerson Callback para llamar al repartidor
 */
@Composable
fun DeliveryPersonSection(
    deliveryPerson: DeliveryPersonInfo?,
    onCallDeliveryPerson: ((String) -> Unit)? = null
) {
    if (deliveryPerson == null) return
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repartidor Asignado",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                // Indicador de estado online
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (deliveryPerson.isOnline) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (deliveryPerson.isOnline) Color(0xFF4CAF50) else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = if (deliveryPerson.isOnline) "En lÃ­nea" else "Desconectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (deliveryPerson.isOnline) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del repartidor
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deliveryPerson.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.1f", deliveryPerson.rating),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "(${deliveryPerson.totalDeliveries} entregas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Tipo de vehÃ­culo
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getVehicleIcon(deliveryPerson.vehicleType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = deliveryPerson.vehicleType.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        deliveryPerson.vehiclePlate?.let { plate ->
                            Text(
                                text = "â€¢ $plate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // BotÃ³n de llamar
                if (onCallDeliveryPerson != null) {
                    FilledIconButton(
                        onClick = { onCallDeliveryPerson(deliveryPerson.phone) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Llamar al repartidor",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // TelÃ©fono
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = onCallDeliveryPerson != null) {
                        onCallDeliveryPerson?.invoke(deliveryPerson.phone)
                    }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = deliveryPerson.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Obtiene el icono correspondiente al tipo de vehÃ­culo
 */
private fun getVehicleIcon(vehicleType: VehicleType): ImageVector {
    return when (vehicleType) {
        VehicleType.MOTO -> Icons.Default.TwoWheeler
        VehicleType.BICICLETA -> Icons.Default.PedalBike
        VehicleType.AUTO -> Icons.Default.DirectionsCar
        VehicleType.A_PIE -> Icons.Default.DirectionsWalk
    }
}
