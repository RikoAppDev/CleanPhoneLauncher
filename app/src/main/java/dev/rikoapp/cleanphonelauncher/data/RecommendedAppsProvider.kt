package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import dev.rikoapp.cleanphonelauncher.R

data class RecommendedApp(val packageName: String, val iconRes: Int)

/**
 * Maps common category apps (phone, camera, messages, …) to our own monochrome
 * vector icons, so quick actions and the picker can show a themed icon that
 * matches the launcher instead of the app's colourful launcher icon.
 */
class RecommendedAppsProvider(private val context: Application) {

    @Volatile
    private var cached: Map<String, Int>? = null

    private fun categoryIntents(): List<Pair<Intent, Int>> = listOf(
        Intent(Intent.ACTION_DIAL) to R.drawable.ic_phone,
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) to R.drawable.ic_camera,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING) to R.drawable.ic_message,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_GALLERY) to R.drawable.ic_image,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER) to R.drawable.ic_public,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS) to R.drawable.ic_person,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_EMAIL) to R.drawable.ic_mail,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MAPS) to R.drawable.ic_map,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC) to R.drawable.ic_music_note,
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR) to R.drawable.ic_calendar,
        Intent(AlarmClock.ACTION_SHOW_ALARMS) to R.drawable.ic_schedule,
        Intent(Settings.ACTION_SETTINGS) to R.drawable.ic_settings
    )

    /** Package name → our icon resource, for the resolved default app of each category. */
    fun categoryIcons(): Map<String, Int> = cached ?: compute().also { cached = it }

    private fun compute(): Map<String, Int> {
        val pm = context.packageManager
        val map = LinkedHashMap<String, Int>()
        categoryIntents().forEach { (intent, icon) ->
            val pkg = runCatching {
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            }.getOrNull()
            if (pkg != null && pkg != "android" && pkg !in map) {
                map[pkg] = icon
            }
        }
        return map
    }

    /** Category apps in a stable order, for the "Recommended" picker section. */
    fun recommended(): List<RecommendedApp> = categoryIcons().map { RecommendedApp(it.key, it.value) }
}
