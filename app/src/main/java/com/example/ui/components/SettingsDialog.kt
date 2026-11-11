package com.example.ui.components

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    isAccessibilityEnabled: Boolean,
    isScreensaverEnabled: Boolean,
    screensaverTimeoutMinutes: Int,
    hiddenAppsCount: Int,
    onDismiss: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleScreensaver: (Boolean) -> Unit,
    onSelectScreensaverTimeout: (Int) -> Unit,
    onUnhideAllApps: () -> Unit,
    onOpenFireTvSettings: () -> Unit,
    onOpenStockLauncher: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(26.dp)

        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFA1E293B),
                            Color(0xFA0F172A),
                            Color(0xFF090D16)
                        )
                    )
                )
                .border(1.5.dp, Brush.linearGradient(listOf(Color(0x80FFFFFF), GlassAccentPurple)), shape)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x25FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = GlassAccentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Launcher Preferences",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fire TV & Performance Configuration",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x20FFFFFF))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: Home Button Intercept / Accessibility Hook
                Text(
                    text = "FIRE OS INTEGRATION",
                    color = GlassAccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionCard(
                    icon = Icons.Default.AccessibilityNew,
                    title = "Home Button Hook (Accessibility)",
                    subtitle = if (isAccessibilityEnabled) "Active: Intercepts Amazon Home key directly" else "Click to enable in Fire TV Accessibility settings",
                    badgeText = if (isAccessibilityEnabled) "ACTIVE" else "SETUP NEEDED",
                    badgeColor = if (isAccessibilityEnabled) GlassAccentGreen else GlassAccentAmber,
                    onClick = onOpenAccessibilitySettings
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsActionCard(
                    icon = Icons.Default.Home,
                    title = "Stock Fire TV Launcher",
                    subtitle = "Instantly switch back to Amazon's default Home screen",
                    badgeText = "FALLBACK",
                    badgeColor = GlassAccentAmber,
                    onClick = onOpenStockLauncher
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsActionCard(
                    icon = Icons.Default.Settings,
                    title = "Fire TV System Settings",
                    subtitle = "Open Network, Display & Audio, Controllers, or My Fire TV",
                    badgeText = "SYSTEM",
                    badgeColor = GlassAccentPurple,
                    onClick = onOpenFireTvSettings
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Screen Burn-in Protection
                Text(
                    text = "OLED BURN-IN PROTECTION",
                    color = GlassAccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Inactivity Deep-Black Screensaver",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Protects TV screens against static image burn-in",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isScreensaverEnabled,
                        onCheckedChange = onToggleScreensaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GlassAccentCyan
                        )
                    )
                }

                if (isScreensaverEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            val isSelected = screensaverTimeoutMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0x4038BDF8) else Color(0x15FFFFFF))
                                    .border(
                                        1.dp,
                                        if (isSelected) GlassAccentCyan else GlassCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectScreensaverTimeout(mins) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$mins Min",
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: App Management
                Text(
                    text = "APP MANAGEMENT",
                    color = GlassAccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionCard(
                    icon = Icons.Default.Visibility,
                    title = "Restore Hidden Apps",
                    subtitle = if (hiddenAppsCount > 0) "$hiddenAppsCount app(s) currently hidden" else "No apps hidden",
                    badgeText = if (hiddenAppsCount > 0) "RESTORE" else "NONE",
                    badgeColor = GlassAccentCyan,
                    onClick = {
                        onUnhideAllApps()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Device Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x10FFFFFF))
                        .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Device: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Engine: Liquid Glass 60 FPS TV Runtime",
                            color = GlassAccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(16.dp)
    val bgBrush = if (isFocused) {
        Brush.horizontalGradient(listOf(Color(0x5038BDF8), Color(0x301E293B)))
    } else {
        Brush.horizontalGradient(listOf(Color(0x15FFFFFF), Color(0x0AFFFFFF)))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1.0f)
            .clip(shape)
            .background(bgBrush)
            .border(
                if (isFocused) 1.5.dp else 1.dp,
                if (isFocused) Color.White else GlassCardBorder,
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x20FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else GlassAccentCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isFocused) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = if (isFocused) Color.White.copy(alpha = 0.8f) else TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.2f))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
