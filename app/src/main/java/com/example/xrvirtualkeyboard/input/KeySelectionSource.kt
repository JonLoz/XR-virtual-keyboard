package com.example.xrvirtualkeyboard.input

import kotlinx.coroutines.flow.Flow

/**
 * Point in the keyboard surface's own local coordinate space — NOT world/activity space.
 * Each source implementation is responsible for performing whatever transform is
 * required to land in this frame. MouseClickSource gets this for free from Compose's
 * pointer coordinates; HandJointSource will need to explicitly transform from
 * activity-space joint poses relative to the keyboard panel's own pose.
 */
data class Point3D(val x: Float, val y: Float, val z: Float)

interface KeySelectionSource {
    val selectionPoint: Flow<Point3D?>

    /**
     * Whether the "select" action is currently active. For MouseClickSource, this is
     * literally "is the left mouse button held."
     *
     * Design note (open question for Phase 2): a future HandJointSource could define this
     * via depth-based proximity (finger crosses into a key's volume) or gesture-based (e.g.
     * a pinch), independent of position. Current working assumption is gesture-based —
     * depth-based engagement would require the source to know key geometry, breaking the
     * separation between input sources and hit-testing this interface exists to preserve.
     * Revisit once Phase 2 confirms what the ARCore Hand Tracking API actually exposes for
     * gesture recognition.
     */
    val isEngaged: Flow<Boolean>
}