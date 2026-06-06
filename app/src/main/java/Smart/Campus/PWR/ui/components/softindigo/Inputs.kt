package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line2
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.icons.SoftIcons

// ─── SearchField ──────────────────────────────────────────────────────────────

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    big: Boolean = false,
    readOnly: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    val radius = if (big) 18.dp else 16.dp
    val shape = RoundedCornerShape(radius)
    val vertPad = if (big) 16.dp else 14.dp
    val horizPad = if (big) 18.dp else 16.dp

    Row(
        modifier = modifier
            .softShadow(shape)
            .clip(shape)
            .background(CardSurface)
            .padding(vertical = vertPad, horizontal = horizPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = SoftIcons.search,
            contentDescription = null,
            tint = Ink3,
            modifier = Modifier.size(19.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = InkToken
                ),
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.fillMaxWidth()
            )
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Ink4
                    )
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

// ─── SoftTextField ────────────────────────────────────────────────────────────

@Composable
fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (isFocused) Primary else Line2

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Ink2
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CardSurface)
                .border(1.dp, borderColor, shape)
                .padding(vertical = 13.dp, horizontal = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                minLines = minLines,
                textStyle = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = InkToken
                ),
                cursorBrush = SolidColor(Primary),
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth()
            )
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = Ink4
                    )
                )
            }
        }
    }
}
