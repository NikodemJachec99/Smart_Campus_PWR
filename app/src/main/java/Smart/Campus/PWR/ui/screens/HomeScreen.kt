package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.ActivityList
import Smart.Campus.PWR.ui.components.ActivityRow
import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.EditorialTopBar
import Smart.Campus.PWR.ui.components.HighlightCard
import Smart.Campus.PWR.ui.components.IconCircleButton
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.PlainCard
import Smart.Campus.PWR.ui.components.RoleSegment
import Smart.Campus.PWR.ui.components.StatPill
import Smart.Campus.PWR.ui.state.AssignmentUi
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.Hairline
import Smart.Campus.PWR.ui.theme.HairlineStrong
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueWhisper
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onRefresh: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onCancelBooking: (String, String, String) -> Unit,
    onNavigate: (String) -> Unit,
    onToggleRole: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val displayName = state.currentUser?.displayName.orEmpty()
    val greeting = greetingForHour(LocalTime.now().hour)
    val hasDualRole = state.currentUser?.hasDualRole() == true
    val lessons = if (activeRole == UserRole.STUDENT) state.dashboardState.myStudentBookings
    else state.dashboardState.myTutorBookings

    val streak = computeStreak(lessons)
    val hours = computeHours(lessons)
    val nextLesson = pickNextLesson(lessons)
    val nextLessonMinutes = nextLesson?.let { minutesUntil(it) }
    val startingSoon = nextLessonMinutes != null && nextLessonMinutes in 0..60
    val urgentItems = computeUrgentItems(state, activeRole)

    MainList {
        EditorialTopBar(
            brand = "KORE TUTORING",
            left = {
                if (hasDualRole) {
                    RoleSegment(
                        activeRole = activeRole,
                        canSwitch = true,
                        onSwitch = { onClearMessages(); onToggleRole() }
                    )
                } else {
                    MonoLabel(if (activeRole == UserRole.TUTOR) "Tutor mode" else "Student mode", color = PwrNavy)
                }
            },
            right = {
                IconCircleButton(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    onClick = { onNavigate(DashboardRoutes.CALENDAR) }
                )
                IconCircleButton(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    dot = state.dashboardState.notificationCount > 0 || urgentItems.isNotEmpty(),
                    onClick = onOpenNotifications
                )
            }
        )

        // 1. Greeting hero
        Text(
            greeting,
            color = InkText,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            letterSpacing = (-1.2).sp,
            style = MaterialTheme.typography.displaySmall
        )
        if (displayName.isNotBlank()) {
            Text(
                displayName,
                color = InkTextSoft,
                fontSize = 13.5.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        MessageBlock(state.errorMessage, state.infoMessage)

        // 3. Urgent banner — visible only when there's something pending
        if (urgentItems.isNotEmpty()) {
            UrgentBanner(items = urgentItems, onTap = onNavigate)
        }

        Spacer(Modifier.size(2.dp))

        // 4. Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatPill(
                eyebrow = "Today",
                leadingIcon = Icons.Rounded.Bolt,
                metric = streak.toString(),
                label = "Day Streak",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                eyebrow = "Studied",
                leadingIcon = Icons.Rounded.AccessTime,
                metric = formatHours(hours),
                label = "Hours Total",
                modifier = Modifier.weight(1f)
            )
        }

        // 5. HighlightCard — next upcoming lesson
        if (nextLesson != null) {
            val counterpartLabel = if (activeRole == UserRole.STUDENT) {
                "with ${nextLesson.tutorDisplayName}"
            } else {
                "with ${nextLesson.studentDisplayName}"
            }
            val (progress, relLabel) = nextLessonProgress(nextLesson)
            HighlightCard(
                eyebrow = if (startingSoon) "Starting soon · $relLabel" else "Next lesson",
                title = nextLesson.subject,
                subtitle = "${nextLesson.dateLabel} · ${nextLesson.timeLabel} · $counterpartLabel",
                progress = progress,
                progressLabel = if (startingSoon) null else relLabel,
                urgent = startingSoon,
                onClick = { onNavigate(DashboardRoutes.CALENDAR) }
            )
        } else {
            PlainCard {
                Text(
                    "No upcoming lessons",
                    color = InkText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (activeRole == UserRole.STUDENT) "Browse open tutor slots on the Calendar tab."
                    else "Add availability slots so students can book you.",
                    color = InkTextSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 6. Quick actions row — role-aware
        QuickActionsRow(activeRole = activeRole, onNavigate = onNavigate)

        // 7. Recent activity title
        Spacer(Modifier.size(4.dp))
        Text(
            "Recent activity".uppercase(),
            color = PwrNavy,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )

        // 8. Activity list
        val rows = buildActivityRows(state, activeRole)
        if (rows.isEmpty()) {
            PlainCard {
                Text(
                    "No recent activity yet.",
                    color = InkTextSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            ActivityList {
                rows.forEachIndexed { index, row ->
                    ActivityRow(
                        icon = row.icon,
                        title = row.title,
                        subtitle = row.subtitle,
                        trailingMetric = row.trailingMetric,
                        showDivider = index != rows.lastIndex
                    )
                }
            }
        }

        Spacer(Modifier.size(4.dp))
        AppDangerButton(
            text = "Refresh dashboard",
            onClick = { onClearMessages(); onRefresh() }
        )
    }
}

// ---- Sub-components ---------------------------------------------------------

@Composable
private fun DualRoleBadge(activeRole: UserRole, onSwitch: () -> Unit) {
    val otherRole = if (activeRole == UserRole.STUDENT) "tutor" else "student"
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PwrBlueWhisper)
            .border(0.5.dp, Hairline, RoundedCornerShape(50))
            .clickable { onSwitch() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Active as ${activeRole.displayName.uppercase()}",
            color = PwrNavy,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text("·", color = PwrNavy.copy(alpha = 0.4f))
        Icon(
            Icons.Rounded.SwapHoriz,
            contentDescription = null,
            tint = PwrNavy,
            modifier = Modifier.size(13.dp)
        )
        Text(
            "switch to $otherRole",
            color = PwrNavy.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun UrgentBanner(items: List<String>, onTap: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(PwrRed.copy(alpha = 0.10f), PwrRed.copy(alpha = 0.04f))
                )
            )
            .border(0.7.dp, PwrRed.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
            .clickable { onTap(DashboardRoutes.ASSIGNMENTS) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PwrRed.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PriorityHigh,
                    contentDescription = null,
                    tint = PwrRed,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "Needs attention".uppercase(),
                    color = PwrRed,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                items.forEach { line ->
                    Text(
                        line,
                        color = InkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(activeRole: UserRole, onNavigate: (String) -> Unit) {
    val actions = if (activeRole == UserRole.STUDENT) {
        listOf(
            QuickAction("Courses", Icons.Rounded.School, DashboardRoutes.COURSES),
            QuickAction("Find tutor", Icons.Rounded.Search, DashboardRoutes.CALENDAR),
            QuickAction("My tasks", Icons.AutoMirrored.Rounded.MenuBook, DashboardRoutes.ASSIGNMENTS),
            QuickAction("Reviews", Icons.Rounded.Star, DashboardRoutes.REVIEWS)
        )
    } else {
        listOf(
            QuickAction("Courses", Icons.Rounded.School, DashboardRoutes.COURSES),
            QuickAction("Add slot", Icons.Rounded.Add, DashboardRoutes.CALENDAR),
            QuickAction("Publish task", Icons.Rounded.Upload, DashboardRoutes.ASSIGNMENTS),
            QuickAction("Reviews", Icons.Rounded.Star, DashboardRoutes.REVIEWS)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.forEach { action ->
            QuickActionChip(
                label = action.label,
                icon = action.icon,
                onClick = { onNavigate(action.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Cloud)
            .border(0.7.dp, Hairline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PwrBlueWhisper)
                    .border(0.5.dp, Hairline, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PwrNavy,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                label,
                color = InkText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private data class QuickAction(val label: String, val icon: ImageVector, val route: String)

private data class ActivityRowData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val trailingMetric: String? = null
)

// ---- Logic helpers ----------------------------------------------------------

private fun computeUrgentItems(state: SmartCampusUiState, activeRole: UserRole): List<String> {
    val items = mutableListOf<String>()
    val currentUid = state.currentUser?.uid ?: return emptyList()

    if (activeRole == UserRole.STUDENT) {
        // Student view: assignments where I haven't submitted yet.
        val mySubmissions = state.dashboardState.mySubmissions
        val openAssignments = state.dashboardState.assignments.filter { assignment ->
            mySubmissions.none { it.assignmentId == assignment.id }
        }
        if (openAssignments.isNotEmpty()) {
            val count = openAssignments.size
            items += "$count assignment${if (count == 1) "" else "s"} waiting for your submission"
        }
    } else {
        // Tutor view: assignments I've published.
        val myAssignments = state.dashboardState.assignments.filter { it.tutorUid == currentUid }
        if (myAssignments.isNotEmpty()) {
            val count = myAssignments.size
            items += "$count published assignment${if (count == 1) "" else "s"} — check submissions"
        }
    }

    return items
}

private fun buildActivityRows(state: SmartCampusUiState, activeRole: UserRole): List<ActivityRowData> {
    val rows = mutableListOf<ActivityRowData>()

    val reviews = if (activeRole == UserRole.STUDENT) state.dashboardState.reviewsByMe
    else state.dashboardState.reviewsForMe
    reviews.firstOrNull()?.let { review ->
        rows += ActivityRowData(
            icon = Icons.Rounded.Star,
            title = if (activeRole == UserRole.STUDENT) "Review for ${review.tutorDisplayName}"
            else "Review from ${review.studentDisplayName}",
            subtitle = review.createdAtLabel.ifBlank { "Recently" },
            trailingMetric = "${review.rating}/5"
        )
    }

    val cancelled = (state.dashboardState.myStudentBookings + state.dashboardState.myTutorBookings)
        .firstOrNull { it.status == "cancelled" }
    cancelled?.let { lesson ->
        rows += ActivityRowData(
            icon = Icons.Rounded.Cancel,
            title = "Cancelled · ${lesson.subject}",
            subtitle = "${lesson.dateLabel} · ${lesson.timeLabel}"
        )
    }

    val booked = (state.dashboardState.myStudentBookings + state.dashboardState.myTutorBookings)
        .firstOrNull { it.status == "booked" }
    booked?.let { lesson ->
        rows += ActivityRowData(
            icon = Icons.Rounded.CalendarToday,
            title = "Booked · ${lesson.subject}",
            subtitle = "${lesson.dateLabel} · ${lesson.timeLabel}"
        )
    }

    val assignment = state.dashboardState.assignments.firstOrNull()
    assignment?.let { a -> addAssignmentActivityRow(state, a, rows) }

    return rows.take(4)
}

private fun addAssignmentActivityRow(state: SmartCampusUiState, a: AssignmentUi, rows: MutableList<ActivityRowData>) {
    rows += ActivityRowData(
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        title = if (a.tutorUid == state.currentUser?.uid) "Published · ${a.title}"
        else "Assignment · ${a.title}",
        subtitle = if (a.dueDateLabel.isNotBlank()) "Due ${a.dueDateLabel}" else "Recently"
    )
}

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..20 -> "Good evening"
    else -> "Good night"
}

private fun computeStreak(lessons: List<LessonBookingUi>): Int {
    val today = LocalDate.now()
    val bookedDates: Set<LocalDate> = lessons
        .filter { it.status == "booked" }
        .mapNotNull { runCatching { LocalDate.parse(it.dateLabel) }.getOrNull() }
        .filter { !it.isAfter(today) }
        .toSet()
    if (bookedDates.isEmpty()) return 0

    var streak = 0
    var cursor = today
    while (cursor in bookedDates) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun computeHours(lessons: List<LessonBookingUi>): Double {
    return lessons.filter { it.status == "booked" }.sumOf { lesson ->
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

private fun pickNextLesson(lessons: List<LessonBookingUi>): LessonBookingUi? {
    val now = LocalDateTime.now()
    return lessons
        .filter { it.status == "booked" }
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

private fun nextLessonProgress(lesson: LessonBookingUi): Pair<Float, String> {
    val now = LocalDateTime.now()
    val dt = parseLessonDateTime(lesson) ?: return 0f to ""
    val totalMinutes = java.time.Duration.between(now, dt).toMinutes()
    val totalHours = totalMinutes / 60.0

    val progress = if (totalHours <= 0) 1f
    else (1.0 - (totalHours / 24.0)).coerceIn(0.0, 1.0).toFloat()

    val label = when {
        totalMinutes <= 0 -> "starting"
        totalMinutes < 60 -> "in ${totalMinutes}m"
        totalMinutes < 24 * 60 -> {
            val h = totalMinutes / 60
            val m = totalMinutes - h * 60
            if (m == 0L) "in ${h}h" else "in ${h}h ${m}m"
        }
        else -> {
            val days = totalMinutes / (24 * 60)
            "in ${days}d"
        }
    }
    return progress to label
}

@Suppress("UNUSED")
private val UnusedHairlineStrong = HairlineStrong
@Suppress("UNUSED")
private val UnusedPwrBlue = PwrBlue
@Suppress("UNUSED")
private val UnusedColor = Color.Transparent
@Suppress("UNUSED")
private val UnusedWidth = 1.dp
