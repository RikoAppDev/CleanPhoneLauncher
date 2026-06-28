package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import java.text.SimpleDateFormat

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier,
    showSeconds: Boolean = true,
    batteryLevel: Int
) {
    val calendar = rememberClockTime(showSeconds)

    val locale = LocalConfiguration.current.locales[0]
    val timeFormat = if (showSeconds) "HH:mm:ss" else "HH:mm"
    val time = SimpleDateFormat(timeFormat, locale).format(calendar.time)
    val dateFormat = SimpleDateFormat("EEE, MMM d", locale)
    val date = dateFormat.format(calendar.time)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            fontSize = 64.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = date,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$batteryLevel%",
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@PreviewLightDark
@Composable
private fun DigitalClockPreview() {
    CleanPhoneLauncherTheme {
        DigitalClock(showSeconds = true, batteryLevel = 100)
    }
}
