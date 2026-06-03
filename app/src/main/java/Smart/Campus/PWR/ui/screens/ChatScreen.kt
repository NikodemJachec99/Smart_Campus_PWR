package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.IconText
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.PlainCard
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.state.MessageUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.CardWhite
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.ClaySoft
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.PwrIndigo
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatTab(
    state: SmartCampusUiState,
    onOpenDirect: (String) -> Unit,
    onOpenDirectWith: (String, String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onManageCourses: () -> Unit
) {
    val chat = state.chat
    val enrolledOrOwned = state.dashboardState.courseCatalog.filter { it.isEnrolled || it.isOwner }

    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)
        MonoLabel("Inbox")
        Text("Messages.", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)

        Text("Course channels", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        if (enrolledOrOwned.isEmpty()) {
            Text("Join or create a course to get its channel.", color = TextSecondary)
        }
        enrolledOrOwned.forEach { course ->
            PlainCard(accent = PwrIndigo) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenCourse(course.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(course.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        IconText(Icons.Rounded.School, if (course.subject.isNotBlank()) course.subject else "Course channel")
                    }
                    Icon(Icons.Rounded.Campaign, contentDescription = null, tint = PwrNavy)
                }
            }
        }
        AppPrimaryButton(text = "Manage courses", onClick = onManageCourses)

        Text("Direct messages", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        if (chat.directConversations.isEmpty()) {
            Text("No conversations yet. Start one below.", color = TextSecondary)
        }
        chat.directConversations.forEach { conversation ->
            PlainCard {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenDirect(conversation.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(conversation.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            conversation.lastMessageText.ifBlank { "No messages yet" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                    if (conversation.lastMessageAtLabel.isNotBlank()) {
                        Text(conversation.lastMessageAtLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        if (chat.contacts.isNotEmpty()) {
            SectionCard("Start a conversation") {
                chat.contacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDirectWith(contact.uid, contact.displayName) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.displayName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            if (contact.subtitle.isNotBlank()) {
                                Text(contact.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = PwrNavy, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationScreen(
    state: SmartCampusUiState,
    onBack: () -> Unit,
    onComposerChanged: (String) -> Unit,
    onSend: () -> Unit,
    onAnnouncementToggle: (Boolean) -> Unit
) {
    val chat = state.chat
    val myUid = state.currentUser?.uid
    val isCourse = chat.activeCourseId != null
    val listState = rememberLazyListState()

    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) listState.animateScrollToItem(chat.messages.size - 1)
    }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(chat.activeTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                MonoLabel(if (isCourse) "Course channel" else "Direct message")
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chat.messages.isEmpty()) {
                item {
                    Text(
                        "No messages yet. Say hello.",
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(chat.messages.size) { index ->
                MessageBubble(message = chat.messages[index], mine = chat.messages[index].senderUid == myUid)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (isCourse && chat.activeIsOwner) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Switch(checked = chat.announcementToggle, onCheckedChange = onAnnouncementToggle)
                    MonoLabel("Post as announcement", color = if (chat.announcementToggle) ClayAccent else InkTextSoft)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = chat.composer,
                    onValueChange = onComposerChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    maxLines = 4
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (chat.composer.isBlank() || chat.isSending) PaperLine else PwrNavy)
                        .clickable(enabled = chat.composer.isNotBlank() && !chat.isSending) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = CardWhite, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageUi, mine: Boolean) {
    if (message.isAnnouncement) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ClaySoft)
                .border(1.dp, ClayAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Campaign, contentDescription = null, tint = ClayAccent, modifier = Modifier.size(14.dp))
                MonoLabel("Announcement · ${message.senderName}", color = ClayAccent)
            }
            Text(message.text, color = InkText, style = MaterialTheme.typography.bodyMedium)
            Text(message.sentAtLabel, color = InkTextSoft, fontSize = 10.sp)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (mine) 16.dp else 4.dp,
                        bottomEnd = if (mine) 4.dp else 16.dp
                    )
                )
                .background(if (mine) PwrNavy else CardWhite)
                .border(
                    1.dp,
                    if (mine) PwrNavy else PaperLine,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!mine) {
                Text(message.senderName, color = PwrNavy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(message.text, color = if (mine) CardWhite else InkText, style = MaterialTheme.typography.bodyMedium)
            Text(
                message.sentAtLabel,
                color = if (mine) CardWhite.copy(alpha = 0.7f) else InkTextSoft,
                fontSize = 10.sp
            )
        }
    }
}
