package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.FirebaseAuthRepository
import Smart.Campus.PWR.auth.SessionDestination
import Smart.Campus.PWR.auth.SessionRouter
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.tutoring.TutoringRepository
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.AvailabilityFormState
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.ReportFormState
import Smart.Campus.PWR.ui.state.ReviewFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartCampusViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val tutoringRepository: TutoringRepository = TutoringRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartCampusUiState())
    val uiState: StateFlow<SmartCampusUiState> = _uiState.asStateFlow()

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
        authRepository.signOut()
        _uiState.update {
            SmartCampusUiState(
                isBootstrapping = false,
                screen = AppScreen.LOGIN,
                loginInput = it.loginInput,
                passwordInput = "",
                infoMessage = "Signed out."
            )
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
        val start = form.startHour.toIntOrNull()
        val end = form.endHour.toIntOrNull()

        if (start == null || end == null) {
            _uiState.update { it.copy(errorMessage = "Start and end hour must be numeric.") }
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

    fun onReviewTutorChanged(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(tutorUid = value)) }
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
        val user = _uiState.value.currentUser
        if (user == null || !user.hasRole(UserRole.ADMIN)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminUsersLoading = true, errorMessage = null, infoMessage = null) }
            try {
                val users = authRepository.listUsers()
                val reports = tutoringRepository.loadAdminReports()
                _uiState.update {
                    it.copy(
                        isAdminUsersLoading = false,
                        adminUsers = users,
                        dashboardState = it.dashboardState.copy(adminReports = reports)
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
                            screen = AppScreen.LOGIN,
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
                        screen = AppScreen.LOGIN,
                        errorMessage = authRepository.userMessage(error)
                    )
                }
            }
        }
    }

    private suspend fun applyRoutingForUser(user: AppUser) {
        when (SessionRouter.resolve(user)) {
            SessionDestination.ADMIN_PANEL -> {
                val users = authRepository.listUsers()
                val reports = tutoringRepository.loadAdminReports()
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ADMIN_PANEL,
                        currentUser = user,
                        activeRole = null,
                        adminUsers = users,
                        dashboardState = DashboardUiState(adminReports = reports),
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
                            screen = AppScreen.LOGIN,
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
                        screen = AppScreen.LOGIN,
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
            _uiState.update {
                it.copy(
                    dashboardState = dashboard.copy(isLoading = false)
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
}