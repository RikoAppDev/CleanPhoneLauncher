package dev.rikoapp.cleanphonelauncher.presentation.widgets

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import org.koin.compose.koinInject

private data class WidgetPickerItem(
    val info: AppWidgetProviderInfo,
    val label: String,
    val appLabel: String
)

@Composable
fun WidgetPickerDialog(
    onPick: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    val widgetManager: WidgetHostManager = koinInject()

    val allItems = remember {
        val pm = context.packageManager
        widgetManager.installedProviders()
            .mapNotNull { info ->
                val label = info.loadLabel(pm)?.trim().orEmpty()
                if (label.isEmpty()) return@mapNotNull null
                val appLabel = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(info.provider.packageName, 0)).toString()
                }.getOrDefault("")
                WidgetPickerItem(info, label, appLabel)
            }
            .sortedWith(compareBy({ it.appLabel.lowercase() }, { it.label.lowercase() }))
    }

    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allItems) {
        if (query.isBlank()) allItems
        else allItems.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.appLabel.contains(query, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.widget_picker_title),
                        color = fg,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = CloseIcon,
                            contentDescription = stringResource(R.string.close),
                            tint = fg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.widget_search)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.info.provider.flattenToString() + it.label }) { item ->
                        WidgetPickerRow(item = item, onClick = { onPick(item.info) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerRow(item: WidgetPickerItem, onClick: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(item.info) {
        runCatching { item.info.loadIcon(context, 0)?.toBitmap()?.asImageBitmap() }.getOrNull()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (icon != null) {
                androidx.compose.foundation.Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                color = fg,
                style = MaterialTheme.typography.bodyLarge
            )
            if (item.appLabel.isNotEmpty() && item.appLabel != item.label) {
                Text(
                    text = item.appLabel,
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
