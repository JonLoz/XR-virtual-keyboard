package com.example.xrvirtualkeyboard

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.runtime.Config
import androidx.xr.runtime.HandTrackingMode
import androidx.xr.scenecore.scene
import com.example.xrvirtualkeyboard.ui.VirtualKeyboard
import com.example.xrvirtualkeyboard.ui.theme.XRVirtualKeyboardTheme

class MainActivity : ComponentActivity() {
    private val handTrackingPermissionGranted = mutableStateOf(false)
    private lateinit var handTrackingPermissionLauncher: ActivityResultLauncher<String>
    private companion object {
        const val HAND_TRACKING_PERMISSION = "android.permission.HAND_TRACKING"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handTrackingPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            handTrackingPermissionGranted.value = isGranted
        }
        val alreadyGranted = ContextCompat.checkSelfPermission(this, HAND_TRACKING_PERMISSION) == PackageManager.PERMISSION_GRANTED
        handTrackingPermissionGranted.value = alreadyGranted
        if (!alreadyGranted) {
            handTrackingPermissionLauncher.launch(HAND_TRACKING_PERMISSION)
        }

        setContent {
            XRVirtualKeyboardTheme {
                val session = LocalSession.current

                LaunchedEffect(session, handTrackingPermissionGranted.value) {
                    if (session != null && handTrackingPermissionGranted.value) {
                        try {
                            session.configure(session.config.copy(handTracking = HandTrackingMode.BOTH))
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to configure hand tracking", e)
                        }
                    }
                }

                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    VirtualKeyboard()
                } else {
                    My2DContent(onRequestFullSpaceMode = { session?.scene?.requestFullSpace() })
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun My2DContent(onRequestFullSpaceMode: () -> Unit) {
    Surface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MainContent(modifier = Modifier.padding(48.dp))
            // Preview does not current support XR sessions.
            if (!LocalInspectionMode.current && LocalSession.current != null) {
                FullSpaceModeIconButton(
                    onClick = onRequestFullSpaceMode,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.hello_android_xr), modifier = modifier)
}

@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Composable
fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@PreviewLightDark
@Composable
fun My2dContentPreview() {
    XRVirtualKeyboardTheme {
        My2DContent(onRequestFullSpaceMode = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FullSpaceModeButtonPreview() {
    XRVirtualKeyboardTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
fun HomeSpaceModeButtonPreview() {
    XRVirtualKeyboardTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}