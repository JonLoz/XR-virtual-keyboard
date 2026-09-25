package com.example.xrvirtualkeyboard.geometry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinchDetectorTest {

    @Test
    fun `fingertips well within threshold are pinching`() {
        assertTrue(isPinching(Vec3(0f, 0f, 0f), Vec3(0.01f, 0f, 0f)))
    }

    @Test
    fun `fingertips well outside threshold are not pinching`() {
        assertFalse(isPinching(Vec3(0f, 0f, 0f), Vec3(0.20f, 0f, 0f)))
    }

    @Test
    fun `distance exactly at threshold counts as pinching`() {
        assertTrue(isPinching(Vec3(0f, 0f, 0f), Vec3(0.05f, 0f, 0f), thresholdMeters = 0.05f))
    }

    @Test
    fun `distance is computed across all three axes, not just x`() {
        // 3-4-5 triangle across y/z only: distance is exactly 0.05
        assertTrue(isPinching(Vec3(0f, 0f, 0f), Vec3(0f, 0.03f, 0.04f)))
    }
}