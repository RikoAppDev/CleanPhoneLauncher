package dev.rikoapp.cleanphonelauncher.data

import android.app.Application
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dev.rikoapp.cleanphonelauncher.BuildConfig
import dev.rikoapp.cleanphonelauncher.domain.UpdateCheckResult
import dev.rikoapp.cleanphonelauncher.domain.VersionCheckRepository
import dev.rikoapp.cleanphonelauncher.presentation.version.VersionState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseVersionCheckRepository(
    context: Application
) : VersionCheckRepository {

    private val prefs = context.getSharedPreferences("version_check", Context.MODE_PRIVATE)

    @Volatile
    private var lastSeenLatest = 0L

    override suspend fun check(): VersionState {
        val rc = FirebaseRemoteConfig.getInstance()

        runCatching {
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(MIN_FETCH_INTERVAL_SECONDS)
                .build()
            rc.setConfigSettingsAsync(settings).await()
            rc.setDefaultsAsync(
                mapOf(
                    KEY_MIN to 0L,
                    KEY_LATEST to 0L,
                    KEY_URL to DEFAULT_STORE_URL
                )
            ).await()
            rc.fetchAndActivate().await()
        }

        val current = BuildConfig.VERSION_CODE.toLong()
        val min = rc.getLong(KEY_MIN)
        val latest = rc.getLong(KEY_LATEST)
        val url = rc.getString(KEY_URL).ifBlank { DEFAULT_STORE_URL }
        lastSeenLatest = latest

        return when {
            min > current -> VersionState.ForceUpgrade(url)
            latest > current && prefs.getLong(KEY_WARNED, 0L) < latest -> VersionState.WarnUpgrade(url)
            else -> VersionState.Ok
        }
    }

    override suspend fun forceCheck(): UpdateCheckResult {
        val rc = FirebaseRemoteConfig.getInstance()
        val fetched = runCatching {
            rc.setDefaultsAsync(
                mapOf(
                    KEY_MIN to 0L,
                    KEY_LATEST to 0L,
                    KEY_URL to DEFAULT_STORE_URL
                )
            ).await()
            rc.fetch(0).await()
            rc.activate().await()
        }.isSuccess

        if (!fetched) return UpdateCheckResult.Error

        val current = BuildConfig.VERSION_CODE.toLong()
        val latest = rc.getLong(KEY_LATEST)
        val url = rc.getString(KEY_URL).ifBlank { DEFAULT_STORE_URL }
        lastSeenLatest = latest
        return if (latest > current) {
            UpdateCheckResult.UpdateAvailable(url)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    override fun dismissWarn() {
        prefs.edit().putLong(KEY_WARNED, lastSeenLatest).apply()
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }

    companion object {
        private const val MIN_FETCH_INTERVAL_SECONDS = 3600L
        private const val KEY_MIN = "min_required_version_code"
        private const val KEY_LATEST = "latest_version_code"
        private const val KEY_URL = "update_store_url"
        private const val KEY_WARNED = "warned_version_code"
        private const val DEFAULT_STORE_URL =
            "https://play.google.com/store/apps/details?id=dev.rikoapp.cleanphonelauncher"
    }
}
