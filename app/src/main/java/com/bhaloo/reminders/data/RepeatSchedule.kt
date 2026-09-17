package com.bhaloo.reminders.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The calendar arithmetic behind every reminder, kept free of Android types so
 * it can be unit-tested on its own. [com.bhaloo.reminders.alarm.ReminderScheduler]
 * is the only caller; it just hands the answer to AlarmManager.
 */
object RepeatSchedule {

    /**
     * The next moment this reminder should speak, or null when it is a one-shot
     * whose time has already passed (or a custom-days rule with no days picked).
     */
    fun nextTriggerMillis(reminder: Reminder, fromMillis: Long = System.currentTimeMillis()): Long? {
        if (reminder.timeMillis > fromMillis && isAllowedDay(reminder, reminder.timeMillis)) {
            return reminder.timeMillis
        }
        return nextOccurrenceAfter(reminder, fromMillis)
    }

    /**
     * Advances a reminder's calendar rule past [fromMillis]. Returns null for
     * [RepeatMode.ONCE], which has no "next".
     */
    fun nextOccurrenceAfter(reminder: Reminder, fromMillis: Long): Long? {
        if (reminder.repeat == RepeatMode.ONCE) return null
        if (reminder.repeat == RepeatMode.CUSTOM_DAYS && reminder.daysOfWeek.isEmpty()) return null

        val zone = ZoneId.systemDefault()
        val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(fromMillis), zone)
        var candidate = LocalDateTime.ofInstant(Instant.ofEpochMilli(reminder.timeMillis), zone)

        // Guard against runaway loops if the clock ever jumps years ahead.
        var guard = 0
        while (guard < MAX_STEPS) {
            val dayOk = reminder.repeat != RepeatMode.CUSTOM_DAYS ||
                reminder.daysOfWeek.contains(candidate.dayOfWeek.value)
            if (candidate.isAfter(from) && dayOk) {
                return candidate.atZone(zone).toInstant().toEpochMilli()
            }
            candidate = when (reminder.repeat) {
                RepeatMode.DAILY -> candidate.plusDays(1)
                RepeatMode.WEEKLY -> candidate.plusWeeks(1)
                RepeatMode.MONTHLY -> candidate.plusMonths(1)
                RepeatMode.YEARLY -> candidate.plusYears(1)
                RepeatMode.CUSTOM_DAYS -> candidate.plusDays(1)
                RepeatMode.ONCE -> return null
            }
            guard++
        }
        return null
    }

    private fun isAllowedDay(reminder: Reminder, millis: Long): Boolean {
        if (reminder.repeat != RepeatMode.CUSTOM_DAYS) return true
        if (reminder.daysOfWeek.isEmpty()) return false
        val day = LocalDateTime
            .ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            .dayOfWeek.value
        return reminder.daysOfWeek.contains(day)
    }

    private const val MAX_STEPS = 4000
}
