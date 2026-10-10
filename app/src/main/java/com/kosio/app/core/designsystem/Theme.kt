package com.kosio.app.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KosioColorScheme = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = PureWhiteSurface,
    primaryContainer = LightIndigo,
    onPrimaryContainer = DeepIndigo,
    secondary = SuccessTeal,
    onSecondary = PureWhiteSurface,
    secondaryContainer = LightTeal,
    onSecondaryContainer = DarkTeal,
    tertiary = IndigoAccent,
    background = WarmIvoryBackground,
    onBackground = CharcoalText,
    surface = PureWhiteSurface,
    onSurface = CharcoalText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = BorderSubtle,
    error = ErrorRed,
    onError = PureWhiteSurface,
    errorContainer = LightRed,
    onErrorContainer = ErrorRed
)

@Composable
fun KosioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = KosioColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepIndigo.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
