package com.example.xrvirtualkeyboard.geometry

import org.junit.Assert
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
        val target = bounds[0]
        Assert.assertEquals(target.id, hitTestKey(Point2D(target.center.x, target.center.y), bounds))
    }

    @Test
    fun `point in the gap between keys hits nothing`() {
        val gapX = (bounds[0].center.x + bounds[1].center.x) / 2f
        Assert.assertEquals(null, hitTestKey(Point2D(gapX, 0f), bounds))
    }

    @Test
    fun `point inside second key hits that key`() {
        val target = bounds[1]
        Assert.assertEquals("W", hitTestKey(Point2D(target.center.x, target.center.y), bounds))
    }
}