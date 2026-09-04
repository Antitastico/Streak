package io.github.antitastico.streak

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.antitastico.streak.ui.theme.HomeScreen
import io.github.antitastico.streak.ui.theme.OnboardingScreen
import io.github.antitastico.streak.ui.theme.StreakTheme

/**
 * Arma la app: crea el "cerebro" (con almacenamiento local) y aplica el tema.
 * La primera vez muestra el onboarding; después, la pantalla principal (Home).
 */
@Composable
fun StreakApp() {
    val context = LocalContext.current
    val state = remember { StreakState(HabitStore(context)) }

    StreakTheme(style = state.style) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!state.onboarded) {
                OnboardingScreen(
                    onFinish = { name, chosen -> state.completeOnboarding(name, chosen) }
                )
            } else {
                HomeScreen(state)
            }
        }
    }
}
