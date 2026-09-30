package io.github.antitastico.streak

import java.time.LocalDate

/**
 * Un hábito. Guardamos EL CONJUNTO DE FECHAS de check-in (completions) y las de
 * descanso (restDays). La racha, el calendario y las estadísticas se calculan a
 * partir de esas fechas. Un día de descanso NO rompe la racha.
 */
data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val completions: Set<LocalDate> = emptySet(),
    val restDays: Set<LocalDate> = emptySet()
) {
    /** Días que mantienen la racha: cumplidos + descansos. */
    val covered: Set<LocalDate> get() = completions + restDays

    val doneToday: Boolean get() = LocalDate.now() in completions
    val restToday: Boolean get() = LocalDate.now() in restDays

    /** Racha actual: días consecutivos cubiertos (cumplido o descanso). */
    val streak: Int get() = HabitStats.currentStreak(covered, LocalDate.now())

    /** Descansos disponibles hoy (se gana 1 por cada 3 días seguidos). */
    fun availableRests(today: LocalDate = LocalDate.now()): Int =
        HabitStats.availableRests(covered, restDays, today)
}

/** Estilo visual seleccionable. */
enum class UiStyle { MODERN, MINIMAL }

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
