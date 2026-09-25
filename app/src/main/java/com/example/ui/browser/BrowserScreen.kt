package com.example.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.player.CyberPlayerScreen
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val isTabManagerVisible by viewModel.isTabManagerVisible.collectAsState()
    val isShieldDialogVisible by viewModel.isShieldDialogVisible.collectAsState()
    val isBookmarksVisible by viewModel.isBookmarksVisible.collectAsState()
    val isHistoryVisible by viewModel.isHistoryVisible.collectAsState()
    val isLocalPlayerVisible by viewModel.isLocalPlayerVisible.collectAsState()
    val selectedVideo by viewModel.selectedLocalVideo.collectAsState()
    val fullscreenState by viewModel.fullscreenState.collectAsState()
    val isBookmarked by viewModel.isCurrentBookmarked.collectAsState()
    val shieldEnabled by viewModel.shieldEnabled.collectAsState()

    val activeTab = viewModel.activeTab
    val focusManager = LocalFocusManager.current
    var isMenuOpen by remember { mutableStateOf(false) }

    // Intercept hardware/system back button
    BackHandler(enabled = !isTabManagerVisible && !isLocalPlayerVisible && fullscreenState.customView == null) {
        if (activeTab != null && activeTab.currentUrl != "cyber://home") {
            viewModel.goBack()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(CyberBg)) {
        Scaffold(
            topBar = {
                CyberTopBar(
                    urlInput = urlInput,
                    isLoading = activeTab?.isLoading == true,
                    loadingProgress = activeTab?.loadingProgress ?: 0,
                    tabCount = tabs.size,
                    isBookmarked = isBookmarked,
                    shieldBlockedCount = activeTab?.blockedCount ?: 0,
                    shieldEnabled = shieldEnabled,
                    onUrlChange = { viewModel.updateUrlInput(it) },
                    onGo = {
                        focusManager.clearFocus()
                        viewModel.loadUrl(urlInput)
                    },
                    onReloadStop = {
                        if (activeTab?.isLoading == true) {
                            viewModel.stopLoading()
                        } else {
                            viewModel.reload()
                        }
                    },
                    onToggleBookmark = { viewModel.toggleBookmark() },
                    onOpenShield = { viewModel.setShieldDialogVisible(true) },
                    onOpenTabs = { viewModel.setTabManagerVisible(true) },
                    onOpenPlayer = { viewModel.openLocalPlayer() },
                    onOpenMenu = { isMenuOpen = true }
                )
            },
            bottomBar = {
                CyberBottomBar(
                    canGoBack = activeTab?.canGoBack == true || activeTab?.currentUrl != "cyber://home",
                    canGoForward = activeTab?.canGoForward == true,
                    tabCount = tabs.size,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onHome = { viewModel.loadUrl("cyber://home") },
                    onTabs = { viewModel.setTabManagerVisible(true) },
                    onPlayer = { viewModel.openLocalPlayer() }
                )
            },
            containerColor = CyberBg
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (activeTab != null) {
                    if (activeTab.currentUrl == "cyber://home") {
                        SpeedDialHomeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Display the active tab's WebView
                        TabWebView(
                            tab = activeTab,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Overflow Menu Dropdown
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, end = 12.dp)
        ) {
            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
                modifier = Modifier
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = if (activeTab?.isDesktopMode == true) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (activeTab?.isDesktopMode == true) "Desktop Mode: ON" else "Request Desktop Site",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    onClick = {
                        viewModel.toggleDesktopMode()
                        isMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = NeonYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Bookmarks",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    onClick = {
                        viewModel.setBookmarksVisible(true)
                        isMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Browsing History",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    onClick = {
                        viewModel.setHistoryVisible(true)
                        isMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ExoPlayer Media Player",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    onClick = {
                        viewModel.openLocalPlayer()
                        isMenuOpen = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextDim,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Close Active Tab",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    onClick = {
                        if (activeTab != null) viewModel.closeTab(activeTab.id)
                        isMenuOpen = false
                    }
                )
            }
        }

        // Dialogs & Sheets
        if (isTabManagerVisible) {
            TabManagerSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.setTabManagerVisible(false) }
            )
        }

        if (isShieldDialogVisible) {
            ShieldDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setShieldDialogVisible(false) }
            )
        }

        if (isBookmarksVisible) {
            BookmarksDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setBookmarksVisible(false) }
            )
        }

        if (isHistoryVisible) {
            HistoryDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setHistoryVisible(false) }
            )
        }

        // Local Dedicated Media3 ExoPlayer Fullscreen View
        AnimatedVisibility(
            visible = isLocalPlayerVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CyberPlayerScreen(
                initialVideo = selectedVideo,
                onClose = { viewModel.closeLocalPlayer() }
            )
        }

        // Web In-Page Fullscreen Overlay (YouTube / HTML5 Video via WebChromeClient)
        fullscreenState.customView?.let { customView ->
            FullscreenVideoOverlay(
                customView = customView,
                onExitFullscreen = { viewModel.exitFullscreenCustomView() }
            )
        }
    }
}

