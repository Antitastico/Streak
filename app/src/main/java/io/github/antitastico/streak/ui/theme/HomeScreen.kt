package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.StreakState
import io.github.antitastico.streak.UiStyle
import io.github.antitastico.streak.audio.SoundFx
import io.github.antitastico.streak.widget.StreakWidget
import kotlinx.coroutines.launch

/**
 * Icono del hábito. En MODERN muestra el emoji; en MINIMAL no muestra nada
 * (enfoque hiper-minimalista, sin monogramas).
 */
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: StreakState) {
    var showSheet by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val sound = remember { SoundFx(context) }
    DisposableEffect(Unit) { onDispose { sound.release() } }

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) showSheet = false
        }
    }

    val effectiveDark = state.darkMode ?: isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeContent(
            habit = state.current,
            style = state.style,
            onCheckIn = {
                val wasDone = state.current.doneToday
                state.toggleToday()
                if (!wasDone) sound.play()
                StreakWidget.updateAll(context)
            },
            onOpenHabits = { showSheet = true },
            onOpenDetail = { showDetail = true }
        )

        // Esquina superior izquierda: alterna claro / oscuro.
        Text(
            text = if (effectiveDark) "☀" else "☾",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .clip(CircleShape)
                .clickable {
                    state.setDarkMode(!effectiveDark)
                    StreakWidget.updateAll(context)
                }
                .padding(10.dp)
        )

        // Esquina superior derecha: alterna Minimal / Moderno.
        Text(
            text = "◐",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
                .clip(CircleShape)
                .clickable {
                    state.toggleStyle()
                    StreakWidget.updateAll(context)
                }
                .padding(10.dp)
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            HabitSheetContent(
                state = state,
                onSelect = { index -> state.select(index); closeSheet() },
                onAddClick = { showAdd = true }
            )
        }
    }

    if (showAdd) {
        AddHabitDialog(
            onDismiss = { showAdd = false },
            onCreate = { name, emoji -> state.addHabit(name, emoji); showAdd = false }
        )
    }

    if (showDetail) {
        HabitDetailDialog(state = state, onClose = { showDetail = false })
    }
}

@Composable
private fun HomeContent(
    habit: Habit,
    style: UiStyle,
    onCheckIn: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Chip: (emoji en Moderno) + nombre — tocar abre la hoja de hábitos
        Surface(
            shape = CircleShape,
            color = if (style == UiStyle.MODERN)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
            border = if (style == UiStyle.MINIMAL)
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            onClick = onOpenHabits
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
            ) {
                if (style == UiStyle.MODERN) {
                    Text(habit.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
                // La flecha solo en Moderno; Minimal se mantiene zen, sin guías.
                if (style == UiStyle.MODERN) {
                    Spacer(Modifier.width(6.dp))
                    Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Racha grande (tocar el número abre la ventana de progreso)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenDetail() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (style == UiStyle.MODERN) {
                Text("🔥", fontSize = 40.sp)
            }
            Text(
                text = habit.streak.toString(),
                fontSize = 92.sp,
                fontWeight = FontWeight.Thin
            )
            Text(
                text = if (habit.streak == 1) "día" else "días",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "toca para ver tu progreso",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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

/** Hoja modal: saludo + lista de hábitos (con estrella de predeterminado) + agregar. */
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
        val title = if (state.userName.isBlank()) "Mis hábitos" else "Hola, ${state.userName} 👋"
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
        Text(
            "La ⭐ marca cuál se abre al iniciar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        state.habits.forEachIndexed { index, habit ->
            val selected = index == state.selectedIndex
            val isDefault = habit.id == state.defaultHabitId
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(14.dp)
            ) {
                if (state.style == UiStyle.MODERN) {
                    Text(habit.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                }
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
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isDefault) "⭐" else "☆",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { state.setDefault(habit.id) }
                        .padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
            Text("+ Agregar hábito")
        }
    }
}

/** Ventana superpuesta centrada con Calendario y Estadísticas del hábito en foco. */
@Composable
private fun HabitDetailDialog(state: StreakState, onClose: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 6.dp, top = 8.dp)
                ) {
                    FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Calendario") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Estadísticas") })
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onClose) { Text("✕", fontSize = 18.sp) }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (tab == 0) CalendarScreen(state) else StatsScreen(state)
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
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
            TextButton(onClick = { onCreate(name, emoji) }, enabled = name.isNotBlank()) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
