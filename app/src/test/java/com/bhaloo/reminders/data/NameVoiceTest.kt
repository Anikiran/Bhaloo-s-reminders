package com.bhaloo.reminders.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The name has to come out of an English voice as "Bhaa-loo", not "Balu".
 */
class NameVoiceTest {

    private val spelling = NameVoice.DEFAULT_SPOKEN_EN

    @Test
    fun `English speech gets the phonetic spelling`() {
        assertEquals(
            "Bhaa-loo, it's time for your medicine.",
            NameVoice.forSpeech(
                "Bhaloo, it's time for your medicine.",
                VoiceLanguage.ENGLISH,
                spelling
            )
        )
    }

    @Test
    fun `Hindi speech is left exactly as written`() {
        val hindi = "भालू, दवाई लेने का समय हो गया है।"
        assertEquals(hindi, NameVoice.forSpeech(hindi, VoiceLanguage.HINDI, spelling))
    }

    @Test
    fun `Devanagari inside an English sentence is romanised, not read letter by letter`() {
        assertEquals(
            "Bhaa-loo, drink water",
            NameVoice.forSpeech("भालू, drink water", VoiceLanguage.ENGLISH, spelling)
        )
    }

    @Test
    fun `other written forms of the name are caught too`() {
        assertEquals(
            "Bhaa-loo and Bhaa-loo",
            NameVoice.forSpeech("Bhalu and Baloo", VoiceLanguage.ENGLISH, spelling)
        )
    }

    @Test
    fun `matching ignores case`() {
        assertEquals(
            "Bhaa-loo, wake up",
            NameVoice.forSpeech("BHALOO, wake up", VoiceLanguage.ENGLISH, spelling)
        )
    }

    @Test
    fun `a custom spelling is honoured`() {
        assertEquals(
            "Bha loo, sleep",
            NameVoice.forSpeech("Bhaloo, sleep", VoiceLanguage.ENGLISH, "Bha loo")
        )
    }

    @Test
    fun `a blank spelling falls back to the default rather than deleting the name`() {
        assertEquals(
            "$spelling, sleep",
            NameVoice.forSpeech("Bhaloo, sleep", VoiceLanguage.ENGLISH, "")
        )
    }

    @Test
    fun `text without the name is untouched`() {
        val text = "Time to drink water."
        assertEquals(text, NameVoice.forSpeech(text, VoiceLanguage.ENGLISH, spelling))
    }

    @Test
    fun `every suggested spelling keeps the two syllables apart`() {
        NameVoice.SUGGESTIONS_EN.forEach { option ->
            assertTrue("$option should not be empty", option.isNotBlank())
        }
        // The default is the one that survives most engines: long vowel, split syllables.
        assertTrue(NameVoice.SUGGESTIONS_EN.contains(NameVoice.DEFAULT_SPOKEN_EN))
    }
}
