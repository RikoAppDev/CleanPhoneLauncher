package dev.rikoapp.cleanphonelauncher.presentation.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun WidgetSlot(
    appWidgetId: Int,
    modifier: Modifier = Modifier
) {
    val manager: WidgetHostManager = koinInject()
    val info = remember(appWidgetId) { manager.getInfo(appWidgetId) }
    val density = LocalDensity.current

    var sizeDp by remember(appWidgetId) { mutableStateOf(0 to 0) }

    // Report the widget's size to the provider only after the size settles, so a
    // live resize drag doesn't spam updateAppWidgetOptions (which re-renders the
    // remote views on every frame and causes the jank this replaces).
    LaunchedEffect(appWidgetId, info) {
        if (info == null) return@LaunchedEffect
        snapshotFlow { sizeDp }.collectLatest { (w, h) ->
            if (w > 0 && h > 0) {
                delay(120)
                manager.updateSize(appWidgetId, w, h)
            }
        }
    }

    AndroidView(
        modifier = modifier.onSizeChanged {
            sizeDp = with(density) { it.width.toDp().value.toInt() to it.height.toDp().value.toInt() }
        },
        factory = { ctx -> manager.createView(ctx, appWidgetId) ?: View(ctx) }
    )
}
