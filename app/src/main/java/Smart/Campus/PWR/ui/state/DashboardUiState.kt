package Smart.Campus.PWR.ui.state

enum class DashboardTab(val route: String, val label: String) {
    HOME(DashboardRoutes.HOME, "Start"),
    MAP(DashboardRoutes.MAP, "Mapa"),
    CHAT(DashboardRoutes.CHAT, "Czaty"),
    PROFILE(DashboardRoutes.PROFILE, "Profil")
}

object DashboardRoutes {
    const val HOME = "home"
    const val MAP = "map"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val EXCHANGE = "exchange"
    const val SCHEDULE = "schedule"
}

enum class QuickActionType {
    MAP,
    CHAT,
    EXCHANGE,
    SCHEDULE
}

data class HomeUserSummaryUi(
    val displayName: String,
    val greeting: String,
    val roleLabel: String,
    val avatarUrl: String? = null,
    val initials: String,
    val notificationCount: Int = 0
)

data class XpSummaryUi(
    val currentXp: Int,
    val targetXp: Int,
    val helperLabel: String,
    val badges: List<String>,
    val isPlaceholder: Boolean
)

data class GpsAlarmUi(
    val minutesUntilLeave: Int,
    val courseTitle: String,
    val locationLabel: String,
    val helperLabel: String
)

data class AiTutorPlanUi(
    val title: String,
    val description: String,
    val taskTitle: String,
    val estimatedTimeLabel: String,
    val isPlaceholder: Boolean
)

data class QuickActionUi(
    val type: QuickActionType,
    val label: String,
    val route: String?
)

data class UpcomingClassUi(
    val id: String,
    val timeLabel: String,
    val durationLabel: String,
    val title: String,
    val typeLabel: String,
    val roomLabel: String,
    val startsAtMillis: Long
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val userSummary: HomeUserSummaryUi? = null,
    val xpSummary: XpSummaryUi = XpSummaryUi(
        currentXp = 0,
        targetXp = 1,
        helperLabel = "Gamifikacja w przygotowaniu",
        badges = emptyList(),
        isPlaceholder = true
    ),
    val gpsAlarm: GpsAlarmUi? = null,
    val aiTutorPlan: AiTutorPlanUi? = null,
    val quickActions: List<QuickActionUi> = listOf(
        QuickActionUi(QuickActionType.MAP, "Mapa PWr", DashboardRoutes.MAP),
        QuickActionUi(QuickActionType.CHAT, "PWr Chat", DashboardRoutes.CHAT),
        QuickActionUi(QuickActionType.EXCHANGE, "Wymiana", DashboardRoutes.EXCHANGE),
        QuickActionUi(QuickActionType.SCHEDULE, "Plan zajec", DashboardRoutes.SCHEDULE)
    ),
    val upcomingClasses: List<UpcomingClassUi> = emptyList()
)
