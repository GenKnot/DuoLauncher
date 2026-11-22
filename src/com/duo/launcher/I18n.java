package com.duo.launcher;

import java.util.Locale;

/** In-app translations; the language is chosen in Settings, independent of the system locale. */
final class I18n {

    enum S {
        LOADING, NO_GAMES, NEED_PERMISSION, NO_SECOND_SCREEN, LAUNCH_FAILED,
        RECENT, GAMES_COUNT, GAMES_SHORT, PLATFORMS_COUNT,
        HOME, OPEN, PLAY, BACK, EMULATOR, CHOOSE_EMULATOR, PLATFORM_EMULATOR,
        SIZE, SORT, SORT_DEFAULT, SORT_NAME, ZOOM_L, ZOOM_M, ZOOM_S, ZOOM_XS,
        FOLLOW_PLATFORM, PEGASUS_DEFAULT, PLATFORM_DEFAULT, CURRENT,
        SETTINGS, LANGUAGE, APPEARANCE, CONSOLE_IMAGE, BACKGROUND_IMAGE, PICK_IMAGE, RESET_DEFAULT,
        CUSTOM, DEFAULT, ABOUT, WELCOME, NOT_SET,
        T_3DS, T_NDS, T_GBA_HACK, T_GBA_VIB, SAVED, SETTINGS_HINT, IMAGE_FAILED,
        GAME_FOLDERS, ADD_FOLDER, REMOVE_FOLDER, AUTO_FIND, RESCAN, ON, OFF, SCAN_DONE,
        SEARCH, SEARCH_HINT, SEARCH_RESULTS, NO_RESULTS, PLAYED_HM, PLAYED_M, LAST_PLAYED,
        DISPLAY_MODE, MODE_SYSTEM, MODE_LIGHT, MODE_DARK, SET_HOME, USAGE_ACCESS, ANDROID_GAMES, APPS, CHOOSE_APPS, DONE, NO_EMULATOR,
        NEW_FOLDER, FOLDER_NAME, RENAME, COLOR, ICON, DELETE_FOLDER, ADD_TO_FOLDER, REMOVE_FROM_FOLDER, EDIT_FOLDER, ADDED_TO, MY_FOLDER,
        HIDE_FOLDER, HIDDEN_FOLDERS,
        HIDE_GAME, HIDDEN_GAMES, BANNER_MUSIC, PERMISSIONS, PERM_FILES, PERM_USAGE, PERM_ALLOWED, PERM_INTRO,
        RECENT_SEARCHES, CLEAR_HISTORY, ALL_PLATFORMS, SEARCH_IN,
        FAVORITES, ADD_FAVORITE, REMOVE_FAVORITE, CONTINUE, SORT_FILTER, SORT_RECENT, SORT_PLAYTIME, ALL, SOUNDS, MUSIC, THEME,
        THEME_BLUE, THEME_RED, THEME_GREEN, THEME_PURPLE, THEME_ORANGE, THEME_PINK, THEME_GRAY, CUSTOM_COLORS, MAIN_COLOR, BASE_COLOR, BACKGROUND_LIGHT, BACKGROUND_DARK,
        FONT_SIZE, SIZE_XL,
        ANDROID_DESKTOP, ANDROID_SETTINGS, DESKTOP_HINT,
        EXIT_TO_DESKTOP, HIDE_TILE, START_SETTINGS_HINT,
        DESKTOP_HOW, GO, CANCEL, DESKTOP_ASK,
        LIST_VIEW, BUTTONS, SWAP_AB, SWAP_XY, STATS, TOTAL_PLAYED, THIS_WEEK, TOP_GAMES, NO_PLAY_YET, SCREENSAVER, MINUTES, VIDEO_PREVIEW, BACKUP_SAVES, RESTORE_SAVES, BACKUP_DONE, RESTORE_DONE, WORKING, WIFI_TRANSFER, WIFI_OPEN, WIFI_STOP, UPLOADED, EXPORT_THEME, EXPORTED, TOOLS, DUPLICATES, MISSING_COVERS, FILL_COVERS, NONE_FOUND, DIAGNOSTICS, COVERS_DONE, TOP_DISPLAY, DISPLAY_AUTO, DISPLAY_LOGO, DISPLAY_COVER, DISPLAY_VIDEO, PERF_MODE, CHEATS, DOWNLOAD_CHEATS, NO_CHEATS, CHEATS_ADDED, CHEATS_NOT_FOUND, NO_TITLE_ID, NO_CHEAT_EMULATOR, TRANSLATE_CHEATS, TRANSLATED, NOT_TRANSLATED, EDIT_CHEATS, NEW_CHEAT, CHEAT_NAME, CHEAT_CODES, BAD_CODES, DELETE_CHEAT, TRANSLATE_ALL_CHEATS, CHEATS_RETROARCH_ONLY, RETROARCH_CHEAT_HINT, CHEATS_TOO_MANY,
        MOVE_TILE, DROP_TILE, HOLD_TO_MOVE,
        AI_TRANSLATE, AI_HINT, AI_SERVICE, API_KEY, AI_MODEL, AI_URL, AI_ON_PHONE, AI_TEST, AI_OFF, AI_OK, AI_FAILED, AI_SAVED, AI_TRANSLATE_CHEATS,
        PICK_CHEATS, PICKED, TRANSLATING, IMPORT_PICKED, SELECT_ALL, SELECT_NONE,
        ADMIN_PASSWORD, DELETE_ALL_CHEATS, DELETE_ALL_SURE, DELETED,
        AI_GAME_TEXT, AI_GAME_TEXT_HINT, AI_NEEDED, AI_GAME_TEXT_PROGRESS,
        RANDOM_GAME, RANDOM_HINT, DETAILS, ORIGINAL_NAME, FILE, PLATFORM, LOCATION, DISCS, SCREENSHOTS,
        UPDATE_AVAILABLE, DOWNLOAD_VERSION, CHECK_UPDATES, UP_TO_DATE,
        AI_IMAGES, AI_IMAGES_HINT, AI_MAKE, AI_COVER, AI_SUBJECT, AI_DRAWING, AI_IMAGES_NEEDED,
        RA_HINT, RA_USER, RA_KEY, RA_RECENT, REMOVE_KEY
    }

    static final String[] CODES = {"zh-CN", "zh-TW", "en", "ja", "ko", "fr", "es", "de", "it", "pt", "ru"};
    static final String[] NAMES = {"简体中文", "繁體中文", "English", "日本語", "한국어", "Français", "Español", "Deutsch", "Italiano", "Português", "Русский"};

