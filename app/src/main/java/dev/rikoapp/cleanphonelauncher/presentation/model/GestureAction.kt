package dev.rikoapp.cleanphonelauncher.presentation.model

import androidx.annotation.StringRes
import dev.rikoapp.cleanphonelauncher.R

enum class GestureAction(@param:StringRes val displayName: Int) {
    NONE(R.string.gesture_none),
    APP_DRAWER(R.string.gesture_app_drawer),
    NOTIFICATIONS(R.string.gesture_notifications),
    LOCK_SCREEN(R.string.gesture_lock_screen),
    SETTINGS(R.string.gesture_settings)
}
