<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

A dual-screen, 3DS-style game launcher for the **Retroid Pocket Duo** and other Android handhelds with a second display.
Point it at your ROM folders and it finds the games, shows them on the lower screen and the artwork on the upper one, and starts each game in the emulator you pick.

一个给 Retroid Pocket Duo 等双屏安卓掌机做的 3DS 风格游戏前端：选好放游戏的文件夹，它会自己扫描、按平台归类，下屏选游戏、上屏展示，每款游戏都能指定用哪个模拟器。

## Highlights · 亮点

- **Two screens, one launcher** – pick on the lower screen, see logos, cover art, video previews, play time and descriptions on the upper one. 下屏选、上屏看：Logo、封面、视频预览、游玩时间、简介。
- **One place for every emulator** – 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita and Switch, with the emulator chosen per platform or per game; multi-disc games show as one. 一个入口管所有模拟器：3DS、NDS、GBA、GB/GBC、NES、SNES、N64、GameCube、Wii、MD、PS1、PS2、PSP、PS Vita 和 Switch，每个平台、每个游戏都能单独选模拟器；多碟游戏自动合并。
- **AI with your own key** – cheat names, game names and descriptions translated in context by DeepSeek, Qwen, OpenAI, Claude or Gemini; covers, console pictures and backdrops drawn by an image model. Keys stay on the handheld and can be typed on a phone over Wi-Fi. 用你自己的密钥接入 AI：按游戏语境翻译金手指、游戏名和简介，自动画封面、主机图和背景；密钥只存在掌机上，可以用手机通过 Wi-Fi 填。
- **Sorted for you** – AI sorts every game into a genre and a series (Ys, Castlevania, Super Robot Wars …) and asks when new games turn up; the home page shows your library by console, by genre or by series, and smart folders keep "recently added", "playing", "want to play", "finished" or "top rated" up to date. AI 自动给游戏分类型、归系列，发现新游戏会问你要不要分；主页可以按主机、按类型、按系列看，智能文件夹自动整理「最近新增」「在玩」「想玩」「通关了」「评分最高」。
- **Cheats, managed for you** – thousands of ready-made cheats (3DS, NDS, PSP, GBA and every RetroArch system), downloaded from public databases or taken from the Chinese packs already on the device, translated into your language in one tap, editable, and shared between emulators. 金手指一站式管理：3DS、NDS、PSP、GBA 和所有 RetroArch 平台的上千个金手指，联网下载或用机器自带的中文库，一键翻译成界面语言，可改名可自建，不同模拟器通用。
- **Fills in what is missing** – real box art from the libretro thumbnail sets, matched by the game code in NDS/GBA ROMs or by the English title for PlayStation, PSP, N64, GameCube, Wii, 3DS and more; your own names and descriptions in any language. 自动补全真实封面（NDS/GBA 按游戏代码，其他平台按英文名），游戏资料可按语言自己写。
- **Made for handhelds** – OLED-safe screensaver, Wi-Fi transfer from a browser, save backups, play statistics, RetroAchievements progress, screenshots per game, quick start-up, held-down directions that keep scrolling, and controls that work with the gamepad or touch. 为掌机设计：防烧屏屏保、Wi-Fi 传文件、存档备份、游玩统计、成就进度、按游戏看截图、按住方向键连续移动、秒开，手柄和触屏都能操作。
- **Yours to change** – colour themes and theme packs, light and dark mode, text size, every home tile renamed, recoloured, hidden or moved (hold A, or long-press), 11 languages. 随你定制：主题、深浅色、字体大小、封面边距，主页每个格子都能改名、换色、换图标、隐藏，按住 A（或长按）拖动排序，11 种语言。

## Screenshots

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Light and dark mode"></p>

