package com.example.xrvirtualkeyboard.geometry

/**
 * Transforms a point from activity space into the keyboard's own local coordinate
 * space, given the keyboard's current origin in activity space.
 *
 * Slide-only assumption: the keyboard can move across its supporting surface but
 * never rotates, so this is pure translation — no orientation/rotation math. If the
 * keyboard ever becomes rotatable, this function's contract changes and every caller
 * needs revisiting.
 */
fun toKeyboardLocal(pointActivitySpace: Vec3, keyboardOriginActivitySpace: Vec3): Vec3 =
    Vec3(
        x = pointActivitySpace.x - keyboardOriginActivitySpace.x,
        y = pointActivitySpace.y - keyboardOriginActivitySpace.y,
        z = pointActivitySpace.z - keyboardOriginActivitySpace.z,
    )