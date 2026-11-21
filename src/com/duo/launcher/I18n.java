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
        T_3DS, T_NDS, T_GBA_HACK, T_GBA_VIB, SAVED, SETTINGS_HINT, IMAGE_FAILED
    }

    static final String[] CODES = {"zh-CN", "zh-TW", "en", "ja", "ko", "fr", "es", "de", "it", "pt", "ru"};
    static final String[] NAMES = {"简体中文", "繁體中文", "English", "日本語", "한국어", "Français", "Español", "Deutsch", "Italiano", "Português", "Русский"};

    private static final String[][] T = {
        { // zh-CN
            "正在读取游戏…", "没有找到游戏\n需要 Roms/<平台>/metadata.pegasus.txt", "需要「所有文件访问权限」才能读取游戏\n请在弹出的设置里允许后返回", "没有找到第二块屏幕", "启动失败：%s",
            "最近玩过", "%d 款游戏", "%d 款", "%d 个平台",
            "主页", "打开", "开始游戏", "返回", "模拟器", "选择模拟器", "平台模拟器",
            "大小", "排序", "默认", "名称", "大", "中", "小", "迷你",
            "跟随平台设置", "天马G 默认（%s）", "%s 平台默认", "当前",
            "设置", "语言", "平台外观", "主机图片", "背景图片", "选择图片…", "恢复默认",
            "自定义", "默认", "关于", "欢迎使用 Duo Launcher", "未设置",
            "任天堂 3DS", "任天堂 DS", "GBA 改版", "GBA 震动版", "已保存", "语言 · 平台外观", "无法读取这张图片"
        },
        { // zh-TW
            "正在讀取遊戲…", "找不到遊戲\n需要 Roms/<平台>/metadata.pegasus.txt", "需要「所有檔案存取權限」才能讀取遊戲\n請在彈出的設定中允許後返回", "找不到第二個螢幕", "啟動失敗：%s",
            "最近玩過", "%d 款遊戲", "%d 款", "%d 個平台",
            "主頁", "開啟", "開始遊戲", "返回", "模擬器", "選擇模擬器", "平台模擬器",
            "大小", "排序", "預設", "名稱", "大", "中", "小", "迷你",
            "跟隨平台設定", "天馬G 預設（%s）", "%s 平台預設", "目前",
            "設定", "語言", "平台外觀", "主機圖片", "背景圖片", "選擇圖片…", "恢復預設",
            "自訂", "預設", "關於", "歡迎使用 Duo Launcher", "未設定",
            "任天堂 3DS", "任天堂 DS", "GBA 改版", "GBA 震動版", "已儲存", "語言 · 平台外觀", "無法讀取這張圖片"
        },
        { // en
            "Loading games…", "No games found\nExpected Roms/<platform>/metadata.pegasus.txt", "All-files access is needed to read your games\nAllow it in the settings screen, then come back", "Second screen not found", "Couldn't start: %s",
            "Recently played", "%d games", "%d", "%d platforms",
            "Home", "Open", "Play", "Back", "Emulator", "Choose emulator", "Platform emulator",
            "Size", "Sort", "Default", "Name", "L", "M", "S", "XS",
            "Use platform setting", "Pegasus default (%s)", "%s default", "Current",
            "Settings", "Language", "Platform look", "Console image", "Background image", "Choose image…", "Reset to default",
            "Custom", "Default", "About", "Welcome to Duo Launcher", "Not set",
            "Nintendo 3DS", "Nintendo DS", "GBA Hacks", "GBA Rumble", "Saved", "Language · Platform look", "Couldn't read this image"
        },
        { // ja
            "ゲームを読み込み中…", "ゲームが見つかりません\nRoms/<機種>/metadata.pegasus.txt が必要です", "ゲームを読み込むには「すべてのファイルへのアクセス」が必要です\n設定画面で許可してから戻ってください", "2つ目の画面が見つかりません", "起動できませんでした：%s",
            "最近遊んだゲーム", "%d 本のゲーム", "%d 本", "%d 機種",
            "ホーム", "開く", "プレイ", "戻る", "エミュレーター", "エミュレーターを選択", "機種のエミュレーター",
            "サイズ", "並び替え", "標準", "名前", "大", "中", "小", "極小",
            "機種の設定に従う", "Pegasus 標準（%s）", "%s の標準", "現在",
            "設定", "言語", "機種の外観", "本体画像", "背景画像", "画像を選択…", "標準に戻す",
            "カスタム", "標準", "情報", "Duo Launcher へようこそ", "未設定",
            "ニンテンドー3DS", "ニンテンドーDS", "GBA 改造版", "GBA 振動版", "保存しました", "言語 · 機種の外観", "この画像を読み込めません"
        },
        { // ko
            "게임 불러오는 중…", "게임을 찾을 수 없습니다\nRoms/<플랫폼>/metadata.pegasus.txt 가 필요합니다", "게임을 읽으려면 '모든 파일 접근' 권한이 필요합니다\n설정 화면에서 허용한 뒤 돌아오세요", "두 번째 화면을 찾을 수 없습니다", "실행 실패: %s",
            "최근 플레이", "게임 %d개", "%d개", "플랫폼 %d개",
            "홈", "열기", "플레이", "뒤로", "에뮬레이터", "에뮬레이터 선택", "플랫폼 에뮬레이터",
            "크기", "정렬", "기본", "이름", "대", "중", "소", "최소",
            "플랫폼 설정 따르기", "Pegasus 기본 (%s)", "%s 기본값", "현재",
            "설정", "언어", "플랫폼 외관", "본체 이미지", "배경 이미지", "이미지 선택…", "기본값으로 복원",
            "사용자 지정", "기본", "정보", "Duo Launcher에 오신 것을 환영합니다", "설정 안 됨",
            "닌텐도 3DS", "닌텐도 DS", "GBA 개조판", "GBA 진동판", "저장됨", "언어 · 플랫폼 외관", "이 이미지를 읽을 수 없습니다"
        },
        { // fr
            "Chargement des jeux…", "Aucun jeu trouvé\nFichier attendu : Roms/<plateforme>/metadata.pegasus.txt", "L'accès à tous les fichiers est nécessaire pour lire vos jeux\nAutorisez-le dans les paramètres puis revenez", "Second écran introuvable", "Échec du lancement : %s",
            "Joués récemment", "%d jeux", "%d", "%d plateformes",
            "Accueil", "Ouvrir", "Jouer", "Retour", "Émulateur", "Choisir l'émulateur", "Émulateur de la plateforme",
            "Taille", "Tri", "Par défaut", "Nom", "G", "M", "P", "TP",
            "Suivre la plateforme", "Pegasus par défaut (%s)", "%s par défaut", "Actuel",
            "Paramètres", "Langue", "Apparence des plateformes", "Image de la console", "Image de fond", "Choisir une image…", "Rétablir par défaut",
            "Personnalisé", "Par défaut", "À propos", "Bienvenue dans Duo Launcher", "Non défini",
            "Nintendo 3DS", "Nintendo DS", "GBA modifiés", "GBA vibration", "Enregistré", "Langue · Apparence", "Impossible de lire cette image"
        },
        { // es
            "Cargando juegos…", "No se encontraron juegos\nSe necesita Roms/<plataforma>/metadata.pegasus.txt", "Se necesita acceso a todos los archivos para leer tus juegos\nPermítelo en los ajustes y vuelve", "No se encontró la segunda pantalla", "No se pudo iniciar: %s",
            "Jugados recientemente", "%d juegos", "%d", "%d plataformas",
            "Inicio", "Abrir", "Jugar", "Atrás", "Emulador", "Elegir emulador", "Emulador de la plataforma",
            "Tamaño", "Orden", "Predeterminado", "Nombre", "G", "M", "P", "MP",
            "Usar ajuste de la plataforma", "Predeterminado de Pegasus (%s)", "Predeterminado de %s", "Actual",
            "Ajustes", "Idioma", "Aspecto de plataformas", "Imagen de la consola", "Imagen de fondo", "Elegir imagen…", "Restablecer",
            "Personalizado", "Predeterminado", "Acerca de", "Bienvenido a Duo Launcher", "Sin definir",
            "Nintendo 3DS", "Nintendo DS", "GBA modificados", "GBA vibración", "Guardado", "Idioma · Aspecto", "No se pudo leer esta imagen"
        },
        { // de
            "Spiele werden geladen…", "Keine Spiele gefunden\nErwartet: Roms/<Plattform>/metadata.pegasus.txt", "Zum Lesen der Spiele wird Zugriff auf alle Dateien benötigt\nBitte in den Einstellungen erlauben und zurückkehren", "Zweiter Bildschirm nicht gefunden", "Start fehlgeschlagen: %s",
            "Zuletzt gespielt", "%d Spiele", "%d", "%d Plattformen",
            "Start", "Öffnen", "Spielen", "Zurück", "Emulator", "Emulator wählen", "Plattform-Emulator",
            "Größe", "Sortierung", "Standard", "Name", "L", "M", "S", "XS",
            "Plattform-Einstellung", "Pegasus-Standard (%s)", "Standard für %s", "Aktuell",
            "Einstellungen", "Sprache", "Plattform-Aussehen", "Konsolenbild", "Hintergrundbild", "Bild wählen…", "Zurücksetzen",
            "Eigenes", "Standard", "Info", "Willkommen bei Duo Launcher", "Nicht festgelegt",
            "Nintendo 3DS", "Nintendo DS", "GBA-Hacks", "GBA mit Rumble", "Gespeichert", "Sprache · Aussehen", "Bild konnte nicht gelesen werden"
        },
        { // it
            "Caricamento giochi…", "Nessun gioco trovato\nServe Roms/<piattaforma>/metadata.pegasus.txt", "Serve l'accesso a tutti i file per leggere i giochi\nConsentilo nelle impostazioni e torna qui", "Secondo schermo non trovato", "Avvio non riuscito: %s",
            "Giocati di recente", "%d giochi", "%d", "%d piattaforme",
            "Home", "Apri", "Gioca", "Indietro", "Emulatore", "Scegli emulatore", "Emulatore della piattaforma",
            "Dimensione", "Ordine", "Predefinito", "Nome", "G", "M", "P", "XP",
            "Usa impostazione piattaforma", "Predefinito Pegasus (%s)", "Predefinito %s", "Attuale",
            "Impostazioni", "Lingua", "Aspetto piattaforme", "Immagine console", "Immagine di sfondo", "Scegli immagine…", "Ripristina",
            "Personalizzato", "Predefinito", "Informazioni", "Benvenuto in Duo Launcher", "Non impostato",
            "Nintendo 3DS", "Nintendo DS", "GBA modificati", "GBA vibrazione", "Salvato", "Lingua · Aspetto", "Impossibile leggere questa immagine"
        },
        { // pt
            "Carregando jogos…", "Nenhum jogo encontrado\nÉ necessário Roms/<plataforma>/metadata.pegasus.txt", "É preciso acesso a todos os arquivos para ler seus jogos\nPermita nas configurações e volte", "Segunda tela não encontrada", "Falha ao iniciar: %s",
            "Jogados recentemente", "%d jogos", "%d", "%d plataformas",
            "Início", "Abrir", "Jogar", "Voltar", "Emulador", "Escolher emulador", "Emulador da plataforma",
            "Tamanho", "Ordem", "Padrão", "Nome", "G", "M", "P", "MP",
            "Usar ajuste da plataforma", "Padrão do Pegasus (%s)", "Padrão de %s", "Atual",
            "Configurações", "Idioma", "Aparência das plataformas", "Imagem do console", "Imagem de fundo", "Escolher imagem…", "Restaurar padrão",
            "Personalizado", "Padrão", "Sobre", "Bem-vindo ao Duo Launcher", "Não definido",
            "Nintendo 3DS", "Nintendo DS", "GBA modificados", "GBA com vibração", "Salvo", "Idioma · Aparência", "Não foi possível ler esta imagem"
        },
        { // ru
            "Загрузка игр…", "Игры не найдены\nНужен файл Roms/<платформа>/metadata.pegasus.txt", "Для чтения игр нужен доступ ко всем файлам\nРазрешите его в настройках и вернитесь", "Второй экран не найден", "Не удалось запустить: %s",
            "Недавние", "Игр: %d", "%d", "Платформ: %d",
            "Главная", "Открыть", "Играть", "Назад", "Эмулятор", "Выбор эмулятора", "Эмулятор платформы",
            "Размер", "Сортировка", "По умолчанию", "Имя", "L", "M", "S", "XS",
            "Как у платформы", "По умолчанию Pegasus (%s)", "По умолчанию для %s", "Текущий",
            "Настройки", "Язык", "Оформление платформ", "Изображение консоли", "Фоновое изображение", "Выбрать изображение…", "Сбросить",
            "Своё", "По умолчанию", "О программе", "Добро пожаловать в Duo Launcher", "Не задано",
            "Nintendo 3DS", "Nintendo DS", "Хаки GBA", "GBA с вибрацией", "Сохранено", "Язык · Оформление", "Не удалось прочитать изображение"
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