<table>
  <tr>
    <th>Home by genre, with clay genre icons · 按类型分组</th>
    <th>Home by series · 按系列分组</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Home by genre, with clay genre icons"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Home by series"></td>
  </tr>
  <tr>
    <th>Genre and series on the game page · 游戏页显示类型与系列</th>
    <th>Note, Ask AI, status, how to play, saves · 备忘录、问 AI、状态、操作说明、存档</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Genre and series on the game page"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Note, Ask AI, status, how to play, saves"></td>
  </tr>
  <tr>
    <th>Gamepad keyboard; English titles match too · 手柄键盘，英文名也能搜</th>
    <th>Ask the AI what to play · 跟 AI 聊聊玩什么</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Gamepad keyboard; English titles match too"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Ask the AI what to play"></td>
  </tr>
  <tr>
    <th>AI tools: series, picks, storage, break reminder · AI 工具与休息提醒</th>
    <th>Storage: size per console, unplayed big games · 存储空间</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI tools: series, picks, storage, break reminder"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Storage: size per console, unplayed big games"></td>
  </tr>
  <tr>
    <th>Game page with video preview · 游戏页与视频预览</th>
    <th>Logo, cover or video, per game · 每个游戏可选上屏显示</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Game page"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Game menu"></td>
  </tr>
  <tr>
    <th>List view · 列表视图</th>
    <th>Play statistics · 游玩统计</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="List view"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Play statistics"></td>
  </tr>
  <tr>
    <th>Colour themes · 颜色主题</th>
    <th>Theme colours reach the backdrops · 主题色会染到上屏背景</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Theme menu"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Pink theme"></td>
  </tr>
  <tr>
    <th>Dark mode · 深色模式</th>
    <th>Dark mode · 深色模式</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Dark mode, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Dark mode, statistics"></td>
  </tr>
  <tr>
    <th>Wi-Fi transfer · Wi-Fi 传输</th>
    <th>Tidy-up tools · 整理工具</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Wi-Fi transfer"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>GameCube, with Dolphin · GameCube（Dolphin）</th>
    <th>Hold A to move a folder · 按住 A 拖动文件夹</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Moving a folder"></td>
  </tr>
  <tr>
    <th>Choose cheats before importing, names translated · 先挑再导入（已翻译）</th>
    <th>Game details: size, discs, screenshots · 游戏详情：大小、碟数、截图</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Choosing cheats"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Game details"></td>
  </tr>
  <tr>
    <th>3DS cheats, translated in one tap · 3DS 金手指（一键翻译）</th>
    <th>NDS cheats for DraStic, Chinese database · NDS 金手指</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS cheats"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS cheats"></td>
  </tr>
  <tr>
    <th>RetroArch cheats (GBA) · RetroArch 金手指</th>
    <th>Edit, add, download · 编辑、新建、下载</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA cheats"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Cheat menu"></td>
  </tr>
  <tr>
    <th>Rename or delete a cheat · 改名或删除</th>
    <th>Translate every cheat at once · 一键翻译所有金手指</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Edit a cheat"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>Screensaver (OLED-safe) · 屏保（防烧屏）</th>
    <th>11 languages · 11 种语言</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Screensaver"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Languages"></td>
  </tr>
</table>

<details>
<summary><b>简体中文截图</b></summary>

<p align="center"><img src="docs/screenshots/zh_light-dark.png" alt="浅色与深色"></p>

