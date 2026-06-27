# CleanPhoneLauncher — Roadmap

## ✅ This release — bug fixes & release hardening

Fixed in the current change set:

| # | Type | Issue | Fix |
|---|------|-------|-----|
| 1 | 🔴 Crash | `UsageStatsManager.queryUsageStats()` can return `null`; calling `.filter` on it threw an NPE and could crash the app list. | Null-safe via `.orEmpty()` in `RecentAppsRepositoryImpl`. |
| 2 | 🔴 Crash | `ClockType.valueOf(stored)` threw `IllegalArgumentException` if a persisted clock-type name no longer maps to an enum (e.g. after a rename). | Safe lookup via `ClockType.entries.firstOrNull { … } ?: ANALOG_WITH_SECONDS`. |
| 3 | 🟠 Bug | Battery calc divided by `scale` without guarding `scale <= 0`, producing garbage `Int.MAX_VALUE`-style values when battery info was unavailable. | Guard `level >= 0 && scale > 0`, result `coerceIn(0, 100)`. |
| 4 | 🟠 Crash | Alphabet fast-scroll read `it.name[0]` — `StringIndexOutOfBoundsException` for any app with an empty label. | `it.name.firstOrNull()?.isLetter() != true`. |
| 5 | 🟠 Bug | Alphabet drag gesture was keyed on `pointerInput(Unit)`, capturing the **first (empty)** app list — fast-scroll drag never worked. | Re-keyed on `state.allApps` so the gesture rebinds when data loads. |
| 6 | 🟠 Bug | Alphabet scroll ignored the recent-apps header/divider rows, jumping to the wrong list position. | Added `leadingItemCount` offset to `scrollToItem`. |
| 7 | 🔴 **Release blocker** | The `release` build type was signed with the **debug** keystore — Google Play rejects debug-signed bundles outright. | Env-driven release signing config; CI signs with the real upload key, local builds fall back to debug. |
| 8 | 🟢 CI | Play Store deploy job was commented out. | Enabled (self-skips until secrets are set); keystore decode + signing env wired into `release.yml`. |
| 9 | 🔴 Bug | App list never refreshed: newly installed apps didn't appear, and clicking an app uninstalled since load could crash. | `PACKAGE_ADDED/REMOVED/REPLACED` broadcast receiver re-loads the list; `launchApp` wrapped in try/catch + triggers a refresh. |

Both `assembleDebug` and `assembleRelease` build green.

---

## 🚀 Releasing to Google Play (one-time setup)

The pipeline (`.github/workflows/release.yml`) already builds a signed AAB and can push it to the **internal** testing track. To activate it:

### 1. Generate an upload keystore (once, keep it safe & backed up)
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias upload
base64 -w0 release-keystore.jks > keystore-base64.txt
```

### 2. Add GitHub repo secrets (Settings → Secrets and variables → Actions)
| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | contents of `keystore-base64.txt` |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | key password |
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | full JSON of a Play service account |

### 3. Create the Play service account
Google Cloud Console → enable **Google Play Android Developer API** → create a service account + JSON key → in Play Console (Users & permissions) invite that service-account email and grant release permissions.

### 4. ⚠️ First release must be manual
Google Play's API can only **update** an app that already has at least one release. Build the first AAB locally (or download it from the GitHub release) and upload it once by hand in the Play Console. After that, every push to `main` auto-deploys to the internal track.

### 5. Promote tracks
Change `track: internal` → `alpha` / `beta` / `production` in `release.yml` when ready, or promote manually in the Console.

---

## 🔭 Next up (known issues & improvements)

### Correctness / robustness
- [ ] **Broadcast receivers are never unregistered** (battery + package). Acceptable for an always-running launcher, but consider lifecycle-aware registration to be tidy.
- [ ] **Clocks don't react to system time / timezone changes.** The 1s tick loop only re-reads the clock; listen for `ACTION_TIME_CHANGED` / `ACTION_TIMEZONE_CHANGED`.
- [ ] **Clock persistence is fire-and-forget SharedPreferences.** Consider DataStore for the (small) settings surface.

### Quality / tooling
- [ ] **Enable R8/minify + resource shrinking** for the release build (smaller download, obfuscation). Currently `isMinifyEnabled = false`.
- [ ] **ViewModel unit tests.** Only the template `ExampleUnitTest` exists; add coverage for `AppListViewModel` / `HomeViewModel` (skill: android-testing — Turbine + JUnit5 + fakes).
- [ ] **Lower `minSdk`** (currently 33 / Android 13+) if broader reach matters — most APIs used have older equivalents.

### Features
- [ ] Per-app rename / hide from drawer.
- [ ] Reorder favorites (drag & drop) + persist order (the entity currently stores only package name).
- [ ] Widget / notification-count support.
- [ ] Theming options (accent color, wallpaper-adaptive).
- [ ] Gesture shortcuts (swipe up = drawer, double-tap = lock).

---

*Versioning is automatic: `fix:` → patch, `feat:` → minor, `feat!:`/`BREAKING CHANGE` → major. A push to `main` calculates the version, tags it, creates a GitHub release, and (once secrets are set) deploys to Play internal.*
