package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardFlat
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.RoleSwitch
import Smart.Campus.PWR.ui.components.softindigo.SearchField
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.StarsRow
import Smart.Campus.PWR.ui.components.softindigo.SubjectDot
import Smart.Campus.PWR.ui.components.softindigo.Tile
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.DashboardRoutes
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Primary700
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.White
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// HomeTab — public entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onRefresh: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onCancelBooking: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit,
    onToggleRole: () -> Unit,
    onOpenNotifications: () -> Unit,
    onAcceptBooking: (String) -> Unit = {},
    onDeclineBooking: (String, String) -> Unit = { _, _ -> },
    onMarkCompleted: (String) -> Unit = {},
    onMarkNoShow: (String) -> Unit = {}
) {
    when (activeRole) {
        UserRole.STUDENT -> StudentHomeContent(
            state = state,
            onToggleRole = { onClearMessages(); onToggleRole() },
            onNavigate = onNavigate,
            onOpenNotifications = onOpenNotifications
        )
        UserRole.TUTOR -> TutorTodayContent(
            state = state,
            onToggleRole = { onClearMessages(); onToggleRole() },
            onNavigate = onNavigate,
            onOpenNotifications = onOpenNotifications,
            onAcceptBooking = onAcceptBooking,
            onDeclineBooking = onDeclineBooking,
            onMarkCompleted = onMarkCompleted,
            onMarkNoShow = onMarkNoShow
        )
        else -> StudentHomeContent(
            state = state,
            onToggleRole = { onClearMessages(); onToggleRole() },
            onNavigate = onNavigate,
            onOpenNotifications = onOpenNotifications
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Student Home
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StudentHomeContent(
    state: SmartCampusUiState,
    onToggleRole: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val displayName = state.currentUser?.displayName.orEmpty()
    val firstName = displayName.split(" ").firstOrNull().orEmpty()
    val greeting = greetingForHour(LocalTime.now().hour)
    val lessons = state.dashboardState.myStudentBookings
    val nextLesson = pickNextLesson(lessons)
    val nextLessonMinutes = nextLesson?.let { minutesUntil(it) }
    // Recommended tutors sorted by rating descending
    val recommendedTutors = state.dashboardState.tutors
        .sortedByDescending { it.ratingAvg }
        .take(4)
    // Subject counts from available slots (distinct tutor IDs per subject)
    val availableSlots = state.dashboardState.availableTutorSlots
    val subjectSlotCounts = availableSlots
        .groupBy { it.subject }
        .mapValues { (_, slots) -> slots.map { it.tutorId }.distinct().size }

    MainList {
        // ── Top bar ──────────────────────────────────────────────────────────
        SoftTopBar(
            leading = {
                InitialsAvatar(name = displayName.ifBlank { "?" }, size = 40.dp)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = greeting,
                        style = SoftType.meta
                    )
                    Text(
                        text = firstName.ifBlank { displayName },
                        style = SoftType.title
                    )
                }
            },
            actions = {
                SoftIconButton(
                    icon = SoftIcons.bell,
                    onClick = onOpenNotifications,
                    badgeCount = state.dashboardState.notificationCount
                )
            }
        )

        // ── Role switch ───────────────────────────────────────────────────────
        RoleSwitch(isTutor = false, onToggle = onToggleRole, fullWidth = true)

        MessageBlock(state.errorMessage, state.infoMessage)

        // ── Next lesson hero card ─────────────────────────────────────────────
        if (nextLesson != null) {
            val context = LocalContext.current
            NextLessonHeroCard(
                lesson = nextLesson,
                minutesUntil = nextLessonMinutes,
                onMessage = { onNavigate(DashboardRoutes.CHAT) },
                onJoin = {
                    if (nextLesson.meetingUrl.isNotBlank()) {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(nextLesson.meetingUrl)))
                        }
                    } else {
                        onNavigate(DashboardRoutes.LESSONS)
                    }
                }
            )
        } else {
            EmptyNextLessonCard()
        }

        // ── Assignments shortcut (Tasks is not in the student bottom nav) ────
        CardQ(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNavigate(DashboardRoutes.ASSIGNMENTS) },
            padding = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.task,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Assignments", style = SoftType.title)
                    Text(text = "Submit work and track deadlines", style = SoftType.meta)
                }
                Icon(
                    imageVector = SoftIcons.chev,
                    contentDescription = null,
                    tint = Ink3,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Browse by subject ─────────────────────────────────────────────────
        SectionHead(
            title = "Browse by subject",
            action = "See all",
            onAction = { onNavigate(DashboardRoutes.CALENDAR) }
        )
        BrowseSubjectsGrid(
            subjectCounts = subjectSlotCounts,
            studentBookings = lessons,
            onSubjectClick = { onNavigate(DashboardRoutes.CALENDAR) }
        )

        // ── Recommended tutors ────────────────────────────────────────────────
        SectionHead(
            title = "Recommended for you",
            action = "See all",
            onAction = { onNavigate(DashboardRoutes.CALENDAR) }
        )
        if (recommendedTutors.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No tutors to show yet — check the Calendar tab.",
                    style = SoftType.bodySm
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recommendedTutors.forEach { tutor ->
                    TutorRecommendationCard(
                        tutor = tutor,
                        onClick = { onNavigate(DashboardRoutes.CALENDAR) }
                    )
                }
            }
        }

        // Bottom padding so nav bar doesn't cover last card
        Spacer(Modifier.height(4.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tutor Today
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TutorTodayContent(
    state: SmartCampusUiState,
    onToggleRole: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onAcceptBooking: (String) -> Unit,
    onDeclineBooking: (String, String) -> Unit,
    onMarkCompleted: (String) -> Unit,
    onMarkNoShow: (String) -> Unit
) {
    val displayName = state.currentUser?.displayName.orEmpty()
    val firstName = displayName.split(" ").firstOrNull().orEmpty()
    val greeting = greetingForHour(LocalTime.now().hour)
    val allTutorBookings = state.dashboardState.myTutorBookings
    val todayStr = LocalDate.now().toString()

    // Today's bookings: CONFIRMED or COMPLETED/NO_SHOW for today
    val todayBookings = allTutorBookings.filter { it.dateLabel == todayStr }
        .filter { it.status in listOf("CONFIRMED", "COMPLETED", "NO_SHOW", "PENDING") }

    // Pending requests (tutor action required)
    val pendingRequests = allTutorBookings.filter { it.status == "PENDING" }

    // Summary stats
    val sessionsCount = todayBookings.size
    val bookedHours = computeHours(todayBookings)
    val distinctStudents = todayBookings.map { it.studentId }.distinct().size

    // Today's date label for hero card
    val todayDayName = LocalDate.now().dayOfWeek
        .getDisplayName(JTextStyle.FULL, Locale.ENGLISH)
    val todayDateNum = LocalDate.now().dayOfMonth
    val todayMonthName = LocalDate.now().month
        .getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)
    val todayLabel = "$todayDayName · $todayMonthName $todayDateNum"

    // How many lessons still to happen today
    val lessonsLeft = todayBookings.filter { session ->
        session.status == "CONFIRMED" && parseLessonDateTime(session)?.isAfter(LocalDateTime.now()) == true
    }.size

    MainList {
        // ── Top bar ──────────────────────────────────────────────────────────
        SoftTopBar(
            leading = {
                InitialsAvatar(name = displayName.ifBlank { "?" }, size = 40.dp)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(text = greeting, style = SoftType.meta)
                    Text(
                        text = firstName.ifBlank { displayName },
                        style = SoftType.title
                    )
                }
            },
            actions = {
                SoftIconButton(
                    icon = SoftIcons.bell,
                    onClick = onOpenNotifications,
                    badgeCount = state.dashboardState.notificationCount
                )
            }
        )

        // ── Role switch ───────────────────────────────────────────────────────
        RoleSwitch(isTutor = true, onToggle = onToggleRole, fullWidth = true)

        MessageBlock(state.errorMessage, state.infoMessage)

        // ── Today summary hero card ───────────────────────────────────────────
        TutorSummaryHeroCard(
            dateLabel = todayLabel,
            lessonsLeft = lessonsLeft,
            sessionsCount = sessionsCount,
            bookedHours = bookedHours,
            distinctStudents = distinctStudents
        )

        // ── Booking requests ─────────────────────────────────────────────────
        SectionHead(title = "Booking requests")
        if (pendingRequests.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No pending requests.",
                    style = SoftType.bodySm
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pendingRequests.forEach { request ->
                    BookingRequestCard(
                        booking = request,
                        onAccept = { onAcceptBooking(request.id) },
                        onDecline = { onDeclineBooking(request.id, "Declined by tutor") }
                    )
                }
            }
        }

        // ── Today's schedule ─────────────────────────────────────────────────
        SectionHead(
            title = "Today's schedule",
            action = "Full week",
            onAction = { onNavigate(DashboardRoutes.CALENDAR) }
        )
        if (todayBookings.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No lessons booked for today.",
                    style = SoftType.bodySm
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                todayBookings
                    .filter { it.status in listOf("CONFIRMED", "COMPLETED", "NO_SHOW") }
                    .sortedWith(compareBy { it.startHour })
                    .forEach { session ->
                        TodayScheduleRow(
                            session = session,
                            onMarkCompleted = onMarkCompleted,
                            onMarkNoShow = onMarkNoShow
                        )
                    }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextLessonHeroCard(
    lesson: LessonBookingUi,
    minutesUntil: Long?,
    onMessage: () -> Unit,
    onJoin: () -> Unit
) {
    val timeUntilLabel = when {
        minutesUntil == null -> "Upcoming"
        minutesUntil <= 0 -> "Starting now"
        minutesUntil < 60 -> "In ${minutesUntil}m"
        else -> {
            val h = minutesUntil / 60
            val m = minutesUntil % 60
            if (m == 0L) "In ${h}h" else "In ${h}h ${m}m"
        }
    }
    val heroShape = RoundedCornerShape(22.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6D5DF2), Color(0xFF5037DC)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(gradient)
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)) {
            // Header row: time badge + "Next lesson" label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = SoftIcons.clock,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = timeUntilLabel,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = White
                        )
                    )
                }
                Text(
                    text = "Next lesson",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Tutor avatar + lesson info
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                InitialsAvatar(
                    name = lesson.tutorDisplayName,
                    size = 50.dp,
                    square = true
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = lesson.subject,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = White
                        )
                    )
                    Text(
                        text = "with ${lesson.tutorDisplayName}",
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = White.copy(alpha = 0.85f)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        HeroBadge("${lesson.startHour}–${lesson.endHour}")
                        if (lesson.format.isNotBlank()) HeroBadge(lesson.format.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        // Action buttons strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White.copy(alpha = 0.08f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftButton(
                text = "Message",
                onClick = onMessage,
                modifier = Modifier.weight(1f),
                variant = SoftButtonVariant.Ghost,
                size = SoftButtonSize.Sm
            )
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Primary)
                    ) { onJoin() }
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = SoftIcons.video,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Join lesson",
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary600
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBadge(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(White.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            color = White
        )
    )
}

