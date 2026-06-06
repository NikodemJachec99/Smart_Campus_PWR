package Smart.Campus.PWR.ui.components.softindigo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.White

// ─── SoftTopBar ───────────────────────────────────────────────────────────────

@Composable
fun SoftTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading + title group
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (leading != null) {
                leading()
            }
            if (title != null || subtitle != null) {
                androidx.compose.foundation.layout.Column {
                    if (title != null) {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = InkToken
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = SoftType.meta
                        )
                    }
                }
            }
        }
        // Actions group
        if (actions != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

// ─── RoleSwitch ───────────────────────────────────────────────────────────────

private val roleSwitchShape = RoundedCornerShape(50)
private val thumbShape = RoundedCornerShape(50)

@Composable
fun RoleSwitch(
    isTutor: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = true
) {
    // Animate thumb position: 0f = left (Studying), 1f = right (Teaching)
    val thumbProgress by animateFloatAsState(
        targetValue = if (isTutor) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "roleSwitchThumb"
    )

    val baseModifier = if (fullWidth) modifier.fillMaxWidth() else modifier

    // Use Layout to measure half-widths and place the animated thumb
    Layout(
        modifier = baseModifier
            .clip(roleSwitchShape)
            .background(Bg2)
            .padding(4.dp),
        content = {
            // Thumb (white animated background)
            Box(
                modifier = Modifier
                    .softShadow(thumbShape)
                    .clip(thumbShape)
                    .background(CardSurface)
            )
            // Studying segment
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { if (isTutor) onToggle() }
                    .padding(vertical = 8.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = SoftIcons.cap,
                    contentDescription = null,
                    tint = if (!isTutor) Primary600 else Ink3,
                    modifier = Modifier.padding(0.dp).run { this }
                        .width(16.dp)
                        .height(16.dp)
                )
                Text(
                    text = "Studying",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (!isTutor) Primary600 else Ink3
                    )
                )
            }
            // Teaching segment
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { if (!isTutor) onToggle() }
                    .padding(vertical = 8.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = SoftIcons.teach,
                    contentDescription = null,
                    tint = if (isTutor) Primary600 else Ink3,
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp)
                )
                Text(
                    text = "Teaching",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isTutor) Primary600 else Ink3
                    )
                )
            }
        },
        measurePolicy = MeasurePolicy { measurables, constraints ->
            val thumbMeasurable   = measurables[0]
            val studyMeasurable  = measurables[1]
            val teachMeasurable  = measurables[2]

            val totalWidth = constraints.maxWidth
            val halfWidth  = totalWidth / 2

            val studyPlaceable  = studyMeasurable.measure(Constraints.fixedWidth(halfWidth))
            val teachPlaceable  = teachMeasurable.measure(Constraints.fixedWidth(halfWidth))
            val segHeight       = maxOf(studyPlaceable.height, teachPlaceable.height)
            val thumbPlaceable  = thumbMeasurable.measure(Constraints.fixed(halfWidth, segHeight))

            val thumbX = (thumbProgress * halfWidth).toInt()

            layout(totalWidth, segHeight) {
                thumbPlaceable.placeRelative(thumbX, 0)
                studyPlaceable.placeRelative(0, 0)
                teachPlaceable.placeRelative(halfWidth, 0)
            }
        }
    )
}
