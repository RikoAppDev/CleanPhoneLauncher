package dev.rikoapp.cleanphonelauncher.presentation.onboarding

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.presentation.model.AppColorStyle
import dev.rikoapp.cleanphonelauncher.presentation.model.ThemeMode
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.BackIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.BarsIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.BellIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CheckIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.HomeIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.LockIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.PaletteIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.SettingsIcon
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.StarIcon
import org.koin.androidx.compose.koinViewModel

private val GrantedGreen = Color(0xFF4CAF82)

@Composable
fun OnboardingScreenRoot(viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    OnboardingScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun OnboardingScreen(
    state: OnboardingScreenState,
    onAction: (OnboardingScreenAction) -> Unit
) {
    val context = LocalContext.current
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    val statusLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { onAction(OnboardingScreenAction.OnRefreshStatuses) }

    fun open(intent: Intent) {
        runCatching { statusLauncher.launch(intent) }
            .onFailure {
                runCatching {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
    }

    fun requestDefaultHome() {
        val roleIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                runCatching { roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME) }.getOrNull()
            } else null
        } else null
        open(roleIntent ?: Intent(Settings.ACTION_HOME_SETTINGS))
    }

    BackHandler(enabled = true) {
        if (!state.isFirstStep) onAction(OnboardingScreenAction.OnBack)
    }

    if (showAccessibilityDisclosure) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDisclosure = false },
            title = { Text(stringResource(R.string.accessibility_disclosure_title)) },
            text = { Text(stringResource(R.string.accessibility_disclosure_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showAccessibilityDisclosure = false
                    open(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) { Text(stringResource(R.string.accessibility_disclosure_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDisclosure = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!state.isFirstStep) {
                IconButton(
                    onClick = { onAction(OnboardingScreenAction.OnBack) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = BackIcon,
                        contentDescription = stringResource(R.string.settings_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(4.dp))
            } else {
                Spacer(Modifier.width(44.dp))
            }
            ProgressSegments(
                current = state.stepIndex,
                total = state.steps.size,
                modifier = Modifier.weight(1f)
            )
            if (!state.isLastStep) {
                Spacer(Modifier.width(12.dp))
                TextButton(onClick = { onAction(OnboardingScreenAction.OnSkipAll) }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                val dir = if (forward) 1 else -1
                (fadeIn(tween(240)) + slideInHorizontally(tween(240)) { w -> dir * w / 8 }) togetherWith
                    (fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { w -> -dir * w / 8 })
            },
            label = "onboarding-step",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> StepScaffold(
                    hero = StarIcon,
                    title = stringResource(R.string.onboarding_welcome_title),
                    description = stringResource(R.string.onboarding_welcome_desc),
                    primaryLabel = stringResource(R.string.onboarding_welcome_button),
                    onPrimary = { onAction(OnboardingScreenAction.OnNext) }
                )

                OnboardingStep.DEFAULT_LAUNCHER -> {
                    val done = state.isDefaultLauncher
                    StepScaffold(
                        hero = HomeIcon,
                        title = stringResource(R.string.onboarding_default_title),
                        description = stringResource(R.string.onboarding_default_desc),
                        primaryLabel = stringResource(
                            if (done) R.string.onboarding_next else R.string.onboarding_default_button
                        ),
                        onPrimary = {
                            if (done) onAction(OnboardingScreenAction.OnNext) else requestDefaultHome()
                        },
                        secondaryLabel = if (done) null else stringResource(R.string.onboarding_skip_for_now),
                        onSecondary = if (done) null else ({ onAction(OnboardingScreenAction.OnNext) })
                    ) {
                        if (done) {
                            StatusPill(
                                granted = true,
                                label = stringResource(R.string.onboarding_default_done)
                            )
                        }
                    }
                }

                OnboardingStep.PERMISSIONS -> StepScaffold(
                    hero = SettingsIcon,
                    title = stringResource(R.string.onboarding_permissions_title),
                    description = stringResource(R.string.onboarding_permissions_desc),
                    primaryLabel = stringResource(R.string.onboarding_next),
                    onPrimary = { onAction(OnboardingScreenAction.OnNext) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        PermissionCard(
                            icon = BarsIcon,
                            title = stringResource(R.string.onboarding_perm_usage_title),
                            description = stringResource(R.string.onboarding_perm_usage_desc),
                            granted = state.hasUsageAccess,
                            onGrant = { open(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                        )
                        Spacer(Modifier.height(10.dp))
                        PermissionCard(
                            icon = BellIcon,
                            title = stringResource(R.string.notification_badges),
                            description = stringResource(R.string.notification_badges_desc),
                            granted = state.notificationListenerEnabled,
                            onGrant = { open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                        )
                        Spacer(Modifier.height(10.dp))
                        PermissionCard(
                            icon = LockIcon,
                            title = stringResource(R.string.onboarding_perm_lock_title),
                            description = stringResource(R.string.onboarding_perm_lock_desc),
                            granted = state.accessibilityLockEnabled,
                            onGrant = { showAccessibilityDisclosure = true }
                        )
                    }
                }

                OnboardingStep.PERSONALIZE -> StepScaffold(
                    hero = PaletteIcon,
                    title = stringResource(R.string.onboarding_personalize_title),
                    description = stringResource(R.string.onboarding_personalize_desc),
                    primaryLabel = stringResource(R.string.onboarding_next),
                    onPrimary = { onAction(OnboardingScreenAction.OnNext) }
                ) {
                    PersonalizeControls(state = state, onAction = onAction)
                }

                OnboardingStep.DONE -> StepScaffold(
                    hero = CheckIcon,
                    heroTint = GrantedGreen,
                    title = stringResource(R.string.onboarding_done_title),
                    description = stringResource(R.string.onboarding_done_desc),
                    primaryLabel = stringResource(R.string.onboarding_done_button),
                    onPrimary = { onAction(OnboardingScreenAction.OnFinish) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        DoneSummary(state = state)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepScaffold(
    hero: ImageVector,
    title: String,
    description: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    heroTint: Color = MaterialTheme.colorScheme.primary,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val fg = MaterialTheme.colorScheme.onBackground
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            HeroGlyph(icon = hero, tint = heroTint)
            Spacer(Modifier.height(28.dp))
            Text(
                text = title,
                color = fg,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = description,
                color = fg.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(28.dp))
            content()
            Spacer(Modifier.height(20.dp))
        }

        Button(
            onClick = onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Text(primaryLabel)
        }
        if (secondaryLabel != null && onSecondary != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(secondaryLabel)
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HeroGlyph(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(46.dp)
        )
    }
}

@Composable
private fun ProgressSegments(current: Int, total: Int, modifier: Modifier = Modifier) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (i <= current) active else inactive)
            )
        }
    }
}

@Composable
private fun StatusPill(granted: Boolean, label: String) {
    val color = if (granted) GrantedGreen else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, fg.copy(alpha = 0.15f), MaterialTheme.shapes.medium)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = fg, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                color = fg.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.width(12.dp))
        if (granted) {
            StatusPill(granted = true, label = stringResource(R.string.onboarding_status_granted))
        } else {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .border(1.dp, fg.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                    .clickable(onClick = onGrant)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_grant),
                    color = fg,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.PersonalizeControls(
    state: OnboardingScreenState,
    onAction: (OnboardingScreenAction) -> Unit
) {
    val fg = MaterialTheme.colorScheme.onBackground
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_theme),
            color = fg,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.entries.forEach { mode ->
                Chip(
                    text = stringResource(mode.displayName),
                    selected = state.themeMode == mode,
                    onClick = { onAction(OnboardingScreenAction.OnThemeModeSelected(mode)) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.settings_color),
            color = fg,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.width(8.dp))
            AppColorStyle.entries
                .filter { it != AppColorStyle.CUSTOM }
                .forEach { style ->
                    ColorCircle(
                        color = onboardingSwatch(style),
                        selected = state.colorStyle == style,
                        onClick = { onAction(OnboardingScreenAction.OnColorStyleSelected(style)) }
                    )
                }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ColumnScope.DoneSummary(state: OnboardingScreenState) {
    val fg = MaterialTheme.colorScheme.onBackground
    val defaultLabel = stringResource(R.string.onboarding_pending_default)
    val usageLabel = stringResource(R.string.onboarding_perm_usage_title)
    val notifLabel = stringResource(R.string.notification_badges)
    val lockLabel = stringResource(R.string.onboarding_perm_lock_title)

    val pending = buildList {
        if (!state.isDefaultLauncher) add(defaultLabel)
        if (!state.hasUsageAccess) add(usageLabel)
        if (!state.notificationListenerEnabled) add(notifLabel)
        if (!state.accessibilityLockEnabled) add(lockLabel)
    }

    if (pending.isEmpty()) {
        Text(
            text = stringResource(R.string.onboarding_done_all_ready),
            color = fg.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_done_pending_label),
                color = fg.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
            pending.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(fg.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = item, color = fg, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
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
private fun ColorCircle(color: Color, selected: Boolean, onClick: () -> Unit) {
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
private fun onboardingSwatch(style: AppColorStyle): Color {
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
