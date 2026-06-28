package com.javanapps.musicplayer.core.designsystem.theme

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
        primary = Violet80,
        onPrimary = Violet20,
        primaryContainer = Violet30,
        onPrimaryContainer = Violet90,
        secondary = Cyan80,
        onSecondary = Cyan20,
        secondaryContainer = Cyan30,
        onSecondaryContainer = Cyan90,
        tertiary = Fuchsia80,
        onTertiary = Fuchsia20,
        tertiaryContainer = Fuchsia30,
        onTertiaryContainer = Fuchsia90,
        error = Red80,
        onError = Red20,
        errorContainer = Red30,
        onErrorContainer = Red90,
        background = Ink05,
        onBackground = Mist90,
        surface = Ink10,
        surfaceVariant = Ink30,
        onSurface = Mist90,
        onSurfaceVariant = InkVariant,
        surfaceContainer = Ink20,
        surfaceContainerHigh = Ink30,
        outline = InkOutline,
        outlineVariant = Ink30,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Violet40,
        onPrimary = Color.White,
        primaryContainer = Violet90,
        onPrimaryContainer = Violet10,
        secondary = Cyan40,
        onSecondary = Color.White,
        secondaryContainer = Cyan90,
        onSecondaryContainer = Cyan10,
        tertiary = Fuchsia40,
        onTertiary = Color.White,
        tertiaryContainer = Fuchsia90,
        onTertiaryContainer = Fuchsia10,
        error = Red40,
        onError = Color.White,
        errorContainer = Red90,
        onErrorContainer = Red10,
        background = Mist99,
        onBackground = Mist10,
        surface = Color.White,
        surfaceVariant = Mist90,
        onSurface = Mist10,
        onSurfaceVariant = MistVariant,
        surfaceContainer = Mist95,
        surfaceContainerHigh = Mist90,
        outline = MistOutline,
        outlineVariant = Mist90,
    )

@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= 31 -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