@Composable
fun CyberTopBar(
    urlInput: String,
    isLoading: Boolean,
    loadingProgress: Int,
    tabCount: Int,
    isBookmarked: Boolean,
    shieldBlockedCount: Int,
    shieldEnabled: Boolean,
    onUrlChange: (String) -> Unit,
    onGo: () -> Unit,
    onReloadStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenShield: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Surface(
        color = CyberSurface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Main Top Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shield Badge Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (shieldEnabled) NeonCyan.copy(alpha = 0.15f) else CyberSurfaceElevated)
                        .border(1.dp, if (shieldEnabled) NeonCyan.copy(alpha = 0.5f) else CyberBorder, RoundedCornerShape(6.dp))
                        .clickable { onOpenShield() }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("top_bar_shield_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield",
                            tint = if (shieldEnabled) NeonCyan else TextDim,
                            modifier = Modifier.size(16.dp)
                        )
                        if (shieldBlockedCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$shieldBlockedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Address Bar
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    placeholder = {
                        Text(
                            text = "Search or type URL",
                            color = TextDim,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        val isHttps = urlInput.startsWith("https://")
                        Icon(
                            imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isHttps) NeonGreen else TextDim,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = { onUrlChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextDim,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onReloadStop,
                                modifier = Modifier.size(28.dp).testTag("reload_stop_button")
                            ) {
                                Icon(
                                    imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                    contentDescription = if (isLoading) "Stop" else "Reload",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { onGo() }),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurfaceElevated,
                        unfocusedContainerColor = CyberCardBg,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("address_bar_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Bookmark Icon
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(34.dp).testTag("bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) NeonYellow else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tab Switcher Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurfaceElevated)
                        .border(1.dp, NeonViolet, RoundedCornerShape(6.dp))
                        .clickable { onOpenTabs() }
                        .testTag("tab_count_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonViolet
                    )
                }

                // Dedicated Player Quick Button
                IconButton(
                    onClick = onOpenPlayer,
                    modifier = Modifier.size(34.dp).testTag("top_bar_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Media Player",
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Overflow Menu
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier.size(30.dp).testTag("top_bar_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // High-Visibility Progress Bar
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = NeonCyan,
                    trackColor = CyberSurfaceElevated
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CyberBorder)
                )
            }
        }
    }
}

@Composable
fun CyberBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabs: () -> Unit,
    onPlayer: () -> Unit
) {
    Surface(
        color = CyberSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CyberBorder)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back
                IconButton(
                    onClick = onBack,
                    enabled = canGoBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (canGoBack) NeonCyan else TextDim
                    )
                }

                // Forward
                IconButton(
                    onClick = onForward,
                    enabled = canGoForward,
                    modifier = Modifier.testTag("nav_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (canGoForward) NeonCyan else TextDim
                    )
                }

                // Home
                IconButton(
                    onClick = onHome,
                    modifier = Modifier.testTag("nav_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = NeonCyan
                    )
                }

                // Tabs
                IconButton(
                    onClick = onTabs,
                    modifier = Modifier.testTag("nav_tabs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "Tabs ($tabCount)",
                        tint = NeonViolet
                    )
                }

                // Dedicated ExoPlayer
                IconButton(
                    onClick = onPlayer,
                    modifier = Modifier.testTag("nav_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "CyberPlayer",
                        tint = NeonMagenta
                    )
                }
            }
        }
    }
}
