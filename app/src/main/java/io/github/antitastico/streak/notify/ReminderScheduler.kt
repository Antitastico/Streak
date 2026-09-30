package io.github.antitastico.streak.notify

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.antitastico.streak.HabitStore
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Programa (o cancela) el recordatorio diario con WorkManager, según los ajustes
 * guardados (activado + hora). Sobrevive reinicios.
 */
object ReminderScheduler {

    private const val UNIQUE = "streak_nightly_reminder"

    /** Aplica los ajustes actuales: reprograma a la hora elegida o cancela. */
    fun apply(context: Context) {
        val data = HabitStore(context).load()
        val enabled = data?.reminderEnabled ?: true
        val hour = data?.reminderHour ?: 22
        val minute = data?.reminderMinute ?: 0
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(UNIQUE)
            return
        }
        val delay = millisUntilNext(hour, minute)
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, req)
    }

    private fun millisUntilNext(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return ChronoUnit.MILLIS.between(now, next)
    }
}
