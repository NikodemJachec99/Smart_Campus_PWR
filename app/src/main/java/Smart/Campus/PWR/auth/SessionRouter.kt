package Smart.Campus.PWR.auth

enum class SessionDestination {
    LOGIN,
    ADMIN_PANEL,
    ROLE_PICKER,
    ROLE_HOME_STUDENT,
    ROLE_HOME_LECTURER
}

object SessionRouter {
    fun resolve(user: AppUser?): SessionDestination {
        if (user == null || !user.isActive) {
            return SessionDestination.LOGIN
        }

        return when {
            user.hasRole(UserRole.ADMIN) -> SessionDestination.ADMIN_PANEL
            user.hasDualRole() -> SessionDestination.ROLE_PICKER
            user.hasRole(UserRole.STUDENT) -> SessionDestination.ROLE_HOME_STUDENT
            user.hasRole(UserRole.LECTURER) -> SessionDestination.ROLE_HOME_LECTURER
            else -> SessionDestination.LOGIN
        }
    }
}
