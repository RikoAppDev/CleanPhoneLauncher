package dev.rikoapp.cleanphonelauncher.domain

import kotlinx.coroutines.flow.StateFlow

interface NotificationCountRepository {
    val counts: StateFlow<Map<String, Int>>
    fun update(counts: Map<String, Int>)
}
