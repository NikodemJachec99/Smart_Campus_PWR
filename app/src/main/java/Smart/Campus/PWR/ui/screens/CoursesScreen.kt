package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.IconText
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.PlainCard
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.state.CourseUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.PwrIndigo
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrTeal
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CoursesTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onCourseNameChanged: (String) -> Unit,
    onCourseSubjectChanged: (String) -> Unit,
    onCourseDescriptionChanged: (String) -> Unit,
    onCreateCourse: () -> Unit,
    onEnroll: (String) -> Unit,
    onLeave: (String) -> Unit,
    onDeleteCourse: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onLoadRoster: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    val courses = state.dashboardState.courseCatalog

    MainList {
        MessageBlock(state.errorMessage, state.infoMessage)
        MonoLabel("Courses")
        Text(
            if (activeRole == UserRole.TUTOR) "Run your courses." else "Find your courses.",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )

        if (activeRole == UserRole.TUTOR) {
            SectionCard("Create course", accent = PwrIndigo) {
                OutlinedTextField(
                    value = state.courseForm.name,
                    onValueChange = onCourseNameChanged,
                    label = { Text("Course name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.courseForm.subject,
                    onValueChange = onCourseSubjectChanged,
                    label = { Text("Subject / code") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.courseForm.description,
                    onValueChange = onCourseDescriptionChanged,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                AppPrimaryButton(
                    text = "Create course",
                    enabled = state.courseForm.name.isNotBlank(),
                    onClick = { onClearMessages(); onCreateCourse() }
                )
            }

            val owned = courses.filter { it.isOwner }
            Text("My courses", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (owned.isEmpty()) {
                Text("No courses yet — create one above.", color = TextSecondary)
            }
            owned.forEach { course ->
                OwnerCourseCard(
                    course = course,
                    roster = state.dashboardState.rosterByCourse[course.id],
                    onOpenChat = { onOpenChat(course.id) },
                    onLoadRoster = { onLoadRoster(course.id) },
                    onDelete = { onClearMessages(); onDeleteCourse(course.id) }
                )
            }
        } else {
            val enrolled = courses.filter { it.isEnrolled }
            Text("Enrolled", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (enrolled.isEmpty()) {
                Text("You are not enrolled in any course yet.", color = TextSecondary)
            }
            enrolled.forEach { course ->
                StudentCourseCard(
                    course = course,
                    enrolled = true,
                    onOpenChat = { onOpenChat(course.id) },
                    onEnroll = { onClearMessages(); onEnroll(course.id) },
                    onLeave = { onClearMessages(); onLeave(course.id) }
                )
            }

            val browsable = courses.filterNot { it.isEnrolled || it.isOwner }
            Text("Browse catalog", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (browsable.isEmpty()) {
                Text("No more courses to join.", color = TextSecondary)
            }
            browsable.forEach { course ->
                StudentCourseCard(
                    course = course,
                    enrolled = false,
                    onOpenChat = { onOpenChat(course.id) },
                    onEnroll = { onClearMessages(); onEnroll(course.id) },
                    onLeave = { onClearMessages(); onLeave(course.id) }
                )
            }
        }
    }
}

@Composable
private fun CourseHeader(course: CourseUi) {
    Text(course.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    if (course.subject.isNotBlank()) {
        IconText(Icons.Rounded.School, course.subject)
    }
    IconText(Icons.Rounded.Person, course.tutorDisplayName)
    IconText(Icons.Rounded.Group, "${course.memberCount} enrolled")
    if (course.description.isNotBlank()) {
        Text(course.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun OwnerCourseCard(
    course: CourseUi,
    roster: List<Smart.Campus.PWR.ui.state.CourseMemberUi>?,
    onOpenChat: () -> Unit,
    onLoadRoster: () -> Unit,
    onDelete: () -> Unit
) {
    var showRoster by remember { mutableStateOf(false) }
    var showDeleteDialog by remember(course.id) { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete course") },
            text = { Text("This removes the course. Assignments and chat for this course will no longer be visible to students.") },
            confirmButton = { AppDangerButton("Delete", onClick = { showDeleteDialog = false; onDelete() }) },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Back") } }
        )
    }

    PlainCard(accent = PwrIndigo) {
        CourseHeader(course)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppPrimaryButton(
                text = "Open chat",
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null) },
                onClick = onOpenChat
            )
            AppDangerButton("Delete", onClick = { showDeleteDialog = true })
        }
        AppDangerButton(
            text = if (showRoster) "Hide roster" else "View roster",
            onClick = {
                showRoster = !showRoster
                if (showRoster && roster == null) onLoadRoster()
            }
        )
        if (showRoster) {
            when {
                roster == null -> Text("Loading roster...", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                roster.isEmpty() -> Text("No students enrolled yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    roster.forEach { member ->
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(member.displayName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(member.enrolledAtLabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCourseCard(
    course: CourseUi,
    enrolled: Boolean,
    onOpenChat: () -> Unit,
    onEnroll: () -> Unit,
    onLeave: () -> Unit
) {
    PlainCard(accent = if (enrolled) PwrTeal else PwrIndigo) {
        CourseHeader(course)
        if (enrolled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppPrimaryButton(
                    text = "Open chat",
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null) },
                    onClick = onOpenChat
                )
                AppDangerButton("Leave", onClick = onLeave)
            }
        } else {
            AppPrimaryButton(text = "Enroll", onClick = onEnroll)
        }
    }
}
