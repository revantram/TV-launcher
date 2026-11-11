package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.GlassAccentAmber
import com.example.ui.theme.GlassAccentCyan
import com.example.ui.theme.GlassAccentPurple
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onFocused: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
        if (isFocused) {
            onFocused?.invoke()
        }
    }

    // 120ms ultra-smooth hardware-accelerated D-Pad focus animation (Scale 1.05x)
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "app_card_scale"
    )

    val shape = RoundedCornerShape(22.dp)

    // Pre-rendered GPU-friendly glass gradient
    val backgroundBrush = if (isFocused) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x5538BDF8),
                Color(0x351E293B),
                Color(0x600F172A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x18FFFFFF),
                Color(0x121A2333),
                Color(0x220B0F19)
            )
        )
    }

    // Pure white iridescent border on focus, subtle translucent glass border normally
    val currentBorder = if (isFocused) {
        BorderStroke(2.dp, Brush.linearGradient(listOf(Color.White, GlassAccentCyan, Color.White)))
    } else {
        BorderStroke(1.dp, GlassCardBorder)
    }

    val cardWidth = if (isCompact) 130.dp else 145.dp
    val cardHeight = if (isCompact) 140.dp else 160.dp

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .scale(animatedScale)
            .clip(shape)
            .background(backgroundBrush)
            .border(currentBorder, shape)
            .onKeyEvent { keyEvent ->
                // Fire TV remote menu button has keyCode 82 (KEYCODE_MENU)
                if (keyEvent.nativeKeyEvent.keyCode == 82 || keyEvent.key == Key.Menu) {
                    onLongClick()
                    true
                } else false
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .focusable(interactionSource = interactionSource)
            .testTag("app_card_${app.packageName}")
            .padding(10.dp)
    ) {
        // Badges: Favorite star, Sideloaded / TV tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.isFavorite) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0x66FBBF24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = GlassAccentAmber,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }

            if (app.isSideloaded) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x40A78BFA))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "APK",
                        color = GlassAccentPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Center: App Icon and Label
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Downsampled 96x96 icon inside liquid glass container
            Box(
                modifier = Modifier
                    .size(if (isCompact) 56.dp else 66.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x20FFFFFF))
                    .border(
                        1.dp,
                        if (isFocused) Color(0x80FFFFFF) else Color(0x20FFFFFF),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconBitmap != null && !app.iconBitmap.isRecycled) {
                    Image(
                        bitmap = app.iconBitmap.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (app.isSideloaded) Icons.Default.Android else Icons.Default.Tv,
                        contentDescription = app.label,
                        tint = if (isFocused) GlassAccentCyan else TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Label
            Text(
                text = app.label,
                color = if (isFocused) Color.White else TextPrimary,
                fontSize = if (isCompact) 12.sp else 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
