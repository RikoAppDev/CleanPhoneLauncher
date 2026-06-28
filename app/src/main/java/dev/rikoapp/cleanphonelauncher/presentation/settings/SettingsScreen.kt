package dev.rikoapp.cleanphonelauncher.presentation.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.BuildConfig
import dev.rikoapp.cleanphonelauncher.R
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
            val context = LocalContext.current
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).primary
            else dynamicLightColorScheme(context).primary
        }

        AppColorStyle.MONO -> MaterialTheme.colorScheme.onBackground
        else -> style.accent ?: MaterialTheme.colorScheme.onBackground
    }
}
