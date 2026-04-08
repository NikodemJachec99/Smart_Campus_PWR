package Smart.Campus.PWR.auth

enum class UserRole(val raw: String, val displayName: String) {
    ADMIN("admin", "Admin"),
    STUDENT("student", "Student"),
    TUTOR("tutor", "Tutor");

    companion object {
        fun fromRaw(value: String?): UserRole? {
            if (value.isNullOrBlank()) {
                return null
            }

            return when (value.trim().lowercase()) {
                "lecturer", "wykladowca" -> TUTOR
                else -> entries.firstOrNull { it.raw == value.trim().lowercase() }
            }
        }
    }
}