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
        NEW_FOLDER, FOLDER_NAME, RENAME, COLOR, ICON, DELETE_FOLDER, ADD_TO_FOLDER, REMOVE_FROM_FOLDER, EDIT_FOLDER, ADDED_TO, MY_FOLDER
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
            "新建文件夹", "文件夹名称", "重命名", "颜色", "图标", "删除文件夹", "加入文件夹…", "从这个文件夹移除", "编辑文件夹", "已加入「%s」", "我的收藏"
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
            "新增資料夾", "資料夾名稱", "重新命名", "顏色", "圖示", "刪除資料夾", "加入資料夾…", "從這個資料夾移除", "編輯資料夾", "已加入「%s」", "我的收藏"
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
            "New folder", "Folder name", "Rename", "Colour", "Icon", "Delete folder", "Add to folder…", "Remove from this folder", "Edit folder", "Added to %s", "My collection"
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
            "新しいフォルダー", "フォルダー名", "名前を変更", "色", "アイコン", "フォルダーを削除", "フォルダーに追加…", "このフォルダーから外す", "フォルダーを編集", "「%s」に追加しました", "マイコレクション"
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
            "새 폴더", "폴더 이름", "이름 바꾸기", "색상", "아이콘", "폴더 삭제", "폴더에 추가…", "이 폴더에서 제거", "폴더 편집", "%s에 추가됨", "내 컬렉션"
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
            "Nouveau dossier", "Nom du dossier", "Renommer", "Couleur", "Icône", "Supprimer le dossier", "Ajouter à un dossier…", "Retirer de ce dossier", "Modifier le dossier", "Ajouté à %s", "Ma collection"
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
            "Nueva carpeta", "Nombre de la carpeta", "Cambiar nombre", "Color", "Icono", "Eliminar carpeta", "Añadir a carpeta…", "Quitar de esta carpeta", "Editar carpeta", "Añadido a %s", "Mi colección"
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
            "Neuer Ordner", "Ordnername", "Umbenennen", "Farbe", "Symbol", "Ordner löschen", "Zu Ordner hinzufügen…", "Aus diesem Ordner entfernen", "Ordner bearbeiten", "Zu %s hinzugefügt", "Meine Sammlung"
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
            "Nuova cartella", "Nome della cartella", "Rinomina", "Colore", "Icona", "Elimina cartella", "Aggiungi a cartella…", "Rimuovi da questa cartella", "Modifica cartella", "Aggiunto a %s", "La mia collezione"
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
            "Nova pasta", "Nome da pasta", "Renomear", "Cor", "Ícone", "Excluir pasta", "Adicionar à pasta…", "Remover desta pasta", "Editar pasta", "Adicionado a %s", "Minha coleção"
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
            "Новая папка", "Название папки", "Переименовать", "Цвет", "Значок", "Удалить папку", "Добавить в папку…", "Убрать из этой папки", "Изменить папку", "Добавлено в «%s»", "Моя коллекция"
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
