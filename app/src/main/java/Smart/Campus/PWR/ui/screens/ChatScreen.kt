package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.chat.AttachmentContract
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.Chip
import Smart.Campus.PWR.ui.components.softindigo.ImagePlaceholder
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.softindigo.SearchField
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.StatusDot
import Smart.Campus.PWR.ui.components.softindigo.cardShadow
import Smart.Campus.PWR.ui.components.softindigo.primShadow
import Smart.Campus.PWR.ui.components.softindigo.softShadow
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.ChatAttachmentUi
import Smart.Campus.PWR.ui.state.ChatInboxFilter
import Smart.Campus.PWR.ui.state.ConversationUi
import Smart.Campus.PWR.ui.state.CourseUi
import Smart.Campus.PWR.ui.state.MessageDeliveryState
import Smart.Campus.PWR.ui.state.MessageUi
import Smart.Campus.PWR.ui.state.PendingAttachmentUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.util.readPickedFile
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.AmberBg
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Line2
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.White
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// ─── ChatTab ─────────────────────────────────────────────────────────────────

@Composable
fun ChatTab(
    state: SmartCampusUiState,
    onOpenDirect: (String) -> Unit,
    onOpenDirectWith: (String, String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onManageCourses: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (ChatInboxFilter) -> Unit
) {
    val chat = state.chat
    val query = chat.searchQuery.trim()
    val enrolledOrOwned = state.dashboardState.courseCatalog
        .filter { it.isEnrolled || it.isOwner }
        .sortedByDescending { it.lastMessageAtMillis }
    val courseUnread = enrolledOrOwned.sumOf { it.unreadCount }
    val directUnread = chat.directUnreadCount
    val visibleCourses = enrolledOrOwned
        .filter { course ->
            chat.inboxFilter == ChatInboxFilter.ALL ||
                chat.inboxFilter == ChatInboxFilter.COURSES ||
                (chat.inboxFilter == ChatInboxFilter.UNREAD && course.hasUnread)
        }
        .filter { course ->
            matchesChatQuery(query, course.name, course.subject, course.tutorDisplayName, course.lastMessageText)
        }
    val visibleDirect = chat.directConversations
        .filter { conversation ->
            chat.inboxFilter == ChatInboxFilter.ALL ||
                chat.inboxFilter == ChatInboxFilter.DIRECT ||
                (chat.inboxFilter == ChatInboxFilter.UNREAD && conversation.hasUnread)
        }
        .filter { conversation ->
            matchesChatQuery(query, conversation.title, conversation.lastMessageText, conversation.lastMessageSenderName)
        }
    val visibleContacts = chat.contacts.filter { contact ->
        matchesChatQuery(query, contact.displayName, contact.subtitle)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        // Top bar (search lives in the field below)
        SoftTopBar(title = "Messages")

        MessageBlock(state.errorMessage, state.infoMessage)

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ALL maps directly; UNREAD maps directly; DIRECT and COURSES map directly
            Chip(
                text = "All",
                selected = chat.inboxFilter == ChatInboxFilter.ALL,
                onClick = { onFilterChanged(ChatInboxFilter.ALL) }
            )
            val totalUnread = courseUnread + directUnread
            Chip(
                text = if (totalUnread > 0) "Unread · $totalUnread" else "Unread",
                selected = chat.inboxFilter == ChatInboxFilter.UNREAD,
                onClick = { onFilterChanged(ChatInboxFilter.UNREAD) }
            )
            // "Tutors" → DIRECT (closest match in the existing filter enum)
            Chip(
                text = "Tutors",
                selected = chat.inboxFilter == ChatInboxFilter.DIRECT,
                onClick = { onFilterChanged(ChatInboxFilter.DIRECT) }
            )
        }

        // Search field
        SearchField(
            value = chat.searchQuery,
            onValueChange = onSearchChanged,
            placeholder = "Search chats, courses, tutors",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Course channels section (hidden when DIRECT filter active)
            if (chat.inboxFilter != ChatInboxFilter.DIRECT) {
                item {
                    SiSectionHead(
                        title = "Course channels",
                        unread = courseUnread,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                if (visibleCourses.isEmpty()) {
                    item {
                        Text(
                            text = if (query.isBlank()) "No course channels here." else "No matching course channels.",
                            color = Ink3,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
                items(visibleCourses.size) { idx ->
                    val course = visibleCourses[idx]
                    CourseConversationRow(
                        course = course,
                        isLast = idx == visibleCourses.lastIndex && chat.inboxFilter == ChatInboxFilter.COURSES,
                        onClick = { onOpenCourse(course.id) }
                    )
                }
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        SoftButton(
                            text = "Manage courses",
                            onClick = onManageCourses,
                            variant = SoftButtonVariant.Soft,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Direct messages section (hidden when COURSES filter active)
            if (chat.inboxFilter != ChatInboxFilter.COURSES) {
                item {
                    SiSectionHead(
                        title = "Direct messages",
                        unread = directUnread,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                if (visibleDirect.isEmpty()) {
                    item {
                        Text(
                            text = if (query.isBlank()) "No direct conversations here." else "No matching direct messages.",
                            color = Ink3,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
                items(visibleDirect.size) { idx ->
                    val conversation = visibleDirect[idx]
                    DirectConversationRow(
                        conversation = conversation,
                        isLast = idx == visibleDirect.lastIndex,
                        onClick = { onOpenDirect(conversation.id) }
                    )
                }
            }

            // Contacts / Start a conversation
            if (chat.inboxFilter != ChatInboxFilter.COURSES && chat.contacts.isNotEmpty()) {
                item {
                    SiSectionHead(
                        title = "Start a conversation",
                        unread = 0,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                val contactsToShow = visibleContacts.take(8)
                if (contactsToShow.isEmpty()) {
                    item {
                        Text(
                            "No matching contacts.",
                            color = Ink3,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
                items(contactsToShow.size) { idx ->
                    val contact = contactsToShow[idx]
                    val isLast = idx == contactsToShow.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ) { onOpenDirectWith(contact.uid, contact.displayName) }
                            .padding(horizontal = 20.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        InitialsAvatar(name = contact.displayName, size = 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                contact.displayName,
                                color = InkToken,
                                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                            )
                            if (contact.subtitle.isNotBlank()) {
                                Text(
                                    contact.subtitle,
                                    color = Ink3,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(SoftIcons.arrow, contentDescription = null, tint = Ink3, modifier = Modifier.size(18.dp))
                    }
                    if (!isLast) {
                        HorizontalDivider(
                            color = Line,
                            modifier = Modifier.padding(start = 77.dp, end = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── ConversationScreen ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    state: SmartCampusUiState,
    onBack: () -> Unit,
    onComposerChanged: (String) -> Unit,
    onSend: () -> Unit,
    onAnnouncementToggle: (Boolean) -> Unit,
    onRetryMessage: (String) -> Unit,
    onLoadOlderMessages: () -> Unit,
    onConversationSearchChanged: (String) -> Unit,
    onAttachmentSelected: (String, String, String, Long) -> Unit,
    onClearAttachment: () -> Unit,
    onReplyToMessage: (String) -> Unit,
    onClearReply: () -> Unit,
    onReactToMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onReportMessage: (String) -> Unit,
    onClearNewMessageHint: () -> Unit,
    onOpenMaterials: () -> Unit = {}
) {
    val chat = state.chat
    val myUid = state.currentUser?.uid.orEmpty()
    val isCourse = chat.activeCourseId != null
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scrollScope = rememberCoroutineScope()
    var pickError by remember { mutableStateOf<String?>(null) }
    var selectedMessage by remember { mutableStateOf<MessageUi?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboard = LocalClipboardManager.current

    val filteredMessages = remember(chat.messages, chat.conversationSearchQuery) {
        val q = chat.conversationSearchQuery.trim()
        if (q.isBlank()) {
            chat.messages
        } else {
            chat.messages.filter { message ->
                matchesChatQuery(
                    q,
                    message.senderName,
                    message.text,
                    message.attachment?.fileName.orEmpty(),
                    message.replyTo?.previewLabel.orEmpty()
                )
            }
        }
    }

    val isNearBottom by remember(listState, filteredMessages.size) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            filteredMessages.isEmpty() || lastVisible >= filteredMessages.lastIndex - 2
        }
    }
    val lastMessageId = chat.messages.lastOrNull()?.id.orEmpty() + chat.messages.lastOrNull()?.clientMessageId.orEmpty()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val file = context.readPickedFile(uri)
            val error = if (file == null) {
                "Could not read selected file."
            } else {
                AttachmentContract.validationError(file.fileName, file.mimeType, file.sizeBytes)
            }
            if (file != null && error == null) {
                pickError = null
                onAttachmentSelected(uri.toString(), file.fileName, file.mimeType, file.sizeBytes)
            } else {
                pickError = error
            }
        }
    }

    LaunchedEffect(listState, chat.canLoadOlder, chat.isLoadingOlder) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (index <= 2 && chat.canLoadOlder && !chat.isLoadingOlder) {
                    onLoadOlderMessages()
                }
            }
    }

    LaunchedEffect(lastMessageId) {
        val lastMessage = chat.messages.lastOrNull()
        if (filteredMessages.isNotEmpty() && (isNearBottom || lastMessage?.senderUid == myUid)) {
            listState.animateScrollToItem(filteredMessages.lastIndex)
            onClearNewMessageHint()
        }
    }

    LaunchedEffect(isNearBottom) {
        if (isNearBottom && chat.hasNewMessages) {
            onClearNewMessageHint()
        }
    }

    // Message actions bottom sheet (long-press)
    selectedMessage?.let { message ->
        ModalBottomSheet(
            onDismissRequest = { selectedMessage = null },
            sheetState = sheetState,
            containerColor = CardSurface
        ) {
            MessageActionsSheet(
                message = message,
                mine = message.senderUid == myUid,
                onReply = {
                    onReplyToMessage(message.id)
                    selectedMessage = null
                },
                onCopy = {
                    clipboard.setText(AnnotatedString(message.bodyPreview))
                    selectedMessage = null
                },
                onRetry = {
                    onRetryMessage(message.clientMessageId)
                    selectedMessage = null
                },
                onDelete = {
                    onDeleteMessage(message.id)
                    selectedMessage = null
                },
                onReport = {
                    onReportMessage(message.id)
                    selectedMessage = null
                },
                onReact = { reaction ->
                    onReactToMessage(message.id, reaction)
                    selectedMessage = null
                }
            )
        }
    }

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
                .background(CardSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            InitialsAvatar(name = chat.activeTitle, size = 40.dp, online = !isCourse)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.activeTitle,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = InkToken
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = chat.activeSubtitle.ifBlank {
                    if (isCourse) "Course channel" else "Online · replies fast"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isCourse) StatusDot(color = Green)
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = if (!isCourse) Green else Ink3
                        )
                    )
                }
            }
            if (isCourse) {
                // Shared course file library (visible to tutor and every member)
                SoftIconButton(icon = SoftIcons.doc, onClick = onOpenMaterials)
            }
        }

        // ── Message list ─────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (chat.isLoadingOlder) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Primary, strokeWidth = 2.dp)
                        }
                    }
                } else if (chat.canLoadOlder && filteredMessages.isNotEmpty()) {
                    item {
                        TextButton(onClick = onLoadOlderMessages, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Load older messages",
                                color = Primary600,
                                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            )
                        }
                    }
                }

                if (filteredMessages.isEmpty()) {
                    item {
                        Text(
                            text = when {
                                chat.conversationSearchQuery.isNotBlank() -> "No loaded messages match this search."
                                isCourse -> "No course messages yet."
                                else -> "No direct messages yet."
                            },
                            color = Ink3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp)
                        )
                    }
                }

                items(filteredMessages.size) { index ->
                    val message = filteredMessages[index]
                    val previous = filteredMessages.getOrNull(index - 1)
                    val next = filteredMessages.getOrNull(index + 1)
                    val dateLabel = message.dateSeparatorLabel()
                    val showDate = previous?.dateSeparatorLabel() != dateLabel
                    val grouped = previous?.senderUid == message.senderUid &&
                        previous.dateSeparatorLabel() == dateLabel &&
                        !message.isAnnouncement &&
                        !previous.isAnnouncement
                    val followedBySame = next?.senderUid == message.senderUid &&
                        next.dateSeparatorLabel() == dateLabel &&
                        !message.isAnnouncement &&
                        !next.isAnnouncement
                    if (showDate) SiDateSeparator(dateLabel)
                    SiMessageBubble(
                        message = message,
                        mine = message.senderUid == myUid,
                        grouped = grouped,
                        followedBySame = followedBySame,
                        highlighted = message.id == chat.highlightedMessageId,
                        onRetry = { onRetryMessage(message.clientMessageId) },
                        onLongPress = { selectedMessage = message },
                        onOpenAttachment = { attachment -> openAttachment(context, attachment) }
                    )
                }

                if (chat.typingText.isNotBlank()) {
                    item { SiTypingLine(chat.typingText) }
                }
            }

            // Scroll-to-bottom FAB
            if (chat.hasNewMessages && !isNearBottom) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .softShadow(CircleShape)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = White)
                        ) {
                            if (filteredMessages.isNotEmpty()) {
                                scrollScope.launch {
                                    listState.animateScrollToItem(filteredMessages.lastIndex)
                                    onClearNewMessageHint()
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                        Text(
                            "New messages",
                            color = White,
                            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )
                    }
                }
            }
        }

        // ── Composer ─────────────────────────────────────────────────────────
        SiComposerPanel(
            state = state,
            isCourse = isCourse,
            pickError = pickError,
            onPickAttachment = { launcher.launch(AttachmentContract.allowedMimeTypes.toTypedArray()) },
            onComposerChanged = onComposerChanged,
            onAnnouncementToggle = onAnnouncementToggle,
            onClearAttachment = onClearAttachment,
            onClearReply = onClearReply,
            onSend = onSend
        )
    }
}

// ─── Private composables ──────────────────────────────────────────────────────

@Composable
private fun SiSectionHead(title: String, unread: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                color = Ink3,
                letterSpacing = 0.6.sp
            )
        )
        if (unread > 0) {
            SiUnreadBubble(unread)
        }
    }
}

@Composable
private fun CourseConversationRow(course: CourseUi, isLast: Boolean, onClick: () -> Unit) {
    val (subjBg, subjFg) = subjectColors(course.subject.ifBlank { course.name })
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        // Subject-colored rounded avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(subjBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(SoftIcons.cap, contentDescription = null, tint = subjFg, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = InkToken),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = course.lastMessageAtLabel,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = if (course.hasUnread) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = if (course.hasUnread) Primary600 else Ink3
                    )
                )
            }
            if (course.subject.isNotBlank()) {
                Text(
                    text = course.subject,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = subjFg),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (course.lastMessageText.isNotBlank())
                        "${course.lastMessageSenderName.ifBlank { "Course" }}: ${course.lastMessageText}"
                    else "${course.memberCount} enrolled",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = if (course.hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = if (course.hasUnread) InkToken else Ink3
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (course.hasUnread) {
                    Spacer(Modifier.size(8.dp))
                    SiUnreadBubble(course.unreadCount)
                }
            }
        }
    }
    if (!isLast) {
        HorizontalDivider(color = Line, modifier = Modifier.padding(start = 83.dp, end = 20.dp))
    }
}

