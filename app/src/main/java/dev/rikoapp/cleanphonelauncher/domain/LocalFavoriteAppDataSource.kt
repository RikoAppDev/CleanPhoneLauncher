package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.FavoriteApp
import kotlinx.coroutines.flow.Flow

interface LocalFavoriteAppDataSource {
    fun getFavoriteApps(): Flow<List<FavoriteApp>>
    suspend fun upsertFavoriteApp(app: FavoriteApp)
    suspend fun deleteFavoriteApp(app: FavoriteApp)
    suspend fun reorderFavoriteApps(orderedPackageNames: List<String>)
}