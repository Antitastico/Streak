package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.antitastico.streak.HabitStats
import io.github.antitastico.streak.StreakState
import io.github.antitastico.streak.UiStyle
import java.time.LocalDate

/** Estadísticas de constancia del hábito en foco. */
@Composable
fun StatsScreen(state: StreakState) {
    val habit = state.current
    val today = LocalDate.now()
    val completions = habit.completions

    val current = HabitStats.currentStreak(completions, today)
    val longest = HabitStats.longestStreak(completions)
    val consistency = HabitStats.consistency(completions, today, 30)
    val total = HabitStats.total(completions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitIcon(habit, state.style, emojiSize = 20.sp, circleSize = 28.dp, monoSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(habit.name, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(20.dp))

        // Tarjetas 2x2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Racha actual", current.toString(), Modifier.weight(1f))
            StatCard("Racha más larga", longest.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Constancia 30d", "$consistency%", Modifier.weight(1f))
            StatCard("Check-ins", total.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        Text("Últimas 8 semanas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        WeeklyBars(HabitStats.weeklyCounts(completions, today, 8))

        Spacer(Modifier.height(28.dp))
        Text("Últimos 35 días", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Heatmap(HabitStats.lastDays(completions, today, 35))
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                value,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun WeeklyBars(counts: List<Int>) {
    val maxPerWeek = 7
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        counts.forEach { c ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("$c", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                val barHeight = (90f * c / maxPerWeek).coerceAtLeast(3f).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun Heatmap(days: List<Pair<LocalDate, Boolean>>) {
    // 35 días = 5 filas x 7 columnas
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (r in 0 until 5) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c
                    val done = idx < days.size && days[idx].second
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (done) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}
