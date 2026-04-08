package Smart.Campus.PWR.ui

import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrNavyDark
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SmartCampusApp(viewModel: SmartCampusViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isBootstrapping) {
        LoadingScreen()
        return
    }

    when (state.screen) {
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
                    onReviewRatingChanged = viewModel::onReviewRatingChanged,
                    onReviewCommentChanged = viewModel::onReviewCommentChanged,
                    onSubmitReview = viewModel::submitReview,
                    onReportTutorChanged = viewModel::onReportTutorChanged,
                    onReportReasonChanged = viewModel::onReportReasonChanged,
                    onReportDetailsChanged = viewModel::onReportDetailsChanged,
                    onSubmitReport = viewModel::submitReport,
                    onClearMessages = viewModel::clearMessages
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
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = PwrNavy)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Loading Smart Campus PWR...", color = Color.White)
                }
            }
        }
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
    Scaffold(containerColor = AppBackground) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Brush.verticalGradient(listOf(PwrNavy, PwrNavyDark)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Smart Campus Tutors",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Sign in", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                        Text("Use your login alias or full email.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                        OutlinedTextField(
                            value = state.loginInput,
                            onValueChange = {
                                onClearMessages()
                                onLoginChanged(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Login or email") },
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

                        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy) {
                            Text(if (state.isBusy) "Signing in..." else "Sign in")
                        }

                        OutlinedButton(onClick = onOpenRegister, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy) {
                            Text("Create account")
                        }
                    }
                }
            }
        }
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

    Scaffold(containerColor = AppBackground) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(listOf(PwrNavy, PwrNavyDark)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Create account",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                        RoleCheckbox(label = "I am a student", checked = form.student, onCheckedChange = onRegisterStudentChecked)
                        RoleCheckbox(label = "I am a tutor", checked = form.tutor, onCheckedChange = onRegisterTutorChecked)

                        MessageBanner(state)

                        Button(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy) {
                            Text(if (state.isBusy) "Creating account..." else "Register")
                        }

                        OutlinedButton(onClick = onOpenLogin, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy) {
                            Text("Back to sign in")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(color = AppBackground, shape = RoundedCornerShape(14.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(label)
        }
    }
}

@Composable
private fun MessageBanner(state: SmartCampusUiState) {
    if (state.errorMessage != null) {
        Surface(color = PwrRed.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp)) {
            Text(state.errorMessage, color = PwrRed, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }

    if (state.infoMessage != null) {
        Surface(color = PwrBlueSoft, shape = RoundedCornerShape(18.dp)) {
            Text(state.infoMessage, color = PwrNavy, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}