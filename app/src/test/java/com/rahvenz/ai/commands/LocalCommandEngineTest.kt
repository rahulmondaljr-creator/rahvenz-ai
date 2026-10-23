package com.rahvenz.ai.commands

import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCommandEngineTest {

    @Test
    fun `time command returns hindi reply`() {
        val result = LocalCommandEngine.handle("samay kya hai")
        assertTrue(result is LocalResult.Reply)
    }

    @Test
    fun `hindi greeting returns reply`() {
        val result = LocalCommandEngine.handle("namaste")
        assertTrue((result as LocalResult.Reply).hindiText.isNotBlank())
    }

    @Test
    fun `stop command returns reply`() {
        val result = LocalCommandEngine.handle("ruk jao")
        assertTrue(result is LocalResult.Reply)
    }

    @Test
    fun `unknown input returns Unknown`() {
        assertTrue(LocalCommandEngine.handle("xyzzy qwerty plugh") is LocalResult.Unknown)
    }
}
