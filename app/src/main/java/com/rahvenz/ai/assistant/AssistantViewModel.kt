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
    data object Speaking : VoiceState
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

    /** Call-style session: while true, RAHVENZ keeps the conversation going. */
    private val _callActive = MutableStateFlow(false)
    val callActive: StateFlow<Boolean> = _callActive.asStateFlow()

    init {
        // When RAHVENZ finishes speaking during a call, it starts listening again.
        tts.onFinishedSpeaking = {
            if (_callActive.value) {
                startListening()
            } else if (_voiceState.value is VoiceState.Speaking) {
                _voiceState.value = VoiceState.Idle
            }
        }
    }

    fun startCall() {
        _callActive.value = true
        startListening()
    }

    fun endCall() {
        _callActive.value = false
        recognizer?.cancel()
        tts.stop()
        _voiceState.value = VoiceState.Idle
    }

    fun sendUserText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        _messages.value = _messages.value + ChatMessage(nextId++, cleaned, isUser = true)

        when (val result = LocalCommandEngine.handle(cleaned)) {
            is LocalResult.Reply -> respond(result.hindiText)
            is LocalResult.Action -> {
                val outcome = AppLauncher.perform(getApplication(), result.action)
                respond(outcome.hindiText)
            }
            LocalResult.Unknown -> {
                respond("मुझे यह समझ नहीं आया। कृपया हिंदी में दोहराएँ या स्पष्ट करें।")
            }
        }
    }

    private fun respond(hindiText: String) {
        addAssistantMessage(hindiText)
        _voiceState.value = VoiceState.Speaking
        tts.speak(hindiText)
    }

    private fun addAssistantMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(nextId++, text, isUser = false)
    }

    // ---------- Voice picker (free device voices) ----------

    fun voiceNames(): List<String> = tts.voiceNames()
    fun currentVoiceName(): String = tts.currentVoiceName()
    fun selectVoice(name: String) {
        tts.setVoiceByName(name)
    }

    // ---------- Speech recognition ----------

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
            if (!text.isNullOrBlank()) {
                sendUserText(text)
            } else {
                _voiceState.value = VoiceState.Idle
                respond("मैंने सुना नहीं। कृपया फिर से बोलिए।")
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

    fun retryListening() {
        if (_callActive.value) startListening()
    }

    fun stopAssistant() = tts.stop()

    override fun onCleared() {
        recognizer?.destroy()
        recognizer = null
        tts.shutdown()
        super.onCleared()
    }
}
