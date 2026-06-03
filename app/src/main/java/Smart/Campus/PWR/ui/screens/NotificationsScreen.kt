package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.PlainCard
import Smart.Campus.PWR.ui.state.NotificationUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, PaperLine, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = InkText, modifier = Modifier.size(18.dp))
            }
            Text("Notifications", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (notifications.any { !it.read }) {
                AppDangerButton("Mark all read", onClick = onMarkAllRead)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            MainList {
                if (notifications.isEmpty()) {
                    Text("No notifications yet.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                notifications.forEach { notification ->
                    NotificationCard(notification = notification, onClick = { onOpen(notification) })
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationUi, onClick: () -> Unit) {
    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (notification.read) PaperLine else ClayAccent)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MonoLabel(notification.type)
                    Text(notification.createdAtLabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text(notification.title, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (notification.body.isNotBlank()) {
                    Text(notification.body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
