package dev.rikoapp.cleanphonelauncher.domain.model

data class HomeWidget(
    val appWidgetId: Int,
    val position: Int = 0,
    val heightDp: Int = 180
)
