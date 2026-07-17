package com.devices.xrvirtualkeyboard.geometry

data class Point2D(val x: Float, val y: Float)

data class KeyDefinition(
    val id: String,
    val label: String
)

data class KeyBounds(
    val id: String,
    val center: Point2D,
    val width: Float,
    val height: Float
)

private const val KEY_SIZE = 120f
private const val KEY_SPACING = 20f

/** Lays keys out left-to-right with even spacing. Pure function: same input, same output, always. */
fun computeRowBounds(keys: List<KeyDefinition>): List<KeyBounds> {
    val step = KEY_SIZE + KEY_SPACING
    return keys.mapIndexed { index, key ->
        KeyBounds(
            id = key.id,
            center = Point2D(x = index * step, y = 0f),
            width = KEY_SIZE,
            height = KEY_SIZE
        )
    }
}

/** The actual hit-test: is this point inside any key's bounds? Returns the key id, or null. */
fun hitTestKey(point: Point2D, bounds: List<KeyBounds>): String? {
    return bounds.firstOrNull { key ->
        val halfW = key.width / 2f
        val halfH = key.height / 2f
        point.x in (key.center.x - halfW)..(key.center.x + halfW) &&
            point.y in (key.center.y - halfH)..(key.center.y + halfH)
    }?.id
}