package com.rahvenz.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Speaks Hindi (hi-IN) using the on-device/system TextToSpeech engine.
 * Phase 1: no audio is recorded or uploaded anywhere.
 */
class HindiTts(context: Context) {

    private var tts: TextToSpeech? = null

    @Volatile
    var isReady: Boolean = false
        private set

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val engine = tts ?: return@TextToSpeech
            val result = engine.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fall back to the device default voice rather than failing silently.
                engine.setLanguage(Locale.getDefault())
            }
            engine.setPitch(1.05f)
            engine.setSpeechRate(0.95f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "rahvenz_reply")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
