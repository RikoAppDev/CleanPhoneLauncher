# CleanPhoneLauncher

A minimalist Android home-screen launcher (it replaces the system launcher): clock,
favorites list, an alphabetical app drawer with search, bottom quick-action shortcuts,
a multi-page widget host, swipe/double-tap gestures, onboarding, and theming.
Application id / namespace: `dev.rikoapp.cleanphonelauncher`.

## Tech stack

- **Kotlin + Jetpack Compose (Material 3)**, single `Activity` (`MainActivity`), single
  `Application` (`MainApplication`).
- **SDK:** min 26 · target 36 · compile 37. JVM target 11 (CI builds on JDK 17).
- **DI:** Koin.
- **Persistence:** Room (favorites, per-app overrides, widgets) + SharedPreferences (settings).
- **Async:** Coroutines + `StateFlow`.
- **Firebase:** Crashlytics (opt-in crash reports) and Remote Config (version / force-upgrade checks).
- **Build:** Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`); KSP for Room.

## Architecture — layered MVI

Everything lives under `app/src/main/java/dev/rikoapp/cleanphonelauncher/`:

- **`presentation/`** — Compose UI + ViewModels, one package per feature (`home`, `applist`,
  `widgets`, `settings`, `onboarding`, `version`) plus shared `components/`, `ui/theme/`,
  `model/`, and `util/`. `LauncherPager.kt` is the top-level pager.
- **`domain/`** — repository *interfaces* and plain models (`domain/model/`). Keep framework
  types out of here where practical.
- **`data/`** — repository/data-source *implementations*, Room (`data/database/`: entities, DAOs,
  the database, migrations), and Android integrations (installed apps, contacts, shortcuts,
  notifications, widget host).
- **DI modules** — `data/di/DataModule`, `data/database/di/databaseModule`,
  `presentation/di/PresentationModule`; `di/AppModule` aggregates them and `MainApplication`
  calls `startKoin`.
- **Services** at the root package: `AppNotificationListenerService` (notification badges) and
  `LockAccessibilityService` (double-tap-to-lock).

### MVI per feature

Each screen is: `XScreenState` (immutable `data class`, `@Stable`), `XScreenAction`
(sealed interface of user intents), `XViewModel` (exposes `StateFlow<XScreenState>`, handles
`onAction`), and a **Root/Screen composable split**:

- `XScreenRoot(...)` — resolves the ViewModel via `koinViewModel()`, collects state, wires
  callbacks and side effects.
- `XScreen(state, onAction, ...)` — **stateless and previewable**, no Koin/ViewModel references.

Hoist state; keep `XScreen` pure so `@Preview` works.

### Navigation

No navigation library. `LauncherPager` is a `HorizontalPager` with a **dynamic** page count:
`widgetPageCount` widget pages on the left, then Home, then the app Drawer (Home index =
`widgetPageCount`). Settings and onboarding are shown as overlays / state, not pager pages.

## Conventions

- **Koin:** `singleOf(::Impl) bind Interface::class`, `viewModelOf(::XViewModel)`. In composables
  inject with `koinInject<T>()` (`org.koin.compose`) or `koinViewModel()` (`org.koin.androidx.compose`).
- **Room:** `exportSchema = false`. **Any** schema change bumps the DB version AND adds a manual
  `Migration(n, n+1)` in `CleanPhoneLauncherDatabase.kt`, registered in `databaseModule`. Never
  rely on destructive migration — user data (favorites/widgets) must survive updates.
- **Settings:** `SettingsRepositoryImpl` is SharedPreferences-backed; each setting is a private
  `MutableStateFlow` exposed as a read-only `StateFlow`, persisted in its setter. Adding one means:
  interface property + backing flow + setter + a `KEY_*` constant + updating the fake in
  `SettingsViewModelTest`.
- **Compose `combine`:** the typed overload supports at most **5** flows. When a ViewModel needs
  more, nest them into pairs/triples first (see `HomeViewModel`).
- **Icons / theming:** monochrome vector icons in `res/drawable/ic_*.xml`, surfaced through
  `presentation/ui/theme/Icon.kt`. The launcher tints app/quick-action/widget content to the
  chosen accent — do not reintroduce colorful stock icons.
- **Strings:** every user-facing string goes in **both** `res/values/strings.xml` (English) and
  `res/values-sk/strings.xml` (Slovak).
- **Comments:** only write comments that explain *why*. No narration of what the code plainly does,
  and no "this fixes bug X" notes.

## Build & test

From the repo root (PowerShell) with a JDK 11+ on `JAVA_HOME` (Android Studio's bundled JBR works):

```powershell
.\gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest --console=plain
```

Unit tests are in `app/src/test/` (JUnit + coroutines-test, fake repositories). Run compile +
unit tests before treating a change as done.

## Releasing (CI)

Pushing to `main` runs `.github/workflows/release.yml`, which **auto-versions from the most recent
commit message** (Conventional Commits):

- `feat!:` / `fix!:` / `BREAKING CHANGE` → **major**
- `feat:` → **minor**
- anything else (`fix:`, `chore:`, `docs:`, …) → **patch**

`versionCode = MAJOR*10000 + MINOR*100 + PATCH`. CI builds a signed APK + AAB, creates a GitHub
release tagged `vX.Y.Z`, and uploads the AAB to the Play Store **internal** track. Pull requests
build a debug APK only.

**Every push to `main` cuts a release** — use Conventional Commit prefixes and be deliberate about
what lands there.

## Gotchas

- **Package visibility (API 30+):** resolving default apps (dialer, camera, `CATEGORY_APP_*`) needs
  the right `<queries>` in the manifest, and `resolveActivity(MATCH_DEFAULT_ONLY)` misses
  category activities that don't declare `CATEGORY_DEFAULT` — enumerate with `queryIntentActivities`.
- **The launcher process is long-lived:** it is not recreated when Home is pressed, so anything done
  once in an `init` / first-resume won't re-run when the user "reopens" the launcher.
- **Widget host views consume touches:** the non-consuming gesture helpers in
  `presentation/util/DoubleTapGesture.kt` exist so page-level long-press / double-tap still work
  over a hosted widget.
- **`google-services.json` is not committed** (injected from a CI secret); a local copy under `app/`
  may be needed to build locally.
