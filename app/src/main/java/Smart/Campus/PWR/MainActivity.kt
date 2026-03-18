package Smart.Campus.PWR

import Smart.Campus.PWR.ui.SmartCampusApp
import Smart.Campus.PWR.ui.theme.SmartCampusPWRTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCampusPWRTheme {
                SmartCampusApp()
            }
        }
    }
}

