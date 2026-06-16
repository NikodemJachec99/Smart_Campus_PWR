package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.tutoring.TutorRatings
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.TutorPicker
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardFlat
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.Chip
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.ProgressBar
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.StarSize
import Smart.Campus.PWR.ui.components.softindigo.StarsRow
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.Star
import Smart.Campus.PWR.ui.theme.White
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Public entry point (signature preserved exactly) ─────────────────────────

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
    onSubmitReport: () -> Unit,
    onToggleReviewTag: (String) -> Unit = {},
    onSetReviewAnonymous: (Boolean) -> Unit = {},
    onReportSeverityChanged: (String) -> Unit = {}
) {
    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)

        SoftTopBar(
            title = if (activeRole == UserRole.STUDENT) "Rate a lesson" else "Reviews about you"
        )

        if (activeRole == UserRole.STUDENT) {
            StudentReviewsContent(
                state = state,
                onClearMessages = onClearMessages,
                onReviewTutorChanged = onReviewTutorChanged,
                onReviewBookingChanged = onReviewBookingChanged,
                onReviewRatingChanged = onReviewRatingChanged,
                onReviewCommentChanged = onReviewCommentChanged,
                onSubmitReview = onSubmitReview,
                onToggleReviewTag = onToggleReviewTag,
                onSetReviewAnonymous = onSetReviewAnonymous,
                onReportTutorChanged = onReportTutorChanged,
                onReportReasonChanged = onReportReasonChanged,
                onReportDetailsChanged = onReportDetailsChanged,
                onReportSeverityChanged = onReportSeverityChanged,
                onSubmitReport = onSubmitReport
            )
        } else {
            TutorReviewsContent(state = state)
        }
    }
}

// ─── STUDENT branch ───────────────────────────────────────────────────────────

