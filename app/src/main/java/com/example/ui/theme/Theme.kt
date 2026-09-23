package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ShirazuDarkPrimary,
    onPrimary = ShirazuDarkOnPrimary,
    primaryContainer = ShirazuDarkPrimaryContainer,
    onPrimaryContainer = ShirazuDarkOnPrimaryContainer,
    secondary = ShirazuDarkSecondary,
    onSecondary = ShirazuDarkOnSecondary,
    secondaryContainer = ShirazuDarkSecondaryContainer,
    onSecondaryContainer = ShirazuDarkOnSecondaryContainer,
    background = ShirazuDarkBackground,
    onBackground = ShirazuDarkOnBackground,
    surface = ShirazuDarkSurface,
    onSurface = ShirazuDarkOnSurface,
    surfaceVariant = ShirazuDarkSurfaceVariant,
    onSurfaceVariant = ShirazuDarkOnSurfaceVariant,
    error = ShirazuError,
    onError = ShirazuOnError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ShirazuPrimary,
    onPrimary = ShirazuOnPrimary,
    primaryContainer = ShirazuPrimaryContainer,
    onPrimaryContainer = ShirazuOnPrimaryContainer,
    secondary = ShirazuSecondary,
    onSecondary = ShirazuOnSecondary,
    secondaryContainer = ShirazuSecondaryContainer,
    onSecondaryContainer = ShirazuOnSecondaryContainer,
    tertiary = ShirazuTertiary,
    onTertiary = ShirazuOnTertiary,
    tertiaryContainer = ShirazuTertiaryContainer,
    onTertiaryContainer = ShirazuOnTertiaryContainer,
    background = ShirazuBackground,
    onBackground = ShirazuOnBackground,
    surface = ShirazuSurface,
    onSurface = ShirazuOnSurface,
    surfaceVariant = ShirazuSurfaceVariant,
    onSurfaceVariant = ShirazuOnSurfaceVariant,
    error = ShirazuError,
    onError = ShirazuOnError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep branded Shiraz University blue/gold by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
