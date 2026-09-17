package com.bhaloo.reminders.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** Which voice Bhaloo hears the reminder in. */
enum class VoiceLanguage(val tag: String) {
    ENGLISH("en-IN"),
    HINDI("hi-IN");

    companion object {
        fun from(name: String?): VoiceLanguage =
            entries.firstOrNull { it.name == name } ?: ENGLISH
    }
}

/** How loudly the reminder announces itself. */
enum class AlertStyle {
    /** Silent-ish: notification only, no speaking. */
    NOTIFICATION,

    /** Speak the message, no notification sound of its own. */
    VOICE,

    /** Notification + Bhaloo's voice. The default, because that is the whole point. */
    BOTH;

    val speaks: Boolean get() = this == VOICE || this == BOTH

    companion object {
        fun from(name: String?): AlertStyle = entries.firstOrNull { it.name == name } ?: BOTH
    }
}

/** Calendar rule that decides when a reminder comes back. */
enum class RepeatMode {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM_DAYS;

    companion object {
        fun from(name: String?): RepeatMode = entries.firstOrNull { it.name == name } ?: ONCE
    }
}

data class Reminder(
    val id: Long,
    val title: String,
    /** Exact words spoken out loud. Blank means "say the title". */
    val spokenMessage: String = "",
    val language: VoiceLanguage = VoiceLanguage.ENGLISH,
    /** Epoch millis of the next time this reminder should fire. */
    val timeMillis: Long,
    val repeat: RepeatMode = RepeatMode.ONCE,
    /** Only used by [RepeatMode.CUSTOM_DAYS]; values are [DayOfWeek.getValue] (1 = Monday). */
    val daysOfWeek: Set<Int> = emptySet(),
    val enabled: Boolean = true,
    /** How many times the message is repeated out loud (1..5). */
    val speakTimes: Int = 2,
    val alertStyle: AlertStyle = AlertStyle.BOTH,
    val vibrate: Boolean = true,
    /** Birthdays and the like: extra warmth in the spoken line, a cake on the card. */
    val isSpecialDay: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastFiredAt: Long = 0L,
    val timesCompleted: Int = 0
) {
    val dateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault())

    val isRepeating: Boolean get() = repeat != RepeatMode.ONCE

    /** True once a one-shot reminder is in the past — it stays in the list, greyed out. */
    val isDone: Boolean
        get() = repeat == RepeatMode.ONCE && timeMillis <= System.currentTimeMillis()

    /** What actually gets spoken, once Bhaloo's name has been woven in. */
    fun speechText(): String {
        val body = spokenMessage.ifBlank { title }.trim()
        if (body.isEmpty()) return defaultNudge(language)
        return when (language) {
            VoiceLanguage.HINDI -> if (containsBhaloo(body)) body else "भालू, $body"
            VoiceLanguage.ENGLISH -> if (containsBhaloo(body)) body else "Bhaloo, $body"
        }
    }

    private fun containsBhaloo(text: String): Boolean =
        text.contains("bhaloo", ignoreCase = true) || text.contains("भालू")

    companion object {
        fun defaultNudge(language: VoiceLanguage): String = when (language) {
            VoiceLanguage.HINDI -> "भालू, ये आपका रिमाइंडर है।"
            VoiceLanguage.ENGLISH -> "Bhaloo, this is your reminder."
        }

        /** A fresh reminder pre-filled for the next round hour. */
        fun blank(at: LocalDateTime): Reminder = Reminder(
            id = 0L,
            title = "",
            timeMillis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
}

/** 1 = Monday .. 7 = Sunday, matching [DayOfWeek.getValue]. */
fun dayOfWeekValues(): List<Int> = DayOfWeek.values().map { it.value }
