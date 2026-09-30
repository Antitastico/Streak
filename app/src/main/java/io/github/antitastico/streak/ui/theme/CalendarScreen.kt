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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val Accent = Color(0xFFE8631A)

/** Calendario "review": número grande de hoy y una grilla de puntos grandes. */
@Composable
fun CalendarScreen(state: StreakState) {
    val habit = state.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val es = Locale("es")

    Box(modifier = Modifier.fillMaxSize()) {
        // Shader de rayas diagonales, solo en el calendario.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .diagonalStripes(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
        Text(
            habit.name.uppercase(es),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))

        // Número grande de hoy (estilo referencia)
        Text(
            "${today.dayOfMonth}",
            fontSize = 64.sp,
            fontWeight = FontWeight.Thin,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Mes/año (mes visible) + día de la semana de hoy
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    month.month.getDisplayName(TextStyle.FULL, es).uppercase(es),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${month.year}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                today.dayOfWeek.getDisplayName(TextStyle.SHORT, es)
                    .replaceFirstChar { it.uppercase() }.trimEnd('.'),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { month = month.minusMonths(1) }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("‹", fontSize = 22.sp)
            }
            TextButton(onClick = { month = month.plusMonths(1) }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("›", fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(10.dp))

        // Letras de día (lunes primero)
        val week = listOf("L", "M", "X", "J", "V", "S", "D")
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
        Spacer(Modifier.height(8.dp))

        // Grilla de puntos grandes (sin números)
        val firstOfMonth = month.atDay(1)
        val offset = (firstOfMonth.dayOfWeek.value + 6) % 7
        val daysInMonth = month.lengthOfMonth()
        val rows = (offset + daysInMonth + 6) / 7

        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val dayNum = r * 7 + c - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            DayDot(month.atDay(dayNum), habit.completions, habit.restDays, today)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(
            "${habit.streak} de racha · ${habit.completions.size} cumplidos · ${habit.restDays.size} descansos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        }
    }
}

@Composable
private fun DayDot(
    date: LocalDate,
    completions: Set<LocalDate>,
    rests: Set<LocalDate>,
    today: LocalDate
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ring = MaterialTheme.colorScheme.onSurfaceVariant
    val done = date in completions
    val rest = date in rests
    val isToday = date == today

    val cell = when {
        isToday && done -> Modifier.background(Accent)
        isToday -> Modifier.border(2.5.dp, Accent, CircleShape)
        done -> Modifier.background(onSurface)
        rest -> Modifier.border(2.5.dp, onSurface, CircleShape)
        else -> Modifier.border(1.5.dp, ring, CircleShape)
    }
    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).then(cell))
}
