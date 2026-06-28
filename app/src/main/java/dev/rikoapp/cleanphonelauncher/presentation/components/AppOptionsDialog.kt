package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

@Composable
fun AppOptionsDialog(
    app: AppData,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    isHidden: Boolean = false,
    onRename: (() -> Unit)? = null,
    onToggleHidden: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.name) },
        text = {
            Column {
                OptionRow(
                    text = stringResource(
                        if (isFavorite) R.string.remove_from_favorites
                        else R.string.add_to_favorites
                    ),
                    onClick = onToggleFavorite
                )
                if (onRename != null) {
                    OptionRow(text = stringResource(R.string.rename), onClick = onRename)
                }
                if (onToggleHidden != null) {
                    OptionRow(
                        text = stringResource(
                            if (isHidden) R.string.unhide else R.string.hide
                        ),
                        onClick = onToggleHidden
                    )
                }
                OptionRow(
                    text = stringResource(R.string.app_info),
                    onClick = onAppInfo
                )
                OptionRow(
                    text = stringResource(R.string.uninstall),
                    onClick = onUninstall
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@Composable
private fun OptionRow(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    )
}

@PreviewLightDark
@Composable
private fun AppOptionsDialogPreview() {
    CleanPhoneLauncherTheme {
        AppOptionsDialog(
            app = AppData(name = "WhatsApp", packageName = "com.whatsapp"),
            isFavorite = false,
            onDismiss = {},
            onToggleFavorite = {},
            onAppInfo = {},
            onUninstall = {}
        )
    }
}
