package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.Star

enum class StarSize { Sm, Lg, Xl }

@Composable
fun StarsRow(
    value: Float,
    modifier: Modifier = Modifier,
    total: Int = 5,
    size: StarSize = StarSize.Sm
) {
    val iconDp = when (size) {
        StarSize.Sm -> 13.dp
        StarSize.Lg -> 26.dp
        StarSize.Xl -> 34.dp
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 1..total) {
            val filled = value >= i - 0.25f
            Icon(
                imageVector = if (filled) SoftIcons.star else SoftIcons.starO,
                contentDescription = null,
                tint = if (filled) Star else Ink4,
                modifier = Modifier.size(iconDp)
            )
        }
    }
}
