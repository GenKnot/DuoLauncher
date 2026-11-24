<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><b>English</b> · <a href="README.fr.md">Français</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <a href="README.es.md">Español</a> · <a href="README.de.md">Deutsch</a> · <a href="README.it.md">Italiano</a> · <a href="README.pt.md">Português</a> · <a href="README.ru.md">Русский</a></p>

A dual-screen game launcher for the **Retroid Pocket Duo** and other Android handhelds with a second display, in the spirit of the 3DS home menu.
Point it at your ROM folders and it finds the games, sorts them by console, genre and series, shows them on the lower screen with the artwork on the upper one, and starts each game in the emulator you choose.

**Download:** the latest APK is on the [Releases page](https://github.com/GenKnot/DuoLauncher/releases/latest) (Android 11 or newer). Installing a new version over the old one keeps every setting and your play time.

## Why another launcher?

Duo Launcher started as one player's launcher for one handheld, and it still grows from how that handheld is actually used. A few ideas run through all of it:

- **Two screens are two jobs.** The lower screen is for choosing: big folders, a grid, a list, menus. The upper screen is for looking: the console on its own illustrated backdrop, a game's logo over its cover, a video preview, how long you have played, a note to yourself, or a conversation with the AI. Nothing has to share space with anything else.
- **The gamepad comes first.** Every feature works without touching the screen: a keyboard made for the d-pad, held directions that keep scrolling, L / R that turn pages, hold L to search by voice, hold R to talk to the AI. Touch works too, it just isn't required.
- **Your library, as it already is.** It reads the Pegasus and ES-DE metadata and pictures you may already have, keeps its own additions in its own `DuoLauncher` folder, and never rewrites another frontend's files.
- **AI that works for you, with your key.** Translation, sorting, pictures, advice and conversation use the service *you* choose and pay for directly. Keys stay on the handheld, and every request is counted in Settings with an estimated cost, so nothing runs behind your back.
- **Everything can be changed, hidden, or shared.** Every home tile can be renamed, recoloured, given a new picture, moved or hidden. Your whole setup can be exported as one file and handed to a friend – without your keys or your play history.

Suggestions and bug reports are welcome on the [issue tracker](https://github.com/GenKnot/DuoLauncher/issues).

## Highlights

- **Two screens, one launcher** – choose on the lower screen; see logos, cover art, video previews, play time and descriptions on the upper one.
- **Every emulator in one place** – 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita and Switch, plus 22 older consoles through RetroArch (Master System to Dreamcast, PC Engine, Neo Geo, WonderSwan, Atari, arcade, DOS …), with the emulator chosen per console or per game. Multi-disc games show as one.
- **Sorted for you** – every game filed under one of 14 genres and under its series (Ys, Castlevania, Super Robot Wars …). The home page shows your library by console, genre or series, and smart folders keep "recently added", "playing", "want to play", "finished" and "top rated" up to date.
- **An AI companion** – hold R anywhere and talk: it recommends from your own library, explains how to get past a part of a game, tells you about a story, or just chats. The answers can be read aloud.
- **Cheats, managed** – thousands of ready-made cheats for 3DS, NDS, PSP, GBA and every RetroArch system, translated into your language in one tap, editable, and shared between emulators.
- **Fills in what is missing** – real box art from the libretro thumbnail sets, official English titles, and AI-drawn covers, logos and backdrops in the launcher's own style.
- **Made for a handheld** – OLED-safe screensaver, Wi-Fi transfer from a browser, save backups, play statistics, RetroAchievements progress, a storage overview, and a home screen that is up in a second.
- **Yours to change** – colour themes and theme packs, light and dark mode, text size, cover margins, a settings page like a console's, and configs you can export and share. 11 interface languages.

## Screenshots

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Light and dark mode"></p>

<table>
  <tr>
    <th>Settings, 3DS-style</th>
    <th>Settings in dark mode</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_settings.png" alt="Settings, 3DS-style"></td>
    <td><img src="docs/screenshots/en_settings_dark.png" alt="Settings in dark mode"></td>
  </tr>
  <tr>
    <th>AI usage and cost</th>
    <th>Export and import your config</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_ai_usage.png" alt="AI usage and cost"></td>
    <td><img src="docs/screenshots/en_config.png" alt="Export and import your config"></td>
  </tr>
  <tr>
    <th colspan="2">Hand-drawn dark backdrops</th>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/screenshots/en_dark_home.png" width="50%" alt="Hand-drawn dark backdrops"></td>
  </tr>
  <tr>
    <th>Home by genre, with clay genre icons</th>
    <th>Home by series</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Home by genre, with clay genre icons"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Home by series"></td>
  </tr>
  <tr>
    <th>Genre and series on the game page</th>
    <th>Note, Ask AI, status, how to play, saves</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Genre and series on the game page"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Note, Ask AI, status, how to play, saves"></td>
  </tr>
  <tr>
    <th>Gamepad keyboard; English titles match too</th>
    <th>Ask the AI what to play</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Gamepad keyboard; English titles match too"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Ask the AI what to play"></td>
  </tr>
  <tr>
    <th>AI tools: series, picks, storage, break reminder</th>
    <th>Storage: size per console, unplayed big games</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI tools: series, picks, storage, break reminder"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Storage: size per console, unplayed big games"></td>
  </tr>
  <tr>
    <th>Game page with video preview</th>
    <th>Logo, cover or video, per game</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Game page"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Game menu"></td>
  </tr>
  <tr>
    <th>List view</th>
    <th>Play statistics</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="List view"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Play statistics"></td>
  </tr>
  <tr>
    <th>Colour themes</th>
    <th>Theme colours reach the backdrops</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Theme menu"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Pink theme"></td>
  </tr>
  <tr>
    <th>Dark mode</th>
    <th>Dark mode</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Dark mode, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Dark mode, statistics"></td>
  </tr>
  <tr>
    <th>Wi-Fi transfer</th>
    <th>Tidy-up tools</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Wi-Fi transfer"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>GameCube, with Dolphin</th>
    <th>Hold A to move a folder</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Moving a folder"></td>
  </tr>
  <tr>
    <th>Choose cheats before importing, names translated</th>
    <th>Game details: size, discs, screenshots</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Choosing cheats"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Game details"></td>
  </tr>
  <tr>
    <th>3DS cheats, translated in one tap</th>
    <th>NDS cheats for DraStic, Chinese database</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS cheats"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS cheats"></td>
  </tr>
  <tr>
    <th>RetroArch cheats (GBA)</th>
    <th>Edit, add, download</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA cheats"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Cheat menu"></td>
  </tr>
  <tr>
    <th>Rename or delete a cheat</th>
    <th>Translate every cheat at once</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Edit a cheat"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>Screensaver (OLED-safe)</th>
    <th>11 languages</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Screensaver"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Languages"></td>
  </tr>
</table>

## Getting started

1. **Install the APK** from the [Releases page](https://github.com/GenKnot/DuoLauncher/releases/latest) and open it.
2. **Follow the welcome guide.** On the first start a short guide walks through the setup, one page at a time:
   - **Your earlier settings and records were found** – shown first, and only when the launcher's automatic copy turns up on a storage (after a reinstall, or on a new handheld with the same card). *Bring them back* lets you pick which parts to restore, as in Import a config; *Start fresh* carries on with the guide as usual. See [Settings and sharing](#settings-and-sharing).
   - **Language.**
   - **Permissions**, all on one page: file access (to find your games), usage access (so play time counts only the time a game is really on screen) and the home screen role, marked optional. This page is also under Settings → System → Permissions.
   - **What was found** – how many consoles and games the scan turned up, and where it looked: the `Roms` folder at the top of every storage, plus any folders you added. If the scan is still running, the page says so and fills in when it is done. A door leads to the [Problem centre](#handy-tools) for anything it could not place.
   - **AI (optional)** – what the AI features do, and that you pay the service you choose for what you use. Then *Type the keys on a phone (Wi-Fi)*, *Set up on the handheld*, or *Later*.
   - **The buttons** – what each one does, on one page.

   Every page has *Skip the guide*, and B goes back a page. Settings → System → Show the welcome guide again runs it once more.
   *Why:* the first few minutes decide whether the launcher makes sense, and a new player should not have to read this page to get there. Seeing the number of games found right away also answers the first question everyone has ("did it find my games?"). Someone who already knows what they are doing can skip it from any page.
3. **Let it find your games.** Any `Roms` folder on internal storage or an SD card is picked up by itself. Other folders can be added under Settings → Home and library → Game folders. Consoles are recognised from folder names (`gba`, `Nintendo DS`, `psx` …) and file extensions.
4. **Choose emulators** where you want something other than the default: press Y on a console folder for the whole console, or on a game for that game only. Only emulators that are installed are offered.
5. **Optional: add an AI key** under Settings → AI and voice → AI services and keys – on the handheld, or more comfortably from a computer or phone over Wi-Fi (Settings → Saves, transfer, sharing → Wi-Fi transfer, or *Enter on a computer or phone* in the same menu). Everything except the AI features works without one.
6. **Optional: make it your home screen** (Settings → System → Set as home screen) so it starts when the handheld does. The Android home tile always leads back to the system home screen.

## The basics

**The home page** (lower screen) holds your console folders and a few built-in tiles: Continue (jump straight back into the last game), Recently played, Favorites, New folder, Search, Play statistics, Storage, What to play?, Settings and Android home. SELECT switches the home page between consoles, genres and series.

**A folder** shows its games as a grid of covers. X makes the covers smaller, and once more turns the grid into a list with names, play time and when you last played. SELECT sorts (default order, name, recently played, most played) and can show only Simplified Chinese, Traditional Chinese or English releases. L2 / R2 jump to the previous or next first letter – Chinese titles count by their pinyin initial.

**The upper screen** follows whatever is selected: a console on its backdrop with its game count and emulator; a game's logo over its blurred cover art with its console, genre · series, emulator, play time and a short description, a video preview after a moment, or the picture of where you left off.

**Y** is the menu for whatever is selected. On a game it is short, with the rarer things one door further in: emulator, favorite, add to / remove from a folder, Cheats ›, AI help › (ask AI, how to play), Game info › (details, genre, status and rating, note, what the upper screen shows, AI pictures for this game) and More › (saves, mark as seen, hide, delete the game file). On a console folder: default emulator, rename, Merge folders ›, mark all as seen, hide. On a built-in tile: name, colour, icon and backdrop. The selected row explains itself in one line at the bottom of the menu.

*Why:* a menu with fifteen rows is hard to read on a handheld. Doors keep the everyday rows in front, and the one-line hint means nobody has to guess what a row does before pressing it.

**Delete the game file** (Y → More) asks first, showing the size and that saves are kept, and the cursor starts on Cancel, so pressing A twice too quickly deletes nothing. A multi-disc game loses all its files at once, and a game deleted from a Problem centre list (Duplicate games, say) drops off that list straight away.

*Why:* duplicates and games you are done with can be cleared away without a computer.

**One-time tips.** The first time a feature is within reach, one line appears just above the button legend on the lower screen. It shows once, and fades after about nine seconds or when you press a key (not in its first two seconds or so, so it is not gone before you can read it; the key still does its job). For now there are four:

- on the home page: hold A to pick up a tile and move it, Y for that tile's options;
- back on the home page after leaving a folder: SELECT shows the home page by console, by genre or by series;
- on a game in a console folder, in the default order: hold A to move the game where you want it, Y for emulator, cheats, saves and more;
- in the Not recognised folder: press A on a file to say which console it is for.

Settings → System → Tips for new features turns them off, and Show all tips again brings back the ones you have already seen.
*Why:* hold A to move, or SELECT on the home page, are easy to miss and hard to guess. A line at the moment you can use it is easier to remember than a manual, and once is enough: a tip that keeps coming back turns into noise.

### Controls

| Button | Action |
|---|---|
| D-pad / left stick | Move; hold to keep moving |
| Left / right, L / R in a list or menu | A page at a time |
| A | Open / play; hold A on a home folder, or on a game inside a console folder, to pick it up and move it |
| B | Back |
| L / R | Previous / next console (in Settings: category) |
| Hold L | Search by voice |
| Hold R | Talk to the AI |
| L2 / R2 | Previous / next first letter |
| X | Cover size, then list view |
| Y | Menu for the selected game, folder or tile |
| START | Settings (from anywhere on the home page, even with the Settings tile hidden); in a menu, the same as A |
| SELECT | On the home page: consoles / genres / series; in a folder: sort and language filter |
| R3 | Search |
| In the search keyboard | A types, B deletes, X space, Y system keyboard, SELECT clears, START or R3 done, hold L to speak |
| In the AI chat | Hold R to speak, Y to type, X for a new conversation, SELECT for the history, L2 / R2 to scroll back, B to close; the d-pad and A pick from the games below |

Everything also works by touch: tap to select, tap again to open, flick to scroll, and tap the pills along the top instead of L, R, X, SELECT or Search. A/B and X/Y can be swapped for Nintendo- or Xbox-style confirm buttons (Settings → Playing → Buttons).

## Your library

- **Finds your games by itself.** Internal storage and SD cards are searched for `Roms` folders; more can be added under Settings → Home and library → Game folders. The last scan is cached, so the home screen is up in a second and the next scan runs quietly in the background.
- **Consoles** – 3DS, NDS, GBA (and GBA rumble and hack folders), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita and Switch, with artwork for PS4, PS5, Xbox 360 and Xbox Series too. Every other console of the Pegasus G pack is recognised as well: Virtual Boy, Master System, Game Gear, 32X, Mega-CD, Saturn, Dreamcast, NAOMI, PC Engine and its CD, PC-FX, Neo Geo CD, Neo Geo Pocket Color, WonderSwan and WonderSwan Color, Pokémon Mini, Atari Lynx, Atari 2600, Game & Watch, 3DO, DOS and arcade.
- **Multi-disc games as one.** `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` in one folder, or a Pegasus `files:` list, become a single entry. RetroArch and DuckStation get an `.m3u` playlist of the discs so you can change discs in-game.
- **Android games and apps** – installed games get their own folder automatically; any other apps (video, browser …) can be added to an Apps folder.
- **Works with what you already have.** It reads Pegasus `metadata.pegasus.txt`, ES-DE / EmulationStation `gamelist.xml` and `downloaded_media`, RetroArch thumbnails, and `media/`, `covers/` and `images/` folders next to the games. Nothing of theirs is changed; the launcher's own additions go into its own `DuoLauncher` folder (see [Your own game info](#your-own-game-info)).
- **Notices new games.** Copy games to the card and the next scan finds them; they carry a NEW badge for a week (until played) and are offered for sorting into genres and series. The question can be turned off. Y → Mark as seen takes the badge off one game; Mark all as seen (Y on a console or folder, or Settings → Home and library) clears many at once, and the NEW badges switch there turns them off entirely.
- **Merge folders.** Two folders of the same console from different places (two PS1 folders, say) can be shown as one: Y → Merge folders on a console. Each game keeps its own emulator, folders with the same name merge by themselves, and "Split off" undoes it.
- **Your own folders.** The New folder tile makes a folder with its own name, colour, icon and backdrop. Add any game or app to it with Y, and take it out again with Y → Remove from this folder.
- **Smart folders** fill themselves: recently added (the last 30 days), not played yet, most played, playing, want to play, finished and top rated. Y on one shows its rule.
- **Continue and favorites.** The first home tile jumps straight back into the last game. Star games with Y to collect them in a Favorites folder.
- **Hide what you don't play** – a console folder, or a single game or app (Y → Hide); bring them back under Settings → Home and library.
- **Tidy up game names.** Names taken straight from ROM files often read like `0153 - Legend of Zelda, The - The Minish Cap (USA) [!]`. Settings → Home and library → Tidy up game names has six switches, all off to begin with: drop region and language tags, drop version and dump tags, drop leading numbers, turn underscores and dots into spaces, move a trailing ", The" to the front, and drop everything in brackets. Next to each switch is an example from your own library showing the name before and after, and See every change lists every game that would be renamed. *Why:* only the name on screen changes. The files on the card stay exactly as they are, so emulators, saves and other frontends are not affected. Titles from Pegasus, ES-DE or your own edits are left alone, because someone already chose them. Tags that tell games apart, such as `(Beta)` or `(Hack)`, are kept unless you turn on the brackets switch.
- **Put games in your own order.** Inside a console folder, hold A on a game (or long-press it) to pick it up, move it with the d-pad, and let go to drop it. The order is kept for that console, survives rescans and is part of a full backup. Games added later keep their usual place. Y on the console → Back to the original game order undoes it. Moving only works in the normal sort with no language filter, since in a sort by name or play time your order would mean nothing. *Why:* the games you play right now, or a series in release order, belong together at the top, and no automatic sort can know that.
- **Your catalogue.** After every scan the whole library is written to `DuoLauncher/catalog.tsv` (console, file, title, English title, genre, series, play time, status, rating) – for spreadsheets, other tools, or the AI.

### Genres and series

Browsing two thousand games by console alone gets tiring, so every game also gets one of 14 genres (role-playing, action, adventure, platformer, tactics, strategy, shooter, puzzle, racing, sports, fighting, simulation, party and music, other) and its series.

- **Built-in knowledge first.** A table inside the launcher (`assets/gamedb.tsv`, 3,860 file names covering every game in the Pegasus G pack lists for these consoles, with both the `.iso` and `.cso` names where a pack has both) already knows the genre, series and official English title of common ROM sets by console and file name. Games it knows need no AI and no internet at all.
- **AI for the rest.** Settings → AI and voice → Let AI tidy the library → AI: sort into genres / AI: find game series files the remaining games, and the results are kept in `DuoLauncher/<console>/genres.txt` and `series.txt` on the card, so it is done once and survives reinstalls. Genres already written by Pegasus or ES-DE ("Action RPG", "角色扮演" …) are mapped onto the 14 genres.
- **By hand** – Y → Genre on any game.
- **On screen.** The upper screen shows a game's genre · series, and SELECT on the home page turns the folders into genres or series. Series names follow the interface language.

### Search

R3, the Search tile, or the search button opens a keyboard on the upper screen that is made for the d-pad, with your recent searches and a row of genre chips above it; results fill the lower screen as you type. Chinese titles match their pinyin, its initials and the start of each syllable (`kdyg`, `mlao` → 马里奥), and every game also matches its official English title, so `zelda` finds 塞尔达传说. Opened from inside a console it searches only that console, with an "All platforms" button to widen it. Hold L to say a name instead of typing it.

### Pictures that fill themselves in

- **Real box art** – Settings → Tidy up → Fetch missing covers (also in the Problem centre, under Games without covers) downloads covers from the libretro thumbnail sets. NDS and GBA games are matched by the game code inside the ROM (zipped ROMs too, with title screens), so translated and renamed files still find their pictures; PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES and Mega Drive games are matched by their English title. A game counts as missing a cover only when it has no box art – a logo or backdrop alone doesn't count – so the Problem centre, Fetch missing covers and the AI pictures menu all give the same number.
- **Icons straight from the ROMs** – NDS banner icons and 3DS SMDH icons; installed 3DS titles show their name in the interface language.
- **Covers that look alike** – covers fill their tiles, or all sit with the same small, medium or large margin (Settings → Look and sound → Cover margin); white frames baked into cover pictures are trimmed.
- **AI pictures** – with an image key, AI draws a cover, a backdrop and a title logo. For one game: Y → Game info → AI pictures for this game, which says whether the game already has each; a new picture replaces the current one, and the title logo needs the English title (AI: game names and descriptions finds it). For many games: Settings → AI and voice → Let AI tidy the library → AI: draw missing game pictures, which says how many games lack each kind; its console list starts with All platforms, and two consoles with the same name show their folder name (a GBA hack folder, say). See [AI](#ai).

## Playing

- **Emulator per game or per console** (Y). Only installed emulators are offered:

  | Console | Emulators (the first is the default) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (performance mode) |
  | NDS | DraStic, DraStic (plain path), RetroArch · melonDS DS |
  | GB / GBC | RetroArch · Gambatte, mGBA |
  | GBA | RetroArch · mGBA, gpSP, gpSP Rumble |
  | NES | RetroArch · FCEUmm, Mesen |
  | SNES | RetroArch · Snes9x |
  | Mega Drive | RetroArch · Genesis Plus GX, PicoDrive |
  | N64 | RetroArch · Mupen64Plus-Next, Mupen64Plus FZ |
  | GameCube / Wii | Dolphin MMJR, Dolphin |
  | PS1 | RetroArch · PCSX ReARMed, SwanStation, DuckStation |
  | PS2 | ARMSX2, AetherSX2 |
  | PSP | PPSSPP, RetroArch · PPSSPP |
  | PS Vita | Vita3K (by title ID, e.g. a file named `PCSE00000.psvita` or a name containing it) |
  | Switch | Eden |
  | Virtual Boy | RetroArch · Beetle VB |
  | Master System / Game Gear | RetroArch · Genesis Plus GX, PicoDrive |
  | 32X | RetroArch · PicoDrive |
  | Mega-CD | RetroArch · Genesis Plus GX, PicoDrive |
  | Saturn | RetroArch · Yaba Sanshiro, Yabause |
  | Dreamcast / NAOMI | RetroArch · Flycast |
  | PC Engine / PC Engine CD | RetroArch · Beetle PCE Fast, Beetle PCE |
  | PC-FX | RetroArch · Beetle PC-FX |
  | Neo Geo CD | RetroArch · NeoCD |
  | Neo Geo Pocket Color | RetroArch · Beetle NeoPop, RACE |
  | WonderSwan / Color | RetroArch · Beetle Cygne |
  | Pokémon Mini | RetroArch · PokeMini |
  | Atari Lynx | RetroArch · Handy, Beetle Lynx |
  | Atari 2600 | RetroArch · Stella |
  | Game & Watch | RetroArch · Game & Watch |
  | 3DO | RetroArch · Opera |
  | DOS | RetroArch · DOSBox Pure |
  | Arcade | RetroArch · FinalBurn Neo, MAME 2003-Plus |

  A Pegasus `metadata.pegasus.txt` usually brings its own launch line for each console; that is used first, and the cores above are offered next to it.

- **Citra MMJ (performance mode)** – the same Citra MMJ build repackaged under the AnTuTu benchmark's package name, which many Android systems run at full clocks. It is offered only when that package really is Citra.
- **Play time and statistics** – how long and when you last played each game, Switch-style; with usage access granted it counts only the time the game was really on screen. The Play statistics tile shows the total, this week day by day, time per console and the most played games.
- **Where you left off** – a played game's newest screenshot or save-state picture becomes its upper-screen background.
- **Status, rating and notes** – mark a game as playing, want to play, finished or dropped and give it one to five stars (Y → Status); write or say a line about where you are and what to do next (Y → Note). All of it shows on the upper screen next time you pick the game up.
- **Game details** (Y → Details) – file, size, discs, folder, emulator, play time, the official English title, and the game's screenshots on the upper screen.
- **Video previews and sounds** – a game's video snap starts on the upper screen after a moment (or choose logo or cover per game: Y → Top screen). Soft interface sounds and each 3DS game's own banner tune can be switched off.
- **Out of the way while you play.** Once a game starts, the lower screen is handed to the emulator and video, music and banner tunes stop. The launcher also lets go of the pictures it holds in memory (measured: about 167 MB down to 109 MB), holds back a card scan that is under way until you return so it does not compete with the emulator for the card, and stops the upper screen's clock refresh and motion. Back in the launcher, the covers are on screen again within a second.
  *Why:* the emulator gets the memory and the card to itself.
- **When a game does not start** – a missing file, an emulator that is not installed, one that cannot open that kind of file, or a game that closes at once each get a message saying what to do. The [Problem centre](#handy-tools) lists consoles whose emulator is not installed before you run into it.
- **Break reminder** (Settings → Playing) – after one, two or three hours in a game, the launcher suggests a break when you come back.
- **RetroAchievements** – with your user name and Web API key, recently played games with their achievement progress, and a 🏆 tag on games that have achievements.

## Your AI companion

All of this is optional and uses the AI service you choose with your own key (see [AI](#ai)).

- **Talk to it anywhere.** Hold R on the home page, in a folder or in the search keyboard and speak – or press Y to type. The conversation fills the upper screen; the games it recommends or finds fill the lower one, ready to start with A. Ask what to play ("a relaxed RPG, not too long"), narrow it down ("something with more action"), ask how to get past the part of the game you have selected, about a story or a character, or just chat. It remembers the conversation for half an hour.
  *Why:* the lower screen is where games are chosen, so the AI's picks land there; the upper screen is free for reading. And on a handheld, holding a shoulder button and talking is quicker than any menu.
- **New conversation, saved history.** X in the chat starts a new conversation; SELECT lists the earlier ones, newest first, to open again, delete one by one or clear all (also Settings → AI and voice → AI chat history). While the chat is open, video previews, 3DS banner tunes and click sounds stay quiet, and spoken answers play at media volume.
  *Why:* a new question should not drag the old topic along, an answer worth keeping should not vanish with a restart, and a spoken reply is no use when a game trailer plays over it. The history is left out of *Share with others*, goes into the automatic copy in `DuoLauncher/profile/` so a reinstall does not lose it, and the Save AI chats switch inside AI chat history turns it off.
- **Answers read aloud** – Settings → AI and voice → AI services and keys → AI voice replies: Soniox (its own key) or OpenAI (the image key), with a choice of voice and pace and a test button. Off unless you turn it on.
- **What to play today** – Y on the What to play? tile has the AI pick six games from your library to match your taste, with a reason for each. A plain press of the tile picks one at random, favouring games you have never played.
- **Ask about a game** (Y → Ask AI) – a question about the selected game, typed or spoken, answered on the upper screen.
- **How to play** (Y → How to play) – the controls (including the swapped confirm / cancel buttons of Japanese releases) and the basics a newcomer needs, written once per game and kept.
- **No key yet?** Holding R explains how to add one – on the handheld, or from a computer over Wi-Fi.

## Cheats

Y → Cheats on a game lists its cheats; A switches one on or off.

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Where they live | `azahar/cheats/` and `citra-emu/cheats/`, `<title ID>.txt` – written for both, so switching emulators keeps them | the whole list in `DuoLauncher/cheats/NDS/`; the ones switched on go to DraStic's own custom cheats (`DraStic/cheats/<game file name>.cht`), which work for translated ROMs too | `PSP/Cheats/<game ID>.ini`, PPSSPP's own cheat files; the game ID is read from the ISO, CSO or EBOOT.PBP | `RetroArch/cheats/<core>/<game file name>.cht`, loaded by RetroArch when the game starts |
| Already on the device | – | DraStic's `usrcheat.dat` (Chinese on Retroid handhelds), matched by the game code only, so patched and translated ROMs find their cheats even where DraStic itself shows none | PPSSPP's `cheat.db`, and on Retroid handhelds Chinese cheat files for about 4,300 games | Retroid ships Chinese cheats for hundreds of games (`000 中文金手指` and the core folders); a game without its own file gets a copy, matched by name |
| Download | [Sharkive](https://github.com/FlagBrew/Sharkive), about 650 games, by title ID read from the ROM | [libretro-database](https://github.com/libretro/libretro-database), about 4,200 games, by the game code in the ROM header | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), about 4,300 games, by game ID | [libretro-database](https://github.com/libretro/libretro-database) for GBA, by the game code in the ROM header |

- **Choose before importing** – a download first lists what it found, already translated, with Select all / Select none; only the ticked ones are added. *Why:* a database can hold dozens of codes per game, and most players want three of them.
- **Translate** – English cheat names become your interface language in one tap, for one game or for every game at once (Settings → Tidy up). Common cheats come from a built-in glossary (无限血量, 金钱最大, 主代码 …); the rest go to the free MyMemory service, with Google's free endpoint as a second try. Version tags (`v1.2`) and button hints (`(START+L)`) are kept, and the original English name stays in the file as a note.
- **Translate with AI** – with an AI key, a game's cheats go to the model together with the game's name, so it uses the official names of characters and items (鬼神林克, 心之容器 …).
- **Edit** – rename or delete any cheat, delete all of a game's cheats at once, or add your own with its codes (3DS, NDS and PSP codes are checked as `XXXXXXXX YYYYYYYY`).
- **Safe for DraStic** – flash-cart patches (anti-piracy bypasses, overlay and Wi-Fi fixes) are left out of NDS downloads because DraStic crashes with them switched on, and at most 256 code lines can be on at once, the most DraStic can load.
- **Safe for your files** – the first time a hand-written 3DS cheat file is changed, the original is kept as `.txt.bak`.
- **RetroArch, once** – switch on Quick Menu → Cheats → "Auto-Apply Cheats During Game Load" in RetroArch once, and the cheats you turn on here are active from the moment a game starts.

## Look and feel

- **The upper screen (1920×1080)** – each console on its own paper-cut backdrop, or the selected game's logo over its blurred cover art, with tags and a short description; the clock and battery sit in the corner.
- **The lower screen (1280×960)** – console folders, a zoomable cover grid and a list view, with key hints along the bottom that stay short and leave out what does not fit when the text is large.
- **Themes** – seven colour schemes, custom colours, and theme packs with console art, backdrops, a font, sounds and music. A theme also colours the line-art backdrops on the upper screen. See [Themes](#themes).
- **Light and dark mode** – follows the system or set it yourself, with a separate set of backdrops drawn for each.
- **Everything on the home page is yours.** Every built-in tile and every console folder can be renamed, recoloured, given its own icon (a picture of yours, or one drawn by the image service in the launcher's clay style), moved (hold A, or long-press) or hidden, and brought back under Settings → Home and library → Hidden folders. Built-in tiles such as Play statistics, Storage and What to play? can have their own upper-screen backdrop too.
- **Console look** – each console's picture, light and dark backdrop and folder colour can be replaced (Settings → Look and sound → Platform look). A dark backdrop can be redrawn by the image service from the light one, so the two always match.
- **Text size** – four sizes for both screens.
- **Screensaver** – after a few idle minutes the upper screen shows your covers with a clock and the lower screen goes black. The text changes corner with every picture and drifts slowly, so nothing burns into an OLED panel.
- **Duo Lite** (Settings → Look and sound, the first row) – one switch that turns off video previews and 3DS banner music, the blurred artwork behind a game, the drifting dots on the upper screen and the gentle tilt and bob of the console picture, the entrance animation and the pulsing selection frame. While it is on, the Video previews and 3DS banner music switches read off and say why when pressed; your own choices come back when Duo Lite goes off. Measured on the Duo, idle on the home page: the upper screen normally redraws about 600 frames in 5 seconds, and none at all in Duo Lite.
  *Why:* for long sessions on battery, or when you would simply rather have a still screen.

## Settings and sharing

- **A settings page like a console's.** START opens one page: the categories on the left (Look and sound, Home and library, Playing, AI and voice, Saves, transfer, sharing, Tidy up, System), the rows of one on the right with their current values and switches, and the upper screen showing the version, the free space on each drive and which AI services are on. L / R change category.
  Some rows are doors to a smaller menu (AI services and keys, Let AI tidy the library, Problem centre …). While the cursor is on one, a line at the bottom of the card says what is behind it, and B from inside comes back to the same row with its values already updated.
  *Why:* a category with a dozen rows reads like a wall on a 1280×960 screen. Grouping the rarer settings behind a door keeps each category short, and the one-line note means you know what a door holds before you go through it.
- **Back up or share your setup.** Settings → Saves, transfer, sharing → Export my config asks what to put in: settings and your own pictures, the play record, game status / ratings / notes, keys and accounts, AI usage and chats, and the game data in the `DuoLauncher` folders – or one of two shortcuts, *Share with others* (settings and pictures only) and *Full backup* (everything). Import a config picks one up from `DuoLauncher/exports` or the Download folder – sent over Wi-Fi, a chat app or a card – and shows what it holds, so you can untick what should stay as it is. On the Wi-Fi page, sending a file to *Configs to import* puts it straight into `DuoLauncher/exports`, so it is waiting there in Import a config.
  *Why:* a config handed to a friend must never carry your keys or your history, while a backup of your own should bring back exactly the same launcher on a new handheld or after a reinstall. A full backup holds your keys, so the export warns you to keep it to yourself, and a key of yours is never pointed at an address that came from someone else's config.
- **Where launcher data lives** (Settings → Saves, transfer, sharing) – where the `DuoLauncher` folder goes: each console's genres, series and AI-written names, fetched and AI-drawn pictures, save backups, exports, themes and the settings copy below. *Next to the games* is the default and the way it always worked: each console's data on the storage its games are on, backups on the card. *Card* puts everything on the TF card, so it travels with the card to another handheld (with no card in, internal storage stands in for the time being). *Internal* keeps it all on the handheld when you swap cards. Whatever was written before a change is still read where it is. With Card or Internal chosen, *Move existing data here* shows how much there is and then moves the `DuoLauncher` folders over from the other storages; files already in place are kept, and the NDS cheat lists and multi-disc playlists stay where the emulators expect them.
  *Why:* where your data lives is your decision; nothing about it is hard-coded.
- **An automatic copy of settings and records.** *Keep a copy of settings and records* (same menu, on by default) copies your settings, own pictures, play time, status / ratings / notes, game order, AI usage and chats to `DuoLauncher/profile/` on the chosen storage – the first time within a minute, after that whenever something has changed. It checks about once a minute while the launcher is on screen, never while a game runs, and keeps only the newest copy. API keys stay out unless *Include API keys in the copy* is switched on, which warns that anyone with the card could read them. *Save a copy now* makes one straight away and shows when the last one was made. After a reinstall, or on a new handheld with the same card, the welcome guide opens with *Your earlier settings and records were found*: *Bring them back* lets you choose which parts, as in Import a config, and *Start fresh* leaves them alone.
  *Why:* uninstalling or a factory reset used to take your play time, notes and settings with it, unless you had remembered to export them by hand.
- **11 languages** – 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Handy tools

- **Problem centre** – the first row of Settings → Tidy up. It checks the library in the background each time you open it and gathers everything that may need a look, with a count on each row:
  - **Missing permissions** – opens the permissions page.
  - **Files not recognised** – opens the Not recognised folder, where A on a file picks its console.
  - **Consoles whose emulator isn't installed** – pick a console to see the emulators that are installed (A switches to one) and the ones the launcher knows would work once installed, plus *Open this console*.
  - **Files that can't be opened** – empty files, and files that a Pegasus or ES-DE game list names but that are not on the card.
  - **Games without covers** – a small menu: See which N games, Fetch missing covers (it runs right there with a *Looking for covers online…* line, then the menu comes back with the new count and how many were found, or says none were found online), and Draw covers for these N games with AI.
  - **Duplicate games** – the same console, the same name and the same size, most likely one game copied twice.

  The first row says that none of this stops the launcher from working, and only the problems that are actually there are listed. The welcome guide's *What was found* page leads here too. B on a list opened from here (Games without covers, Duplicate games, Files that can't be opened) goes back to the Problem centre, not the home page.
  *Why:* these checks used to be scattered across Settings, and a missing emulator only showed up when a game would not start. One place that is checked when you go there, and never pops up on its own, means you can tidy up when you feel like it and ignore the rest.
- **Wi-Fi transfer** – Settings → Saves, transfer, sharing → Wi-Fi transfer shows the page's address on the upper screen with a QR code next to it, the admin password (six random digits, or your own), and a short list of what the page is for. Scan the code with a phone camera, or type the address in a browser on the same network. The QR code is made on the handheld, so no internet connection is needed. B on the Wi-Fi menu goes back to wherever you opened it from.
  The page follows the phone's or computer's light or dark mode, and is in the first language the browser prefers among the launcher's eleven (Traditional Chinese for Taiwan, Hong Kong and Macau), or in English if none of them fits. Everything on it is translated, with the same names as in the app. Before it is unlocked it asks only for the admin password and says what you can do there. Once unlocked (a reload does not send the password again) it has:
  - **Send files** – pick a folder and drop files in: ROMs into any console folder, theme packs, or a config into *Configs to import*, which puts it in `DuoLauncher/exports` so it shows up in Settings → Import a config.
  - **AI translation, AI pictures, AI voice replies and RetroAchievements settings**, so long keys can be pasted from a computer. Saved keys are never shown in full.
  - **Downloads** – every exported config, exported theme and save backup, newest first, with size and date.

  The handheld's screen stays on while the transfer runs, because the page cannot be reached once the screen is off.
  *Why:* typing an address on a phone is the fiddly part, and a camera does it in a second. Downloads sit behind the password because a full backup can hold your API keys, and a page anyone on the same Wi-Fi can open should not hand those out.
- **Storage** – a home tile shows how full the card is, and the free space of every drive on the upper screen. Inside: the size of each console's games, the biggest games never played, and deleting a game's files (all discs, and the files a `.cue` or `.m3u` names) after a confirmation.
- **Save backups** – back up and restore the saves of DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar and Citra in one zip, kept in `DuoLauncher/backups` on the SD card (internal storage when there is no card). After three hours of play without a backup the launcher reminds you, or makes one by itself if you prefer (the newest ten automatic backups are kept; yours are never removed). A restore replaces each save only once its copy is complete. Y → Saves backs up and restores one game on its own.
- **Tidy up** – the Problem centre, Storage, Fetch missing covers, Translate all cheats, and a Diagnostics page. Duplicate games and Games without covers now live inside the Problem centre. The AI ones (names and descriptions for a whole console, genres, series, genre icons, pictures for missing art) are under AI and voice → Let AI tidy the library.
- **Updates** – Settings → System → About checks the latest GitHub release once a day and links to it.
- **Home screen** – set it as the default home app so it starts with the handheld; the Android home tile (or Settings → System → Exit to Android home) leads back to the system home screen, and Settings → System → Android settings opens the system's own settings.

## AI

Everything here is optional and uses your own account. Keys are kept in the launcher's settings on the handheld and sent only to the service you chose. Enter them in Settings → AI and voice → AI services and keys, or on the Wi-Fi page from a computer or phone.

Settings → AI and voice has six rows:

- **Ask AI what to play** and **AI: what to play today** – the chat and the six picks (see [Your AI companion](#your-ai-companion)).
- **AI services and keys** – AI translation, AI pictures and AI voice replies, each with its service and key, and *Enter on a computer or phone (Wi-Fi page)*.
- **Let AI tidy the library** – AI: game names and descriptions, AI: sort into genres, AI: find game series, AI: genre folder icons, and AI: draw missing game pictures.
- **AI chat history** – earlier conversations, and the Save AI chats switch.
- **AI usage and cost.**

*Why:* this category used to have thirteen rows in one list, mixing things you use every day with keys you enter once. Now the two things people come for are on top, the one-time setup is behind one door, and the jobs that work through the whole library are behind another.

- **AI translation** – DeepSeek, Qwen, OpenAI, Claude, Gemini or any OpenAI-compatible address. This key does the talking: the chat, game picks, "Ask AI", "How to play", genres and series, cheat names (a whole game in one request), and game names and descriptions in your language (written into the launcher's own metadata together with the official English title, so a library of Chinese titles reads naturally in English and the other way round). Without a key, cheats are translated by free services.
- **AI pictures** – an OpenAI image key (the newest image model the key can use is chosen automatically). Covers, title logos, backgrounds, console pictures, folder and tile icons and backdrops, all in the same clay-icon and paper-cut style as the built-in art; icons are drawn with a transparent background so their white parts stay. A picture takes about half a minute. The same key turns your voice into text (`/audio/transcriptions`), since the handheld has no speech recognizer of its own.
- **AI voice replies** – Soniox with its own key, or OpenAI with the image key; voice and pace (normal, brisk, fast – brisk by default, as both read Chinese slowly).
- **AI usage and cost** – every request the launcher makes is counted on the handheld: requests, tokens, pictures and minutes of speech, with an estimated cost for today, this week, this month and all time. The estimate uses the services' published prices for what you use; each price can be set to your own (another model, a reseller, a discount). Your provider's bill is what counts.

## How it finds your games

You don't need a metadata file, a scraper or a particular folder layout to start: copy the games to the card and the launcher works out the rest. This section explains exactly how, so you know where to put things and why a game shows up where it does.

### Where it looks

- **Every `Roms` folder at the top of a storage** – internal storage and each SD card. Capital letters don't matter (`ROMs`, `roms`). Turn this off under Settings → Home and library → Game folders → Find Roms folders automatically.
- **Any folder you add** under Settings → Home and library → Game folders, for games kept somewhere else.
- **Up to three folder levels down** from there. Folders that hold pictures or emulator files are skipped: `media`, `images`, `videos`, `covers`, `boxart`, `screenshots`, `manuals`, `marquees`, `wheel`, `saves`, `states`, `savestates`, `bios`, `system`, `cheats`, `downloaded_media`.
- **3DS games installed into Azahar or Citra** (from their `sdmc` folder) and **Android games** are added as well.

The result of a scan is kept, so the home screen is there in a second; games copied in later are found by the next scan.

*Why:* most people already have a `Roms` folder from another frontend or a ROM pack. Starting from it means nothing has to be moved, renamed or set up first.

### Recommended layout

One folder per console, named after the console:

```
Roms/
  3DS/      NDS/      GBA/      GBC/      GB/
  NES/      SNES/     N64/      NGC/      WII/
  MD/       PS1/      PS2/      PSP/      PSV/
  SWITCH/   SATURN/   DC/       PCE/      ARCADE/  …
```

Folder names are matched loosely (spaces, dashes and capitals are ignored), and the usual names from Pegasus, ES-DE and RetroArch work too:

| Console | Folder names it understands | File types it recognises anywhere |
|---|---|---|
| Nintendo 3DS | `3ds`, `n3ds`, `nintendo3ds` | `.3ds` `.cci` `.cxi` `.cia` `.3dsx` |
| Nintendo DS | `nds`, `ds`, `nintendods` | `.nds` `.dsi` |
| Game Boy Advance | `gba`, `gameboyadvance` | `.gba` |
| Game Boy Color | `gbc`, `gameboycolor` | `.gbc` |
| Game Boy | `gb`, `gameboy` | `.gb` |
| NES / Famicom | `nes`, `fc`, `famicom` | `.nes` `.fds` `.unf` |
| SNES / Super Famicom | `snes`, `sfc`, `superfamicom`, `supernintendo` | `.sfc` `.smc` |
| Nintendo 64 | `n64`, `nintendo64` | `.n64` `.z64` `.v64` |
| GameCube | `ngc`, `gc`, `gamecube`, `nintendogamecube` | `.gcm` `.gcz` `.rvz` `.ciso` |
| Wii | `wii`, `nintendowii` | `.wbfs` `.wad` |
| Mega Drive / Genesis | `md`, `genesis`, `megadrive`, `segagenesis` | `.md` `.gen` `.smd` |
| PlayStation | `ps1`, `psx`, `playstation` | `.pbp` |
| PlayStation 2 | `ps2`, `playstation2` | – (needs its folder) |
| PSP | `psp` | `.cso` |
| PS Vita | `psv`, `psvita`, `vita` | `.vpk` |
| Switch | `switch`, `ns`, `nintendoswitch` | `.nsp` `.xci` `.nsz` `.xcz` |
| Virtual Boy | `vb`, `virtualboy` | `.vb` `.vboy` |
| Master System | `sms`, `mastersystem` | `.sms` |
| Game Gear | `gg`, `gamegear` | `.gg` |
| 32X | `32x`, `sega32x`, `md32x` | `.32x` |
| Mega-CD / Sega CD | `segacd`, `megacd`, `mdcd` | – (by content) |
| Saturn | `saturn`, `ss`, `segasaturn` | – (by content) |
| Dreamcast | `dc`, `dreamcast` | `.cdi` `.gdi` |
| NAOMI / Atomiswave | `naomi`, `naomi2`, `atomiswave` | – (needs its folder) |
| PC Engine / TurboGrafx-16 | `pce`, `pcengine`, `tg16` | `.pce` |
| PC Engine CD | `pcecd`, `pcenginecd`, `tgcd` | – (needs its folder) |
| PC-FX | `pcfx` | – (needs its folder) |
| Neo Geo CD | `neogeocd`, `ngcd` | – (needs its folder) |
| Neo Geo Pocket Color | `ngpc`, `ngp`, `neogeopocket` | `.ngp` `.ngc` |
| WonderSwan | `ws`, `wonderswan` | `.ws` |
| WonderSwan Color | `wsc`, `wonderswancolor` | `.wsc` |
| Pokémon Mini | `pokemini` | `.min` |
| Atari Lynx | `lynx`, `atarilynx` | `.lnx` |
| Atari 2600 | `atari`, `atari2600`, `2600` | `.a26` |
| Game & Watch | `gw`, `gameandwatch` | `.mgw` |
| 3DO | `3do` | – (by content) |
| DOS | `dos`, `msdos`, `pc` | `.dosz` |
| Arcade | `arcade`, `mame`, `fbneo`, `fba`, `neogeo`, `cps1`/`cps2`/`cps3` | – (needs its folder) |

### Mixed folders, and what needs its own folder

You can also drop everything into one folder. For each file the launcher asks, in this order:

1. **Did you say which console it is for?** (see the last point below)
2. **Does its folder name say?**
3. **Does its file type belong to one console only?** (the right-hand column above)
4. **What does the file itself say?** For file types several consoles share, it reads a few bytes from the start of the file – fast even on a big card:
   - `.iso` – GameCube, Wii, PSP, PlayStation, Saturn, Mega-CD and 3DO discs each mark themselves; a PlayStation disc over 900 MB counts as PS2.
   - `.rvz`, `.wia`, `.ciso` – the disc header tells GameCube from Wii. (`.gcz` cannot be told apart and is counted as GameCube.)
   - `.cue` – the first track of the `.bin` it names: PlayStation, Saturn, Mega-CD or 3DO.
   - `.chd` – a Dreamcast GD-ROM or a PS2 DVD; CD images of other consoles need their folder.
   - `.zip` – one game inside (up to four files) whose type belongs to one console, such as a `.gba` in a `.zip`. Arcade sets, with many parts, need an arcade folder.
   - `.m3u` – whatever its first disc is.

Whatever is still unknown after that (a `.7z`, say, or a `.zip` of an arcade game outside an arcade folder) is not dropped: it goes into a **Not recognised** folder on the home page. Press A or Y on such a game and pick its console once; the choice is remembered, survives rescans, and can be undone with Y → Which console is this for? → Reset to default. For PlayStation `.cue` + `.bin` pairs, only the `.cue` is listed. The [Problem centre](#handy-tools) counts these files under Files not recognised and opens the folder for you.

*Why:* guessing wrong is worse than not guessing – a PS2 game counted as PSP would not start. So the launcher only decides when the folder, the file type or the file's own header gives a clear answer, and asks you about the rest instead of hiding it.

### Where names, pictures and details come from

For each game, the first source that has something wins:

1. **The launcher's own folder `DuoLauncher/`** (see below) – your corrections, AI translations, genres and series. Nothing overrides it.
2. **Pegasus** – a `metadata.pegasus.txt` in the game folder, with its titles, descriptions, pictures, launch lines and multi-disc lists. Games copied in later without an entry still show up.
3. **ES-DE / EmulationStation** – `gamelist.xml` next to the games or in `ES-DE/gamelists/`, and pictures in `downloaded_media/`.
4. **Pictures next to the games** – `media/<file name without extension>/`, or a picture with the same name in `covers/`, `images/` or `boxart/`.
5. **RetroArch thumbnails.**
6. **The built-in game table** (3,860 well-known file names) – official English title, genre and series.
7. **The file name** itself.

Other frontends' files are only read, never changed, so Pegasus, ES-DE and Duo Launcher can share the same card.

*Why:* your own edits should always win, but everything you or another frontend already gathered should be used, not thrown away.

### What the `DuoLauncher` folder holds

The launcher keeps its own files in a `DuoLauncher` folder at the root of a storage. By default, what belongs to a console sits on the storage that holds its games (usually the SD card); Settings → Saves, transfer, sharing → Where launcher data lives can put all of it on the card or on internal storage instead (see [Settings and sharing](#settings-and-sharing)). Cheat lists and disc lists always sit on internal storage:

| Path | What it is |
|---|---|
| `DuoLauncher/<console>/metadata.txt` | Names, descriptions and English titles you or the AI added (Pegasus format, see [Your own game info](#your-own-game-info)) |
| `DuoLauncher/<console>/media/<file name without extension>/` | Your own or downloaded logo, cover and background pictures |
| `DuoLauncher/<console>/genres.txt`, `series.txt` | Genre and series of each game |
| `DuoLauncher/cheats/NDS/` | The full NDS cheat lists (internal storage) |
| `DuoLauncher/m3u/` | Disc lists for multi-disc games (internal storage) |
| `DuoLauncher/themes/` | Theme packs (on any storage) |
| `DuoLauncher/backups/` | Save backups (on the SD card when it has a `DuoLauncher` folder) |
| `DuoLauncher/exports/` | Exported configs and themes |
| `DuoLauncher/profile/` | The automatic copy of your settings and records (on the storage chosen under Where launcher data lives) |
| `DuoLauncher/catalog.tsv` | A list of your whole library, written for your own use |

Settings such as the home layout, folders, emulator choices and play time are kept inside the app, with a copy in `DuoLauncher/profile/` that is kept up to date by itself; Settings → Saves, transfer, sharing → Export my config can save them, together with everything above, to one file (see [Settings and sharing](#settings-and-sharing)).

## Your own game info

Put a `DuoLauncher` folder at the root of the SD card (or internal storage) to add or correct names, descriptions and pictures without touching other frontends' files. Anything here wins over Pegasus, ES-DE and the game's own data.

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

`metadata.txt` uses the Pegasus format. Entries are matched by `file:`, or by `titleid:` for 3DS games installed into an emulator:

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

Names and descriptions can be given per language with `name.<language>:` and `description.<language>:` (`en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `es`, `de`, `it`, `pt`, `ru`; `zh` covers both Chinese). They are used while the launcher runs in that language, so one folder can serve players of several languages:

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## Themes

Settings → Look and sound → Theme offers seven built-in colour themes (blue, red, green, purple, orange, pink, graphite), **custom colours** (pick a main colour and a background colour; every shade for light and dark mode is worked out from them), and any installed theme packs. A theme colours both screens, including the line-art backdrops on the upper screen.

A theme pack is a folder (or a `.zip` of one, unpacked on first use) in `DuoLauncher/themes/` on the SD card or internal storage. Everything is optional; whatever a theme leaves out comes from the built-in look, so a theme can be as small as a few colours.

```
DuoLauncher/themes/my-theme/
  theme.txt                      name, author, colours (see below)
  consoles/<type>.png            console pictures      (3ds, nds, gba, ..., recent, search, settings, custom)
  backgrounds/<type>.jpg         upper-screen backdrops
  backgrounds/dark/<type>.jpg    backdrops for dark mode
  logo.png
  font.ttf, font-bold.ttf
  sounds/move|open|back|launch.ogg   (or .wav / .mp3)
  music.ogg (or music.mp3)       loops on the home screen (Settings → Background music appears when a theme has music)
```

`theme.txt` uses `key: value` lines: `color.<name>` for light mode and `dark.color.<name>` for dark mode, with names such as `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (the upper-screen line art) and `folder.<type>` for the home folders. `base-color: #RRGGBB` alone is enough for a complete colour scheme. [themes/sakura](themes/sakura/theme.txt) is a complete example with every key explained. Settings → Saves, transfer, sharing → Export current theme saves whatever you have set up as a zip in `DuoLauncher/exports/` that others can drop into their own `themes/` folder; your own pictures go along under `own/`.

## Building

No Android Studio or Gradle needed – only the Android SDK command-line tools (build-tools 35, platform android-35) and JDK 17 or newer:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

A debug signing key (`debug.keystore`) is generated on the first build and is not committed. `shot.ps1` sends key presses over adb and saves screenshots of both screens, which is handy while working on the interface.

## Artwork

`assets/consoles/<type>.png` (clay console and tile icons) and `assets/backgrounds/<type>.jpg` (paper-cut line-art backdrops, with their own dark-mode versions in `backgrounds/dark/`) were made for this project. The backdrops are greyscale, so a colour theme can tint them. Consoles without a picture fall back to a drawn console.

## Launch notes

- AzaharPlus only accepts a tree URI under a folder it already has access to:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` with `-e GamePath <path>`.
- Citra MMJ (performance mode) is the same build repackaged as `com.antutu.ABenchMark`; many Android builds run that package name at full CPU/GPU clocks. It is only offered when that package really contains Citra.
- DraStic as shipped on Retroid handhelds wants `-e GAMEPATH "/sf<path>"`; a "plain path" variant is offered for stock builds.
- RetroArch: `com.retroarch.aarch64/...RetroActivityFuture` with `-e ROM`, `-e LIBRETRO <core>` and `-e CONFIGFILE`. The config file named in the launch line is kept: on the Duo, RetroArch's own `retroarch.cfg` shows Retroid's per-system frames at full size, while Retroid's per-screen `retroarch1920x1080.cfg` draws games small inside a TV frame.
- Dolphin (MMJR and official): `.../ui.main.MainActivity` with `--es AutoStartFile <path>`.
- ARMSX2, AetherSX2, Eden, PPSSPP and Mupen64Plus FZ get the game as a read-only `content://com.duo.launcher.rom/...` link with a one-time read grant, so they need no folder access of their own.
- Vita3K: `org.vita3k.emulator/.Emulator` with the string array extra `AppStartParameters = ["-r", "<title ID>"]`.
- Launch lines in Pegasus metadata are used unchanged.

### Citra MMJ on a dual-screen handheld

Citra MMJ puts the touch screen on the upper display by default. To get the main picture on top and hide the on-screen buttons, close Citra and set in `/sdcard/citra-emu/config/config-mmj.ini`:

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## Feedback

Duo Launcher is shaped by how it gets used. If something is missing, confusing or broken – or you have made a theme worth sharing – open an [issue](https://github.com/GenKnot/DuoLauncher/issues).

## License

[MIT](LICENSE)
