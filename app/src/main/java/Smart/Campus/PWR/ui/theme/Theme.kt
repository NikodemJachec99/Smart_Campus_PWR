package Smart.Campus.PWR.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PwrNavy,
    onPrimary = White,
    primaryContainer = PwrBlueSoft,
    onPrimaryContainer = PwrNavyDark,
    secondary = PwrBlueMuted,
    onSecondary = White,
    secondaryContainer = AppSurfaceMuted,
    onSecondaryContainer = TextPrimary,
    tertiary = WarningText,
    onTertiary = White,
    tertiaryContainer = WarningSoft,
    onTertiaryContainer = WarningText,
    error = PwrRed,
    onError = White,
    errorContainer = PwrRedSoft,
    onErrorContainer = PwrRed,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = AppBorder,
    outlineVariant = AppBorder,
    scrim = Black.copy(alpha = 0.4f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PwrBlueSoft,
    onPrimary = PwrNavyDark,
    secondary = PwrBlueMuted,
    onSecondary = White,
    error = PwrRed,
    onError = White,
    background = PwrNavyDark,
    onBackground = White,
    surface = ColorTokens.DarkSurface,
    onSurface = White,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = PwrBlueSoft,
    outline = PwrBlueMuted
)

private object ColorTokens {
    val DarkSurface = PwrNavyDark.copy(alpha = 0.96f)
    val DarkSurfaceVariant = PwrNavy.copy(alpha = 0.88f)
}

@Composable
fun SmartCampusPWRTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val ignoredDynamicColor = dynamicColor
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
