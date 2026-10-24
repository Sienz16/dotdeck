#!/usr/bin/env python3
"""Exercise the actual APK through ADB. Requires an explicit test-device serial."""
import argparse
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PROJECT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--adb", default="adb")
parser.add_argument("--serial", required=True)
args = parser.parse_args()
PACKAGE = "app.dotdeck.launcher"


def adb(*parts, binary=False):
    result = subprocess.run(
        [args.adb, "-s", args.serial, *parts], check=True,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=40,
    )
    return result.stdout if binary else result.stdout.decode()


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/dotdeck-smoke.xml")
    return list(ET.fromstring(adb("exec-out", "cat", "/sdcard/dotdeck-smoke.xml")).iter("node"))


def find(value, prefix=False):
    for _ in range(4):
        current = nodes()
        # Prefer the labelled control over an EditText containing the same search term.
        for attribute in ("content-desc", "text"):
            for node in current:
                label = node.get(attribute, "")
                if label.startswith(value) if prefix else label == value:
                    return node
        time.sleep(.4)
    raise AssertionError("UI element not found: " + value)


def point(node):
    left, top, right, bottom = map(int, re.findall(r"\d+", node.get("bounds")))
    if right <= left or bottom <= top:
        raise AssertionError("Unreachable UI element: " + str(node.attrib))
    return str((left + right) // 2), str((top + bottom) // 2)


def tap(value, prefix=False):
    adb("shell", "input", "tap", *point(find(value, prefix)))
    time.sleep(.35)


def hold(value, prefix=False):
    x, y = point(find(value, prefix))
    adb("shell", "input", "swipe", x, y, x, y, "850")
    time.sleep(.35)


def start():
    adb("shell", "am", "start", "-n", PACKAGE + "/.HomeActivity")
    find("ALL APPS")


def screenshot(name):
    folder = PROJECT / "app/build/screenshots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / name).write_bytes(adb("exec-out", "screencap", "-p", binary=True))


apk = PROJECT / "app/build/outputs/apk/debug/app-debug.apk"
adb("install", "-r", str(apk))
start()
screenshot("home.png")
tap("ALL APPS")
find("All apps")
tap("Search apps")
adb("shell", "input", "text", "zzzzdotdecknomatch")
find("No matching apps.", prefix=True)
adb("shell", "input", "keyevent", "KEYCODE_BACK")
tap("HOME")
hold("Favourite 1:", prefix=True)
tap("Choose app")
tap("Search apps")
adb("shell", "input", "text", "Settings")
adb("shell", "input", "keyevent", "KEYCODE_BACK")
tap("Settings")
find("Favourite 1: Settings", prefix=True)
adb("shell", "am", "force-stop", PACKAGE)
start()
find("Favourite 1: Settings", prefix=True)
tap("Favourite 1: Settings", prefix=True)
time.sleep(.7)
resumed = adb("shell", "dumpsys", "activity", "activities")
if not re.search(r"(?:mResumedActivity|topResumedActivity).*com.android.settings", resumed):
    raise AssertionError("Favourite did not launch Android Settings")
start()
tap("SETTINGS")
find("Make it yours")
tap("Theme LIGHT", prefix=True)
find("Make it yours")
screenshot("settings-light.png")
tap("HOME")
find("ALL APPS")
screenshot("home-alternate-theme.png")
tap("SETTINGS")
for name in ("TERMINAL", "SIGNAL", "DARK"):
    tap("Theme " + name, prefix=True)
    find("Theme " + name + ", selected")
    screenshot("settings-" + name.lower() + ".png")
tap("HOME")
for name in ("DOCK", "LIST", "BENTO", "STRIP", "DECK"):
    tap("SETTINGS")
    tap("Layout " + name, prefix=True)
    tap("HOME")
    find("ALL APPS")
    screenshot("layout-" + name.lower() + ".png")
hold("Favourite 1:", prefix=True)
tap("Clear tile")
find("Favourite 1: Choose an app", prefix=True)
adb("shell", "am", "force-stop", PACKAGE)
start()
find("Favourite 1: Choose an app", prefix=True)
tap("ALL APPS")
screenshot("app-drawer.png")
tap("HOME")
print("PASS: drawer search/empty state, assignment, launch, persistence, theme and layout switching, clearing")
