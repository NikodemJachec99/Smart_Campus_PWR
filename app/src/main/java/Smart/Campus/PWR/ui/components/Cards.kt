package Smart.Campus.PWR.ui.components

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorReportUi
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.state.TutorSummaryUi
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.Cloud
import Smart.Campus.PWR.ui.theme.Hairline
import Smart.Campus.PWR.ui.theme.HairlineStrong
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Mist
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.PwrBlue
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrBlueWhisper
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.SoftType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete slot", tint = PwrRed, modifier = Modifier.size(18.dp))
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
fun ReviewCard(review: TutorReviewUi, perspective: UserRole = UserRole.STUDENT) {
    val title = if (perspective == UserRole.TUTOR) review.studentDisplayName else review.tutorDisplayName
    val subtitle = if (perspective == UserRole.TUTOR) {
        "Student review"
    } else {
        "Tutor review"
    }

    PlainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title.ifBlank { review.tutorDisplayName },
                style = MaterialTheme.typography.titleMedium,
                color = InkText,
                fontWeight = FontWeight.SemiBold
            )
            RatingStars(rating = review.rating)
        }
        if (review.reviewType == "lesson" && review.subject.isNotBlank()) {
            Text(
                "$subtitle · ${review.subject} · ${review.lessonDateLabel} · ${review.lessonTimeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = InkTextSoft
            )
        } else {
            Text("General $subtitle", style = MaterialTheme.typography.bodySmall, color = InkTextSoft)
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
    // Severity badge — reflects the report's REAL severity field (LOW/MEDIUM/HIGH)
    val severityBadgeTone = when (report.severity.uppercase()) {
        "HIGH"   -> BadgeTone.Red
        "MEDIUM" -> BadgeTone.Amber
        "LOW"    -> BadgeTone.Gray
        else     -> BadgeTone.Amber
    }
    val severityLabel = report.severity.replaceFirstChar { it.uppercase() }

    val isOpen = report.status == "open"
    val isInReview = report.status == "in-review" || report.status == "in_review"
    val canAct = isOpen || isInReview

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        // Header row: report id + severity badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Report #${report.id.takeLast(4).uppercase()}",
                style = SoftType.meta,
                color = Ink3
            )
            Badge(text = severityLabel, tone = severityBadgeTone)
        }
        Spacer(Modifier.height(6.dp))
        // Reason heading
        Text(report.reason, style = SoftType.h3, color = InkText)
        Text(report.createdAtLabel, style = SoftType.meta, color = Ink3)
        Spacer(Modifier.height(10.dp))
        SoftDivider()
        Spacer(Modifier.height(10.dp))
        // Tutor | Reporter split row
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "TUTOR",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = Ink3
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InitialsAvatar(name = report.tutorDisplayName, size = 26.dp)
                    Text(report.tutorDisplayName, style = SoftType.title, color = InkText)
                }
            }
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Line))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "REPORTER",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = Ink3
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InitialsAvatar(name = report.studentDisplayName.ifBlank { "?" }, size = 26.dp)
                    Text(
                        report.studentDisplayName.ifBlank { "Anonymous" },
                        style = SoftType.title,
                        color = InkText
                    )
                }
            }
        }
        // Details quote block
        if (report.details.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "“${report.details}”",
                    style = SoftType.bodySm,
                    color = Ink2
                )
            }
        }
        // Moderator note (if present)
        if (report.moderatorNote.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Note: ${report.moderatorNote}",
                    style = SoftType.bodySm,
                    color = Amber
                )
            }
        }
        // Action buttons
        onStatusChange?.takeIf { canAct }?.let { changeStatus ->
            Spacer(Modifier.height(10.dp))
            SoftDivider()
            Row(modifier = Modifier.fillMaxWidth()) {
                // Dismiss ghost button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Ink3)
                        ) { changeStatus(report.id, "dismissed") }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Dismiss",
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink3
                        )
                    )
                }
                if (isOpen) {
                    // Open: show Investigate
                    Box(modifier = Modifier.width(1.dp).height(46.dp).background(Line))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Primary600)
                            ) { changeStatus(report.id, "in_review") }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Investigate",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Primary600
                            )
                        )
                    }
                } else if (isInReview) {
                    // In-review: show Resolve
                    Box(modifier = Modifier.width(1.dp).height(46.dp).background(Line))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Primary600)
                            ) { changeStatus(report.id, "resolved") }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Resolve",
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Primary600
                            )
                        )
                    }
                }
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
    onDeleteUser: ((String) -> Unit)? = null,
    onSetVerified: ((Boolean) -> Unit)? = null
) {
    var student by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.STUDENT)) }
    var tutor by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.TUTOR)) }
    var showDeleteDialog by remember(user.uid) { mutableStateOf(false) }
    var expanded by remember(user.uid) { mutableStateOf(false) }

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

    // Compact user row — matches ScreenAdminUsers design
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = InkText)
                ) { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(name = user.displayName, size = 42.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, style = SoftType.title, color = InkText)
                Text("@${user.login}", style = SoftType.meta, color = Ink3)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                user.roles.forEach { role ->
                    val tone = when (role) {
                        UserRole.ADMIN   -> BadgeTone.Red
                        UserRole.TUTOR   -> BadgeTone.Prim
                        UserRole.STUDENT -> BadgeTone.Green
                    }
                    Badge(text = role.displayName, tone = tone)
                }
            }
            // More / expand indicator
            Icon(
                imageVector = SoftIcons.more,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Ink3,
                modifier = Modifier.size(18.dp)
            )
        }

        // Expanded controls
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Bg)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${user.email}", style = SoftType.meta, color = Ink3)
                if (!user.hasRole(UserRole.ADMIN)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            student,
                            { student = it },
                            colors = CheckboxDefaults.colors(checkedColor = PwrNavy, uncheckedColor = HairlineStrong, checkmarkColor = Cloud)
                        )
                        Text("Student", color = InkText, style = SoftType.body)
                        Spacer(Modifier.width(8.dp))
                        Checkbox(
                            tutor,
                            { tutor = it },
                            colors = CheckboxDefaults.colors(checkedColor = PwrNavy, uncheckedColor = HairlineStrong, checkmarkColor = Cloud)
                        )
                        Text("Tutor", color = InkText, style = SoftType.body)
                        Spacer(Modifier.weight(1f))
                        SoftButton(
                            text = "Save",
                            onClick = { onUpdateUserRoles(user.uid, student, tutor) },
                            variant = SoftButtonVariant.Soft,
                            size = SoftButtonSize.Sm
                        )
                    }
                }
                // Verify toggle (tutors only, non-admin)
                if (onSetVerified != null && user.hasRole(UserRole.TUTOR) && !user.hasRole(UserRole.ADMIN)) {
                    SoftButton(
                        text = if (user.verified) "Unverify" else "Verify",
                        onClick = { onSetVerified(!user.verified) },
                        variant = if (user.verified) SoftButtonVariant.Soft else SoftButtonVariant.Primary,
                        size = SoftButtonSize.Sm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftButton(
                        text = if (inspector == null) "Inspect" else "Hide",
                        onClick = { onToggleInspector(user.uid) },
                        variant = SoftButtonVariant.Outline,
                        size = SoftButtonSize.Sm,
                        modifier = Modifier.weight(1f)
                    )
                    if (onDeleteUser != null && !user.hasRole(UserRole.ADMIN)) {
                        SoftButton(
                            text = "Delete",
                            onClick = { showDeleteDialog = true },
                            variant = SoftButtonVariant.Danger,
                            size = SoftButtonSize.Sm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (inspector != null) {
                    if (inspector.isLoading) {
                        Text("Loading…", color = InkTextSoft, style = SoftType.meta)
                    } else {
                        InspectorSection("Availability", inspector.availability.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                        InspectorSection("Bookings as tutor", inspector.bookingsAsTutor.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                        InspectorSection("Bookings as student", inspector.bookingsAsStudent.map { "· ${it.dateLabel} ${it.timeLabel} — ${it.subject}" })
                        InspectorSection("Reviews received", inspector.reviewsReceived.map { "· ${it.rating}/5 ${it.studentDisplayName} — ${it.comment}" })
                        InspectorSection("Reports received", inspector.reportsReceived.map { "· ${it.reason} — ${it.status}" })
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun InspectorSection(title: String, items: List<String>) {
    Text(
        title.uppercase(),
        color = PwrNavy,
        style = SoftType.eyebrow
    )
    if (items.isEmpty()) Text("None.", color = Ink3, style = SoftType.bodySm)
    items.forEach { Text(it, style = SoftType.bodySm, color = InkText) }
}

@Suppress("UNUSED")
private val UnusedPwrBlue = PwrBlue
