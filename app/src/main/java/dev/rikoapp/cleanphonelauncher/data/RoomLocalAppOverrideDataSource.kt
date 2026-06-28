package dev.rikoapp.cleanphonelauncher.data

import dev.rikoapp.cleanphonelauncher.data.database.dao.AppOverrideDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.AppOverrideEntity
import dev.rikoapp.cleanphonelauncher.domain.LocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.domain.model.AppOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalAppOverrideDataSource(
    private val appOverrideDao: AppOverrideDao
) : LocalAppOverrideDataSource {

    override fun getOverrides(): Flow<List<AppOverride>> {
        return appOverrideDao.getOverrides().map { entities ->
            entities.map { AppOverride(it.packageName, it.hidden, it.customName) }
        }
    }

    override suspend fun setHidden(packageName: String, hidden: Boolean) {
        val current = appOverrideDao.getOverride(packageName)
        save(packageName, hidden = hidden, customName = current?.customName)
    }

    override suspend fun setCustomName(packageName: String, customName: String?) {
        val current = appOverrideDao.getOverride(packageName)
        save(packageName, hidden = current?.hidden ?: false, customName = customName)
    }

    private suspend fun save(packageName: String, hidden: Boolean, customName: String?) {
        if (!hidden && customName == null) {
            appOverrideDao.delete(packageName)
        } else {
            appOverrideDao.upsert(AppOverrideEntity(packageName, hidden, customName))
        }
    }
}