<table>
  <tr>
    <th>按类型分组（黏土风格类型图标）</th>
    <th>按系列分组</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="按类型分组（黏土风格类型图标）"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="按系列分组"></td>
  </tr>
  <tr>
    <th>游戏页显示类型 · 系列</th>
    <th>备忘录、问 AI、状态、操作说明、存档</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="系列"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="备忘录、问 AI、状态、操作说明、存档"></td>
  </tr>
  <tr>
    <th>手柄键盘：英文名也能搜到中文游戏</th>
    <th>跟 AI 聊聊玩什么</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="手柄键盘：英文名也能搜到中文游戏"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="跟 AI 聊聊玩什么"></td>
  </tr>
  <tr>
    <th>AI 游戏系列、今天玩什么、存储空间、休息提醒</th>
    <th>存储空间：各主机占用、没玩过的大游戏</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI 游戏系列、今天玩什么、存储空间、休息提醒"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="存储空间：各主机占用、没玩过的大游戏"></td>
  </tr>
  <tr>
    <th>主页</th>
    <th>游戏页</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_home.png" alt="主页"></td>
    <td><img src="docs/screenshots/zh_game.png" alt="游戏页"></td>
  </tr>
  <tr>
    <th>上屏显示：Logo / 封面 / 视频</th>
    <th>列表视图</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_menu.png" alt="游戏菜单"></td>
    <td><img src="docs/screenshots/zh_list.png" alt="列表视图"></td>
  </tr>
  <tr>
    <th>GameCube（Dolphin）</th>
    <th>按住 A 拖动文件夹</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="拖动文件夹"></td>
  </tr>
  <tr>
    <th>下载的金手指先挑再导入（名称已翻译）</th>
    <th>游戏详情：大小、碟数、截图</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="挑选金手指"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="游戏详情"></td>
  </tr>
  <tr>
    <th>3DS 金手指：一键翻译成中文</th>
    <th>NDS 金手指：DraStic 中文金手指库</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS 金手指"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS 金手指"></td>
  </tr>
  <tr>
    <th>GBA 金手指（自带中文库）</th>
    <th>金手指：编辑、新建、下载</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA 金手指"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="金手指菜单"></td>
  </tr>
  <tr>
    <th>改名或删除</th>
    <th>一键翻译所有金手指</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="编辑金手指"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="整理工具"></td>
  </tr>
  <tr>
    <th>游玩统计</th>
    <th>游玩统计 · 深色</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_stats.png" alt="游玩统计"></td>
    <td><img src="docs/screenshots/zh_dstats.png" alt="游玩统计 深色"></td>
  </tr>
  <tr>
    <th>颜色主题</th>
    <th>樱花粉主题</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_themes.png" alt="主题"></td>
    <td><img src="docs/screenshots/zh_pink.png" alt="樱花粉"></td>
  </tr>
  <tr>
    <th>Wi-Fi 传输</th>
    <th>整理工具</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_wifi.png" alt="Wi-Fi 传输"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="整理工具"></td>
  </tr>
  <tr>
    <th>深色模式</th>
    <th>屏保</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_dark.png" alt="深色模式"></td>
    <td><img src="docs/screenshots/zh_saver.png" alt="屏保"></td>
  </tr>
</table>

</details>

## Features

### Your library · 游戏库

