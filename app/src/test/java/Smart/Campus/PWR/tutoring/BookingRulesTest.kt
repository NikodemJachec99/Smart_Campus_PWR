package Smart.Campus.PWR.tutoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingRulesTest {

    @Test
    fun normalizeLegacyStatus_null_returnsConfirmed() {
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus(null))
    }

    @Test
    fun normalizeLegacyStatus_blank_returnsConfirmed() {
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus(""))
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus("   "))
    }

    @Test
    fun normalizeLegacyStatus_booked_returnsConfirmed() {
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus("booked"))
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus("BOOKED"))
        assertEquals("CONFIRMED", BookingRules.normalizeLegacyStatus("Booked"))
    }

    @Test
    fun normalizeLegacyStatus_completed_returnsCompleted() {
        assertEquals("COMPLETED", BookingRules.normalizeLegacyStatus("COMPLETED"))
    }

    @Test
    fun canReview_completed_returnsTrue() {
        assertTrue(BookingRules.canReview("COMPLETED"))
    }

    @Test
    fun canReview_confirmed_returnsFalse() {
        assertFalse(BookingRules.canReview("CONFIRMED"))
    }

    @Test
    fun isPending_pending_returnsTrue() {
        assertTrue(BookingRules.isPending("PENDING"))
    }

    @Test
    fun isPending_other_returnsFalse() {
        assertFalse(BookingRules.isPending("CONFIRMED"))
        assertFalse(BookingRules.isPending("COMPLETED"))
    }
}
