# Fondo Curvo Animado - Compose Multiplatform

Este documento explica cómo replicar el componente de fondo con línea curva animada usado en la app Llego, que divide la pantalla entre un color primario (verde oscuro) y un color de superficie (blanco/gris claro).

## Vista previa del efecto

El componente `CurvedBackground` crea un fondo dividido en dos secciones:
- **Parte superior**: Color primario (verde oscuro `#023133`)
- **Parte inferior**: Color de superficie (gris claro `#F3F3F3`)
- **División**: Línea curva suave que se anima entre diferentes estados

## Ubicación del código

- **Componente**: `composeApp/src/commonMain/kotlin/com/llego/multiplatform/ui/components/background/CurvedBackground.kt`
- **Uso**: `composeApp/src/commonMain/kotlin/com/llego/multiplatform/ui/screens/HomeScreen.kt`

## Código completo

### 1. Componente CurvedBackground

```kotlin
package com.llego.multiplatform.ui.components.background

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun CurvedBackground(
    modifier: Modifier = Modifier,
    homeState: HomeScreenState? = null,
    curveStart: () -> Float = { 0.22f },
    curveEnd: () -> Float = { 0.22f },
    curveInclination: () -> Float = { 0.08f },
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.background

    // Calcular valores según el estado con animación
    val actualCurveStart by animateFloatAsState(
        targetValue = when {
            homeState?.isLoading == true -> 1.0f // Sin curva verde en loading
            homeState?.isInSeeMoreMode == true -> 0.245f // Menos espacio verde
            else -> curveStart()
        },
        animationSpec = tween(durationMillis = 600)
    )

    val actualCurveEnd by animateFloatAsState(
        targetValue = when {
            homeState?.isLoading == true -> 1.0f
            homeState?.isInSeeMoreMode == true -> 0.245f
            else -> curveEnd()
        },
        animationSpec = tween(durationMillis = 600)
    )

    val backgroundOffset by animateFloatAsState(
        targetValue = if (homeState?.isInSeeMoreMode == true) -50f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = backgroundOffset.dp)
        ) {
            drawCurvedBackground(
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                curveStart = actualCurveStart,
                curveEnd = actualCurveEnd,
                curveInclination = curveInclination(),
                showCurve = homeState?.isLoading != true
            )
        }

        content()
    }
}

private fun DrawScope.drawCurvedBackground(
    primaryColor: androidx.compose.ui.graphics.Color,
    surfaceColor: androidx.compose.ui.graphics.Color,
    curveStart: Float,
    curveEnd: Float,
    curveInclination: Float,
    showCurve: Boolean = true
) {
    val width = size.width
    val height = size.height
    val curveStartY = height * curveStart
    val curveEndY = height * curveEnd

    // Dibujar fondo gris (superficie) completo
    drawRect(
        color = surfaceColor,
        size = size
    )

    // Solo dibujar la curva verde si showCurve es true
    if (showCurve) {
        val path = Path().apply {
            // Empezar desde la esquina superior izquierda
            moveTo(0f, 0f)
            // Línea hasta donde empieza la curva
            lineTo(0f, curveStartY)

            // Curva con inclinación configurable
            val curveHeight = height * curveInclination
            val controlPointY = curveStartY + curveHeight

            // Curva cúbica que simula un semicírculo
            cubicTo(
                width * 0.25f, controlPointY,  // Primer punto de control
                width * 0.75f, controlPointY,  // Segundo punto de control
                width, curveEndY                // Punto final
            )

            // Completar el rectángulo verde
            lineTo(width, 0f)
            close()
        }

        // Dibujar la parte verde
        drawPath(
            path = path,
            color = primaryColor
        )
    }
}
```

## Cómo funciona

### 1. Parámetros configurables

- **`curveStart`**: Define dónde inicia la curva en el eje Y (porcentaje de la altura, por defecto `0.22f` = 22%)
- **`curveEnd`**: Define dónde termina la curva en el eje Y (por defecto `0.22f`)
- **`curveInclination`**: Controla qué tan pronunciada es la curva (por defecto `0.08f` = 8% de altura)
- **`homeState`**: Estado opcional para animar la curva según el estado de la UI

### 2. Animaciones

El componente usa `animateFloatAsState` para crear transiciones suaves de 600ms:

- **Estado normal**: Curva al 22% de la altura
- **Modo "Ver más"**: Curva al 24.5% (reduce el espacio verde)
- **Estado de carga**: Sin curva verde (valor 1.0f oculta la curva)
- **Offset vertical**: Desplaza el fondo -50dp hacia arriba en modo "Ver más"

### 3. Dibujado con Canvas

El componente dibuja dos capas:

1. **Capa base**: Rectángulo completo con color de superficie (gris)
2. **Capa superior**: Forma con `Path` que crea:
   - Rectángulo desde el top hasta donde empieza la curva
   - Curva cúbica de Bézier con dos puntos de control
   - Cierre hacia la esquina superior derecha

### 4. Curva de Bézier

La curva se crea con `cubicTo()` usando:

```kotlin
cubicTo(
    width * 0.25f, controlPointY,  // Control 1: 25% del ancho
    width * 0.75f, controlPointY,  // Control 2: 75% del ancho
    width, curveEndY                // Punto final: 100% ancho
)
```

Esto crea una curva suave y simétrica que se ve natural.

## Uso en una pantalla

### Ejemplo básico

```kotlin
@Composable
fun MyScreen() {
    CurvedBackground {
        // Tu contenido aquí
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Contenido sobre el fondo curvo")
        }
    }
}
```

### Ejemplo con animación de estado

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CurvedBackground(homeState = state) {
        // Contenido que se adapta al fondo animado
        Column {
            SearchBar()
            ProductsList()
        }
    }
}
```

### Ejemplo con parámetros personalizados

```kotlin
CurvedBackground(
    curveStart = { 0.30f },        // Curva más abajo (30%)
    curveEnd = { 0.28f },          // Ligera inclinación
    curveInclination = { 0.12f }   // Curva más pronunciada
) {
    // Contenido
}
```

## Personalización

### Cambiar colores

Los colores se toman del `MaterialTheme`:

```kotlin
val primaryColor = MaterialTheme.colorScheme.primary      // Verde oscuro
val surfaceColor = MaterialTheme.colorScheme.background   // Gris claro
```

Para cambiar los colores, modifica tu tema en `Theme.kt`:

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Color(2, 49, 51),        // Tu color primario
    background = Color(0xFFF3F3F3)     // Tu color de fondo
)
```

### Cambiar velocidad de animación

Modifica el `durationMillis` en `animateFloatAsState`:

```kotlin
animationSpec = tween(durationMillis = 600)  // Cambia a 400, 800, etc.
```

### Cambiar forma de la curva

Ajusta los puntos de control en la función `cubicTo()`:

```kotlin
// Curva más cerrada
cubicTo(
    width * 0.30f, controlPointY,  // Más hacia el centro
    width * 0.70f, controlPointY,
    width, curveEndY
)

// Curva más abierta
cubicTo(
    width * 0.20f, controlPointY,  // Más hacia los extremos
    width * 0.80f, controlPointY,
    width, curveEndY
)
```

## Estados animables

El componente responde a tres estados principales:

1. **Normal** (`homeState == null` o valores por defecto)
   - Curva visible al 22% de altura
   - Sin offset vertical

2. **Ver más** (`homeState.isInSeeMoreMode == true`)
   - Curva se reduce al 24.5%
   - Offset de -50dp hacia arriba
   - Da más espacio al contenido

3. **Cargando** (`homeState.isLoading == true`)
   - Oculta la curva verde completamente
   - Muestra solo el fondo gris

## Tips de implementación

1. **Performance**: El `Canvas` se redibuja solo cuando cambian los valores animados
2. **Responsivo**: Usa porcentajes (Float) en lugar de valores fijos (dp) para que funcione en todos los tamaños
3. **Z-Index**: El contenido se renderiza sobre el Canvas automáticamente por el orden en el `Box`
4. **Safe Area**: No olvides ajustar padding para barras de estado en la UI

## Requisitos

- Compose Multiplatform 1.8.2+
- Material 3 (`androidx.compose.material3`)
- Kotlin 2.2.10+

## Referencias

- Archivo original: [CurvedBackground.kt](composeApp/src/commonMain/kotlin/com/llego/multiplatform/ui/components/background/CurvedBackground.kt)
- Ejemplo de uso: [HomeScreen.kt](composeApp/src/commonMain/kotlin/com/llego/multiplatform/ui/screens/HomeScreen.kt) línea 85