@Composable
private fun EmptyNextLessonCard() {
    CardQ(modifier = Modifier.fillMaxWidth()) {
        Text(text = "No upcoming lessons", style = SoftType.title)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Browse open tutor slots on the Calendar tab.",
            style = SoftType.bodySm
        )
    }
}

@Composable
private fun BrowseSubjectsGrid(
    subjectCounts: Map<String, Int>,
    studentBookings: List<LessonBookingUi>,
    onSubjectClick: () -> Unit
) {
    // Merge subjects from live slot data + bookings + static starter set
    val fromSlots = subjectCounts.keys.toList()
    val fromBookings = studentBookings.map { it.subject }.distinct()
    val staticSubjects = listOf("Calculus", "Lin. Algebra", "Thermo", "OOP / Java")
    val subjects = (fromSlots + fromBookings + staticSubjects).distinct().take(6)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        subjects.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { subject ->
                    val (_, fg) = subjectColors(subject)
                    val count = subjectCounts[subject]
                    Tile(
                        modifier = Modifier.weight(1f),
                        padding = 13.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSubjectClick() }
                        ) {
                            SubjectDot(subject = subject, size = 40.dp, icon = SoftIcons.cap)
                            Column {
                                Text(
                                    text = subject,
                                    style = TextStyle(
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = InkToken
                                    )
                                )
                                Text(
                                    text = if (count != null && count > 0) "$count ${if (count == 1) "tutor" else "tutors"}" else "Find tutors",
                                    style = SoftType.meta
                                )
                            }
                        }
                    }
                }
                // Fill last slot if row is odd
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TutorRecommendationCard(
    tutor: Smart.Campus.PWR.ui.state.TutorSummaryUi,
    onClick: () -> Unit = {}
) {
    CardQ(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        padding = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            InitialsAvatar(name = tutor.displayName, size = 52.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tutor.displayName,
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = InkToken
                            )
                        )
                        if (tutor.verified) {
                            Badge(text = "Verified", tone = BadgeTone.Green)
                        }
                    }
                }
                Text(
                    text = tutor.subjects.ifBlank { "Various subjects" },
                    style = SoftType.bodySm,
                    modifier = Modifier.padding(top = 1.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayRating = tutor.ratingAvg.toFloat().coerceIn(0f, 5f)
                    StarsRow(value = if (tutor.ratingCount > 0) displayRating else 0f)
                    if (tutor.ratingCount > 0) {
                        Text(
                            text = "${String.format(Locale.ENGLISH, "%.1f", tutor.ratingAvg)} (${tutor.ratingCount})",
                            style = SoftType.meta
                        )
                    } else {
                        Text(text = "No reviews yet", style = SoftType.meta)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tutor Today sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TutorSummaryHeroCard(
    dateLabel: String,
    lessonsLeft: Int,
    sessionsCount: Int,
    bookedHours: Double,
    distinctStudents: Int
) {
    val heroShape = RoundedCornerShape(22.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6D5DF2), Color(0xFF5037DC)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(gradient)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = White.copy(alpha = 0.85f)
                )
            )
            Text(
                text = "$lessonsLeft ${if (lessonsLeft == 1) "lesson" else "lessons"} left",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = White
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            listOf(
                sessionsCount.toString() to "Sessions",
                "${formatHours(bookedHours)}h" to "Booked",
                distinctStudents.toString() to "Students"
            ).forEachIndexed { index, (value, label) ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(White.copy(alpha = 0.2f))
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (index > 0) 12.dp else 0.dp)
                ) {
                    Text(
                        text = value,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = White
                        )
                    )
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

/** Card for a pending booking request that the tutor can accept or decline. */
@Composable
private fun BookingRequestCard(
    booking: LessonBookingUi,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    CardQ(modifier = Modifier.fillMaxWidth(), padding = 15.dp) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(name = booking.studentDisplayName.ifBlank { "?" }, size = 42.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.studentDisplayName.ifBlank { "Student" },
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = InkToken
                        )
                    )
                    Badge(text = "Pending", tone = BadgeTone.Amber)
                }
                val (_, subjectFg) = subjectColors(booking.subject)
                Text(
                    text = "${booking.subject} · ${booking.dateLabel} · ${booking.startHour}",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = subjectFg
                    )
                )
                // Optional topic / request message
                val detail = listOfNotNull(
                    booking.topic.takeIf { it.isNotBlank() }?.let { "Topic: $it" },
                    booking.requestMessage.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = SoftType.bodySm,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftButton(
                        text = "Accept",
                        onClick = onAccept,
                        variant = SoftButtonVariant.Primary,
                        size = SoftButtonSize.Sm,
                        modifier = Modifier.weight(1f)
                    )
                    SoftButton(
                        text = "Decline",
                        onClick = onDecline,
                        variant = SoftButtonVariant.Ghost,
                        size = SoftButtonSize.Sm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayScheduleRow(
    session: LessonBookingUi,
    onMarkCompleted: (String) -> Unit,
    onMarkNoShow: (String) -> Unit
) {
    CardQ(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        ScheduleRowContent(
            session = session,
            onMarkCompleted = onMarkCompleted,
            onMarkNoShow = onMarkNoShow
        )
    }
}

@Composable
private fun ScheduleRowContent(
    session: LessonBookingUi,
    onMarkCompleted: (String) -> Unit,
    onMarkNoShow: (String) -> Unit
) {
    val now = LocalDateTime.now()
    val dt = parseLessonDateTime(session)
    // A CONFIRMED lesson is "past-or-now" if its start time is <= now
    val isPastOrNow = dt != null && !dt.isAfter(now)

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Time column
        Column(modifier = Modifier.width(50.dp)) {
            Text(
                text = session.startHour,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = InkToken
                )
            )
            val durationMin = run {
                val start = parseHour(session.startHour)
                val end = parseHour(session.endHour)
                if (start != null && end != null && end > start) ((end - start) * 60).toInt() else null
            }
            if (durationMin != null) {
                Text(text = "${durationMin} min", style = SoftType.meta)
            }
        }

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(52.dp)
                .background(Smart.Campus.PWR.ui.theme.Line)
        )

        // Session info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.subject,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = InkToken
                    )
                )
                // Status badge
                when (session.status) {
                    "PENDING"   -> Badge(text = "Pending",   tone = BadgeTone.Amber)
                    "CONFIRMED" -> Badge(text = "Confirmed", tone = BadgeTone.Green)
                    "COMPLETED" -> Badge(text = "Done",      tone = BadgeTone.Gray)
                    "NO_SHOW"   -> Badge(text = "No-show",   tone = BadgeTone.Red)
                    else        -> Badge(text = session.status, tone = BadgeTone.Gray)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (session.studentDisplayName.isNotBlank()) {
                    InitialsAvatar(name = session.studentDisplayName, size = 20.dp)
                }
                Text(
                    text = session.studentDisplayName.ifBlank { "No student" },
                    style = SoftType.bodySm
                )
            }

            // Mark done / No-show buttons for CONFIRMED lessons that are past or happening now
            if (session.status == "CONFIRMED" && isPastOrNow) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SoftButton(
                        text = "Mark done",
                        onClick = { onMarkCompleted(session.id) },
                        variant = SoftButtonVariant.Soft,
                        size = SoftButtonSize.Sm
                    )
                    SoftButton(
                        text = "No-show",
                        onClick = { onMarkNoShow(session.id) },
                        variant = SoftButtonVariant.Ghost,
                        size = SoftButtonSize.Sm
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logic helpers (preserved from original)
// ─────────────────────────────────────────────────────────────────────────────

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..20 -> "Good evening"
    else -> "Good night"
}

