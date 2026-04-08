package Smart.Campus.PWR.ui.state

enum class DashboardTab(val route: String, val label: String) {
    HOME(DashboardRoutes.HOME, "Home"),
    CALENDAR(DashboardRoutes.CALENDAR, "Calendar"),
    REVIEWS(DashboardRoutes.REVIEWS, "Reviews"),
    PROFILE(DashboardRoutes.PROFILE, "Profile")
}

object DashboardRoutes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val REVIEWS = "reviews"
    const val PROFILE = "profile"
}

data class TutorAvailabilityUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val subject: String,
    val dateLabel: String,
    val startHour: Int,
    val endHour: Int,
    val isBooked: Boolean
) {
    val timeLabel: String
        get() = "%02d:00 - %02d:00".format(startHour, endHour)
}

data class LessonBookingUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val subject: String,
    val dateLabel: String,
    val startHour: Int,
    val endHour: Int,
    val status: String
) {
    val timeLabel: String
        get() = "%02d:00 - %02d:00".format(startHour, endHour)
}

data class TutorReviewUi(
    val id: String,
    val tutorId: String,
    val tutorDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val rating: Int,
    val comment: String,
    val createdAtLabel: String
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
    val adminReports: List<TutorReportUi> = emptyList()
)