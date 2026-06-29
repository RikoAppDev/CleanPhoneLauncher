# CleanPhoneLauncher — Roadmap

> Forward-looking plan only. For what already shipped, see [CHANGELOG.md](CHANGELOG.md).
>
> The 1.0 → 1.8 line delivered the foundations: stable app drawer, favorites with
> drag-and-drop reorder, hide/rename, a settings screen with theming, optional
> crash reporting, notification badges, a home widget, gesture shortcuts, and a
> fully automated signed build → GitHub release → Play (internal track) pipeline.
> This roadmap is what comes next.

## 🎯 Near term (next few releases)

### Widgets
- [ ] **Resize the home widget** instead of a single fixed size (honour the provider's min/max cells; respect `OPTION_APPWIDGET_*` sizing).
- [ ] **Multiple widgets** on the home screen, with reordering, rather than one slot.
- [ ] Graceful empty/error state when a widget's host app is uninstalled (currently the slot can render blank).

### Search & launch
- [ ] **App shortcuts** (long-press an app → static/dynamic shortcuts via `LauncherApps` / `ShortcutManager`).
- [ ] **Contacts search** and an optional **web-search fallback** from the drawer search box.
- [ ] Keyboard "go" on a single search result launches it directly.

### Polish
- [ ] **Custom accent colour picker** (free colour, not just the preset swatches).
- [ ] **Localization** — extract any remaining hard-coded strings and add a Slovak (`sk`) translation; English is the only locale today.
- [ ] **Accessibility pass** — verify TalkBack labels/focus order across the drawer, home, and settings.

## 🌱 Later / exploratory

- [ ] **Backup & restore** of settings (favorites, hidden apps, renames, theme) via export/import — useful before a device switch.
- [ ] **App drawer organization** — folders or categories, and per-app "hide from search".
- [ ] **Work profile / private space** apps surfaced correctly (multi-profile `LauncherApps`).
- [ ] **Icon pack** support and an optional font choice for the clock/labels.
- [ ] **Configurable gestures** — let the user choose what double-tap / swipe actions do, beyond the current lock/drawer defaults.

## 🔧 Technical follow-ups

- [ ] **Migrate Firebase deps to the BoM** (`firebase-bom`) so Crashlytics/analytics versions stay aligned — noted but deferred.
- [ ] **Instrumented Compose UI tests** + a couple of **screenshot tests** for the drawer and home; unit coverage exists, on-device UI coverage doesn't yet.
- [ ] **Baseline Profiles** for faster cold start (the launcher is the first thing the user sees after unlock).
- [ ] **Settings storage:** still SharedPreferences. DataStore was *deliberately deferred* — the settings surface is tiny and reading theme synchronously at startup avoids a first-frame theme flicker that an async DataStore read would introduce. Revisit only if settings grow or a reactive/async store becomes worth the trade-off.

## 🚀 Play Store next steps

The signed build → GitHub release → Play **internal** track deploy is fully automated on every push to `main`. Remaining manual/product steps:

- [ ] **Update the Play Data Safety form** to declare crash diagnostics (Crashlytics — collected only when the user opts in) and the notification-listener access used for badges, before promoting beyond internal testing.
- [ ] **Promote the track** when ready: change `track: internal` → `beta` / `production` in `release.yml`, or promote a build manually in the Console.
- [ ] Consider a **closed beta** group for wider real-device feedback before production.

---

*Versioning is automatic: `fix:` → patch, `feat:` → minor, `feat!:`/`BREAKING CHANGE` → major. A push to `main` calculates the version, tags it, creates a GitHub release, and deploys to the Play internal track.*
