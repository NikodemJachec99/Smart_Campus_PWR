package Smart.Campus.PWR.auth

data class AppUser(
    val uid: String,
    val login: String,
    val email: String,
    val displayName: String,
    val roles: Set<UserRole>,
    val isActive: Boolean
) {
    fun hasRole(role: UserRole): Boolean = roles.contains(role)

    fun hasDualRole(): Boolean =
        roles.contains(UserRole.STUDENT) && roles.contains(UserRole.LECTURER)
}
