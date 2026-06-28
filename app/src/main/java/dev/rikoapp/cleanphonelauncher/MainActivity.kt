package dev.rikoapp.cleanphonelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.LauncherPager
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings: SettingsRepository = koinInject()
            val themeMode by settings.themeMode.collectAsState()
            val colorStyle by settings.colorStyle.collectAsState()

            CleanPhoneLauncherTheme(themeMode = themeMode, colorStyle = colorStyle) {
                LauncherPager()
            }
        }
    }
}
