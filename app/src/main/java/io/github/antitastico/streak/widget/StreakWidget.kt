package io.github.antitastico.streak.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.unit.ColorProvider
import androidx.glance.action.ActionParameters
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.compose.runtime.Composable
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.HabitStats
import io.github.antitastico.streak.HabitStore
import io.github.antitastico.streak.MainActivity
import io.github.antitastico.streak.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val Bg = ColorProvider(R.color.widget_bg)
private val Fg = ColorProvider(R.color.widget_fg)
private val Muted = ColorProvider(R.color.widget_muted)

/** Widget de pantalla de inicio en 3 tamaños (Jetpack Glance). */
class StreakWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habit = withContext(Dispatchers.IO) { HabitStore(context).defaultHabit() }
        provideContent { WidgetRoot(habit) }
    }

    companion object {
        /** Actualiza todos los widgets colocados (no suspende: lanza una corrutina). */
        fun updateAll(context: Context) {
            CoroutineScope(Dispatchers.Default).launch {
                StreakWidget().updateAll(context)
            }
        }
    }
}

@Composable
private fun WidgetRoot(habit: Habit?) {
    val today = LocalDate.now()
    val comps = habit?.completions ?: emptySet()
    val streak = HabitStats.currentStreak(comps, today)
    val done = today in comps
    val name = habit?.name ?: "Streak"
    val size = LocalSize.current

    Box(
        modifier = GlanceModifier.fillMaxSize().background(Bg).cornerRadius(20.dp).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            size.width < 170.dp -> SmallWidget(name, streak, done)
            size.height < 170.dp -> MediumWidget(name, streak, comps, today)
            else -> LargeWidget(name, streak, done, comps, today)
        }
    }
}

@Composable
private fun SmallWidget(name: String, streak: Int, done: Boolean) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, maxLines = 1, style = TextStyle(color = Fg, fontSize = 12.sp))
        Text("$streak", style = TextStyle(color = Fg, fontSize = 40.sp, fontWeight = FontWeight.Medium))
        Text(if (streak == 1) "día" else "días", style = TextStyle(color = Muted, fontSize = 11.sp))
        Spacer(GlanceModifier.height(8.dp))
        CheckButton(done)
    }
}

@Composable
private fun MediumWidget(name: String, streak: Int, comps: Set<LocalDate>, today: LocalDate) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth()
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                maxLines = 1,
                style = TextStyle(color = Fg, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = GlanceModifier.defaultWeight()
            )
            Text("$streak", style = TextStyle(color = Fg, fontSize = 15.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(8.dp))
        DotGrid(comps, today, weeks = 5)
    }
}

@Composable
private fun LargeWidget(name: String, streak: Int, done: Boolean, comps: Set<LocalDate>, today: LocalDate) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth()
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(name, maxLines = 1, style = TextStyle(color = Fg, fontSize = 15.sp, fontWeight = FontWeight.Medium))
                Text("racha actual", style = TextStyle(color = Muted, fontSize = 11.sp))
            }
            Text("$streak", style = TextStyle(color = Fg, fontSize = 34.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(10.dp))
        DotGrid(comps, today, weeks = 4)
        Spacer(GlanceModifier.height(10.dp))
        CheckButton(done)
    }
}

@Composable
private fun DotGrid(comps: Set<LocalDate>, today: LocalDate, weeks: Int) {
    val days = HabitStats.lastDays(comps, today, weeks * 7)
    Column {
        for (r in 0 until weeks) {
            Row {
                for (c in 0 until 7) {
                    val done = days[r * 7 + c].second
                    Box(GlanceModifier.padding(2.dp)) {
                        Box(
                            GlanceModifier.size(12.dp).cornerRadius(6.dp)
                                .background(if (done) Fg else Muted)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckButton(done: Boolean) {
    Box(
        modifier = GlanceModifier.fillMaxWidth().height(38.dp).cornerRadius(12.dp)
            .background(if (done) Muted else Fg)
            .clickable(actionRunCallback<ToggleAction>()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (done) "Hecho ✓" else "Marcar hoy",
            style = TextStyle(
                color = if (done) Fg else Bg,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}

/** Marca/desmarca hoy desde el widget y refresca. */
class ToggleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        HabitStore(context).toggleDefaultToday()
        StreakWidget().updateAll(context)
    }
}
