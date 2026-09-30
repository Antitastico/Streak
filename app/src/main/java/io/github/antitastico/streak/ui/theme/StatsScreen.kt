package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.HabitStats
import io.github.antitastico.streak.StreakState
import java.time.LocalDate

/** Estadísticas de constancia del hábito en foco (monocromo, tarjetas de borde fino). */
@Composable
fun StatsScreen(state: StreakState) {
    val habit = state.current
    val today = LocalDate.now()

    val current = HabitStats.currentStreak(habit.covered, today)
    val longest = HabitStats.longestStreak(habit.covered)
    val consistency = HabitStats.consistency(habit.completions, today, 30)
    val total = HabitStats.total(habit.completions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(habit.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))

        // Constancia (número grande + puntos de los últimos 30 días)
        Text(
            "$consistency%",
            fontSize = 56.sp,
            fontWeight = FontWeight.Thin,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "constancia · últimos 30 días",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        ConsistencyDots(habit, today)

        Spacer(Modifier.height(24.dp))

        // Tarjetas de borde fino 2x2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Racha actual", current.toString(), Modifier.weight(1f))
            StatCard("Racha más larga", longest.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Cumplidos", total.toString(), Modifier.weight(1f))
            StatCard("Descansos", habit.restDays.size.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        Text("Últimas 8 semanas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(14.dp))
        WeeklyBars(HabitStats.weeklyCounts(habit.completions, today, 8))
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ConsistencyDots(habit: Habit, today: LocalDate) {
    val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ringDim = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        for (r in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                for (c in 0 until 15) {
                    val d = days[r * 15 + c]
                    val done = d in habit.completions
                    val rest = d in habit.restDays
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .then(
                                when {
                                    done -> Modifier.background(onSurface)
                                    rest -> Modifier.border(2.dp, onSurface, CircleShape)
                                    else -> Modifier.border(1.5.dp, ringDim, CircleShape)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                value,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyBars(counts: List<Int>) {
    val maxPerWeek = 7
    val onSurface = MaterialTheme.colorScheme.onSurface
    val lastIndex = counts.lastIndex
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        counts.forEachIndexed { i, c ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "$c",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                val barHeight = (86f * c / maxPerWeek).coerceAtLeast(6f).dp
                val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                val current = i == lastIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(shape)
                        .then(
                            if (current) Modifier.background(onSurface)
                            else Modifier.border(1.5.dp, onSurface, shape)
                        )
                )
            }
        }
    }
}
