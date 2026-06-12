package Smart.Campus.PWR.tutoring

import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorRatingStats
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.state.TutorSearchFilterState
import kotlin.math.roundToInt

object TutorRatings {

    /**
     * Groups [reviews] by tutorId and returns a [TutorRatingStats] per tutor.
     *
     * Distribution [dist]:
     *   index 0 = count of 5-star reviews
     *   index 1 = count of 4-star reviews
     *   index 2 = count of 3-star reviews
     *   index 3 = count of 2-star reviews
     *   index 4 = count of 1-star reviews
     */
    fun aggregateRatings(reviews: List<TutorReviewUi>): Map<String, TutorRatingStats> {
        return reviews
            .groupBy { it.tutorId }
            .mapValues { (_, tutorReviews) -> buildStats(tutorReviews) }
    }

    /**
     * Returns [TutorRatingStats] for a single tutor from the full review list.
     * Returns zeroed stats when the tutor has no reviews.
     */
    fun tutorRatingStats(tutorId: String, reviews: List<TutorReviewUi>): TutorRatingStats {
        val tutorReviews = reviews.filter { it.tutorId == tutorId }
        return if (tutorReviews.isEmpty()) {
            TutorRatingStats(avg = 0.0, count = 0, dist = listOf(0, 0, 0, 0, 0))
        } else {
            buildStats(tutorReviews)
        }
    }

    /**
     * Applies [filter] to [slots] and sorts the result.
     *
     * Filters applied (all conditions must match):
     * - [TutorSearchFilterState.subjectQuery]: slot.subject contains query (case-insensitive)
     * - [TutorSearchFilterState.tutorQuery]: slot.tutorDisplayName contains query (case-insensitive)
     * - [TutorSearchFilterState.date]: slot.dateLabel == date (exact)
     * - [TutorSearchFilterState.format]: slot.format == format (exact, null = no filter)
     * - [TutorSearchFilterState.minRating]: ratingByTutor[tutorId] >= minRating (null = no filter)
     * - [TutorSearchFilterState.availableToday]: slot.dateLabel == today (when true)
     *
     * Sort order ([TutorSearchFilterState.sort]):
     * - "TOP_RATED" → ratingByTutor descending, then dateLabel ascending
     * - "SOONEST"   → dateLabel ascending, then startHour ascending
     */
    fun applyTutorSearch(
        slots: List<TutorAvailabilityUi>,
        ratingByTutor: Map<String, Double>,
        filter: TutorSearchFilterState,
        today: String
    ): List<TutorAvailabilityUi> {
        val query = filter.query.trim()
        val subjectQuery = filter.subjectQuery.trim()
        val tutorQuery = filter.tutorQuery.trim()

        val filtered = slots.filter { slot ->
            val matchesQuery = query.isBlank() ||
                    slot.subject.contains(query, ignoreCase = true) ||
                    slot.tutorDisplayName.contains(query, ignoreCase = true)
            val matchesSubject = subjectQuery.isBlank() ||
                    slot.subject.contains(subjectQuery, ignoreCase = true)
            val matchesTutor = tutorQuery.isBlank() ||
                    slot.tutorDisplayName.contains(tutorQuery, ignoreCase = true)
            val matchesDate = filter.date.isBlank() || slot.dateLabel == filter.date
            val matchesFormat = filter.format == null || slot.format == filter.format
            val matchesMinRating = filter.minRating == null ||
                    (ratingByTutor[slot.tutorId] ?: 0.0) >= filter.minRating
            val matchesToday = !filter.availableToday || slot.dateLabel == today

            matchesQuery && matchesSubject && matchesTutor && matchesDate && matchesFormat &&
                    matchesMinRating && matchesToday
        }

        return when (filter.sort) {
            "SOONEST" -> filtered.sortedWith(
                compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour }
            )
            else -> filtered.sortedWith(
                compareByDescending<TutorAvailabilityUi> { ratingByTutor[it.tutorId] ?: 0.0 }
                    .thenBy { it.dateLabel }
            )
        }
    }

    // -------------------------------------------------------------------------

    private fun buildStats(reviews: List<TutorReviewUi>): TutorRatingStats {
        val count = reviews.size
        val avg = if (count == 0) 0.0 else {
            val raw = reviews.sumOf { it.rating }.toDouble() / count
            (raw * 10).roundToInt() / 10.0
        }
        val dist = List(5) { i ->
            val star = 5 - i
            reviews.count { it.rating == star }
        }
        return TutorRatingStats(avg = avg, count = count, dist = dist)
    }
}
