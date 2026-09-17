package com.bhaloo.reminders.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bhaloo never has thousands of reminders, so the whole list lives in one JSON blob
 * inside SharedPreferences. No database, no migrations, nothing to go wrong at 6 a.m.
 */
class ReminderStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _reminders = MutableStateFlow(load())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    /** Times Bhaloo has been reminded, all-time. Shown proudly on the About screen. */
    var timesReminded: Int
        get() = prefs.getInt(KEY_TIMES_REMINDED, 0)
        private set(value) = prefs.edit().putInt(KEY_TIMES_REMINDED, value).apply()

    var snoozeMinutes: Int
        get() = prefs.getInt(KEY_SNOOZE, 10)
        set(value) = prefs.edit().putInt(KEY_SNOOZE, value.coerceIn(1, 120)).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_RATE, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_RATE, value.coerceIn(0.5f, 1.5f)).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_PITCH, value.coerceIn(0.5f, 1.6f)).apply()

    /** The name of whoever made this for Bhaloo — printed on the dedication card. */
    /** Guards the one-time birthday seed so a deleted birthday stays deleted. */
    var hasSeeded: Boolean
        get() = prefs.getBoolean(KEY_SEEDED, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEDED, value).apply()

    var madeBy: String
        get() = prefs.getString(KEY_MADE_BY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MADE_BY, value.trim()).apply()

    val installedAt: Long
        get() {
            val stored = prefs.getLong(KEY_INSTALLED_AT, 0L)
            if (stored != 0L) return stored
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_INSTALLED_AT, now).apply()
            return now
        }

    fun byId(id: Long): Reminder? = _reminders.value.firstOrNull { it.id == id }

    /** Inserts when [reminder] has id 0, otherwise replaces. Returns the stored copy. */
    fun upsert(reminder: Reminder): Reminder {
        val current = _reminders.value.toMutableList()
        val stored: Reminder
        if (reminder.id == 0L) {
            val nextId = prefs.getLong(KEY_NEXT_ID, 1L)
            stored = reminder.copy(id = nextId)
            prefs.edit().putLong(KEY_NEXT_ID, nextId + 1).apply()
            current.add(stored)
        } else {
            stored = reminder
            val index = current.indexOfFirst { it.id == reminder.id }
            if (index >= 0) current[index] = stored else current.add(stored)
        }
        persist(current)
        return stored
    }

    fun delete(id: Long) {
        persist(_reminders.value.filterNot { it.id == id })
    }

    fun setEnabled(id: Long, enabled: Boolean) {
        val updated = _reminders.value.map { if (it.id == id) it.copy(enabled = enabled) else it }
        persist(updated)
    }

    /** Called after a reminder has been announced. */
    fun markFired(id: Long, nextTimeMillis: Long?) {
        val now = System.currentTimeMillis()
        val updated = _reminders.value.map {
            when {
                it.id != id -> it
                nextTimeMillis != null -> it.copy(lastFiredAt = now, timeMillis = nextTimeMillis)
                else -> it.copy(lastFiredAt = now, enabled = false)
            }
        }
        timesReminded += 1
        persist(updated)
    }

    fun markCompleted(id: Long) {
        val updated = _reminders.value.map {
            if (it.id == id) it.copy(timesCompleted = it.timesCompleted + 1) else it
        }
        persist(updated)
    }

    private fun persist(list: List<Reminder>) {
        val sorted = list.sortedBy { it.timeMillis }
        val array = JSONArray()
        sorted.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_REMINDERS, array.toString()).apply()
        _reminders.value = sorted
    }

    private fun load(): List<Reminder> {
        val raw = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }.getOrElse { emptyList() }.sortedBy { it.timeMillis }
    }

    private fun toJson(reminder: Reminder): JSONObject = JSONObject().apply {
        put("id", reminder.id)
        put("title", reminder.title)
        put("spokenMessage", reminder.spokenMessage)
        put("language", reminder.language.name)
        put("timeMillis", reminder.timeMillis)
        put("repeat", reminder.repeat.name)
        put("daysOfWeek", JSONArray().also { array -> reminder.daysOfWeek.forEach(array::put) })
        put("enabled", reminder.enabled)
        put("speakTimes", reminder.speakTimes)
        put("alertStyle", reminder.alertStyle.name)
        put("vibrate", reminder.vibrate)
        put("isSpecialDay", reminder.isSpecialDay)
        put("note", reminder.note)
        put("createdAt", reminder.createdAt)
        put("lastFiredAt", reminder.lastFiredAt)
        put("timesCompleted", reminder.timesCompleted)
    }

    private fun fromJson(json: JSONObject): Reminder {
        val daysArray = json.optJSONArray("daysOfWeek")
        val days = buildSet {
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) add(daysArray.getInt(i))
            }
        }
        return Reminder(
            id = json.optLong("id"),
            title = json.optString("title"),
            spokenMessage = json.optString("spokenMessage"),
            language = VoiceLanguage.from(json.optString("language")),
            timeMillis = json.optLong("timeMillis"),
            repeat = RepeatMode.from(json.optString("repeat")),
            daysOfWeek = days,
            enabled = json.optBoolean("enabled", true),
            speakTimes = json.optInt("speakTimes", 2).coerceIn(1, 5),
            alertStyle = AlertStyle.from(json.optString("alertStyle")),
            vibrate = json.optBoolean("vibrate", true),
            isSpecialDay = json.optBoolean("isSpecialDay", false),
            note = json.optString("note"),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            lastFiredAt = json.optLong("lastFiredAt", 0L),
            timesCompleted = json.optInt("timesCompleted", 0)
        )
    }

    private companion object {
        const val PREFS = "bhaloo_reminders"
        const val KEY_REMINDERS = "reminders"
        const val KEY_NEXT_ID = "next_id"
        const val KEY_TIMES_REMINDED = "times_reminded"
        const val KEY_SNOOZE = "snooze_minutes"
        const val KEY_RATE = "speech_rate"
        const val KEY_PITCH = "speech_pitch"
        const val KEY_MADE_BY = "made_by"
        const val KEY_SEEDED = "seeded_birthday"
        const val KEY_INSTALLED_AT = "installed_at"
    }
}
