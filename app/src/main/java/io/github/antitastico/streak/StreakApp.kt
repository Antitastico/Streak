package io.github.antitastico.streak

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.antitastico.streak.ui.theme.HomeScreen

/**
 * Punto donde se arma la app.
 *
 * Creamos el "cerebro" (StreakState) una sola vez con 'remember' y se lo
 * entregamos a la pantalla. La pantalla lee de él y le pide acciones;
 * nunca guarda la lógica por su cuenta.
 */
@Composable
fun StreakApp() {
    val state = remember { StreakState() }
    HomeScreen(state = state)
}
