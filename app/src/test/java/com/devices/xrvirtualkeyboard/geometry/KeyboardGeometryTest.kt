package com.devices.xrvirtualkeyboard.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardGeometryTest {
    private  val bounds = computeRowBounds(
        listOf(
            KeyDefinition("Q", "Q"),
            KeyDefinition("W", "W"),
        )
    )

    @Test
    fun `point inside first key hits that key`() {
        assertEquals("Q", hitTestKey(Point2D(10f, 10f), bounds))
    }

    @Test
    fun `point in the gap between keys hits nothing`() {
        assertEquals(null, hitTestKey(Point2D(70f, 0f), bounds))
    }

    @Test
    fun `point inside second key hits that key`() {
        assertEquals("W", hitTestKey(Point2D(140f, -10f), bounds))
    }
}