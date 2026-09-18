package io.github.antitastico.streak

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

    // Al volver a la app, recarga desde el almacenamiento (por si el widget o la
    // notificación marcaron un cumplido mientras estaba en segundo plano).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state.reload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
