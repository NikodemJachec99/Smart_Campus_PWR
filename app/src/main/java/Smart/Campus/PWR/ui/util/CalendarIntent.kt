package Smart.Campus.PWR.ui.util

import android.content.Intent
import android.provider.CalendarContract

/**
 * Returns a map of Calendar event extras, keyed by CalendarContract constants.
 * Blank [location] and [description] are omitted from the result.
 *
 * This is a pure function — no Android context required — making it unit-testable.
 */
fun calendarEventFields(
    title: String,
    beginMillis: Long,
    endMillis: Long,
    location: String,
    description: String
): Map<String, Any> = buildMap {
    put(CalendarContract.Events.TITLE, title)
    put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
    put(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
    if (location.isNotBlank()) put(CalendarContract.Events.EVENT_LOCATION, location)
    if (description.isNotBlank()) put(CalendarContract.Events.DESCRIPTION, description)
}

/**
 * Builds an ACTION_INSERT intent that opens the system calendar to add a new event
 * pre-filled with the given fields.
 */
fun buildAddToCalendarIntent(
    title: String,
    beginMillis: Long,
    endMillis: Long,
    location: String,
    description: String
): Intent {
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
    calendarEventFields(title, beginMillis, endMillis, location, description)
        .forEach { (key, value) ->
            when (value) {
                is String -> intent.putExtra(key, value)
                is Long   -> intent.putExtra(key, value)
                else      -> intent.putExtra(key, value.toString())
            }
        }
    return intent
}
