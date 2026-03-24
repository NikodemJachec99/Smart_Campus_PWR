package Smart.Campus.PWR.ui.state

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole

enum class AppScreen {
    LOGIN,
    ROLE_PICKER,
    MAIN_SHELL,
    ADMIN_PANEL
}

data class CreateUserFormState(
    val login: String = "",
    val password: String = "",
    val displayName: String = "",
    val admin: Boolean = false,
    val student: Boolean = true,
    val lecturer: Boolean = false
)

data class SmartCampusUiState(
    val isBootstrapping: Boolean = true,
    val isBusy: Boolean = false,
    val screen: AppScreen = AppScreen.LOGIN,
    val currentUser: AppUser? = null,
    val activeRole: UserRole? = null,
    val loginInput: String = "",
    val passwordInput: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val adminUsers: List<AppUser> = emptyList(),
    val isAdminUsersLoading: Boolean = false,
    val isAdminSubmitting: Boolean = false,
    val createUserForm: CreateUserFormState = CreateUserFormState(),
    val dashboardState: DashboardUiState = DashboardUiState()
)
