package dev.rikoapp.cleanphonelauncher.domain.model

/**
 * A widget placed on the grid of a widget page. Position is a cell rectangle
 * ([col], [row]) with a size of [colSpan] x [rowSpan] cells (see WidgetGrid).
 */
data class HomeWidget(
    val appWidgetId: Int,
    val page: Int = 0,
    val col: Int = 0,
    val row: Int = 0,
    val colSpan: Int = 2,
    val rowSpan: Int = 2,
    val position: Int = 0,
)
