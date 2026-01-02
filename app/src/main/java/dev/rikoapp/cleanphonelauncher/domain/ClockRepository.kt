package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType
import kotlinx.coroutines.flow.StateFlow

interface ClockRepository {
    val clockType: StateFlow<ClockType>
    val batteryLevel: StateFlow<Int>

    fun setClockType(type: ClockType)
    fun loadClockType()
    fun registerBatteryReceiver()
}