package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.rikoapp.cleanphonelauncher.domain.AppData
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

@Composable
fun AppListItem(app: AppData) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.small)
            .clickable {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                context.startActivity(intent)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.name,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    CleanPhoneLauncherTheme {
        AppListItem(
            app = AppData(
                name = "WhatsApp",
                packageName = "com.whatsapp"
            )
        )
    }
}