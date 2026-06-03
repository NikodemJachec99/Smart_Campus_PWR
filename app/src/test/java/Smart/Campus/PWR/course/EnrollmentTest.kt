package Smart.Campus.PWR.course

import org.junit.Assert.assertEquals
import org.junit.Test

class EnrollmentTest {
    @Test
    fun extractsParentCourseIdsFromMemberPaths() {
        val paths = listOf(
            "courses/abc/members/u1",
            "courses/def/members/u1",
            "courses/abc/members/u1"
        )
        assertEquals(listOf("abc", "def"), CoursePaths.courseIdsFromMemberPaths(paths))
    }
}
