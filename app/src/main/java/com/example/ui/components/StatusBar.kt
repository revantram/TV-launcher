package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassAccentAmber
import com.example.ui.theme.GlassAccentCyan
import com.example.ui.theme.GlassAccentGreen
import com.example.ui.theme.GlassAccentPurple
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.NetworkStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopStatusBar(
    networkStatus: NetworkStatus,
    onRamBoostClick: () -> Unit,
    onFireTvSettingsClick: () -> Unit,
    onStockLauncherClick: () -> Unit,
    onLauncherSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now).uppercase()
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x28FFFFFF),
                        Color(0x181E293B),
                        Color(0x200F172A)
                    )
                )
            )
            .border(1.dp, GlassCardBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: iOS-Style Minimal Clock & Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Glass clock capsule
                Column {
                    Text(
                        text = if (currentTimeString.isNotEmpty()) currentTimeString else "--:--",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (currentDateString.isNotEmpty()) currentDateString else "Loading...",
                        color = GlassAccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Wi-Fi status, RAM Boost, Fire TV Settings, Stock Launcher fallback, App Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wi-Fi Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x20FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val isConnected = networkStatus is NetworkStatus.Connected
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Wi-Fi Status",
                        tint = if (isConnected) GlassAccentGreen else GlassAccentAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) GlassAccentGreen else GlassAccentAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "Online" else "Offline",
                        color = if (isConnected) TextPrimary else GlassAccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 1-Click RAM Booster Button
                StatusActionButton(
                    icon = Icons.Default.Memory,
                    label = "Boost RAM",
                    accentColor = GlassAccentCyan,
                    testTag = "ram_boost_button",
                    onClick = onRamBoostClick
                )

                // Fire TV Settings Button
                StatusActionButton(
                    icon = Icons.Default.Settings,
                    label = "Fire TV",
                    accentColor = GlassAccentPurple,
                    testTag = "firetv_settings_button",
                    onClick = onFireTvSettingsClick
                )

                // Stock Home Button
                StatusActionButton(
                    icon = Icons.Default.Home,
                    label = "Stock Home",
                    accentColor = GlassAccentAmber,
                    testTag = "stock_launcher_button",
                    onClick = onStockLauncherClick
                )

                // Launcher Preferences
                StatusActionButton(
                    icon = Icons.Default.Tune,
                    label = "Prefs",
                    accentColor = TextPrimary,
                    testTag = "launcher_settings_button",
                    onClick = onLauncherSettingsClick
                )
            }
        }
    }
}

@Composable
fun StatusActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(16.dp)
    val bgBrush = if (isFocused) {
        Brush.verticalGradient(listOf(Color(0x60FFFFFF), Color(0x3538BDF8)))
    } else {
        Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x101E293B)))
    }
    val borderStroke = if (isFocused) 2.dp else 1.dp
    val borderColor = if (isFocused) Color.White else GlassCardBorder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1.0f)
            .clip(shape)
            .background(bgBrush)
            .border(borderStroke, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) Color.White else accentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isFocused) Color.White else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
