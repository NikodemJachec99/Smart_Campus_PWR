package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.AppSurfaceMuted
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            onClearMessages = viewModel::clearMessages
        )

        AppScreen.ROLE_PICKER -> RolePickerScreen(
            state = state,
            onStudentClick = { viewModel.selectRole(UserRole.STUDENT) },
            onLecturerClick = { viewModel.selectRole(UserRole.LECTURER) },
            onLogout = viewModel::logout
        )

        AppScreen.MAIN_SHELL -> {
            val currentUser = state.currentUser
            val activeRole = state.activeRole
            if (currentUser != null && activeRole != null) {
                MainShellScreen(
                    state = state.dashboardState,
                    currentUser = currentUser,
                    activeRole = activeRole,
                    onRefreshDashboard = viewModel::refreshDashboard,
                    onLogout = viewModel::logout,
                    onOpenRolePicker = viewModel::openRolePicker
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
            onCreateLecturerChecked = viewModel::onCreateLecturerChecked,
            onCreateUserClick = viewModel::createUserByAdmin,
            onUpdateUserRoles = viewModel::updateUserRolesByAdmin,
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
                    .height(260.dp)
                    .background(Brush.verticalGradient(listOf(PwrNavy, PwrNavyDark)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Smart Campus PWR", color = Color.White, style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(bottom = 20.dp))
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Logowanie", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                        Text("Sign in with an alias or email to access the PWr panel.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

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

                        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth(), enabled = !state.isBusy) {
                            Text(if (state.isBusy) "Signing in..." else "Sign in")
                        }

                        Text("Test admin: login admin / password admin123", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RolePickerScreen(
    state: SmartCampusUiState,
    onStudentClick: () -> Unit,
    onLecturerClick: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = PwrNavy)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose active role", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text(state.currentUser?.displayName.orEmpty(), color = PwrBlueSoft)
                }
            }

            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface), modifier = Modifier.padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onStudentClick, modifier = Modifier.fillMaxWidth()) { Text("Continue as Student") }
                    Button(onClick = onLecturerClick, modifier = Modifier.fillMaxWidth()) { Text("Continue as Lecturer") }
                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
                }
            }
        }
    }
}

