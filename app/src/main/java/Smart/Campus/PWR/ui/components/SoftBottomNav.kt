package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.navShadow
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.DashboardTab
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.White
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class SoftNavItem(
    val tab: DashboardTab,
    val label: String,
    val icon: ImageVector,
    val unread: Int = 0
)

/**
 * Flat Soft Indigo bottom navigation — full-width white bar, role-aware 5 tabs,
 * pill-highlighted active item. Replaces the liquid-glass bar.
 */
@Composable
fun SoftBottomNav(
    currentRoute: String,
    activeRole: UserRole,
    chatUnreadCount: Int = 0,
    modifier: Modifier = Modifier,
    onTabSelected: (DashboardTab) -> Unit
) {
    val items = if (activeRole == UserRole.TUTOR) {
        listOf(
            SoftNavItem(DashboardTab.HOME, "Today", SoftIcons.home),
            SoftNavItem(DashboardTab.CALENDAR, "Schedule", SoftIcons.calendar),
            SoftNavItem(DashboardTab.ASSIGNMENTS, "Tasks", SoftIcons.task),
            SoftNavItem(DashboardTab.CHAT, "Messages", SoftIcons.chat, chatUnreadCount),
            SoftNavItem(DashboardTab.PROFILE, "You", SoftIcons.user),
        )
    } else {
        listOf(
            SoftNavItem(DashboardTab.HOME, "Home", SoftIcons.home),
            SoftNavItem(DashboardTab.CALENDAR, "Find", SoftIcons.compass),
            SoftNavItem(DashboardTab.LESSONS, "Lessons", SoftIcons.calendar),
            SoftNavItem(DashboardTab.CHAT, "Messages", SoftIcons.chat, chatUnreadCount),
            SoftNavItem(DashboardTab.PROFILE, "You", SoftIcons.user),
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navShadow(RectangleShape)
            .background(CardSurface)
            .drawBehind {
                drawLine(
                    color = Line,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            SoftNavCell(
                item = item,
                selected = item.tab.route == currentRoute,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(item.tab) }
            )
        }
    }
}

@Composable
private fun SoftNavCell(
    item: SoftNavItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) Primary600 else Ink3
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(if (selected) Primary100 else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(21.dp)
            )
            if (item.unread > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp, end = 6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.unread > 9) "9+" else item.unread.toString(),
                        color = White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 9.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = item.label,
            color = tint,
            fontSize = 10.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            lineHeight = 11.sp
        )
    }
}
