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

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