@Composable
private fun StudentReviewsContent(
    state: SmartCampusUiState,
    onClearMessages: () -> Unit,
    onReviewTutorChanged: (String) -> Unit,
    onReviewBookingChanged: (String) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onToggleReviewTag: (String) -> Unit,
    onSetReviewAnonymous: (Boolean) -> Unit,
    onReportTutorChanged: (String) -> Unit,
    onReportReasonChanged: (String) -> Unit,
    onReportDetailsChanged: (String) -> Unit,
    onReportSeverityChanged: (String) -> Unit,
    onSubmitReport: () -> Unit
) {
    val lessons = reviewableLessons(state)
    val selectedLesson = lessons.firstOrNull { it.id == state.reviewForm.bookingId }
    val selectedTutor = state.dashboardState.tutors.firstOrNull { it.uid == state.reviewForm.tutorUid }
    val selectedRating = state.reviewForm.rating.toIntOrNull()?.coerceIn(1, 5) ?: 5
    val isLessonMode = state.reviewForm.bookingId.isNotBlank()
    val isTutorMode = state.reviewForm.tutorUid.isNotBlank()
    val canSubmit = isLessonMode || isTutorMode

    // ── Step 1 – pick a lesson ────────────────────────────────────────────────
    SoftCard {
        Text(
            text = "STEP 1 — WHAT ARE YOU REVIEWING?",
            style = SoftType.eyebrow,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (lessons.isNotEmpty()) {
            Text(
                text = "Finished lessons",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lessons.forEach { lesson ->
                    LessonPickRow(
                        lesson = lesson,
                        selected = state.reviewForm.bookingId == lesson.id,
                        onSelect = {
                            onReviewTutorChanged("")
                            onReviewBookingChanged(lesson.id)
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Or rate a tutor in general",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        } else {
            Text(
                text = "Pick a tutor to review",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        TutorPicker(
            tutors = state.dashboardState.tutors,
            selected = state.reviewForm.tutorUid,
            onPick = {
                onReviewBookingChanged("")
                onReviewTutorChanged(it)
            }
        )
    }

    // ── Step 2 – rate & comment (only when something is selected) ─────────────
    if (canSubmit) {
        // Lesson/tutor summary card
        val summaryTitle = selectedLesson?.subject
            ?: selectedTutor?.displayName.orEmpty()
        val summaryMeta = selectedLesson?.let {
            "with ${it.tutorDisplayName} · ${it.dateLabel}"
        } ?: selectedTutor?.subjects.orEmpty()
        val summaryName = selectedLesson?.tutorDisplayName ?: selectedTutor?.displayName ?: ""

        CardQ {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialsAvatar(name = summaryName, size = 44.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = summaryTitle,
                        style = SoftType.title,
                        color = InkToken
                    )
                    Text(
                        text = summaryMeta,
                        style = SoftType.meta
                    )
                }
            }
        }

        // Star picker
        SoftCard {
            Text(
                text = "STEP 2 — HOW WAS IT?",
                style = SoftType.eyebrow,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InteractiveStarsRow(
                        rating = selectedRating,
                        onRatingChanged = { onReviewRatingChanged(it.toString()) }
                    )
                    Text(
                        text = ratingLabel(selectedRating),
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Primary600
                        )
                    )
                }
            }
        }

        // Quick tags ("What went well?") — backed by reviewForm.selectedTags, NOT folded into comment
        WentWellTags(
            selectedTags = state.reviewForm.selectedTags,
            onToggleTag = onToggleReviewTag
        )

        // Free-text note + anon toggle + submit
        SoftCard {
            Text(
                text = "Add a note",
                style = SoftType.h3,
                color = InkToken,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Optional · helps other students",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            SoftTextField(
                value = state.reviewForm.comment,
                onValueChange = onReviewCommentChanged,
                placeholder = "Share a few words to help other students…",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CardFlat {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Post anonymously",
                            style = SoftType.title,
                            color = InkToken
                        )
                        Text(
                            text = "Hide your name on this review",
                            style = SoftType.meta
                        )
                    }
                    AnonToggle(
                        checked = state.reviewForm.anonymous,
                        onCheckedChange = onSetReviewAnonymous
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SoftButton(
                text = if (isLessonMode) "Submit lesson review" else "Submit tutor review",
                onClick = { onClearMessages(); onSubmitReview() },
                enabled = true,
                variant = SoftButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        CardFlat {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.star,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Select a lesson or tutor above",
                        style = SoftType.title,
                        color = InkToken
                    )
                    Text(
                        text = "Your rating form will appear here",
                        style = SoftType.meta
                    )
                }
            }
        }
    }

    // ── Reviews you've written ─────────────────────────────────────────────────
    if (state.dashboardState.reviewsByMe.isNotEmpty()) {
        SectionHead(title = "Reviews you wrote")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.dashboardState.reviewsByMe.forEach { review ->
                ReviewListCard(review = review, showStudent = false)
            }
        }
    }

    // ── Report a tutor ────────────────────────────────────────────────────────
    ReportSection(
        state = state,
        onClearMessages = onClearMessages,
        onReportTutorChanged = onReportTutorChanged,
        onReportReasonChanged = onReportReasonChanged,
        onReportDetailsChanged = onReportDetailsChanged,
        onReportSeverityChanged = onReportSeverityChanged,
        onSubmitReport = onSubmitReport
    )
}

// ─── TUTOR branch ─────────────────────────────────────────────────────────────

@Composable
private fun TutorReviewsContent(state: SmartCampusUiState) {
    val reviews = state.dashboardState.reviewsForMe
    val currentUid = state.currentUser?.uid.orEmpty()

    if (reviews.isEmpty()) {
        CardFlat {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.star,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "No reviews yet",
                    style = SoftType.h3,
                    color = InkToken
                )
                Text(
                    text = "When a student rates a lesson,\nit shows up here.",
                    style = SoftType.body,
                    color = Ink3,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    // ── Summary card — real stats via TutorRatings ────────────────────────────
    val stats = TutorRatings.tutorRatingStats(currentUid, reviews)
    val avgStr = String.format("%.1f", stats.avg)
    val maxDist = stats.dist.maxOrNull()?.coerceAtLeast(1) ?: 1

    SoftCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big number + stars + count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = avgStr,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        color = InkToken
                    )
                )
                StarsRow(value = stats.avg.toFloat(), size = StarSize.Sm)
                Text(
                    text = "${stats.count} reviews",
                    style = SoftType.meta
                )
            }

            // Distribution bars — dist[0]=5★ … dist[4]=1★
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stats.dist.forEachIndexed { index, count ->
                    val star = 5 - index
                    val fraction = count.toFloat() / maxDist.toFloat()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$star",
                            style = SoftType.meta,
                            modifier = Modifier.width(10.dp)
                        )
                        ProgressBar(
                            fraction = fraction,
                            height = 6.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$count",
                            style = SoftType.meta,
                            modifier = Modifier.width(22.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }

    // ── Top tags — aggregated from review.tags (not from comment text) ────────
    val tagCounts = reviews
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }

    if (tagCounts.isNotEmpty()) {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tagCounts.take(6).forEach { (tag, count) ->
                Badge(text = "$tag · $count", tone = BadgeTone.Prim)
            }
        }
    }

    // ── Recent reviews — anonymous ones show "Anonymous" ─────────────────────
    SectionHead(title = "Recent")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        reviews.forEach { review ->
            ReviewListCard(review = review, showStudent = true)
        }
    }
}

