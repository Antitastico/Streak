package io.github.antitastico.streak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * El "cerebro" de la app: guarda el ESTADO y la LÓGICA, separados de la pantalla.
 *
 * Todavía no es el ViewModel oficial de Android (ese llegará junto con Room, para
 * que el estado sobreviva rotaciones de pantalla y se guarde en disco), pero el
 * concepto es el mismo: la pantalla solo dibuja; aquí se decide qué pasa.
 *
 * Nota Compose: 'mutableStateListOf' y 'mutableStateOf' son valores "observables".
 * Cuando cambian, Compose vuelve a dibujar automáticamente lo que los usa
 * (parecido a cómo Streamlit re-ejecuta tu script cuando cambia session_state).
 */
class StreakState {

    // La lista de hábitos. Al ser 'mutableStateListOf', Compose escucha sus cambios.
    val habits = mutableStateListOf<Habit>().apply { addAll(defaultHabits) }

    // Índice del hábito que se muestra en la Home. 'private set' = solo esta
    // clase puede cambiarlo (la pantalla lo cambia llamando a select()).
    var selectedIndex by mutableStateOf(0)
        private set

    // Atajo de lectura: el hábito que está en foco ahora mismo.
    val current: Habit
        get() = habits[selectedIndex]

    /** Elegir otro hábito desde la hoja inferior. */
    fun select(index: Int) {
        if (index in habits.indices) selectedIndex = index
    }

    /**
     * Marcar / desmarcar "hecho hoy" del hábito en foco.
     * Reemplazamos el elemento por una copia modificada para que Compose lo note.
     */
    fun toggleToday() {
        val h = habits[selectedIndex]
        habits[selectedIndex] = if (h.doneToday) {
            h.copy(streak = h.streak - 1, doneToday = false)
        } else {
            h.copy(streak = h.streak + 1, doneToday = true)
        }
    }

    /** Crear un hábito nuevo y dejarlo en foco. */
    fun addHabit(name: String, emoji: String) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val newId = (habits.maxOfOrNull { it.id } ?: 0) + 1
        habits.add(Habit(id = newId, name = cleanName, emoji = emoji))
        selectedIndex = habits.lastIndex
    }
}
