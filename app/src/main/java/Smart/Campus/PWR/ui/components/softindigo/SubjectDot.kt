package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.theme.CsBg
import Smart.Campus.PWR.ui.theme.CsFg
import Smart.Campus.PWR.ui.theme.LangBg
import Smart.Campus.PWR.ui.theme.LangFg
import Smart.Campus.PWR.ui.theme.MathBg
import Smart.Campus.PWR.ui.theme.MathFg
import Smart.Campus.PWR.ui.theme.PhysBg
import Smart.Campus.PWR.ui.theme.PhysFg
import Smart.Campus.PWR.ui.theme.StatBg
import Smart.Campus.PWR.ui.theme.StatFg

/** Returns (background, foreground) color pair for a subject name. */
fun subjectColors(name: String): Pair<Color, Color> {
    val n = name.lowercase()
    return when {
        n.contains("calculus") || n.contains("lin") || n.contains("algebra") || n.contains("math") ->
            MathBg to MathFg
        n.contains("thermo") || n.contains("physics") || n.contains("phys") ->
            PhysBg to PhysFg
        n.contains("oop") || n.contains("java") || n.contains("cs") || n.contains("programming") ->
            CsBg to CsFg
        n.contains("stat") ->
            StatBg to StatFg
        n.contains("english") || n.contains("polish") || n.contains("lang") ->
            LangBg to LangFg
        else ->
            MathBg to MathFg
    }
}

/** Rounded icon box colored by subject. */
@Composable
fun SubjectDot(
    subject: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    icon: ImageVector = SoftIcons.cap
) {
    val (bg, fg) = subjectColors(subject)
    val iconSize = (size.value * 0.5f).dp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = subject,
            tint = fg,
            modifier = Modifier.size(iconSize)
        )
    }
}
