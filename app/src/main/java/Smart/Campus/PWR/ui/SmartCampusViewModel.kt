package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.FirebaseAuthRepository
import Smart.Campus.PWR.auth.SessionDestination
import Smart.Campus.PWR.auth.SessionRouter
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.dashboard.DashboardRepository
import Smart.Campus.PWR.ui.state.AiTutorPlanUi
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.HomeUserSummaryUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.UpcomingClassUi
import Smart.Campus.PWR.ui.state.XpSummaryUi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartCampusViewModel(
    private val repository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val dashboardRepository: DashboardRepository = DashboardRepository()
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

    fun onCreateLecturerChecked(value: Boolean) {
        _uiState.update { it.copy(createUserForm = it.createUserForm.copy(lecturer = value)) }
    }

    fun login() {
        val state = _uiState.value
        if (state.loginInput.isBlank() || state.passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Podaj login i haslo.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, infoMessage = null) }
            try {
                val user = repository.signIn(state.loginInput, state.passwordInput)
                applyRoutingForUser(user)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isBusy = false, errorMessage = repository.userMessage(error))
                }
            }
        }
    }

    fun logout() {
        repository.signOut()
        _uiState.update {
            SmartCampusUiState(
                isBootstrapping = false,
                screen = AppScreen.LOGIN,
                loginInput = it.loginInput,
                passwordInput = "",
                infoMessage = "Wylogowano."
            )
        }
    }

    fun selectRole(role: UserRole) {
        val currentUser = _uiState.value.currentUser ?: return
        if (!currentUser.hasRole(role)) {
            _uiState.update { it.copy(errorMessage = "Brak uprawnien do tej roli.") }
            return
        }

        viewModelScope.launch {
            navigateToMainShell(currentUser, role)
        }
    }

    fun openRolePicker() {
        val currentUser = _uiState.value.currentUser ?: return
        if (!currentUser.hasDualRole()) {
            return
        }

        _uiState.update {
            it.copy(
                screen = AppScreen.ROLE_PICKER,
                activeRole = null,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun refreshDashboard() {
        val user = _uiState.value.currentUser ?: return
        val activeRole = _uiState.value.activeRole ?: return

        viewModelScope.launch {
            loadDashboard(user, activeRole)
        }
    }

    fun refreshAdminUsers() {
        val user = _uiState.value.currentUser
        if (user == null || !user.hasRole(UserRole.ADMIN)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminUsersLoading = true, errorMessage = null, infoMessage = null) }
            try {
                val users = repository.listUsers()
                _uiState.update {
                    it.copy(isAdminUsersLoading = false, adminUsers = users)
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminUsersLoading = false, errorMessage = repository.userMessage(error))
                }
            }
        }
    }

    fun createUserByAdmin() {
        val form = _uiState.value.createUserForm

        if (!form.admin && !form.student && !form.lecturer) {
            _uiState.update { it.copy(errorMessage = "Zaznacz co najmniej jedna role.") }
            return
        }

        if (form.login.isBlank() || form.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Login i haslo sa wymagane.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                repository.adminCreateUser(
                    login = form.login,
                    password = form.password,
                    displayName = form.displayName,
                    admin = form.admin,
                    student = form.student,
                    lecturer = form.lecturer
                )

                val users = repository.listUsers()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        adminUsers = users,
                        infoMessage = "Uzytkownik zostal dodany.",
                        createUserForm = CreateUserFormState()
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminSubmitting = false, errorMessage = repository.userMessage(error))
                }
            }
        }
    }

    fun updateUserRolesByAdmin(uid: String, student: Boolean, lecturer: Boolean) {
        if (!student && !lecturer) {
            _uiState.update { it.copy(errorMessage = "Uzytkownik musi miec przynajmniej jedna role nie-admin.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdminSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                repository.adminUpdateUserRoles(uid, student, lecturer)
                val users = repository.listUsers()
                _uiState.update {
                    it.copy(
                        isAdminSubmitting = false,
                        adminUsers = users,
                        infoMessage = "Role zostaly zaktualizowane."
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isAdminSubmitting = false, errorMessage = repository.userMessage(error))
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
                val user = repository.getCurrentUser()
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
                        errorMessage = repository.userMessage(error)
                    )
                }
            }
        }
    }

    private suspend fun applyRoutingForUser(user: AppUser) {
        when (SessionRouter.resolve(user)) {
            SessionDestination.ADMIN_PANEL -> {
                val users = repository.listUsers()
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ADMIN_PANEL,
                        currentUser = user,
                        activeRole = null,
                        adminUsers = users,
                        passwordInput = "",
                        errorMessage = null
                    )
                }
            }

            SessionDestination.ROLE_PICKER -> {
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.ROLE_PICKER,
                        currentUser = user,
                        activeRole = null,
                        passwordInput = "",
                        errorMessage = null
                    )
                }
            }

            SessionDestination.ROLE_HOME_STUDENT -> navigateToMainShell(user, UserRole.STUDENT)
            SessionDestination.ROLE_HOME_LECTURER -> navigateToMainShell(user, UserRole.LECTURER)
            SessionDestination.LOGIN -> {
                _uiState.update {
                    it.copy(
                        isBootstrapping = false,
                        isBusy = false,
                        screen = AppScreen.LOGIN,
                        currentUser = null,
                        activeRole = null,
                        errorMessage = "Brak przypisanej roli dla konta."
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
                dashboardState = buildDashboardState(
                    user = user,
                    activeRole = role,
                    upcomingClasses = emptyList(),
                    isLoading = true
                )
            )
        }

        loadDashboard(user, role)
    }

    private suspend fun loadDashboard(user: AppUser, activeRole: UserRole) {
        _uiState.update {
            it.copy(
                dashboardState = it.dashboardState.copy(isLoading = true),
                errorMessage = null
            )
        }

        val upcomingClasses = try {
            dashboardRepository.getUpcomingClasses(user, activeRole)
        } catch (_: Exception) {
            emptyList()
        }

        _uiState.update {
            it.copy(
                dashboardState = buildDashboardState(
                    user = user,
                    activeRole = activeRole,
                    upcomingClasses = upcomingClasses,
                    isLoading = false
                )
            )
        }
    }

    private fun buildDashboardState(
        user: AppUser,
        activeRole: UserRole,
        upcomingClasses: List<UpcomingClassUi>,
        isLoading: Boolean
    ): DashboardUiState {
        val roleLabel = when (activeRole) {
            UserRole.STUDENT -> "Tryb Student"
            UserRole.LECTURER -> "Tryb Wykladowca"
            UserRole.ADMIN -> "Tryb Admin"
        }

        val greeting = when (activeRole) {
            UserRole.STUDENT -> "Dzien dobry,"
            UserRole.LECTURER -> "Witaj ponownie,"
            UserRole.ADMIN -> "Witaj,"
        }

        val xpSummary = when (activeRole) {
            UserRole.STUDENT -> XpSummaryUi(
                currentXp = 1250,
                targetXp = 2000,
                helperLabel = "Modul gamifikacji jest w wersji demonstracyjnej",
                badges = listOf("XP", "AI"),
                isPlaceholder = true
            )
            UserRole.LECTURER -> XpSummaryUi(
                currentXp = 890,
                targetXp = 1500,
                helperLabel = "Statystyki aktywnosci prowadzacego sa w przygotowaniu",
                badges = listOf("LE", "AI"),
                isPlaceholder = true
            )
            UserRole.ADMIN -> XpSummaryUi(
                currentXp = 0,
                targetXp = 1,
                helperLabel = "Dashboard admina pozostaje osobnym ekranem",
                badges = emptyList(),
                isPlaceholder = true
            )
        }

        val aiPlan = when (activeRole) {
            UserRole.STUDENT -> AiTutorPlanUi(
                title = "AI Tutor: Twoj plan na dzis",
                description = "Podlacz modul AI, aby otrzymywac zadania dopasowane do Twojego poziomu i terminow.",
                taskTitle = "Brak aktywnego planu AI",
                estimatedTimeLabel = "Oczekuje na integracje",
                isPlaceholder = true
            )
            UserRole.LECTURER -> AiTutorPlanUi(
                title = "AI Assistant: wsparcie prowadzacego",
                description = "Tutaj pojawia sie rekomendacje materialow, streszczenia zajec i wskazowki dla grup.",
                taskTitle = "Brak aktywnych rekomendacji",
                estimatedTimeLabel = "Oczekuje na integracje",
                isPlaceholder = true
            )
            UserRole.ADMIN -> null
        }

        return DashboardUiState(
            isLoading = isLoading,
            userSummary = HomeUserSummaryUi(
                displayName = user.displayName,
                greeting = greeting,
                roleLabel = roleLabel,
                avatarUrl = user.avatarUrl,
                initials = user.initials(),
                notificationCount = 0
            ),
            xpSummary = xpSummary,
            gpsAlarm = null,
            aiTutorPlan = aiPlan,
            upcomingClasses = upcomingClasses
        )
    }
}

