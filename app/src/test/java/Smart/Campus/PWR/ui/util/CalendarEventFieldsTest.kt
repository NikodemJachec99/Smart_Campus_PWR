package Smart.Campus.PWR.ui.util

import android.provider.CalendarContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarEventFieldsTest {

    private val title       = "Calculus I — tutoring"
    private val beginMillis = 1_700_000_000_000L
    private val endMillis   = 1_700_003_600_000L

    @Test
    fun `map contains title`() {
        val map = calendarEventFields(title, beginMillis, endMillis, "", "")
        assertEquals(title, map[CalendarContract.Events.TITLE])
    }

    @Test
    fun `map contains begin time`() {
        val map = calendarEventFields(title, beginMillis, endMillis, "", "")
        assertEquals(beginMillis, map[CalendarContract.EXTRA_EVENT_BEGIN_TIME])
    }

    @Test
    fun `map contains end time`() {
        val map = calendarEventFields(title, beginMillis, endMillis, "", "")
        assertEquals(endMillis, map[CalendarContract.EXTRA_EVENT_END_TIME])
    }

    @Test
    fun `map omits blank location`() {
        val map = calendarEventFields(title, beginMillis, endMillis, "  ", "")
        assertFalse(
            "location key must be absent when location is blank",
            map.containsKey(CalendarContract.Events.EVENT_LOCATION)
        )
    }

    @Test
    fun `map omits blank description`() {
        val map = calendarEventFields(title, beginMillis, endMillis, "", "  ")
        assertFalse(
            "description key must be absent when description is blank",
            map.containsKey(CalendarContract.Events.DESCRIPTION)
        )
    }

    @Test
    fun `map includes non-blank location`() {
        val location = "B4 building, room 204"
        val map = calendarEventFields(title, beginMillis, endMillis, location, "")
        assertTrue(map.containsKey(CalendarContract.Events.EVENT_LOCATION))
        assertEquals(location, map[CalendarContract.Events.EVENT_LOCATION])
    }

    @Test
    fun `map includes non-blank description`() {
        val desc = "Bring your lecture notes."
        val map = calendarEventFields(title, beginMillis, endMillis, "", desc)
        assertTrue(map.containsKey(CalendarContract.Events.DESCRIPTION))
        assertEquals(desc, map[CalendarContract.Events.DESCRIPTION])
    }
}
