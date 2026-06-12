package Smart.Campus.PWR.tutoring

object BookingRules {

    fun canReview(status: String): Boolean = status == "COMPLETED"

    fun isPending(status: String): Boolean = status == "PENDING"

    /**
     * Normalises legacy/raw booking status strings to canonical upper-case values.
     *
     * Rules:
     *  - null / blank → "CONFIRMED"  (documents written before the PENDING flow had no status field)
     *  - "booked" (any case) → "CONFIRMED"  (old `bookAvailability` wrote "booked")
     *  - anything else → returned as-is
     */
    fun normalizeLegacyStatus(raw: String?): String {
        if (raw.isNullOrBlank()) return "CONFIRMED"
        if (raw.equals("booked", ignoreCase = true)) return "CONFIRMED"
        return raw
    }
}
