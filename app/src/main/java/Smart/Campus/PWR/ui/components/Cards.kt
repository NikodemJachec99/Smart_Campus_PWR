package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorReportUi
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.state.TutorSummaryUi
import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.Hairline
import Smart.Campus.PWR.ui.theme.HairlineStrong
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.Mist
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrBlueWhisper
import Smart.Campus.PWR.ui.theme.PwrNavy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun StatusChip(label: String, bg: Color, fg: Color) {
    @Suppress("UNUSED_PARAMETER") val ignoredBg = bg
    @Suppress("UNUSED_PARAMETER") val ignoredFg = fg
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PwrBlueWhisper)
            .border(0.6.dp, Hairline, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            label.uppercase(),
            color = PwrNavy,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
    }
}

fun statusChipColors(status: String): Pair<Color, Color> {
    @Suppress("UNUSED_PARAMETER") val ignoredStatus = status
    return PwrBlueWhisper to PwrNavy
}

@Composable
fun RatingStars(rating: Int, max: Int = 5, size: Int = 14) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(max) { index ->
            if (index < rating) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = PwrNavy,
                    modifier = Modifier.size(size.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.StarBorder,
                    contentDescription = null,
                    tint = InkTextSoft.copy(alpha = 0.4f),
                    modifier = Modifier.size(size.dp)
                )
            }
        }
    }
}

@Composable
fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color = InkTextSoft) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(text, color = InkText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AvailabilityCard(
    slot: TutorAvailabilityUi,
    onBook: ((String) -> Unit)?,
    onDelete: ((String) -> Unit)? = null,
    onEdit: ((TutorAvailabilityUi) -> Unit)? = null
) {
    var showDeleteDialog by remember(slot.id) { mutableStateOf(false) }

    if (showDeleteDialog && onDelete != null) {
        ConfirmDangerDialog(
            title = "Delete slot",
            body = "This removes the open availability slot. Booked lessons must be cancelled from the lesson card first.",
            confirmText = "Delete slot",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete(slot.id)
            }
        )
    }

    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    slot.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkText,
                    fontWeight = FontWeight.SemiBold
                )
                IconText(Icons.Rounded.Person, slot.tutorDisplayName)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconText(Icons.Rounded.CalendarToday, slot.dateLabel)
                    IconText(Icons.Rounded.AccessTime, slot.timeLabel)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (slot.isBooked) {
                    StatusChip("BOOKED", Color.Transparent, PwrNavy)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onEdit != null && !slot.isBooked) {
                        IconButton(onClick = { onEdit(slot) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit slot", tint = PwrNavy, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (onDelete != null && !slot.isBooked) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete slot", tint = Smart.Campus.PWR.ui.theme.PwrRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (onBook != null && !slot.isBooked) {
                    AppPrimaryButton(
                        text = "Book",
                        onClick = { onBook(slot.id) },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: LessonBookingUi,
    onCancel: ((String, String, String) -> Unit)? = null
) {
    var showCancelDialog by remember(lesson.id) { mutableStateOf(false) }

    if (showCancelDialog && onCancel != null) {
        CancelLessonDialog(
            lesson = lesson,
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason ->
                showCancelDialog = false
                onCancel(lesson.id, lesson.availabilityId, reason)
            }
        )
    }

    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        lesson.subject,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkText,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusChip(lesson.status, Color.Transparent, PwrNavy)
                }
                IconText(Icons.Rounded.Person, "Tutor · ${lesson.tutorDisplayName}")
                IconText(Icons.Rounded.Person, "Student · ${lesson.studentDisplayName}")
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconText(Icons.Rounded.CalendarToday, lesson.dateLabel)
                    IconText(Icons.Rounded.AccessTime, lesson.timeLabel)
                }
                if (lesson.status == "cancelled" && lesson.cancelReason.isNotBlank()) {
                    Text(
                        "Cancel reason: ${lesson.cancelReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTextSoft
                    )
                }
            }
            if (onCancel != null && lesson.status == "booked") {
                AppDangerButton(text = "Cancel", onClick = { showCancelDialog = true })
            }
        }
    }
}

