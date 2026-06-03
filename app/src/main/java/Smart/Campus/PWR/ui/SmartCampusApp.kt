package Smart.Campus.PWR.ui

import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.AppPrimaryButton
import Smart.Campus.PWR.ui.components.EditorialCard
import Smart.Campus.PWR.ui.components.EditorialScreen
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.Pill
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.CardWhite
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.DisplayFontFamily
import Smart.Campus.PWR.ui.theme.ForestAccent
import Smart.Campus.PWR.ui.theme.InkPrimary
import Smart.Campus.PWR.ui.theme.InkSecondary
import Smart.Campus.PWR.ui.theme.InkTertiary
import Smart.Campus.PWR.ui.theme.MonoFontFamily
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SmartCampusApp(viewModel: SmartCampusViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isBootstrapping) {
        LoadingScreen()
        return
    }

    when (state.screen) {
        AppScreen.ONBOARDING -> OnboardingScreen(
            state = state,
            onGetStarted = viewModel::openRegister,
            onSignIn = viewModel::openLogin
        )

        AppScreen.LOGIN -> LoginScreen(
            state = state,
            onLoginChanged = viewModel::onLoginChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLoginClick = viewModel::login,
            onOpenRegister = viewModel::openRegister,
            onClearMessages = viewModel::clearMessages
        )

        AppScreen.REGISTER -> RegisterScreen(
            state = state,
            onRegisterLoginChanged = viewModel::onRegisterLoginChanged,
            onRegisterPasswordChanged = viewModel::onRegisterPasswordChanged,
            onRegisterDisplayNameChanged = viewModel::onRegisterDisplayNameChanged,
            onRegisterStudentChecked = viewModel::onRegisterStudentChecked,
            onRegisterTutorChecked = viewModel::onRegisterTutorChecked,
            onRegisterClick = viewModel::register,
            onOpenLogin = viewModel::openLogin,
            onClearMessages = viewModel::clearMessages
        )

        AppScreen.MAIN_SHELL -> {
            val currentUser = state.currentUser
            val activeRole = state.activeRole
            if (currentUser != null && activeRole != null) {
                MainShellScreen(
                    state = state,
                    viewModel = viewModel,
                    onToggleRole = viewModel::toggleActiveRole,
                    onRefreshDashboard = viewModel::refreshMainData,
                    onLogout = viewModel::logout,
                    onAvailabilitySubjectChanged = viewModel::onAvailabilitySubjectChanged,
                    onAvailabilityDateChanged = viewModel::onAvailabilityDateChanged,
                    onAvailabilityStartHourChanged = viewModel::onAvailabilityStartHourChanged,
                    onAvailabilityEndHourChanged = viewModel::onAvailabilityEndHourChanged,
                    onAddAvailability = viewModel::addAvailability,
                    onBookTutorSlot = viewModel::bookTutorSlot,
                    onReviewTutorChanged = viewModel::onReviewTutorChanged,
                    onReviewBookingChanged = viewModel::onReviewBookingChanged,
                    onReviewRatingChanged = viewModel::onReviewRatingChanged,
                    onReviewCommentChanged = viewModel::onReviewCommentChanged,
                    onSubmitReview = viewModel::submitReview,
                    onReportTutorChanged = viewModel::onReportTutorChanged,
                    onReportReasonChanged = viewModel::onReportReasonChanged,
                    onReportDetailsChanged = viewModel::onReportDetailsChanged,
                    onSubmitReport = viewModel::submitReport,
                    onClearMessages = viewModel::clearMessages,
                    onDeleteAvailability = viewModel::deleteAvailabilitySlot,
                    onUpdateAvailability = viewModel::updateAvailabilitySlot,
                    onCancelBooking = viewModel::cancelLessonBooking,
                    onAssignmentTitleChanged = viewModel::onAssignmentTitleChanged,
                    onAssignmentDescriptionChanged = viewModel::onAssignmentDescriptionChanged,
                    onAssignmentSubjectChanged = viewModel::onAssignmentSubjectChanged,
                    onAssignmentDueDateChanged = viewModel::onAssignmentDueDateChanged,
                    onCreateAssignment = viewModel::createAssignment,
                    onDeleteAssignment = viewModel::deleteAssignment,
                    onUploadSubmission = viewModel::uploadSubmission,
                    onLoadSubmissionsForAssignment = viewModel::loadSubmissionsForAssignment
                )
            } else {
                LoadingScreen()
            }
        }

        AppScreen.ADMIN_PANEL -> AdminShellScreen(
            state = state,
            onRefresh = viewModel::refreshAdminUsers,
            onLogout = viewModel::logout,
            onCreateLoginChanged = viewModel::onCreateLoginChanged,
            onCreatePasswordChanged = viewModel::onCreatePasswordChanged,
            onCreateDisplayNameChanged = viewModel::onCreateDisplayNameChanged,
            onCreateAdminChecked = viewModel::onCreateAdminChecked,
            onCreateStudentChecked = viewModel::onCreateStudentChecked,
            onCreateTutorChecked = viewModel::onCreateTutorChecked,
            onCreateUserClick = viewModel::createUserByAdmin,
            onUpdateUserRoles = viewModel::updateUserRolesByAdmin,
            onToggleInspector = viewModel::toggleAdminInspector,
            onDeleteUser = viewModel::deleteUserByAdmin,
            onDeleteAvailability = viewModel::deleteAvailabilitySlot,
            onUpdateAvailability = viewModel::updateAvailabilitySlot,
            onUpdateReportStatus = viewModel::updateReportStatusByAdmin,
            onClearMessages = viewModel::clearMessages
        )
    }
}

