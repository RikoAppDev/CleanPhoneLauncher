package dev.rikoapp.cleanphonelauncher.presentation.widgets

import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget

/**
 * A fixed [COLUMNS] x [ROWS] cell grid per widget page. Widgets occupy a rectangle
 * of cells and may not overlap. All coordinate/placement math lives here so the
 * ViewModel and the screen agree on what "fits".
 */
object WidgetGrid {
    const val COLUMNS = 4
    const val ROWS = 8
    const val DEFAULT_SPAN = 2

    private fun emptyGrid(): Array<BooleanArray> = Array(ROWS) { BooleanArray(COLUMNS) }

    private fun mark(g: Array<BooleanArray>, col: Int, row: Int, colSpan: Int, rowSpan: Int) {
        for (r in row until row + rowSpan) {
            if (r !in 0 until ROWS) continue
            for (c in col until col + colSpan) {
                if (c in 0 until COLUMNS) g[r][c] = true
            }
        }
    }

    /** Occupancy of a page's widgets, optionally excluding one (e.g. the one being moved/resized). */
    fun occupancy(widgets: List<HomeWidget>, excludeId: Int? = null): Array<BooleanArray> {
        val g = emptyGrid()
        for (w in widgets) {
            if (w.appWidgetId == excludeId) continue
            mark(g, w.col, w.row, w.colSpan, w.rowSpan)
        }
        return g
    }

    fun fits(g: Array<BooleanArray>, col: Int, row: Int, colSpan: Int, rowSpan: Int): Boolean {
        if (col < 0 || row < 0 || colSpan < 1 || rowSpan < 1) return false
        if (col + colSpan > COLUMNS || row + rowSpan > ROWS) return false
        for (r in row until row + rowSpan) for (c in col until col + colSpan) if (g[r][c]) return false
        return true
    }

    /** First free top-left cell (row-major) for a widget of the given span, or null if the page is full. */
    fun findFree(g: Array<BooleanArray>, colSpan: Int, rowSpan: Int): Pair<Int, Int>? {
        if (colSpan > COLUMNS || rowSpan > ROWS) return null
        for (r in 0..ROWS - rowSpan) for (c in 0..COLUMNS - colSpan) {
            if (fits(g, c, r, colSpan, rowSpan)) return c to r
        }
        return null
    }

    /** Shrink a desired span down to what actually fits at (col,row) given occupancy [g]. */
    fun clampSpan(g: Array<BooleanArray>, col: Int, row: Int, desiredColSpan: Int, desiredRowSpan: Int): Pair<Int, Int> {
        var cs = desiredColSpan.coerceIn(1, COLUMNS - col)
        var rs = desiredRowSpan.coerceIn(1, ROWS - row)
        while (cs > 1 && !fits(g, col, row, cs, rs)) cs--
        while (rs > 1 && !fits(g, col, row, cs, rs)) rs--
        return cs to rs
    }

    /** True if any page has overlapping or out-of-bounds widgets (i.e. the layout needs repacking). */
    fun hasOverlaps(widgets: List<HomeWidget>): Boolean {
        for ((_, list) in widgets.groupBy { it.page }) {
            val g = emptyGrid()
            for (w in list) {
                if (w.col < 0 || w.row < 0 || w.col + w.colSpan > COLUMNS || w.row + w.rowSpan > ROWS) return true
                for (r in w.row until w.row + w.rowSpan) for (c in w.col until w.col + w.colSpan) {
                    if (g[r][c]) return true
                    g[r][c] = true
                }
            }
        }
        return false
    }

    /**
     * Deterministically re-lay widgets into valid, non-overlapping cells (used once after
     * migration and to repair any corrupt layout). Fills page by page in (page, position)
     * order, spilling to the next page when the current one is full.
     */
    fun repack(widgets: List<HomeWidget>): List<HomeWidget> {
        val sorted = widgets.sortedWith(compareBy({ it.page }, { it.position }))
        val result = ArrayList<HomeWidget>(sorted.size)
        var page = 0
        var grid = emptyGrid()
        for (w in sorted) {
            val cs = w.colSpan.coerceIn(1, COLUMNS)
            val rs = w.rowSpan.coerceIn(1, ROWS)
            var free = findFree(grid, cs, rs)
            if (free == null) {
                page++
                grid = emptyGrid()
                free = findFree(grid, cs, rs)
            }
            val (c, r) = free ?: (0 to 0)
            mark(grid, c, r, cs, rs)
            result += w.copy(page = page, col = c, row = r, colSpan = cs, rowSpan = rs)
        }
        return result
    }
}
