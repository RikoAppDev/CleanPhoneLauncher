package dev.rikoapp.cleanphonelauncher.presentation.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Detects a double tap without consuming any pointer events, so child clickables
 * keep their ripple and click behaviour. The built-in [detectTapGestures] consumes
 * the down/up, which cancels the press interaction of clickable rows underneath and
 * suppresses their ripple.
 */
suspend fun PointerInputScope.detectDoubleTapNonConsuming(onDoubleTap: () -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        waitForUpOrCancellation() ?: return@awaitEachGesture
        val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
            awaitFirstDown(requireUnconsumed = false)
        }
        if (secondDown != null) onDoubleTap()
    }
}

/**
 * Detects a long press from an *ancestor* of the content it guards, leaving every touch
 * free to reach that content — including interop views such as an app-widget host.
 *
 * Such views consume the events they handle, so consumption is deliberately ignored while
 * waiting; bailing out on it (as [waitForUpOrCancellation] does) would mean the long press
 * never fires over an interactive widget.
 *
 * Once the long press fires, the rest of the gesture is consumed on the Initial pass. The
 * pass matters: an interop view forwards to Android on the Main pass, which reaches the leaf
 * before any ancestor, so consuming on Main would arrive after the release already landed as
 * a click. Consuming on Initial turns it into an ACTION_CANCEL instead.
 *
 * [enabled] is re-checked per gesture, so callers can switch the detector off without
 * restarting the `pointerInput` coroutine — restarting it would cancel the consuming loop
 * below the moment [onLongPress] flips the caller's state.
 */
suspend fun PointerInputScope.detectLongPressNonConsuming(
    enabled: () -> Boolean = { true },
    onLongPress: () -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (!enabled()) return@awaitEachGesture
        var longPressed = false
        try {
            withTimeout(viewConfiguration.longPressTimeoutMillis) {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) break
                }
            }
        } catch (_: PointerEventTimeoutCancellationException) {
            longPressed = true
        }
        if (!longPressed) return@awaitEachGesture
        onLongPress()
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            event.changes.forEach { it.consume() }
        } while (event.changes.any { it.pressed })
    }
}
