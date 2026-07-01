package dev.rikoapp.cleanphonelauncher.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.components.AnalogClock
import dev.rikoapp.cleanphonelauncher.presentation.components.AppListItem
import dev.rikoapp.cleanphonelauncher.presentation.components.ClockTypeDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.AppOptionsDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.DigitalClock
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CameraIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.DragHandleIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.PhoneIcon
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreenRoot(
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings
    )
}

@Composable
private fun HomeScreen(
    state: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    var reorderMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val runGesture by rememberUpdatedState<(GestureAction) -> Unit>({ action ->
        when (action) {
            GestureAction.NONE -> {}
            GestureAction.APP_DRAWER -> onOpenDrawer()
            GestureAction.SETTINGS -> onOpenSettings()
            GestureAction.NOTIFICATIONS -> onAction(HomeScreenAction.OnExpandNotifications)
            GestureAction.LOCK_SCREEN -> onAction(HomeScreenAction.OnLockScreen)
        }
    })
    val swipeUpAction by rememberUpdatedState(state.swipeUpAction)
    val swipeDownAction by rememberUpdatedState(state.swipeDownAction)
    val doubleTapAction by rememberUpdatedState(state.doubleTapAction)

    BackHandler(enabled = reorderMode) { reorderMode = false }

    if (state.showAccessibilityDisclosure) {
        AlertDialog(
            onDismissRequest = { onAction(HomeScreenAction.OnAccessibilityDisclosureDismiss) },
            title = { Text(stringResource(R.string.accessibility_disclosure_title)) },
            text = { Text(stringResource(R.string.accessibility_disclosure_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                    onAction(HomeScreenAction.OnAccessibilityDisclosureDismiss)
                }) {
                    Text(stringResource(R.string.accessibility_disclosure_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(HomeScreenAction.OnAccessibilityDisclosureDismiss)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { reorderMode = true },
                    onDoubleTap = { runGesture(doubleTapAction) },
                    onTap = { if (reorderMode) reorderMode = false }
                )
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -120f) runGesture(swipeUpAction)
                        else if (totalDrag > 120f) runGesture(swipeDownAction)
                    }
                ) { _, dragAmount -> totalDrag += dragAmount }
            }
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

            Text(
                text = stringResource(R.string.reorder_hint),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .alpha(if (reorderMode) 1f else 0f)
            )

            val haptics = LocalHapticFeedback.current
            var favorites by remember(state.favoriteAppsData) {
                mutableStateOf(state.favoriteAppsData)
            }
            val lazyListState = rememberLazyListState()
            val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
                favorites = favorites.toMutableList().apply { add(to.index, removeAt(from.index)) }
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = reorderMode
            ) {
                items(favorites, key = { it.packageName }) { app ->
                    ReorderableItem(reorderState, key = app.packageName) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 6.dp else 0.dp)
                        Surface(
                            shadowElevation = elevation,
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    AppListItem(
                                        app = app,
                                        onAppClick = {
                                            if (reorderMode) {
                                                reorderMode = false
                                            } else {
                                                onAction(HomeScreenAction.OnFavoriteAppClick(app))
                                            }
                                        },
                                        onAppLongClick = {
                                            if (!reorderMode) {
                                                onAction(HomeScreenAction.OnFavoriteAppLongClick(app))
                                            }
                                        },
                                        badgeCount = state.notificationCounts[app.packageName] ?: 0
                                    )
                                }
                                if (reorderMode) {
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.draggableHandle(
                                            onDragStopped = {
                                                onAction(
                                                    HomeScreenAction.OnReorderFavorites(
                                                        favorites.map { it.packageName }
                                                    )
                                                )
                                            }
                                        )
                                    ) {
                                        Icon(
                                            imageVector = DragHandleIcon,
                                            contentDescription = stringResource(R.string.reorder_favorite),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
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
