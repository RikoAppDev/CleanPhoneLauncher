package dev.rikoapp.cleanphonelauncher.presentation.settings

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
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
    val context = LocalContext.current

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
                    color = swatchColor(style, state.accentColor),
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

        if (state.colorStyle == AppColorStyle.CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))
            CustomColorPicker(
                color = state.accentColor,
                onColorChange = { onAction(SettingsScreenAction.OnAccentColorSelected(it)) }
            )
        }

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
private fun swatchColor(style: AppColorStyle, accentColor: Int): Color {
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
        AppColorStyle.CUSTOM -> Color(accentColor)
        else -> style.accent ?: MaterialTheme.colorScheme.onBackground
    }
}

@Composable
private fun CustomColorPicker(
    color: Int,
    onColorChange: (Int) -> Unit
) {
    val seed = remember { FloatArray(3).also { AndroidColor.colorToHSV(color, it) } }
    var hue by rememberSaveable { mutableFloatStateOf(seed[0]) }
    var saturation by rememberSaveable { mutableFloatStateOf(seed[1].coerceIn(0f, 1f)) }
    var brightness by rememberSaveable { mutableFloatStateOf(seed[2].coerceIn(0.15f, 1f)) }

    fun emit() {
        onColorChange(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness)))
    }

    val current = Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness)))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f), CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
                .background(current)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorSlider(
                label = stringResource(R.string.color_custom_hue),
                fraction = hue / 360f,
                trackBrush = Brush.horizontalGradient(HueColors),
                onFraction = { hue = (it * 360f).coerceIn(0f, 360f); emit() }
            )
            ColorSlider(
                label = stringResource(R.string.color_custom_saturation),
                fraction = saturation,
                trackBrush = Brush.horizontalGradient(
                    listOf(
                        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0f, brightness))),
                        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, brightness)))
                    )
                ),
                onFraction = { saturation = it; emit() }
            )
            ColorSlider(
                label = stringResource(R.string.color_custom_brightness),
                fraction = brightness,
                trackBrush = Brush.horizontalGradient(
                    listOf(
                        Color.Black,
                        Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                    )
                ),
                onFraction = { brightness = it; emit() }
            )
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    fraction: Float,
    trackBrush: Brush,
    onFraction: (Float) -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    var trackWidth by remember { mutableFloatStateOf(0f) }
    val thumbPx = with(LocalDensity.current) { 22.dp.toPx() }

    Column {
        Text(
            text = label,
            color = fg.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { trackWidth = it.width.toFloat() }
                .clip(CircleShape)
                .background(trackBrush)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (trackWidth > 0f) onFraction((offset.x / trackWidth).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        if (trackWidth > 0f) onFraction((change.position.x / trackWidth).coerceIn(0f, 1f))
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((fraction * (trackWidth - thumbPx)).roundToInt(), 0) }
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, fg.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

private val HueColors = listOf(
    Color(0xFFFF0000),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF00FFFF),
    Color(0xFF0000FF),
    Color(0xFFFF00FF),
    Color(0xFFFF0000)
)
