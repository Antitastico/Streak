package io.github.antitastico.streak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

/**
 * El "cerebro" de la app: estado + lógica, separados de la pantalla.
 * Ahora también PERSISTE: cada cambio se guarda en el archivo local (HabitStore).
 */
class StreakState(private val store: HabitStore) {

    val habits = mutableStateListOf<Habit>()

    var selectedIndex by mutableStateOf(0)
        private set

    var style by mutableStateOf(UiStyle.MODERN)
        private set

    init {
        val loaded = store.load()
        if (loaded != null && loaded.first.isNotEmpty()) {
            habits.addAll(loaded.first)
            style = loaded.second
            selectedIndex = 0
        } else {
            // Primer arranque: datos de ejemplo.
            habits.addAll(seedHabits())
        }
    }

    val current: Habit
        get() = habits[selectedIndex]

    private fun persist() = store.save(habits.toList(), style)

    fun select(index: Int) {
        if (index in habits.indices) selectedIndex = index
    }

    /** Marca o desmarca "hoy" en el hábito en foco. */
    fun toggleToday() {
        val h = habits[selectedIndex]
        val today = LocalDate.now()
        val newCompletions =
            if (today in h.completions) h.completions - today
            else h.completions + today
        habits[selectedIndex] = h.copy(completions = newCompletions)
        persist()
    }

    fun addHabit(name: String, emoji: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val newId = (habits.maxOfOrNull { it.id } ?: 0) + 1
        habits.add(Habit(id = newId, name = clean, emoji = emoji))
        selectedIndex = habits.lastIndex
        persist()
    }

    fun changeStyle(newStyle: UiStyle) {
        style = newStyle
        persist()
    }
}
