package com.rahvenz.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

/**
 * Speaks Hindi (hi-IN) using the on-device/system TextToSpeech engine.
 * Phase 2B: exposes the device's free TTS voices so the user can pick one,
 * and notifies when speech finishes so the assistant can listen again.
 */
class HindiTts(context: Context) {

    private var tts: TextToSpeech? = null
    private val prefs = context.getSharedPreferences("rahvenz_prefs", Context.MODE_PRIVATE)
    private var selectedVoiceName: String? = prefs.getString("tts_voice", null)

    @Volatile
    var isReady: Boolean = false
        private set

    /** Called on the main thread when an utterance finishes naturally. */
    var onFinishedSpeaking: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val engine = tts ?: return@TextToSpeech
            val result = engine.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.getDefault())
            }
            engine.setPitch(1.05f)
            engine.setSpeechRate(0.95f)
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onError(id: String?) {}
                override fun onDone(id: String?) {
                    onFinishedSpeaking?.invoke()
                }
            })
            applySavedVoice()
            isReady = true
        }
    }

    /** Free voices installed on the device (Google TTS etc.), Hindi first. */
    fun availableVoices(): List<Voice> {
        if (!isReady) return emptyList()
        val all = try {
            tts?.voices
        } catch (e: Exception) {
            null
        } ?: return emptyList()
        return all.sortedByDescending { it.locale.language == "hi" }
    }

    fun voiceNames(): List<String> =
        availableVoices().map { it.name ?: "Default voice" }

    fun currentVoiceName(): String = selectedVoiceName ?: "Default voice"

    fun setVoiceByName(name: String): Boolean {
        val voice = availableVoices().firstOrNull { (it.name ?: "Default voice") == name }
            ?: return false
        val ok = tts?.setVoice(voice) == TextToSpeech.SUCCESS
        if (ok) {
            selectedVoiceName = name
            prefs.edit().putString("tts_voice", name).apply()
        }
        return ok
    }

    private fun applySavedVoice() {
        val name = selectedVoiceName ?: return
        availableVoices().firstOrNull { (it.name ?: "Default voice") == name }?.let {
            tts?.setVoice(it)
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
