package com.example.xrvirtualkeyboard.geometry

/**
 * Pinch-gesture detection: true when the thumb and index fingertip positions are
 * within [thresholdMeters] of each other. Positions must be in the same coordinate
 * frame (e.g. both in activity space) — this function doesn't care which frame, only
 * that they match.
 *
 * Pure spatial check only, single frame — no temporal smoothing here. Flicker at the
 * threshold boundary is KeyDebouncer's job, same as it already is for hover/press;
 * duplicating that here would split debounce logic across two places.
 *
 * 0.05m mirrors the threshold in Google's own ARCore pinch-detection sample —
 * a starting point to tune empirically once this runs against real hand data,
 * not a validated constant.
 */
fun isPinching(thumbTip: Vec3, indexTip: Vec3, thresholdMeters: Float = 0.05f): Boolean {
    val dx = thumbTip.x - indexTip.x
    val dy = thumbTip.y - indexTip.y
    val dz = thumbTip.z - indexTip.z
    return (dx * dx + dy * dy + dz * dz) <= thresholdMeters * thresholdMeters
}