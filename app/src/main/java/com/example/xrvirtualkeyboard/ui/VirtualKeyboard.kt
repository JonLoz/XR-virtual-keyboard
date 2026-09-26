package com.example.xrvirtualkeyboard.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import com.example.xrvirtualkeyboard.geometry.KeyBounds
import com.example.xrvirtualkeyboard.geometry.KeyDefinition
import com.example.xrvirtualkeyboard.geometry.Point2D
import com.example.xrvirtualkeyboard.geometry.computeRowBounds
import com.example.xrvirtualkeyboard.geometry.hitTestKey
import com.example.xrvirtualkeyboard.input.HandJointSource
import com.example.xrvirtualkeyboard.input.KeyDebouncer
import com.example.xrvirtualkeyboard.input.KeyEvent
import kotlinx.coroutines.flow.combine

private val testRowKeys = listOf(
    KeyDefinition("Q", "Q"),
    KeyDefinition("W", "W"),
    KeyDefinition("E", "E"),
    KeyDefinition("R", "R"),
    KeyDefinition("T", "T"),
)

private const val METERS_TO_DP = 1000f

// Expanded panel dimensions so the right hand is visible in the full interaction space around the keyboard.
private const val PANEL_WIDTH_DP = 1200f
private const val PANEL_HEIGHT_DP = 800f

@Composable
fun VirtualKeyboard() {
    val session = LocalSession.current ?: return
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled
    Log.d("VirtualKeyboard", "isSpatialUiEnabled: $isSpatialUiEnabled")

    val keyBounds = remember { computeRowBounds(testRowKeys) }
    val keyboardWidth = remember(keyBounds) {
        keyBounds.maxOf { it.center.x + it.width / 2f } - keyBounds.minOf { it.center.x - it.width / 2f }
    }
    val keyboardHeight = remember(keyBounds) { keyBounds.maxOf { it.height } }
    val selectionSource = remember(session) { HandJointSource(session) }
    val debouncer = remember { KeyDebouncer() }

    var hoveredKeyId by remember { mutableStateOf<String?>(null) }
    var pressedKeyIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cursorPosition by remember { mutableStateOf<Point2D?>(null) }
    var isEngaged by remember { mutableStateOf(false) }

    LaunchedEffect(selectionSource) {
        combine(selectionSource.selectionPoint, selectionSource.isEngaged) { point, engaged ->
            val pointInDp = point?.let { Point2D(it.x * METERS_TO_DP, it.y * METERS_TO_DP) }
            val hitKey = pointInDp?.let { hitTestKey(it, keyBounds) }
            Triple(pointInDp, hitKey, engaged)
        }.collect { (pointInDp, hitKey, engaged) ->
            cursorPosition = pointInDp
            isEngaged = engaged
            hoveredKeyId = hitKey
            debouncer.update(hitKey, engaged).forEach { event ->
                when (event) {
                    is KeyEvent.Pressed -> pressedKeyIds = pressedKeyIds + event.keyId
                    is KeyEvent.Released -> pressedKeyIds = pressedKeyIds - event.keyId
                    is KeyEvent.Repeated -> Unit // key's already highlighted; nothing to change yet
                }
            }
        }
    }

    Subspace {
        SpatialPanel(
            modifier = SubspaceModifier.width(PANEL_WIDTH_DP.dp).height(PANEL_HEIGHT_DP.dp)
        ) {
            KeyboardSurface(
                keyBounds = keyBounds,
                keyboardWidth = keyboardWidth,
                keyboardHeight = keyboardHeight,
                hoveredKeyId = hoveredKeyId,
                pressedKeyIds = pressedKeyIds,
                cursorPosition = cursorPosition,
                isEngaged = isEngaged,
                keys = testRowKeys,
            )
        }
    }
}

@Composable
private fun KeyboardSurface(
    keyBounds: List<KeyBounds>,
    keyboardWidth: Float,
    keyboardHeight: Float,
    hoveredKeyId: String?,
    pressedKeyIds: Set<String>,
    cursorPosition: Point2D?,
    isEngaged: Boolean,
    keys: List<KeyDefinition>,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Keyboard background housing / plate
        Box(
            modifier = Modifier
                .size((keyboardWidth + 32f).dp, (keyboardHeight + 32f).dp)
                .background(Color(0xEE1E1E1E), shape = RoundedCornerShape(20.dp))
        )

        keyBounds.forEach { bounds ->
            val key = keys.first { it.id == bounds.id }
            val isPressed = bounds.id in pressedKeyIds
            val isHovered = bounds.id == hoveredKeyId

            val targetColor = when {
                isPressed -> Color(0xFF4CAF50)  // green: confirmed press
                isHovered -> Color(0xFF616161)  // light gray: hovering, not yet pressed
                else -> Color(0xFF212121)       // dark gray: idle
            }
            val animatedColor by animateColorAsState(targetValue = targetColor, label = "key-${bounds.id}")

            Box(
                modifier = Modifier
                    .offset(x = bounds.center.x.dp, y = bounds.center.y.dp)
                    .size(bounds.width.dp, bounds.height.dp)
                    .background(animatedColor, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = key.label, color = Color.White, fontSize = 32.sp)
            }
        }

        if (cursorPosition != null) {
            VirtualRightHand(
                cursorPosition = cursorPosition,
                isEngaged = isEngaged,
            )
        }
    }
}