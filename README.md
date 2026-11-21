# Duo Launcher

A dual-screen, 3DS-style game launcher for the **Retroid Pocket Duo** (and other Android handhelds with a second display).
It reads an existing Pegasus library, so nothing needs to be moved or renamed.

一个给 Retroid Pocket Duo 双屏掌机做的 3DS 风格游戏前端，直接读取天马G（Pegasus）的游戏目录，不改动任何文件。

## Features

- **Upper screen (1920×1080)** – the selected platform's console on top of its own backdrop, or the selected game's logo over its blurred cover art, with platform/emulator tags and a short description. Everything tilts gently in 3D.
- **Lower screen (1280×960)** – platform folders and a zoomable icon grid. Works with the gamepad and with touch.
- **Uses your Pegasus library as-is** – reads `Roms/<platform>/metadata.pegasus.txt`, its `launch:` commands and the `media/` artwork.
- **Icons straight from the ROMs** – NDS banner icons from the cartridge header, 3DS SMDH icons from `.cci/.3ds` ExeFS (decrypted dumps). GB/GBA use the Pegasus box art.
- **Emulator per game or per platform** (Y button) – e.g. AzaharPlus or Citra MMJ for 3DS, different RetroArch cores for GB/GBA.
- **Custom artwork** – in Settings → Platform look, every platform's console image and background image can be replaced with your own picture.
- **11 languages** – 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский. Chosen on first start, changeable in Settings.

## Controls

| Button | Action |
|---|---|
| D-pad / left stick | Move |
| A | Open / play |
| B | Back |
| L / R | Previous / next platform |
| X | Icon size |
| Y | Choose emulator |
| SELECT | Sort (default / by name) |

## Building

No Android Studio or Gradle needed – only the Android SDK command-line tools (build-tools 35, platform android-35) and JDK 17+:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

A debug signing key (`debug.keystore`) is generated on the first build and is not committed.
`shot.ps1` sends key presses over adb and saves screenshots of both screens, which is handy while tweaking the UI.

## Artwork

`assets/consoles/<type>.png` is bundled into the APK and used for the console pictures; platforms without a picture fall back to a drawn console.
The current photos are by Evan-Amos on Wikimedia Commons and are in the **public domain**:
`Nintendo-3DS-AquaOpen.png`, `Nintendo-Game-Boy-Color-FL.png`.
Optional bundled backgrounds go in `assets/backgrounds/<type>.png`.

## Launch notes

- AzaharPlus only accepts a tree URI under a folder it already has access to:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` with `-e GamePath <path>`
- DraStic and RetroArch: the commands from the Pegasus metadata are used unchanged.
