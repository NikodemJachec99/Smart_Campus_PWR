package Smart.Campus.PWR.assignments

data class Assignment(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val dueDate: String = "",
    val dueAt: Long = 0L,
    val tutorUid: String = "",
    val tutorDisplayName: String = "",
    val createdAt: Long = 0L
)

data class AssignmentSubmission(
    val id: String = "",
    val assignmentId: String = "",
    val studentUid: String = "",
    val studentDisplayName: String = "",
    val fileName: String = "",
    val storagePath: String = "",
    val downloadUrl: String = "",
    val submittedAt: Long = 0L,
    val status: String = "submitted"
)
