package Smart.Campus.PWR.auth

enum class UserRole(val raw: String, val displayName: String) {
    ADMIN("admin", "Admin"),
    STUDENT("student", "Student"),
    LECTURER("lecturer", "Wykladowca");

    companion object {
        fun fromRaw(value: String?): UserRole? {
            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { it.raw == value.trim().lowercase() }
        }
    }
}
