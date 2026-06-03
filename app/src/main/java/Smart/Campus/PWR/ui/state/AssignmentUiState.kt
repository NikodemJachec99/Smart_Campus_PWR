package Smart.Campus.PWR.ui.state

data class AssignmentUi(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val courseId: String,
    val courseName: String,
    val dueDateLabel: String,
    val dueAtMillis: Long?,
    val tutorUid: String,
    val tutorDisplayName: String,
    val createdAtLabel: String
)

data class SubmissionUi(
    val id: String,
    val assignmentId: String,
    val studentUid: String,
    val studentDisplayName: String,
    val fileName: String,
    val downloadUrl: String,
    val submittedAtLabel: String,
    val status: String
)

data class AssignmentFormState(
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val dueDate: String = "",
    val dueAtMillis: Long? = null
)
