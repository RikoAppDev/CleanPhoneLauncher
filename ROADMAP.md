# CleanPhoneLauncher — Roadmap

> Forward-looking plan only. For what already shipped, see [CHANGELOG.md](CHANGELOG.md).
>
> The 1.0 → 1.12 line delivered the foundations and most of the original "next up"
> list: stable app drawer, favorites with reorder, hide/rename, theming, optional
> crash reporting, notification badges, a dedicated resizable multi-widget screen
> with a searchable in-app picker, home/drawer gestures (swipe-up drawer,
> swipe-down notification shade, double-tap lock), web-search fallback, and a
> fully automated signed build → GitHub release → Play (internal) pipeline.

## ✅ Recently shipped (was on this roadmap)

- **Widgets:** dedicated screen left of home; multiple widgets; per-widget resize; reorder; reconfigure/remove; searchable in-app picker. *(home now shows only the clock + favorites)*
- **Gestures:** swipe down on home opens the notification shade; double-tap to lock now uses an **accessibility service** (power-button-style), so fingerprint unlock keeps working.
- **Search & launch:** keyboard Done launches the top result; **web-search fallback** when nothing matches.
- **Force-update:** Firebase Remote Config–driven blocking "Update required" screen + optional "Update available" notice.
- **Localization:** complete Slovak translation.
- **Play compliance:** dropped `QUERY_ALL_PACKAGES` (uses `<queries>`), accessibility prominent-disclosure dialog, native debug symbols.

## 🎯 Near term (next few releases)

### Search & launch
- [ ] **App shortcuts** (long-press an app → static/dynamic shortcuts via `LauncherApps` / `ShortcutManager`). *(needs on-device testing)*
- [ ] **Contacts search** in the drawer (opt-in `READ_CONTACTS`, surface matching contacts alongside apps). *(needs on-device testing)*

### Polish
- [ ] **Custom accent colour picker** (free colour, not just the preset swatches).
- [ ] **Localization** — Slovak (`sk`) translation of `strings.xml`.
- [ ] **Accessibility pass** — verify TalkBack labels / focus order across drawer, home, settings.
- [ ] **Configurable gestures** — a Settings section to choose what double-tap / swipe-up / swipe-down do (and turn each off).

## 🌱 Later / exploratory

- [ ] **Backup & restore** of settings (favorites, hidden apps, renames, widgets, theme) via export/import JSON.
- [ ] **App drawer organization** — folders / categories, and per-app "hide from search".
- [ ] **Work profile / private space** apps surfaced correctly (multi-profile `LauncherApps`).
- [ ] **Icon pack** support and an optional font choice for the clock/labels.

## 🔧 Technical follow-ups

- [ ] **Baseline Profiles** for faster cold start.
- [ ] **Instrumented Compose UI / screenshot tests** for the drawer and home (unit coverage exists; on-device coverage doesn't). *(CI would need an emulator job)*
- [x] **Firebase BoM** — done. Adding Remote Config (for force-update) made a second Firebase dependency worthwhile, so Crashlytics + Remote Config now share the BoM.
- [ ] **Settings storage** — still SharedPreferences. DataStore *deliberately deferred*: the surface is tiny and a synchronous theme read at startup avoids a first-frame flicker an async DataStore read would introduce.

## 🚀 Play Store next steps (manual / product)

The signed build → GitHub release → Play **internal** deploy is automated on every push to `main`. Remaining steps need the Play Console or a product decision and can't be done from code:

- [ ] **Update the Data Safety form** for crash diagnostics (opt-in Crashlytics) and the notification-listener access used for badges.
- [ ] **Declare accessibility-service use.** Double-tap-to-lock now uses an `AccessibilityService` (`GLOBAL_ACTION_LOCK_SCREEN`). Play scrutinises accessibility use — provide the prominent-disclosure / declaration before promoting beyond internal testing, or gate the feature.
- [ ] **Promote the track** (`internal` → `beta` / `production` in `release.yml`, or in the Console) when ready; consider a **closed beta** first.

---

*Versioning is automatic: `fix:` → patch, `feat:` → minor, `feat!:`/`BREAKING CHANGE` → major. A push to `main` calculates the version, tags it, creates a GitHub release, and deploys to the Play internal track.*
