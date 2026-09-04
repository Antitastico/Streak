package io.github.antitastico.streak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

/**
 * El "cerebro" de la app: estado + lógica + persistencia.
 * Por defecto arranca en estilo MINIMAL. Al abrir, muestra el hábito marcado
 * como predeterminado.
 */
class StreakState(private val store: HabitStore) {

    val habits = mutableStateListOf<Habit>()

    var selectedIndex by mutableStateOf(0)
        private set

    var style by mutableStateOf(UiStyle.MINIMAL)
        private set

    var userName by mutableStateOf("")
        private set

    var onboarded by mutableStateOf(false)
        private set

    var defaultHabitId by mutableStateOf<Int?>(null)
        private set

    init {
        val data = store.load()
        if (data != null) {
            habits.addAll(data.habits)
            style = data.style
            userName = data.userName
            onboarded = data.onboarded
            defaultHabitId = data.defaultHabitId
            // Abrir en el hábito predeterminado (si existe).
            val idx = habits.indexOfFirst { it.id == data.defaultHabitId }
            selectedIndex = if (idx >= 0) idx else 0
        }
        // Si no hay datos, onboarded = false → se muestra el onboarding.
    }

    val current: Habit
        get() = habits[selectedIndex]

    private fun persist() =
        store.save(habits.toList(), style, userName, onboarded, defaultHabitId)

    fun select(index: Int) {
        if (index in habits.indices) selectedIndex = index
    }

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

    /** Alterna entre Minimal y Moderno (para el símbolo de la esquina). */
    fun toggleStyle() {
        style = if (style == UiStyle.MINIMAL) UiStyle.MODERN else UiStyle.MINIMAL
        persist()
    }

    /** Marca un hábito como el predeterminado (el que se abre al iniciar). */
    fun setDefault(id: Int) {
        defaultHabitId = id
        persist()
    }

    /** Termina el onboarding: guarda nombre y crea los hábitos elegidos. */
    fun completeOnboarding(name: String, chosen: List<Pair<String, String>>) {
        userName = name.trim()
        var nextId = 1
        chosen.forEach { (habitName, emoji) ->
            habits.add(Habit(id = nextId++, name = habitName.trim(), emoji = emoji))
        }
        if (habits.isNotEmpty()) {
            defaultHabitId = habits.first().id
            selectedIndex = 0
        }
        onboarded = true
        persist()
    }
}
