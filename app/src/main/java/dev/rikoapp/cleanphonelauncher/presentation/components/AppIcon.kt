package dev.rikoapp.cleanphonelauncher.presentation.components

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import dev.rikoapp.cleanphonelauncher.R
import dev.rikoapp.cleanphonelauncher.data.RecommendedAppsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * A themed, monochrome app icon that matches the launcher instead of the app's
 * colourful launcher icon:
 *  - known category apps (phone, camera, messages, …) use our own vector icon,
 *  - other apps use their adaptive-icon monochrome layer (Android 13+) tinted to
 *    [tint], falling back to a generic glyph when none is available.
 */
@Composable
fun AppIcon(
    packageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    val provider = koinInject<RecommendedAppsProvider>()
    val iconRes = remember(packageName) { provider.categoryIcons()[packageName] }

    if (iconRes != null) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
        return
    }

    val context = LocalContext.current
    val tintArgb = tint.toArgb()
    val themed by produceState<ImageBitmap?>(initialValue = null, packageName, tintArgb) {
        value = withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                monochromeBitmap(context, packageName, tintArgb)
            } else {
                null
            }
        }
    }

    val bitmap = themed
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = contentDescription, modifier = modifier)
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_apps),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun monochromeBitmap(context: Context, packageName: String, tintArgb: Int): ImageBitmap? =
    runCatching {
        val adaptive = context.packageManager.getApplicationIcon(packageName) as? AdaptiveIconDrawable
        adaptive?.monochrome?.let { mono ->
            mono.mutate()
            mono.setTint(tintArgb)
            mono.toBitmap(width = 144, height = 144).asImageBitmap()
        }
    }.getOrNull()
