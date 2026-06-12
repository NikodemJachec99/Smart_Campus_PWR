package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.AmberBg
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.GreenBg
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.White

// ─── Pill ─────────────────────────────────────────────────────────────────────

private val pillShape = RoundedCornerShape(50)

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    on: Boolean = false,
    prim: Boolean = false,
    outline: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val bg = when {
        on      -> InkToken
        prim    -> Primary
        outline -> CardSurface
        else    -> Bg2
    }
    val textColor = when {
        on || prim -> White
        else       -> Ink2
    }

    var mod = modifier
    mod = if (outline) mod.softShadow(pillShape) else mod
    mod = mod
        .clip(pillShape)
        .background(bg)
        .padding(vertical = 8.dp, horizontal = 13.dp)

    Row(
        modifier = mod,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = textColor
            )
        )
    }
}

// ─── Chip ─────────────────────────────────────────────────────────────────────

private val chipShape = RoundedCornerShape(10.dp)

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val bg = if (selected) Primary else Bg2
    val textColor = if (selected) White else Ink2

    Row(
        modifier = modifier
            .clip(chipShape)
            .background(bg)
            .then(
                if (onClick != null)
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = textColor)
                    ) { onClick() }
                else Modifier
            )
            .padding(vertical = 7.dp, horizontal = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor
            )
        )
    }
}

// ─── Badge ────────────────────────────────────────────────────────────────────

enum class BadgeTone { Green, Amber, Red, Prim, Gray }

private val badgeShape = RoundedCornerShape(50)

@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    tone: BadgeTone = BadgeTone.Gray,
    leadingIcon: ImageVector? = null
) {
    val (bg, textColor) = when (tone) {
        BadgeTone.Green -> GreenBg to Green
        BadgeTone.Amber -> AmberBg to Amber
        BadgeTone.Red   -> RedBg   to Red
        BadgeTone.Prim  -> Primary100 to Primary600
        BadgeTone.Gray  -> Bg2    to Ink3
    }

    Row(
        modifier = modifier
            .clip(badgeShape)
            .background(bg)
            .padding(vertical = 4.dp, horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor
            )
        )
    }
}

// ─── StatusDot ────────────────────────────────────────────────────────────────

@Composable
fun StatusDot(
    color: Color = Green,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ─── SoftIconButton ───────────────────────────────────────────────────────────

@Composable
fun SoftIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dot: Boolean = false,
    badgeCount: Int = 0,
    solid: Boolean = false,
    prim: Boolean = false
) {
    val bg = when {
        prim   -> Primary
        solid  -> InkToken
        else   -> CardSurface
    }
    val iconTint = when {
        prim || solid -> White
        else          -> InkToken
    }

    val shadowMod = when {
        prim  -> modifier.primShadow(CircleShape)
        else  -> modifier.softShadow(CircleShape)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = shadowMod
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = iconTint)
            ) { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .clip(badgeShape)
                    .background(White)
                    .padding(1.5.dp)
                    .clip(badgeShape)
                    .background(Red)
                    .padding(horizontal = 4.5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = White
                    )
                )
            }
        } else if (dot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Red)
            )
        }
    }
}
