package Smart.Campus.PWR.ui

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.DashboardTab
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.state.QuickActionType
import Smart.Campus.PWR.ui.state.QuickActionUi
import Smart.Campus.PWR.ui.state.UpcomingClassUi
import Smart.Campus.PWR.ui.theme.AppBackground
import Smart.Campus.PWR.ui.theme.AppSurface
import Smart.Campus.PWR.ui.theme.AppSurfaceMuted
import Smart.Campus.PWR.ui.theme.OrangeSoft
import Smart.Campus.PWR.ui.theme.OrangeText
import Smart.Campus.PWR.ui.theme.PurpleSoft
import Smart.Campus.PWR.ui.theme.PurpleText
import Smart.Campus.PWR.ui.theme.PwrBlueMuted
import Smart.Campus.PWR.ui.theme.PwrBlueSoft
import Smart.Campus.PWR.ui.theme.PwrNavy
import Smart.Campus.PWR.ui.theme.PwrNavyDark
import Smart.Campus.PWR.ui.theme.PwrRed
import Smart.Campus.PWR.ui.theme.PwrRedSoft
import Smart.Campus.PWR.ui.theme.SuccessSoft
import Smart.Campus.PWR.ui.theme.SuccessText
import Smart.Campus.PWR.ui.theme.TextPrimary
import Smart.Campus.PWR.ui.theme.TextSecondary
import Smart.Campus.PWR.ui.theme.WarningSoft
import Smart.Campus.PWR.ui.theme.WarningText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

private enum class AdminShellTab(val route: String, val label: String, val icon: ImageVector) {
    ADMIN("admin_home", "Admin", Icons.Rounded.Shield),
    MAP(DashboardRoutes.MAP, "Mapa", Icons.Rounded.Map),
    CHAT(DashboardRoutes.CHAT, "Czaty", Icons.Rounded.ChatBubbleOutline),
    PROFILE(DashboardRoutes.PROFILE, "Profil", Icons.Rounded.Person)
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
    onCreateLecturerChecked: (Boolean) -> Unit,
    onCreateUserClick: () -> Unit,
    onUpdateUserRoles: (String, Boolean, Boolean) -> Unit,
    onClearMessages: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AdminShellTab.ADMIN.route
    val currentUser = state.currentUser

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White.copy(alpha = 0.96f)) {
                AdminShellTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PwrNavy,
                            selectedTextColor = PwrNavy,
                            indicatorColor = PwrBlueSoft,
                            unselectedIconColor = PwrBlueMuted,
                            unselectedTextColor = PwrBlueMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminShellTab.ADMIN.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(AdminShellTab.ADMIN.route) {
                AdminPanelScreen(
                    state = state,
                    onRefresh = onRefresh,
                    onLogout = onLogout,
                    onCreateLoginChanged = onCreateLoginChanged,
                    onCreatePasswordChanged = onCreatePasswordChanged,
                    onCreateDisplayNameChanged = onCreateDisplayNameChanged,
                    onCreateAdminChecked = onCreateAdminChecked,
                    onCreateStudentChecked = onCreateStudentChecked,
                    onCreateLecturerChecked = onCreateLecturerChecked,
                    onCreateUserClick = onCreateUserClick,
                    onUpdateUserRoles = onUpdateUserRoles,
                    onClearMessages = onClearMessages
                )
            }
            composable(AdminShellTab.MAP.route) {
                ModulePlaceholderScreen(Icons.Rounded.Map, "Mapa PWr", "Modul mapy jest gotowy pod przyszla integracje z trasami i salami.")
            }
            composable(AdminShellTab.CHAT.route) {
                ModulePlaceholderScreen(Icons.Rounded.Forum, "PWr Chat", "Tutaj pojawia sie czaty grupowe i komunikacja kampusowa.")
            }
            composable(AdminShellTab.PROFILE.route) {
                if (currentUser != null) {
                    ProfileScreen(
                        currentUser = currentUser,
                        activeRole = UserRole.ADMIN,
                        onOpenRolePicker = {},
                        onLogout = onLogout
                    )
                } else {
                    ModulePlaceholderScreen(Icons.Rounded.Person, "Profil", "Trwa ladowanie danych administratora.")
                }
            }
        }
    }
}

