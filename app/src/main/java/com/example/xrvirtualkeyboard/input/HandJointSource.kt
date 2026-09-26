package com.example.xrvirtualkeyboard.input

import androidx.xr.arcore.Hand
import androidx.xr.runtime.HandTrackingMode
import androidx.xr.runtime.Session
import com.example.xrvirtualkeyboard.geometry.Vec3
import com.example.xrvirtualkeyboard.geometry.isPinching
import com.example.xrvirtualkeyboard.geometry.toKeyboardLocal
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

/**
 * KeySelectionSource backed by ARCore hand tracking. Right hand only for now (see
 * class-level notes in code review).
 */
class HandJointSource(private val session: Session) : KeySelectionSource {

    // TODO: VirtualKeyboard's SpatialPanel has no .movable() yet, so there's no real
    // moving origin to track. Revisit once the keyboard is actually draggable — this
    // needs to observe the panel's live pose instead of a fixed value.
    private val keyboardOriginActivitySpace = Vec3(0f, 0f, 0f)

    private val handStateFlow: Flow<Hand.State> = flow {
        while (currentCoroutineContext().isActive) {
            if (session.config.handTracking != HandTrackingMode.DISABLED) {
                val hand = try {
                    Hand.right(session)
                } catch (_: IllegalStateException) {
                    null
                }
                hand?.state?.collect { emit(it) }
            }
            delay(100)
        }
    }

    override val selectionPoint: Flow<Point3D?> =
        handStateFlow.map { handState ->
            val (_, indexTip) = handState.fingertipsInActivitySpace(session) ?: return@map null
            val local = toKeyboardLocal(indexTip, keyboardOriginActivitySpace)
            // Spatial Y is +Y UP, -Y DOWN.
            // On 2D Compose UI surface, +Y is DOWN, -Y is UP.
            // Inverting Y maps spatial UP/DOWN directly to UI UP/DOWN.
            Point3D(local.x, -local.y, local.z)
        }

    override val isEngaged: Flow<Boolean> =
        handStateFlow.map { handState ->
            val (thumbTip, indexTip) = handState.fingertipsInActivitySpace(session) ?: return@map false
            isPinching(thumbTip, indexTip)
        }
}