@Composable
fun AdminPanelScreen(
    state: SmartCampusUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateLoginChanged: (String) -> Unit,
    onCreatePasswordChanged: (String) -> Unit,
    onCreateDisplayNameChanged: (String) -> Unit,
    onCreateAdminChecked: (Boolean) -> Unit,
    onCreateStudentChecked: (Boolean) -> Unit,
    onCreateLecturerChecked: (Boolean) -> Unit,
    onCreateUserClick: () -> Unit,
    onUpdateUserRoles: (String, Boolean, Boolean) -> Unit,
    onClearMessages: () -> Unit
) {
    Scaffold(containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = PwrNavy)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Admin Panel", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        Text(state.currentUser?.displayName.orEmpty(), color = PwrBlueSoft)
                        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = onRefresh, enabled = !state.isAdminUsersLoading && !state.isAdminSubmitting) {
                                Text(if (state.isAdminUsersLoading) "Refreshanie..." else "Refresh")
                            }
                            OutlinedButton(onClick = onLogout) { Text("Sign out") }
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Surface(color = PwrRed.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp)) {
                        Text(state.errorMessage, color = PwrRed, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            if (state.infoMessage != null) {
                item {
                    Surface(color = PwrBlueSoft, shape = RoundedCornerShape(18.dp)) {
                        Text(state.infoMessage, color = PwrNavy, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            item {
                CreateUserCard(
                    form = state.createUserForm,
                    isSubmitting = state.isAdminSubmitting,
                    onCreateLoginChanged = {
                        onClearMessages()
                        onCreateLoginChanged(it)
                    },
                    onCreatePasswordChanged = {
                        onClearMessages()
                        onCreatePasswordChanged(it)
                    },
                    onCreateDisplayNameChanged = {
                        onClearMessages()
                        onCreateDisplayNameChanged(it)
                    },
                    onCreateAdminChecked = {
                        onClearMessages()
                        onCreateAdminChecked(it)
                    },
                    onCreateStudentChecked = {
                        onClearMessages()
                        onCreateStudentChecked(it)
                    },
                    onCreateLecturerChecked = {
                        onClearMessages()
                        onCreateLecturerChecked(it)
                    },
                    onCreateUserClick = onCreateUserClick
                )
            }

            item {
                Text("Users", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (state.adminUsers.isEmpty() && !state.isAdminUsersLoading) {
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
                        Text("No users.", modifier = Modifier.padding(16.dp), color = TextSecondary)
                    }
                }
            }

            items(state.adminUsers, key = { it.uid }) { listedUser ->
                UserRow(user = listedUser, isSubmitting = state.isAdminSubmitting, onUpdateUserRoles = onUpdateUserRoles)
            }
        }
    }
}

@Composable
private fun CreateUserCard(
    form: CreateUserFormState,
    isSubmitting: Boolean,
    onCreateLoginChanged: (String) -> Unit,
    onCreatePasswordChanged: (String) -> Unit,
    onCreateDisplayNameChanged: (String) -> Unit,
    onCreateAdminChecked: (Boolean) -> Unit,
    onCreateStudentChecked: (Boolean) -> Unit,
    onCreateLecturerChecked: (Boolean) -> Unit,
    onCreateUserClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add new user", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Password must be at least 6 characters.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            OutlinedTextField(value = form.login, onValueChange = onCreateLoginChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Login") }, singleLine = true, enabled = !isSubmitting)
            OutlinedTextField(value = form.password, onValueChange = onCreatePasswordChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !isSubmitting)
            OutlinedTextField(value = form.displayName, onValueChange = onCreateDisplayNameChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Display name") }, singleLine = true, enabled = !isSubmitting)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleCheckboxRow("Admin", form.admin, onCreateAdminChecked, isSubmitting)
                RoleCheckboxRow("Student", form.student, onCreateStudentChecked, isSubmitting)
                RoleCheckboxRow("Lecturer", form.lecturer, onCreateLecturerChecked, isSubmitting)
            }

            Button(onClick = onCreateUserClick, modifier = Modifier.fillMaxWidth(), enabled = !isSubmitting) {
                Text(if (isSubmitting) "Creating..." else "Add user")
            }
        }
    }
}

@Composable
private fun RoleCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, isSubmitting: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = !isSubmitting)
        Text(label)
    }
}

@Composable
private fun UserRow(
    user: AppUser,
    isSubmitting: Boolean,
    onUpdateUserRoles: (String, Boolean, Boolean) -> Unit
) {
    var student by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.STUDENT)) }
    var lecturer by remember(user.uid, user.roles) { mutableStateOf(user.hasRole(UserRole.LECTURER)) }

    val isAdminUser = user.hasRole(UserRole.ADMIN)
    val changed = student != user.hasRole(UserRole.STUDENT) || lecturer != user.hasRole(UserRole.LECTURER)
    val rolesLabel = user.roles.joinToString { it.displayName }

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(user.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${user.login} | ${user.email}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Surface(color = AppSurfaceMuted, shape = RoundedCornerShape(12.dp)) {
                Text("Role: $rolesLabel", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.bodySmall)
            }

            if (isAdminUser) {
                Text("Admin role cannot be edited from this panel.", color = PwrNavy, style = MaterialTheme.typography.bodySmall)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = student, onCheckedChange = { student = it }, enabled = !isSubmitting)
                    Text("Student")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = lecturer, onCheckedChange = { lecturer = it }, enabled = !isSubmitting)
                    Text("Lecturer")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { onUpdateUserRoles(user.uid, student, lecturer) }, enabled = !isSubmitting && changed && (student || lecturer)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}



