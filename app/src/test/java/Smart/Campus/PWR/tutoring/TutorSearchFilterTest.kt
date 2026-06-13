package Smart.Campus.PWR.tutoring

import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorSearchFilterState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorSearchFilterTest {

    private fun slot(
        id: String = "s",
        tutorId: String = "t1",
        tutorDisplayName: String = "Alice",
        subject: String = "Math",
        dateLabel: String = "2025-06-10",
        startHour: String = "10:00",
        format: String = "ONLINE"
    ) = TutorAvailabilityUi(
        id = id,
        tutorId = tutorId,
        tutorDisplayName = tutorDisplayName,
        subject = subject,
        dateLabel = dateLabel,
        startHour = startHour,
        endHour = "11:00",
        isBooked = false,
        format = format
    )

    private val today = "2025-06-10"
    private val noRatings = emptyMap<String, Double>()

    // ─── subject filter ──────────────────────────────────────────────────────

    @Test
    fun subjectFilter_matchesSubstring_caseInsensitive() {
        val slots = listOf(slot(subject = "Mathematics"), slot(id = "s2", subject = "Physics"))
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(subjectQuery = "math"), today
        )
        assertEquals(1, result.size)
        assertEquals("Mathematics", result[0].subject)
    }

    @Test
    fun subjectFilter_blank_returnsAll() {
        val slots = listOf(slot(id = "s1"), slot(id = "s2", subject = "Physics"))
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(subjectQuery = ""), today
        )
        assertEquals(2, result.size)
    }

    // ─── tutor filter ────────────────────────────────────────────────────────

    @Test
    fun tutorFilter_matchesTutorDisplayName() {
        val slots = listOf(
            slot(id = "s1", tutorDisplayName = "Alice Smith"),
            slot(id = "s2", tutorDisplayName = "Bob Jones")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(tutorQuery = "alice"), today
        )
        assertEquals(1, result.size)
        assertEquals("Alice Smith", result[0].tutorDisplayName)
    }

    // ─── date filter ─────────────────────────────────────────────────────────

    @Test
    fun dateFilter_exactMatch() {
        val slots = listOf(
            slot(id = "s1", dateLabel = "2025-06-10"),
            slot(id = "s2", dateLabel = "2025-06-11")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(date = "2025-06-10"), today
        )
        assertEquals(1, result.size)
        assertEquals("2025-06-10", result[0].dateLabel)
    }

    // ─── format filter ───────────────────────────────────────────────────────

    @Test
    fun formatFilter_excludesDifferentFormat() {
        val slots = listOf(
            slot(id = "s1", format = "ONLINE"),
            slot(id = "s2", format = "IN_PERSON")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(format = "ONLINE"), today
        )
        assertEquals(1, result.size)
        assertEquals("ONLINE", result[0].format)
    }

    @Test
    fun formatFilter_null_returnsAll() {
        val slots = listOf(
            slot(id = "s1", format = "ONLINE"),
            slot(id = "s2", format = "IN_PERSON")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(format = null), today
        )
        assertEquals(2, result.size)
    }

    // ─── minRating filter ────────────────────────────────────────────────────

    @Test
    fun minRatingFilter_excludesLowRated() {
        val slots = listOf(
            slot(id = "s1", tutorId = "t1"),
            slot(id = "s2", tutorId = "t2")
        )
        val ratings = mapOf("t1" to 4.5, "t2" to 2.0)
        val result = TutorRatings.applyTutorSearch(
            slots, ratings, TutorSearchFilterState(minRating = 4), today
        )
        assertEquals(1, result.size)
        assertEquals("t1", result[0].tutorId)
    }

    @Test
    fun minRatingFilter_null_returnsAll() {
        val slots = listOf(slot(id = "s1", tutorId = "t1"), slot(id = "s2", tutorId = "t2"))
        val ratings = mapOf("t1" to 4.5, "t2" to 1.0)
        val result = TutorRatings.applyTutorSearch(
            slots, ratings, TutorSearchFilterState(minRating = null), today
        )
        assertEquals(2, result.size)
    }

    @Test
    fun minRatingFilter_tutorWithNoRatingTreatedAsZero() {
        val slots = listOf(slot(id = "s1", tutorId = "unknown"))
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(minRating = 3), today
        )
        assertTrue(result.isEmpty())
    }

    // ─── availableToday filter ───────────────────────────────────────────────

    @Test
    fun availableTodayFilter_excludesOtherDays() {
        val slots = listOf(
            slot(id = "s1", dateLabel = today),
            slot(id = "s2", dateLabel = "2025-06-15")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(availableToday = true), today
        )
        assertEquals(1, result.size)
        assertEquals(today, result[0].dateLabel)
    }

    @Test
    fun availableTodayFilter_false_returnsAll() {
        val slots = listOf(
            slot(id = "s1", dateLabel = today),
            slot(id = "s2", dateLabel = "2025-06-15")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(availableToday = false), today
        )
        assertEquals(2, result.size)
    }

    // ─── sort orders ─────────────────────────────────────────────────────────

    @Test
    fun sort_topRated_sortsByRatingDescThenDate() {
        val slots = listOf(
            slot(id = "s1", tutorId = "t1", dateLabel = "2025-06-11"),
            slot(id = "s2", tutorId = "t2", dateLabel = "2025-06-10"),
            slot(id = "s3", tutorId = "t1", dateLabel = "2025-06-10")
        )
        val ratings = mapOf("t1" to 4.8, "t2" to 3.5)
        val result = TutorRatings.applyTutorSearch(
            slots, ratings, TutorSearchFilterState(sort = "TOP_RATED"), today
        )
        // t1 slots first (higher rating), then by date
        assertEquals("t1", result[0].tutorId)
        assertEquals("2025-06-10", result[0].dateLabel)
        assertEquals("t1", result[1].tutorId)
        assertEquals("2025-06-11", result[1].dateLabel)
        assertEquals("t2", result[2].tutorId)
    }

    @Test
    fun sort_soonest_sortsByDateThenHour() {
        val slots = listOf(
            slot(id = "s1", dateLabel = "2025-06-11", startHour = "09:00"),
            slot(id = "s2", dateLabel = "2025-06-10", startHour = "14:00"),
            slot(id = "s3", dateLabel = "2025-06-10", startHour = "08:00")
        )
        val result = TutorRatings.applyTutorSearch(
            slots, noRatings, TutorSearchFilterState(sort = "SOONEST"), today
        )
        assertEquals("s3", result[0].id)
        assertEquals("s2", result[1].id)
        assertEquals("s1", result[2].id)
    }

    // ─── combined filters ────────────────────────────────────────────────────

    @Test
    fun combinedFilter_subjectAndMinRating() {
        val slots = listOf(
            slot(id = "s1", tutorId = "t1", subject = "Math"),
            slot(id = "s2", tutorId = "t2", subject = "Math"),
            slot(id = "s3", tutorId = "t1", subject = "Physics")
        )
        val ratings = mapOf("t1" to 4.0, "t2" to 2.0)
        val result = TutorRatings.applyTutorSearch(
            slots, ratings, TutorSearchFilterState(subjectQuery = "math", minRating = 3), today
        )
        assertEquals(1, result.size)
        assertEquals("t1", result[0].tutorId)
        assertEquals("Math", result[0].subject)
    }
}