// ─── Shared components ────────────────────────────────────────────────────────

@Composable
private fun InteractiveStarsRow(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= rating) SoftIcons.star else SoftIcons.starO,
                contentDescription = "$index stars",
                tint = if (index <= rating) Star else Ink4,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Star, bounded = false)
                    ) { onRatingChanged(index) }
            )
        }
    }
}

private val WELL_TAGS = listOf(
    "Clear explanations", "Patient", "On time",
    "Great materials", "Well prepared", "Good pace"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WentWellTags(selectedTags: List<String>, onToggleTag: (String) -> Unit) {
    SoftCard {
        Text(
            text = "WHAT WENT WELL?",
            style = SoftType.eyebrow,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WELL_TAGS.forEach { tag ->
                val isSelected = tag in selectedTags
                Chip(
                    text = tag,
                    selected = isSelected,
                    leadingIcon = if (isSelected) SoftIcons.check else null,
                    onClick = { onToggleTag(tag) }
                )
            }
        }
    }
}

@Composable
private fun ReviewListCard(review: TutorReviewUi, showStudent: Boolean) {
    val rawName = if (showStudent) review.studentDisplayName else review.tutorDisplayName
    val name = if (showStudent && review.anonymous) "Anonymous" else rawName
    val subject = review.subject.ifBlank { "General" }
    val (subjBg, subjFg) = subjectColors(subject)

    CardQ(padding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                InitialsAvatar(name = name, size = 34.dp)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = name,
                        style = SoftType.title,
                        color = InkToken,
                        fontSize = 13.5.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject,
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = subjFg
                            )
                        )
                        Text(
                            text = "·",
                            style = SoftType.meta
                        )
                        Text(
                            text = review.createdAtLabel,
                            style = SoftType.meta
                        )
                    }
                }
            }
            StarsRow(
                value = review.rating.toFloat(),
                size = StarSize.Sm
            )
        }
        if (review.comment.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = SoftType.bodySm,
                color = Ink2
            )
        }
    }
}

@Composable
private fun LessonPickRow(
    lesson: LessonBookingUi,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (selected) Primary else Line
    val bgColor = if (selected) Primary50 else CardSurface
    val (subjBg, subjFg) = subjectColors(lesson.subject)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Primary)
            ) { onSelect() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(subjBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SoftIcons.cap,
                    contentDescription = null,
                    tint = subjFg,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = lesson.subject,
                    style = SoftType.title,
                    color = InkToken
                )
                Text(
                    text = "${lesson.tutorDisplayName} · ${lesson.dateLabel} · ${lesson.timeLabel}",
                    style = SoftType.meta
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = SoftIcons.check,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "Select",
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = Primary600
                )
            )
        }
    }
}

@Composable
private fun AnonToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor = if (checked) Primary else Bg2
    val thumbOffset = if (checked) 21.dp else 3.dp
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(White)
        )
    }
}

// ─── Report section ───────────────────────────────────────────────────────────

