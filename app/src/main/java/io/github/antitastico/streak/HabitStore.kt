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
    val defaultHabitId: Int?,
    /** null = seguir el tema del sistema; true/false = elección manual. */
    val darkMode: Boolean?
)

/**
 * Guarda y carga los datos en un archivo JSON DENTRO del teléfono (carpeta privada
 * de la app). Usa org.json, que Android ya incluye: sin librerías extra.
 *
 * Seguridad de datos:
 *  - Escritura ATÓMICA: primero a un archivo temporal, luego se renombra.
 *  - RESPALDO: antes de sobrescribir, el archivo bueno anterior se copia a
 *    `streak_data.backup.json`.
 *  - RESTAURACIÓN: si el archivo principal falta o está dañado, se lee el respaldo.
 *
 * Es la ÚNICA fuente de verdad: la app, las notificaciones y el widget leen/escriben aquí.
 */
class HabitStore(context: Context) {

    private val dir = context.applicationContext.filesDir
    private val file = File(dir, "streak_data.json")
    private val backup = File(dir, "streak_data.backup.json")
    private val tmp = File(dir, "streak_data.tmp")

    fun load(): StoreData? {
        parse(file)?.let { return it }
        // El principal falta o está dañado: intenta restaurar desde el respaldo.
        return parse(backup)
    }

    private fun parse(f: File): StoreData? {
        if (!f.exists() || f.length() == 0L) return null
        return try {
            val root = JSONObject(f.readText())
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
            val darkMode = if (root.has("darkMode") && !root.isNull("darkMode"))
                root.getBoolean("darkMode") else null
            StoreData(
                habits = habits,
                style = style,
                userName = root.optString("userName", ""),
                onboarded = root.optBoolean("onboarded", habits.isNotEmpty()),
                defaultHabitId = defaultId,
                darkMode = darkMode
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
        defaultHabitId: Int?,
        darkMode: Boolean?
    ) {
        val json = try {
            buildJson(habits, style, userName, onboarded, defaultHabitId, darkMode)
        } catch (e: Exception) {
            return
        }
        try {
            // 1) Respalda el archivo bueno anterior antes de tocarlo.
            if (file.exists() && file.length() > 0) {
                file.copyTo(backup, overwrite = true)
            }
            // 2) Escribe a un temporal y renómbralo (atómico en el mismo disco).
            tmp.writeText(json)
            file.delete()
            if (!tmp.renameTo(file)) {
                file.writeText(json)
                tmp.delete()
            }
        } catch (e: Exception) {
            // Último recurso: escribir directo, sin romper la app.
            try {
                file.writeText(json)
            } catch (_: Exception) {
            }
        }
    }

    private fun buildJson(
        habits: List<Habit>,
        style: UiStyle,
        userName: String,
        onboarded: Boolean,
        defaultHabitId: Int?,
        darkMode: Boolean?
    ): String {
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
        return JSONObject().apply {
            put("style", style.name)
            put("userName", userName)
            put("onboarded", onboarded)
            if (defaultHabitId != null) put("defaultHabitId", defaultHabitId)
            if (darkMode != null) put("darkMode", darkMode)
            put("habits", arr)
        }.toString()
    }

    // ---- Operaciones de alto nivel sobre el hábito predeterminado ----
    // (las usan la notificación de las 10pm y el widget de la pantalla de inicio)

    private fun defaultIndex(habits: List<Habit>, defaultId: Int?): Int {
        val i = habits.indexOfFirst { it.id == defaultId }
        return if (i >= 0) i else if (habits.isNotEmpty()) 0 else -1
    }

    /** El hábito que se abre al iniciar (o el primero). */
    fun defaultHabit(): Habit? {
        val d = load() ?: return null
        val i = defaultIndex(d.habits, d.defaultHabitId)
        return if (i >= 0) d.habits[i] else null
    }

    /** Marca/desmarca HOY en el hábito predeterminado. Devuelve el nuevo estado. */
    fun toggleDefaultToday(): Boolean {
        val d = load() ?: return false
        val habits = d.habits.toMutableList()
        val idx = defaultIndex(habits, d.defaultHabitId)
        if (idx < 0) return false
        val h = habits[idx]
        val today = LocalDate.now()
        val done = today in h.completions
        habits[idx] = h.copy(
            completions = if (done) h.completions - today else h.completions + today
        )
        save(habits, d.style, d.userName, d.onboarded, d.defaultHabitId, d.darkMode)
        return today in habits[idx].completions
    }

    /** Asegura que HOY quede marcado (idempotente). Devuelve true si hubo cambio. */
    fun markDefaultDoneToday(): Boolean {
        val d = load() ?: return false
        val habits = d.habits.toMutableList()
        val idx = defaultIndex(habits, d.defaultHabitId)
        if (idx < 0) return false
        val h = habits[idx]
        val today = LocalDate.now()
        if (today in h.completions) return false
        habits[idx] = h.copy(completions = h.completions + today)
        save(habits, d.style, d.userName, d.onboarded, d.defaultHabitId, d.darkMode)
        return true
    }
}
