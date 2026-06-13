package Smart.Campus.PWR.tutoring

import Smart.Campus.PWR.ui.state.TutorReviewUi
import org.junit.Assert.assertEquals
import org.junit.Test

class RatingAggregationTest {

    private fun review(
        id: String = "r",
        tutorId: String,
        rating: Int,
        studentId: String = "s"
    ) = TutorReviewUi(
        id = id,
        tutorId = tutorId,
        tutorDisplayName = "Tutor $tutorId",
        studentId = studentId,
        studentDisplayName = "Student",
        rating = rating,
        comment = "",
        createdAtLabel = ""
    )

    // ─── aggregateRatings ────────────────────────────────────────────────────

    @Test
    fun aggregateRatings_emptyList_returnsEmptyMap() {
        val result = TutorRatings.aggregateRatings(emptyList())
        assertEquals(emptyMap<String, Any>(), result)
    }

    @Test
    fun aggregateRatings_singleReview_correctStats() {
        val reviews = listOf(review(tutorId = "t1", rating = 4))
        val stats = TutorRatings.aggregateRatings(reviews)["t1"]!!
        assertEquals(4.0, stats.avg, 0.001)
        assertEquals(1, stats.count)
        assertEquals(listOf(0, 1, 0, 0, 0), stats.dist)
    }

    @Test
    fun aggregateRatings_multipleReviews_correctAvg() {
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t1", rating = 3)
        )
        val stats = TutorRatings.aggregateRatings(reviews)["t1"]!!
        assertEquals(4.0, stats.avg, 0.001)
        assertEquals(2, stats.count)
        assertEquals(listOf(1, 0, 1, 0, 0), stats.dist)
    }

    @Test
    fun aggregateRatings_avgRoundedToOneDecimal() {
        // avg = (5+4+3) / 3 = 4.0 exactly
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t1", rating = 4),
            review(id = "r3", tutorId = "t1", rating = 3)
        )
        val stats = TutorRatings.aggregateRatings(reviews)["t1"]!!
        assertEquals(4.0, stats.avg, 0.001)
    }

    @Test
    fun aggregateRatings_avgRoundedToOneDecimal_nonExact() {
        // avg = (5+4) / 3 ≈ 3.0  → actually (5+4+1)/3 = 10/3 = 3.333... → 3.3
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t1", rating = 4),
            review(id = "r3", tutorId = "t1", rating = 1)
        )
        val stats = TutorRatings.aggregateRatings(reviews)["t1"]!!
        assertEquals(3.3, stats.avg, 0.001)
    }

    @Test
    fun aggregateRatings_groupsByTutorId() {
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t2", rating = 2)
        )
        val result = TutorRatings.aggregateRatings(reviews)
        assertEquals(5.0, result["t1"]!!.avg, 0.001)
        assertEquals(2.0, result["t2"]!!.avg, 0.001)
    }

    @Test
    fun aggregateRatings_distIndexOrder_5starIsIndex0() {
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t1", rating = 5),
            review(id = "r3", tutorId = "t1", rating = 1)
        )
        val dist = TutorRatings.aggregateRatings(reviews)["t1"]!!.dist
        assertEquals(2, dist[0]) // 5★
        assertEquals(0, dist[1]) // 4★
        assertEquals(0, dist[2]) // 3★
        assertEquals(0, dist[3]) // 2★
        assertEquals(1, dist[4]) // 1★
    }

    // ─── tutorRatingStats ────────────────────────────────────────────────────

    @Test
    fun tutorRatingStats_noReviews_returnsZeroed() {
        val stats = TutorRatings.tutorRatingStats("unknown", emptyList())
        assertEquals(0.0, stats.avg, 0.001)
        assertEquals(0, stats.count)
        assertEquals(listOf(0, 0, 0, 0, 0), stats.dist)
    }

    @Test
    fun tutorRatingStats_filtersToRequestedTutor() {
        val reviews = listOf(
            review(id = "r1", tutorId = "t1", rating = 5),
            review(id = "r2", tutorId = "t2", rating = 1)
        )
        val stats = TutorRatings.tutorRatingStats("t1", reviews)
        assertEquals(5.0, stats.avg, 0.001)
        assertEquals(1, stats.count)
    }
}
