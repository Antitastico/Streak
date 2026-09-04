package io.github.antitastico.streak

import java.time.LocalDate

/**
 * Un hábito. Guardamos EL CONJUNTO DE FECHAS de check-in (completions);
 * la racha, el calendario y las estadísticas se calculan a partir de esas fechas.
 */
data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val completions: Set<LocalDate> = emptySet()
) {
    val doneToday: Boolean get() = LocalDate.now() in completions
    val streak: Int get() = HabitStats.currentStreak(completions, LocalDate.now())
}

/** Estilo visual seleccionable. */
enum class UiStyle { MODERN, MINIMAL }

/** Inicial del hábito, para el estilo Minimal (en vez del emoji). */
fun monogram(name: String): String {
    val t = name.trim()
    return if (t.isEmpty()) "?" else t.substring(0, 1).uppercase()
}

/** Catálogo de hábitos sugeridos para el onboarding (nombre a emoji). */
val predefinedHabits: List<Pair<String, String>> = listOf(
    "Correr" to "🏃",
    "Leer" to "📖",
    "Beber agua" to "💧",
    "Meditar" to "🧘",
    "Entrenar" to "🏋️",
    "Comer sano" to "🥗",
    "Dormir bien" to "😴",
    "Estudiar" to "🖊️",
    "Caminar" to "🚶",
    "Gratitud" to "🙏"
)
