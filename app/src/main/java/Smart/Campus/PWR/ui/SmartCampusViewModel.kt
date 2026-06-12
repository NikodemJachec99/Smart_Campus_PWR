package Smart.Campus.PWR.ui

import Smart.Campus.PWR.assignments.AssignmentRepository
import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.FirebaseAuthRepository
import Smart.Campus.PWR.auth.SessionDestination
import Smart.Campus.PWR.auth.SessionRouter
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.chat.ChatAttachment
import Smart.Campus.PWR.chat.ChatConversation
import Smart.Campus.PWR.chat.ChatIds
import Smart.Campus.PWR.chat.ChatMessage
import Smart.Campus.PWR.chat.ChatReadState
import Smart.Campus.PWR.chat.ChatRepository
import Smart.Campus.PWR.chat.ChatReplyPreview
import Smart.Campus.PWR.chat.ChatThreadTarget
import Smart.Campus.PWR.chat.OutgoingChatAttachment
import Smart.Campus.PWR.course.CourseRepository
import Smart.Campus.PWR.notifications.FcmTokenRegistrar
import Smart.Campus.PWR.notifications.NotificationRepository
import Smart.Campus.PWR.notifications.NotificationUtil
import Smart.Campus.PWR.tutoring.TutoringRepository
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.AssignmentFormState
import Smart.Campus.PWR.ui.state.AssignmentUi
import Smart.Campus.PWR.ui.state.AvailabilityFormState
import Smart.Campus.PWR.ui.state.ChatAttachmentUi
import Smart.Campus.PWR.ui.state.ChatContactUi
import Smart.Campus.PWR.ui.state.ChatInboxFilter
import Smart.Campus.PWR.ui.state.ChatMessageType
import Smart.Campus.PWR.ui.state.ChatReplyPreviewUi
import Smart.Campus.PWR.ui.state.ChatUiState
import Smart.Campus.PWR.ui.state.ConversationUi
import Smart.Campus.PWR.ui.state.CourseFormState
import Smart.Campus.PWR.ui.state.CourseUi
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.MessageDeliveryState
import Smart.Campus.PWR.ui.state.MessageUi
import Smart.Campus.PWR.ui.state.PendingAttachmentUi
import Smart.Campus.PWR.ui.state.BookingRequestState
import Smart.Campus.PWR.ui.state.ProfileEditState
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.ReportFormState
import Smart.Campus.PWR.ui.state.ReviewFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorSearchFilterState
import com.google.firebase.firestore.ListenerRegistration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SmartCampusViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val tutoringRepository: TutoringRepository = TutoringRepository(),
    private val assignmentRepository: AssignmentRepository = AssignmentRepository(),
    private val courseRepository: CourseRepository = CourseRepository(),
    private val chatRepository: ChatRepository = ChatRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {
    private companion object {
        const val TAG = "SmartCampusViewModel"
    }

    private val _uiState = MutableStateFlow(SmartCampusUiState())
    val uiState: StateFlow<SmartCampusUiState> = _uiState.asStateFlow()
    private var tutoringRealtime: ListenerRegistration? = null
    private var assignmentsRealtime: ListenerRegistration? = null
    private var mySubmissionsRealtime: ListenerRegistration? = null
    private var coursesRealtime: ListenerRegistration? = null
    private var conversationsRealtime: ListenerRegistration? = null
    private var messagesRealtime: ListenerRegistration? = null
    private var typingRealtime: ListenerRegistration? = null
    private var materialsRealtime: ListenerRegistration? = null
    private var notificationsRealtime: ListenerRegistration? = null
    private var mySubmissionAssignmentIds: List<String> = emptyList()
    private val readReceiptInFlight = mutableSetOf<String>()
    private val readReceiptBackoffUntil = mutableMapOf<String, Long>()
    private var lastTypingSentAtMillis = 0L

    init {
        bootstrapSession()
    }

    private fun showRealtimeError(area: String, error: Throwable) {
        _uiState.update { it.copy(errorMessage = "$area: ${authRepository.userMessage(error)}") }
    }

    fun onLoginChanged(value: String) {
        _uiState.update { it.copy(loginInput = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value) }
    }

    fun onRegisterLoginChanged(value: String) {
        _uiState.update { it.copy(registerForm = it.registerForm.copy(login = value)) }
    }

    fun onRegisterPasswordChanged(value: String) {
        _uiState.update { it.copy(registerForm = it.registerForm.copy(password = value)) }
    }

    fun onRegisterDisplayNameChanged(value: String) {
        _uiState.update { it.copy(registerForm = it.registerForm.copy(displayName = value)) }
    }

    fun onRegisterStudentChecked(value: Boolean) {
        _uiState.update { it.copy(registerForm = it.registerForm.copy(student = value)) }
    }

    fun onRegisterTutorChecked(value: Boolean) {
        _uiState.update { it.copy(registerForm = it.registerForm.copy(tutor = value)) }
    }

    fun openRegister() {
        _uiState.update {
            it.copy(
                screen = AppScreen.REGISTER,
                errorMessage = null,
                infoMessage = null,
                registerForm = RegisterFormState()
            )
        }
    }

    fun openLogin() {
        _uiState.update {
            it.copy(
                screen = AppScreen.LOGIN,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun openOnboarding() {
        _uiState.update {
            it.copy(
                screen = AppScreen.ONBOARDING,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.loginInput.isBlank() || state.passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter login and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, infoMessage = null) }
            try {
                val user = authRepository.signIn(state.loginInput, state.passwordInput)
                applyRoutingForUser(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isBusy = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun register() {
        val form = _uiState.value.registerForm

        if (form.login.isBlank() || form.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Login and password are required.") }
            return
        }

        if (!form.student && !form.tutor) {
            _uiState.update { it.copy(errorMessage = "Select at least one role (Student or Tutor).") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, infoMessage = null) }
            try {
                val user = authRepository.register(
                    loginOrEmail = form.login,
                    password = form.password,
                    displayName = form.displayName,
                    student = form.student,
                    tutor = form.tutor
                )
                applyRoutingForUser(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isBusy = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun logout() {
        val uid = _uiState.value.currentUser?.uid
        viewModelScope.launch {
            if (uid != null) {
                runCatching { FcmTokenRegistrar.unregister(uid) }
            }
            stopRealtime()
            authRepository.signOut()
            _uiState.update {
                SmartCampusUiState(
                    isBootstrapping = false,
                    screen = AppScreen.ONBOARDING,
                    loginInput = it.loginInput,
                    passwordInput = "",
                    infoMessage = "Signed out."
                )
            }
        }
    }

    fun toggleActiveRole() {
        val state = _uiState.value
        val user = state.currentUser ?: return
        val currentRole = state.activeRole ?: return

        if (!user.hasDualRole()) {
            return
        }

        val nextRole = if (currentRole == UserRole.STUDENT) UserRole.TUTOR else UserRole.STUDENT
        _uiState.update {
            it.copy(activeRole = nextRole, infoMessage = "Switched to ${nextRole.displayName} mode.")
        }
    }

    fun refreshMainData() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        viewModelScope.launch {
            loadMainData(user)
        }
    }

    fun onAvailabilitySubjectChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(subject = value)) }
    }

    fun onAvailabilityDateChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(date = value)) }
    }

    fun onAvailabilityStartHourChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(startHour = value)) }
    }

    fun onAvailabilityEndHourChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(endHour = value)) }
    }

    fun onAvailabilityDurationChanged(value: Int) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(durationMinutes = value)) }
    }

    fun onAvailabilityFormatChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(format = value)) }
    }

    fun onAvailabilityLocationChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(location = value)) }
    }

    fun onAvailabilityMeetingUrlChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(meetingUrl = value)) }
    }

    fun onAvailabilityTopicChanged(value: String) {
        _uiState.update { it.copy(availabilityForm = it.availabilityForm.copy(topic = value)) }
    }

    fun onTutorSearchQueryChanged(value: String) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(query = value)) }
    }

    fun onTutorSearchTutorChanged(value: String) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(tutorQuery = value)) }
    }

    fun onTutorSearchSubjectChanged(value: String) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(subjectQuery = value)) }
    }

    fun onTutorSearchDateChanged(value: String) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(date = value)) }
    }

    fun onTutorSearchFormatChanged(value: String?) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(format = value)) }
    }

    fun onTutorSearchMinRatingChanged(value: Int?) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(minRating = value)) }
    }

    fun onTutorSearchAvailableTodayChanged(value: Boolean) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(availableToday = value)) }
    }

    fun onTutorSearchSortChanged(value: String) {
        _uiState.update { it.copy(tutorSearchFilters = it.tutorSearchFilters.copy(sort = value)) }
    }

    fun clearTutorSearchFilters() {
        _uiState.update { it.copy(tutorSearchFilters = TutorSearchFilterState()) }
    }

    fun addAvailability() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.TUTOR)) {
            _uiState.update { it.copy(errorMessage = "Only tutors can add availability.") }
            return
        }

        val form = state.availabilityForm
        val start = form.startHour.trim()
        val end = form.endHour.trim()

        if (start.isBlank() || end.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Start and end hour are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.createAvailability(
                    tutor = user,
                    subject = form.subject,
                    date = form.date,
                    startHour = start,
                    endHour = end,
                    durationMinutes = form.durationMinutes,
                    format = form.format,
                    location = form.location,
                    meetingUrl = form.meetingUrl,
                    topic = form.topic
                )
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        availabilityForm = AvailabilityFormState(),
                        infoMessage = "Availability slot added."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun deleteAvailabilitySlot(slotId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.deleteAvailability(slotId, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Availability slot removed.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun updateAvailabilitySlot(slotId: String, newSubject: String, newDate: String, newStartHour: String, newEndHour: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.updateAvailability(slotId, newSubject, newDate, newStartHour, newEndHour, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Availability slot updated.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun startBookingRequest(slotId: String) {
        _uiState.update { it.copy(bookingRequest = it.bookingRequest.copy(slotId = slotId)) }
    }

    fun onBookingRequestMessageChanged(value: String) {
        _uiState.update { it.copy(bookingRequest = it.bookingRequest.copy(message = value)) }
    }

    fun onBookingRequestTopicChanged(value: String) {
        _uiState.update { it.copy(bookingRequest = it.bookingRequest.copy(topic = value)) }
    }

    fun bookTutorSlot(slotId: String) {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can book lessons.") }
            return
        }

        val message = state.bookingRequest.message
        val topic = state.bookingRequest.topic

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.requestBooking(slotId, user, message, topic)
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        bookingRequest = BookingRequestState(),
                        infoMessage = "Booking request sent."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun cancelLessonBooking(bookingId: String, slotId: String, reason: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.cancelBooking(bookingId, slotId, reason, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Lesson cancelled.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun acceptBooking(bookingId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.acceptBooking(bookingId, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Booking accepted.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun declineBooking(bookingId: String, reason: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.declineBooking(bookingId, user, reason)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Booking declined.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun rescheduleBooking(bookingId: String, newSlotId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.rescheduleBooking(bookingId, newSlotId, user)
                _uiState.update {
                    it.copy(isMainSubmitting = false, rescheduleTargetBookingId = null, infoMessage = "Booking rescheduled.")
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun markLessonCompleted(bookingId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.markLessonCompleted(bookingId, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Lesson marked as completed.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun markNoShow(bookingId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.markNoShow(bookingId, user)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Lesson marked as no-show.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun startReschedule(bookingId: String) {
        _uiState.update { it.copy(rescheduleTargetBookingId = bookingId) }
    }

    fun clearReschedule() {
        _uiState.update { it.copy(rescheduleTargetBookingId = null) }
    }

    fun onReviewTutorChanged(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(tutorUid = value)) }
    }

    fun onReviewBookingChanged(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(bookingId = value)) }
    }

    fun onReviewRatingChanged(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(rating = value)) }
    }

    fun onReviewCommentChanged(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(comment = value)) }
    }

    fun toggleReviewTag(tag: String) {
        _uiState.update { state ->
            val current = state.reviewForm.selectedTags
            val updated = if (tag in current) current - tag else current + tag
            state.copy(reviewForm = state.reviewForm.copy(selectedTags = updated))
        }
    }

    fun setReviewAnonymous(value: Boolean) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(anonymous = value)) }
    }

    fun submitReview() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can submit reviews.") }
            return
        }

        val rating = state.reviewForm.rating.toIntOrNull()
        if (rating == null) {
            _uiState.update { it.copy(errorMessage = "Rating must be a number between 1 and 5.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.createReview(
                    student = user,
                    tutorUid = state.reviewForm.tutorUid,
                    bookingId = state.reviewForm.bookingId,
                    rating = rating,
                    comment = state.reviewForm.comment,
                    tags = state.reviewForm.selectedTags,
                    anonymous = state.reviewForm.anonymous
                )
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        reviewForm = ReviewFormState(rating = "5"),
                        infoMessage = "Review added."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun onReportTutorChanged(value: String) {
        _uiState.update { it.copy(reportForm = it.reportForm.copy(tutorUid = value)) }
    }

    fun onReportReasonChanged(value: String) {
        _uiState.update { it.copy(reportForm = it.reportForm.copy(reason = value)) }
    }

    fun onReportDetailsChanged(value: String) {
        _uiState.update { it.copy(reportForm = it.reportForm.copy(details = value)) }
    }

    fun onReportSeverityChanged(value: String) {
        _uiState.update { it.copy(reportForm = it.reportForm.copy(severity = value)) }
    }

    fun submitReport() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can report tutors.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.createReport(
                    student = user,
                    tutorUid = state.reportForm.tutorUid,
                    reason = state.reportForm.reason,
                    details = state.reportForm.details,
                    severity = state.reportForm.severity
                )
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        reportForm = ReportFormState(),
                        infoMessage = "Report sent to admin."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun onCreateLoginChanged(value: String) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(login = value)) }
    }

    fun onCreatePasswordChanged(value: String) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(password = value)) }
    }

    fun onCreateDisplayNameChanged(value: String) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(displayName = value)) }
    }

    fun onCreateAdminChecked(value: Boolean) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(admin = value)) }
    }

    fun onCreateStudentChecked(value: Boolean) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(student = value)) }
    }

    fun onCreateTutorChecked(value: Boolean) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(tutor = value)) }
    }

    fun refreshAdminUsers() {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.ADMIN)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminUsersLoading = true, errorMessage = null, infoMessage = null) }
            try {
                val users = authRepository.listUsers()
                val reports = tutoringRepository.loadAdminReports()
                val allSlots = tutoringRepository.getAllAvailability()
                _uiState.update {
                    it.copy(
                        isAdminUsersLoading = false,
                        adminUsers = users,
                        dashboardState = it.dashboardState.copy(
                            adminReports = reports,
                            availableTutorSlots = allSlots
                        )
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminUsersLoading = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun createUserByAdmin() {
        val form = _uiState.value.createUserForm

        if (!form.admin && !form.student && !form.tutor) {
            _uiState.update { it.copy(errorMessage = "Select at least one role.") }
            return
        }

        if (form.login.isBlank() || form.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Login and password are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                authRepository.adminCreateUser(
                    login = form.login,
                    password = form.password,
                    displayName = form.displayName,
                    admin = form.admin,
                    student = form.student,
                    tutor = form.tutor
                )

                val users = authRepository.listUsers()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        adminUsers = users,
                        infoMessage = "User has been added.",
                        createUserForm = CreateUserFormState()
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun updateUserRolesByAdmin(uid: String, student: Boolean, tutor: Boolean) {
        if (!student && !tutor) {
            _uiState.update { it.copy(errorMessage = "User must have at least one non-admin role.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                authRepository.adminUpdateUserRoles(uid, student, tutor)
                val users = authRepository.listUsers()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        adminUsers = users,
                        infoMessage = "Roles have been updated."
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun deleteUserByAdmin(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                authRepository.adminDeleteUser(uid)
                refreshAdminUsers()
                _uiState.update { it.copy(isAdminSubmitting = false, infoMessage = "User removed.") }
            } catch (error: Throwable) {
                _uiState.update { it.copy(isAdminSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun updateReportStatusByAdmin(reportId: String, status: String) {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.ADMIN)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.updateReportStatus(reportId, status, user)
                val reports = tutoringRepository.loadAdminReports()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        dashboardState = it.dashboardState.copy(adminReports = reports),
                        infoMessage = "Report marked as $status."
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    // ----- Profile edit -----

    fun startProfileEdit() {
        val user = _uiState.value.currentUser ?: return
        _uiState.update {
            it.copy(
                profileEdit = ProfileEditState(
                    bio = user.bio.orEmpty(),
                    subjects = user.subjects.joinToString(", "),
                    experienceYears = user.experienceYears?.toString().orEmpty(),
                    program = user.program.orEmpty(),
                    studyYear = user.studyYear.orEmpty(),
                    faculty = user.faculty.orEmpty()
                )
            )
        }
    }

    fun onProfileBioChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(bio = value)) }
    }

    fun onProfileSubjectsChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(subjects = value)) }
    }

    fun onProfileExperienceYearsChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(experienceYears = value)) }
    }

    fun onProfileProgramChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(program = value)) }
    }

    fun onProfileStudyYearChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(studyYear = value)) }
    }

    fun onProfileFacultyChanged(value: String) {
        _uiState.update { it.copy(profileEdit = it.profileEdit.copy(faculty = value)) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val user = state.currentUser ?: return
        val edit = state.profileEdit
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                authRepository.updateTutorProfile(
                    uid = user.uid,
                    bio = edit.bio,
                    subjects = edit.subjects.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    experienceYears = edit.experienceYears.toIntOrNull(),
                    program = edit.program,
                    studyYear = edit.studyYear,
                    faculty = edit.faculty
                )
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Profile saved.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    // ----- Admin: set verified -----

    fun adminSetVerified(uid: String, verified: Boolean) {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.ADMIN)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                authRepository.adminSetVerified(uid, verified)
                val users = authRepository.listUsers()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        adminUsers = users,
                        infoMessage = if (verified) "Tutor verified." else "Verification removed."
                    )
                }
            } catch (error: Throwable) {
                _uiState.update { it.copy(isAdminSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun toggleAdminInspector(uid: String) {
        val state = _uiState.value
        val adminUser = state.currentUser ?: return
        if (!adminUser.hasRole(UserRole.ADMIN)) {
            return
        }

        if (state.adminInspectors.containsKey(uid)) {
            _uiState.update {
                it.copy(adminInspectors = it.adminInspectors - uid)
            }
            return
        }

        val targetUser = state.adminUsers.firstOrNull { it.uid == uid } ?: return
        _uiState.update {
            it.copy(
                adminInspectors = it.adminInspectors + (uid to AdminUserInspectorUi(user = targetUser, isLoading = true))
            )
        }

        viewModelScope.launch {
            try {
                val inspector = tutoringRepository.loadAdminUserInspector(targetUser)
                _uiState.update {
                    it.copy(adminInspectors = it.adminInspectors + (uid to inspector))
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        adminInspectors = it.adminInspectors - uid,
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    // ----- Courses -----

    fun onCourseNameChanged(value: String) {
        _uiState.update { it.copy(courseForm = it.courseForm.copy(name = value)) }
    }

    fun onCourseSubjectChanged(value: String) {
        _uiState.update { it.copy(courseForm = it.courseForm.copy(subject = value)) }
    }

    fun onCourseDescriptionChanged(value: String) {
        _uiState.update { it.copy(courseForm = it.courseForm.copy(description = value)) }
    }

    fun createCourse() {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.TUTOR)) {
            _uiState.update { it.copy(errorMessage = "Only tutors can create courses.") }
            return
        }
        val form = _uiState.value.courseForm
        if (form.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Course name is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                courseRepository.createCourse(user, form.name, form.subject, form.description)
                _uiState.update { it.copy(isMainSubmitting = false, courseForm = CourseFormState(), infoMessage = "Course created.") }
                loadMainData(user)
                restartAssignmentsListener()
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can enroll.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                courseRepository.enroll(user, courseId)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Enrolled.") }
                loadMainData(user)
                restartAssignmentsListener()
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun leaveCourse(courseId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                courseRepository.leave(user.uid, courseId)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Left course.") }
                loadMainData(user)
                restartAssignmentsListener()
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun deleteCourse(courseId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                courseRepository.deleteCourse(courseId)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Course deleted.") }
                loadMainData(user)
                restartAssignmentsListener()
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun loadRoster(courseId: String) {
        viewModelScope.launch {
            try {
                val roster = courseRepository.loadRoster(courseId)
                _uiState.update {
                    it.copy(
                        dashboardState = it.dashboardState.copy(
                            rosterByCourse = it.dashboardState.rosterByCourse + (courseId to roster)
                        )
                    )
                }
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    private fun restartAssignmentsListener() {
        val visibleCourseIds = _uiState.value.dashboardState.visibleCourseIds
        assignmentsRealtime?.remove()
        assignmentsRealtime = assignmentRepository.listenAssignmentsForCourses(
            courseIds = visibleCourseIds,
            onUpdate = { assignments ->
                _uiState.update { it.copy(dashboardState = it.dashboardState.copy(assignments = assignments)) }
            },
            onError = { error ->
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        )
    }

    // ----- Chat -----

    private fun activeChatTarget(): ChatThreadTarget? {
        val chat = _uiState.value.chat
        return when {
            chat.activeDirectId != null -> ChatThreadTarget.direct(chat.activeDirectId)
            chat.activeCourseId != null -> ChatThreadTarget.course(chat.activeCourseId)
            else -> null
        }
    }

    fun onChatSearchChanged(value: String) {
        _uiState.update { it.copy(chat = it.chat.copy(searchQuery = value)) }
    }

    fun onChatInboxFilterChanged(filter: ChatInboxFilter) {
        _uiState.update { it.copy(chat = it.chat.copy(inboxFilter = filter)) }
    }

    fun onConversationSearchChanged(value: String) {
        _uiState.update { it.copy(chat = it.chat.copy(conversationSearchQuery = value)) }
    }

    fun onComposerChanged(value: String) {
        _uiState.update { it.copy(chat = it.chat.copy(composer = value)) }
        val me = _uiState.value.currentUser ?: return
        val target = activeChatTarget() ?: return
        val now = System.currentTimeMillis()
        if (value.isBlank() || now - lastTypingSentAtMillis > 3_000L) {
            lastTypingSentAtMillis = now
            viewModelScope.launch {
                runCatching { chatRepository.setTyping(target, me.uid, me.displayName, value.isNotBlank()) }
            }
        }
    }

    fun onAnnouncementToggle(value: Boolean) {
        _uiState.update { it.copy(chat = it.chat.copy(announcementToggle = value)) }
    }

    fun onAttachmentSelected(uri: String, fileName: String, mimeType: String, sizeBytes: Long) {
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    pendingAttachment = PendingAttachmentUi(
                        uri = uri,
                        fileName = fileName,
                        mimeType = mimeType,
                        sizeBytes = sizeBytes
                    )
                )
            )
        }
    }

    fun clearPendingAttachment() {
        _uiState.update { it.copy(chat = it.chat.copy(pendingAttachment = null, uploadProgress = null)) }
    }

    fun replyToMessage(messageId: String) {
        val message = _uiState.value.chat.messages.firstOrNull { it.id == messageId } ?: return
        if (message.isDeleted) return
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    replyDraft = ChatReplyPreviewUi(
                        messageId = message.id,
                        senderName = message.senderName,
                        textPreview = message.text.take(120),
                        attachmentName = message.attachment?.fileName.orEmpty()
                    )
                )
            )
        }
    }

    fun clearReplyDraft() {
        _uiState.update { it.copy(chat = it.chat.copy(replyDraft = null)) }
    }

    fun openDirectWith(otherUid: String, otherName: String) {
        val me = _uiState.value.currentUser ?: return
        val id = ChatIds.directId(me.uid, otherUid)
        val existing = _uiState.value.chat.directConversations.firstOrNull {
            it.id == id || it.otherUid == otherUid
        }
        if (existing != null) {
            attachDirectConversation(existing.id, existing.title)
            return
        }
        viewModelScope.launch {
            try {
                val createdId = chatRepository.openOrCreateDirect(me.uid, me.displayName, otherUid, otherName)
                attachDirectConversation(createdId, otherName)
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun openDirectConversation(conversationId: String, highlightedMessageId: String = "") {
        val title = _uiState.value.chat.directConversations.firstOrNull { it.id == conversationId }?.title ?: "Conversation"
        attachDirectConversation(conversationId, title, highlightedMessageId)
    }

    private fun attachDirectConversation(conversationId: String, title: String, highlightedMessageId: String = "") {
        val me = _uiState.value.currentUser ?: return
        messagesRealtime?.remove()
        typingRealtime?.remove()
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    activeDirectId = conversationId,
                    activeCourseId = null,
                    activeTitle = title,
                    activeSubtitle = "Direct message",
                    activeIsOwner = false,
                    messages = emptyList(),
                    composer = "",
                    conversationSearchQuery = "",
                    replyDraft = null,
                    highlightedMessageId = highlightedMessageId,
                    isLoadingOlder = false,
                    canLoadOlder = false,
                    typingText = "",
                    pendingAttachment = null,
                    uploadProgress = null,
                    hasNewMessages = false
                )
            )
        }
        markActiveConversationRead()
        val target = ChatThreadTarget.direct(conversationId)
        messagesRealtime = chatRepository.listenLatestMessages(
            target = target,
            viewerUid = me.uid,
            onUpdate = { msgs ->
                mergeRemoteMessages(msgs.map { it.toUi() })
                _uiState.update { it.copy(chat = it.chat.copy(canLoadOlder = msgs.size >= 50)) }
                markActiveConversationRead()
            },
            onError = { error -> _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) } }
        )
        typingRealtime = chatRepository.listenTyping(
            target = target,
            viewerUid = me.uid,
            onUpdate = { typingText -> _uiState.update { it.copy(chat = it.chat.copy(typingText = typingText)) } },
            onError = { /* non-blocking */ }
        )
    }

    fun openCourseConversation(courseId: String, highlightedMessageId: String = "") {
        val me = _uiState.value.currentUser ?: return
        val course = _uiState.value.dashboardState.courseCatalog.firstOrNull { it.id == courseId }
        messagesRealtime?.remove()
        typingRealtime?.remove()
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    activeCourseId = courseId,
                    activeDirectId = null,
                    activeTitle = course?.name ?: "Course",
                    activeSubtitle = course?.let { selected ->
                        buildString {
                            append("Course channel")
                            if (selected.memberCount > 0) append(" · ${selected.memberCount} enrolled")
                            if (selected.tutorDisplayName.isNotBlank()) append(" · ${selected.tutorDisplayName}")
                        }
                    } ?: "Course channel",
                    activeIsOwner = course?.isOwner ?: false,
                    messages = emptyList(),
                    composer = "",
                    conversationSearchQuery = "",
                    replyDraft = null,
                    highlightedMessageId = highlightedMessageId,
                    isLoadingOlder = false,
                    canLoadOlder = false,
                    typingText = "",
                    pendingAttachment = null,
                    uploadProgress = null,
                    hasNewMessages = false
                )
            )
        }
        markActiveConversationRead()
        val target = ChatThreadTarget.course(courseId)
        messagesRealtime = chatRepository.listenLatestMessages(
            target = target,
            viewerUid = me.uid,
            onUpdate = { msgs ->
                mergeRemoteMessages(msgs.map { it.toUi() })
                _uiState.update { it.copy(chat = it.chat.copy(canLoadOlder = msgs.size >= 50)) }
                markActiveConversationRead()
            },
            onError = { error -> _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) } }
        )
        typingRealtime = chatRepository.listenTyping(
            target = target,
            viewerUid = me.uid,
            onUpdate = { typingText -> _uiState.update { it.copy(chat = it.chat.copy(typingText = typingText)) } },
            onError = { /* non-blocking */ }
        )
    }

    fun closeConversation() {
        val me = _uiState.value.currentUser
        val target = activeChatTarget()
        if (me != null && target != null) {
            viewModelScope.launch { runCatching { chatRepository.setTyping(target, me.uid, me.displayName, false) } }
        }
        messagesRealtime?.remove()
        typingRealtime?.remove()
        materialsRealtime?.remove()
        messagesRealtime = null
        typingRealtime = null
        materialsRealtime = null
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    activeDirectId = null,
                    activeCourseId = null,
                    activeTitle = "",
                    activeSubtitle = "",
                    activeIsOwner = false,
                    messages = emptyList(),
                    composer = "",
                    conversationSearchQuery = "",
                    replyDraft = null,
                    highlightedMessageId = "",
                    isLoadingOlder = false,
                    canLoadOlder = false,
                    typingText = "",
                    pendingAttachment = null,
                    uploadProgress = null,
                    hasNewMessages = false,
                    announcementToggle = false,
                    materialsOpen = false,
                    materials = emptyList(),
                    materialsLoading = false,
                    materialUploadProgress = null
                )
            )
        }
    }

    fun openCourseMaterials() {
        val courseId = _uiState.value.chat.activeCourseId ?: return
        materialsRealtime?.remove()
        _uiState.update {
            it.copy(chat = it.chat.copy(materialsOpen = true, materials = emptyList(), materialsLoading = true))
        }
        materialsRealtime = courseRepository.listenCourseMaterials(
            courseId = courseId,
            onUpdate = { materials ->
                _uiState.update {
                    it.copy(chat = it.chat.copy(materials = materials, materialsLoading = false))
                }
            },
            onError = { error ->
                _uiState.update { it.copy(chat = it.chat.copy(materialsLoading = false)) }
                showRealtimeError("Course materials", error)
            }
        )
    }

    fun closeCourseMaterials() {
        materialsRealtime?.remove()
        materialsRealtime = null
        _uiState.update {
            it.copy(
                chat = it.chat.copy(
                    materialsOpen = false,
                    materials = emptyList(),
                    materialsLoading = false,
                    materialUploadProgress = null
                )
            )
        }
    }

    fun uploadCourseMaterial(uri: String, fileName: String, mimeType: String, sizeBytes: Long) {
        val me = _uiState.value.currentUser ?: return
        val courseId = _uiState.value.chat.activeCourseId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(chat = it.chat.copy(materialUploadProgress = 0f), errorMessage = null) }
            try {
                courseRepository.uploadCourseMaterial(
                    courseId = courseId,
                    uploader = me,
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    uri = Uri.parse(uri),
                    onProgress = { progress ->
                        _uiState.update { it.copy(chat = it.chat.copy(materialUploadProgress = progress)) }
                    }
                )
                _uiState.update { it.copy(chat = it.chat.copy(materialUploadProgress = null)) }
            } catch (error: Throwable) {
                Log.e(TAG, "Uploading course material failed", error)
                _uiState.update {
                    it.copy(
                        chat = it.chat.copy(materialUploadProgress = null),
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    fun deleteCourseMaterial(materialId: String) {
        val courseId = _uiState.value.chat.activeCourseId ?: return
        val material = _uiState.value.chat.materials.firstOrNull { it.id == materialId } ?: return
        viewModelScope.launch {
            try {
                courseRepository.deleteCourseMaterial(courseId, material)
            } catch (error: Throwable) {
                Log.e(TAG, "Deleting course material failed", error)
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun sendMessage() {
        val me = _uiState.value.currentUser ?: return
        val chat = _uiState.value.chat
        val text = chat.composer.trim()
        val target = activeChatTarget() ?: return
        val pendingAttachment = chat.pendingAttachment
        if (text.isEmpty() && pendingAttachment == null) return
        val clientMessageId = UUID.randomUUID().toString()
        val localMessage = MessageUi(
            id = "local_$clientMessageId",
            senderUid = me.uid,
            senderName = me.displayName,
            text = text,
            isAnnouncement = chat.activeCourseId != null && chat.announcementToggle,
            sentAtLabel = "Sending",
            sentAtMillis = System.currentTimeMillis(),
            clientMessageId = clientMessageId,
            deliveryState = MessageDeliveryState.SENDING,
            messageType = pendingAttachment?.mimeType?.let { if (it.startsWith("image/")) ChatMessageType.IMAGE else ChatMessageType.FILE } ?: ChatMessageType.TEXT,
            attachment = pendingAttachment?.toAttachment(),
            replyTo = chat.replyDraft
        )
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chat = it.chat.copy(
                        composer = "",
                        isSending = true,
                        replyDraft = null,
                        pendingAttachment = null,
                        uploadProgress = pendingAttachment?.let { 0f },
                        messages = it.chat.messages + localMessage
                    ),
                    errorMessage = null
                )
            }
            try {
                val uploaded = pendingAttachment?.let {
                    chatRepository.uploadChatAttachment(
                        target = target,
                        senderUid = me.uid,
                        clientMessageId = clientMessageId,
                        fileName = it.fileName,
                        mimeType = it.mimeType,
                        sizeBytes = it.sizeBytes,
                        uri = Uri.parse(it.uri),
                        onProgress = { progress ->
                            _uiState.update { state -> state.copy(chat = state.chat.copy(uploadProgress = progress)) }
                        }
                    )
                }
                chatRepository.sendMessage(
                    target = target,
                    senderUid = me.uid,
                    senderName = me.displayName,
                    text = text,
                    isAnnouncement = chat.announcementToggle,
                    clientMessageId = clientMessageId,
                    attachment = uploaded,
                    replyTo = chat.replyDraft?.toRepository()
                )
                _uiState.update { state ->
                    state.copy(
                        chat = state.chat.copy(
                            isSending = false,
                            announcementToggle = false,
                            uploadProgress = null,
                            messages = state.chat.messages.map { message ->
                                if (message.clientMessageId == clientMessageId) {
                                    message.copy(
                                        sentAtLabel = if (target.isCourse) "Posted" else "Sent",
                                        deliveryState = if (target.isCourse) MessageDeliveryState.POSTED else MessageDeliveryState.SENT,
                                        attachment = uploaded?.toUi(localUri = pendingAttachment?.uri.orEmpty()) ?: message.attachment
                                    )
                                } else {
                                    message
                                }
                            }
                        )
                    )
                }
            } catch (error: Throwable) {
                Log.e(TAG, "Sending chat message failed", error)
                _uiState.update { state ->
                    state.copy(
                        chat = state.chat.copy(
                            isSending = false,
                            uploadProgress = null,
                            messages = state.chat.messages.map { message ->
                                if (message.clientMessageId == clientMessageId) {
                                    message.copy(sentAtLabel = "Not sent", deliveryState = MessageDeliveryState.FAILED)
                                } else {
                                    message
                                }
                            }
                        ),
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    fun retryMessage(clientMessageId: String) {
        val me = _uiState.value.currentUser ?: return
        val failed = _uiState.value.chat.messages.firstOrNull {
            it.clientMessageId == clientMessageId && it.deliveryState == MessageDeliveryState.FAILED
        } ?: return
        val chat = _uiState.value.chat
        val target = activeChatTarget() ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    chat = state.chat.copy(
                        isSending = true,
                        messages = state.chat.messages.map { message ->
                            if (message.clientMessageId == clientMessageId) {
                                message.copy(sentAtLabel = "Sending", deliveryState = MessageDeliveryState.SENDING)
                            } else {
                                message
                            }
                        }
                    ),
                    errorMessage = null
                )
            }
            try {
                val uploaded = failed.attachment?.takeIf { it.downloadUrl.isBlank() && it.localUri.isNotBlank() }?.let {
                    chatRepository.uploadChatAttachment(
                        target = target,
                        senderUid = me.uid,
                        clientMessageId = clientMessageId,
                        fileName = it.fileName,
                        mimeType = it.mimeType,
                        sizeBytes = it.sizeBytes,
                        uri = Uri.parse(it.localUri),
                        onProgress = { progress ->
                            _uiState.update { state -> state.copy(chat = state.chat.copy(uploadProgress = progress)) }
                        }
                    )
                }
                chatRepository.sendMessage(
                    target = target,
                    senderUid = me.uid,
                    senderName = me.displayName,
                    text = failed.text,
                    isAnnouncement = failed.isAnnouncement,
                    clientMessageId = clientMessageId,
                    attachment = uploaded ?: failed.attachment?.toOutgoing(),
                    replyTo = failed.replyTo?.toRepository()
                )
                _uiState.update { state ->
                    state.copy(
                        chat = state.chat.copy(
                            isSending = false,
                            uploadProgress = null,
                            messages = state.chat.messages.map { message ->
                                if (message.clientMessageId == clientMessageId) {
                                    message.copy(
                                        sentAtLabel = if (target.isCourse) "Posted" else "Sent",
                                        deliveryState = if (target.isCourse) MessageDeliveryState.POSTED else MessageDeliveryState.SENT,
                                        attachment = uploaded?.toUi(localUri = failed.attachment?.localUri.orEmpty()) ?: message.attachment
                                    )
                                } else {
                                    message
                                }
                            }
                        )
                    )
                }
            } catch (error: Throwable) {
                Log.e(TAG, "Retrying chat message failed", error)
                _uiState.update { state ->
                    state.copy(
                        chat = state.chat.copy(
                            isSending = false,
                            uploadProgress = null,
                            messages = state.chat.messages.map { message ->
                                if (message.clientMessageId == clientMessageId) {
                                    message.copy(sentAtLabel = "Not sent", deliveryState = MessageDeliveryState.FAILED)
                                } else {
                                    message
                                }
                            }
                        ),
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    fun loadOlderMessages() {
        val me = _uiState.value.currentUser ?: return
        val chat = _uiState.value.chat
        val target = activeChatTarget() ?: return
        if (chat.isLoadingOlder || !chat.canLoadOlder) return
        val beforeMillis = chat.messages.firstOrNull { !it.id.startsWith("local_") }?.sentAtMillis ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(chat = it.chat.copy(isLoadingOlder = true), errorMessage = null) }
            try {
                val older = chatRepository.loadOlderMessages(
                    target = target,
                    viewerUid = me.uid,
                    beforeMillis = beforeMillis
                ).map { it.toUi() }
                _uiState.update { state ->
                    val merged = (older + state.chat.messages)
                        .distinctBy { message -> message.id.ifBlank { message.clientMessageId } }
                        .sortedBy { it.sentAtMillis }
                    state.copy(
                        chat = state.chat.copy(
                            messages = decorateRemoteMessages(merged, state.chat),
                            isLoadingOlder = false,
                            canLoadOlder = older.size >= 50
                        )
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        chat = it.chat.copy(isLoadingOlder = false),
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    fun reactToMessage(messageId: String, reactionKey: String) {
        val me = _uiState.value.currentUser ?: return
        val target = activeChatTarget() ?: return
        if (messageId.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.setReaction(target, messageId, me.uid, reactionKey)
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        val me = _uiState.value.currentUser ?: return
        val target = activeChatTarget() ?: return
        val message = _uiState.value.chat.messages.firstOrNull { it.id == messageId } ?: return
        if (message.senderUid != me.uid || message.isDeleted) return
        viewModelScope.launch {
            try {
                chatRepository.deleteOwnMessage(target, messageId, me.uid)
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun reportMessage(messageId: String) {
        val me = _uiState.value.currentUser ?: return
        val target = activeChatTarget() ?: return
        val message = _uiState.value.chat.messages.firstOrNull { it.id == messageId } ?: return
        viewModelScope.launch {
            try {
                chatRepository.reportMessage(
                    target = target,
                    message = message.toRepositoryMessage(),
                    reporterUid = me.uid
                )
                _uiState.update { it.copy(infoMessage = "Message reported.") }
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun clearNewMessageHint() {
        _uiState.update { it.copy(chat = it.chat.copy(hasNewMessages = false)) }
    }

    private fun ChatConversation.toUi(): ConversationUi =
        ConversationUi(
            id = id,
            title = title,
            lastMessageText = lastMessageText,
            lastMessageAtLabel = lastMessageAtLabel,
            lastMessageAtMillis = lastMessageAtMillis,
            lastMessageSenderName = lastMessageSenderName,
            lastMessageSenderUid = lastMessageSenderUid,
            currentUserReadAtMillis = currentUserReadAtMillis,
            otherReadAtMillis = otherReadAtMillis,
            unreadCount = unreadCount,
            otherUid = otherUid
        )

    private fun ChatAttachment.toUi(localUri: String = ""): ChatAttachmentUi =
        ChatAttachmentUi(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            localUri = localUri,
            width = width,
            height = height
        )

    private fun OutgoingChatAttachment.toUi(localUri: String = ""): ChatAttachmentUi =
        ChatAttachmentUi(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            localUri = localUri,
            width = width,
            height = height
        )

    private fun ChatAttachmentUi.toOutgoing(): OutgoingChatAttachment =
        OutgoingChatAttachment(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            width = width,
            height = height
        )

    private fun ChatAttachmentUi.toRepository(): ChatAttachment =
        ChatAttachment(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            width = width,
            height = height
        )

    private fun ChatReplyPreview.toUi(): ChatReplyPreviewUi =
        ChatReplyPreviewUi(
            messageId = messageId,
            senderName = senderName,
            textPreview = textPreview,
            attachmentName = attachmentName
        )

    private fun ChatReplyPreviewUi.toRepository(): ChatReplyPreview =
        ChatReplyPreview(
            messageId = messageId,
            senderName = senderName,
            textPreview = textPreview,
            attachmentName = attachmentName
        )

    private fun String.toChatMessageType(): ChatMessageType = when (lowercase()) {
        "image" -> ChatMessageType.IMAGE
        "file" -> ChatMessageType.FILE
        else -> ChatMessageType.TEXT
    }

    private fun ChatMessage.toUi(): MessageUi =
        MessageUi(
            id = id,
            senderUid = senderUid,
            senderName = senderName,
            text = text,
            isAnnouncement = isAnnouncement,
            sentAtLabel = sentAtLabel,
            sentAtMillis = sentAtMillis,
            clientMessageId = clientMessageId,
            messageType = messageType.toChatMessageType(),
            attachment = attachment?.toUi(),
            replyTo = replyTo?.toUi(),
            editedAtLabel = editedAtLabel,
            deletedAtLabel = deletedAtLabel,
            reactionCounts = reactionCounts,
            myReaction = myReaction
        )

    private fun MessageUi.toRepositoryMessage(): ChatMessage =
        ChatMessage(
            id = id,
            senderUid = senderUid,
            senderName = senderName,
            text = text,
            isAnnouncement = isAnnouncement,
            sentAtLabel = sentAtLabel,
            sentAtMillis = sentAtMillis,
            clientMessageId = clientMessageId,
            messageType = when (messageType) {
                ChatMessageType.IMAGE -> "image"
                ChatMessageType.FILE -> "file"
                ChatMessageType.TEXT -> "text"
            },
            attachment = attachment?.toRepository(),
            replyTo = replyTo?.toRepository(),
            editedAtLabel = editedAtLabel,
            deletedAtLabel = deletedAtLabel,
            reactionCounts = reactionCounts,
            myReaction = myReaction
        )

    private fun mergeRemoteMessages(remoteMessages: List<MessageUi>) {
        _uiState.update { state ->
            val remoteClientIds = remoteMessages.mapNotNull { it.clientMessageId.ifBlank { null } }.toSet()
            val localMessages = state.chat.messages.filter { message ->
                message.id.startsWith("local_") && message.clientMessageId !in remoteClientIds
            }
            val hadRemoteMessages = state.chat.messages.any { !it.id.startsWith("local_") }
            val latestKnownRemoteMillis = state.chat.messages
                .filterNot { it.id.startsWith("local_") }
                .maxOfOrNull { it.sentAtMillis } ?: 0L
            val me = state.currentUser?.uid.orEmpty()
            val hasIncomingNew = hadRemoteMessages && remoteMessages.any { message ->
                message.sentAtMillis > latestKnownRemoteMillis && message.senderUid != me
            }
            state.copy(
                chat = state.chat.copy(
                    messages = decorateRemoteMessages(remoteMessages, state.chat) + localMessages,
                    hasNewMessages = state.chat.hasNewMessages || hasIncomingNew
                )
            )
        }
    }

    private fun decorateRemoteMessages(messages: List<MessageUi>, chat: ChatUiState): List<MessageUi> {
        val me = _uiState.value.currentUser ?: return messages
        val activeDirectId = chat.activeDirectId
        val otherReadAt = activeDirectId
            ?.let { id -> chat.directConversations.firstOrNull { it.id == id }?.otherReadAtMillis }
            ?: 0L

        return messages.map { message ->
            when {
                message.id.startsWith("local_") -> message
                message.senderUid != me.uid -> message.copy(deliveryState = MessageDeliveryState.RECEIVED)
                chat.activeCourseId != null -> message.copy(deliveryState = MessageDeliveryState.POSTED)
                otherReadAt > 0L -> message.copy(
                    deliveryState = if (ChatReadState.isReadByOther(message.sentAtMillis, otherReadAt)) {
                        MessageDeliveryState.READ
                    } else {
                        MessageDeliveryState.DELIVERED
                    }
                )
                else -> message.copy(deliveryState = MessageDeliveryState.DELIVERED)
            }
        }
    }

    private fun redecorateActiveMessages() {
        _uiState.update { state ->
            state.copy(
                chat = state.chat.copy(
                    messages = decorateRemoteMessages(state.chat.messages, state.chat)
                )
            )
        }
    }

    private fun markActiveConversationRead() {
        val me = _uiState.value.currentUser ?: return
        val chat = _uiState.value.chat
        val readTarget = when {
            chat.activeDirectId != null -> {
                val conversation = chat.directConversations.firstOrNull { it.id == chat.activeDirectId } ?: return
                if (
                    conversation.lastMessageSenderUid == me.uid ||
                    conversation.lastMessageAtMillis <= conversation.currentUserReadAtMillis
                ) return
                "direct:${conversation.id}" to suspend { chatRepository.markDirectRead(conversation.id, me.uid) }
            }
            chat.activeCourseId != null -> {
                val course = _uiState.value.dashboardState.courseCatalog.firstOrNull { it.id == chat.activeCourseId } ?: return
                if (course.unreadCount <= 0) return
                "course:${course.id}" to suspend { courseRepository.markCourseRead(course.id, me.uid) }
            }
            else -> return
        }
        val key = readTarget.first
        val now = System.currentTimeMillis()
        if (key in readReceiptInFlight || (readReceiptBackoffUntil[key] ?: 0L) > now) return
        readReceiptInFlight += key
        viewModelScope.launch {
            runCatching { readTarget.second.invoke() }
                .onSuccess { readReceiptBackoffUntil.remove(key) }
                .onFailure { readReceiptBackoffUntil[key] = System.currentTimeMillis() + 60_000L }
            readReceiptInFlight -= key
        }
    }

    // ----- Notifications -----

    fun markNotificationRead(id: String) {
        viewModelScope.launch { runCatching { notificationRepository.markRead(id) } }
    }

    fun markAllNotificationsRead() {
        val unreadIds = _uiState.value.dashboardState.notifications.filterNot { it.read }.map { it.id }
        viewModelScope.launch { runCatching { notificationRepository.markAllRead(unreadIds) } }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun bootstrapSession() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isBootstrapping = false,
                            isBusy = false,
                            screen = AppScreen.ONBOARDING,
                            currentUser = null,
                            activeRole = null,
                            passwordInput = ""
                        )
                    }
                    return@launch
                }

                applyRoutingForUser(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ONBOARDING,
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    private suspend fun applyRoutingForUser(user: AppUser) {
        when (SessionRouter.resolve(user)) {
            SessionDestination.ADMIN_PANEL -> {
                stopRealtime()
                val users = authRepository.listUsers()
                val reports = tutoringRepository.loadAdminReports()
                val allSlots = tutoringRepository.getAllAvailability()
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ADMIN_PANEL,
                        currentUser = user,
                        activeRole = null,
                        adminUsers = users,
                        dashboardState = DashboardUiState(
                            adminReports = reports,
                            availableTutorSlots = allSlots
                        ),
                        passwordInput = "",
                        errorMessage = null,
                        registerForm = RegisterFormState()
                    )
                }
            }

            SessionDestination.MAIN_SHELL -> {
                val defaultRole = when {
                    user.hasRole(UserRole.STUDENT) -> UserRole.STUDENT
                    user.hasRole(UserRole.TUTOR) -> UserRole.TUTOR
                    else -> null
                }

                if (defaultRole == null) {
                    _uiState.update {
                        it.copy(
                            isBootstrapping = false,
                            isBusy = false,
                            screen = AppScreen.ONBOARDING,
                            currentUser = null,
                            activeRole = null,
                            errorMessage = "No role assigned to this account."
                        )
                    }
                    return
                }

                navigateToMainShell(user, defaultRole)
            }

            SessionDestination.LOGIN -> {
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ONBOARDING,
                        currentUser = null,
                        activeRole = null,
                        errorMessage = "No role assigned to this account."
                    )
                }
            }
        }
    }

    private suspend fun navigateToMainShell(user: AppUser, role: UserRole) {
        _uiState.update {
            it.copy(
                isBootstrapping = false,
                isBusy = false,
                screen = AppScreen.MAIN_SHELL,
                currentUser = user,
                activeRole = role,
                passwordInput = "",
                errorMessage = null,
                registerForm = RegisterFormState(),
                dashboardState = DashboardUiState(isLoading = true)
            )
        }

        loadMainData(user)
        startRealtime(user)
        runCatching { FcmTokenRegistrar.register(user.uid) }
    }

    private suspend fun loadMainData(user: AppUser) {
        _uiState.update {
            it.copy(
                dashboardState = it.dashboardState.copy(isLoading = true),
                errorMessage = null
            )
        }

        try {
            val dashboard = tutoringRepository.loadDashboard(user)
            val visibleCourseIds = buildList {
                if (user.hasRole(UserRole.STUDENT)) addAll(runCatching { courseRepository.myEnrolledCourseIds(user.uid) }.getOrDefault(emptyList()))
                if (user.hasRole(UserRole.TUTOR)) addAll(runCatching { courseRepository.myOwnedCourseIds(user.uid) }.getOrDefault(emptyList()))
            }.distinct()
            val assignments = runCatching { assignmentRepository.loadAssignmentsForCourses(visibleCourseIds) }.getOrDefault(emptyList())
            val mySubmissions = if (user.hasRole(UserRole.STUDENT)) {
                runCatching {
                    assignmentRepository.loadMySubmissions(
                        studentUid = user.uid,
                        assignmentIds = assignments.map { it.id }
                    )
                }.getOrDefault(emptyList())
            } else emptyList()
            _uiState.update { state ->
                state.copy(
                    dashboardState = dashboard.copy(
                        isLoading = false,
                        assignments = assignments,
                        mySubmissions = mySubmissions,
                        visibleCourseIds = visibleCourseIds,
                        courseCatalog = courseWithFlags(state.dashboardState.courseCatalog, visibleCourseIds),
                        rosterByCourse = state.dashboardState.rosterByCourse,
                        submissionsByAssignment = state.dashboardState.submissionsByAssignment,
                        notifications = state.dashboardState.notifications,
                        notificationCount = state.dashboardState.notificationCount
                    )
                )
            }
        } catch (error: Throwable) {
            _uiState.update {
                it.copy(
                    dashboardState = it.dashboardState.copy(isLoading = false),
                    errorMessage = authRepository.userMessage(error)
                )
            }
        }
    }

    fun onAssignmentTitleChanged(value: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(title = value)) }
    }

    fun onAssignmentDescriptionChanged(value: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(description = value)) }
    }

    fun onAssignmentSubjectChanged(value: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(subject = value)) }
    }

    fun onAssignmentDueDateChanged(value: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(dueDate = value)) }
    }

    fun onAssignmentCourseChanged(courseId: String, courseName: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(courseId = courseId, courseName = courseName)) }
    }

    fun onAssignmentDueAtChanged(millis: Long?, label: String) {
        _uiState.update { it.copy(assignmentForm = it.assignmentForm.copy(dueAtMillis = millis, dueDate = label)) }
    }

    fun createAssignment() {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.TUTOR)) {
            _uiState.update { it.copy(errorMessage = "Only tutors can create assignments.") }
            return
        }

        if (state.assignmentForm.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title is required.") }
            return
        }

        if (state.assignmentForm.courseId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pick a course for this assignment.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                assignmentRepository.createAssignment(
                    tutor = user,
                    title = state.assignmentForm.title,
                    description = state.assignmentForm.description,
                    subject = state.assignmentForm.subject,
                    courseId = state.assignmentForm.courseId,
                    courseName = state.assignmentForm.courseName,
                    dueDate = state.assignmentForm.dueDate,
                    dueAtMillis = state.assignmentForm.dueAtMillis
                )
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        assignmentForm = AssignmentFormState(),
                        infoMessage = "Assignment published."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun deleteAssignment(assignmentId: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                assignmentRepository.deleteAssignment(assignmentId)
                _uiState.update { it.copy(isMainSubmitting = false, infoMessage = "Assignment deleted.") }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update { it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun uploadSubmission(assignmentId: String, fileName: String, bytes: ByteArray) {
        val user = _uiState.value.currentUser ?: return
        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can upload submissions.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                assignmentRepository.uploadSubmission(user, assignmentId, fileName, bytes)
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        infoMessage = "Submission uploaded."
                    )
                }
                loadMainData(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isMainSubmitting = false, errorMessage = authRepository.userMessage(error))
                }
            }
        }
    }

    fun loadSubmissionsForAssignment(assignmentId: String) {
        viewModelScope.launch {
            try {
                val submissions = assignmentRepository.loadSubmissionsForAssignment(assignmentId)
                _uiState.update {
                    it.copy(
                        dashboardState = it.dashboardState.copy(
                            submissionsByAssignment = it.dashboardState.submissionsByAssignment + (assignmentId to submissions)
                        )
                    )
                }
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    private fun courseWithFlags(courses: List<CourseUi>, visibleCourseIds: List<String>): List<CourseUi> =
        courses.map { it.copy(isEnrolled = visibleCourseIds.contains(it.id)) }

    private fun startRealtime(user: AppUser) {
        stopRealtime()

        tutoringRealtime = tutoringRepository.listenDashboard(
            user = user,
            onUpdate = { dashboard ->
                _uiState.update { state ->
                    state.copy(
                        dashboardState = dashboard.copy(
                            assignments = state.dashboardState.assignments,
                            mySubmissions = state.dashboardState.mySubmissions,
                            submissionsByAssignment = state.dashboardState.submissionsByAssignment,
                            courseCatalog = state.dashboardState.courseCatalog,
                            visibleCourseIds = state.dashboardState.visibleCourseIds,
                            rosterByCourse = state.dashboardState.rosterByCourse,
                            notifications = state.dashboardState.notifications,
                            notificationCount = state.dashboardState.notificationCount
                        )
                    )
                }
                loadChatContacts(user)
            },
            onError = { error ->
                showRealtimeError("Dashboard realtime", error)
            }
        )

        val visibleCourseIds = _uiState.value.dashboardState.visibleCourseIds
        assignmentsRealtime = assignmentRepository.listenAssignmentsForCourses(
            courseIds = visibleCourseIds,
            onUpdate = { assignments ->
                _uiState.update {
                    it.copy(dashboardState = it.dashboardState.copy(assignments = assignments))
                }
                restartMySubmissionsRealtime(user, assignments)
            },
            onError = { error ->
                showRealtimeError("Assignments realtime", error)
            }
        )

        coursesRealtime = courseRepository.listenCourseCatalog(
            uid = user.uid,
            onUpdate = { courses ->
                _uiState.update {
                    it.copy(
                        dashboardState = it.dashboardState.copy(
                            courseCatalog = courseWithFlags(courses, it.dashboardState.visibleCourseIds)
                        )
                    )
                }
                if (_uiState.value.chat.activeCourseId != null) {
                    markActiveConversationRead()
                }
            },
            onError = { error ->
                showRealtimeError("Course catalog", error)
            }
        )

        conversationsRealtime = chatRepository.listenDirectConversations(
            uid = user.uid,
            onUpdate = { conversations ->
                _uiState.update { it.copy(chat = it.chat.copy(directConversations = conversations.map { item -> item.toUi() })) }
                redecorateActiveMessages()
                if (_uiState.value.chat.activeDirectId != null) {
                    markActiveConversationRead()
                }
            },
            onError = { error ->
                showRealtimeError("Direct conversations", error)
            }
        )

        notificationsRealtime = notificationRepository.listenMine(
            uid = user.uid,
            onUpdate = { notifications ->
                _uiState.update {
                    it.copy(
                        dashboardState = it.dashboardState.copy(
                            notifications = notifications,
                            notificationCount = NotificationUtil.unreadCount(notifications)
                        )
                    )
                }
            },
            onError = { error ->
                showRealtimeError("Notifications", error)
            }
        )

        restartMySubmissionsRealtime(user, _uiState.value.dashboardState.assignments)

        loadChatContacts(user)
    }

    private fun restartMySubmissionsRealtime(user: AppUser, assignments: List<AssignmentUi>) {
        if (!user.hasRole(UserRole.STUDENT)) {
            mySubmissionsRealtime?.remove()
            mySubmissionsRealtime = null
            mySubmissionAssignmentIds = emptyList()
            return
        }

        val assignmentIds = assignments.map { it.id }.distinct().sorted()
        if (assignmentIds == mySubmissionAssignmentIds) return

        mySubmissionsRealtime?.remove()
        mySubmissionAssignmentIds = assignmentIds
        mySubmissionsRealtime = assignmentRepository.listenMySubmissions(
            studentUid = user.uid,
            assignmentIds = assignmentIds,
            onUpdate = { submissions ->
                _uiState.update {
                    it.copy(dashboardState = it.dashboardState.copy(mySubmissions = submissions))
                }
            },
            onError = { error ->
                showRealtimeError("My submissions", error)
            }
        )
    }

    private fun loadChatContacts(user: AppUser) {
        viewModelScope.launch {
            try {
                val dashboard = _uiState.value.dashboardState
                val tutorContacts = dashboard.tutors
                    .filter { it.uid != user.uid }
                    .map { ChatContactUi(uid = it.uid, displayName = it.displayName, subtitle = it.subjects) }
                val lessonContacts = buildList {
                    dashboard.myStudentBookings.forEach { lesson ->
                        if (lesson.tutorId != user.uid) {
                            add(ChatContactUi(uid = lesson.tutorId, displayName = lesson.tutorDisplayName, subtitle = lesson.subject))
                        }
                    }
                    dashboard.myTutorBookings.forEach { lesson ->
                        if (lesson.studentId != user.uid) {
                            add(ChatContactUi(uid = lesson.studentId, displayName = lesson.studentDisplayName, subtitle = lesson.subject))
                        }
                    }
                }
                val contacts = (tutorContacts + lessonContacts)
                    .distinctBy { it.uid }
                    .sortedBy { it.displayName.lowercase() }
                _uiState.update { it.copy(chat = it.chat.copy(contacts = contacts)) }
            } catch (_: Throwable) {
                // contacts are best-effort
            }
        }
    }

    private fun stopRealtime() {
        tutoringRealtime?.remove()
        assignmentsRealtime?.remove()
        mySubmissionsRealtime?.remove()
        coursesRealtime?.remove()
        conversationsRealtime?.remove()
        messagesRealtime?.remove()
        typingRealtime?.remove()
        materialsRealtime?.remove()
        notificationsRealtime?.remove()
        mySubmissionAssignmentIds = emptyList()
        tutoringRealtime = null
        assignmentsRealtime = null
        mySubmissionsRealtime = null
        coursesRealtime = null
        conversationsRealtime = null
        messagesRealtime = null
        typingRealtime = null
        materialsRealtime = null
        notificationsRealtime = null
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }
}
