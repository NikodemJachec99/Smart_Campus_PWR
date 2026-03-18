package Smart.Campus.PWR.auth

object AuthMapping {
    private const val LOGIN_DOMAIN = "smartcampus.local"

    fun aliasToEmail(loginOrEmail: String): String {
        val normalized = loginOrEmail.trim().lowercase()
        if (normalized.isBlank()) {
            return normalized
        }

        return if (normalized.contains("@")) {
            normalized
        } else {
            "$normalized@$LOGIN_DOMAIN"
        }
    }

    fun normalizeLogin(loginOrEmail: String): String {
        val normalized = loginOrEmail.trim().lowercase()
        if (normalized.isBlank()) {
            return normalized
        }

        return if (normalized.contains("@")) {
            normalized.substringBefore("@")
        } else {
            normalized
        }
    }

    fun loginFromEmail(email: String): String = email.substringBefore("@").lowercase()

    fun parseRoles(rolesField: Any?, legacyRoleField: String?): Set<UserRole> {
        val parsed = mutableSetOf<UserRole>()

        when (rolesField) {
            is List<*> -> rolesField.forEach { item ->
                if (item is String) {
                    parseRoleTokens(item).forEach(parsed::add)
                }
            }

            is String -> parseRoleTokens(rolesField).forEach(parsed::add)
        }

        if (parsed.isEmpty() && !legacyRoleField.isNullOrBlank()) {
            parseRoleTokens(legacyRoleField).forEach(parsed::add)
        }

        return parsed
    }

    private fun parseRoleTokens(rawRole: String): Set<UserRole> {
        return rawRole
            .split(",", ";", " ")
            .mapNotNull { token -> UserRole.fromRaw(token) }
            .toSet()
    }
}
