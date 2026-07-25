package com.example.xrvirtualkeyboard.input

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MouseClickSource(
    private val surfaceWidthDp: Float,
    private val surfaceHeightDp: Float
) : KeySelectionSource {

    private val rawPointerPx = MutableStateFlow<Offset?>(null)
    private val densityState = MutableStateFlow<Density?>(null)
    private val engage = MutableStateFlow(false)

    override val isEngaged: Flow<Boolean> = engage

    override val selectionPoint: Flow<Point3D?> =
        combine(rawPointerPx, densityState) { pointer, density ->
            if (pointer == null || density == null) return@combine null
            with(density) {
                val xDp = pointer.x.toDp().value
                val yDp = pointer.y.toDp().value
                // Raw pointer coords are top-left-origin; our geometry is center-origin.
                Point3D(
                    x = xDp - surfaceWidthDp / 2f,
                    y = yDp - surfaceHeightDp / 2f,
                    z = 0f,
                )
            }
        }

    fun updatePointerPosition(position: Offset, density: Density) {
        rawPointerPx.value = position
        densityState.value = density
    }

    fun updateEngaged(isPressed: Boolean) {
        engage.value = isPressed
    }

    fun clearPointer() {
        rawPointerPx.value = null
        engage.value = false
    }

}