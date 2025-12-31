package dev.rikoapp.cleanphonelauncher.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClockViewModel : ViewModel() {

    private val _clockType = MutableStateFlow(ClockType.ANALOG_WITH_SECONDS)
    val clockType = _clockType.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel = _batteryLevel.asStateFlow()

    fun setClockType(context: Context, type: ClockType) {
        viewModelScope.launch {
            val sharedPref = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("clock_type", type.name)
                apply()
            }
            _clockType.value = type
        }
    }

    fun loadClockType(context: Context) {
        viewModelScope.launch {
            val sharedPref = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
            val typeName = sharedPref.getString("clock_type", ClockType.ANALOG_WITH_SECONDS.name)
            _clockType.value = ClockType.valueOf(typeName ?: ClockType.ANALOG_WITH_SECONDS.name)
        }
    }

    fun registerBatteryReceiver(context: Context) {
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
