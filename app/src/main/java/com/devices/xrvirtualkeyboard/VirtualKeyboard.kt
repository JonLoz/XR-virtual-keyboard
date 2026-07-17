package com.devices.xrvirtualkeyboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SpatialArrangement
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width

data class KeyDefinition(
    val id: String,
    val label: String,
)

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

    Subspace {
        SpatialRow(
            modifier = SubspaceModifier.width(900.dp).height(140.dp),
            horizontalArrangement = SpatialArrangement.SpaceEvenly,
        ) {
            testRowKeys.forEach { key ->
                SpatialPanel(
                    modifier = SubspaceModifier.width(120.dp).height(120.dp)
                ) {
                    KeyContent(label = key.label)
                }
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