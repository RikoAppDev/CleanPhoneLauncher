package dev.rikoapp.cleanphonelauncher.presentation.model

import androidx.annotation.StringRes
import dev.rikoapp.cleanphonelauncher.R

enum class ClockType(@StringRes val displayName: Int) {
    ANALOG(R.string.clock_type_analog),
    ANALOG_WITH_SECONDS(R.string.clock_type_analog_with_seconds),
    DIGITAL(R.string.clock_type_digital),
    DIGITAL_WITH_SECONDS(R.string.clock_type_digital_with_seconds)
}