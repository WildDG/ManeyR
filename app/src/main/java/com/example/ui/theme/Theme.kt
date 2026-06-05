package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkHighDensityPrimary,
    onPrimary = DarkHighDensityBg,
    primaryContainer = DarkHighDensityPrimaryContainer,
    onPrimaryContainer = DarkHighDensityOnPrimaryContainer,
    background = DarkHighDensityBg,
    onBackground = DarkHighDensityText,
    surface = DarkHighDensitySurface,
    onSurface = DarkHighDensityText,
    surfaceVariant = DarkHighDensitySurfaceVariant,
    onSurfaceVariant = DarkHighDensityMuted,
    outline = DarkHighDensityOutline,
    outlineVariant = DarkHighDensityOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = Color.White,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    background = HighDensityBg,
    onBackground = HighDensityText,
    surface = HighDensitySurface,
    onSurface = HighDensityText,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityMuted,
    outline = HighDensityOutline,
    outlineVariant = HighDensityOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to enforce High Density aesthetic
  dynamicColor: Boolean = false,
  customAppColorHex: String = "",
  content: @Composable () -> Unit,
) {
  var baseColorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }
  
  if (customAppColorHex.isNotEmpty()) {
      val customColor = runCatching { Color(android.graphics.Color.parseColor(customAppColorHex)) }.getOrNull()
      if (customColor != null) {
          baseColorScheme = baseColorScheme.copy(
              primary = customColor,
              primaryContainer = customColor.copy(alpha = 0.3f),
              secondary = customColor.copy(alpha = 0.7f),
              secondaryContainer = customColor.copy(alpha = 0.2f)
          )
      }
  }

  val colorScheme = baseColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
