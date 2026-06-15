package Smart.Campus.PWR.ui

import Smart.Campus.PWR.ui.components.EditorialCard
import Smart.Campus.PWR.ui.components.EditorialScreen
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.softindigo.Badge
import Smart.Campus.PWR.ui.components.softindigo.BadgeTone
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTextField
import Smart.Campus.PWR.ui.components.softindigo.cardShadow
import Smart.Campus.PWR.ui.components.softindigo.primShadow
import Smart.Campus.PWR.ui.components.softindigo.raiseShadow
import Smart.Campus.PWR.ui.components.softindigo.softShadow
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.AppScreen
import Smart.Campus.PWR.ui.state.RegisterFormState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.CardSurface
import Smart.Campus.PWR.ui.theme.ClayAccent
import Smart.Campus.PWR.ui.theme.DisplayFontFamily
import Smart.Campus.PWR.ui.theme.ForestAccent
import Smart.Campus.PWR.ui.theme.Green
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.InkPrimary
import Smart.Campus.PWR.ui.theme.InkSecondary
import Smart.Campus.PWR.ui.theme.InkTertiary
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Line
import Smart.Campus.PWR.ui.theme.Line2
import Smart.Campus.PWR.ui.theme.MonoFontFamily
import Smart.Campus.PWR.ui.theme.PaperLine
import Smart.Campus.PWR.ui.theme.Primary
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.Red
import Smart.Campus.PWR.ui.theme.SoftType
import Smart.Campus.PWR.ui.theme.Star
import Smart.Campus.PWR.ui.theme.White
import Smart.Campus.PWR.ui.theme.BodyFontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
    val heroGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF6D5DF2),
            0.48f to Color(0xFF5B4DF0),
            1.00f to Color(0xFF4A3DD6)
        )
    )
    val heroShape = RoundedCornerShape(26.dp)

    val tutors = listOf(
        Triple("Marta Lewandowska", "Calculus II",      "45 zł"),
        Triple("Olivia Kowal",      "OOP · Java",       "50 zł"),
        Triple("Jakub Wójcik",      "Thermodynamics",   "60 zł")
    )
    val tutorColorIndices = listOf(0, 2, 1)
    val tutorOffsets = listOf(0.dp, 26.dp, 12.dp)

    Scaffold(containerColor = Bg) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SoftIcons.cap,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        "Korepetycje PWr",
                        style = SoftType.title.copy(fontSize = 14.sp, color = InkToken)
                    )
                }
                Text(
                    "Skip",
                    style = SoftType.meta,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSignIn() }
                )
            }

            // ── Hero card ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(330.dp)
                    .raiseShadow(heroShape)
                    .clip(heroShape)
                    .background(brush = heroGradient)
            ) {
                // Soft glow blobs
                Box(
                    modifier = Modifier
                        .offset(x = (-40).dp, y = (-50).dp)
                        .align(Alignment.TopEnd)
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-30).dp, y = 60.dp)
                        .align(Alignment.BottomStart)
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.08f))
                )

                // Eyebrow + star badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Twoja uczelnia, Twoi ludzie",
                        style = SoftType.eyebrow.copy(
                            color = White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = SoftIcons.star,
                            contentDescription = null,
                            tint = Star,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "4.9",
                            style = SoftType.meta.copy(color = White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Floating tutor mini-cards
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tutors.forEachIndexed { i, (name, subject, rate) ->
                        Row(
                            modifier = Modifier
                                .offset(x = tutorOffsets[i])
                                .softShadow(RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(White.copy(alpha = 0.96f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InitialsAvatar(
                                name = name,
                                size = 36.dp,
                                colorIndex = tutorColorIndices[i]
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    name,
                                    style = SoftType.title.copy(fontSize = 13.sp),
                                    maxLines = 1
                                )
                                Text(
                                    subject,
                                    style = SoftType.meta.copy(fontSize = 11.5.sp)
                                )
                            }
                            Text(
                                rate,
                                style = SoftType.title.copy(
                                    fontSize = 13.sp,
                                    color = Primary600
                                )
                            )
                        }
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 22.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        append("Learn from someone who just ")
                        withStyle(SpanStyle(color = Primary600)) { append("aced it") }
                        append(".")
                    },
                    style = SoftType.display.copy(
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = InkToken
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "A vetted circle of PWr students and lecturers, ready to walk you through whatever the lecture skipped — calculus, thermo, OOP, anything.",
                    style = SoftType.body,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(Modifier.weight(1f, fill = false))
                Spacer(Modifier.height(28.dp))

                // 3-dot pager
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Line2)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Line2)
                    )
                }

                // CTA button
                SoftButton(
                    text = "Get started",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = SoftIcons.arrow
                )

                // Sign-in link
                Text(
                    buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Bold)) {
                            append("Sign in")
                        }
                    },
                    style = SoftType.meta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSignIn() }
                )
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
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Scaffold(containerColor = Bg) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar: back arrow ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconButton(
                    icon = SoftIcons.back,
                    onClick = onOpenRegister  // navigate back (to onboarding/register)
                )
                Spacer(Modifier.width(1.dp)) // keep layout balanced
            }

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp)
            ) {
                // 52dp indigo cap badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .primShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.cap,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "Welcome back.",
                    style = SoftType.display.copy(
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = InkToken
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pick up right where the lecture left off.",
                    style = SoftType.body
                )

                Spacer(Modifier.height(24.dp))

                // ── Email field ───────────────────────────────────────────────
                SoftTextField(
                    value = state.loginInput,
                    onValueChange = {
                        onClearMessages()
                        onLoginChanged(it)
                    },
                    label = "PWr login or email",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ── Password field with Show affordance ───────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Password",
                        style = SoftType.meta.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink2,
                            fontSize = 12.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSurface)
                            .border(1.dp, Line2, RoundedCornerShape(12.dp))
                            .padding(vertical = 13.dp, horizontal = 14.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = state.passwordInput,
                            onValueChange = {
                                onClearMessages()
                                onPasswordChanged(it)
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = InkToken
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 42.dp)
                        )
                        if (state.passwordInput.isEmpty()) {
                            Text(
                                "••••••••",
                                style = SoftType.body.copy(color = Ink3)
                            )
                        }
                        Text(
                            if (passwordVisible) "Hide" else "Show",
                            style = SoftType.meta.copy(
                                color = Primary600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { passwordVisible = !passwordVisible }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Remember me + Forgot ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (rememberMe) Primary else Line2)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { rememberMe = !rememberMe },
                            contentAlignment = Alignment.Center
                        ) {
                            if (rememberMe) {
                                Icon(
                                    imageVector = SoftIcons.check,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            "Remember me",
                            style = SoftType.meta.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Ink2,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Text(
                        "Forgot?",
                        style = SoftType.title.copy(
                            fontSize = 13.sp,
                            color = Primary600
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Error / success message ───────────────────────────────────
                if (state.errorMessage != null) {
                    Text(
                        state.errorMessage,
                        style = SoftType.bodySm.copy(color = Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Red.copy(alpha = 0.08f))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                if (state.infoMessage != null) {
                    Text(
                        state.infoMessage,
                        style = SoftType.bodySm.copy(color = Green),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green.copy(alpha = 0.08f))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Sign in button ────────────────────────────────────────────
                SoftButton(
                    text = if (state.isBusy) "Signing in..." else "Sign in",
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = SoftIcons.arrow,
                    enabled = !state.isBusy
                )

                Spacer(Modifier.height(20.dp))
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
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(containerColor = Bg) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar: back arrow ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconButton(
                    icon = SoftIcons.back,
                    onClick = onOpenLogin
                )
                Spacer(Modifier.width(1.dp))
            }

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp)
            ) {
                // 52dp indigo cap badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .primShadow(RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SoftIcons.cap,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "Create your account.",
                    style = SoftType.display.copy(
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = InkToken
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dołącz do PWr — student, tutor lub obie role.",
                    style = SoftType.body
                )

                Spacer(Modifier.height(24.dp))

                // ── Login field ───────────────────────────────────────────────
                SoftTextField(
                    value = form.login,
                    onValueChange = {
                        onClearMessages()
                        onRegisterLoginChanged(it)
                    },
                    label = "PWr login or email",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ── Display name field ────────────────────────────────────────
                SoftTextField(
                    value = form.displayName,
                    onValueChange = {
                        onClearMessages()
                        onRegisterDisplayNameChanged(it)
                    },
                    label = "Display name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ── Password field with Show affordance ───────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Password",
                        style = SoftType.meta.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink2,
                            fontSize = 12.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSurface)
                            .border(1.dp, Line2, RoundedCornerShape(12.dp))
                            .padding(vertical = 13.dp, horizontal = 14.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = form.password,
                            onValueChange = {
                                onClearMessages()
                                onRegisterPasswordChanged(it)
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = BodyFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = InkToken
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 42.dp)
                        )
                        if (form.password.isEmpty()) {
                            Text(
                                "min 6 characters",
                                style = SoftType.body.copy(color = Ink3)
                            )
                        }
                        Text(
                            if (passwordVisible) "Hide" else "Show",
                            style = SoftType.meta.copy(
                                color = Primary600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { passwordVisible = !passwordVisible }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Role toggles ──────────────────────────────────────────────
                Text(
                    "Your role",
                    style = SoftType.meta.copy(
                        fontWeight = FontWeight.Bold,
                        color = Ink2,
                        fontSize = 12.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SiRoleToggle(
                        label = "I am a student",
                        checked = form.student,
                        onCheckedChange = onRegisterStudentChecked
                    )
                    SiRoleToggle(
                        label = "I am a tutor",
                        checked = form.tutor,
                        onCheckedChange = onRegisterTutorChecked
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Error / success message ───────────────────────────────────
                if (state.errorMessage != null) {
                    Text(
                        state.errorMessage,
                        style = SoftType.bodySm.copy(color = Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Red.copy(alpha = 0.08f))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                if (state.infoMessage != null) {
                    Text(
                        state.infoMessage,
                        style = SoftType.bodySm.copy(color = Green),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green.copy(alpha = 0.08f))
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Create account button ─────────────────────────────────────
                SoftButton(
                    text = if (state.isBusy) "Creating account..." else "Create account",
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = SoftIcons.arrow,
                    enabled = !state.isBusy
                )

                Spacer(Modifier.height(16.dp))

                // ── Footer link back to sign-in ───────────────────────────────
                Text(
                    buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(SpanStyle(color = Primary600, fontWeight = FontWeight.Bold)) {
                            append("Sign in")
                        }
                    },
                    style = SoftType.meta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOpenLogin() }
                )
            }
        }
    }
}

@Composable
private fun SiRoleToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .border(1.dp, if (checked) Primary.copy(alpha = 0.35f) else Line2, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Primary else Line2),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = SoftIcons.check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Text(
            label,
            style = SoftType.meta.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (checked) InkToken else Ink2,
                fontSize = 14.sp
            )
        )
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