@Composable
private fun LoadingScreen() {
    Scaffold(containerColor = AppBackground) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            EditorialCard(modifier = Modifier.padding(horizontal = 24.dp)) {
                CircularProgressIndicator(color = ForestAccent)
                Text("Loading Smart Campus PWR...", color = InkPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun OnboardingScreen(
    state: SmartCampusUiState,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    EditorialScreen {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(368.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(ForestAccent, ForestAccent.copy(alpha = 0.92f))),
                    shape = RoundedCornerShape(topStart = 200.dp, topEnd = 200.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonoLabel("PWR TUTORING", color = Color.White.copy(alpha = 0.72f), fontSize = 9.sp, letterSpacing = 2.2.sp)
                MonoLabel("2026", color = Color.White.copy(alpha = 0.72f), fontSize = 9.sp, letterSpacing = 2.2.sp)
            }
            Text(
                "alpha",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp),
                color = Color.White.copy(alpha = 0.13f),
                fontFamily = DisplayFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 76.sp
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonoLabel("A new chapter", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp, letterSpacing = 1.8.sp)
                Text(
                    "Learn from\nsomeone who\njust aced it.",
                    color = Color.White,
                    fontFamily = DisplayFontFamily,
                    fontSize = 39.sp,
                    lineHeight = 38.sp,
                    letterSpacing = 0.sp
                )
            }
        }

        Text(
            "A small, vetted circle of PWr students and lecturers who can sit down with you and walk through what you missed: calculus, thermo, OOP, anything.",
            color = InkSecondary,
            style = MaterialTheme.typography.bodyLarge
        )

        MessageBanner(state)

        AppPrimaryButton(
            text = "Get started",
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = CardWhite, modifier = Modifier.size(16.dp)) }
        )
        AppDangerButton(
            text = "Have an account? Sign in",
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoginScreen(
    state: SmartCampusUiState,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onOpenRegister: () -> Unit,
    onClearMessages: () -> Unit
) {
    AuthScaffold(
        eyebrow = "Welcome back",
        title = "The good kind\nof overtime.",
        subtitle = "Pick up where the lecture left off."
    ) {
        OutlinedTextField(
            value = state.loginInput,
            onValueChange = {
                onClearMessages()
                onLoginChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("PWr login or email") },
            singleLine = true
        )
        OutlinedTextField(
            value = state.passwordInput,
            onValueChange = {
                onClearMessages()
                onPasswordChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        MessageBanner(state)

        AppPrimaryButton(
            text = if (state.isBusy) "Signing in..." else "Sign in",
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy,
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = CardWhite, modifier = Modifier.size(16.dp)) }
        )

        AppDangerButton("Create account", onClick = onOpenRegister, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy)
    }
}

@Composable
private fun RegisterScreen(
    state: SmartCampusUiState,
    onRegisterLoginChanged: (String) -> Unit,
    onRegisterPasswordChanged: (String) -> Unit,
    onRegisterDisplayNameChanged: (String) -> Unit,
    onRegisterStudentChecked: (Boolean) -> Unit,
    onRegisterTutorChecked: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onOpenLogin: () -> Unit,
    onClearMessages: () -> Unit
) {
    val form: RegisterFormState = state.registerForm

    AuthScaffold(
        eyebrow = "Request access",
        title = "Join the\nstudy circle.",
        subtitle = "Create a tutoring account with a student, tutor, or dual role."
    ) {
        OutlinedTextField(
            value = form.login,
            onValueChange = {
                onClearMessages()
                onRegisterLoginChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Login") },
            singleLine = true
        )
        OutlinedTextField(
            value = form.password,
            onValueChange = {
                onClearMessages()
                onRegisterPasswordChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password (min 6 chars)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = form.displayName,
            onValueChange = {
                onClearMessages()
                onRegisterDisplayNameChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            singleLine = true
        )

        EditorialCard {
            MonoLabel("Roles")
            RoleCheckbox(label = "I am a student", checked = form.student, onCheckedChange = onRegisterStudentChecked)
            RoleCheckbox(label = "I am a tutor", checked = form.tutor, onCheckedChange = onRegisterTutorChecked)
        }

        MessageBanner(state)

        AppPrimaryButton(
            text = if (state.isBusy) "Creating account..." else "Register",
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy,
            leadingIcon = { Icon(Icons.Rounded.Check, contentDescription = null, tint = CardWhite, modifier = Modifier.size(16.dp)) }
        )

        AppDangerButton("Back to sign in", onClick = onOpenLogin, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy)
    }
}

@Composable
private fun AuthScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    EditorialScreen {
        Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(width = 24.dp, height = 1.dp).background(InkPrimary))
                MonoLabel(eyebrow, color = InkSecondary, letterSpacing = 2.2.sp)
            }
            Text(
                title,
                color = InkPrimary,
                fontFamily = DisplayFontFamily,
                fontSize = 44.sp,
                lineHeight = 43.sp,
                letterSpacing = 0.sp
            )
            Text(subtitle, color = InkSecondary, style = MaterialTheme.typography.bodyLarge)
        }

        EditorialCard {
            content()
        }
    }
}

@Composable
private fun RoleCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground, RoundedCornerShape(14.dp))
            .border(1.dp, PaperLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, color = InkPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MessageBanner(state: SmartCampusUiState) {
    if (state.errorMessage != null) {
        Surface(color = PwrRed.copy(alpha = 0.10f), shape = RoundedCornerShape(18.dp)) {
            Text(state.errorMessage, color = PwrRed, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }

    if (state.infoMessage != null) {
        Surface(color = PwrBlueSoft, shape = RoundedCornerShape(18.dp)) {
            Text(state.infoMessage, color = PwrNavy, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(PaperLine))
        Text(
            text.uppercase(),
            color = InkTertiary,
            fontFamily = MonoFontFamily,
            fontSize = 9.sp,
            letterSpacing = 2.2.sp,
            textAlign = TextAlign.Center
        )
        Box(Modifier.weight(1f).height(1.dp).background(PaperLine))
    }
}
