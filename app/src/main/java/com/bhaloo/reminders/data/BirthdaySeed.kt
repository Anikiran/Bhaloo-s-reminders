package com.bhaloo.reminders.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Bhaloo's birthday, already in the app before the app is ever handed over.
 *
 * It is seeded once, on first launch, so the very first thing in the list is
 * the one date this whole thing was really built around. Delete it in the app
 * and it stays deleted — the seed never runs twice.
 */
object BirthdaySeed {

    /** 31 July. */
    private val BIRTHDAY: LocalDate = LocalDate.of(2000, 7, 31)

    /** Midnight, so the app is the first one to wish them. */
    private val WISH_AT: LocalTime = LocalTime.MIDNIGHT

    /**
     * Spoken in Hindi regardless of the phone's UI language: the voice is
     * fixed to [VoiceLanguage.HINDI], so the words have to match it.
     */
    private const val WISH =
        "भालू, जन्मदिन मुबारक हो! तुम्हारा ये दिन और पूरा साल ख़ुशियों से भरा रहे।"

    fun build(title: String, note: String, today: LocalDate = LocalDate.now()): Reminder {
        val at = nextBirthday(today).atTime(WISH_AT)
        return Reminder(
            id = 0L,
            title = title,
            spokenMessage = WISH,
            language = VoiceLanguage.HINDI,
            timeMillis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            repeat = RepeatMode.YEARLY,
            enabled = true,
            speakTimes = 2,
            alertStyle = AlertStyle.BOTH,
            vibrate = true,
            isSpecialDay = true,
            note = note
        )
    }

    /** This year's birthday if it is still ahead, otherwise next year's. */
    fun nextBirthday(today: LocalDate = LocalDate.now()): LocalDate {
        val thisYear = LocalDate.of(today.year, BIRTHDAY.month, BIRTHDAY.dayOfMonth)
        return if (!thisYear.isBefore(today)) thisYear else thisYear.plusYears(1)
    }

    /** Exposed for the editor's preview text and for tests. */
    fun wishAt(today: LocalDate = LocalDate.now()): LocalDateTime =
        nextBirthday(today).atTime(WISH_AT)
}
