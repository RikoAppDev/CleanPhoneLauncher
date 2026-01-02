package dev.rikoapp.cleanphonelauncher.data.database.mappers

import dev.rikoapp.cleanphonelauncher.data.database.entities.FavoriteAppEntity
import dev.rikoapp.cleanphonelauncher.domain.model.FavoriteApp

fun FavoriteAppEntity.toFavoriteApp(): FavoriteApp {
    return FavoriteApp(
        packageName = packageName
    )
}

fun FavoriteApp.toFavoriteAppEntity(): FavoriteAppEntity {
    return FavoriteAppEntity(
        packageName = packageName
    )
}