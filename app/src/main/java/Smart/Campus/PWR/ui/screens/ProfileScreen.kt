package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.AppDangerButton
import Smart.Campus.PWR.ui.components.EditorialCard
import Smart.Campus.PWR.ui.components.FlatCard
import Smart.Campus.PWR.ui.components.InitialsAvatar
import Smart.Campus.PWR.ui.components.MainList
import Smart.Campus.PWR.ui.components.MonoLabel
import Smart.Campus.PWR.ui.components.Pill
import Smart.Campus.PWR.ui.components.StarsRow
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.DisplayFontFamily
import Smart.Campus.PWR.ui.theme.InkPrimary
import Smart.Campus.PWR.ui.theme.InkSecondary
import Smart.Campus.PWR.ui.theme.PwrNavy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTab(
    currentUser: AppUser,
    activeRole: UserRole,
    state: SmartCampusUiState,
    onLogout: () -> Unit
) {
    val lessons = if (activeRole == UserRole.TUTOR) {
        state.dashboardState.myTutorBookings
    } else {
        state.dashboardState.myStudentBookings
    }

    MainList {
        MonoLabel(if (activeRole == UserRole.TUTOR) "Tutor profile" else "Student profile", color = PwrNavy)
        EditorialCard {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                InitialsAvatar(name = currentUser.displayName, size = 88.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        currentUser.displayName.ifBlank { currentUser.login },
                        color = InkPrimary,
                        fontFamily = DisplayFontFamily,
                        fontSize = 31.sp,
                        lineHeight = 31.sp
                    )
                    Text(currentUser.email, color = InkSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                currentUser.roles.forEach { role ->
                    Pill(role.displayName, selected = role == activeRole)
                }
            }

            Text(
                if (activeRole == UserRole.TUTOR) {
                    "Your teaching profile and live tutoring history live here."
                } else {
                    "Your learning ledger, active bookings, and account details live here."
                },
                color = InkSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (activeRole == UserRole.TUTOR) {
            TutorEarningsSummary(lessons)
        } else {
            StudentLearningSummary(lessons)
        }

        AppDangerButton("Sign out", onClick = onLogout, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TutorEarningsSummary(lessons: List<LessonBookingUi>) {
    val booked = lessons.filter { it.status == "booked" }
    val cancelled = lessons.count { it.status == "cancelled" }
    val hours = booked.sumOf { estimateHours(it) }
    val earnings = hours * 45.0

    EditorialCard {
        MonoLabel("Live teaching estimate")
        Text(
            "${formatMoney(earnings)} zl",
            color = InkPrimary,
            fontFamily = DisplayFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 45.sp,
            lineHeight = 45.sp
        )
        Text(
            "${booked.size} sessions · ${formatHours(hours)} booked hours",
            color = InkSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        FlatCard {
            MonoLabel("Booking health")
            Text(
                "${booked.size} active · $cancelled cancelled",
                color = InkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StudentLearningSummary(lessons: List<LessonBookingUi>) {
    val booked = lessons.filter { it.status == "booked" }
    val cancelled = lessons.count { it.status == "cancelled" }
    val hours = booked.sumOf { estimateHours(it) }

    EditorialCard {
        MonoLabel("Live learning ledger")
        Text(
            "${booked.size} lessons,\n${formatHours(hours)} hours on campus.",
            color = InkPrimary,
            fontFamily = DisplayFontFamily,
            fontSize = 28.sp,
            lineHeight = 30.sp
        )
        FlatCard {
            MonoLabel("Booking health")
            Text(
                "${booked.size} active · $cancelled cancelled",
                color = InkSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

private fun estimateHours(lesson: LessonBookingUi): Double {
    val start = parseHour(lesson.startHour) ?: return 1.0
    val end = parseHour(lesson.endHour) ?: return 1.0
    return (end - start).coerceAtLeast(0.5)
}

private fun parseHour(value: String): Double? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour + minute / 60.0
}

private fun formatHours(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else String.format(java.util.Locale.ENGLISH, "%.1f", rounded)
}

private fun formatMoney(value: Double): String {
    val rounded = kotlin.math.round(value * 100.0) / 100.0
    return String.format(java.util.Locale.ENGLISH, "%.2f", rounded).replace('.', ',')
}
