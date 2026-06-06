package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.White

// ─── MessageBubble ────────────────────────────────────────────────────────────

private fun bubbleShape(isMe: Boolean) = if (isMe) {
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 5.dp)
} else {
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 5.dp, bottomEnd = 18.dp)
}

@Composable
fun MessageBubble(
    isMe: Boolean,
    modifier: Modifier = Modifier,
    text: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    val shape = bubbleShape(isMe)
    val bg = if (isMe) Primary else CardSurface

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .run { if (isMe) this else softShadow(shape) }
                .clip(shape)
                .background(bg)
                .padding(vertical = 11.dp, horizontal = 14.dp)
        ) {
            if (content != null) {
                content()
            } else if (text != null) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isMe) White else InkToken
                    )
                )
            }
        }
    }
}

// ─── MessageTime ──────────────────────────────────────────────────────────────

@Composable
fun MessageTime(
    text: String,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                color = Ink4
            )
        )
    }
}
