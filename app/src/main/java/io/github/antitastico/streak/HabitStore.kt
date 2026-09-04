package io.github.antitastico.streak

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/**
 * Guarda y carga los datos en un archivo JSON DENTRO del teléfono
 * (carpeta privada de la app). Usa org.json, que Android ya incluye:
 * sin librerías extra. Es nuestro "almacenamiento local".
 */
class HabitStore(context: Context) {

    private val file = File(context.applicationContext.filesDir, "streak_data.json")

    /** Carga (hábitos + estilo) o null si aún no hay datos guardados. */
    fun load(): Pair<List<Habit>, UiStyle>? {
        if (!file.exists()) return null
        return try {
            val root = JSONObject(file.readText())
            val style = if (root.optString("style") == "MINIMAL") UiStyle.MINIMAL else UiStyle.MODERN
            val arr = root.getJSONArray("habits")
            val habits = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val comps = o.getJSONArray("completions")
                val dates = (0 until comps.length())
                    .map { LocalDate.parse(comps.getString(it)) }
                    .toSet()
                Habit(
                    id = o.getInt("id"),
                    name = o.getString("name"),
                    emoji = o.getString("emoji"),
                    completions = dates
                )
            }
            habits to style
        } catch (e: Exception) {
            null
        }
    }

    /** Guarda el estado actual en el archivo. */
    fun save(habits: List<Habit>, style: UiStyle) {
        try {
            val arr = JSONArray()
            habits.forEach { h ->
                val comps = JSONArray()
                h.completions.forEach { comps.put(it.toString()) } // fecha ISO: 2026-09-04
                arr.put(
                    JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("emoji", h.emoji)
                        put("completions", comps)
                    }
                )
            }
            val root = JSONObject().apply {
                put("style", style.name)
                put("habits", arr)
            }
            file.writeText(root.toString())
        } catch (e: Exception) {
            // Si falla el guardado, no rompemos la app.
        }
    }
}