- **Finds your games by itself** – add folders in Settings → Game folders, or let it pick up any `Roms` folder on internal storage and SD cards. Platforms are recognised from folder names (`gba`, `Nintendo DS`, `psx`, …) and file extensions.
- **Platforms** – 3DS, NDS, GBA (also GBA rumble and hack folders), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita, Switch, plus artwork for PS4, PS5, Xbox 360 and Xbox Series.
- **Multi-disc games as one** – `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` in one folder, or a Pegasus `files:` list, become a single entry; RetroArch and DuckStation get an `.m3u` playlist of the discs so they can change discs in-game.
- **Android games and apps** – installed games get their own page automatically; pick any apps (video, browser, …) to show on the Apps page.
- **Works with existing setups** – reads Pegasus `metadata.pegasus.txt`, ES-DE / EmulationStation `gamelist.xml` and `downloaded_media`, RetroArch thumbnails, and `media/`, `covers/`, `images/` folders next to the games.
- **Your own game info** – names, descriptions and pictures in a `DuoLauncher` folder that wins over every other source, with names and descriptions per language (`name.en:`, `description.en:` …); see [Your own game info](#your-own-game-info).
- **Missing covers filled in** – Settings → Tools → Fetch missing covers downloads box art from the libretro thumbnail sets: for NDS and GBA matched by the game code inside the ROM (zipped ROMs too, with title screens), so translated and renamed files still find their pictures; for PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES and Mega Drive by the game's English title or an English file name.
- **Icons straight from the ROMs** – NDS banner icons and 3DS SMDH icons (decrypted dumps); installed 3DS titles show their name in the interface language.
- **Your own folders** – the "+" tile makes a folder with its own name, colour, icon and backdrop (from the bundled art or your own pictures); add any game or app to it with Y, and take it out again with Y → Remove from this folder.
- **Continue and favorites** – the first home tile jumps straight back into the last game; star games with Y to collect them in a Favorites folder.
- **Search with the gamepad** – R3 or the search button opens a keyboard on the upper screen made for the d-pad (A types, B deletes, X space, Y system keyboard, SELECT clears, START or R3 done), with recent searches (and a chip to clear them) and genre chips above it; hold L and speak to search by voice. Results update as you type. Chinese titles also match pinyin, its initials and the start of each syllable (`kdyg`, `mlao` → 马里奥), and the English title; opened from inside a platform it searches only that platform, with an "All platforms" button to widen it.
- **Genres and series** – Settings → Tools → AI: sort into genres / find game series files every game under one of 14 genres and under its series, kept in `DuoLauncher/<platform>/genres.txt` and `series.txt` on the card so it is done once. A built-in table (`assets/gamedb.tsv`, 3,860 file names – every game in the Pegasus G pack lists for these consoles, with both the `.iso` and `.cso` names where a pack has both) already knows the genre, series and official English title of the common ROM sets by platform and file name, so those need no AI at all. The genre and series of a game show on the upper screen. SELECT on the home page switches between consoles, genres and series; a game's genre can be changed by hand (Y → Genre). When a scan finds new games the launcher offers to sort them ("Don't ask again", or the switch under Settings → Tools → AI: sort into genres, turns the question off). Genres already written by Pegasus or ES-DE ("Action RPG", "角色扮演" …) are mapped onto the 14 genres; series names show in Chinese or English with the interface language.
- **Smart folders** – the "+" tile also makes folders that fill themselves: recently added (the last 30 days), not played yet, most played, playing, want to play, finished and top rated; Y on one shows its rule.
- **Status and rating** (Y → Status) – mark a game as playing, want to play, finished or dropped and give it one to five stars; shown on the upper screen.
- **New games stand out** – games added in the last week carry a NEW badge until they are played.
- **Your catalog** – after every scan the whole library is written to `DuoLauncher/catalog.tsv` (platform, file, title, English title, genre, series, play time, status, rating), for spreadsheets, other tools and the AI.
- **Merge folders** – Y → Merge folders on a console shows its games inside another console folder (two PS1 folders from different places become one); the games keep their own emulator, folders with the same name merge by themselves, and "Split off" in the same menu undoes it.
- **Sort and filter** (SELECT) – default order, name, recently played or most played, and show only 简 / 繁 / 英 games.
- **Hide what you don't play** – hide a platform folder, or a single game or app (Y → Hide); bring them back in Settings.
- **Jump by letter** – L2 / R2 jump to the previous / next first letter in a platform; Chinese titles count by their pinyin initial.
- **What to play?** – a home tile that picks a random game, never-played games more often, and shows it.
- **Notes** (Y → Note) – a line or two about where you are and what to do next, typed or spoken, shown on the upper screen with the game.
- **Game details** (Y → Details) – file, size, number of discs, folder, emulator, play time, the official English title when known, and the game's screenshots (named after the game, or taken while it ran) on the upper screen.
- **Starts instantly** – the last scan is cached, so the home screen is up in a second and the scan runs in the background.

### Playing · 游玩

- **Emulator per game or per platform** (Y button) – only installed emulators are offered:

  | Platform | Emulators (first one is the default) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (performance mode) |
  | NDS | DraStic, DraStic (plain path), RetroArch · melonDS DS |
  | GB / GBC | RetroArch · Gambatte, mGBA |
  | GBA | RetroArch · mGBA, gpSP, gpSP Rumble |
  | NES | RetroArch · FCEUmm, Mesen |
  | SNES | RetroArch · Snes9x |
  | MD | RetroArch · Genesis Plus GX, PicoDrive |
  | N64 | RetroArch · Mupen64Plus-Next, Mupen64Plus FZ |
  | GameCube / Wii | Dolphin MMJR, Dolphin |
  | PS1 | RetroArch · PCSX ReARMed, SwanStation, DuckStation |
  | PS2 | ARMSX2, AetherSX2 |
  | PSP | PPSSPP, RetroArch · PPSSPP |
  | PS Vita | Vita3K (by title ID, e.g. a file named `PCSE00000.psvita` or a name containing it) |
  | Switch | Eden |

- **Citra MMJ (performance mode)** – the same Citra MMJ build repackaged under the AnTuTu benchmark's package name, which many Android systems run at full clocks; offered only when that package really is Citra.
- **Cheats** – see [Cheats](#cheats).
- **Play time** – how long and when you last played each game, Switch-style. With Usage access granted it counts only the time the game was really on screen.
- **Play statistics** – total play time, this week by day, time per platform and the most played games.
- **Video previews** – a game's video snap (`videos/` or `video.mp4` in its media folder) starts on the upper screen after a moment; per game you can choose logo, cover or video instead (Y → Top screen).
- **Sounds** – soft UI sounds (can be turned off) and each 3DS game's own banner tune when it is selected.
- **Where you left off** – a played game's newest screenshot or save-state picture becomes its upper-screen background.
- **How to play** (Y → How to play) – the AI writes the controls (including swapped confirm / cancel buttons in Japanese releases) and the basics a newcomer needs, once per game; up / down scrolls, Y writes it again.
- **Ask the AI** (Y → Ask AI) – type or say a question about the game and the answer appears on the upper screen.
- **What to play today** – Y on the random-game tile (or Settings → Tools) has the AI pick six games from your own library to match your taste, with a reason for each; "Ask AI what to play" turns it into a conversation – say or type what you feel like ("a relaxed RPG, not too long"), then narrow it down ("something with action instead").
- **When a game does not start** – a missing file, an emulator that is not installed, one that cannot be opened that way, or a game that closes at once each get a message saying what to do.
- **Break reminder** (Settings → Tools → Break reminder) – after two hours in a game (or one or three, or off) the launcher suggests a break when you come back.

### Look and feel · 外观

- **Upper screen (1920×1080)** – each platform's console on its own illustrated backdrop, or the selected game's logo over its blurred cover art, with platform, genre · series, emulator and play-time tags and a short description; the clock and battery sit in the corner.
- **Lower screen (1280×960)** – platform folders, a zoomable icon grid, and a list view (press X until the grid becomes a list with names, play time and last played). Works with the gamepad and with touch: tap to select, tap again to open, flick to scroll, and tap the pills at the top instead of L, R, X, SELECT or search.
- **Themes** – seven colour schemes, custom colours, and theme packs with console art, backdrops, font, sounds and music; a theme also tints the upper-screen backdrops. See [Themes](#themes).
- **Light and dark mode** – follows the system or set it yourself, with separate backdrops for each.
- **Everything on the home page is yours** – every built-in tile (Continue, Recently played, Favorites, New folder, Search, Statistics, What to play?, Settings, Android home) and every platform folder can be renamed, recoloured or hidden, and brought back under Settings → Hidden folders.
- **Custom artwork** – replace any platform's console picture, light and dark backdrop, and folder colour in Settings → Platform look.
- **Text size** – four sizes for both screens.
- **Cover margin** – game pictures fill their tiles, or all sit with the same small, medium or large margin; white frames baked into cover pictures are trimmed.
- **Clay icons everywhere** – every home tile, console and genre folder can get its own picture or one drawn by the image service in the bundled clay style (Y → Icon); Settings → Tools → AI: genre folder icons can draw the whole set again in one style.
- **Screensaver** – after a few idle minutes the upper screen shows your covers with a clock and the lower screen goes black. Text changes corner with every picture and drifts slowly, so nothing burns into an OLED panel.

### Handy tools · 实用工具

- **Wi-Fi transfer** – Settings → Wi-Fi transfer shows an address and a PIN; open the address in a browser on the same network, enter the PIN, and drop ROMs or theme packs straight into any platform folder. The same page has the AI translation, AI pictures and RetroAchievements settings behind an admin password (six random digits, or your own of four characters or more), so long keys can be pasted from a phone.
- **RetroAchievements** – with your user name and Web API key, recently played games and their achievement progress, and a 🏆 tag on matching games.
- **Updates** – Settings → About checks the latest GitHub release once a day and links to it.
- **Save backups** – back up and restore the saves of DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar and Citra in one zip, kept in `DuoLauncher/backups` on the SD card (internal storage when there is no card). After three hours of play without a backup the launcher reminds you (Back up now, Always, In 3 hours, Don't remind) or, if you set Settings → Tools → Save backup reminder to "Back up by itself", makes one by itself (the newest ten automatic ones are kept; your own are never removed). A restore replaces each save only once its copy is complete.
- **One game's saves** (Y → Saves) – the save files of a single game, backed up and restored on their own.
- **Storage** (Settings → Tools → Storage) – free space, the size of each console's games, the biggest games never played, and deleting a game's files (all discs, and the files a `.cue` or `.m3u` names) after a confirmation.
- **Tidy-up tools** (Settings → Tools) – find duplicate games, list games without pictures, fetch missing covers, translate every cheat at once, AI names and descriptions for a whole platform, AI genres and series, AI pictures for every game that lacks a cover, background or title logo, AI icons for the genre folders, export the current theme as a zip, and a diagnostics page.
- **Button layout** – swap A/B and X/Y for Nintendo- or Xbox-style confirm buttons.

### Setup · 设置

- **Home screen** – can be set as the default home app so it starts on boot; the Android home tile (or Settings → Exit to Android home) leads back to the system home screen, and Settings → Android settings opens the system settings.
- **First-start setup** – language, then every permission it uses (file access, usage access for play time, home screen) on one page, also under Settings → Permissions.
- **11 languages** – 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Cheats

Y → Cheats on a game lists its cheats; A switches one on or off. 在游戏上按 Y →「金手指」，按 A 开关。

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Where they live | `azahar/cheats/` and `citra-emu/cheats/`, `<title ID>.txt` – written for both, so switching emulators keeps them | the whole list in `DuoLauncher/cheats/NDS/`; the ones switched on go to DraStic's own custom cheats (`DraStic/cheats/<game file name>.cht`), which work for translated ROMs too | `PSP/Cheats/<game ID>.ini`, PPSSPP's own cheat files; the game ID is read from the ISO, CSO or EBOOT.PBP | `RetroArch/cheats/<core>/<game file name>.cht`, loaded by RetroArch when the game starts |
| Already on the device | – | DraStic's `usrcheat.dat` (Chinese on Retroid handhelds), matched by the game code only, so patched and translated ROMs find their cheats even where DraStic itself shows none | PPSSPP's `cheat.db`, and on Retroid handhelds Chinese cheat files for about 4,300 games | Retroid ships Chinese cheats for hundreds of games (`000 中文金手指` and the core folders); a game without its own file gets a copy, matched by name |
| Download | [Sharkive](https://github.com/FlagBrew/Sharkive), about 650 games, by title ID read from the ROM | [libretro-database](https://github.com/libretro/libretro-database), about 4,200 games, by the game code in the ROM header | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), about 4,300 games, by game ID | [libretro-database](https://github.com/libretro/libretro-database) for GBA, by the game code in the ROM header |

- **Translate** – English cheat names become the interface language in one tap, for one game or for every game at once (Settings → Tools). Common cheats come from a built-in glossary (无限血量, 金钱最大, 主代码 …); the rest go to the free MyMemory service, with Google's free endpoint as a second try. Version tags (`v1.2`) and button hints (`(START+L)`) are kept as they are, and the original English name stays in the file as a note.
- **Choose before importing** – a download first lists what it found, already translated, with Select all / Select none; only the ticked ones are added.
- **Translate with AI** – with an AI key set (see [AI](#ai)), a game's cheats go to the model together with the game's name, so it uses the official names of characters and items (鬼神林克, 心之容器 …); names translated earlier by the free services can be redone.
- **Edit** – rename or delete any cheat, delete all of a game's cheats at once, or add your own with its codes (3DS, NDS and PSP codes are checked as `XXXXXXXX YYYYYYYY`).
- **Safe for DraStic** – flash-cart patches (anti-piracy bypasses, overlay and Wi-Fi fixes) are left out of NDS downloads because DraStic crashes with them switched on, and at most 256 code lines can be on at once, the most DraStic can load.
- **Safe for your files** – the first time a hand-written 3DS cheat file is changed, the original is kept as `.txt.bak`.
- **RetroArch, once** – switch on Quick Menu → Cheats → "Auto-Apply Cheats During Game Load" in RetroArch once, and the cheats you turn on here are active from the moment a game starts.

## AI

Everything here is optional and uses your own account; keys are kept in the launcher's settings on the handheld and sent only to the service you chose. Enter them in Settings, or on the Wi-Fi page from a phone or computer.

- **Settings → AI translation** – DeepSeek, Qwen, OpenAI, Claude, Gemini or any OpenAI-compatible address. Used for cheat names (a whole game in one request) and for Settings → Tools → AI: game names and descriptions, which writes `name.<language>`, `description.<language>` and the official English title (`original:`) into the launcher's own metadata, so a library of Chinese titles reads naturally in English and the other way round. Without a key the free translation services are used for cheats.
- **Settings → AI pictures** – an OpenAI image key (the newest image model the key can use is picked automatically). Y → Details → Make a cover with AI for games without box art, or Settings → Tools → AI pictures for every game of a platform that lacks a cover, background or title logo; Platform look, folder and genre-folder icons and backdrops get "Make with AI", in the same clay-icon and paper-cut style as the built-in art. A picture takes about half a minute.
- **Genres, series, help and picks** – the translation key above also sorts games into genres and series, writes a game's "How to play", answers questions about a game and picks what to play today.
- **Speech** – holding L while the search keyboard is open, or after choosing to speak a note or a question, records from the microphone and sends it to the image service's speech-to-text (`/audio/transcriptions`, OpenAI or compatible). The handheld has no speech recognizer of its own.

AI 功能都是可选的，用你自己的账号；密钥只存在掌机上，只发给你选的服务。可以在设置里填，也可以在手机或电脑上打开 Wi-Fi 页面填（需要掌机上显示的管理密码）。

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

Settings → Theme offers seven built-in colour themes (blue, red, green, purple, orange, pink, graphite), **Custom colours** (pick a main colour and a background colour; every shade for light and dark mode is worked out from them), and any installed theme packs. A theme colours both screens, including the line-art backdrops on the upper screen.

Settings → Platform look sets, per platform, the console picture, a separate light-mode and dark-mode backdrop, and the folder colour.

A theme pack is a folder (or a `.zip` of one, unpacked on first use) in `DuoLauncher/themes/` on the SD card or internal storage, picked in Settings → Theme. Everything is optional; whatever a theme leaves out comes from the built-in look, so a theme can be as small as a few colours.

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

`theme.txt` uses `key: value` lines: `color.<name>` for light mode and `dark.color.<name>` for dark mode, with names such as `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (the upper-screen line art) and `folder.<type>` for the home folders. `base-color: #RRGGBB` alone is enough for a complete colour scheme. [themes/sakura](themes/sakura/theme.txt) is a complete example with every key explained. Settings → Tools → Export current theme saves whatever you have set up as a zip in `DuoLauncher/exports/` that others can drop into their own `themes/` folder; your own pictures (console, folder, genre and tool icons, backdrops) go along under `own/`.

## Controls

| Button | Action |
|---|---|
| D-pad / left stick | Move; hold to keep moving |
| Left / right, L / R in a list or menu | A page at a time |
| A | Open / play; hold A on a home folder to move it |
| B | Back |
| L / R | Previous / next platform |
| L2 / R2 | Previous / next first letter |
| X | Icon size, then list view |
| Y | On a game: emulator, favorite, add to / remove from a folder, cheats, details, genre, note, ask AI, status and rating, how to play, saves, top-screen display, hide. On a console folder: default emulator, rename, merge folders, hide. On a genre or series folder: icon. On an Android app: add to a folder. On the home page: rename, recolour, change the icon of or hide any tile |
| START | Settings (from the home page, even with the Settings tile hidden); in a menu, the same as A |
| SELECT | On the home page: consoles / genres / series; in a folder: sort and language filter |
| R3 | Search |
| L (search keyboard) | Hold and speak |

## Building

No Android Studio or Gradle needed – only the Android SDK command-line tools (build-tools 35, platform android-35) and JDK 17+:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

A debug signing key (`debug.keystore`) is generated on the first build and is not committed.
`shot.ps1` sends key presses over adb and saves screenshots of both screens, which is handy while tweaking the UI.

## Artwork

`assets/consoles/<type>.png` (chibi console renders) and `assets/backgrounds/<type>.jpg` (paper-cut line-art backdrops, with their own versions in `backgrounds/dark/`) were made for this project. The backdrops are greyscale, so a colour theme can tint them.
Platforms without a picture fall back to a drawn console.

## Launch notes

- AzaharPlus only accepts a tree URI under a folder it already has access to:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` with `-e GamePath <path>`
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

## License

[MIT](LICENSE)
