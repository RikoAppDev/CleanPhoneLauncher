package dev.rikoapp.cleanphonelauncher.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import dev.rikoapp.cleanphonelauncher.R

val CameraIcon: ImageVector
    @Composable
    get() = ImageVector.vectorResource(id = R.drawable.ic_camera)

val CloseIcon: ImageVector
    @Composable
    get() = ImageVector.vectorResource(id = R.drawable.ic_close)


val PhoneIcon: ImageVector
    @Composable
    get() = ImageVector.vectorResource(id = R.drawable.ic_phone)

val DragHandleIcon: ImageVector
    @Composable
    get() = ImageVector.vectorResource(id = R.drawable.ic_drag_handle)

val SettingsIcon: ImageVector
    @Composable
    get() = ImageVector.vectorResource(id = R.drawable.ic_settings)