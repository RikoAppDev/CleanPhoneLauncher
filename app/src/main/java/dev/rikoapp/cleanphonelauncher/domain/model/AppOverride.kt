package dev.rikoapp.cleanphonelauncher.domain.model

data class AppOverride(
    val packageName: String,
    val hidden: Boolean,
    val customName: String?
)
