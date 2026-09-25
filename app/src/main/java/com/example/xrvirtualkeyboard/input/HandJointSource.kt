package com.example.xrvirtualkeyboard.input

import androidx.xr.arcore.Hand
import androidx.xr.runtime.Session
import com.example.xrvirtualkeyboard.geometry.Vec3
import com.example.xrvirtualkeyboard.geometry.isPinching
import com.example.xrvirtualkeyboard.geometry.toKeyboardLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * KeySelectionSource backed by ARCore hand tracking. Right hand only for now (see
 * class-level notes in code review). If Hand.right(session) is null at construction
 * (hand tracking unsupported/unavailable), this source degrades to permanently-empty
 * selection and permanently-false engagement rather than throwing.
 */
class HandJointSource(private val session: Session) : KeySelectionSource {

    // TODO: VirtualKeyboard's SpatialPanel has no .movable() yet, so there's no real
    // moving origin to track. Revisit once the keyboard is actually draggable — this
    // needs to observe the panel's live pose instead of a fixed value.
    private val keyboardOriginActivitySpace = Vec3(0f, 0f, 0f)

    private val handStateFlow: Flow<Hand.State>? = Hand.right(session)?.state

    override val selectionPoint: Flow<Point3D?> =
        handStateFlow?.map { handState ->
            val (_, indexTip) = handState.fingertipsInActivitySpace(session) ?: return@map null
            val local = toKeyboardLocal(indexTip, keyboardOriginActivitySpace)
            Point3D(local.x, local.y, local.z)
        } ?: flowOf(null)

    override val isEngaged: Flow<Boolean> =
        handStateFlow?.map { handState ->
            val (thumbTip, indexTip) = handState.fingertipsInActivitySpace(session) ?: return@map false
            isPinching(thumbTip, indexTip)
        } ?: flowOf(false)
}