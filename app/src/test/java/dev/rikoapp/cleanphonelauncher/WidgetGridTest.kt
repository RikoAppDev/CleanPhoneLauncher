package dev.rikoapp.cleanphonelauncher

import dev.rikoapp.cleanphonelauncher.domain.model.HomeWidget
import dev.rikoapp.cleanphonelauncher.presentation.widgets.WidgetGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetGridTest {

    private fun widget(id: Int, col: Int, row: Int, cs: Int = 2, rs: Int = 2, page: Int = 0) =
        HomeWidget(appWidgetId = id, page = page, col = col, row = row, colSpan = cs, rowSpan = rs, position = id)

    @Test
    fun `fits respects bounds and occupancy`() {
        val grid = WidgetGrid.occupancy(listOf(widget(1, 0, 0, 2, 2)))
        assertTrue(WidgetGrid.fits(grid, 2, 0, 2, 2))       // free area next to it
        assertFalse(WidgetGrid.fits(grid, 1, 1, 2, 2))      // overlaps the occupied 2x2
        assertFalse(WidgetGrid.fits(grid, 3, 0, 2, 2))      // exceeds 4 columns
        assertFalse(WidgetGrid.fits(grid, 0, 7, 2, 2))      // exceeds 8 rows
    }

    @Test
    fun `findFree returns first free cell row-major`() {
        val grid = WidgetGrid.occupancy(listOf(widget(1, 0, 0, 2, 2)))
        // A 2x2 no longer fits at (0,0); the next row-major spot is (2,0).
        assertEquals(2 to 0, WidgetGrid.findFree(grid, 2, 2))
    }

    @Test
    fun `findFree returns null when page is full`() {
        // Four 2x2 widgets fill rows 0-3 completely (4 cols x 4 rows).
        val full = listOf(
            widget(1, 0, 0), widget(2, 2, 0), widget(3, 0, 2), widget(4, 2, 2),
        )
        val grid = WidgetGrid.occupancy(full)
        // A full-width 4x8 no longer fits.
        assertNull(WidgetGrid.findFree(grid, 4, 8))
    }

    @Test
    fun `clampSpan is bounded by grid edge`() {
        val grid = WidgetGrid.occupancy(emptyList())
        // A widget at column 2 can be at most 2 wide (cols 2,3).
        val (cs, rs) = WidgetGrid.clampSpan(grid, 2, 0, desiredColSpan = 4, desiredRowSpan = 2)
        assertEquals(2, cs)
        assertEquals(2, rs)
    }

    @Test
    fun `clampSpan is bounded by an occupied neighbour`() {
        // A widget occupies column 2-3; a widget at column 0 can't grow past column 1.
        val neighbour = listOf(widget(9, 2, 0, 2, 2))
        val grid = WidgetGrid.occupancy(neighbour)
        val (cs, _) = WidgetGrid.clampSpan(grid, 0, 0, desiredColSpan = 4, desiredRowSpan = 2)
        assertEquals(2, cs)
    }

    @Test
    fun `hasOverlaps detects stacked widgets and clears after repack`() {
        val overlapping = listOf(widget(1, 0, 0), widget(2, 0, 0), widget(3, 0, 0))
        assertTrue(WidgetGrid.hasOverlaps(overlapping))

        val packed = WidgetGrid.repack(overlapping)
        assertFalse(WidgetGrid.hasOverlaps(packed))
        assertEquals(3, packed.size)
    }

    @Test
    fun `repack spills overflow onto a new page`() {
        // Five full-width (4x2) widgets: a page holds 4 rows worth, so the 5th spills.
        val many = (1..5).map { widget(it, 0, 0, cs = 4, rs = 2) }
        val packed = WidgetGrid.repack(many)
        assertFalse(WidgetGrid.hasOverlaps(packed))
        assertTrue(packed.any { it.page > 0 })
    }
}
