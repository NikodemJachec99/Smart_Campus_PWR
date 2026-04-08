package Smart.Campus.PWR.auth

enum class SessionDestination {
    LOGIN,
    ADMIN_PANEL,
    MAIN_SHELL
}

object SessionRouter {
    fun resolve(user: AppUser?): SessionDestination {
        if (user == null || !user.isActive) {
            return SessionDestination.LOGIN
        }

        return when {
            user.hasRole(UserRole.ADMIN) -> SessionDestination.ADMIN_PANEL
            user.hasRole(UserRole.STUDENT) || user.hasRole(UserRole.TUTOR) -> SessionDestination.MAIN_SHELL
            else -> SessionDestination.LOGIN
        }
    }
}