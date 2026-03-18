package Smart.Campus.PWR.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthMappingTest {
    @Test
    fun aliasToEmail_mapsAliasToDomainEmail() {
        assertEquals("admin@smartcampus.local", AuthMapping.aliasToEmail("admin"))
    }

    @Test
    fun aliasToEmail_keepsProvidedEmail() {
        assertEquals("x@y.z", AuthMapping.aliasToEmail("x@y.z"))
    }

    @Test
    fun parseRoles_supportsArrayAndLegacySingleRole() {
        val fromArray = AuthMapping.parseRoles(listOf("student", "lecturer"), null)
        assertTrue(fromArray.contains(UserRole.STUDENT))
        assertTrue(fromArray.contains(UserRole.LECTURER))

        val fromLegacy = AuthMapping.parseRoles(null, "student")
        assertEquals(setOf(UserRole.STUDENT), fromLegacy)
    }
}
