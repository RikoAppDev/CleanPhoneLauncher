package dev.rikoapp.cleanphonelauncher.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.rikoapp.cleanphonelauncher.domain.model.AppData
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme

@Composable
fun AppListItem(
    app: AppData,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    badgeCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick
            )
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
        if (badgeCount > 0) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                color = MaterialTheme.colorScheme.background,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
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
            ),
            onAppClick = {},
            onAppLongClick = {}
        )
    }
}