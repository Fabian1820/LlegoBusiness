package com.llego.business.tutorials.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Pantalla de Tutoriales de la Aplicación
 *
 * Ofrece videos tutoriales cortos sobre el uso de la aplicación Llego Business
 * con diseño elegante y moderna siguiendo la paleta de colores Llego.
 *
 * Categorías:
 * - Primeros Pasos
 * - Gestión de Productos
 * - Configuración
 * - Pedidos y Entregas
 * - Wallet y Pagos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<TutorialCategory?>(null) }
    var selectedTutorial by remember { mutableStateOf<Tutorial?>(null) }
    var animateContent by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Botón de navegación flotante
        IconButton(
            onClick = {
                when {
                    selectedTutorial != null -> selectedTutorial = null
                    selectedCategory != null -> selectedCategory = null
                    else -> onNavigateBack()
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .shadow(4.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
        ) {
            when {
                selectedTutorial != null -> {
                    // Vista de reproducción de tutorial individual
                    TutorialPlayerView(
                        tutorial = selectedTutorial!!,
                        onClose = { selectedTutorial = null }
                    )
                }
                selectedCategory != null -> {
                    // Vista de tutoriales de una categoría específica
                    CategoryTutorialsView(
                        category = selectedCategory!!,
                        onTutorialClick = { tutorial -> selectedTutorial = tutorial }
                    )
                }
                else -> {
                    // Vista principal con todas las categorías
                    TutorialCategoriesView(
                        onCategoryClick = { category -> selectedCategory = category }
                    )
                }
            }
        }
    }
}

/**
 * Vista principal con categorías de tutoriales
 */
@Composable
private fun TutorialCategoriesView(
    onCategoryClick: (TutorialCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con estadísticas
        item {
            TutorialsHeaderCard()
        }

        // Categorías de tutoriales
        items(TutorialCategory.values().toList()) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Header con estadísticas de tutoriales
 */
@Composable
private fun TutorialsHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "¡Bienvenido!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aprende a maximizar tu experiencia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(12.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                // Estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = "18",
                        label = "Videos",
                        icon = Icons.Default.VideoLibrary,
                        color = MaterialTheme.colorScheme.primary // Llego Primary
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    StatItem(
                        value = "2-5",
                        label = "Minutos",
                        icon = Icons.Default.Timer,
                        color = MaterialTheme.colorScheme.secondary // Llego Secondary
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    StatItem(
                        value = "5",
                        label = "Categorías",
                        icon = Icons.Default.Category,
                        color = Color(0xFF035658) // Llego Primary Variante
                    )
                }
            }
        }
    }
}

/**
 * Item de estadística individual
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

/**
 * Card de categoría de tutoriales
 */
@Composable
private fun CategoryCard(
    category: TutorialCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icono de categoría
                    Surface(
                        shape = CircleShape,
                        color = category.color.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, category.color.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier
                                .size(56.dp)
                                .padding(14.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Badge con cantidad de videos
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = category.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${category.getTutorials().size} videos",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = category.color
                            )
                        }
                    }
                }

                // Flecha
                Surface(
                    shape = CircleShape,
                    color = category.color.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Vista de tutoriales de una categoría específica
 */
@Composable
private fun CategoryTutorialsView(
    category: TutorialCategory,
    onTutorialClick: (Tutorial) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header de categoría
        item {
            CategoryHeaderCard(category = category)
        }

        // Lista de tutoriales
        items(category.getTutorials()) { tutorial ->
            TutorialCard(
                tutorial = tutorial,
                categoryColor = category.color,
                onClick = { onTutorialClick(tutorial) }
            )
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Header de categoría
 */
@Composable
private fun CategoryHeaderCard(category: TutorialCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = category.color.copy(alpha = 0.2f),
                        border = BorderStroke(3.dp, category.color.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(14.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card de tutorial individual
 */
@Composable
private fun TutorialCard(
    tutorial: Tutorial,
    categoryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail simulado con icono de play
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = categoryColor,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                    )
                }
            }

            // Información del tutorial
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = tutorial.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duración
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tutorial.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Nivel
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tutorial.level.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = tutorial.level.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = tutorial.level.color
                        )
                    }
                }
            }

            // Badge de nuevo si aplica
            if (tutorial.isNew) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE1C78E) // Llego Secondary
                ) {
                    Text(
                        text = "NUEVO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF023133) // Llego Primary
                    )
                }
            }
        }
    }
}

