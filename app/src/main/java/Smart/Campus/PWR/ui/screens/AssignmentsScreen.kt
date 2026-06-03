package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.IconText
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.Pill
import Smart.Campus.PWR.ui.components.PlainCard
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.components.StatusChip
import Smart.Campus.PWR.ui.components.stableLiquidGlassSurface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import Smart.Campus.PWR.ui.state.AssignmentUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.SubmissionUi
import Smart.Campus.PWR.ui.theme.PwrIndigo
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrTeal
import Smart.Campus.PWR.ui.theme.StatusBookedBg
import Smart.Campus.PWR.ui.theme.StatusBookedFg
import Smart.Campus.PWR.ui.theme.StatusPendingBg
import Smart.Campus.PWR.ui.theme.StatusPendingFg
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var filter by remember(activeRole) { mutableStateOf(if (activeRole == UserRole.TUTOR) "Published" else "Open") }

    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)
        MonoLabel("Tasks")
        Text(
            if (activeRole == UserRole.TUTOR) "Publish work." else "Your task ledger.",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = if (activeRole == UserRole.TUTOR) {
                listOf("Published", "Due dated", "All")
            } else {
                listOf("Open", "Submitted", "All")
            }
            filters.forEach { label ->
                Pill(
                    text = label,
                    selected = filter == label,
                    onClick = { filter = label }
                )
            }
        }
        if (activeRole == UserRole.TUTOR) {
            TutorAssignments(
                state = state,
                filter = filter,
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
            StudentAssignments(
                state = state,
                filter = filter,
                onUploadSubmission = onUploadSubmission
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorAssignments(
    state: SmartCampusUiState,
    filter: String,
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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val ownedCourses = state.dashboardState.courseCatalog.filter { it.isOwner }

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

    SectionCard("Create assignment", accent = PwrIndigo) {
        if (ownedCourses.isEmpty()) {
            Text(
                "Create a course first — assignments belong to a course.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            MonoLabel("Course")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ownedCourses.forEach { course ->
                    Pill(
                        text = course.name,
                        selected = state.assignmentForm.courseId == course.id,
                        onClick = { onAssignmentCourseChanged(course.id, course.name) }
                    )
                }
            }
        }
        OutlinedTextField(
            value = state.assignmentForm.title,
            onValueChange = onAssignmentTitleChanged,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.assignmentForm.subject,
            onValueChange = onAssignmentSubjectChanged,
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
            OutlinedTextField(
                value = state.assignmentForm.dueDate,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Due date") },
                trailingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        OutlinedTextField(
            value = state.assignmentForm.description,
            onValueChange = onAssignmentDescriptionChanged,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        AppPrimaryButton(
            text = "Publish assignment",
            enabled = state.assignmentForm.title.isNotBlank() && state.assignmentForm.courseId.isNotBlank(),
            onClick = { onClearMessages(); onCreateAssignment() }
        )
    }

    Text(
        "My assignments",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )

    val myAssignments = state.dashboardState.assignments
        .filter { it.tutorUid == state.currentUser?.uid }
        .filter { assignment ->
            when (filter) {
                "Due dated" -> assignment.dueDateLabel.isNotBlank()
                else -> true
            }
        }
    if (myAssignments.isEmpty()) {
        Text("No assignments yet.", color = TextSecondary)
    }
    myAssignments.forEach { assignment ->
        TutorAssignmentCard(
            assignment = assignment,
            submissions = state.dashboardState.submissionsByAssignment[assignment.id],
            onLoadSubmissions = { onLoadSubmissionsForAssignment(assignment.id) },
            onDelete = { onDeleteAssignment(assignment.id) }
        )
    }
}

@Composable
private fun TutorAssignmentCard(
    assignment: AssignmentUi,
    submissions: List<SubmissionUi>?,
    onLoadSubmissions: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember(assignment.id) { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete assignment") },
            text = { Text("This deletes the assignment plus its submissions and uploaded files.") },
            confirmButton = {
                AppDangerButton(
                    text = "Delete",
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Back")
                }
            }
        )
    }

    PlainCard(accent = PwrIndigo) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(assignment.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                if (assignment.courseName.isNotBlank()) {
                    IconText(Icons.Rounded.School, assignment.courseName)
                }
                if (assignment.subject.isNotBlank()) {
                    IconText(Icons.AutoMirrored.Rounded.Assignment, assignment.subject)
                }
                if (assignment.dueDateLabel.isNotBlank()) {
                    IconText(Icons.Rounded.CalendarToday, "Due: ${assignment.dueDateLabel}")
                }
                if (assignment.description.isNotBlank()) {
                    Text(
                        assignment.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppPrimaryButton(
                text = if (expanded) "Hide submissions" else "View submissions",
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = !expanded
                    if (expanded && submissions == null) onLoadSubmissions()
                }
            )
            AppDangerButton("Delete", onClick = { showDeleteDialog = true })
        }

        if (expanded) {
            if (submissions == null) {
                Text("Loading submissions...", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } else if (submissions.isEmpty()) {
                Text("No submissions yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            } else {
                submissions.forEach { submission ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                submission.studentDisplayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                submission.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                submission.submittedAtLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        TextButton(onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, submission.downloadUrl.toUri())
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(Icons.Rounded.Download, contentDescription = "Download", tint = PwrNavy)
                            Spacer(Modifier.width(4.dp))
                            Text("Open", color = PwrNavy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentAssignments(
    state: SmartCampusUiState,
    filter: String,
    onUploadSubmission: (assignmentId: String, fileName: String, bytes: ByteArray) -> Unit
) {
    Text(
        "Open assignments",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
    val filteredAssignments = state.dashboardState.assignments.filter { assignment ->
        val submitted = state.dashboardState.mySubmissions.any { it.assignmentId == assignment.id }
        when (filter) {
            "Open" -> !submitted
            "Submitted" -> submitted
            else -> true
        }
    }
    if (filteredAssignments.isEmpty()) {
        Text("No assignments available.", color = TextSecondary)
    }
    filteredAssignments.forEach { assignment ->
        val mySubmission = state.dashboardState.mySubmissions
            .firstOrNull { it.assignmentId == assignment.id }
        StudentAssignmentCard(
            assignment = assignment,
            submission = mySubmission,
            onUpload = { fileName, bytes -> onUploadSubmission(assignment.id, fileName, bytes) }
        )
    }
}

@Composable
private fun StudentAssignmentCard(
    assignment: AssignmentUi,
    submission: SubmissionUi?,
    onUpload: (String, ByteArray) -> Unit
) {
    val context = LocalContext.current
    var pickError by remember { mutableStateOf<String?>(null) }

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

    PlainCard(accent = if (submission != null) PwrTeal else PwrIndigo) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(assignment.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                if (assignment.courseName.isNotBlank()) {
                    IconText(Icons.Rounded.School, assignment.courseName)
                }
                IconText(Icons.Rounded.Person, assignment.tutorDisplayName)
                if (assignment.subject.isNotBlank()) {
                    IconText(Icons.AutoMirrored.Rounded.Assignment, assignment.subject)
                }
                if (assignment.dueDateLabel.isNotBlank()) {
                    IconText(Icons.Rounded.CalendarToday, "Due: ${assignment.dueDateLabel}")
                }
                if (assignment.description.isNotBlank()) {
                    Text(
                        assignment.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            if (submission != null) {
                StatusChip("SUBMITTED", StatusBookedBg, StatusBookedFg)
            } else {
                StatusChip("PENDING", StatusPendingBg, StatusPendingFg)
            }
        }

        if (submission != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .stableLiquidGlassSurface(
                        cornerRadius = 16.dp,
                        tint = Smart.Campus.PWR.ui.theme.PwrBlueWhisper,
                        tintAlpha = 0.95f,
                        glowColor = Smart.Campus.PWR.ui.theme.PwrBlueLight,
                        glowAlpha = 0.30f
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Rounded.AttachFile,
                        contentDescription = null,
                        tint = Smart.Campus.PWR.ui.theme.PwrNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            submission.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Smart.Campus.PWR.ui.theme.PwrNavy,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            "Uploaded ${submission.submittedAtLabel}",
                            color = Smart.Campus.PWR.ui.theme.InkTextSoft,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
            AppPrimaryButton(
                text = "Replace file",
                leadingIcon = { Icon(Icons.Rounded.AttachFile, contentDescription = null) },
                onClick = { launcher.launch(arrayOf("*/*")) }
            )
        } else {
            AppPrimaryButton(
                text = "Upload file",
                leadingIcon = { Icon(Icons.Rounded.AttachFile, contentDescription = null) },
                onClick = { launcher.launch(arrayOf("*/*")) }
            )
        }

        pickError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
