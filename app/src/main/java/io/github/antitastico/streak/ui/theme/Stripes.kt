package io.github.antitastico.streak.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Patrón de rayas diagonales (45°) dibujado detrás del contenido — el "shader"
 * de las referencias. Úsalo con `Modifier.clip(shape)` antes para recortarlo.
 */
fun Modifier.diagonalStripes(
    color: Color,
    stripe: Float = 3f,
    gap: Float = 9f
): Modifier = drawBehind {
    val step = stripe + gap
    val h = size.height
    var x = -h
    val end = size.width + h
    while (x < end) {
        drawLine(
            color = color,
            start = Offset(x, h),
            end = Offset(x + h, 0f),
            strokeWidth = stripe
        )
        x += step
    }
}
