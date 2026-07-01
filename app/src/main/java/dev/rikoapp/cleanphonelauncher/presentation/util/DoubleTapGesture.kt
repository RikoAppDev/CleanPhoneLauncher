package dev.rikoapp.cleanphonelauncher.presentation.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.PointerInputScope
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
