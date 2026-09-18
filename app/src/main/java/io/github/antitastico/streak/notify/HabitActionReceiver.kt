package io.github.antitastico.streak.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.antitastico.streak.HabitStore
import io.github.antitastico.streak.widget.StreakWidget

/** Maneja los botones de la notificación (marcar hecho / descartar). */
class HabitActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DONE = "io.github.antitastico.streak.ACTION_DONE"
        const val ACTION_DISMISS = "io.github.antitastico.streak.ACTION_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val ctx = context.applicationContext
        when (intent.action) {
            ACTION_DONE -> {
                val pending = goAsync()
                Thread {
                    try {
                        HabitStore(ctx).markDefaultDoneToday()
                        StreakWidget.updateAll(ctx)
                    } finally {
                        Notify.cancelReminder(ctx)
                        pending.finish()
                    }
                }.start()
            }
            ACTION_DISMISS -> Notify.cancelReminder(ctx)
        }
    }
}
