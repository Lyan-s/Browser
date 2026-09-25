package com.example.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShieldDialog(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val isShieldEnabled by viewModel.shieldEnabled.collectAsState()
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val recentBlocked by viewModel.recentBlocked.collectAsState()
    val activeTab = viewModel.activeTab

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp))
                .testTag("shield_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isShieldEnabled) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceElevated)
                                .border(1.dp, if (isShieldEnabled) NeonCyan else CyberBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isShieldEnabled) NeonCyan else TextDim,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CYBER SHIELD",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Text(
                                text = "AD & TRACKER BLOCKER",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_shield_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isShieldEnabled) "SHIELD ACTIVE" else "SHIELD PAUSED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isShieldEnabled) NeonCyan else TextDim
                        )
                        Text(
                            text = if (isShieldEnabled) "Intercepting ads, analytics & telemetry" else "Web tracking is unblocked",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isShieldEnabled,
                        onCheckedChange = { viewModel.toggleShield() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBg,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextDim,
                            uncheckedTrackColor = CyberBorder
                        ),
                        modifier = Modifier.testTag("shield_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$totalBlocked",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                            Text(
                                text = "TOTAL BLOCKED",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextDim
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${activeTab?.blockedCount ?: 0}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonViolet
                            )
                            Text(
                                text = "CURRENT TAB",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "RECENT INTERCEPTED REQUESTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (recentBlocked.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No blocked requests in session yet",
                            fontSize = 11.sp,
                            color = TextDim,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentBlocked.take(15), key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val badgeColor = when (item.category) {
                                        "AD" -> NeonRed
                                        "TRACKER" -> NeonViolet
                                        else -> NeonMagenta
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeColor.copy(alpha = 0.2f))
                                            .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = badgeColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.host,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
