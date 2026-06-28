package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.AppOverride
import kotlinx.coroutines.flow.Flow

interface LocalAppOverrideDataSource {
    fun getOverrides(): Flow<List<AppOverride>>
    suspend fun setHidden(packageName: String, hidden: Boolean)
    suspend fun setCustomName(packageName: String, customName: String?)
}
