package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.SoftType

// ─── ProgressBar ──────────────────────────────────────────────────────────────

private val progressShape = RoundedCornerShape(50)

@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(progressShape)
            .background(Bg2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(progressShape)
                .background(Primary)
        )
    }
}

// ─── SegTabs ──────────────────────────────────────────────────────────────────

private val segTabsOuter = RoundedCornerShape(14.dp)
private val segTabsItem  = RoundedCornerShape(10.dp)

@Composable
fun SegTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(segTabsOuter)
            .background(Bg2)
            .padding(4.dp)
    ) {
        options.forEachIndexed { idx, label ->
            val isSelected = idx == selectedIndex
            var itemMod: Modifier = Modifier.weight(1f)
            itemMod = if (isSelected) itemMod.softShadow(segTabsItem) else itemMod
            itemMod = itemMod
                .clip(segTabsItem)
                .background(if (isSelected) CardSurface else CardSurface.copy(alpha = 0f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Primary)
                ) { onSelect(idx) }
                .padding(vertical = 8.dp, horizontal = 4.dp)

            Box(
                modifier = itemMod,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) InkToken else Ink3
                    )
                )
            }
        }
    }
}

// ─── SectionHead ──────────────────────────────────────────────────────────────

@Composable
fun SectionHead(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = SoftType.h3
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Primary600
                ),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAction() }
            )
        } else if (action != null) {
            Text(
                text = action,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Primary600
                )
            )
        }
    }
}

// ─── ImagePlaceholder ─────────────────────────────────────────────────────────

@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    label: String? = null,
    height: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(Bg2),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Ink3
                )
            )
        }
    }
}
