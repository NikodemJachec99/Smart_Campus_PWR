package Smart.Campus.PWR.ui.state

data class ConversationUi(
    val id: String,
    val title: String,
    val lastMessageText: String,
    val lastMessageAtLabel: String,
    val lastMessageAtMillis: Long,
    val otherUid: String = ""
)

data class MessageUi(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val isAnnouncement: Boolean,
    val sentAtLabel: String
)

data class ChatContactUi(
    val uid: String,
    val displayName: String,
    val subtitle: String
)

data class ChatUiState(
    val directConversations: List<ConversationUi> = emptyList(),
    val contacts: List<ChatContactUi> = emptyList(),
    val activeDirectId: String? = null,
    val activeCourseId: String? = null,
    val activeTitle: String = "",
    val activeIsOwner: Boolean = false,
    val messages: List<MessageUi> = emptyList(),
    val composer: String = "",
    val announcementToggle: Boolean = false,
    val isSending: Boolean = false
)
