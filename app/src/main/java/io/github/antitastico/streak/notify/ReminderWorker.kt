package io.github.antitastico.streak.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.antitastico.streak.HabitStats
import io.github.antitastico.streak.HabitStore
import java.time.LocalDate
import kotlin.math.abs

/**
 * Corre cada noche (~22:00). Decide qué notificación mostrar para el hábito
 * predeterminado:
 *  - no hecho hoy  -> recordatorio urgente con acciones (o ánimo si se rompió una racha)
 *  - hecho hoy     -> felicitación si la racha llegó a un hito
 */
class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        Notify.ensureChannel(ctx)

        val habit = HabitStore(ctx).defaultHabit() ?: return Result.success()
        val today = LocalDate.now()
        val done = today in habit.completions
        val streak = HabitStats.currentStreak(habit.completions, today)
        val prefs = ctx.getSharedPreferences("streak_notif", Context.MODE_PRIVATE)

        if (done) {
            if (isMilestone(streak) && prefs.getInt("congrats", -1) != streak) {
                Notify.showMessage(ctx, "¡$streak días seguidos! 🔥", congrats(streak))
                prefs.edit().putInt("congrats", streak).apply()
            }
            return Result.success()
        }

        // No hecho hoy. ¿Se acaba de romper una racha (ayer tampoco)?
        val last = habit.completions.maxOrNull()
        val yesterdayMissed = today.minusDays(1) !in habit.completions
        val brokenStreak = if (last != null) HabitStats.currentStreak(habit.completions, last) else 0
        val breakKey = last?.toString() ?: ""

        if (last != null && yesterdayMissed && brokenStreak >= 3 &&
            prefs.getString("encouraged", "") != breakKey
        ) {
            Notify.showReminder(ctx, "No pasa nada 🌙", encouragement(brokenStreak))
            prefs.edit().putString("encouraged", breakKey).apply()
        } else {
            Notify.showReminder(ctx, "¿Entrenaste hoy?", "Marca tu ${habit.name} antes de dormir.")
        }
        return Result.success()
    }
}

private fun isMilestone(streak: Int): Boolean =
    streak in intArrayOf(3, 7, 14, 21, 30, 50, 75, 100) || (streak > 0 && streak % 50 == 0)

private fun pick(day: Int, options: List<String>): String =
    options[abs(day) % options.size]

private fun congrats(streak: Int): String = pick(
    streak,
    listOf(
        "$streak días sin fallar. Eso ya es identidad, no motivación.",
        "Racha de $streak días. Lo difícil ya lo estás haciendo: seguir.",
        "$streak días seguidos. Tu yo del futuro te lo agradece.",
        "¡$streak días! La constancia gana, un día a la vez."
    )
)

private fun encouragement(previous: Int): String = pick(
    previous,
    listOf(
        "Venías de $previous días. Una falla no borra el progreso: hoy empiezas de nuevo.",
        "Perder una racha de $previous días duele, pero volver es lo que cuenta. ¿Lo marcas hoy?",
        "$previous días no se pierden por un tropiezo. El día 1 de la próxima racha es hoy.",
        "Nadie es perfecto. Retoma hoy y esa racha de $previous vuelve a crecer."
    )
)
