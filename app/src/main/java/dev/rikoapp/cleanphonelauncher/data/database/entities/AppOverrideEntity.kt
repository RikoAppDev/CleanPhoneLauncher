package dev.rikoapp.cleanphonelauncher.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_overrides")
data class AppOverrideEntity(
    @PrimaryKey val packageName: String,
    val hidden: Boolean = false,
    val customName: String? = null
)