@Composable
fun MainShellScreen(
    state: DashboardUiState,
    currentUser: AppUser,
    activeRole: UserRole,
    onRefreshDashboard: () -> Unit,
    onLogout: () -> Unit,
    onOpenRolePicker: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: DashboardRoutes.HOME

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White.copy(alpha = 0.96f)) {
                DashboardTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PwrNavy,
                            selectedTextColor = PwrNavy,
                            indicatorColor = PwrBlueSoft,
                            unselectedIconColor = PwrBlueMuted,
                            unselectedTextColor = PwrBlueMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoutes.HOME,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(DashboardRoutes.HOME) {
                DashboardHomeScreen(
                    state = state,
                    currentUser = currentUser,
                    activeRole = activeRole,
                    onRefreshDashboard = onRefreshDashboard,
                    onQuickAction = { route -> navController.navigate(route) { launchSingleTop = true } }
                )
            }
            composable(DashboardRoutes.MAP) { ModulePlaceholderScreen(Icons.Rounded.Map, "Mapa PWr", "Modul mapy jest gotowy pod przyszla integracje z trasami i salami.") }
            composable(DashboardRoutes.CHAT) { ModulePlaceholderScreen(Icons.Rounded.Forum, "PWr Chat", "Tutaj pojawia sie czaty grupowe i komunikacja kampusowa.") }
            composable(DashboardRoutes.EXCHANGE) { ModulePlaceholderScreen(Icons.Rounded.SwapHoriz, "Wymiana plikow", "Placeholder modulu wymiany materialow.") }
            composable(DashboardRoutes.SCHEDULE) { ModulePlaceholderScreen(Icons.Rounded.CalendarMonth, "Plan zajec", "Rozszerzony harmonogram pojawi sie tutaj.") }
            composable(DashboardRoutes.PROFILE) {
                ProfileScreen(currentUser = currentUser, activeRole = activeRole, onOpenRolePicker = onOpenRolePicker, onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun DashboardHomeScreen(
    state: DashboardUiState,
    currentUser: AppUser,
    activeRole: UserRole,
    onRefreshDashboard: () -> Unit,
    onQuickAction: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HomeHeader(state = state, currentUser = currentUser, activeRole = activeRole) }
        item { XpCard(state = state) }
        item { GpsCard(state = state) }
        item { AiCard(state = state) }
        item { QuickActions(actions = state.quickActions, onQuickAction = onQuickAction) }
        item { SectionHeader(title = "Dalsze zajecia", actionLabel = if (state.isLoading) "Odswiezanie..." else "Odswiez", onAction = onRefreshDashboard) }
        if (state.upcomingClasses.isEmpty()) {
            item { EmptyCard(Icons.Rounded.Schedule, "Brak nadchodzacych zajec", "Po pojawieniu sie danych w kolekcji classes zobaczysz je tutaj automatycznie.") }
        } else {
            items(state.upcomingClasses, key = { it.id }) { classItem ->
                UpcomingClassCard(classItem)
            }
        }
    }
}

@Composable
private fun HomeHeader(state: DashboardUiState, currentUser: AppUser, activeRole: UserRole) {
    val summary = state.userSummary
    val roleLabel = summary?.roleLabel ?: when (activeRole) {
        UserRole.STUDENT -> "Tryb Student"
        UserRole.LECTURER -> "Tryb Wykladowca"
        UserRole.ADMIN -> "Tryb Admin"
    }

    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PwrNavy, PwrNavyDark)), RoundedCornerShape(30.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        AvatarBubble(summary?.avatarUrl ?: currentUser.avatarUrl, summary?.initials ?: currentUser.initials())
                        Column {
                            Text(summary?.greeting ?: "Dzien dobry,", color = PwrBlueSoft)
                            Text(summary?.displayName ?: currentUser.displayName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            ElevatedAssistChip(onClick = {}, enabled = false, label = { Text(roleLabel, color = PwrNavy) }, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    Box {
                        IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.14f))) {
                            Icon(Icons.Rounded.NotificationsNone, contentDescription = "Powiadomienia", tint = Color.White)
                        }
                        if ((summary?.notificationCount ?: 0) > 0) {
                            Badge(modifier = Modifier.align(Alignment.TopEnd), containerColor = PwrRed) { Text((summary?.notificationCount ?: 0).toString()) }
                        }
                    }
                }
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = Color.White.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun AvatarBubble(avatarUrl: String?, initials: String) {
    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize().clip(CircleShape))
        } else {
            Text(initials, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun XpCard(state: DashboardUiState) {
    val xp = state.xpSummary
    val progress = if (xp.targetXp == 0) 0f else xp.currentXp.toFloat() / xp.targetXp.toFloat()
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(WarningSoft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = WarningText)
                    }
                    Column {
                        Text("Twoj poziom", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${xp.currentXp} XP / ${xp.targetXp} XP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    xp.badges.take(2).forEach { badge ->
                        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(PwrBlueSoft), contentAlignment = Alignment.Center) { Text(badge) }
                    }
                }
            }
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), color = PwrNavy, trackColor = AppSurfaceMuted)
            Text(xp.helperLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun GpsCard(state: DashboardUiState) {
    val alarm = state.gpsAlarm
    val background = if (alarm == null) PwrRedSoft else PwrRed
    val textColor = if (alarm == null) PwrRed else Color.White
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Row(modifier = Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = if (alarm == null) 1f else 0.18f)), contentAlignment = Alignment.Center) {
                Icon(if (alarm == null) Icons.Rounded.Explore else Icons.Rounded.Shield, contentDescription = null, tint = textColor)
            }
            Column {
                Text(alarm?.let { "Wyjdz za ${it.minutesUntilLeave} minut" } ?: "Alarm GPS gotowy na integracje", color = textColor, fontWeight = FontWeight.Bold)
                Text(alarm?.courseTitle ?: "Karta pojawi sie, gdy dojdzie integracja trasy i czasu przejscia.", color = textColor, style = MaterialTheme.typography.bodyMedium)
                if (alarm != null) {
                    Text(alarm.locationLabel, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun AiCard(state: DashboardUiState) {
    val ai = state.aiTutorPlan
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(PwrBlueSoft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Psychology, contentDescription = null, tint = PwrNavy)
                    }
                    Text(ai?.title ?: "AI Tutor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = {}) { Text("Zobacz") }
            }
            Text(ai?.description ?: "Sekcja AI jest gotowa na dane. Po integracji pojawia sie tu plan nauki lub wsparcia dla prowadzacego.", color = TextSecondary, modifier = Modifier.padding(top = 10.dp))
            Surface(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), color = AppSurfaceMuted, shape = RoundedCornerShape(22.dp)) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(PwrNavy), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(ai?.taskTitle ?: "Brak aktywnego planu AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(ai?.estimatedTimeLabel ?: "Oczekuje na integracje", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = PwrNavy)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActions(actions: List<QuickActionUi>, onQuickAction: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Szybki dostep", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            actions.forEach { action ->
                val config = when (action.type) {
                    QuickActionType.MAP -> Triple(Icons.Rounded.Place, action.label, SuccessSoft to SuccessText)
                    QuickActionType.CHAT -> Triple(Icons.Rounded.ChatBubbleOutline, action.label, PurpleSoft to PurpleText)
                    QuickActionType.EXCHANGE -> Triple(Icons.Rounded.SwapHoriz, action.label, OrangeSoft to OrangeText)
                    QuickActionType.SCHEDULE -> Triple(Icons.Rounded.CalendarMonth, action.label, PwrBlueSoft to PwrNavy)
                }
                val route = action.route ?: return@forEach
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = config.first,
                    label = config.second,
                    background = config.third.first,
                    contentColor = config.third.second,
                    onClick = { onQuickAction(route) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(modifier: Modifier, icon: ImageVector, label: String, background: Color, contentColor: Color, onClick: () -> Unit) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(modifier = Modifier.fillMaxWidth().height(82.dp).clickable(onClick = onClick), color = background, shape = RoundedCornerShape(24.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(28.dp)) }
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun UpcomingClassCard(classItem: UpcomingClassUi) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(6.dp).height(54.dp).clip(RoundedCornerShape(12.dp)).background(PwrNavy))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(classItem.timeLabel, color = PwrNavy, fontWeight = FontWeight.ExtraBold)
                Text(classItem.durationLabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(classItem.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = AppSurfaceMuted, shape = RoundedCornerShape(12.dp)) {
                        Text(classItem.typeLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(classItem.roomLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(currentUser: AppUser, activeRole: UserRole, onOpenRolePicker: () -> Unit, onLogout: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = PwrNavy)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarBubble(currentUser.avatarUrl, currentUser.initials())
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentUser.displayName, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                            Text(currentUser.email, color = PwrBlueSoft)
                        }
                    }
                    ElevatedAssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(when (activeRole) { UserRole.STUDENT -> "Tryb Student"; UserRole.LECTURER -> "Tryb Wykladowca"; UserRole.ADMIN -> "Tryb Admin" }, color = PwrNavy) },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Profil i ustawienia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Ta zakladka jest placeholderem pod przyszle ustawienia i preferencje kampusu.", color = TextSecondary)
                    if (currentUser.hasDualRole()) {
                        OutlinedButton(onClick = onOpenRolePicker, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Przelacz role")
                        }
                    }
                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wyloguj")
                    }
                }
            }
        }
        item { EmptyCard(Icons.Rounded.WorkspacePremium, "Sekcje profilu w przygotowaniu", "Tutaj pojawia sie osiagniecia, ustawienia powiadomien i szczegoly konta.") }
    }
}

@Composable
private fun ModulePlaceholderScreen(icon: ImageVector, title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize().background(AppBackground).padding(20.dp), contentAlignment = Alignment.Center) {
        EmptyCard(icon = icon, title = title, description = description)
    }
}

@Composable
private fun EmptyCard(icon: ImageVector, title: String, description: String) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(AppSurfaceMuted), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PwrNavy)
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

private fun DashboardTab.icon(): ImageVector = when (this) {
    DashboardTab.HOME -> Icons.Rounded.Home
    DashboardTab.MAP -> Icons.Rounded.Map
    DashboardTab.CHAT -> Icons.Rounded.ChatBubbleOutline
    DashboardTab.PROFILE -> Icons.Rounded.Person
}



