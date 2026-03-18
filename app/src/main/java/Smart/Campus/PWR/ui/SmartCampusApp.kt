package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.CreateUserFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        AppScreen.ROLE_HOME -> RoleHomeScreen(
            state = state,
            onSwitchRole = viewModel::openRolePicker,
            onLogout = viewModel::logout
        )

        AppScreen.ADMIN_PANEL -> AdminPanelScreen(
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
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Ladowanie sesji...", modifier = Modifier.padding(top = 12.dp))
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
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smart Campus PWR",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Logowanie (alias + haslo)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = state.loginInput,
                onValueChange = {
                    onClearMessages()
                    onLoginChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Login lub email") },
                singleLine = true
            )

            OutlinedTextField(
                value = state.passwordInput,
                onValueChange = {
                    onClearMessages()
                    onPasswordChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Haslo") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (state.infoMessage != null) {
                Text(
                    text = state.infoMessage,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !state.isBusy
            ) {
                Text(if (state.isBusy) "Logowanie..." else "Zaloguj")
            }

            Text(
                text = "Test admin: login admin / haslo admin123",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp)
            )
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
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wybierz aktywna role", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(state.currentUser?.displayName.orEmpty(), modifier = Modifier.padding(top = 6.dp, bottom = 20.dp))

            Button(onClick = onStudentClick, modifier = Modifier.fillMaxWidth()) {
                Text("Kontynuuj jako Student")
            }

            Button(
                onClick = onLecturerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Kontynuuj jako Wykladowca")
            }

            OutlinedButton(onClick = onLogout, modifier = Modifier.padding(top = 20.dp)) {
                Text("Wyloguj")
            }
        }
    }
}

@Composable
private fun RoleHomeScreen(
    state: SmartCampusUiState,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val dualRole = state.currentUser?.hasDualRole() == true

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Zalogowano jako", style = MaterialTheme.typography.titleMedium)
            Text(
                state.activeRole?.displayName ?: "Brak roli",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )
            Text("To jest placeholder ekranu dla wybranej roli.")

            if (dualRole) {
                OutlinedButton(onClick = onSwitchRole, modifier = Modifier.padding(top = 20.dp)) {
                    Text("Przelacz role")
                }
            }

            OutlinedButton(onClick = onLogout, modifier = Modifier.padding(top = 12.dp)) {
                Text("Wyloguj")
            }
        }
    }
}

@Composable
private fun AdminPanelScreen(
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
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text("Panel Admin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text("${state.currentUser?.displayName.orEmpty()} (${state.currentUser?.login.orEmpty()})")

            Row(modifier = Modifier.padding(top = 12.dp)) {
                Button(onClick = onRefresh, enabled = !state.isAdminUsersLoading && !state.isAdminSubmitting) {
                    Text(if (state.isAdminUsersLoading) "Odswiezanie..." else "Odswiez")
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onLogout) {
                    Text("Wyloguj")
                }
            }

            if (state.errorMessage != null) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
            }

            if (state.infoMessage != null) {
                Text(state.infoMessage, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Text("Uzytkownicy", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

            if (state.adminUsers.isEmpty() && !state.isAdminUsersLoading) {
                Text("Brak uzytkownikow.")
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.adminUsers, key = { it.uid }) { listedUser ->
                    UserRow(
                        user = listedUser,
                        isSubmitting = state.isAdminSubmitting,
                        onUpdateUserRoles = onUpdateUserRoles
                    )
                }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Dodaj nowego uzytkownika", fontWeight = FontWeight.Bold)
            Text(
                "Haslo musi miec co najmniej 6 znakow.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            OutlinedTextField(
                value = form.login,
                onValueChange = onCreateLoginChanged,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Login") },
                singleLine = true,
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = form.password,
                onValueChange = onCreatePasswordChanged,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Haslo") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = form.displayName,
                onValueChange = onCreateDisplayNameChanged,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Display name") },
                singleLine = true,
                enabled = !isSubmitting
            )

            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = form.admin, onCheckedChange = onCreateAdminChecked, enabled = !isSubmitting)
                Text("Admin")
                Spacer(Modifier.width(12.dp))
                Checkbox(checked = form.student, onCheckedChange = onCreateStudentChecked, enabled = !isSubmitting)
                Text("Student")
                Spacer(Modifier.width(12.dp))
                Checkbox(checked = form.lecturer, onCheckedChange = onCreateLecturerChecked, enabled = !isSubmitting)
                Text("Lecturer")
            }

            Button(
                onClick = onCreateUserClick,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "Tworzenie..." else "Dodaj uzytkownika")
            }
        }
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(user.displayName, fontWeight = FontWeight.Bold)
            Text("${user.login} | ${user.email}", style = MaterialTheme.typography.bodySmall)
            Text("Role: $rolesLabel", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))

            if (isAdminUser) {
                Text("Rola admin nie jest edytowalna z panelu.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            } else {
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = student, onCheckedChange = { student = it }, enabled = !isSubmitting)
                    Text("Student")
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = lecturer, onCheckedChange = { lecturer = it }, enabled = !isSubmitting)
                    Text("Lecturer")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onUpdateUserRoles(user.uid, student, lecturer) },
                        enabled = !isSubmitting && changed && (student || lecturer)
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        }
    }
}
