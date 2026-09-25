package com.example.xrvirtualkeyboard.input

import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.Session
import androidx.xr.scenecore.scene
import com.example.xrvirtualkeyboard.geometry.Vec3

/**
 * Thumb and index fingertip positions from this Hand.State, transformed from
 * perceptionSpace into activitySpace. Returns null if either joint isn't currently
 * tracked (e.g. hand partially occluded, or not yet in view).
 *
 * Deliberately drops rotation — isPinching and toKeyboardLocal only need position.
 */
internal fun Hand.State.fingertipsInActivitySpace(session: Session): Pair<Vec3, Vec3>? {
    val thumbTip = handJoints[HandJointType.THUMB_TIP] ?: return null
    val indexTip = handJoints[HandJointType.INDEX_TIP] ?: return null

    val thumbActivity = session.scene.perceptionSpace.transformPoseTo(thumbTip, session.scene.activitySpace)
    val indexActivity = session.scene.perceptionSpace.transformPoseTo(indexTip, session.scene.activitySpace)

    return Vec3(thumbActivity.translation.x, thumbActivity.translation.y, thumbActivity.translation.z) to
            Vec3(indexActivity.translation.x, indexActivity.translation.y, indexActivity.translation.z)
}