@Composable
private fun CancelLessonDialog(
    lesson: LessonBookingUi,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember(lesson.id) { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Cloud,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonoLabel("Cancel lesson")
                Text(
                    lesson.subject,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${lesson.dateLabel} · ${lesson.timeLabel}",
                    color = InkTextSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it.take(500) },
                    label = { Text("Reason") },
                    placeholder = { Text("Why is this lesson being cancelled?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Back", color = InkTextSoft)
                    }
                    Spacer(Modifier.size(8.dp))
                    AppDangerButton(
                        text = "Confirm cancel",
                        enabled = reason.isNotBlank(),
                        onClick = { onConfirm(reason.trim()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmDangerDialog(
    title: String,
    body: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Cloud,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonoLabel("Confirm")
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(body, color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Back", color = InkTextSoft)
                    }
                    Spacer(Modifier.size(8.dp))
                    AppDangerButton(text = confirmText, onClick = onConfirm)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: TutorReviewUi) {
    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                review.tutorDisplayName,
                style = MaterialTheme.typography.titleMedium,
                color = InkText,
                fontWeight = FontWeight.SemiBold
            )
            RatingStars(rating = review.rating)
        }
        if (review.reviewType == "lesson" && review.subject.isNotBlank()) {
            Text(
                "${review.subject} · ${review.lessonDateLabel} · ${review.lessonTimeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = InkTextSoft
            )
        } else {
            Text("General tutor review", style = MaterialTheme.typography.bodySmall, color = InkTextSoft)
        }
        Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = InkText)
        Text(review.createdAtLabel, color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ReportCard(
    report: TutorReportUi,
    onStatusChange: ((String, String) -> Unit)? = null
) {
    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                report.reason,
                style = MaterialTheme.typography.titleMedium,
                color = InkText,
                fontWeight = FontWeight.SemiBold
            )
            StatusChip(report.status, Color.Transparent, PwrNavy)
        }
        Text("Tutor · ${report.tutorDisplayName}", color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
        if (report.details.isNotBlank()) {
            Text(report.details, style = MaterialTheme.typography.bodyMedium, color = InkText)
        }
        Text(report.createdAtLabel, color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
        if (onStatusChange != null && report.status == "open") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppPrimaryButton(
                    text = "Resolve",
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange(report.id, "resolved") }
                )
                AppDangerButton(
                    text = "Dismiss",
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange(report.id, "dismissed") }
                )
            }
        }
    }
}

@Composable
fun TutorPicker(tutors: List<TutorSummaryUi>, selected: String, onPick: (String) -> Unit) {
    if (tutors.isEmpty()) {
        Text("No tutors found.", color = InkTextSoft)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tutors.take(4).forEach { tutor ->
            val isSelected = selected == tutor.uid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PwrBlueSoft else Mist)
                    .border(
                        width = 0.6.dp,
                        color = if (isSelected) HairlineStrong else Hairline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPick(tutor.uid) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        tint = if (isSelected) PwrNavy else InkTextSoft,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        tutor.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) PwrNavy else InkText
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: AppUser,
    inspector: AdminUserInspectorUi?,
    onUpdateUserRoles: (String, Boolean, Boolean) -> Unit,
    onToggleInspector: (String) -> Unit,
    onDeleteUser: ((String) -> Unit)? = null
) {
    var student by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.STUDENT)) }
    var tutor by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.TUTOR)) }
    var showDeleteDialog by remember(user.uid) { mutableStateOf(false) }

    if (showDeleteDialog && onDeleteUser != null) {
        ConfirmDangerDialog(
            title = "Delete user",
            body = "This deletes the Firebase Auth account and Firestore profile. Existing bookings and reviews keep their historical labels.",
            confirmText = "Delete user",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteUser(user.uid)
            }
        )
    }

    SectionCard(user.displayName) {
        Text("${user.login} · ${user.email}", color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
        Text("Roles · ${user.roles.joinToString { it.displayName }}", color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
        if (!user.hasRole(UserRole.ADMIN)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    student,
                    { student = it },
                    colors = CheckboxDefaults.colors(checkedColor = PwrNavy, uncheckedColor = HairlineStrong, checkmarkColor = Cloud)
                )
                Text("Student", color = InkText)
                Spacer(Modifier.size(8.dp))
                Checkbox(
                    tutor,
                    { tutor = it },
                    colors = CheckboxDefaults.colors(checkedColor = PwrNavy, uncheckedColor = HairlineStrong, checkmarkColor = Cloud)
                )
                Text("Tutor", color = InkText)
                Spacer(Modifier.size(8.dp))
                AppPrimaryButton("Save", onClick = { onUpdateUserRoles(user.uid, student, tutor) })
            }
        }
        AppPrimaryButton(
            if (inspector == null) "Inspect user" else "Hide details",
            onClick = { onToggleInspector(user.uid) }
        )
        if (onDeleteUser != null && !user.hasRole(UserRole.ADMIN)) {
            AppDangerButton("Delete user", onClick = { showDeleteDialog = true })
        }
        if (inspector != null) {
            if (inspector.isLoading) {
                Text("Loading…", color = InkTextSoft)
            } else {
                InspectorSection("Availability", inspector.availability.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                InspectorSection("Bookings as tutor", inspector.bookingsAsTutor.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                InspectorSection("Bookings as student", inspector.bookingsAsStudent.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                InspectorSection("Reviews received", inspector.reviewsReceived.map { "· ${it.rating}/5 ${it.studentDisplayName} — ${it.comment}" })
                InspectorSection("Reports received", inspector.reportsReceived.map { "· ${it.reason} — ${it.status}" })
            }
        }
    }
}

@Composable
private fun InspectorSection(title: String, items: List<String>) {
    Text(
        title.uppercase(),
        color = PwrNavy,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )
    if (items.isEmpty()) Text("None.", color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
    items.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = InkText) }
}

@Suppress("UNUSED")
private val UnusedPwrBlue = PwrBlue