@Composable
private fun DirectConversationRow(conversation: ConversationUi, isLast: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        InitialsAvatar(name = conversation.title, size = 50.dp, online = false)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.title,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = InkToken),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = conversation.lastMessageAtLabel,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = if (conversation.hasUnread) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = if (conversation.hasUnread) Primary600 else Ink3
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessageText.ifBlank { "No messages yet" },
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = if (conversation.hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = if (conversation.hasUnread) InkToken else Ink3
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (conversation.hasUnread) {
                    Spacer(Modifier.size(8.dp))
                    SiUnreadBubble(conversation.unreadCount)
                }
            }
        }
    }
    if (!isLast) {
        HorizontalDivider(color = Line, modifier = Modifier.padding(start = 83.dp, end = 20.dp))
    }
}

@Composable
private fun SiUnreadBubble(count: Int) {
    if (count <= 0) return
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            color = White,
            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp),
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun SiDateSeparator(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Badge(text = text, tone = BadgeTone.Gray)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SiMessageBubble(
    message: MessageUi,
    mine: Boolean,
    grouped: Boolean,
    followedBySame: Boolean,
    highlighted: Boolean,
    onRetry: () -> Unit,
    onLongPress: () -> Unit,
    onOpenAttachment: (ChatAttachmentUi) -> Unit
) {
    // Announcement style
    if (message.isAnnouncement) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (highlighted) AmberBg else AmberBg.copy(alpha = 0.7f))
                .border(1.dp, Amber.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusDot(color = Amber)
                Text(
                    text = "Announcement · ${message.senderName}",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = Amber,
                        letterSpacing = 0.4.sp
                    )
                )
            }
            SiMessageContent(message = message, mine = false, onOpenAttachment = onOpenAttachment)
            SiReactionStrip(message)
            SiMessageStatusRow(message = message, mine = false, onRetry = onRetry)
        }
        return
    }

    val bubbleShape = if (mine) {
        RoundedCornerShape(
            topStart = 18.dp, topEnd = if (grouped) 6.dp else 18.dp,
            bottomStart = 18.dp, bottomEnd = if (followedBySame) 6.dp else 5.dp
        )
    } else {
        RoundedCornerShape(
            topStart = if (grouped) 6.dp else 18.dp, topEnd = 18.dp,
            bottomStart = if (followedBySame) 6.dp else 5.dp, bottomEnd = 18.dp
        )
    }
    val bubbleBg = when {
        highlighted -> Primary100
        message.deliveryState == MessageDeliveryState.FAILED -> RedBg
        mine -> Primary
        else -> CardSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!mine && !grouped) {
                InitialsAvatar(name = message.senderName, size = 28.dp)
                Spacer(Modifier.size(6.dp))
            } else if (!mine) {
                Spacer(Modifier.size(34.dp))
            }
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .run { if (!mine) softShadow(bubbleShape) else this }
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .combinedClickable(onClick = {}, onLongClick = onLongPress)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!mine && !grouped) {
                    Text(
                        text = message.senderName,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Primary600
                        )
                    )
                }
                SiMessageContent(
                    message = message,
                    mine = mine && !highlighted,
                    onOpenAttachment = onOpenAttachment
                )
                SiReactionStrip(message)
                SiMessageStatusRow(message = message, mine = mine && !highlighted, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun SiMessageContent(
    message: MessageUi,
    mine: Boolean,
    onOpenAttachment: (ChatAttachmentUi) -> Unit
) {
    val bodyColor = when {
        message.deliveryState == MessageDeliveryState.FAILED -> Red
        mine -> White
        else -> InkToken
    }

    // Reply preview
    message.replyTo?.let { reply ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (mine) White.copy(alpha = 0.16f) else Bg2)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = reply.senderName,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (mine) White.copy(alpha = 0.82f) else Primary600
                )
            )
            Text(
                text = reply.previewLabel,
                color = if (mine) White.copy(alpha = 0.7f) else Ink2,
                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (message.isDeleted) {
        Text(
            "Message deleted",
            color = bodyColor.copy(alpha = 0.72f),
            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp, fontStyle = FontStyle.Italic)
        )
        return
    }

    message.attachment?.let { attachment ->
        SiAttachmentBlock(attachment = attachment, mine = mine, onClick = { onOpenAttachment(attachment) })
    }

    if (message.text.isNotBlank()) {
        Text(
            text = message.text,
            color = bodyColor,
            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp)
        )
    }
}