/**
 * Vista del reproductor de tutorial
 */
@Composable
private fun TutorialPlayerView(
    tutorial: Tutorial,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp)
            .background(Color.Black)
    ) {
        // Player simulado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp)
                    )
                }
                Text(
                    text = "Video Tutorial",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = "(Simulado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Información del tutorial
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TutorialInfoCard(tutorial = tutorial)
            }

            item {
                TutorialDescriptionCard(tutorial = tutorial)
            }

            // Tutoriales relacionados
            item {
                Text(
                    text = "Tutoriales Relacionados",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Text(
                    text = "Próximamente...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Card de información del tutorial
 */
@Composable
private fun TutorialInfoCard(tutorial: Tutorial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = tutorial.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.AccessTime,
                    label = tutorial.duration,
                    color = Color(0xFF023133) // Llego Primary
                )
                InfoChip(
                    icon = Icons.Default.BarChart,
                    label = tutorial.level.displayName,
                    color = tutorial.level.color
                )
                if (tutorial.isNew) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE1C78E) // Llego Secondary para badge NUEVO
                    ) {
                        Text(
                            text = "NUEVO",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF023133) // Texto en color primario
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip de información
 */
@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        }
    }
}

/**
 * Card de descripción del tutorial
 */
@Composable
private fun TutorialDescriptionCard(tutorial: Tutorial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tutorial.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

// ===== MODELOS DE DATOS =====

/**
 * Enum de categorías de tutoriales
 */
enum class TutorialCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
) {
    GETTING_STARTED(
        title = "Primeros Pasos",
        description = "Configura tu cuenta y conoce la app",
        icon = Icons.Default.RocketLaunch,
        color = Color(0xFF023133) // Llego Primary
    ),
    PRODUCTS(
        title = "Gestión de Productos",
        description = "Crea, edita y organiza tu menú",
        icon = Icons.Default.Restaurant,
        color = Color(0xFFE1C78E) // Llego Secondary
    ),
    SETTINGS(
        title = "Configuración",
        description = "Personaliza tu negocio",
        icon = Icons.Default.Settings,
        color = Color(0xFF035658) // Llego Primary Variante
    ),
    ORDERS(
        title = "Pedidos y Entregas",
        description = "Gestiona pedidos eficientemente",
        icon = Icons.Default.DeliveryDining,
        color = Color(0xFF023133) // Llego Primary
    ),
    WALLET(
        title = "Wallet y Pagos",
        description = "Administra tus finanzas",
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFFC9A963) // Llego Secondary Variante
    );

    fun getTutorials(): List<Tutorial> = when (this) {
        GETTING_STARTED -> listOf(
            Tutorial(
                id = "1",
                title = "Bienvenido a Llego Business",
                description = "Conoce las funcionalidades principales de la aplicación y cómo navegar por ella.",
                duration = "2:30",
                level = TutorialLevel.BEGINNER,
                isNew = true
            ),
            Tutorial(
                id = "2",
                title = "Cómo completar tu perfil",
                description = "Aprende a configurar la información de tu negocio y personalizar tu perfil público.",
                duration = "3:45",
                level = TutorialLevel.BEGINNER,
                isNew = true
            ),
            Tutorial(
                id = "3",
                title = "Configuración inicial del negocio",
                description = "Configura horarios, zona de entrega y métodos de pago desde el principio.",
                duration = "4:20",
                level = TutorialLevel.BEGINNER,
                isNew = false
            )
        )
        PRODUCTS -> listOf(
            Tutorial(
                id = "4",
                title = "Cómo crear un producto",
                description = "Guía paso a paso para agregar productos a tu menú con fotos, precios y descripciones.",
                duration = "3:15",
                level = TutorialLevel.BEGINNER,
                isNew = false
            ),
            Tutorial(
                id = "5",
                title = "Organizar productos por categorías",
                description = "Aprende a crear categorías y organizar tu menú de forma efectiva.",
                duration = "2:50",
                level = TutorialLevel.INTERMEDIATE,
                isNew = false
            ),
            Tutorial(
                id = "6",
                title = "Modificar precios y disponibilidad",
                description = "Actualiza precios y marca productos como agotados rápidamente.",
                duration = "2:10",
                level = TutorialLevel.BEGINNER,
                isNew = true
            ),
            Tutorial(
                id = "7",
                title = "Agregar variantes y opciones",
                description = "Crea variantes de productos con diferentes tamaños, sabores o complementos.",
                duration = "4:35",
                level = TutorialLevel.INTERMEDIATE,
                isNew = false
            )
        )
        SETTINGS -> listOf(
            Tutorial(
                id = "8",
                title = "Cómo modificar tu perfil",
                description = "Edita la información de tu negocio, logo, banner y redes sociales.",
                duration = "3:00",
                level = TutorialLevel.BEGINNER,
                isNew = false
            ),
            Tutorial(
                id = "9",
                title = "Configurar horarios de atención",
                description = "Define los horarios en que tu negocio estará disponible para recibir pedidos.",
                duration = "2:40",
                level = TutorialLevel.BEGINNER,
                isNew = false
            ),
            Tutorial(
                id = "10",
                title = "Configurar zonas de entrega",
                description = "Establece el radio de entrega y costos de envío para tu negocio.",
                duration = "3:20",
                level = TutorialLevel.INTERMEDIATE,
                isNew = false
            ),
            Tutorial(
                id = "11",
                title = "Gestión de notificaciones",
                description = "Personaliza las alertas y notificaciones que recibes de la app.",
                duration = "2:15",
                level = TutorialLevel.BEGINNER,
                isNew = false
            )
        )
        ORDERS -> listOf(
            Tutorial(
                id = "12",
                title = "Cómo aceptar un pedido",
                description = "Aprende el proceso completo para aceptar y preparar un pedido.",
                duration = "3:10",
                level = TutorialLevel.BEGINNER,
                isNew = false
            ),
            Tutorial(
                id = "13",
                title = "Gestión del estado de pedidos",
                description = "Actualiza el estado de los pedidos para mantener informados a tus clientes.",
                duration = "2:55",
                level = TutorialLevel.INTERMEDIATE,
                isNew = false
            ),
            Tutorial(
                id = "14",
                title = "Comunicación con clientes",
                description = "Usa el chat integrado para resolver dudas y mantener buena comunicación.",
                duration = "3:30",
                level = TutorialLevel.BEGINNER,
                isNew = true
            ),
            Tutorial(
                id = "15",
                title = "Gestionar cancelaciones",
                description = "Aprende a manejar cancelaciones y reembolsos de forma profesional.",
                duration = "4:00",
                level = TutorialLevel.ADVANCED,
                isNew = false
            )
        )
        WALLET -> listOf(
            Tutorial(
                id = "16",
                title = "Cómo gestionar tu wallet",
                description = "Conoce tu wallet digital, saldo disponible y movimientos.",
                duration = "3:25",
                level = TutorialLevel.BEGINNER,
                isNew = true
            ),
            Tutorial(
                id = "17",
                title = "Solicitar retiros de dinero",
                description = "Aprende a transferir tu saldo a tu cuenta bancaria.",
                duration = "3:50",
                level = TutorialLevel.INTERMEDIATE,
                isNew = false
            ),
            Tutorial(
                id = "18",
                title = "Historial de transacciones",
                description = "Revisa tus ventas, comisiones y movimientos financieros.",
                duration = "2:45",
                level = TutorialLevel.BEGINNER,
                isNew = false
            )
        )
    }
}

/**
 * Nivel de dificultad del tutorial
 */
enum class TutorialLevel(val displayName: String, val color: Color) {
    BEGINNER("Básico", Color(0xFF035658)), // Llego Primary Variante
    INTERMEDIATE("Intermedio", Color(0xFFE1C78E)), // Llego Secondary
    ADVANCED("Avanzado", Color(0xFFC9A963)) // Llego Secondary Variante
}

/**
 * Modelo de datos de tutorial individual
 */
data class Tutorial(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val level: TutorialLevel,
    val isNew: Boolean = false
)
