package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.CardWhite
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.DisplayFontFamily
import Smart.Campus.PWR.ui.theme.ForestAccent
import Smart.Campus.PWR.ui.theme.InkPrimary
import Smart.Campus.PWR.ui.theme.InkSecondary
import Smart.Campus.PWR.ui.theme.InkTertiary
import Smart.Campus.PWR.ui.theme.MonoFontFamily
import Smart.Campus.PWR.ui.theme.PaperLineSoft
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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
