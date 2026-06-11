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
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
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
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
// Public entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LessonsTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onNavigate: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onClearMessages: () -> Unit,
    onStartReschedule: (String) -> Unit = {},
    onReschedule: (String, String) -> Unit = { _, _ -> },
    onClearReschedule: () -> Unit = {},
    onOpenDirectWith: (String, String) -> Unit = { _, _ -> }
) {
    val bookings = state.dashboardState.myStudentBookings
    val reviewedIds = state.dashboardState.reviewsByMe
        .mapNotNull { it.bookingId.ifBlank { null } }
        .toSet()

    val now = LocalDateTime.now()

    // Status-aware partition: upcoming = PENDING or CONFIRMED (and not past-dated);
    // past = COMPLETED / NO_SHOW / CANCELLED / DECLINED or past-dated PENDING/CONFIRMED.
    val upcoming = bookings.filter { it.isUpcoming(now) }
    val past     = bookings.filter { !it.isUpcoming(now) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabLabels = listOf(
        "Upcoming · ${upcoming.size}",
        "Past · ${past.size}"
    )

    // Most recent past lesson that has NOT yet been reviewed (exclude fully cancelled/declined)
    val ratePromptLesson = past
        .filter { b ->
            val s = b.status.uppercase()
            s == "COMPLETED" && b.id !in reviewedIds
        }
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
                    rescheduleTargetBookingId = state.rescheduleTargetBookingId,
                    availableTutorSlots = state.dashboardState.availableTutorSlots,
                    onNavigate = onNavigate,
                    onOpenDirectWith = onOpenDirectWith,
                    onCancelBooking = onCancelBooking,
                    onStartReschedule = onStartReschedule,
                    onReschedule = onReschedule,
                    onClearReschedule = onClearReschedule,
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
    rescheduleTargetBookingId: String?,
    availableTutorSlots: List<TutorAvailabilityUi>,
    onNavigate: (String) -> Unit,
    onOpenDirectWith: (String, String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onStartReschedule: (String) -> Unit,
    onReschedule: (String, String) -> Unit,
    onClearReschedule: () -> Unit,
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
                        isRescheduleTarget = rescheduleTargetBookingId == lesson.id,
                        openSlotsForTutor = availableTutorSlots.filter {
                            it.tutorId == lesson.tutorId && !it.isBooked
                        },
                        onCancelBooking = onCancelBooking,
                        onNavigate = onNavigate,
                        onOpenDirectWith = onOpenDirectWith,
                        onStartReschedule = onStartReschedule,
                        onReschedule = onReschedule,
                        onClearReschedule = onClearReschedule
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
    isRescheduleTarget: Boolean,
    openSlotsForTutor: List<TutorAvailabilityUi>,
    onCancelBooking: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenDirectWith: (String, String) -> Unit,
    onStartReschedule: (String) -> Unit,
    onReschedule: (String, String) -> Unit,
    onClearReschedule: () -> Unit
) {
    val context = LocalContext.current
    val (dayOfWeek, dayNum) = lesson.parseDayParts()
    val statusNorm = lesson.status.uppercase()
    val isConfirmed = statusNorm == "CONFIRMED"
    val hasMeetingUrl = lesson.meetingUrl.isNotBlank()

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
                    Spacer(modifier = Modifier.width(6.dp))
                    // Status badge
                    StatusBadgeForUpcoming(statusNorm = statusNorm, isSoon = isSoon)
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

                // Format / location / topic meta row
                if (lesson.topic.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.topic,
                        style = SoftType.meta,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftButton(
                text = "Reschedule",
                onClick = { onStartReschedule(lesson.id) },
                variant = SoftButtonVariant.Outline,
                size = SoftButtonSize.Sm,
                modifier = Modifier.weight(1f)
            )

            if (isConfirmed && hasMeetingUrl) {
                // Join — opens the meeting URL in external browser/app
                SoftButton(
                    text = "Join",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lesson.meetingUrl))
                        context.startActivity(intent)
                    },
                    variant = SoftButtonVariant.Primary,
                    size = SoftButtonSize.Sm,
                    modifier = Modifier.weight(1f)
                )
            } else if (isConfirmed) {
                // Confirmed but no URL: message the tutor directly
                SoftButton(
                    text = "Message",
                    onClick = { onOpenDirectWith(lesson.tutorId, lesson.tutorDisplayName) },
                    variant = SoftButtonVariant.Primary,
                    size = SoftButtonSize.Sm,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // PENDING: message the tutor directly
                SoftButton(
                    text = "Message",
                    onClick = { onOpenDirectWith(lesson.tutorId, lesson.tutorDisplayName) },
                    variant = SoftButtonVariant.Soft,
                    size = SoftButtonSize.Sm,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Inline reschedule slot picker ────────────────────────────────
        if (isRescheduleTarget) {
            Spacer(modifier = Modifier.height(12.dp))
            SoftDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pick a new slot",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = InkToken
                    )
                )
                SoftButton(
                    text = "Cancel",
                    onClick = onClearReschedule,
                    variant = SoftButtonVariant.Ghost,
                    size = SoftButtonSize.Sm
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (openSlotsForTutor.isEmpty()) {
                Text(
                    text = "No open slots available for this tutor.",
                    style = SoftType.meta,
                    color = Ink3
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    openSlotsForTutor.forEach { slot ->
                        SlotPickerRow(
                            slot = slot,
                            onSelect = { onReschedule(lesson.id, slot.id) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status badge helper for upcoming lessons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusBadgeForUpcoming(statusNorm: String, isSoon: Boolean) {
    when (statusNorm) {
        "PENDING" -> Badge(text = "Pending", tone = BadgeTone.Amber, leadingIcon = null)
        "CONFIRMED" -> if (isSoon) {
            Badge(text = "Soon", tone = BadgeTone.Green, leadingIcon = null)
        } else {
            Badge(text = "Confirmed", tone = BadgeTone.Green, leadingIcon = null)
        }
        else -> if (isSoon) {
            Badge(text = "Soon", tone = BadgeTone.Amber, leadingIcon = null)
        } else Unit
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slot picker row (used in inline reschedule panel)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SlotPickerRow(
    slot: TutorAvailabilityUi,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Primary50)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Primary)
            ) { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${slot.subject}  ·  ${slot.dateLabel}",
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = InkToken
                )
            )
            Text(
                text = slot.timeLabel,
                style = SoftType.meta
            )
        }
        Icon(
            imageVector = SoftIcons.chev,
            contentDescription = "Select slot",
            tint = Primary600,
            modifier = Modifier.size(16.dp)
        )
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
    val statusNorm = lesson.status.uppercase()
    val isCancelledOrDeclined = statusNorm == "CANCELLED" || statusNorm == "DECLINED"
    val isCompleted = statusNorm == "COMPLETED"
    val isNoShow = statusNorm == "NO_SHOW"

    CardQ(modifier = Modifier
        .fillMaxWidth()
        .alpha(if (isCancelledOrDeclined) 0.65f else 1f)
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
                    .background(
                        when {
                            isCancelledOrDeclined -> Bg2
                            isNoShow -> RedBg
                            else -> Primary50
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SoftIcons.cap,
                    contentDescription = null,
                    tint = when {
                        isCancelledOrDeclined -> Ink3
                        isNoShow -> Red
                        else -> Primary600
                    },
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
                        color = if (isCancelledOrDeclined) Ink3 else InkToken
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

            // Status badge / rate affordance
            when {
                statusNorm == "CANCELLED" -> Badge(text = "Cancelled", tone = BadgeTone.Red)
                statusNorm == "DECLINED"  -> Badge(text = "Declined",  tone = BadgeTone.Red)
                isNoShow                  -> Badge(text = "No-show",   tone = BadgeTone.Red)
                statusNorm == "DISPUTED"  -> Badge(text = "Disputed",  tone = BadgeTone.Amber)
                isCompleted && isReviewed -> Badge(text = "Rated",     tone = BadgeTone.Green)
                isCompleted               -> {
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
                else -> Badge(text = "Done", tone = BadgeTone.Green)
            }
        }

        // Cancel/decline reason row if present
        if (isCancelledOrDeclined && lesson.cancelReason.isNotBlank()) {
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
 * Upcoming = PENDING or CONFIRMED and end time is in the future (or unparseable).
 * Everything else (COMPLETED, CANCELLED, DECLINED, NO_SHOW, DISPUTED, or past-dated) is Past.
 */
private fun LessonBookingUi.isUpcoming(now: LocalDateTime): Boolean {
    val s = status.uppercase()
    if (s == "COMPLETED" || s == "CANCELLED" || s == "DECLINED" || s == "NO_SHOW" || s == "DISPUTED") return false
    // PENDING or CONFIRMED: check time
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
