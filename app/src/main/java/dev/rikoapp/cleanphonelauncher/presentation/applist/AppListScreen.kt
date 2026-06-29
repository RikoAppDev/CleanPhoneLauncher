package dev.rikoapp.cleanphonelauncher.presentation.applist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.components.AppListItem
import dev.rikoapp.cleanphonelauncher.presentation.components.AppOptionsDialog
import dev.rikoapp.cleanphonelauncher.presentation.components.RenameDialog
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.SettingsIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun AppListScreenRoot(
    isActive: Boolean,
    onOpenSettings: () -> Unit = {},
    viewModel: AppListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(isActive) {
        viewModel.onAction(AppListScreenAction.OnActiveStateChanged(isActive))
        if (isActive) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                viewModel.onAction(AppListScreenAction.OnResume)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AppListScreen(
        state = state,
        onAction = viewModel::onAction,
        focusRequester = focusRequester,
        focusManager = focusManager,
        coroutineScope = coroutineScope,
        listState = listState,
        onOpenSettings = {
            focusManager.clearFocus()
            onOpenSettings()
        }
    )
}

@Composable
private fun AppListScreen(
    state: AppListScreenState,
    onAction: (AppListScreenAction) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    focusManager: FocusManager = LocalFocusManager.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    listState: LazyListState = rememberLazyListState(),
    onOpenSettings: () -> Unit = {}
) {
    val alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    val leadingItemCount = if (state.searchText.text.isBlank()) {
        if (state.hasUsageStatsPermission) {
            if (state.recentApps.isNotEmpty()) state.recentApps.size + 2 else 0
        } else {
            1
        }
    } else {
        0
    }

    if (state.showDialogApp != null) {
        val app = state.showDialogApp
        AppOptionsDialog(
            app = app,
            isFavorite = state.isFavorite(app),
            onDismiss = { onAction(AppListScreenAction.OnDialogDismiss) },
            onToggleFavorite = {
                onAction(
                    AppListScreenAction.OnFavoriteAction(
                        app = app,
                        isFavorite = state.isFavorite(app)
                    )
                )
            },
            onAppInfo = { onAction(AppListScreenAction.OnAppInfoClick(app)) },
            onUninstall = { onAction(AppListScreenAction.OnUninstallClick(app)) },
            isHidden = state.isHidden(app),
            onRename = { onAction(AppListScreenAction.OnRenameClick(app)) },
            onToggleHidden = {
                if (state.isHidden(app)) {
                    onAction(AppListScreenAction.OnUnhideApp(app))
                } else {
                    onAction(AppListScreenAction.OnHideApp(app))
                }
            }
        )
    }

    state.showRenameApp?.let { app ->
        RenameDialog(
            currentName = app.name,
            onDismiss = { onAction(AppListScreenAction.OnRenameDismiss) },
            onConfirm = { newName -> onAction(AppListScreenAction.OnRenameConfirm(app, newName)) }
        )
    }

    var showHidden by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        TextField(
            state = state.searchText,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text(text = stringResource(R.string.search_apps_placeholder)) },
            trailingIcon = {
                if (state.searchText.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onAction(AppListScreenAction.OnClearSearch)
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = CloseIcon,
                            contentDescription = stringResource(R.string.clear_search_content_description)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                val appToLaunch = if (state.searchText.text.isNotBlank()) {
                    state.filteredApps.firstOrNull()
                } else {
                    state.recentApps.firstOrNull()
                }
                onAction(AppListScreenAction.OnSearchDone(appToLaunch))
                focusManager.clearFocus()
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground
            )
        )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = SettingsIcon,
                    contentDescription = stringResource(R.string.open_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (state.searchText.text.isBlank()) {
                    if (state.hasUsageStatsPermission) {
                        if (state.recentApps.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.recent_apps),
                                    modifier = Modifier.padding(bottom = 8.dp, start = 16.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            items(state.recentApps) { app ->
                                AppListItem(
                                    app = app,
                                    onAppClick = { onAction(AppListScreenAction.OnAppClick(app)) },
                                    onAppLongClick = { onAction(AppListScreenAction.OnAppLongClick(app)) }
                                )
                            }
                            item {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = stringResource(R.string.recent_apps_permission_request),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onAction(AppListScreenAction.OnGrantPermissionClick) },
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onBackground,
                                        contentColor = MaterialTheme.colorScheme.background
                                    )
                                ) {
                                    Text(text = stringResource(R.string.grant_permission))
                                }
                            }
                        }
                    }
                }
                items(state.filteredApps) { app ->
                    AppListItem(
                        app = app,
                        onAppClick = { onAction(AppListScreenAction.OnAppClick(app)) },
                        onAppLongClick = { onAction(AppListScreenAction.OnAppLongClick(app)) },
                        badgeCount = state.badgeCount(app)
                    )
                }

                if (state.searchText.text.isBlank() && state.hiddenApps.isNotEmpty()) {
                    item {
                        Text(
                            text = if (showHidden) {
                                stringResource(R.string.hide_hidden_apps)
                            } else {
                                stringResource(R.string.show_hidden_apps, state.hiddenApps.size)
                            },
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHidden = !showHidden }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    if (showHidden) {
                        items(state.hiddenApps) { app ->
                            AppListItem(
                                app = app,
                                onAppClick = { onAction(AppListScreenAction.OnAppClick(app)) },
                                onAppLongClick = { onAction(AppListScreenAction.OnAppLongClick(app)) }
                            )
                        }
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp)
                    .width(24.dp)
            ) {
                val columnHeight = this.maxHeight
                val density = LocalDensity.current
                val letterTextHeight = 14.sp

                val letterTextHeightPx = with(density) { letterTextHeight.toPx() }
                val maxLetters = (columnHeight.value / letterTextHeightPx).toInt()

                val displayedAlphabet = remember(maxLetters, alphabet) {
                    if (maxLetters <= 0) {
                        ""
                    } else if (maxLetters >= alphabet.length) {
                        alphabet
                    } else {
                        val step = (alphabet.length - 1).toFloat() / (maxLetters - 1)
                        (0 until maxLetters).map { i ->
                            alphabet[(i * step).roundToInt()]
                        }.distinct().joinToString("")
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .pointerInput(state.allApps, leadingItemCount) {
                            detectVerticalDragGestures { change, _ ->
                                val y = change.position.y
                                val letterIndex = (y / (size.height / alphabet.length))
                                    .toInt()
                                    .coerceIn(alphabet.indices)
                                if (letterIndex in alphabet.indices) {
                                    onAction(AppListScreenAction.OnAlphabetScroll(alphabet[letterIndex]))
                                    val letter = alphabet[letterIndex]
                                    val index = if (letter == '#') {
                                        state.allApps.indexOfFirst { it.name.firstOrNull()?.isLetter() != true }
                                    } else {
                                        state.allApps.indexOfFirst {
                                            it.name.startsWith(
                                                letter,
                                                ignoreCase = true
                                            )
                                        }
                                    }
                                    if (index != -1) {
                                        coroutineScope.launch {
                                            listState.scrollToItem(index + leadingItemCount)
                                        }
                                    }
                                }
                            }
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    displayedAlphabet.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                onAction(AppListScreenAction.OnAlphabetClick(letter))
                                val index = if (letter == '#') {
                                    state.allApps.indexOfFirst { !it.name[0].isLetter() }
                                } else {
                                    state.allApps.indexOfFirst {
                                        it.name.startsWith(
                                            letter,
                                            ignoreCase = true
                                        )
                                    }
                                }
                                if (index != -1) {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index + leadingItemCount)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AppListScreenPreview() {
    CleanPhoneLauncherTheme {
        AppListScreen(
            state = AppListScreenState(
                allApps = listOf(
                    AppData(name = "WhatsApp", packageName = "com.whatsapp"),
                    AppData(name = "Camera", packageName = "com.google.android.apps.camera"),
                    AppData(name = "Discord", packageName = "com.discord"),
                    AppData(name = "Telegram", packageName = "org.telegram.messenger"),
                    AppData(name = "Facebook", packageName = "com.facebook.katana"),
                    AppData(name = "Instagram", packageName = "com.instagram.android"),
                    AppData(name = "Twitter", packageName = "com.twitter.android"),
                    AppData(name = "Reddit", packageName = "com.reddit.app"),
                    AppData(name = "YouTube", packageName = "com.google.android.youtube"),
                    AppData(name = "Spotify", packageName = "com.spotify.music"),
                    AppData(name = "Netflix", packageName = "com.netflix.mediaclient"),
                    AppData(name = "Amazon Prime Video", packageName = "com.amazon.avod"),
                ),
                recentApps = listOf(
                    AppData(name = "WhatsApp", packageName = "com.whatsapp"),
                    AppData(name = "Telegram", packageName = "org.telegram.messenger")
                ),
                hasUsageStatsPermission = true,
                isActive = true
            ),
            onAction = {}
        )
    }
}
