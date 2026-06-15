package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.CardQ
import Smart.Campus.PWR.ui.components.softindigo.InitialsAvatar
import Smart.Campus.PWR.ui.components.softindigo.RoleSwitch
import Smart.Campus.PWR.ui.components.softindigo.SoftButton
import Smart.Campus.PWR.ui.components.softindigo.SoftDivider
import Smart.Campus.PWR.ui.components.softindigo.SoftIconButton
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.components.softindigo.Tile
import Smart.Campus.PWR.ui.icons.SoftIcons
import Smart.Campus.PWR.ui.state.DashboardRoutes
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Ink2
import Smart.Campus.PWR.ui.theme.Ink4
import Smart.Campus.PWR.ui.theme.InkToken
import Smart.Campus.PWR.ui.theme.Primary50
import Smart.Campus.PWR.ui.theme.Primary600
import Smart.Campus.PWR.ui.components.softindigo.SoftButtonVariant
import Smart.Campus.PWR.ui.theme.SoftType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProfileTab(
    currentUser: AppUser,
    activeRole: UserRole,
    state: SmartCampusUiState,
    onLogout: () -> Unit,
    onToggleRole: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val isTutor = activeRole == UserRole.TUTOR
    val studentBookings = state.dashboardState.myStudentBookings
    val tutorBookings = state.dashboardState.myTutorBookings

    // Stats derivation
    val lessonCount = if (isTutor) tutorBookings.size else studentBookings.size
    val studiedHours = if (isTutor) {
        tutorBookings.filter { it.status == "booked" }.sumOf { estimateProfileHours(it) }
    } else {
        studentBookings.filter { it.status == "booked" }.sumOf { estimateProfileHours(it) }
    }
    val distinctTutorsCount = studentBookings.map { it.tutorId }.distinct().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        SoftTopBar(
            title = "Profile",
            actions = {
                SoftIconButton(
                    icon = SoftIcons.settings,
                    onClick = { /* DESIGN-PLACEHOLDER */ }
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header row: avatar + name + meta ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                InitialsAvatar(
                    name = currentUser.displayName.ifBlank { currentUser.login },
                    size = 68.dp
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = currentUser.displayName.ifBlank { currentUser.login },
                        style = SoftType.h2,
                        color = InkToken
                    )
                    // DESIGN-PLACEHOLDER: program/year not yet in AppUser model
                    Text(
                        text = "Computer Science · 2nd year", // DESIGN-PLACEHOLDER
                        style = SoftType.meta
                    )
                    Text(
                        text = currentUser.email,
                        style = SoftType.meta.copy(color = Ink4)
                    )
                }
            }

            // ── Role switch card ──────────────────────────────────────────────
            CardQ {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "I'm here to…", style = SoftType.title, color = InkToken)
                    Text(text = "Switch anytime", style = SoftType.meta)
                }
                Spacer(Modifier.height(10.dp))
                RoleSwitch(
                    isTutor = isTutor,
                    onToggle = onToggleRole,
                    fullWidth = true
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (isTutor)
                        "You're in Teaching mode — managing slots & earning."
                    else
                        "You're in Studying mode — browsing & booking tutors.",
                    style = SoftType.meta
                )
            }

            // ── Stats row ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Tile(modifier = Modifier.weight(1f), padding = 14.dp) {
                    Text(
                        text = lessonCount.toString(),
                        style = SoftType.h2,
                        color = InkToken,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Lessons",
                        style = SoftType.meta,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Tile(modifier = Modifier.weight(1f), padding = 14.dp) {
                    Text(
                        text = formatProfileHours(studiedHours),
                        style = SoftType.h2,
                        color = InkToken,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = if (isTutor) "Taught (h)" else "Studied (h)",
                        style = SoftType.meta,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Tile(modifier = Modifier.weight(1f), padding = 14.dp) {
                    Text(
                        text = if (isTutor) "—" else distinctTutorsCount.toString(), // DESIGN-PLACEHOLDER for tutor
                        style = SoftType.h2,
                        color = InkToken,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = if (isTutor) "Students" else "Tutors",
                        style = SoftType.meta,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // ── Menu card ─────────────────────────────────────────────────────
            val menuItems = buildList {
                add(ProfileMenuItem(
                    title = "Reviews you've written",
                    subtitle = null,
                    icon = SoftIcons.star,
                    onClick = { onNavigate(DashboardRoutes.REVIEWS) }
                ))
                if (isTutor) {
                    add(ProfileMenuItem(
                        title = "Reviews about you",
                        subtitle = null,
                        icon = SoftIcons.star,
                        onClick = { onNavigate(DashboardRoutes.REVIEWS) }
                    ))
                }
                add(ProfileMenuItem(
                    title = "Notifications",
                    subtitle = null, // DESIGN-PLACEHOLDER
                    icon = SoftIcons.bell,
                    onClick = { /* DESIGN-PLACEHOLDER */ }
                ))
                add(ProfileMenuItem(
                    title = "Help & support",
                    subtitle = null,
                    icon = SoftIcons.shield,
                    onClick = { /* DESIGN-PLACEHOLDER */ }
                ))
            }

            CardQ(padding = 6.dp) {
                menuItems.forEachIndexed { index, item ->
                    ProfileMenuRow(item = item)
                    if (index < menuItems.lastIndex) {
                        SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }

            // ── Sign out button ───────────────────────────────────────────────
            SoftButton(
                text = "Sign out",
                onClick = onLogout,
                variant = SoftButtonVariant.Danger,
                leadingIcon = SoftIcons.logout,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(100.dp))
        }
    }
}

// ─── Data model for menu rows ─────────────────────────────────────────────────

private data class ProfileMenuItem(
    val title: String,
    val subtitle: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

// ─── Menu row composable ──────────────────────────────────────────────────────

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = InkToken)
            ) { item.onClick() }
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Primary50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(19.dp)
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = SoftType.title, color = InkToken)
            if (item.subtitle != null) {
                Text(text = item.subtitle, style = SoftType.meta)
            }
        }

        // Chevron
        Icon(
            imageVector = SoftIcons.chev,
            contentDescription = null,
            tint = Ink4,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Private helpers ──────────────────────────────────────────────────────────

private fun estimateProfileHours(lesson: LessonBookingUi): Double {
    val start = parseProfileHour(lesson.startHour) ?: return 1.0
    val end = parseProfileHour(lesson.endHour) ?: return 1.0
    return (end - start).coerceAtLeast(0.5)
}

private fun parseProfileHour(value: String): Double? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour + minute / 60.0
}

private fun formatProfileHours(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else String.format(java.util.Locale.ENGLISH, "%.1f", rounded)
}
