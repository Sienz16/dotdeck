# Setting up DotDeck on your head unit

A step-by-step guide to getting DotDeck onto an Android car head unit or Android player, making it the home screen, and tuning it for your screen. Written for people who have never used ADB, with the exact commands for those who have.

> **Do the first install while parked.** Switching the home screen on a car stereo changes what appears on every power-up. Keep a way back (see [Getting back to your old launcher](#8-getting-back-to-your-old-launcher)) before you change anything.

**Contents**

1. [What you need](#1-what-you-need)
2. [Find out about your unit](#2-find-out-about-your-unit)
3. [Choose how to install](#3-choose-how-to-install)
4. [Method A: USB stick or the unit's browser](#4-method-a-usb-stick-or-the-units-browser-no-computer)
5. [Method B: ADB from a computer](#5-method-b-adb-from-a-computer)
6. [Make DotDeck the home screen](#6-make-dotdeck-the-home-screen)
7. [Set it up for your unit](#7-set-it-up-for-your-unit)
8. [Getting back to your old launcher](#8-getting-back-to-your-old-launcher)
9. [Updating](#9-updating)
10. [Parked test checklist](#10-parked-test-checklist)
11. [Troubleshooting](#11-troubleshooting)
12. [What to include in a bug report](#12-what-to-include-in-a-bug-report)

---

## 1. What you need

| Item | Notes |
| --- | --- |
| The DotDeck APK | Download `dotdeck-<version>.apk` from the [Releases page](https://github.com/Sienz16/dotdeck/releases). The APK is about 40 KB. |
| An Android head unit or player | **Android 7.0 (API 24) or newer**, with a touch screen. It is not an Android Auto app and does not replace Android Automotive OS. |
| A way to get the file onto the unit | A USB stick (method A), or a computer and a cable or Wi-Fi (method B). |
| 10 to 20 minutes | And a parked car. |

DotDeck needs **no root**, asks for **no permissions** and uses **no internet**. It does not touch firmware properties, background apps or car services. Anything it cannot do through normal Android, it does not do.

## 2. Find out about your unit

Open the unit's **Settings** and look for **About** (sometimes "About device", "System" or "Device info"). Note:

- **Android version**: must be 7.0 or newer.
- **Model / build number**: helpful for searching forums and for bug reports.
- **Screen resolution**: shown in some "Display" pages. Landscape units around 1024x600, 1280x720 and 1920x1080 are all fine. Tall "tablet-style" portrait screens also work.

If a computer is connected later with ADB (method B), you can read all of this in one go:

```sh
adb shell getprop ro.build.version.release   # Android version, e.g. 10
adb shell getprop ro.build.version.sdk       # API level, must be 24 or higher
adb shell getprop ro.product.model           # model
adb shell getprop ro.product.cpu.abi         # CPU type, e.g. arm64-v8a
adb shell wm size                            # screen size in pixels
adb shell wm density                         # screen density
```

> Many units are sold under several brand names with the same internals, and some hide the normal Android Settings behind a vendor "Settings" app. If you cannot find **Apps** or **About**, try the unit's app drawer for a gear icon labelled "Android settings", or search your exact model number with "open Android settings".

## 3. Choose how to install

```
Does your unit have a web browser and internet?
  yes ─► Method A (download the APK on the unit itself)
  no  ─► Does it have a USB port you can plug a USB stick into, and a file manager app?
           yes ─► Method A (USB stick)
           no  ─► Method B (ADB from a computer)
```

Method A needs nothing but the unit. Method B is what you want if you plan to tune things, read device information, or recover from a mistake.

## 4. Method A: USB stick or the unit's browser (no computer)

1. **Get the APK.**
   - *USB stick*: download `dotdeck-<version>.apk` on a computer or phone and copy it to the root of a USB stick (FAT32 or exFAT works with most units). Plug the stick into the unit.
   - *Unit's browser*: open the [Releases page](https://github.com/Sienz16/dotdeck/releases) in the unit's browser and download the APK.
2. **Allow installing from outside the store.** The first time you open an APK, Android asks you to allow the app you are installing from (File Manager or Browser) to "install unknown apps". Turn that on. Older units have one global **Unknown sources** switch under Settings, Security.
3. **Open the file.** In the unit's **File Manager**, browse to the USB stick or Downloads, tap `dotdeck-<version>.apk` and choose **Install**.
4. Choose **Open** or find **DotDeck** in the app list. Because it also declares itself as a home app, Android may immediately ask what to use for the home screen. You can answer **Just once** for now, then continue at [section 6](#6-make-dotdeck-the-home-screen).

If the unit refuses to open APKs from its file manager, use method B.

## 5. Method B: ADB from a computer

ADB (Android Debug Bridge) lets a computer install apps and read information. It is the most reliable way to set up and recover a head unit.

### 5.1 Install ADB on your computer

| System | Command or download |
| --- | --- |
| macOS | `brew install android-platform-tools` |
| Windows | Download "SDK Platform-Tools for Windows" from Google, unzip, run commands from that folder |
| Linux | `sudo apt install adb` (Debian/Ubuntu) or your distribution's `android-tools` |

Check it works: `adb version`.

### 5.2 Turn on debugging on the unit

1. In Settings, About, tap **Build number** seven times. Android says "You are now a developer". (If your unit has no Build number, look for **Developer options** or a vendor "Factory settings" or "Engineer mode" page. These differ by manufacturer, and some are protected by a code that the manufacturer or forums for your model document. Do not guess codes.)
2. Open **Developer options** and turn on **USB debugging**. If there is an **ADB over network** or **Wireless debugging** switch, you can use that instead of a cable.

### 5.3 Connect

**Over USB** (needs a USB port that supports connecting to a computer, which is not always the case: many car ports are host-only and cannot talk to a computer; try each port, or use Wi-Fi):

```sh
adb devices
```

The unit may show an "Allow USB debugging?" prompt. Tap **Allow** (tick "Always allow"). `adb devices` should then list your unit as `device`. If it says `unauthorized`, accept the prompt on the unit. If it lists nothing, try another cable, port or the Wi-Fi route below.

**Over Wi-Fi**, with the unit and computer on the same network:

- *Unit offers "ADB over network" or fixed port 5555* (common on head units):

  ```sh
  adb connect 192.168.1.50:5555      # use your unit's IP address
  adb devices
  ```

- *Android 11 or newer "Wireless debugging"*: open Developer options, Wireless debugging, **Pair device with pairing code**, then:

  ```sh
  adb pair 192.168.1.50:37123        # pairing IP and port shown on the unit
  # enter the six-digit code
  adb connect 192.168.1.50:40123     # the *different* IP:port shown on the main Wireless debugging page
  ```

- *Only USB debugging, but you want Wi-Fi*: with the cable connected once, run `adb tcpip 5555`, unplug, then `adb connect <unit-ip>:5555`.

The unit's IP address is in Settings, Wi-Fi, the connected network. If you have more than one device attached, add `-s <serial>` to every command. The serial is the first column of `adb devices`.

### 5.4 Install DotDeck

```sh
adb install -r dotdeck-<version>.apk
adb shell am start -n app.dotdeck.launcher/.HomeActivity
```

`Success` means it installed, and the second command opens it. `-r` keeps your settings if you are updating. Confirm with:

```sh
adb shell pm path app.dotdeck.launcher
```

## 6. Make DotDeck the home screen

Pick whichever works on your unit. You can try them in order.

**Option 1: from DotDeck.** Open DotDeck, tap **SETTINGS**, then **Default home screen**.
- Android 10 and newer shows a system dialog: choose DotDeck.
- Older Android opens the system's home-app settings or a chooser. Pick DotDeck and choose **Always**.

**Option 2: from Android settings.** Settings, Apps, Default apps (older versions: the gear icon in Apps), **Home app**, then choose DotDeck.

**Option 3: press the Home button.** If Android shows "Complete action using", choose DotDeck and tap **Always**.

**Option 4: with ADB** (a firm way to set it on Android versions that support it; verified on Android 7):

```sh
adb shell cmd package set-home-activity app.dotdeck.launcher/.HomeActivity
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

The second command prints the home app Android will now open. It should list `app.dotdeck.launcher/.HomeActivity`.

> **Vendor overrides.** Some firmware (including some NWD units) ignores the standard Android home setting, because the vendor's launcher is hard-wired to the Home button, the boot sequence or the car's mode-switch. If DotDeck opens fine but the Home button or a reboot still brings back the original launcher, see [Troubleshooting](#11-troubleshooting). DotDeck deliberately does not write vendor system properties, because that can break camera, CAN-bus or radio integration.

Now press **Home**, power-cycle the unit (ACC off and on, or a reboot) and check which launcher appears.

## 7. Set it up for your unit

Everything is configured inside the app under **SETTINGS**. Nothing needs a config file.

### 7.1 Pick a layout and theme

The home screen has two independent choices.

| Setting | Choices | Pick for |
| --- | --- | --- |
| **Layout** | DECK, DOCK, LIST, BENTO, STRIP | Layout is about shape. DECK is balanced. DOCK is clock-first and glanceable, with the favourites as round buttons. LIST gives six wide, easy-to-hit rows. BENTO puts the clock in a tile among the favourites. STRIP shows a large clock with a swipeable row of cards. |
| **Theme** | DARK, LIGHT, VOID, MILK, SIGNAL, TERMINAL, AMBER, BLUEPRINT, EMBER | Colours only. VOID (true black) is easiest at night on OLED/AMOLED, AMBER and TERMINAL are low-glare. |
| **Animations** | on or off | Turn off if your unit is slow or you want it still. They also switch off automatically if Android's animation scale is 0. |
| **Clock format** | 24 hour or 12 hour | |
| **App icons** | monochrome or original colours | |

Try a few layouts on your own screen; the layouts adapt to landscape, portrait and small screens, but only you know what reads best at your viewing distance.

### 7.2 Choose your six favourites

On first run DotDeck guesses an app for each tile by looking for common names. Change any of them:

- Tap an **empty tile**: the app list opens, tap an app to put it there.
- **Hold a tile**: **Choose app** or **Clear tile**.
- In **ALL APPS**, **hold an app**: **Set as favourite** (then pick a tile) or **App info**.
- Choosing an app that is already on another tile swaps the two tiles.

The six tiles are Navigation, Audio, Phone, Projection (phone mirroring), Camera and Your pick. The names are only starting hints; you can put any app on any tile.

### 7.3 Make sure car apps show up

The app list shows every installed app that has a launcher icon. Vendor car apps (radio, 360 camera, car settings, steering-wheel learning) sometimes have no icon of their own or are started in a special way. To check:

```sh
adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
adb shell pm list packages | grep -i -E "radio|camera|avm|car|can"
```

- If the app is in the first list, it appears in DotDeck's **ALL APPS** and can be a favourite.
- If it is only in the second list, it has no launcher icon. DotDeck cannot show it; keep using the original launcher for it, or open it from the unit's own toolbar or steering buttons, which usually keep working because they do not go through the launcher.

### 7.4 For tinkerers: change the first-run guesses

The first-run guess is a list of words in [`core/Slot.java`](../app/src/main/java/app/dotdeck/launcher/core/Slot.java). If your unit's apps have unusual names, add the package name or a word from the label, then rebuild (see the README). Example: add `"mycar360"` to the `CAMERA` terms. The guess runs once, on first start with an empty set of tiles. To re-run it: Settings, Apps, DotDeck, Storage, **Clear data** (this resets all DotDeck settings).

## 8. Getting back to your old launcher

Do this **before** you need it. Note your current launcher:

```sh
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

The second command lists every home app on the unit, such as `com.android.launcher3/.Launcher` or a vendor name. Write the one you want to return to down.

Ways back, from easiest:

1. **In DotDeck**: Settings, **Default home screen**, choose the other launcher.
2. **In Android settings**: Settings, Apps, Default apps, **Home app**.
3. **With ADB**:
   ```sh
   adb shell cmd package set-home-activity <original.package>/<ActivityName>
   ```
4. **Uninstall DotDeck**, which removes it as the home choice:
   ```sh
   adb uninstall app.dotdeck.launcher
   ```
   Or Settings, Apps, DotDeck, **Uninstall**.

If DotDeck is the only home app and the Home button misbehaves, a connected ADB session (method B) is the fastest fix. That is why setting it up first is worth the extra minutes.

## 9. Updating

Install a newer APK over the old one. Settings and favourites are kept.

```sh
adb install -r dotdeck-<newer-version>.apk
```

If you get `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the new APK was signed with a different key than the one installed (for example a debug build over a release build). Uninstall DotDeck first, which resets its settings, then install again. Stay on the signed release builds from the Releases page and this will not happen.

## 10. Parked test checklist

Test these before driving. Tick each with the engine off and then on:

- [ ] DotDeck appears after a cold boot (ACC off, wait, ACC on).
- [ ] The Home button always opens DotDeck.
- [ ] Back button inside Settings and ALL APPS returns to the DotDeck home.
- [ ] Reverse camera comes on when you select reverse.
- [ ] 360 / surround camera, if fitted, still opens.
- [ ] Steering-wheel buttons: volume, next track, phone, voice.
- [ ] Radio, Bluetooth music and Bluetooth calls (incoming call screen shows, and you can come back).
- [ ] Navigation app launches from its tile and keeps running when you return Home.
- [ ] Sleep and wake, with the screen blank and back on.
- [ ] Clock and date are correct. Tapping the clock opens date settings.
- [ ] Home reads well at arm's length, in daylight and at night.

## 11. Troubleshooting

| Symptom | Likely cause and fix |
| --- | --- |
| "App not installed" or nothing happens opening the APK | Install from unknown sources is off for File Manager or the browser. Turn it on. Or use method B. |
| `INSTALL_FAILED_OLDER_SDK` | The unit runs Android older than 7.0 (API 24). DotDeck cannot run there. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Installed copy was signed with a different key. `adb uninstall app.dotdeck.launcher`, then install again. |
| `adb devices` shows nothing | Try another cable and port; car ports are often host-only. Use the Wi-Fi method. |
| `unauthorized` | Accept "Allow USB debugging?" on the unit; if it never appears, toggle USB debugging off and on. |
| `adb connect` times out | Unit and computer must be on the same network. ADB-over-network or Wireless debugging must be on. Some units only allow it after `adb tcpip 5555` over USB. |
| DotDeck opens but Home or reboot goes back to the old launcher | Vendor firmware overrides the home setting. Try [section 6](#6-make-dotdeck-the-home-screen) options 1, 2 and 4 in turn. If none stick, check for a vendor "Launcher" or "Default launcher" selection in the unit's own settings or factory menu. Open an issue with the details in section 12. |
| Home screen shows the wrong layout size, or content is cut off | Try another layout. Report the output of `adb shell wm size` and `adb shell wm density`. |
| A favourite tile says "Unavailable" | The app was uninstalled or changed. Hold the tile, **Choose app**. |
| An app is missing from ALL APPS | It has no launcher icon. See [7.3](#73-make-sure-car-apps-show-up). |
| The screen feels sluggish | Turn **Animations** off. Icons: choose **Monochrome**. Report the model and Android version. |
| Back button does nothing on the home screen | That is by design: a launcher has nowhere to go back to. |

## 12. What to include in a bug report

Open an issue at <https://github.com/Sienz16/dotdeck/issues> and paste the output of:

```sh
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
adb shell wm size
adb shell wm density
adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell pm list packages | grep -i dotdeck
adb logcat -d -s AndroidRuntime:E | tail -50
```

Also say what you did, what you expected, and what happened. Please do not post anything you consider private: the output above contains no personal data, but a full `adb logcat` can.
