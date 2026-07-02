package dev.rikoapp.cleanphonelauncher.presentation.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
 * Detects a long press without consuming pointer events, so children (e.g. an
 * app-widget host view) keep receiving touches. Fires when the pointer stays down
 * past the long-press timeout without being released or moved out.
 */
suspend fun PointerInputScope.detectLongPressNonConsuming(onLongPress: () -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        try {
            withTimeout(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation()
            }
            // Released (or cancelled) before the timeout — not a long press.
        } catch (_: PointerEventTimeoutCancellationException) {
            onLongPress()
        }
    }
}
