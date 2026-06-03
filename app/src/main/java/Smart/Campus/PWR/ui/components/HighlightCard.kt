package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.OnHighlight
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueLight
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
fun HighlightCard(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    progress: Float? = null,
    progressLabel: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    urgent: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "highlightScale"
    )

    // Starting-soon pulse — animates the inner glow strength when `urgent` is true.
    val pulseTransition = rememberInfiniteTransition(label = "highlightPulse")
    val pulseStrength by pulseTransition.animateFloat(
        initialValue = if (urgent) 0.55f else 0.55f,
        targetValue = if (urgent) 0.95f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightPulseStrength"
    )
    val effectiveGlow = if (urgent) pulseStrength else 0.55f

    val baseModifier = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = if (urgent) 28.dp else 22.dp,
            shape = RoundedCornerShape(28.dp),
            ambientColor = PwrNavy,
            spotColor = PwrNavy
        )
        .clip(RoundedCornerShape(28.dp))
        .stableLiquidGlassSurface(
            cornerRadius = 28.dp,
            tint = PwrNavy,
            tintAlpha = 0.95f,
            glowColor = PwrBlueLight,
            glowAlpha = effectiveGlow
        )

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else baseModifier

    Box(modifier = clickableModifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (leading != null) leading()
                    Text(
                        eyebrow.uppercase(),
                        color = PwrBlueSoft,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                }
                if (trailing != null) trailing()
            }

            Text(
                title,
                color = OnHighlight,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = (-0.4).sp
            )

            if (subtitle != null) {
                Text(
                    subtitle,
                    color = PwrBlueSoft,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.5.sp
                )
            }

            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                ProgressBar(progress = progress.coerceIn(0f, 1f), label = progressLabel)
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, label: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .height(8.dp)
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PwrBlueLight, OnHighlight)
                        )
                    )
            )
        }
        if (label != null) {
            Text(
                label,
                color = PwrBlueSoft,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Suppress("UNUSED")
private val UnusedCloud = Cloud
@Suppress("UNUSED")
private val UnusedPwrBlue = PwrBlue
