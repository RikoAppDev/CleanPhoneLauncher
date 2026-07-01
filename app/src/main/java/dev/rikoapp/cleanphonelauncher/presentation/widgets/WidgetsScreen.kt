package dev.rikoapp.cleanphonelauncher.presentation.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.presentation.components.WidgetSlot
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.DragHandleIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.SettingsIcon
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun WidgetsScreenRoot(
    onWidgetFlowActive: (Boolean) -> Unit = {},
    viewModel: WidgetsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val widgetManager: WidgetHostManager = koinInject()
    var pendingWidgetId by remember { mutableStateOf(-1) }

    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onWidgetBound(pendingWidgetId)
        } else {
            viewModel.discardAllocation(pendingWidgetId)
        }
        onWidgetFlowActive(false)
    }

    fun confirmOrConfigure(id: Int) {
        val info = widgetManager.getInfo(id)
        if (info?.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }
            runCatching { configureLauncher.launch(intent) }
                .onFailure {
                    viewModel.onWidgetBound(id)
                    onWidgetFlowActive(false)
                }
        } else {
            viewModel.onWidgetBound(id)
            onWidgetFlowActive(false)
        }
    }

    val bindLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || widgetManager.getInfo(pendingWidgetId) != null) {
            confirmOrConfigure(pendingWidgetId)
        } else {
            viewModel.discardAllocation(pendingWidgetId)
            onWidgetFlowActive(false)
        }
    }

    val reconfigureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { onWidgetFlowActive(false) }

    var showPicker by remember { mutableStateOf(false) }

    fun addWidget() {
        showPicker = true
    }

    fun startWidget(info: AppWidgetProviderInfo) {
        showPicker = false
        onWidgetFlowActive(true)
        val id = widgetManager.allocateId()
        pendingWidgetId = id
        if (widgetManager.bindIfAllowed(id, info)) {
            confirmOrConfigure(id)
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            }
            runCatching { bindLauncher.launch(intent) }
                .onFailure {
                    widgetManager.deleteId(id)
                    onWidgetFlowActive(false)
                }
        }
    }

    fun reconfigure(id: Int) {
        val info = widgetManager.getInfo(id) ?: return
        if (info.configure == null) return
        onWidgetFlowActive(true)
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = info.configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        runCatching { reconfigureLauncher.launch(intent) }
            .onFailure { onWidgetFlowActive(false) }
    }

    DisposableEffect(Unit) {
        widgetManager.startListening()
        onDispose { widgetManager.stopListening() }
    }

    WidgetsScreen(
        state = state,
        onAddWidget = ::addWidget,
        onRemove = viewModel::onRemove,
        onResize = viewModel::onResize,
        onReorder = viewModel::onReorder,
        onReconfigure = ::reconfigure,
        isReconfigurable = { id -> widgetManager.getInfo(id)?.configure != null }
    )

    if (showPicker) {
        WidgetPickerDialog(
            onPick = ::startWidget,
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun WidgetsScreen(
    state: WidgetsScreenState,
    onAddWidget: () -> Unit,
    onRemove: (Int) -> Unit,
    onResize: (Int, Int, Int) -> Unit,
    onReorder: (List<Int>) -> Unit,
    onReconfigure: (Int) -> Unit,
    isReconfigurable: (Int) -> Boolean
) {
    val fg = MaterialTheme.colorScheme.onBackground
    var editMode by remember { mutableStateOf(false) }
    BackHandler(enabled = editMode) { editMode = false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.widgets_title),
                color = fg,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            if (state.widgets.isNotEmpty()) {
                TextChip(
                    text = stringResource(if (editMode) R.string.widgets_done else R.string.widgets_edit),
                    onClick = { editMode = !editMode }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            TextChip(
                text = stringResource(R.string.add_widget),
                onClick = onAddWidget
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.widgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.widgets_empty),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddWidget)
                        .padding(16.dp)
                )
            }
        } else {
            var ordered by remember(state.widgets) { mutableStateOf(state.widgets) }
            val lazyState = rememberLazyListState()
            val reorderState = rememberReorderableLazyListState(lazyState) { from, to ->
                ordered = ordered.toMutableList().apply { add(to.index, removeAt(from.index)) }
            }

            LazyColumn(
                state = lazyState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ordered, key = { it.appWidgetId }) { widget ->
                    ReorderableItem(reorderState, key = widget.appWidgetId) { _ ->
                        val density = LocalDensity.current
                        var liveHeight by remember(widget.heightDp) { mutableIntStateOf(widget.heightDp) }
                        var liveWidthPercent by remember(widget.widthPercent) {
                            mutableIntStateOf(widget.widthPercent)
                        }
                        var containerWidthPx by remember { mutableIntStateOf(0) }

                        // Full-width track so the corner handle can map horizontal drag to a width fraction.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { containerWidthPx = it.width }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(liveWidthPercent / 100f)
                                    .height(liveHeight.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (editMode) Modifier.border(
                                            width = 1.dp,
                                            color = fg.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) else Modifier
                                    )
                                    .then(
                                        if (!editMode) Modifier.pointerInput(widget.appWidgetId) {
                                            detectTapGestures(onLongPress = { editMode = true })
                                        } else Modifier
                                    )
                            ) {
                                WidgetSlot(
                                    appWidgetId = widget.appWidgetId,
                                    modifier = Modifier.matchParentSize()
                                )

                                if (editMode) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.Black.copy(alpha = 0.18f))
                                            .pointerInput(Unit) {}
                                    )

                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .draggableHandle(
                                                onDragStopped = {
                                                    onReorder(ordered.map { it.appWidgetId })
                                                }
                                            )
                                    ) {
                                        Icon(
                                            imageVector = DragHandleIcon,
                                            contentDescription = stringResource(R.string.reorder_favorite),
                                            tint = Color.White
                                        )
                                    }

                                    Row(modifier = Modifier.align(Alignment.TopEnd)) {
                                        if (isReconfigurable(widget.appWidgetId)) {
                                            IconButton(onClick = { onReconfigure(widget.appWidgetId) }) {
                                                Icon(
                                                    imageVector = SettingsIcon,
                                                    contentDescription = stringResource(R.string.widget_reconfigure),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onRemove(widget.appWidgetId) }) {
                                            Icon(
                                                imageVector = CloseIcon,
                                                contentDescription = stringResource(R.string.remove_widget),
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    // Bottom-end corner handle: drag to resize width and height at once.
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.85f))
                                            .pointerInput(widget.appWidgetId) {
                                                detectDragGestures(
                                                    onDragEnd = {
                                                        onResize(
                                                            widget.appWidgetId,
                                                            liveWidthPercent,
                                                            liveHeight
                                                        )
                                                    }
                                                ) { change, dragAmount ->
                                                    change.consume()
                                                    val deltaHeightDp = with(density) { dragAmount.y.toDp().value }
                                                    liveHeight = (liveHeight + deltaHeightDp)
                                                        .roundToInt()
                                                        .coerceIn(
                                                            WidgetsViewModel.MIN_HEIGHT_DP,
                                                            WidgetsViewModel.MAX_HEIGHT_DP
                                                        )
                                                    if (containerWidthPx > 0) {
                                                        val deltaPercent =
                                                            dragAmount.x / containerWidthPx * 100f
                                                        liveWidthPercent = (liveWidthPercent + deltaPercent)
                                                            .roundToInt()
                                                            .coerceIn(
                                                                WidgetsViewModel.MIN_WIDTH_PERCENT,
                                                                WidgetsViewModel.MAX_WIDTH_PERCENT
                                                            )
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

            if (editMode) {
                Text(
                    text = stringResource(R.string.widgets_hint),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TextChip(text: String, onClick: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(width = 1.dp, color = fg.copy(alpha = 0.3f), shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}
