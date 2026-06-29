package dev.rikoapp.cleanphonelauncher.presentation.components

import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import org.koin.compose.koinInject

@Composable
fun WidgetSlot(
    appWidgetId: Int,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    val manager: WidgetHostManager = koinInject()
    val info = remember(appWidgetId) { manager.getInfo(appWidgetId) }

    val density = LocalDensity.current
    var widthDp by remember { mutableStateOf(0) }

    LaunchedEffect(appWidgetId, widthDp, heightDp) {
        if (info != null && widthDp > 0) manager.updateSize(appWidgetId, widthDp, heightDp)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .onSizeChanged { widthDp = with(density) { it.width.toDp().value.toInt() } },
        factory = { ctx -> manager.createView(ctx, appWidgetId) ?: View(ctx) }
    )
}
