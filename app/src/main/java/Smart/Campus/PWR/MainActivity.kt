package Smart.Campus.PWR

import Smart.Campus.PWR.notifications.FcmTokenRegistrar
import Smart.Campus.PWR.ui.SmartCampusApp
import Smart.Campus.PWR.ui.theme.SmartCampusPWRTheme
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Own the window insets in Compose so the keyboard is handled by imePadding()
        // (WindowInsets.ime). On API 35+ `windowSoftInputMode=adjustResize` is ignored, so
        // without this the chat composer ends up behind the keyboard.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FcmTokenRegistrar.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            SmartCampusPWRTheme {
                SmartCampusApp()
            }
        }
    }
}
