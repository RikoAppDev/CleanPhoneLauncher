# Changelog

All notable changes to this project are documented here.
Format loosely follows [Keep a Changelog](https://keepachangelog.com/); versioning is automatic from commit prefixes (`fix:` → patch, `feat:` → minor, `feat!:` → major).

## [Unreleased]

## [1.12.0]

### Added
- **Web-search fallback from the drawer search.** Press the keyboard's Done/Search action when your query matches no app and it runs a web search for that text (falls back to a Google search URL if no search app handles it). Matching an app still launches it directly.

## [1.11.1]

### Fixed
- **Double-tap to lock no longer forces a PIN/password to unlock.** It previously locked via a device-admin policy, which Android always follows with a "strong auth" requirement — disabling fingerprint unlock until the PIN is entered. It now locks through an accessibility service (the same way the power button does), so **fingerprint unlock keeps working**. The first double-tap asks you to enable "CleanPhoneLauncher screen lock" in Accessibility settings; the device-admin permission is no longer used.

## [1.11.0]

### Added
- **Swipe down on the home screen to open the notification shade** (system status bar / quick settings), matching the stock launcher gesture.

### Fixed
- **Double-tap to lock now works.** The "enable device admin" prompt was launched from the application context as a new task, which the system rejects (`Cannot start ADD_DEVICE_ADMIN as a new task`) — so the prompt flashed and vanished and the admin could never be enabled. It is now launched from the activity, so it stays up; once enabled, double-tapping the home screen locks the device.

## [1.10.0]

### Added
- **Searchable widget picker.** Adding a widget now opens an in-app picker with a search box and app icons (grouped by app), instead of the plain system list — so you can find a widget by typing its name or its app.

## [1.9.0]

### Added
- **Dedicated widgets screen** to the left of the home screen. Swipe right from home to reach it. Add **multiple** widgets, tap **Edit** to reorder them (drag handle), **resize** each one (drag the bottom edge), **reconfigure** or **remove** them.

### Changed
- **The home screen now shows only the clock and your favorites** — widgets live on their own screen. The launcher is now a three-pane layout: widgets ← home → app drawer, with home in the middle.

## [1.8.1]

### Fixed
- **Adding a home widget now works on Samsung / One UI.** The widget bind dialog returns a "canceled" result on some devices even after you allow access, which caused the launcher to discard the just-bound widget; the bind state is now checked directly. The Settings screen also stays open through the whole pick → bind → configure flow instead of being dismissed mid-way.

## [1.8.0]

### Added
- **Home screen widget:** add a single app widget shown on the home screen above your favorites, via Settings → Widget → Add widget (pick, bind and configure flow). Remove it from the same place.

## [1.7.0]

### Added
- **Notification badges:** unread notification counts shown on apps in the drawer and on home favorites. Opt-in via Settings → Notifications → Grant access (uses a notification listener; no badges until access is granted).
- **Hidden apps in Settings:** a "Hidden apps" section in Settings lists hidden apps with an Unhide action (in addition to the drawer's "Show hidden apps" row).

## [1.6.1]

### Changed
- Enabled **R8 minification and resource shrinking** for release builds (smaller, obfuscated APK/AAB). ProGuard keep rules preserve enum names used for settings persistence; Crashlytics mapping is uploaded for readable crash reports.

## [1.6.0]

### Changed
- **Lowered `minSdk` to 26 (Android 8.0)** — the launcher now installs on far more devices. Dynamic (Material You) colour and a few newer APIs are guarded with version checks and graceful fallbacks.

## [1.5.2]

### Added
- Unit tests for `SettingsViewModel` and the app-override data source (kotlinx-coroutines-test).

## [1.5.1]

### Changed
- Battery and package broadcast receivers are now **lifecycle-aware** (registered while the app is foregrounded via `ProcessLifecycleOwner`, unregistered in the background) and use `RECEIVER_NOT_EXPORTED`, so they are no longer leaked.

## [1.5.0]

### Changed
- Clocks now react to system **time and timezone changes**, and the no-seconds clock updates once a minute via `ACTION_TIME_TICK` instead of waking every second (more battery-friendly).

## [1.4.0]

### Added
- **Swipe up** on the home screen to open the app drawer.
- **Double-tap** the home screen to lock the device (asks you to enable device admin the first time).

## [1.3.0]

### Added
- **Hide apps** from the drawer (long-press → Hide). A "Show hidden apps" row at the bottom of the list reveals them so you can unhide.
- **Rename apps** (long-press → Rename) with a custom label.
- **App version** shown at the bottom of Settings (with the build code in debug builds).

### Changed
- Local builds now derive their version name from git (e.g. `1.2.0-3-g1a2b3c`), so a dev build is clearly distinguishable from a store release.

## [1.2.0]

### Added
- **Settings screen** (open via the gear in the app drawer): choose **theme mode** (System / Light / Dark) and **color style** (Dynamic Material You, Monochrome, or an accent colour).
- **Optional crash reporting** via Firebase Crashlytics — **off by default**, opt-in from Settings.

### Fixed
- Lint `NonObservableLocale` errors in the clocks (locale is now read observably).

## [1.1.1]

### CI
- Fixed the Play Store deploy: release-notes file must be named `whatsnew-en-US` (no extension) so the language code parses.

## [1.1.0]

### Added
- **Reorder home favorites:** long-press the home background to enter reorder mode, then drag the handle next to a favorite to reorder it. Tap the background or press back to finish. The order is saved and restored across restarts.

### Changed
- Raised `compileSdk` to 37 (required by updated AndroidX dependencies).

## [1.0.5]

### Added
- **Home favorites long-press menu:** long-pressing a favorite on the home screen now opens the same options menu (Remove favorite / App info / Uninstall) as the app drawer, instead of only offering to remove the favorite.

### Fixed
- **Back from the app list now returns to home.** Back was globally swallowed (to stop the launcher being hidden); it now scrolls to the home page from the drawer and is only a no-op when already on home.

### Changed
- App options menu rows now have a rounded, padded ripple instead of a sharp full-bleed rectangle.
- Extracted launch / app-info / uninstall handling into a shared `AppActions` helper so both screens use one implementation.

### CI
- Added a `concurrency` guard so overlapping pushes can't race two releases.
- Made keystore base64 decoding tolerant of CR/whitespace so signed release builds don't fail.

## [1.0.4]

### Fixed
- **Uninstall did nothing:** tapping Uninstall silently returned to the home screen because the app lacked the `REQUEST_DELETE_PACKAGES` permission, so the system denied the request. Added the permission and switched to `ACTION_UNINSTALL_PACKAGE`, which now shows the system uninstall dialog.

## [1.0.3]

### Fixed
- **Tap-to-launch crash & double launch:** the list item called `startActivity` directly with a possibly-`null` intent, crashing when an app was uninstalled and launching every app twice. Launching now goes solely through the guarded ViewModel path; home-screen favorites use the same path.

## [1.0.2]

### Added
- **App long-press menu in the drawer:** long-pressing an app now opens a menu with **Add/Remove favorite**, **App info** (opens system app settings) and **Uninstall**, instead of only the favorite toggle.

### Fixed
- **Recent apps crash:** `UsageStatsManager.queryUsageStats()` can return `null`; calling `.filter` on it threw an NPE. Now null-safe.
- **Clock type crash:** `ClockType.valueOf(stored)` threw `IllegalArgumentException` when a persisted name no longer mapped to an enum. Now falls back safely.
- **Battery percentage:** guarded `scale <= 0` division that produced garbage values; result is clamped to `0..100`.
- **Alphabet scroll crash:** reading `name[0]` threw `StringIndexOutOfBoundsException` for apps with an empty label.
- **Alphabet fast-scroll drag:** the gesture was keyed on `pointerInput(Unit)` and captured the first (empty) app list, so dragging never scrolled. Re-keyed on the loaded list.
- **Alphabet scroll position:** scroll ignored the recent-apps header/divider rows and jumped to the wrong item; now offset correctly.
- **App list not refreshing:** newly installed apps didn't appear and tapping an app uninstalled since load could crash. A `PACKAGE_ADDED/REMOVED/REPLACED` receiver now keeps the list live, and app launch is guarded against the stale window.

### Changed
- **Release signing:** the `release` build type was signed with the **debug** keystore (rejected by Google Play). Now uses an env-driven upload keystore in CI, falling back to debug only for local builds.

### CI
- Enabled the Play Store deploy job (internal track) with keystore decoding and signing env wiring; it self-skips until the required secrets are configured.

[Unreleased]: https://github.com/RikoAppDev/CleanPhoneLauncher/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/RikoAppDev/CleanPhoneLauncher/releases/tag/v1.0.2
