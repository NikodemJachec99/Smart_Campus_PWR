package Smart.Campus.PWR.ui.state

data class CourseUi(
    val id: String,
    val name: String,
    val subject: String,
    val description: String,
    val tutorUid: String,
    val tutorDisplayName: String,
    val memberCount: Int,
    val lastMessageText: String = "",
    val lastMessageAtLabel: String = "",
    val lastMessageAtMillis: Long = 0L,
    val lastMessageSenderName: String = "",
    val lastMessageSenderUid: String = "",
    val currentUserReadAtMillis: Long = 0L,
    val unreadCount: Int = 0,
    val isEnrolled: Boolean = false,
    val isOwner: Boolean = false
) {
    val hasUnread: Boolean
        get() = unreadCount > 0
}

data class CourseMemberUi(
    val studentUid: String,
    val displayName: String,
    val enrolledAtLabel: String
)

data class CourseFormState(
    val name: String = "",
    val subject: String = "",
    val description: String = ""
)
