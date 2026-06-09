package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.Chip
import Smart.Campus.PWR.ui.components.softindigo.ProgressBar
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SegTabs
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.SubjectDot
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.AssignmentUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.SubmissionUi
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.AmberBg
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.GreenBg
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.White
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Entry point — signature preserved exactly ────────────────────────────────

@Composable
fun AssignmentsTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onClearMessages: () -> Unit,
    onAssignmentTitleChanged: (String) -> Unit,
    onAssignmentDescriptionChanged: (String) -> Unit,
    onAssignmentSubjectChanged: (String) -> Unit,
    onAssignmentDueDateChanged: (String) -> Unit,
    onAssignmentCourseChanged: (String, String) -> Unit,
    onAssignmentDueAtChanged: (Long?, String) -> Unit,
    onCreateAssignment: () -> Unit,
    onDeleteAssignment: (String) -> Unit,
    onUploadSubmission: (assignmentId: String, fileName: String, bytes: ByteArray) -> Unit,
    onLoadSubmissionsForAssignment: (String) -> Unit
) {
    if (activeRole == UserRole.TUTOR) {
        TutorTasksScreen(
            state = state,
            onClearMessages = onClearMessages,
            onAssignmentTitleChanged = onAssignmentTitleChanged,
            onAssignmentDescriptionChanged = onAssignmentDescriptionChanged,
            onAssignmentSubjectChanged = onAssignmentSubjectChanged,
            onAssignmentCourseChanged = onAssignmentCourseChanged,
            onAssignmentDueAtChanged = onAssignmentDueAtChanged,
            onCreateAssignment = onCreateAssignment,
            onDeleteAssignment = onDeleteAssignment,
            onLoadSubmissionsForAssignment = onLoadSubmissionsForAssignment
        )
    } else {
        StudentTasksScreen(
            state = state,
            onUploadSubmission = onUploadSubmission
        )
    }
}

