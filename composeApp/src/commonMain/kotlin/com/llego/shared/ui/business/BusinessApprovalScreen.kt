package com.llego.shared.ui.business

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.ui.branch.BranchSelectorViewModel
import com.llego.shared.ui.theme.LlegoAccent
import com.llego.shared.ui.theme.LlegoError
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.ui.theme.LlegoSecondary
import kotlinx.coroutines.delay

private const val PENDING_POLL_INTERVAL_MS = 30_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessApprovalScreen(
    business: BusinessWithBranches,
    selectedBranch: Branch?,
    branchSelectorViewModel: BranchSelectorViewModel,
    onBranchSelected: (Branch) -> Unit,
    onStartNewBusiness: () -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by branchSelectorViewModel.uiState.collectAsState()
    val isRefreshing = uiState.isRefreshing

    // Estado de aprobación actualizable por polling
    var currentStatus by remember(business.id) { mutableStateOf(business.approvalStatus) }
    var currentRejectionReason by remember(business.id) { mutableStateOf(business.rejectionReason) }

    // Polling automático mientras esté pending
    LaunchedEffect(business.id, currentStatus) {
        if (currentStatus == "pending") {
            while (true) {
                delay(PENDING_POLL_INTERVAL_MS)
                branchSelectorViewModel.refreshBusinesses()
            }
        }
    }

    // Observar cambios del estado tras polling
    LaunchedEffect(uiState.businessesWithBranches) {
        val updated = uiState.businessesWithBranches.firstOrNull { it.id == business.id }
        if (updated != null && updated.approvalStatus != currentStatus) {
            currentStatus = updated.approvalStatus
            currentRejectionReason = updated.rejectionReason
        }
    }

    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { branchSelectorViewModel.refreshBusinesses() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LlegoPrimary, Color(0xFF034A4D))
                    )
                )
        ) {
            // Botón de retroceso
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 4.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                when (currentStatus) {
                    "approved" -> ApprovedContent(businessName = business.name)
                    "rejected" -> RejectedContent(
                        businessName = business.name,
                        rejectionReason = currentRejectionReason
                    )
                    else -> PendingContent(businessName = business.name)
                }

                Spacer(modifier = Modifier.height(40.dp))

                when (currentStatus) {
                    "approved" -> {
                        if (selectedBranch != null) {
                            Button(
                                onClick = { onBranchSelected(selectedBranch) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LlegoAccent,
                                    contentColor = LlegoPrimary
                                )
                            ) {
                                Text(
                                    text = "Comenzar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }
                    "rejected" -> {
                        Button(
                            onClick = onStartNewBusiness,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LlegoSecondary,
                                contentColor = LlegoPrimary
                            )
                        ) {
                            Text(
                                text = "Crear un nuevo negocio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Text(text = "Volver a mis negocios", fontSize = 15.sp)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(text = "Volver a mis negocios", fontSize = 15.sp)
                        }
                    }
                }

                if (currentStatus == "pending") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Desliza hacia abajo para actualizar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.40f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingContent(businessName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.HourglassEmpty,
            contentDescription = null,
            tint = LlegoSecondary,
            modifier = Modifier
                .size(54.dp)
                .rotate(rotation)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "En revisión",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = businessName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = LlegoSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.09f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu negocio está siendo revisado por nuestro equipo. Esto puede tomar algunas horas.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusStep(icon = "✓", label = "Negocio creado", done = true)
            Spacer(modifier = Modifier.height(8.dp))
            StatusStep(icon = "⏳", label = "Revisión del equipo", done = false, active = true)
            Spacer(modifier = Modifier.height(8.dp))
            StatusStep(icon = "🚀", label = "¡Listo para operar!", done = false)
        }
    }
}

@Composable
private fun ApprovedContent(businessName: String) {
    Box(
        modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(LlegoAccent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = LlegoAccent,
            modifier = Modifier.size(58.dp)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "¡Negocio aprobado!",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = businessName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = LlegoSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.09f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu negocio ha sido aprobado y ya puede empezar a operar. Toca Comenzar para entrar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusStep(icon = "✓", label = "Negocio creado", done = true)
            Spacer(modifier = Modifier.height(8.dp))
            StatusStep(icon = "✓", label = "Revisión del equipo", done = true)
            Spacer(modifier = Modifier.height(8.dp))
            StatusStep(icon = "🚀", label = "¡Listo para operar!", done = true, active = true)
        }
    }
}

@Composable
private fun RejectedContent(businessName: String, rejectionReason: String?) {
    Box(
        modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(LlegoError.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Block,
            contentDescription = null,
            tint = Color(0xFFFF6B6B),
            modifier = Modifier.size(54.dp)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Solicitud rechazada",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = businessName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = LlegoSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.09f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Motivo del rechazo",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFF6B6B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rejectionReason ?: "No se proporcionó un motivo específico.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Puedes registrar un nuevo negocio corrigiendo los problemas indicados.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StatusStep(icon: String, label: String, done: Boolean, active: Boolean = false) {
    val textColor = when {
        done -> LlegoAccent
        active -> Color.White
        else -> Color.White.copy(alpha = 0.40f)
    }
    val bgColor = when {
        done -> LlegoAccent.copy(alpha = 0.18f)
        active -> LlegoSecondary.copy(alpha = 0.22f)
        else -> Color.White.copy(alpha = 0.06f)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "$icon  $label",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (active || done) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}
