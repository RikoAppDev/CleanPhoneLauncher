package dev.rikoapp.cleanphonelauncher.presentation.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    showSeconds: Boolean = true,
    batteryLevel: Int
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val locale = LocalConfiguration.current.locales[0]

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    val dayOfMonth = SimpleDateFormat("d", locale).format(calendar.time)

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val batteryRadius = size.minDimension / 2
        val dialRadius = batteryRadius - 12.dp.toPx()

        // Battery circle
        drawArc(
            color = onBackgroundColor,
            startAngle = -90f,
            sweepAngle = -(batteryLevel / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = 4f)
        )

        // Dial
        for (i in 1..12) {
            val angle = i * 30f
            val isQuarter = i % 3 == 0
            val strokeWidth = if (isQuarter) 6f else 3f
            val lineLength = if (isQuarter) 25f else 15f

            rotate(angle, center) {
                drawLine(
                    color = onBackgroundColor,
                    start = Offset(x = center.x, y = center.y - dialRadius),
                    end = Offset(x = center.x, y = center.y - dialRadius + lineLength),
                    strokeWidth = strokeWidth
                )
            }
        }

        // Date
        drawIntoCanvas {
            val paint = Paint().apply {
                color = onBackgroundColor.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = 32f
            }
            it.nativeCanvas.drawText(dayOfMonth, center.x + dialRadius * 0.6f, center.y + 12, paint)
        }

        // Hour hand
        rotate(degrees = (hours + minutes / 60f) * 30f, pivot = center) {
            drawLine(
                color = onBackgroundColor,
                start = center,
                end = Offset(center.x, center.y - dialRadius * 0.5f),
                strokeWidth = 8f
            )
        }

        // Minute hand
        rotate(degrees = minutes * 6f, pivot = center) {
            drawLine(
                color = onBackgroundColor,
                start = center,
                end = Offset(center.x, center.y - dialRadius * 0.7f),
                strokeWidth = 4f
            )
        }

        // Second hand
        if (showSeconds) {
            rotate(degrees = seconds * 6f, pivot = center) {
                drawLine(
                    color = onBackgroundColor,
                    start = center,
                    end = Offset(center.x, center.y - dialRadius * 0.85f),
                    strokeWidth = 2f
                )
            }
        }

        // Center circle
        drawCircle(
            color = onBackgroundColor,
            radius = 5f,
            center = center
        )
    }
}

@PreviewLightDark
@Composable
private fun AnalogClockPreview() {
    CleanPhoneLauncherTheme {
        AnalogClock(showSeconds = true, batteryLevel = 100)
    }
}