package com.rahvenz.ai.commands

/**
 * Safe, explicit phone actions the assistant may perform.
 * Phase 2: opening apps only. Later phases extend this list.
 */
sealed interface PhoneAction {
    data class OpenTarget(val target: String) : PhoneAction
}
