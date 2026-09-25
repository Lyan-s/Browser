package com.example.ui.browser

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.CyberBg
import com.example.ui.theme.NeonCyan

@Composable
fun FullscreenVideoOverlay(
    customView: View,
    onExitFullscreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        onExitFullscreen()
    }

    DisposableEffect(Unit) {
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            if (activity != null) {
                activity.requestedOrientation = originalOrientation
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                // Ensure customView has proper layout params
                (customView.parent as? ViewGroup)?.removeView(customView)
                customView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                customView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Exit Fullscreen Button
        FilledIconButton(
            onClick = onExitFullscreen,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = CyberBg.copy(alpha = 0.75f),
                contentColor = NeonCyan
            ),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "Exit Fullscreen"
            )
        }
    }
}
