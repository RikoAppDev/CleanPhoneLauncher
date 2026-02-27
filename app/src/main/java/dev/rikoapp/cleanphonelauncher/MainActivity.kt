package dev.rikoapp.cleanphonelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.rikoapp.cleanphonelauncher.presentation.LauncherPager
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Prevent back press from hiding the launcher app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - this is a launcher app, back press should not hide it
            }
        })

        setContent {
            CleanPhoneLauncherTheme {
                LauncherPager()
            }
        }
    }
}
