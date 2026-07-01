package dev.rikoapp.cleanphonelauncher.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import dev.rikoapp.cleanphonelauncher.presentation.components.AppIcon
import dev.rikoapp.cleanphonelauncher.presentation.components.AppListItem
import dev.rikoapp.cleanphonelauncher.presentation.components.AppPickerDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.ClockTypeDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.AppOptionsDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.DigitalClock
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.DragHandleIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.PlusIcon
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreenRoot(
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    pageIndicatorEnabled: Boolean = false,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings,
        pageIndicatorEnabled = pageIndicatorEnabled
    )
}

@Composable
private fun HomeScreen(
    state: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    pageIndicatorEnabled: Boolean = false,
) {
    var reorderMode by remember { mutableStateOf(false) }
    var quickEditMode by remember { mutableStateOf(false) }
    var quickPickerTarget by remember { mutableStateOf<Int?>(null) }
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

    BackHandler(enabled = reorderMode || quickEditMode) {
        reorderMode = false
        quickEditMode = false
    }

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
                    onTap = {
                        if (reorderMode) reorderMode = false
                        if (quickEditMode) quickEditMode = false
                    }
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
                                            } else if (quickEditMode) {
                                                quickEditMode = false
                                            } else {
                                                onAction(HomeScreenAction.OnFavoriteAppClick(app))
                                            }
                                        },
                                        onAppLongClick = {
                                            if (!reorderMode && !quickEditMode) {
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
        QuickActionsRow(
            // Lift the buttons above the liquid page dots so a centered button
            // doesn't collide with them (only an issue with 3+ quick actions).
            modifier = Modifier.padding(
                bottom = if (pageIndicatorEnabled && state.quickActions.size > 2) 28.dp else 0.dp
            ),
            quickActions = state.quickActions,
            editMode = quickEditMode,
            onClick = { onAction(HomeScreenAction.OnQuickActionClick(it)) },
            onEnterEdit = { quickEditMode = true },
            onReassign = { index -> quickPickerTarget = index },
            onRemove = { index -> onAction(HomeScreenAction.OnQuickActionRemove(index)) },
            onAdd = { quickPickerTarget = ADD_QUICK_ACTION }
        )
    }

    quickPickerTarget?.let { target ->
        AppPickerDialog(
            apps = state.allApps,
            title = stringResource(R.string.quick_action_pick_title),
            onPick = { app ->
                if (target == ADD_QUICK_ACTION) {
                    onAction(HomeScreenAction.OnQuickActionAdd(app.packageName))
                } else {
                    onAction(HomeScreenAction.OnQuickActionSet(target, app.packageName))
                }
                quickPickerTarget = null
            },
            onDismiss = { quickPickerTarget = null }
        )
    }
}

private const val ADD_QUICK_ACTION = -1
private const val MIN_QUICK_ACTIONS = 0
private const val MAX_QUICK_ACTIONS = 5

@Composable
private fun QuickActionsRow(
    quickActions: List<AppData>,
    editMode: Boolean,
    onClick: (AppData) -> Unit,
    onEnterEdit: () -> Unit,
    onReassign: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (quickActions.isEmpty()) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickActions.forEachIndexed { index, app ->
            QuickActionButton(
                app = app,
                editMode = editMode,
                canRemove = quickActions.size > MIN_QUICK_ACTIONS,
                onClick = {
                    if (editMode) onReassign(index) else onClick(app)
                },
                onLongClick = onEnterEdit,
                onRemove = { onRemove(index) }
            )
        }
        // Keep the add affordance reachable even when the user has removed them all.
        if ((editMode || quickActions.isEmpty()) && quickActions.size < MAX_QUICK_ACTIONS) {
            AddQuickActionButton(onClick = onAdd)
        }
    }
}

@Composable
private fun QuickActionButton(
    app: AppData,
    editMode: Boolean,
    canRemove: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit
) {
    val wiggle by rememberInfiniteTransition(label = "wiggle").animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle-angle"
    )
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = if (editMode) wiggle else 0f
                }
                .size(48.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                packageName = app.packageName,
                contentDescription = app.name,
                modifier = Modifier.size(32.dp)
            )
        }
        if (editMode && canRemove) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CloseIcon,
                    contentDescription = stringResource(R.string.quick_action_remove),
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddQuickActionButton(onClick: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, fg.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PlusIcon,
            contentDescription = stringResource(R.string.quick_action_add),
            tint = fg,
            modifier = Modifier.size(22.dp)
        )
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
