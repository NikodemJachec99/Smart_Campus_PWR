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

data class CourseMaterialUi(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storagePath: String,
    val downloadUrl: String,
    val uploaderUid: String,
    val uploaderName: String,
    val createdAtLabel: String,
    val createdAtMillis: Long = 0L
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
