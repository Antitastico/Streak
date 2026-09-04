package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.StreakState
import io.github.antitastico.streak.UiStyle
import io.github.antitastico.streak.monogram
import kotlinx.coroutines.launch

/** Icono del hábito: emoji en MODERN, monograma (inicial en círculo) en MINIMAL. */
@Composable
fun HabitIcon(
    habit: Habit,
    style: UiStyle,
    emojiSize: TextUnit,
    circleSize: Dp,
    monoSize: TextUnit
) {
    if (style == UiStyle.MODERN) {
        Text(habit.emoji, fontSize = emojiSize)
    } else {
        Box(
            modifier = Modifier
                .size(circleSize)
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(monogram(habit.name), fontSize = monoSize, fontWeight = FontWeight.Medium)
        }
    }
}

/** Pantalla Inicio: el hábito en foco. Tocar el nombre abre la hoja de hábitos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: StreakState) {
    var showSheet by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Cierra la hoja con animación suave.
    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) showSheet = false
        }
    }

    HomeContent(
        habit = state.current,
        style = state.style,
        onCheckIn = { state.toggleToday() },
        onOpenHabits = { showSheet = true }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            HabitSheetContent(
                state = state,
                onSelect = { index ->
                    state.select(index)
                    closeSheet()
                },
                onAddClick = { showAdd = true }
            )
        }
    }

    if (showAdd) {
        AddHabitDialog(
            onDismiss = { showAdd = false },
            onCreate = { name, emoji ->
                state.addHabit(name, emoji)
                showAdd = false
            }
        )
    }
}

@Composable
private fun HomeContent(
    habit: Habit,
    style: UiStyle,
    onCheckIn: () -> Unit,
    onOpenHabits: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Chip con icono + nombre (tocable para abrir la hoja de hábitos)
        Surface(
            shape = CircleShape,
            color = if (style == UiStyle.MODERN)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else Color.Transparent,
            border = if (style == UiStyle.MINIMAL)
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            else null,
            onClick = onOpenHabits
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
            ) {
                HabitIcon(habit, style, emojiSize = 18.sp, circleSize = 24.dp, monoSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Racha grande
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (style == UiStyle.MODERN) {
                Text("🔥", fontSize = 40.sp)
            }
            Text(
                text = habit.streak.toString(),
                fontSize = 88.sp,
                fontWeight = if (style == UiStyle.MINIMAL) FontWeight.Thin else FontWeight.Light
            )
            Text(
                text = if (habit.streak == 1) "día" else "días",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Botón de check-in
        when {
            style == UiStyle.MINIMAL && habit.doneToday ->
                Button(onClick = onCheckIn) { Text("✓ Hecho hoy") }
            style == UiStyle.MINIMAL ->
                OutlinedButton(onClick = onCheckIn) { Text("Hecho hoy") }
            habit.doneToday ->
                FilledTonalButton(onClick = onCheckIn) { Text("✓ Hecho hoy") }
            else ->
                Button(onClick = onCheckIn) { Text("Hecho hoy") }
        }
    }
}

/** Contenido de la hoja modal: selector de estilo + lista de hábitos + agregar. */
@Composable
private fun HabitSheetContent(
    state: StreakState,
    onSelect: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            FilterChip(
                selected = state.style == UiStyle.MODERN,
                onClick = { state.changeStyle(UiStyle.MODERN) },
                label = { Text("Moderno") }
            )
            FilterChip(
                selected = state.style == UiStyle.MINIMAL,
                onClick = { state.changeStyle(UiStyle.MINIMAL) },
                label = { Text("Minimal") }
            )
        }

        Text(
            text = "Mis hábitos",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        state.habits.forEachIndexed { index, habit ->
            val selected = index == state.selectedIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(12.dp)
            ) {
                HabitIcon(habit, state.style, emojiSize = 22.sp, circleSize = 40.dp, monoSize = 16.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (habit.doneToday) "Hecho hoy" else "racha actual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (state.style == UiStyle.MODERN) "🔥 ${habit.streak}" else "${habit.streak}",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Agregar hábito")
        }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🏃") }
    val options = listOf("🏃", "📖", "💧", "🧘", "🏋️", "🎸", "🥗", "😴", "💪", "🧠")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo hábito") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Spacer(Modifier.height(14.dp))
                Text("Icono", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        val isSel = option == emoji
                        Text(
                            text = option,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    else Color.Transparent
                                )
                                .clickable { emoji = option }
                                .padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, emoji) }, enabled = name.isNotBlank()) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
