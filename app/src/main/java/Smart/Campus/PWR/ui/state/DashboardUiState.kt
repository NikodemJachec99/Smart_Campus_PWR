package Smart.Campus.PWR.ui.state

enum class DashboardTab(val route: String, val label: String) {
    HOME(DashboardRoutes.HOME, "Home"),
    CALENDAR(DashboardRoutes.CALENDAR, "Calendar"),
    ASSIGNMENTS(DashboardRoutes.ASSIGNMENTS, "Tasks"),
    CHAT(DashboardRoutes.CHAT, "Chat"),
    REVIEWS(DashboardRoutes.REVIEWS, "Reviews"),
    PROFILE(DashboardRoutes.PROFILE, "Profile"),
    LESSONS(DashboardRoutes.LESSONS, "Lessons")
}

object DashboardRoutes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val ASSIGNMENTS = "assignments"
    const val CHAT = "chat"
    const val REVIEWS = "reviews"
    const val PROFILE = "profile"
    const val COURSES = "courses"
    const val CONVERSATION = "conversation"
    const val NOTIFICATIONS = "notifications"
    const val LESSONS = "lessons"
    const val EARNINGS = "earnings"
}

data class TutorAvailabilityUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val subject: String,
    val dateLabel: String,
    val startHour: String,
    val endHour: String,
    val isBooked: Boolean
) {
    val timeLabel: String
        get() = "$startHour - $endHour"
}

data class LessonBookingUi(
    val id: String,
    val availabilityId: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val subject: String,
    val dateLabel: String,
    val startHour: String,
    val endHour: String,
    val status: String,
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val cancelledAtLabel: String = ""
) {
    val timeLabel: String
        get() = "$startHour - $endHour"
}

data class TutorReviewUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val rating: Int,
    val comment: String,
    val createdAtLabel: String,
    val bookingId: String = "",
    val reviewType: String = "general",
    val subject: String = "",
    val lessonDateLabel: String = "",
    val lessonTimeLabel: String = ""
)

data class TutorReportUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val reason: String,
    val details: String,
    val status: String,
    val createdAtLabel: String
)

data class TutorSummaryUi(
    val uid: String,
    val displayName: String,
    val subjects: String
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val notificationCount: Int = 0,
    val myStudentBookings: List<LessonBookingUi> = emptyList(),
    val myTutorBookings: List<LessonBookingUi> = emptyList(),
    val myAvailability: List<TutorAvailabilityUi> = emptyList(),
    val availableTutorSlots: List<TutorAvailabilityUi> = emptyList(),
    val tutors: List<TutorSummaryUi> = emptyList(),
    val reviewsByMe: List<TutorReviewUi> = emptyList(),
    val reviewsForMe: List<TutorReviewUi> = emptyList(),
    val reportsByMe: List<TutorReportUi> = emptyList(),
    val adminReports: List<TutorReportUi> = emptyList(),
    val assignments: List<AssignmentUi> = emptyList(),
    val mySubmissions: List<SubmissionUi> = emptyList(),
    val submissionsByAssignment: Map<String, List<SubmissionUi>> = emptyMap(),
    val courseCatalog: List<CourseUi> = emptyList(),
    val visibleCourseIds: List<String> = emptyList(),
    val rosterByCourse: Map<String, List<CourseMemberUi>> = emptyMap(),
    val notifications: List<NotificationUi> = emptyList()
)
