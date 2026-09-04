package io.github.antitastico.streak

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Funciones puras (sin UI) que calculan las estadísticas a partir de las fechas
 * de check-in. Al ser puras son fáciles de entender y de probar.
 */
object HabitStats {

    /** Racha actual: días consecutivos terminando hoy (o ayer, como gracia). */
    fun currentStreak(completions: Set<LocalDate>, today: LocalDate): Int {
        if (completions.isEmpty()) return 0
        var day = if (today in completions) today else today.minusDays(1)
        if (day !in completions) return 0
        var count = 0
        while (day in completions) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** Racha más larga jamás lograda. */
    fun longestStreak(completions: Set<LocalDate>): Int {
        if (completions.isEmpty()) return 0
        val sorted = completions.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            if (ChronoUnit.DAYS.between(sorted[i - 1], sorted[i]) == 1L) {
                run++
                best = maxOf(best, run)
            } else {
                run = 1
            }
        }
        return best
    }

    /** % de constancia en los últimos [windowDays] días (0..100). */
    fun consistency(completions: Set<LocalDate>, today: LocalDate, windowDays: Int = 30): Int {
        val start = today.minusDays((windowDays - 1).toLong())
        val hits = completions.count { !it.isBefore(start) && !it.isAfter(today) }
        return (hits * 100) / windowDays
    }

    /** Total de check-ins. */
    fun total(completions: Set<LocalDate>): Int = completions.size

    /** Check-ins por semana en las últimas [weeks] semanas (de más vieja a más nueva). */
    fun weeklyCounts(completions: Set<LocalDate>, today: LocalDate, weeks: Int = 8): List<Int> {
        return (weeks - 1 downTo 0).map { w ->
            val end = today.minusDays((w * 7).toLong())
            val start = end.minusDays(6)
            completions.count { !it.isBefore(start) && !it.isAfter(end) }
        }
    }

    /** Últimos [days] días como pares (fecha, hecho) — para el heatmap. */
    fun lastDays(completions: Set<LocalDate>, today: LocalDate, days: Int = 35): List<Pair<LocalDate, Boolean>> {
        return (days - 1 downTo 0).map { d ->
            val date = today.minusDays(d.toLong())
            date to (date in completions)
        }
    }
}
