package com.rahvenz.ai.actions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.rahvenz.ai.commands.PhoneAction

data class ActionOutcome(val success: Boolean, val hindiText: String)

/**
 * Executes only safe, user-requested actions via standard Android intents.
 * No background activity, no protected-data access, no automation.
 */
object AppLauncher {

    private data class Target(
        val aliases: List<String>,
        val displayName: String,
        val packageName: String? = null,
        val intentAction: String? = null,
        val webUrl: String? = null,
    )

    private val targets = listOf(
        Target(listOf("chrome", "क्रोम", "browser", "ब्राउज़र"), "Chrome", packageName = "com.android.chrome"),
        Target(listOf("youtube", "यूट्यूब"), "YouTube", packageName = "com.google.android.youtube"),
        Target(listOf("whatsapp", "व्हाट्सऐप", "व्हाट्सएप"), "WhatsApp", packageName = "com.whatsapp"),
        Target(listOf("gmail", "जीमेल", "email", "ईमेल"), "Gmail", packageName = "com.google.android.gm"),
        Target(listOf("maps", "मैप", "map"), "Google Maps", packageName = "com.google.android.apps.maps"),
        Target(listOf("play store", "प्ले स्टोर", "playstore"), "Play Store", packageName = "com.android.vending"),
        Target(listOf("photos", "फोटो", "gallery", "गैलरी"), "Photos", packageName = "com.google.android.apps.photos"),
        Target(listOf("settings", "सेटिंग", "setting"), "Settings", intentAction = Settings.ACTION_SETTINGS),
        Target(listOf("camera", "कैमरा"), "Camera", intentAction = "android.media.action.IMAGE_CAPTURE"),
        Target(listOf("google", "गूगल"), "Google", webUrl = "https://www.google.com"),
    )

    fun perform(context: Context, action: PhoneAction): ActionOutcome = when (action) {
        is PhoneAction.OpenTarget -> openTarget(context, action.target)
    }

    private fun openTarget(context: Context, rawTarget: String): ActionOutcome {
        val t = rawTarget.trim().lowercase()
        val target = targets.firstOrNull { spec -> spec.aliases.any { alias -> t.contains(alias) } }
            ?: return ActionOutcome(false, "मुझे यह ऐप नहीं मिला। कृपया दोबारा बताइए।")

        return try {
            when {
                target.intentAction != null -> {
                    context.startActivity(Intent(target.intentAction).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    ActionOutcome(true, "${target.displayName} खोल रहा हूँ।")
                }
                target.webUrl != null -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target.webUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    ActionOutcome(true, "${target.displayName} खोल रहा हूँ।")
                }
                else -> {
                    val launch = context.packageManager.getLaunchIntentForPackage(target.packageName!!)
                    if (launch == null) {
                        ActionOutcome(false, "${target.displayName} आपके फोन में इन्स्टॉल नहीं है।")
                    } else {
                        context.startActivity(launch)
                        ActionOutcome(true, "${target.displayName} खोल रहा हूँ।")
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            ActionOutcome(false, "${target.displayName} खोलने में समस्या हुई।")
        } catch (e: SecurityException) {
            ActionOutcome(false, "इसे खोलने की अनुमति नहीं है।")
        }
    }
}
