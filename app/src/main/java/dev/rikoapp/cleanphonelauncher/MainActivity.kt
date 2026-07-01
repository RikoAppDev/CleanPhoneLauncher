package dev.rikoapp.cleanphonelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.LauncherPager
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.version.AppViewModel
import dev.rikoapp.cleanphonelauncher.presentation.version.ForceUpgradeScreen
import dev.rikoapp.cleanphonelauncher.presentation.version.VersionState
import dev.rikoapp.cleanphonelauncher.presentation.version.WarnUpgradeDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings: SettingsRepository = koinInject()
            val themeMode by settings.themeMode.collectAsState()
            val colorStyle by settings.colorStyle.collectAsState()
            val accentColor by settings.accentColor.collectAsState()

            val appViewModel: AppViewModel = koinViewModel()
            val versionState by appViewModel.versionState.collectAsState()

            CleanPhoneLauncherTheme(
                themeMode = themeMode,
                colorStyle = colorStyle,
                customAccent = Color(accentColor)
            ) {
                when (val state = versionState) {
                    is VersionState.ForceUpgrade -> ForceUpgradeScreen(storeUrl = state.storeUrl)
                    else -> {
                        LauncherPager()
                        if (state is VersionState.WarnUpgrade) {
                            WarnUpgradeDialog(
                                storeUrl = state.storeUrl,
                                onDismiss = appViewModel::onWarnDismissed
                            )
                        }
                    }
                }
            }
        }
    }
}
