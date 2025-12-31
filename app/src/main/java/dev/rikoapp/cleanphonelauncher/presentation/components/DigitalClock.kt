package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier,
    showSeconds: Boolean = true,
    batteryLevel: Int
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(showSeconds) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    val timeFormat = if (showSeconds) "HH:mm:ss" else "HH:mm"
    val time = SimpleDateFormat(timeFormat, Locale.getDefault()).format(calendar.time)
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
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
