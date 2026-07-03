package dev.rikoapp.cleanphonelauncher.presentation.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import dev.rikoapp.cleanphonelauncher.presentation.components.WidgetSlot
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.DragHandleIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.SettingsIcon
import dev.rikoapp.cleanphonelauncher.presentation.util.detectDoubleTapNonConsuming
import dev.rikoapp.cleanphonelauncher.presentation.util.detectLongPressNonConsuming
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun WidgetsScreenRoot(
    pageIndex: Int,
    pageCount: Int,
    onWidgetFlowActive: (Boolean) -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onAddPage: () -> Unit = {},
    onRemovePage: () -> Unit = {},
    onMoveToPage: (Int) -> Unit = {},
    viewModel: WidgetsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pageWidgets = remember(state.widgets, pageIndex) {
        state.widgets.filter { it.page == pageIndex }
    }
    val widgetManager: WidgetHostManager = koinInject()
    var pendingWidgetId by remember { mutableStateOf(-1) }

    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onWidgetBound(pendingWidgetId, pageIndex)
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
                    viewModel.onWidgetBound(id, pageIndex)
                    onWidgetFlowActive(false)
                }
        } else {
            viewModel.onWidgetBound(id, pageIndex)
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
        pageWidgets = pageWidgets,
        pageIndex = pageIndex,
        pageCount = pageCount,
        editMode = state.editMode,
        onSetEditMode = viewModel::setEditMode,
        onAddWidget = ::addWidget,
        onRemove = viewModel::onRemove,
        onReconfigure = ::reconfigure,
        isReconfigurable = { id -> widgetManager.getInfo(id)?.configure != null },
        onMove = { id, col, row -> viewModel.onMoveWidget(id, pageIndex, col, row) },
        onMoveToAdjacentPage = { id, delta ->
            val target = pageIndex + delta
            if (target in 0 until pageCount) {
                viewModel.onMoveWidgetToPage(id, target)
                onMoveToPage(delta)
            }
        },
        onResize = { id, colSpan, rowSpan -> viewModel.onResizeWidget(id, colSpan, rowSpan) },
        onDoubleTap = onDoubleTap,
        onAddPage = {
            viewModel.onAddPageAtStart()
            onAddPage()
        },
        onRemovePage = {
            viewModel.onRemovePage(pageIndex)
            onRemovePage()
        }
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
    pageWidgets: List<HomeWidget>,
    pageIndex: Int,
    pageCount: Int,
    editMode: Boolean,
    onSetEditMode: (Boolean) -> Unit,
    onAddWidget: () -> Unit,
    onRemove: (Int) -> Unit,
    onReconfigure: (Int) -> Unit,
    isReconfigurable: (Int) -> Boolean,
    onMove: (Int, Int, Int) -> Unit,
    onMoveToAdjacentPage: (Int, Int) -> Unit,
    onResize: (Int, Int, Int) -> Unit,
    onDoubleTap: () -> Unit = {},
    onAddPage: () -> Unit = {},
    onRemovePage: () -> Unit = {}
) {
    val fg = MaterialTheme.colorScheme.onBackground
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    BackHandler(enabled = editMode) { onSetEditMode(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectDoubleTapNonConsuming { currentOnDoubleTap() }
            }
            .pointerInput(Unit) {
                detectLongPressNonConsuming { onSetEditMode(true) }
            }
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        if (editMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.widget_page_label, pageIndex + 1, pageCount),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                TextChip(
                    text = stringResource(R.string.widgets_done),
                    onClick = { onSetEditMode(false) }
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextChip(text = stringResource(R.string.add_widget), onClick = onAddWidget)
                TextChip(text = stringResource(R.string.widget_add_page), onClick = onAddPage)
                TextChip(text = stringResource(R.string.widget_remove_page), onClick = onRemovePage)
            }

            Spacer(modifier = Modifier.size(12.dp))
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val cellW: Dp = maxWidth / WidgetGrid.COLUMNS
            val cellH: Dp = maxHeight / WidgetGrid.ROWS
            val gridColor = fg.copy(alpha = 0.12f)

            if (editMode) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width / WidgetGrid.COLUMNS
                    val ch = size.height / WidgetGrid.ROWS
                    for (c in 0..WidgetGrid.COLUMNS) {
                        drawLine(gridColor, Offset(c * cw, 0f), Offset(c * cw, size.height), 2f)
                    }
                    for (r in 0..WidgetGrid.ROWS) {
                        drawLine(gridColor, Offset(0f, r * ch), Offset(size.width, r * ch), 2f)
                    }
                }
            }

            pageWidgets.forEach { widget ->
                key(widget.appWidgetId) {
                    WidgetCell(
                        widget = widget,
                        pageWidgets = pageWidgets,
                        cellW = cellW,
                        cellH = cellH,
                        editMode = editMode,
                        onSetEditMode = onSetEditMode,
                        onRemove = onRemove,
                        onReconfigure = onReconfigure,
                        isReconfigurable = isReconfigurable,
                        onMove = onMove,
                        onMoveToAdjacentPage = onMoveToAdjacentPage,
                        onResize = onResize
                    )
                }
            }

            if (pageWidgets.isEmpty() && !editMode) {
                Text(
                    text = stringResource(R.string.widgets_empty),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onAddWidget() },
                                onLongPress = { onSetEditMode(true) }
                            )
                        }
                        .padding(16.dp)
                )
            }
        }

        if (editMode) {
            Text(
                text = stringResource(R.string.widgets_grid_hint),
                color = fg.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun WidgetCell(
    widget: HomeWidget,
    pageWidgets: List<HomeWidget>,
    cellW: Dp,
    cellH: Dp,
    editMode: Boolean,
    onSetEditMode: (Boolean) -> Unit,
    onRemove: (Int) -> Unit,
    onReconfigure: (Int) -> Unit,
    isReconfigurable: (Int) -> Boolean,
    onMove: (Int, Int, Int) -> Unit,
    onMoveToAdjacentPage: (Int, Int) -> Unit,
    onResize: (Int, Int, Int) -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    val density = LocalDensity.current
    val cellWpx = with(density) { cellW.toPx() }
    val cellHpx = with(density) { cellH.toPx() }

    // Live (snapped) placement — reset whenever the persisted value changes.
    var liveCol by remember(widget.col) { mutableIntStateOf(widget.col) }
    var liveRow by remember(widget.row) { mutableIntStateOf(widget.row) }
    var liveColSpan by remember(widget.colSpan) { mutableIntStateOf(widget.colSpan) }
    var liveRowSpan by remember(widget.rowSpan) { mutableIntStateOf(widget.rowSpan) }
    var accX by remember { mutableFloatStateOf(0f) }
    var accY by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    // +1/-1 when the widget is dragged past the right/left edge (transfer to that page).
    var pendingPageDelta by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .offset(x = cellW * liveCol, y = cellH * liveRow)
            .size(width = cellW * liveColSpan, height = cellH * liveRowSpan)
            .zIndex(if (dragging) 1f else 0f)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (editMode) Modifier.border(
                        width = 1.dp,
                        color = fg.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) else Modifier
                )
        ) {
            WidgetSlot(
                appWidgetId = widget.appWidgetId,
                modifier = Modifier.matchParentSize()
            )

            if (!editMode) {
                // Intercept touches so a tap can't launch the app; long-press edits.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(widget.appWidgetId) {
                            detectTapGestures(onLongPress = { onSetEditMode(true) })
                        }
                )
            }

            if (editMode) {
                // Drag the body to move the widget cell-by-cell.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                        .pointerInput(widget.appWidgetId, pageWidgets) {
                            detectDragGestures(
                                onDragStart = { dragging = true },
                                onDragEnd = {
                                    dragging = false
                                    accX = 0f
                                    accY = 0f
                                    if (pendingPageDelta != 0) {
                                        onMoveToAdjacentPage(widget.appWidgetId, pendingPageDelta)
                                        // Snap back here; if the transfer lands, this cell leaves the page.
                                        liveCol = widget.col
                                        liveRow = widget.row
                                    } else {
                                        onMove(widget.appWidgetId, liveCol, liveRow)
                                    }
                                    pendingPageDelta = 0
                                },
                                onDragCancel = { dragging = false; accX = 0f; accY = 0f; pendingPageDelta = 0 }
                            ) { change, dragAmount ->
                                change.consume()
                                accX += dragAmount.x
                                accY += dragAmount.y
                                val rawCol = widget.col + (accX / cellWpx).roundToInt()
                                pendingPageDelta = when {
                                    rawCol > WidgetGrid.COLUMNS - liveColSpan -> 1
                                    rawCol < 0 -> -1
                                    else -> 0
                                }
                                val targetCol = rawCol.coerceIn(0, WidgetGrid.COLUMNS - liveColSpan)
                                val targetRow = (widget.row + (accY / cellHpx).roundToInt())
                                    .coerceIn(0, WidgetGrid.ROWS - liveRowSpan)
                                val grid = WidgetGrid.occupancy(pageWidgets, excludeId = widget.appWidgetId)
                                if (WidgetGrid.fits(grid, targetCol, targetRow, liveColSpan, liveRowSpan)) {
                                    liveCol = targetCol
                                    liveRow = targetRow
                                }
                            }
                        }
                )

                Icon(
                    imageVector = DragHandleIcon,
                    contentDescription = stringResource(R.string.reorder_favorite),
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                )

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

                // Bottom-end corner: drag to resize, snapping to cells and bounded by free space.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.85f))
                        .pointerInput(widget.appWidgetId, pageWidgets) {
                            detectDragGestures(
                                onDragStart = { dragging = true },
                                onDragEnd = {
                                    dragging = false
                                    accX = 0f
                                    accY = 0f
                                    onResize(widget.appWidgetId, liveColSpan, liveRowSpan)
                                },
                                onDragCancel = { dragging = false; accX = 0f; accY = 0f }
                            ) { change, dragAmount ->
                                change.consume()
                                accX += dragAmount.x
                                accY += dragAmount.y
                                val desiredCs = widget.colSpan + (accX / cellWpx).roundToInt()
                                val desiredRs = widget.rowSpan + (accY / cellHpx).roundToInt()
                                val grid = WidgetGrid.occupancy(pageWidgets, excludeId = widget.appWidgetId)
                                val (cs, rs) = WidgetGrid.clampSpan(grid, widget.col, widget.row, desiredCs, desiredRs)
                                liveColSpan = cs
                                liveRowSpan = rs
                            }
                        }
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
