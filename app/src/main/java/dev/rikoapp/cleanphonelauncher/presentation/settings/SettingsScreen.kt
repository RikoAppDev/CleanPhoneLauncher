package dev.rikoapp.cleanphonelauncher.presentation.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.BuildConfig
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.data.WidgetHostManager
import dev.rikoapp.cleanphonelauncher.domain.SettingsRepository
import org.koin.compose.koinInject
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenRoot(
    onClose: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    SettingsScreen(state = state, onAction = viewModel::onAction, onClose = onClose)
}

@Composable
private fun SettingsScreen(
    state: SettingsScreenState,
    onAction: (SettingsScreenAction) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val fg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current

    val widgetManager: WidgetHostManager = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val widgetId by settingsRepository.widgetId.collectAsState()
    var pendingWidgetId by remember { mutableStateOf(-1) }

    val configureWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            settingsRepository.setWidgetId(pendingWidgetId)
        } else {
            widgetManager.deleteId(pendingWidgetId)
        }
    }

    fun confirmOrConfigure(id: Int) {
        val info = widgetManager.getInfo(id)
        if (info?.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }
            runCatching { configureWidgetLauncher.launch(intent) }
                .onFailure { settingsRepository.setWidgetId(id) }
        } else {
            settingsRepository.setWidgetId(id)
        }
    }

    val bindWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) confirmOrConfigure(pendingWidgetId)
        else widgetManager.deleteId(pendingWidgetId)
    }

    val pickWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        when {
            result.resultCode != Activity.RESULT_OK -> if (id != -1) widgetManager.deleteId(id)
            id == -1 -> Unit
            else -> {
                val info = widgetManager.getInfo(id)
                pendingWidgetId = id
                if (info != null && widgetManager.bindIfAllowed(id, info)) {
                    confirmOrConfigure(id)
                } else {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        if (info != null) {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                        }
                    }
                    runCatching { bindWidgetLauncher.launch(intent) }
                        .onFailure { widgetManager.deleteId(id) }
                }
            }
        }
    }

    fun addWidget() {
        val id = widgetManager.allocateId()
        pendingWidgetId = id
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        runCatching { pickWidgetLauncher.launch(intent) }
            .onFailure { widgetManager.deleteId(id) }
    }

    fun removeWidget() {
        if (widgetId != -1) widgetManager.deleteId(widgetId)
        settingsRepository.setWidgetId(-1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = CloseIcon,
                    contentDescription = stringResource(R.string.settings_back),
                    tint = fg
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_title),
                color = fg,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.settings_theme))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                SelectableChip(
                    text = stringResource(mode.displayName),
                    selected = state.themeMode == mode,
                    onClick = { onAction(SettingsScreenAction.OnThemeModeSelected(mode)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.settings_color))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppColorStyle.entries.forEach { style ->
                ColorCircle(
                    color = swatchColor(style),
                    selected = state.colorStyle == style,
                    onClick = { onAction(SettingsScreenAction.OnColorStyleSelected(style)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(state.colorStyle.displayName),
            color = fg.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.settings_privacy))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_crash_reporting),
                    color = fg,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.settings_crash_reporting_desc),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = state.crashReportingEnabled,
                onCheckedChange = { onAction(SettingsScreenAction.OnCrashReportingToggled(it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.settings_notifications))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notification_badges),
                    color = fg,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.notification_badges_desc),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SelectableChip(
                text = stringResource(R.string.grant_access),
                selected = false,
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionLabel(stringResource(R.string.settings_widget))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_widget),
                    color = fg,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.home_widget_desc),
                    color = fg.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SelectableChip(
                text = stringResource(
                    if (widgetId == -1) R.string.add_widget else R.string.remove_widget
                ),
                selected = false,
                onClick = { if (widgetId == -1) addWidget() else removeWidget() }
            )
        }

        if (state.hiddenApps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.hidden_apps_section))
            state.hiddenApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.name,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    SelectableChip(
                        text = stringResource(R.string.unhide),
                        selected = false,
                        onClick = { onAction(SettingsScreenAction.OnUnhideApp(app.packageName)) }
                    )
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.settings_test_crash),
                color = Color(0xFFE5639B),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { throw RuntimeException("Test Crash") }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (BuildConfig.DEBUG) {
                "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            } else {
                "v${BuildConfig.VERSION_NAME}"
            },
            color = fg.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) fg else fg.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .background(if (selected) fg.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ring = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .size(44.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) ring else ring.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun swatchColor(style: AppColorStyle): Color {
    return when (style) {
        AppColorStyle.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).primary
                else dynamicLightColorScheme(context).primary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        }

        AppColorStyle.MONO -> MaterialTheme.colorScheme.onBackground
        else -> style.accent ?: MaterialTheme.colorScheme.onBackground
    }
}
