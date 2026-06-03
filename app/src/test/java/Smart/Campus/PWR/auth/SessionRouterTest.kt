package Smart.Campus.PWR.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionRouterTest {
    @Test
    fun resolve_returnsAdminPanelForAdminUser() {
        val user = AppUser(
            uid = "1",
            login = "admin",
            email = "admin@smartcampus.local",
            displayName = "Admin",
            roles = setOf(UserRole.ADMIN),
            isActive = true
        )

        assertEquals(SessionDestination.ADMIN_PANEL, SessionRouter.resolve(user))
    }

    @Test
    fun resolve_returnsMainShellForDualRole() {
        val user = AppUser(
            uid = "2",
            login = "dual",
            email = "dual@smartcampus.local",
            displayName = "Dual",
            roles = setOf(UserRole.STUDENT, UserRole.TUTOR),
            isActive = true
        )

        assertEquals(SessionDestination.MAIN_SHELL, SessionRouter.resolve(user))
    }

    @Test
    fun resolve_returnsMainShellForStudentOrTutor() {
        val student = AppUser(
            uid = "3",
            login = "student",
            email = "student@smartcampus.local",
            displayName = "Student",
            roles = setOf(UserRole.STUDENT),
            isActive = true
        )

        val tutor = AppUser(
            uid = "4",
            login = "tutor",
            email = "tutor@smartcampus.local",
            displayName = "Tutor",
            roles = setOf(UserRole.TUTOR),
            isActive = true
        )

        assertEquals(SessionDestination.MAIN_SHELL, SessionRouter.resolve(student))
        assertEquals(SessionDestination.MAIN_SHELL, SessionRouter.resolve(tutor))
    }
}
