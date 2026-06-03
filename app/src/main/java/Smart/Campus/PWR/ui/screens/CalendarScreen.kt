package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.AvailabilityCard
import Smart.Campus.PWR.ui.components.CalendarDayUi
import Smart.Campus.PWR.ui.components.CalendarStrip
import Smart.Campus.PWR.ui.components.LessonCard
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.Pill
import Smart.Campus.PWR.ui.components.ReceiptCard
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.components.TimePickerDialogWrapper
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun CalendarTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onAvailabilitySubjectChanged: (String) -> Unit,
    onAvailabilityDateChanged: (String) -> Unit,
    onAvailabilityStartHourChanged: (String) -> Unit,
    onAvailabilityEndHourChanged: (String) -> Unit,
    onAddAvailability: () -> Unit,
    onBookTutorSlot: (String) -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit
) {
    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)
        if (activeRole == UserRole.TUTOR) {
            TutorCalendar(
                state = state,
                onClearMessages = onClearMessages,
                onAvailabilitySubjectChanged = onAvailabilitySubjectChanged,
                onAvailabilityDateChanged = onAvailabilityDateChanged,
                onAvailabilityStartHourChanged = onAvailabilityStartHourChanged,
                onAvailabilityEndHourChanged = onAvailabilityEndHourChanged,
                onAddAvailability = onAddAvailability,
                onDeleteAvailability = onDeleteAvailability,
                onCancelBooking = onCancelBooking
            )
        } else {
            StudentCalendar(state, onBookTutorSlot, onCancelBooking)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorCalendar(
    state: SmartCampusUiState,
    onClearMessages: () -> Unit,
    onAvailabilitySubjectChanged: (String) -> Unit,
    onAvailabilityDateChanged: (String) -> Unit,
    onAvailabilityStartHourChanged: (String) -> Unit,
    onAvailabilityEndHourChanged: (String) -> Unit,
    onAddAvailability: () -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )

    val startTimeState = rememberTimePickerState(initialHour = 12, initialMinute = 0)
    val endTimeState = rememberTimePickerState(initialHour = 13, initialMinute = 0)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onAvailabilityDateChanged(formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialogWrapper(
            title = "Select start time",
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = String.format("%02d:%02d", startTimeState.hour, startTimeState.minute)
                    onAvailabilityStartHourChanged(formatted)
                    showStartTimePicker = false
                }) { Text("OK") }
            }
        ) { TimePicker(state = startTimeState) }
    }

    if (showEndTimePicker) {
        TimePickerDialogWrapper(
            title = "Select end time",
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = String.format("%02d:%02d", endTimeState.hour, endTimeState.minute)
                    onAvailabilityEndHourChanged(formatted)
                    showEndTimePicker = false
                }) { Text("OK") }
            }
        ) { TimePicker(state = endTimeState) }
    }

    MonoLabel("My availability", color = PwrNavy)
    Text(
        "When you're open.",
        style = MaterialTheme.typography.headlineLarge,
        color = TextPrimary
    )
    Text(
        "Open a slot, set the subject, and PWr students will see it in their feed.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
    )
    CalendarStrip(days = availabilityCalendarDays(state))

    SectionCard("Set availability") {
        OutlinedTextField(
            value = state.availabilityForm.subject,
            onValueChange = onAvailabilitySubjectChanged,
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
            OutlinedTextField(
                value = state.availabilityForm.date,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Date") },
                trailingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }) {
                OutlinedTextField(
                    value = state.availabilityForm.startHour,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Start") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            Box(modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }) {
                OutlinedTextField(
                    value = state.availabilityForm.endHour,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("End") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        AppPrimaryButton(
            text = "Add slot",
            enabled = state.availabilityForm.subject.isNotBlank()
                && state.availabilityForm.date.isNotBlank()
                && state.availabilityForm.startHour.isNotBlank()
                && state.availabilityForm.endHour.isNotBlank(),
            onClick = { onClearMessages(); onAddAvailability() }
        )
    }

    Text(
        "Scheduled lessons",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
    val bookedLessons = state.dashboardState.myTutorBookings.filter { it.status == "booked" }
    if (bookedLessons.isEmpty()) Text("No scheduled lessons.", color = TextSecondary)
    bookedLessons.forEach { LessonCard(it, onCancel = onCancelBooking) }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        "Open availability slots",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
    val openSlots = state.dashboardState.myAvailability.filter { !it.isBooked }
    if (openSlots.isEmpty()) Text("No open slots.", color = TextSecondary)
    openSlots.forEach { AvailabilityCard(it, onBook = null, onDelete = onDeleteAvailability) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentCalendar(
    state: SmartCampusUiState,
    onBookTutorSlot: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterDate by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )
    val subjects = remember(state.dashboardState.availableTutorSlots) {
        listOf("All") + state.dashboardState.availableTutorSlots
            .map { it.subject }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .take(5)
    }

    MonoLabel("Discover")
    Text(
        "Find a tutor.",
        style = MaterialTheme.typography.headlineLarge,
        color = TextPrimary
    )
    Text(
        "Search by subject, tutor, or next open slot. Live bookings use the current tutoring repository.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        subjects.forEach { label ->
            Pill(
                text = label,
                selected = selectedSubject == label,
                onClick = { selectedSubject = label }
            )
        }
    }

    if (state.infoMessage?.contains("booked", ignoreCase = true) == true) {
        val booked = state.dashboardState.myStudentBookings.firstOrNull()
        ReceiptCard(
            title = booked?.subject ?: "Lesson booked",
            subtitle = booked?.let { "with ${it.tutorDisplayName}" } ?: "Your tutor session is ready.",
            rows = listOf(
                "Date" to (booked?.dateLabel ?: "Next available"),
                "Time" to (booked?.timeLabel ?: "Check calendar"),
                "Format" to "Online / campus",
                "Total" to "Confirmed"
            ),
            note = "Cancel free up to 12h before."
        )
    }

    Text(
        "My booked lessons",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
    if (state.dashboardState.myStudentBookings.isEmpty()) {
        Text("No booked lessons.", color = TextSecondary)
    }
    state.dashboardState.myStudentBookings.forEach { LessonCard(it, onCancel = onCancelBooking) }

    Spacer(modifier = Modifier.height(8.dp))

    SectionCard("Search & filter") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Subject or tutor") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = "Filter by date")
            }
        }

        if (filterDate.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Date: $filterDate", color = PwrNavy, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { filterDate = "" }) {
                    Text("Clear date", color = PwrRed)
                }
            }
        }
    }

    val filteredSlots = state.dashboardState.availableTutorSlots.filter { slot ->
        val matchesText = searchQuery.isBlank() ||
                slot.subject.contains(searchQuery, ignoreCase = true) ||
                slot.tutorDisplayName.contains(searchQuery, ignoreCase = true)
        val matchesDate = filterDate.isBlank() || slot.dateLabel == filterDate
        val matchesSubject = selectedSubject == "All" || slot.subject == selectedSubject
        matchesText && matchesDate && matchesSubject
    }

    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Open tutor slots",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )

    if (filteredSlots.isEmpty()) {
        Text("No slots match your search.", color = TextSecondary)
    }
    filteredSlots.forEach { AvailabilityCard(it, onBook = onBookTutorSlot) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        filterDate = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Filter") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun availabilityCalendarDays(state: SmartCampusUiState): List<CalendarDayUi> {
    val today = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    return (0..4).map { offset ->
        val date = today.plusDays(offset.toLong())
        val dateLabel = date.format(isoFormatter)
        val count = state.dashboardState.myAvailability.count { it.dateLabel == dateLabel }
        CalendarDayUi(
            day = date.format(dayFormatter),
            number = date.dayOfMonth.toString(),
            caption = "$count slot${if (count == 1) "" else "s"}",
            selected = offset == 0
        )
    }
}
