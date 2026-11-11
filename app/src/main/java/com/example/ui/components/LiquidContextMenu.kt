package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.GlassAccentAmber
import com.example.ui.theme.GlassAccentCyan
import com.example.ui.theme.GlassAccentPink
import com.example.ui.theme.GlassAccentPurple
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.GlassSheetBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidContextMenu(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHideApp: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        val shape = RoundedCornerShape(26.dp)

        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE1E293B),
                            Color(0xF50F172A),
                            Color(0xFA070A11)
                        )
                    )
                )
                .border(1.5.dp, Brush.linearGradient(listOf(Color(0x80FFFFFF), GlassAccentCyan)), shape)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: App icon + Label + Package
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x25FFFFFF))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.iconBitmap != null && !app.iconBitmap.isRecycled) {
                            Image(
                                bitmap = app.iconBitmap.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = app.packageName,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Close Button
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

                Spacer(modifier = Modifier.height(20.dp))

                // Actions List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ContextMenuItem(
                        icon = Icons.Default.PlayArrow,
                        title = "Open App",
                        subtitle = "Launch application on Fire TV",
                        accentColor = GlassAccentCyan,
                        onClick = onLaunch
                    )

                    ContextMenuItem(
                        icon = if (app.isFavorite) Icons.Default.StarBorder else Icons.Default.Star,
                        title = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        subtitle = if (app.isFavorite) "Remove from top favorite row" else "Pin to top favorite row",
                        accentColor = GlassAccentAmber,
                        onClick = onToggleFavorite
                    )

                    ContextMenuItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hide App",
                        subtitle = "Hide from launcher grid (restore via Settings)",
                        accentColor = GlassAccentPurple,
                        onClick = onHideApp
                    )

                    ContextMenuItem(
                        icon = Icons.Default.Info,
                        title = "Fire OS App Info",
                        subtitle = "View permissions, clear cache & data",
                        accentColor = GlassAccentCyan,
                        onClick = onAppInfo
                    )

                    if (!app.isSystem) {
                        ContextMenuItem(
                            icon = Icons.Default.Delete,
                            title = "Uninstall",
                            subtitle = "Remove app completely from Fire TV",
                            accentColor = GlassAccentPink,
                            onClick = onUninstall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(16.dp)
    val bgBrush = if (isFocused) {
        Brush.horizontalGradient(listOf(Color(0x4538BDF8), Color(0x301E293B)))
    } else {
        Brush.horizontalGradient(listOf(Color(0x15FFFFFF), Color(0x0AFFFFFF)))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.03f else 1.0f)
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
                .background(accentColor.copy(alpha = if (isFocused) 0.35f else 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isFocused) Color.White else TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = if (isFocused) Color.White.copy(alpha = 0.8f) else TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
