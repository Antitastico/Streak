package io.github.antitastico.streak

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.antitastico.streak.ui.theme.CalendarScreen
import io.github.antitastico.streak.ui.theme.HomeScreen
import io.github.antitastico.streak.ui.theme.StatsScreen
import io.github.antitastico.streak.ui.theme.StreakTheme

/**
 * Arma la app: crea el "cerebro" (con almacenamiento local), aplica el tema según
 * el estilo, y muestra una barra de navegación inferior con 3 secciones.
 */
@Composable
fun StreakApp() {
    val context = LocalContext.current
    val state = remember { StreakState(HabitStore(context)) }

    StreakTheme(style = state.style) {
        var tab by rememberSaveable { mutableStateOf(0) }
        val modern = state.style == UiStyle.MODERN

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Text(if (modern) "🏠" else "◉") },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Text(if (modern) "📅" else "▦") },
                        label = { Text("Calendario") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Text(if (modern) "📊" else "▤") },
                        label = { Text("Estadísticas") }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (tab) {
                    0 -> HomeScreen(state)
                    1 -> CalendarScreen(state)
                    else -> StatsScreen(state)
                }
            }
        }
    }
}
