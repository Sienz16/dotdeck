# AGENTS.md: guide for AI coding agents

This file is for LLM agents (Claude Code, Codex, Cursor, Gemini CLI and similar) working in this repository. It covers two jobs:

1. **Helping a user get DotDeck onto their Android head unit** (the [playbook](#2-playbook-help-a-user-set-up-their-head-unit)).
2. **Changing the code** (the [development guide](#3-development-guide)).

If the user only wants the human-readable steps, point them to [`docs/HEADUNIT_SETUP.md`](docs/HEADUNIT_SETUP.md) and [`README.md`](README.md).

---

## 0. What DotDeck is, in six lines

- An **Android home-screen launcher** for car head units and Android players. Plain Java, standard Android Views, **zero runtime dependencies**.
- `minSdk 24` (Android 7.0), `targetSdk 35`, Java 8 source level, package `app.dotdeck.launcher`, entry `.HomeActivity`.
- **Requests no permissions.** No root, no internet, no accessibility service, no ADB-at-runtime.
- Home screen = **theme** (colours) x **layout** (arrangement) x optional **animations**, all chosen in the app's Settings.
- It never alters firmware properties, kills apps or touches car services, and **you must not make it (or the user's unit) do so either**.
- Tested on an Android 7 emulator only. Real head units vary; treat every unit as unverified.

## 1. Hard rules

Follow these without exception. If a user asks for something that breaks one, explain and ask them to do it themselves or confirm explicitly.

**Never**

- Run commands on a user's head unit or phone that you were not asked to run. Read-only inspection is fine; anything that changes the device needs the user's go-ahead for that specific command.
- Use root or `su`, `setprop`, `settings put`, `pm disable*`, `pm hide`, `pm clear` of other packages, `pm uninstall` of anything other than `app.dotdeck.launcher`, `am force-stop` of other apps, `reboot`, `wipe`, `fastboot`, or flash or modify firmware, the boot image or vendor partitions.
- Guess factory/engineer-menu codes or passwords, or tell the user to try codes found for other units.
- Disable verification, install unsigned or unknown APKs from third parties, or sideload other software "to make it work".
- Add permissions, network access, analytics, third-party libraries, or a background service to DotDeck. Zero dependencies and zero permissions are part of the product.
- Commit signing keys, keystores, passwords, `local.properties`, or screenshots with personal data. `.gitignore` already excludes keystores, build output and `local.properties`; keep it that way.
- Claim something works on the user's head unit unless you or the user verified it there. The only verified target is the API 24 emulator.

**Always**

- Name the target device with `adb -s <serial>` when more than one device is attached, and show the user the serial before any state-changing command.
- Tell the user to **park the vehicle** and keep a way back (section 2.6) before changing the default home app.
- Prefer the smallest reversible step, and say how to undo it.
- Use [Conventional Commits](https://www.conventionalcommits.org/) for any commit: `feat:`, `fix:`, `docs:`, `test:`, `chore:`, `refactor:` with an optional scope (`feat(ui): ...`). Do not add `Co-Authored-By` trailers unless the user asks.

## 2. Playbook: help a user set up their head unit

Work through these phases in order. At each **GATE**, stop and wait for the user.

### 2.1 Understand the situation (no device access yet)

Ask, in one message:

1. What is the unit (brand, model, Android version if known) and does it have a web browser and a USB port?
2. Do you have a computer to use with ADB, or do you want to install with only the unit?
3. Is the car parked and safe to work in?

Then pick the path:

| User has | Path |
| --- | --- |
| Only the unit | Method A in `docs/HEADUNIT_SETUP.md` (USB stick or the unit's browser). You cannot run commands; guide them step by step and ask what they see. |
| A computer with ADB access to the unit | Method B. You can run read-only commands directly once connected. |
| A computer but no way to connect the unit | Method A, and offer to help them enable Developer options for later. |

### 2.2 Get the APK

Direct the user to the latest signed release:
`https://github.com/Sienz16/dotdeck/releases/latest` (file `dotdeck-<version>.apk`).

To build from source instead (only if asked or if no release fits), see section 3.2. Debug builds (`app-debug.apk`) are signed with a different key from release builds, so never mix them on one device without uninstalling first.

### 2.3 Connect (Method B)

Verify ADB, then the device. These are read-only and safe:

```sh
adb version
adb devices -l
```

- Empty list: help the user enable Developer options and USB debugging (guide section 5.2) or Wi-Fi ADB (guide section 5.3). Car USB ports are often host-only; fall back to Wi-Fi.
- `unauthorized`: ask the user to accept the prompt on the unit.
- More than one device: **ask which serial** and use `adb -s <serial>` from now on.

### 2.4 Inspect the unit (read-only)

Run, then summarise for the user in plain language:

```sh
adb -s <serial> shell getprop ro.build.version.release
adb -s <serial> shell getprop ro.build.version.sdk
adb -s <serial> shell getprop ro.product.manufacturer
adb -s <serial> shell getprop ro.product.model
adb -s <serial> shell getprop ro.product.cpu.abi
adb -s <serial> shell wm size
adb -s <serial> shell wm density
adb -s <serial> shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s <serial> shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s <serial> shell pm list packages | grep -i dotdeck
```

Decide:

- **SDK below 24**: stop. DotDeck cannot install. Tell the user.
- **Existing DotDeck installed**: note whether it is a debug or release build only if relevant (update failures).
- Record the **current default home component** (the `resolve-activity` output) and the list of home apps. This is the user's way back. **Show it to them and ask them to keep it.**

> Note: `pm resolve-activity` does not exist on Android 7. Use `cmd package resolve-activity` and `cmd package query-activities`, as above.

### 2.5 Install (GATE: user confirms the serial and APK path)

```sh
adb -s <serial> install -r <path-to>/dotdeck-<version>.apk
adb -s <serial> shell pm path app.dotdeck.launcher
adb -s <serial> shell am start -n app.dotdeck.launcher/.HomeActivity
```

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE`: different signing key. Explain that uninstalling DotDeck (`adb -s <serial> uninstall app.dotdeck.launcher`) resets its settings, and ask permission before doing it. This is the **only** uninstall you may ever run.
- `INSTALL_FAILED_OLDER_SDK`: unit is too old.
- Anything else: show the exact error and consult the troubleshooting table in the guide.

Ask the user to look at the unit: does DotDeck open and fit the screen? If it is cut off or cramped, suggest another layout in Settings, and collect `wm size` and `wm density` for a bug report.

### 2.6 Make it the home app (GATE: explicit "yes, switch my home screen")

Before switching, confirm you recorded the original home component (2.4) and the user knows how to revert.

Preferred order (least invasive first):

1. User opens DotDeck, Settings, **Default home screen**, and picks DotDeck. No ADB needed.
2. User uses Android Settings, Apps, Default apps, **Home app**.
3. With ADB, only on explicit approval:
   ```sh
   adb -s <serial> shell cmd package set-home-activity app.dotdeck.launcher/.HomeActivity
   adb -s <serial> shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
   ```

Have the user press the Home button and power-cycle the unit (ACC off/on), then confirm which launcher appears.

**If the vendor launcher keeps coming back** (some firmware, including some NWD units, overrides the standard setting): do **not** modify system properties, edit vendor partitions or disable the vendor launcher. Report what you saw, collect the bug-report information (guide section 12) and tell the user the honest status: DotDeck works as an app, but this firmware keeps its own launcher. They can still open DotDeck from the app list.

**Revert** (any time):

```sh
adb -s <serial> shell cmd package set-home-activity <original.package>/<OriginalActivity>
# or, with the user's approval:
adb -s <serial> uninstall app.dotdeck.launcher
```

### 2.7 Configure for the unit (all optional, in-app)

Guide the user through, in the app's Settings:

- **Layout**: DECK, DOCK, LIST, BENTO, STRIP. **Theme**: DARK, LIGHT, VOID, MILK, SIGNAL, TERMINAL, AMBER, BLUEPRINT, EMBER. Independent choices.
- **Animations**: off on slow units. **Clock format**, **App icons**.
- **Favourites**: tap an empty tile, or hold a tile to choose or clear. Hold an app in ALL APPS to set it as a favourite.

For vendor car apps that do not appear in ALL APPS, see guide section 7.3: they have no launcher icon, and DotDeck cannot show them.

If the first-run guess picked the wrong apps and the user wants better guesses for their unit's app names, edit the search words in [`core/Slot.java`](app/src/main/java/app/dotdeck/launcher/core/Slot.java) (section 3.4), rebuild, reinstall and clear DotDeck's data to re-run the guess.

### 2.8 Verify together (parked)

Walk through the checklist in guide section 10. Record pass/fail per item. Never tell the user the unit is "safe to drive with" on the basis of this checklist; it only covers what DotDeck can affect.

### 2.9 Report

Finish with a short summary: unit details, what was installed (version, signing), what the home setting is now, how to revert, what passed, and what is untested or failed.

---

## 3. Development guide

### 3.1 Layout

```
app/src/main/java/app/dotdeck/launcher/
  HomeActivity.java      lifecycle and wiring only
  core/                  PURE JAVA. No android.* imports. Unit-tested.
    Slot, Glyph, Page, NavState, FavoriteSlots, FavouriteSeeder,
    AppEntry, AppCatalog, Theme, Themes, LayoutStyle
  data/                  Android services
    DeckSettings, FavouriteStore (SharedPreferences)
    AppRepository (background app scan), IconLoader (LRU cache)
    SystemEvents (broadcast receiver wrapper)
  ui/                    Views
    Screen (interface), HomeScreen, DrawerScreen, SettingsScreen
    HomeLayout (interface), HomeLayouts (factory), DeckLayout, DockLayout,
      ListLayout, BentoLayout, StripLayout
    ClockPanel, TileFactory, TileGrid, DotClock, GlyphView
    Ui (themed view factory), ScreenContext, SystemActions, Dialogs,
    Motion, ThemeSwatch, LayoutPreview
app/src/test/java/app/dotdeck/launcher/core/   pure-Java tests (no JUnit)
tools/smoke.py                                  ADB UI smoke test
docs/                                           user guide and screenshots
```

Data flow: `HomeActivity` builds the services and, on each render, a `Ui` (theme + metrics) and a `ScreenContext` (everything a screen needs). `ScreenContext.createScreen()` returns the `Screen` for `NavState.page()`. Screens ask the activity to navigate only through `Navigator`.

### 3.2 Build and test

Requirements: JDK 17, Android SDK platform 35, Build Tools 35.0.0 (Gradle and AGP are pinned in the wrapper). Put the SDK path in an untracked `local.properties` (`sdk.dir=...`) or set `ANDROID_HOME`.

```sh
./test.sh        # pure-Java core tests only; needs just a JDK, no Android SDK
./build.sh       # core tests + debug APK + optimized release APK + lint
```

Equivalent: `./gradlew :app:coreTest :app:assembleDebug :app:assembleRelease :app:lintDebug`.

**Run `./build.sh` before every commit.** Lint has `abortOnError true`. Outputs:

- `app/build/outputs/apk/debug/app-debug.apk` (debug-signed, installable)
- `app/build/outputs/apk/release/app-release-unsigned.apk` (R8-optimized, must be signed with `zipalign` then `apksigner` before it installs)

UI smoke test (needs a running emulator or test device; never point it at someone's car unit):

```sh
python3 tools/smoke.py --adb "$ANDROID_HOME/platform-tools/adb" --serial emulator-5554
```

### 3.3 Rules for code changes

- **Keep `core/` free of `android.*`.** That is what lets `test.sh` run with only a JDK. Anything needing a Context, View or preference belongs in `data/` or `ui/`.
- **No dependencies, no permissions, no network.** Do not add Gradle `implementation` lines or manifest `uses-permission`.
- **Java 8 source level**, `minSdk 24`. Guard newer APIs with `Build.VERSION.SDK_INT` (see `SystemEvents`, `SystemActions.chooseHome`).
- **No continuous animation.** `Motion` and `DotClock` are one-shot only and must respect `Ui.motion`. Do not add timers, polling or services.
- **No god classes.** One job per class; `HomeActivity` stays a thin shell. Add a class rather than growing one.
- **Match the surrounding style**: brace-on-same-line one-liners, `final` classes, short Javadoc only where it explains a non-obvious *why*.
- Add or update tests in `app/src/test/.../core/` for any `core/` change. Theme colours must pass the contrast checks in `ThemeTest`.
- Keep preference keys backward compatible (`slot_0..5`, `seeded`, `theme`, `layout`, `motion`, `clock24`, `mono`; legacy `light` is migrated).
- Keep the saved-state keys (`page`, `picking`, `query`) and `HomeActivity`'s component name (`.HomeActivity`); the smoke test and the ADB commands in the docs use them.
- Update `README.md` and the relevant `docs/` page when behaviour changes.

### 3.4 Common tasks

**Add a theme** (colours only): add one `Theme.builder(...)` entry to `core/Themes.java` and include it in `ALL`. `ThemeTest` enforces unique ids and readable contrast; adjust the palette until it passes. The picker shows it automatically.

**Add a layout**: (1) add a `LayoutStyle` value in `core/`, (2) create `ui/<Name>Layout.java` implementing `HomeLayout` using `ClockPanel` and `TileFactory`/`TileGrid`, (3) add a `case` in `ui/HomeLayouts.java`, (4) add a wireframe `case` in `ui/LayoutPreview.java`. Verify portrait and compact sizes (below).

**Change first-run favourite guesses**: edit the seed terms in the `Slot` enum. `FavouriteSeeder` tries each tile's terms in order against `package/activity + label`, skips apps already used, and never overwrites a tile the user set. Cover the change in `SeederTest`.

**Check visual layouts**: boot the API 24 emulator, install the debug APK, and use `adb shell wm size 360x640` (portrait), `800x480` (small landscape) and `wm size reset` (default 1280x720). Take screenshots with `adb exec-out screencap -p`. Reset `wm size` afterwards.

### 3.5 Releasing

1. Bump `versionCode` and `versionName` in `app/build.gradle`; update the README if needed.
2. `./build.sh` must pass.
3. Sign the release APK with the **maintainer's** keystore, which lives outside the repo (never commit it):
   ```sh
   zipalign -f -p 4 app/build/outputs/apk/release/app-release-unsigned.apk aligned.apk
   apksigner sign --ks <keystore> --out dotdeck-<version>.apk aligned.apk
   apksigner verify --print-certs dotdeck-<version>.apk
   ```
   Keep the same key for every release so users can update in place.
4. Tag `v<version>` and publish a GitHub release with the signed APK attached. Do this only when the user asks.

### 3.6 Known limits (be honest about them)

- Not tested on any real head unit. Vendor firmware may override the home setting, hide Android Settings, or run a different Android build.
- Apps without a launcher icon cannot appear in ALL APPS.
- No license file has been chosen yet.
- Performance numbers (RAM, startup) on a 1 GB unit are unmeasured; one emulator snapshot showed about 21 MB PSS.
