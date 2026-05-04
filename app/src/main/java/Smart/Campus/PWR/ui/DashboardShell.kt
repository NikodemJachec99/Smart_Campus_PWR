package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.DashboardTab
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorReportUi
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.state.TutorSummaryUi
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    state: SmartCampusUiState,
    onToggleRole: () -> Unit,
    onRefreshDashboard: () -> Unit,
    onLogout: () -> Unit,
    onAvailabilitySubjectChanged: (String) -> Unit,
    onAvailabilityDateChanged: (String) -> Unit,
    onAvailabilityStartHourChanged: (String) -> Unit,
    onAvailabilityEndHourChanged: (String) -> Unit,
    onAddAvailability: () -> Unit,
    onBookTutorSlot: (String) -> Unit,
    onReviewTutorChanged: (String) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onReportTutorChanged: (String) -> Unit,
    onReportReasonChanged: (String) -> Unit,
    onReportDetailsChanged: (String) -> Unit,
    onSubmitReport: () -> Unit,
    onClearMessages: () -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onCancelBooking: (String, String) -> Unit
) {
    val currentUser = state.currentUser ?: return
    val activeRole = state.activeRole ?: return
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: DashboardRoutes.HOME

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("${currentUser.displayName} (${activeRole.displayName})") },
                actions = {
                    if (currentUser.hasDualRole()) {
                        OutlinedButton(onClick = { onClearMessages(); onToggleRole() }) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (activeRole == UserRole.STUDENT) "Tutor" else "Student")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AppSurface) {
                DashboardTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PwrNavy,
                            selectedTextColor = PwrNavy,
                            indicatorColor = PwrBlueSoft
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = DashboardRoutes.HOME, modifier = Modifier.fillMaxSize().padding(padding)) {
            composable(DashboardRoutes.HOME) {
                MainList {
                    MessageBlock(state.errorMessage, state.infoMessage)
                    SectionCard("Dashboard") {
                        Text(if (activeRole == UserRole.STUDENT) "Book lessons and manage your student calendar." else "Manage tutor availability and sessions.")
                        Button(onClick = { onClearMessages(); onRefreshDashboard() }) { Text("Refresh") }
                    }
                    Text(if (activeRole == UserRole.STUDENT) "My student lessons" else "My tutor lessons", fontWeight = FontWeight.Bold)
                    val lessons = if (activeRole == UserRole.STUDENT) state.dashboardState.myStudentBookings else state.dashboardState.myTutorBookings
                    if (lessons.isEmpty()) Text("No lessons yet.", color = TextSecondary)
                    lessons.forEach { LessonCard(it, onCancel = onCancelBooking) }
                }
            }
            composable(DashboardRoutes.CALENDAR) {
                MainList {
                    MessageBlock(state.errorMessage, state.infoMessage)
                    if (activeRole == UserRole.TUTOR) {
                        var showDatePicker by remember { mutableStateOf(false) }
                        var showStartTimePicker by remember { mutableStateOf(false) }
                        var showEndTimePicker by remember { mutableStateOf(false) }

                        val datePickerState = rememberDatePickerState(
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    calendar.set(java.util.Calendar.MINUTE, 0)
                                    calendar.set(java.util.Calendar.SECOND, 0)
                                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                                    return utcTimeMillis >= calendar.timeInMillis
                                }
                            }
                        )

                        val startTimeState = rememberTimePickerState(initialHour = 12, initialMinute = 0)
                        val endTimeState = rememberTimePickerState(initialHour = 13, initialMinute = 0)

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            onAvailabilityDateChanged(formatter.format(Date(millis)))
                                        }
                                        showDatePicker = false
                                    }) { Text("OK") }
                                }
                            ) { DatePicker(state = datePickerState) }
                        }

                        if (showStartTimePicker) {
                            TimePickerDialogWrapper(
                                title = "Select start time",
                                onDismissRequest = { showStartTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val formatted = String.format("%02d:%02d", startTimeState.hour, startTimeState.minute)
                                        onAvailabilityStartHourChanged(formatted)
                                        showStartTimePicker = false
                                    }) { Text("OK") }
                                }
                            ) { TimePicker(state = startTimeState) }
                        }

                        if (showEndTimePicker) {
                            TimePickerDialogWrapper(
                                title = "Select end time",
                                onDismissRequest = { showEndTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val formatted = String.format("%02d:%02d", endTimeState.hour, endTimeState.minute)
                                        onAvailabilityEndHourChanged(formatted)
                                        showEndTimePicker = false
                                    }) { Text("OK") }
                                }
                            ) { TimePicker(state = endTimeState) }
                        }

                        SectionCard("Set availability") {
                            OutlinedTextField(
                                value = state.availabilityForm.subject,
                                onValueChange = onAvailabilitySubjectChanged,
                                label = { Text("Subject") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                                OutlinedTextField(
                                    value = state.availabilityForm.date,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Date") },
                                    trailingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }) {
                                    OutlinedTextField(
                                        value = state.availabilityForm.startHour,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Start") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                                Box(modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }) {
                                    OutlinedTextField(
                                        value = state.availabilityForm.endHour,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("End") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                            }

                            Button(
                                onClick = { onClearMessages(); onAddAvailability() },
                                enabled = state.availabilityForm.subject.isNotBlank() && state.availabilityForm.date.isNotBlank() && state.availabilityForm.startHour.isNotBlank()
                            ) { Text("Add slot") }
                        }

                        Text("Scheduled lessons", fontWeight = FontWeight.Bold)
                        val bookedLessons = state.dashboardState.myTutorBookings.filter { it.status == "booked" }
                        if (bookedLessons.isEmpty()) Text("No scheduled lessons.", color = TextSecondary)
                        bookedLessons.forEach { LessonCard(it, onCancel = onCancelBooking) }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Open availability slots", fontWeight = FontWeight.Bold)
                        val openSlots = state.dashboardState.myAvailability.filter { !it.isBooked }
                        if (openSlots.isEmpty()) Text("No open slots.", color = TextSecondary)
                        openSlots.forEach { AvailabilityCard(it, onBook = null, onDelete = onDeleteAvailability) }

                    } else {
                        var searchQuery by remember { mutableStateOf("") }
                        var filterDate by remember { mutableStateOf("") }
                        var showDatePicker by remember { mutableStateOf(false) }
                        val datePickerState = rememberDatePickerState()

                        Text("My booked lessons", fontWeight = FontWeight.Bold)
                        if (state.dashboardState.myStudentBookings.isEmpty()) Text("No booked lessons.", color = TextSecondary)
                        state.dashboardState.myStudentBookings.forEach { LessonCard(it, onCancel = onCancelBooking) }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Search & Filter", fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Subject or tutor") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                            )

                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = "Filter by date")
                            }
                        }

                        if (filterDate.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Date: $filterDate", color = PwrNavy, style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { filterDate = "" }) {
                                    Text("Clear date", color = PwrRed)
                                }
                            }
                        }

                        val filteredSlots = state.dashboardState.availableTutorSlots.filter { slot ->
                            val matchesText = searchQuery.isBlank() ||
                                    slot.subject.contains(searchQuery, ignoreCase = true) ||
                                    slot.tutorDisplayName.contains(searchQuery, ignoreCase = true)

                            val matchesDate = filterDate.isBlank() || slot.dateLabel == filterDate

                            matchesText && matchesDate
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Open tutor slots", fontWeight = FontWeight.Bold)

                        if (filteredSlots.isEmpty()) {
                            Text("No slots match your search.", color = TextSecondary)
                        }

                        filteredSlots.forEach { AvailabilityCard(it, onBook = onBookTutorSlot) }

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            filterDate = formatter.format(Date(millis))
                                        }
                                        showDatePicker = false
                                    }) { Text("Filter") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }
                }
            }
            composable(DashboardRoutes.REVIEWS) {
                MainList {
                    MessageBlock(state.errorMessage, state.infoMessage)
                    if (activeRole == UserRole.STUDENT) {
                        SectionCard("Add review") {
                            TutorPicker(state.dashboardState.tutors, state.reviewForm.tutorUid, onReviewTutorChanged)
                            OutlinedTextField(state.reviewForm.tutorUid, onReviewTutorChanged, label = { Text("Tutor UID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.reviewForm.rating, onReviewRatingChanged, label = { Text("Rating 1-5") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.reviewForm.comment, onReviewCommentChanged, label = { Text("Comment") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = { onClearMessages(); onSubmitReview() }) { Text("Submit review") }
                        }
                        SectionCard("Report tutor") {
                            TutorPicker(state.dashboardState.tutors, state.reportForm.tutorUid, onReportTutorChanged)
                            OutlinedTextField(state.reportForm.tutorUid, onReportTutorChanged, label = { Text("Tutor UID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.reportForm.reason, onReportReasonChanged, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.reportForm.details, onReportDetailsChanged, label = { Text("Details") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = { onClearMessages(); onSubmitReport() }) { Text("Send report") }
                        }
                        Text("My reviews", fontWeight = FontWeight.Bold)
                        if (state.dashboardState.reviewsByMe.isEmpty()) Text("No reviews.", color = TextSecondary)
                        state.dashboardState.reviewsByMe.forEach { ReviewCard(it) }
                    } else {
                        Text("Reviews about me", fontWeight = FontWeight.Bold)
                        if (state.dashboardState.reviewsForMe.isEmpty()) Text("No reviews yet.", color = TextSecondary)
                        state.dashboardState.reviewsForMe.forEach { ReviewCard(it) }
                    }
                }
            }
            composable(DashboardRoutes.PROFILE) {
                MainList {
                    SectionCard("Profile") {
                        Text(currentUser.displayName)
                        Text(currentUser.email, color = TextSecondary)
                        Text("Roles: ${currentUser.roles.joinToString { it.displayName }}")
                        OutlinedButton(onClick = onLogout) {
                            Icon(Icons.Rounded.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminShellScreen(
    state: SmartCampusUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateLoginChanged: (String) -> Unit,
    onCreatePasswordChanged: (String) -> Unit,
    onCreateDisplayNameChanged: (String) -> Unit,
    onCreateAdminChecked: (Boolean) -> Unit,
    onCreateStudentChecked: (Boolean) -> Unit,
    onCreateTutorChecked: (Boolean) -> Unit,
    onCreateUserClick: () -> Unit,
    onUpdateUserRoles: (String, Boolean, Boolean) -> Unit,
    onToggleInspector: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    MainList {
        SectionCard("Admin Panel") {
            Text(state.currentUser?.displayName.orEmpty())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onClearMessages(); onRefresh() }) { Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(4.dp)); Text("Refresh") }
                OutlinedButton(onClick = onLogout) { Text("Sign out") }
            }
        }
        MessageBlock(state.errorMessage, state.infoMessage)
        SectionCard("Add user") {
            OutlinedTextField(state.createUserForm.login, onCreateLoginChanged, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.createUserForm.password, onCreatePasswordChanged, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.createUserForm.displayName, onCreateDisplayNameChanged, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            RoleCheck("Admin", state.createUserForm.admin, onCreateAdminChecked)
            RoleCheck("Student", state.createUserForm.student, onCreateStudentChecked)
            RoleCheck("Tutor", state.createUserForm.tutor, onCreateTutorChecked)
            Button(onClick = { onClearMessages(); onCreateUserClick() }) { Text("Create user") }
        }
        Text("Users", fontWeight = FontWeight.Bold)
        if (state.adminUsers.isEmpty()) Text("No users.", color = TextSecondary)
        state.adminUsers.forEach { user ->
            val inspector = state.adminInspectors[user.uid]
            AdminUserCard(user, inspector, onUpdateUserRoles, onToggleInspector)
        }
        Text("Reports", fontWeight = FontWeight.Bold)
        if (state.dashboardState.adminReports.isEmpty()) Text("No reports.", color = TextSecondary)
        state.dashboardState.adminReports.forEach { ReportCard(it) }
    }
}

@Composable private fun MainList(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
    }
}

@Composable private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable private fun RoleCheck(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked, onChange); Text(label) }
}

@Composable
private fun AdminUserCard(user: AppUser, inspector: AdminUserInspectorUi?, onUpdateUserRoles: (String, Boolean, Boolean) -> Unit, onToggleInspector: (String) -> Unit) {
    var student by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.STUDENT)) }
    var tutor by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.TUTOR)) }
    SectionCard(user.displayName) {
        Text("${user.login} | ${user.email}", color = TextSecondary)
        Text("Roles: ${user.roles.joinToString { it.displayName }}", color = TextSecondary)
        if (!user.hasRole(UserRole.ADMIN)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(student, { student = it }); Text("Student")
                Checkbox(tutor, { tutor = it }); Text("Tutor")
                Button(onClick = { onUpdateUserRoles(user.uid, student, tutor) }) { Text("Save") }
            }
        }
        OutlinedButton(onClick = { onToggleInspector(user.uid) }) { Text(if (inspector == null) "Inspect user" else "Hide details") }
        if (inspector != null) {
            if (inspector.isLoading) {
                Text("Loading...")
            } else {
                Text("Availability", fontWeight = FontWeight.SemiBold)
                if (inspector.availability.isEmpty()) Text("No availability.", color = TextSecondary)
                inspector.availability.forEach { slot ->
                    Text("- ${slot.dateLabel} ${slot.timeLabel} | ${slot.subject}", style = MaterialTheme.typography.bodySmall)
                }

                Text("Bookings as tutor", fontWeight = FontWeight.SemiBold)
                if (inspector.bookingsAsTutor.isEmpty()) Text("No tutor bookings.", color = TextSecondary)
                inspector.bookingsAsTutor.forEach { lesson ->
                    Text("- ${lesson.dateLabel} ${lesson.timeLabel} | ${lesson.subject}", style = MaterialTheme.typography.bodySmall)
                }

                Text("Bookings as student", fontWeight = FontWeight.SemiBold)
                if (inspector.bookingsAsStudent.isEmpty()) Text("No student bookings.", color = TextSecondary)
                inspector.bookingsAsStudent.forEach { lesson ->
                    Text("- ${lesson.dateLabel} ${lesson.timeLabel} | ${lesson.subject}", style = MaterialTheme.typography.bodySmall)
                }

                Text("Reviews received", fontWeight = FontWeight.SemiBold)
                if (inspector.reviewsReceived.isEmpty()) Text("No received reviews.", color = TextSecondary)
                inspector.reviewsReceived.forEach { review ->
                    Text("- ${review.rating}/5 ${review.studentDisplayName}: ${review.comment}", style = MaterialTheme.typography.bodySmall)
                }

                Text("Reports received", fontWeight = FontWeight.SemiBold)
                if (inspector.reportsReceived.isEmpty()) Text("No reports.", color = TextSecondary)
                inspector.reportsReceived.forEach { report ->
                    Text("- ${report.reason} | ${report.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable private fun TutorPicker(tutors: List<TutorSummaryUi>, selected: String, onPick: (String) -> Unit) {
    if (tutors.isEmpty()) {
        Text("No tutors found.", color = TextSecondary)
        return
    }
    tutors.take(4).forEach { tutor ->
        Surface(color = if (selected == tutor.uid) PwrBlueSoft else AppBackground, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().clickable { onPick(tutor.uid) }) {
            Text("${tutor.displayName} (${tutor.uid})", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun AvailabilityCard(
    slot: TutorAvailabilityUi,
    onBook: ((String) -> Unit)?,
    onDelete: ((String) -> Unit)? = null
) {
    SectionCard(slot.subject) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Tutor: ${slot.tutorDisplayName}")
                Text("${slot.dateLabel} | ${slot.timeLabel}")
            }
            if (onDelete != null && !slot.isBooked) {
                OutlinedButton(onClick = { onDelete(slot.id) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = PwrRed)
                }
            }
            if (onBook != null) {
                Button(onClick = { onBook(slot.id) }, enabled = !slot.isBooked) {
                    Text(if (slot.isBooked) "Booked" else "Book")
                }
            }
        }
    }
}

@Composable private fun LessonCard(
    lesson: LessonBookingUi,
    onCancel: ((String, String) -> Unit)? = null
) {
    SectionCard(lesson.subject) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Tutor: ${lesson.tutorDisplayName}")
                Text("Student: ${lesson.studentDisplayName}")
                Text("${lesson.dateLabel} | ${lesson.timeLabel}")
                Text("Status: ${lesson.status.uppercase()}", fontWeight = FontWeight.SemiBold, color = if (lesson.status == "cancelled") PwrRed else PwrNavy)
            }
            if (onCancel != null && lesson.status == "booked") {
                OutlinedButton(onClick = { onCancel(lesson.id, lesson.availabilityId) }) {
                    Text("Cancel Lesson", color = PwrRed)
                }
            }
        }
    }
}

@Composable private fun ReviewCard(review: TutorReviewUi) {
    SectionCard("${review.rating}/5 - ${review.tutorDisplayName}") {
        Text(review.comment)
        Text(review.createdAtLabel, color = TextSecondary)
    }
}

@Composable private fun ReportCard(report: TutorReportUi) {
    SectionCard("Report: ${report.reason}") {
        Text("Tutor: ${report.tutorDisplayName}")
        if (report.details.isNotBlank()) Text(report.details)
        Text("Status: ${report.status}")
        Text(report.createdAtLabel, color = TextSecondary)
    }
}

@Composable private fun MessageBlock(error: String?, info: String?) {
    if (error != null) Surface(color = PwrRed.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) { Text(error, color = PwrRed, modifier = Modifier.padding(10.dp)) }
    if (info != null) Surface(color = PwrBlueSoft, shape = RoundedCornerShape(12.dp)) { Text(info, color = PwrNavy, modifier = Modifier.padding(10.dp)) }
}

private fun DashboardTab.icon(): ImageVector = when (this) {
    DashboardTab.HOME -> Icons.Rounded.Home
    DashboardTab.CALENDAR -> Icons.Rounded.CalendarMonth
    DashboardTab.REVIEWS -> Icons.Rounded.MenuBook
    DashboardTab.PROFILE -> Icons.Rounded.Person
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogWrapper(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(16.dp),
            color = AppSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 20.dp))
                content()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    confirmButton()
                }
            }
        }
    }
}