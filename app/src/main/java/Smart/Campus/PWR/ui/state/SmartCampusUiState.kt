package Smart.Campus.PWR.ui.state

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole

enum class AppScreen {
    ONBOARDING,
    LOGIN,
    REGISTER,
    MAIN_SHELL,
    ADMIN_PANEL
}

data class RegisterFormState(
    val login: String = "",
    val password: String = "",
    val displayName: String = "",
    val student: Boolean = true,
    val tutor: Boolean = false
)

data class CreateUserFormState(
    val login: String = "",
    val password: String = "",
    val displayName: String = "",
    val admin: Boolean = false,
    val student: Boolean = true,
    val tutor: Boolean = false
)

data class AvailabilityFormState(
    val subject: String = "",
    val date: String = "",
    val startHour: String = "",
    val endHour: String = "",
    val durationMinutes: Int = 60,
    val format: String = "ONLINE",
    val location: String = "",
    val meetingUrl: String = "",
    val topic: String = ""
)

data class TutorSearchFilterState(
    /** Free-text search matching subject OR tutor name. */
    val query: String = "",
    val tutorQuery: String = "",
    val subjectQuery: String = "",
    val date: String = "",
    val format: String? = null,
    val minRating: Int? = null,
    val availableToday: Boolean = false,
    val sort: String = "TOP_RATED"
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || tutorQuery.isNotBlank() || subjectQuery.isNotBlank() || date.isNotBlank()
}

data class ReviewFormState(
    val tutorUid: String = "",
    val bookingId: String = "",
    val rating: String = "5",
    val comment: String = "",
    val selectedTags: List<String> = emptyList(),
    val anonymous: Boolean = false
)

data class ReportFormState(
    val tutorUid: String = "",
    val reason: String = "",
    val details: String = "",
    val severity: String = "MEDIUM"
)

data class ProfileEditState(
    val bio: String = "",
    val subjects: String = "",
    val experienceYears: String = "",
    val program: String = "",
    val studyYear: String = "",
    val faculty: String = ""
)

data class BookingRequestState(
    val slotId: String = "",
    val message: String = "",
    val topic: String = ""
)

data class AdminUserInspectorUi(
    val user: AppUser,
    val availability: List<TutorAvailabilityUi> = emptyList(),
    val bookingsAsTutor: List<LessonBookingUi> = emptyList(),
    val bookingsAsStudent: List<LessonBookingUi> = emptyList(),
    val reviewsReceived: List<TutorReviewUi> = emptyList(),
    val reviewsWritten: List<TutorReviewUi> = emptyList(),
    val reportsReceived: List<TutorReportUi> = emptyList(),
    val reportsWritten: List<TutorReportUi> = emptyList(),
    val isLoading: Boolean = false
)

data class SmartCampusUiState(
    val isBootstrapping: Boolean = true,
    val isBusy: Boolean = false,
    val screen: AppScreen = AppScreen.ONBOARDING,
    val currentUser: AppUser? = null,
    val activeRole: UserRole? = null,
    val loginInput: String = "",
    val passwordInput: String = "",
    val registerForm: RegisterFormState = RegisterFormState(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val adminUsers: List<AppUser> = emptyList(),
    val adminInspectors: Map<String, AdminUserInspectorUi> = emptyMap(),
    val isAdminUsersLoading: Boolean = false,
    val isAdminSubmitting: Boolean = false,
    val createUserForm: CreateUserFormState = CreateUserFormState(),
    val isMainSubmitting: Boolean = false,
    val availabilityForm: AvailabilityFormState = AvailabilityFormState(),
    val tutorSearchFilters: TutorSearchFilterState = TutorSearchFilterState(),
    val reviewForm: ReviewFormState = ReviewFormState(),
    val reportForm: ReportFormState = ReportFormState(),
    val assignmentForm: AssignmentFormState = AssignmentFormState(),
    val courseForm: CourseFormState = CourseFormState(),
    val chat: ChatUiState = ChatUiState(),
    val dashboardState: DashboardUiState = DashboardUiState(),
    val profileEdit: ProfileEditState = ProfileEditState(),
    val bookingRequest: BookingRequestState = BookingRequestState(),
    val rescheduleTargetBookingId: String? = null
)
