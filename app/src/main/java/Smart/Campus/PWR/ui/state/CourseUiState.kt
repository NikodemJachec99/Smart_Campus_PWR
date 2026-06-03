package Smart.Campus.PWR.ui.state

data class CourseUi(
    val id: String,
    val name: String,
    val subject: String,
    val description: String,
    val tutorUid: String,
    val tutorDisplayName: String,
    val memberCount: Int,
    val isEnrolled: Boolean = false,
    val isOwner: Boolean = false
)

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
