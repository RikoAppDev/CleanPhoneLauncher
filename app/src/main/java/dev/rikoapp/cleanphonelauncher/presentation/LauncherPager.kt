package dev.rikoapp.cleanphonelauncher.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherPager(launcherViewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val apps by launcherViewModel.apps.collectAsState()
    val phoneApp by launcherViewModel.phoneApp.collectAsState()
    val cameraApp by launcherViewModel.cameraApp.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        launcherViewModel.getInstalledApps(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (it) {
            0 -> HomeScreen(phoneApp, cameraApp)
            1 -> AppListScreen(apps = apps, isActive = pagerState.currentPage == 1)
        }
    }
}
