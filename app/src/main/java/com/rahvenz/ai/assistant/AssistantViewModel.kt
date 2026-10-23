package com.rahvenz.ai.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val tts = HindiTts(application)
    private var nextId = 1L

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

    fun sendUserText(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return

        _messages.value = _messages.value + ChatMessage(nextId++, cleaned, isUser = true)

        val reply = when (val result = LocalCommandEngine.handle(cleaned)) {
            is LocalResult.Reply -> result.hindiText
            LocalResult.Unknown -> "मुझे यह समझ नहीं आया। कृपया हिंदी में दोहराएँ या स्पष्ट करें।"
        }
        _messages.value = _messages.value + ChatMessage(nextId++, reply, isUser = false)
        tts.speak(reply)
    }

    fun stopAssistant() = tts.stop()

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