private fun pickNextLesson(lessons: List<LessonBookingUi>): LessonBookingUi? {
    val now = LocalDateTime.now()
    return lessons
        .filter { it.status == "CONFIRMED" }
        .mapNotNull { lesson ->
            val dt = parseLessonDateTime(lesson) ?: return@mapNotNull null
            if (dt.isBefore(now)) null else lesson to dt
        }
        .minByOrNull { it.second }
        ?.first
}

private fun parseLessonDateTime(lesson: LessonBookingUi): LocalDateTime? {
    val date = runCatching { LocalDate.parse(lesson.dateLabel) }.getOrNull() ?: return null
    val time = runCatching {
        LocalTime.parse(lesson.startHour, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    return LocalDateTime.of(date, time)
}

private fun minutesUntil(lesson: LessonBookingUi): Long? {
    val dt = parseLessonDateTime(lesson) ?: return null
    return java.time.Duration.between(LocalDateTime.now(), dt).toMinutes()
}

private fun computeHours(lessons: List<LessonBookingUi>): Double {
    return lessons.sumOf { lesson ->
        val start = parseHour(lesson.startHour)
        val end = parseHour(lesson.endHour)
        if (start != null && end != null && end > start) end - start else 0.0
    }
}

private fun parseHour(s: String): Double? {
    val parts = s.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return h + m / 60.0
}

private fun formatHours(h: Double): String {
    if (h <= 0) return "0"
    val rounded = (h * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else String.format(java.util.Locale.ENGLISH, "%.1f", rounded)
}

// Silence legacy import warnings — these symbols are pulled in transitively
// via the theme but not directly referenced in this file.
@Suppress("UNUSED")
private val _unusedBg = Bg
@Suppress("UNUSED")
private val _unusedP7 = Primary700
@Suppress("UNUSED")
private val _unusedGreen = Green
@Suppress("UNUSED")
private val _unusedInk2 = Ink2
@Suppress("UNUSED")
private val _unusedInk3 = Ink3
@Suppress("UNUSED")
private val _unusedPrimary = Primary
@Suppress("UNUSED")
private val _unusedTransparent = Color.Transparent
