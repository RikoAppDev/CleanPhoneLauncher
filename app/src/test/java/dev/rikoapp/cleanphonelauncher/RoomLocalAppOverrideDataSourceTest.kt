package dev.rikoapp.cleanphonelauncher

import dev.rikoapp.cleanphonelauncher.data.RoomLocalAppOverrideDataSource
import dev.rikoapp.cleanphonelauncher.data.database.dao.AppOverrideDao
import dev.rikoapp.cleanphonelauncher.data.database.entities.AppOverrideEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomLocalAppOverrideDataSourceTest {

    private class FakeAppOverrideDao : AppOverrideDao {
        val data = MutableStateFlow<List<AppOverrideEntity>>(emptyList())
        override fun getOverrides(): Flow<List<AppOverrideEntity>> = data
        override suspend fun getOverride(packageName: String) =
            data.value.firstOrNull { it.packageName == packageName }

        override suspend fun upsert(override: AppOverrideEntity) {
            data.value = data.value.filterNot { it.packageName == override.packageName } + override
        }

        override suspend fun delete(packageName: String) {
            data.value = data.value.filterNot { it.packageName == packageName }
        }
    }

    @Test
    fun hidingThenUnhiding_withoutCustomName_removesTheOverride() = runBlocking {
        val dao = FakeAppOverrideDao()
        val dataSource = RoomLocalAppOverrideDataSource(dao)

        dataSource.setHidden("com.example", true)
        assertTrue(dao.getOverride("com.example")!!.hidden)

        dataSource.setHidden("com.example", false)
        assertNull(dao.getOverride("com.example"))
    }

    @Test
    fun renaming_preservesHiddenFlag() = runBlocking {
        val dao = FakeAppOverrideDao()
        val dataSource = RoomLocalAppOverrideDataSource(dao)

        dataSource.setHidden("com.example", true)
        dataSource.setCustomName("com.example", "My App")

        val override = dao.getOverride("com.example")!!
        assertEquals("My App", override.customName)
        assertTrue(override.hidden)
    }

    @Test
    fun clearingNameWhenNotHidden_removesTheOverride() = runBlocking {
        val dao = FakeAppOverrideDao()
        val dataSource = RoomLocalAppOverrideDataSource(dao)

        dataSource.setCustomName("com.example", "Name")
        dataSource.setCustomName("com.example", null)

        assertNull(dao.getOverride("com.example"))
    }
}
