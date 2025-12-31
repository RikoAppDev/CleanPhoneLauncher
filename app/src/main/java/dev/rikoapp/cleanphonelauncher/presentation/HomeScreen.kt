package dev.rikoapp.cleanphonelauncher.presentation

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.rikoapp.cleanphonelauncher.domain.AppData
import dev.rikoapp.cleanphonelauncher.presentation.components.AnalogClock
import dev.rikoapp.cleanphonelauncher.presentation.components.DigitalClock
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CameraIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.PhoneIcon

@Composable
fun HomeScreen(
    phoneApp: AppData?,
    cameraApp: AppData?,
    clockViewModel: ClockViewModel = viewModel()
) {
    val context = LocalContext.current
    val clockType by clockViewModel.clockType.collectAsState()
    val batteryLevel by clockViewModel.batteryLevel.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        clockViewModel.loadClockType(context)
        clockViewModel.registerBatteryReceiver(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val pm = context.packageManager
                            val clockIntent = listOf(
                                "com.google.android.deskclock",
                                "com.sec.android.app.clockpackage",
                                "com.sonymobile.digitalclock",
                                "com.htc.android.worldclock",
                                "com.motorola.blur.alarmclock",
                                "com.lge.clock",
                                "com.android.deskclock"
                            ).firstNotNullOfOrNull { pkgName ->
                                pm.getLaunchIntentForPackage(pkgName)
                            } ?: Intent(AlarmClock.ACTION_SHOW_ALARMS)

                            try {
                                context.startActivity(clockIntent)
                            } catch (_: Exception) {
                                context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                            }
                        },
                        onLongClick = { showDropdown = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (clockType) {
                    ClockType.ANALOG -> AnalogClock(
                        showSeconds = false,
                        batteryLevel = batteryLevel
                    )

                    ClockType.ANALOG_WITH_SECONDS -> AnalogClock(
                        showSeconds = true,
                        batteryLevel = batteryLevel
                    )

                    ClockType.DIGITAL -> DigitalClock(
                        showSeconds = false,
                        batteryLevel = batteryLevel
                    )

                    ClockType.DIGITAL_WITH_SECONDS -> DigitalClock(
                        showSeconds = true,
                        batteryLevel = batteryLevel
                    )
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    ClockType.entries.forEach { type ->
                        DropdownMenuItem(onClick = {
                            clockViewModel.setClockType(context, type)
                            showDropdown = false
                        }, text = { Text(type.displayName) })
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            phoneApp?.let { app ->
                IconButton(
                    onClick = {
                        val intent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = PhoneIcon,
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            cameraApp?.let { app ->
                IconButton(
                    onClick = {
                        val intent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = CameraIcon,
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
