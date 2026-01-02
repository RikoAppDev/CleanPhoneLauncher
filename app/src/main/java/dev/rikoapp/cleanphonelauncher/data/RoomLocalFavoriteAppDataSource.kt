package dev.rikoapp.cleanphonelauncher.data

import dev.rikoapp.cleanphonelauncher.data.database.dao.FavoriteAppDao
import dev.rikoapp.cleanphonelauncher.data.database.mappers.toFavoriteApp
import dev.rikoapp.cleanphonelauncher.data.database.mappers.toFavoriteAppEntity
import dev.rikoapp.cleanphonelauncher.domain.model.FavoriteApp
import dev.rikoapp.cleanphonelauncher.domain.LocalFavoriteAppDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalFavoriteAppDataSource(
    private val favoriteAppDao: FavoriteAppDao
) : LocalFavoriteAppDataSource {

    override fun getFavoriteApps(): Flow<List<FavoriteApp>> {
        return favoriteAppDao.getFavoriteApps().map { entities ->
            entities.map { it.toFavoriteApp() }
        }
    }

    override suspend fun upsertFavoriteApp(app: FavoriteApp) {
        favoriteAppDao.upsert(app.toFavoriteAppEntity())
    }

    override suspend fun deleteFavoriteApp(app: FavoriteApp) {
        favoriteAppDao.delete(app.toFavoriteAppEntity())
    }
}