    private static final String[][] T = {
        { // zh-CN
            "正在读取游戏…", "没有找到游戏\n在「设置 → 游戏文件夹」里添加放游戏的文件夹", "需要「所有文件访问权限」才能读取游戏\n请在弹出的设置里允许后返回", "没有找到第二块屏幕", "启动失败：%s",
            "最近玩过", "%d 款游戏", "%d 款", "%d 个平台",
            "主页", "打开", "开始游戏", "返回", "模拟器", "选择模拟器", "平台模拟器",
            "大小", "排序", "默认", "名称", "大", "中", "小", "迷你",
            "跟随平台设置", "天马G 默认（%s）", "%s 平台默认", "当前",
            "设置", "语言", "平台外观", "主机图片", "背景图片", "选择图片…", "恢复默认",
            "自定义", "默认", "关于", "欢迎使用 Duo Launcher", "未设置",
            "任天堂 3DS", "任天堂 DS", "GBA 改版", "GBA 震动版", "已保存", "语言 · 平台外观", "无法读取这张图片",
            "游戏文件夹", "添加文件夹…", "移除这个文件夹", "自动查找 Roms 文件夹", "重新扫描", "开", "关", "找到 %d 款游戏",
            "搜索", "输入游戏名称", "搜索：%s", "没有找到匹配的游戏", "已游玩 %d 小时 %d 分", "已游玩 %d 分钟", "上次游玩 %s", "显示模式", "跟随系统", "浅色", "深色", "设为默认桌面（开机启动）", "更精确地统计游玩时间", "安卓游戏", "应用", "选择要显示的应用", "完成", "这个平台还没有设置模拟器",
            "新建文件夹", "文件夹名称", "重命名", "颜色", "图标", "删除文件夹", "加入文件夹…", "从这个文件夹移除", "编辑文件夹", "已加入「%s」", "我的收藏", "隐藏这个文件夹", "已隐藏的文件夹",
            "隐藏这个游戏", "已隐藏的游戏", "3DS 横幅音乐", "权限", "所有文件访问（读取游戏）", "使用情况访问（统计游玩时间）", "已允许", "第一次使用需要允许下面的权限", "最近搜索", "清除搜索记录", "全部平台", "搜索范围",
            "收藏", "加入收藏", "取消收藏", "继续游玩", "排序与筛选", "最近玩过", "游玩时间最长", "全部", "音效", "背景音乐", "主题",
            "天空蓝", "樱桃红", "薄荷绿", "葡萄紫", "橘子橙", "樱花粉", "石墨灰", "自定义颜色…", "主色", "背景色", "背景图片（浅色）", "背景图片（深色）",
            "字体大小", "特大",
            "安卓桌面", "安卓系统设置", "回到系统自带的桌面",
            "退出到安卓桌面", "隐藏这个格子", "隐藏后按 START 也能打开设置",
            "在下一页选择「Quickstep」就能回到安卓桌面", "前往", "取消", "返回安卓桌面？",
            "列表", "按键设置", "A/B 键互换", "X/Y 键互换", "游玩统计", "总游玩时间", "本周", "玩得最多", "还没有游玩记录", "屏保", "%d 分钟", "视频预览", "备份存档", "恢复存档", "已备份到 %s", "已恢复 %d 个文件", "处理中…", "Wi-Fi 传输", "在同一 Wi-Fi 下用手机或电脑浏览器打开", "停止传输", "已收到 %s", "导出当前主题", "已导出到 %s", "整理工具", "重复的游戏", "缺封面的游戏", "联网补全封面", "没有发现", "诊断信息", "补全了 %d 个封面", "上屏显示", "自动（有视频就播放）", "Logo", "封面", "视频", "性能模式", "金手指", "下载金手指", "还没有金手指，可以下载", "已添加 %d 条金手指", "金手指库里没有这个游戏，或者下载失败", "读不到游戏 ID（ROM 可能是加密的）", "没有找到 Azahar 或 Citra 的文件夹", "翻译成中文", "已翻译 %d 条", "%d 条暂时没翻译（没网或今日额度用完）", "编辑金手指", "新建金手指", "金手指名称", "代码（每行一条）", "每行应是两组 8 位十六进制数，比如 12345678 0000FFFF", "删除这条", "翻译所有金手指", "这个模拟器的金手指请在模拟器里设置（这里支持 3DS、DraStic、PPSSPP 和用 RetroArch 启动的游戏）", "第一次用：在 RetroArch 快捷菜单 → 金手指 里打开「载入游戏时自动应用」", "开启的金手指太多了，DraStic 最多能载入 256 行代码，先关掉几条", "移动", "放下", "按住：移动", "AI 翻译", "用 AI 翻译金手指，名称更自然；失败时自动改用免费翻译", "服务", "API 密钥", "模型", "接口地址", "用电脑或手机输入（Wi-Fi 传输页）", "测试连接", "关闭 AI 翻译", "连接成功：%s", "AI 翻译失败：%s", "AI 翻译设置已保存", "用 AI 翻译金手指", "选择要导入的金手指", "已选 %d / %d", "正在翻译…", "导入所选（%d）", "全选", "全不选", "管理密码", "删除全部金手指", "删除这个游戏的全部 %d 条金手指？", "已删除", "AI 翻译游戏名和简介", "找出官方原名，把名称和简介译成当前语言", "请先在「设置 → AI 翻译」里填好 API 密钥", "AI 翻译中 %d / %d", "今天玩什么", "按 A 随机挑一个游戏，再按一次换一个", "游戏详情", "原名", "文件", "平台", "位置", "%d 张碟", "截图", "有新版本 %s", "下载 %s（GitHub）", "检查更新", "已经是最新版本", "AI 生图", "用你自己的 OpenAI 密钥生成主机图、背景和封面，每张约几毛钱", "AI 生成", "AI 生成封面", "画什么？", "正在生成图片，大约半分钟…", "请先在「设置 → AI 生图」里填好密钥", "显示你在 RetroArch 里的成就进度；密钥在 retroachievements.org 的设置页", "用户名", "Web API 密钥", "最近玩过的成就", "删除密钥"
        },
        { // zh-TW
            "正在讀取遊戲…", "找不到遊戲\n請在「設定 → 遊戲資料夾」中新增放遊戲的資料夾", "需要「所有檔案存取權限」才能讀取遊戲\n請在彈出的設定中允許後返回", "找不到第二個螢幕", "啟動失敗：%s",
            "最近玩過", "%d 款遊戲", "%d 款", "%d 個平台",
            "主頁", "開啟", "開始遊戲", "返回", "模擬器", "選擇模擬器", "平台模擬器",
            "大小", "排序", "預設", "名稱", "大", "中", "小", "迷你",
            "跟隨平台設定", "天馬G 預設（%s）", "%s 平台預設", "目前",
            "設定", "語言", "平台外觀", "主機圖片", "背景圖片", "選擇圖片…", "恢復預設",
            "自訂", "預設", "關於", "歡迎使用 Duo Launcher", "未設定",
            "任天堂 3DS", "任天堂 DS", "GBA 改版", "GBA 震動版", "已儲存", "語言 · 平台外觀", "無法讀取這張圖片",
            "遊戲資料夾", "新增資料夾…", "移除這個資料夾", "自動尋找 Roms 資料夾", "重新掃描", "開", "關", "找到 %d 款遊戲",
            "搜尋", "輸入遊戲名稱", "搜尋：%s", "找不到符合的遊戲", "已遊玩 %d 小時 %d 分", "已遊玩 %d 分鐘", "上次遊玩 %s", "顯示模式", "跟隨系統", "淺色", "深色", "設為預設桌面（開機啟動）", "更精確地統計遊玩時間", "Android 遊戲", "應用程式", "選擇要顯示的應用程式", "完成", "這個平台尚未設定模擬器",
            "新增資料夾", "資料夾名稱", "重新命名", "顏色", "圖示", "刪除資料夾", "加入資料夾…", "從這個資料夾移除", "編輯資料夾", "已加入「%s」", "我的收藏", "隱藏這個資料夾", "已隱藏的資料夾",
            "隱藏這個遊戲", "已隱藏的遊戲", "3DS 橫幅音樂", "權限", "所有檔案存取（讀取遊戲）", "使用情況存取（統計遊玩時間）", "已允許", "第一次使用需要允許下列權限", "最近搜尋", "清除搜尋記錄", "全部平台", "搜尋範圍",
            "收藏", "加入收藏", "取消收藏", "繼續遊玩", "排序與篩選", "最近玩過", "遊玩時間最長", "全部", "音效", "背景音樂", "主題",
            "天空藍", "櫻桃紅", "薄荷綠", "葡萄紫", "橘子橙", "櫻花粉", "石墨灰", "自訂顏色…", "主色", "背景色", "背景圖片（淺色）", "背景圖片（深色）",
            "字體大小", "特大",
            "Android 桌面", "Android 系統設定", "回到系統內建的桌面",
            "退出到 Android 桌面", "隱藏這個格子", "隱藏後按 START 也能打開設定",
            "在下一頁選擇「Quickstep」就能回到 Android 桌面", "前往", "取消", "返回 Android 桌面？",
            "列表", "按鍵設定", "A/B 鍵互換", "X/Y 鍵互換", "遊玩統計", "總遊玩時間", "本週", "玩得最多", "還沒有遊玩記錄", "螢幕保護", "%d 分鐘", "影片預覽", "備份存檔", "還原存檔", "已備份到 %s", "已還原 %d 個檔案", "處理中…", "Wi-Fi 傳輸", "在同一 Wi-Fi 下用手機或電腦瀏覽器開啟", "停止傳輸", "已收到 %s", "匯出目前主題", "已匯出到 %s", "整理工具", "重複的遊戲", "缺封面的遊戲", "連網補全封面", "沒有發現", "診斷資訊", "補全了 %d 個封面", "上屏顯示", "自動（有影片就播放）", "Logo", "封面", "影片", "效能模式", "金手指", "下載金手指", "還沒有金手指，可以下載", "已新增 %d 條金手指", "金手指庫裡沒有這個遊戲，或者下載失敗", "讀不到遊戲 ID（ROM 可能是加密的）", "沒有找到 Azahar 或 Citra 的資料夾", "翻譯成中文", "已翻譯 %d 條", "%d 條暫時沒翻譯（沒網路或今日額度用完）", "編輯金手指", "新增金手指", "金手指名稱", "代碼（每行一條）", "每行應是兩組 8 位十六進位數，例如 12345678 0000FFFF", "刪除這條", "翻譯所有金手指", "這個模擬器的金手指請在模擬器裡設定（這裡支援 3DS、DraStic、PPSSPP 和用 RetroArch 啟動的遊戲）", "第一次用：在 RetroArch 快捷選單 → 金手指 裡開啟「載入遊戲時自動套用」", "開啟的金手指太多了，DraStic 最多能載入 256 行代碼，先關掉幾條", "移動", "放下", "按住：移動", "AI 翻譯", "用 AI 翻譯金手指，名稱更自然；失敗時自動改用免費翻譯", "服務", "API 金鑰", "模型", "介面位址", "用電腦或手機輸入（Wi-Fi 傳輸頁）", "測試連線", "關閉 AI 翻譯", "連線成功：%s", "AI 翻譯失敗：%s", "AI 翻譯設定已儲存", "用 AI 翻譯金手指", "選擇要匯入的金手指", "已選 %d / %d", "正在翻譯…", "匯入所選（%d）", "全選", "全不選", "管理密碼", "刪除全部金手指", "刪除這個遊戲的全部 %d 條金手指？", "已刪除", "AI 翻譯遊戲名和簡介", "找出官方原名，把名稱和簡介譯成目前語言", "請先在「設定 → AI 翻譯」裡填好 API 金鑰", "AI 翻譯中 %d / %d", "今天玩什麼", "按 A 隨機挑一個遊戲，再按一次換一個", "遊戲詳情", "原名", "檔案", "平台", "位置", "%d 張碟", "截圖", "有新版本 %s", "下載 %s（GitHub）", "檢查更新", "已經是最新版本", "AI 生圖", "用你自己的 OpenAI 金鑰生成主機圖、背景和封面", "AI 生成", "AI 生成封面", "畫什麼？", "正在生成圖片，大約半分鐘…", "請先在「設定 → AI 生圖」裡填好金鑰", "顯示你在 RetroArch 裡的成就進度；金鑰在 retroachievements.org 的設定頁", "使用者名稱", "Web API 金鑰", "最近玩過的成就", "刪除金鑰"
        },
        { // en
            "Loading games…", "No games found\nAdd the folders with your games in Settings → Game folders", "All-files access is needed to read your games\nAllow it in the settings screen, then come back", "Second screen not found", "Couldn't start: %s",
            "Recently played", "%d games", "%d", "%d platforms",
            "Home", "Open", "Play", "Back", "Emulator", "Choose emulator", "Platform emulator",
            "Size", "Sort", "Default", "Name", "L", "M", "S", "XS",
            "Use platform setting", "Pegasus default (%s)", "%s default", "Current",
            "Settings", "Language", "Platform look", "Console image", "Background image", "Choose image…", "Reset to default",
            "Custom", "Default", "About", "Welcome to Duo Launcher", "Not set",
            "Nintendo 3DS", "Nintendo DS", "GBA Hacks", "GBA Rumble", "Saved", "Language · Platform look", "Couldn't read this image",
            "Game folders", "Add folder…", "Remove this folder", "Find Roms folders automatically", "Rescan", "On", "Off", "Found %d games",
            "Search", "Type a game name", "Search: %s", "No matching games", "Played %dh %dm", "Played %d min", "Last played %s", "Display mode", "Follow system", "Light", "Dark", "Set as home screen (start on boot)", "Track play time more precisely", "Android games", "Apps", "Choose apps to show", "Done", "No emulator set for this platform",
            "New folder", "Folder name", "Rename", "Colour", "Icon", "Delete folder", "Add to folder…", "Remove from this folder", "Edit folder", "Added to %s", "My collection", "Hide this folder", "Hidden folders",
            "Hide this game", "Hidden games", "3DS banner music", "Permissions", "All files access (to read games)", "Usage access (play time)", "Allowed", "First start: please allow these permissions", "Recent searches", "Clear search history", "All platforms", "Search in",
            "Favorites", "Add to favorites", "Remove from favorites", "Continue", "Sort and filter", "Recently played", "Most played", "All", "Sound effects", "Background music", "Theme",
            "Sky blue", "Cherry red", "Mint green", "Grape purple", "Tangerine", "Sakura pink", "Graphite", "Custom colours…", "Main colour", "Background colour", "Background (light)", "Background (dark)",
            "Text size", "Extra large",
            "Android home", "Android settings", "Back to the system home screen",
            "Exit to Android home", "Hide this tile", "START opens Settings even when hidden",
            "Pick \"Quickstep\" on the next page to get the Android home back", "Go", "Cancel", "Back to the Android home?",
            "List", "Buttons", "Swap A/B", "Swap X/Y", "Play statistics", "Total play time", "This week", "Most played", "Nothing played yet", "Screensaver", "%d min", "Video previews", "Back up saves", "Restore saves", "Saved to %s", "Restored %d files", "Working…", "Wi-Fi transfer", "Open this in a browser on the same Wi-Fi", "Stop transfer", "Received %s", "Export current theme", "Exported to %s", "Tidy up", "Duplicate games", "Games without covers", "Fetch missing covers", "Nothing found", "Diagnostics", "Added %d covers", "Top screen", "Auto (video when there is one)", "Logo", "Cover", "Video", "performance mode", "Cheats", "Download cheats", "No cheats yet – download some", "Added %d cheats", "No cheats for this game in the database, or the download failed", "Cannot read the game ID (encrypted ROM?)", "No Azahar or Citra folder found", "Translate", "Translated %d", "%d not translated (offline or daily limit reached)", "Edit cheats", "New cheat", "Cheat name", "Code (one line each)", "Each line needs two 8-digit hex numbers, e.g. 12345678 0000FFFF", "Delete", "Translate all cheats", "Set cheats for this emulator inside it (here: 3DS, DraStic, PPSSPP and games started with RetroArch)", "First time: in RetroArch, Quick Menu → Cheats, turn on \"Auto-Apply Cheats During Game Load\"", "Too many cheats on: DraStic can load at most 256 code lines, switch some off first", "Move", "Drop", "Hold: move", "AI translation", "Cheat names translated by an AI service with your own key; the free services stay as a fallback", "Service", "API key", "Model", "Address", "Enter on a computer or phone (Wi-Fi page)", "Test connection", "Turn AI translation off", "Connected: %s", "AI translation failed: %s", "AI translation settings saved", "Translate cheats with AI", "Choose cheats to import", "%d of %d chosen", "translating…", "Import chosen (%d)", "Select all", "Select none", "Admin password", "Delete all cheats", "Delete all %d cheats of this game?", "Deleted", "AI: game names and descriptions", "Finds each official title and puts names and descriptions into this language", "Set an API key in Settings → AI translation first", "AI translating %d / %d", "What to play?", "Press A for a random game; again for another", "Details", "Original title", "File", "Platform", "Location", "%d discs", "Screenshots", "Version %s available", "Download %s (GitHub)", "Check for updates", "You have the latest version", "AI pictures", "Console pictures, backdrops and covers made with your own OpenAI key (a few cents each)", "Make with AI", "Make a cover with AI", "What should it show?", "Drawing the picture, about half a minute…", "Set a key in Settings → AI pictures first", "Shows your achievement progress from RetroArch; the key is on retroachievements.org → Settings", "User name", "Web API key", "Recently played achievements", "Remove key"
        },
        { // ja
            "ゲームを読み込み中…", "ゲームが見つかりません\n「設定 → ゲームフォルダー」でゲームのフォルダーを追加してください", "ゲームを読み込むには「すべてのファイルへのアクセス」が必要です\n設定画面で許可してから戻ってください", "2つ目の画面が見つかりません", "起動できませんでした：%s",
            "最近遊んだゲーム", "%d 本のゲーム", "%d 本", "%d 機種",
            "ホーム", "開く", "プレイ", "戻る", "エミュレーター", "エミュレーターを選択", "機種のエミュレーター",
            "サイズ", "並び替え", "標準", "名前", "大", "中", "小", "極小",
            "機種の設定に従う", "Pegasus 標準（%s）", "%s の標準", "現在",
            "設定", "言語", "機種の外観", "本体画像", "背景画像", "画像を選択…", "標準に戻す",
            "カスタム", "標準", "情報", "Duo Launcher へようこそ", "未設定",
            "ニンテンドー3DS", "ニンテンドーDS", "GBA 改造版", "GBA 振動版", "保存しました", "言語 · 機種の外観", "この画像を読み込めません",
            "ゲームフォルダー", "フォルダーを追加…", "このフォルダーを外す", "Roms フォルダーを自動で探す", "再スキャン", "オン", "オフ", "%d 本のゲームが見つかりました",
            "検索", "ゲーム名を入力", "検索：%s", "一致するゲームがありません", "プレイ時間 %d時間%d分", "プレイ時間 %d分", "前回のプレイ %s", "表示モード", "システムに合わせる", "ライト", "ダーク", "ホームアプリに設定（起動時に開く）", "プレイ時間をより正確に記録", "Android ゲーム", "アプリ", "表示するアプリを選択", "完了", "この機種のエミュレーターが未設定です",
            "新しいフォルダー", "フォルダー名", "名前を変更", "色", "アイコン", "フォルダーを削除", "フォルダーに追加…", "このフォルダーから外す", "フォルダーを編集", "「%s」に追加しました", "マイコレクション", "このフォルダーを隠す", "非表示のフォルダー",
            "このゲームを隠す", "非表示のゲーム", "3DS バナーの音楽", "権限", "すべてのファイルへのアクセス（ゲームの読み込み）", "使用状況へのアクセス（プレイ時間）", "許可済み", "初回起動：次の権限を許可してください", "最近の検索", "検索履歴を消去", "すべてのプラットフォーム", "検索範囲",
            "お気に入り", "お気に入りに追加", "お気に入りから外す", "続きから遊ぶ", "並び替えと絞り込み", "最近遊んだ順", "プレイ時間順", "すべて", "効果音", "BGM", "テーマ",
            "スカイブルー", "チェリーレッド", "ミントグリーン", "グレープパープル", "オレンジ", "さくらピンク", "グラファイト", "カスタムカラー…", "メインカラー", "背景色", "背景画像（ライト）", "背景画像（ダーク）",
            "文字サイズ", "特大",
            "Android ホーム", "Android の設定", "システムのホーム画面に戻る",
            "Android ホームに戻る", "このタイルを隠す", "隠しても START で設定を開けます",
            "次の画面で「Quickstep」を選ぶと Android のホームに戻れます", "進む", "キャンセル", "Android のホームに戻りますか？",
            "リスト", "ボタン設定", "A/B を入れ替え", "X/Y を入れ替え", "プレイ統計", "総プレイ時間", "今週", "よく遊んだゲーム", "まだプレイ記録がありません", "スクリーンセーバー", "%d 分", "動画プレビュー", "セーブをバックアップ", "セーブを復元", "%s に保存しました", "%d 個のファイルを復元しました", "処理中…", "Wi-Fi 転送", "同じ Wi-Fi のスマホやパソコンのブラウザで開く", "転送を停止", "%s を受信しました", "今のテーマを書き出す", "%s に書き出しました", "整理ツール", "重複したゲーム", "カバーのないゲーム", "カバーをダウンロード", "見つかりませんでした", "診断情報", "%d 枚のカバーを追加しました", "上画面の表示", "自動（動画があれば再生）", "ロゴ", "パッケージ", "動画", "パフォーマンスモード", "チート", "チートをダウンロード", "チートはまだありません", "%d 件のチートを追加しました", "データベースにこのゲームがないか、ダウンロードに失敗しました", "ゲーム ID を読み取れません（暗号化された ROM？）", "Azahar / Citra のフォルダが見つかりません", "日本語に翻訳", "%d 件を翻訳しました", "%d 件は翻訳できませんでした（オフラインまたは本日の上限）", "チートを編集", "新しいチート", "チート名", "コード（1 行に 1 つ）", "各行は 8 桁の 16 進数 2 つ（例: 12345678 0000FFFF）", "削除", "すべてのチートを翻訳", "このエミュレーターのチートはエミュレーター側で設定してください（ここでは 3DS と RetroArch のゲームに対応）", "初回: RetroArch のクイックメニュー → チートで「ゲーム読み込み時に自動適用」をオン", "オンのチートが多すぎます。DraStic は最大 256 行まで読み込めます。いくつかオフにしてください", "移動", "置く", "長押し：移動", "AI 翻訳", "自分のキーで AI がチートを翻訳。失敗時は無料翻訳を使用", "サービス", "API キー", "モデル", "アドレス", "PC やスマホで入力（Wi-Fi ページ）", "接続テスト", "AI 翻訳をオフ", "接続成功：%s", "AI 翻訳に失敗：%s", "AI 翻訳の設定を保存しました", "AI でチートを翻訳", "取り込むチートを選択", "%d / %d 選択", "翻訳中…", "選択したものを取り込む（%d）", "すべて選択", "すべて解除", "管理パスワード", "チートをすべて削除", "このゲームのチート %d 件をすべて削除しますか？", "削除しました", "AI：ゲーム名と説明", "正式名を調べ、名前と説明をこの言語に", "先に「設定 → AI 翻訳」で API キーを設定してください", "AI 翻訳中 %d / %d", "今日は何で遊ぶ？", "A でランダムにゲームを選びます", "詳細", "原題", "ファイル", "機種", "場所", "ディスク %d 枚", "スクリーンショット", "新バージョン %s", "%s をダウンロード（GitHub）", "更新を確認", "最新バージョンです", "AI 画像", "自分の OpenAI キーで本体画像・背景・カバーを作成", "AI で作成", "AI でカバーを作成", "何を描きますか？", "画像を作成中（約30秒）…", "先に「設定 → AI 画像」でキーを設定してください", "RetroArch での実績の進み具合を表示（キーは retroachievements.org の設定）", "ユーザー名", "Web API キー", "最近遊んだゲームの実績", "キーを削除"
        },
        { // ko
            "게임 불러오는 중…", "게임을 찾을 수 없습니다\n설정 → 게임 폴더에서 게임이 있는 폴더를 추가하세요", "게임을 읽으려면 '모든 파일 접근' 권한이 필요합니다\n설정 화면에서 허용한 뒤 돌아오세요", "두 번째 화면을 찾을 수 없습니다", "실행 실패: %s",
            "최근 플레이", "게임 %d개", "%d개", "플랫폼 %d개",
            "홈", "열기", "플레이", "뒤로", "에뮬레이터", "에뮬레이터 선택", "플랫폼 에뮬레이터",
            "크기", "정렬", "기본", "이름", "대", "중", "소", "최소",
            "플랫폼 설정 따르기", "Pegasus 기본 (%s)", "%s 기본값", "현재",
            "설정", "언어", "플랫폼 외관", "본체 이미지", "배경 이미지", "이미지 선택…", "기본값으로 복원",
            "사용자 지정", "기본", "정보", "Duo Launcher에 오신 것을 환영합니다", "설정 안 됨",
            "닌텐도 3DS", "닌텐도 DS", "GBA 개조판", "GBA 진동판", "저장됨", "언어 · 플랫폼 외관", "이 이미지를 읽을 수 없습니다",
            "게임 폴더", "폴더 추가…", "이 폴더 제거", "Roms 폴더 자동 찾기", "다시 검색", "켜짐", "꺼짐", "게임 %d개를 찾았습니다",
            "검색", "게임 이름 입력", "검색: %s", "일치하는 게임이 없습니다", "플레이 %d시간 %d분", "플레이 %d분", "마지막 플레이 %s", "화면 모드", "시스템 설정 따르기", "라이트", "다크", "기본 홈 화면으로 설정(부팅 시 실행)", "플레이 시간 더 정확하게 기록", "Android 게임", "앱", "표시할 앱 선택", "완료", "이 플랫폼에 설정된 에뮬레이터가 없습니다",
            "새 폴더", "폴더 이름", "이름 바꾸기", "색상", "아이콘", "폴더 삭제", "폴더에 추가…", "이 폴더에서 제거", "폴더 편집", "%s에 추가됨", "내 컬렉션", "이 폴더 숨기기", "숨긴 폴더",
            "이 게임 숨기기", "숨긴 게임", "3DS 배너 음악", "권한", "모든 파일 접근(게임 읽기)", "사용 정보 접근(플레이 시간)", "허용됨", "처음 시작: 다음 권한을 허용해 주세요", "최근 검색", "검색 기록 지우기", "모든 플랫폼", "검색 범위",
            "즐겨찾기", "즐겨찾기에 추가", "즐겨찾기에서 제거", "이어서 하기", "정렬 및 필터", "최근 플레이", "플레이 시간순", "전체", "효과음", "배경 음악", "테마",
            "스카이 블루", "체리 레드", "민트 그린", "그레이프 퍼플", "탠저린", "벚꽃 핑크", "그래파이트", "사용자 색상…", "주 색상", "배경 색상", "배경 이미지 (라이트)", "배경 이미지 (다크)",
            "글자 크기", "아주 크게",
            "안드로이드 홈", "안드로이드 설정", "시스템 홈 화면으로 돌아가기",
            "안드로이드 홈으로 나가기", "이 타일 숨기기", "숨겨도 START로 설정을 열 수 있어요",
            "다음 화면에서 \"Quickstep\"을 고르면 안드로이드 홈으로 돌아가요", "이동", "취소", "안드로이드 홈으로 돌아갈까요?",
            "목록", "버튼 설정", "A/B 바꾸기", "X/Y 바꾸기", "플레이 통계", "총 플레이 시간", "이번 주", "가장 많이 한 게임", "아직 플레이 기록이 없어요", "화면 보호기", "%d분", "영상 미리보기", "세이브 백업", "세이브 복원", "%s에 저장했어요", "파일 %d개를 복원했어요", "처리 중…", "Wi-Fi 전송", "같은 Wi-Fi의 휴대폰이나 PC 브라우저에서 열기", "전송 중지", "%s 받음", "현재 테마 내보내기", "%s에 내보냈어요", "정리 도구", "중복된 게임", "표지가 없는 게임", "표지 내려받기", "찾지 못했어요", "진단 정보", "표지 %d개를 추가했어요", "위 화면 표시", "자동 (영상이 있으면 재생)", "로고", "표지", "영상", "성능 모드", "치트", "치트 다운로드", "아직 치트가 없습니다", "치트 %d개를 추가했습니다", "데이터베이스에 이 게임이 없거나 다운로드에 실패했습니다", "게임 ID를 읽을 수 없습니다 (암호화된 ROM?)", "Azahar 또는 Citra 폴더를 찾을 수 없습니다", "한국어로 번역", "%d개 번역함", "%d개는 번역하지 못했습니다 (오프라인 또는 오늘 한도 초과)", "치트 편집", "새 치트", "치트 이름", "코드 (한 줄에 하나)", "각 줄은 8자리 16진수 두 개여야 합니다 (예: 12345678 0000FFFF)", "삭제", "모든 치트 번역", "이 에뮬레이터의 치트는 에뮬레이터에서 설정하세요 (여기서는 3DS와 RetroArch 게임 지원)", "처음: RetroArch 빠른 메뉴 → 치트에서 \"게임 로드 시 자동 적용\"을 켜세요", "켜진 치트가 너무 많습니다. DraStic은 최대 256줄까지 불러올 수 있습니다. 몇 개를 끄세요", "이동", "놓기", "길게: 이동", "AI 번역", "내 키로 AI가 치트 이름을 번역, 실패하면 무료 번역 사용", "서비스", "API 키", "모델", "주소", "컴퓨터나 휴대폰으로 입력 (Wi-Fi 페이지)", "연결 테스트", "AI 번역 끄기", "연결됨: %s", "AI 번역 실패: %s", "AI 번역 설정 저장됨", "AI로 치트 번역", "가져올 치트 선택", "%d / %d 선택됨", "번역 중…", "선택 항목 가져오기 (%d)", "모두 선택", "모두 해제", "관리 비밀번호", "치트 모두 삭제", "이 게임의 치트 %d개를 모두 삭제할까요?", "삭제됨", "AI: 게임 이름과 설명", "공식 이름을 찾아 이름과 설명을 이 언어로", "먼저 설정 → AI 번역에서 API 키를 넣으세요", "AI 번역 중 %d / %d", "오늘 뭐 하지?", "A를 누르면 게임을 무작위로 고릅니다", "상세 정보", "원제", "파일", "플랫폼", "위치", "디스크 %d장", "스크린샷", "새 버전 %s", "%s 다운로드 (GitHub)", "업데이트 확인", "최신 버전입니다", "AI 이미지", "내 OpenAI 키로 기기 그림, 배경, 표지를 만듭니다", "AI로 만들기", "AI로 표지 만들기", "무엇을 그릴까요?", "그림을 만드는 중 (약 30초)…", "먼저 설정 → AI 이미지에서 키를 넣으세요", "RetroArch 업적 진행도 표시 (키는 retroachievements.org 설정)", "사용자 이름", "Web API 키", "최근 플레이한 업적", "키 삭제"
        },
        { // fr
            "Chargement des jeux…", "Aucun jeu trouvé\nAjoutez vos dossiers de jeux dans Paramètres → Dossiers de jeux", "L'accès à tous les fichiers est nécessaire pour lire vos jeux\nAutorisez-le dans les paramètres puis revenez", "Second écran introuvable", "Échec du lancement : %s",
            "Joués récemment", "%d jeux", "%d", "%d plateformes",
            "Accueil", "Ouvrir", "Jouer", "Retour", "Émulateur", "Choisir l'émulateur", "Émulateur de la plateforme",
            "Taille", "Tri", "Par défaut", "Nom", "G", "M", "P", "TP",
            "Suivre la plateforme", "Pegasus par défaut (%s)", "%s par défaut", "Actuel",
            "Paramètres", "Langue", "Apparence des plateformes", "Image de la console", "Image de fond", "Choisir une image…", "Rétablir par défaut",
            "Personnalisé", "Par défaut", "À propos", "Bienvenue dans Duo Launcher", "Non défini",
            "Nintendo 3DS", "Nintendo DS", "GBA modifiés", "GBA vibration", "Enregistré", "Langue · Apparence", "Impossible de lire cette image",
            "Dossiers de jeux", "Ajouter un dossier…", "Retirer ce dossier", "Trouver les dossiers Roms automatiquement", "Analyser à nouveau", "Activé", "Désactivé", "%d jeux trouvés",
            "Rechercher", "Saisissez un nom de jeu", "Recherche : %s", "Aucun jeu correspondant", "Joué %d h %d min", "Joué %d min", "Dernière partie %s", "Mode d'affichage", "Suivre le système", "Clair", "Sombre", "Définir comme écran d'accueil (au démarrage)", "Mesurer le temps de jeu plus précisément", "Jeux Android", "Applications", "Choisir les applications à afficher", "Terminé", "Aucun émulateur défini pour cette plateforme",
            "Nouveau dossier", "Nom du dossier", "Renommer", "Couleur", "Icône", "Supprimer le dossier", "Ajouter à un dossier…", "Retirer de ce dossier", "Modifier le dossier", "Ajouté à %s", "Ma collection", "Masquer ce dossier", "Dossiers masqués",
            "Masquer ce jeu", "Jeux masqués", "Musique des bannières 3DS", "Autorisations", "Accès à tous les fichiers (lire les jeux)", "Accès aux données d'utilisation (temps de jeu)", "Autorisé", "Premier lancement : autorisez ces permissions", "Recherches récentes", "Effacer l'historique", "Toutes les plateformes", "Rechercher dans",
            "Favoris", "Ajouter aux favoris", "Retirer des favoris", "Continuer", "Trier et filtrer", "Joués récemment", "Les plus joués", "Tous", "Effets sonores", "Musique de fond", "Thème",
            "Bleu ciel", "Rouge cerise", "Vert menthe", "Violet raisin", "Mandarine", "Rose sakura", "Graphite", "Couleurs perso…", "Couleur principale", "Couleur de fond", "Fond (clair)", "Fond (sombre)",
            "Taille du texte", "Très grand",
            "Accueil Android", "Paramètres Android", "Revenir à l'écran d'accueil du système",
            "Quitter vers l'accueil Android", "Masquer cette tuile", "START ouvre les paramètres même masqué",
            "Choisissez « Quickstep » à l'écran suivant pour retrouver l'accueil Android", "Continuer", "Annuler", "Revenir à l'accueil Android ?",
            "Liste", "Boutons", "Inverser A/B", "Inverser X/Y", "Statistiques", "Temps de jeu total", "Cette semaine", "Les plus joués", "Aucune partie pour le moment", "Économiseur d'écran", "%d min", "Aperçus vidéo", "Sauvegarder les sauvegardes", "Restaurer les sauvegardes", "Enregistré dans %s", "%d fichiers restaurés", "En cours…", "Transfert Wi-Fi", "Ouvrez ceci dans un navigateur sur le même Wi-Fi", "Arrêter le transfert", "%s reçu", "Exporter le thème actuel", "Exporté vers %s", "Ranger", "Jeux en double", "Jeux sans jaquette", "Télécharger les jaquettes", "Rien trouvé", "Diagnostic", "%d jaquettes ajoutées", "Écran du haut", "Auto (vidéo si disponible)", "Logo", "Jaquette", "Vidéo", "mode performance", "Codes de triche", "Télécharger des codes", "Aucun code pour l’instant", "%d codes ajoutés", "Aucun code pour ce jeu dans la base, ou échec du téléchargement", "Impossible de lire l’ID du jeu (ROM chiffrée ?)", "Aucun dossier Azahar ou Citra trouvé", "Traduire", "%d traduits", "%d non traduits (hors ligne ou limite du jour atteinte)", "Modifier les codes", "Nouveau code", "Nom du code", "Code (une ligne chacun)", "Chaque ligne doit contenir deux nombres hexadécimaux de 8 chiffres, ex. 12345678 0000FFFF", "Supprimer", "Traduire tous les codes", "Réglez les codes dans cet émulateur (ici : 3DS et jeux lancés avec RetroArch)", "La première fois : dans RetroArch, Menu rapide → Codes de triche, activez l’application automatique au chargement", "Trop de codes activés : DraStic charge au plus 256 lignes, désactivez-en quelques-uns", "Déplacer", "Poser", "Maintenir : déplacer", "Traduction IA", "Noms de codes traduits par une IA avec votre clé ; les services gratuits restent en secours", "Service", "Clé API", "Modèle", "Adresse", "Saisir sur un ordinateur ou un téléphone (page Wi-Fi)", "Tester la connexion", "Désactiver la traduction IA", "Connecté : %s", "Échec de la traduction IA : %s", "Réglages de traduction IA enregistrés", "Traduire les codes avec l'IA", "Choisir les codes à importer", "%d sur %d choisis", "traduction…", "Importer la sélection (%d)", "Tout sélectionner", "Tout désélectionner", "Mot de passe admin", "Supprimer tous les codes", "Supprimer les %d codes de ce jeu ?", "Supprimé", "IA : noms et descriptions", "Trouve le titre officiel et traduit noms et descriptions", "Indiquez d'abord une clé API dans Réglages → Traduction IA", "Traduction IA %d / %d", "À quoi jouer ?", "A : un jeu au hasard, encore pour un autre", "Détails", "Titre original", "Fichier", "Plateforme", "Emplacement", "%d disques", "Captures", "Version %s disponible", "Télécharger %s (GitHub)", "Rechercher des mises à jour", "Vous avez la dernière version", "Images IA", "Images de console, fonds et jaquettes avec votre clé OpenAI", "Créer avec l'IA", "Créer une jaquette avec l'IA", "Que représenter ?", "Création de l'image, environ 30 s…", "Indiquez d'abord une clé dans Réglages → Images IA", "Progression des succès dans RetroArch ; clé sur retroachievements.org → Réglages", "Nom d'utilisateur", "Clé Web API", "Succès des jeux récents", "Supprimer la clé"
        },
        { // es
            "Cargando juegos…", "No se encontraron juegos\nAñade las carpetas con tus juegos en Ajustes → Carpetas de juegos", "Se necesita acceso a todos los archivos para leer tus juegos\nPermítelo en los ajustes y vuelve", "No se encontró la segunda pantalla", "No se pudo iniciar: %s",
            "Jugados recientemente", "%d juegos", "%d", "%d plataformas",
            "Inicio", "Abrir", "Jugar", "Atrás", "Emulador", "Elegir emulador", "Emulador de la plataforma",
            "Tamaño", "Orden", "Predeterminado", "Nombre", "G", "M", "P", "MP",
            "Usar ajuste de la plataforma", "Predeterminado de Pegasus (%s)", "Predeterminado de %s", "Actual",
            "Ajustes", "Idioma", "Aspecto de plataformas", "Imagen de la consola", "Imagen de fondo", "Elegir imagen…", "Restablecer",
            "Personalizado", "Predeterminado", "Acerca de", "Bienvenido a Duo Launcher", "Sin definir",
            "Nintendo 3DS", "Nintendo DS", "GBA modificados", "GBA vibración", "Guardado", "Idioma · Aspecto", "No se pudo leer esta imagen",
            "Carpetas de juegos", "Añadir carpeta…", "Quitar esta carpeta", "Buscar carpetas Roms automáticamente", "Volver a buscar", "Sí", "No", "%d juegos encontrados",
            "Buscar", "Escribe el nombre de un juego", "Búsqueda: %s", "No hay juegos que coincidan", "Jugado %d h %d min", "Jugado %d min", "Última partida %s", "Modo de pantalla", "Seguir el sistema", "Claro", "Oscuro", "Usar como pantalla de inicio (al arrancar)", "Medir el tiempo de juego con más precisión", "Juegos Android", "Aplicaciones", "Elegir aplicaciones visibles", "Listo", "No hay emulador configurado para esta plataforma",
            "Nueva carpeta", "Nombre de la carpeta", "Cambiar nombre", "Color", "Icono", "Eliminar carpeta", "Añadir a carpeta…", "Quitar de esta carpeta", "Editar carpeta", "Añadido a %s", "Mi colección", "Ocultar esta carpeta", "Carpetas ocultas",
            "Ocultar este juego", "Juegos ocultos", "Música de los banners de 3DS", "Permisos", "Acceso a todos los archivos (leer juegos)", "Acceso a datos de uso (tiempo de juego)", "Permitido", "Primer inicio: permite estos permisos", "Búsquedas recientes", "Borrar historial", "Todas las plataformas", "Buscar en",
            "Favoritos", "Añadir a favoritos", "Quitar de favoritos", "Continuar", "Ordenar y filtrar", "Jugados recientemente", "Más jugados", "Todos", "Efectos de sonido", "Música de fondo", "Tema",
            "Azul cielo", "Rojo cereza", "Verde menta", "Morado uva", "Mandarina", "Rosa sakura", "Grafito", "Colores propios…", "Color principal", "Color de fondo", "Fondo (claro)", "Fondo (oscuro)",
            "Tamaño del texto", "Muy grande",
            "Inicio de Android", "Ajustes de Android", "Volver a la pantalla de inicio del sistema",
            "Salir al inicio de Android", "Ocultar esta casilla", "START abre los ajustes aunque esté oculta",
            "Elige «Quickstep» en la siguiente pantalla para volver al inicio de Android", "Ir", "Cancelar", "¿Volver al inicio de Android?",
            "Lista", "Botones", "Intercambiar A/B", "Intercambiar X/Y", "Estadísticas", "Tiempo total de juego", "Esta semana", "Más jugados", "Aún no hay partidas", "Salvapantallas", "%d min", "Vista previa en vídeo", "Copiar partidas guardadas", "Restaurar partidas guardadas", "Guardado en %s", "%d archivos restaurados", "Procesando…", "Transferencia Wi-Fi", "Ábrelo en un navegador en la misma Wi-Fi", "Detener transferencia", "%s recibido", "Exportar tema actual", "Exportado a %s", "Ordenar", "Juegos duplicados", "Juegos sin portada", "Descargar portadas", "No se encontró nada", "Diagnóstico", "%d portadas añadidas", "Pantalla superior", "Automático (vídeo si hay)", "Logo", "Portada", "Vídeo", "modo rendimiento", "Trucos", "Descargar trucos", "Aún no hay trucos", "%d trucos añadidos", "No hay trucos para este juego o falló la descarga", "No se puede leer el ID del juego (¿ROM cifrada?)", "No se encontró la carpeta de Azahar o Citra", "Traducir", "%d traducidos", "%d sin traducir (sin conexión o límite diario alcanzado)", "Editar trucos", "Nuevo truco", "Nombre del truco", "Código (una línea cada uno)", "Cada línea necesita dos números hexadecimales de 8 dígitos, p. ej. 12345678 0000FFFF", "Eliminar", "Traducir todos los trucos", "Configura los trucos en el propio emulador (aquí: 3DS y juegos con RetroArch)", "La primera vez: en RetroArch, Menú rápido → Trucos, activa aplicar al cargar el juego", "Demasiados trucos activos: DraStic carga como máximo 256 líneas, desactiva algunos", "Mover", "Soltar", "Mantener: mover", "Traducción con IA", "Nombres de trucos traducidos por una IA con tu clave; los servicios gratuitos quedan de reserva", "Servicio", "Clave API", "Modelo", "Dirección", "Introducir en un ordenador o móvil (página Wi-Fi)", "Probar conexión", "Desactivar traducción con IA", "Conectado: %s", "Falló la traducción con IA: %s", "Ajustes de traducción con IA guardados", "Traducir trucos con IA", "Elige los trucos a importar", "%d de %d elegidos", "traduciendo…", "Importar elegidos (%d)", "Seleccionar todo", "Quitar todo", "Contraseña de administración", "Borrar todos los trucos", "¿Borrar los %d trucos de este juego?", "Borrado", "IA: nombres y descripciones", "Busca el título oficial y traduce nombres y descripciones", "Primero pon una clave API en Ajustes → Traducción con IA", "Traduciendo con IA %d / %d", "¿A qué juego?", "A: un juego al azar; otra vez para otro", "Detalles", "Título original", "Archivo", "Plataforma", "Ubicación", "%d discos", "Capturas", "Versión %s disponible", "Descargar %s (GitHub)", "Buscar actualizaciones", "Tienes la última versión", "Imágenes con IA", "Consolas, fondos y portadas con tu propia clave de OpenAI", "Crear con IA", "Crear portada con IA", "¿Qué debe mostrar?", "Creando la imagen, unos 30 s…", "Primero pon una clave en Ajustes → Imágenes con IA", "Progreso de logros en RetroArch; la clave está en retroachievements.org → Ajustes", "Usuario", "Clave Web API", "Logros de juegos recientes", "Quitar la clave"
        },
        { // de
            "Spiele werden geladen…", "Keine Spiele gefunden\nFüge deine Spielordner unter Einstellungen → Spielordner hinzu", "Zum Lesen der Spiele wird Zugriff auf alle Dateien benötigt\nBitte in den Einstellungen erlauben und zurückkehren", "Zweiter Bildschirm nicht gefunden", "Start fehlgeschlagen: %s",
            "Zuletzt gespielt", "%d Spiele", "%d", "%d Plattformen",
            "Start", "Öffnen", "Spielen", "Zurück", "Emulator", "Emulator wählen", "Plattform-Emulator",
            "Größe", "Sortierung", "Standard", "Name", "L", "M", "S", "XS",
            "Plattform-Einstellung", "Pegasus-Standard (%s)", "Standard für %s", "Aktuell",
            "Einstellungen", "Sprache", "Plattform-Aussehen", "Konsolenbild", "Hintergrundbild", "Bild wählen…", "Zurücksetzen",
            "Eigenes", "Standard", "Info", "Willkommen bei Duo Launcher", "Nicht festgelegt",
            "Nintendo 3DS", "Nintendo DS", "GBA-Hacks", "GBA mit Rumble", "Gespeichert", "Sprache · Aussehen", "Bild konnte nicht gelesen werden",
            "Spielordner", "Ordner hinzufügen…", "Diesen Ordner entfernen", "Roms-Ordner automatisch finden", "Neu scannen", "An", "Aus", "%d Spiele gefunden",
            "Suchen", "Spielnamen eingeben", "Suche: %s", "Keine passenden Spiele", "Gespielt: %d Std. %d Min.", "Gespielt: %d Min.", "Zuletzt gespielt %s", "Darstellung", "Wie System", "Hell", "Dunkel", "Als Startbildschirm festlegen (beim Booten)", "Spielzeit genauer erfassen", "Android-Spiele", "Apps", "Angezeigte Apps wählen", "Fertig", "Für diese Plattform ist kein Emulator festgelegt",
            "Neuer Ordner", "Ordnername", "Umbenennen", "Farbe", "Symbol", "Ordner löschen", "Zu Ordner hinzufügen…", "Aus diesem Ordner entfernen", "Ordner bearbeiten", "Zu %s hinzugefügt", "Meine Sammlung", "Diesen Ordner ausblenden", "Ausgeblendete Ordner",
            "Dieses Spiel ausblenden", "Ausgeblendete Spiele", "3DS-Bannermusik", "Berechtigungen", "Zugriff auf alle Dateien (Spiele lesen)", "Nutzungsdatenzugriff (Spielzeit)", "Erlaubt", "Erster Start: bitte diese Berechtigungen erlauben", "Letzte Suchen", "Suchverlauf löschen", "Alle Plattformen", "Suchen in",
            "Favoriten", "Zu Favoriten", "Aus Favoriten entfernen", "Weiterspielen", "Sortieren und filtern", "Zuletzt gespielt", "Meistgespielt", "Alle", "Soundeffekte", "Hintergrundmusik", "Design",
            "Himmelblau", "Kirschrot", "Mintgrün", "Traubenlila", "Mandarine", "Sakura-Rosa", "Graphit", "Eigene Farben…", "Hauptfarbe", "Hintergrundfarbe", "Hintergrund (hell)", "Hintergrund (dunkel)",
            "Schriftgröße", "Sehr groß",
            "Android-Startbildschirm", "Android-Einstellungen", "Zurück zum System-Startbildschirm",
            "Zum Android-Startbildschirm", "Kachel ausblenden", "START öffnet die Einstellungen auch ausgeblendet",
            "Auf der nächsten Seite „Quickstep“ wählen, um zum Android-Startbildschirm zurückzukehren", "Weiter", "Abbrechen", "Zurück zum Android-Startbildschirm?",
            "Liste", "Tasten", "A/B tauschen", "X/Y tauschen", "Spielstatistik", "Gesamtspielzeit", "Diese Woche", "Meistgespielt", "Noch nichts gespielt", "Bildschirmschoner", "%d Min.", "Videovorschau", "Spielstände sichern", "Spielstände wiederherstellen", "Gespeichert unter %s", "%d Dateien wiederhergestellt", "Wird bearbeitet…", "WLAN-Übertragung", "Im Browser im selben WLAN öffnen", "Übertragung beenden", "%s empfangen", "Aktuelles Design exportieren", "Exportiert nach %s", "Aufräumen", "Doppelte Spiele", "Spiele ohne Cover", "Cover herunterladen", "Nichts gefunden", "Diagnose", "%d Cover hinzugefügt", "Oberer Bildschirm", "Automatisch (Video, falls vorhanden)", "Logo", "Cover", "Video", "Leistungsmodus", "Cheats", "Cheats herunterladen", "Noch keine Cheats", "%d Cheats hinzugefügt", "Keine Cheats für dieses Spiel in der Datenbank, oder der Download ist fehlgeschlagen", "Spiel-ID nicht lesbar (verschlüsseltes ROM?)", "Kein Azahar- oder Citra-Ordner gefunden", "Übersetzen", "%d übersetzt", "%d nicht übersetzt (offline oder Tageslimit erreicht)", "Cheats bearbeiten", "Neuer Cheat", "Cheat-Name", "Code (eine Zeile pro Eintrag)", "Jede Zeile braucht zwei 8-stellige Hex-Zahlen, z. B. 12345678 0000FFFF", "Löschen", "Alle Cheats übersetzen", "Cheats für diesen Emulator dort einstellen (hier: 3DS und Spiele über RetroArch)", "Beim ersten Mal: in RetroArch Schnellmenü → Cheats „Beim Laden automatisch anwenden“ einschalten", "Zu viele Cheats aktiv: DraStic lädt höchstens 256 Codezeilen, schalte einige aus", "Verschieben", "Ablegen", "Halten: verschieben", "KI-Übersetzung", "Cheat-Namen per KI mit eigenem Schlüssel übersetzt; die kostenlosen Dienste bleiben als Ersatz", "Dienst", "API-Schlüssel", "Modell", "Adresse", "Am Computer oder Handy eingeben (WLAN-Seite)", "Verbindung testen", "KI-Übersetzung ausschalten", "Verbunden: %s", "KI-Übersetzung fehlgeschlagen: %s", "KI-Übersetzung gespeichert", "Cheats mit KI übersetzen", "Cheats zum Importieren wählen", "%d von %d gewählt", "wird übersetzt…", "Gewählte importieren (%d)", "Alle wählen", "Keine wählen", "Admin-Passwort", "Alle Cheats löschen", "Alle %d Cheats dieses Spiels löschen?", "Gelöscht", "KI: Spielnamen und Beschreibungen", "Findet den offiziellen Titel und übersetzt Namen und Beschreibungen", "Zuerst unter Einstellungen → KI-Übersetzung einen API-Schlüssel eintragen", "KI übersetzt %d / %d", "Was spielen?", "A: ein zufälliges Spiel, noch mal für ein anderes", "Details", "Originaltitel", "Datei", "Plattform", "Ort", "%d Discs", "Screenshots", "Version %s verfügbar", "%s herunterladen (GitHub)", "Nach Updates suchen", "Du hast die neueste Version", "KI-Bilder", "Konsolenbilder, Hintergründe und Cover mit deinem OpenAI-Schlüssel", "Mit KI erstellen", "Cover mit KI erstellen", "Was soll es zeigen?", "Bild wird erstellt, etwa 30 s…", "Zuerst unter Einstellungen → KI-Bilder einen Schlüssel eintragen", "Erfolgsfortschritt aus RetroArch; Schlüssel unter retroachievements.org → Einstellungen", "Benutzername", "Web-API-Schlüssel", "Erfolge zuletzt gespielter Spiele", "Schlüssel entfernen"
        },
        { // it
            "Caricamento giochi…", "Nessun gioco trovato\nAggiungi le cartelle dei giochi in Impostazioni → Cartelle dei giochi", "Serve l'accesso a tutti i file per leggere i giochi\nConsentilo nelle impostazioni e torna qui", "Secondo schermo non trovato", "Avvio non riuscito: %s",
            "Giocati di recente", "%d giochi", "%d", "%d piattaforme",
            "Home", "Apri", "Gioca", "Indietro", "Emulatore", "Scegli emulatore", "Emulatore della piattaforma",
            "Dimensione", "Ordine", "Predefinito", "Nome", "G", "M", "P", "XP",
            "Usa impostazione piattaforma", "Predefinito Pegasus (%s)", "Predefinito %s", "Attuale",
            "Impostazioni", "Lingua", "Aspetto piattaforme", "Immagine console", "Immagine di sfondo", "Scegli immagine…", "Ripristina",
            "Personalizzato", "Predefinito", "Informazioni", "Benvenuto in Duo Launcher", "Non impostato",
            "Nintendo 3DS", "Nintendo DS", "GBA modificati", "GBA vibrazione", "Salvato", "Lingua · Aspetto", "Impossibile leggere questa immagine",
            "Cartelle dei giochi", "Aggiungi cartella…", "Rimuovi questa cartella", "Trova automaticamente le cartelle Roms", "Ripeti scansione", "Sì", "No", "%d giochi trovati",
            "Cerca", "Scrivi il nome di un gioco", "Ricerca: %s", "Nessun gioco corrispondente", "Giocato %d h %d min", "Giocato %d min", "Ultima partita %s", "Modalità schermo", "Segui il sistema", "Chiaro", "Scuro", "Imposta come schermata Home (all'avvio)", "Misura il tempo di gioco con più precisione", "Giochi Android", "App", "Scegli le app da mostrare", "Fine", "Nessun emulatore impostato per questa piattaforma",
            "Nuova cartella", "Nome della cartella", "Rinomina", "Colore", "Icona", "Elimina cartella", "Aggiungi a cartella…", "Rimuovi da questa cartella", "Modifica cartella", "Aggiunto a %s", "La mia collezione", "Nascondi questa cartella", "Cartelle nascoste",
            "Nascondi questo gioco", "Giochi nascosti", "Musica dei banner 3DS", "Autorizzazioni", "Accesso a tutti i file (leggere i giochi)", "Accesso ai dati di utilizzo (tempo di gioco)", "Consentito", "Primo avvio: consenti queste autorizzazioni", "Ricerche recenti", "Cancella cronologia", "Tutte le piattaforme", "Cerca in",
            "Preferiti", "Aggiungi ai preferiti", "Rimuovi dai preferiti", "Continua", "Ordina e filtra", "Giocati di recente", "Più giocati", "Tutti", "Effetti sonori", "Musica di sottofondo", "Tema",
            "Azzurro", "Rosso ciliegia", "Verde menta", "Viola uva", "Mandarino", "Rosa sakura", "Grafite", "Colori personali…", "Colore principale", "Colore di sfondo", "Sfondo (chiaro)", "Sfondo (scuro)",
            "Dimensione testo", "Molto grande",
            "Home di Android", "Impostazioni Android", "Torna alla schermata Home di sistema",
            "Esci alla Home di Android", "Nascondi questo riquadro", "START apre le impostazioni anche se nascosto",
            "Scegli \"Quickstep\" nella pagina successiva per tornare alla Home di Android", "Vai", "Annulla", "Tornare alla Home di Android?",
            "Elenco", "Tasti", "Scambia A/B", "Scambia X/Y", "Statistiche", "Tempo di gioco totale", "Questa settimana", "Più giocati", "Ancora nessuna partita", "Salvaschermo", "%d min", "Anteprime video", "Backup dei salvataggi", "Ripristina salvataggi", "Salvato in %s", "%d file ripristinati", "In corso…", "Trasferimento Wi-Fi", "Apri in un browser sulla stessa rete Wi-Fi", "Ferma trasferimento", "%s ricevuto", "Esporta tema attuale", "Esportato in %s", "Riordina", "Giochi doppi", "Giochi senza copertina", "Scarica copertine", "Niente trovato", "Diagnostica", "%d copertine aggiunte", "Schermo superiore", "Automatico (video se presente)", "Logo", "Copertina", "Video", "modalità prestazioni", "Trucchi", "Scarica trucchi", "Ancora nessun trucco", "%d trucchi aggiunti", "Nessun trucco per questo gioco nel database, o download non riuscito", "Impossibile leggere l’ID del gioco (ROM cifrata?)", "Nessuna cartella di Azahar o Citra trovata", "Traduci", "%d tradotti", "%d non tradotti (offline o limite giornaliero raggiunto)", "Modifica trucchi", "Nuovo trucco", "Nome del trucco", "Codice (una riga ciascuno)", "Ogni riga richiede due numeri esadecimali di 8 cifre, es. 12345678 0000FFFF", "Elimina", "Traduci tutti i trucchi", "Imposta i trucchi nell’emulatore (qui: 3DS e giochi avviati con RetroArch)", "La prima volta: in RetroArch, Menu rapido → Trucchi, attiva l’applicazione automatica al caricamento", "Troppi trucchi attivi: DraStic carica al massimo 256 righe, disattivane alcuni", "Sposta", "Rilascia", "Tieni: sposta", "Traduzione IA", "Nomi dei trucchi tradotti da un'IA con la tua chiave; i servizi gratuiti restano di riserva", "Servizio", "Chiave API", "Modello", "Indirizzo", "Inserisci da computer o telefono (pagina Wi-Fi)", "Prova connessione", "Disattiva traduzione IA", "Connesso: %s", "Traduzione IA non riuscita: %s", "Impostazioni traduzione IA salvate", "Traduci i trucchi con l'IA", "Scegli i trucchi da importare", "%d di %d scelti", "traduzione…", "Importa scelti (%d)", "Seleziona tutto", "Deseleziona tutto", "Password amministratore", "Elimina tutti i trucchi", "Eliminare tutti i %d trucchi di questo gioco?", "Eliminato", "IA: nomi e descrizioni", "Trova il titolo ufficiale e traduce nomi e descrizioni", "Prima inserisci una chiave API in Impostazioni → Traduzione IA", "Traduzione IA %d / %d", "A cosa giocare?", "A: un gioco a caso, di nuovo per un altro", "Dettagli", "Titolo originale", "File", "Piattaforma", "Posizione", "%d dischi", "Screenshot", "Versione %s disponibile", "Scarica %s (GitHub)", "Cerca aggiornamenti", "Hai l'ultima versione", "Immagini IA", "Console, sfondi e copertine con la tua chiave OpenAI", "Crea con l'IA", "Crea copertina con l'IA", "Cosa deve mostrare?", "Creazione immagine, circa 30 s…", "Prima inserisci una chiave in Impostazioni → Immagini IA", "Progressi degli obiettivi in RetroArch; chiave su retroachievements.org → Impostazioni", "Nome utente", "Chiave Web API", "Obiettivi dei giochi recenti", "Rimuovi chiave"
        },
        { // pt
            "Carregando jogos…", "Nenhum jogo encontrado\nAdicione as pastas dos seus jogos em Configurações → Pastas de jogos", "É preciso acesso a todos os arquivos para ler seus jogos\nPermita nas configurações e volte", "Segunda tela não encontrada", "Falha ao iniciar: %s",
            "Jogados recentemente", "%d jogos", "%d", "%d plataformas",
            "Início", "Abrir", "Jogar", "Voltar", "Emulador", "Escolher emulador", "Emulador da plataforma",
            "Tamanho", "Ordem", "Padrão", "Nome", "G", "M", "P", "MP",
            "Usar ajuste da plataforma", "Padrão do Pegasus (%s)", "Padrão de %s", "Atual",
            "Configurações", "Idioma", "Aparência das plataformas", "Imagem do console", "Imagem de fundo", "Escolher imagem…", "Restaurar padrão",
            "Personalizado", "Padrão", "Sobre", "Bem-vindo ao Duo Launcher", "Não definido",
            "Nintendo 3DS", "Nintendo DS", "GBA modificados", "GBA com vibração", "Salvo", "Idioma · Aparência", "Não foi possível ler esta imagem",
            "Pastas de jogos", "Adicionar pasta…", "Remover esta pasta", "Encontrar pastas Roms automaticamente", "Procurar novamente", "Ligado", "Desligado", "%d jogos encontrados",
            "Pesquisar", "Digite o nome de um jogo", "Pesquisa: %s", "Nenhum jogo encontrado", "Jogado %d h %d min", "Jogado %d min", "Última vez %s", "Modo de exibição", "Seguir o sistema", "Claro", "Escuro", "Definir como tela inicial (ao ligar)", "Medir o tempo de jogo com mais precisão", "Jogos Android", "Apps", "Escolher apps exibidos", "Concluir", "Nenhum emulador definido para esta plataforma",
            "Nova pasta", "Nome da pasta", "Renomear", "Cor", "Ícone", "Excluir pasta", "Adicionar à pasta…", "Remover desta pasta", "Editar pasta", "Adicionado a %s", "Minha coleção", "Ocultar esta pasta", "Pastas ocultas",
            "Ocultar este jogo", "Jogos ocultos", "Música dos banners do 3DS", "Permissões", "Acesso a todos os arquivos (ler jogos)", "Acesso ao uso (tempo de jogo)", "Permitido", "Primeira vez: permita estas permissões", "Pesquisas recentes", "Limpar histórico", "Todas as plataformas", "Pesquisar em",
            "Favoritos", "Adicionar aos favoritos", "Remover dos favoritos", "Continuar", "Ordenar e filtrar", "Jogados recentemente", "Mais jogados", "Todos", "Efeitos sonoros", "Música de fundo", "Tema",
            "Azul céu", "Vermelho cereja", "Verde menta", "Roxo uva", "Tangerina", "Rosa sakura", "Grafite", "Cores próprias…", "Cor principal", "Cor de fundo", "Fundo (claro)", "Fundo (escuro)",
            "Tamanho do texto", "Muito grande",
            "Início do Android", "Configurações do Android", "Voltar à tela inicial do sistema",
            "Sair para o início do Android", "Ocultar este bloco", "START abre as configurações mesmo oculto",
            "Escolha \"Quickstep\" na próxima tela para voltar ao início do Android", "Ir", "Cancelar", "Voltar ao início do Android?",
            "Lista", "Botões", "Trocar A/B", "Trocar X/Y", "Estatísticas", "Tempo total de jogo", "Esta semana", "Mais jogados", "Nenhum jogo ainda", "Protetor de tela", "%d min", "Prévias em vídeo", "Fazer backup dos saves", "Restaurar saves", "Salvo em %s", "%d arquivos restaurados", "Processando…", "Transferência Wi-Fi", "Abra num navegador na mesma rede Wi-Fi", "Parar transferência", "%s recebido", "Exportar tema atual", "Exportado para %s", "Organizar", "Jogos duplicados", "Jogos sem capa", "Baixar capas", "Nada encontrado", "Diagnóstico", "%d capas adicionadas", "Tela superior", "Automático (vídeo se houver)", "Logo", "Capa", "Vídeo", "modo desempenho", "Trapaças", "Baixar trapaças", "Ainda não há trapaças", "%d trapaças adicionadas", "Não há trapaças para este jogo ou o download falhou", "Não foi possível ler o ID do jogo (ROM criptografada?)", "Pasta do Azahar ou Citra não encontrada", "Traduzir", "%d traduzidas", "%d não traduzidas (offline ou limite diário atingido)", "Editar trapaças", "Nova trapaça", "Nome da trapaça", "Código (uma linha cada)", "Cada linha precisa de dois números hexadecimais de 8 dígitos, ex. 12345678 0000FFFF", "Excluir", "Traduzir todas as trapaças", "Configure as trapaças no próprio emulador (aqui: 3DS e jogos pelo RetroArch)", "Na primeira vez: no RetroArch, Menu rápido → Trapaças, ative aplicar ao carregar o jogo", "Trapaças demais ativadas: o DraStic carrega no máximo 256 linhas, desative algumas", "Mover", "Soltar", "Segurar: mover", "Tradução por IA", "Nomes de cheats traduzidos por uma IA com a sua chave; os serviços gratuitos ficam de reserva", "Serviço", "Chave de API", "Modelo", "Endereço", "Digitar no computador ou celular (página Wi-Fi)", "Testar conexão", "Desligar tradução por IA", "Conectado: %s", "Falha na tradução por IA: %s", "Ajustes de tradução por IA salvos", "Traduzir cheats com IA", "Escolha os cheats a importar", "%d de %d escolhidos", "traduzindo…", "Importar escolhidos (%d)", "Selecionar tudo", "Desmarcar tudo", "Senha de administração", "Apagar todos os cheats", "Apagar todos os %d cheats deste jogo?", "Apagado", "IA: nomes e descrições", "Encontra o título oficial e traduz nomes e descrições", "Primeiro coloque uma chave de API em Ajustes → Tradução por IA", "Traduzindo com IA %d / %d", "O que jogar?", "A: um jogo aleatório; de novo para outro", "Detalhes", "Título original", "Arquivo", "Plataforma", "Local", "%d discos", "Capturas", "Versão %s disponível", "Baixar %s (GitHub)", "Procurar atualizações", "Você tem a versão mais recente", "Imagens por IA", "Consoles, fundos e capas com a sua chave da OpenAI", "Criar com IA", "Criar capa com IA", "O que deve mostrar?", "Criando a imagem, cerca de 30 s…", "Primeiro coloque uma chave em Ajustes → Imagens por IA", "Progresso das conquistas no RetroArch; chave em retroachievements.org → Configurações", "Usuário", "Chave Web API", "Conquistas dos jogos recentes", "Remover chave"
        },
        { // ru
            "Загрузка игр…", "Игры не найдены\nДобавьте папки с играми в Настройки → Папки с играми", "Для чтения игр нужен доступ ко всем файлам\nРазрешите его в настройках и вернитесь", "Второй экран не найден", "Не удалось запустить: %s",
            "Недавние", "Игр: %d", "%d", "Платформ: %d",
            "Главная", "Открыть", "Играть", "Назад", "Эмулятор", "Выбор эмулятора", "Эмулятор платформы",
            "Размер", "Сортировка", "По умолчанию", "Имя", "L", "M", "S", "XS",
            "Как у платформы", "По умолчанию Pegasus (%s)", "По умолчанию для %s", "Текущий",
            "Настройки", "Язык", "Оформление платформ", "Изображение консоли", "Фоновое изображение", "Выбрать изображение…", "Сбросить",
            "Своё", "По умолчанию", "О программе", "Добро пожаловать в Duo Launcher", "Не задано",
            "Nintendo 3DS", "Nintendo DS", "Хаки GBA", "GBA с вибрацией", "Сохранено", "Язык · Оформление", "Не удалось прочитать изображение",
            "Папки с играми", "Добавить папку…", "Убрать эту папку", "Искать папки Roms автоматически", "Пересканировать", "Вкл", "Выкл", "Найдено игр: %d",
            "Поиск", "Введите название игры", "Поиск: %s", "Совпадений нет", "Сыграно %d ч %d мин", "Сыграно %d мин", "Последний запуск %s", "Режим отображения", "Как в системе", "Светлый", "Тёмный", "Сделать главным экраном (запуск при включении)", "Точнее считать игровое время", "Игры Android", "Приложения", "Выбрать приложения для показа", "Готово", "Для этой платформы не задан эмулятор",
            "Новая папка", "Название папки", "Переименовать", "Цвет", "Значок", "Удалить папку", "Добавить в папку…", "Убрать из этой папки", "Изменить папку", "Добавлено в «%s»", "Моя коллекция", "Скрыть эту папку", "Скрытые папки",
            "Скрыть эту игру", "Скрытые игры", "Музыка баннеров 3DS", "Разрешения", "Доступ ко всем файлам (чтение игр)", "Доступ к статистике (время игры)", "Разрешено", "Первый запуск: разрешите эти права", "Недавние запросы", "Очистить историю", "Все платформы", "Искать в",
            "Избранное", "В избранное", "Убрать из избранного", "Продолжить", "Сортировка и фильтр", "Недавние", "Больше всего играл", "Все", "Звуки", "Фоновая музыка", "Тема",
            "Небесно-голубая", "Вишнёвая", "Мятная", "Виноградная", "Мандариновая", "Сакура", "Графит", "Свои цвета…", "Основной цвет", "Цвет фона", "Фон (светлый)", "Фон (тёмный)",
            "Размер текста", "Очень крупный",
            "Рабочий стол Android", "Настройки Android", "Вернуться на системный рабочий стол",
            "Выйти на рабочий стол Android", "Скрыть эту плитку", "START открывает настройки, даже если скрыто",
            "На следующем экране выберите «Quickstep», чтобы вернуть рабочий стол Android", "Перейти", "Отмена", "Вернуться на рабочий стол Android?",
            "Список", "Кнопки", "Поменять A/B", "Поменять X/Y", "Статистика", "Всего в игре", "Эта неделя", "Больше всего играли", "Пока ничего не запускали", "Заставка", "%d мин", "Видеопревью", "Резервная копия сохранений", "Восстановить сохранения", "Сохранено в %s", "Восстановлено файлов: %d", "Подождите…", "Передача по Wi-Fi", "Откройте в браузере в той же сети Wi-Fi", "Остановить передачу", "Получено: %s", "Экспорт текущей темы", "Экспортировано в %s", "Порядок", "Дубликаты игр", "Игры без обложек", "Скачать обложки", "Ничего не найдено", "Диагностика", "Добавлено обложек: %d", "Верхний экран", "Авто (видео, если есть)", "Логотип", "Обложка", "Видео", "режим производительности", "Читы", "Скачать читы", "Читов пока нет", "Добавлено читов: %d", "Для этой игры нет читов в базе, или загрузка не удалась", "Не удаётся прочитать ID игры (зашифрованный ROM?)", "Папка Azahar или Citra не найдена", "Перевести", "Переведено: %d", "Не переведено: %d (нет сети или дневной лимит исчерпан)", "Изменить читы", "Новый чит", "Название чита", "Код (по строке)", "Каждая строка — два 16-ричных числа по 8 цифр, напр. 12345678 0000FFFF", "Удалить", "Перевести все читы", "Читы для этого эмулятора настраиваются в нём (здесь: 3DS и игры через RetroArch)", "Впервые: в RetroArch, Быстрое меню → Читы, включите автоприменение при загрузке игры", "Слишком много включённых читов: DraStic загружает не более 256 строк, отключите часть", "Переместить", "Положить", "Удерживать: переместить", "ИИ-перевод", "Названия читов переводит ИИ по вашему ключу; бесплатные сервисы остаются запасными", "Сервис", "API-ключ", "Модель", "Адрес", "Ввести с компьютера или телефона (страница Wi-Fi)", "Проверить связь", "Выключить ИИ-перевод", "Подключено: %s", "Ошибка ИИ-перевода: %s", "Настройки ИИ-перевода сохранены", "Перевести читы с ИИ", "Выберите читы для импорта", "выбрано %d из %d", "перевод…", "Импортировать выбранные (%d)", "Выбрать все", "Снять все", "Пароль администратора", "Удалить все читы", "Удалить все читы этой игры (%d)?", "Удалено", "ИИ: названия и описания игр", "Находит официальное название и переводит названия и описания", "Сначала укажите API-ключ в Настройки → ИИ-перевод", "ИИ-перевод %d / %d", "Во что поиграть?", "A — случайная игра, ещё раз — другая", "Подробности", "Оригинальное название", "Файл", "Платформа", "Расположение", "дисков: %d", "Скриншоты", "Доступна версия %s", "Скачать %s (GitHub)", "Проверить обновления", "Установлена последняя версия", "ИИ-картинки", "Консоли, фоны и обложки по вашему ключу OpenAI", "Создать с ИИ", "Создать обложку с ИИ", "Что изобразить?", "Рисую картинку, около 30 с…", "Сначала укажите ключ в Настройки → ИИ-картинки", "Прогресс достижений в RetroArch; ключ на retroachievements.org → Settings", "Имя пользователя", "Ключ Web API", "Достижения недавних игр", "Удалить ключ"
        },
    };

    static {
        for (int i = 0; i < T.length; i++)
            if (T[i].length != S.values().length)
                throw new IllegalStateException("translation " + CODES[i] + " has " + T[i].length + " strings, expected " + S.values().length);
    }

    private static int lang = 2;

    static void set(String code) {
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(code)) { lang = i; return; }
    }

    static int index() { return lang; }

    static String code() { return CODES[lang]; }

    static String name() { return NAMES[lang]; }

    static String t(S s) { return T[lang][s.ordinal()]; }

    static String f(S s, Object... args) { return String.format(Locale.ROOT, t(s), args); }

    /** Best match for the device locale, used until the user picks a language. */
    static String systemDefault() {
        Locale l = Locale.getDefault();
        String lg = l.getLanguage();
        if (lg.equals("zh")) {
            String sc = l.getScript(), rg = l.getCountry();
            return sc.equals("Hant") || rg.equals("TW") || rg.equals("HK") || rg.equals("MO") ? "zh-TW" : "zh-CN";
        }
        for (String c : CODES) if (c.equals(lg)) return c;
        return "en";
    }
}