@Composable
private fun SiAttachmentBlock(attachment: ChatAttachmentUi, mine: Boolean, onClick: () -> Unit) {
    val source = attachment.downloadUrl.ifBlank { attachment.localUri }
    if (attachment.isImage && source.isNotBlank()) {
        AsyncImage(
            model = source,
            contentDescription = attachment.fileName,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 130.dp, max = 210.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
    } else if (attachment.isImage) {
        // No URL yet — show placeholder
        ImagePlaceholder(
            label = attachment.fileName.ifBlank { "Image" },
            height = 130.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (mine) White.copy(alpha = 0.14f) else Bg2)
                .clickable { onClick() }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.InsertDriveFile,
                contentDescription = null,
                tint = if (mine) White else Primary,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    attachment.fileName,
                    color = if (mine) White else InkToken,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                )
                Text(
                    attachment.sizeLabel,
                    color = if (mine) White.copy(alpha = 0.7f) else Ink3,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.5.sp)
                )
            }
        }
    }
}

@Composable
private fun SiReactionStrip(message: MessageUi) {
    if (message.reactionCounts.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
        message.reactionCounts.entries.sortedBy { it.key }.forEach { (reaction, count) ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Primary50)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "${reactionLabel(reaction)} $count",
                    color = Primary600,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                )
            }
        }
    }
}

