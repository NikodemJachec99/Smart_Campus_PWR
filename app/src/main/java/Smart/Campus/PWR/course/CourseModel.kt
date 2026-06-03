package Smart.Campus.PWR.course

data class Course(
    val id: String = "",
    val name: String = "",
    val subject: String = "",
    val description: String = "",
    val tutorUid: String = "",
    val tutorDisplayName: String = "",
    val memberCount: Int = 0,
    val createdAt: Long = 0L
)

data class CourseMember(
    val studentUid: String = "",
    val displayName: String = "",
    val enrolledAt: Long = 0L
)
