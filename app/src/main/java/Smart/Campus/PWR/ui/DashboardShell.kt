package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AdminUserCard
import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.AvailabilityCard
import Smart.Campus.PWR.ui.components.EditorialTopBar
import Smart.Campus.PWR.ui.components.SoftBottomNav
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MessageBlock
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.ProvideScrollLifted
import Smart.Campus.PWR.ui.components.ReportCard
import Smart.Campus.PWR.ui.components.RoleCheck
import Smart.Campus.PWR.ui.components.SectionCard
import Smart.Campus.PWR.ui.components.TimePickerDialogWrapper
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.Chip
import Smart.Campus.PWR.ui.components.softindigo.SearchField
import Smart.Campus.PWR.ui.components.softindigo.SectionHead
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.components.softindigo.SoftCard
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.theme.Amber
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Bg2
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary100
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.RedBg
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.White
import Smart.Campus.PWR.ui.screens.AssignmentsTab
import Smart.Campus.PWR.ui.screens.CalendarTab
import Smart.Campus.PWR.ui.screens.ChatTab
import Smart.Campus.PWR.ui.screens.ConversationScreen
import Smart.Campus.PWR.ui.screens.CoursesTab
import Smart.Campus.PWR.ui.screens.HomeTab
import Smart.Campus.PWR.ui.screens.LessonsTab
import Smart.Campus.PWR.ui.screens.NotificationsScreen
import Smart.Campus.PWR.ui.screens.ProfileTab
import Smart.Campus.PWR.ui.screens.ReviewsTab
import Smart.Campus.PWR.ui.state.NotificationUi
import androidx.activity.compose.BackHandler
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.DashboardTab
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.InkText
import Smart.Campus.PWR.ui.theme.InkTextSoft
import Smart.Campus.PWR.ui.theme.PwrRed
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainShellScreen(
    state: SmartCampusUiState,
    viewModel: SmartCampusViewModel,
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
    onReviewBookingChanged: (String) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onReportTutorChanged: (String) -> Unit,
    onReportReasonChanged: (String) -> Unit,
    onReportDetailsChanged: (String) -> Unit,
    onSubmitReport: () -> Unit,
    onClearMessages: () -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onUpdateAvailability: (String, String, String, String, String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onAssignmentTitleChanged: (String) -> Unit,
    onAssignmentDescriptionChanged: (String) -> Unit,
    onAssignmentSubjectChanged: (String) -> Unit,
    onAssignmentDueDateChanged: (String) -> Unit,
    onCreateAssignment: () -> Unit,
    onDeleteAssignment: (String) -> Unit,
    onUploadSubmission: (assignmentId: String, fileName: String, bytes: ByteArray) -> Unit,
    onLoadSubmissionsForAssignment: (String) -> Unit
) {
    val currentUser = state.currentUser ?: return
    val activeRole = state.activeRole ?: return
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: DashboardRoutes.HOME
    val backdrop = rememberLayerBackdrop()
    val scrollLifted = remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    val conversationActive = state.chat.activeDirectId != null || state.chat.activeCourseId != null

    val openNotification: (NotificationUi) -> Unit = { notification ->
        viewModel.markNotificationRead(notification.id)
        showNotifications = false
        val conversationId = notification.data["conversationId"]
        val courseId = notification.data["courseId"]
        val messageId = notification.data["messageId"].orEmpty()
        when {
            conversationId != null -> viewModel.openDirectConversation(conversationId, messageId)
            courseId != null && (notification.type == "chat" || notification.type == "announcement") ->
                viewModel.openCourseConversation(courseId, messageId)
            else -> navController.navigateToRoute(DashboardRoutes.ASSIGNMENTS)
        }
    }

    ProvideScrollLifted(scrollLifted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
        ) {
            when {
                conversationActive -> {
                    BackHandler { viewModel.closeConversation() }
                    ConversationScreen(
                        state = state,
                        onBack = { viewModel.closeConversation() },
                        onComposerChanged = viewModel::onComposerChanged,
                        onSend = viewModel::sendMessage,
                        onAnnouncementToggle = viewModel::onAnnouncementToggle,
                        onRetryMessage = viewModel::retryMessage,
                        onLoadOlderMessages = viewModel::loadOlderMessages,
                        onConversationSearchChanged = viewModel::onConversationSearchChanged,
                        onAttachmentSelected = viewModel::onAttachmentSelected,
                        onClearAttachment = viewModel::clearPendingAttachment,
                        onReplyToMessage = viewModel::replyToMessage,
                        onClearReply = viewModel::clearReplyDraft,
                        onReactToMessage = viewModel::reactToMessage,
                        onDeleteMessage = viewModel::deleteMessage,
                        onReportMessage = viewModel::reportMessage,
                        onClearNewMessageHint = viewModel::clearNewMessageHint
                    )
                }

                showNotifications -> {
                    BackHandler { showNotifications = false }
                    NotificationsScreen(
                        state = state,
                        onBack = { showNotifications = false },
                        onOpen = openNotification,
                        onMarkAllRead = viewModel::markAllNotificationsRead
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = DashboardRoutes.HOME,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(DashboardRoutes.HOME) {
                                HomeTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onClearMessages = onClearMessages,
                                    onRefresh = onRefreshDashboard,
                                    onCancelBooking = onCancelBooking,
                                    onNavigate = { navController.navigateToRoute(it) },
                                    onToggleRole = onToggleRole,
                                    onOpenNotifications = { showNotifications = true },
                                    onAcceptBooking = viewModel::acceptBooking,
                                    onDeclineBooking = viewModel::declineBooking,
                                    onMarkCompleted = viewModel::markLessonCompleted,
                                    onMarkNoShow = viewModel::markNoShow
                                )
                            }
                            composable(DashboardRoutes.CALENDAR) {
                                CalendarTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onClearMessages = onClearMessages,
                                    onAvailabilitySubjectChanged = onAvailabilitySubjectChanged,
                                    onAvailabilityDateChanged = onAvailabilityDateChanged,
                                    onAvailabilityStartHourChanged = onAvailabilityStartHourChanged,
                                    onAvailabilityEndHourChanged = onAvailabilityEndHourChanged,
                                    onAvailabilityDurationChanged = viewModel::onAvailabilityDurationChanged,
                                    onAvailabilityFormatChanged = viewModel::onAvailabilityFormatChanged,
                                    onAvailabilityLocationChanged = viewModel::onAvailabilityLocationChanged,
                                    onAvailabilityMeetingUrlChanged = viewModel::onAvailabilityMeetingUrlChanged,
                                    onAvailabilityTopicChanged = viewModel::onAvailabilityTopicChanged,
                                    onAddAvailability = onAddAvailability,
                                    onBookTutorSlot = onBookTutorSlot,
                                    onStartBookingRequest = viewModel::startBookingRequest,
                                    onBookingRequestMessageChanged = viewModel::onBookingRequestMessageChanged,
                                    onBookingRequestTopicChanged = viewModel::onBookingRequestTopicChanged,
                                    onDeleteAvailability = onDeleteAvailability,
                                    onCancelBooking = onCancelBooking,
                                    onTutorSearchTutorChanged = viewModel::onTutorSearchTutorChanged,
                                    onTutorSearchSubjectChanged = viewModel::onTutorSearchSubjectChanged,
                                    onTutorSearchDateChanged = viewModel::onTutorSearchDateChanged,
                                    onTutorSearchFormatChanged = viewModel::onTutorSearchFormatChanged,
                                    onTutorSearchMinRatingChanged = viewModel::onTutorSearchMinRatingChanged,
                                    onTutorSearchAvailableTodayChanged = viewModel::onTutorSearchAvailableTodayChanged,
                                    onTutorSearchSortChanged = viewModel::onTutorSearchSortChanged,
                                    onClearTutorSearchFilters = viewModel::clearTutorSearchFilters
                                )
                            }
                            composable(DashboardRoutes.LESSONS) {
                                LessonsTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onNavigate = { navController.navigateToRoute(it) },
                                    onCancelBooking = onCancelBooking,
                                    onClearMessages = onClearMessages,
                                    onStartReschedule = viewModel::startReschedule,
                                    onReschedule = viewModel::rescheduleBooking,
                                    onClearReschedule = viewModel::clearReschedule
                                )
                            }
                            composable(DashboardRoutes.ASSIGNMENTS) {
                                AssignmentsTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onClearMessages = onClearMessages,
                                    onAssignmentTitleChanged = onAssignmentTitleChanged,
                                    onAssignmentDescriptionChanged = onAssignmentDescriptionChanged,
                                    onAssignmentSubjectChanged = onAssignmentSubjectChanged,
                                    onAssignmentDueDateChanged = onAssignmentDueDateChanged,
                                    onAssignmentCourseChanged = viewModel::onAssignmentCourseChanged,
                                    onAssignmentDueAtChanged = viewModel::onAssignmentDueAtChanged,
                                    onCreateAssignment = onCreateAssignment,
                                    onDeleteAssignment = onDeleteAssignment,
                                    onUploadSubmission = onUploadSubmission,
                                    onLoadSubmissionsForAssignment = onLoadSubmissionsForAssignment
                                )
                            }
                            composable(DashboardRoutes.CHAT) {
                                ChatTab(
                                    state = state,
                                    onOpenDirect = { viewModel.openDirectConversation(it) },
                                    onOpenDirectWith = viewModel::openDirectWith,
                                    onOpenCourse = { viewModel.openCourseConversation(it) },
                                    onManageCourses = { navController.navigateToRoute(DashboardRoutes.COURSES) },
                                    onSearchChanged = viewModel::onChatSearchChanged,
                                    onFilterChanged = viewModel::onChatInboxFilterChanged
                                )
                            }
                            composable(DashboardRoutes.COURSES) {
                                CoursesTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onCourseNameChanged = viewModel::onCourseNameChanged,
                                    onCourseSubjectChanged = viewModel::onCourseSubjectChanged,
                                    onCourseDescriptionChanged = viewModel::onCourseDescriptionChanged,
                                    onCreateCourse = viewModel::createCourse,
                                    onEnroll = viewModel::enrollInCourse,
                                    onLeave = viewModel::leaveCourse,
                                    onDeleteCourse = viewModel::deleteCourse,
                                    onOpenChat = { viewModel.openCourseConversation(it) },
                                    onLoadRoster = viewModel::loadRoster,
                                    onClearMessages = onClearMessages
                                )
                            }
                            composable(DashboardRoutes.REVIEWS) {
                                ReviewsTab(
                                    state = state,
                                    activeRole = activeRole,
                                    onClearMessages = onClearMessages,
                                    onReviewTutorChanged = onReviewTutorChanged,
                                    onReviewBookingChanged = onReviewBookingChanged,
                                    onReviewRatingChanged = onReviewRatingChanged,
                                    onReviewCommentChanged = onReviewCommentChanged,
                                    onSubmitReview = onSubmitReview,
                                    onReportTutorChanged = onReportTutorChanged,
                                    onReportReasonChanged = onReportReasonChanged,
                                    onReportDetailsChanged = onReportDetailsChanged,
                                    onSubmitReport = onSubmitReport,
                                    onToggleReviewTag = viewModel::toggleReviewTag,
                                    onSetReviewAnonymous = viewModel::setReviewAnonymous,
                                    onReportSeverityChanged = viewModel::onReportSeverityChanged
                                )
                            }
                            composable(DashboardRoutes.PROFILE) {
                                ProfileTab(
                                    currentUser = currentUser,
                                    activeRole = activeRole,
                                    state = state,
                                    onLogout = onLogout,
                                    onToggleRole = onToggleRole,
                                    onNavigate = { navController.navigateToRoute(it) }
                                )
                            }
                        }
                    }

                    SoftBottomNav(
                        currentRoute = currentRoute,
                        activeRole = activeRole,
                        chatUnreadCount = state.chat.directUnreadCount + state.dashboardState.courseCatalog
                            .filter { it.isEnrolled || it.isOwner }
                            .sumOf { it.unreadCount },
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onTabSelected = { tab -> navController.navigateToTab(tab) }
                    )
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
    onDeleteUser: (String) -> Unit,
    onDeleteAvailability: (String) -> Unit,
    onUpdateAvailability: (String, String, String, String, String) -> Unit,
    onUpdateReportStatus: (String, String) -> Unit,
    onClearMessages: () -> Unit
) {
    var editingSlot by remember { mutableStateOf<TutorAvailabilityUi?>(null) }
    var reportFilter by remember { mutableStateOf("Open") }
    var userQuery by remember { mutableStateOf("") }
    var userRoleFilter by remember { mutableStateOf("All") }
    var showCreateUser by remember { mutableStateOf(false) }

    editingSlot?.let { slot ->
        EditSlotDialog(
            slot = slot,
            onDismiss = { editingSlot = null },
            onConfirm = { subject, date, start, end ->
                onUpdateAvailability(slot.id, subject, date, start, end)
                editingSlot = null
            }
        )
    }

    // Derive stat counts from adminReports
    val openCount = state.dashboardState.adminReports.count { it.status == "open" }
    val inReviewCount = state.dashboardState.adminReports.count { it.status == "in-review" || it.status == "in_review" }
    val resolvedCount = state.dashboardState.adminReports.count { it.status == "resolved" || it.status == "dismissed" }

    // Filtered reports
    val visibleReports = state.dashboardState.adminReports.filter { report ->
        when (reportFilter) {
            "Open"      -> report.status == "open"
            "In review" -> report.status == "in-review" || report.status == "in_review"
            "Resolved"  -> report.status == "resolved" || report.status == "dismissed"
            else        -> true
        }
    }

    // Filtered users
    val visibleUsers = state.adminUsers.filter { user ->
        val matchesQuery = userQuery.isBlank() ||
            user.displayName.contains(userQuery, ignoreCase = true) ||
            user.login.contains(userQuery, ignoreCase = true)
        val matchesRole = when (userRoleFilter) {
            "Students" -> user.hasRole(UserRole.STUDENT)
            "Tutors"   -> user.hasRole(UserRole.TUTOR)
            "Admins"   -> user.hasRole(UserRole.ADMIN)
            else       -> true
        }
        matchesQuery && matchesRole
    }

    MainList {
        // ─── MODERATION TOP BAR ─────────────────────────────────────────────
        SoftTopBar(
            leading = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(InkToken, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(SoftIcons.shield, contentDescription = null, tint = White, modifier = Modifier.size(19.dp))
                }
            },
            title = "Moderation",
            subtitle = "Smart Campus PWR · admin",
            actions = {
                SoftIconButton(icon = SoftIcons.filter, onClick = { /* filter — DESIGN-PLACEHOLDER: future filter sheet */ })
                SoftIconButton(
                    icon = SoftIcons.repeat,
                    onClick = { onClearMessages(); onRefresh() }
                )
            }
        )

        MessageBlock(state.errorMessage, state.infoMessage)

        // ─── STAT BAND ──────────────────────────────────────────────────────
        CardQ(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    Triple(openCount.toString(),     "Open",      Red),
                    Triple(inReviewCount.toString(), "In review", Amber),
                    Triple(resolvedCount.toString(), "Resolved",  Green)
                ).forEachIndexed { idx, (count, label, color) ->
                    if (idx > 0) {
                        Box(modifier = Modifier.width(1.dp).height(48.dp).background(Line))
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(count, style = SoftType.h2, color = color)
                        Spacer(Modifier.height(2.dp))
                        Text(label, style = SoftType.meta)
                    }
                }
            }
        }

        // ─── FILTER CHIPS ───────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Open", "In review", "Resolved").forEach { filter ->
                Chip(
                    text = filter,
                    selected = reportFilter == filter,
                    onClick = { reportFilter = filter }
                )
            }
        }

        // ─── REPORT CARDS ───────────────────────────────────────────────────
        if (visibleReports.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No reports in this category.",
                    style = SoftType.body,
                    color = Ink3
                )
            }
        } else {
            visibleReports.forEach { report ->
                ReportCard(
                    report = report,
                    onStatusChange = onUpdateReportStatus
                )
            }
        }

        // ─── ALL TUTORING SLOTS ─────────────────────────────────────────────
        SectionHead(title = "Tutoring slots", action = if (state.dashboardState.availableTutorSlots.isEmpty()) null else "${state.dashboardState.availableTutorSlots.size}")
        if (state.dashboardState.availableTutorSlots.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text("No slots found.", style = SoftType.body, color = Ink3)
            }
        } else {
            state.dashboardState.availableTutorSlots.forEach { slot ->
                AvailabilityCard(
                    slot = slot,
                    onBook = null,
                    onDelete = onDeleteAvailability,
                    onEdit = { editingSlot = it }
                )
            }
        }

        // ─── USERS TOP BAR ──────────────────────────────────────────────────
        SoftTopBar(
            modifier = Modifier.padding(top = 8.dp),
            leading = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(InkToken, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(SoftIcons.user, contentDescription = null, tint = White, modifier = Modifier.size(19.dp))
                }
            },
            title = "Users",
            subtitle = "${state.adminUsers.size} accounts",
            actions = {
                SoftIconButton(
                    icon = SoftIcons.plus,
                    prim = true,
                    onClick = { showCreateUser = !showCreateUser }
                )
            }
        )

        // ─── USER SEARCH + ROLE FILTERS ─────────────────────────────────────
        SearchField(
            value = userQuery,
            onValueChange = { userQuery = it },
            placeholder = "Search by name or login…",
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Students", "Tutors", "Admins").forEach { role ->
                Chip(
                    text = role,
                    selected = userRoleFilter == role,
                    onClick = { userRoleFilter = role }
                )
            }
        }

        // ─── USER LIST ──────────────────────────────────────────────────────
        if (visibleUsers.isEmpty()) {
            CardQ(modifier = Modifier.fillMaxWidth()) {
                Text("No users match.", style = SoftType.body, color = Ink3)
            }
        } else {
            CardQ(modifier = Modifier.fillMaxWidth(), padding = 6.dp) {
                visibleUsers.forEachIndexed { idx, user ->
                    AdminUserCard(
                        user = user,
                        inspector = state.adminInspectors[user.uid],
                        onUpdateUserRoles = onUpdateUserRoles,
                        onToggleInspector = onToggleInspector,
                        onDeleteUser = onDeleteUser
                    )
                    if (idx < visibleUsers.lastIndex) {
                        SoftDivider()
                    }
                }
            }
        }

        // ─── CREATE USER FORM (collapsible) ─────────────────────────────────
        if (showCreateUser) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                SectionHead(title = "Create new user")
                Spacer(Modifier.height(12.dp))
                SoftTextField(
                    value = state.createUserForm.login,
                    onValueChange = onCreateLoginChanged,
                    label = "Login",
                    placeholder = "login",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                SoftTextField(
                    value = state.createUserForm.password,
                    onValueChange = onCreatePasswordChanged,
                    label = "Password",
                    placeholder = "••••••••",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                SoftTextField(
                    value = state.createUserForm.displayName,
                    onValueChange = onCreateDisplayNameChanged,
                    label = "Display name",
                    placeholder = "Full name",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleCheck("Admin",   state.createUserForm.admin,   onCreateAdminChecked)
                    RoleCheck("Student", state.createUserForm.student, onCreateStudentChecked)
                    RoleCheck("Tutor",   state.createUserForm.tutor,   onCreateTutorChecked)
                }
                Spacer(Modifier.height(12.dp))
                SoftButton(
                    text = "Create user",
                    onClick = { onClearMessages(); onCreateUserClick() },
                    leadingIcon = SoftIcons.plus,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ─── BOTTOM CREATE BUTTON ────────────────────────────────────────────
        SoftButton(
            text = "Create new user",
            onClick = { showCreateUser = !showCreateUser },
            leadingIcon = SoftIcons.plus,
            variant = if (showCreateUser) SoftButtonVariant.Soft else SoftButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        SoftButton(
            text = "Sign out",
            onClick = onLogout,
            variant = SoftButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun NavHostController.navigateToTab(tab: DashboardTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToRoute(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotDialog(
    slot: TutorAvailabilityUi,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf(slot.subject) }
    var date by remember { mutableStateOf(slot.dateLabel) }
    var startHour by remember { mutableStateOf(slot.startHour) }
    var endHour by remember { mutableStateOf(slot.endHour) }

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

    val startParts = startHour.split(":")
    val initialStartHour = startParts.getOrNull(0)?.toIntOrNull() ?: 12
    val initialStartMinute = startParts.getOrNull(1)?.toIntOrNull() ?: 0
    val startTimeState = rememberTimePickerState(initialHour = initialStartHour, initialMinute = initialStartMinute)

    val endParts = endHour.split(":")
    val initialEndHour = endParts.getOrNull(0)?.toIntOrNull() ?: 13
    val initialEndMinute = endParts.getOrNull(1)?.toIntOrNull() ?: 0
    val endTimeState = rememberTimePickerState(initialHour = initialEndHour, initialMinute = initialEndMinute)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        date = formatter.format(Date(millis))
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
                    startHour = String.format("%02d:%02d", startTimeState.hour, startTimeState.minute)
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
                    endHour = String.format("%02d:%02d", endTimeState.hour, endTimeState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            }
        ) { TimePicker(state = endTimeState) }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppSurface,
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonoLabel("Edit slot")
                Text(slot.tutorDisplayName, color = InkTextSoft, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(subject, { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(date, { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(startHour, { startHour = it }, label = { Text("Start") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(endHour, { endHour = it }, label = { Text("End") }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = PwrRed) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(subject, date, startHour, endHour) }) { Text("Save") }
                }
            }
        }
    }
}
