package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KemetLuxuryColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianDeep,
    primaryContainer = Color(0xFF2B230B),
    onPrimaryContainer = RoyalGoldLight,
    secondary = ElectricAzure,
    onSecondary = CrispWhite,
    secondaryContainer = DeepSkyBlue,
    onSecondaryContainer = CrispWhite,
    tertiary = RoyalGoldLight,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = CrispWhite,
    surface = MidnightNavy,
    onSurface = CrispWhite,
    surfaceVariant = MidnightElevated,
    onSurfaceVariant = CoolSilver,
    outline = RoyalGoldDim,
    error = TerracottaError,
    onError = CrispWhite,
    errorContainer = Color(0xFF450A0A)
)

val KemetShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KemetLoopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KemetLuxuryColorScheme,
        typography = KemetTypography,
        shapes = KemetShapes,
        content = content
    )
}
