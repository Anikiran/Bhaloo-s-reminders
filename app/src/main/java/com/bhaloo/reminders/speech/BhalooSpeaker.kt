package com.bhaloo.reminders.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.bhaloo.reminders.data.NameVoice
import com.bhaloo.reminders.data.VoiceLanguage

/**
 * One text-to-speech engine for the whole process, warmed at startup.
 *
 * Binding to a TTS engine is slow — one to three seconds on most phones — and
 * the app used to pay that cost on *every* playback, because each run built a
 * fresh engine and threw it away. That, stacked on top of starting a
 * foreground service and a deliberate wait for the notification chime, is what
 * made a reminder take four to six seconds to speak.
 *
 * Here the engine is created once, as early as the process exists, and kept.
 * Anything asked for before it finishes binding is queued and fires the
 * instant it is ready, so the wait is only ever paid once per process — and
 * usually not at all, because warming starts long before the first request.
 */
object BhalooSpeaker {

    private val main = Handler(Looper.getMainLooper())

    private var engine: TextToSpeech? = null
    private var ready = false
    private var failed = false
    private var queued: (() -> Unit)? = null

    /** Cheap to call repeatedly; only the first call does anything. */
    fun warmUp(context: Context) {
        if (engine != null) return
        val app = context.applicationContext
        engine = TextToSpeech(app) { status ->
            main.post {
                ready = status == TextToSpeech.SUCCESS
                failed = !ready
                val pending = queued
                queued = null
                if (ready) pending?.invoke() else pending?.let { onEngineUnavailable?.invoke() }
            }
        }
    }

    /** Called when the engine could not start, so callers can stop waiting. */
    var onEngineUnavailable: (() -> Unit)? = null

    /**
     * Speaks [text] [times] times. [onFinished] runs when the last line ends,
     * or immediately if the engine is unusable.
     */
    fun speak(
        context: Context,
        text: String,
        language: VoiceLanguage,
        times: Int,
        special: Boolean,
        spokenNameEnglish: String,
        rate: Float,
        pitch: Float,
        onFinished: () -> Unit
    ) {
        warmUp(context)
        val run = { doSpeak(text, language, times, special, spokenNameEnglish, rate, pitch, onFinished) }
        when {
            ready -> run()
            failed -> onFinished()
            // Still binding: hold the newest request and fire it on ready.
            else -> queued = run
        }
    }

    private fun doSpeak(
        text: String,
        language: VoiceLanguage,
        times: Int,
        special: Boolean,
        spokenNameEnglish: String,
        rate: Float,
        pitch: Float,
        onFinished: () -> Unit
    ) {
        val tts = engine ?: return onFinished()

        val locale = BhalooVoice.applyLanguage(tts, language)
        if (locale == null) return onFinished()
        BhalooVoice.preferOfflineVoice(tts, locale)

        tts.setSpeechRate(rate)
        tts.setPitch(pitch)
        runCatching {
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) {
                if (utteranceId == LAST) main.post(onFinished)
            }
            override fun onError(utteranceId: String?) { main.post(onFinished) }
            override fun onError(utteranceId: String?, errorCode: Int) { main.post(onFinished) }
        })

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
        }

        val lines = mutableListOf<String>()
        if (special) {
            lines += when (language) {
                VoiceLanguage.HINDI -> "भालू, आज का दिन ख़ास है।"
                VoiceLanguage.ENGLISH -> "Bhaloo, today is a special day."
            }
        }
        repeat(times.coerceIn(1, 5)) { lines += text }

        lines.map { NameVoice.forSpeech(it, language, spokenNameEnglish) }
            .forEachIndexed { index, line ->
                val last = index == lines.lastIndex
                tts.speak(
                    line,
                    if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    params,
                    if (last) LAST else "bhaloo_$index"
                )
                if (!last) tts.playSilentUtterance(GAP_MS, TextToSpeech.QUEUE_ADD, "gap_$index")
            }
    }

    fun stop() {
        queued = null
        runCatching { engine?.stop() }
    }

    /** If the alarm stream is muted the message would be silent. */
    fun nudgeAlarmVolume(context: Context) {
        runCatching {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audio.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
                val max = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audio.setStreamVolume(AudioManager.STREAM_ALARM, (max * 0.6f).toInt(), 0)
            }
        }
    }

    private const val LAST = "bhaloo_last"
    private const val GAP_MS = 900L
}
