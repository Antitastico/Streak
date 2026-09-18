package io.github.antitastico.streak.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import androidx.glance.unit.ColorProvider
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.HabitStats
import io.github.antitastico.streak.HabitStore
import io.github.antitastico.streak.MainActivity
import io.github.antitastico.streak.UiStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Paleta del widget, calculada para que combine con el tema de la app. */
private data class WidgetPalette(
    val bg: ColorProvider,
    val fg: ColorProvider,
    val muted: ColorProvider,
    val accent: ColorProvider,
    val onAccent: ColorProvider
)

private fun cp(argb: Long) = ColorProvider(Color(argb))

private fun paletteFor(style: UiStyle, dark: Boolean): WidgetPalette =
    if (style == UiStyle.MODERN) {
        if (dark) WidgetPalette(cp(0xFF141218), cp(0xFFE6E1E9), cp(0xFF2A2732), cp(0xFFCFBCFF), cp(0xFF20183A))
        else WidgetPalette(cp(0xFFFFFFFF), cp(0xFF1C1B1F), cp(0xFFE7E0EC), cp(0xFF6750A4), cp(0xFFFFFFFF))
    } else {
        if (dark) WidgetPalette(cp(0xFF000000), cp(0xFFF4F4F4), cp(0xFF2A2A2A), cp(0xFFF4F4F4), cp(0xFF000000))
        else WidgetPalette(cp(0xFFFFFFFF), cp(0xFF0A0A0A), cp(0xFFE3E3E3), cp(0xFF0A0A0A), cp(0xFFFFFFFF))
    }

private fun systemDark(context: Context): Boolean =
    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

/** Widget de pantalla de inicio en 3 tamaños (Jetpack Glance). */
class StreakWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = withContext(Dispatchers.IO) { HabitStore(context).load() }
        val habit = data?.let { d ->
            d.habits.firstOrNull { it.id == d.defaultHabitId } ?: d.habits.firstOrNull()
        }
        val style = data?.style ?: UiStyle.MINIMAL
        val dark = data?.darkMode ?: systemDark(context)
        val palette = paletteFor(style, dark)
        provideContent { WidgetRoot(habit, palette) }
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
private fun WidgetRoot(habit: Habit?, p: WidgetPalette) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val comps = habit?.completions ?: emptySet()
    val streak = HabitStats.currentStreak(comps, today)
    val done = today in comps
    val name = habit?.name ?: "Streak"
    val size = LocalSize.current

    Box(
        modifier = GlanceModifier.fillMaxSize().background(p.bg).cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            size.width < 170.dp -> SmallWidget(name, streak, done, p)
            size.height < 170.dp -> MediumWidget(name, streak, comps, today, p)
            else -> LargeWidget(name, streak, done, comps, today, p)
        }
    }
}

@Composable
private fun SmallWidget(name: String, streak: Int, done: Boolean, p: WidgetPalette) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, maxLines = 1, style = TextStyle(color = p.fg, fontSize = 12.sp))
        Text("$streak", style = TextStyle(color = p.fg, fontSize = 40.sp, fontWeight = FontWeight.Medium))
        Text(if (streak == 1) "día" else "días", style = TextStyle(color = p.muted, fontSize = 11.sp))
        Spacer(GlanceModifier.height(8.dp))
        CheckButton(done, p)
    }
}

@Composable
private fun MediumWidget(name: String, streak: Int, comps: Set<LocalDate>, today: LocalDate, p: WidgetPalette) {
    Column(GlanceModifier.fillMaxSize()) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                maxLines = 1,
                style = TextStyle(color = p.fg, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = GlanceModifier.defaultWeight()
            )
            Text("$streak", style = TextStyle(color = p.fg, fontSize = 15.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(8.dp))
        DotGrid(comps, today, weeks = 5, p = p)
    }
}

@Composable
private fun LargeWidget(name: String, streak: Int, done: Boolean, comps: Set<LocalDate>, today: LocalDate, p: WidgetPalette) {
    Column(GlanceModifier.fillMaxSize()) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                Text(name, maxLines = 1, style = TextStyle(color = p.fg, fontSize = 15.sp, fontWeight = FontWeight.Medium))
                Text("racha actual", style = TextStyle(color = p.muted, fontSize = 11.sp))
            }
            Text("$streak", style = TextStyle(color = p.fg, fontSize = 34.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(10.dp))
        DotGrid(comps, today, weeks = 4, p = p)
        Spacer(GlanceModifier.height(10.dp))
        CheckButton(done, p)
    }
}

@Composable
private fun DotGrid(comps: Set<LocalDate>, today: LocalDate, weeks: Int, p: WidgetPalette) {
    val days = HabitStats.lastDays(comps, today, weeks * 7)
    Column {
        for (r in 0 until weeks) {
            Row {
                for (c in 0 until 7) {
                    val done = days[r * 7 + c].second
                    Box(GlanceModifier.padding(2.dp)) {
                        Box(
                            GlanceModifier.size(12.dp).cornerRadius(6.dp)
                                .background(if (done) p.accent else p.muted)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckButton(done: Boolean, p: WidgetPalette) {
    Box(
        modifier = GlanceModifier.fillMaxWidth().height(38.dp).cornerRadius(12.dp)
            .background(if (done) p.muted else p.accent)
            .clickable(actionRunCallback<ToggleAction>()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (done) "Hecho ✓" else "Marcar hoy",
            style = TextStyle(
                color = if (done) p.fg else p.onAccent,
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
