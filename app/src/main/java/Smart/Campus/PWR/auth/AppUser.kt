package Smart.Campus.PWR.auth

data class AppUser(
    val uid: String,
    val login: String,
    val email: String,
    val displayName: String,
    val roles: Set<UserRole>,
    val isActive: Boolean,
    val avatarUrl: String? = null
) {
    fun hasRole(role: UserRole): Boolean = roles.contains(role)

    fun hasDualRole(): Boolean =
        roles.contains(UserRole.STUDENT) && roles.contains(UserRole.LECTURER)

    fun initials(): String {
        val words = displayName
            .trim()
            .split(" ")
            .filter { part -> part.isNotBlank() }

        return when {
            words.size >= 2 -> "${words.first().first()}${words.last().first()}".uppercase()
            words.isNotEmpty() -> words.first().take(2).uppercase()
            login.isNotBlank() -> login.take(2).uppercase()
            else -> "SC"
        }
    }
}
