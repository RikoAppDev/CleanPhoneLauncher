package dev.rikoapp.cleanphonelauncher.data

import dev.rikoapp.cleanphonelauncher.domain.NotificationCountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationCountRepositoryImpl : NotificationCountRepository {
    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val counts = _counts.asStateFlow()

    override fun update(counts: Map<String, Int>) {
        _counts.value = counts
    }
}
