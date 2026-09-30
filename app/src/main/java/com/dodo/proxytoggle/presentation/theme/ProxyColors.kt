package com.dodo.proxytoggle.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colors used across the proxy screen, resolved once for the current dark/light mode.
 */
data class ProxyColors(
    val background: Color,
    val foreground: Color,
    val muted: Color,
    val accent: Color,
    val outline: Color
)

private val LightProxyColors = ProxyColors(
    background = Color(0xFFEAF4FA),
    foreground = Color(0xFF000000),
    muted = Color(0xFF1A1A1A),
    accent = Color(0xFF0091B3),
    outline = Color(0xFF333333)
)

private val DarkProxyColors = ProxyColors(
    background = Color(0xFF050A14),
    foreground = Color(0xFFE3F4FA),
    muted = Color(0xFF9FB8C8),
    accent = Color(0xFF00E5FF),
    outline = Color(0xFF3E6378)
)

fun proxyColors(darkMode: Boolean): ProxyColors = if (darkMode) DarkProxyColors else LightProxyColors

/**
 * Wraps [content] in a [MaterialTheme] whose color scheme is derived from [colors].
 */
@Composable
fun ProxyTheme(darkMode: Boolean, colors: ProxyColors, content: @Composable () -> Unit) {
    val colorScheme = if (darkMode) {
        darkColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.background,
            onSurface = colors.foreground
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.background,
            onSurface = colors.foreground,
            onSurfaceVariant = colors.muted
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
