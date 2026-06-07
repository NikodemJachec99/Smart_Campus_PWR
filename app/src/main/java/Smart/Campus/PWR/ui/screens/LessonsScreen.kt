package Smart.Campus.PWR.ui.screens

import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.components.softindigo.SoftTopBar
import Smart.Campus.PWR.ui.state.SmartCampusUiState
import Smart.Campus.PWR.ui.theme.Bg
import Smart.Campus.PWR.ui.theme.Ink3
import Smart.Campus.PWR.ui.theme.SoftType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Student "Lessons" tab — placeholder scaffold (Task 9). The full Soft Indigo
 * lessons screen (upcoming/past bookings, rate prompt) is implemented in Task 14.
 */
@Composable
fun LessonsTab(
    state: SmartCampusUiState,
    activeRole: UserRole,
    onNavigate: (String) -> Unit,
    onCancelBooking: (String, String, String) -> Unit,
    onClearMessages: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        SoftTopBar(title = "Lessons")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Lessons", style = SoftType.h3, color = Ink3)
        }
    }
}
