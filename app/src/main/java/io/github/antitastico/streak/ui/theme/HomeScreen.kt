package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.antitastico.streak.Habit
import io.github.antitastico.streak.StreakState
import kotlinx.coroutines.launch

/**
 * Pantalla principal.
 *
 * Usa un BottomSheetScaffold: una pantalla con una "hoja inferior" que se puede
 * arrastrar hacia arriba. 'sheetPeekHeight' es la parte que asoma cuando está
 * cerrada (el asa "Mis hábitos"). Al deslizar hacia arriba se expande.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: StreakState) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 96.dp,
        sheetContent = {
            HabitSheet(
                state = state,
                onSelect = { index ->
                    state.select(index)
                    // Al elegir un hábito, colapsamos la hoja (vuelve al "peek").
                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                },
                onAddClick = { showAddDialog = true }
            )
        }
    ) { innerPadding ->
        HomeContent(
            habit = state.current,
            onCheckIn = { state.toggleToday() },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name, emoji ->
                state.addHabit(name, emoji)
                showAddDialog = false
            }
        )
    }
}

/** La Home minimalista: un solo hábito en foco. */
@Composable
private fun HomeContent(
    habit: Habit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()          // deja espacio bajo la barra de estado
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 112.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Chip con emoji + nombre del hábito
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(habit.emoji, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
            }
        }

        // Racha grande
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥", fontSize = 40.sp)
            Text(
                text = habit.streak.toString(),
                fontSize = 88.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                // Plural resuelto: "1 día" vs "2 días"
                text = if (habit.streak == 1) "día" else "días",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Botón de check-in. Cambia de aspecto si ya está hecho hoy.
        if (habit.doneToday) {
            FilledTonalButton(onClick = onCheckIn) {
                Text("✓ Hecho hoy")
            }
        } else {
            Button(onClick = onCheckIn) {
                Text("Hecho hoy")
            }
        }
    }
}

/** Contenido de la hoja inferior: lista de hábitos + botón para agregar. */
@Composable
private fun HabitSheet(
    state: StreakState,
    onSelect: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Mis hábitos",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Recorremos los hábitos y dibujamos una fila por cada uno.
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
                Text(habit.emoji, fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (habit.doneToday) "Hecho hoy" else "racha actual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("🔥 ${habit.streak}", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Agregar hábito")
        }
        Spacer(Modifier.height(20.dp))
    }
}

/** Diálogo para crear un hábito nuevo (nombre + icono). */
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
            TextButton(
                onClick = { onCreate(name, emoji) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
