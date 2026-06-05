package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Line2
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.White

enum class SoftButtonVariant { Primary, Ink, Soft, Outline, Ghost, Danger }
enum class SoftButtonSize { Md, Sm }

private data class ButtonTokens(
    val bg: Color,
    val contentColor: Color,
    val borderColor: Color?,
    val hasPrimShadow: Boolean,
    val hasSoftShadow: Boolean
)

private fun resolveTokens(variant: SoftButtonVariant) = when (variant) {
    SoftButtonVariant.Primary  -> ButtonTokens(Primary,    White,   null,  hasPrimShadow = true,  hasSoftShadow = false)
    SoftButtonVariant.Ink      -> ButtonTokens(InkToken,   White,   null,  hasPrimShadow = false, hasSoftShadow = true)
    SoftButtonVariant.Soft     -> ButtonTokens(Primary50,  Primary600, null, hasPrimShadow = false, hasSoftShadow = false)
    SoftButtonVariant.Outline  -> ButtonTokens(CardSurface, InkToken, Line2, hasPrimShadow = false, hasSoftShadow = true)
    SoftButtonVariant.Ghost    -> ButtonTokens(Color.Transparent, Ink2, null, hasPrimShadow = false, hasSoftShadow = false)
    SoftButtonVariant.Danger   -> ButtonTokens(RedBg,      Red,     null,  hasPrimShadow = false, hasSoftShadow = false)
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SoftButtonVariant = SoftButtonVariant.Primary,
    size: SoftButtonSize = SoftButtonSize.Md,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val tokens = resolveTokens(variant)
    val isMd = size == SoftButtonSize.Md
    val shape = RoundedCornerShape(if (isMd) 16.dp else 12.dp)
    val iconSize = if (isMd) 18.dp else 15.dp
    val vertPad = if (isMd) 14.dp else 10.dp
    val horizPad = if (isMd) 20.dp else 14.dp
    val fontSize = if (isMd) 15.sp else 13.sp

    var mod = modifier
        .alpha(if (enabled) 1f else 0.5f)

    mod = when {
        tokens.hasPrimShadow -> mod.primShadow(shape)
        tokens.hasSoftShadow -> mod.softShadow(shape)
        else                 -> mod
    }

    if (tokens.borderColor != null) {
        mod = mod
            .clip(shape)
            .background(tokens.bg)
            .border(1.dp, tokens.borderColor, shape)
    } else {
        mod = mod
            .clip(shape)
            .background(tokens.bg)
    }

    mod = mod
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = tokens.contentColor)
        ) { onClick() }
        .padding(vertical = vertPad, horizontal = horizPad)

    Row(
        modifier = mod,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = tokens.contentColor,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = tokens.contentColor
            )
        )
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = tokens.contentColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
