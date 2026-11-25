package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AzureGlow
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldGlow

/**
 * Multi-layered Deep Obsidian & Midnight Navy background with soft Royal Gold and Deep Sky Blue
 * ambient mesh halos.
 */
@Composable
fun AmbientLuxuryBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianDeep, MidnightNavy, ObsidianDeep)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-right Royal Gold ambient halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RoyalGoldGlow, Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.88f, size.height * 0.12f)
            )
            // Mid-left Deep Sky Blue ambient halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AzureGlow, Color.Transparent),
                    center = Offset(size.width * 0.14f, size.height * 0.48f),
                    radius = size.width * 0.78f
                ),
                radius = size.width * 0.78f,
                center = Offset(size.width * 0.14f, size.height * 0.48f)
            )
            // Bottom-right subtle Gold + Blue blend
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(DeepSkyBlue.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * 0.75f, size.height * 0.88f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.75f, size.height * 0.88f)
            )
        }
        content()
    }
}

/**
 * Glassmorphism Card with translucent Midnight Navy fill, 1.dp luminous Royal Gold border,
 * and 20.dp rounded corners.
 */
@Composable
fun GlassLuxuryCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = RoyalGold.copy(alpha = 0.45f),
    containerColor: Color = MidnightGlass,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GoldTagPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RoyalGold.copy(alpha = 0.16f),
    borderColor: Color = RoyalGold.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}