// ─── TUTOR branch ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorTasksScreen(
    state: SmartCampusUiState,
    onClearMessages: () -> Unit,
    onAssignmentTitleChanged: (String) -> Unit,
    onAssignmentDescriptionChanged: (String) -> Unit,
    onAssignmentSubjectChanged: (String) -> Unit,
    onAssignmentCourseChanged: (String, String) -> Unit,
    onAssignmentDueAtChanged: (Long?, String) -> Unit,
    onCreateAssignment: () -> Unit,
    onDeleteAssignment: (String) -> Unit,
    onLoadSubmissionsForAssignment: (String) -> Unit
) {
    var segIndex by remember { mutableStateOf(0) }
    var showCreateForm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val ownedCourses = state.dashboardState.courseCatalog.filter { it.isOwner }

    val myAssignments = state.dashboardState.assignments
        .filter { it.tutorUid == state.currentUser?.uid }

    val activeAssignments = myAssignments.filter { it.dueDateLabel.isNotBlank() }
    val closedAssignments = myAssignments.filter { it.dueDateLabel.isBlank() }

    val displayedAssignments = if (segIndex == 0) activeAssignments else closedAssignments

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onAssignmentDueAtChanged(millis, formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top bar
            item {
                SoftTopBar(
                    title = "Tasks",
                    actions = {
                        SoftIconButton(
                            icon = SoftIcons.plus,
                            onClick = { showCreateForm = !showCreateForm },
                            prim = showCreateForm
                        )
                    }
                )
            }

            // Create form (collapsible)
            if (showCreateForm) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHead(title = "New assignment")

                        if (ownedCourses.isEmpty()) {
                            CardQ {
                                Text(
                                    text = "Create a course first — assignments belong to a course.",
                                    style = SoftType.body
                                )
                            }
                        } else {
                            // Course picker chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "COURSE",
                                    style = SoftType.eyebrow
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ownedCourses.forEach { course ->
                                        Chip(
                                            text = course.name,
                                            selected = state.assignmentForm.courseId == course.id,
                                            onClick = { onAssignmentCourseChanged(course.id, course.name) }
                                        )
                                    }
                                }
                            }

                            SoftTextField(
                                value = state.assignmentForm.title,
                                onValueChange = onAssignmentTitleChanged,
                                label = "Title",
                                placeholder = "e.g. Midterm assignment",
                                modifier = Modifier.fillMaxWidth()
                            )
                            SoftTextField(
                                value = state.assignmentForm.subject,
                                onValueChange = onAssignmentSubjectChanged,
                                label = "Subject",
                                placeholder = "e.g. Mathematics",
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Date field (tappable, read-only)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Due date",
                                    style = TextStyle(
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Ink2
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardSurface)
                                        .border(1.dp, Bg2, RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { showDatePicker = true }
                                        .padding(vertical = 13.dp, horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.assignmentForm.dueDate.ifBlank { "Pick a date" },
                                        style = TextStyle(
                                            fontFamily = BodyFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 15.sp,
                                            color = if (state.assignmentForm.dueDate.isBlank()) Ink3 else InkToken
                                        )
                                    )
                                    Icon(
                                        imageVector = SoftIcons.calendar,
                                        contentDescription = null,
                                        tint = Ink3,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            SoftTextField(
                                value = state.assignmentForm.description,
                                onValueChange = onAssignmentDescriptionChanged,
                                label = "Description",
                                placeholder = "Describe the task…",
                                singleLine = false,
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            SoftButton(
                                text = "Publish assignment",
                                onClick = { onClearMessages(); onCreateAssignment(); showCreateForm = false },
                                enabled = state.assignmentForm.title.isNotBlank() && state.assignmentForm.courseId.isNotBlank(),
                                variant = SoftButtonVariant.Primary,
                                leadingIcon = SoftIcons.check,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // SegTabs
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    SegTabs(
                        options = listOf(
                            "Active · ${activeAssignments.size}",
                            "No-due · ${closedAssignments.size}"
                        ),
                        selectedIndex = segIndex,
                        onSelect = { segIndex = it }
                    )
                }
            }

            // Assignment list
            if (displayedAssignments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (segIndex == 0) "No active assignments yet.\nTap + to create one." else "No assignments without a due date.",
                            style = SoftType.body,
                            color = Ink3
                        )
                    }
                }
            } else {
                items(displayedAssignments, key = { it.id }) { assignment ->
                    TutorAssignmentCard(
                        assignment = assignment,
                        submissions = state.dashboardState.submissionsByAssignment[assignment.id],
                        isClosed = segIndex == 1,
                        onLoadSubmissions = { onLoadSubmissionsForAssignment(assignment.id) },
                        onDelete = { onDeleteAssignment(assignment.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom padding for nav bar
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun TutorAssignmentCard(
    assignment: AssignmentUi,
    submissions: List<SubmissionUi>?,
    isClosed: Boolean,
    onLoadSubmissions: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember(assignment.id) { mutableStateOf(false) }
    val context = LocalContext.current

    val subsCount = submissions?.size ?: 0
    // We don't have enrollment count — use submissionsByAssignment as proxy
    // Use a placeholder total of max(subsCount, 1) for ProgressBar fraction when null
    val totalKnown = if (submissions != null) submissions.size else 0

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete assignment",
                    style = SoftType.h3,
                    color = InkToken
                )
            },
            text = {
                Text(
                    "This deletes the assignment plus its submissions and uploaded files.",
                    style = SoftType.body
                )
            },
            confirmButton = {
                SoftButton(
                    text = "Delete",
                    onClick = { showDeleteDialog = false; onDelete() },
                    variant = SoftButtonVariant.Danger,
                    size = SoftButtonSize.Sm
                )
            },
            dismissButton = {
                SoftButton(
                    text = "Back",
                    onClick = { showDeleteDialog = false },
                    variant = SoftButtonVariant.Soft,
                    size = SoftButtonSize.Sm
                )
            }
        )
    }

    SoftCard(modifier = modifier.fillMaxWidth()) {
        // Header row: SubjectDot + title + badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            SubjectDot(
                subject = assignment.subject.ifBlank { assignment.courseName },
                icon = SoftIcons.doc
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = assignment.title,
                    style = SoftType.title,
                    color = InkToken
                )
                if (assignment.courseName.isNotBlank()) {
                    val (_, fg) = subjectColors(assignment.subject.ifBlank { assignment.courseName })
                    Text(
                        text = assignment.courseName,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = fg
                        )
                    )
                }
            }
            if (isClosed) {
                Badge(text = "No due date", tone = BadgeTone.Gray)
            } else if (assignment.dueDateLabel.isNotBlank()) {
                Badge(
                    text = "Due ${assignment.dueDateLabel}",
                    tone = BadgeTone.Amber,
                    leadingIcon = SoftIcons.clock
                )
            }
        }

        if (assignment.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = assignment.description,
                style = SoftType.body,
                color = Ink2
            )
        }

        Spacer(Modifier.height(10.dp))
        SoftDivider()
        Spacer(Modifier.height(10.dp))

        // Submissions progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Submissions",
                style = SoftType.meta
            )
            Text(
                text = if (submissions == null) "—" else "$subsCount submitted",
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Primary600
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        // ProgressBar: fraction based on submissions loaded (indeterminate if null)
        ProgressBar(
            fraction = if (submissions == null || submissions.isEmpty()) 0f
                       else (subsCount.toFloat() / maxOf(subsCount, 1).toFloat()),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftButton(
                text = if (expanded) "Hide" else "Review",
                onClick = {
                    expanded = !expanded
                    if (expanded && submissions == null) onLoadSubmissions()
                },
                variant = SoftButtonVariant.Soft,
                size = SoftButtonSize.Sm,
                leadingIcon = if (expanded) SoftIcons.x else SoftIcons.arrow,
                modifier = Modifier.weight(1f)
            )
            SoftButton(
                text = "Delete",
                onClick = { showDeleteDialog = true },
                variant = SoftButtonVariant.Danger,
                size = SoftButtonSize.Sm,
                leadingIcon = SoftIcons.x,
                modifier = Modifier.weight(1f)
            )
        }

        // Expanded submissions list
        if (expanded) {
            Spacer(Modifier.height(12.dp))
            SoftDivider()
            Spacer(Modifier.height(10.dp))

            when {
                submissions == null -> {
                    Text(
                        text = "Loading submissions…",
                        style = SoftType.meta
                    )
                }
                submissions.isEmpty() -> {
                    Text(
                        text = "No submissions yet.",
                        style = SoftType.meta
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        submissions.forEach { submission ->
                            SubmissionRow(
                                submission = submission,
                                onOpenUrl = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, submission.downloadUrl.toUri())
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionRow(
    submission: SubmissionUi,
    onOpenUrl: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Bg2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SoftIcons.doc,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = submission.studentDisplayName,
                style = SoftType.title,
                color = InkToken
            )
            Text(
                text = submission.fileName,
                style = SoftType.meta
            )
            Text(
                text = "Submitted ${submission.submittedAtLabel}",
                style = SoftType.meta
            )
        }
        SoftButton(
            text = "Open",
            onClick = onOpenUrl,
            variant = SoftButtonVariant.Soft,
            size = SoftButtonSize.Sm,
            leadingIcon = SoftIcons.arrow
        )
    }
}

// ─── STUDENT branch ───────────────────────────────────────────────────────────

@Composable
private fun StudentTasksScreen(
    state: SmartCampusUiState,
    onUploadSubmission: (assignmentId: String, fileName: String, bytes: ByteArray) -> Unit
) {
    var segIndex by remember { mutableStateOf(0) }

    val allAssignments = state.dashboardState.assignments
    val openAssignments = allAssignments.filter { assignment ->
        state.dashboardState.mySubmissions.none { it.assignmentId == assignment.id }
    }
    val submittedAssignments = allAssignments.filter { assignment ->
        state.dashboardState.mySubmissions.any { it.assignmentId == assignment.id }
    }

    val displayedAssignments = when (segIndex) {
        0 -> openAssignments
        1 -> submittedAssignments
        else -> allAssignments
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top bar
            item {
                SoftTopBar(title = "My Tasks")
            }

            // SegTabs
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    SegTabs(
                        options = listOf(
                            "Open · ${openAssignments.size}",
                            "Submitted · ${submittedAssignments.size}",
                            "All · ${allAssignments.size}"
                        ),
                        selectedIndex = segIndex,
                        onSelect = { segIndex = it }
                    )
                }
            }

            // Assignment list
            if (displayedAssignments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (segIndex) {
                                0 -> "All caught up — no open tasks."
                                1 -> "No submitted assignments yet."
                                else -> "No assignments available."
                            },
                            style = SoftType.body,
                            color = Ink3
                        )
                    }
                }
            } else {
                items(displayedAssignments, key = { it.id }) { assignment ->
                    val mySubmission = state.dashboardState.mySubmissions
                        .firstOrNull { it.assignmentId == assignment.id }
                    StudentAssignmentCard(
                        assignment = assignment,
                        submission = mySubmission,
                        onUpload = { fileName, bytes ->
                            onUploadSubmission(assignment.id, fileName, bytes)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom padding for nav bar
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun StudentAssignmentCard(
    assignment: AssignmentUi,
    submission: SubmissionUi?,
    onUpload: (String, ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pickError by remember { mutableStateOf<String?>(null) }

    // Existing file-picker mechanism — preserved exactly
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val resolver = context.contentResolver
                val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
                } ?: uri.lastPathSegment ?: "submission.bin"

                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    pickError = "Could not read selected file."
                } else if (bytes.size > 10 * 1024 * 1024) {
                    pickError = "File must be smaller than 10 MB."
                } else {
                    pickError = null
                    onUpload(fileName, bytes)
                }
            } catch (t: Throwable) {
                pickError = t.message ?: "Failed to read file."
            }
        }
    }

    SoftCard(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            SubjectDot(
                subject = assignment.subject.ifBlank { assignment.courseName },
                icon = SoftIcons.doc
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = assignment.title,
                    style = SoftType.title,
                    color = InkToken
                )
                if (assignment.courseName.isNotBlank()) {
                    val (_, fg) = subjectColors(assignment.subject.ifBlank { assignment.courseName })
                    Text(
                        text = assignment.courseName,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = fg
                        )
                    )
                }
                if (assignment.tutorDisplayName.isNotBlank()) {
                    Text(
                        text = assignment.tutorDisplayName,
                        style = SoftType.meta
                    )
                }
            }
            // Submission status badge
            if (submission != null) {
                Badge(text = "Submitted", tone = BadgeTone.Green, leadingIcon = SoftIcons.check)
            } else {
                Badge(text = "Pending", tone = BadgeTone.Amber)
            }
        }

        // Due date
        if (assignment.dueDateLabel.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = SoftIcons.clock,
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Due ${assignment.dueDateLabel}",
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Amber
                    )
                )
            }
        }

        if (assignment.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = assignment.description,
                style = SoftType.body,
                color = Ink2
            )
        }

        Spacer(Modifier.height(10.dp))
        SoftDivider()
        Spacer(Modifier.height(10.dp))

        // Submission info row (if submitted)
        if (submission != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = SoftIcons.attach,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(15.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.fileName,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Green
                        )
                    )
                    Text(
                        text = "Uploaded ${submission.submittedAtLabel}",
                        style = SoftType.meta
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Upload / replace button
        SoftButton(
            text = if (submission != null) "Replace file" else "Upload file",
            onClick = { launcher.launch(arrayOf("*/*")) },
            variant = if (submission != null) SoftButtonVariant.Soft else SoftButtonVariant.Primary,
            leadingIcon = SoftIcons.upload,
            modifier = Modifier.fillMaxWidth()
        )

        // Pick error
        pickError?.let { error ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = error,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Red
                )
            )
        }
    }
}
