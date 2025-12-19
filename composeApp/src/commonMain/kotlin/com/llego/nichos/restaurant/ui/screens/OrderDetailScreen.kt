package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.components.orders.EstimatedTimeSection
import com.llego.nichos.restaurant.ui.components.orders.OrderActionsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderItemsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderStatusSection
import com.llego.nichos.restaurant.ui.components.orders.PaymentSummarySection
import com.llego.nichos.restaurant.ui.components.orders.SpecialNotesSection

/**
 * Pantalla completa de detalle del pedido con gestión de estados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    onNavigateBack: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onNavigateToChat: ((String, String, String) -> Unit)? = null // orderId, orderNumber, customerName para navegar al chat
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pedido ${order.orderNumber}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estado actual del pedido
                OrderStatusSection(order = order)

                HorizontalDivider()

                // Items del pedido
                OrderItemsSection(items = order.items)

                HorizontalDivider()

                // Notas especiales
                if (order.specialNotes != null) {
                    SpecialNotesSection(notes = order.specialNotes)
                    HorizontalDivider()
                }

                // Resumen de pago
                PaymentSummarySection(
                    paymentMethod = order.paymentMethod,
                    total = order.total
                )

                // Tiempo estimado
                order.estimatedTime?.let { time ->
                    EstimatedTimeSection(estimatedTime = time)
                }
            }

            // Footer con acciones
            OrderActionsSection(
                orderId = order.id,
                orderNumber = order.orderNumber,
                customerName = order.customer.name,
                orderStatus = order.status,
                onUpdateStatus = onUpdateStatus,
                onNavigateToChat = onNavigateToChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
