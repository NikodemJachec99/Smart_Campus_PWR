package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.DashboardTab
import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrNavyDeep
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop

private fun DashboardTab.icon(): ImageVector = when (this) {
    DashboardTab.HOME -> Icons.Rounded.Home
    DashboardTab.CALENDAR -> Icons.Rounded.CalendarMonth
    DashboardTab.LESSONS -> Icons.Rounded.CalendarMonth
    DashboardTab.ASSIGNMENTS -> Icons.AutoMirrored.Rounded.Assignment
    DashboardTab.CHAT -> Icons.AutoMirrored.Rounded.Chat
    DashboardTab.REVIEWS -> Icons.AutoMirrored.Rounded.MenuBook
    DashboardTab.PROFILE -> Icons.Rounded.Person
}

private data class NavbarItem(
    val tab: DashboardTab,
    val icon: ImageVector,
    val label: String,
    val unreadCount: Int = 0
)

@Composable
fun LiquidGlassNavbar(
    currentRoute: String,
    activeRole: UserRole,
    backdrop: Backdrop,
    chatUnreadCount: Int = 0,
    modifier: Modifier = Modifier,
    onTabSelected: (DashboardTab) -> Unit
) {
    val navbarItems = remember(activeRole, chatUnreadCount) {
        DashboardTab.entries.map { tab ->
            NavbarItem(
                tab = tab,
                icon = tab.icon(),
                label = tab.labelFor(activeRole),
                unreadCount = if (tab == DashboardTab.CHAT) chatUnreadCount else 0
            )
        }
    }
    val pillItems = navbarItems.filterNot { it.tab == DashboardTab.PROFILE }
    val actionItem = navbarItems.first { it.tab == DashboardTab.PROFILE }

    // Apple Music: bar lifts ~2dp and gains shadow when content is scrolled underneath.
    val lifted = LocalScrollLifted.current.value
    val liftOffset by animateDpAsState(
        targetValue = if (lifted) (-2).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navbarLift"
    )
    val liftShadowAlpha by animateFloatAsState(
        targetValue = if (lifted) 1f else 0.55f,
        animationSpec = tween(280),
        label = "navbarShadowAlpha"
    )

    // Apple Music: faint inner glow pulse on tab change.
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(currentRoute) {
        pulse.snapTo(1f)
        pulse.animateTo(0f, animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing))
    }
    val baseGlow = 0.22f
    val pulsedGlow = baseGlow + pulse.value * 0.42f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp, bottom = 16.dp)
            .offset(y = liftOffset),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.widthIn(max = 460.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .shadow(
                        elevation = if (lifted) 28.dp else 22.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = PwrNavy.copy(alpha = liftShadowAlpha),
                        spotColor = PwrNavy.copy(alpha = liftShadowAlpha)
                    )
                    .stableLiquidGlassSurface(
                        backdrop = backdrop,
                        cornerRadius = 50.dp,
                        tint = Cloud,
                        tintAlpha = 0.30f,
                        glowColor = Cloud,
                        glowAlpha = pulsedGlow
                    )
                    .height(74.dp)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pillItems.forEach { item ->
                    NavbarPillItem(
                        item = item,
                        selected = item.tab.route == currentRoute,
                        onClick = { onTabSelected(item.tab) }
                    )
                }
            }

            NavbarActionButton(
                item = actionItem,
                selected = actionItem.tab.route == currentRoute,
                backdrop = backdrop,
                lifted = lifted,
                pulseAlpha = pulsedGlow,
                onClick = { onTabSelected(actionItem.tab) }
            )
        }
    }
}

private fun DashboardTab.labelFor(activeRole: UserRole): String = when (this) {
    DashboardTab.HOME -> if (activeRole == UserRole.TUTOR) "Today" else "Home"
    DashboardTab.CALENDAR -> if (activeRole == UserRole.TUTOR) "Slots" else "Discover"
    DashboardTab.ASSIGNMENTS -> "Tasks"
    DashboardTab.LESSONS -> "Lessons"
    DashboardTab.CHAT -> "Chat"
    DashboardTab.REVIEWS -> "Reviews"
    DashboardTab.PROFILE -> "You"
}

@Composable
private fun NavbarPillItem(
    item: NavbarItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navbarItemScale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Cloud else PwrNavy,
        label = "navbarIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) Cloud else PwrNavy.copy(alpha = 0.78f),
        label = "navbarLabelColor"
    )

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (selected) {
                    Modifier
                        .clip(RoundedCornerShape(38.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PwrNavy, PwrNavyDeep)
                            )
                        )
                } else {
                    Modifier.clip(RoundedCornerShape(38.dp))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                color = labelColor,
                fontSize = 9.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                lineHeight = 10.sp
            )
        }
        if (item.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 7.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC7572A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.unreadCount > 9) "9+" else item.unreadCount.toString(),
                    color = Cloud,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
private fun NavbarActionButton(
    item: NavbarItem,
    selected: Boolean,
    backdrop: Backdrop,
    lifted: Boolean,
    pulseAlpha: Float,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navbarActionScale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Cloud else PwrNavy,
        label = "navbarActionIconColor"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (lifted) 1f else 0.6f,
        animationSpec = tween(280),
        label = "navbarActionShadowAlpha"
    )

    val backgroundModifier = if (selected) {
        Modifier
            .size(66.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (lifted) 24.dp else 18.dp,
                shape = CircleShape,
                ambientColor = PwrNavy.copy(alpha = shadowAlpha),
                spotColor = PwrNavy.copy(alpha = shadowAlpha)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(colors = listOf(PwrNavy, PwrNavyDeep))
            )
    } else {
        Modifier
            .size(66.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (lifted) 24.dp else 18.dp,
                shape = CircleShape,
                ambientColor = PwrNavy.copy(alpha = shadowAlpha),
                spotColor = PwrNavy.copy(alpha = shadowAlpha)
            )
            .stableLiquidGlassSurface(
                backdrop = backdrop,
                cornerRadius = 50.dp,
                tint = Cloud,
                tintAlpha = 0.32f,
                glowColor = Cloud,
                glowAlpha = pulseAlpha.coerceAtLeast(0.25f)
            )
    }

    Box(
        modifier = backgroundModifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
