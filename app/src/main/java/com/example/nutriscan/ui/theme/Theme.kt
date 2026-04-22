package com.example.nutriscan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NutriScanColorScheme = lightColorScheme(
    primary          = TealPrimary,
    onPrimary        = SheetBg,
    primaryContainer = TealSurface,
    onPrimaryContainer = TealDark,
    surface          = SheetBg,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextSecondary,
    background       = CameraBg,
    onBackground     = SheetBg,
)

@Composable
fun NutriScanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NutriScanColorScheme,
        typography  = NutriScanTypography,
        content     = content,
    )
}
