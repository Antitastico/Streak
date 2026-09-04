package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.antitastico.streak.StreakState
import io.github.antitastico.streak.UiStyle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Calendario mensual del hábito en foco: días marcados resaltados. */
@Composable
fun CalendarScreen(state: StreakState) {
    val habit = state.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado con el hábito
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitIcon(habit, state.style, emojiSize = 20.sp, circleSize = 28.dp, monoSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(habit.name, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(20.dp))

        // Selector de mes
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹", fontSize = 22.sp) }
            val label = month.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercase() } + " " + month.year
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("›", fontSize = 22.sp) }
        }
        Spacer(Modifier.height(8.dp))

        // Cabecera de días (lunes primero)
        val week = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
        Row(Modifier.fillMaxWidth()) {
            week.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Grilla de días
        val firstOfMonth = month.atDay(1)
        val offset = (firstOfMonth.dayOfWeek.value + 6) % 7 // lunes = 0
        val daysInMonth = month.lengthOfMonth()
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val dayNum = r * 7 + c - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = month.atDay(dayNum)
                            DayCell(
                                day = dayNum,
                                done = date in habit.completions,
                                isToday = date == today
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        val flame = if (state.style == UiStyle.MODERN) "🔥 " else ""
        Text(
            text = "$flame${habit.streak} de racha · ${habit.completions.size} check-ins en total",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DayCell(day: Int, done: Boolean, isToday: Boolean) {
    val bg = if (done) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (done) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (isToday && !done)
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$day",
            color = fg,
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
