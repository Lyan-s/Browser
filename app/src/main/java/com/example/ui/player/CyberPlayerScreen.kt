package com.example.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.model.VideoItem
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun CyberPlayerScreen(
    initialVideo: VideoItem? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var currentVideo by remember {
        mutableStateOf(
            initialVideo ?: VideoItem(
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                title = "Big Buck Bunny (Demo Stream)",
                isSample = true
            )
        )
    }

    val sampleVideos = remember {
        listOf(
            VideoItem(
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                title = "Big Buck Bunny Demo",
                isSample = true
            ),
            VideoItem(
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                title = "Tears of Steel (Sci-Fi)",
                isSample = true
            ),
            VideoItem(
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                title = "Elephants Dream (CGI)",
                isSample = true
            )
        )
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }
    var isFullscreen by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var isSpeedMenuOpen by remember { mutableStateOf(false) }

    // Video Picker Launcher using Android Photo Picker for zero-permission video selection
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentVideo = VideoItem(
                uri = uri,
                title = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Storage Video",
                isSample = false
            )
        }
    }

    // Load video when currentVideo changes
    LaunchedEffect(currentVideo.uri) {
        val mediaItem = MediaItem.fromUri(currentVideo.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Position update loop
    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(400)
        }
    }

    // Auto-hide controls
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    // Handle orientation & system bars
    fun toggleOrientationFullscreen() {
        isFullscreen = !isFullscreen
        val window = activity?.window
        if (activity != null && window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler {
        if (isFullscreen) {
            toggleOrientationFullscreen()
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .testTag("cyber_player_screen")
    ) {
        // ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // Custom Cyberpunk controller overlay
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
        )

        // Custom Cyberpunk Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (isFullscreen) {
                                    toggleOrientationFullscreen()
                                } else {
                                    onClose()
                                }
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentVideo.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentVideo.isSample) "CYBER STREAM // DEMO" else "LOCAL DEVICE STORAGE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonViolet
                            )
                        }
                    }

                    // Pick File Button
                    FilledIconButton(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CyberSurfaceElevated,
                            contentColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("pick_local_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Pick Video from Device"
                        )
                    }
                }

                // Center Controls (Play/Pause, Rewind, Fast Forward)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceElevated.copy(alpha = 0.7f))
                            .border(1.dp, CyberBorder, CircleShape)
                            .testTag("player_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10s",
                            tint = TextPrimary
                        )
                    }

                    // Large Play / Pause Button with Neon Glow
                    IconButton(
                        onClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = CyberBg,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            val max = exoPlayer.duration.coerceAtLeast(0L)
                            val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(max)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceElevated.copy(alpha = 0.7f))
                            .border(1.dp, CyberBorder, CircleShape)
                            .testTag("player_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10s",
                            tint = TextPrimary
                        )
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    // Quick Sample Switcher (when not in fullscreen)
                    if (!isFullscreen) {
                        Text(
                            text = "DEMO CHANNELS:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextDim,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(sampleVideos) { sample ->
                                val isSelected = currentVideo.uri == sample.uri
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) NeonViolet.copy(alpha = 0.25f) else CyberSurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonViolet else CyberBorder,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { currentVideo = sample }
                                ) {
                                    Text(
                                        text = sample.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) NeonCyan else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Seekbar Slider
                    val effectivePosition = if (isSeeking) {
                        (seekProgress * duration).toLong()
                    } else {
                        currentPosition
                    }

                    Slider(
                        value = if (duration > 0) {
                            if (isSeeking) seekProgress else (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                        } else 0f,
                        onValueChange = { progress ->
                            isSeeking = true
                            seekProgress = progress
                        },
                        onValueChangeFinished = {
                            val target = (seekProgress * duration).toLong()
                            exoPlayer.seekTo(target)
                            currentPosition = target
                            isSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CyberBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek_slider")
                    )

                    // Bottom Bar Tools: Time, Volume, Speed, Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Time Display
                        Text(
                            text = "${formatDuration(effectivePosition)} / ${formatDuration(duration)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Volume / Mute
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else volume
                                },
                                modifier = Modifier.size(36.dp).testTag("player_volume_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isMuted || volume == 0f) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = if (isMuted) NeonMagenta else TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Speed Dropdown
                            Box {
                                IconButton(
                                    onClick = { isSpeedMenuOpen = true },
                                    modifier = Modifier.size(36.dp).testTag("player_speed_button")
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonViolet
                                    )
                                }
                                DropdownMenu(
                                    expanded = isSpeedMenuOpen,
                                    onDismissRequest = { isSpeedMenuOpen = false },
                                    modifier = Modifier.background(CyberCardBg)
                                ) {
                                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${speed}x",
                                                    color = if (playbackSpeed == speed) NeonCyan else TextPrimary,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            },
                                            onClick = {
                                                playbackSpeed = speed
                                                exoPlayer.setPlaybackSpeed(speed)
                                                isSpeedMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = { toggleOrientationFullscreen() },
                                modifier = Modifier.size(36.dp).testTag("player_fullscreen_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
