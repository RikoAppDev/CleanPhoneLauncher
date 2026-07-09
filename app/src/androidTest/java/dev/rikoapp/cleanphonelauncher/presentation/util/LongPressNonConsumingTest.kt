package dev.rikoapp.cleanphonelauncher.presentation.util

import android.widget.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The hosted [Button] stands in for an `AppWidgetHostView`: both reach Android through the
 * same interop path, which consumes the touches it handles.
 */
@RunWith(AndroidJUnit4::class)
class LongPressNonConsumingTest {

    @get:Rule
    val rule = createComposeRule()

    private var hostedClicks = 0
    private var longPresses = 0

    private fun setContent(editMode: Boolean = false) {
        rule.setContent {
            Box(
                modifier = Modifier
                    .testTag(PAGE)
                    .size(300.dp)
                    .pointerInput(Unit) {
                        detectLongPressNonConsuming(enabled = { !editMode }) { longPresses++ }
                    }
            ) {
                AndroidView(
                    factory = { context -> Button(context).apply { setOnClickListener { hostedClicks++ } } },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }

    @Test
    fun tapReachesTheHostedView() {
        setContent()

        rule.onNodeWithTag(PAGE).performTouchInput { click() }
        rule.waitForIdle()

        assertEquals("hosted view should receive the tap", 1, hostedClicks)
        assertEquals("a tap is not a long press", 0, longPresses)
    }

    @Test
    fun longPressFiresAndTheHostedViewIsNotClicked() {
        setContent()

        rule.onNodeWithTag(PAGE).performTouchInput { longClick() }
        rule.waitForIdle()

        assertEquals("long press should fire over the hosted view", 1, longPresses)
        assertEquals("release must not reach the hosted view as a click", 0, hostedClicks)
    }

    @Test
    fun disabledDetectorLeavesTheGestureToTheHostedView() {
        setContent(editMode = true)

        rule.onNodeWithTag(PAGE).performTouchInput { longClick() }
        rule.waitForIdle()

        assertEquals("detector is off", 0, longPresses)
        assertEquals("hosted view keeps the whole gesture", 1, hostedClicks)
    }

    private companion object {
        const val PAGE = "page"
    }
}
