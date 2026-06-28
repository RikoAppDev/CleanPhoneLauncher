package dev.rikoapp.cleanphonelauncher.presentation.model

import androidx.annotation.StringRes
import dev.rikoapp.cleanphonelauncher.R

enum class ThemeMode(@param:StringRes val displayName: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}
