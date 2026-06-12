package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonSize
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.subjectColors
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.CourseUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.SoftType
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextOverflow
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
    val isTutor = activeRole == UserRole.TUTOR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        SoftTopBar(
            title = "Courses",
            subtitle = if (isTutor) "Run your courses" else "Find your courses"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageBlock(state.errorMessage, state.infoMessage)

            if (isTutor) {
                SectionHead(title = "Create course")
                CardQ(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoftTextField(
                            value = state.courseForm.name,
                            onValueChange = onCourseNameChanged,
                            label = "Course name",
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftTextField(
                            value = state.courseForm.subject,
                            onValueChange = onCourseSubjectChanged,
                            label = "Subject / code",
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftTextField(
                            value = state.courseForm.description,
                            onValueChange = onCourseDescriptionChanged,
                            label = "Description",
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftButton(
                            text = "Create course",
                            enabled = state.courseForm.name.isNotBlank(),
                            onClick = { onClearMessages(); onCreateCourse() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                val owned = courses.filter { it.isOwner }
                SectionHead(title = "My courses")
                if (owned.isEmpty()) {
                    Text("No courses yet — create one above.", style = SoftType.bodySm, color = Ink3)
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
                SectionHead(title = "Enrolled")
                if (enrolled.isEmpty()) {
                    Text("You are not enrolled in any course yet.", style = SoftType.bodySm, color = Ink3)
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
                SectionHead(title = "Browse catalog")
                if (browsable.isEmpty()) {
                    Text("No more courses to join.", style = SoftType.bodySm, color = Ink3)
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
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CourseHeader(course: CourseUi) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (bg, fg) = subjectColors(course.subject.ifBlank { course.name })
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = SoftIcons.cap,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = SoftType.title,
                color = InkToken,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(
                    course.subject,
                    course.tutorDisplayName,
                    "${course.memberCount} enrolled"
                ).filter { it.isNotBlank() }.joinToString(" · "),
                style = SoftType.meta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (course.description.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(course.description, style = SoftType.bodySm, color = Ink3)
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
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) { Text("Delete", color = Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Back") } },
            containerColor = CardSurface
        )
    }

    CardQ(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CourseHeader(course)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SoftButton(
                    text = "Open chat",
                    onClick = onOpenChat,
                    leadingIcon = SoftIcons.chat,
                    size = SoftButtonSize.Sm,
                    modifier = Modifier.weight(1f)
                )
                SoftButton(
                    text = if (showRoster) "Hide roster" else "Roster",
                    onClick = {
                        showRoster = !showRoster
                        if (showRoster && roster == null) onLoadRoster()
                    },
                    variant = SoftButtonVariant.Outline,
                    size = SoftButtonSize.Sm
                )
                SoftButton(
                    text = "Delete",
                    onClick = { showDeleteDialog = true },
                    variant = SoftButtonVariant.Danger,
                    size = SoftButtonSize.Sm
                )
            }
            if (showRoster) {
                when {
                    roster == null -> Text("Loading roster…", style = SoftType.meta)
                    roster.isEmpty() -> Text("No students enrolled yet.", style = SoftType.meta)
                    else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        roster.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(member.displayName, style = SoftType.bodySm, color = InkToken)
                                Text(member.enrolledAtLabel, style = SoftType.meta)
                            }
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
    CardQ(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CourseHeader(course)
            if (enrolled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SoftButton(
                        text = "Open chat",
                        onClick = onOpenChat,
                        leadingIcon = SoftIcons.chat,
                        size = SoftButtonSize.Sm,
                        modifier = Modifier.weight(1f)
                    )
                    SoftButton(
                        text = "Leave",
                        onClick = onLeave,
                        variant = SoftButtonVariant.Outline,
                        size = SoftButtonSize.Sm
                    )
                }
            } else {
                SoftButton(
                    text = "Enroll",
                    onClick = onEnroll,
                    size = SoftButtonSize.Sm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
