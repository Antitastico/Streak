package io.github.antitastico.streak

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.antitastico.streak.ui.theme.HomeScreen
import io.github.antitastico.streak.ui.theme.StreakTheme

/**
 * Punto donde se arma la app.
 *
 * Creamos el "cerebro" (StreakState) una sola vez con 'remember' y aplicamos el
 * tema según el estilo elegido. Al cambiar el estilo, esto se vuelve a dibujar y
 * el tema cambia junto con la pantalla.
 */
@Composable
fun StreakApp() {
    val state = remember { StreakState() }
    StreakTheme(style = state.style) {
        HomeScreen(state = state)
    }
}
