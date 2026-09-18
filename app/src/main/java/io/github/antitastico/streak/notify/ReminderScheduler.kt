package io.github.antitastico.streak.notify

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/** Programa el recordatorio diario de las 22:00 con WorkManager (sobrevive reinicios). */
object ReminderScheduler {

    private const val UNIQUE = "streak_nightly_reminder"
    private const val HOUR = 22
    private const val MINUTE = 0

    fun scheduleDaily(context: Context) {
        val delay = millisUntilNext()
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    private fun millisUntilNext(): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(HOUR).withMinute(MINUTE).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return ChronoUnit.MILLIS.between(now, next)
    }
}
