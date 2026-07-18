package com.example.xrvirtualkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyDebouncerTest {
    private var fakeTime = 0L
    private val debouncer = KeyDebouncer(
        initialRepeatDelayMs = 400L,
        repeatIntervalMs = 100L,
        nowMs = { fakeTime }
    )

    @Test
    fun `hovering without engagement produces no events`() {
        assertTrue(debouncer.update(hoveredKeyId = "Q", isEngaged = false).isEmpty())
    }

    @Test
    fun `engaging produces exactly one Pressed event`() {
        assertEquals(listOf(KeyEvent.Pressed("Q")), debouncer.update("Q", true))
    }

    @Test
    fun `holding before the initial delay produces no repeat`() {
        debouncer.update("Q", true)
        fakeTime += 200L
        assertTrue(debouncer.update("Q", true).isEmpty())
    }

    @Test
    fun `holding past the initial delay produces a Repeated event`() {
        debouncer.update("Q", true)
        fakeTime += 400L
        assertEquals(listOf(KeyEvent.Repeated("Q")), debouncer.update("Q", true))
    }

    @Test
    fun `releasing produces a Released event`() {
        debouncer.update("Q", true)
        assertEquals(listOf(KeyEvent.Released("Q")), debouncer.update("Q", false))
    }

    @Test
    fun `sliding to a different key while engaged releases old and presses new`() {
        debouncer.update("Q", true)
        fakeTime += 400L
        assertEquals(
            listOf(KeyEvent.Released("Q"), KeyEvent.Pressed("W")),
            debouncer.update("W", true)
        )
    }
}