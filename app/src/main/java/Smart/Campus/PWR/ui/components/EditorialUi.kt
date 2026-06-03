package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppBorder
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardWhite
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.DisplayFontFamily
import Smart.Campus.PWR.ui.theme.ForestAccent
import Smart.Campus.PWR.ui.theme.InkPrimary
import Smart.Campus.PWR.ui.theme.InkSecondary
import Smart.Campus.PWR.ui.theme.InkTertiary
import Smart.Campus.PWR.ui.theme.MonoFontFamily
import Smart.Campus.PWR.ui.theme.PaperLayer
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.PaperLineSoft
import Smart.Campus.PWR.ui.theme.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorialScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun EditorialTopBar(
    brand: String,
    modifier: Modifier = Modifier,
    left: (@Composable () -> Unit)? = null,
    right: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (left != null) {
                left()
            } else {
                MonoLabel(text = brand, color = InkSecondary, letterSpacing = 2.2.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (right != null) right()
        }
    }
}

@Composable
fun RoleSegment(
    activeRole: UserRole,
    canSwitch: Boolean,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(CardWhite)
            .border(1.dp, PaperLine, RoundedCornerShape(50))
            .clickable(
                enabled = canSwitch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSwitch
            )
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoleSegmentItem("Student", activeRole == UserRole.STUDENT)
        RoleSegmentItem("Tutor", activeRole == UserRole.TUTOR)
    }
}

@Composable
private fun RoleSegmentItem(label: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) InkPrimary else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        MonoLabel(
            text = label,
            color = if (selected) AppBackground else InkTertiary,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun EditorialCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(cornerRadius), ambientColor = InkPrimary.copy(alpha = 0.08f), spotColor = InkPrimary.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(cornerRadius))
            .background(CardWhite)
            .border(1.dp, PaperLineSoft, RoundedCornerShape(cornerRadius))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Transparent)
            .border(1.dp, PaperLine, RoundedCornerShape(cornerRadius))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun Pill(
    text: String,
    selected: Boolean = false,
    accent: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bg = when {
        selected -> InkPrimary
        accent -> ForestAccent
        else -> Color.Transparent
    }
    val fg = if (selected || accent) AppBackground else InkSecondary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (selected || accent) bg else PaperLine, RoundedCornerShape(50))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        MonoLabel(text = text, color = fg, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = InkTertiary,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 1.4.sp,
    textAlign: TextAlign? = null
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontFamily = MonoFontFamily,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 4).sp,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SerifTitle(
    text: String,
    modifier: Modifier = Modifier,
    italicTail: String? = null,
    color: Color = InkPrimary,
    fontSize: androidx.compose.ui.unit.TextUnit = 32.sp
) {
    Text(
        text = if (italicTail == null) text else "$text $italicTail",
        modifier = modifier,
        color = color,
        fontFamily = DisplayFontFamily,
        fontStyle = if (italicTail == null) FontStyle.Normal else FontStyle.Italic,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 2).sp,
        letterSpacing = 0.sp
    )
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    square: Boolean = false,
    paletteIndex: Int? = null
) {
    val palettes = listOf(
        ClayAccent to AppBackground,
        ForestAccent to AppBackground,
        Color(0xFF2C4A6B) to AppBackground,
        Color(0xFF7A5A28) to AppBackground,
        Color(0xFFA23B5B) to AppBackground,
        Color(0xFF3F4A2C) to AppBackground
    )
    val index = paletteIndex ?: (name.sumOf { it.code }.mod(palettes.size))
    val (bg, fg) = palettes[index.mod(palettes.size)]
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

    Box(
        modifier = modifier
            .size(size)
            .clip(if (square) RoundedCornerShape(14.dp) else CircleShape)
            .background(bg)
            .border(1.dp, Color.Transparent, if (square) RoundedCornerShape(14.dp) else CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = fg,
            fontFamily = DisplayFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = (size.value * 0.42f).sp,
            lineHeight = (size.value * 0.42f).sp
        )
    }
}

@Composable
fun StarsRow(
    value: Int,
    max: Int = 5,
    modifier: Modifier = Modifier,
    size: Dp = 13.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(max) { index ->
            Icon(
                imageVector = if (index < value) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (index < value) ClayAccent else InkTertiary.copy(alpha = 0.45f),
                modifier = Modifier.size(size)
            )
        }
    }
}

data class CalendarDayUi(
    val day: String,
    val number: String,
    val caption: String = "",
    val selected: Boolean = false
)

@Composable
fun CalendarStrip(
    days: List<CalendarDayUi>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier
                    .widthIn(min = 54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (day.selected) InkPrimary else CardWhite)
                    .border(1.dp, if (day.selected) InkPrimary else PaperLine, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MonoLabel(text = day.day, color = if (day.selected) AppBackground.copy(alpha = 0.62f) else InkTertiary, fontSize = 9.sp)
                Text(
                    text = day.number,
                    color = if (day.selected) AppBackground else InkPrimary,
                    fontFamily = DisplayFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 24.sp
                )
                if (day.caption.isNotBlank()) {
                    MonoLabel(text = day.caption, color = if (day.selected) AppBackground.copy(alpha = 0.62f) else InkTertiary, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun SlotChip(
    text: String,
    selected: Boolean = false,
    disabled: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) InkPrimary else CardWhite)
            .border(1.dp, if (selected) InkPrimary else PaperLine, RoundedCornerShape(12.dp))
            .then(if (onClick != null && !disabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = when {
                disabled -> InkTertiary.copy(alpha = 0.45f)
                selected -> AppBackground
                else -> InkSecondary
            },
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            textDecoration = if (disabled) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun BottomActionBar(
    label: String,
    value: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground)
            .border(1.dp, AppBorder)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(0.75f)) {
            MonoLabel(text = label)
            Text(
                text = value,
                color = InkPrimary,
                fontFamily = DisplayFontFamily,
                fontSize = 22.sp,
                lineHeight = 24.sp
            )
        }
        AppPrimaryButton(
            text = action,
            onClick = onAction,
            modifier = Modifier.weight(1.25f)
        )
    }
}

@Composable
fun ReceiptCard(
    title: String,
    subtitle: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    note: String? = null
) {
    EditorialCard(modifier = modifier, cornerRadius = 18.dp) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            MonoLabel(text = "Receipt")
            MonoLabel(text = "PWR")
        }
        Text(title, color = InkPrimary, fontFamily = DisplayFontFamily, fontSize = 22.sp, lineHeight = 25.sp)
        Text(subtitle, color = InkSecondary, style = MaterialTheme.typography.bodyMedium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, PaperLineSoft, RoundedCornerShape(14.dp))
        ) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (index > 0) Modifier.border(0.5.dp, PaperLineSoft) else Modifier)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoLabel(text = row.first)
                    Text(
                        text = row.second,
                        color = InkPrimary,
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (!note.isNullOrBlank()) {
            Text(note, color = InkSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun IconCircleButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    dot: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(1.dp, PaperLine, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = InkPrimary, modifier = Modifier.size(17.dp))
        if (dot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ClayAccent)
                    .border(1.5.dp, AppBackground, CircleShape)
            )
        }
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .width(1.dp)
            .background(PaperLineSoft)
    )
}
