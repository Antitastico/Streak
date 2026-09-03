package io.github.antitastico.streak

/**
 * Un hábito que el usuario quiere seguir.
 *
 * Es un "data class": el equivalente a una @dataclass de Python — solo guarda
 * datos. Todos los campos son 'val' (inmutables); para "cambiar" un hábito
 * creamos una COPIA con .copy(...). Trabajar con copias inmutables ayuda a
 * Jetpack Compose a detectar los cambios y redibujar la pantalla.
 */
data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val streak: Int = 0,
    val doneToday: Boolean = false
)

/**
 * Lista de ejemplo con la que arranca la app.
 * Por ahora vive solo en memoria: se reinicia al cerrar la app.
 * Cuando conectemos Room, estos datos se guardarán en el disco del teléfono.
 */
val defaultHabits = listOf(
    Habit(id = 1, name = "Correr", emoji = "🏃", streak = 12),
    Habit(id = 2, name = "Leer", emoji = "📖", streak = 4),
    Habit(id = 3, name = "Agua", emoji = "💧", streak = 30, doneToday = true),
    Habit(id = 4, name = "Meditar", emoji = "🧘", streak = 7)
)
