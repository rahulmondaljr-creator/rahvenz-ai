package com.rahvenz.ai.commands

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface LocalResult {
    data class Reply(val hindiText: String) : LocalResult
    data class Action(val action: PhoneAction) : LocalResult
    data object Unknown : LocalResult
}

/**
 * Phase 2 offline command engine.
 * Understands short Hindi / Hinglish commands and answers in Hindi.
 * Anything unrecognized returns Unknown so the caller can ask the user
 * to repeat or clarify in Hindi (never guess and act).
 */
object LocalCommandEngine {

    private val hindiLocale = Locale("hi", "IN")

    fun handle(raw: String): LocalResult {
        val t = raw.trim().lowercase()
        fun has(vararg keys: String) = keys.any { it in t }

        return when {
            has("ruk", "रुक", "ruk jao", "रुको", "chup", "चुप", "bas karo", "बस करो", "stop") ->
                LocalResult.Reply("ठीक है, मैं रुक गया।")

            has("kholo", "खोलो", "khol do", "खोल दो", "khul", "khul jao") -> {
                val target = t
                    .replace("kholo", "")
                    .replace("khol do", "")
                    .replace("khul jao", "")
                    .replace("khul", "")
                    .replace("खोलो", "")
                    .replace("खोल दो", "")
                    .trim()
                if (target.isNotEmpty()) {
                    LocalResult.Action(PhoneAction.OpenTarget(target))
                } else {
                    LocalResult.Unknown
                }
            }

            has("samay", "समय", "time") &&
                has("kya", "क्या", "batao", "बताओ", "bata", "बता", "hai", "है") ->
                LocalResult.Reply("अभी ${currentTime()} बज रहा है।")

            has("tarikh", "तारीख", "tareekh", "date") &&
                has("kya", "क्या", "batao", "बताओ", "hai", "है") ->
                LocalResult.Reply("आज ${currentDate()} है।")

            has("din", "दिन") && has("kaun", "कौन", "konsa", "कौन सा") ->
                LocalResult.Reply("आज ${currentDay()} है।")

            t == "hi" || t == "hii" || t == "hello" || t == "hey" ||
                has("namaste", "नमस्ते", "namaskar", "नमस्कार", "pranam", "प्रणाम") ->
                LocalResult.Reply("नमस्ते! मैं RAHVENZ हूँ। आप कैसे हैं? मैं आपकी क्या मदद कर सकता हूँ?")

            has("kaun ho", "कौन हो", "tumhara naam", "तुम्हारा नाम", "tum kaun", "तुम कौन",
                "aapka naam", "आपका नाम") ->
                LocalResult.Reply("मैं RAHVENZ AI हूँ — आपका हिंदी भाषी सहायक। मैं आपके फोन पर काम करता हूँ और आपकी मदद के लिए यहाँ हूँ।")

            has("kaise ho", "कैसे हो", "kaisi ho", "कैसी हो", "how are you") ->
                LocalResult.Reply("मैं बिल्कुल ठीक हूँ, धन्यवाद! बताइए, आज मैं आपकी क्या मदद करूँ?")

            has("dhanyavad", "धन्यवाद", "shukriya", "शुक्रिया", "thank") ->
                LocalResult.Reply("आपका स्वागत है! और कुछ मदद चाहिए तो बताइए।")

            has("madad", "मदद", "help") ->
                LocalResult.Reply("मैं ये काम कर सकता हूँ: समय और तारीख बताना, Chrome, YouTube, WhatsApp, Gmail, Maps, Play Store, Photos, Settings, Camera खोलना। बोलिए, जैसे: Chrome खोलो।")

            else -> LocalResult.Unknown
        }
    }

    private fun currentTime(): String =
        SimpleDateFormat("h:mm a", hindiLocale).format(Date())

    private fun currentDate(): String =
        SimpleDateFormat("d MMMM yyyy", hindiLocale).format(Date())

    private fun currentDay(): String =
        SimpleDateFormat("EEEE", hindiLocale).format(Date())
}
