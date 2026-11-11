package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.ui.theme.GlassAccentCyan
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppsGridSection(
    apps: List<AppItem>,
    selectedCategory: AppCategory,
    searchQuery: String,
    isLoading: Boolean,
    onCategorySelect: (AppCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    selectedAlphabet: String? = null,
    onAlphabetSelect: ((String?) -> Unit)? = null,
    scrollToTopTrigger: Long = 0L,
    onAppFocused: ((AppItem) -> Unit)? = null
) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    androidx.compose.runtime.LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0L) {
            gridState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Category Filter Bar & Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryTabChip(
                    title = "All Apps",
                    isSelected = selectedCategory == AppCategory.ALL,
                    count = null,
                    onClick = { onCategorySelect(AppCategory.ALL) }
                )
                CategoryTabChip(
                    title = "Amazon & TV",
                    isSelected = selectedCategory == AppCategory.AMAZON_TV,
                    count = null,
                    onClick = { onCategorySelect(AppCategory.AMAZON_TV) }
                )
                CategoryTabChip(
                    title = "Sideloaded",
                    isSelected = selectedCategory == AppCategory.SIDELOADED,
                    count = null,
                    onClick = { onCategorySelect(AppCategory.SIDELOADED) }
                )
            }

            // Compact Search Bar
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search apps...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassAccentCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_apps_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clickable { onSearchChange("") }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = GlassAccentCyan,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No apps found matching \"$searchQuery\"" else "No apps available in this section",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // D-Pad Remote Accessible Alphabet Quick-Jump Bar
            if (apps.size > 8) {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        AlphabetChip(
                            letter = "ALL",
                            isSelected = selectedAlphabet == null,
                            onClick = { onAlphabetSelect?.invoke(null) }
                        )
                    }
                    val alphabet = ('A'..'Z').map { it.toString() }
                    items(alphabet) { letter ->
                        val hasApps = apps.any { it.label.uppercase().startsWith(letter) }
                        if (hasApps) {
                            AlphabetChip(
                                letter = letter,
                                isSelected = selectedAlphabet == letter,
                                onClick = { onAlphabetSelect?.invoke(letter) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // High-Performance Recycled LazyVerticalGrid
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = apps,
                    key = { it.packageName }
                ) { app ->
                    AppCard(
                        app = app,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) },
                        isCompact = false,
                        onFocused = { onAppFocused?.invoke(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetChip(
    letter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(10.dp)
    val bgColor = when {
        isFocused -> Color(0x7038BDF8)
        isSelected -> Color(0x4038BDF8)
        else -> Color(0x18FFFFFF)
    }
    val borderColor = when {
        isFocused -> Color.White
        isSelected -> GlassAccentCyan
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .scale(if (isFocused) 1.15f else 1.0f)
            .clip(shape)
            .background(bgColor)
            .border(if (isFocused || isSelected) 1.5.dp else 0.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = if (isSelected || isFocused) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun CategoryTabChip(
    title: String,
    isSelected: Boolean,
    count: Int?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(14.dp)
    val bgColor = when {
        isFocused -> Color(0x6038BDF8)
        isSelected -> Color(0x3538BDF8)
        else -> Color(0x15FFFFFF)
    }
    val borderColor = when {
        isFocused -> Color.White
        isSelected -> GlassAccentCyan
        else -> GlassCardBorder
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .scale(if (isFocused) 1.05f else 1.0f)
            .clip(shape)
            .background(bgColor)
            .border(if (isFocused || isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected || isFocused) Color.White else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$count",
                color = if (isSelected || isFocused) Color.White.copy(alpha = 0.8f) else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
