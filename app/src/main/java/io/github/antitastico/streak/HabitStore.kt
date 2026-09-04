package io.github.antitastico.streak

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/** Todo lo que persiste la app (se guarda en un JSON local). */
data class StoreData(
    val habits: List<Habit>,
    val style: UiStyle,
    val userName: String,
    val onboarded: Boolean,
    val defaultHabitId: Int?
)

/**
 * Guarda y carga los datos en un archivo JSON DENTRO del teléfono (carpeta privada
 * de la app). Usa org.json, que Android ya incluye: sin librerías extra.
 */
class HabitStore(context: Context) {

    private val file = File(context.applicationContext.filesDir, "streak_data.json")

    fun load(): StoreData? {
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
                Habit(o.getInt("id"), o.getString("name"), o.getString("emoji"), dates)
            }
            val defaultId = if (root.has("defaultHabitId") && !root.isNull("defaultHabitId"))
                root.getInt("defaultHabitId") else null
            StoreData(
                habits = habits,
                style = style,
                userName = root.optString("userName", ""),
                onboarded = root.optBoolean("onboarded", habits.isNotEmpty()),
                defaultHabitId = defaultId
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(
        habits: List<Habit>,
        style: UiStyle,
        userName: String,
        onboarded: Boolean,
        defaultHabitId: Int?
    ) {
        try {
            val arr = JSONArray()
            habits.forEach { h ->
                val comps = JSONArray()
                h.completions.forEach { comps.put(it.toString()) }
                arr.put(JSONObject().apply {
                    put("id", h.id)
                    put("name", h.name)
                    put("emoji", h.emoji)
                    put("completions", comps)
                })
            }
            val root = JSONObject().apply {
                put("style", style.name)
                put("userName", userName)
                put("onboarded", onboarded)
                if (defaultHabitId != null) put("defaultHabitId", defaultHabitId)
                put("habits", arr)
            }
            file.writeText(root.toString())
        } catch (e: Exception) {
            // No rompemos la app si falla el guardado.
        }
    }
}
