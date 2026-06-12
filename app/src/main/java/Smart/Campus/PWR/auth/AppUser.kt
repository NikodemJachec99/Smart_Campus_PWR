package Smart.Campus.PWR.auth

data class AppUser(
    val uid: String,
    val login: String,
    val email: String,
    val displayName: String,
    val roles: Set<UserRole>,
    val isActive: Boolean,
    val avatarUrl: String? = null,
    val program: String? = null,
    val studyYear: String? = null,
    val faculty: String? = null,
    val bio: String? = null,
    val subjects: List<String> = emptyList(),
    val experienceYears: Int? = null,
    val verified: Boolean = false
) {
    fun hasRole(role: UserRole): Boolean = roles.contains(role)

    fun hasDualRole(): Boolean =
        roles.contains(UserRole.STUDENT) && roles.contains(UserRole.TUTOR)

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