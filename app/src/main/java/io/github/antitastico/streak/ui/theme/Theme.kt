package io.github.antitastico.streak.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.antitastico.streak.UiStyle

// ---- Estilo MODERNO (Material 3 con color) ----
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// ---- Estilo MINIMAL (blanco y negro) ----
private val MinimalLight = lightColorScheme(
    primary = Color(0xFF0A0A0A),
    onPrimary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F1F1),
    onSecondaryContainer = Color(0xFF0A0A0A),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A0A0A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A0A0A),
    onSurfaceVariant = Color(0xFF5A5A5A),
    outline = Color(0xFF111111),
    outlineVariant = Color(0xFFE4E4E4)
)

private val MinimalDark = darkColorScheme(
    primary = Color(0xFFF4F4F4),
    onPrimary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFF4F4F4),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF4F4F4),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFF4F4F4),
    onSurfaceVariant = Color(0xFF9B9B9B),
    outline = Color(0xFFEAEAEA),
    outlineVariant = Color(0xFF242424)
)

/**
 * Tema de la app. Elige la paleta según el estilo seleccionado por el usuario.
 * - MINIMAL: blanco y negro fijo (sin color dinámico).
 * - MODERN: Material 3 con color dinámico (Android 12+) o la paleta morada.
 */
@Composable
fun StreakTheme(
    style: UiStyle = UiStyle.MODERN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (style) {
        UiStyle.MINIMAL -> if (darkTheme) MinimalDark else MinimalLight
        UiStyle.MODERN -> when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