@Composable
private fun ReportSection(
    state: SmartCampusUiState,
    onClearMessages: () -> Unit,
    onReportTutorChanged: (String) -> Unit,
    onReportReasonChanged: (String) -> Unit,
    onReportDetailsChanged: (String) -> Unit,
    onReportSeverityChanged: (String) -> Unit,
    onSubmitReport: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Entry point — collapsed by default to keep the happy path clean
    CardFlat {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.flag,
                        contentDescription = null,
                        tint = Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "Report a tutor",
                        style = SoftType.title,
                        color = InkToken
                    )
                    Text(
                        text = "For serious issues only",
                        style = SoftType.meta
                    )
                }
            }
            Icon(
                imageVector = if (expanded) SoftIcons.x else SoftIcons.chev,
                contentDescription = null,
                tint = Ink3,
                modifier = Modifier.size(18.dp)
            )
        }

        if (expanded) {
            Spacer(Modifier.height(14.dp))
            SoftDivider()
            Spacer(Modifier.height(14.dp))

            Text(
                text = "Use this only for serious issues. Reports go to the admin team.",
                style = SoftType.meta,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            TutorPicker(
                tutors = state.dashboardState.tutors,
                selected = state.reportForm.tutorUid,
                onPick = onReportTutorChanged
            )

            Spacer(Modifier.height(10.dp))

            SoftTextField(
                value = state.reportForm.reason,
                onValueChange = onReportReasonChanged,
                label = "Reason",
                placeholder = "e.g. inappropriate behaviour",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            SoftTextField(
                value = state.reportForm.details,
                onValueChange = onReportDetailsChanged,
                label = "Details",
                placeholder = "Describe what happened…",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "SEVERITY",
                style = SoftType.eyebrow,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LOW", "MEDIUM", "HIGH").forEach { level ->
                    Chip(
                        text = level.lowercase().replaceFirstChar { it.uppercase() },
                        selected = state.reportForm.severity == level,
                        onClick = { onReportSeverityChanged(level) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SoftButton(
                text = "Send report",
                onClick = { onClearMessages(); onSubmitReport() },
                enabled = state.reportForm.tutorUid.isNotBlank() && state.reportForm.reason.isNotBlank(),
                variant = SoftButtonVariant.Danger,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ── Reports you sent ──────────────────────────────────────────────────────
    if (state.dashboardState.reportsByMe.isNotEmpty()) {
        SectionHead(title = "Reports you sent")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val open = state.dashboardState.reportsByMe.count { it.status == "open" }
            val resolved = state.dashboardState.reportsByMe.count { it.status == "resolved" }
            val dismissed = state.dashboardState.reportsByMe.count { it.status == "dismissed" }
            if (open > 0) Badge(text = "open $open", tone = BadgeTone.Amber)
            if (resolved > 0) Badge(text = "resolved $resolved", tone = BadgeTone.Green)
            if (dismissed > 0) Badge(text = "dismissed $dismissed", tone = BadgeTone.Gray)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.dashboardState.reportsByMe.forEach { report ->
                CardQ(padding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = report.tutorDisplayName,
                                style = SoftType.title,
                                color = InkToken
                            )
                            Text(
                                text = report.reason,
                                style = SoftType.bodySm,
                                color = Ink2
                            )
                            Text(
                                text = report.createdAtLabel,
                                style = SoftType.meta
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        when (report.status) {
                            "resolved" -> Badge(text = "Resolved", tone = BadgeTone.Green)
                            "dismissed" -> Badge(text = "Dismissed", tone = BadgeTone.Gray)
                            else -> Badge(text = "Open", tone = BadgeTone.Amber)
                        }
                    }
                    if (report.details.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = report.details,
                            style = SoftType.meta,
                            color = Ink3
                        )
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun ratingLabel(rating: Int): String = when (rating) {
    5 -> "Excellent"
    4 -> "Very good"
    3 -> "Good"
    2 -> "Fair"
    else -> "Poor"
}

private fun reviewableLessons(state: SmartCampusUiState): List<LessonBookingUi> {
    val reviewedBookingIds = state.dashboardState.reviewsByMe
        .mapNotNull { it.bookingId.ifBlank { null } }
        .toSet()
    return state.dashboardState.myStudentBookings
        .filter { it.id !in reviewedBookingIds && it.canBeReviewedAfterLesson() }
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
