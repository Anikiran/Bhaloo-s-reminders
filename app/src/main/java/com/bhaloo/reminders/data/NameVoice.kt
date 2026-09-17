package com.bhaloo.reminders.data

/**
 * Getting the name said right.
 *
 * "भालू" opens with an aspirated *bh* — a sound English simply does not have.
 * An English voice reading the letters "Bhaloo" drops the aspiration and
 * shortens the vowel, so it comes out "Balu" instead of "Bhaa-loo".
 *
 * The fix is to hand the English voice a spelling shaped like the sound rather
 * than like the name: extra vowel length ("aa") holds the first syllable, and
 * splitting the syllables stops the engine collapsing them. Hindi needs none of
 * this — it gets the Devanagari, which it reads correctly.
 *
 * Which spelling wins depends on the TTS engine on the phone, so the English
 * spelling is a setting with a preview button rather than a constant: it can be
 * tuned by ear on the actual device in a few seconds.
 */
object NameVoice {

    /** What the app is called in writing. */
    const val WRITTEN_EN = "Bhaloo"
    const val WRITTEN_HI = "भालू"

    /**
     * Default phonetic spelling for English voices. The doubled "aa" lengthens
     * the first syllable and the hyphen keeps the two syllables apart, which is
     * what stops "Bhaa-loo" collapsing into "Balu".
     */
    const val DEFAULT_SPOKEN_EN = "Bhaa-loo"

    /** Spellings offered in Settings, best first, for tuning by ear. */
    val SUGGESTIONS_EN = listOf(
        "Bhaa-loo",
        "Bhaaloo",
        "Bha loo",
        "Bhalu",
        "Buh-aa-loo",
        "Bhaloo"
    )

    /**
     * Rewrites every written form of the name in [text] into the spelling the
     * given voice pronounces correctly.
     *
     * Hindi is returned untouched: a Hindi voice reads भालू correctly, and
     * feeding it a romanised spelling would make things worse.
     */
    fun forSpeech(text: String, language: VoiceLanguage, spokenEnglish: String): String {
        if (language == VoiceLanguage.HINDI) return text
        val spelling = spokenEnglish.ifBlank { DEFAULT_SPOKEN_EN }
        var out = text
        // Longest first, so "Bhaloo" is replaced before a bare "Bhalu".
        for (written in WRITTEN_FORMS) {
            out = out.replace(written, spelling, ignoreCase = true)
        }
        // A Devanagari name inside an English sentence would be read as a
        // string of letter names, or skipped entirely.
        out = out.replace(WRITTEN_HI, spelling)
        return out
    }

    private val WRITTEN_FORMS = listOf("Bhaloo", "Bhalu", "Baloo")
}
