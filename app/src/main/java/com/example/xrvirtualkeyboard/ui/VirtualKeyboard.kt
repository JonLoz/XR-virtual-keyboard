package com.example.xrvirtualkeyboard.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.xrvirtualkeyboard.input.KeyDebouncer
import com.example.xrvirtualkeyboard.input.KeyEvent
import com.example.xrvirtualkeyboard.input.MouseClickSource
import kotlinx.coroutines.flow.combine

private val testRowKeys = listOf(
    KeyDefinition("Q", "Q"),
    KeyDefinition("W", "W"),
    KeyDefinition("E", "E"),
    KeyDefinition("R", "R"),
    KeyDefinition("T", "T"),
)

@Composable
fun VirtualKeyboard() {
    val isSpatialUiEnabled = LocalSpatialCapabilities.current.isSpatialUiEnabled
    Log.d("VirtualKeyboard", "isSpatialUiEnabled: $isSpatialUiEnabled")

    val keyBounds = remember { computeRowBounds(testRowKeys) }
    val surfaceWidth = remember(keyBounds) {
        keyBounds.maxOf { it.center.x + it.width / 2f } - keyBounds.minOf { it.center.x - it.width / 2f }
    }
    val surfaceHeight = remember(keyBounds) { keyBounds.maxOf { it.height } }
    val selectionSource = remember { MouseClickSource(surfaceWidth, surfaceHeight) }
    val debouncer = remember { KeyDebouncer() }

    var hoveredKeyId by remember { mutableStateOf<String?>(null) }
    var pressedKeyIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(selectionSource) {
        combine(selectionSource.selectionPoint, selectionSource.isEngaged) { point, engaged ->
            val hitKey = point?.let { hitTestKey(Point2D(it.x, it.y), keyBounds) }
            hitKey to engaged
        }.collect { (hitKey, engaged) ->
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
            modifier = SubspaceModifier.width(surfaceWidth.dp).height(surfaceHeight.dp)
        ) {
            KeyboardSurface(
                keyBounds = keyBounds,
                source = selectionSource,
                hoveredKeyId = hoveredKeyId,
                pressedKeyIds = pressedKeyIds,
                keys = testRowKeys,
            )
        }
    }
}

@Composable
private fun KeyboardSurface(
    keyBounds: List<KeyBounds>,
    source: MouseClickSource,
    hoveredKeyId: String?,
    pressedKeyIds: Set<String>,
    keys: List<KeyDefinition>,
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Exit) {
                            source.clearPointer()
                            continue
                        }
                        val change = event.changes.firstOrNull() ?: continue
                        source.updatePointerPosition(change.position, density)
                        source.updateEngaged(change.pressed)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
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
                    .background(animatedColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = key.label, color = Color.White, fontSize = 32.sp)
            }
        }
    }
}