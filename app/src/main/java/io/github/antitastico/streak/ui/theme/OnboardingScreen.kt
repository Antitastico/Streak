package io.github.antitastico.streak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.antitastico.streak.predefinedHabits

/**
 * Pantalla de bienvenida (solo la primera vez):
 * paso 1 pide el nombre, paso 2 permite elegir hábitos predefinidos o crear uno.
 */
@Composable
fun OnboardingScreen(onFinish: (name: String, chosen: List<Pair<String, String>>) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    val chosen = remember { mutableStateListOf<Pair<String, String>>() }
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        if (step == 0) {
            Spacer(Modifier.height(40.dp))
            Text("🔥", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Bienvenido a Streak", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Construye constancia, un día a la vez.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("¿Cómo te llamas?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) {
                Text("Continuar")
            }
        } else {
            val greeting = if (name.isBlank()) "Elige tus hábitos" else "Muy bien, ${name.trim()}"
            Text(greeting, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Selecciona los hábitos que quieras seguir, o crea el tuyo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val extras = chosen.filter { c -> predefinedHabits.none { it.first == c.first } }
                (predefinedHabits + extras).forEach { (habitName, emoji) ->
                    val selected = chosen.any { it.first == habitName }
                    SelectableHabitRow(emoji, habitName, selected) {
                        if (selected) chosen.removeAll { it.first == habitName }
                        else chosen.add(habitName to emoji)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Crear el mío")
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onFinish(name, chosen.toList()) },
                enabled = chosen.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (chosen.isEmpty()) "Elige al menos uno" else "Empezar (${chosen.size})")
            }
        }
    }

    if (showAdd) {
        AddHabitDialog(
            onDismiss = { showAdd = false },
            onCreate = { n, e ->
                if (n.isNotBlank()) chosen.add(n.trim() to e)
                showAdd = false
            }
        )
    }
}

@Composable
private fun SelectableHabitRow(emoji: String, name: String, selected: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
            )
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            if (selected) "✓" else "＋",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
