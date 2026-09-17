package com.bhaloo.reminders.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.bhaloo.reminders.data.VoiceLanguage
import java.util.Locale

/**
 * Thin wrapper over [TextToSpeech] that knows about Bhaloo's two languages and
 * degrades gracefully when a device has no Hindi voice installed.
 */
object BhalooVoice {

    fun localeFor(language: VoiceLanguage): Locale = when (language) {
        VoiceLanguage.HINDI -> Locale("hi", "IN")
        VoiceLanguage.ENGLISH -> Locale("en", "IN")
    }

    /**
     * Picks the best available locale for [language]: the exact one, then a
     * looser variant, then English as a last resort.
     * Returns null when nothing usable is installed.
     */
    fun applyLanguage(tts: TextToSpeech, language: VoiceLanguage): Locale? {
        val preferred = localeFor(language)
        val candidates = when (language) {
            VoiceLanguage.HINDI -> listOf(preferred, Locale("hi"), Locale.US)
            VoiceLanguage.ENGLISH -> listOf(preferred, Locale.UK, Locale.US, Locale.ENGLISH)
        }
        for (locale in candidates) {
            val result = runCatching { tts.setLanguage(locale) }.getOrDefault(
                TextToSpeech.LANG_NOT_SUPPORTED
            )
            if (result == TextToSpeech.LANG_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            ) {
                return locale
            }
        }
        return null
    }

    /** True when the device can actually speak Hindi today. */
    fun isLanguageInstalled(tts: TextToSpeech, language: VoiceLanguage): Boolean {
        val result = runCatching { tts.isLanguageAvailable(localeFor(language)) }
            .getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)
        return result >= TextToSpeech.LANG_AVAILABLE
    }

    /** Prefers a non-network voice so reminders still speak with no signal. */
    fun preferOfflineVoice(tts: TextToSpeech, locale: Locale) {
        runCatching {
            val offline: Voice? = tts.voices
                ?.filter { it.locale.language == locale.language && !it.isNetworkConnectionRequired }
                ?.minByOrNull { it.latency }
            if (offline != null) tts.voice = offline
        }
    }

    /** Opens the system screen where voice data can be installed. */
    fun installVoiceDataIntent(): android.content.Intent =
        android.content.Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)

    fun hasAnyEngine(context: Context): Boolean {
        val intent = android.content.Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        return context.packageManager.queryIntentServices(intent, 0).isNotEmpty()
    }
}
