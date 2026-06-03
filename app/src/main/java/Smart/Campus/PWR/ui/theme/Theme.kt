package Smart.Campus.PWR.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EditorialColorScheme = lightColorScheme(
    primary = ForestAccent,
    onPrimary = CardWhite,
    primaryContainer = ClaySoft,
    onPrimaryContainer = ForestDeep,
    secondary = ClayAccent,
    onSecondary = CardWhite,
    secondaryContainer = PaperLayer,
    onSecondaryContainer = InkPrimary,
    tertiary = GoldAccent,
    onTertiary = InkPrimary,
    tertiaryContainer = YellowSoft,
    onTertiaryContainer = InkPrimary,
    error = DangerText,
    onError = CardWhite,
    errorContainer = DangerSoft,
    onErrorContainer = DangerText,
    background = Bone,
    onBackground = InkPrimary,
    surface = Surface,
    onSurface = InkPrimary,
    surfaceVariant = PaperLayer,
    onSurfaceVariant = InkSecondary,
    outline = PaperLine,
    outlineVariant = PaperLineSoft,
    scrim = Black.copy(alpha = 0.36f)
)

@Composable
fun SmartCampusPWRTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_PARAMETER") val ignoredDarkTheme = darkTheme
    @Suppress("UNUSED_PARAMETER") val ignoredDynamicColor = dynamicColor

    MaterialTheme(
        colorScheme = EditorialColorScheme,
        typography = Typography,
        content = content
    )
}
