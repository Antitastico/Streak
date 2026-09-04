package io.github.antitastico.streak

import java.time.LocalDate

/**
 * Un hábito. Ahora, en vez de un simple contador, guardamos EL CONJUNTO DE FECHAS
 * en las que se marcó (completions). De ahí calculamos racha, calendario y stats.
 *
 * 'val' en todo (inmutable): para cambiarlo hacemos .copy(...), y así Compose y el
 * almacenamiento detectan el cambio.
 */
data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val completions: Set<LocalDate> = emptySet()
) {
    /** ¿Se marcó hoy? */
    val doneToday: Boolean get() = LocalDate.now() in completions

    /** Racha actual (días consecutivos hasta hoy o ayer). */
    val streak: Int get() = HabitStats.currentStreak(completions, LocalDate.now())
}

/** Estilo visual seleccionable. */
enum class UiStyle { MODERN, MINIMAL }

/** Inicial del hábito, para el estilo Minimal (en vez del emoji). */
fun monogram(name: String): String {
    val t = name.trim()
    return if (t.isEmpty()) "?" else t.substring(0, 1).uppercase()
}

/** Hábitos de ejemplo para el primer arranque (con rachas ya "vividas"). */
fun seedHabits(today: LocalDate = LocalDate.now()): List<Habit> {
    fun lastDays(n: Int): Set<LocalDate> =
        (0 until n).map { today.minusDays(it.toLong()) }.toSet()
    return listOf(
        Habit(1, "Correr", "🏃", lastDays(12)),
        Habit(2, "Leer", "📖", lastDays(4)),
        Habit(3, "Agua", "💧", lastDays(30)),
        Habit(4, "Meditar", "🧘", lastDays(7))
    )
}
