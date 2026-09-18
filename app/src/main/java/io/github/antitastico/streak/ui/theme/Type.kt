package io.github.antitastico.streak.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía hiper-minimalista: sans-serif del sistema (Roboto) en pesos
 * delgados (Thin / Light), recta — nunca cursiva — con un poco de aire entre
 * letras para una lectura delicada.
 */
private val Sans = FontFamily.SansSerif

private fun light(
    size: Int,
    line: Int,
    weight: FontWeight = FontWeight.Light,
    spacing: Double = 0.0
) = TextStyle(
    fontFamily = Sans,
    fontWeight = weight,
    fontStyle = FontStyle.Normal,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

val Typography = Typography(
    displayLarge   = light(56, 60, FontWeight.Thin, (-0.5)),
    displayMedium  = light(44, 50, FontWeight.Thin),
    displaySmall   = light(36, 42, FontWeight.Thin),
    headlineLarge  = light(32, 38, FontWeight.Thin),
    headlineMedium = light(28, 34, FontWeight.Light),
    headlineSmall  = light(24, 30, FontWeight.Light),
    titleLarge     = light(22, 28, FontWeight.Light, 0.2),
    titleMedium    = light(17, 24, FontWeight.Light, 0.3),
    titleSmall     = light(15, 20, FontWeight.Normal, 0.2),
    bodyLarge      = light(16, 24, FontWeight.Light, 0.3),
    bodyMedium     = light(14, 20, FontWeight.Light, 0.2),
    bodySmall      = light(12, 16, FontWeight.Light, 0.2),
    labelLarge     = light(14, 20, FontWeight.Normal, 0.6),
    labelMedium    = light(12, 16, FontWeight.Normal, 0.5),
    labelSmall     = light(11, 16, FontWeight.Normal, 0.5)
)
