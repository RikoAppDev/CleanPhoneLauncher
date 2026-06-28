package dev.rikoapp.cleanphonelauncher.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.components.AnalogClock
import dev.rikoapp.cleanphonelauncher.presentation.components.AppListItem
import dev.rikoapp.cleanphonelauncher.presentation.components.ClockTypeDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.AppOptionsDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.DigitalClock
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CameraIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.PhoneIcon
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    HomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun HomeScreen(
    state: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
) {
    if (state.showClockTypeDialog) {
        ClockTypeDialog(
            currentClockType = state.clockType,
            onDismiss = { onAction(HomeScreenAction.OnClockTypeDialogDismiss) },
            onConfirm = { selectedType ->
                onAction(HomeScreenAction.OnClockTypeConfirm(selectedType))
            }
        )
    }

    if (state.showDialogApp != null) {
        val app = state.showDialogApp
        AppOptionsDialog(
            app = app,
            isFavorite = true,
            onDismiss = { onAction(HomeScreenAction.OnFavoriteDialogDismiss) },
            onToggleFavorite = { onAction(HomeScreenAction.OnRemoveFavorite(app.packageName)) },
            onAppInfo = { onAction(HomeScreenAction.OnAppInfoClick(app)) },
            onUninstall = { onAction(HomeScreenAction.OnUninstallClick(app)) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onAction(HomeScreenAction.OnClockClick) },
                        onLongClick = { onAction(HomeScreenAction.OnClockLongClick) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state.clockType) {
                    ClockType.ANALOG -> AnalogClock(
                        showSeconds = false,
                        batteryLevel = state.batteryLevel
                    )

                    ClockType.ANALOG_WITH_SECONDS -> AnalogClock(
                        showSeconds = true,
                        batteryLevel = state.batteryLevel
                    )

                    ClockType.DIGITAL -> DigitalClock(
                        showSeconds = false,
                        batteryLevel = state.batteryLevel
                    )

                    ClockType.DIGITAL_WITH_SECONDS -> DigitalClock(
                        showSeconds = true,
                        batteryLevel = state.batteryLevel
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                items(state.favoriteAppsData) { app ->
                    AppListItem(
                        app = app,
                        onAppClick = { onAction(HomeScreenAction.OnFavoriteAppClick(app)) },
                        onAppLongClick = { onAction(HomeScreenAction.OnFavoriteAppLongClick(app)) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.phoneApp?.let { app ->
                IconButton(
                    onClick = { onAction(HomeScreenAction.OnPhoneAppClick(app)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = PhoneIcon,
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            state.cameraApp?.let { app ->
                IconButton(
                    onClick = { onAction(HomeScreenAction.OnCameraAppClick(app)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = CameraIcon,
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    CleanPhoneLauncherTheme {
        HomeScreen(
            state = HomeScreenState(
                allApps = listOf(
                    AppData(name = "WhatsApp", packageName = "com.whatsapp"),
                    AppData(name = "Camera", packageName = "com.google.android.apps.camera"),
                    AppData(name = "Discord", packageName = "com.discord")
                ),
                phoneApp = AppData(
                    name = "WhatsApp",
                    packageName = "com.whatsapp"
                ),
                cameraApp = AppData(
                    name = "Camera",
                    packageName = "com.google.android.apps.camera"
                ),
                clockType = ClockType.DIGITAL_WITH_SECONDS,
                batteryLevel = 75,
                favoriteAppsData = listOf(
                    AppData(name = "WhatsApp", packageName = "com.whatsapp"),
                    AppData(name = "Discord", packageName = "com.discord")
                )
            ),
            onAction = {}
        )
    }
}
