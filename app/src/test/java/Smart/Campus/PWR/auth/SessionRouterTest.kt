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
    fun resolve_returnsRolePickerForDualRole() {
        val user = AppUser(
            uid = "2",
            login = "dual",
            email = "dual@smartcampus.local",
            displayName = "Dual",
            roles = setOf(UserRole.STUDENT, UserRole.LECTURER),
            isActive = true
        )

        assertEquals(SessionDestination.ROLE_PICKER, SessionRouter.resolve(user))
    }

    @Test
    fun resolve_returnsSingleRoleHome() {
        val student = AppUser(
            uid = "3",
            login = "student",
            email = "student@smartcampus.local",
            displayName = "Student",
            roles = setOf(UserRole.STUDENT),
            isActive = true
        )

        val lecturer = AppUser(
            uid = "4",
            login = "lecturer",
            email = "lecturer@smartcampus.local",
            displayName = "Lecturer",
            roles = setOf(UserRole.LECTURER),
            isActive = true
        )

        assertEquals(SessionDestination.ROLE_HOME_STUDENT, SessionRouter.resolve(student))
        assertEquals(SessionDestination.ROLE_HOME_LECTURER, SessionRouter.resolve(lecturer))
    }
}
