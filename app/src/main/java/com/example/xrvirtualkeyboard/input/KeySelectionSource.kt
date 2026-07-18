package com.example.xrvirtualkeyboard.input

import kotlinx.coroutines.flow.Flow

/** A point in the keyboard's local space. z stays at 0 for mouse input — see note below. */
data class Point3D(val x: Float, val y: Float, val z: Float)

interface KeySelectionSource {
    val selectionPoint: Flow<Point3D?>
    val isEngaged: Flow<Boolean>
}