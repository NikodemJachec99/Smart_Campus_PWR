package Smart.Campus.PWR.ui.state

data class ConversationUi(
    val id: String,
    val title: String,
    val lastMessageText: String,
    val lastMessageAtLabel: String,
    val lastMessageAtMillis: Long,
    val lastMessageSenderName: String = "",
    val lastMessageSenderUid: String = "",
    val currentUserReadAtMillis: Long = 0L,
    val otherReadAtMillis: Long = 0L,
    val unreadCount: Int = 0,
    val otherUid: String = ""
) {
    val hasUnread: Boolean
        get() = unreadCount > 0
}

enum class ChatInboxFilter {
    ALL,
    DIRECT,
    COURSES,
    UNREAD
}

enum class ChatMessageType {
    TEXT,
    IMAGE,
    FILE
}

enum class MessageDeliveryState {
    RECEIVED,
    SENDING,
    FAILED,
    SENT,
    DELIVERED,
    READ,
    POSTED
}

data class ChatAttachmentUi(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storagePath: String = "",
    val downloadUrl: String = "",
    val localUri: String = "",
    val width: Int? = null,
    val height: Int? = null
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val sizeLabel: String
        get() = when {
            sizeBytes >= 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
            sizeBytes >= 1024 -> "${sizeBytes / 1024} KB"
            sizeBytes > 0 -> "$sizeBytes B"
            else -> ""
        }
}

data class ChatReplyPreviewUi(
    val messageId: String,
    val senderName: String,
    val textPreview: String,
    val attachmentName: String = ""
) {
    val previewLabel: String
        get() = textPreview.ifBlank { attachmentName.ifBlank { "Attachment" } }
}

data class PendingAttachmentUi(
    val uri: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long
) {
    fun toAttachment(): ChatAttachmentUi =
        ChatAttachmentUi(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            localUri = uri
        )
}

data class MessageUi(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val isAnnouncement: Boolean,
    val sentAtLabel: String,
    val sentAtMillis: Long = 0L,
    val clientMessageId: String = "",
    val deliveryState: MessageDeliveryState = MessageDeliveryState.RECEIVED,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val attachment: ChatAttachmentUi? = null,
    val replyTo: ChatReplyPreviewUi? = null,
    val editedAtLabel: String = "",
    val deletedAtLabel: String = "",
    val reactionCounts: Map<String, Int> = emptyMap(),
    val myReaction: String = ""
) {
    val isDeleted: Boolean
        get() = deletedAtLabel.isNotBlank()

    val bodyPreview: String
        get() = when {
            isDeleted -> "Message deleted"
            text.isNotBlank() -> text.take(120)
            attachment?.isImage == true -> "Photo"
            attachment != null -> attachment.fileName
            else -> ""
        }

    val deliveryLabel: String
        get() = when (deliveryState) {
            MessageDeliveryState.RECEIVED -> sentAtLabel
            MessageDeliveryState.SENDING -> "Sending"
            MessageDeliveryState.FAILED -> "Not sent"
            MessageDeliveryState.SENT -> "Sent"
            MessageDeliveryState.DELIVERED -> "Delivered"
            MessageDeliveryState.READ -> "Read"
            MessageDeliveryState.POSTED -> "Posted"
        }
}

data class ChatContactUi(
    val uid: String,
    val displayName: String,
    val subtitle: String
)

data class ChatUiState(
    val directConversations: List<ConversationUi> = emptyList(),
    val contacts: List<ChatContactUi> = emptyList(),
    val searchQuery: String = "",
    val inboxFilter: ChatInboxFilter = ChatInboxFilter.ALL,
    val conversationSearchQuery: String = "",
    val activeDirectId: String? = null,
    val activeCourseId: String? = null,
    val activeTitle: String = "",
    val activeSubtitle: String = "",
    val activeIsOwner: Boolean = false,
    val messages: List<MessageUi> = emptyList(),
    val composer: String = "",
    val replyDraft: ChatReplyPreviewUi? = null,
    val highlightedMessageId: String = "",
    val isLoadingOlder: Boolean = false,
    val canLoadOlder: Boolean = false,
    val typingText: String = "",
    val pendingAttachment: PendingAttachmentUi? = null,
    val uploadProgress: Float? = null,
    val hasNewMessages: Boolean = false,
    val announcementToggle: Boolean = false,
    val isSending: Boolean = false
) {
    val directUnreadCount: Int
        get() = directConversations.sumOf { it.unreadCount }
}
