package com.example.xrvirtualkeyboard.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardLocalTransformTest {

    @Test
    fun `point coincident with keyboard origin maps to local origin`() {
        val result = toKeyboardLocal(
            pointActivitySpace = Vec3(1f, 2f, 3f),
            keyboardOriginActivitySpace = Vec3(1f, 2f, 3f),
        )
        assertEquals(Vec3(0f, 0f, 0f), result)
    }

    @Test
    fun `point offset from origin subtracts correctly on all axes`() {
        val result = toKeyboardLocal(
            pointActivitySpace = Vec3(5f, 5f, 5f),
            keyboardOriginActivitySpace = Vec3(2f, 1f, 0.5f),
        )
        assertEquals(Vec3(3f, 4f, 4.5f), result)
    }

    @Test
    fun `keyboard sliding to a new origin shifts a fixed world point's local coords`() {
        val worldPoint = Vec3(10f, 0f, 0f)
        val beforeSlide = toKeyboardLocal(worldPoint, keyboardOriginActivitySpace = Vec3(8f, 0f, 0f))
        val afterSlide = toKeyboardLocal(worldPoint, keyboardOriginActivitySpace = Vec3(9f, 0f, 0f))

        assertEquals(Vec3(2f, 0f, 0f), beforeSlide)
        assertEquals(Vec3(1f, 0f, 0f), afterSlide)
    }
}