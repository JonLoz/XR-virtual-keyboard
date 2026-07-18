package com.example.xrvirtualkeyboard.input

sealed class KeyEvent {
    data class Pressed(val keyId: String) : KeyEvent()
    data class Repeated(val keyId: String) : KeyEvent()
    data class Released(val keyId: String) : KeyEvent()
}

class KeyDebouncer(
    private val initialRepeatDelayMs: Long = 400L,
    private val repeatIntervalMs: Long = 100L,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    private var currentKeyId: String? = null
    private var pressStartMs: Long = 0L
    private var lastRepeatMs: Long = 0L

    /** Call on every (hoveredKeyId, isEngaged) update. Returns events to emit — usually 0 or 1, occasionally 2. */
    fun update(hoveredKeyId: String?, isEngaged: Boolean): List<KeyEvent> {
        val now = nowMs()
        val events = mutableListOf<KeyEvent>()

        if (!isEngaged || hoveredKeyId == null) {
            currentKeyId?.let { events += KeyEvent.Released(it) }
            currentKeyId = null
            return events
        }

        when {
            currentKeyId == null -> {
                currentKeyId = hoveredKeyId
                pressStartMs = now
                lastRepeatMs = now
                events += KeyEvent.Pressed(hoveredKeyId)
            }
            currentKeyId != hoveredKeyId -> {
                events += KeyEvent.Released(currentKeyId!!)
                currentKeyId = hoveredKeyId
                pressStartMs = now
                lastRepeatMs = now
                events += KeyEvent.Pressed(hoveredKeyId)
            }
            now - pressStartMs >= initialRepeatDelayMs && now - lastRepeatMs >= repeatIntervalMs -> {
                events += KeyEvent.Repeated(hoveredKeyId)
                lastRepeatMs = now
            }
        }
        return events
    }
}