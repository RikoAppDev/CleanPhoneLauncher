package dev.rikoapp.cleanphonelauncher.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_apps")
data class FavoriteAppEntity(@PrimaryKey val packageName: String)