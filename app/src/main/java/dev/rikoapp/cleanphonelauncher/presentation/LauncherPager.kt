package dev.rikoapp.cleanphonelauncher.presentation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.rikoapp.cleanphonelauncher.LockAccessibilityService
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import dev.rikoapp.cleanphonelauncher.presentation.applist.AppListScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.components.PageIndicator
import dev.rikoapp.cleanphonelauncher.presentation.home.HomeScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.model.GestureAction
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.widgets.WidgetsScreenRoot
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val WIDGETS_PAGE = 0
private const val HOME_PAGE = 1
private const val DRAWER_PAGE = 2

@Composable
fun LauncherPager() {
    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { 3 })
    val settings: SettingsRepository = koinInject()
    val pageIndicatorEnabled by settings.pageIndicatorEnabled.collectAsState()
    val doubleTapAction by settings.doubleTapAction.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstResume by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var widgetFlowActive by remember { mutableStateOf(false) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }
    val settingsOpen by rememberUpdatedState(showSettings)
    val widgetFlow by rememberUpdatedState(widgetFlowActive)

    // Double-tap handling lives here so the configured action works on every page,
    // not only home. Home keeps its own detector; the drawer/widgets call this.
    val onDoubleTap by rememberUpdatedState<() -> Unit>({
        when (doubleTapAction) {
            GestureAction.NONE -> {}
            GestureAction.APP_DRAWER ->
                coroutineScope.launch { pagerState.animateScrollToPage(DRAWER_PAGE) }

            GestureAction.SETTINGS -> showSettings = true
            GestureAction.NOTIFICATIONS -> expandNotificationShade(context)
            GestureAction.LOCK_SCREEN ->
                if (!LockAccessibilityService.lockScreen()) showAccessibilityDisclosure = true
        }
    })

    if (showAccessibilityDisclosure) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDisclosure = false },
            title = { Text(stringResource(R.string.accessibility_disclosure_title)) },
            text = { Text(stringResource(R.string.accessibility_disclosure_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showAccessibilityDisclosure = false
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }) { Text(stringResource(R.string.accessibility_disclosure_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDisclosure = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    BackHandler(enabled = !showSettings) {
        if (pagerState.currentPage != HOME_PAGE) {
            coroutineScope.launch { pagerState.animateScrollToPage(HOME_PAGE) }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                } else if (!settingsOpen && !widgetFlow) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(HOME_PAGE)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                WIDGETS_PAGE -> WidgetsScreenRoot(
                    onWidgetFlowActive = { widgetFlowActive = it },
                    onDoubleTap = { onDoubleTap() }
                )

                HOME_PAGE -> HomeScreenRoot(
                    onOpenDrawer = {
                        coroutineScope.launch { pagerState.animateScrollToPage(DRAWER_PAGE) }
                    },
                    onOpenSettings = { showSettings = true }
                )

                DRAWER_PAGE -> AppListScreenRoot(
                    isActive = pagerState.currentPage == DRAWER_PAGE,
                    onOpenSettings = { showSettings = true },
                    onDoubleTap = { onDoubleTap() }
                )
            }
        }

        if (pageIndicatorEnabled && !showSettings) {
            val position by remember {
                derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            }
            PageIndicator(
                pageCount = pagerState.pageCount,
                position = position,
                activeColor = MaterialTheme.colorScheme.onBackground,
                inactiveColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            )
        }

        if (showSettings) {
            SettingsScreenRoot(onClose = { showSettings = false })
        }
    }
}

private fun expandNotificationShade(context: Context) {
    runCatching {
        val service = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        statusBarManager.getMethod("expandNotificationsPanel").invoke(service)
    }
}
