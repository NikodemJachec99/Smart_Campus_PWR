package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueLight
import Smart.Campus.PWR.ui.theme.PwrNavy
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiquidRoleSwitch(
    activeRole: UserRole,
    onToggleRole: () -> Unit
) {
    val selectorOffset by animateFloatAsState(
        targetValue = if (activeRole == UserRole.STUDENT) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "roleSwitchSelectorOffset"
    )
    val studentColor by animateColorAsState(
        targetValue = if (activeRole == UserRole.STUDENT) Cloud else InkTextSoft,
        label = "studentRoleColor"
    )
    val tutorColor by animateColorAsState(
        targetValue = if (activeRole == UserRole.TUTOR) Cloud else InkTextSoft,
        label = "tutorRoleColor"
    )
    val segmentWidth = 78.dp

    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .width(segmentWidth * 2 + 8.dp)
            .height(46.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = PwrNavy,
                spotColor = PwrNavy
            )
            .stableLiquidGlassSurface(
                cornerRadius = 28.dp,
                tint = Cloud,
                tintAlpha = 0.40f,
                glowColor = PwrBlueLight,
                glowAlpha = 0.22f
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = segmentWidth * selectorOffset)
                .size(width = segmentWidth, height = 38.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(colors = listOf(PwrNavy, PwrBlue))
                )
        )

        Row(
            modifier = Modifier
                .padding(end = 0.dp)
                .height(38.dp)
                .width(segmentWidth * 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoleSwitchSegment(
                label = "Student",
                color = studentColor,
                selected = activeRole == UserRole.STUDENT,
                onClick = {
                    if (activeRole != UserRole.STUDENT) onToggleRole()
                }
            )
            RoleSwitchSegment(
                label = "Tutor",
                color = tutorColor,
                selected = activeRole == UserRole.TUTOR,
                onClick = {
                    if (activeRole != UserRole.TUTOR) onToggleRole()
                }
            )
        }
    }
}

@Composable
private fun RowScope.RoleSwitchSegment(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "roleSwitchSegmentScale"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 12.sp
        )
    }
}
