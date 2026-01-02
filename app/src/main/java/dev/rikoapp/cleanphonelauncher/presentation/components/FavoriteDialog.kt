package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

@Composable
fun FavoriteDialog(
    app: AppData,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.name) },
        text = { Text(if (isFavorite) stringResource(R.string.remove_from_favorites_question) else stringResource(R.string.add_to_favorites_question)) },
        shape = MaterialTheme.shapes.medium,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.yes),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.no),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun FavoriteDialogPreview() {
    CleanPhoneLauncherTheme {
        FavoriteDialog(
            app = AppData(name = "WhatsApp", packageName = "com.whatsapp"),
            isFavorite = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}