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
- **Custom accent colour:** a **Custom** colour style with a free HSV colour picker (hue / saturation / brightness sliders + live preview), not just the preset swatches.
- **First-run onboarding:** guided setup wizard (welcome → set as default home → optional permissions → theme/colour → done) with live per-permission status pills, skip/do-later on every step, and a re-openable **Settings → Setup** entry.
- **Configurable gestures:** a **Gestures** section in Settings to choose what swipe-up / swipe-down / double-tap on home do (drawer, notification shade, lock, settings, or nothing).
- **App shortcuts:** long-press an app in the drawer to see its static/dynamic/pinned shortcuts (via `LauncherApps`), shown above the options menu; available when set as the default home app. *(shipped; still wants on-device verification across OEMs)*
- **Contacts search (opt-in):** enable it in Settings → Search (grants `READ_CONTACTS`) to surface matching contacts under the app results in the drawer search; tap to open the contact. Off by default. *(shipped; wants on-device verification)*

## 🎯 Near term (next few releases)

### Onboarding follow-ups
The core first-run wizard shipped (see *Recently shipped*). Remaining polish:
- [ ] **OEM settings fallbacks** — verify the default-home / usage-access / notification-listener / accessibility deep-links land on the right screen on Samsung One UI, Xiaomi/MIUI, etc., with graceful fallbacks where a surface is missing. *(needs on-device testing)*
- [ ] **Reduced-motion** — skip the step slide/fade transitions when the system animation scale is 0.
- [ ] **Existing-user gating** — currently the wizard shows once for everyone on update (one-tap skippable); consider suppressing it for installs that already look set up.
- [ ] **Onboarding in the accessibility/TalkBack pass** (tracked under *Polish → Accessibility pass*).

### Polish
- [ ] **Accessibility pass** — verify TalkBack labels / focus order across drawer, home, settings, onboarding.

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

- [ ] **Update the Data Safety form** for crash diagnostics (opt-in Crashlytics), the notification-listener access used for badges, and the new opt-in `READ_CONTACTS` used for contacts search.
- [ ] **Declare accessibility-service use.** Double-tap-to-lock now uses an `AccessibilityService` (`GLOBAL_ACTION_LOCK_SCREEN`). Play scrutinises accessibility use — provide the prominent-disclosure / declaration before promoting beyond internal testing, or gate the feature.
- [ ] **Promote the track** (`internal` → `beta` / `production` in `release.yml`, or in the Console) when ready; consider a **closed beta** first.

---

*Versioning is automatic: `fix:` → patch, `feat:` → minor, `feat!:`/`BREAKING CHANGE` → major. A push to `main` calculates the version, tags it, creates a GitHub release, and deploys to the Play internal track.*
