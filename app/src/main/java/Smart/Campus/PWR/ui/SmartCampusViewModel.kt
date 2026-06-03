package Smart.Campus.PWR.ui

import Smart.Campus.PWR.assignments.AssignmentRepository
import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.FirebaseAuthRepository
import Smart.Campus.PWR.auth.SessionDestination
import Smart.Campus.PWR.auth.SessionRouter
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.chat.ChatRepository
import Smart.Campus.PWR.course.CourseRepository
import Smart.Campus.PWR.notifications.FcmTokenRegistrar
import Smart.Campus.PWR.notifications.NotificationRepository
import Smart.Campus.PWR.notifications.NotificationUtil
import Smart.Campus.PWR.tutoring.TutoringRepository
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.AssignmentFormState
import Smart.Campus.PWR.ui.state.AvailabilityFormState
import Smart.Campus.PWR.ui.state.ChatContactUi
import Smart.Campus.PWR.ui.state.CourseFormState
import Smart.Campus.PWR.ui.state.CourseUi
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.ReportFormState
import Smart.Campus.PWR.ui.state.ReviewFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import com.google.firebase.firestore.ListenerRegistration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartCampusViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val tutoringRepository: TutoringRepository = TutoringRepository(),
    private val assignmentRepository: AssignmentRepository = AssignmentRepository(),
    private val courseRepository: CourseRepository = CourseRepository(),
    private val chatRepository: ChatRepository = ChatRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartCampusUiState())
    val uiState: StateFlow<SmartCampusUiState> = _uiState.asStateFlow()
    private var tutoringRealtime: ListenerRegistration? = null
    private var assignmentsRealtime: ListenerRegistration? = null
    private var mySubmissionsRealtime: ListenerRegistration? = null
    private var coursesRealtime: ListenerRegistration? = null
    private var conversationsRealtime: ListenerRegistration? = null
    private var messagesRealtime: ListenerRegistration? = null
    private var notificationsRealtime: ListenerRegistration? = null

    init {
        bootstrapSession()
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
                tutoringRepository.createAvailability(user, form.subject, form.date, start, end)
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

    fun bookTutorSlot(slotId: String) {
        val state = _uiState.value
        val user = state.currentUser ?: return

        if (!user.hasRole(UserRole.STUDENT)) {
            _uiState.update { it.copy(errorMessage = "Only students can book lessons.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMainSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                tutoringRepository.bookAvailability(slotId, user)
                _uiState.update {
                    it.copy(
                        isMainSubmitting = false,
                        infoMessage = "Lesson booked successfully."
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
                    comment = state.reviewForm.comment
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
                    details = state.reportForm.details
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

    fun onComposerChanged(value: String) {
        _uiState.update { it.copy(chat = it.chat.copy(composer = value)) }
    }

    fun onAnnouncementToggle(value: Boolean) {
        _uiState.update { it.copy(chat = it.chat.copy(announcementToggle = value)) }
    }

    fun openDirectWith(otherUid: String, otherName: String) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            try {
                val id = chatRepository.openOrCreateDirect(me, otherUid, otherName)
                attachDirectConversation(id, otherName)
            } catch (error: Throwable) {
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        }
    }

    fun openDirectConversation(conversationId: String) {
        val title = _uiState.value.chat.directConversations.firstOrNull { it.id == conversationId }?.title ?: "Conversation"
        attachDirectConversation(conversationId, title)
    }

    private fun attachDirectConversation(conversationId: String, title: String) {
        messagesRealtime?.remove()
        _uiState.update {
            it.copy(chat = it.chat.copy(activeDirectId = conversationId, activeCourseId = null, activeTitle = title, activeIsOwner = false, messages = emptyList(), composer = ""))
        }
        messagesRealtime = chatRepository.listenDirectMessages(
            conversationId = conversationId,
            onUpdate = { msgs -> _uiState.update { it.copy(chat = it.chat.copy(messages = msgs)) } },
            onError = { error -> _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) } }
        )
    }

    fun openCourseConversation(courseId: String) {
        val course = _uiState.value.dashboardState.courseCatalog.firstOrNull { it.id == courseId }
        messagesRealtime?.remove()
        _uiState.update {
            it.copy(chat = it.chat.copy(activeCourseId = courseId, activeDirectId = null, activeTitle = course?.name ?: "Course", activeIsOwner = course?.isOwner ?: false, messages = emptyList(), composer = ""))
        }
        messagesRealtime = chatRepository.listenCourseMessages(
            courseId = courseId,
            onUpdate = { msgs -> _uiState.update { it.copy(chat = it.chat.copy(messages = msgs)) } },
            onError = { error -> _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) } }
        )
    }

    fun closeConversation() {
        messagesRealtime?.remove()
        messagesRealtime = null
        _uiState.update {
            it.copy(chat = it.chat.copy(activeDirectId = null, activeCourseId = null, activeTitle = "", activeIsOwner = false, messages = emptyList(), composer = "", announcementToggle = false))
        }
    }

    fun sendMessage() {
        val me = _uiState.value.currentUser ?: return
        val chat = _uiState.value.chat
        val text = chat.composer.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(chat = it.chat.copy(isSending = true)) }
            try {
                val courseId = chat.activeCourseId
                val directId = chat.activeDirectId
                when {
                    courseId != null -> chatRepository.sendCourseMessage(courseId, me, text, chat.announcementToggle)
                    directId != null -> chatRepository.sendDirectMessage(directId, me, text)
                }
                _uiState.update { it.copy(chat = it.chat.copy(composer = "", isSending = false, announcementToggle = false)) }
            } catch (error: Throwable) {
                _uiState.update { it.copy(chat = it.chat.copy(isSending = false), errorMessage = authRepository.userMessage(error)) }
            }
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
                runCatching { assignmentRepository.loadMySubmissions(user.uid) }.getOrDefault(emptyList())
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
            },
            onError = { error ->
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
            }
        )

        val visibleCourseIds = _uiState.value.dashboardState.visibleCourseIds
        assignmentsRealtime = assignmentRepository.listenAssignmentsForCourses(
            courseIds = visibleCourseIds,
            onUpdate = { assignments ->
                _uiState.update {
                    it.copy(dashboardState = it.dashboardState.copy(assignments = assignments))
                }
            },
            onError = { error ->
                _uiState.update { it.copy(errorMessage = authRepository.userMessage(error)) }
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
            },
            onError = { /* non-blocking: catalog simply stays empty if unavailable */ }
        )

        conversationsRealtime = chatRepository.listenDirectConversations(
            uid = user.uid,
            onUpdate = { conversations ->
                _uiState.update { it.copy(chat = it.chat.copy(directConversations = conversations)) }
            },
            onError = { /* non-blocking: conversation list stays empty if unavailable */ }
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
            onError = { /* non-blocking: notifications require deployed Cloud Functions */ }
        )

        mySubmissionsRealtime = if (user.hasRole(UserRole.STUDENT)) {
            assignmentRepository.listenMySubmissions(
                studentUid = user.uid,
                onUpdate = { submissions ->
                    _uiState.update {
                        it.copy(dashboardState = it.dashboardState.copy(mySubmissions = submissions))
                    }
                },
                onError = { /* non-blocking: submissions simply won't refresh live */ }
            )
        } else {
            null
        }

        loadChatContacts(user)
    }

    private fun loadChatContacts(user: AppUser) {
        viewModelScope.launch {
            try {
                val tutors = _uiState.value.dashboardState.tutors
                val contacts = tutors
                    .filter { it.uid != user.uid }
                    .map { ChatContactUi(uid = it.uid, displayName = it.displayName, subtitle = it.subjects) }
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
        notificationsRealtime?.remove()
        tutoringRealtime = null
        assignmentsRealtime = null
        mySubmissionsRealtime = null
        coursesRealtime = null
        conversationsRealtime = null
        messagesRealtime = null
        notificationsRealtime = null
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }
}