@Composable
private fun SiMessageStatusRow(message: MessageUi, mine: Boolean, onRetry: () -> Unit) {
    val statusColor = when {
        message.deliveryState == MessageDeliveryState.FAILED -> Red
        mine -> White.copy(alpha = 0.7f)
        else -> Ink4
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 2.dp)
    ) {
        when (message.deliveryState) {
            MessageDeliveryState.SENDING -> CircularProgressIndicator(modifier = Modifier.size(10.dp), color = statusColor, strokeWidth = 1.5.dp)
            MessageDeliveryState.FAILED -> Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Red, modifier = Modifier.size(12.dp))
            MessageDeliveryState.READ -> Icon(Icons.Rounded.DoneAll, contentDescription = null, tint = Green, modifier = Modifier.size(12.dp))
            MessageDeliveryState.DELIVERED,
            MessageDeliveryState.SENT,
            MessageDeliveryState.POSTED -> Icon(Icons.Rounded.Done, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
            MessageDeliveryState.RECEIVED -> Unit
        }
        Text(
            text = message.deliveryLabel.ifBlank { message.sentAtLabel },
            color = statusColor,
            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
        )
        if (message.deliveryState == MessageDeliveryState.FAILED) {
            TextButton(onClick = onRetry, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(
                    "Retry",
                    color = Red,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                )
            }
        }
    }
}

