<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><a href="README.md">English</a> · <a href="README.fr.md">Français</a> · <b>简体中文</b> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <a href="README.es.md">Español</a> · <a href="README.de.md">Deutsch</a> · <a href="README.it.md">Italiano</a> · <a href="README.pt.md">Português</a> · <a href="README.ru.md">Русский</a></p>

一款给 **Retroid Pocket Duo** 以及其他带副屏的安卓掌机用的双屏游戏启动器，思路参考了 3DS 的主菜单。
告诉它你的 ROM 放在哪，它会自己找出游戏，按主机、类型和系列分好，在下屏列出游戏、上屏显示图片，再用你选好的模拟器把游戏打开。

**下载：** 最新的 APK 在 [Releases 页面](https://github.com/GenKnot/DuoLauncher/releases/latest)（需要 Android 11 或更高版本）。新版本直接覆盖安装，所有设置和游玩时间都会保留。

## 为什么又做一个启动器？

Duo Launcher 一开始只是一个玩家给自己的掌机做的启动器，到现在也还是照着这台掌机的真实用法一点点长出来的。整个启动器有这么几个出发点：

- **两块屏，各干各的事。** 下屏用来挑：大文件夹、封面网格、列表、菜单。上屏用来看：主机配着自己的插画背景、游戏 Logo 叠在封面上、视频预览、玩了多久、给自己留的备忘，或者和 AI 的对话。谁都不用跟谁抢地方。
- **手柄优先。** 所有功能都不用碰屏幕：专门为十字键设计的键盘、按住方向键一直滚动、L / R 翻页、按住 L 语音搜索、按住 R 和 AI 说话。触屏当然也能用，只是不必用。
- **游戏库保持原样。** 你已经有的 Pegasus（天马G）和 ES-DE 的资料和图片它都能直接读；它自己加的东西都放在自己的 `DuoLauncher` 文件夹里，绝不改写别的前端的文件。
- **AI 为你干活，用的是你自己的密钥。** 翻译、分类、画图、出主意、聊天，用的都是*你*自己选、自己付费的服务。密钥只存在掌机上，每一次请求都会记在设置里，并附上估算的花费，没有任何东西在背后偷偷运行。
- **什么都能改、能藏、能分享。** 主页上每个格子都能改名、换颜色、换图、挪位置或隐藏。整套设置可以导出成一个文件发给朋友，里面不会带上你的密钥和游玩记录。

欢迎在 [Issues](https://github.com/GenKnot/DuoLauncher/issues) 里提建议、报 bug。

## 亮点

- **两块屏，一个启动器**：在下屏挑游戏；上屏看 Logo、封面、视频预览、游玩时间和简介。
- **所有模拟器集中在一处**：3DS、NDS、GBA、GB/GBC、NES、SNES、N64、GameCube、Wii、Mega Drive、PS1、PS2、PSP、PS Vita 和 Switch，模拟器可以按主机选，也可以单个游戏单独选。多碟游戏只显示成一个。
- **自动分好类**：每个游戏都会归到 14 个类型之一，并归到它所在的系列（伊苏、恶魔城、超级机器人大战……）。主页可以按主机、类型或系列来排；智能文件夹会自动更新「最近新增」「在玩」「想玩」「通关了」和「评分最高」。
- **AI 伙伴**：在任何地方按住 R 说话就行：它会从你自己的游戏库里推荐游戏，告诉你某一段怎么过，讲讲剧情，或者就是陪你聊聊。回答还可以念出来。
- **金手指，统一管理**：3DS、NDS、PSP、GBA 以及所有 RetroArch 平台都有成千上万条现成的金手指，一键翻译成你的语言，可以编辑，不同模拟器之间共用。
- **缺什么补什么**：从 libretro 封面库下载真实的游戏封面，补上官方英文名，还能用 AI 按启动器自己的风格画封面、Logo 和背景。
- **为掌机而做**：防 OLED 烧屏的屏保、用浏览器走 Wi-Fi 传文件、存档备份、游玩统计、RetroAchievements 成就进度、存储空间一览，主页一秒就能打开。
- **随你改**：颜色主题和主题包、浅色和深色模式、字体大小、封面边距、像主机一样的设置页，配置还能导出分享。界面支持 11 种语言。

## 截图

<p align="center"><img src="docs/screenshots/zh_light-dark.png" alt="浅色与深色"></p>

<table>
  <tr>
    <th>像 3DS 一样的设置页</th>
    <th>设置页（深色）</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_settings.png" alt="像 3DS 一样的设置页"></td>
    <td><img src="docs/screenshots/zh_settings_dark.png" alt="设置页（深色）"></td>
  </tr>
  <tr>
    <th>AI 用量与花费</th>
    <th>导出、导入配置</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_ai_usage.png" alt="AI 用量与花费"></td>
    <td><img src="docs/screenshots/zh_config.png" alt="导出、导入配置"></td>
  </tr>
  <tr>
    <th colspan="2">深色模式的手绘插画</th>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/screenshots/zh_dark_home.png" width="50%" alt="深色模式的手绘插画"></td>
  </tr>
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

## 上手

1. **安装 APK**：从 [Releases 页面](https://github.com/GenKnot/DuoLauncher/releases/latest) 下载并打开。
2. **选择语言**，然后在同一页里给它需要的权限：文件访问（用来找游戏）、使用情况访问（这样游玩时间只算游戏真正在屏幕上的时间），愿意的话再把它设为桌面。之后随时可以在「设置 → 系统 → 权限」回到这一页。
3. **让它自己找游戏。** 内部存储或 TF 卡上的 `Roms` 文件夹会被自动识别。其他文件夹可以在「设置 → 主页与游戏库 → 游戏文件夹」里添加。主机是按文件夹名（`gba`、`Nintendo DS`、`psx`……）和文件扩展名来认的。
4. **选模拟器**：不想用默认模拟器的地方，在主机文件夹上按 Y 就是改整个主机，在游戏上按 Y 就只改这一个游戏。列出来的只有已经装好的模拟器。
5. **可选：填 AI 密钥**，在「设置 → AI 与语音」里。可以直接在掌机上填，更方便的是用电脑或手机通过 Wi-Fi 填（「设置 → 存档、传输与分享 → Wi-Fi 传输」）。除了 AI 功能，其他功能都不需要密钥。
6. **可选：设为桌面**（「设置 → 系统 → 设为默认桌面（开机启动）」），这样掌机一开机就进 Duo Launcher。主页上的「安卓桌面」格子随时能回到系统自带的桌面。

## 基础操作

**主页**（下屏）放着你的主机文件夹和几个内置格子：「继续游玩」（直接回到上次玩的游戏）、「最近玩过」、「收藏」、「新建文件夹」、「搜索」、「游玩统计」、「存储空间」、「今天玩什么」、「设置」和「安卓桌面」。按 SELECT 可以在主机、类型、系列三种排法之间切换。

**文件夹**里的游戏以封面网格显示。按 X 封面变小，再按一次变成列表，显示游戏名、游玩时间和上次玩的时间。按 SELECT 排序（默认、名称、最近玩过、游玩时间最长），还可以只显示简体中文、繁体中文或英文版本。L2 / R2 跳到上一个或下一个首字母，中文游戏按拼音首字母算。

**上屏**跟着当前选中的东西变：选中主机时，显示主机的背景图、游戏数量和模拟器；选中游戏时，显示游戏 Logo 叠在模糊的封面上，配上主机、类型 · 系列、模拟器、游玩时间和一段简介，停一会儿会播放视频预览，或者显示你上次玩到的画面。

**Y** 是当前选中项的菜单。在游戏上：模拟器、收藏、文件夹、金手指、游戏详情、类型、备忘录、问 AI、状态和评分、操作说明、存档、上屏显示、隐藏。在主机文件夹上：默认模拟器、重命名、合并文件夹、隐藏。在内置格子上：名称、颜色、图标和背景图。

### 按键

| 按键 | 作用 |
|---|---|
| 十字键 / 左摇杆 | 移动；按住一直移动 |
| 在列表或菜单里按左 / 右、L / R | 一次翻一页 |
| A | 打开 / 开始游戏；在主页文件夹上按住 A 可以拿起来挪位置 |
| B | 返回 |
| L / R | 上一个 / 下一个主机（在设置里是切换分类） |
| 按住 L | 语音搜索 |
| 按住 R | 和 AI 说话 |
| L2 / R2 | 上一个 / 下一个首字母 |
| X | 调封面大小，再按切到列表 |
| Y | 当前游戏、文件夹或格子的菜单 |
| START | 设置（在主页任何位置都能按，「设置」格子隐藏了也行）；在菜单里等于 A |
| SELECT | 在主页：主机 / 类型 / 系列；在文件夹里：排序和语言筛选 |
| R3 | 搜索 |
| 在搜索键盘里 | A 输入，B 删除，X 空格，Y 系统键盘，SELECT 清空，START 或 R3 完成，按住 L 说话 |
| 在 AI 聊天里 | 按住 R 说话，Y 打字，X 开始新对话，SELECT 打开聊天记录，L2 / R2 往回翻，B 关闭；用十字键和 A 从下面的游戏里挑 |

所有操作也都能用触屏：点一下选中，再点一下打开，滑动滚动，顶部那排小按钮可以代替 L、R、X、SELECT 和搜索。A/B 和 X/Y 可以互换，按任天堂或 Xbox 的习惯来确认（「设置 → 游玩 → 按键设置」）。

## 游戏库

- **自己找游戏。** 会在内部存储和 TF 卡上找 `Roms` 文件夹；更多文件夹可以在「设置 → 主页与游戏库 → 游戏文件夹」里添加。上一次扫描的结果会缓存下来，所以主页一秒就能打开，下一次扫描在后台悄悄完成。
- **支持的主机**：3DS、NDS、GBA（还有 GBA 震动版和改版文件夹）、GBC、GB、NES、SNES、N64、GameCube、Wii、Mega Drive、PlayStation 1/2/3、PSP、PS Vita 和 Switch，PS4、PS5、Xbox 360 和 Xbox Series 也有主机图。
- **多碟游戏合成一个。** 同一文件夹里的 `Disc A/B`、`Disc 1/2`、`CD1/CD2`、`(Disc 2 of 3)`，或者 Pegasus 的 `files:` 列表，都会合成一条。RetroArch 和 DuckStation 会拿到一个包含所有碟的 `.m3u` 播放列表，游戏里就能换碟。
- **安卓游戏和应用**：装好的安卓游戏会自动归到自己的文件夹；其他应用（视频、浏览器……）可以加进「应用」文件夹。
- **兼容你已有的资料。** 它能读 Pegasus 的 `metadata.pegasus.txt`、ES-DE / EmulationStation 的 `gamelist.xml` 和 `downloaded_media`、RetroArch 的缩略图，以及游戏旁边的 `media/`、`covers/` 和 `images/` 文件夹。这些文件一个都不会改；启动器自己加的内容放在它自己的 `DuoLauncher` 文件夹里（见[自定义游戏信息](#自定义游戏信息)）。
- **发现新游戏。** 把游戏拷到卡上，下次扫描就能找到；新游戏会挂一个 NEW 标记，持续一周（玩过就消失），并会问你要不要分到类型和系列里。这个询问可以关掉。按 Y →「标为已看过」可以去掉单个游戏的标记；「全部标为已看过」（在主机或文件夹上按 Y，或在「设置 → 主页与游戏库」里）一次去掉一批；那里的「NEW 标记」开关可以彻底关掉。
- **合并文件夹。** 同一个主机的两个文件夹放在不同地方（比如两个 PS1 文件夹），可以显示成一个：在主机上按「Y → 合并文件夹」。每个游戏保留各自的模拟器，同名文件夹会自动合并，用「拆开」可以撤销。
- **自己的文件夹。** 「新建文件夹」格子可以建一个有自己名称、颜色、图标和背景图的文件夹。在任意游戏或应用上按 Y 就能加进去，用「Y → 从这个文件夹移除」再拿出来。
- **智能文件夹**会自己装游戏：最近新增（近 30 天）、还没玩过、玩得最多、在玩、想玩、通关了、评分最高。在智能文件夹上按 Y 可以看到它的规则。
- **继续游玩和收藏。** 主页第一个格子直接回到上次玩的游戏。在游戏上按 Y 加星，就会收进「收藏」文件夹。
- **藏起不玩的**：主机文件夹，或单个游戏、应用都能隐藏（「Y → 隐藏这个游戏」）；在「设置 → 主页与游戏库」里可以找回来。
- **游戏清单。** 每次扫描后，整个游戏库都会写进 `DuoLauncher/catalog.tsv`（主机、文件、游戏名、英文名、类型、系列、游玩时间、状态、评分），可以拿去用表格打开、给别的工具用，或者交给 AI。

### 类型与系列

两千个游戏光按主机翻实在太累，所以每个游戏还会分到 14 个类型之一（角色扮演、动作、冒险、平台跳跃、战棋、策略、射击、益智、赛车、体育、格斗、模拟经营、派对音乐、其他），并标上它所属的系列。

- **先用内置资料。** 启动器里自带一张表（`assets/gamedb.tsv`，收录 3,860 个文件名，覆盖天马G 整合包这些主机列表里的全部游戏，整合包同时有 `.iso` 和 `.cso` 的，两种文件名都收了），按主机和文件名就能认出常见 ROM 合集的类型、系列和官方英文名。表里有的游戏，完全不需要 AI，也不需要联网。
- **剩下的交给 AI。** 「设置 → AI 与语音」里的「AI 游戏分类」和「AI 游戏系列」会处理剩下的游戏，结果存在卡上的 `DuoLauncher/<console>/genres.txt` 和 `series.txt` 里，做一次就够，重装也不会丢。Pegasus 或 ES-DE 已经写好的类型（"Action RPG"、"角色扮演"……）会自动对应到这 14 个类型上。
- **手动设置**：在任意游戏上按「Y → 类型」。
- **显示在屏幕上。** 上屏会显示游戏的类型 · 系列；在主页按 SELECT，文件夹就会变成按类型或按系列排。系列名跟随界面语言。

### 搜索

按 R3、点「搜索」格子或搜索按钮，上屏会弹出一个专为十字键设计的键盘，上面有最近搜索和一排类型标签；边输入，下屏边出结果。中文游戏名可以用全拼、拼音首字母或者每个字的拼音开头来搜（`kdyg`、`mlao` → 马里奥），每个游戏也能用官方英文名搜到，所以输入 `zelda` 就能找到塞尔达传说。在某个主机里打开搜索时只搜这个主机，点「全部平台」可以扩大范围。不想打字的话，按住 L 直接说游戏名。

### 自动补全的图片

- **真实封面**：「设置 → 整理工具 → 联网补全封面」会从 libretro 封面库下载封面。NDS 和 GBA 游戏是按 ROM 里的游戏代码来匹配的（压缩包里的 ROM 也行，还会带上标题画面），所以汉化过、改过名的文件也能找到图；PlayStation 1/2、PSP、N64、GameCube、Wii、3DS、GB/GBC、NES、SNES 和 Mega Drive 游戏按英文名匹配。
- **直接从 ROM 里取图标**：NDS 的 banner 图标和 3DS 的 SMDH 图标；装进模拟器的 3DS 游戏会用界面语言显示名称。
- **封面看起来整齐一致**：封面可以铺满格子，也可以统一留出小、中、大三种边距（「设置 → 外观与声音 → 封面边距」）；封面图里自带的白边会被裁掉。
- **AI 画图**：有了图片密钥，就能给没有图的游戏画封面、标题 Logo 和背景（「Y → 游戏详情 → AI 生成封面」，或者在「设置 → AI 与语音 → AI 生图」里一次画完整个主机）。详见 [AI](#ai)。

## 游玩

- **模拟器可以按游戏或按主机设置**（按 Y）。只列出已经装好的模拟器：

  | 主机 | 模拟器（第一个是默认） |
  |---|---|
  | 3DS | AzaharPlus、Azahar、Citra MMJ、Citra MMJ（性能模式） |
  | NDS | DraStic、DraStic（普通路径）、RetroArch · melonDS DS |
  | GB / GBC | RetroArch · Gambatte、mGBA |
  | GBA | RetroArch · mGBA、gpSP、gpSP Rumble |
  | NES | RetroArch · FCEUmm、Mesen |
  | SNES | RetroArch · Snes9x |
  | Mega Drive | RetroArch · Genesis Plus GX、PicoDrive |
  | N64 | RetroArch · Mupen64Plus-Next、Mupen64Plus FZ |
  | GameCube / Wii | Dolphin MMJR、Dolphin |
  | PS1 | RetroArch · PCSX ReARMed、SwanStation、DuckStation |
  | PS2 | ARMSX2、AetherSX2 |
  | PSP | PPSSPP、RetroArch · PPSSPP |
  | PS Vita | Vita3K（按游戏 ID 启动，比如文件名叫 `PCSE00000.psvita`，或者文件名里带有这个 ID） |
  | Switch | Eden |

- **Citra MMJ（性能模式）**：就是同一个 Citra MMJ，只是重新打包成了安兔兔跑分软件的包名，很多安卓系统会让这个包名满频运行。只有当这个包里装的确实是 Citra 时才会出现这个选项。
- **游玩时间和统计**：像 Switch 一样记录每个游戏玩了多久、上次什么时候玩的；给了使用情况访问权限后，只统计游戏真正显示在屏幕上的时间。「游玩统计」格子里有总时长、本周每天的时长、各主机的时长和玩得最多的游戏。
- **上次玩到哪**：玩过的游戏，会用最新的截图或即时存档画面作为上屏背景。
- **状态、评分和备忘录**：把游戏标成在玩、想玩、通关了或弃坑，再打一到五颗星（「Y → 状态」）；写一句或说一句现在打到哪里、下一步要做什么（「Y → 备忘录」）。下次再选中这个游戏时，这些都会显示在上屏。
- **游戏详情**（「Y → 游戏详情」）：文件、大小、碟数、所在文件夹、模拟器、游玩时间、官方英文名，上屏还会显示游戏截图。
- **视频预览和音效**：选中游戏停一会儿，上屏就会播放视频预览（也可以每个游戏单独选显示 Logo 或封面：「Y → 上屏显示」）。轻柔的界面音效，以及每个 3DS 游戏自己的横幅音乐，都可以关掉。
- **游戏打不开时**：文件不见了、模拟器没装、模拟器打不开这种文件、游戏一开就闪退，每种情况都会提示你该怎么办。
- **休息提醒**（「设置 → 游玩」）：在一个游戏里玩满一、二或三小时后，回到启动器时会提醒你休息一下。
- **RetroAchievements**：填上用户名和 Web API 密钥，就能看到最近玩过的游戏和它们的成就进度，有成就的游戏会挂一个 🏆 标记。

## AI 伙伴

这些全都是可选的，用的是你自己选的 AI 服务和你自己的密钥（见 [AI](#ai)）。

- **随时开口聊。** 在主页、文件夹或搜索键盘里按住 R 说话，或者按 Y 打字。对话显示在上屏；它推荐或找到的游戏显示在下屏，按 A 就能直接开玩。可以问它玩什么（「来个轻松点的 RPG，别太长」），再缩小范围（「动作多一点的」），问当前选中的游戏某一段怎么过，问剧情、问角色，或者随便聊聊。半小时内它都记得你们聊过什么。
  为什么这样做：下屏本来就是挑游戏的地方，所以 AI 推荐的游戏放在下屏；上屏空出来专门给你看文字。而且在掌机上，按住肩键说一句话，比翻什么菜单都快。
- **新对话、聊天记录。** 在聊天里按 X 开始新对话；按 SELECT 列出以前的对话（最新的在最上面），可以重新打开，也可以一段段删除或一次清空（「设置 → AI 与语音 → AI 聊天记录」里也能看）。聊天框打开时，视频预览、3DS 横幅音乐和按键音效都会停下，语音回答按媒体音量播放。
  为什么这样做：换个问题不该拖着上一个话题；值得留的回答不该一重启就没了；游戏预告片盖在语音回答上，回答就白念了。聊天记录只存在掌机上，不会跟着配置导出，「保存 AI 聊天记录」可以关掉。
- **回答念出来**：「设置 → AI 与语音 → AI 语音回复」：可以用 Soniox（要它自己的密钥）或 OpenAI（用图片密钥），能选声音和语速，还有试听按钮。默认关闭，要你自己打开。
- **今天玩什么**：在「今天玩什么」格子上按 Y，AI 会按你的口味从你的游戏库里挑六个游戏，每个都说明理由。直接按这个格子则是随机挑一个，优先挑你从没玩过的。
- **问问这个游戏**（「Y → 问 AI」）：针对选中的游戏提个问题，打字或说话都行，回答显示在上屏。
- **操作说明**（「Y → 操作说明」）：游戏的按键（包括日版游戏确认 / 取消键互换的情况）和新手需要知道的基础玩法，每个游戏只生成一次，之后一直保留。
- **还没有密钥？** 按住 R 会告诉你怎么添加：直接在掌机上填，或者用电脑通过 Wi-Fi 填。

## 金手指

在游戏上按「Y → 金手指」会列出它的金手指，按 A 开启或关闭。

| | 3DS（Azahar、Citra） | NDS（DraStic） | PSP（PPSSPP） | RetroArch（GBA、GB、GBC、NES、SNES、N64、MD、PS1……） |
|---|---|---|---|---|
| 存放位置 | `azahar/cheats/` 和 `citra-emu/cheats/`，文件名 `<title ID>.txt`，两边都会写，所以换模拟器也不会丢 | 完整列表在 `DuoLauncher/cheats/NDS/`；开启的那些会写进 DraStic 自己的自定义金手指（`DraStic/cheats/<game file name>.cht`），汉化 ROM 也能用 | `PSP/Cheats/<game ID>.ini`，就是 PPSSPP 自己的金手指文件；游戏 ID 从 ISO、CSO 或 EBOOT.PBP 里读出 | `RetroArch/cheats/<core>/<game file name>.cht`，游戏启动时由 RetroArch 加载 |
| 机器上已有的 | – | DraStic 的 `usrcheat.dat`（Retroid 掌机上是中文的），只按游戏代码匹配，所以打过补丁、汉化过的 ROM 也能找到金手指，哪怕 DraStic 自己显示没有 | PPSSPP 的 `cheat.db`；Retroid 掌机上还有约 4,300 个游戏的中文金手指文件 | Retroid 自带几百个游戏的中文金手指（`000 中文金手指` 和各个核心的文件夹）；没有专属文件的游戏会按名称匹配，复制一份过来 |
| 下载来源 | [Sharkive](https://github.com/FlagBrew/Sharkive)，约 650 个游戏，按 ROM 里读出的 title ID 匹配 | [libretro-database](https://github.com/libretro/libretro-database)，约 4,200 个游戏，按 ROM 文件头里的游戏代码匹配 | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-)，约 4,300 个游戏，按游戏 ID 匹配 | [libretro-database](https://github.com/libretro/libretro-database) 的 GBA 部分，按 ROM 文件头里的游戏代码匹配 |

- **先挑再导入**：下载后会先把找到的金手指列出来（已经翻译好），可以「全选」或「全不选」，只有勾上的才会加进去。为什么这样做：数据库里一个游戏可能有几十条代码，大多数人只想要其中三条。
- **翻译**：英文金手指名可以一键翻译成界面语言，单个游戏或者所有游戏一起翻都行（「设置 → 整理工具」）。常见的金手指直接用内置词表（无限血量、金钱最大、主代码……）；剩下的交给免费的 MyMemory 服务，不行再试 Google 的免费接口。版本标记（`v1.2`）和按键提示（`(START+L)`）会保留，原来的英文名作为注释留在文件里。
- **用 AI 翻译**：有 AI 密钥的话，一个游戏的金手指会连同游戏名一起发给模型，这样角色和道具都会用官方译名（鬼神林克、心之容器……）。
- **编辑**：任意金手指都能改名或删除，可以一次删掉一个游戏的全部金手指，也可以自己加金手指和代码（3DS、NDS 和 PSP 的代码会按 `XXXXXXXX YYYYYYYY` 格式检查）。
- **对 DraStic 安全**：NDS 下载里会去掉烧录卡补丁（反盗版绕过、overlay 和 Wi-Fi 修正），因为开着它们 DraStic 会崩溃；同时开启的代码最多 256 行，这是 DraStic 能加载的上限。
- **对你的文件安全**：你手写的 3DS 金手指文件第一次被改动时，原文件会保存为 `.txt.bak`。
- **RetroArch 只需设置一次**：在 RetroArch 的 Quick Menu（快速菜单）→ Cheats（金手指）里，把 "Auto-Apply Cheats During Game Load"（加载游戏时自动应用金手指）打开一次，之后在这里开启的金手指从游戏一启动就生效。

## 外观

- **上屏（1920×1080）**：每个主机都有自己的剪纸风格背景；选中游戏时，游戏 Logo 叠在模糊的封面上，配上标签和一段简介；角落里是时钟和电量。
- **下屏（1280×960）**：主机文件夹、可缩放的封面网格和列表视图；底部的按键提示会尽量简短，字体调大后放不下的就不显示。
- **主题**：七套配色、自定义颜色，以及包含主机图、背景、字体、音效和音乐的主题包。主题也会给上屏的线稿背景上色。详见[主题](#主题)。
- **浅色和深色模式**：跟随系统，或者自己设定；两种模式各有一套单独画的背景。
- **主页上的东西都由你做主。** 每个内置格子和主机文件夹都能改名、换颜色、换图标（用你自己的图，或者让图片服务按启动器的黏土风格画一个）、挪位置（按住 A，或者长按）或隐藏，隐藏的可以在「设置 → 主页与游戏库 → 已隐藏的文件夹」里找回。「游玩统计」「存储空间」「今天玩什么」这样的内置格子也可以有自己的上屏背景。
- **平台外观**：每个主机的图片、浅色和深色背景、文件夹颜色都可以替换（「设置 → 外观与声音 → 平台外观」）。深色背景可以让图片服务照着浅色背景重画，两套总是对得上。
- **字体大小**：两块屏都有四种大小可选。
- **屏保**：闲置几分钟后，上屏会轮流显示你的游戏封面和时钟，下屏变黑。每换一张图文字就换个角落，还会慢慢漂移，OLED 屏不会烧屏。

## 设置与分享

- **像主机一样的设置页。** 按 START 打开一整页：左边是分类（外观与声音、主页与游戏库、游玩、AI 与语音、存档、传输与分享、整理工具、系统），右边是当前分类的各项设置，直接显示当前值和开关；上屏显示版本号、每个存储的剩余空间，以及开着哪些 AI 服务。L / R 切换分类。
- **分享你的设置。** 「设置 → 存档、传输与分享 → 导出我的配置」会把所有设置（外观、主页布局和顺序、文件夹、隐藏的内容、模拟器选择、AI 服务）连同你自己的图片打成一个 zip。「导入配置」会从 `DuoLauncher/exports` 或 Download 文件夹里读取配置，文件可以用 Wi-Fi、聊天软件或者存储卡传过来。
  为什么可以放心分享：密钥、密码和游玩记录永远不会写进配置；导入别人的配置时，你自己的这些都会保留；你的密钥也绝不会被指向别人配置里带来的地址。
- **11 种语言**：简体中文、繁體中文、English、日本語、한국어、Français、Español、Deutsch、Italiano、Português、Русский。

## 实用工具

- **Wi-Fi 传输**：「设置 → 存档、传输与分享 → Wi-Fi 传输」会显示一个地址和 PIN 码。在同一网络下用浏览器打开这个地址，输入 PIN，就能把 ROM 或主题包直接拖进任意主机文件夹。同一页面还有 AI、语音和 RetroAchievements 的设置，用管理密码保护（随机六位数字，也可以自己设），长长的密钥可以直接从电脑上粘贴。传输期间掌机屏幕会保持常亮，因为屏幕一关，这个页面就连不上了。
- **存储空间**：主页上有个格子显示卡用了多少，上屏显示每个存储的剩余空间。点进去能看到每个主机的游戏占了多大、从没玩过的大游戏，还可以在确认后删除游戏文件（包括所有碟，以及 `.cue` 或 `.m3u` 里列出的文件）。
- **存档备份**：DraStic、RetroArch、PPSSPP、AetherSX2、DuckStation、Mupen64Plus FZ、Azahar 和 Citra 的存档可以一起备份进一个 zip，也能一起恢复，备份放在 TF 卡的 `DuoLauncher/backups` 里（没有卡就放在内部存储）。玩了三小时还没备份，启动器会提醒你；你也可以让它自动备份（自动备份保留最新的十份，你手动做的备份永远不会被删）。恢复时，每个存档要等备份完整复制好才会替换。「Y → 存档」可以单独备份和恢复一个游戏。
- **整理工具**：找重复的游戏、列出缺封面的游戏、联网补全封面、一键翻译所有金手指，还有一个诊断信息页；AI 相关的工具（整个主机的游戏名和简介、类型、系列、图片、类型图标）放在「AI 与语音」里。
- **更新**：「设置 → 系统 → 关于」每天检查一次 GitHub 上的最新版本，并给出下载链接。
- **桌面**：把它设为默认桌面应用，掌机开机就会启动它；「安卓桌面」格子（或「设置 → 系统 → 退出到安卓桌面」）可以回到系统自带的桌面，「设置 → 系统 → 安卓系统设置」会打开系统自己的设置。

## AI

这里的一切都是可选的，用的是你自己的账号。密钥存在掌机上启动器的设置里，只会发给你选的那个服务。可以在「设置 → AI 与语音」里填，也可以用电脑或手机在 Wi-Fi 页面上填。

- **AI 翻译**：DeepSeek、通义千问（Qwen）、OpenAI、Claude、Gemini，或者任何兼容 OpenAI 的接口地址。和文字有关的活都靠这个密钥：聊天、挑游戏、「问 AI」、「操作说明」、类型和系列、金手指名称（一个游戏一次请求翻完），以及把游戏名和简介翻成你的语言（连同官方英文名一起写进启动器自己的资料里，这样一个全是中文名的游戏库换成英文界面也很自然，反过来也一样）。没有密钥时，金手指用免费服务翻译。
- **AI 生图**：OpenAI 的图片密钥（会自动选这个密钥能用的最新图片模型）。封面、标题 Logo、背景、主机图、文件夹和格子的图标与背景，都和内置素材一样是黏土图标和剪纸风格；图标用透明背景生成，所以图标里白色的部分不会被抠掉。一张图大约半分钟。同一个密钥还负责把你的语音转成文字（`/audio/transcriptions`），因为掌机自己没有语音识别。
- **AI 语音回复**：用 Soniox（要它自己的密钥），或者用 OpenAI（图片密钥）；可以选声音和语速（正常、快、很快，默认是「快」，因为两家念中文都偏慢）。
- **AI 用量与花费**：启动器发出的每一次请求都在掌机上记着：请求次数、token、图片张数和语音分钟数，并估算出今天、本周、本月和累计的花费。估算按各服务公开的价格计算，每项价格都可以改成你自己的（换了模型、用了中转、有折扣）。实际花了多少，以服务商的账单为准。

## 自定义游戏信息

在 TF 卡（或内部存储）根目录放一个 `DuoLauncher` 文件夹，就可以添加或修正游戏名、简介和图片，而不用动其他前端的文件。这里的内容优先于 Pegasus、ES-DE 和游戏自带的数据。

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

`metadata.txt` 用 Pegasus 的格式。条目按 `file:` 匹配；装进模拟器的 3DS 游戏则按 `titleid:` 匹配：

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

游戏名和简介可以按语言分别写，用 `name.<language>:` 和 `description.<language>:`（`en`、`zh-CN`、`zh-TW`、`ja`、`ko`、`fr`、`es`、`de`、`it`、`pt`、`ru`；`zh` 同时适用于简体和繁体）。启动器用哪种语言，就显示哪种语言的内容，所以同一个文件夹可以给说不同语言的玩家一起用：

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## 主题

「设置 → 外观与声音 → 主题」里有七套内置颜色主题（天空蓝、樱桃红、薄荷绿、葡萄紫、橘子橙、樱花粉、石墨灰）、**自定义颜色**（选一个主色和一个背景色，浅色和深色模式下的各种深浅都会自动算出来），以及所有已安装的主题包。主题会给两块屏都上色，包括上屏的线稿背景。

主题包就是 TF 卡或内部存储上 `DuoLauncher/themes/` 里的一个文件夹（也可以是这个文件夹的 `.zip`，第一次使用时自动解压）。里面的东西全都是可选的；主题没提供的部分会用内置的样子，所以一个主题可以小到只有几种颜色。

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

上面各项依次是：`theme.txt` 写名称、作者和颜色（见下文）；`consoles/` 放主机图片；`backgrounds/` 放上屏背景，`backgrounds/dark/` 放深色模式的背景；`logo.png`；字体；`sounds/` 放移动、打开、返回、启动的音效（也可以是 .wav / .mp3）；`music.ogg` 是主页循环播放的音乐（主题带音乐时，设置里会出现「背景音乐」）。

`theme.txt` 每行写成 `key: value`：浅色模式用 `color.<name>`，深色模式用 `dark.color.<name>`，名称有 `accent`、`shelf-top`、`shelf-bottom`、`card`、`text`、`pill`、`footer`、`backdrop-paper` / `backdrop-ink`（上屏的线稿）以及主页文件夹用的 `folder.<type>` 等。只写一行 `base-color: #RRGGBB` 就足够生成一整套配色。[themes/sakura](themes/sakura/theme.txt) 是一个完整的例子，每个 key 都有说明。「设置 → 存档、传输与分享 → 导出当前主题」会把你现在的设置存成 zip，放在 `DuoLauncher/exports/` 里，别人放进自己的 `themes/` 文件夹就能用；你自己的图片会一起放在 `own/` 里。

## 编译

不需要 Android Studio 或 Gradle，只要 Android SDK 命令行工具（build-tools 35、platform android-35）和 JDK 17 或更高版本：

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

第一条命令编译出 DuoLauncher.apk；加上 `-Install` 还会通过 adb 安装、授予文件访问权限并启动。第一次编译时会生成一个调试签名密钥（`debug.keystore`），它不会提交到仓库。`shot.ps1` 会通过 adb 发送按键，并保存两块屏的截图，改界面时很好用。

## 美术素材

`assets/consoles/<type>.png`（黏土风格的主机和格子图标）和 `assets/backgrounds/<type>.jpg`（剪纸线稿背景，深色模式版本在 `backgrounds/dark/` 里）都是为这个项目专门做的。背景是灰度图，所以颜色主题可以给它们上色。没有图片的主机会用一个绘制出来的主机图代替。

## 启动参数说明

- AzaharPlus 只接受它已有访问权限的文件夹下的 tree URI：
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ：`org.citra.emu/org.citra.emu.ui.EmulationActivity`，参数 `-e GamePath <path>`。
- Citra MMJ（性能模式）是同一个版本重新打包成 `com.antutu.ABenchMark`；很多安卓系统会让这个包名以满频 CPU/GPU 运行。只有当这个包里确实是 Citra 时才会出现这个选项。
- Retroid 掌机自带的 DraStic 要用 `-e GAMEPATH "/sf<path>"`；给原版 DraStic 另外提供了「普通路径」的选项。
- RetroArch：`com.retroarch.aarch64/...RetroActivityFuture`，参数 `-e ROM`、`-e LIBRETRO <core>` 和 `-e CONFIGFILE`。启动命令里指定的配置文件会保留原样：在 Duo 上，RetroArch 自己的 `retroarch.cfg` 会让 Retroid 为各平台准备的边框全尺寸显示，而 Retroid 按屏幕分的 `retroarch1920x1080.cfg` 会把游戏画面缩小放在一个电视边框里。
- Dolphin（MMJR 和官方版）：`.../ui.main.MainActivity`，参数 `--es AutoStartFile <path>`。
- ARMSX2、AetherSX2、Eden、PPSSPP 和 Mupen64Plus FZ 拿到的是一个只读的 `content://com.duo.launcher.rom/...` 链接，附带一次性的读取授权，所以它们自己不需要任何文件夹访问权限。
- Vita3K：`org.vita3k.emulator/.Emulator`，带字符串数组参数 `AppStartParameters = ["-r", "<title ID>"]`。
- Pegasus 资料里写好的启动命令会原样使用。

### 在双屏掌机上用 Citra MMJ

Citra MMJ 默认把触摸屏放在上面那块屏上。想让主画面在上屏、并隐藏屏幕上的虚拟按键，先关掉 Citra，再在 `/sdcard/citra-emu/config/config-mmj.ini` 里这样设置：

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## 反馈

Duo Launcher 是照着大家的实际用法慢慢改出来的。如果你觉得少了什么、哪里看不懂、哪里坏了，或者做了一个值得分享的主题，欢迎开个 [issue](https://github.com/GenKnot/DuoLauncher/issues)。

## 许可证

[MIT](LICENSE)
