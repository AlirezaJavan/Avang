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
        primary = Blue80,
        onPrimary = Blue20,
        primaryContainer = Blue30,
        onPrimaryContainer = Blue90,
        secondary = DarkBlue80,
        onSecondary = DarkBlue20,
        secondaryContainer = DarkBlue30,
        onSecondaryContainer = DarkBlue90,
        tertiary = Yellow80,
        onTertiary = Yellow20,
        tertiaryContainer = Yellow30,
        onTertiaryContainer = Yellow90,
        error = Red80,
        onError = Red20,
        errorContainer = Red30,
        onErrorContainer = Red90,
        background = Color(0xFF080D0F),
        onBackground = Gray90,
        surface = Color(0xFF0F171A),
        onSurface = BlueGray90,
        surfaceVariant = BlueGray30,
        onSurfaceVariant = BlueGray80,
        outline = BlueGray50,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Blue40,
        onPrimary = Color.White,
        primaryContainer = Blue90,
        onPrimaryContainer = Blue10,
        secondary = DarkBlue40,
        onSecondary = Color.White,
        secondaryContainer = DarkBlue90,
        onSecondaryContainer = DarkBlue10,
        tertiary = Yellow40,
        onTertiary = Color.White,
        tertiaryContainer = Yellow90,
        onTertiaryContainer = Yellow10,
        error = Red40,
        onError = Color.White,
        errorContainer = Red90,
        onErrorContainer = Red10,
        background = Gray99,
        onBackground = Gray10,
        surface = BlueGray90,
        onSurface = BlueGray10,
        surfaceVariant = BlueGray90,
        onSurfaceVariant = BlueGray30,
        outline = BlueGray50,
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
