package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import Smart.Campus.PWR.ui.theme.Primary

// ─── Shared shadow helpers ─────────────────────────────────────────────────
// Each clips=false so the shadow is drawn outside the shape bounds.
// Pass a shape if the default doesn't match the component's corner radius.

fun Modifier.softShadow(shape: Shape = RoundedCornerShape(22.dp)): Modifier =
    this.shadow(elevation = 2.dp, shape = shape, clip = false)

fun Modifier.cardShadow(shape: Shape = RoundedCornerShape(22.dp)): Modifier =
    this.shadow(elevation = 8.dp, shape = shape, clip = false)

fun Modifier.raiseShadow(shape: Shape = RoundedCornerShape(22.dp)): Modifier =
    this.shadow(elevation = 16.dp, shape = shape, clip = false)

fun Modifier.primShadow(shape: Shape = RoundedCornerShape(16.dp)): Modifier =
    this.shadow(
        elevation = 12.dp,
        shape = shape,
        clip = false,
        spotColor = Primary.copy(alpha = 0.4f),
        ambientColor = Primary.copy(alpha = 0.4f)
    )

/** Upward-facing shadow for bottom navigation bars. */
fun Modifier.navShadow(shape: Shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)): Modifier =
    this.shadow(elevation = 14.dp, shape = shape, clip = false)
