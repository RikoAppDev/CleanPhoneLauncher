package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * A "liquid" worm-style page indicator: inactive dots with a single pill that
 * stretches from the current dot toward the next as [position] moves between pages.
 *
 * @param position continuous page position (page + offset fraction), in [0, pageCount - 1].
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    position: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 7.dp,
    dotSpacing: Dp = 10.dp
) {
    if (pageCount <= 1) return

    val totalWidth = dotSize * pageCount + dotSpacing * (pageCount - 1)
    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(dotSize)
    ) {
        val diameter = dotSize.toPx()
        val radius = diameter / 2f
        val step = diameter + dotSpacing.toPx()
        val cy = size.height / 2f

        for (i in 0 until pageCount) {
            drawCircle(
                color = inactiveColor,
                radius = radius,
                center = Offset(radius + i * step, cy)
            )
        }

        val clamped = position.coerceIn(0f, (pageCount - 1).toFloat())
        val base = floor(clamped).toInt().coerceAtMost(pageCount - 2).coerceAtLeast(0)
        val fraction = (clamped - base).coerceIn(0f, 1f)

        // Worm: the leading edge races ahead in the first half, the trailing edge
        // catches up in the second half, so the pill stretches then contracts.
        val headOffset = (fraction * 2f).coerceIn(0f, 1f)
        val tailOffset = (fraction * 2f - 1f).coerceIn(0f, 1f)

        val headCenter = radius + (base + headOffset) * step
        val tailCenter = radius + (base + tailOffset) * step

        drawRoundRect(
            color = activeColor,
            topLeft = Offset(tailCenter - radius, cy - radius),
            size = Size(width = (headCenter - tailCenter) + diameter, height = diameter),
            cornerRadius = CornerRadius(radius, radius)
        )
    }
}
