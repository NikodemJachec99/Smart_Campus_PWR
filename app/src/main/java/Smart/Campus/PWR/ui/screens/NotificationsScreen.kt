package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.NotificationUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.SoftType
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(
    state: SmartCampusUiState,
    onBack: () -> Unit,
    onOpen: (NotificationUi) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val notifications = state.dashboardState.notifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            Text(
                text = "Notifications",
                style = SoftType.h3,
                color = InkToken,
                modifier = Modifier.weight(1f)
            )
            if (notifications.any { !it.read }) {
                SoftButton(
                    text = "Mark all read",
                    onClick = onMarkAllRead,
                    variant = SoftButtonVariant.Outline,
                    size = SoftButtonSize.Sm
                )
            }
        }

        // ── List ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notifications.isEmpty()) {
                Text(
                    text = "No notifications yet. Booking updates, course messages and deadlines will land here.",
                    style = SoftType.bodySm,
                    color = Ink3,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
            notifications.forEach { notification ->
                NotificationCard(notification = notification, onClick = { onOpen(notification) })
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationUi, onClick: () -> Unit) {
    CardQ(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        padding = 14.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.typeIcon(),
                    contentDescription = null,
                    tint = Primary600,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = SoftType.title,
                        color = InkToken,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = notification.createdAtLabel,
                        style = SoftType.meta,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (notification.body.isNotBlank()) {
                    Text(
                        text = notification.body,
                        style = SoftType.bodySm,
                        color = Ink3,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
        }
    }
}

private fun NotificationUi.typeIcon(): ImageVector = when (type.lowercase()) {
    "chat" -> SoftIcons.chat
    "announcement" -> SoftIcons.bell
    "assignment", "deadline" -> SoftIcons.task
    "booking", "booking_request" -> SoftIcons.calendar
    else -> SoftIcons.bell
}