@Composable
private fun SiTypingLine(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .softShadow(RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text,
                color = Ink3,
                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.5.sp)
            )
        }
    }
}

@Composable
private fun SiComposerPanel(
    state: SmartCampusUiState,
    isCourse: Boolean,
    pickError: String?,
    onPickAttachment: () -> Unit,
    onComposerChanged: (String) -> Unit,
    onAnnouncementToggle: (Boolean) -> Unit,
    onClearAttachment: () -> Unit,
    onClearReply: () -> Unit,
    onSend: () -> Unit
) {
    val chat = state.chat
    val canSend = (chat.composer.isNotBlank() || chat.pendingAttachment != null) && !chat.isSending

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Announcement toggle (course owner)
        if (isCourse && chat.activeIsOwner) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Switch(
                    checked = chat.announcementToggle,
                    onCheckedChange = onAnnouncementToggle
                )
                Text(
                    "Post as announcement",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (chat.announcementToggle) Amber else Ink3
                    )
                )
            }
        }

        // Reply draft
        chat.replyDraft?.let { reply ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary50)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null, tint = Primary600, modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Replying to ${reply.senderName}",
                        style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Primary600)
                    )
                    Text(
                        reply.previewLabel,
                        color = Ink2,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) { onClearReply() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(SoftIcons.x, contentDescription = "Cancel reply", tint = Ink3, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Pending attachment
        chat.pendingAttachment?.let { attachment ->
            SiPendingAttachmentBar(
                attachment = attachment,
                progress = chat.uploadProgress,
                onClear = onClearAttachment
            )
        }

        // Pick error
        if (pickError != null) {
            Text(
                pickError,
                color = Red,
                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp)
            )
        }

        // Composer row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftIconButton(icon = SoftIcons.attach, onClick = onPickAttachment)

            // Text input styled as search field
            Row(
                modifier = Modifier
                    .weight(1f)
                    .softShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Bg)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = chat.composer,
                        onValueChange = onComposerChanged,
                        maxLines = 4,
                        textStyle = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = InkToken
                        ),
                        cursorBrush = SolidColor(Primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (chat.composer.isEmpty()) {
                        Text(
                            text = if (isCourse) "Message this course" else "Message…",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = Ink4
                            )
                        )
                    }
                }
            }

            // Send button
            Box(
                modifier = Modifier
                    .run { if (canSend) primShadow(CircleShape) else softShadow(CircleShape) }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (canSend) Primary else Bg2)
                    .clickable(
                        enabled = canSend,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = White)
                    ) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                if (chat.isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Primary600, strokeWidth = 2.dp)
                } else {
                    Icon(
                        SoftIcons.send,
                        contentDescription = "Send",
                        tint = if (canSend) White else Ink3,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SiPendingAttachmentBar(
    attachment: PendingAttachmentUi,
    progress: Float?,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Bg2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (attachment.mimeType.startsWith("image/")) Icons.Rounded.Image else Icons.AutoMirrored.Rounded.InsertDriveFile,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    attachment.fileName,
                    color = InkToken,
                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatBytes(attachment.sizeBytes),
                    color = Ink3,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp)
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Icon(SoftIcons.x, contentDescription = "Remove", tint = Ink3, modifier = Modifier.size(14.dp))
            }
        }
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = Primary,
                trackColor = Line2
            )
        }
    }
}

