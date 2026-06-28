# Changelog

All notable changes to this project are documented here.
Format loosely follows [Keep a Changelog](https://keepachangelog.com/); versioning is automatic from commit prefixes (`fix:` → patch, `feat:` → minor, `feat!:` → major).

## [Unreleased]

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
