package com.bhaloo.reminders.util

import com.bhaloo.reminders.data.VoiceLanguage
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * All the little phrases that make the app sound like it belongs to Bhaloo
 * and nobody else.
 */
object BhalooWords {

    private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    private val shortDateFormat = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(timeFormat)

    fun formatDate(dateTime: LocalDateTime): String = dateTime.format(dateFormat)

    fun formatShortDate(dateTime: LocalDateTime): String = dateTime.format(shortDateFormat)

    /** "Good morning, Bhaloo" and friends, rotated by the clock. */
    fun greeting(now: LocalDateTime = LocalDateTime.now()): String = when (now.hour) {
        in 5..11 -> "Good morning, Bhaloo"
        in 12..16 -> "Good afternoon, Bhaloo"
        in 17..20 -> "Good evening, Bhaloo"
        else -> "Still awake, Bhaloo?"
    }

    /** The same greeting in Hindi, shown underneath. */
    fun greetingHindi(now: LocalDateTime = LocalDateTime.now()): String = when (now.hour) {
        in 5..11 -> "सुप्रभात, भालू!"
        in 12..16 -> "नमस्ते, भालू!"
        in 17..20 -> "शुभ संध्या, भालू!"
        else -> "भालू, अभी तक जाग रहे हो?"
    }

    /** One warm line under the greeting; changes every day so it never gets stale. */
    fun lineOfTheDay(date: LocalDate = LocalDate.now()): String {
        val lines = listOf(
            "Nothing important slips past us today.",
            "Your personal hawaldar is on duty. 🐻",
            "Paani peena mat bhoolna, Bhaloo.",
            "One thing at a time — I'll shout when it's time.",
            "Made for you, so it only cares about your list.",
            "Aaj ka plan sambhal liya maine.",
            "If you forget, that's my fault, not yours."
        )
        return lines[(date.toEpochDay().mod(lines.size))]
    }

    /** "in 2 hours", "tomorrow at 7:00 AM", "3 days ago". */
    fun relative(target: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(now, target).toMinutes()
        val past = minutes < 0
        val abs = kotlin.math.abs(minutes)
        val phrase = when {
            abs < 1L -> "now"
            abs < 60L -> "$abs min"
            abs < 60L * 24L -> "${abs / 60} hr ${abs % 60} min".removeSuffix(" 0 min")
            abs < 60L * 24L * 7L -> "${abs / (60 * 24)} days"
            else -> "${abs / (60 * 24 * 7)} weeks"
        }
        return when {
            phrase == "now" -> "right now"
            past -> "$phrase ago"
            else -> "in $phrase"
        }
    }

    /** Sentence describing when the reminder next speaks, e.g. "Tomorrow, 7:00 AM". */
    fun whenLabel(dateTime: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val today = now.toLocalDate()
        val day = dateTime.toLocalDate()
        val prefix = when (ChronoUnit.DAYS.between(today, day)) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            else -> formatDate(dateTime)
        }
        return "$prefix, ${formatTime(dateTime)}"
    }

    fun daysTogether(installedAtMillis: Long): Long {
        val start = Instant.ofEpochMilli(installedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(start, LocalDate.now()).coerceAtLeast(0)
    }

    /** Suggested first message when someone picks a language in the editor. */
    fun sampleMessage(language: VoiceLanguage): String = when (language) {
        VoiceLanguage.HINDI -> "भालू, दवाई लेने का समय हो गया है।"
        VoiceLanguage.ENGLISH -> "Bhaloo, it's time to take your medicine."
    }

    /** Next round half-hour — a friendly default when creating a reminder. */
    fun nextRoundTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val bumped = now.plusMinutes(30)
        val minute = if (bumped.minute < 30) 30 else 0
        val base = bumped.withMinute(minute).withSecond(0).withNano(0)
        return if (minute == 0) base.plusHours(1) else base
    }

    fun atTime(date: LocalDate, time: LocalTime): LocalDateTime = LocalDateTime.of(date, time)
}
