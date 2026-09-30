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
    val darkMode: Boolean?,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int
)

/**
 * Guarda y carga los datos en un archivo JSON DENTRO del teléfono (carpeta privada
 * de la app). Usa org.json, que Android ya incluye: sin librerías extra.
 *
 * Seguridad de datos: escritura atómica + respaldo + restauración (ver save/load).
 * Es la ÚNICA fuente de verdad: app, notificaciones y widget leen/escriben aquí.
 */
class HabitStore(context: Context) {

    private val dir = context.applicationContext.filesDir
    private val file = File(dir, "streak_data.json")
    private val backup = File(dir, "streak_data.backup.json")
    private val tmp = File(dir, "streak_data.tmp")

    fun load(): StoreData? {
        parse(file)?.let { return it }
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
                val dates = datesOf(o, "completions")
                val rests = datesOf(o, "rests")
                Habit(o.getInt("id"), o.getString("name"), o.getString("emoji"), dates, rests)
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
                darkMode = darkMode,
                reminderEnabled = root.optBoolean("reminderEnabled", true),
                reminderHour = root.optInt("reminderHour", 22),
                reminderMinute = root.optInt("reminderMinute", 0)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun datesOf(o: JSONObject, key: String): Set<LocalDate> {
        if (!o.has(key)) return emptySet()
        val a = o.getJSONArray(key)
        return (0 until a.length()).map { LocalDate.parse(a.getString(it)) }.toSet()
    }

    fun save(
        habits: List<Habit>,
        style: UiStyle,
        userName: String,
        onboarded: Boolean,
        defaultHabitId: Int?,
        darkMode: Boolean?,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val json = try {
            buildJson(habits, style, userName, onboarded, defaultHabitId, darkMode, reminderEnabled, reminderHour, reminderMinute)
        } catch (e: Exception) {
            return
        }
        try {
            if (file.exists() && file.length() > 0) file.copyTo(backup, overwrite = true)
            tmp.writeText(json)
            file.delete()
            if (!tmp.renameTo(file)) {
                file.writeText(json)
                tmp.delete()
            }
        } catch (e: Exception) {
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
        darkMode: Boolean?,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ): String {
        val arr = JSONArray()
        habits.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("emoji", h.emoji)
                put("completions", datesToJson(h.completions))
                put("rests", datesToJson(h.restDays))
            })
        }
        return JSONObject().apply {
            put("style", style.name)
            put("userName", userName)
            put("onboarded", onboarded)
            if (defaultHabitId != null) put("defaultHabitId", defaultHabitId)
            if (darkMode != null) put("darkMode", darkMode)
            put("reminderEnabled", reminderEnabled)
            put("reminderHour", reminderHour)
            put("reminderMinute", reminderMinute)
            put("habits", arr)
        }.toString()
    }

    private fun datesToJson(dates: Set<LocalDate>): JSONArray {
        val a = JSONArray()
        dates.forEach { a.put(it.toString()) }
        return a
    }

    // ---- Operaciones de alto nivel sobre el hábito predeterminado ----
    // (las usan la notificación y el widget de la pantalla de inicio)

    private fun defaultIndex(habits: List<Habit>, defaultId: Int?): Int {
        val i = habits.indexOfFirst { it.id == defaultId }
        return if (i >= 0) i else if (habits.isNotEmpty()) 0 else -1
    }

    fun defaultHabit(): Habit? {
        val d = load() ?: return null
        val i = defaultIndex(d.habits, d.defaultHabitId)
        return if (i >= 0) d.habits[i] else null
    }

    private fun saveWith(d: StoreData, habits: List<Habit>) =
        save(habits, d.style, d.userName, d.onboarded, d.defaultHabitId, d.darkMode, d.reminderEnabled, d.reminderHour, d.reminderMinute)

    /** Marca/desmarca HOY (cumplido) en el hábito predeterminado. Devuelve el nuevo estado. */
    fun toggleDefaultToday(): Boolean {
        val d = load() ?: return false
        val habits = d.habits.toMutableList()
        val idx = defaultIndex(habits, d.defaultHabitId)
        if (idx < 0) return false
        val h = habits[idx]
        val today = LocalDate.now()
        val done = today in h.completions
        habits[idx] = h.copy(
            completions = if (done) h.completions - today else h.completions + today,
            restDays = if (done) h.restDays else h.restDays - today
        )
        saveWith(d, habits)
        return today in habits[idx].completions
    }

    /** Asegura que HOY quede marcado como cumplido (idempotente). */
    fun markDefaultDoneToday(): Boolean {
        val d = load() ?: return false
        val habits = d.habits.toMutableList()
        val idx = defaultIndex(habits, d.defaultHabitId)
        if (idx < 0) return false
        val h = habits[idx]
        val today = LocalDate.now()
        if (today in h.completions) return false
        habits[idx] = h.copy(completions = h.completions + today, restDays = h.restDays - today)
        saveWith(d, habits)
        return true
    }
}
