package dev.rikoapp.cleanphonelauncher.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.rikoapp.cleanphonelauncher.presentation.applist.AppListScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.home.HomeScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.settings.SettingsScreenRoot
import dev.rikoapp.cleanphonelauncher.presentation.widgets.WidgetsScreenRoot
import kotlinx.coroutines.launch

private const val WIDGETS_PAGE = 0
private const val HOME_PAGE = 1
private const val DRAWER_PAGE = 2

@Composable
fun LauncherPager() {
    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstResume by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var widgetFlowActive by remember { mutableStateOf(false) }
    val settingsOpen by rememberUpdatedState(showSettings)
    val widgetFlow by rememberUpdatedState(widgetFlowActive)

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
                    onWidgetFlowActive = { widgetFlowActive = it }
                )

                HOME_PAGE -> HomeScreenRoot(
                    onSwipeUp = {
                        coroutineScope.launch { pagerState.animateScrollToPage(DRAWER_PAGE) }
                    }
                )

                DRAWER_PAGE -> AppListScreenRoot(
                    isActive = pagerState.currentPage == DRAWER_PAGE,
                    onOpenSettings = { showSettings = true }
                )
            }
        }

        if (showSettings) {
            SettingsScreenRoot(onClose = { showSettings = false })
        }
    }
}
