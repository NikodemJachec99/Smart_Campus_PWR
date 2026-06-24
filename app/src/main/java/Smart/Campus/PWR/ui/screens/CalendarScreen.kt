package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.TimePickerDialogWrapper
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardFlat
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.Chip
import Smart.Campus.PWR.ui.components.softindigo.DayCell
import Smart.Campus.PWR.ui.components.softindigo.DayStrip
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.ProgressBar
import Smart.Campus.PWR.ui.components.softindigo.SearchField
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.SlotChip
import Smart.Campus.PWR.ui.components.softindigo.SlotState
import Smart.Campus.PWR.ui.components.softindigo.StatusDot
import Smart.Campus.PWR.ui.components.softindigo.StarsRow
import Smart.Campus.PWR.ui.components.softindigo.SubjectDot
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.tutoring.TutorRatings
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorSummaryUi
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.GreenBg
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.Star
import Smart.Campus.PWR.ui.theme.White
import Smart.Campus.PWR.ui.util.buildAddToCalendarIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// ─── Navigation states ────────────────────────────────────────────────────────

private sealed class StudentStep {
    object Find : StudentStep()
    object Results : StudentStep()
    data class Profile(val tutorId: String, val tutorName: String) : StudentStep()
    data class Booking(val tutorId: String, val tutorName: String, val initialDate: String) : StudentStep()
    data class Confirm(val slotId: String, val slot: TutorAvailabilityUi) : StudentStep()
}

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun CalendarTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onAvailabilitySubjectChanged: (String) -> Unit,
    onAvailabilityDateChanged: (String) -> Unit,
    onAvailabilityStartHourChanged: (String) -> Unit,
    onAvailabilityEndHourChanged: (String) -> Unit,
    onAvailabilityDurationChanged: (Int) -> Unit,
    onAvailabilityFormatChanged: (String) -> Unit,
    onAvailabilityLocationChanged: (String) -> Unit,
    onAvailabilityMeetingUrlChanged: (String) -> Unit,
    onAvailabilityTopicChanged: (String) -> Unit,
    onAddAvailability: () -> Unit,
    onBookTutorSlot: (String) -> Unit,
    onStartBookingRequest: (String) -> Unit,
    onBookingRequestMessageChanged: (String) -> Unit,
    onBookingRequestTopicChanged: (String) -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onTutorSearchQueryChanged: (String) -> Unit,
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
    onTutorSearchFormatChanged: (String?) -> Unit,
    onTutorSearchMinRatingChanged: (Int?) -> Unit,
    onTutorSearchAvailableTodayChanged: (Boolean) -> Unit,
    onTutorSearchSortChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit,
    onOpenDirectWith: (String, String) -> Unit = { _, _ -> },
    onConsumeOpenResults: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        if (activeRole == UserRole.TUTOR) {
            TutorScheduleScreen(
                state = state,
                onClearMessages = onClearMessages,
                onAvailabilitySubjectChanged = onAvailabilitySubjectChanged,
                onAvailabilityDateChanged = onAvailabilityDateChanged,
                onAvailabilityStartHourChanged = onAvailabilityStartHourChanged,
                onAvailabilityEndHourChanged = onAvailabilityEndHourChanged,
                onAvailabilityDurationChanged = onAvailabilityDurationChanged,
                onAvailabilityFormatChanged = onAvailabilityFormatChanged,
                onAvailabilityLocationChanged = onAvailabilityLocationChanged,
                onAvailabilityMeetingUrlChanged = onAvailabilityMeetingUrlChanged,
                onAvailabilityTopicChanged = onAvailabilityTopicChanged,
                onAddAvailability = onAddAvailability,
                onDeleteAvailability = onDeleteAvailability,
                onCancelBooking = onCancelBooking
            )
        } else {
            StudentFindFlow(
                state = state,
                onBookTutorSlot = onBookTutorSlot,
                onTutorSearchQueryChanged = onTutorSearchQueryChanged,
                onStartBookingRequest = onStartBookingRequest,
                onBookingRequestMessageChanged = onBookingRequestMessageChanged,
                onBookingRequestTopicChanged = onBookingRequestTopicChanged,
                onClearMessages = onClearMessages,
                onTutorSearchTutorChanged = onTutorSearchTutorChanged,
                onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
                onTutorSearchDateChanged = onTutorSearchDateChanged,
                onTutorSearchFormatChanged = onTutorSearchFormatChanged,
                onTutorSearchMinRatingChanged = onTutorSearchMinRatingChanged,
                onTutorSearchAvailableTodayChanged = onTutorSearchAvailableTodayChanged,
                onTutorSearchSortChanged = onTutorSearchSortChanged,
                onClearTutorSearchFilters = onClearTutorSearchFilters,
                onOpenDirectWith = onOpenDirectWith,
                onConsumeOpenResults = onConsumeOpenResults,
                onNavigate = onNavigate
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  STUDENT branch — multi-step Find → Results → Profile → Booking → Confirm
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StudentFindFlow(
    state: SmartCampusUiState,
    onBookTutorSlot: (String) -> Unit,
    onTutorSearchQueryChanged: (String) -> Unit,
    onStartBookingRequest: (String) -> Unit,
    onBookingRequestMessageChanged: (String) -> Unit,
    onBookingRequestTopicChanged: (String) -> Unit,
    onClearMessages: () -> Unit,
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
    onTutorSearchFormatChanged: (String?) -> Unit,
    onTutorSearchMinRatingChanged: (Int?) -> Unit,
    onTutorSearchAvailableTodayChanged: (Boolean) -> Unit,
    onTutorSearchSortChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit,
    onOpenDirectWith: (String, String) -> Unit,
    onConsumeOpenResults: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var step: StudentStep by remember {
        mutableStateOf(if (state.openTutorSearchResults) StudentStep.Results else StudentStep.Find)
    }
    // Honor the one-shot "open results" intent set by Home "Browse by subject".
    LaunchedEffect(state.openTutorSearchResults) {
        if (state.openTutorSearchResults) {
            step = StudentStep.Results
            onConsumeOpenResults()
        }
    }

    when (val s = step) {
        is StudentStep.Find -> ScreenFind(
            state = state,
            onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
            onClearTutorSearchFilters = onClearTutorSearchFilters,
            onNavigateResults = { step = StudentStep.Results }
        )
        is StudentStep.Results -> ScreenFindResults(
            state = state,
            onTutorSearchQueryChanged = onTutorSearchQueryChanged,
            onTutorSearchTutorChanged = onTutorSearchTutorChanged,
            onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
            onTutorSearchDateChanged = onTutorSearchDateChanged,
            onTutorSearchFormatChanged = onTutorSearchFormatChanged,
            onTutorSearchMinRatingChanged = onTutorSearchMinRatingChanged,
            onTutorSearchAvailableTodayChanged = onTutorSearchAvailableTodayChanged,
            onTutorSearchSortChanged = onTutorSearchSortChanged,
            onClearTutorSearchFilters = onClearTutorSearchFilters,
            onBack = { step = StudentStep.Find },
            onOpenProfile = { tutorId, tutorName ->
                step = StudentStep.Profile(tutorId, tutorName)
            }
        )
        is StudentStep.Profile -> ScreenTutorProfile(
            state = state,
            tutorId = s.tutorId,
            tutorName = s.tutorName,
            onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
            onBack = { step = StudentStep.Results },
            onBook = { selectedDate -> step = StudentStep.Booking(s.tutorId, s.tutorName, selectedDate) }
        )
        is StudentStep.Booking -> ScreenBooking(
            state = state,
            tutorId = s.tutorId,
            tutorName = s.tutorName,
            initialDate = s.initialDate,
            onBack = { step = StudentStep.Profile(s.tutorId, s.tutorName) },
            onStartBookingRequest = onStartBookingRequest,
            onBookingRequestMessageChanged = onBookingRequestMessageChanged,
            onBookingRequestTopicChanged = onBookingRequestTopicChanged,
            onConfirm = { slotId, slot ->
                onClearMessages()
                onBookTutorSlot(slotId)
                step = StudentStep.Confirm(slotId, slot)
            }
        )
        is StudentStep.Confirm -> ScreenBookingConfirm(
            state = state,
            slot = s.slot,
            onBackToBooking = {
                step = StudentStep.Booking(
                    tutorId = s.slot.tutorId,
                    tutorName = s.slot.tutorDisplayName,
                    initialDate = s.slot.dateLabel
                )
            },
            onClose = { step = StudentStep.Find },
            onMessageTutor = { onOpenDirectWith(s.slot.tutorId, s.slot.tutorDisplayName) },
            onViewLessons = { onNavigate(DashboardRoutes.LESSONS) }
        )
    }
}

// ─── ScreenFind ────────────────────────────────────────────────────────────────

@Composable
private fun ScreenFind(
    state: SmartCampusUiState,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit,
    onNavigateResults: () -> Unit
) {
    val subjects = remember(state.dashboardState.availableTutorSlots) {
        state.dashboardState.availableTutorSlots
            .map { it.subject }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .take(6)
    }
    val searchQuery = state.tutorSearchFilters.query

    Column(modifier = Modifier.fillMaxSize()) {
        SoftTopBar(title = "Find a tutor")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Big search field — pure shortcut into Results (which has the
            // editable search); enabled=false so the tap is not swallowed.
            SearchField(
                value = searchQuery,
                onValueChange = { onTutorSearchSubjectChanged(it) },
                placeholder = "Search a subject, topic or tutor",
                big = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateResults() }
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Chip(text = "All", selected = true) {
                    onClearTutorSearchFilters()
                    onNavigateResults()
                }
                Chip(text = "Online", leadingIcon = SoftIcons.globe) { onNavigateResults() }
                Chip(text = "In person", leadingIcon = SoftIcons.pin) { onNavigateResults() }
                Chip(text = "Filters", leadingIcon = SoftIcons.sliders) { onNavigateResults() }
            }

            Spacer(Modifier.height(24.dp))

            // "What do you need?" goal tiles — DESIGN-PLACEHOLDER (no matching VM callbacks)
            SectionHead(title = "What do you need?")
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple("Exam prep", "Cram before the session", SoftIcons.cap),
                    Triple("Homework help", "Stuck on a problem set", SoftIcons.task)
                ).forEach { (title, subtitle, icon) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardSurface)
                            .border(1.dp, Line, RoundedCornerShape(16.dp))
                            .clickable { onNavigateResults() }
                            .padding(15.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Primary50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Primary600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(text = title, style = SoftType.title)
                            Text(text = subtitle, style = SoftType.meta)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Popular subjects grid — derived from availableTutorSlots
            SectionHead(title = "Popular subjects", action = "A–Z")
            Spacer(Modifier.height(12.dp))

            if (subjects.isEmpty()) {
                CardFlat {
                    Text(
                        "No subjects available yet.",
                        style = SoftType.bodySm,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            } else {
                // 2-col grid with coloured subject tiles
                val rows = subjects.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { rowSubjects ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowSubjects.forEach { subj ->
                                val (bg, fg) = subjectColors(subj)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(bg)
                                        .clickable {
                                            onTutorSearchSubjectChanged(subj)
                                            onNavigateResults()
                                        }
                                        .padding(14.dp)
                                        .height(92.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(White.copy(alpha = 0.65f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = SoftIcons.cap,
                                                contentDescription = null,
                                                tint = fg,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = subj,
                                                style = TextStyle(
                                                    fontFamily = BodyFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = fg
                                                )
                                            )
                                            val count = state.dashboardState.availableTutorSlots
                                                .count { it.subject == subj }
                                            Text(
                                                text = "$count slot${if (count == 1) "" else "s"}",
                                                style = TextStyle(
                                                    fontFamily = BodyFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = fg.copy(alpha = 0.75f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining space if odd number
                            if (rowSubjects.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // CTA to search
            SoftButton(
                text = "Browse all tutors",
                onClick = onNavigateResults,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = SoftIcons.arrow
            )
        }
    }
}

// ─── ScreenFindResults ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenFindResults(
    state: SmartCampusUiState,
    onTutorSearchQueryChanged: (String) -> Unit,
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
    onTutorSearchFormatChanged: (String?) -> Unit,
    onTutorSearchMinRatingChanged: (Int?) -> Unit,
    onTutorSearchAvailableTodayChanged: (Boolean) -> Unit,
    onTutorSearchSortChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit,
    onBack: () -> Unit,
    onOpenProfile: (String, String) -> Unit
) {
    val filters = state.tutorSearchFilters
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                return utcTimeMillis >= cal.timeInMillis
            }
        }
    )

    // Tutor map for rating look-up
    val tutorMap = remember(state.dashboardState.tutors) {
        state.dashboardState.tutors.associateBy { it.uid }
    }

    // Build per-tutor result cards from availableTutorSlots
    val tutorSlotMap = remember(state.dashboardState.availableTutorSlots, filters) {
        filteredTutorSlots(state)
            .groupBy { it.tutorId }
    }
    val resultCount = tutorSlotMap.size

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onTutorSearchDateChanged(formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("Filter") }
            },
            dismissButton = {
                Row {
                    if (filters.date.isNotBlank()) {
                        TextButton(onClick = {
                            onTutorSearchDateChanged("")
                            showDatePicker = false
                        }) { Text("Clear date", color = Smart.Campus.PWR.ui.theme.Red) }
                    }
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        // Custom top bar with search inline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            SearchField(
                value = filters.query,
                onValueChange = onTutorSearchQueryChanged,
                placeholder = "Subject or tutor name…",
                modifier = Modifier.weight(1f),
                trailing = if (filters.query.isNotBlank()) {
                    { SoftIconButton(icon = SoftIcons.x, onClick = { onTutorSearchQueryChanged("") }) }
                } else null
            )
        }

        // Count + filter chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$resultCount tutor${if (resultCount == 1) "" else "s"}",
                    style = SoftType.title.copy(fontSize = 13.5.sp)
                )
                if (filters.hasActiveFilters) {
                    Text(
                        text = "Clear filters",
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Smart.Campus.PWR.ui.theme.Red
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onClearTutorSearchFilters() }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Date filter chip
                Chip(
                    text = if (filters.date.isNotBlank()) filters.date else "Any date",
                    selected = filters.date.isNotBlank(),
                    leadingIcon = SoftIcons.calendar
                ) { showDatePicker = true }

                // Active tutor / subject filters (tap to clear)
                if (filters.tutorQuery.isNotBlank()) {
                    Chip(
                        text = filters.tutorQuery,
                        selected = true,
                        leadingIcon = SoftIcons.user
                    ) { onTutorSearchTutorChanged("") }
                }
                if (filters.subjectQuery.isNotBlank()) {
                    Chip(
                        text = filters.subjectQuery,
                        selected = true,
                        leadingIcon = SoftIcons.book
                    ) { onTutorSearchSubjectChanged("") }
                }

                // Online / In-person format toggle
                Chip(
                    text = "Online",
                    selected = filters.format == "ONLINE",
                    leadingIcon = SoftIcons.globe
                ) {
                    onTutorSearchFormatChanged(if (filters.format == "ONLINE") null else "ONLINE")
                }
                Chip(
                    text = "In person",
                    selected = filters.format == "IN_PERSON",
                    leadingIcon = SoftIcons.pin
                ) {
                    onTutorSearchFormatChanged(if (filters.format == "IN_PERSON") null else "IN_PERSON")
                }

                // 4-star minimum rating chip
                Chip(
                    text = "4+ stars",
                    selected = filters.minRating == 4,
                    leadingIcon = SoftIcons.star
                ) {
                    onTutorSearchMinRatingChanged(if (filters.minRating == 4) null else 4)
                }

                // Available today chip
                Chip(
                    text = "Today",
                    selected = filters.availableToday,
                    leadingIcon = SoftIcons.calendar
                ) {
                    onTutorSearchAvailableTodayChanged(!filters.availableToday)
                }

                // Sort toggle: Top rated / Soonest
                Chip(
                    text = if (filters.sort == "TOP_RATED") "Top rated" else "Soonest",
                    selected = true,
                    leadingIcon = SoftIcons.sliders
                ) {
                    onTutorSearchSortChanged(if (filters.sort == "TOP_RATED") "SOONEST" else "TOP_RATED")
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Tutor result cards, sectioned by what the query matched
        val query = filters.query.trim()
        val entries = tutorSlotMap.entries.toList()
        val nameMatches = if (query.isBlank()) emptyList() else entries.filter { (_, slots) ->
            slots.first().tutorDisplayName.contains(query, ignoreCase = true)
        }
        val subjectMatches = entries.filterNot { entry -> nameMatches.any { it.key == entry.key } }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (entries.isEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    CardFlat {
                        Text(
                            text = if (query.isBlank())
                                "No tutors match your filters."
                            else
                                "Nothing matches \"$query\" \u2014 try a subject (e.g. Analiza) or a tutor name.",
                            style = SoftType.bodySm,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            } else {
                if (query.isNotBlank() && nameMatches.isNotEmpty()) {
                    item { ResultsSectionLabel(icon = SoftIcons.user, text = "Tutors matching \"$query\"") }
                    items(nameMatches) { (tutorId, slots) ->
                        TutorResultCard(
                            slots = slots,
                            tutorSummary = tutorMap[tutorId],
                            query = query,
                            onClick = { onOpenProfile(tutorId, slots.first().tutorDisplayName) }
                        )
                    }
                    if (subjectMatches.isNotEmpty()) {
                        item { ResultsSectionLabel(icon = SoftIcons.book, text = "Teaching \"$query\"") }
                    }
                }
                items(subjectMatches) { (tutorId, slots) ->
                    TutorResultCard(
                        slots = slots,
                        tutorSummary = tutorMap[tutorId],
                        query = query,
                        onClick = { onOpenProfile(tutorId, slots.first().tutorDisplayName) }
                    )
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

// ─── Results helpers ──────────────────────────────────────────────────

@Composable
private fun ResultsSectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary600,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Ink3
            )
        )
    }
}

@Composable
private fun TutorResultCard(
    slots: List<TutorAvailabilityUi>,
    tutorSummary: TutorSummaryUi?,
    query: String,
    onClick: () -> Unit
) {
    val firstSlot = slots.first()
    val subjects = slots.map { it.subject }.distinct().take(3)
    val nextSlot = slots.minByOrNull { it.dateLabel + it.startHour }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Header row: avatar + name + badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            InitialsAvatar(name = firstSlot.tutorDisplayName, size = 56.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = firstSlot.tutorDisplayName,
                        style = SoftType.title.copy(fontSize = 15.sp),
                        modifier = Modifier.weight(1f)
                    )
                    if (tutorSummary?.verified == true) {
                        Badge(text = "Verified", tone = BadgeTone.Prim)
                    }
                }
                Spacer(Modifier.height(2.dp))
                // Subjects line — matching subjects are highlighted so it is
                // obvious WHY this tutor is in the results.
                Text(
                    text = buildAnnotatedString {
                        subjects.forEachIndexed { index, subj ->
                            if (index > 0) append(" · ")
                            val hit = query.isNotBlank() && subj.contains(query, ignoreCase = true)
                            if (hit) {
                                withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Bold)) {
                                    append(subj)
                                }
                            } else {
                                append(subj)
                            }
                        }
                    },
                    style = SoftType.bodySm
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rating = tutorSummary?.ratingAvg?.toFloat() ?: 0f
                    val count = tutorSummary?.ratingCount ?: 0
                    if (count > 0) {
                        StarsRow(value = rating)
                        Text(
                            text = "${"%.1f".format(rating)} ($count)",
                            style = SoftType.meta.copy(fontSize = 12.sp)
                        )
                    } else {
                        Text(
                            text = "No reviews yet",
                            style = SoftType.meta.copy(fontSize = 12.sp)
                        )
                    }
                    if (tutorSummary?.experienceYears != null) {
                        Text(
                            text = "· ${tutorSummary.experienceYears} yrs",
                            style = SoftType.meta.copy(fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        SoftDivider()
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Next available slot
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDot(color = Green)
                Text(
                    text = if (nextSlot != null)
                        "${nextSlot.dateLabel} ${nextSlot.startHour}"
                    else "No slots",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Green
                    )
                )
            }
            Badge(
                text = "${slots.size} open",
                tone = BadgeTone.Prim
            )
        }
    }
}

// ─── ScreenTutorProfile ───────────────────────────────────────────────────────

@Composable
private fun ScreenTutorProfile(
    state: SmartCampusUiState,
    tutorId: String,
    tutorName: String,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onBack: () -> Unit,
    onBook: (String) -> Unit
) {
    // Honor the active subject filter so the profile shows only the slots the
    // student drilled in for (e.g. "Analiza Matematyczna"), not the tutor's
    // entire schedule across every subject.
    val subjectQuery = state.tutorSearchFilters.subjectQuery.trim()
    val tutorSlots = remember(state.dashboardState.availableTutorSlots, tutorId, subjectQuery) {
        state.dashboardState.availableTutorSlots.filter {
            it.tutorId == tutorId &&
                (subjectQuery.isBlank() || it.subject.contains(subjectQuery, ignoreCase = true))
        }
    }
    val tutorSummary: TutorSummaryUi? = remember(state.dashboardState.tutors, tutorId) {
        state.dashboardState.tutors.firstOrNull { it.uid == tutorId }
    }
    // Subjects this tutor actually has open slots for — each chip is a live
    // filter the student can tap to switch which subject's slots are shown.
    val subjectsWithSlots = remember(state.dashboardState.availableTutorSlots, tutorId) {
        state.dashboardState.availableTutorSlots
            .filter { it.tutorId == tutorId }
            .map { it.subject }
            .distinct()
    }

    val reviews = remember(state.dashboardState.reviewsForMe, tutorId) {
        state.dashboardState.reviewsForMe.filter { it.tutorId == tutorId }.take(3)
    }

    val dayCells = remember(tutorSlots) { tutorSlotDays(tutorSlots) }
    var selectedDate by remember(tutorId) { mutableStateOf(dayCells.firstOrNull()?.dateLabel.orEmpty()) }
    LaunchedEffect(tutorId, dayCells) {
        selectedDate = dayCells.firstOrNull()?.dateLabel.orEmpty()
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconButton(icon = SoftIcons.back, onClick = onBack)
            }

            // Header section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InitialsAvatar(
                        name = tutorName,
                        size = 78.dp,
                        online = tutorSlots.isNotEmpty()
                    )
                    Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                        Text(text = tutorName, style = SoftType.h1)
                        Spacer(Modifier.height(2.dp))
                        if (tutorSummary?.experienceYears != null) {
                            Text(
                                text = "${tutorSummary.experienceYears} yrs experience",
                                style = SoftType.meta
                            )
                        } else {
                            Text(text = "PWr tutor", style = SoftType.meta)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (tutorSlots.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(GreenBg)
                                        .padding(vertical = 4.dp, horizontal = 9.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusDot()
                                    Text(
                                        text = "Available",
                                        style = TextStyle(
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Green
                                        )
                                    )
                                }
                            }
                            if (tutorSummary?.verified == true) {
                                Badge(text = "Verified PWr", tone = BadgeTone.Prim)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stat strip — real ratingAvg + ratingCount
                CardQ {
                    val ratingAvg = tutorSummary?.ratingAvg ?: 0.0
                    val ratingCount = tutorSummary?.ratingCount ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "${tutorSlots.size}" to "Slots",
                            if (ratingCount > 0) "${"%.1f".format(ratingAvg)}" to "Rating ($ratingCount)"
                            else "—" to "Rating",
                            if (tutorSummary?.verified == true) "PWr" to "Verified" else "—" to "Verified"
                        ).forEachIndexed { idx, (value, label) ->
                            if (idx > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(Line)
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(value, style = SoftType.h3)
                                Text(label, style = SoftType.meta.copy(fontSize = 11.5.sp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Bio — real bio if available, fallback text
                Text(
                    text = if (tutorSummary?.bio?.isNotBlank() == true)
                        tutorSummary.bio
                    else
                        "Open slots available for PWr students. Book a session to get started.",
                    style = SoftType.body
                )

                Spacer(Modifier.height(14.dp))

                // Subject chips — tap to scope the calendar to one of the tutor's
                // other available subjects.
                if (subjectsWithSlots.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectsWithSlots.forEach { subj ->
                            val isActive = subjectQuery.isNotBlank() &&
                                    subj.contains(subjectQuery, ignoreCase = true)
                            Chip(
                                text = subj,
                                selected = isActive,
                                onClick = {
                                    // Tapping the active subject clears the filter (show all),
                                    // tapping another scopes the calendar to that subject.
                                    onTutorSearchSubjectChanged(if (isActive) "" else subj)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // DayStrip
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHead(title = "Pick a day")
                Spacer(Modifier.height(12.dp))
                if (dayCells.isEmpty()) {
                    CardFlat {
                        Text("No available dates for this tutor.", style = SoftType.bodySm)
                    }
                } else {
                    DayStrip {
                        dayCells.forEach { day ->
                            DayCell(
                                dow = day.dow,
                                dayNum = day.dayNum,
                                slotLabel = "${day.slotCount} slot${if (day.slotCount == 1) "" else "s"}",
                                selected = day.dateLabel == selectedDate,
                                enabled = day.slotCount > 0,
                                onClick = { selectedDate = day.dateLabel }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Available times for the picked day — shown up-front so the
                    // student sees when the tutor is free without opening booking.
                    val timesForDay = tutorSlots
                        .filter { it.dateLabel == selectedDate }
                        .sortedBy { it.startHour }
                    if (timesForDay.isNotEmpty()) {
                        Text("Available times", style = SoftType.meta)
                        Spacer(Modifier.height(8.dp))
                        timesForDay.chunked(3).forEach { rowSlots ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSlots.forEach { slot ->
                                    SlotChip(
                                        label = slot.startHour,
                                        modifier = Modifier.weight(1f),
                                        state = SlotState.Default
                                    )
                                }
                                repeat(3 - rowSlots.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    SoftButton(
                        text = "Choose time",
                        onClick = { if (selectedDate.isNotBlank()) onBook(selectedDate) },
                        enabled = selectedDate.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = SoftIcons.arrow
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Reviews
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHead(
                    title = "Reviews",
                    action = if (reviews.isNotEmpty()) "All ${reviews.size}" else null
                )
                Spacer(Modifier.height(12.dp))
                if (reviews.isEmpty()) {
                    CardFlat {
                        Text("No reviews yet.", style = SoftType.bodySm, modifier = Modifier.padding(4.dp))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.forEach { review ->
                            CardQ {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        InitialsAvatar(name = review.studentDisplayName, size = 34.dp)
                                        Column {
                                            Text(
                                                text = review.studentDisplayName,
                                                style = SoftType.title.copy(fontSize = 13.5.sp)
                                            )
                                            Text(
                                                text = review.createdAtLabel,
                                                style = SoftType.meta.copy(fontSize = 11.5.sp)
                                            )
                                        }
                                    }
                                    StarsRow(value = review.rating.toFloat())
                                }
                                if (review.comment.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(text = review.comment, style = SoftType.bodySm)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Sticky footer CTA — no price, just the button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, Line, RoundedCornerShape(0.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            SoftButton(
                text = "Book a session",
                onClick = { if (selectedDate.isNotBlank()) onBook(selectedDate) },
                enabled = selectedDate.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = SoftIcons.arrow
            )
        }
    }
}

// ─── ScreenBooking ────────────────────────────────────────────────────────────

@Composable
private fun ScreenBooking(
    state: SmartCampusUiState,
    tutorId: String,
    tutorName: String,
    initialDate: String,
    onBack: () -> Unit,
    onStartBookingRequest: (String) -> Unit,
    onBookingRequestMessageChanged: (String) -> Unit,
    onBookingRequestTopicChanged: (String) -> Unit,
    onConfirm: (String, TutorAvailabilityUi) -> Unit
) {
    // Keep the booking calendar scoped to the subject the student picked, so the
    // day strip and slot count match the subject tile they came from.
    val subjectQuery = state.tutorSearchFilters.subjectQuery.trim()
    val tutorSlots = remember(state.dashboardState.availableTutorSlots, tutorId, subjectQuery) {
        state.dashboardState.availableTutorSlots
            .filter {
                it.tutorId == tutorId &&
                    (subjectQuery.isBlank() || it.subject.contains(subjectQuery, ignoreCase = true))
            }
            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
    }

    val days = remember(tutorSlots) { tutorSlotDays(tutorSlots) }

    var selectedDate by remember(tutorId, initialDate) { mutableStateOf(initialDate) }
    var selectedSlotId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tutorId, initialDate, days) {
        selectedDate = initialDate.takeIf { date ->
            days.any { it.dateLabel == date }
        } ?: days.firstOrNull()?.dateLabel.orEmpty()
        selectedSlotId = null
    }

    val slotsForDay = tutorSlots.filter { it.dateLabel == selectedDate }

    // Group by morning/afternoon-evening
    val morningSlots = slotsForDay.filter {
        it.startHour.substringBefore(":").toIntOrNull()?.let { h -> h < 12 } == true
    }
    val afternoonSlots = slotsForDay.filter {
        it.startHour.substringBefore(":").toIntOrNull()?.let { h -> h >= 12 } == true
    }

    val selectedSlot = slotsForDay.find { it.id == selectedSlotId }
        ?: slotsForDay.firstOrNull()

    // When a slot is selected, call startBookingRequest to prime the request state
    val activeSlot = slotsForDay.find { it.id == selectedSlotId } ?: selectedSlot

    val bookingRequest = state.bookingRequest
    val sendBookingRequest: () -> Unit = {
        activeSlot?.let { slot ->
            onConfirm(slot.id, slot)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            SoftTopBar(
                title = "Book a session",
                leading = { SoftIconButton(icon = SoftIcons.back, onClick = onBack) }
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Progress bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressBar(fraction = 1.0f, modifier = Modifier.weight(1f))
                    ProgressBar(fraction = if (selectedSlotId != null) 1.0f else 0.6f, modifier = Modifier.weight(1f))
                    ProgressBar(fraction = 0f, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // Tutor summary card
                CardQ {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InitialsAvatar(name = tutorName, size = 42.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tutorName, style = SoftType.title.copy(fontSize = 14.sp))
                            val firstSubj = tutorSlots.firstOrNull()?.subject ?: ""
                            Text(
                                text = firstSubj,
                                style = SoftType.meta.copy(fontSize = 12.sp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatusDot()
                            Badge(text = "Open", tone = BadgeTone.Green)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Choose date
                Text("Choose a date", style = SoftType.h3)
                Spacer(Modifier.height(12.dp))
                if (days.isEmpty()) {
                    CardFlat {
                        Text("No available dates for this tutor.", style = SoftType.bodySm)
                    }
                } else {
                    DayStrip {
                        days.forEach { day ->
                            DayCell(
                                dow = day.dow,
                                dayNum = day.dayNum,
                                slotLabel = "${day.slotCount} slot${if (day.slotCount == 1) "" else "s"}",
                                selected = day.dateLabel == selectedDate,
                                enabled = day.slotCount > 0,
                                onClick = {
                                    selectedDate = day.dateLabel
                                    selectedSlotId = null
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pick a time", style = SoftType.h3)
                    Text(selectedDate, style = SoftType.meta)
                }
                Spacer(Modifier.height(10.dp))

                if (slotsForDay.isEmpty()) {
                    Text("No slots available on this day.", style = SoftType.bodySm)
                } else {
                    if (morningSlots.isNotEmpty()) {
                        Text(
                            "Morning",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Ink2
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        // 3-column grid of SlotChips
                        val morningRows = morningSlots.chunked(3)
                        morningRows.forEach { rowSlots ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowSlots.forEach { slot ->
                                    val chipState = when {
                                        slot.id == (selectedSlotId ?: selectedSlot?.id) -> SlotState.On
                                        else -> SlotState.Default
                                    }
                                    SlotChip(
                                        label = slot.startHour,
                                        state = chipState,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedSlotId = slot.id
                                            onStartBookingRequest(slot.id)
                                        }
                                    )
                                }
                                // Fill empty cells
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(9.dp))
                        }
                    }

                    if (afternoonSlots.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Afternoon & evening",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Ink2
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        val afternoonRows = afternoonSlots.chunked(3)
                        afternoonRows.forEach { rowSlots ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowSlots.forEach { slot ->
                                    val chipState = when {
                                        slot.id == (selectedSlotId ?: selectedSlot?.id) -> SlotState.On
                                        else -> SlotState.Default
                                    }
                                    SlotChip(
                                        label = slot.startHour,
                                        state = chipState,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedSlotId = slot.id
                                            onStartBookingRequest(slot.id)
                                        }
                                    )
                                }
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(9.dp))
                        }
                    }
                }

                // Show slot details (format + duration) when a slot is selected
                if (activeSlot != null) {
                    Spacer(Modifier.height(20.dp))
                    Text("Session details", style = SoftType.h3)
                    Spacer(Modifier.height(10.dp))
                    CardQ {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Format", style = SoftType.meta.copy(fontSize = 11.5.sp))
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (activeSlot.format == "ONLINE") "Online" else "In person",
                                    style = SoftType.title.copy(fontSize = 14.sp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Duration", style = SoftType.meta.copy(fontSize = 11.5.sp))
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${activeSlot.durationMinutes} min",
                                    style = SoftType.title.copy(fontSize = 14.sp)
                                )
                            }
                        }
                        if (activeSlot.format == "IN_PERSON" && activeSlot.location.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Location", style = SoftType.meta.copy(fontSize = 11.5.sp))
                            Spacer(Modifier.height(2.dp))
                            Text(activeSlot.location, style = SoftType.bodySm)
                        }
                        if (activeSlot.format == "ONLINE" && activeSlot.meetingUrl.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Meeting link", style = SoftType.meta.copy(fontSize = 11.5.sp))
                            Spacer(Modifier.height(2.dp))
                            Text(activeSlot.meetingUrl, style = SoftType.bodySm)
                        }
                        if (activeSlot.topic.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Suggested topic", style = SoftType.meta.copy(fontSize = 11.5.sp))
                            Spacer(Modifier.height(2.dp))
                            Text(activeSlot.topic, style = SoftType.bodySm)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Topic note field — wired to bookingRequest.topic
                Text("What would you like to work on?", style = SoftType.h3)
                Spacer(Modifier.height(10.dp))
                SoftTextField(
                    value = bookingRequest.topic,
                    onValueChange = onBookingRequestTopicChanged,
                    label = "Topic / goal",
                    placeholder = "e.g. Integration by parts, exam prep...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Optional message to tutor
                SoftTextField(
                    value = bookingRequest.message,
                    onValueChange = onBookingRequestMessageChanged,
                    label = "Message to tutor (optional)",
                    placeholder = "Say hi or share context...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                SoftButton(
                    text = "Send booking request",
                    onClick = sendBookingRequest,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = activeSlot != null,
                    trailingIcon = SoftIcons.arrow
                )
            }
        }

        // Sticky footer — no price, just the CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, Line, RoundedCornerShape(0.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            SoftButton(
                text = "Send booking request",
                onClick = sendBookingRequest,
                modifier = Modifier.fillMaxWidth(),
                enabled = activeSlot != null,
                trailingIcon = SoftIcons.arrow
            )
        }
    }
}

// ─── ScreenBookingConfirm ─────────────────────────────────────────────────────

@Composable
private fun ScreenBookingConfirm(
    state: SmartCampusUiState,
    slot: TutorAvailabilityUi,
    onBackToBooking: () -> Unit,
    onClose: () -> Unit,
    onMessageTutor: () -> Unit,
    onViewLessons: () -> Unit
) {
    val context = LocalContext.current
    val bookingRequest = state.bookingRequest
    val isSending = state.isMainSubmitting
    val errorMessage = state.errorMessage
    val isError = errorMessage != null

    // Parse begin/end millis for calendar intent
    val calendarBeginMillis: Long? = remember(slot.dateLabel, slot.startHour) {
        try {
            val dtStr = "${slot.dateLabel}T${slot.startHour}:00"
            LocalDateTime.parse(dtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) { null }
    }
    val calendarEndMillis: Long? = remember(slot.dateLabel, slot.endHour) {
        try {
            val dtStr = "${slot.dateLabel}T${slot.endHour}:00"
            LocalDateTime.parse(dtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) { null }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        SoftTopBar(
            title = when {
                isSending -> "Sending request"
                isError -> "Request failed"
                else -> "Request sent"
            },
            leading = { SoftIconButton(icon = SoftIcons.x, onClick = onClose) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 64.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Pending/sent check circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isError) SoftIcons.x else SoftIcons.check,
                    contentDescription = null,
                    tint = if (isError) Red else Primary600,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = when {
                    isSending -> "Sending request..."
                    isError -> "Request failed"
                    else -> "Request sent!"
                },
                style = SoftType.display
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    isSending -> "Saving the booking request in Firebase."
                    isError -> "The request was not saved. Go back and try again."
                    else -> "Waiting for the tutor to confirm. You'll be notified when they accept."
                },
                style = SoftType.body
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(14.dp))
                CardFlat {
                    Text(errorMessage, style = SoftType.bodySm.copy(color = Red))
                }
                Spacer(Modifier.height(12.dp))
                SoftButton(
                    text = "Back to booking",
                    onClick = onBackToBooking,
                    modifier = Modifier.fillMaxWidth(),
                    variant = SoftButtonVariant.Outline
                )
            }

            Spacer(Modifier.height(20.dp))

            // Ticket card
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                // Tutor + subject header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InitialsAvatar(name = slot.tutorDisplayName, size = 40.dp)
                        Column {
                            Text(slot.subject, style = SoftType.title.copy(fontSize = 14.sp))
                            Text(slot.tutorDisplayName, style = SoftType.meta.copy(fontSize = 12.sp))
                        }
                    }
                    Badge(
                        text = when {
                            isSending -> "Sending"
                            isError -> "Failed"
                            else -> "Pending"
                        },
                        tone = when {
                            isSending -> BadgeTone.Gray
                            isError -> BadgeTone.Red
                            else -> BadgeTone.Prim
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
                SoftDivider()
                Spacer(Modifier.height(16.dp))

                // Ticket grid: Date / Time / Format / Duration
                val formatLabel = if (slot.format == "ONLINE") "Online" else "In person"
                val ticketRows = listOf(
                    "Date" to slot.dateLabel,
                    "Time" to slot.timeLabel,
                    "Format" to formatLabel,
                    "Duration" to "${slot.durationMinutes} min"
                )
                // 2×2 grid rendered as two rows
                ticketRows.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { (label, value) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, style = SoftType.meta.copy(fontSize = 11.5.sp))
                                Spacer(Modifier.height(2.dp))
                                Text(value, style = SoftType.title.copy(fontSize = 15.sp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Meeting URL if present
                if (slot.format == "ONLINE" && slot.meetingUrl.isNotBlank()) {
                    Text("Meeting link", style = SoftType.meta.copy(fontSize = 11.5.sp))
                    Spacer(Modifier.height(2.dp))
                    Text(slot.meetingUrl, style = SoftType.bodySm)
                    Spacer(Modifier.height(16.dp))
                }
                if (slot.format == "IN_PERSON" && slot.location.isNotBlank()) {
                    Text("Location", style = SoftType.meta.copy(fontSize = 11.5.sp))
                    Spacer(Modifier.height(2.dp))
                    Text(slot.location, style = SoftType.bodySm)
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Topic note card if topic was entered
            if (bookingRequest.topic.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                CardFlat {
                    Text(
                        "Topic / goal",
                        style = SoftType.eyebrow.copy(color = Ink3)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        bookingRequest.topic,
                        style = SoftType.bodySm
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Add to calendar — disabled if parsing failed
            SoftButton(
                text = "Add to calendar",
                onClick = {
                    if (calendarBeginMillis != null && calendarEndMillis != null) {
                        val locationStr = when {
                            slot.format == "IN_PERSON" && slot.location.isNotBlank() -> slot.location
                            slot.format == "ONLINE" && slot.meetingUrl.isNotBlank() -> slot.meetingUrl
                            else -> ""
                        }
                        val intent = buildAddToCalendarIntent(
                            title = "Tutoring: ${slot.subject} with ${slot.tutorDisplayName}",
                            beginMillis = calendarBeginMillis,
                            endMillis = calendarEndMillis,
                            location = locationStr,
                            description = if (bookingRequest.topic.isNotBlank())
                                "Topic: ${bookingRequest.topic}" else ""
                        )
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = SoftButtonVariant.Primary,
                enabled = !isSending && !isError && calendarBeginMillis != null && calendarEndMillis != null,
                trailingIcon = SoftIcons.arrow
            )

            Spacer(Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SoftButton(
                    text = "Message",
                    onClick = onMessageTutor,
                    modifier = Modifier.weight(1f),
                    variant = SoftButtonVariant.Outline
                )
                SoftButton(
                    text = "View lesson",
                    onClick = onViewLessons,
                    modifier = Modifier.weight(1f),
                    variant = SoftButtonVariant.Outline
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Free cancellation up to 12h before",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  TUTOR branch — ScreenTutorSchedule
// ═══════════════════════════════════════════════════════════════════════════════

private fun minutesBetweenHhmm(start: String, end: String): Int = try {
    val (sh, sm) = start.split(":").map { it.toInt() }
    val (eh, em) = end.split(":").map { it.toInt() }
    ((eh * 60 + em) - (sh * 60 + sm)).coerceAtLeast(0)
} catch (e: Exception) {
    60
}

private fun isValidTimeRange(start: String, end: String): Boolean =
    minutesBetweenHhmm(start, end) > 0

private fun formatSlotDateLabel(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH))
} catch (e: Exception) {
    iso
}

/** A read-only, tappable field that opens a picker (avoids the text-field tap-swallow bug). */
@Composable
private fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink2))
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
                .border(1.dp, Line, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                value.ifBlank { placeholder },
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isBlank()) Ink4 else InkToken
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorScheduleScreen(
    state: SmartCampusUiState,
    onClearMessages: () -> Unit,
    onAvailabilitySubjectChanged: (String) -> Unit,
    onAvailabilityDateChanged: (String) -> Unit,
    onAvailabilityStartHourChanged: (String) -> Unit,
    onAvailabilityEndHourChanged: (String) -> Unit,
    onAvailabilityDurationChanged: (Int) -> Unit,
    onAvailabilityFormatChanged: (String) -> Unit,
    onAvailabilityLocationChanged: (String) -> Unit,
    onAvailabilityMeetingUrlChanged: (String) -> Unit,
    onAvailabilityTopicChanged: (String) -> Unit,
    onAddAvailability: () -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showNewSlotForm by remember { mutableStateOf(false) }

    val startTimeState = rememberTimePickerState(initialHour = 12, initialMinute = 0)
    val endTimeState = rememberTimePickerState(initialHour = 13, initialMinute = 0)

    if (showStartTimePicker) {
        TimePickerDialogWrapper(
            title = "Select start time",
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val fmt = String.format("%02d:%02d", startTimeState.hour, startTimeState.minute)
                    onAvailabilityStartHourChanged(fmt)
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
                    val fmt = String.format("%02d:%02d", endTimeState.hour, endTimeState.minute)
                    onAvailabilityEndHourChanged(fmt)
                    showEndTimePicker = false
                }) { Text("OK") }
            }
        ) { TimePicker(state = endTimeState) }
    }

    // DayStrip data from myAvailability
    val today = LocalDate.now()
    val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
    val dowFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    val days = (0..13).map { offset ->
        val date = today.plusDays(offset.toLong())
        val dateLabel = date.format(isoFmt)
        val count = state.dashboardState.myAvailability.count { it.dateLabel == dateLabel }
        Triple(date.format(dowFmt), date.dayOfMonth.toString(), count to dateLabel)
    }

    var selectedDayIndex by remember { mutableStateOf(0) }
    val selectedDateLabel = days.getOrNull(selectedDayIndex)?.third?.second ?: today.format(isoFmt)

    val slotsForDay = state.dashboardState.myAvailability.filter { it.dateLabel == selectedDateLabel }
    val bookedForDay = slotsForDay.filter { it.isBooked }
    val openForDay = slotsForDay.filter { !it.isBooked }

    val form = state.availabilityForm

    // The new slot's date follows the day selected in the strip above (no separate date field).
    LaunchedEffect(selectedDateLabel) { onAvailabilityDateChanged(selectedDateLabel) }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        SoftTopBar(
            title = "Your schedule",
            actions = {
                SoftIconButton(
                    icon = SoftIcons.plus,
                    onClick = { showNewSlotForm = !showNewSlotForm },
                    prim = true
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Open a slot, set the subject — PWr students see it instantly in their feed.",
                style = SoftType.bodySm
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // DayStrip
            item {
                DayStrip(modifier = Modifier.fillMaxWidth()) {
                    days.forEachIndexed { idx, (dow, dayNum, countAndLabel) ->
                        val (count, _) = countAndLabel
                        DayCell(
                            dow = dow,
                            dayNum = dayNum,
                            slotLabel = if (count > 0) "$count slot" else "–",
                            selected = idx == selectedDayIndex,
                            enabled = true,
                            onClick = { selectedDayIndex = idx }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Day header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            selectedDateLabel.uppercase(),
                            style = SoftType.eyebrow
                        )
                        Spacer(Modifier.height(2.dp))
                        Row {
                            Text("${slotsForDay.size} slot${if (slotsForDay.size == 1) "" else "s"}", style = SoftType.h3)
                            if (bookedForDay.isNotEmpty()) {
                                Text(
                                    " · ${bookedForDay.size} booked",
                                    style = SoftType.h3.copy(color = Ink3)
                                )
                            }
                        }
                    }
                    SoftButton(
                        text = "Add slot",
                        onClick = { showNewSlotForm = !showNewSlotForm },
                        variant = SoftButtonVariant.Soft,
                        size = SoftButtonSize.Sm,
                        leadingIcon = SoftIcons.plus
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Slot cards for selected day
            if (slotsForDay.isEmpty()) {
                item {
                    CardFlat {
                        Text("No slots on this day.", style = SoftType.bodySm, modifier = Modifier.padding(4.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            items(slotsForDay) { slot ->
                if (slot.isBooked) {
                    // Booked slot — CardQ with student name + Booked badge
                    CardQ(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Time column
                            Column(modifier = Modifier.width(92.dp)) {
                                Text(slot.timeLabel, style = SoftType.title.copy(fontSize = 13.sp))
                            }
                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Line)
                            )
                            // Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.subject, style = SoftType.title.copy(fontSize = 14.sp))
                                Spacer(Modifier.height(4.dp))
                                // Student name from booked lessons
                                val booking = state.dashboardState.myTutorBookings
                                    .firstOrNull { it.availabilityId == slot.id && it.status == "booked" }
                                if (booking != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        InitialsAvatar(name = booking.studentDisplayName, size = 18.dp)
                                        Text(booking.studentDisplayName, style = SoftType.bodySm)
                                    }
                                }
                            }
                            Badge(text = "Booked", tone = BadgeTone.Green)
                        }
                    }
                } else {
                    // Open slot — CardFlat with delete icon
                    CardFlat(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.width(92.dp)) {
                                Text(slot.timeLabel, style = SoftType.title.copy(fontSize = 13.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Line)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.subject, style = SoftType.title.copy(fontSize = 14.sp))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Open · waiting for a student",
                                    style = SoftType.meta.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            // Delete icon
                            SoftIconButton(
                                icon = SoftIcons.x,
                                onClick = { onDeleteAvailability(slot.id) }
                            )
                        }
                    }
                }
            }

            // New slot quick-add form (toggled by + button)
            if (showNewSlotForm) {
                item {
                    Spacer(Modifier.height(10.dp))
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("New slot", style = SoftType.title.copy(fontSize = 14.sp))
                            Badge(text = "Quick add", tone = BadgeTone.Prim)
                        }
                        Spacer(Modifier.height(12.dp))

                        // Subject field
                        SoftTextField(
                            value = form.subject,
                            onValueChange = onAvailabilitySubjectChanged,
                            label = "Subject",
                            placeholder = "e.g. Calculus II",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))

                        // Date comes from the day selected in the calendar strip above
                        Text(
                            "Date",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink2)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatSlotDateLabel(selectedDateLabel),
                            style = SoftType.title.copy(fontSize = 14.sp)
                        )
                        Text(
                            "Pick the day in the calendar above",
                            style = SoftType.meta.copy(fontSize = 11.sp)
                        )
                        Spacer(Modifier.height(10.dp))

                        // From / To — tappable picker fields that open the time dialog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PickerField(
                                label = "From",
                                value = form.startHour,
                                placeholder = "Pick time",
                                modifier = Modifier.weight(1f),
                                onClick = { showStartTimePicker = true }
                            )
                            PickerField(
                                label = "To",
                                value = form.endHour,
                                placeholder = "Pick time",
                                modifier = Modifier.weight(1f),
                                onClick = { showEndTimePicker = true }
                            )
                        }
                        if (form.startHour.isNotBlank() && form.endHour.isNotBlank()
                            && !isValidTimeRange(form.startHour, form.endHour)
                        ) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "End time must be after the start time.",
                                style = SoftType.meta.copy(fontSize = 11.sp, color = Red)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Format chips: Online / In-person
                        Text("Format", style = SoftType.title.copy(fontSize = 13.sp))
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip(
                                text = "Online",
                                selected = form.format == "ONLINE",
                                leadingIcon = SoftIcons.globe,
                                onClick = { onAvailabilityFormatChanged("ONLINE") }
                            )
                            Chip(
                                text = "In person",
                                selected = form.format == "IN_PERSON",
                                leadingIcon = SoftIcons.pin,
                                onClick = { onAvailabilityFormatChanged("IN_PERSON") }
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Conditional location / meeting URL field
                        if (form.format == "IN_PERSON") {
                            SoftTextField(
                                value = form.location,
                                onValueChange = onAvailabilityLocationChanged,
                                label = "Location",
                                placeholder = "e.g. C-13 room 2.15",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        } else {
                            SoftTextField(
                                value = form.meetingUrl,
                                onValueChange = onAvailabilityMeetingUrlChanged,
                                label = "Meeting link",
                                placeholder = "https://meet.google.com/...",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        // Topic field
                        SoftTextField(
                            value = form.topic,
                            onValueChange = onAvailabilityTopicChanged,
                            label = "Topic hint (optional)",
                            placeholder = "e.g. Integrals, sorting algorithms...",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        SoftButton(
                            text = "Publish slot",
                            onClick = {
                                onClearMessages()
                                onAvailabilityDurationChanged(
                                    minutesBetweenHhmm(form.startHour, form.endHour)
                                )
                                onAddAvailability()
                                showNewSlotForm = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = form.subject.isNotBlank()
                                && form.startHour.isNotBlank()
                                && form.endHour.isNotBlank()
                                && isValidTimeRange(form.startHour, form.endHour),
                            trailingIcon = SoftIcons.arrow
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
            } else {
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private data class TutorDayOption(
    val dateLabel: String,
    val dow: String,
    val dayNum: String,
    val slotCount: Int
)

private fun tutorSlotDays(slots: List<TutorAvailabilityUi>): List<TutorDayOption> {
    val dowFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    return slots
        .filter { it.dateLabel.isNotBlank() }
        .groupBy { it.dateLabel }
        .toSortedMap()
        .map { (dateLabel, slotsForDate) ->
            val parsedDate = runCatching { LocalDate.parse(dateLabel) }.getOrNull()
            TutorDayOption(
                dateLabel = dateLabel,
                dow = parsedDate?.format(dowFmt) ?: dateLabel,
                dayNum = parsedDate?.dayOfMonth?.toString() ?: dateLabel,
                slotCount = slotsForDate.size
            )
        }
}

private fun filteredTutorSlots(state: SmartCampusUiState): List<TutorAvailabilityUi> {
    val ratingByTutor = state.dashboardState.tutors
        .associate { it.uid to it.ratingAvg }
    val today = LocalDate.now().toString()
    return TutorRatings.applyTutorSearch(
        slots = state.dashboardState.availableTutorSlots,
        ratingByTutor = ratingByTutor,
        filter = state.tutorSearchFilters,
        today = today
    )
}
