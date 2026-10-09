package dev.rikoapp.cleanphonelauncher.presentation.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.rikoapp.cleanphonelauncher.R

// Google Play's prominent-disclosure rules require explicit consent before the user is sent to
// Accessibility settings, so dismissing or declining must never open them.
@Composable
fun AccessibilityDisclosureDialog(
    onAgree: (Intent) -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.accessibility_disclosure_title)) },
        text = {
            Text(
                text = stringResource(R.string.accessibility_disclosure_message),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = { onAgree(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                Text(stringResource(R.string.accessibility_disclosure_agree))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.accessibility_disclosure_decline))
            }
        }
    )
}
