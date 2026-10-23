package com.rahvenz.ai.assistant

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import com.rahvenz.ai.actions.AppLauncher
import com.rahvenz.ai.commands.LocalCommandEngine
import com.rahvenz.ai.commands.LocalResult
import com.rahvenz.ai.voice.HindiTts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
)

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Processing : VoiceState
    data class Error(val code: Int) : VoiceState
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val tts = HindiTts(application)
    private var nextId = 1L
    private var recognizer: SpeechRecognizer? = null

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = nextId++,
                text = "नमस्ते! मैं RAHVENZ हूँ — आपका हिंदी भाषी सहायक।",
                isUser = false,
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    fun sendUserText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        _messages.value = _messages.value + ChatMessage(nextId++, cleaned, isUser = true)

        when (val result = LocalCommandEngine.handle(cleaned)) {
            is LocalResult.Reply -> {
                addAssistantMessage(result.hindiText)
                tts.speak(result.hindiText)
            }
            is LocalResult.Action -> {
                val outcome = AppLauncher.perform(getApplication(), result.action)
                addAssistantMessage(outcome.hindiText)
                tts.speak(outcome.hindiText)
            }
            LocalResult.Unknown -> {
                val ask = "मुझे यह समझ नहीं आया। कृपया हिंदी में दोहराएँ या स्पष्ट करें।"
                addAssistantMessage(ask)
                tts.speak(ask)
            }
        }
    }

    private fun addAssistantMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(nextId++, text, isUser = false)
    }

    // ---------- Voice input (Phase 2) ----------

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = VoiceState.Listening
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            _voiceState.value = VoiceState.Processing
        }
        override fun onError(error: Int) {
            _voiceState.value = VoiceState.Error(error)
        }
        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            _voiceState.value = VoiceState.Idle
            if (!text.isNullOrBlank()) {
                sendUserText(text)
            } else {
                val ask = "मैंने सुना नहीं। कृपया फिर से बोलिए।"
                addAssistantMessage(ask)
                tts.speak(ask)
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        tts.stop()
        val app = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _voiceState.value = VoiceState.Error(SpeechRecognizer.ERROR_NO_MATCH)
            return
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(app).also {
                it.setRecognitionListener(recognitionListener)
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        _voiceState.value = VoiceState.Processing
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _voiceState.value = VoiceState.Idle
    }

    fun stopAssistant() = tts.stop()

    override fun onCleared() {
        recognizer?.destroy()
        recognizer = null
        tts.shutdown()
        super.onCleared()
    }
}
