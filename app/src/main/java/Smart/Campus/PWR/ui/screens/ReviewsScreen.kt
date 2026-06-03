package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.EditorialCard
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.ReviewCard
import Smart.Campus.PWR.ui.components.ReportCard
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.components.TutorPicker
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.PwrGold
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewsTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onReviewTutorChanged: (String) -> Unit,
    onReviewBookingChanged: (String) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onReportTutorChanged: (String) -> Unit,
    onReportReasonChanged: (String) -> Unit,
    onReportDetailsChanged: (String) -> Unit,
    onSubmitReport: () -> Unit
) {
    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)
        MonoLabel("Reviews")
        Text(
            if (activeRole == UserRole.STUDENT) "Rate your tutors." else "Your reviews.",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )

        if (activeRole == UserRole.STUDENT) {
            StudentReviewComposer(
                state = state,
                onReviewTutorChanged = onReviewTutorChanged,
                onReviewBookingChanged = onReviewBookingChanged,
                onReviewRatingChanged = onReviewRatingChanged,
                onReviewCommentChanged = onReviewCommentChanged,
                onClearMessages = onClearMessages,
                onSubmitReview = onSubmitReview
            )

            SectionHeader("Reviews you wrote")
            if (state.dashboardState.reviewsByMe.isEmpty()) {
                EmptyHint("Nothing yet — pick a tutor or a finished lesson above to leave your first review.")
            }
            state.dashboardState.reviewsByMe.forEach { ReviewCard(it) }

            SectionCard("Report a tutor", accent = PwrRed) {
                Text(
                    "Use this only for serious issues. Reports go to the admin team.",
                    color = InkTextSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
                TutorPicker(state.dashboardState.tutors, state.reportForm.tutorUid, onReportTutorChanged)
                OutlinedTextField(
                    state.reportForm.reason,
                    onReportReasonChanged,
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    state.reportForm.details,
                    onReportDetailsChanged,
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                AppPrimaryButton(
                    text = "Send report",
                    enabled = state.reportForm.tutorUid.isNotBlank() && state.reportForm.reason.isNotBlank(),
                    onClick = { onClearMessages(); onSubmitReport() }
                )
            }

            if (state.dashboardState.reportsByMe.isNotEmpty()) {
                SectionHeader("Reports you sent")
                state.dashboardState.reportsByMe.forEach { ReportCard(it) }
            }
        } else {
            SectionHeader("Reviews about you")
            if (state.dashboardState.reviewsForMe.isEmpty()) {
                EmptyHint("No reviews yet. When a student rates a lesson or your tutoring, it shows up here.")
            }
            state.dashboardState.reviewsForMe.forEach { ReviewCard(it) }
        }
    }
}

@Composable
private fun StudentReviewComposer(
    state: SmartCampusUiState,
    onReviewTutorChanged: (String) -> Unit,
    onReviewBookingChanged: (String) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onClearMessages: () -> Unit,
    onSubmitReview: () -> Unit
) {
    val reviewedBookingIds = state.dashboardState.reviewsByMe
        .mapNotNull { it.bookingId.ifBlank { null } }
        .toSet()
    val lessons = state.dashboardState.myStudentBookings
        .filter { it.id !in reviewedBookingIds && it.canBeReviewedAfterLesson() }

    val isLessonMode = state.reviewForm.bookingId.isNotBlank()
    val isTutorMode = state.reviewForm.tutorUid.isNotBlank()
    val canSubmit = isLessonMode || isTutorMode

    SectionCard("Write a review", accent = PwrGold) {
        MonoLabel("Step 1 · What are you reviewing?")

        if (lessons.isNotEmpty()) {
            Text("Finished lessons", color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
            lessons.forEach { lesson ->
                LessonChoiceRow(
                    lesson = lesson,
                    selected = state.reviewForm.bookingId == lesson.id,
                    onSelect = {
                        onReviewTutorChanged("")
                        onReviewBookingChanged(lesson.id)
                    }
                )
            }
            Text("Or a tutor in general", color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Pick a tutor to review", color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
        }

        TutorPicker(
            tutors = state.dashboardState.tutors,
            selected = state.reviewForm.tutorUid,
            onPick = {
                onReviewBookingChanged("")
                onReviewTutorChanged(it)
            }
        )

        if (canSubmit) {
            MonoLabel("Step 2 · Your rating")
            StarRatingInput(
                rating = state.reviewForm.rating.toIntOrNull()?.coerceIn(1, 5) ?: 5,
                onRatingChanged = { onReviewRatingChanged(it.toString()) }
            )
            OutlinedTextField(
                state.reviewForm.comment,
                onReviewCommentChanged,
                label = { Text("Comment") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            AppPrimaryButton(
                text = if (isLessonMode) "Submit lesson review" else "Submit tutor review",
                enabled = state.reviewForm.comment.isNotBlank(),
                onClick = { onClearMessages(); onSubmitReview() }
            )
        } else {
            Text(
                "Select a lesson or a tutor above to rate them.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StarRatingInput(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = "$index stars",
                tint = if (index <= rating) ClayAccent else InkTextSoft.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onRatingChanged(index) }
                    )
            )
        }
        Text(
            "$rating/5",
            color = InkTextSoft,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun LessonChoiceRow(
    lesson: LessonBookingUi,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PwrNavy.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
            .border(1.dp, if (selected) PwrNavy else PaperLine, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(lesson.subject, color = InkText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("${lesson.tutorDisplayName} · ${lesson.dateLabel} · ${lesson.timeLabel}", color = InkTextSoft, style = MaterialTheme.typography.bodySmall)
        }
        if (selected) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = ClayAccent, modifier = Modifier.size(18.dp))
        } else {
            MonoLabel("Select", color = PwrNavy, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyHint(text: String) {
    EditorialCard {
        Text(text, color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun LessonBookingUi.canBeReviewedAfterLesson(): Boolean {
    if (status == "cancelled") return false
    if (status == "completed") return true
    if (status != "booked") return false
    val end = parseLessonEnd() ?: return false
    return !end.isAfter(LocalDateTime.now())
}

private fun LessonBookingUi.parseLessonEnd(): LocalDateTime? {
    val date = runCatching { LocalDate.parse(dateLabel) }.getOrNull() ?: return null
    val time = runCatching {
        LocalTime.parse(endHour, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    return LocalDateTime.of(date, time)
}
