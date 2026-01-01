package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.presentation.ClockType
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

@Composable
fun ClockTypeDialog(
    currentClockType: ClockType,
    onDismiss: () -> Unit,
    onConfirm: (ClockType) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentClockType) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.clock_type_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClockType.entries.forEach { clockType ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.small)
                        .selectable(
                            selected = (clockType == selectedType),
                            onClick = {
                                selectedType = clockType
                                onConfirm(clockType)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = (clockType == selectedType),
                        onClick = { selectedType = clockType },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.onBackground,
                            unselectedColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = clockType.displayName,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ClockTypeDialogAnalogPreview() {
    CleanPhoneLauncherTheme {
        ClockTypeDialog(
            currentClockType = ClockType.ANALOG,
            onDismiss = {},
            onConfirm = {}
        )
    }
}