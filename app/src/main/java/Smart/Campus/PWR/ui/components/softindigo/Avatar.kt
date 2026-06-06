package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.White

private val avatarPalette = listOf(
    Color(0xFF6D5DF2),
    Color(0xFF2C6BD4),
    Color(0xFF1E9E6A),
    Color(0xFFC9722E),
    Color(0xFFC84A77),
    Color(0xFF7A5AE0)
)

private fun initials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(1).uppercase()
        else            -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    square: Boolean = false,
    online: Boolean = false,
    colorIndex: Int? = null
) {
    val shape = if (square) RoundedCornerShape(18.dp) else CircleShape
    val idx = ((colorIndex ?: name.hashCode()) % avatarPalette.size + avatarPalette.size) % avatarPalette.size
    val bgColor = avatarPalette[idx]
    val textSp = (size.value * 0.4f).sp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials(name),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = textSp,
                    color = White
                )
            )
        }
        if (online) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Green)
            )
        }
    }
}
