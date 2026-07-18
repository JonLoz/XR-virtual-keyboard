package com.example.xrvirtualkeyboard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import com.example.xrvirtualkeyboard.geometry.KeyBounds
import com.example.xrvirtualkeyboard.geometry.KeyDefinition
import com.example.xrvirtualkeyboard.geometry.Point2D
import com.example.xrvirtualkeyboard.geometry.computeRowBounds
import com.example.xrvirtualkeyboard.geometry.hitTestKey
import com.example.xrvirtualkeyboard.input.MouseClickSource

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

    LaunchedEffect(selectionSource) {
        selectionSource.selectionPoint.collect { point ->
            val hitKey = point?.let { hitTestKey(Point2D(it.x, it.y), keyBounds) }
            Log.d("VirtualKeyboard", "point=$point, hitKey=$hitKey")
        }
    }

    Subspace {
        SpatialPanel(
            modifier = SubspaceModifier.width(surfaceWidth.dp).height(surfaceHeight.dp)
        ) {
            KeyboardSurface(keyBounds = keyBounds, source = selectionSource)
        }
    }
}

@Composable
fun KeyboardSurface(keyBounds: List<KeyBounds>, source: MouseClickSource) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        source.updatePointerPosition(change.position, density)
                        source.updateEngaged(change.pressed)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        keyBounds.forEach { bounds ->
            val key = testRowKeys.first { it.id == bounds.id }
            Box(
                modifier = Modifier
                    .offset(x = bounds.center.x.dp, y = bounds.center.y.dp)
                    .size(bounds.width.dp, bounds.height.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = key.label, color = Color.White, fontSize = 32.sp)
            }
        }
    }
}

@Composable
private fun KeyContent(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 32.sp)
    }
}