@Composable
private fun MessageActionsSheet(
    message: MessageUi,
    mine: Boolean,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onReact: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Reaction row
        Text(
            "React",
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = Ink3,
                letterSpacing = 0.5.sp
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetReactionChip("Like", selected = message.myReaction == "like") { onReact("like") }
            SheetReactionChip("Thanks", selected = message.myReaction == "thanks") { onReact("thanks") }
            SheetReactionChip("+1", selected = message.myReaction == "plus1") { onReact("plus1") }
            if (message.myReaction.isNotBlank()) {
                SheetReactionChip("Clear", selected = false) { onReact("") }
            }
        }
        HorizontalDivider(color = Line2, modifier = Modifier.padding(vertical = 4.dp))
        SheetAction(icon = SoftIcons.back, text = "Reply", onClick = onReply) // reuse back as stand-in arrow
        SheetAction(
            iconMat = Icons.Rounded.ContentCopy,
            text = "Copy",
            onClick = onCopy,
            enabled = message.bodyPreview.isNotBlank()
        )
        if (message.deliveryState == MessageDeliveryState.FAILED) {
            SheetAction(iconMat = Icons.Rounded.ErrorOutline, text = "Retry send", onClick = onRetry)
        }
        if (mine && !message.id.startsWith("local_") && !message.isDeleted) {
            SheetAction(iconMat = Icons.Rounded.DeleteOutline, text = "Delete for everyone", onClick = onDelete, danger = true)
        }
        if (!mine && !message.id.startsWith("local_")) {
            SheetAction(iconMat = Icons.Rounded.Flag, text = "Report message", onClick = onReport, danger = true)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SheetReactionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Primary100 else Bg2)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Primary600 else Ink2,
            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        )
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconMat: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    danger: Boolean = false
) {
    val color = if (danger) Red else InkToken
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = ripple()) { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val tint = if (enabled) color else Ink4
        val effectiveIcon = iconMat ?: icon
        if (effectiveIcon != null) {
            Icon(effectiveIcon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(
            text,
            color = if (enabled) color else Ink4,
            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        )
    }
}

// ─── Utility ──────────────────────────────────────────────────────────────────

private fun openAttachment(context: Context, attachment: ChatAttachmentUi) {
    val source = attachment.downloadUrl.ifBlank { attachment.localUri }
    if (source.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(source), attachment.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}

private fun MessageUi.dateSeparatorLabel(): String =
    sentAtLabel.substringBeforeLast(",", sentAtLabel).ifBlank { "Now" }

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
    bytes >= 1024 -> "${bytes / 1024} KB"
    bytes > 0 -> "$bytes B"
    else -> "Unknown size"
}

private fun reactionLabel(key: String): String = when (key) {
    "like" -> "Like"
    "thanks" -> "Thanks"
    "plus1" -> "+1"
    else -> key
}

private fun matchesChatQuery(query: String, vararg values: String): Boolean {
    if (query.isBlank()) return true
    val normalized = query.lowercase()
    return values.any { it.lowercase().contains(normalized) }
}
