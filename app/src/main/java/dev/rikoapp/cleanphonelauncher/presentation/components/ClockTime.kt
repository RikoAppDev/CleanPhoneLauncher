package dev.rikoapp.cleanphonelauncher.presentation.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Current time as Compose state. With seconds it ticks every second; without seconds it updates
 * once a minute and reacts to system time / timezone changes (no per-second wake-ups).
 */
@Composable
fun rememberClockTime(showSeconds: Boolean): Calendar {
    var time by remember { mutableStateOf(Calendar.getInstance()) }

    if (showSeconds) {
        LaunchedEffect(Unit) {
            while (true) {
                time = Calendar.getInstance()
                delay(1000)
            }
        }
    } else {
        val context = LocalContext.current
        DisposableEffect(Unit) {
            time = Calendar.getInstance()
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    time = Calendar.getInstance()
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose { context.unregisterReceiver(receiver) }
        }
    }

    return time
}
