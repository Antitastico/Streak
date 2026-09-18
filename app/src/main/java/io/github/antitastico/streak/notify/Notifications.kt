package io.github.antitastico.streak.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.antitastico.streak.MainActivity
import io.github.antitastico.streak.R

/** Construye y muestra las notificaciones (recordatorio, felicitación, ánimo). */
object Notify {

    const val CHANNEL_ID = "streak_reminders"
    private const val CHANNEL_NAME = "Recordatorios"
    private const val ID_REMINDER = 1001
    private const val ID_MESSAGE = 1002

    fun ensureChannel(context: Context) {
        val ch = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorio nocturno y mensajes de racha"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun actionIntent(context: Context, action: String, reqCode: Int): PendingIntent {
        val i = Intent(context, HabitActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context, reqCode, i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun base(context: Context) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_streak)
        .setColor(0xFF0A0A0A.toInt())
        .setContentIntent(openAppIntent(context))
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    /** Recordatorio urgente con acciones para marcar si entrenó o no. */
    fun showReminder(context: Context, title: String, body: String) {
        val n = base(context)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .addAction(0, "Sí, lo hice", actionIntent(context, HabitActionReceiver.ACTION_DONE, 11))
            .addAction(0, "Hoy no", actionIntent(context, HabitActionReceiver.ACTION_DISMISS, 12))
            .build()
        safeNotify(context, ID_REMINDER, n)
    }

    /** Mensaje simple (felicitación o ánimo). */
    fun showMessage(context: Context, title: String, body: String) {
        val n = base(context)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        safeNotify(context, ID_MESSAGE, n)
    }

    fun cancelReminder(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_REMINDER)
    }

    private fun safeNotify(context: Context, id: Int, n: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) {
            // Sin permiso de notificaciones: lo ignoramos silenciosamente.
        }
    }
}
