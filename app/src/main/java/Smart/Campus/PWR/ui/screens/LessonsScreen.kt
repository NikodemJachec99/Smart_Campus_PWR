package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SegTabs
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.StatusDot
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.GreenBg
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Public entry point — keep exact signature unchanged (Task 14)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LessonsTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onNavigate: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onClearMessages: () -> Unit
) {
    val bookings = state.dashboardState.myStudentBookings
    val reviewedIds = state.dashboardState.reviewsByMe
        .mapNotNull { it.bookingId.ifBlank { null } }
        .toSet()

    val now = LocalDateTime.now()
    val upcoming = bookings.filter { it.isUpcoming(now) }
    val past = bookings.filter { !it.isUpcoming(now) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabLabels = listOf(
        "Upcoming · ${upcoming.size}",
        "Past · ${past.size}"
    )

    // Most recent past lesson that has NOT yet been reviewed
    val ratePromptLesson = past
        .filter { it.status != "cancelled" && it.id !in reviewedIds }
        .firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // ─── Top bar ─────────────────────────────────────────────────────
        SoftTopBar(
            title = "Lessons",
            actions = {
                SoftIconButton(
                    icon = SoftIcons.calendar,
                    onClick = { onNavigate(DashboardRoutes.CALENDAR) }
                )
            }
        )

        // ─── Seg tabs (outside scroll so they stay visible) ──────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            SegTabs(
                options = tabLabels,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ─── Scrollable content ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 100.dp)
        ) {
            if (selectedTab == 0) {
                UpcomingContent(
                    upcoming = upcoming,
                    ratePromptLesson = ratePromptLesson,
                    onNavigate = onNavigate,
                    onCancelBooking = onCancelBooking,
                    now = now
                )
            } else {
                PastContent(
                    past = past,
                    reviewedIds = reviewedIds,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Upcoming tab content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UpcomingContent(
    upcoming: List<LessonBookingUi>,
    ratePromptLesson: LessonBookingUi?,
    onNavigate: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    now: LocalDateTime
) {
    // Rate prompt banner
    if (ratePromptLesson != null) {
        RatePromptCard(
            lesson = ratePromptLesson,
            onClick = { onNavigate(DashboardRoutes.REVIEWS) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (upcoming.isEmpty()) {
        LessonsEmptyState(
            message = "No upcoming lessons",
            sub = "Book a session with a tutor to get started.",
            onBookClick = { onNavigate(DashboardRoutes.CALENDAR) }
        )
        return
    }

    // Group lessons into "This week", "Next week", "Later"
    val groups = groupByWeek(upcoming, now)

    groups.forEach { (label, lessons) ->
        if (lessons.isNotEmpty()) {
            // Section eyebrow label
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = Ink3
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lessons.forEach { lesson ->
                    UpcomingLessonCard(
                        lesson = lesson,
                        isSoon = lesson.isSoon(now),
                        onCancelBooking = onCancelBooking,
                        onNavigate = onNavigate
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Book another session CTA
    SoftButton(
        text = "Book another session",
        onClick = { onNavigate(DashboardRoutes.CALENDAR) },
        variant = SoftButtonVariant.Soft,
        leadingIcon = SoftIcons.plus,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Past tab content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PastContent(
    past: List<LessonBookingUi>,
    reviewedIds: Set<String>,
    onNavigate: (String) -> Unit
) {
    if (past.isEmpty()) {
        LessonsEmptyState(
            message = "No past lessons yet",
            sub = "Completed and cancelled lessons will appear here.",
            onBookClick = null
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        past.forEach { lesson ->
            PastLessonCard(
                lesson = lesson,
                isReviewed = lesson.id in reviewedIds,
                onRate = { onNavigate(DashboardRoutes.REVIEWS) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rate prompt card (Primary50 background, no shadow)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RatePromptCard(lesson: LessonBookingUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Primary50)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Primary)
            ) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Star icon box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SoftIcons.star,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "How was ${lesson.subject} with ${lesson.tutorDisplayName.substringBefore(" ")}?",
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = InkToken
                ),
                maxLines = 1
            )
            Text(
                text = "Finished ${lesson.dateLabel} · 30 sec",
                style = SoftType.meta
            )
        }

        Icon(
            imageVector = SoftIcons.chev,
            contentDescription = null,
            tint = Primary600,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Upcoming lesson card (SoftCard with day/date block + actions)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UpcomingLessonCard(
    lesson: LessonBookingUi,
    isSoon: Boolean,
    onCancelBooking: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val (dayOfWeek, dayNum) = lesson.parseDayParts()

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Day/date column
            Column(
                modifier = Modifier.width(46.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayOfWeek,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Primary600
                    )
                )
                Text(
                    text = dayNum,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = InkToken
                    )
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(Line)
            )

            // Details column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lesson.subject,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = InkToken
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (isSoon) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(
                            text = "Soon",
                            tone = BadgeTone.Amber,
                            leadingIcon = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InitialsAvatar(
                        name = lesson.tutorDisplayName,
                        size = 20.dp
                    )
                    Text(
                        text = lesson.tutorDisplayName,
                        style = SoftType.bodySm,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.timeLabel,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Ink3
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // DESIGN-PLACEHOLDER: No real reschedule flow exists yet.
            // Button is Outline/Sm. onClick triggers onCancelBooking ONLY when the user
            // taps — it does NOT cancel on render. The reason string marks it as a reschedule
            // request so the admin/tutor can act on it; it is not an auto-cancel.
            SoftButton(
                text = "Reschedule",
                onClick = {
                    onCancelBooking(
                        lesson.id,
                        lesson.availabilityId,
                        "Reschedule requested"
                    )
                },
                variant = SoftButtonVariant.Outline,
                size = SoftButtonSize.Sm,
                modifier = Modifier.weight(1f)
            )

            // DESIGN-PLACEHOLDER: "Join" deep-links to chat; "Details" also goes to chat
            // until a dedicated lesson-detail screen exists.
            SoftButton(
                text = if (isSoon) "Join" else "Details",
                onClick = { onNavigate(DashboardRoutes.CHAT) },
                variant = SoftButtonVariant.Primary,
                size = SoftButtonSize.Sm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Past lesson card (muted, with optional Rate affordance)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PastLessonCard(
    lesson: LessonBookingUi,
    isReviewed: Boolean,
    onRate: () -> Unit
) {
    val isCancelled = lesson.status == "cancelled"

    CardQ(modifier = Modifier
        .fillMaxWidth()
        .alpha(if (isCancelled) 0.65f else 1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subject icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCancelled) Bg2 else Primary50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SoftIcons.cap,
                    contentDescription = null,
                    tint = if (isCancelled) Ink3 else Primary600,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.subject,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isCancelled) Ink3 else InkToken
                    )
                )
                Text(
                    text = "${lesson.tutorDisplayName} · ${lesson.dateLabel}",
                    style = SoftType.meta
                )
                Text(
                    text = lesson.timeLabel,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Ink3
                    )
                )
            }

            // Status badge
            when {
                isCancelled -> Badge(text = "Cancelled", tone = BadgeTone.Red)
                isReviewed -> Badge(text = "Rated", tone = BadgeTone.Green)
                else -> {
                    // Rate affordance for un-reviewed completed lessons
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Primary50)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Primary)
                            ) { onRate() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Rate",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Primary600
                            )
                        )
                    }
                }
            }
        }

        // Cancel reason row if present
        if (isCancelled && lesson.cancelReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            SoftDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reason: ${lesson.cancelReason}",
                style = SoftType.meta
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LessonsEmptyState(
    message: String,
    sub: String,
    onBookClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Bg2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SoftIcons.calendar,
                contentDescription = null,
                tint = Ink3,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = message,
            style = SoftType.h3,
            color = InkToken
        )
        Text(
            text = sub,
            style = SoftType.bodySm,
            color = Ink3
        )
        if (onBookClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            SoftButton(
                text = "Book a session",
                onClick = onBookClick,
                variant = SoftButtonVariant.Primary,
                leadingIcon = SoftIcons.plus
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns true when the booking is not yet finished (status == "booked" AND
 * the lesson end time is still in the future, or status is "booked" with no
 * parseable date yet).
 */
private fun LessonBookingUi.isUpcoming(now: LocalDateTime): Boolean {
    if (status == "completed" || status == "cancelled") return false
    val end = parseLessonEndDt() ?: return true   // treat unparseable as upcoming
    return end.isAfter(now)
}

/**
 * Returns true when the lesson starts within the next 2 hours.
 */
private fun LessonBookingUi.isSoon(now: LocalDateTime): Boolean {
    val start = parseLessonStartDt() ?: return false
    val twoHoursAhead = now.plusHours(2)
    return !start.isBefore(now) && start.isBefore(twoHoursAhead)
}

private fun LessonBookingUi.parseLessonEndDt(): LocalDateTime? {
    val date = runCatching { LocalDate.parse(dateLabel) }.getOrNull() ?: return null
    val time = runCatching {
        LocalTime.parse(endHour, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    return LocalDateTime.of(date, time)
}

private fun LessonBookingUi.parseLessonStartDt(): LocalDateTime? {
    val date = runCatching { LocalDate.parse(dateLabel) }.getOrNull() ?: return null
    val time = runCatching {
        LocalTime.parse(startHour, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    return LocalDateTime.of(date, time)
}

/**
 * Returns (dayOfWeekAbbr, dayOfMonthNumber) as a pair of strings.
 * Falls back to (dateLabel, "") when parsing fails.
 */
private fun LessonBookingUi.parseDayParts(): Pair<String, String> {
    val date = runCatching { LocalDate.parse(dateLabel) }.getOrNull()
        ?: return Pair(dateLabel, "")
    val dayAbbr = date.dayOfWeek.name.take(3)
    val dayNum = date.dayOfMonth.toString()
    return Pair(dayAbbr, dayNum)
}

/**
 * Groups lessons into "This week", "Next week", "Later" based on ISO week number.
 * Uses the natural list order within each group.
 */
private fun groupByWeek(
    lessons: List<LessonBookingUi>,
    now: LocalDateTime
): LinkedHashMap<String, List<LessonBookingUi>> {
    val weekFields = WeekFields.of(Locale.getDefault())
    val currentWeek = now.toLocalDate().get(weekFields.weekOfWeekBasedYear())
    val currentYear = now.toLocalDate().get(weekFields.weekBasedYear())

    val thisWeek = mutableListOf<LessonBookingUi>()
    val nextWeek = mutableListOf<LessonBookingUi>()
    val later = mutableListOf<LessonBookingUi>()

    for (lesson in lessons) {
        val date = runCatching { LocalDate.parse(lesson.dateLabel) }.getOrNull()
        if (date == null) {
            later.add(lesson)
            continue
        }
        val lessonWeek = date.get(weekFields.weekOfWeekBasedYear())
        val lessonYear = date.get(weekFields.weekBasedYear())

        when {
            lessonYear == currentYear && lessonWeek == currentWeek -> thisWeek.add(lesson)
            lessonYear == currentYear && lessonWeek == currentWeek + 1 ||
                (currentWeek == 52 && lessonYear == currentYear + 1 && lessonWeek == 1) ->
                nextWeek.add(lesson)
            else -> later.add(lesson)
        }
    }

    return linkedMapOf(
        "This week" to thisWeek,
        "Next week" to nextWeek,
        "Later" to later
    )
}
