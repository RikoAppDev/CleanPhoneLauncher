package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dev.rikoapp.cleanphonelauncher.domain.ClockRepository
import dev.rikoapp.cleanphonelauncher.presentation.model.ClockType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClockRepositoryImpl(
    private val context: Application,
    private val applicationScope: CoroutineScope
) : ClockRepository {

    private val _clockType = MutableStateFlow(ClockType.ANALOG_WITH_SECONDS)
    override val clockType = _clockType.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    override val batteryLevel = _batteryLevel.asStateFlow()

    init {
        loadClockType()
        registerBatteryReceiver()
    }

    override fun setClockType(type: ClockType) {
        applicationScope.launch {
            val sharedPref = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("clock_type", type.name)
                apply()
            }
            _clockType.value = type
        }
    }

    override fun loadClockType() {
        applicationScope.launch {
            val sharedPref = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
            val typeName = sharedPref.getString("clock_type", ClockType.ANALOG_WITH_SECONDS.name)
            _clockType.value = ClockType.valueOf(typeName ?: ClockType.ANALOG_WITH_SECONDS.name)
        }
    }

    override fun registerBatteryReceiver() {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }
}