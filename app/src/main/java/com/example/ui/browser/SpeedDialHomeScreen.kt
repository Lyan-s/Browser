package com.example.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickLink(
    val title: String,
    val subtitle: String,
    val url: String,
    val icon: ImageVector,
    val glowColor: Color
)

@Composable
fun SpeedDialHomeScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val shieldEnabled by viewModel.shieldEnabled.collectAsState()

    val quickLinks = remember {
        listOf(
            QuickLink("DuckDuckGo", "Anonymous Search", "https://duckduckgo.com", Icons.Default.Search, NeonCyan),
            QuickLink("Wikipedia", "Neural Archive", "https://en.wikipedia.org", Icons.Default.Public, NeonViolet),
            QuickLink("YouTube", "Holo Stream", "https://m.youtube.com", Icons.Default.SmartDisplay, NeonMagenta),
            QuickLink("GitHub", "Code Matrix", "https://github.com", Icons.Default.Code, NeonGreen),
            QuickLink("Reddit", "Cyber Forum", "https://reddit.com", Icons.Default.Language, NeonYellow),
            QuickLink("TechNews", "Net Intel", "https://news.ycombinator.com", Icons.Default.TravelExplore, NeonCyan)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Header
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cyber Emblem
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.2f), NeonViolet.copy(alpha = 0.2f))
                            )
                        )
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "CyberNet Core",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CYBERNET // BROWSER",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "HIGH-PERFORMANCE SECURE NEURAL WEB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Inline Search Bar
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text(
                            text = "Search or Enter Web Address...",
                            color = TextDim,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan
                        )
                    },
                    trailingIcon = {
                        if (searchInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.loadUrl(searchInput)
                                },
                                modifier = Modifier.testTag("home_search_submit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Go",
                                    tint = NeonCyan
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurfaceElevated,
                        unfocusedContainerColor = CyberCardBg,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        // Cyber Shield Status Hero Card
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (shieldEnabled) NeonCyan.copy(alpha = 0.5f) else TextDim,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setShieldDialogVisible(true) }
                    .testTag("shield_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (shieldEnabled) NeonCyan.copy(alpha = 0.15f) else CyberSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Shield",
                                tint = if (shieldEnabled) NeonCyan else TextDim,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (shieldEnabled) "AD & TRACKER SHIELD ACTIVE" else "SHIELD OFFLINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (shieldEnabled) NeonCyan else TextDim
                            )
                            Text(
                                text = "$totalBlocked ads & telemetry trackers blocked",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (shieldEnabled) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceElevated)
                            .border(1.dp, if (shieldEnabled) NeonCyan else CyberBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (shieldEnabled) "PROTECTED" else "PAUSED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (shieldEnabled) NeonCyan else TextDim
                        )
                    }
                }
            }
        }

        // Dedicated Local Player Hero Card
        item(span = { GridItemSpan(2) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonViolet.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.openLocalPlayer() }
                    .testTag("open_local_player_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Local Video Player",
                                tint = NeonViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CYBERPLAYER // MEDIA3 EXOPLAYER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonViolet
                            )
                            Text(
                                text = "Play local files, downloads & storage videos",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Open Player",
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Speed Dial Section Title
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "SPEED DIAL // DIRECT ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Quick Links Grid
        items(quickLinks) { link ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                    .clickable { viewModel.loadUrl(link.url) }
                    .testTag("speed_dial_${link.title.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(link.glowColor.copy(alpha = 0.15f))
                            .border(1.dp, link.glowColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = link.icon,
                            contentDescription = link.title,
                            tint = link.glowColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = link.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = link.subtitle,
                            fontSize = 10.sp,
                            color = TextDim,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
