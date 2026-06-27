# Changelog

All notable changes to this project are documented here.
Format loosely follows [Keep a Changelog](https://keepachangelog.com/); versioning is automatic from commit prefixes (`fix:` → patch, `feat:` → minor, `feat!:` → major).

## [Unreleased]

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
