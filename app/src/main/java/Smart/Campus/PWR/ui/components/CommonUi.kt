package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.Hairline
import Smart.Campus.PWR.ui.theme.HairlineStrong
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.Mist
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrBlueWhisper
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.ShadowSoft
import Smart.Campus.PWR.ui.theme.Snow
import Smart.Campus.PWR.ui.theme.SnowDeep
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tracks whether any scrollable list inside the dashboard has been scrolled past the top.
 * The bottom navbar reads this to perform its lift-on-scroll animation (Apple Music style).
 */
val LocalScrollLifted = compositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(false)
}

@Composable
fun MainList(content: @Composable ColumnScope.() -> Unit) {
    val listState = rememberLazyListState()
    val scrollLifted = LocalScrollLifted.current
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 6
        }.collect { lifted -> scrollLifted.value = lifted }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Snow)
                // Soft top blue ambient
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(
                            PwrBlueWhisper.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, -size.height * 0.05f),
                        radius = size.width * 1.0f
                    )
                )
            },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content) }
    }
}

@Composable
fun ProvideScrollLifted(state: MutableState<Boolean>, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalScrollLifted provides state, content = content)
}

internal fun Modifier.floatingCard(cornerRadius: Dp): Modifier = this
    .shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = PwrNavy,
        spotColor = PwrNavy,
        clip = false
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            colors = listOf(Cloud, SnowDeep)
        )
    )
    .border(
        width = 0.7.dp,
        brush = Brush.verticalGradient(
            colors = listOf(HairlineStrong, Hairline)
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

@Composable
fun SectionCard(
    title: String,
    accent: Color = PwrNavy,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    @Suppress("UNUSED_PARAMETER") val ignoredAccent = accent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(cornerRadius = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(PwrNavy)
                    )
                    Text(
                        title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = PwrNavy,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp
                    )
                }
                if (trailing != null) trailing()
            }
            content()
        }
    }
}

@Composable
fun PlainCard(
    accent: Color = PwrNavy,
    content: @Composable ColumnScope.() -> Unit
) {
    @Suppress("UNUSED_PARAMETER") val ignoredAccent = accent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .floatingCard(cornerRadius = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun RoleCheck(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PwrNavy,
                uncheckedColor = HairlineStrong,
                checkmarkColor = Cloud
            )
        )
        Text(label, color = InkText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MessageBlock(error: String?, info: String?) {
    if (error != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PwrRed.copy(alpha = 0.08f))
                .border(0.6.dp, PwrRed.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = PwrRed)
            Text(error, color = PwrRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
    if (info != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PwrBlueWhisper)
                .border(0.6.dp, Hairline, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null, tint = PwrNavy)
            Text(info, color = PwrNavy, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "appBtnScale"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = RoundedCornerShape(50),
                spotColor = PwrNavy,
                ambientColor = PwrNavy
            )
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(PwrNavy, PwrBlue)
                    )
                } else {
                    Brush.linearGradient(colors = listOf(Mist, Mist))
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (leadingIcon != null) leadingIcon()
            Text(
                text,
                color = if (enabled) Cloud else InkTextSoft,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun AppDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dangerBtnScale"
    )
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .background(Cloud)
            .border(0.8.dp, HairlineStrong, RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(),
            color = PwrNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Suppress("UNUSED")
private val UnusedShadowSoft = ShadowSoft
