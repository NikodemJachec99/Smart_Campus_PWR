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
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
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
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.Star
import Smart.Campus.PWR.ui.theme.White
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// ─── Navigation states ────────────────────────────────────────────────────────

private sealed class StudentStep {
    object Find : StudentStep()
    object Results : StudentStep()
    data class Profile(val tutorId: String, val tutorName: String) : StudentStep()
    data class Booking(val tutorId: String, val tutorName: String) : StudentStep()
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
    onAddAvailability: () -> Unit,
    onBookTutorSlot: (String) -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit
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
                onAddAvailability = onAddAvailability,
                onDeleteAvailability = onDeleteAvailability,
                onCancelBooking = onCancelBooking
            )
        } else {
            StudentFindFlow(
                state = state,
                onBookTutorSlot = onBookTutorSlot,
                onClearMessages = onClearMessages,
                onTutorSearchTutorChanged = onTutorSearchTutorChanged,
                onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
                onTutorSearchDateChanged = onTutorSearchDateChanged,
                onClearTutorSearchFilters = onClearTutorSearchFilters
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
    onClearMessages: () -> Unit,
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
    onClearTutorSearchFilters: () -> Unit
) {
    var step: StudentStep by remember { mutableStateOf(StudentStep.Find) }

    when (val s = step) {
        is StudentStep.Find -> ScreenFind(
            state = state,
            onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
            onNavigateResults = { step = StudentStep.Results }
        )
        is StudentStep.Results -> ScreenFindResults(
            state = state,
            onTutorSearchTutorChanged = onTutorSearchTutorChanged,
            onTutorSearchSubjectChanged = onTutorSearchSubjectChanged,
            onTutorSearchDateChanged = onTutorSearchDateChanged,
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
            onBack = { step = StudentStep.Results },
            onBook = { step = StudentStep.Booking(s.tutorId, s.tutorName) }
        )
        is StudentStep.Booking -> ScreenBooking(
            state = state,
            tutorId = s.tutorId,
            tutorName = s.tutorName,
            onBack = { step = StudentStep.Profile(s.tutorId, s.tutorName) },
            onConfirm = { slotId, slot ->
                onClearMessages()
                onBookTutorSlot(slotId)
                step = StudentStep.Confirm(slotId, slot)
            }
        )
        is StudentStep.Confirm -> ScreenBookingConfirm(
            slot = s.slot,
            onClose = { step = StudentStep.Find }
        )
    }
}

// ─── ScreenFind ────────────────────────────────────────────────────────────────

@Composable
private fun ScreenFind(
    state: SmartCampusUiState,
    onTutorSearchSubjectChanged: (String) -> Unit,
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
    val searchQuery = state.tutorSearchFilters.subjectQuery

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
            // Big search field
            SearchField(
                value = searchQuery,
                onValueChange = { onTutorSearchSubjectChanged(it) },
                placeholder = "Search a subject, topic or tutor",
                big = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateResults() }
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips — DESIGN-PLACEHOLDER: no format/mode filter callback in VM
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Chip(text = "All", selected = true) // DESIGN-PLACEHOLDER
                Chip(text = "Online", leadingIcon = SoftIcons.globe) // DESIGN-PLACEHOLDER
                Chip(text = "In person", leadingIcon = SoftIcons.pin) // DESIGN-PLACEHOLDER
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
                            .clickable { onNavigateResults() } // DESIGN-PLACEHOLDER
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
    onTutorSearchTutorChanged: (String) -> Unit,
    onTutorSearchSubjectChanged: (String) -> Unit,
    onTutorSearchDateChanged: (String) -> Unit,
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
                value = filters.subjectQuery,
                onValueChange = onTutorSearchSubjectChanged,
                placeholder = "Search subjects or tutors",
                modifier = Modifier.weight(1f),
                trailing = if (filters.subjectQuery.isNotBlank()) {
                    { SoftIconButton(icon = SoftIcons.x, onClick = { onTutorSearchSubjectChanged("") }) }
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
                // Tutor filter chip (wire to onTutorSearchTutorChanged quick-clear)
                if (filters.tutorQuery.isNotBlank()) {
                    Chip(
                        text = filters.tutorQuery,
                        selected = true,
                        leadingIcon = SoftIcons.user
                    ) { onTutorSearchTutorChanged("") }
                }
                // DESIGN-PLACEHOLDER format/mode chips
                Chip(text = "Online", leadingIcon = SoftIcons.globe) // DESIGN-PLACEHOLDER
                Chip(text = "Top rated", leadingIcon = SoftIcons.star) // DESIGN-PLACEHOLDER
            }
        }

        Spacer(Modifier.height(4.dp))

        // Tutor result cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tutorSlotMap.isEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    CardFlat {
                        Text(
                            text = "No tutors match your search.",
                            style = SoftType.bodySm,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            } else {
                items(tutorSlotMap.entries.toList()) { (tutorId, slots) ->
                    val firstSlot = slots.first()
                    val subjects = slots.map { it.subject }.distinct().take(3)
                    val nextSlot = slots.minByOrNull { it.dateLabel + it.startHour }

                    SoftCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onOpenProfile(tutorId, firstSlot.tutorDisplayName) }
                    ) {
                        // Header row: avatar + name + rate placeholder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(13.dp)
                        ) {
                            InitialsAvatar(name = firstSlot.tutorDisplayName, size = 56.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = firstSlot.tutorDisplayName,
                                        style = SoftType.title.copy(fontSize = 15.sp)
                                    )
                                    // DESIGN-PLACEHOLDER: rate not in TutorAvailabilityUi
                                    Text(
                                        text = "— zł",
                                        style = TextStyle(
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Primary600
                                        )
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = subjects.joinToString(" · "),
                                    style = SoftType.bodySm
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StarsRow(value = 5f) // DESIGN-PLACEHOLDER: no rating in model
                                    Text(
                                        text = "5.0",
                                        style = SoftType.meta.copy(fontSize = 12.sp)
                                    )
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
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

// ─── ScreenTutorProfile ───────────────────────────────────────────────────────

@Composable
private fun ScreenTutorProfile(
    state: SmartCampusUiState,
    tutorId: String,
    tutorName: String,
    onBack: () -> Unit,
    onBook: () -> Unit
) {
    val tutorSlots = remember(state.dashboardState.availableTutorSlots, tutorId) {
        state.dashboardState.availableTutorSlots.filter { it.tutorId == tutorId }
    }
    val subjects = tutorSlots.map { it.subject }.distinct()
    val reviews = remember(state.dashboardState.reviewsForMe, tutorId) {
        state.dashboardState.reviewsForMe.filter { it.tutorId == tutorId }.take(3)
    }

    // DayStrip data: unique dates from tutor's open slots
    val today = LocalDate.now()
    val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
    val dowFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    val availableDates = tutorSlots.map { it.dateLabel }.toSet()
    val dayCells = (0..5).map { offset ->
        val date = today.plusDays(offset.toLong())
        val label = date.format(isoFmt)
        val slotCount = tutorSlots.count { it.dateLabel == label }
        Triple(date.format(dowFmt), date.dayOfMonth.toString(), slotCount)
    }

    var selectedDayIndex by remember { mutableStateOf(0) }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftIconButton(icon = SoftIcons.heart, onClick = {}) // DESIGN-PLACEHOLDER
                    SoftIconButton(icon = SoftIcons.more, onClick = {}) // DESIGN-PLACEHOLDER
                }
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
                        Text(text = "PWr tutor", style = SoftType.meta)
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
                            Badge(text = "Verified PWr", tone = BadgeTone.Prim)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stat strip
                CardQ {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // DESIGN-PLACEHOLDER: rating not in model — show slot count instead
                        listOf(
                            Triple("${tutorSlots.size}", "Slots", null as Nothing?),
                            Triple("5.0", "Rating", null), // DESIGN-PLACEHOLDER
                            Triple("PWr", "Verified", null) // DESIGN-PLACEHOLDER
                        ).forEachIndexed { idx, (value, label, _) ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (idx > 0) {
                                    Row(
                                        modifier = Modifier
                                            .height(40.dp)
                                            .width(1.dp)
                                            .background(Line),
                                        content = {}
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "${tutorSlots.size}" to "Slots",
                            "5.0" to "Rating", // DESIGN-PLACEHOLDER
                            "PWr" to "Verified" // DESIGN-PLACEHOLDER
                        ).forEachIndexed { idx, (value, label) ->
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

                // Bio — DESIGN-PLACEHOLDER (no bio field in model)
                Text(
                    text = "Open slots available for PWr students. Book a session to get started.",
                    style = SoftType.body
                )

                Spacer(Modifier.height(14.dp))

                // Subject chips
                if (subjects.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { subj ->
                            Chip(text = subj)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // DayStrip
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHead(title = "Pick a day")
                Spacer(Modifier.height(12.dp))
                DayStrip {
                    dayCells.forEachIndexed { idx, (dow, dayNum, slotCount) ->
                        DayCell(
                            dow = dow,
                            dayNum = dayNum,
                            slotLabel = if (slotCount > 0) "$slotCount slot" else "–",
                            selected = idx == selectedDayIndex,
                            enabled = slotCount > 0 || idx == selectedDayIndex,
                            onClick = { selectedDayIndex = idx }
                        )
                    }
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

        // Sticky footer CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, Line, RoundedCornerShape(0.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(text = "Starting at", style = SoftType.meta.copy(fontSize = 11.5.sp))
                    // DESIGN-PLACEHOLDER: rate not in model
                    Text(text = "— zł/h", style = SoftType.h2)
                }
                SoftButton(
                    text = "Book a session",
                    onClick = onBook,
                    modifier = Modifier.weight(1f),
                    trailingIcon = SoftIcons.arrow
                )
            }
        }
    }
}

// ─── ScreenBooking ────────────────────────────────────────────────────────────

@Composable
private fun ScreenBooking(
    state: SmartCampusUiState,
    tutorId: String,
    tutorName: String,
    onBack: () -> Unit,
    onConfirm: (String, TutorAvailabilityUi) -> Unit
) {
    val tutorSlots = remember(state.dashboardState.availableTutorSlots, tutorId) {
        state.dashboardState.availableTutorSlots.filter { it.tutorId == tutorId }
    }

    // Available dates
    val today = LocalDate.now()
    val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
    val dowFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
    val days = (0..5).map { offset ->
        val date = today.plusDays(offset.toLong())
        date.format(isoFmt) to Pair(date.format(dowFmt), date.dayOfMonth.toString())
    }

    var selectedDateIndex by remember { mutableStateOf(0) }
    var selectedSlotId by remember { mutableStateOf<String?>(null) }

    val selectedDate = days.getOrNull(selectedDateIndex)?.first ?: ""
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
                            // DESIGN-PLACEHOLDER: rate not in model
                            Text(
                                text = "$firstSubj · — zł/h",
                                style = SoftType.meta.copy(fontSize = 12.sp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatusDot()
                            Badge(text = "Free", tone = BadgeTone.Green)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Choose date
                Text("Choose a date", style = SoftType.h3)
                Spacer(Modifier.height(12.dp))
                DayStrip {
                    days.forEachIndexed { idx, (dateLabel, pair) ->
                        val hasSlots = tutorSlots.any { it.dateLabel == dateLabel }
                        DayCell(
                            dow = pair.first,
                            dayNum = pair.second,
                            selected = idx == selectedDateIndex,
                            enabled = hasSlots,
                            onClick = {
                                selectedDateIndex = idx
                                selectedSlotId = null
                            }
                        )
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
                                        onClick = { selectedSlotId = slot.id }
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
                                        onClick = { selectedSlotId = slot.id }
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

                Spacer(Modifier.height(20.dp))

                // Duration chips — DESIGN-PLACEHOLDER: no duration field in model
                Text("Duration", style = SoftType.h3)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = "1 h") // DESIGN-PLACEHOLDER
                    Chip(text = "1.5 h", selected = true) // DESIGN-PLACEHOLDER
                    Chip(text = "2 h") // DESIGN-PLACEHOLDER
                }

                Spacer(Modifier.height(20.dp))

                // Format chips — DESIGN-PLACEHOLDER: no format field in model
                Text("Format", style = SoftType.h3)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = "Online", selected = true, leadingIcon = SoftIcons.globe) // DESIGN-PLACEHOLDER
                    Chip(text = "In person", leadingIcon = SoftIcons.pin) // DESIGN-PLACEHOLDER
                }
            }
        }

        // Sticky footer
        val activeSlot = slotsForDay.find { it.id == selectedSlotId } ?: selectedSlot
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, Line, RoundedCornerShape(0.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Total · 1h", style = SoftType.meta.copy(fontSize = 11.5.sp)) // DESIGN-PLACEHOLDER
                    Text("— zł", style = SoftType.h2) // DESIGN-PLACEHOLDER: no rate in model
                }
                SoftButton(
                    text = "Review & book",
                    onClick = {
                        activeSlot?.let { slot ->
                            onConfirm(slot.id, slot)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = activeSlot != null,
                    trailingIcon = SoftIcons.arrow
                )
            }
        }
    }
}

// ─── ScreenBookingConfirm ─────────────────────────────────────────────────────

@Composable
private fun ScreenBookingConfirm(
    slot: TutorAvailabilityUi,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        SoftTopBar(
            title = "Booked",
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

            // Success check circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(GreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SoftIcons.check,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("You're all set!", style = SoftType.display)
            Spacer(Modifier.height(8.dp))
            Text(
                "See you on ${slot.dateLabel}. A receipt is in your inbox.",
                style = SoftType.body
            )

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
                    Badge(text = "Confirmed", tone = BadgeTone.Prim)
                }

                Spacer(Modifier.height(16.dp))
                SoftDivider()
                Spacer(Modifier.height(16.dp))

                // Ticket grid: Date / Time / Format / Total
                val ticketRows = listOf(
                    "Date" to slot.dateLabel,
                    "Time" to slot.timeLabel,
                    "Format" to "Online", // DESIGN-PLACEHOLDER: no format in model
                    "Total" to "Confirmed" // DESIGN-PLACEHOLDER: no price in model
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
            }

            Spacer(Modifier.height(12.dp))

            // Notes card
            CardFlat {
                Text(
                    "I want to work on",
                    style = SoftType.eyebrow.copy(color = Ink3)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "— // DESIGN-PLACEHOLDER: no notes field in booking model",
                    style = SoftType.bodySm
                )
            }

            Spacer(Modifier.height(16.dp))

            // Add to calendar — DESIGN-PLACEHOLDER
            SoftButton(
                text = "Add to calendar",
                onClick = {}, // DESIGN-PLACEHOLDER
                modifier = Modifier.fillMaxWidth(),
                variant = SoftButtonVariant.Primary,
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
                    onClick = {}, // DESIGN-PLACEHOLDER: would navigate to chat
                    modifier = Modifier.weight(1f),
                    variant = SoftButtonVariant.Outline
                )
                SoftButton(
                    text = "View lesson",
                    onClick = {}, // DESIGN-PLACEHOLDER: would navigate to lessons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorScheduleScreen(
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
    var showNewSlotForm by remember { mutableStateOf(false) }

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
    val days = (0..5).map { offset ->
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
                    // Open slot — CardFlat with edit icon
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
                            // Delete/edit icon
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

                        // 2-column grid: Subject / Rate  |  Date / Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                SoftTextField(
                                    value = state.availabilityForm.subject,
                                    onValueChange = onAvailabilitySubjectChanged,
                                    label = "Subject",
                                    placeholder = "e.g. Calculus II"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                // Rate — DESIGN-PLACEHOLDER: no rate field in AvailabilityFormState
                                SoftTextField(
                                    value = "", // DESIGN-PLACEHOLDER
                                    onValueChange = {}, // DESIGN-PLACEHOLDER
                                    label = "Rate",
                                    placeholder = "e.g. 45 zł/h" // DESIGN-PLACEHOLDER
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true }
                                ) {
                                    SoftTextField(
                                        value = state.availabilityForm.date,
                                        onValueChange = {},
                                        label = "Date",
                                        placeholder = "Tap to pick"
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                // Start + end time inline
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showStartTimePicker = true }
                                    ) {
                                        SoftTextField(
                                            value = state.availabilityForm.startHour,
                                            onValueChange = {},
                                            label = "From",
                                            placeholder = "12:00"
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showEndTimePicker = true }
                                    ) {
                                        SoftTextField(
                                            value = state.availabilityForm.endHour,
                                            onValueChange = {},
                                            label = "To",
                                            placeholder = "13:00"
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        SoftButton(
                            text = "Publish slot",
                            onClick = {
                                onClearMessages()
                                onAddAvailability()
                                showNewSlotForm = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.availabilityForm.subject.isNotBlank()
                                && state.availabilityForm.date.isNotBlank()
                                && state.availabilityForm.startHour.isNotBlank()
                                && state.availabilityForm.endHour.isNotBlank(),
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
