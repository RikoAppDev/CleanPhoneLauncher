package dev.rikoapp.cleanphonelauncher.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey val appWidgetId: Int,
    val position: Int = 0,
    // Legacy free-form size (pre-grid); retained so the v6->v7 migration can seed
    // colSpan/rowSpan. No longer read by the grid.
    val heightDp: Int = 180,
    val widthPercent: Int = 100,
    val page: Int = 0,
    val col: Int = 0,
    val row: Int = 0,
    val colSpan: Int = 2,
    val rowSpan: Int = 2,
)
