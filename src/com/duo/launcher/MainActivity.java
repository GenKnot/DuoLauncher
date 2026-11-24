package com.duo.launcher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Presentation;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.io.File;
import android.os.SystemClock;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

public final class MainActivity extends Activity implements ShelfView.Listener {
    private static final int MAX_RECENT = 20;
    private static final int PICK_IMAGE = 1, PICK_FOLDER = 2;
    private static final long MAX_SESSION = 24L * 3600_000;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ShelfView.State shelfState = new ShelfView.State();
    private final Set<String> hiddenGames = new HashSet<>();
    private final java.util.Map<Library.Platform, List<Library.Game>> fullLists = new java.util.HashMap<>();
    private SharedPreferences prefs;
    private Icons icons;
    private TopView top;
    private ShelfView shelf;
    private Presentation pres;
    private Library lib;
    private boolean loading, reloadPending, askedPermission;
    private int stickX, stickY;
    private String pickPlatform, pickKind;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // emulators such as DraStic take plain file paths in extras; allow file:// data too
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        prefs = getSharedPreferences("duo", MODE_PRIVATE);
        Usage.init(this);
        I18n.set(prefs.getString("lang", I18n.systemDefault()));
        applyDisplayMode();
        shelfState.hidden.addAll(prefs.getStringSet("hidden", new HashSet<>()));
        for (String k : prefs.getString("homeOrder", "").split("\n")) if (!k.isEmpty()) shelfState.homeOrder.add(k);
        for (java.util.Map.Entry<String, ?> e : prefs.getAll().entrySet())
            if (e.getKey().startsWith("order:") && e.getValue() instanceof String) {
                List<String> keys = new ArrayList<>();
                for (String k : ((String) e.getValue()).split("\n")) if (!k.isEmpty()) keys.add(k);
                shelfState.gameOrder.put(e.getKey().substring(6), keys);
            }
        shelfState.homeMode = prefs.getInt("homeMode", prefs.getBoolean("homeByGenre", false) ? 1 : 0);
        shelfState.tilePad = prefs.getInt("tilePad", 0);
        hiddenGames.addAll(prefs.getStringSet("hiddenGames", new HashSet<>()));
        Theme.init(this);
        applyTextScale();
        applyLite();
        Storage.mode = prefs.getString("dataHome", Storage.AUTO);
        Storage.init(this);
        prefs.registerOnSharedPreferenceChangeListener(copyWatch);
        TopView.prefs = prefs;
        loadSeenNew();
        // RetroArch keeps the config file the game's launch line names (its own retroarch.cfg): that one shows Retroid's
        // per-system frames at full size. Retroid's per-screen retroarch1920x1080.cfg draws every game small inside
        // a TV frame on the Duo's upper screen, so it is not used.
        loadToolLooks();
        loadSounds();
        icons = new Icons(this);
        top = new TopView(this, icons);
        setContentView(top);
    }

    @Override protected void onResume() {
        super.onResume();
        Library.paused = false;
        Storage.init(this);   // a card may have been put in or taken out
        main.removeCallbacks(copyTick);
        main.postDelayed(copyTick, 30_000);
        if (top != null) top.resumeTick();
        hideBars();
        applyDisplayMode();
        finishPlaySession();
        if (sfx != null) sfx.resumeMusic();
        lastInput = SystemClock.uptimeMillis();
        main.removeCallbacks(idleCheck);
        main.postDelayed(idleCheck, 15_000);
        keepAwake(prefs.getInt("screensaver", 5) > 0 || (wifi != null && wifi.running()));
        getSystemService(DisplayManager.class).registerDisplayListener(displays, main);
        showShelf();
        shelfState.askAi = true;
        PadKeyboard.askAi = true;
        if (lib == null && !loading) {
            if (Environment.isExternalStorageManager()) {
                load();
            } else {
                top.showMessage(t(S.NEED_PERMISSION));
                if (!askedPermission) {
                    askedPermission = true;
                    try {
                        startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                    }
                }
            }
        } else if (lib != null && shelf != null) {
            // games copied while the launcher was in the background show up without a manual rescan
            if (!loading && SystemClock.uptimeMillis() - lastScanAt > RESCAN_MS) { quietScan = true; load(); }
            shelf.setRecent(recentGames());
            if (pendingPermissions && shelfState.menu != null) showPermissionsMenu(shelfState.menu.parent);
            if (shelfState.menu == null) onFocus(shelf.selected());
            if (askBackup && shelfState.menu == null) showBackupReminder();
            if (!newGames.isEmpty() && shelfState.menu == null) askNewGenres();
            if (!prefs.contains("lang") && shelfState.menu == null) showGuide(G_LANG);
        }
    }

    @Override protected void onPause() {
        main.removeCallbacks(copyTick);
        stopBanner();
        main.removeCallbacks(idleCheck);
        if (saving) stopSaver();
        keepAwake(false);
        if (sfx != null) sfx.pauseMusic();
        // a game's video must not keep playing (or start late) under the emulator; redrawn on return
        if (voice != null) { voice.stop(); voice = null; }
        voiceTo = null;
        main.removeCallbacks(holdFired);
        holdCode = 0;
        holdListening = false;
        closeChat();
        closeKeyboard();
        if (top != null) { top.reset(); top.pauseTick(); }
        stopRepeat();
        // a game (or another app) takes over: a scan under way waits, and the pictures in memory are let go
        Library.paused = true;
        if (icons != null) icons.trim();
        // emulators drive the second screen themselves
        if (pres != null) { pres.dismiss(); pres = null; shelf = null; }
        getSystemService(DisplayManager.class).unregisterDisplayListener(displays);
        super.onPause();
    }

    /** Pressing Home while this is the home app returns to the home page. */
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            shelfState.menu = null;
            if (shelf != null && lib != null) shelf.goHome();
        }
    }

    private void hideBars() {
        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController c = getWindow().getInsetsController();
        if (c != null) {
            c.hide(WindowInsets.Type.systemBars());
            c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private final DisplayManager.DisplayListener displays = new DisplayManager.DisplayListener() {
        @Override public void onDisplayAdded(int id) { if (pres == null && hasWindowFocus()) showShelf(); }

        @Override public void onDisplayRemoved(int id) {
            if (pres != null && pres.getDisplay() != null && pres.getDisplay().getDisplayId() == id) {
                try { pres.dismiss(); } catch (RuntimeException ignored) { }
                pres = null;
                shelf = null;
            }
        }

        @Override public void onDisplayChanged(int id) { }
    };

    private void showShelf() {
        if (pres != null) return;
        DisplayManager dm = getSystemService(DisplayManager.class);
        Display mine = getDisplay();
        Display target = null;
        for (Display d : dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)) {
            if (mine == null || d.getDisplayId() != mine.getDisplayId()) { target = d; break; }
        }
        if (target == null) {
            Toast.makeText(this, t(S.NO_SECOND_SCREEN), Toast.LENGTH_LONG).show();
            return;
        }
        pres = new Presentation(this, target) {
            @Override protected void onCreate(Bundle s) {
                super.onCreate(s);
                shelf = new ShelfView(getContext(), shelfState, icons, MainActivity.this);
                setContentView(shelf);
            }

            // Touching the lower screen moves key focus to this window; route the gamepad back.
            @Override public boolean dispatchKeyEvent(KeyEvent e) {
                return handleKey(e) || super.dispatchKeyEvent(e);
            }

            @Override public boolean dispatchGenericMotionEvent(MotionEvent e) {
                return handleMotion(e) || super.dispatchGenericMotionEvent(e);
            }

            // a touch on the lower screen also ends the screensaver (and is not passed on)
            @Override public boolean dispatchTouchEvent(MotionEvent e) {
                if (noteActivity(e)) return true;
                return super.dispatchTouchEvent(e);
            }
        };
        pres.show();
    }

    /**
     * Scans the game folders in the background. At start-up the library saved after the previous scan is
     * shown first, so the launcher is usable at once; the fresh scan replaces it only if something changed.
     */
    private void load() {
        if (loading) { reloadPending = true; return; }
        loading = true;
        boolean first = lib == null, manual = lib != null && !quietScan;
        quietScan = false;
        if (first) top.showMessage(t(S.LOADING));
        File cacheFile = new File(getFilesDir(), "library.cache");
        loadChosenSystems();
        NameClean.on = new HashSet<>(prefs.getStringSet("nameClean", new HashSet<>()));
        List<String> folders = folders();
        boolean autoFind = prefs.getBoolean("autoFind", true);
        Set<String> apps = new HashSet<>(prefs.getStringSet("apps", new HashSet<>()));
        Library before = lib;
        new Thread(() -> {
            Library prev = before;
            if (first) {
                Library cached = Library.readCache(cacheFile);
                if (cached != null) main.post(() -> { if (lib == null) applyLibrary(cached, false); });
                if (prev == null) prev = cached;
            }
            long t0 = SystemClock.uptimeMillis();
            Library l;
            try {
                l = Library.load(this, folders, autoFind, apps);
            } catch (RuntimeException e) {
                main.post(() -> {
                    loading = false;
                    Toast.makeText(this, I18n.f(S.LAUNCH_FAILED, String.valueOf(e.getMessage())), Toast.LENGTH_LONG).show();
                });
                return;
            }
            lastScanMs = SystemClock.uptimeMillis() - t0;
            lastScanAt = SystemClock.uptimeMillis();
            // right after start-up the SD card may not be mounted yet: keep what we had and look again shortly
            if (prev != null && volumeMissing(prev, l) && cardRetries < 8) {
                cardRetries++;
                main.post(() -> {
                    loading = false;
                    main.postDelayed(() -> { quietScan = true; load(); }, 15_000);
                });
                return;
            }
            cardRetries = 0;
            Library.writeCache(cacheFile, l);
            main.post(() -> {
                loading = false;
                if (lib == null || !lib.signature.equals(l.signature)) applyLibrary(l, lib != null);
                // the guide's "what was found" page waits for the scan
                if (guideStep == G_FOUND && shelfState.menu != null && t(S.GUIDE_FOUND).equals(shelfState.menu.title)) showGuide(G_FOUND);
                int n = 0;
                for (Library.Platform p : l.platforms) n += p.games.size();
                if (n == 0) Toast.makeText(this, t(S.NO_GAMES), Toast.LENGTH_LONG).show();
                else if (manual && !reloadPending) Toast.makeText(this, I18n.f(S.SCAN_DONE, n), Toast.LENGTH_SHORT).show();
                if (reloadPending) { reloadPending = false; load(); } else {
                    prepareSearchIndex(); askNewGenres(); writeCatalog();
                    List<Runnable> then = new ArrayList<>(afterLoad);
                    afterLoad.clear();
                    for (Runnable r : then) r.run();
                }
                checkForUpdate(false, null);
                if (Achievements.configured(prefs)) new Thread(() -> Achievements.refresh(prefs, false), "ra").start();
            });
        }, "library").start();
    }

    /** Puts a library on screen; with {@code keep}, the open platform page stays open. */
    private void applyLibrary(Library l, boolean keep) {
        Library.Platform cur = shelfState.current;
        String curName = keep && cur != null && shelf != null && !shelf.isPseudo(cur) ? cur.name : null;
        int curSel = shelfState.sel;
        lib = l;
        fullLists.clear();
        pinyin.clear();
        shelfState.message = null;
        shelfState.current = null;
        applyHiddenGames();
        applyMerges();
        noteNewGames();
        applyCustomFolders();
        applyFavorites();
        for (Library.Platform p : l.platforms) {
            if (p.isCustom()) continue;
            p.color = prefs.getInt("pcolor:" + p.name, 0);
            p.label = prefs.getString("pname:" + p.name, null);
        }
        if (shelf != null) {
            shelf.setLibrary(l, recentGames());
            if (curName != null) shelf.openByName(curName, curSel);
        } else {
            shelfState.lib = l;
        }
        // not again while the guide or the restore offer is already on screen (a rescan lands here too)
        if (!prefs.contains("lang") && guideStep < 0) showGuide(G_LANG);
    }

    private List<String> folders() {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString("folders", "").split("\n")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private void setFolders(List<String> fs) { prefs.edit().putString("folders", String.join("\n", fs)).apply(); }

    // ---- display mode ----

    private void applyDisplayMode() {
        String mode = prefs.getString("mode", "system");
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        Palette.dark = mode.equals("dark") || (mode.equals("system") && night);
    }

    private void showDisplayModeMenu(ShelfView.Menu parent) {
        String mode = prefs.getString("mode", "system");
        ShelfView.Menu m = new ShelfView.Menu(t(S.DISPLAY_MODE), null);
        m.parent = parent;
        String[] keys = {"system", "light", "dark"};
        S[] labels = {S.MODE_SYSTEM, S.MODE_LIGHT, S.MODE_DARK};
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            m.add(t(labels[i]), k.equals(mode), () -> {
                prefs.edit().putString("mode", k).apply();
                applyDisplayMode();
                onSettings();
                if (shelf != null) shelf.invalidate();
            });
        }
        shelf.showMenu(m);
    }

    // ---- emulator choice ----

    private String effectiveCmd(Library.Game g) {
        String c = prefs.getString("emu:" + g.key(), null);
        if (c != null) return c;
        if (g.launch != null) return g.launch;
        return platformCmd(g.platform);
    }

    private String platformCmd(Library.Platform p) {
        String c = prefs.getString("pemu:" + p.name, null);
        return c != null ? c : p.launch;
    }

    @Override public String chosenCmd(Object item) {
        if (item instanceof Library.Game) return prefs.getString("emu:" + ((Library.Game) item).key(), null);
        if (item instanceof Library.Platform) return prefs.getString("pemu:" + ((Library.Platform) item).name, null);
        return null;
    }

    @Override public void onChoose(Object item, String cmd) {
        String k = item instanceof Library.Game ? "emu:" + ((Library.Game) item).key() : "pemu:" + ((Library.Platform) item).name;
        SharedPreferences.Editor e = prefs.edit();
        if (cmd == null) e.remove(k); else e.putString(k, cmd);
        if (item instanceof Library.Platform) for (Library.Platform src : ((Library.Platform) item).mergedFrom) {
            if (cmd == null) e.remove("pemu:" + src.name); else e.putString("pemu:" + src.name, cmd);
        }
        e.apply();
    }

    // ---- settings ----

    /** The settings page last shown, so a refresh keeps the category and rows where they were. */
    private ShelfView.Menu settingsPage;

    @Override public void onSettings() {
        if (shelf == null) return;
        ShelfView.Menu old = settingsPage;
        ShelfView.Menu page = new ShelfView.Menu(t(S.SETTINGS), "Duo Launcher " + versionName());
        settingsPage = page;
        ShelfView.Menu look = page.tab(t(S.TAB_LOOK), 0xFFE07A3F), games = page.tab(t(S.TAB_LIBRARY), 0xFF2BAE66),
                play = page.tab(t(S.TAB_PLAY), 0xFF6366F1), ai = page.tab(t(S.TAB_AI), 0xFF9B5DE5), data = page.tab(t(S.TAB_DATA), 0xFF14A38B),
                tools = page.tab(t(S.TOOLS), 0xFF2F8BFF), sys = page.tab(t(S.TAB_SYSTEM), 0xFF6D8299);

        // look and sound; Duo Lite first, since it switches several of the rows below off at once
        boolean lite = prefs.getBoolean("lite", false);
        look.toggle(t(S.LITE), lite, () -> {
            prefs.edit().putBoolean("lite", !lite).apply();
            applyLite();
            if (!lite) { stopBanner(); Toast.makeText(this, t(S.LITE_ON), Toast.LENGTH_LONG).show(); }
            if (shelf != null) shelf.invalidate();
            onSettings();
        }).hint(t(S.H_LITE));
        String mode = prefs.getString("mode", "system");
        look.add(t(S.DISPLAY_MODE) + "  ·  " + t(mode.equals("dark") ? S.MODE_DARK : mode.equals("light") ? S.MODE_LIGHT : S.MODE_SYSTEM),
                false, () -> showDisplayModeMenu(look));
        look.add(t(S.APPEARANCE), false, () -> showAppearanceMenu(look));
        File pack = Theme.packDir();
        look.add(t(S.THEME) + "  ·  " + (pack == null ? t(S.DEFAULT) : Theme.packName(pack)), false, () -> showThemeMenu(look));
        int fontRow = look.labels.size();
        look.add(t(S.FONT_SIZE) + "  ·  " + t(TEXT_SIZE_NAMES[prefs.getInt("textScale", 1)]), false, () -> showTextSizeMenu(fontRow));
        int padRow = look.labels.size();
        look.add(tilePadLabel(), false, () -> {
            shelfState.tilePad = (shelfState.tilePad + 1) % 4;
            prefs.edit().putInt("tilePad", shelfState.tilePad).apply();
            look.labels.set(padRow, tilePadLabel());
            shelf.invalidate();
        });
        boolean video = prefs.getBoolean("videoPreview", true);
        // under Duo Lite these read off, and say why instead of switching
        Runnable liteSays = () -> Toast.makeText(this, t(S.LITE_ON), Toast.LENGTH_LONG).show();
        look.toggle(t(S.VIDEO_PREVIEW), video && !lite, lite ? liteSays : () -> {
            prefs.edit().putBoolean("videoPreview", !video).apply();
            applyLite();
            onSettings();
        });
        boolean sounds = prefs.getBoolean("sounds", true);
        look.toggle(t(S.SOUNDS), sounds, () -> {
            prefs.edit().putBoolean("sounds", !sounds).apply();
            sfx.enabled = !sounds;
            onSettings();
        });
        if (sfx.hasMusic()) {
            boolean music = prefs.getBoolean("music", true);
            look.toggle(t(S.MUSIC), music, () -> {
                prefs.edit().putBoolean("music", !music).apply();
                sfx.musicEnabled = !music;
                if (music) sfx.pauseMusic(); else sfx.resumeMusic();
                onSettings();
            });
        }
        boolean banner = prefs.getBoolean("bannerMusic", true);
        look.toggle(t(S.BANNER_MUSIC), banner && !lite, lite ? liteSays : () -> {
            prefs.edit().putBoolean("bannerMusic", !banner).apply();
            if (banner) stopBanner();
            onSettings();
        });
        int saverMin = prefs.getInt("screensaver", 5);
        look.add(t(S.SCREENSAVER) + "  ·  " + (saverMin == 0 ? t(S.OFF) : I18n.f(S.MINUTES, saverMin)), false, () -> {
            ShelfView.Menu sm = new ShelfView.Menu(t(S.SCREENSAVER), null);
            sm.parent = look;
            for (int v : new int[]{0, 1, 3, 5, 10, 20}) {
                sm.add(v == 0 ? t(S.OFF) : I18n.f(S.MINUTES, v), v == saverMin, () -> {
                    prefs.edit().putInt("screensaver", v).apply();
                    onSettings();
                });
            }
            shelf.showMenu(sm);
        });

        // home and library
        games.add(t(S.GAME_FOLDERS), false, () -> showFoldersMenu(games));
        games.add(t(S.CHOOSE_APPS), false, () -> showAppsMenu(games, 0));
        games.add(t(S.HIDDEN_FOLDERS) + (shelfState.hidden.isEmpty() ? "" : "  ·  " + shelfState.hidden.size()), false, () -> showHiddenMenu(games, 0));
        int rulesOn = prefs.getStringSet("nameClean", new HashSet<>()).size();
        games.add(t(S.NAME_CLEAN) + "  ·  " + (rulesOn == 0 ? t(S.OFF) : String.valueOf(rulesOn)), rulesOn > 0,
                () -> showNameCleanMenu(games, 0, new boolean[1]));
        if (!hiddenGames.isEmpty())
            games.add(t(S.HIDDEN_GAMES) + "  ·  " + hiddenGames.size(), false, () -> showHiddenGamesMenu(games, 0));
        boolean badges = prefs.getBoolean("newBadges", true);
        games.toggle(t(S.NEW_BADGES), badges, () -> {
            prefs.edit().putBoolean("newBadges", !badges).apply();
            ShelfView.newBadges = !badges;
            shelf.invalidate();
            onSettings();
        });
        List<Library.Game> freshNow = allNewGames();
        if (badges && !freshNow.isEmpty())
            games.add(I18n.f(S.MARK_ALL_SEEN, freshNow.size()), false, () -> { onMarkSeen(freshNow); onSettings(); });

        // playing
        play.add(t(S.BUTTONS), false, () -> showButtonsMenu(0));
        boolean ra = Achievements.configured(prefs);
        play.add("RetroAchievements  ·  " + (ra ? prefs.getString("ra.user", "") : t(S.OFF)), ra, () -> showRaMenu(play));

        // AI: the two things to do with it, then three doors (services, library jobs, chat history) and the cost
        aiTab = ai;
        ai.add(t(S.AI_CHAT), false, () -> { shelf.closeMenu(); startChat(); });
        ai.add(t(S.AI_PICK), false, this::aiPick);
        int aiOn = (Ai.from(prefs) != null ? 1 : 0) + (AiImage.from(prefs) != null ? 1 : 0) + (Speaker.ready(prefs) ? 1 : 0);
        ai.add(t(S.AI_SERVICES) + "  ·  " + (aiOn == 0 ? t(S.OFF) : aiOn + " / 3"), false, () -> showAiServices(ai, 0)).hint(t(S.H_AI_SERVICES));
        ai.add(t(S.AI_LIBRARY), false, () -> showAiLibrary(0)).hint(t(S.H_AI_LIBRARY));
        int chats = ChatHistory.count(getFilesDir());
        ai.add(t(S.CHAT_HISTORY) + (chats == 0 ? "" : "  ·  " + chats), false, () -> showChatHistory(ai)).hint(t(S.H_AI_CHAT_HISTORY));
        ai.add(t(S.USAGE) + "  ·  " + t(S.USAGE_MONTH) + " " + Usage.money(Usage.since(Usage.monthStart(), null)[4]), false, () -> showUsageMenu(ai));

        // saves, transfer and sharing
        data.add(t(S.WIFI_TRANSFER) + (wifi != null && wifi.running() ? "  ·  " + t(S.ON) : ""), wifi != null && wifi.running(), this::showWifiMenu);
        addToolRows(tools, data, ai, play);
        data.add(t(S.EXPORT_CONFIG), false, () -> showExportMenu(data, java.util.EnumSet.copyOf(Config.SHARE), 0));
        data.add(t(S.IMPORT_CONFIG), false, () -> showImportConfigMenu(data));
        data.add(t(S.DATA_HOME) + "  ·  " + dataHomeName(), false, () -> showDataHome(0)).hint(t(S.H_DATA_HOME));

        // system
        sys.add(t(S.LANGUAGE) + "  ·  " + I18n.name(), false, () -> showLanguageMenu(sys, false));
        sys.add(t(S.SET_HOME), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
        });
        sys.add(t(S.EXIT_TO_DESKTOP), false, this::onOpenDesktop);
        sys.add(t(S.ANDROID_SETTINGS), false, () -> {
            shelf.closeMenu();
            try { startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception ignored) { }
        });
        sys.add(t(S.PERMISSIONS) + (hasUsageAccess() ? "" : "  ·  !"), false, () -> showPermissionsMenu(sys));
        boolean tips = prefs.getBoolean("tips", true);
        sys.toggle(t(S.TIPS), tips, () -> {
            prefs.edit().putBoolean("tips", !tips).apply();
            onSettings();
        }).hint(t(S.H_TIPS));
        if (tips && anyTipSeen()) sys.add(t(S.TIPS_AGAIN), false, () -> {
            SharedPreferences.Editor e = prefs.edit();
            for (String k : prefs.getAll().keySet()) if (k.startsWith("tip.")) e.remove(k);
            e.apply();
            Toast.makeText(this, t(S.TIPS_RESET), Toast.LENGTH_SHORT).show();
            onSettings();
        });
        sys.add(t(S.GUIDE_AGAIN), false, () -> showGuide(G_LANG)).hint(t(S.H_GUIDE));
        String newer = newerVersion();
        sys.add(t(S.ABOUT) + "  ·  Duo Launcher " + versionName() + (newer != null ? "  ·  " + I18n.f(S.UPDATE_AVAILABLE, newer) : ""),
                newer != null, this::showUpdateMenu);

        // the same place as before a refresh; a fresh page opens on its first category
        for (int i = 0; i < page.tabs.size(); i++) {
            ShelfView.Menu tb = page.tabs.get(i);
            tb.sel = old != null && i < old.tabs.size() ? Math.min(old.tabs.get(i).sel, Math.max(0, tb.labels.size() - 1)) : 0;
            tb.top = old != null && i < old.tabs.size() ? Math.min(old.tabs.get(i).top, tb.sel) : 0;
        }
        if (old != null) { page.tab = Math.min(old.tab, page.tabs.size() - 1); page.inRows = old.inRows; }
        if (top != null) top.showMessage(settingsStatus());
        shelf.showMenu(page);
    }

    /** What the upper screen shows with Settings open: version, free space, AI and Wi-Fi. */
    private String settingsStatus() {
        StringBuilder b = new StringBuilder("Duo Launcher " + versionName());
        java.util.Set<String> vols = new java.util.LinkedHashSet<>();
        if (lib != null) for (Library.Game g : lib.byKey.values()) if (g.pkg == null && g.path != null) vols.add(volumeOf(g.path));
        for (String v : vols) {
            try {
                android.os.StatFs fs = new android.os.StatFs(v);
                b.append("\n").append(I18n.f(S.STORAGE_FREE, v.startsWith("/storage/emulated") ? t(S.INTERNAL_STORAGE) : t(S.CARD_STORAGE),
                        gb(fs.getAvailableBytes()), gb(fs.getTotalBytes())));
            } catch (Exception ignored) {
            }
        }
        Ai aiNow = Ai.from(prefs);
        b.append("\n").append(t(S.AI_TRANSLATE)).append("  ·  ").append(aiNow == null ? t(S.OFF) : Ai.preset(aiNow.provider)[1] == null ? t(S.CUSTOM) : Ai.preset(aiNow.provider)[1]);
        b.append("\n").append(t(S.TTS_REPLY)).append("  ·  ").append(ttsName(Speaker.provider(prefs)));
        if (wifi != null && wifi.running() && WifiServer.address() != null) b.append("\n").append(t(S.WIFI_TRANSFER)).append("  ·  ").append(WifiServer.address());
        return b.toString();
    }

    /** Settings → Import a config: the configs found in DuoLauncher/exports and Download, newest first. */
    private void showImportConfigMenu(ShelfView.Menu parent) {
        List<File> dirs = new ArrayList<>();
        dirs.add(new File(Backups.folder(volumeRoots()).getParentFile(), "exports"));
        dirs.add(new File(android.os.Environment.getExternalStorageDirectory(), "Download"));
        for (File v : volumeRoots()) dirs.add(new File(v, "DuoLauncher/exports"));
        ShelfView.Menu m = new ShelfView.Menu(t(S.IMPORT_CONFIG), t(S.IMPORT_CONFIG_HINT));
        m.parent = parent;
        List<File> found = Config.find(dirs);
        if (found.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        for (File z : found) m.add(z.getName(), false, () -> {
            java.util.Set<Config.Part> has = Config.partsIn(z);
            showImportPartsMenu(m, z, has, java.util.EnumSet.copyOf(has.isEmpty() ? Config.SHARE : has), 0);
        });
        shelf.showMenu(m);
    }

    // ---- what goes into an export, and what is taken from an import ----

    private static final Config.Part[] PARTS = Config.Part.values();
    private static final S[] PART_NAMES = {S.PART_SETTINGS, S.PART_PLAY, S.PART_NOTES, S.PART_KEYS, S.PART_AI, S.PART_CARD};

    /** The DuoLauncher folders on the storages (where the game data and exports live). */
    private List<File> launcherDirs() {
        List<File> out = new ArrayList<>();
        for (File v : volumeRoots()) {
            File d = new File(v, "DuoLauncher");
            if (d.isDirectory()) out.add(d);
        }
        return out;
    }

    /** One row per part, ticked or not; a tick toggles it and the menu is shown again at the same row. */
    private void addPartRows(ShelfView.Menu m, java.util.Set<Config.Part> have, java.util.Set<Config.Part> sel, String cardSize,
                             java.util.function.IntConsumer reshow) {
        for (Config.Part pt : PARTS) {
            if (have != null && !have.contains(pt)) continue;
            String label = t(PART_NAMES[pt.ordinal()]) + (pt == Config.Part.CARD && cardSize != null ? "  ·  " + cardSize : "");
            int row = m.labels.size();
            // drawn like the cheat picker, so an unticked row still reads as something to tick
            m.add((sel.contains(pt) ? "☑  " : "☐  ") + label, false, () -> {
                if (!sel.remove(pt)) sel.add(pt);
                reshow.accept(row);
            });
        }
    }

    private String cardSize;

    /** Settings → Export my config: pick what goes in ("share with others", "full backup" or any mix), then export. */
    private void showExportMenu(ShelfView.Menu parent, java.util.Set<Config.Part> sel, int row) {
        if (shelf == null) return;
        if (cardSize == null) {   // the size of the game data, worked out once in the background
            cardSize = "…";
            new Thread(() -> {
                long n = 0;
                for (File d : Config.gameData(launcherDirs())) n += Config.size(d);
                String v = n >= 1L << 30 ? String.format(Locale.ROOT, "%.1f GB", n / (double) (1L << 30)) : (n >> 20) + " MB";
                main.post(() -> {
                    cardSize = v;
                    if (shelfState.menu != null && t(S.EXPORT_WHAT).equals(shelfState.menu.title)) showExportMenu(parent, sel, shelfState.menu.sel);
                });
            }, "game-data-size").start();
        }
        ShelfView.Menu m = new ShelfView.Menu(t(S.EXPORT_WHAT), sel.contains(Config.Part.KEYS) ? t(S.KEYS_WARNING) : t(S.EXPORT_WHAT_HINT));
        m.parent = parent;
        m.add(t(S.PRESET_SHARE), sel.equals(Config.SHARE), () -> { sel.clear(); sel.addAll(Config.SHARE); showExportMenu(parent, sel, 0); });
        m.add(t(S.PRESET_FULL), sel.equals(Config.FULL), () -> { sel.clear(); sel.addAll(Config.FULL); showExportMenu(parent, sel, 1); });
        addPartRows(m, null, sel, cardSize, r -> showExportMenu(parent, sel, r));
        m.add(t(S.SELECT_ALL), false, () -> { sel.addAll(Config.FULL); showExportMenu(parent, sel, 2 + PARTS.length); });
        m.add(t(S.SELECT_NONE), false, () -> { sel.clear(); showExportMenu(parent, sel, 3 + PARTS.length); });
        m.add("↓  " + t(S.START_EXPORT), false, () -> {
            if (sel.isEmpty()) { Toast.makeText(this, t(S.NOTHING_PICKED), Toast.LENGTH_SHORT).show(); return; }
            java.util.Set<Config.Part> parts = java.util.EnumSet.copyOf(sel);
            List<File> dirs = launcherDirs();
            File files = getFilesDir();
            shelf.closeMenu();
            runInBackground(() -> {
                File out = Config.export(prefs, Theme.dir, files, dirs, parts, new File(Backups.folder(volumeRoots()).getParentFile(), "exports"));
                return () -> Toast.makeText(this, I18n.f(S.EXPORTED, out.getAbsolutePath()), Toast.LENGTH_LONG).show();
            });
        });
        m.sel = Math.min(row, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    /** Import: the parts this config holds, all ticked to begin with; untick what should stay as it is. */
    private void showImportPartsMenu(ShelfView.Menu parent, File z, java.util.Set<Config.Part> has, java.util.Set<Config.Part> sel, int row) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.IMPORT_WHAT), z.getName() + "  ·  " + t(S.IMPORT_WHAT_HINT));
        m.parent = parent;
        addPartRows(m, has, sel, null, r -> showImportPartsMenu(parent, z, has, sel, r));
        m.add("↑  " + t(S.START_IMPORT), false, () -> {
            if (sel.isEmpty()) { Toast.makeText(this, t(S.NOTHING_PICKED), Toast.LENGTH_SHORT).show(); return; }
            java.util.Set<Config.Part> parts = java.util.EnumSet.copyOf(sel);
            File cardRoot = Backups.folder(volumeRoots()).getParentFile();
            File files = getFilesDir();
            shelf.closeMenu();
            runInBackground(() -> {
                stopSpeaking();
                int n = Config.apply(z, prefs, Theme.dir, files, cardRoot, parts);
                // a restore without the settings must not be offered again after the restart
                if (!prefs.contains("lang")) prefs.edit().putString("lang", I18n.code()).commit();
                Usage.reload(this);
                // the scan is done again, so pictures, names and dates just taken over are used
                if (parts.contains(Config.Part.CARD) || parts.contains(Config.Part.PLAY)) new File(files, "library.cache").delete();
                return () -> {
                    Toast.makeText(this, I18n.f(S.CONFIG_IMPORTED, n), Toast.LENGTH_LONG).show();
                    settingsPage = null;
                    recreate();   // the look, home and language all come from the settings just taken over
                };
            });
        });
        m.add(t(S.CANCEL), false, () -> shelf.showMenu(parent));
        m.sel = Math.min(row, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    // ---- new versions: the latest GitHub release, looked up at most once a day; downloads stay on GitHub ----

    private static final String RELEASES_API = "https://api.github.com/repos/GenKnot/DuoLauncher/releases/latest";
    static final String RELEASES_PAGE = "https://github.com/GenKnot/DuoLauncher/releases/latest";

    /** "1.2.0" when the last check found a release newer than this build, else null. */
    private String newerVersion() {
        String tag = prefs.getString("update.tag", "");
        return !tag.isEmpty() && compareVersions(tag, versionName()) > 0 ? tag : null;
    }

    static int compareVersions(String a, String b) {
        String[] x = a.replaceAll("[^0-9.]", "").split("\\."), y = b.replaceAll("[^0-9.]", "").split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length && !x[i].isEmpty() ? Integer.parseInt(x[i]) : 0, q = i < y.length && !y[i].isEmpty() ? Integer.parseInt(y[i]) : 0;
            if (p != q) return Integer.compare(p, q);
        }
        return 0;
    }

    /** Looks up the latest release in the background (forced, or when the last look is a day old). */
    private void checkForUpdate(boolean force, Runnable then) {
        if (!force && System.currentTimeMillis() - prefs.getLong("update.checked", 0) < 24L * 3600 * 1000) return;
        new Thread(() -> {
            String tag = null;
            java.net.HttpURLConnection con = null;
            try {
                con = (java.net.HttpURLConnection) new java.net.URL(RELEASES_API).openConnection();
                con.setConnectTimeout(10000);
                con.setReadTimeout(15000);
                con.setRequestProperty("Accept", "application/vnd.github+json");
                if (con.getResponseCode() == 200) {
                    java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                    try (java.io.InputStream in = con.getInputStream()) { Backups.copy(in, b); }
                    tag = new org.json.JSONObject(b.toString("UTF-8")).optString("tag_name", "").replaceFirst("^[vV]", "");
                }
            } catch (Exception ignored) {
            } finally {
                if (con != null) con.disconnect();
            }
            String found = tag;
            main.post(() -> {
                if (found != null && !found.isEmpty())
                    prefs.edit().putString("update.tag", found).putLong("update.checked", System.currentTimeMillis()).apply();
                if (then != null) then.run();
            });
        }, "update-check").start();
    }

    private void showUpdateMenu() {
        String newer = newerVersion();
        ShelfView.Menu m = new ShelfView.Menu("Duo Launcher " + versionName(), newer != null ? I18n.f(S.UPDATE_AVAILABLE, newer) : null);
        m.parent = shelfState.menu;
        if (newer != null) m.add("⇣ " + I18n.f(S.DOWNLOAD_VERSION, newer), false, this::openReleasePage);
        m.add(t(S.CHECK_UPDATES), false, () -> checkForUpdate(true, () -> {
            String n = newerVersion();
            Toast.makeText(this, n != null ? I18n.f(S.UPDATE_AVAILABLE, n) : t(S.UP_TO_DATE), Toast.LENGTH_LONG).show();
            if (shelf != null) { shelf.closeMenu(); onSettings(); }
        }));
        m.add("github.com/GenKnot/DuoLauncher", false, this::openReleasePage);
        shelf.showMenu(m);
    }

    private void openReleasePage() {
        shelf.closeMenu();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Toast.makeText(this, RELEASES_PAGE, Toast.LENGTH_LONG).show();
        }
    }

    // ---- the system's own home screen, for when this launcher is the default one ----

    @Override public boolean isDefaultHome() {
        android.content.pm.ResolveInfo ri = getPackageManager().resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
        return ri != null && ri.activityInfo != null && getPackageName().equals(ri.activityInfo.packageName);
    }

    /**
     * Asks before leaving for the Android home screen. While this is the default home app the system
     * sends any home request straight back here, so the way out is the system's default-home page.
     */
    @Override public void onOpenDesktop() {
        if (shelf == null) return;
        boolean ours = isDefaultHome();
        ShelfView.Menu m = new ShelfView.Menu(t(S.DESKTOP_ASK), null);
        m.add((ours ? t(S.GO) + "  ·  " + t(S.DESKTOP_HOW) : t(S.GO)), false, () -> {
            shelf.closeMenu();
            if (ours) {
                try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
            } else {
                moveTaskToBack(true);
            }
        });
        m.add(t(S.CANCEL), false, () -> shelf.closeMenu());
        shelf.showMenu(m);
    }

    // ---- editing the built-in home tiles: prefs "tool.<id>.name" / "tool.<id>.color", hidden as "tool:<id>" ----

    private void loadToolLooks() {
        ShelfView.TOOL_NAMES.clear();
        ShelfView.TOOL_COLORS.clear();
        for (String id : new String[]{"continue", "recent", "favorites", "add", "search", "stats", "storage", "random", "settings", "desktop"}) {
            String n = prefs.getString("tool." + id + ".name", null);
            if (n != null) ShelfView.TOOL_NAMES.put(id, n);
            if (prefs.contains("tool." + id + ".color")) ShelfView.TOOL_COLORS.put(id, prefs.getInt("tool." + id + ".color", 0));
        }
    }

    @Override public void onEditTool(String id, String label, int color) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(label, null);
        if (id.equals("random")) {
            m.add(t(S.AI_PICK), false, this::aiPick);
            m.add(t(S.AI_CHAT), false, this::startChat);
        }
        // the tile's icon: the user's own picture or one drawn by the image service, instead of the bundled one
        Library.Platform tp = new Library.Platform();
        tp.name = "tool-" + id;
        tp.system = "CUSTOM";
        tp.label = label;
        Runnable iconChanged = () -> { Theme.forgetPlatform(tp.name); if (shelf != null) { shelf.closeMenu(); shelf.refresh(); } };
        m.add(t(S.ICON) + "  ·  " + t(S.AI_MAKE), false, () -> { shelf.closeMenu(); aiPicture(tp, Theme.CONSOLE, iconChanged); });
        m.add(t(S.ICON) + "  ·  " + t(S.PICK_IMAGE), false, () -> pickImage(tp, Theme.CONSOLE));
        if (Theme.isCustom(tp.name, Theme.CONSOLE)) m.add(t(S.ICON) + "  ·  " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(tp.name, Theme.CONSOLE);
            iconChanged.run();
        });
        // and its backdrop on the upper screen
        m.add(t(S.BACKGROUND_LIGHT) + "  ·  " + t(S.AI_MAKE), false, () -> { shelf.closeMenu(); aiPicture(tp, Theme.BACKGROUND, iconChanged); });
        m.add(t(S.BACKGROUND_LIGHT) + "  ·  " + t(S.PICK_IMAGE), false, () -> pickImage(tp, Theme.BACKGROUND));
        if (Theme.isCustom(tp.name, Theme.BACKGROUND)) m.add(t(S.BACKGROUND_LIGHT) + "  ·  " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(tp.name, Theme.BACKGROUND);
            iconChanged.run();
        });
        m.add(t(S.BACKGROUND_DARK) + "  ·  " + t(S.AI_FROM_LIGHT), false, () -> {
            shelf.closeMenu();
            aiDarkCut(tp.name, Theme.lightBackdrop("tool:" + id, id), iconChanged);
        });
        if (Theme.isCustom(tp.name, Theme.BACKGROUND_DARK)) m.add(t(S.BACKGROUND_DARK) + "  ·  " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(tp.name, Theme.BACKGROUND_DARK);
            iconChanged.run();
        });
        m.add(t(S.RENAME), false, () -> askText(t(S.RENAME), label, name -> {
            prefs.edit().putString("tool." + id + ".name", name).apply();
            loadToolLooks();
            shelf.closeMenu();
            shelf.refresh();
        }));
        if (prefs.contains("tool." + id + ".name")) m.add(t(S.RENAME) + "  ·  " + t(S.RESET_DEFAULT), false, () -> {
            prefs.edit().remove("tool." + id + ".name").apply();
            loadToolLooks();
            shelf.closeMenu();
            shelf.refresh();
        });
        m.add(t(S.COLOR), false, color, null, () -> {
            ShelfView.Menu cm = new ShelfView.Menu(t(S.COLOR), label);
            cm.parent = shelfState.menu;
            cm.add(t(S.RESET_DEFAULT), !prefs.contains("tool." + id + ".color"), () -> {
                prefs.edit().remove("tool." + id + ".color").apply();
                loadToolLooks();
                shelf.closeMenu();
                shelf.refresh();
            });
            for (int c : Theme.SWATCHES) cm.add(String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF), false, c, null, () -> {
                prefs.edit().putInt("tool." + id + ".color", c).apply();
                loadToolLooks();
                shelf.closeMenu();
                shelf.refresh();
            });
            shelf.showMenu(cm);
        });
        m.add(t(S.HIDE_TILE) + (id.equals("settings") ? "  ·  " + t(S.START_SETTINGS_HINT) : ""), false, () -> {
            shelf.closeMenu();
            shelfState.hidden.add("tool:" + id);
            prefs.edit().putStringSet("hidden", new HashSet<>(shelfState.hidden)).apply();
            shelf.refresh();
        });
        shelf.showMenu(m);
    }

    @Override public void onRenamePlatform(Library.Platform p) {
        askText(t(S.RENAME), p.title(), name -> {
            prefs.edit().putString("pname:" + p.name, name).apply();
            p.label = name;
            if (shelf != null) { shelf.closeMenu(); shelf.refresh(); }
        });
    }

    private static final S[] TEXT_SIZE_NAMES = {S.ZOOM_S, S.ZOOM_M, S.ZOOM_L, S.SIZE_XL};

    /** "Cover margin · Fill" and so on: A on the row steps through fill, small, medium and large. */
    private String tilePadLabel() {
        S[] names = {S.TILE_FILL, S.ZOOM_S, S.ZOOM_M, S.ZOOM_L};
        return t(S.TILE_PAD) + "  ·  " + t(names[Math.max(0, Math.min(3, shelfState.tilePad))]);
    }

    private void applyTextScale() {
        int i = Math.max(0, Math.min(Palette.TEXT_SCALES.length - 1, prefs.getInt("textScale", 1)));
        Palette.textScale = Palette.TEXT_SCALES[i];
    }

    /** Text size for both screens; the change shows at once so the sizes can be compared. */
    private void showTextSizeMenu(int settingsRow) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.FONT_SIZE), null);
        int cur = prefs.getInt("textScale", 1);
        for (int i = 0; i < TEXT_SIZE_NAMES.length; i++) {
            int idx = i;
            m.add(t(TEXT_SIZE_NAMES[i]), i == cur, () -> {
                prefs.edit().putInt("textScale", idx).apply();
                applyTextScale();
                if (top != null) top.requestLayout();
                reopenSettingsAt(settingsRow);
                showTextSizeMenu(settingsRow);
            });
        }
        m.parent = shelfState.menu;
        shelf.showMenu(m);
    }

    // ---- AI translation: service, key, model; the key can also be pasted on the Wi-Fi page ----

    /** The AI category of the settings page last built, so its door menus can be shown on top of it. */
    private ShelfView.Menu aiTab;

    /** Settings → AI → AI services and keys: translation, pictures and voice, each saying whether it is set up. */
    private void showAiServices(ShelfView.Menu parent, int sel) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_SERVICES), null);
        m.parent = parent;
        // from Settings it comes back over the rebuilt page, so it takes the new AI category as its parent
        m.reopen = () -> showAiServices(parent != null && parent.page != null ? aiTab : parent, m.sel);
        Ai aiNow = Ai.from(prefs);
        m.add(t(S.AI_TRANSLATE) + "  ·  " + (aiNow == null ? t(S.OFF) : Ai.preset(aiNow.provider)[1] == null ? t(S.CUSTOM) : Ai.preset(aiNow.provider)[1]),
                aiNow != null, () -> showAiMenu(m, 0)).hint(t(S.AI_HINT));
        AiImage imgNow = AiImage.from(prefs);
        m.add(t(S.AI_IMAGES) + "  ·  " + (imgNow == null ? t(S.OFF) : imgNow.model.isEmpty() ? AiImage.DEFAULT_MODEL : imgNow.model),
                imgNow != null, () -> showImageAiMenu(m, 0)).hint(t(S.AI_IMAGES_HINT));
        m.add(t(S.TTS_REPLY) + "  ·  " + ttsName(Speaker.provider(prefs)), Speaker.ready(prefs), () -> showVoiceMenu(m, 0)).hint(t(S.TTS_HINT));
        m.add(t(S.AI_ON_PHONE), false, this::showWifiMenu);
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** Settings → AI → Let AI tidy the library: the jobs that go through the whole library. */
    private void showAiLibrary(int sel) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_LIBRARY), null);
        m.parent = aiTab;
        m.add(t(S.AI_GAME_TEXT), false, this::showGameTextMenu);
        m.add(t(S.AI_GENRES), false, this::showGenresMenu);
        m.add(t(S.AI_SERIES), false, this::showSeriesMenu);
        m.add(t(S.AI_GENRE_ICONS), false, this::confirmGenreIcons);
        if (imageJob != null) m.add(t(S.AI_PICTURES_STOP), false, () -> { imageJob.set(true); shelf.closeMenu(); });
        else m.add(t(S.AI_PICTURES), false, this::showAiPicturesMenu);
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** B from a settings submenu: Settings with fresh labels, and the door menu it was opened from, if any. */
    private void backToSettings(ShelfView.Menu parent) {
        shelf.closeMenu();
        if (parent == null || parent.reopen == null) { onSettings(); return; }
        // a door menu: Settings underneath again when it was opened from there, then the door itself
        if (parent.parent != null && (parent.parent.page != null || parent.parent.isPage())) onSettings();
        parent.reopen.run();
    }

    private void showAiMenu(ShelfView.Menu parent, int sel) {
        String provider = prefs.getString("ai.provider", "deepseek");
        String[] p = Ai.preset(provider);
        String key = prefs.getString("ai.key", "");
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_TRANSLATE), t(S.AI_HINT));
        m.parent = parent;
        m.onBack = () -> backToSettings(parent);
        m.add(t(S.AI_SERVICE) + "  ·  " + (p[1] == null ? t(S.CUSTOM) : p[1]), false, () -> {
            ShelfView.Menu pm = new ShelfView.Menu(t(S.AI_SERVICE), null);
            pm.parent = m;
            for (String[] q : Ai.PROVIDERS) pm.add(q[1] == null ? t(S.CUSTOM) : q[1], q[0].equals(provider), () -> {
                // a new service starts from its own address and model
                prefs.edit().putString("ai.provider", q[0]).remove("ai.url").remove("ai.model").apply();
                showAiMenu(parent, 0);
            });
            shelf.showMenu(pm);
        });
        m.add(t(S.API_KEY) + "  ·  " + (key.isEmpty() ? t(S.NOT_SET) : Ai.masked(key)), !key.isEmpty(),
                () -> askText(t(S.API_KEY), "", k -> { prefs.edit().putString("ai.key", k.trim()).apply(); showAiMenu(parent, 1); }));
        String model = prefs.getString("ai.model", "");
        m.add(t(S.AI_MODEL) + "  ·  " + (model.isEmpty() ? p[3] : model), false,
                () -> askText(t(S.AI_MODEL), model.isEmpty() ? p[3] : model, v -> { prefs.edit().putString("ai.model", v.trim()).apply(); showAiMenu(parent, 2); }));
        String url = prefs.getString("ai.url", "");
        m.add(t(S.AI_URL) + "  ·  " + (url.isEmpty() ? (p[2].isEmpty() ? t(S.NOT_SET) : p[2]) : url), false,
                () -> askText(t(S.AI_URL), url.isEmpty() ? p[2] : url, v -> { prefs.edit().putString("ai.url", v.trim()).apply(); showAiMenu(parent, 3); }));
        m.add(t(S.AI_ON_PHONE), false, this::showWifiMenu);
        m.add(t(S.AI_TEST), false, () -> {
            Ai ai = Ai.from(prefs);
            if (ai == null) { Toast.makeText(this, t(S.API_KEY) + " · " + t(S.NOT_SET), Toast.LENGTH_LONG).show(); return; }
            String lang = Translate.target() != null ? Translate.target() : "zh-CN";
            runInBackground(() -> {
                List<String> r = ai.translate(java.util.Arrays.asList("Infinite Health", "Max Money"), lang, null);
                return () -> Toast.makeText(this, r != null ? I18n.f(S.AI_OK, r.get(0) + " · " + r.get(1)) : I18n.f(S.AI_FAILED, ai.error),
                        Toast.LENGTH_LONG).show();
            });
        });
        if (!key.isEmpty()) m.add(t(S.AI_OFF), false, () -> { prefs.edit().remove("ai.key").apply(); showAiMenu(parent, 0); });
        m.sel = Math.min(sel, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    private void showImageAiMenu(ShelfView.Menu parent, int sel) {
        String key = prefs.getString("img.key", "");
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_IMAGES), t(S.AI_IMAGES_HINT));
        m.parent = parent;
        m.onBack = () -> backToSettings(parent);
        m.add(t(S.API_KEY) + "  ·  " + (key.isEmpty() ? t(S.NOT_SET) : Ai.masked(key)), !key.isEmpty(),
                () -> askText(t(S.API_KEY), "", k -> { prefs.edit().putString("img.key", k.trim()).apply(); showImageAiMenu(parent, 0); }));
        String model = prefs.getString("img.model", "");
        m.add(t(S.AI_MODEL) + "  ·  " + (model.isEmpty() ? AiImage.DEFAULT_MODEL : model), false,
                () -> askText(t(S.AI_MODEL), model.isEmpty() ? AiImage.DEFAULT_MODEL : model, v -> { prefs.edit().putString("img.model", v.trim()).apply(); showImageAiMenu(parent, 1); }));
        String url = prefs.getString("img.url", "");
        m.add(t(S.AI_URL) + "  ·  " + (url.isEmpty() ? AiImage.DEFAULT_URL : url), false,
                () -> askText(t(S.AI_URL), url.isEmpty() ? AiImage.DEFAULT_URL : url, v -> { prefs.edit().putString("img.url", v.trim()).apply(); showImageAiMenu(parent, 2); }));
        m.add(t(S.AI_ON_PHONE), false, this::showWifiMenu);
        if (!key.isEmpty()) m.add(t(S.REMOVE_KEY), false, () -> { prefs.edit().remove("img.key").apply(); showImageAiMenu(parent, 0); });
        m.sel = Math.min(sel, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    private static String ttsName(String provider) {
        switch (provider) {
            case Speaker.SONIOX: return "Soniox";
            case Speaker.OPENAI: return "OpenAI";
            default: return t(S.OFF);
        }
    }

    /** Settings → AI voice replies: off, Soniox (its own key) or OpenAI (the image service's key), the voice, a test. */
    private void showVoiceMenu(ShelfView.Menu parent, int sel) {
        String prov = Speaker.provider(prefs), key = prefs.getString("soniox.key", "");
        ShelfView.Menu m = new ShelfView.Menu(t(S.TTS_REPLY), t(S.TTS_HINT));
        m.parent = parent;
        m.onBack = () -> backToSettings(parent);
        for (String p : new String[]{Speaker.OFF, Speaker.SONIOX, Speaker.OPENAI}) {
            int row = m.labels.size();
            m.add(ttsName(p), p.equals(prov), () -> { prefs.edit().putString("tts.provider", p).apply(); showVoiceMenu(parent, row); });
        }
        int keyRow = m.labels.size();
        m.add(t(S.SONIOX_KEY) + "  ·  " + (key.isEmpty() ? t(S.NOT_SET) : Ai.masked(key)), !key.isEmpty(),
                () -> askText(t(S.SONIOX_KEY), "", k -> { prefs.edit().putString("soniox.key", k.trim()).apply(); showVoiceMenu(parent, keyRow); }));
        if (prov.equals(Speaker.OPENAI) && AiImage.from(prefs) == null) m.add(t(S.TTS_OPENAI_KEY), false, () -> showImageAiMenu(parent, 0));
        if (!prov.equals(Speaker.OFF)) {
            String vk = "tts.voice." + prov, def = prov.equals(Speaker.SONIOX) ? Speaker.SONIOX_VOICE : Speaker.OPENAI_VOICE;
            String voiceName = prefs.getString(vk, def);
            int voiceRow = m.labels.size();
            m.add(t(S.TTS_VOICE) + "  ·  " + voiceName, false, () -> askText(t(S.TTS_VOICE), voiceName, v -> {
                if (v.trim().isEmpty()) prefs.edit().remove(vk).apply();
                else prefs.edit().putString(vk, v.trim()).apply();
                showVoiceMenu(parent, voiceRow);
            }));
            int paceRow = m.labels.size();
            S[] paces = {S.SPEED_NORMAL, S.SPEED_FAST, S.SPEED_FASTER};
            m.add(t(S.TTS_SPEED) + "  ·  " + t(paces[Speaker.pace(prefs)]), false, () -> {
                prefs.edit().putInt("tts.pace", (Speaker.pace(prefs) + 1) % 3).apply();
                showVoiceMenu(parent, paceRow);
            });
            m.add(t(S.TTS_TEST), false, () -> {
                if (!Speaker.ready(prefs)) { Toast.makeText(this, t(S.TTS_NEEDS_KEY), Toast.LENGTH_LONG).show(); return; }
                speaker().say(t(S.TTS_TEST_TEXT), I18n.code(), err -> Toast.makeText(this, I18n.f(S.TTS_FAILED, String.valueOf(err)), Toast.LENGTH_LONG).show());
            });
        }
        m.add(t(S.AI_ON_PHONE), false, this::showWifiMenu);
        if (!key.isEmpty()) m.add(t(S.REMOVE_KEY), false, () -> { prefs.edit().remove("soniox.key").apply(); showVoiceMenu(parent, 0); });
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** Settings → AI usage: requests, tokens, pictures, minutes of speech and an estimated cost, by period and service. */
    private void showUsageMenu(ShelfView.Menu parent) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.USAGE), t(S.USAGE_HINT));
        m.parent = parent;
        String[][] periods = {{Usage.today(), t(S.USAGE_TODAY)}, {Usage.weekStart(), t(S.USAGE_WEEK)}, {Usage.monthStart(), t(S.USAGE_MONTH)}, {null, t(S.USAGE_ALL)}};
        for (String[] pr : periods) {
            double[] a = Usage.since(pr[0], null);
            m.add(pr[1] + "  ·  " + I18n.f(S.USAGE_CALLS, (int) a[0]) + "  ·  ≈ " + Usage.money(a[4]), false, null);
        }
        String month = Usage.monthStart();
        double[] tx = Usage.since(month, Usage.TEXT), im = Usage.since(month, Usage.IMAGE), st = Usage.since(month, Usage.STT), ts = Usage.since(month, Usage.TTS);
        m.add(t(S.USAGE_TEXT) + "  ·  " + I18n.f(S.USAGE_CALLS, (int) tx[0]) + " · " + tokens(tx[1] + tx[2]) + " tokens · ≈ " + Usage.money(tx[4]), false, null);
        m.add(t(S.USAGE_IMAGE) + "  ·  " + I18n.f(S.USAGE_PICTURES, (int) im[0]) + " · ≈ " + Usage.money(im[4]), false, null);
        m.add(t(S.USAGE_STT) + "  ·  " + I18n.f(S.USAGE_MIN, String.format(Locale.ROOT, "%.1f", st[3] / 60)) + " · ≈ " + Usage.money(st[4]), false, null);
        m.add(t(S.USAGE_TTS) + "  ·  " + I18n.f(S.USAGE_MIN, String.format(Locale.ROOT, "%.1f", ts[3] / 60)) + " · ≈ " + Usage.money(ts[4]), false, null);
        // the prices used, each the player's own or the published one for the service in use
        Ai aiNow = Ai.from(prefs);
        String[][] rows = {
                {Usage.TEXT, t(S.PRICE_TEXT), aiNow == null ? null : aiNow.model},
                {Usage.IMAGE, t(S.PRICE_IMAGE), null},
                {Usage.STT, t(S.PRICE_STT), null},
                {Usage.TTS, t(S.PRICE_TTS), Speaker.provider(prefs)}};
        for (String[] r : rows) {
            double[] p = Usage.price(r[0], r[2]);
            String shown = p == null ? t(S.NOT_SET) : p.length > 1 ? "$" + num(p[0]) + " / $" + num(p[1]) : "$" + num(p[0]);
            boolean own = Usage.own(r[0]) != null;
            int row = m.labels.size();
            m.add(r[1] + "  ·  " + shown + (own ? "" : "  (" + t(S.DEFAULT) + ")"), own, () -> askText(r[1],
                    p == null ? "" : p.length > 1 ? num(p[0]) + "/" + num(p[1]) : num(p[0]), v -> {
                        if (v.trim().isEmpty() || v.trim().equalsIgnoreCase("default")) prefs.edit().remove("price." + r[0]).apply();
                        else prefs.edit().putString("price." + r[0], v.trim()).apply();
                        showUsageMenu(parent);
                    }));
        }
        m.add(t(S.PRICE_RESET), false, () -> {
            prefs.edit().remove("price.text").remove("price.image").remove("price.stt").remove("price.tts").apply();
            showUsageMenu(parent);
        });
        m.add(t(S.USAGE_RESET), false, () -> {
            ShelfView.Menu sure = new ShelfView.Menu(t(S.USAGE_RESET), null);
            sure.parent = m;
            sure.add(t(S.GO), false, () -> { Usage.clear(); showUsageMenu(parent); });
            sure.add(t(S.CANCEL), false, () -> shelf.showMenu(m));
            shelf.showMenu(sure);
        });
        m.sel = 2;
        shelf.showMenu(m);
    }

    private static String num(double v) {
        String s = String.format(Locale.ROOT, "%.4f", v).replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    private static String tokens(double n) {
        return n >= 1_000_000 ? String.format(Locale.ROOT, "%.1fM", n / 1e6) : n >= 1000 ? String.format(Locale.ROOT, "%.1fk", n / 1e3) : String.valueOf((long) n);
    }

    private Speaker speaker;

    private Speaker speaker() {
        if (speaker == null) speaker = new Speaker(getApplicationContext(), prefs);
        return speaker;
    }

    private void stopSpeaking() {
        if (speaker != null) speaker.stop();
    }

    private void showRaMenu(ShelfView.Menu parent) {
        String user = prefs.getString("ra.user", ""), key = prefs.getString("ra.key", "");
        ShelfView.Menu m = new ShelfView.Menu("RetroAchievements", t(S.RA_HINT));
        m.parent = parent;
        m.onBack = () -> backToSettings(parent);
        m.add(t(S.RA_USER) + "  ·  " + (user.isEmpty() ? t(S.NOT_SET) : user), !user.isEmpty(),
                () -> askText(t(S.RA_USER), user, v -> { prefs.edit().putString("ra.user", v.trim()).apply(); showRaMenu(parent); }));
        m.add(t(S.RA_KEY) + "  ·  " + (key.isEmpty() ? t(S.NOT_SET) : Ai.masked(key)), !key.isEmpty(),
                () -> askText(t(S.RA_KEY), "", v -> { prefs.edit().putString("ra.key", v.trim()).apply(); showRaMenu(parent); }));
        m.add(t(S.AI_ON_PHONE), false, this::showWifiMenu);
        if (Achievements.configured(prefs)) m.add("🏆 " + t(S.RA_RECENT), false, () -> runInBackground(() -> {
            boolean ok = Achievements.refresh(prefs, true);
            List<Achievements.Game> gs = Achievements.cached(prefs);
            return () -> {
                if (!ok) { Toast.makeText(this, I18n.f(S.AI_FAILED, Achievements.error), Toast.LENGTH_LONG).show(); return; }
                ShelfView.Menu r = new ShelfView.Menu(t(S.RA_RECENT), user);
                r.parent = m;
                if (gs.isEmpty()) r.add(t(S.NONE_FOUND), false, null);
                for (Achievements.Game a : gs) r.add(a.title + "  ·  " + a.console + "  ·  🏆 " + a.got + " / " + a.total, a.total > 0 && a.got == a.total, null);
                shelf.showMenu(r);
            };
        }));
        if (!key.isEmpty()) m.add(t(S.REMOVE_KEY), false, () -> {
            prefs.edit().remove("ra.key").remove("ra.cache").apply();
            showRaMenu(parent);
        });
        shelf.showMenu(m);
    }

    /** A console picture or backdrop for a platform or folder, made by the image service from its name. */
    /** What the picture of a home tool tile shows, in words the image service understands. */
    private static String toolSubject(String id, String label) {
        switch (id) {
            case "storage": return "SD memory cards, a storage gauge bar, folders and boxes";
            case "stats": return "a golden trophy with small bar charts";
            case "random": return "two dice";
            case "search": return "a magnifying glass over a game cartridge";
            case "settings": return "a gear and a wrench";
            case "desktop": return "a little house surrounded by app icons";
            case "add": return "a folder with a plus sign";
            case "continue": return "a big play button with a game controller";
            case "recent": return "a clock with game cartridges";
            case "favorites": return "a golden star with a game cartridge";
            default: return label;
        }
    }

    private void aiPicture(Library.Platform p, String kind, Runnable then) {
        AiImage ai = AiImage.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
        boolean icon = kind.equals(Theme.CONSOLE);
        String subject = p.name.startsWith("tool-") ? toolSubject(p.name.substring(5), p.title())
                : p.isCustom() ? p.title() : p.name.startsWith("series-") ? p.title() + " video game series: one iconic object or creature from it"
                : p.system.equals("GENRE") ? Genres.subject(p.name.substring("genre-".length()))
                : Library.platformTitle(p.system.isEmpty() ? p.name : p.system) + " video game console";
        askText(t(S.AI_SUBJECT), subject, what -> {
            Toast.makeText(this, t(S.AI_DRAWING), Toast.LENGTH_LONG).show();
            new Thread(() -> {
                android.graphics.Bitmap b = ai.generate(icon ? AiImage.iconPrompt(what) : AiImage.backdropPrompt(what), icon ? "1024x1024" : "1536x1024", icon);
                boolean ok = false;
                if (b != null) {
                    try { ok = Theme.save(p.name, kind, new java.io.ByteArrayInputStream(AiImage.png(icon ? AiImage.cutOut(b) : b))); } catch (IOException ignored) { }
                }
                boolean done = ok;
                main.post(() -> {
                    Toast.makeText(this, done ? t(S.SAVED) : I18n.f(S.AI_FAILED, ai.error), Toast.LENGTH_LONG).show();
                    Theme.forgetPlatform(p.name);
                    if (shelf != null) then.run();
                });
            }, "ai-picture").start();
        });
    }

    /** The dark-mode backdrop of an owner, redrawn by the image service from its light one as clean line art. */
    private void aiDarkCut(String owner, android.graphics.Bitmap light, Runnable then) {
        AiImage ai = AiImage.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
        if (light == null) { Toast.makeText(this, t(S.NONE_FOUND), Toast.LENGTH_SHORT).show(); return; }
        Toast.makeText(this, t(S.AI_DRAWING), Toast.LENGTH_LONG).show();
        new Thread(() -> {
            android.graphics.Bitmap b = ai.edit(light, AiImage.darkCutPrompt(), "1536x1024");
            boolean ok = false;
            if (b != null) {
                // greyscale, like the bundled cuts, so the theme can colour it
                android.graphics.Bitmap g = android.graphics.Bitmap.createBitmap(b.getWidth(), b.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                android.graphics.Canvas cv = new android.graphics.Canvas(g);
                android.graphics.ColorMatrix cm = new android.graphics.ColorMatrix();
                cm.setSaturation(0);
                android.graphics.Paint pt = new android.graphics.Paint();
                pt.setColorFilter(new android.graphics.ColorMatrixColorFilter(cm));
                cv.drawBitmap(b, 0, 0, pt);
                try { ok = Theme.save(owner, Theme.BACKGROUND_DARK, new java.io.ByteArrayInputStream(AiImage.png(g))); } catch (IOException ignored) { }
            }
            boolean done = ok;
            main.post(() -> {
                Toast.makeText(this, done ? t(S.SAVED) : I18n.f(S.AI_FAILED, ai.error), Toast.LENGTH_LONG).show();
                Theme.forgetPlatform(owner);
                if (shelf != null) then.run();
            });
        }, "ai-dark-cut").start();
    }

    // ---- AI pictures for every game that lacks one: covers, backgrounds, title logos ----

    private static final String[] PIC_KINDS = {"box_front", "background", "logo"};
    /** Set while pictures are being drawn; setting it to true stops after the current one. */
    private java.util.concurrent.atomic.AtomicBoolean imageJob;

    private File ownMediaDir(Library.Game g) {
        return new File(GameText.ownDir(g.platform, g), "media/" + (g.mediaBase != null ? g.mediaBase : new File(g.baseName()).getName()));
    }

    private static String picName(String kind) {
        switch (kind) {
            case "box_front": return t(S.AI_COVER);
            case "background": return t(S.BACKGROUND_IMAGE);
            default: return t(S.AI_LOGO);
        }
    }

    /** Games of the platform without this kind of picture (a logo only for games whose English title is known). */
    private static List<Library.Game> missingPictures(Library.Platform p, String kind) {
        List<Library.Game> out = new ArrayList<>();
        for (Library.Game g : p.games) {
            if (g.pkg != null || g.path == null || g.asset(kind) != null) continue;
            if (kind.equals("logo") && (g.original == null || g.original.trim().isEmpty())) continue;
            out.add(g);
        }
        return out;
    }

    private void showAiPicturesMenu() {
        if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_PICTURES), t(S.AI_PICTURES_HINT));
        m.parent = shelfState.menu;
        // each kind says how many games lack it, so the next page's numbers are no surprise
        for (String kind : PIC_KINDS) {
            int n = 0;
            for (Library.Platform p : picturePlatforms()) n += missingPictures(p, kind).size();
            m.add(picName(kind) + "  ·  " + I18n.f(S.PICS_MISSING, n), false, () -> showAiPicturesFor(m, kind))
                    .hint(kind.equals("logo") ? t(S.AI_LOGO_HINT) : null);
        }
        shelf.showMenu(m);
    }

    /** The consoles whose games can get pictures (not folders, lists or files without a console). */
    private List<Library.Platform> picturePlatforms() {
        List<Library.Platform> out = new ArrayList<>();
        if (lib != null) for (Library.Platform p : lib.platforms)
            if (!p.isCustom() && !shelf.isPseudo(p) && !Library.UNSORTED.equals(p.system)) out.add(p);
        return out;
    }

    /** A console's name, with its folder when two consoles would read the same ("GBA" and its hack folder). */
    private String distinctTitle(Library.Platform p, List<Library.Platform> all) {
        for (Library.Platform o : all)
            if (o != p && o.title().equals(p.title()) && !p.games.isEmpty() && p.games.get(0).dir != null)
                return p.title() + " (" + p.games.get(0).dir.getName() + ")";
        return p.title();
    }

    private void showAiPicturesFor(ShelfView.Menu parent, String kind) {
        List<Library.Platform> all = picturePlatforms(), ps = new ArrayList<>();
        List<Library.Game> every = new ArrayList<>();
        for (Library.Platform p : all) {
            List<Library.Game> gs = missingPictures(p, kind);
            if (gs.isEmpty()) continue;
            ps.add(p);
            every.addAll(gs);
        }
        ShelfView.Menu m = new ShelfView.Menu(picName(kind) + "  ·  " + I18n.f(S.PICS_MISSING, every.size()), t(S.PICK_PLATFORM_OR_ALL));
        m.parent = parent;
        if (ps.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        else if (ps.size() > 1) m.add(t(S.ALL_PLATFORMS) + "  ·  " + I18n.f(S.GAMES_COUNT, every.size()), false, () -> confirmAiPictures(m, every, kind));
        for (Library.Platform p : ps) {
            List<Library.Game> gs = missingPictures(p, kind);
            m.add(distinctTitle(p, all) + "  ·  " + I18n.f(S.GAMES_COUNT, gs.size()), false, () -> confirmAiPictures(m, gs, kind));
        }
        shelf.showMenu(m);
    }

    private void confirmAiPictures(ShelfView.Menu parent, List<Library.Game> gs, String kind) {
        ShelfView.Menu m = new ShelfView.Menu(picName(kind), I18n.f(S.AI_PICTURES_CONFIRM, gs.size(), Math.max(1, gs.size() / 2)));
        m.parent = parent;
        m.add(t(S.GO), false, () -> { shelf.closeMenu(); runAiPictures(gs, kind); });
        m.add(t(S.CANCEL), false, () -> shelf.showMenu(parent));
        shelf.showMenu(m);
    }

    private void runAiPictures(List<Library.Game> games, String kind) {
        AiImage ai = AiImage.from(prefs);
        if (ai == null) return;
        if (imageJob != null) { Toast.makeText(this, t(S.AI_PICTURES_BUSY), Toast.LENGTH_LONG).show(); return; }
        java.util.concurrent.atomic.AtomicBoolean stop = new java.util.concurrent.atomic.AtomicBoolean();
        imageJob = stop;
        Toast.makeText(this, I18n.f(S.AI_PICTURES_PROGRESS, 0, games.size()), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            int done = 0, failed = 0;
            for (int i = 0; i < games.size() && !stop.get(); i++) {
                Library.Game g = games.get(i);
                String platform = Library.platformTitle(g.platform.system.isEmpty() ? g.platform.name : g.platform.system);
                android.graphics.Bitmap b;
                String file;
                if (kind.equals("box_front")) { b = ai.generate(AiImage.coverPrompt(g.title, g.original, platform), "1024x1536"); file = "cover.png"; }
                else if (kind.equals("background")) { b = ai.generate(AiImage.gameBackdropPrompt(g.title, g.original, platform), "1536x1024"); file = "background.png"; }
                else {
                    b = ai.generate(AiImage.logoPrompt(g.original.trim()), "1536x1024", true);
                    if (b != null) b = AiImage.cutOut(b);
                    file = "logo.png";
                }
                boolean ok = false;
                if (b != null) {
                    File dir = ownMediaDir(g);
                    dir.mkdirs();
                    try (java.io.FileOutputStream o = new java.io.FileOutputStream(new File(dir, file))) {
                        o.write(AiImage.png(b));
                        ok = true;
                    } catch (IOException e) {
                        ai.error = e.getMessage();
                    }
                }
                if (ok) done++; else failed++;
                // a key that is refused or out of credit fails every time: stop early
                if (!ok && done == 0 && failed >= 2) break;
                int n = i + 1;
                main.post(() -> Toast.makeText(this, I18n.f(S.AI_PICTURES_PROGRESS, n, games.size()), Toast.LENGTH_SHORT).show());
            }
            int made = done;
            String err = made < games.size() && !stop.get() ? ai.error : null;
            main.post(() -> {
                imageJob = null;
                Toast.makeText(this, I18n.f(S.AI_PICTURES_DONE, made) + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                if (made > 0) load();
            });
        }, "ai-pictures").start();
    }

    // ---- Wi-Fi transfer ----

    private WifiServer wifi;
    private int wifiReceived;

    /** Folders the transfer page can write to: every platform folder, plus the themes folder. */
    private java.util.Map<String, File> wifiTargets() {
        java.util.Map<String, File> m = new java.util.LinkedHashMap<>();
        if (lib != null) for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || p.games.isEmpty() || p.games.get(0).dir == null || p.games.get(0).pkg != null) continue;
            m.put(p.title() + "  —  " + p.games.get(0).dir.getAbsolutePath(), p.games.get(0).dir);
        }
        File themes = Backups.folder(volumeRoots()).getParentFile();
        m.put(t(S.THEME) + "  —  " + new File(themes, "themes").getAbsolutePath(), new File(themes, "themes"));
        // a config sent here shows up in Settings → Import a config
        m.put(t(S.IMPORT_TARGET) + "  —  " + new File(themes, "exports").getAbsolutePath(), new File(themes, "exports"));
        return m;
    }

    /** What the Wi-Fi page offers for download: exported configs and themes, then save backups, each newest first. */
    private List<File> wifiDownloads() {
        List<File> out = new ArrayList<>();
        File backups = Backups.folder(volumeRoots());
        out.addAll(Backups.list(new File(backups.getParentFile(), "exports")));
        out.addAll(Backups.list(backups));
        return out;
    }

    /** The QR code of a short text (the Wi-Fi page address), drawn with square modules; null if it does not fit. */
    private static Bitmap qrBitmap(String text) {
        boolean[][] m = QrCode.encode(text);
        if (m == null) return null;
        int n = m.length, k = Math.max(1, 640 / n), s = n * k;
        int[] px = new int[s * s];
        for (int y = 0; y < s; y++)
            for (int x = 0; x < s; x++) px[y * s + x] = m[y / k][x / k] ? 0xFF1F2A36 : 0xFFFFFFFF;
        return Bitmap.createBitmap(px, s, s, Bitmap.Config.ARGB_8888);
    }

    /** The menu the Wi-Fi page was opened from, where B goes back to. */
    private ShelfView.Menu wifiBack;

    private void showWifiMenu() {
        ShelfView.Menu from = shelfState.menu;
        if (from == null || !t(S.WIFI_TRANSFER).equals(from.title)) wifiBack = from;
        if (wifi == null || !wifi.running()) {
            wifiReceived = 0;
            wifi = new WifiServer(wifiTargets(), name -> main.post(() -> {
                wifiReceived++;
                Toast.makeText(this, I18n.f(S.UPLOADED, name), Toast.LENGTH_SHORT).show();
            }), new WifiServer.AiSettings() {
                @Override public String[] current() {
                    return new String[]{prefs.getString("ai.provider", "deepseek"), prefs.getString("ai.url", ""),
                            prefs.getString("ai.model", ""), Ai.masked(prefs.getString("ai.key", ""))};
                }

                @Override public void save(String provider, String url, String model, String key) {
                    SharedPreferences.Editor e = prefs.edit().putString("ai.provider", provider).putString("ai.url", url).putString("ai.model", model);
                    // an empty field keeps the key already set, but only for the same address: a stored key is
                    // never sent to a new one
                    if (key != null) e.putString("ai.key", key);
                    else if (!url.equals(prefs.getString("ai.url", "")) || !provider.equals(prefs.getString("ai.provider", provider))) e.remove("ai.key");
                    e.apply();
                    main.post(() -> Toast.makeText(MainActivity.this, t(S.AI_SAVED), Toast.LENGTH_LONG).show());
                }

                @Override public String password() { return adminPassword(); }

                @Override public String[] currentRa() {
                    return new String[]{prefs.getString("ra.user", ""), Ai.masked(prefs.getString("ra.key", ""))};
                }

                @Override public void saveRa(String user, String key) {
                    SharedPreferences.Editor e = prefs.edit().putString("ra.user", user);
                    if (key != null) e.putString("ra.key", key);
                    e.apply();
                }

                @Override public String[] currentImage() {
                    return new String[]{prefs.getString("img.url", ""), prefs.getString("img.model", ""), Ai.masked(prefs.getString("img.key", ""))};
                }

                @Override public String[] currentVoice() {
                    return new String[]{Speaker.provider(prefs), prefs.getString("tts.voice." + Speaker.provider(prefs), ""), Ai.masked(prefs.getString("soniox.key", ""))};
                }

                @Override public void saveVoice(String provider, String voice, String key) {
                    SharedPreferences.Editor e = prefs.edit().putString("tts.provider", provider);
                    if (!provider.equals(Speaker.OFF)) {
                        if (voice.isEmpty()) e.remove("tts.voice." + provider);
                        else e.putString("tts.voice." + provider, voice);
                    }
                    if (key != null) e.putString("soniox.key", key);
                    e.apply();
                }

                @Override public void saveImage(String url, String model, String key) {
                    SharedPreferences.Editor e = prefs.edit().putString("img.url", url).putString("img.model", model);
                    if (key != null) e.putString("img.key", key);
                    else if (!url.equals(prefs.getString("img.url", ""))) e.remove("img.key");
                    e.apply();
                    main.post(() -> Toast.makeText(MainActivity.this, t(S.AI_SAVED), Toast.LENGTH_LONG).show());
                }
            }, this::wifiDownloads);
            new Thread(() -> {
                try { wifi.start(); } catch (IOException e) { wifi = null; }
                main.post(this::showWifiMenu2);
            }, "wifi-start").start();
            return;
        }
        showWifiMenu2();
    }

    /** Unlocks the settings on the Wi-Fi page; a random six-digit one until the user sets their own. */
    private String adminPassword() {
        String pw = prefs.getString("admin.pw", "");
        if (pw.isEmpty()) {
            pw = String.format(Locale.ROOT, "%06d", new java.security.SecureRandom().nextInt(1_000_000));
            prefs.edit().putString("admin.pw", pw).apply();
        }
        return pw;
    }

    private void showWifiMenu2() {
        if (shelf == null) return;
        String url = WifiServer.address();
        if (wifi == null || url == null) {
            Toast.makeText(this, t(S.NONE_FOUND), Toast.LENGTH_SHORT).show();
            return;
        }
        String info = t(S.WIFI_OPEN) + "\n" + url + "\n" + t(S.ADMIN_PASSWORD) + "  " + adminPassword() + "\n\n" + t(S.WIFI_WHAT) + "\n\n" + t(S.WIFI_KEEP_ON);
        Bitmap qr = qrBitmap(url);
        if (qr != null) top.showQr(qr, t(S.WIFI_SCAN) + "\n\n" + info); else top.showMessage(info);
        keepAwake(true);   // with the screen off the page would not open
        ShelfView.Menu m = new ShelfView.Menu(t(S.WIFI_TRANSFER), url);
        m.parent = wifiBack;
        m.onBack = this::leaveWifiMenu;
        m.add(url, false, null);
        m.add(t(S.ADMIN_PASSWORD) + "  ·  " + adminPassword(), false, () -> askText(t(S.ADMIN_PASSWORD), "", pw -> {
            if (pw.trim().length() >= 4) prefs.edit().putString("admin.pw", pw.trim()).apply();
            showWifiMenu2();
        }));
        m.add(t(S.WIFI_STOP), false, () -> {
            wifi.stop();
            wifi = null;
            keepAwake(prefs.getInt("screensaver", 5) > 0);
            if (wifiReceived > 0) load();
            leaveWifiMenu();
        });
        shelf.showMenu(m);
    }

    /** B on the Wi-Fi page (or after stopping it): back where it was opened from, with fresh labels. */
    private void leaveWifiMenu() {
        ShelfView.Menu b = wifiBack;
        wifiBack = null;
        if (b == null) { shelf.closeMenu(); onFocus(shelf.selected()); return; }
        if (b.reopen != null || b.page != null) { backToSettings(b); return; }
        if (b.isPage()) { shelf.closeMenu(); onSettings(); return; }
        shelf.showMenu(b);
        onFocus(shelf.selected());
    }

    private List<File> volumeRoots() {
        List<File> out = new ArrayList<>();
        android.os.storage.StorageManager sm = getSystemService(android.os.storage.StorageManager.class);
        for (android.os.storage.StorageVolume v : sm.getStorageVolumes()) if (v.getDirectory() != null) out.add(v.getDirectory());
        return out;
    }

    // ---- tidy-up tools: duplicates, missing covers, backups, theme export, diagnostics ----

    private void runInBackground(java.util.concurrent.Callable<Runnable> work) {
        Toast.makeText(this, t(S.WORKING), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            Runnable then;
            try { then = work.call(); } catch (Exception e) { then = () -> Toast.makeText(this, String.valueOf(e.getMessage()), Toast.LENGTH_LONG).show(); }
            Runnable result = then;
            main.post(() -> {
                try {
                    result.run();
                } catch (NullPointerException gone) {
                    // the lower screen went away (power button, a game started) while this was working
                }
            });
        }, "tools").start();
    }

    /** The tools, each in its settings category. */
    private void addToolRows(ShelfView.Menu tools, ShelfView.Menu data, ShelfView.Menu ai, ShelfView.Menu play) {
        tools.add(t(S.PROBLEMS) + (lastProblems == null || lib != lastProblems.lib ? "" : "  ·  " + lastProblems.total()), false,
                () -> showProblems(tools)).hint(t(S.H_PROBLEMS));
        tools.add(t(S.STORAGE), false, this::showStorage);
        tools.add(t(S.FILL_COVERS), false, this::fillCovers);
        data.add(t(S.BACKUP_SAVES), false, () -> runInBackground(() -> {
            File out = Backups.backup(Backups.folder(volumeRoots()));
            prefs.edit().putLong("unbackedPlay", 0).apply();
            return () -> Toast.makeText(this, I18n.f(S.BACKUP_DONE, out.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }));
        data.add(t(S.BACKUP_REMINDER) + "  ·  " + backupRemindLabel(), false, () -> showBackupRemindMenu(data));
        data.add(t(S.RESTORE_SAVES), false, () -> {
            ShelfView.Menu rm = new ShelfView.Menu(t(S.RESTORE_SAVES), null);
            rm.parent = shelfState.menu;
            List<File> zips = Backups.list(Backups.folder(volumeRoots()));
            if (zips.isEmpty()) rm.add(t(S.NONE_FOUND), false, null);
            for (File z : zips) rm.add(z.getName() + "  ·  " + (z.length() >> 20) + " MB", false, () -> {
                ShelfView.Menu sure = new ShelfView.Menu(t(S.RESTORE_SAVES), z.getName());
                sure.parent = shelfState.menu;
                sure.add(t(S.GO), false, () -> runInBackground(() -> {
                    int n = Backups.restore(z);
                    return () -> { shelf.closeMenu(); Toast.makeText(this, I18n.f(S.RESTORE_DONE, n), Toast.LENGTH_LONG).show(); };
                }));
                sure.add(t(S.CANCEL), false, () -> shelf.closeMenu());
                shelf.showMenu(sure);
            });
            shelf.showMenu(rm);
        });
        String lang = Translate.target();
        if (lang != null) tools.add(t(S.TRANSLATE_ALL_CHEATS), false, () -> runInBackground(() -> {
            Translate tr = new Translate(new File(getFilesDir(), "translations.tsv"), lang, Ai.from(prefs));
            int n = 0, left = 0;
            List<Cheats.Store> stores = new ArrayList<>();
            for (String tid : Cheats.allTitleIds()) stores.add(Cheats.citra(tid));
            stores.addAll(RaCheats.all());
            stores.addAll(NdsCheats.all());
            stores.addAll(PspCheats.all());
            for (Cheats.Store st : stores) {
                int[] r = Cheats.translate(st, tr);
                n += r[0];
                left += r[1];
            }
            int done = n, rest = left;
            String err = tr.aiError();
            return () -> Toast.makeText(this, I18n.f(S.TRANSLATED, done) + (rest > 0 ? "  ·  " + I18n.f(S.NOT_TRANSLATED, rest) : "")
                    + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
        }));
        int restRow = play.labels.size();
        play.add(restLabel(), false, () -> {
            prefs.edit().putInt("restHours", (prefs.getInt("restHours", 2) + 1) % 4).apply();
            play.labels.set(restRow, restLabel());
            shelf.invalidate();
        });
        data.add(t(S.EXPORT_THEME), false, () -> runInBackground(() -> {
            File out = Theme.export(this, new File(Backups.folder(volumeRoots()).getParentFile(), "exports"));
            return () -> Toast.makeText(this, I18n.f(S.EXPORTED, out.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }));
        tools.add(t(S.DIAGNOSTICS), false, this::showDiagnostics);
    }

    /** Game names and descriptions into the interface language with the AI service, platform by platform. */
    private void showGameTextMenu() {
        Ai ai = Ai.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        String lang = I18n.code();
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_GAME_TEXT), t(S.AI_GAME_TEXT_HINT));
        m.parent = shelfState.menu;
        List<Library.Platform> ps = new ArrayList<>();
        int total = 0;
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || shelf.isPseudo(p)) continue;
            int n = GameText.todo(p, lang).size();
            if (n == 0) continue;
            ps.add(p);
            total += n;
            m.add(p.title() + "  ·  " + I18n.f(S.GAMES_COUNT, n), false, () -> runGameText(java.util.Collections.singletonList(p), ai, lang));
        }
        if (ps.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        else if (ps.size() > 1) m.add(t(S.ALL_PLATFORMS) + "  ·  " + I18n.f(S.GAMES_COUNT, total), false, () -> runGameText(ps, ai, lang));
        shelf.showMenu(m);
    }

    /** Settings → Tools → AI: sort games into genres, per platform or all at once. */
    private void showGenresMenu() {
        Ai ai = Ai.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_GENRES), t(S.AI_GENRES_HINT));
        m.parent = shelfState.menu;
        List<Library.Platform> ps = new ArrayList<>();
        int total = 0;
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || shelf.isPseudo(p)) continue;
            int n = Genres.todo(p).size();
            if (n == 0) continue;
            ps.add(p);
            total += n;
            m.add(p.title() + "  ·  " + I18n.f(S.GAMES_COUNT, n), false, () -> runGenres(java.util.Collections.singletonList(p), ai));
        }
        if (ps.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        else if (ps.size() > 1) m.add(t(S.ALL_PLATFORMS) + "  ·  " + I18n.f(S.GAMES_COUNT, total), false, () -> runGenres(ps, ai));
        boolean ask = prefs.getBoolean("askGenres", true);
        m.add(t(S.ASK_NEW_GAMES) + "  ·  " + t(ask ? S.ON : S.OFF), ask, () -> {
            prefs.edit().putBoolean("askGenres", !ask).apply();
            showGenresMenu();
        });
        shelf.showMenu(m);
    }

    private boolean sorting;

    private void runGenres(List<Library.Platform> ps, Ai ai) { runGenres(ps, ai, false); }

    /** Genres, and with {@code alsoSeries} the series too (for new games). */
    private void runGenres(List<Library.Platform> ps, Ai ai, boolean alsoSeries) {
        shelf.closeMenu();
        if (sorting) return;
        sorting = true;
        int total = 0;
        for (Library.Platform p : ps) total += Genres.todo(p).size() + (alsoSeries ? Series.todo(p).size() : 0);
        int all = total;
        Toast.makeText(this, I18n.f(S.AI_GENRES_PROGRESS, 0, all), Toast.LENGTH_SHORT).show();
        Library startLib = lib;
        new Thread(() -> {
            int done = 0;
            outer:
            for (Library.Platform p : ps) {
                for (java.util.Map.Entry<Library.Platform, List<Library.Game>> e : byPlatform(Genres.todo(p)).entrySet()) {
                    List<Library.Game> gs = e.getValue();
                    int base = done;
                    int n = Genres.classify(ai, e.getKey(), gs, k -> main.post(() ->
                            Toast.makeText(this, I18n.f(S.AI_GENRES_PROGRESS, base + k, all), Toast.LENGTH_SHORT).show()), null);
                    done += n;
                    if (n < gs.size() && ai.error != null) break outer;
                }
            }
            if (alsoSeries && ai.error == null) done += seriesPass(ps, ai, done, all, S.AI_GENRES_PROGRESS);
            int finished = done;
            String err = ai.error;
            main.post(() -> {
                sorting = false;
                Toast.makeText(this, I18n.f(S.AI_GENRES_DONE, finished) + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                if (lib != startLib) load();
                else { if (shelf != null) shelf.refresh(); writeCatalog(); }
            });
        }, "genres").start();
    }

    /** Games grouped by their own platform, whose folder their files are kept in. */
    private static java.util.Map<Library.Platform, List<Library.Game>> byPlatform(List<Library.Game> games) {
        java.util.Map<Library.Platform, List<Library.Game>> m = new java.util.LinkedHashMap<>();
        for (Library.Game g : games) m.computeIfAbsent(g.platform, k -> new ArrayList<>()).add(g);
        return m;
    }

    // ---- new games: offer to sort them ----

    /** Games first seen in this run of the launcher, until the user has been asked about them. */
    private final List<Library.Game> newGames = new ArrayList<>();

    private void askNewGenres() {
        if (newGames.isEmpty() || shelf == null || lib == null || shelfState.menu != null || sorting || kb != null) return;
        Ai ai = Ai.from(prefs);
        if (ai == null || !prefs.getBoolean("askGenres", true)) { newGames.clear(); return; }
        java.util.LinkedHashSet<Library.Platform> ps = new java.util.LinkedHashSet<>();
        int n = 0;
        for (Library.Game old : newGames) {
            Library.Game g = lib.byKey.get(old.key());
            if (g == null || g.genre != null) continue;
            n++;
            ps.add(g.platform);
        }
        newGames.clear();
        if (n == 0) return;
        ShelfView.Menu m = new ShelfView.Menu(I18n.f(S.NEW_GAMES_FOUND, n), t(S.AI_GENRES_HINT));
        m.add(t(S.SORT_NOW), false, () -> runGenres(new ArrayList<>(ps), ai, true));
        m.add(t(S.NOT_NOW), false, () -> shelf.closeMenu());
        m.add(t(S.NEVER_ASK), false, () -> {
            prefs.edit().putBoolean("askGenres", false).apply();
            shelf.closeMenu();
        });
        shelf.showMenu(m);
    }

    // ---- series ----

    /** Settings → Tools → AI: find the series of games, per platform or all at once. */
    private void showSeriesMenu() {
        Ai ai = Ai.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_SERIES), t(S.AI_SERIES_HINT));
        m.parent = shelfState.menu;
        List<Library.Platform> ps = new ArrayList<>();
        int total = 0;
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || shelf.isPseudo(p)) continue;
            int n = Series.todo(p).size();
            if (n == 0) continue;
            ps.add(p);
            total += n;
            m.add(p.title() + "  ·  " + I18n.f(S.GAMES_COUNT, n), false, () -> runSeries(java.util.Collections.singletonList(p), ai));
        }
        if (ps.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        else if (ps.size() > 1) m.add(t(S.ALL_PLATFORMS) + "  ·  " + I18n.f(S.GAMES_COUNT, total), false, () -> runSeries(ps, ai));
        shelf.showMenu(m);
    }

    private void runSeries(List<Library.Platform> ps, Ai ai) {
        shelf.closeMenu();
        if (sorting) return;
        sorting = true;
        int total = 0;
        for (Library.Platform p : ps) total += Series.todo(p).size();
        int all = total;
        Toast.makeText(this, I18n.f(S.AI_SERIES_PROGRESS, 0, all), Toast.LENGTH_SHORT).show();
        Library startLib = lib;
        new Thread(() -> {
            int finished = seriesPass(ps, ai, 0, all, S.AI_SERIES_PROGRESS);
            String err = ai.error;
            main.post(() -> {
                sorting = false;
                Toast.makeText(this, I18n.f(S.AI_SERIES_DONE, finished) + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                if (lib != startLib) load();
                else { if (shelf != null) shelf.refresh(); writeCatalog(); }
            });
        }, "series").start();
    }

    /** Runs in the background: the series of every game of {@code ps} not looked at yet; returns how many were done. */
    private int seriesPass(List<Library.Platform> ps, Ai ai, int base0, int all, S progress) {
        java.util.Set<String> known = Series.known(lib);
        int done = 0;
        for (Library.Platform p : ps) {
            for (java.util.Map.Entry<Library.Platform, List<Library.Game>> e : byPlatform(Series.todo(p)).entrySet()) {
                List<Library.Game> gs = e.getValue();
                int base = base0 + done;
                int n = Series.classify(ai, e.getKey(), gs, known, k -> main.post(() ->
                        Toast.makeText(this, I18n.f(progress, base + k, all), Toast.LENGTH_SHORT).show()));
                done += n;
                if (n < gs.size() && ai.error != null) return done;
            }
        }
        return done;
    }

    // ---- status (playing, want to play, done, dropped) and a 1-5 star rating ----

    private static final String[] STATUSES = {"playing", "wish", "done", "dropped"};

    private String restLabel() {
        int rest = prefs.getInt("restHours", 2);
        return t(S.REST_SETTING) + "  ·  " + (rest == 0 ? t(S.OFF) : I18n.f(S.REST_HOURS, rest));
    }

    private static String statusName(String s) {
        switch (s) {
            case "playing": return t(S.STATUS_PLAYING);
            case "wish": return t(S.STATUS_WISH);
            case "done": return t(S.STATUS_DONE);
            case "dropped": return t(S.STATUS_DROPPED);
            default: return t(S.STATUS_NONE);
        }
    }

    private static String stars(int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 1; i <= 5; i++) b.append(i <= n ? '★' : '☆');
        return b.toString();
    }

    /** "Playing · ★★★★☆" for the top screen, or null when nothing is set. */
    private String statusTag(Library.Game g) {
        String s = prefs.getString("status:" + g.key(), null);
        int r = prefs.getInt("rating:" + g.key(), 0);
        if (s == null && r == 0) return null;
        return (s != null ? statusName(s) : "") + (s != null && r > 0 ? " · " : "") + (r > 0 ? stars(r) : "");
    }

    @Override public String statusLine(Library.Game g) {
        String tag = statusTag(g);
        return tag != null ? tag : t(S.STATUS_NONE);
    }

    @Override public void onStatus(Library.Game g, ShelfView.Menu parent) {
        String cur = prefs.getString("status:" + g.key(), "");
        int rating = prefs.getInt("rating:" + g.key(), 0);
        ShelfView.Menu m = new ShelfView.Menu(t(S.STATUS), g.title);
        m.parent = parent;
        m.add(t(S.STATUS_NONE), cur.isEmpty(), () -> setStatus(g, null, -1));
        for (String s : STATUSES) m.add(statusName(s), s.equals(cur), () -> setStatus(g, s, -1));
        for (int i = 5; i >= 1; i--) {
            int r = i;
            m.add(stars(r), r == rating, () -> setStatus(g, cur.isEmpty() ? null : cur, r));
        }
        m.add(t(S.NO_RATING), rating == 0, () -> setStatus(g, cur.isEmpty() ? null : cur, 0));
        shelf.showMenu(m);
    }

    /** rating -1 keeps the rating. */
    private void setStatus(Library.Game g, String status, int rating) {
        SharedPreferences.Editor e = prefs.edit();
        if (status == null) e.remove("status:" + g.key()); else e.putString("status:" + g.key(), status);
        if (rating == 0) e.remove("rating:" + g.key()); else if (rating > 0) e.putInt("rating:" + g.key(), rating);
        e.apply();
        playedCache.clear();
        applyCustomFolders();
        if (shelf != null) {
            shelf.closeMenu();
            shelf.refresh();
            onFocus(shelf.selected());
        }
    }

    // ---- how to play: controls and basics written by the AI once, kept for next time ----

    @Override public void onHowTo(Library.Game g) {
        String cached = prefs.getString("howto:" + g.key(), null);
        if (cached != null) { showHowTo(g, cached); return; }
        Ai ai = Ai.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        if (shelf != null) shelf.closeMenu();
        Toast.makeText(this, t(S.THINKING), Toast.LENGTH_SHORT).show();
        String game = g.title + (g.original != null && !g.original.isEmpty() ? " (" + g.original + ")" : "") + ", " + platformName(g);
        String lang = I18n.name();
        new Thread(() -> {
            String a = ai.chat("You explain how to play a retro video game to someone starting it on a handheld emulator whose "
                    + "buttons follow the original console's layout. Write in " + lang + ", plain text without markdown, at most "
                    + "about 180 words: first the controls (which button does what, including confirm/cancel when they are swapped "
                    + "as in many Japanese releases), then the few rules or systems a newcomer must know. If unsure of a detail, "
                    + "leave it out.", "Game: " + game, 900);
            String err = ai.error;
            main.post(() -> {
                if (a == null) { Toast.makeText(this, I18n.f(S.AI_FAILED, err), Toast.LENGTH_LONG).show(); return; }
                String text = a.replace("**", "").replace("##", "").trim();
                prefs.edit().putString("howto:" + g.key(), text).apply();
                showHowTo(g, text);
            });
        }, "how-to").start();
    }

    private void showHowTo(Library.Game g, String text) {
        showTextDialog(t(S.HOW_TO) + " · " + g.title, text, t(S.REGENERATE), () -> {
            prefs.edit().remove("howto:" + g.key()).apply();
            onHowTo(g);
        });
    }

    // ---- the last picture of a played game (newest screenshot or save-state picture) on the top screen ----

    private final java.util.Map<String, File> lastShots = new java.util.HashMap<>();
    private Runnable shotLookup;

    private void lookUpLastShot(Library.Game g) {
        if (shotLookup != null) main.removeCallbacks(shotLookup);
        shotLookup = null;
        if (!prefs.getBoolean("lastShot", true) || prefs.getString("sessions:" + g.key(), "").isEmpty()) return;
        if (lastShots.containsKey(g.key())) { top.showLastShot(g, lastShots.get(g.key())); return; }
        shotLookup = () -> new Thread(() -> {
            List<File> shots = screenshots(g);
            File f = shots.isEmpty() ? null : shots.get(0);
            main.post(() -> {
                lastShots.put(g.key(), f);
                if (top != null) top.showLastShot(g, f);
            });
        }, "last-shot").start();
        main.postDelayed(shotLookup, 700);   // not while scrolling past
    }

    // ---- one game's saves: list, back up, restore ----

    @Override public void onSaves(Library.Game g, ShelfView.Menu parent) {
        Toast.makeText(this, t(S.WORKING), Toast.LENGTH_SHORT).show();
        File dir = Backups.folder(volumeRoots());
        new Thread(() -> {
            List<File> saves = Backups.gameSaves(g);
            main.post(() -> showSaves(g, saves, dir, parent));
        }, "saves").start();
    }

    private static String safeName(String s) {
        String t = s.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        return t.length() > 40 ? t.substring(0, 40) : t;
    }

    private void showSaves(Library.Game g, List<File> saves, File dir, ShelfView.Menu parent) {
        if (shelf == null) return;
        long total = 0;
        for (File f : saves) total += f.length();
        ShelfView.Menu m = new ShelfView.Menu(t(S.SAVES) + " · " + g.title,
                saves.isEmpty() ? t(S.SAVES_NONE) : I18n.f(S.SAVES_HINT, saves.size(), gb(total)));
        m.parent = parent;
        String prefix = "game-" + safeName(g.title);
        if (!saves.isEmpty()) m.add(t(S.SAVES_BACKUP), false, () -> runInBackground(() -> {
            Backups.backupFiles(dir, prefix, saves);
            return () -> Toast.makeText(this, I18n.f(S.SAVES_DONE, saves.size()), Toast.LENGTH_LONG).show();
        }));
        File[] zips = dir.listFiles((d, n) -> n.startsWith(prefix + "-") && n.endsWith(".zip"));
        if (zips != null) {
            java.util.Arrays.sort(zips, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag(I18n.code()));
            for (File z : zips) m.add(I18n.f(S.SAVES_RESTORE, df.format(new Date(z.lastModified()))), false, () -> {
                ShelfView.Menu sure = new ShelfView.Menu(t(S.SAVES_RESTORE_Q), z.getName());
                sure.parent = m;
                sure.add(t(S.GO), false, () -> runInBackground(() -> {
                    int n = Backups.restore(z);
                    return () -> Toast.makeText(this, I18n.f(S.SAVES_RESTORED, n), Toast.LENGTH_LONG).show();
                }));
                sure.add(t(S.CANCEL), false, () -> shelf.showMenu(m));
                shelf.showMenu(sure);
            });
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.forLanguageTag(I18n.code()));
        for (File f : saves) m.add(f.getName() + "  ·  " + gb(f.length()) + "  ·  " + df.format(new Date(f.lastModified())), false, null);
        shelf.showMenu(m);
    }

    // ---- game notes ----

    @Override public String noteOf(Library.Game g) { return prefs.getString("note:" + g.key(), null); }

    private void setNote(Library.Game g, String s) {
        if (s == null || s.trim().isEmpty()) prefs.edit().remove("note:" + g.key()).apply();
        else prefs.edit().putString("note:" + g.key(), s.trim()).apply();
        if (shelf != null) {
            shelf.closeMenu();
            onFocus(shelf.selected());
        }
    }

    @Override public void onNote(Library.Game g, ShelfView.Menu parent) {
        String cur = noteOf(g);
        ShelfView.Menu m = new ShelfView.Menu(t(S.NOTE), cur != null ? cur : t(S.NOTE_HINT));
        m.parent = parent;
        m.add(t(S.WRITE_NOTE), false, () -> askText(t(S.NOTE), cur != null ? cur : "", s -> setNote(g, s)));
        m.add(t(S.SPEAK_NOTE), false, () -> { shelf.closeMenu(); listen(s -> setNote(g, s)); });
        if (cur != null) m.add(t(S.DELETE_NOTE), false, () -> setNote(g, null));
        shelf.showMenu(m);
    }

    // ---- asking the AI about a game ----

    @Override public void onAskAi(Library.Game g, ShelfView.Menu parent) {
        if (Ai.from(prefs) == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.ASK_AI), g.title);
        m.parent = parent;
        m.add(t(S.ASK_TYPE), false, () -> askText(t(S.ASK_AI), "", q -> askAi(g, q)));
        m.add(t(S.ASK_SPEAK), false, () -> { shelf.closeMenu(); listen(q -> askAi(g, q)); });
        shelf.showMenu(m);
    }

    private static String platformName(Library.Game g) {
        return Library.platformTitle(g.platform.system.isEmpty() ? g.platform.name : g.platform.system);
    }

    private void askAi(Library.Game g, String q) {
        Ai ai = Ai.from(prefs);
        if (ai == null) return;
        if (shelf != null) shelf.closeMenu();
        Toast.makeText(this, t(S.THINKING), Toast.LENGTH_SHORT).show();
        String game = g.title + (g.original != null && !g.original.isEmpty() ? " (" + g.original + ")" : "") + ", " + platformName(g);
        new Thread(() -> {
            String a = ai.chat("You help someone playing a retro video game on a handheld. Answer the question about the game in the "
                    + "language the question is written in, short and practical (at most about 150 words, plain text, no markdown). "
                    + "If you are not sure about a specific detail, say so rather than guessing.", "Game: " + game + "\nQuestion: " + q, 700);
            String err = ai.error;
            main.post(() -> showAnswer(g, q, a != null ? a.replace("**", "").replace("##", "").trim() : I18n.f(S.AI_FAILED, err)));
        }, "ask-ai").start();
    }

    private void showAnswer(Library.Game g, String q, String a) {
        showTextDialog(q, a, t(S.ASK_AGAIN), () -> onAskAi(g, null));
    }

    /**
     * A text from the AI on the top screen, made for the gamepad: A or B closes it, Y does {@code onY}, up and down
     * scroll a long text. (A dialog's own buttons would need the d-pad to reach, and B would not close it.)
     */
    private void showTextDialog(String title, String text, String yLabel, Runnable onY) {
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        AlertDialog.Builder b = new AlertDialog.Builder(this, theme).setTitle(title).setMessage(text)
                .setPositiveButton("A · " + t(S.ASK_CLOSE), null);
        if (onY != null) b.setNeutralButton("Y · " + yLabel, (x, w) -> onY.run());
        AlertDialog d = b.create();
        d.setOnKeyListener((di, code, e) -> {
            int k = remap(code);
            boolean ours = k == KeyEvent.KEYCODE_BUTTON_A || k == KeyEvent.KEYCODE_BUTTON_B || k == KeyEvent.KEYCODE_BUTTON_Y
                    || k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN;
            if (!ours) return false;
            if (e.getAction() != KeyEvent.ACTION_DOWN || e.getRepeatCount() > 0 && k != KeyEvent.KEYCODE_DPAD_UP && k != KeyEvent.KEYCODE_DPAD_DOWN) return true;
            if (k == KeyEvent.KEYCODE_BUTTON_Y) {
                if (onY == null) return true;
                di.dismiss();
                onY.run();
            } else if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) {
                android.view.View v = d.findViewById(android.R.id.message);
                while (v != null && !(v instanceof android.widget.ScrollView)) v = v.getParent() instanceof android.view.View ? (android.view.View) v.getParent() : null;
                if (v != null) ((android.widget.ScrollView) v).smoothScrollBy(0, (k == KeyEvent.KEYCODE_DPAD_UP ? -1 : 1) * v.getHeight() / 2);
            } else {
                di.dismiss();
            }
            return true;
        });
        d.show();
    }

    // ---- AI: what to play today ----

    private void aiPick() {
        Ai ai = Ai.from(prefs);
        if (ai == null) { Toast.makeText(this, t(S.AI_NEEDED), Toast.LENGTH_LONG).show(); return; }
        if (lib == null) return;
        if (shelf != null) shelf.closeMenu();
        Toast.makeText(this, t(S.THINKING), Toast.LENGTH_LONG).show();
        List<Library.Game> all = new ArrayList<>();
        for (Library.Game g : lib.byKey.values()) if (g.pkg == null && g.path != null) all.add(g);
        List<Library.Game> most = new ArrayList<>(all);
        most.sort((a, b) -> Long.compare(prefs.getLong("time:" + b.key(), 0), prefs.getLong("time:" + a.key(), 0)));
        StringBuilder sb = new StringBuilder("Most played:\n");
        for (int i = 0; i < Math.min(15, most.size()); i++) {
            Library.Game g = most.get(i);
            long min = prefs.getLong("time:" + g.key(), 0) / 60_000;
            if (min < 1) break;
            sb.append(g.title).append(" (").append(platformName(g)).append(", ").append(min).append(" min)\n");
        }
        sb.append("\nLibrary (index|title|platform|genre|minutes played):\n");
        for (int i = 0; i < all.size(); i++) {
            Library.Game g = all.get(i);
            sb.append(i).append('|').append(g.title).append('|').append(platformName(g)).append('|')
                    .append(g.genre == null ? "" : g.genre).append('|').append(prefs.getLong("time:" + g.key(), 0) / 60_000).append('\n');
        }
        String lang = I18n.name();
        new Thread(() -> {
            String a = ai.chat("You recommend what to play next from the user's own game library. Judge their taste from the most "
                    + "played games. Pick 6 games from the library that they have played little or not at all: varied in platform "
                    + "and genre, well regarded, good on a handheld. Answer only with a JSON array of objects {\"i\": index, "
                    + "\"why\": one short sentence in " + lang + "}.", sb.toString(), 900);
            List<Library.Game> picks = new ArrayList<>();
            java.util.Map<Library.Game, String> whys = new java.util.HashMap<>();
            if (a != null) {
                int x = a.indexOf('['), y = a.lastIndexOf(']');
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(a.substring(x, y + 1));
                    for (int j = 0; j < arr.length(); j++) {
                        org.json.JSONObject o = arr.getJSONObject(j);
                        int i = o.optInt("i", -1);
                        if (i < 0 || i >= all.size() || picks.contains(all.get(i))) continue;
                        picks.add(all.get(i));
                        whys.put(all.get(i), o.optString("why", ""));
                    }
                } catch (Exception ignored) {
                }
            }
            String err = ai.error;
            main.post(() -> {
                if (picks.isEmpty()) {
                    Toast.makeText(this, I18n.f(S.AI_FAILED, err != null ? err : "unexpected answer"), Toast.LENGTH_LONG).show();
                    return;
                }
                TopView.extra.clear();
                TopView.extra.putAll(whys);
                searchText = "";
                searchGenre = null;
                if (shelf != null) shelf.showResults(t(S.AI_PICK), picks);
            });
        }, "ai-pick").start();
    }

    /** The library as DuoLauncher/catalog.tsv, written in the background. */
    private void writeCatalog() {
        Library l = lib;
        if (l == null) return;
        new Thread(() -> GameDb.writeCatalog(l, prefs), "catalog").start();
    }

    // ---- talking with the AI about what to play: say or type what you feel like, then narrow it down ----

    private void startChat() {
        openChat();
    }

    // ---- storage: what takes the space, and deleting games ----

    private final java.util.Map<Library.Game, Long> sizes = new java.util.HashMap<>();

    private static long sizeOf(File f) {
        if (f == null || !f.exists()) return 0;
        if (f.isFile()) return f.length();
        long s = 0;
        File[] fs = f.listFiles();
        if (fs != null) for (File x : fs) s += sizeOf(x);
        return s;
    }

    /** Every file of a game: the file itself, its other discs, and what a .cue or .m3u names next to it. */
    private static List<File> gameFiles(Library.Game g) {
        java.util.LinkedHashSet<File> out = new java.util.LinkedHashSet<>();
        List<File> todo = new ArrayList<>();
        todo.add(g.path);
        // further discs are named relative to the game list's folder, not the first disc's
        for (String d : g.discs) todo.add(new File(g.dir != null ? g.dir : g.path.getParentFile(), d));
        for (int i = 0; i < todo.size() && i < 64; i++) {
            File f = todo.get(i);
            if (!out.add(f) || !f.isFile()) continue;
            String n = f.getName().toLowerCase(java.util.Locale.ROOT);
            if (!n.endsWith(".cue") && !n.endsWith(".m3u")) continue;
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) {
                    String t = line.trim(), name = null;
                    if (n.endsWith(".m3u")) { if (!t.isEmpty() && !t.startsWith("#")) name = t; }
                    else if (t.regionMatches(true, 0, "FILE", 0, 4)) {
                        int a = t.indexOf('"'), b = t.lastIndexOf('"');
                        name = a >= 0 && b > a ? t.substring(a + 1, b) : t.substring(4).trim().replaceAll("\\s+\\S+$", "");
                    }
                    if (name == null || name.contains("..")) continue;
                    File c = new File(f.getParentFile(), name.replace('\\', '/'));
                    if (c.getParentFile() != null && c.getParentFile().getAbsolutePath().equals(f.getParentFile().getAbsolutePath())) todo.add(c);
                }
            } catch (IOException ignored) {
            }
        }
        return new ArrayList<>(out);
    }

    private static long gameSize(Library.Game g) {
        long s = 0;
        for (File f : gameFiles(g)) s += sizeOf(f);
        return s;
    }

    static String gb(long b) {
        return b >= 1L << 30 ? String.format(java.util.Locale.ROOT, "%.1f GB", b / (double) (1L << 30))
                : String.format(java.util.Locale.ROOT, "%d MB", Math.max(1, b >> 20));
    }

    private static String volumeOf(File f) {
        String path = f.getAbsolutePath();
        if (path.startsWith("/storage/emulated/0/")) return "/storage/emulated/0";
        if (path.startsWith("/storage/")) {
            int s = path.indexOf('/', 9);
            return s < 0 ? path : path.substring(0, s);
        }
        return f.getParentFile().getAbsolutePath();
    }

    /** Installed 3DS titles live in the emulator's own folders: shown, but not deleted from here. */
    static boolean deletable(Library.Game g) {
        return g.pkg == null && g.path != null && g.treeDir == null && !g.path.getName().endsWith(".app");
    }

    private void showStorage() {
        if (lib == null) return;
        Toast.makeText(this, t(S.COUNTING), Toast.LENGTH_SHORT).show();
        List<Library.Game> gs = new ArrayList<>();
        for (Library.Game g : lib.byKey.values()) if (g.pkg == null && g.path != null) gs.add(g);
        new Thread(() -> {
            java.util.Map<Library.Game, Long> m = new java.util.HashMap<>();
            for (Library.Game g : gs) m.put(g, gameSize(g));
            main.post(() -> {
                sizes.clear();
                sizes.putAll(m);
                showStorageMenu();
            });
        }, "sizes").start();
    }

    private void showStorageMenu() {
        if (shelf == null) return;
        java.util.Set<String> vols = new java.util.LinkedHashSet<>();
        for (Library.Game g : sizes.keySet()) vols.add(volumeOf(g.path));
        StringBuilder hint = new StringBuilder();
        for (String v : vols) {
            try {
                android.os.StatFs fs = new android.os.StatFs(v);
                if (hint.length() > 0) hint.append("  ·  ");
                hint.append(I18n.f(S.STORAGE_FREE, v.startsWith("/storage/emulated") ? t(S.INTERNAL_STORAGE) : t(S.CARD_STORAGE),
                        gb(fs.getAvailableBytes()), gb(fs.getTotalBytes())));
            } catch (Exception ignored) {
            }
        }
        ShelfView.Menu m = new ShelfView.Menu(t(S.STORAGE), hint.toString());
        m.parent = shelfState.menu;
        List<Library.Game> unplayed = new ArrayList<>();
        for (Library.Game g : sizes.keySet()) if (playedLine(g) == null) unplayed.add(g);
        unplayed.sort((a, b) -> Long.compare(sizes.get(b), sizes.get(a)));
        if (unplayed.size() > 40) unplayed.subList(40, unplayed.size()).clear();
        m.add(t(S.BIG_UNPLAYED), false, () -> showSizeList(t(S.BIG_UNPLAYED), unplayed, m));
        List<Library.Platform> ps = new ArrayList<>();
        java.util.Map<Library.Platform, Long> totals = new java.util.HashMap<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || shelf.isPseudo(p)) continue;
            long s = 0;
            for (Library.Game g : p.games) s += sizes.getOrDefault(g, 0L);
            if (s == 0) continue;
            ps.add(p);
            totals.put(p, s);
        }
        ps.sort((a, b) -> Long.compare(totals.get(b), totals.get(a)));
        for (Library.Platform p : ps) {
            List<Library.Game> gs = new ArrayList<>();
            for (Library.Game g : p.games) if (sizes.containsKey(g)) gs.add(g);
            gs.sort((a, b) -> Long.compare(sizes.get(b), sizes.get(a)));
            m.add(p.title() + "  ·  " + gb(totals.get(p)) + "  ·  " + I18n.f(S.GAMES_COUNT, gs.size()), false, () -> showSizeList(p.title(), gs, m));
        }
        shelf.showMenu(m);
    }

    private void showSizeList(String title, List<Library.Game> gs, ShelfView.Menu parent) {
        ShelfView.Menu m = new ShelfView.Menu(title, null);
        m.parent = parent;
        if (gs.isEmpty()) m.add(t(S.NONE_FOUND), false, null);
        for (Library.Game g : gs) {
            String played = playedLine(g);
            m.add(g.title + "  ·  " + gb(sizes.getOrDefault(g, 0L)) + "  ·  " + (played == null ? t(S.NOT_PLAYED) : played), false,
                    deletable(g) ? () -> confirmDeleteGame(g, gs, title, m) : null);
        }
        shelf.showMenu(m);
    }

    private void confirmDeleteGame(Library.Game g, List<Library.Game> list, String title, ShelfView.Menu parent) {
        ShelfView.Menu m = new ShelfView.Menu(I18n.f(S.DELETE_GAME_Q, g.title), I18n.f(S.DELETE_GAME_HINT, gb(sizes.getOrDefault(g, 0L))));
        m.parent = parent;
        m.add(t(S.DELETE_GAME), false, () -> {
            shelf.closeMenu();
            new Thread(() -> {
                boolean ok = true;
                for (File f : gameFiles(g)) ok &= deleteTree(f);
                boolean done = ok;
                main.post(() -> {
                    Toast.makeText(this, done ? I18n.f(S.GAME_DELETED, g.title) : t(S.DELETE_FAILED), Toast.LENGTH_SHORT).show();
                    sizes.remove(g);
                    list.remove(g);
                    if (shelf != null) showSizeList(title, list, parent.parent);
                    load();
                });
            }, "delete").start();
        });
        m.add(t(S.CANCEL), false, () -> shelf.showMenu(parent));
        shelf.showMenu(m);
    }

    private static boolean deleteTree(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) for (File x : fs) deleteTree(x);
        }
        return f.delete() || !f.exists();
    }

    // ---- speech: hold L while the keyboard is open, or after choosing to speak ----

    private Voice voice;
    /** Where the next spoken words go when the keyboard is not open. */
    private java.util.function.Consumer<String> voiceTo;
    private long voiceToAt;

    private boolean voiceReady() {
        if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.VOICE_NEEDS_KEY), Toast.LENGTH_LONG).show(); return false; }
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 7);
            Toast.makeText(this, t(S.MIC_PERMISSION), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void listen(java.util.function.Consumer<String> to) {
        if (!voiceReady()) return;
        voiceTo = to;
        voiceToAt = SystemClock.uptimeMillis();
        Toast.makeText(this, t(S.HOLD_L), Toast.LENGTH_LONG).show();
    }

    private void startVoice() {
        if (voice != null || !voiceReady()) return;
        Voice v = new Voice();
        if (!v.start()) { Toast.makeText(this, I18n.f(S.VOICE_FAILED, String.valueOf(v.error)), Toast.LENGTH_LONG).show(); return; }
        voice = v;
        String msg = t(holdCode == KeyEvent.KEYCODE_BUTTON_R1 ? S.R_LISTENING : S.LISTENING);
        if (kb != null) kb.setStatus(msg);
        else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void stopVoice() {
        Voice v = voice;
        if (v == null) return;
        voice = null;
        byte[] wav = v.stop();
        if (wav == null) {
            if (kb != null) kb.setStatus(null);
            if (chat != null) chat.setStatus(null);
            return;
        }
        if (kb != null) kb.setStatus(t(S.TRANSCRIBING));
        else Toast.makeText(this, t(S.TRANSCRIBING), Toast.LENGTH_SHORT).show();
        AiImage ai = AiImage.from(prefs);
        PadKeyboard forKb = kb;
        java.util.function.Consumer<String> to = voiceTo;
        new Thread(() -> {
            String heard = v.transcribe(ai, wav);
            String text = heard == null ? null : heard.replaceAll("[。．.！!？?，,、\\s]+$", "");
            main.post(() -> {
                if (text == null || text.isEmpty()) {
                    if (kb != null) kb.setStatus(null);
                    if (chat != null) chat.setStatus(null);   // no "transcribing…" left behind in the chat
                    Toast.makeText(this, I18n.f(S.VOICE_FAILED, String.valueOf(v.error)), Toast.LENGTH_LONG).show();
                    return;
                }
                if (forKb != null && kb == forKb) kb.insert(text);
                else if (to != null && voiceTo == to) {
                    voiceTo = null;
                    to.accept(text);
                }
            });
        }, "voice").start();
    }

    // ---- merging console folders ----

    private static boolean mergeable(Library.Platform p) {
        return !p.isCustom() && !p.system.equals("ANDROID") && !p.system.equals("APPS") && !p.system.equals("GENRE");
    }

    /**
     * Shows a console folder inside another one: the user's choice ("merge:" + name = target name), and two folders of
     * the same name found in different places. The games keep their own platform for launching and the launcher's files.
     */
    /** Puts merged folders back on their own (their games are taken out of the folder they were shown in). */
    private void unmerge() {
        for (int i = 0; i < lib.platforms.size(); i++) {
            Library.Platform p = lib.platforms.get(i);
            if (p.mergedFrom.isEmpty()) continue;
            p.games.removeIf(g -> g.platform != p);
            lib.platforms.addAll(i + 1, p.mergedFrom);
            p.mergedFrom.clear();
        }
    }

    /** The folder a merge chain ends in (A → B → C gives C), or null. */
    private String mergeTarget(String name) {
        String to = prefs.getString("merge:" + name, null);
        for (int hops = 0; to != null && hops < 8; hops++) {
            String next = prefs.getString("merge:" + to, null);
            if (next == null || next.equals(name)) break;
            to = next;
        }
        return to == null || to.equals(name) ? null : to;
    }

    private void applyMerges() {
        if (lib == null) return;
        unmerge();
        java.text.Collator col = java.text.Collator.getInstance(java.util.Locale.forLanguageTag(I18n.code()));
        for (Library.Platform p : new ArrayList<>(lib.platforms)) {
            if (!mergeable(p) || !lib.platforms.contains(p)) continue;
            String to = mergeTarget(p.name);
            Library.Platform target = null;
            for (Library.Platform q : lib.platforms) {
                if (q == p || !mergeable(q)) continue;
                if (to != null ? q.name.equals(to) : q.name.equalsIgnoreCase(p.name) && lib.platforms.indexOf(q) < lib.platforms.indexOf(p)) {
                    target = q;
                    break;
                }
            }
            if (target == null) continue;
            target.games.addAll(p.games);
            target.games.sort((a, b) -> col.compare(a.title, b.title));
            target.mergedFrom.add(p);
            lib.platforms.remove(p);
        }
    }

    private void remerge() {
        applyHiddenGames();
        shelfState.current = null;
        if (shelf != null) shelf.setLibrary(lib, recentGames());
    }

    @Override public void onMergePlatform(Library.Platform p, ShelfView.Menu parent) {
        if (shelf == null || lib == null) return;
        ShelfView.Menu m = new ShelfView.Menu(I18n.f(S.MERGE_INTO, p.title()), t(S.MERGE_HINT));
        m.parent = parent;
        for (Library.Platform q : lib.platforms) {
            if (q == p || !mergeable(q) || shelf.isPseudo(q)) continue;
            m.add(q.title() + "  ·  " + I18n.f(S.GAMES_COUNT, q.games.size()), false, () -> {
                prefs.edit().putString("merge:" + p.name, q.name).apply();
                shelf.closeMenu();
                remerge();
                Toast.makeText(this, I18n.f(S.MERGED, p.title(), q.title()), Toast.LENGTH_SHORT).show();
            });
        }
        for (Library.Platform src : p.mergedFrom) {
            if (!prefs.contains("merge:" + src.name)) continue;
            m.add(I18n.f(S.UNMERGE, src.title()), false, () -> {
                prefs.edit().remove("merge:" + src.name).apply();
                shelf.closeMenu();
                remerge();
            });
        }
        shelf.showMenu(m);
    }

    // ---- genre folder pictures ----

    @Override public void onEditGenre(Library.Platform p) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.ICON), p.title());
        m.add(t(S.AI_MAKE), false, () -> { shelf.closeMenu(); aiPicture(p, Theme.CONSOLE, () -> shelf.refresh()); });
        m.add(t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.CONSOLE));
        if (Theme.isCustom(p.name, Theme.CONSOLE)) m.add(t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.CONSOLE);
            Theme.forgetPlatform(p.name);
            shelf.closeMenu();
            shelf.refresh();
        });
        shelf.showMenu(m);
    }

    /** Settings → Tools: a clay icon for every genre folder that has no picture yet. */
    private void confirmGenreIcons() {
        if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
        List<String> ids = new ArrayList<>();
        for (Library.Platform g : shelfState.genres) if (!Theme.isCustom(g.name, Theme.CONSOLE)) ids.add(g.name.substring("genre-".length()));
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_GENRE_ICONS), ids.isEmpty() ? null : I18n.f(S.AI_PICTURES_CONFIRM, ids.size(), Math.max(1, ids.size() / 2)));
        m.parent = shelfState.menu;
        if (!ids.isEmpty()) m.add(t(S.GO), false, () -> { shelf.closeMenu(); runGenreIcons(ids); });
        // drawing them all again keeps one style across the set
        List<String> all = new ArrayList<>();
        for (Library.Platform g : shelfState.genres) all.add(g.name.substring("genre-".length()));
        if (!all.isEmpty()) m.add(I18n.f(S.REDRAW_ALL, all.size()), false, () -> { shelf.closeMenu(); runGenreIcons(all); });
        m.add(t(S.CANCEL), false, () -> shelf.showMenu(m.parent));
        shelf.showMenu(m);
    }

    private void runGenreIcons(List<String> ids) {
        AiImage ai = AiImage.from(prefs);
        if (ai == null || imageJob != null) return;
        java.util.concurrent.atomic.AtomicBoolean stop = new java.util.concurrent.atomic.AtomicBoolean();
        imageJob = stop;
        Toast.makeText(this, I18n.f(S.AI_PICTURES_PROGRESS, 0, ids.size()), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            int done = 0, failed = 0;
            for (int i = 0; i < ids.size() && !stop.get(); i++) {
                String name = "genre-" + ids.get(i);
                android.graphics.Bitmap b = ai.generate(AiImage.iconPrompt(Genres.subject(ids.get(i))), "1024x1024", true);
                boolean ok = false;
                if (b != null) {
                    try { ok = Theme.save(name, Theme.CONSOLE, new java.io.ByteArrayInputStream(AiImage.png(AiImage.cutOut(b)))); } catch (IOException ignored) { }
                }
                if (ok) done++; else failed++;
                if (!ok && done == 0 && failed >= 2) break;
                int n = i + 1;
                main.post(() -> {
                    Theme.forgetPlatform(name);
                    if (shelf != null) shelf.refresh();
                    Toast.makeText(this, I18n.f(S.AI_PICTURES_PROGRESS, n, ids.size()), Toast.LENGTH_SHORT).show();
                });
            }
            int made = done;
            String err = made < ids.size() && !stop.get() ? ai.error : null;
            main.post(() -> {
                imageJob = null;
                Toast.makeText(this, I18n.f(S.AI_PICTURES_DONE, made) + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                if (shelf != null) shelf.refresh();
            });
        }, "genre-icons").start();
    }

    private boolean gameTextRunning;

    private void runGameText(List<Library.Platform> ps, Ai ai, String lang) {
        shelf.closeMenu();
        if (gameTextRunning) { Toast.makeText(this, t(S.WORKING), Toast.LENGTH_SHORT).show(); return; }
        gameTextRunning = true;
        int total = 0;
        for (Library.Platform p : ps) total += GameText.todo(p, lang).size();
        int all = total;
        Toast.makeText(this, I18n.f(S.AI_GAME_TEXT_PROGRESS, 0, all), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            int done = 0, skipped = 0, seen = 0;
            outer:
            for (Library.Platform p : ps) {
                for (java.util.Map.Entry<Library.Platform, List<Library.Game>> e : byPlatform(GameText.todo(p, lang)).entrySet()) {
                    List<Library.Game> gs = e.getValue();
                    int base = seen;
                    int n = GameText.translate(ai, e.getKey(), gs, lang, k -> main.post(() ->
                            Toast.makeText(this, I18n.f(S.AI_GAME_TEXT_PROGRESS, base + k, all), Toast.LENGTH_SHORT).show()));
                    done += n;
                    skipped += GameText.skipped;
                    seen += gs.size();
                    if (GameText.gaveUp) break outer;   // failing again and again (a wrong key, no network): stop
                }
            }
            int finished = done, left = skipped;
            String err = GameText.gaveUp ? ai.error : null;
            main.post(() -> {
                gameTextRunning = false;
                Toast.makeText(this, I18n.f(S.TRANSLATED, finished) + (left > 0 ? "  ·  " + I18n.f(S.AI_SKIPPED, left) : "")
                        + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                if (finished > 0) load();
            });
        }, "game-text").start();
    }

    /** Same platform, same file size and the same name once tags and punctuation are ignored. */
    /** Downloads the covers that are missing, in the background. */
    private void fillCovers() {
        runInBackground(() -> {
            int n = Covers.fetchMissing(this, lib, pct -> { });
            return () -> {
                Toast.makeText(this, I18n.f(S.COVERS_DONE, n), Toast.LENGTH_LONG).show();
                if (n > 0) load();
            };
        });
    }

    // ---- the problem centre: everything that may need the player, gathered in one place ----

    private static final class Problems {
        Library lib;
        final List<Library.Game> unsorted = new ArrayList<>(), covers = new ArrayList<>(), dups = new ArrayList<>(), broken = new ArrayList<>();
        final List<Library.Platform> noEmu = new ArrayList<>();
        boolean perms;

        int total() { return unsorted.size() + covers.size() + dups.size() + broken.size() + noEmu.size() + (perms ? 1 : 0); }
    }

    /** The last check, for the count on the Tools row (shown only while it is about the library on screen). */
    private Problems lastProblems;

    /** Looks through the library; slow on a big card (it checks files), so never on the main thread. */
    private Problems findProblems(Library l) {
        Problems pr = new Problems();
        pr.lib = l;
        pr.perms = !Environment.isExternalStorageManager() || !hasUsageAccess();
        if (l == null) return pr;
        android.content.pm.PackageManager pm = getPackageManager();
        java.util.Set<Library.Game> seen = new HashSet<>();
        for (Library.Platform p : l.platforms) {
            if (p.isCustom() || p.system.equals("ANDROID") || p.system.equals("APPS") || ShelfView.isGenre(p)) continue;
            boolean unsorted = Library.UNSORTED.equals(p.system);
            for (Library.Game g : p.games) {
                if (!seen.add(g) || g.pkg != null || g.path == null) continue;
                if (unsorted) { pr.unsorted.add(g); continue; }
                // listed (by Pegasus or ES-DE) but not on the card, or an empty file; missing extra discs are
                // already left out by the scan
                if (!g.path.exists() || g.path.isFile() && g.path.length() == 0) pr.broken.add(g);
            }
            if (unsorted || p.games.isEmpty() || p.games.get(0).pkg != null) continue;
            // the console's emulator: the one chosen, else Pegasus' or the built-in default
            String cmd = platformCmd(p), pkg = Library.packageOf(cmd);
            boolean works = pkg != null ? Library.installed(pm, pkg) : cmd != null && !cmd.isEmpty();
            if (!works) pr.noEmu.add(p);
        }
        for (Library.Game g : Covers.missing(l)) if (!Library.UNSORTED.equals(g.platform.system)) pr.covers.add(g);
        pr.dups.addAll(findDuplicates(l));
        return pr;
    }

    /** Settings → Tools → Problem centre: checked again each time it is opened. */
    private void showProblems(ShelfView.Menu parent) {
        if (shelf == null) return;
        ShelfView.Menu wait = new ShelfView.Menu(t(S.PROBLEMS), t(S.CHECKING));
        wait.parent = parent;
        wait.add(t(S.CHECKING), false, null);
        shelf.showMenu(wait);
        Library l = lib;
        new Thread(() -> {
            Problems pr = findProblems(l);
            main.post(() -> {
                lastProblems = pr;
                if (shelf != null && shelfState.menu == wait) showProblems(parent, pr, 0);
            });
        }, "problems").start();
    }

    private void showProblems(ShelfView.Menu parent, Problems pr, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.PROBLEMS), null);
        m.parent = parent;
        m.reopen = () -> showProblems(parent);
        if (pr.total() == 0) m.add(t(S.NO_PROBLEMS), false, null);
        else m.add(t(S.PROBLEMS_HINT), false, null);   // none of it is urgent: said once, as the first row
        if (pr.perms) m.add(t(S.P_PERMS), false, () -> showPermissionsMenu(m)).hint(t(S.H_P_PERMS));
        if (!pr.unsorted.isEmpty()) m.add(t(S.P_UNSORTED) + "  ·  " + pr.unsorted.size(), false, () -> {
            shelf.closeMenu();
            shelf.openByName(Library.UNSORTED, 0);
        }).hint(t(S.H_P_UNSORTED));
        if (!pr.noEmu.isEmpty()) m.add(t(S.P_NO_EMU) + "  ·  " + pr.noEmu.size(), false, () -> {
            ShelfView.Menu pm = new ShelfView.Menu(t(S.P_NO_EMU), t(S.H_P_NO_EMU));
            pm.parent = m;
            for (Library.Platform p : pr.noEmu)
                pm.add(p.title() + "  ·  " + I18n.f(S.GAMES_COUNT, p.games.size()), false, () -> showEmulatorFix(pm, p));
            shelf.showMenu(pm);
        }).hint(t(S.H_P_NO_EMU));
        if (!pr.broken.isEmpty()) m.add(t(S.P_BROKEN) + "  ·  " + pr.broken.size(), false, () -> {
            shelf.closeMenu();
            shelf.showList(t(S.P_BROKEN), pr.broken, () -> showProblems(parent));
        }).hint(t(S.H_P_BROKEN));
        if (!pr.covers.isEmpty()) m.add(t(S.MISSING_COVERS) + "  ·  " + pr.covers.size(), false,
                () -> showMissingCovers(parent, pr, null)).hint(t(S.H_P_COVERS));
        if (!pr.dups.isEmpty()) m.add(t(S.DUPLICATES) + "  ·  " + pr.dups.size(), false, () -> {
            shelf.closeMenu();
            shelf.showList(t(S.DUPLICATES), pr.dups, () -> showProblems(parent));
        }).hint(t(S.H_P_DUPLICATES));
        m.sel = Math.max(pr.total() > 0 ? 1 : 0, Math.min(sel, m.labels.size() - 1));   // on the first problem, past the note
        shelf.showMenu(m);
    }

    /**
     * Problem centre → Games without covers: which ones, fetch them online, or draw exactly these with AI.
     * Everything happens here: after a fetch the check runs again and this menu comes back with the new count.
     * @param note a line saying how the last fetch went, or null
     */
    private void showMissingCovers(ShelfView.Menu problemsParent, Problems pr, String note) {
        ShelfView.Menu cm = new ShelfView.Menu(t(S.MISSING_COVERS), note != null ? note : t(S.H_P_COVERS));
        cm.parent = problemsParent;
        cm.onBack = () -> showProblems(problemsParent);
        int n = pr.covers.size();
        if (n == 0) cm.add(t(S.NO_PROBLEMS), false, null);
        else {
            cm.add(I18n.f(S.SHOW_WHICH, n), false, () -> { shelf.closeMenu(); shelf.showList(t(S.MISSING_COVERS), pr.covers, () -> showProblems(problemsParent)); });
            cm.add(t(S.FILL_COVERS), false, () -> fetchCoversHere(problemsParent, cm));
            cm.add(I18n.f(S.AI_DRAW_THESE, n), false, () -> {
                if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
                confirmAiPictures(cm, new ArrayList<>(pr.covers), "box_front");
            });
        }
        shelf.showMenu(cm);
    }

    /** Fetches covers while the menu says so, then checks again and shows the covers menu with the result. */
    private void fetchCoversHere(ShelfView.Menu problemsParent, ShelfView.Menu from) {
        ShelfView.Menu wait = new ShelfView.Menu(t(S.MISSING_COVERS), null);
        wait.parent = from;
        wait.add(t(S.FETCHING_COVERS), false, null);
        shelf.showMenu(wait);
        Library l = lib;
        new Thread(() -> {
            int got = Covers.fetchMissing(this, l, pct -> { });
            String note = got == 0 ? t(S.COVERS_NONE_ONLINE) : I18n.f(S.COVERS_DONE, got);
            // new covers sit in the games' own media folders, which only a scan picks up: check again after it
            Runnable recheck = () -> new Thread(() -> {
                Problems pr = findProblems(lib);
                main.post(() -> {
                    lastProblems = pr;
                    if (shelf != null && shelfState.menu == wait) showMissingCovers(problemsParent, pr, note);
                });
            }, "problems").start();
            main.post(() -> {
                if (got == 0) { recheck.run(); return; }
                afterLoad.add(recheck);
                quietScan = true;
                load();
            });
        }, "covers").start();
    }

    /** A console whose emulator is not installed: the installed ones to switch to, and the ones that would work. */
    private void showEmulatorFix(ShelfView.Menu parent, Library.Platform p) {
        ShelfView.Menu m = new ShelfView.Menu(p.title(), Library.emulatorLabel(platformCmd(p)) + "  ✕");
        m.parent = parent;
        android.content.pm.PackageManager pm = getPackageManager();
        for (String[] o : Library.options(p, pm))
            m.add(o[0] + "  ·  " + t(S.EMU_INSTALLED), false, () -> {
                onChoose(p, o[1]);
                Toast.makeText(this, p.title() + "  ·  " + o[0], Toast.LENGTH_SHORT).show();
                if (parent.parent != null && parent.parent.reopen != null) parent.parent.reopen.run(); else shelf.closeMenu();
            });
        int known = 0;
        for (String[] o : Library.knownOptions(p.system)) {
            if (Library.installed(pm, o[2])) continue;
            m.add(o[0] + "  ·  " + t(S.EMU_TO_INSTALL), false, null);
            known++;
        }
        if (known == 0 && m.labels.isEmpty()) m.add(t(S.EMU_NONE_KNOWN), false, null);
        m.add(t(S.OPEN_CONSOLE), false, () -> { shelf.closeMenu(); shelf.openByName(p.name, 0); });
        shelf.showMenu(m);
    }

    private static List<Library.Game> findDuplicates(Library lib) {
        java.util.Map<String, List<Library.Game>> groups = new java.util.LinkedHashMap<>();
        java.util.Set<Library.Game> seen = new HashSet<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            for (Library.Game g : p.games) {
                if (!seen.add(g) || g.path == null) continue;
                String norm = g.title.replaceAll("[\\(\\[（【][^\\)\\]）】]*[\\)\\]）】]", "").replaceAll("[\\s\\p{Punct}·：:！!]", "").toLowerCase(Locale.ROOT);
                String k = p.system + "|" + g.path.length() + "|" + norm;
                groups.computeIfAbsent(k, x -> new ArrayList<>()).add(g);
            }
        }
        List<Library.Game> out = new ArrayList<>();
        for (List<Library.Game> gs : groups.values()) if (gs.size() > 1) out.addAll(gs);
        return out;
    }

    private void showDiagnostics() {
        ShelfView.Menu m = new ShelfView.Menu(t(S.DIAGNOSTICS), "Duo Launcher " + versionName());
        m.parent = shelfState.menu;
        int games = 0;
        if (lib != null) for (Library.Platform p : lib.platforms) if (!p.isCustom()) games += p.games.size();
        m.add(I18n.f(S.PLATFORMS_COUNT, lib == null ? 0 : lib.platforms.size()) + "  ·  " + I18n.f(S.GAMES_COUNT, games), false, null);
        m.add("Scan: " + lastScanMs + " ms  ·  cache " + (new File(getFilesDir(), "library.cache").length() >> 10) + " KB", false, null);
        m.add("Icons: " + (dirSize(new File(getCacheDir(), "icons")) >> 10) + " KB", false, null);
        for (File v : volumeRoots()) m.add(v.getAbsolutePath() + "  ·  " + (v.getFreeSpace() >> 30) + " GB free / " + (v.getTotalSpace() >> 30) + " GB", false, null);
        String themeName = Theme.packDir() != null ? Theme.packDir().getName() : prefs.getString("theme", "");
        m.add("Theme: " + (themeName.isEmpty() ? "default" : themeName) + "  ·  Android " + android.os.Build.VERSION.RELEASE, false, null);
        shelf.showMenu(m);
    }

    private static long dirSize(File d) {
        File[] fs = d.listFiles();
        long n = 0;
        if (fs != null) for (File f : fs) n += f.isDirectory() ? dirSize(f) : f.length();
        return n;
    }

    private long lastScanMs, lastScanAt;
    /** Run once the scan under way (or the next one) is finished and on screen. */
    private final List<Runnable> afterLoad = new ArrayList<>();
    private int cardRetries;

    /** True when games of {@code prev} lived on a storage volume that is not there now (and the new scan has none from it). */
    private static boolean volumeMissing(Library prev, Library now) {
        java.util.Set<String> had = new HashSet<>(), have = new HashSet<>();
        for (Library.Platform p : prev.platforms) for (Library.Game g : p.games) if (g.path != null) had.add(volumeOf(g.path));
        for (Library.Platform p : now.platforms) for (Library.Game g : p.games) if (g.path != null) have.add(volumeOf(g.path));
        for (String v : had) {
            if (have.contains(v)) continue;
            String[] list = new File(v).list();
            if (list == null || list.length == 0) return true;
        }
        return false;
    }
    /** The next scan was not asked for: no "found N games" message. */
    private boolean quietScan;
    private static final long RESCAN_MS = 10 * 60_000;

    private void showButtonsMenu(int settingsRow) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.BUTTONS), null);
        m.parent = shelfState.menu;
        for (String[] o : new String[][]{{"swapAB", t(S.SWAP_AB)}, {"swapXY", t(S.SWAP_XY)}}) {
            boolean on = prefs.getBoolean(o[0], false);
            m.add(o[1] + "  ·  " + t(on ? S.ON : S.OFF), on, () -> {
                prefs.edit().putBoolean(o[0], !on).apply();
                reopenSettingsAt(settingsRow);
                showButtonsMenu(settingsRow);
            });
        }
        shelf.showMenu(m);
    }

    /** Shows Settings again (labels changed) with the cursor back on {@code row}. */
    private void reopenSettingsAt(int row) {
        onSettings();
    }

    /**
     * Every permission the launcher uses, with its state; picking one opens the system page for it.
     * Shown once after the first-run language choice and kept under Settings.
     */
    private void showPermissionsMenu(ShelfView.Menu parent) {
        if (shelf == null) return;
        boolean first = parent == null;
        ShelfView.Menu m = new ShelfView.Menu(t(S.PERMISSIONS), first ? t(S.PERM_INTRO) : null);
        m.parent = parent;
        boolean files = Environment.isExternalStorageManager(), usage = hasUsageAccess();
        m.add(t(S.PERM_FILES) + (files ? "  ·  " + t(S.PERM_ALLOWED) : ""), files, () -> {
            if (!files) startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        });
        m.add(t(S.PERM_USAGE) + (usage ? "  ·  " + t(S.PERM_ALLOWED) : ""), usage, () -> {
            if (usage) return;
            pendingPermissions = true;
            try {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });
        m.add(t(S.SET_HOME) + "  ·  " + t(S.OPTIONAL), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
        });
        // in the welcome guide: on to what was found, or out of the guide
        m.add(first ? t(S.NEXT) : t(S.DONE), false, () -> { pendingPermissions = false; if (first) showGuide(G_FOUND); else backToSettings(parent); });
        if (first) m.add(t(S.GUIDE_SKIP), false, () -> { pendingPermissions = false; endGuide(); });
        shelf.showMenu(m);
    }

    // ---- Duo Lite: one switch for less work on every screen ----

    private void applyLite() {
        boolean lite = prefs.getBoolean("lite", false);
        ShelfView.lite = lite;
        TopView.lite = lite;
        if (top != null) top.applyMotion(true);
        TopView.videoEnabled = prefs.getBoolean("videoPreview", true) && !lite;
    }

    // ---- where the launcher keeps its own data, and a copy of the settings and records ----

    private String dataHomeName() {
        switch (Storage.mode) {
            case Storage.CARD: return t(S.CARD_STORAGE);
            case Storage.INTERNAL: return t(S.INTERNAL_STORAGE);
            default: return t(S.HOME_AUTO);
        }
    }

    /** Settings → Saves, transfer, sharing → Where launcher data lives: the place, moving, and the copy. */
    private void showDataHome(int sel) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.DATA_HOME), Storage.general(volumeRoots()).getAbsolutePath());
        m.parent = settingsPage;
        m.onBack = () -> { shelf.closeMenu(); onSettings(); };
        String[][] modes = {{Storage.AUTO, t(S.HOME_AUTO), t(S.H_HOME_AUTO)},
                {Storage.CARD, t(S.CARD_STORAGE), Storage.card() == null ? t(S.NO_CARD) : t(S.H_HOME_CARD)},
                {Storage.INTERNAL, t(S.INTERNAL_STORAGE), t(S.H_HOME_INTERNAL)}};
        for (String[] md : modes) {
            int row = m.labels.size();
            m.add(md[1], md[0].equals(Storage.mode), () -> {
                prefs.edit().putString("dataHome", md[0]).apply();
                Storage.mode = md[0];
                showDataHome(row);
            }).hint(md[2]);
        }
        if (!Storage.mode.equals(Storage.AUTO)) m.add(t(S.MOVE_DATA), false, this::confirmMoveData).hint(t(S.H_MOVE_DATA));
        boolean auto = prefs.getBoolean("copy.auto", true);
        int autoRow = m.labels.size();
        m.add(t(S.AUTO_COPY) + "  ·  " + t(auto ? S.ON : S.OFF), false, () -> {
            prefs.edit().putBoolean("copy.auto", !auto).apply();
            showDataHome(autoRow);
        }).hint(t(S.H_AUTO_COPY));
        if (auto) {
            boolean keys = prefs.getBoolean("copy.keys", false);
            int keyRow = m.labels.size();
            m.add(t(S.AUTO_COPY_KEYS) + "  ·  " + t(keys ? S.ON : S.OFF), false, () -> {
                prefs.edit().putBoolean("copy.keys", !keys).apply();
                copyDirty = true;
                showDataHome(keyRow);
            }).hint(t(S.H_AUTO_COPY_KEYS));
            long last = prefs.getLong("copy.last", 0);
            String when = last == 0 ? t(S.COPY_NONE) : I18n.f(S.COPY_LAST,
                    android.text.format.DateFormat.getMediumDateFormat(this).format(new java.util.Date(last)) + " "
                            + android.text.format.DateFormat.getTimeFormat(this).format(new java.util.Date(last)));
            int nowRow = m.labels.size();
            m.add(t(S.COPY_NOW) + "  ·  " + when, false, () -> saveCopy(() -> {
                Toast.makeText(this, t(S.COPY_SAVED), Toast.LENGTH_SHORT).show();
                if (shelf != null && shelfState.menu != null && t(S.DATA_HOME).equals(shelfState.menu.title)) showDataHome(nowRow);
            }));
        }
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** Asks before moving the DuoLauncher folders of the other storages to the chosen one (size worked out first). */
    private void confirmMoveData() {
        Storage.init(this);   // a card put in since the launcher came to the front
        File target = Storage.root(null);
        List<File> from = Storage.others(volumeRoots(), target);
        runInBackground(() -> {
            long size = Storage.movable(from);
            return () -> {
                if (shelf == null) return;
                if (from.isEmpty() || size == 0) { Toast.makeText(this, t(S.NOTHING_TO_MOVE), Toast.LENGTH_LONG).show(); return; }
                ShelfView.Menu m = new ShelfView.Menu(I18n.f(S.MOVE_CONFIRM, gb(size)), target.getAbsolutePath());
                m.parent = shelfState.menu;
                m.add(t(S.CANCEL), false, () -> showDataHome(3));
                m.add(t(S.GO), false, () -> {
                    ShelfView.Menu wait = new ShelfView.Menu(t(S.MOVE_DATA), target.getAbsolutePath());
                    wait.add(t(S.MOVING), false, null);
                    shelf.showMenu(wait);
                    new Thread(() -> {
                        int moved = 0, left = 0;
                        for (File f : from) { int[] n = Storage.move(f, target); moved += n[0]; left += n[1]; }
                        int mv = moved, lf = left;
                        main.post(() -> {
                            Toast.makeText(this, I18n.f(S.MOVE_DONE, mv) + (lf > 0 ? "\n" + I18n.f(S.MOVE_LEFT, lf) : ""), Toast.LENGTH_LONG).show();
                            if (shelf != null && shelfState.menu == wait) showDataHome(3);
                            quietScan = true;
                            load();
                        });
                    }, "move-data").start();
                });
                shelf.showMenu(m);
            };
        });
    }

    /** Something worth copying changed since the last copy. */
    private volatile boolean copyDirty;
    private final SharedPreferences.OnSharedPreferenceChangeListener copyWatch = (p, k) -> {
        if (k != null && !k.startsWith("copy.") && Config.part(k) != null) copyDirty = true;
    };
    /** While the launcher is on screen, a changed setting or record is copied within about a minute. */
    private final Runnable copyTick = new Runnable() {
        @Override public void run() {
            // the first copy is made without waiting for a change, so existing setups get one too; never on a fresh
            // install before the welcome guide has asked about restoring, or the earlier install's copy would be replaced
            if (prefs.contains("lang") && (copyDirty || prefs.getLong("copy.last", 0) == 0) && prefs.getBoolean("copy.auto", true)) saveCopy(null);
            main.postDelayed(this, 60_000);
        }
    };

    /** Writes the copy of settings and records to DuoLauncher/profile/ (keys only when asked), keeping the newest three. */
    private void saveCopy(Runnable then) {
        java.util.Set<Config.Part> parts = java.util.EnumSet.of(Config.Part.SETTINGS, Config.Part.PLAY, Config.Part.NOTES, Config.Part.AI);
        if (prefs.getBoolean("copy.keys", false)) parts.add(Config.Part.KEYS);
        File dir = new File(Storage.general(volumeRoots()), "profile"), files = getFilesDir();
        copyDirty = false;
        new Thread(() -> {
            try {
                File out = Config.export(prefs, Theme.dir, files, new ArrayList<>(), parts, dir);
                // the newest three are kept; with keys switched off, no older copy keeps them either
                boolean keys = parts.contains(Config.Part.KEYS);
                List<File> all = Backups.list(dir);
                for (int i = 0; i < all.size(); i++) {
                    File f = all.get(i);
                    if (f.equals(out)) continue;
                    if (i >= 3 || (!keys && Config.partsIn(f).contains(Config.Part.KEYS))) f.delete();
                }
                prefs.edit().putLong("copy.last", System.currentTimeMillis()).apply();
            } catch (Exception e) {
                copyDirty = true;   // tried again later
            }
            if (then != null) main.post(then);
        }, "copy").start();
    }

    /** The newest copy of settings and records on any storage, or null. */
    private File findCopy() {
        File best = null;
        for (File v : volumeRoots())
            for (File f : Backups.list(new File(v, "DuoLauncher/profile")))
                if (Config.isConfig(f) && (best == null || f.lastModified() > best.lastModified())) best = f;
        return best;
    }

    private boolean restoreAsked;

    /** First start with a copy found: bring it back (choosing what), or start fresh with the guide. */
    private void showRestore(File copy) {
        top.showMessage(t(S.WELCOME));
        java.text.DateFormat df = android.text.format.DateFormat.getMediumDateFormat(this);
        ShelfView.Menu m = new ShelfView.Menu(t(S.RESTORE_FOUND), I18n.f(S.RESTORE_FROM, df.format(new java.util.Date(copy.lastModified()))));
        m.add(t(S.RESTORE_YES), false, () -> {
            java.util.Set<Config.Part> has = Config.partsIn(copy);
            if (has.isEmpty()) has = java.util.EnumSet.copyOf(Config.SHARE);
            showImportPartsMenu(m, copy, has, java.util.EnumSet.copyOf(has), 0);
        });
        m.add(t(S.RESTORE_NO), false, () -> showGuide(G_LANG));
        shelf.showMenu(m);
    }

    // ---- the welcome guide: language, permissions, what was found, the optional AI, the buttons ----

    private static final int G_LANG = 0, G_PERMS = 1, G_FOUND = 2, G_AI = 3, G_KEYS = 4;
    /** The guide page on screen, or -1. */
    private int guideStep = -1;

    /** Shows one page of the guide: the choices below, what they mean on the upper screen; B goes a page back. */
    private void showGuide(int step) {
        if (shelf == null) return;
        guideStep = step;
        if (step == G_LANG) {
            // a fresh install with a copy of earlier settings on a storage: offer it back first
            if (!prefs.contains("lang") && !restoreAsked) {
                File copy = findCopy();
                if (copy != null) { restoreAsked = true; showRestore(copy); return; }
            }
            showLanguageMenu(null, true);
            return;
        }
        if (step == G_PERMS) {
            top.showMessage(t(S.WELCOME));
            showPermissionsMenu(null);
            if (shelfState.menu != null) shelfState.menu.onBack = () -> showGuide(G_LANG);   // B: a page back, as on every page
            return;
        }
        ShelfView.Menu m;
        if (step == G_FOUND) {
            m = new ShelfView.Menu(t(S.GUIDE_FOUND), null);
            if (lib == null || loading) top.showText(t(S.GUIDE_SCANNING));
            else {
                int platforms = 0, games = 0;
                for (Library.Platform p : lib.platforms) {
                    if (p.isCustom() || p.system.equals("ANDROID") || p.system.equals("APPS") || Library.UNSORTED.equals(p.system) || p.games.isEmpty()) continue;
                    platforms++;
                    games += p.games.size();
                }
                top.showText(I18n.f(S.GUIDE_FOUND_TEXT, platforms, games));
            }
            m.add(t(S.NEXT), false, () -> showGuide(G_AI));
            m.add(t(S.PROBLEMS), false, () -> showProblems(m)).hint(t(S.H_PROBLEMS));
        } else if (step == G_AI) {
            m = new ShelfView.Menu(t(S.GUIDE_AI), null);
            top.showText(t(S.GUIDE_AI_TEXT));
            m.add(t(S.LATER), false, () -> showGuide(G_KEYS));
            m.add(t(S.GUIDE_AI_PHONE), false, this::showWifiMenu);
            m.add(t(S.GUIDE_AI_HERE), false, () -> showAiServices(m, 0));
        } else {
            m = new ShelfView.Menu(t(S.GUIDE_KEYS), null);
            top.showText(t(S.GUIDE_KEYS_TEXT));
            m.add(t(S.GUIDE_START), false, this::endGuide);
        }
        if (step != G_KEYS) m.add(t(S.GUIDE_SKIP), false, this::endGuide);
        m.reopen = () -> showGuide(step);
        m.onBack = () -> showGuide(step - 1);
        shelf.showMenu(m);
    }

    private void endGuide() {
        guideStep = -1;
        if (!prefs.contains("lang")) prefs.edit().putString("lang", I18n.code()).apply();
        shelf.closeMenu();
    }

    // ---- one-time tips: a line above the key legend the first time a feature is in reach ----

    /** Where the cursor was last time, so going back home from a folder can be noticed. */
    private boolean focusWasInFolder;

    private void maybeTip(Object item) {
        if (shelf == null) return;
        boolean inFolder = shelfState.current != null;
        if (!inFolder) {
            if (!tipOnce("home", S.TIP_HOME) && focusWasInFolder) tipOnce("homeSelect", S.TIP_HOME_SELECT);
        } else if (Library.UNSORTED.equals(shelfState.current.system)) {
            tipOnce("unsorted", S.TIP_UNSORTED);
        } else if (item instanceof Library.Game && shelf.canReorder()) {
            tipOnce("game", S.TIP_GAME);
        }
        focusWasInFolder = inFolder;
    }

    /** Shows a tip once ever (prefs "tip." + id), with tips on and nothing else in the way; false when it did not. */
    private boolean tipOnce(String id, S text) {
        if (!prefs.getBoolean("tips", true) || shelfState.menu != null || shelf.tipShowing() || prefs.getBoolean("tip." + id, false)) return false;
        prefs.edit().putBoolean("tip." + id, true).apply();
        shelf.showTip(t(text));
        return true;
    }

    private boolean anyTipSeen() {
        for (String k : prefs.getAll().keySet()) if (k.startsWith("tip.")) return true;
        return false;
    }

    /** Back from a system permission page: show the permission list again with the new state. */
    private boolean pendingPermissions;

    private void showLanguageMenu(ShelfView.Menu parent, boolean firstRun) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(firstRun ? t(S.WELCOME) : t(S.LANGUAGE), firstRun ? t(S.LANGUAGE) : null);
        m.parent = parent;
        for (int i = 0; i < I18n.CODES.length; i++) {
            String code = I18n.CODES[i];
            m.add(I18n.NAMES[i], i == I18n.index(), () -> {
                I18n.set(code);
                prefs.edit().putString("lang", code).apply();
                if (firstRun) showGuide(G_PERMS); else onSettings();
                if (shelfState.menu != null && !firstRun) onFocus(shelf.selected());
            });
        }
        if (firstRun) m.add(t(S.GUIDE_SKIP), false, this::endGuide);
        if (firstRun) top.showMessage(t(S.WELCOME));
        shelf.showMenu(m);
    }

    private void showAppsMenu(ShelfView.Menu parent, int sel) {
        if (shelf == null) return;
        Set<String> chosen = new HashSet<>(prefs.getStringSet("apps", new HashSet<>()));
        ShelfView.Menu m = new ShelfView.Menu(t(S.CHOOSE_APPS), t(S.APPS));
        m.parent = parent;
        List<String[]> apps = Library.launchableApps(this);
        for (int i = 0; i < apps.size(); i++) {
            String pkg = apps.get(i)[1];
            int idx = i;
            m.add(apps.get(i)[0], chosen.contains(pkg), () -> {
                if (!chosen.remove(pkg)) chosen.add(pkg);
                prefs.edit().putStringSet("apps", chosen).apply();
                showAppsMenu(parent, idx);
                load();
            });
        }
        m.sel = Math.max(0, Math.min(sel, apps.size() - 1));
        shelf.showMenu(m);
    }

    // ---- tidier names for games that only have their file name ----

    /** A built-in example per rule, for a library that has no name the rule would change. */
    private static final java.util.Map<String, String> NAME_SAMPLES = new java.util.HashMap<>();
    static {
        NAME_SAMPLES.put(NameClean.REGION, "Metroid Fusion (USA, Europe)");
        NAME_SAMPLES.put(NameClean.TAGS, "Super Mario World (Rev 1) [!]");
        NAME_SAMPLES.put(NameClean.NUMBER, "0153 - Golden Sun");
        NAME_SAMPLES.put(NameClean.SPACES, "Final_Fantasy_Tactics");
        NAME_SAMPLES.put(NameClean.ARTICLE, "Legend of Zelda, The - The Minish Cap");
        NAME_SAMPLES.put(NameClean.BRACKETS, "Advance Wars (Europe) (En,Fr,De) [b1]");
    }

    /** File-name titles of the library (the names these rules work on). */
    private List<String> fileTitles() {
        List<String> out = new ArrayList<>();
        if (lib == null) return out;
        Set<String> rules = NameClean.on;
        for (Library.Platform p : lib.platforms)
            for (Library.Game g : p.games) {
                if (g.pkg != null || g.path == null) continue;
                String n = g.path.getName(), ext = Library.extension(n);
                String base = ext.isEmpty() ? n : n.substring(0, n.length() - ext.length() - 1);
                String[] disc = Library.discTag(base);
                String b = disc != null ? disc[0] : base;
                // a game shown under its file name, tidied or not (names from Pegasus, ES-DE or the player stay out)
                if (g.title.equals(b) || g.title.equals(NameClean.apply(b, rules))) out.add(b);
            }
        return out;
    }

    private void showNameCleanMenu(ShelfView.Menu parent, int sel, boolean[] changed) {
        if (shelf == null) return;
        Set<String> on = new HashSet<>(prefs.getStringSet("nameClean", new HashSet<>()));
        List<String> names = fileTitles();
        int differ = 0;
        for (String n : names) if (!NameClean.apply(n, on).equals(n)) differ++;
        ShelfView.Menu m = new ShelfView.Menu(t(S.NAME_CLEAN), I18n.f(S.NAME_CLEAN_COUNT, differ, names.size()));
        m.parent = parent;
        S[] labels = {S.NC_REGION, S.NC_TAGS, S.NC_NUMBER, S.NC_SPACES, S.NC_ARTICLE, S.NC_BRACKETS};
        for (int i = 0; i < NameClean.RULES.size(); i++) {
            String rule = NameClean.RULES.get(i);
            int idx = i;
            boolean isOn = on.contains(rule);
            m.toggle(t(labels[i]), isOn, () -> {
                if (isOn) on.remove(rule); else on.add(rule);
                prefs.edit().putStringSet("nameClean", new HashSet<>(on)).apply();
                changed[0] = true;
                showNameCleanMenu(parent, idx, changed);
            });
            // live preview: the first of the player's own names this rule changes, with the other rules as they are
            Set<String> with = new HashSet<>(on);
            with.add(rule);
            Set<String> without = new HashSet<>(on);
            without.remove(rule);
            String example = null;
            for (String n : names)
                if (!NameClean.apply(n, with).equals(NameClean.apply(n, without))) { example = n; break; }
            if (example == null) example = NAME_SAMPLES.get(rule);
            m.hint(example + "  →  " + NameClean.apply(example, with));
        }
        int changes = differ;
        if (changes > 0) m.add(I18n.f(S.NC_SHOW_ALL, changes), false, () -> {
            ShelfView.Menu all = new ShelfView.Menu(I18n.f(S.NC_SHOW_ALL, changes), null);
            all.parent = m;
            for (String n : names) {
                String after = NameClean.apply(n, on);
                if (!after.equals(n)) all.add(after, false, null).hint(n);
            }
            shelf.showMenu(all);
        }).hint(t(S.H_NC_SHOW_ALL));
        m.sel = Math.min(sel, m.labels.size() - 1);
        // the names are given during the scan: look again once the player leaves, if anything changed
        m.onBack = () -> {
            shelf.closeMenu();
            if (changed[0]) { new File(getFilesDir(), "library.cache").delete(); load(); }
            onSettings();
        };
        shelf.showMenu(m);
    }

    private void showFoldersMenu(ShelfView.Menu parent) {
        if (shelf == null) return;
        boolean auto = prefs.getBoolean("autoFind", true);
        ShelfView.Menu m = new ShelfView.Menu(t(S.GAME_FOLDERS), null);
        m.parent = parent;
        m.add(t(S.AUTO_FIND) + "  ·  " + t(auto ? S.ON : S.OFF), false, () -> {
            prefs.edit().putBoolean("autoFind", !auto).apply();
            showFoldersMenu(parent);
            load();
        });
        for (String f : folders()) m.add("▸ " + f, false, () -> showFolderMenu(m, f));
        m.add(t(S.ADD_FOLDER), false, () -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), PICK_FOLDER));
        m.add(t(S.RESCAN), false, () -> {
            shelf.closeMenu();
            load();
        });
        shelf.showMenu(m);
    }

    private void showFolderMenu(ShelfView.Menu parent, String folder) {
        ShelfView.Menu m = new ShelfView.Menu(new java.io.File(folder).getName(), folder);
        m.parent = parent;
        m.add(t(S.REMOVE_FOLDER), false, () -> {
            List<String> fs = folders();
            fs.remove(folder);
            setFolders(fs);
            showFoldersMenu(parent.parent);
            load();
        });
        shelf.showMenu(m);
    }

    /** Turns a document-tree id such as "4A21-0000:Roms/GBA" or "primary:Games" into a file path. */
    private static String treeToPath(Uri tree) {
        String id = DocumentsContract.getTreeDocumentId(tree);
        int c = id.indexOf(':');
        if (c < 0) return null;
        String vol = id.substring(0, c), rel = id.substring(c + 1);
        String root = vol.equals("primary") ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/storage/" + vol;
        return rel.isEmpty() ? root : root + "/" + rel;
    }

    private void showAppearanceMenu(ShelfView.Menu parent) {
        if (shelf == null || lib == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.APPEARANCE), null);
        m.parent = parent;
        for (Library.Platform p : lib.platforms) m.add(p.title(), false, () -> showPlatformLookMenu(m, p));
        shelf.showMenu(m);
    }

    private void showPlatformLookMenu(ShelfView.Menu parent, Library.Platform p) {
        if (shelf == null) return;
        boolean customConsole = Theme.isCustom(p.name, Theme.CONSOLE), customBg = Theme.isCustom(p.name, Theme.BACKGROUND);
        String state = t(S.CONSOLE_IMAGE) + ": " + t(customConsole ? S.CUSTOM : S.DEFAULT)
                + "   " + t(S.BACKGROUND_IMAGE) + ": " + t(customBg ? S.CUSTOM : S.DEFAULT);
        ShelfView.Menu m = new ShelfView.Menu(p.title(), state);
        m.parent = parent;
        m.add(t(S.CONSOLE_IMAGE) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.CONSOLE));
        if (customConsole) m.add(t(S.CONSOLE_IMAGE) + " · " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.CONSOLE);
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
            showPlatformLookMenu(parent, p);
        });
        m.add(t(S.CONSOLE_IMAGE) + " · ✦ " + t(S.AI_MAKE), false, () -> aiPicture(p, Theme.CONSOLE, () -> showPlatformLookMenu(parent, p)));
        boolean customDark = Theme.isCustom(p.name, Theme.BACKGROUND_DARK);
        m.add(t(S.BACKGROUND_LIGHT) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.BACKGROUND));
        m.add(t(S.BACKGROUND_LIGHT) + " · ✦ " + t(S.AI_MAKE), false, () -> aiPicture(p, Theme.BACKGROUND, () -> showPlatformLookMenu(parent, p)));
        if (customBg) m.add(t(S.BACKGROUND_LIGHT) + " · " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.BACKGROUND);
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
            showPlatformLookMenu(parent, p);
        });
        m.add(t(S.BACKGROUND_DARK) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.BACKGROUND_DARK));
        m.add(t(S.BACKGROUND_DARK) + " · ✦ " + t(S.AI_FROM_LIGHT), false,
                () -> aiDarkCut(p.name, Theme.lightBackdrop(p.name, p.backdropType()), () -> showPlatformLookMenu(parent, p)));
        if (customDark) m.add(t(S.BACKGROUND_DARK) + " · " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.BACKGROUND_DARK);
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
            showPlatformLookMenu(parent, p);
        });
        if (!p.isCustom()) m.add(t(S.COLOR), false, p.accent(), null, () -> showPlatformColors(m, p));
        shelf.showMenu(m);
        // preview this platform on the upper screen while its look is being edited
        top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
    }

    /** Folder colour of a platform; stored as "pcolor:<name>", 0 = the theme's or built-in colour. */
    private void showPlatformColors(ShelfView.Menu parent, Library.Platform p) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.COLOR), p.title());
        m.parent = parent;
        int cur = prefs.getInt("pcolor:" + p.name, 0);
        m.add(t(S.RESET_DEFAULT), cur == 0, () -> setPlatformColor(p, 0, parent));
        for (int c : Theme.SWATCHES) m.add(String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF), c == cur, c, null, () -> setPlatformColor(p, c, parent));
        shelf.showMenu(m);
    }

    private void setPlatformColor(Library.Platform p, int c, ShelfView.Menu parent) {
        if (c == 0) prefs.edit().remove("pcolor:" + p.name).apply(); else prefs.edit().putInt("pcolor:" + p.name, c).apply();
        p.color = c;
        top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
        showPlatformLookMenu(parent.parent, p);
    }

    private void pickImage(Library.Platform p, String kind) {
        pickPlatform = p.name;
        pickKind = kind;
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
        startActivityForResult(it, PICK_IMAGE);
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_FOLDER && res == RESULT_OK && data != null && data.getData() != null) {
            String path = treeToPath(data.getData());
            List<String> fs = folders();
            if (path != null && !fs.contains(path)) {
                fs.add(path);
                setFolders(fs);
            }
            final ShelfView.Menu parent = shelfState.menu != null ? shelfState.menu.parent : null;
            main.post(() -> showFoldersMenu(parent));
            load();
            return;
        }
        if (req != PICK_IMAGE || res != RESULT_OK || data == null || data.getData() == null || pickPlatform == null) return;
        boolean ok = false;
        try (InputStream in = getContentResolver().openInputStream(data.getData())) {
            ok = in != null && Theme.save(pickPlatform, pickKind, in);
            if (ok) Theme.forgetPlatform(pickPlatform);
        } catch (Exception ignored) {
        }
        Toast.makeText(this, ok ? t(S.SAVED) : t(S.IMAGE_FAILED), Toast.LENGTH_SHORT).show();
        Library.Platform p = null;
        if (lib != null) for (Library.Platform x : lib.platforms) if (x.name.equals(pickPlatform)) p = x;
        final Library.Platform fp = p;
        final ShelfView.Menu parent = shelfState.menu != null ? shelfState.menu.parent : null;
        // the shelf is recreated in onResume; rebuild the menu once it is back
        if (fp != null && fp.isCustom()) {
            Theme.forgetPlatform(fp.name);
            main.post(() -> { foldersChanged(); Library.Platform f = folder(idOf(fp)); if (f != null) onEditFolder(f); });
        } else if (fp != null) {
            main.post(() -> showPlatformLookMenu(parent, fp));
        }
    }

    private String versionName() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    // ---- search ----

    /** Platform the search is limited to (opened from inside a platform), or null for all platforms. */
    private Library.Platform searchScope;

    /** The gamepad keyboard on the upper screen while searching, else null. */
    private PadKeyboard kb;
    /** What the search is for: typed text and a genre (either may be empty). */
    private String searchText = "", searchGenre;

    @Override public void onSearch() {
        if (lib == null || top == null) return;
        closeChat();
        TopView.extra.clear();
        Library.Platform cur = shelfState.current;
        if (cur != shelfState.results) searchScope = cur == null || cur == shelfState.recent || shelf.isPseudo(cur) ? null : cur;
        if (cur != shelfState.results) { searchText = ""; searchGenre = null; }
        prepareSearchIndex();
        java.util.Set<String> have = new HashSet<>();
        for (Library.Game g : lib.byKey.values()) if (g.genre != null) have.add(g.genre);
        List<String> genres = new ArrayList<>();
        for (String id : Genres.IDS) if (have.contains(id)) genres.add(id);
        kb = new PadKeyboard(this, searchText, searchGenre, searchHistory(), genres, new PadKeyboard.Host() {
            @Override public void onQuery(String text, String genre) {
                searchText = text;
                searchGenre = genre;
                search(text);
            }
            @Override public void onClose() { closeKeyboard(); }
            @Override public void onSystemKeyboard(String text) {
                closeKeyboard();
                systemSearch();
            }
            @Override public void onCue(int cue) { MainActivity.this.onCue(cue); }
            @Override public int resultCount() { return shelfState.results.games.size(); }
        });
        top.setOverlay(kb);
        shelfState.typing = true;
        if (shelf != null) shelf.invalidate();
        if (!searchText.isEmpty() || searchGenre != null) search(searchText);
    }

    private void closeKeyboard() {
        if (kb == null) return;
        rememberSearch(searchText.trim());
        kb = null;
        shelfState.typing = false;
        if (shelf != null) shelf.invalidate();
        stopRepeat();
        if (top != null) top.setOverlay(null);
        if (shelf != null) onFocus(shelf.selected());
    }

    /** The system's own keyboard (for typing Chinese characters), in a dialog on the upper screen. */
    private void systemSearch() {
        if (lib == null) return;
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        android.content.Context dc = new android.view.ContextThemeWrapper(this, theme);
        EditText field = new EditText(dc);
        field.setSingleLine(true);
        // keep the dialog visible above the keyboard instead of the full-screen landscape editor
        field.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_ACTION_DONE);
        field.setHint(t(S.SEARCH_HINT));
        field.setText(searchText);
        field.setSelection(field.getText().length());
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { searchText = s.toString(); search(searchText); }
        });
        // recent searches as one tappable row under the field
        android.widget.LinearLayout box = new android.widget.LinearLayout(dc);
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(field);
        List<String> history = searchHistory();
        if (!history.isEmpty()) {
            android.widget.HorizontalScrollView hs = new android.widget.HorizontalScrollView(dc);
            android.widget.LinearLayout row = new android.widget.LinearLayout(dc);
            for (String h : history) row.addView(chip(dc, h, () -> field.setText(h)));
            row.addView(chip(dc, "✕ " + t(S.CLEAR_HISTORY), () -> {
                prefs.edit().remove("searchHistory").apply();
                hs.setVisibility(android.view.View.GONE);
            }));
            hs.addView(row);
            box.addView(hs);
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this, theme)
                .setTitle(searchScope == null ? t(S.SEARCH) : t(S.SEARCH_IN) + "  " + searchScope.title())
                .setView(box)
                .setPositiveButton(t(S.DONE), null);
        if (searchScope != null) b.setNeutralButton(t(S.ALL_PLATFORMS), null);
        AlertDialog d = b.create();
        d.setOnShowListener(x -> {
            field.requestFocus();
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            if (searchScope != null) d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                searchScope = null;
                d.setTitle(t(S.SEARCH));
                v.setVisibility(android.view.View.GONE);
                search(field.getText().toString());
            });
        });
        d.setOnDismissListener(x -> rememberSearch(field.getText().toString().trim()));
        field.setOnEditorActionListener((v, action, ev) -> {
            d.dismiss();
            return true;
        });
        d.show();
        prepareSearchIndex();
    }

    private android.widget.TextView chip(android.content.Context c, String s, Runnable onTap) {
        android.widget.TextView v = new android.widget.TextView(c);
        float dp = getResources().getDisplayMetrics().density;
        v.setText(s);
        v.setTextSize(14);
        v.setPadding(Math.round(12 * dp), Math.round(6 * dp), Math.round(12 * dp), Math.round(6 * dp));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(16 * dp);
        bg.setColor(Palette.dark ? 0x33FFFFFF : 0x14000000);
        v.setBackground(bg);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, Math.round(8 * dp), Math.round(8 * dp), 0);
        v.setLayoutParams(lp);
        v.setOnClickListener(x -> onTap.run());
        return v;
    }

    private List<String> searchHistory() {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString("searchHistory", "").split("\n")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private void rememberSearch(String q) {
        if (q.isEmpty() || shelfState.results.games.isEmpty()) return;
        List<String> h = searchHistory();
        h.remove(q);
        h.add(0, q);
        while (h.size() > 8) h.remove(h.size() - 1);
        prefs.edit().putString("searchHistory", String.join("\n", h)).apply();
    }

    /** Pinyin of every title ("koudaiyaoguai") and its initials ("kdyg"), built once in the background. */
    private final java.util.Map<Library.Game, String[]> pinyin = new java.util.concurrent.ConcurrentHashMap<>();
    private boolean indexing;

    private void prepareSearchIndex() {
        if (indexing || lib == null) return;
        indexing = true;
        List<Library.Game> all = new ArrayList<>(lib.byKey.values());
        new Thread(() -> {
            android.icu.text.Transliterator tr = android.icu.text.Transliterator.getInstance("Han-Latin; Latin-ASCII; Lower");
            for (Library.Game g : all) {
                if (pinyin.containsKey(g)) continue;
                String full = tr.transliterate(g.title);
                StringBuilder ini = new StringBuilder(), joined = new StringBuilder(), words = new StringBuilder();
                for (String w : full.split("[^a-z0-9]+")) {
                    if (w.isEmpty()) continue;
                    ini.append(w.charAt(0));
                    joined.append(w);
                    words.append(w).append(' ');
                }
                pinyin.put(g, new String[]{joined.toString(), ini.toString(), words.toString().trim()});
            }
            main.post(() -> { indexing = false; if (shelfState.current == shelfState.results && shelfState.listTitle == null
                    && (!searchText.isEmpty() || searchGenre != null)) search(searchText); });   // an AI pick is no search: kept
        }, "pinyin").start();
    }

    /** True when q is the beginnings of consecutive syllables (or words): "mlao", "malao", "mla" all find "ma li ao". */
    private static boolean syllablePrefixes(String q, String[] syl) {
        for (int start = 0; start < syl.length; start++) if (prefixesFrom(q, 0, syl, start)) return true;
        return false;
    }

    private static boolean prefixesFrom(String q, int qi, String[] syl, int si) {
        if (qi == q.length()) return true;
        if (si >= syl.length) return false;
        String s = syl[si];
        for (int len = Math.min(s.length(), q.length() - qi); len >= 1; len--)
            if (s.regionMatches(0, q, qi, len) && prefixesFrom(q, qi + len, syl, si + 1)) return true;
        return false;
    }

    private void search(String q) {
        if (shelf == null || lib == null) return;
        String needle = q.trim().toLowerCase(Locale.ROOT);
        String genre = searchGenre;
        String label = genre == null ? q.trim() : Genres.name(genre) + (needle.isEmpty() ? "" : " · " + q.trim());
        if (needle.isEmpty() && genre == null) {
            shelf.showResults("", new ArrayList<>());
            return;
        }
        String compact = needle.replaceAll("[^a-z0-9]", "");
        boolean latin = compact.length() == needle.replace(" ", "").length();
        java.util.LinkedHashSet<Library.Game> hits = new java.util.LinkedHashSet<>();
        List<Library.Platform> where = new ArrayList<>();
        if (searchScope != null) where.add(searchScope); else where.addAll(lib.platforms);
        for (Library.Platform p : where)
            for (Library.Game g : p.games) {
                if (genre != null && !genre.equals(g.genre)) continue;
                if (needle.isEmpty()) { hits.add(g); continue; }
                if (g.title.toLowerCase(Locale.ROOT).contains(needle) || g.file.toLowerCase(Locale.ROOT).contains(needle)
                        || g.original != null && g.original.toLowerCase(Locale.ROOT).contains(needle)) { hits.add(g); continue; }
                // Latin letters also match Chinese titles by pinyin: "kdyg" or "koudai" finds 口袋妖怪
                String[] py = latin && !compact.isEmpty() ? pinyin.get(g) : null;
                if (py != null && (py[1].startsWith(compact) || py[1].contains(compact) && compact.length() >= 2 || py[0].contains(compact)
                        || compact.length() >= 2 && syllablePrefixes(compact, py[2].split(" ")))) hits.add(g);
            }
        shelf.showResults(label, new ArrayList<>(hits));
    }

    // ---- custom folders: stored as cf.ids = "id,id" and cf.<id>.name|color|art|bg|items ----

    private static final int[] FOLDER_COLORS = {0xFF6C5CE7, 0xFFE8463E, 0xFFF2A93B, 0xFF2E9E5B, 0xFF1FA99B,
            0xFF2F8BFF, 0xFF3A4FA3, 0xFFD6458F, 0xFF6D8299, 0xFF37474F};
    private static final String[] ART = {"custom", "recent", "android", "apps", "search", "settings",
            "3ds", "nds", "gba", "gbc", "gb", "nes", "snes", "n64", "gc", "wii", "md", "ps1", "ps2", "ps3", "ps4", "ps5",
            "psp", "psv", "switch", "xbox360", "xboxsx"};

    private List<String> folderIds() {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString("cf.ids", "").split(",")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private static String fk(String id, String k) { return "cf." + id + "." + k; }

    private static String idOf(Library.Platform p) { return p.name.substring("custom-".length()); }

    private List<String> folderItems(String id) {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString(fk(id, "items"), "").split("\n")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    /** Puts the user's folders (from prefs) at the front of the platform list. */
    private void applyCustomFolders() {
        if (lib == null) return;
        lib.platforms.removeIf(Library.Platform::isCustom);
        int at = 0;
        for (String id : folderIds()) {
            Library.Platform p = new Library.Platform();
            p.name = "custom-" + id;
            p.system = "CUSTOM";
            p.label = prefs.getString(fk(id, "name"), t(S.MY_FOLDER));
            p.color = prefs.getInt(fk(id, "color"), FOLDER_COLORS[0]);
            p.art = prefs.getString(fk(id, "art"), "custom");
            p.bgArt = prefs.getString(fk(id, "bg"), "custom");
            p.smart = prefs.getString(fk(id, "smart"), null);
            // a smart folder still called by its default name (in whatever language it was made) follows the UI language
            if (p.smart != null && I18n.anyLanguage(smartKey(p.smart), p.label)) p.label = smartName(p.smart);
            if (p.smart != null) p.games.addAll(smartGames(p.smart));
            else for (String k : folderItems(id)) {
                Library.Game g = lib.byKey.get(k);
                if (g != null && !hiddenGames.contains(k)) p.games.add(g);
            }
            lib.platforms.add(at++, p);
        }
    }

    private Library.Platform folder(String id) {
        if (lib != null) for (Library.Platform p : lib.platforms) if (p.name.equals("custom-" + id)) return p;
        return null;
    }

    private void foldersChanged() {
        applyCustomFolders();
        if (shelf != null) shelf.refresh();
    }

    /** A dialog's field, lined up with the dialog's title instead of touching its edges. */
    private android.view.View inset(android.view.View field) {
        android.widget.FrameLayout box = new android.widget.FrameLayout(field.getContext());
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(field);
        return box;
    }

    /** Text input on the upper screen. */
    private void askText(String title, String initial, java.util.function.Consumer<String> done) {
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        // the field takes its text colour from the dialog's theme, not the launcher's (which made it pale on a light dialog)
        EditText field = new EditText(new android.view.ContextThemeWrapper(this, theme));
        field.setSingleLine(true);
        field.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_ACTION_DONE);
        field.setText(initial);
        field.selectAll();
        AlertDialog d = new AlertDialog.Builder(this, theme).setTitle(title).setView(inset(field))
                .setPositiveButton(t(S.DONE), (x, w) -> {
                    String s = field.getText().toString().trim();
                    if (!s.isEmpty()) done.accept(s);
                }).create();
        d.setOnShowListener(x -> {
            field.requestFocus();
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });
        field.setOnEditorActionListener((v, a, e) -> {
            d.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            return true;
        });
        d.show();
    }

    private String createFolder(String name) {
        String id = Long.toString(System.currentTimeMillis(), 36);
        List<String> ids = folderIds();
        ids.add(id);
        prefs.edit().putString("cf.ids", String.join(",", ids)).putString(fk(id, "name"), name)
                .putInt(fk(id, "color"), FOLDER_COLORS[(ids.size() - 1) % FOLDER_COLORS.length]).apply();
        return id;
    }

    @Override public void onAddFolder() {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.NEW_FOLDER), t(S.SMART_HINT));
        m.add(t(S.PLAIN_FOLDER), false, this::addPlainFolder);
        String[] kinds = {"new", "unplayed", "most", "playing", "wish", "done", "top"};
        for (String k : kinds) m.add(smartName(k), false, () -> {
            String id = createFolder(smartName(k));
            prefs.edit().putString(fk(id, "smart"), k).putString(fk(id, "art"), k.equals("most") ? "recent" : "custom").apply();
            shelf.closeMenu();
            foldersChanged();
            Library.Platform p = folder(id);
            if (p != null) shelf.select(p);
        });
        shelf.showMenu(m);
    }

    private static String smartName(String kind) { return t(smartKey(kind)); }

    private static S smartKey(String kind) {
        switch (kind) {
            case "new": return S.SMART_NEW;
            case "unplayed": return S.SMART_UNPLAYED;
            case "playing": return S.STATUS_PLAYING;
            case "wish": return S.STATUS_WISH;
            case "done": return S.STATUS_DONE;
            case "top": return S.SMART_TOP;
            default: return S.SMART_MOST;
        }
    }

    /** Games for a smart folder: added in the last 30 days, never played, or the most played. */
    private List<Library.Game> smartGames(String kind) {
        List<Library.Game> out = new ArrayList<>();
        java.util.Set<Library.Game> seen = new HashSet<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            for (Library.Game g : p.games) if (g.pkg == null && seen.add(g)) out.add(g);
        }
        long now = System.currentTimeMillis();
        switch (kind) {
            case "new": {
                java.util.Map<String, Long> first = firstSeen();
                out.removeIf(g -> { Long t0 = first.get(g.key()); return t0 == null || now - t0 > 30L * 86_400_000L; });
                out.sort((a, b) -> Long.compare(first.get(b.key()), first.get(a.key())));
                break;
            }
            case "unplayed":
                out.removeIf(g -> prefs.getLong("time:" + g.key(), 0) > 0 || prefs.getLong("last:" + g.key(), 0) > 0);
                break;
            case "playing":
            case "wish":
            case "done":
                out.removeIf(g -> !kind.equals(prefs.getString("status:" + g.key(), "")));
                out.sort((a, b) -> Long.compare(prefs.getLong("last:" + b.key(), 0), prefs.getLong("last:" + a.key(), 0)));
                break;
            case "top":
                out.removeIf(g -> prefs.getInt("rating:" + g.key(), 0) < 4);
                out.sort((a, b) -> Integer.compare(prefs.getInt("rating:" + b.key(), 0), prefs.getInt("rating:" + a.key(), 0)));
                break;
            default:
                out.removeIf(g -> prefs.getLong("time:" + g.key(), 0) < 60_000);
                out.sort((a, b) -> Long.compare(prefs.getLong("time:" + b.key(), 0), prefs.getLong("time:" + a.key(), 0)));
                while (out.size() > 30) out.remove(out.size() - 1);
        }
        return out;
    }

    // ---- when each game was first seen, for "recently added" ----

    private java.util.Map<String, Long> firstSeenCache;

    private java.util.Map<String, Long> firstSeen() {
        if (firstSeenCache != null) return firstSeenCache;
        java.util.Map<String, Long> m = new java.util.HashMap<>();
        File f = new File(getFilesDir(), "firstseen.txt");
        if (f.isFile()) {
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) {
                    int tab = line.lastIndexOf('\t');
                    if (tab > 0) try { m.put(line.substring(0, tab), Long.parseLong(line.substring(tab + 1))); } catch (NumberFormatException ignored) { }
                }
            } catch (java.io.IOException ignored) {
            }
        }
        return firstSeenCache = m;
    }

    /** Notes games the launcher has not seen before: now, or on the very first run the file's own date. */
    private void noteNewGames() {
        if (lib == null) return;
        java.util.Map<String, Long> m = firstSeen();
        boolean firstRun = m.isEmpty();
        long now = System.currentTimeMillis();
        StringBuilder add = new StringBuilder();
        for (Library.Game g : lib.byKey.values()) {
            if (g.pkg != null) continue;
            Long seen = m.get(g.key());
            if (seen != null) { g.added = seen; continue; }
            long t0 = now;
            if (firstRun) t0 = g.path != null && g.path.lastModified() > 0 ? Math.min(now, g.path.lastModified()) : 0;
            m.put(g.key(), t0);
            g.added = t0;
            if (!firstRun) newGames.add(g);
            add.append(g.key()).append('\t').append(t0).append('\n');
        }
        if (add.length() == 0) return;
        try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(getFilesDir(), "firstseen.txt"), true), java.nio.charset.StandardCharsets.UTF_8)) {
            w.write(add.toString());
        } catch (java.io.IOException ignored) {
        }
    }

    private void addPlainFolder() {
        askText(t(S.NEW_FOLDER), t(S.MY_FOLDER), name -> {
            String id = createFolder(name);
            foldersChanged();
            Library.Platform p = folder(id);
            if (p != null) {
                shelf.select(p);
                onEditFolder(p);
            }
        });
    }

    @Override public void onEditFolder(Library.Platform pl) {
        if (shelf == null) return;
        String id = idOf(pl);
        ShelfView.Menu m = new ShelfView.Menu(t(S.EDIT_FOLDER), pl.title());
        if (pl.smart != null) m.add(t(S.SMART_RULE) + "  ·  " + smartName(pl.smart), false, null);
        List<Library.Game> fresh = shelf.newIn(pl);
        if (!fresh.isEmpty()) m.add(I18n.f(S.MARK_ALL_SEEN, fresh.size()), false, () -> { shelf.closeMenu(); onMarkSeen(fresh); });
        m.add(t(S.RENAME), false, () -> askText(t(S.FOLDER_NAME), pl.title(), name -> {
            prefs.edit().putString(fk(id, "name"), name).apply();
            foldersChanged();
            Library.Platform p = folder(id);
            if (p != null) onEditFolder(p);
        }));
        m.add(t(S.COLOR), false, pl.color, null, () -> showFolderColors(m, id));
        m.add(t(S.ICON), false, null, Theme.console(pl), () -> showFolderArt(m, id, true, 0));
        m.add(t(S.BACKGROUND_IMAGE), false, null, Theme.background(pl), () -> showFolderArt(m, id, false, 0));
        m.add(t(S.DELETE_FOLDER), false, () -> {
            ShelfView.Menu sure = new ShelfView.Menu(t(S.DELETE_FOLDER), pl.title());
            sure.parent = m;
            sure.add("✕  " + t(S.DELETE_FOLDER), false, () -> {
                List<String> ids = folderIds();
                ids.remove(id);
                SharedPreferences.Editor e = prefs.edit().putString("cf.ids", String.join(",", ids));
                for (String k : new String[]{"name", "color", "art", "bg", "items", "smart"}) e.remove(fk(id, k));
                e.apply();
                Theme.reset(pl.name, Theme.CONSOLE);
                Theme.reset(pl.name, Theme.BACKGROUND);
                shelf.closeMenu();
                foldersChanged();
            });
            sure.add(t(S.BACK), false, () -> onEditFolder(pl));
            shelf.showMenu(sure);
        });
        shelf.showMenu(m);
    }

    private void showFolderColors(ShelfView.Menu parent, String id) {
        int cur = prefs.getInt(fk(id, "color"), FOLDER_COLORS[0]);
        ShelfView.Menu m = new ShelfView.Menu(t(S.COLOR), null);
        m.parent = parent;
        for (int col : FOLDER_COLORS) {
            m.add(String.format(Locale.ROOT, "#%06X", col & 0xFFFFFF), col == cur, col, null, () -> {
                prefs.edit().putInt(fk(id, "color"), col).apply();
                foldersChanged();
                Library.Platform p = folder(id);
                if (p != null) onEditFolder(p);
            });
        }
        shelf.showMenu(m);
    }

    private static String artLabel(String art) {
        switch (art) {
            case "custom": return t(S.MY_FOLDER);
            case "recent": return t(S.RECENT);
            case "android": return t(S.ANDROID_GAMES);
            case "apps": return t(S.APPS);
            case "search": return t(S.SEARCH);
            case "settings": return t(S.SETTINGS);
            case "xboxsx": return "Xbox Series X";
            default: return Library.platformTitle(art.toUpperCase(Locale.ROOT));
        }
    }

    /** Pick the folder's icon ({@code icon}) or backdrop from the bundled art, or an own picture. */
    private void showFolderArt(ShelfView.Menu parent, String id, boolean icon, int sel) {
        Library.Platform pl = folder(id);
        if (pl == null) return;
        String key = icon ? "art" : "bg", cur = prefs.getString(fk(id, key), "custom");
        String kind = icon ? Theme.CONSOLE : Theme.BACKGROUND;
        boolean own = Theme.isCustom(pl.name, kind);
        ShelfView.Menu m = new ShelfView.Menu(icon ? t(S.ICON) : t(S.BACKGROUND_IMAGE), pl.title());
        m.parent = parent;
        m.add(t(S.PICK_IMAGE), own, () -> pickImage(pl, kind));
        m.add(t(S.AI_MAKE), false, () -> aiPicture(pl, kind, () -> { foldersChanged(); showFolderArt(parent, id, icon, 0); }));
        if (own) m.add(t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(pl.name, kind);
            foldersChanged();
            showFolderArt(parent, id, icon, 1);
        });
        int base = m.labels.size();
        for (int i = 0; i < ART.length; i++) {
            String art = ART[i];
            Bitmap pic = icon ? Theme.toolIcon(art) : Theme.toolBackground(art);
            int idx = base + i;
            m.add(artLabel(art), !own && art.equals(cur), null, pic, () -> {
                prefs.edit().putString(fk(id, key), art).apply();
                Theme.forgetPlatform(pl.name);
                foldersChanged();
                showFolderArt(parent, id, icon, idx);
            });
        }
        if (sel > 0) m.sel = Math.min(sel, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    @Override public void onAddToFolder(Library.Game g, ShelfView.Menu parent) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.ADD_TO_FOLDER), g.title);
        m.parent = parent;
        for (String id : folderIds()) {
            if (prefs.contains(fk(id, "smart"))) continue;
            List<String> items = folderItems(id);
            boolean in = items.contains(g.key());
            m.add(prefs.getString(fk(id, "name"), t(S.MY_FOLDER)), in, prefs.getInt(fk(id, "color"), FOLDER_COLORS[0]), null, () -> {
                List<String> now = folderItems(id);
                if (!now.remove(g.key())) {
                    now.add(g.key());
                    Toast.makeText(this, I18n.f(S.ADDED_TO, prefs.getString(fk(id, "name"), "")), Toast.LENGTH_SHORT).show();
                }
                prefs.edit().putString(fk(id, "items"), String.join("\n", now)).apply();
                foldersChanged();
                onAddToFolder(g, parent);
            });
        }
        m.add(t(S.NEW_FOLDER), false, () -> askText(t(S.NEW_FOLDER), t(S.MY_FOLDER), name -> {
            String id = createFolder(name);
            prefs.edit().putString(fk(id, "items"), g.key()).apply();
            Toast.makeText(this, I18n.f(S.ADDED_TO, name), Toast.LENGTH_SHORT).show();
            foldersChanged();
            onAddToFolder(g, parent);
        }));
        shelf.showMenu(m);
    }

    @Override public void onHideFolder(Library.Platform folder) {
        shelfState.hidden.add(folder.name);
        prefs.edit().putStringSet("hidden", new HashSet<>(shelfState.hidden)).apply();
        if (shelf != null) shelf.refresh();
    }

    /** Settings list of platform folders; ✓ = hidden, picking one toggles it. */
    private void showHiddenMenu(ShelfView.Menu parent, int sel) {
        if (shelf == null || lib == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.HIDDEN_FOLDERS), null);
        m.parent = parent;
        List<Library.Platform> ps = new ArrayList<>();
        for (Library.Platform p : lib.platforms) if (!p.isCustom()) ps.add(p);
        for (int i = 0; i < ps.size(); i++) {
            Library.Platform p = ps.get(i);
            int idx = i;
            m.add(p.title(), shelfState.hidden.contains(p.name), p.accent(), null, () -> {
                if (!shelfState.hidden.remove(p.name)) shelfState.hidden.add(p.name);
                prefs.edit().putStringSet("hidden", new HashSet<>(shelfState.hidden)).apply();
                shelf.refresh();
                showHiddenMenu(parent, idx);
            });
        }
        // the built-in home tiles, after the platforms
        String[] ids = {"continue", "recent", "favorites", "add", "search", "stats", "storage", "random", "settings", "desktop"};
        S[] names = {S.CONTINUE, S.RECENT, S.FAVORITES, S.NEW_FOLDER, S.SEARCH, S.STATS, S.STORAGE, S.RANDOM_GAME, S.SETTINGS, S.ANDROID_DESKTOP};
        for (int i = 0; i < ids.length; i++) {
            String key = "tool:" + ids[i];
            int idx = ps.size() + i;
            m.add(ShelfView.toolName(ids[i], t(names[i])), shelfState.hidden.contains(key), null, null, () -> {
                if (!shelfState.hidden.remove(key)) shelfState.hidden.add(key);
                prefs.edit().putStringSet("hidden", new HashSet<>(shelfState.hidden)).apply();
                shelf.refresh();
                showHiddenMenu(parent, idx);
            });
        }
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    @Override public void onRemoveFromFolder(Library.Platform folder, Library.Game g) {
        String id = idOf(folder);
        List<String> items = folderItems(id);
        items.remove(g.key());
        prefs.edit().putString(fk(id, "items"), String.join("\n", items)).apply();
        foldersChanged();
    }

    // ---- shelf callbacks ----

    // ---- 3DS banner music: the game's own HOME-menu tune, once, shortly after it is selected ----

    private android.media.AudioTrack bannerTrack;
    private int bannerSeq;
    private Sfx sfx;

    /** (Re)creates UI sounds and music for the active theme. */
    private void loadSounds() {
        if (sfx != null) sfx.release();
        sfx = new Sfx(this, Theme.packDir());
        sfx.enabled = prefs.getBoolean("sounds", true);
        sfx.musicEnabled = prefs.getBoolean("music", true);
    }

    private static final S[] PRESET_NAMES = {S.THEME_BLUE, S.THEME_RED, S.THEME_GREEN, S.THEME_PURPLE, S.THEME_ORANGE, S.THEME_PINK, S.THEME_GRAY};

    /** Default look, built-in colour themes, own colours, then theme packs from DuoLauncher/themes. */
    private void showThemeMenu(ShelfView.Menu parent, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.THEME), null);
        m.parent = parent;
        String cur = prefs.getString("theme", "");
        m.add(t(S.DEFAULT), cur.isEmpty(), 0xFF2F8BFF, null, () -> applyTheme("", 0));
        for (int i = 0; i < Theme.PRESETS.length; i++) {
            String id = "preset:" + i;
            int row = m.labels.size();
            m.add(t(PRESET_NAMES[i]), cur.equals(id), Theme.PRESETS[i][0], null, () -> applyTheme(id, row));
        }
        int customRow = m.labels.size();
        m.add(t(S.CUSTOM_COLORS), cur.equals("custom"), prefs.getInt("custom.accent", Theme.PRESETS[0][0]), null,
                () -> showCustomColors(customRow));
        for (File f : Theme.packs(this)) {
            int row = m.labels.size();
            m.add(Theme.packName(f), f.getName().equals(cur), () -> applyTheme(f.getName(), row));
        }
        if (sel >= 0) m.sel = Math.min(sel, m.labels.size() - 1);
        shelf.showMenu(m);
    }

    private void showThemeMenu(ShelfView.Menu parent) { showThemeMenu(parent, -1); }

    /** Own colour theme: a main colour (selection, buttons) and a background colour (both screens). */
    private void showCustomColors(int themeRow) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.CUSTOM_COLORS), null);
        int accent = prefs.getInt("custom.accent", Theme.PRESETS[0][0]), base = prefs.getInt("custom.base", Theme.PRESETS[0][1]);
        m.add(t(S.MAIN_COLOR), false, accent, null, () -> pickCustomColor("custom.accent", t(S.MAIN_COLOR), themeRow));
        m.add(t(S.BASE_COLOR), false, base, null, () -> pickCustomColor("custom.base", t(S.BASE_COLOR), themeRow));
        m.parent = shelfState.menu;
        shelf.showMenu(m);
    }

    private void pickCustomColor(String key, String title, int themeRow) {
        ShelfView.Menu m = new ShelfView.Menu(title, null);
        m.parent = shelfState.menu;
        int cur = prefs.getInt(key, key.equals("custom.accent") ? Theme.PRESETS[0][0] : Theme.PRESETS[0][1]);
        for (int c : Theme.SWATCHES) {
            m.add(String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF), c == cur, c, null, () -> {
                prefs.edit().putInt(key, c).apply();
                applyTheme("custom", themeRow);
                showCustomColors(themeRow);
            });
        }
        shelf.showMenu(m);
    }

    private void applyTheme(String name, int row) {
        prefs.edit().putString("theme", name).apply();
        Theme.init(this);
        loadSounds();
        sfx.resumeMusic();
        onSettings();
        showThemeMenu(shelfState.menu, row);
        if (shelf != null) onFocus(shelf.selected());
        if (top != null) top.invalidate();
    }

    private void stopBanner() {
        bannerSeq++;
        if (bannerTrack != null) {
            try { bannerTrack.stop(); } catch (IllegalStateException ignored) { }
            bannerTrack.release();
            bannerTrack = null;
        }
    }

    private void playBanner(Library.Game g) {
        String e = g.path == null ? "" : g.ext();
        if (!prefs.getBoolean("bannerMusic", true) || prefs.getBoolean("lite", false) || !(e.equals("3ds") || e.equals("cci") || e.equals("cxi") || e.equals("app"))) return;
        if (TopView.wantsVideo(g)) return; // the video brings its own sound
        if (chat != null) return;          // the AI chat is open: its spoken reply must be heard
        int seq = bannerSeq;
        main.postDelayed(() -> {
            if (seq != bannerSeq || chat != null) return;
            new Thread(() -> {
                BannerSound s = BannerSound.load(g.path);
                if (s != null) main.post(() -> {
                    if (seq == bannerSeq && pres != null) {
                        try { bannerTrack = s.play(); } catch (RuntimeException ignored) { }
                    }
                });
            }, "banner").start();
        }, 450);
    }

    @Override public void onFocus(Object item) {
        stopBanner();
        if (item == ShelfView.CONTINUE) item = shelfState.recent.games.isEmpty() ? null : shelfState.recent.games.get(0);
        if (item instanceof Library.Game) playBanner((Library.Game) item);
        if (item == ShelfView.STATS) {
            top.showStats(collectStats());
        } else if (item == ShelfView.STORAGE) {
            TopView.storageInfo = storageLines();
            top.showTool(item);
        } else if (item == ShelfView.SETTINGS || item == ShelfView.SEARCH || item == ShelfView.DESKTOP || item == ShelfView.RANDOM) {
            top.showTool(item);
        } else if (item instanceof Library.Game) {
            Library.Game g = (Library.Game) item;
            String emu = g.pkg != null ? null : Library.emulatorLabel(effectiveCmd(g));
            top.showGame(g, emu, playedTags(g));
            lookUpLastShot(g);
        } else if (item instanceof Library.Platform) {
            Library.Platform p = (Library.Platform) item;
            if (ShelfView.isGenre(p)) top.showPlatform(p, playTimeOf(p.games), "genre");
            else top.showPlatform(p, Library.emulatorLabel(platformCmd(p)),
                    p == shelfState.recent ? "recent" : p == shelfState.favorites ? "favorites" : p == shelfState.results ? "search" : null);
        }
        maybeTip(item);
    }

    // ---- files no folder name or content could place: the player says which console they are for ----

    private void loadChosenSystems() {
        java.util.Map<String, String> m = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, ?> e : prefs.getAll().entrySet())
            if (e.getKey().startsWith("sysOf:") && e.getValue() instanceof String) m.put(e.getKey().substring(6), (String) e.getValue());
        Library.chosenSystems = m;
    }

    @Override public void onPickSystem(Library.Game g) {
        if (shelf == null || g.path == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.PICK_SYSTEM), g.path.getName());
        String cur = prefs.getString("sysOf:" + g.path.getAbsolutePath(), null);
        for (String key : Library.systemKeys())
            m.add(Library.platformTitle(key), key.equals(cur), () -> {
                prefs.edit().putString("sysOf:" + g.path.getAbsolutePath(), key).apply();
                shelf.closeMenu();
                Toast.makeText(this, I18n.f(S.SYSTEM_PICKED, Library.platformTitle(key)), Toast.LENGTH_SHORT).show();
                new File(getFilesDir(), "library.cache").delete();
                load();
            });
        if (cur != null) m.add(t(S.RESET_DEFAULT), false, () -> {
            prefs.edit().remove("sysOf:" + g.path.getAbsolutePath()).apply();
            shelf.closeMenu();
            new File(getFilesDir(), "library.cache").delete();
            load();
        });
        shelf.showMenu(m);
    }

    @Override public void onActivate(Library.Game g) {
        if (Library.UNSORTED.equals(g.platform.system)) { onPickSystem(g); return; }   // no console yet: say which first
        try {
            Intent it;
            String pkg;
            if (g.pkg != null) {
                it = getPackageManager().getLaunchIntentForPackage(g.pkg);
                if (it == null) return;
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                pkg = g.pkg;
            } else {
                String cmd = effectiveCmd(g);
                if (cmd == null || cmd.isEmpty()) {
                    Toast.makeText(this, t(S.NO_EMULATOR), Toast.LENGTH_LONG).show();
                    return;
                }
                if (g.path != null && !g.path.exists()) {
                    Toast.makeText(this, I18n.f(S.FILE_MISSING, g.path.getAbsolutePath()), Toast.LENGTH_LONG).show();
                    return;
                }
                it = Launch.build(cmd, g);
                pkg = it.getComponent() != null ? it.getComponent().getPackageName() : it.getPackage() != null ? it.getPackage() : "";
                if (!pkg.isEmpty()) {
                    try {
                        getPackageManager().getPackageInfo(pkg, 0);
                    } catch (android.content.pm.PackageManager.NameNotFoundException nf) {
                        Toast.makeText(this, I18n.f(S.EMULATOR_MISSING, Library.emulatorLabel(cmd), pkg), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            addRecent(g);
            prefs.edit().putString("running", g.key() + "\n" + System.currentTimeMillis() + "\n" + pkg).apply();
            ActivityOptions o = ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY);
            startActivity(it, o.toBundle());
        } catch (android.content.ActivityNotFoundException e) {
            prefs.edit().remove("running").apply();
            Toast.makeText(this, t(S.EMULATOR_CANT_OPEN), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            prefs.edit().remove("running").apply();
            Toast.makeText(this, I18n.f(S.LAUNCH_FAILED, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void addRecent(Library.Game g) {
        List<String> keys = new ArrayList<>();
        keys.add(g.key());
        for (String k : prefs.getString("recent", "").split("\n"))
            if (!k.isEmpty() && !k.equals(g.key()) && keys.size() < MAX_RECENT) keys.add(k);
        prefs.edit().putString("recent", String.join("\n", keys)).apply();
    }

    private List<Library.Game> recentGames() {
        List<Library.Game> out = new ArrayList<>();
        if (lib == null) return out;
        for (String k : prefs.getString("recent", "").split("\n")) {
            Library.Game g = lib.byKey.get(k);
            if (g != null && !hiddenGames.contains(k)) out.add(g);
        }
        return out;
    }

    // ---- play statistics ----

    private StatsView.Stats collectStats() {
        StatsView.Stats s = new StatsView.Stats();
        List<Library.Game> played = new ArrayList<>();
        java.util.Map<Library.Platform, Long> perPlatform = new java.util.LinkedHashMap<>();
        if (lib != null) {
            java.util.Set<Library.Game> seen = new HashSet<>();
            for (Library.Platform p : lib.platforms) {
                if (p.isCustom()) continue;
                for (Library.Game g : p.games) {
                    if (!seen.add(g)) continue;
                    long ms = prefs.getLong("time:" + g.key(), 0);
                    if (ms <= 0) continue;
                    s.total += ms;
                    played.add(g);
                    Long cur = perPlatform.get(p);
                    perPlatform.put(p, (cur == null ? 0 : cur) + ms);
                }
            }
        }
        played.sort((a, b) -> Long.compare(prefs.getLong("time:" + b.key(), 0), prefs.getLong("time:" + a.key(), 0)));
        for (int i = 0; i < Math.min(5, played.size()); i++) {
            s.top.add(played.get(i));
            s.topTimes.add(prefs.getLong("time:" + played.get(i).key(), 0));
        }
        List<java.util.Map.Entry<Library.Platform, Long>> ps = new ArrayList<>(perPlatform.entrySet());
        ps.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        for (java.util.Map.Entry<Library.Platform, Long> e : ps) {
            s.platformNames.add(e.getKey().title());
            s.platformTimes.add(e.getValue());
            s.platformColors.add(e.getKey().accent());
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat key = new java.text.SimpleDateFormat("yyyyMMdd", Locale.ROOT);
        java.text.SimpleDateFormat label = new java.text.SimpleDateFormat("EEE", Locale.forLanguageTag(I18n.code()));
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            s.week[i] = prefs.getLong("day:" + key.format(cal.getTime()), 0);
            s.weekLabels[i] = label.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        return s;
    }

    /** A on the statistics tile: every played game, most played first. */
    /**
     * A game to play: from every platform except Android apps, games never started from here three times as
     * likely, and not one of the last ten played.
     */
    @Override public void onRandom() {
        if (lib == null) return;
        java.util.Set<Library.Game> recentSet = new java.util.HashSet<>();
        for (int i = 0; i < Math.min(10, shelfState.recent.games.size()); i++) recentSet.add(shelfState.recent.games.get(i));
        List<Library.Game> pool = new ArrayList<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom() || shelfState.hidden.contains(p.name)) continue;
            for (Library.Game g : p.games) {
                if (g.pkg != null || recentSet.contains(g) || hiddenGames.contains(g.key())) continue;
                pool.add(g);
                if (prefs.getLong("time:" + g.key(), 0) == 0) { pool.add(g); pool.add(g); }
            }
        }
        if (pool.isEmpty()) return;
        Library.Game g = pool.get(new java.util.Random().nextInt(pool.size()));
        shelf.reveal(g);
    }

    @Override public char initial(Library.Game g) {
        String[] py = pinyin.get(g);
        String s = py != null && !py[1].isEmpty() ? py[1] : g.title.trim();
        char c = s.isEmpty() ? '#' : Character.toUpperCase(s.charAt(0));
        return c >= 'A' && c <= 'Z' ? c : '#';
    }

    @Override public void onGameOrder(String platform, String order) {
        if (order.isEmpty()) prefs.edit().remove("order:" + platform).apply();
        else prefs.edit().putString("order:" + platform, order).apply();
    }

    @Override public void onHomeOrder(String order) {
        prefs.edit().putString("homeOrder", order).apply();
    }

    @Override public void onHomeMode(int mode) {
        prefs.edit().putInt("homeMode", mode).remove("homeByGenre").apply();
        if (mode == 0) { Toast.makeText(this, t(S.HOME_BY_PLATFORM), Toast.LENGTH_SHORT).show(); return; }
        boolean any = mode == 1 ? !shelfState.genres.isEmpty() && lib != null && hasGenres() : !shelfState.series.isEmpty();
        Toast.makeText(this, any ? t(mode == 1 ? S.HOME_BY_GENRE : S.HOME_BY_SERIES) : t(mode == 1 ? S.GENRES_EMPTY : S.SERIES_EMPTY),
                any ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    private boolean hasGenres() {
        for (Library.Game g : lib.byKey.values()) if (g.genre != null) return true;
        return false;
    }

    /** Y → Genre on a game: pick one of the genres by hand. */
    @Override public void onPickGenre(Library.Game g, ShelfView.Menu parent) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.GENRE), g.title);
        m.parent = parent;
        List<String> ids = new ArrayList<>(java.util.Arrays.asList(Genres.IDS));
        ids.add(null);
        for (String id : ids) {
            m.add(Genres.name(id), java.util.Objects.equals(id, g.genre), Genres.color(id), null, () -> {
                if (!Genres.set(g, id)) {
                    Toast.makeText(this, t(S.IMAGE_FAILED), Toast.LENGTH_SHORT).show();
                    return;
                }
                shelf.closeMenu();
                shelf.refresh();
                Toast.makeText(this, t(S.GENRE) + " · " + Genres.name(id), Toast.LENGTH_SHORT).show();
            });
        }
        shelf.showMenu(m);
    }

    /** Total play time of these games ("played 12 h 5 min"), or null when none was played. */
    private String playTimeOf(List<Library.Game> games) {
        long ms = 0;
        for (Library.Game g : games) ms += prefs.getLong("time:" + g.key(), 0);
        if (ms < 60_000) return null;
        long min = ms / 60_000;
        return min >= 60 ? I18n.f(S.PLAYED_HM, min / 60, min % 60) : I18n.f(S.PLAYED_M, min);
    }

    @Override public void onStorageTile() { showStorage(); }

    /** Free space of every volume that holds games, one line each; also sets how full the card is for the tile. */
    private String[] storageLines() {
        java.util.Set<String> vols = new java.util.LinkedHashSet<>();
        if (lib != null) for (Library.Game g : lib.byKey.values()) if (g.pkg == null && g.path != null) vols.add(volumeOf(g.path));
        List<String> out = new ArrayList<>();
        for (String v : vols) {
            try {
                android.os.StatFs fs = new android.os.StatFs(v);
                out.add(I18n.f(S.STORAGE_FREE, v.startsWith("/storage/emulated") ? t(S.INTERNAL_STORAGE) : t(S.CARD_STORAGE),
                        gb(fs.getAvailableBytes()), gb(fs.getTotalBytes())));
                if (!v.startsWith("/storage/emulated") && fs.getTotalBytes() > 0)
                    ShelfView.storageUsed = 1f - (float) fs.getAvailableBytes() / fs.getTotalBytes();
            } catch (Exception ignored) {
            }
        }
        return out.toArray(new String[0]);
    }

    @Override public void onStats() {
        if (shelf == null || lib == null) return;
        List<Library.Game> played = new ArrayList<>();
        java.util.Set<Library.Game> seen = new HashSet<>();
        for (Library.Platform p : lib.platforms)
            for (Library.Game g : p.games) if (seen.add(g) && prefs.getLong("time:" + g.key(), 0) > 0) played.add(g);
        played.sort((a, b) -> Long.compare(prefs.getLong("time:" + b.key(), 0), prefs.getLong("time:" + a.key(), 0)));
        shelf.showList(t(S.TOP_GAMES), played);
    }

    // ---- favorites: game keys in prefs "favorites", newest first ----

    private List<String> favoriteKeys() {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString("favorites", "").split("\n")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private void applyFavorites() {
        shelfState.favorites.games.clear();
        if (lib == null) return;
        for (String k : favoriteKeys()) {
            Library.Game g = lib.byKey.get(k);
            if (g != null && !hiddenGames.contains(k)) shelfState.favorites.games.add(g);
        }
    }

    @Override public boolean isFavorite(Library.Game g) { return favoriteKeys().contains(g.key()); }

    @Override public void onToggleFavorite(Library.Game g) {
        List<String> keys = favoriteKeys();
        if (!keys.remove(g.key())) keys.add(0, g.key());
        prefs.edit().putString("favorites", String.join("\n", keys)).apply();
        applyFavorites();
        if (shelf != null) shelf.refresh();
    }

    @Override public long sortValue(Library.Game g, int mode) {
        return prefs.getLong((mode == ShelfView.SORT_PLAYTIME ? "time:" : "last:") + g.key(), 0);
    }

    @Override public void onCue(int cue) {
        if (chat != null || (speaker != null && speaker.speaking())) return;   // keep the spoken AI reply clear
        if (sfx != null) sfx.play(cue);
    }

    // ---- hidden games: keys in prefs "hiddenGames"; each platform keeps its full list for unhiding ----

    private void applyHiddenGames() {
        if (lib == null) return;
        unmerge();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            List<Library.Game> all = fullLists.computeIfAbsent(p, k -> new ArrayList<>(k.games));
            p.games.clear();
            for (Library.Game g : all) if (!hiddenGames.contains(g.key())) p.games.add(g);
        }
        applyMerges();
    }

    private void hiddenGamesChanged() {
        prefs.edit().putStringSet("hiddenGames", new HashSet<>(hiddenGames)).apply();
        applyHiddenGames();
        applyCustomFolders();
        applyFavorites();
        if (shelf != null) {
            shelf.setRecent(recentGames());
            shelf.refresh();
        }
    }

    // ---- 3DS cheats, shared by Azahar and Citra ----

    @Override public void onCheats(Library.Game g) {
        ShelfView.Menu parent = shelfState.menu;
        String cmd = effectiveCmd(g);
        if (PspCheats.isPpsspp(cmd) && g.path != null) {
            runInBackground(() -> {
                String id = PspId.of(g.path);
                Cheats.Store st = id == null ? null : PspCheats.store(this, g, id);
                List<Cheats.Cheat> list = st == null ? null : st.load();
                return () -> {
                    if (st == null) { Toast.makeText(this, t(S.NO_TITLE_ID), Toast.LENGTH_LONG).show(); return; }
                    showCheats(g, st, list, parent, 0);
                };
            });
            return;
        }
        if (NdsCheats.isDrastic(cmd) && g.path != null) {
            Cheats.Store st = NdsCheats.store(this, g);
            runInBackground(() -> {
                List<Cheats.Cheat> list = st.load();
                return () -> showCheats(g, st, list, parent, 0);
            });
            return;
        }
        if (!g.platform.system.equals("3DS")) {
            if (!RaCheats.isRetroArch(cmd)) { Toast.makeText(this, t(S.CHEATS_RETROARCH_ONLY), Toast.LENGTH_LONG).show(); return; }
            Cheats.Store st = RaCheats.store(this, g, cmd);
            runInBackground(() -> {
                List<Cheats.Cheat> list = st.load();
                return () -> showCheats(g, st, list, parent, 0);
            });
            return;
        }
        runInBackground(() -> {
            String tid = Cheats.titleId(g);
            List<Cheats.Cheat> list = tid == null ? null : Cheats.load(tid);
            return () -> {
                if (tid == null) { Toast.makeText(this, t(S.NO_TITLE_ID), Toast.LENGTH_LONG).show(); return; }
                if (Cheats.dirs().isEmpty()) { Toast.makeText(this, t(S.NO_CHEAT_EMULATOR), Toast.LENGTH_LONG).show(); return; }
                showCheats(g, Cheats.citra(tid), list, parent, 0);
            };
        });
    }

    private void showCheats(Library.Game g, Cheats.Store st, List<Cheats.Cheat> list, ShelfView.Menu parent, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.CHEATS), g.title);
        m.parent = parent;
        if (list.isEmpty()) m.add(t(S.NO_CHEATS), false, null);
        for (int i = 0; i < list.size(); i++) {
            Cheats.Cheat c = list.get(i);
            int row = i;
            boolean code = c.props.containsKey("code");
            for (String l : c.lines) if (!l.startsWith("*")) { code = true; break; }
            if (!code) { m.add("— " + c.name + " —", false, null); continue; }   // a heading in the file
            m.add(c.name, c.enabled, () -> {
                c.enabled = !c.enabled;
                if (!st.save(list)) {
                    c.enabled = !c.enabled;
                    Toast.makeText(this, t(S.CHEATS_TOO_MANY), Toast.LENGTH_LONG).show();
                }
                showCheats(g, st, list, parent, row);
            });
        }
        String lang = Translate.target();
        Ai ai = Ai.from(prefs);
        boolean english = false, translatedBefore = false;
        for (Cheats.Cheat c : list) {
            if (Cheats.untranslated(c)) english = true;
            if (Cheats.original(c) != null) translatedBefore = true;
        }
        // with an AI service, names translated before by the free services can be translated again
        if (lang != null && (english || (ai != null && translatedBefore)))
            m.add("文 " + t(ai != null ? S.AI_TRANSLATE_CHEATS : S.TRANSLATE_CHEATS), false, () -> runInBackground(() -> {
            Translate tr = new Translate(new java.io.File(getFilesDir(), "translations.tsv"), lang, ai);
            tr.context = g.title;
            int[] r = Cheats.translate(st, tr);
            List<Cheats.Cheat> fresh = st.load();
            String err = tr.aiError();
            return () -> {
                Toast.makeText(this, I18n.f(S.TRANSLATED, r[0]) + (r[1] > 0 ? "  ·  " + I18n.f(S.NOT_TRANSLATED, r[1]) : "")
                        + (err != null ? "\n" + I18n.f(S.AI_FAILED, err) : ""), Toast.LENGTH_LONG).show();
                showCheats(g, st, fresh, parent, 0);
            };
        }));
        if (!list.isEmpty()) m.add(t(S.EDIT_CHEATS), false, () -> showCheatEditor(g, st, list, parent, 0));
        m.add(t(S.NEW_CHEAT), false, () -> askText(t(S.CHEAT_NAME), "", name -> askCodes(st.strictCodes(), codes -> {
            Cheats.Cheat c = st.newCheat(name, codes);
            list.add(c);
            st.save(list);
            showCheats(g, st, list, parent, list.size() - 1);
        })));
        if (!list.isEmpty()) m.add(t(S.DELETE_ALL_CHEATS), false, () -> {
            ShelfView.Menu sure = new ShelfView.Menu(I18n.f(S.DELETE_ALL_SURE, list.size()), g.title);
            sure.parent = shelfState.menu;
            sure.add(t(S.CANCEL), false, () -> showCheats(g, st, list, parent, 0));
            sure.add(t(S.DELETE_ALL_CHEATS), false, () -> {
                st.save(new ArrayList<>());
                Toast.makeText(this, t(S.DELETED), Toast.LENGTH_SHORT).show();
                showCheats(g, st, st.load(), parent, 0);
            });
            shelf.showMenu(sure);
        });
        m.add("⇣ " + t(S.DOWNLOAD_CHEATS), false, () -> runInBackground(() -> {
            // the store adds what it found; take those back out and let the user choose which to keep
            List<Cheats.Cheat> before = st.load();
            int n = st.download();
            if (n <= 0) return () -> Toast.makeText(this, n < 0 ? t(S.CHEATS_NOT_FOUND) : I18n.f(S.CHEATS_ADDED, 0), Toast.LENGTH_LONG).show();
            java.util.Set<String> had = new java.util.HashSet<>();
            for (Cheats.Cheat c : before) had.add(c.name);
            List<Cheats.Cheat> found = new ArrayList<>();
            for (Cheats.Cheat c : st.load()) {
                boolean code = c.props.containsKey("code");
                for (String l : c.lines) if (!l.startsWith("*")) { code = true; break; }
                if (code && !had.contains(c.name)) { c.enabled = false; found.add(c); }
            }
            st.save(before);
            CheatPick pick = new CheatPick(g, st, before, found, parent);
            return () -> { showCheatPick(pick, 0); translatePick(pick); };
        }));
        if (!st.strictCodes()) m.add(t(S.RETROARCH_CHEAT_HINT), false, null);
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** Cheats just downloaded, waiting for the user to choose which to import; names shown translated when possible. */
    private static final class CheatPick {
        final Library.Game g;
        final Cheats.Store st;
        final List<Cheats.Cheat> have, found;
        final ShelfView.Menu parent;
        final String[] shown;
        final boolean[] picked;
        ShelfView.Menu menu;
        boolean translating;

        CheatPick(Library.Game g, Cheats.Store st, List<Cheats.Cheat> have, List<Cheats.Cheat> found, ShelfView.Menu parent) {
            this.g = g;
            this.st = st;
            this.have = have;
            this.found = found;
            this.parent = parent;
            shown = new String[found.size()];
            picked = new boolean[found.size()];
        }

        int count() { int n = 0; for (boolean b : picked) if (b) n++; return n; }
    }

    private static final int PICK_HEAD = 3;   // import, select all, select none

    private void showCheatPick(CheatPick p, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.PICK_CHEATS), p.g.title + "  ·  " + I18n.f(S.PICKED, p.count(), p.found.size())
                + (p.translating ? "  ·  " + t(S.TRANSLATING) : ""));
        m.parent = p.parent;
        m.onBack = () -> showCheats(p.g, p.st, p.st.load(), p.parent, 0);
        m.add("⇣ " + I18n.f(S.IMPORT_PICKED, p.count()), false, () -> {
            if (p.count() == 0) return;
            List<Cheats.Cheat> list = new ArrayList<>(p.have);
            for (int i = 0; i < p.found.size(); i++) {
                if (!p.picked[i]) continue;
                Cheats.Cheat c = p.found.get(i);
                if (p.shown[i] != null && !p.shown[i].equals(c.name)) {
                    c.lines.add(0, "*" + c.name);   // the original name stays as a note
                    c.name = p.shown[i];
                }
                list.add(c);
            }
            st(p).save(list);
            Toast.makeText(this, I18n.f(S.CHEATS_ADDED, p.count()), Toast.LENGTH_LONG).show();
            p.menu = null;
            showCheats(p.g, p.st, p.st.load(), p.parent, p.have.size());
        });
        m.add("☑ " + t(S.SELECT_ALL), false, () -> { java.util.Arrays.fill(p.picked, true); showCheatPick(p, 1); });
        m.add("☐ " + t(S.SELECT_NONE), false, () -> { java.util.Arrays.fill(p.picked, false); showCheatPick(p, 2); });
        for (int i = 0; i < p.found.size(); i++) {
            int row = i;
            m.add(p.shown[i] != null ? p.shown[i] : p.found.get(i).name, p.picked[i], () -> {
                p.picked[row] = !p.picked[row];
                showCheatPick(p, PICK_HEAD + row);
            });
        }
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        if (p.menu != null && shelfState.menu == p.menu) m.top = p.menu.top;   // a refresh keeps the scroll position
        p.menu = m;
        shelf.showMenu(m);
    }

    private static Cheats.Store st(CheatPick p) { return p.st; }

    /** Translates the found names into the interface language in the background, refreshing the list as they come. */
    private void translatePick(CheatPick p) {
        String lang = Translate.target();
        if (lang == null) return;
        List<String> names = new ArrayList<>();
        for (Cheats.Cheat c : p.found) if (Translate.needs(c.name)) names.add(c.name);
        if (names.isEmpty()) return;
        p.translating = true;
        refreshPick(p);
        new Thread(() -> {
            Translate tr = new Translate(new File(getFilesDir(), "translations.tsv"), lang, Ai.from(prefs));
            tr.context = p.g.title;
            tr.prefetch(names);   // with an AI service: all of them in a few requests
            for (int i = 0; i < p.found.size(); i++) {
                String n = p.found.get(i).name;
                if (!Translate.needs(n)) continue;
                String z = tr.translate(n);
                if (z != null) p.shown[i] = z;
                if (i % 12 == 11) main.post(() -> refreshPick(p));
            }
            p.translating = false;
            String err = tr.aiError();
            main.post(() -> {
                refreshPick(p);
                if (err != null) Toast.makeText(this, I18n.f(S.AI_FAILED, err), Toast.LENGTH_LONG).show();
            });
        }, "pick-translate").start();
    }

    /** Redraws the picker with new names if it is still the menu on screen, keeping the cursor where it is. */
    private void refreshPick(CheatPick p) {
        if (p.menu == null || shelf == null || shelfState.menu != p.menu) return;
        showCheatPick(p, p.menu.sel);
    }

    /** Rename or delete single cheats. */
    private void showCheatEditor(Library.Game g, Cheats.Store st, List<Cheats.Cheat> list, ShelfView.Menu parent, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.EDIT_CHEATS), g.title);
        m.parent = parent;
        m.onBack = () -> showCheats(g, st, list, parent, 0);   // back to the cheat list, with the new names
        for (int i = 0; i < list.size(); i++) {
            Cheats.Cheat c = list.get(i);
            int row = i;
            m.add(c.name, false, () -> {
                ShelfView.Menu one = new ShelfView.Menu(c.name, g.title);
                one.parent = shelfState.menu;
                one.add(t(S.RENAME), false, () -> askText(t(S.CHEAT_NAME), c.name, name -> {
                    c.name = name;
                    st.save(list);
                    showCheatEditor(g, st, list, parent, row);
                }));
                one.add(t(S.DELETE_CHEAT), false, () -> {
                    list.remove(c);
                    st.save(list);
                    showCheatEditor(g, st, list, parent, Math.max(0, row - 1));
                });
                shelf.showMenu(one);
            });
        }
        m.sel = Math.max(0, Math.min(sel, m.labels.size() - 1));
        shelf.showMenu(m);
    }

    /** Several lines of cheat code, "XXXXXXXX YYYYYYYY" each; anything else is refused with a hint. */
    private void askCodes(boolean strict, java.util.function.Consumer<List<String>> done) {
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        EditText field = new EditText(new android.view.ContextThemeWrapper(this, theme));
        field.setSingleLine(false);
        field.setMinLines(4);
        field.setHint(strict ? "12345678 0000FFFF" : "82025F42 0063");
        field.setTypeface(android.graphics.Typeface.MONOSPACE);
        field.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        AlertDialog d = new AlertDialog.Builder(this, theme).setTitle(t(S.CHEAT_CODES)).setView(inset(field))
                .setPositiveButton(t(S.DONE), null).setNegativeButton(t(S.CANCEL), null).create();
        d.setOnShowListener(x -> {
            field.requestFocus();
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                List<String> lines = new ArrayList<>();
                for (String l : field.getText().toString().toUpperCase(Locale.ROOT).split("\\n")) {
                    String c = l.trim().replaceAll("\\s+", " ");
                    if (c.isEmpty()) continue;
                    if (strict && !c.matches("[0-9A-F]{8} [0-9A-F]{8}")) { field.setError(t(S.BAD_CODES)); return; }
                    lines.add(c);
                }
                if (lines.isEmpty()) { field.setError(t(S.BAD_CODES)); return; }
                d.dismiss();
                done.accept(lines);
            });
        });
        d.show();
    }

    @Override public void onDisplayModeChanged(Library.Game g) {
        top.reset();
        onFocus(shelf.selected());
    }

    // ---- NEW badges: games marked as seen are kept in files/seennew.txt (one key per line) ----

    private void loadSeenNew() {
        ShelfView.newBadges = prefs.getBoolean("newBadges", true);
        ShelfView.seenNew.clear();
        File f = new File(getFilesDir(), "seennew.txt");
        if (!f.isFile()) return;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) if (!line.isEmpty()) ShelfView.seenNew.add(line);
        } catch (java.io.IOException ignored) {
        }
    }

    @Override public void onMarkSeen(List<Library.Game> games) {
        StringBuilder add = new StringBuilder();
        int n = 0;
        for (Library.Game g : games) if (ShelfView.seenNew.add(g.key())) { add.append(g.key()).append('\n'); n++; }
        if (add.length() > 0) {
            try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(getFilesDir(), "seennew.txt"), true), java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(add.toString());
            } catch (java.io.IOException ignored) {
            }
        }
        Toast.makeText(this, I18n.f(S.MARKED_SEEN, n), Toast.LENGTH_SHORT).show();
        if (shelf != null) shelf.invalidate();
        if (top != null && shelf != null) { top.reset(); onFocus(shelf.selected()); }
    }

    /** Every game that shows a NEW badge right now. */
    private List<Library.Game> allNewGames() {
        List<Library.Game> out = new ArrayList<>();
        if (lib == null || shelf == null) return out;
        for (Library.Game g : lib.byKey.values()) if (g.pkg == null && shelf.isNew(g)) out.add(g);
        return out;
    }

    @Override public void onHideGame(Library.Game g) {
        hiddenGames.add(g.key());
        hiddenGamesChanged();
    }

    /** Settings list of hidden games and apps; picking one shows it again. */
    private void showHiddenGamesMenu(ShelfView.Menu parent, int sel) {
        if (shelf == null || lib == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.HIDDEN_GAMES), null);
        m.parent = parent;
        List<String> keys = new ArrayList<>();
        for (String k : hiddenGames) if (lib.byKey.containsKey(k)) keys.add(k);
        keys.sort((a, b) -> lib.byKey.get(a).title.compareTo(lib.byKey.get(b).title));
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Library.Game g = lib.byKey.get(k);
            int idx = i;
            m.add(g.title + "  ·  " + g.platform.title(), true, () -> {
                hiddenGames.remove(k);
                hiddenGamesChanged();
                if (hiddenGames.isEmpty()) onSettings(); else showHiddenGamesMenu(parent, idx);
            });
        }
        m.sel = Math.max(0, Math.min(sel, keys.size() - 1));
        shelf.showMenu(m);
    }

    // ---- play time ----

    private boolean hasUsageAccess() {
        AppOpsManager ops = getSystemService(AppOpsManager.class);
        return ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

    /** Time {@code pkg} spent in the foreground between two moments, from the system's usage events. */
    private long foregroundTime(String pkg, long from, long to) {
        UsageStatsManager um = getSystemService(UsageStatsManager.class);
        UsageEvents events = um.queryEvents(from, to);
        UsageEvents.Event e = new UsageEvents.Event();
        long total = 0, since = -1;
        while (events.hasNextEvent()) {
            events.getNextEvent(e);
            if (!pkg.equals(e.getPackageName())) continue;
            int type = e.getEventType();
            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (since < 0) since = e.getTimeStamp();
            } else if ((type == UsageEvents.Event.ACTIVITY_PAUSED || type == UsageEvents.Event.ACTIVITY_STOPPED) && since >= 0) {
                total += e.getTimeStamp() - since;
                since = -1;
            }
        }
        if (since >= 0) total += to - since;
        return total;
    }

    /** Called when we come back to the front: adds the finished session to the game's play time. */
    private void finishPlaySession() {
        String running = prefs.getString("running", null);
        if (running == null) return;
        prefs.edit().remove("running").apply();
        String[] parts = running.split("\n");
        if (parts.length < 3) return;
        long start, now = System.currentTimeMillis();
        try { start = Long.parseLong(parts[1]); } catch (NumberFormatException e) { return; }
        long spent = now - start;
        Library.Game ran = lib != null ? lib.byKey.get(parts[0]) : null;
        if (spent < 6000 && spent >= 0 && ran != null && ran.pkg == null) {
            // back within a few seconds: the emulator most likely could not open the game
            Toast.makeText(this, t(S.QUIT_AT_ONCE), Toast.LENGTH_LONG).show();
        }
        if (hasUsageAccess() && !parts[2].isEmpty()) {
            try { spent = foregroundTime(parts[2], start, now); } catch (Exception ignored) { }
        }
        spent = Math.max(0, Math.min(spent, MAX_SESSION));
        String key = parts[0];
        // per-day totals feed the "this week" chart on the statistics page
        String day = "day:" + new java.text.SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(new Date(now));
        prefs.edit().putLong("time:" + key, prefs.getLong("time:" + key, 0) + spent).putLong("last:" + key, now)
                .putLong(day, prefs.getLong(day, 0) + spent).apply();
        // the last sessions' times, so screenshots taken while the game ran can be found again
        List<String> ses = new ArrayList<>(java.util.Arrays.asList(prefs.getString("sessions:" + key, "").split(",")));
        ses.removeIf(String::isEmpty);
        ses.add(start + "-" + now);
        while (ses.size() > 40) ses.remove(0);
        prefs.edit().putString("sessions:" + key, String.join(",", ses)).apply();
        int restHours = prefs.getInt("restHours", 2);
        if (restHours > 0 && spent >= restHours * 3_600_000L) {
            long min = spent / 60_000;
            Toast.makeText(this, I18n.f(S.REST_REMIND, min / 60, min % 60), Toast.LENGTH_LONG).show();
        }
        lastShots.remove(parts[0]);
        // play time since the last save backup, for the reminder
        long unbacked = prefs.getLong("unbackedPlay", 0) + spent;
        prefs.edit().putLong("unbackedPlay", unbacked).apply();
        if (unbacked >= BACKUP_REMIND_MS && spent > 0) {
            String mode = prefs.getString("backupRemind", "ask");
            if (mode.equals("auto")) autoBackup();
            else if (mode.equals("ask")) askBackup = true;
        }
        playedCache.clear();
        if (lib != null) applyCustomFolders();   // smart folders: played / most played changed
    }

    // ---- save backup reminder: after some hours of play without a backup, offer one (or make it) ----

    private static final long BACKUP_REMIND_MS = 3 * 3_600_000L;
    private boolean askBackup;

    private void autoBackup() {
        List<File> roots = volumeRoots();
        new Thread(() -> {
            try {
                File dir = Backups.folder(roots);
                Backups.backup(dir, Backups.AUTO);
                // automatic backups keep the newest ten of their own; backups made by hand are never removed here
                List<File> all = Backups.list(dir);
                all.removeIf(f -> !f.getName().startsWith(Backups.AUTO));
                for (int i = 10; i < all.size(); i++) all.get(i).delete();
                prefs.edit().putLong("unbackedPlay", 0).apply();
                main.post(() -> Toast.makeText(this, t(S.AUTO_BACKUP_DONE), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                main.post(() -> Toast.makeText(this, I18n.f(S.AI_FAILED, String.valueOf(e.getMessage())), Toast.LENGTH_LONG).show());
            }
        }, "backup").start();
    }

    private void showBackupReminder() {
        askBackup = false;
        if (shelf == null) return;
        long h = prefs.getLong("unbackedPlay", 0) / 3_600_000L;
        ShelfView.Menu m = new ShelfView.Menu(t(S.BACKUP_SAVES), I18n.f(S.BACKUP_REMIND, h));
        m.add(t(S.BACKUP_NOW), false, () -> { shelf.closeMenu(); autoBackup(); });
        m.add(t(S.BACKUP_ALWAYS), false, () -> {
            prefs.edit().putString("backupRemind", "auto").apply();
            shelf.closeMenu();
            autoBackup();
        });
        m.add(t(S.BACKUP_LATER), false, () -> { prefs.edit().putLong("unbackedPlay", 0).apply(); shelf.closeMenu(); });
        m.add(t(S.BACKUP_NEVER), false, () -> { prefs.edit().putString("backupRemind", "off").apply(); shelf.closeMenu(); });
        shelf.showMenu(m);
    }

    private String backupRemindLabel() {
        String mode = prefs.getString("backupRemind", "ask");
        return t(mode.equals("auto") ? S.BACKUP_AUTO : mode.equals("off") ? S.OFF : S.BACKUP_ASK);
    }

    private void showBackupRemindMenu(ShelfView.Menu tools) {
        String mode = prefs.getString("backupRemind", "ask");
        ShelfView.Menu m = new ShelfView.Menu(t(S.BACKUP_REMINDER), t(S.BACKUP_REMINDER_HINT));
        m.parent = tools;
        String[] modes = {"ask", "auto", "off"};
        S[] names = {S.BACKUP_ASK, S.BACKUP_AUTO, S.OFF};
        for (int i = 0; i < modes.length; i++) {
            String md = modes[i];
            m.add(t(names[i]), md.equals(mode), () -> {
                prefs.edit().putString("backupRemind", md).apply();
                Toast.makeText(this, t(S.SAVED), Toast.LENGTH_SHORT).show();
                shelf.closeMenu();
            });
        }
        shelf.showMenu(m);
    }

    // ---- game details and screenshots ----

    private static final String[] SHOT_DIRS = {"Pictures/Screenshots", "DCIM/Screenshots", "RetroArch/screenshots", "azahar/screenshots",
            "citra-emu/screenshots", "DraStic/screenshots", "PSP/SCREENSHOT", "dolphin-mmjr/ScreenShots", "dolphin-emu/ScreenShots",
            "duckstation/screenshots", "ARMSX2/snaps", "Pictures/RetroArch"};

    /** Screenshots of a game: named after its file, or taken while it was running (by the recorded play sessions). */
    private List<File> screenshots(Library.Game g) {
        List<long[]> ses = new ArrayList<>();
        for (String s : prefs.getString("sessions:" + g.key(), "").split(",")) {
            int d = s.indexOf('-');
            if (d <= 0) continue;
            try { ses.add(new long[]{Long.parseLong(s.substring(0, d)), Long.parseLong(s.substring(d + 1)) + 60_000}); } catch (NumberFormatException ignored) { }
        }
        String base = (g.mediaBase != null ? g.mediaBase : new File(g.baseName()).getName()).toLowerCase(Locale.ROOT);
        List<File> out = new ArrayList<>();
        File prim = android.os.Environment.getExternalStorageDirectory();
        java.util.ArrayDeque<File[]> todo = new java.util.ArrayDeque<>();
        for (String d : SHOT_DIRS) todo.add(new File[]{new File(prim, d)});
        int dirs = 0;
        while (!todo.isEmpty() && dirs < 200) {
            File dir = todo.poll()[0];
            File[] fs = dir.listFiles();
            if (fs == null) continue;
            dirs++;
            for (File f : fs) {
                if (f.isDirectory()) { todo.add(new File[]{f}); continue; }
                String n = f.getName().toLowerCase(Locale.ROOT);
                if (!(n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".webp"))) continue;
                boolean mine = base.length() >= 3 && n.contains(base);
                long t = f.lastModified();
                for (long[] s : ses) if (t >= s[0] && t <= s[1]) { mine = true; break; }
                if (mine) out.add(f);
            }
        }
        out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return out;
    }

    @Override public void onDeleteGame(Library.Game g, ShelfView.Menu parent) {
        if (shelf == null) return;
        if (!deletable(g)) { Toast.makeText(this, t(S.DELETE_FAILED), Toast.LENGTH_SHORT).show(); return; }
        List<File> files = gameFiles(g);
        long size = 0;
        for (File f : files) size += f.length();
        ShelfView.Menu m = new ShelfView.Menu(I18n.f(S.DELETE_GAME_Q, g.title), I18n.f(S.DELETE_GAME_HINT, gb(size)));
        m.parent = parent;
        // Cancel first, so a quick double press of A does not delete anything
        m.add(t(S.CANCEL), false, () -> shelf.showMenu(parent));
        m.add(t(S.DELETE_GAME), false, () -> {
            shelf.closeMenu();
            new Thread(() -> {
                boolean ok = true;
                for (File f : files) ok &= deleteTree(f);
                boolean done = ok;
                main.post(() -> {
                    Toast.makeText(this, done ? I18n.f(S.GAME_DELETED, g.title) : t(S.DELETE_FAILED), Toast.LENGTH_SHORT).show();
                    sizes.remove(g);
                    // a list opened from the problem centre (duplicates) loses the game at once
                    if (shelf != null && shelfState.current == shelfState.results && shelfState.listTitle != null
                            && shelfState.results.games.contains(g)) {
                        List<Library.Game> rest = new ArrayList<>(shelfState.results.games);
                        rest.remove(g);
                        shelf.showList(shelfState.listTitle, rest, shelfState.listBack);
                    }
                    load();
                });
            }, "delete").start();
        });
        shelf.showMenu(m);
    }

    /** Y → Game info → AI pictures for this game: each kind with whether it has one; a new one replaces it. */
    @Override public void onAiGamePictures(Library.Game g, ShelfView.Menu parent) {
        if (shelf == null) return;
        if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.AI_IMAGES_NEEDED), Toast.LENGTH_LONG).show(); return; }
        ShelfView.Menu m = new ShelfView.Menu(t(S.AI_GAME_PICTURES), g.title);
        m.parent = parent;
        for (String kind : PIC_KINDS) {
            boolean has = g.asset(kind) != null;
            String label = picName(kind) + "  ·  " + t(has ? S.PIC_HAVE : S.PIC_NONE);
            if (kind.equals("logo") && (g.original == null || g.original.trim().isEmpty())) {
                m.add(label, false, null).hint(t(S.LOGO_NEEDS_NAME));
                continue;
            }
            m.add(label, has, () -> confirmAiPictures(m, java.util.Collections.singletonList(g), kind)).hint(has ? t(S.PIC_REPLACE) : null);
        }
        m.sel = 0;
        shelf.showMenu(m);
    }

    @Override public void onDetails(Library.Game g) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.DETAILS), g.title);
        m.parent = shelfState.menu;
        m.add(t(S.PLATFORM) + "  ·  " + g.platform.title(), false, null);
        if (g.original != null && !g.original.isEmpty()) m.add(t(S.ORIGINAL_NAME) + "  ·  " + g.original, false, null);
        m.add(t(S.FILE) + "  ·  " + g.path.getName(), false, null);
        long size = g.path.length();
        for (String d : g.discs) size += new File(g.dir, d).length();
        String sz = size >= 1L << 30 ? String.format(Locale.ROOT, "%.2f GB", size / (double) (1L << 30)) : size >= 1L << 20 ? (size >> 20) + " MB" : Math.max(1, size >> 10) + " KB";
        m.add(t(S.SIZE) + "  ·  " + sz + (g.discs.isEmpty() ? "" : "  ·  " + I18n.f(S.DISCS, g.discs.size() + 1)), false, null);
        m.add(t(S.LOCATION) + "  ·  " + g.path.getParent(), false, null);
        m.add(t(S.EMULATOR) + "  ·  " + Library.emulatorLabel(effectiveCmd(g)), false, null);
        for (String s : playedTags(g)) if (s != null) m.add(s, false, null);
        int shotRow = m.labels.size();
        m.add(t(S.SCREENSHOTS) + "  ·  …", false, null);
        shelf.showMenu(m);
        new Thread(() -> {
            List<File> shots = screenshots(g);
            main.post(() -> {
                if (shelfState.menu != m) return;
                m.labels.set(shotRow, t(S.SCREENSHOTS) + "  ·  " + shots.size());
                if (!shots.isEmpty()) m.actions.set(shotRow, () -> showScreenshots(g, shots, m, 0));
                if (shelf != null) shelf.invalidate();
            });
        }, "shots").start();
    }

    private void showScreenshots(Library.Game g, List<File> shots, ShelfView.Menu parent, int sel) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.SCREENSHOTS), g.title + "  ·  " + shots.size());
        m.parent = parent;
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag(I18n.code()));
        for (File f : shots) m.add(df.format(new Date(f.lastModified())), false, null);
        m.onSelect = i -> top.showPicture(shots.get(i), g.title + "  ·  " + df.format(new Date(shots.get(i).lastModified())));
        m.onBack = () -> { shelf.showMenu(parent); onFocus(g); };
        m.sel = sel;
        shelf.showMenu(m);
        m.onSelect.accept(sel);
    }

    /** Play time and last-played text per game, worked out at most once a minute (list rows ask on every frame). */
    private final java.util.Map<String, String[]> playedCache = new java.util.HashMap<>();
    private long playedCacheAt;

    private String[] playedTags(Library.Game g) {
        long now = SystemClock.uptimeMillis();
        if (now - playedCacheAt > 60_000) { playedCache.clear(); playedCacheAt = now; }
        String[] t = playedCache.get(g.key());
        if (t == null) playedCache.put(g.key(), t = computePlayedTags(g));
        return t;
    }

    private String[] computePlayedTags(Library.Game g) {
        long ms = prefs.getLong("time:" + g.key(), 0), last = prefs.getLong("last:" + g.key(), 0);
        String played = null, when = null;
        if (ms >= 60_000) {
            long min = ms / 60_000;
            played = "⏱ " + (min >= 60 ? I18n.f(S.PLAYED_HM, min / 60, min % 60) : I18n.f(S.PLAYED_M, min));
        }
        if (last > 0) {
            String d = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag(I18n.code())).format(new Date(last));
            when = I18n.f(S.LAST_PLAYED, d);
        }
        Achievements.Game a = Achievements.find(prefs, g);
        String trophies = a != null && a.total > 0 ? "🏆 " + a.got + " / " + a.total : null;
        return new String[]{played, when, trophies, statusTag(g)};
    }

    // ---- input: gamepad keys and the left stick drive the lower-screen shelf, whichever window has focus ----

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        return handleKey(e) || super.dispatchKeyEvent(e);
    }

    @Override public boolean dispatchGenericMotionEvent(MotionEvent e) {
        return handleMotion(e) || super.dispatchGenericMotionEvent(e);
    }

    private boolean handleKey(KeyEvent e) {
        if (shelf == null || lib == null) return false;
        int code = e.getKeyCode();
        if (code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN || code == KeyEvent.KEYCODE_HOME) return false;
        if (noteActivity(e)) return true; // the key only woke the screensaver
        code = remap(code);
        if (voiceTo != null && voice == null && SystemClock.uptimeMillis() - voiceToAt > 30_000) voiceTo = null;
        if (holdCode != 0 && code == holdCode) return holdKey(code, e);
        if ((code == KeyEvent.KEYCODE_BUTTON_L1 && (kb != null || voiceTo != null))
                || (code == KeyEvent.KEYCODE_BUTTON_R1 && voiceTo != null)) {
            if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) startVoice();
            else if (e.getAction() == KeyEvent.ACTION_UP) stopVoice();
            return true;
        }
        if ((code == KeyEvent.KEYCODE_BUTTON_L1 || code == KeyEvent.KEYCODE_BUTTON_R1)
                && e.getAction() == KeyEvent.ACTION_DOWN && !shelf.menuOpen()) return holdKey(code, e);
        if (chat != null && e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0 && chatKey(code)) return true;
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            if (e.getRepeatCount() > 0 && shelf.holdingA()) return true;   // holding A to lift a home tile
            if (isDirection(code)) {
                if (e.getRepeatCount() > 0) return true;   // our own repeat runs instead, at a steady pace
                startRepeat(code);
            }
            return nav(code);
        }
        if (e.getAction() == KeyEvent.ACTION_UP && isDirection(code) && code == repeatCode) stopRepeat();
        if (e.getAction() == KeyEvent.ACTION_UP && kb != null) return true;
        if (e.getAction() == KeyEvent.ACTION_UP && shelf.onKeyUp(code)) return true;
        return e.getAction() == KeyEvent.ACTION_UP && code != KeyEvent.KEYCODE_POWER;
    }

    // ---- hold L to search by voice, hold R to chat with the AI; a short press keeps its old job ----

    /** The L or R key being held (0 = none), and whether holding it has started listening. */
    private int holdCode;
    private boolean holdListening;
    private final Runnable holdFired = () -> {
        if (holdCode == 0 || shelf == null) return;
        if (holdCode == KeyEvent.KEYCODE_BUTTON_L1) {
            // the search keyboard opens and the words go into it
            closeChat();
            if (kb == null) onSearch();
            if (AiImage.from(prefs) == null) { Toast.makeText(this, t(S.VOICE_NEEDS_KEY), Toast.LENGTH_LONG).show(); return; }
            holdListening = true;
            startVoice();
            if (voice == null) holdListening = false;
            return;
        }
        if (!openChat()) return;
        stopSpeaking();
        if (AiImage.from(prefs) == null) { askText(t(S.CHAT_TITLE), "", this::chatSend); return; }   // no speech-to-text: type
        holdListening = true;
        voiceTo = this::chatSend;
        voiceToAt = SystemClock.uptimeMillis();
        startVoice();
        if (voice == null) { holdListening = false; voiceTo = null; }
        else if (this.chat != null) this.chat.setStatus(t(S.R_LISTENING));
    };

    private boolean holdKey(int code, KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            if (e.getRepeatCount() == 0 && holdCode == 0) {
                holdCode = code;
                main.postDelayed(holdFired, 450);
            }
            return true;
        }
        if (e.getAction() == KeyEvent.ACTION_UP && holdCode == code) {
            holdCode = 0;
            main.removeCallbacks(holdFired);
            if (holdListening) {
                holdListening = false;
                if (chat != null && voice != null) chat.setStatus(t(S.TRANSCRIBING));
                stopVoice();
            } else if (voice == null) nav(code);   // a short press: previous / next platform, as before
        }
        return true;
    }

    // ---- the AI chat on the upper screen ----

    private ChatPanel chat;
    private final List<ChatPanel.Msg> chatMsgs = new ArrayList<>();
    private long chatAt;
    /** The saved file of the conversation on screen (null until its first message is saved). */
    private String chatId;
    /** Bumped whenever the conversation on screen is replaced or deleted, so a reply still on its way is dropped. */
    private int chatSeq;

    /** Empties the chat on screen (a new conversation, or the one shown was deleted). */
    private void resetChat() {
        stopSpeaking();
        chatSeq++;
        chatMsgs.clear();
        chatId = null;
        if (chat != null) { chat.setStatus(null); chat.invalidate(); }
    }

    private boolean chatSaving() { return prefs.getBoolean("chat.save", true); }

    private void saveChat() {
        if (!chatSaving() || chatMsgs.isEmpty()) return;
        if (chatId == null) chatId = String.valueOf(System.currentTimeMillis());
        List<ChatPanel.Msg> copy = new ArrayList<>(chatMsgs);
        String id = chatId;
        File dir = getFilesDir();
        ChatHistory.WRITER.execute(() -> ChatHistory.save(dir, id, copy));
    }

    /** X in the chat: put this conversation away (it stays in the history) and start a fresh one. */
    private void newChat() {
        boolean kept = chatSaving() && !chatMsgs.isEmpty();
        resetChat();
        Toast.makeText(this, t(kept ? S.CHAT_NEW_STARTED : S.CHAT_NEW_ONLY), Toast.LENGTH_SHORT).show();
    }

    /** The saved conversations on the lower screen; picking one opens it in the chat. */
    private void showChatHistory(ShelfView.Menu parent) {
        if (shelf == null) return;
        List<ChatHistory.Entry> all = ChatHistory.list(getFilesDir());
        ShelfView.Menu m = new ShelfView.Menu(t(S.CHAT_HISTORY), t(S.CHAT_HISTORY_HINT));
        m.parent = parent;
        if (all.isEmpty()) m.add(t(S.CHAT_NO_HISTORY), false, null);
        java.text.DateFormat df = android.text.format.DateFormat.getMediumDateFormat(this), tf = android.text.format.DateFormat.getTimeFormat(this);
        for (ChatHistory.Entry e : all) {
            java.util.Date d = new java.util.Date(e.updated);
            String when = df.format(d) + " " + tf.format(d);
            m.add(e.title + "  ·  " + when, e.id.equals(chatId), () -> {
                ShelfView.Menu one = new ShelfView.Menu(e.title, when);
                one.parent = m;
                one.add(t(S.OPEN), false, () -> {
                    shelf.closeMenu();
                    if (!openChat()) return;
                    resetChat();
                    chatMsgs.addAll(ChatHistory.load(getFilesDir(), e.id));
                    chatId = e.id;
                    chatAt = SystemClock.uptimeMillis();
                    if (chat != null) { chat.setStatus(null); chat.invalidate(); }
                });
                one.add("✕  " + t(S.CHAT_DELETE), false, () -> {
                    ChatHistory.delete(getFilesDir(), e.id);
                    if (e.id.equals(chatId)) resetChat();   // or the next message would save it again
                    showChatHistory(parent);
                });
                shelf.showMenu(one);
            });
        }
        // whether new conversations are kept (was a switch on the AI page)
        boolean saving = chatSaving();
        int saveRow = m.labels.size();
        m.add(t(S.CHAT_SAVE) + "  ·  " + t(saving ? S.ON : S.OFF), false, () -> {
            prefs.edit().putBoolean("chat.save", !saving).apply();
            showChatHistory(parent);
            if (shelfState.menu != null) shelfState.menu.sel = saveRow;
        });
        if (!all.isEmpty()) m.add("✕  " + t(S.CHAT_CLEAR_ALL), false, () -> {
            ShelfView.Menu sure = new ShelfView.Menu(t(S.CHAT_CLEAR_ALL), I18n.f(S.CHAT_CLEAR_SURE, all.size()));
            sure.parent = m;
            sure.add("✕  " + t(S.CHAT_CLEAR_ALL), false, () -> {
                ChatHistory.clearAll(getFilesDir());
                if (chatId != null) resetChat();
                Toast.makeText(this, t(S.DELETED), Toast.LENGTH_SHORT).show();
                showChatHistory(parent);
            });
            sure.add(t(S.BACK), false, () -> showChatHistory(parent));
            shelf.showMenu(sure);
        });
        shelf.showMenu(m);
    }

    /** Opens the chat (carrying on a conversation from the last half hour); false with a setup help when there is no AI key. */
    private boolean openChat() {
        if (Ai.from(prefs) == null) {
            showTextDialog(t(S.AI_SETUP_TITLE), t(S.AI_SETUP_HELP), t(S.OPEN_WIFI), this::showWifiMenu);
            return false;
        }
        if (shelf != null) shelf.closeMenu();
        closeKeyboard();
        if (SystemClock.uptimeMillis() - chatAt > 30 * 60_000L) { chatMsgs.clear(); chatId = null; chatSeq++; }
        if (chat == null) {
            chat = new ChatPanel(this, chatMsgs, AiImage.from(prefs) != null);
            top.setOverlay(chat);
            // the chat covers the upper screen: no video sound or banner tune under the spoken replies
            stopBanner();
            top.setQuiet(true);
        }
        return true;
    }

    private void closeChat() {
        stopSpeaking();
        if (chat == null) return;
        chat = null;
        // a failed voice message leaves R aimed at the chat (to try again); once it is closed, R opens it anew
        if (voice == null) voiceTo = null;
        if (top != null) {
            top.setOverlay(null);
            top.setQuiet(false);
            top.reset();   // so the selected game is drawn again and its video can start
        }
        if (shelf != null) onFocus(shelf.selected());
    }

    /** Keys of the chat; everything else drives the lower screen, where its games are. */
    private boolean chatKey(int code) {
        if (shelf != null && shelf.menuOpen()) return false;   // the history list on the lower screen has the keys
        switch (code) {
            case KeyEvent.KEYCODE_BUTTON_B: closeChat(); return true;
            case KeyEvent.KEYCODE_BUTTON_Y: askText(t(S.CHAT_TITLE), "", this::chatSend); return true;
            case KeyEvent.KEYCODE_BUTTON_X: newChat(); return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT: showChatHistory(null); return true;
            case KeyEvent.KEYCODE_BUTTON_L2: chat.scrollBy(-1); return true;
            case KeyEvent.KEYCODE_BUTTON_R2: chat.scrollBy(1); return true;
            default: return false;
        }
    }

    /** One message to the AI: it answers in the chat, and the games it picks or searches for fill the lower screen. */
    private void chatSend(String text) {
        Ai ai = Ai.from(prefs);
        if (ai == null || lib == null) return;
        if (chat == null && !openChat()) return;
        stopSpeaking();
        chatAt = SystemClock.uptimeMillis();
        chatMsgs.add(new ChatPanel.Msg(true, text, null));
        saveChat();
        if (chat != null) chat.setStatus(t(S.THINKING));
        List<Library.Game> all = new ArrayList<>();
        for (Library.Game g : lib.byKey.values()) if (g.pkg == null && g.path != null) all.add(g);
        StringBuilder cat = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            Library.Game g = all.get(i);
            cat.append(i).append('|').append(g.title).append('|').append(platformName(g)).append('|').append(g.genre == null ? "" : g.genre)
                    .append('|').append(prefs.getLong("time:" + g.key(), 0) / 60_000).append('|').append(prefs.getString("status:" + g.key(), "")).append('\n');
        }
        // the conversation so far, newest last
        StringBuilder convo = new StringBuilder();
        for (int i = Math.max(0, chatMsgs.size() - 20); i < chatMsgs.size(); i++) {
            ChatPanel.Msg m = chatMsgs.get(i);
            convo.append(m.me ? "User: " : "AI: ").append(m.text);
            if (!m.games.isEmpty()) convo.append(" [shown games: ").append(String.join("; ", m.games)).append(']');
            convo.append('\n');
        }
        // "this game" means the one selected on screen
        Object sel = shelf == null ? null : shelf.selected();
        String here = "";
        if (sel instanceof Library.Game && ((Library.Game) sel).pkg == null) {
            Library.Game g = (Library.Game) sel;
            here = "\nThe game selected on screen now (\"this game\"): " + g.title + " · " + platformName(g)
                    + (g.original == null || g.original.isEmpty() ? "" : " (" + g.original + ")");
        }
        String lang = I18n.name(), context = here;
        int seq = chatSeq;
        new Thread(() -> {
            String a = ai.chat("You are a friendly, knowledgeable game companion on the user's retro handheld, chatting with them. "
                    + "Their own game library is listed below. Answer the latest message of the conversation in " + lang
                    + ", in plain text without markdown. Talk about anything they bring up: what to play, walkthroughs and tips, "
                    + "stories, characters, game history, or just chat; keep answers to about eight sentences unless they ask for "
                    + "more. When they want something to play, pick up to 6 fitting games from the library, each with a short "
                    + "reason, preferring games they have not finished or dropped. When they ask to find or show games in their "
                    + "library by name or series, put a short search word (a title fragment as it is written in the library) in "
                    + "\"search\". Otherwise leave picks empty and search empty. Answer only with JSON: "
                    + "{\"reply\": string, \"picks\": [{\"i\": index, \"why\": string}], \"search\": string}.",
                    "Library (index|title|platform|genre|minutes played|status):\n" + cat + context + "\nConversation:\n" + convo, 1800);
            String reply = null, find = "";
            List<Library.Game> picks = new ArrayList<>();
            java.util.Map<Library.Game, String> whys = new java.util.HashMap<>();
            if (a != null) {
                int x = a.indexOf('{'), y = a.lastIndexOf('}');
                try {
                    org.json.JSONObject o = new org.json.JSONObject(a.substring(x, y + 1));
                    reply = o.optString("reply", "");
                    find = o.optString("search", "").trim();
                    org.json.JSONArray arr = o.optJSONArray("picks");
                    for (int j = 0; arr != null && j < arr.length(); j++) {
                        org.json.JSONObject pk = arr.getJSONObject(j);
                        int i = pk.optInt("i", -1);
                        if (i < 0 || i >= all.size() || picks.contains(all.get(i))) continue;
                        picks.add(all.get(i));
                        whys.put(all.get(i), pk.optString("why", ""));
                    }
                } catch (Exception ignored) {
                    if (x < 0) reply = a.trim();   // a model that answered in plain text
                }
            }
            String said = reply, err = ai.error, look = find;
            main.post(() -> {
                if (seq != chatSeq) return;   // the conversation was replaced or deleted meanwhile
                if (chat != null) chat.setStatus(null);
                if (said == null) {
                    chatMsgs.add(new ChatPanel.Msg(false, I18n.f(S.AI_FAILED, err != null ? err : "unexpected answer"), null));
                    if (chat != null) chat.invalidate();
                    return;
                }
                List<String> names = new ArrayList<>();
                String answer = said;
                if (!picks.isEmpty()) {
                    for (Library.Game g : picks) names.add(g.title + " · " + platformName(g));
                    TopView.extra.clear();
                    TopView.extra.putAll(whys);
                    searchText = "";
                    searchGenre = null;
                    if (shelf != null) shelf.showResults(t(S.CHAT_TITLE), picks);
                } else if (!look.isEmpty() && shelf != null) {
                    TopView.extra.clear();
                    searchScope = null;
                    searchGenre = null;
                    searchText = look;
                    prepareSearchIndex();
                    search(look);
                    answer = said + "\n\n" + I18n.f(S.CHAT_FOUND, shelfState.results.games.size());
                }
                chatAt = SystemClock.uptimeMillis();
                if (chat != null && Speaker.ready(prefs))
                    speaker().say(said, I18n.code(), e -> Toast.makeText(this, I18n.f(S.TTS_FAILED, String.valueOf(e)), Toast.LENGTH_LONG).show());
                ChatPanel.Msg m = new ChatPanel.Msg(false, answer, names);
                if (chat != null) chat.add(m);
                else chatMsgs.add(m);
                saveChat();
            });
        }, "ai-chat").start();
    }

    /** A key goes to the gamepad keyboard while it is open, else to the lower screen. */
    private boolean nav(int code) {
        if (kb != null) return kb.onKey(code);
        return shelf != null && shelf.onKey(code);
    }

    /** Button layout from Settings → Buttons: A/B and X/Y can be swapped (e.g. for a Japanese-style confirm). */
    private int remap(int code) {
        if (prefs.getBoolean("swapAB", false)) {
            if (code == KeyEvent.KEYCODE_BUTTON_A) return KeyEvent.KEYCODE_BUTTON_B;
            if (code == KeyEvent.KEYCODE_BUTTON_B) return KeyEvent.KEYCODE_BUTTON_A;
        }
        if (prefs.getBoolean("swapXY", false)) {
            if (code == KeyEvent.KEYCODE_BUTTON_X) return KeyEvent.KEYCODE_BUTTON_Y;
            if (code == KeyEvent.KEYCODE_BUTTON_Y) return KeyEvent.KEYCODE_BUTTON_X;
        }
        return code;
    }

    @Override public String playedLine(Library.Game g) {
        String[] t = playedTags(g);
        if (t[0] == null && t[1] == null) return null;
        return t[0] == null ? t[1] : t[1] == null ? t[0] : t[0] + "  ·  " + t[1];
    }

    // ---- screensaver: after some idle minutes the upper screen cycles covers and the lower one dims ----

    private long lastInput = SystemClock.uptimeMillis();
    private boolean saving;
    private final Runnable idleCheck = new Runnable() {
        @Override public void run() {
            int min = prefs.getInt("screensaver", 5);
            long idle = SystemClock.uptimeMillis() - lastInput;
            if (min > 0 && !saving && pres != null && idle > min * 60_000L) startSaver();
            // the screensaver runs for a while, then the system may turn the screen off as usual
            keepAwake(min > 0 && idle < (min + SAVER_RUN_MIN) * 60_000L);
            main.postDelayed(this, 15_000);
        }
    };

    private static final int SAVER_RUN_MIN = 10;
    private boolean awake;

    /** The system's own screen timeout is usually shorter than the screensaver delay, so hold the screen on meanwhile. */
    private void keepAwake(boolean on) {
        if (on == awake) return;
        awake = on;
        if (on) getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startSaver() {
        List<Library.Game> pool = new ArrayList<>();
        if (lib != null) for (Library.Platform p : lib.platforms) for (Library.Game g : p.games) if (g.pkg == null) pool.add(g);
        if (pool.isEmpty()) return;
        saving = true;
        stopBanner();
        top.startSaver(pool);
        if (shelf != null) shelf.setDimmed(true);
    }

    private void stopSaver() {
        saving = false;
        top.stopSaver();
        if (shelf != null) {
            shelf.setDimmed(false);
            onFocus(shelf.selected());
        }
    }

    /** Records input for the screensaver; true when the event only ended the screensaver. */
    private boolean noteActivity(android.view.InputEvent e) {
        lastInput = SystemClock.uptimeMillis();
        if (!awake && prefs.getInt("screensaver", 5) > 0) keepAwake(true);
        if (!saving) return false;
        if (e instanceof KeyEvent && ((KeyEvent) e).getAction() == KeyEvent.ACTION_DOWN) stopSaver();
        return true;
    }

    private boolean handleMotion(MotionEvent e) {
        if (shelf == null || (e.getSource() & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK
                || e.getAction() != MotionEvent.ACTION_MOVE) return false;
        if (saving) {
            if (axis(e.getAxisValue(MotionEvent.AXIS_X)) != 0 || axis(e.getAxisValue(MotionEvent.AXIS_Y)) != 0) stopSaver();
            return true;
        }
        int x = axis(e.getAxisValue(MotionEvent.AXIS_X)), y = axis(e.getAxisValue(MotionEvent.AXIS_Y));
        if (x == 0) x = axis(e.getAxisValue(MotionEvent.AXIS_HAT_X));
        if (y == 0) y = axis(e.getAxisValue(MotionEvent.AXIS_HAT_Y));
        if (x != 0 || y != 0) lastInput = SystemClock.uptimeMillis();   // resting stick noise is not activity
        if (x != stickX && x != 0) {
            int k = x < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
            nav(k);
            startRepeat(k);
        }
        if (y != stickY && y != 0) {
            int k = y < 0 ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN;
            nav(k);
            startRepeat(k);
        }
        if (x == 0 && y == 0 && (stickX != 0 || stickY != 0)) stopRepeat();
        else if ((stickX != 0 || stickY != 0) && repeatCode != 0) {
            boolean vert = repeatCode == KeyEvent.KEYCODE_DPAD_UP || repeatCode == KeyEvent.KEYCODE_DPAD_DOWN;
            if (vert ? y == 0 : x == 0) {
                stopRepeat();
                if (vert && x != 0) startRepeat(x < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
                else if (!vert && y != 0) startRepeat(y < 0 ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
            }
        }
        stickX = x;
        stickY = y;
        return true;
    }

    // ---- holding a direction (d-pad key, hat or stick) keeps moving: a pause, then steady steps that speed up ----

    private static final long REPEAT_DELAY = 380, REPEAT_STEP = 95, REPEAT_FAST = 45;
    private int repeatCode;
    private long repeatStart;

    private static boolean isDirection(int code) {
        return code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    private final Runnable repeater = new Runnable() {
        @Override public void run() {
            if (repeatCode == 0 || shelf == null) return;
            lastInput = SystemClock.uptimeMillis();
            nav(repeatCode);
            boolean fast = SystemClock.uptimeMillis() - repeatStart > 1500;   // after a while, faster
            main.postDelayed(this, fast ? REPEAT_FAST : REPEAT_STEP);
        }
    };

    private void startRepeat(int code) {
        main.removeCallbacks(repeater);
        repeatCode = code;
        repeatStart = SystemClock.uptimeMillis();
        main.postDelayed(repeater, REPEAT_DELAY);
    }

    private void stopRepeat() {
        main.removeCallbacks(repeater);
        repeatCode = 0;
    }

    private static int axis(float v) { return v > 0.6f ? 1 : v < -0.6f ? -1 : 0; }
}
