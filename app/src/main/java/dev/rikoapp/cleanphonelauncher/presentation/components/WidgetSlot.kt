package dev.rikoapp.cleanphonelauncher.presentation.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import org.koin.compose.koinInject

@Composable
fun WidgetSlot(
    widgetId: Int,
    modifier: Modifier = Modifier
) {
    if (widgetId == -1) return

    val manager: WidgetHostManager = koinInject()
    val info = remember(widgetId) { manager.getInfo(widgetId) }
    if (info == null) return

    DisposableEffect(Unit) {
        manager.startListening()
        onDispose { manager.stopListening() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx -> manager.createView(ctx, widgetId) ?: View(ctx) }
    )
}
