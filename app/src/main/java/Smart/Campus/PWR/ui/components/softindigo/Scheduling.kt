package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.White

// ─── DayStrip ─────────────────────────────────────────────────────────────────

@Composable
fun DayStrip(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// ─── DayCell ──────────────────────────────────────────────────────────────────

private val dayCellShape = RoundedCornerShape(16.dp)

@Composable
fun DayCell(
    dow: String,
    dayNum: String,
    modifier: Modifier = Modifier,
    slotLabel: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val bg = if (selected) Primary else CardSurface
    val dayOfWeekColor = if (selected) White.copy(alpha = 0.8f) else Ink3
    val dayNumColor = if (selected) White else InkToken

    var mod = modifier
        .alpha(if (enabled) 1f else 0.4f)

    mod = if (selected) mod.primShadow(dayCellShape) else mod.softShadow(dayCellShape)

    mod = mod
        .defaultMinSize(minWidth = 50.dp)
        .clip(dayCellShape)
        .background(bg)

    if (onClick != null && enabled) {
        mod = mod.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = if (selected) White else Primary)
        ) { onClick() }
    }

    Column(
        modifier = mod.padding(horizontal = 6.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = dow,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = dayOfWeekColor
            )
        )
        Text(
            text = dayNum,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = dayNumColor
            )
        )
        if (slotLabel != null) {
            Text(
                text = slotLabel,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (selected) White.copy(alpha = 0.7f) else Ink3
                )
            )
        }
    }
}

// ─── SlotChip ─────────────────────────────────────────────────────────────────

enum class SlotState { On, Off, Default }

private val slotShape = RoundedCornerShape(14.dp)

@Composable
fun SlotChip(
    label: String,
    modifier: Modifier = Modifier,
    state: SlotState = SlotState.Default,
    onClick: (() -> Unit)? = null
) {
    val isOn  = state == SlotState.On
    val isOff = state == SlotState.Off

    val bg = when {
        isOn  -> Primary
        isOff -> Bg2
        else  -> CardSurface
    }
    val textColor = if (isOn) White else InkToken
    val textDecoration = if (isOff) TextDecoration.LineThrough else null

    var mod = modifier
        .alpha(if (isOff) 0.4f else 1f)

    mod = when {
        isOn  -> mod.primShadow(slotShape)
        isOff -> mod  // no shadow for off state
        else  -> mod.softShadow(slotShape)
    }

    mod = mod
        .clip(slotShape)
        .background(bg)

    if (onClick != null && !isOff) {
        mod = mod.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = if (isOn) White else Primary)
        ) { onClick() }
    }

    Text(
        text = label,
        modifier = mod.padding(horizontal = 13.dp, vertical = 8.dp),
        style = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = textColor,
            textDecoration = textDecoration
        )
    )
}
