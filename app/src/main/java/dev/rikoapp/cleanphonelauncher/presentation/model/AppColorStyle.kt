package dev.rikoapp.cleanphonelauncher.presentation.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import dev.rikoapp.cleanphonelauncher.R

enum class AppColorStyle(
    @param:StringRes val displayName: Int,
    val accent: Color?
) {
    DYNAMIC(R.string.color_dynamic, null),
    MONO(R.string.color_mono, null),
    BLUE(R.string.color_blue, Color(0xFF5B8DEF)),
    GREEN(R.string.color_green, Color(0xFF4CAF82)),
    AMBER(R.string.color_amber, Color(0xFFE0A33E)),
    PINK(R.string.color_pink, Color(0xFFE5639B)),
    PURPLE(R.string.color_purple, Color(0xFF9B7BE5))
}
