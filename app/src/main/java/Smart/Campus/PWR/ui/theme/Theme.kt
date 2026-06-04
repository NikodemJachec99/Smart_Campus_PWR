package Smart.Campus.PWR.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SoftIndigoColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary700,
    secondary = Primary600,
    onSecondary = White,
    secondaryContainer = Primary50,
    onSecondaryContainer = Primary700,
    tertiary = Amber,
    onTertiary = White,
    tertiaryContainer = AmberBg,
    onTertiaryContainer = Amber,
    error = Red,
    onError = White,
    errorContainer = RedBg,
    onErrorContainer = Red,
    background = Bg,
    onBackground = InkToken,
    surface = CardSurface,
    onSurface = InkToken,
    surfaceVariant = Bg2,
    onSurfaceVariant = Ink2,
    outline = Line2,
    outlineVariant = Line,
    scrim = Black.copy(alpha = 0.36f)
)

private val SoftIndigoShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(26.dp)
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
        colorScheme = SoftIndigoColorScheme,
        typography = Typography,
        shapes = SoftIndigoShapes,
        content = content
    )
}
