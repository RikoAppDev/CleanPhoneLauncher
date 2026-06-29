package dev.rikoapp.cleanphonelauncher.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey val appWidgetId: Int,
    val position: Int = 0,
    val heightDp: Int = 180
)
