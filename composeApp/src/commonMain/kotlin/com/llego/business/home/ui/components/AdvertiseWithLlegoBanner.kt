package com.llego.business.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.theme.LlegoAccent
import com.llego.shared.ui.theme.LlegoPrimary
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.ads_promo_banner
import llegobusiness.composeapp.generated.resources.social_whatsapp
import org.jetbrains.compose.resources.painterResource

// Número comercial de Llego para vender promociones (Cuba, +53).
private const val LLEGO_ADS_WHATSAPP_DISPLAY = "5483 0854"
private const val LLEGO_ADS_WHATSAPP_URL =
    "https://wa.me/5354830854?text=Hola%20Llego%2C%20quiero%20poner%20una%20promoci%C3%B3n%20de%20mi%20negocio%20en%20la%20app%20de%20clientes"

/**
 * Banner estilo tienda que invita al negocio a anunciarse en la app de clientes.
 * Al tocarlo abre WhatsApp con el equipo comercial de Llego.
 */
@Composable
fun AdvertiseWithLlegoBanner(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { runCatching { uriHandler.openUri(LLEGO_ADS_WHATSAPP_URL) } }
    ) {
        Image(
            painter = painterResource(Res.drawable.ads_promo_banner),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            LlegoPrimary.copy(alpha = 0.95f),
                            LlegoPrimary.copy(alpha = 0.8f),
                            LlegoPrimary.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = LlegoAccent
                ) {
                    Text(
                        text = "PUBLICIDAD EN LLEGO",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = LlegoPrimary
                    )
                }
                Text(
                    text = "Pon tus promociones en la app de clientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Text(
                    text = "Banners, destacados y ofertas de todo tipo a precios competitivos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.social_whatsapp),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Escríbenos al $LLEGO_ADS_WHATSAPP_DISPLAY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = LlegoPrimary
                    )
                }
            }
        }
    }
}
