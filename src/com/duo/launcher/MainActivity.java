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
        I18n.set(prefs.getString("lang", I18n.systemDefault()));
        applyDisplayMode();
        shelfState.hidden.addAll(prefs.getStringSet("hidden", new HashSet<>()));
        hiddenGames.addAll(prefs.getStringSet("hiddenGames", new HashSet<>()));
        Theme.init(this);
        applyTextScale();
        TopView.videoEnabled = prefs.getBoolean("videoPreview", true);
        TopView.prefs = prefs;
        loadToolLooks();
        loadSounds();
        icons = new Icons(this);
        top = new TopView(this, icons);
        setContentView(top);
    }

    @Override protected void onResume() {
        super.onResume();
        hideBars();
        applyDisplayMode();
        finishPlaySession();
        if (sfx != null) sfx.resumeMusic();
        lastInput = SystemClock.uptimeMillis();
        main.removeCallbacks(idleCheck);
        main.postDelayed(idleCheck, 15_000);
        keepAwake(prefs.getInt("screensaver", 5) > 0);
        showShelf();
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
            shelf.setRecent(recentGames());
            if (pendingPermissions && shelfState.menu != null) showPermissionsMenu(shelfState.menu.parent);
            if (shelfState.menu == null) onFocus(shelf.selected());
            if (!prefs.contains("lang") && shelfState.menu == null) showLanguageMenu(null, true);
        }
    }

    @Override protected void onPause() {
        stopBanner();
        main.removeCallbacks(idleCheck);
        if (saving) stopSaver();
        keepAwake(false);
        if (sfx != null) sfx.pauseMusic();
        // emulators drive the second screen themselves
        if (pres != null) { pres.dismiss(); pres = null; shelf = null; }
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
        boolean first = lib == null, manual = lib != null;
        if (first) top.showMessage(t(S.LOADING));
        File cacheFile = new File(getFilesDir(), "library.cache");
        List<String> folders = folders();
        boolean autoFind = prefs.getBoolean("autoFind", true);
        Set<String> apps = new HashSet<>(prefs.getStringSet("apps", new HashSet<>()));
        new Thread(() -> {
            if (first) {
                Library cached = Library.readCache(cacheFile);
                if (cached != null) main.post(() -> { if (lib == null) applyLibrary(cached, false); });
            }
            long t0 = SystemClock.uptimeMillis();
            Library l = Library.load(this, folders, autoFind, apps);
            lastScanMs = SystemClock.uptimeMillis() - t0;
            Library.writeCache(cacheFile, l);
            main.post(() -> {
                loading = false;
                if (lib == null || !lib.signature.equals(l.signature)) applyLibrary(l, lib != null);
                int n = 0;
                for (Library.Platform p : l.platforms) n += p.games.size();
                if (n == 0) Toast.makeText(this, t(S.NO_GAMES), Toast.LENGTH_LONG).show();
                else if (manual && !reloadPending) Toast.makeText(this, I18n.f(S.SCAN_DONE, n), Toast.LENGTH_SHORT).show();
                if (reloadPending) { reloadPending = false; load(); } else prepareSearchIndex();
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
        if (!prefs.contains("lang")) showLanguageMenu(null, true);
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
        e.apply();
    }

    // ---- settings ----

    @Override public void onSettings() {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(t(S.SETTINGS), null);
        m.add(t(S.GAME_FOLDERS), false, () -> showFoldersMenu(m));
        m.add(t(S.CHOOSE_APPS), false, () -> showAppsMenu(m, 0));
        m.add(t(S.LANGUAGE) + "  ·  " + I18n.name(), false, () -> showLanguageMenu(m, false));
        String mode = prefs.getString("mode", "system");
        m.add(t(S.DISPLAY_MODE) + "  ·  " + t(mode.equals("dark") ? S.MODE_DARK : mode.equals("light") ? S.MODE_LIGHT : S.MODE_SYSTEM),
                false, () -> showDisplayModeMenu(m));
        m.add(t(S.APPEARANCE), false, () -> showAppearanceMenu(m));
        File pack = Theme.packDir();
        m.add(t(S.THEME) + "  ·  " + (pack == null ? t(S.DEFAULT) : Theme.packName(pack)), false, () -> showThemeMenu(m));
        int fontRow = m.labels.size();
        m.add(t(S.FONT_SIZE) + "  ·  " + t(TEXT_SIZE_NAMES[prefs.getInt("textScale", 1)]), false, () -> showTextSizeMenu(fontRow));
        boolean sounds = prefs.getBoolean("sounds", true);
        int soundsRow = m.labels.size();
        m.add(t(S.SOUNDS) + "  ·  " + t(sounds ? S.ON : S.OFF), sounds, () -> {
            prefs.edit().putBoolean("sounds", !sounds).apply();
            sfx.enabled = !sounds;
            reopenSettingsAt(soundsRow);
        });
        if (sfx.hasMusic()) {
            boolean music = prefs.getBoolean("music", true);
            int musicRow = m.labels.size();
            m.add(t(S.MUSIC) + "  ·  " + t(music ? S.ON : S.OFF), music, () -> {
                prefs.edit().putBoolean("music", !music).apply();
                sfx.musicEnabled = !music;
                if (music) sfx.pauseMusic(); else sfx.resumeMusic();
                reopenSettingsAt(musicRow);
            });
        }
        int saverMin = prefs.getInt("screensaver", 5);
        int saverRow = m.labels.size();
        m.add(t(S.SCREENSAVER) + "  ·  " + (saverMin == 0 ? t(S.OFF) : I18n.f(S.MINUTES, saverMin)), false, () -> {
            ShelfView.Menu sm = new ShelfView.Menu(t(S.SCREENSAVER), null);
            sm.parent = shelfState.menu;
            for (int v : new int[]{0, 1, 3, 5, 10, 20}) {
                sm.add(v == 0 ? t(S.OFF) : I18n.f(S.MINUTES, v), v == saverMin, () -> {
                    prefs.edit().putInt("screensaver", v).apply();
                    reopenSettingsAt(saverRow);
                });
            }
            shelf.showMenu(sm);
        });
        int buttonsRow = m.labels.size();
        m.add(t(S.BUTTONS), false, () -> showButtonsMenu(buttonsRow));
        boolean video = prefs.getBoolean("videoPreview", true);
        int videoRow = m.labels.size();
        m.add(t(S.VIDEO_PREVIEW) + "  ·  " + t(video ? S.ON : S.OFF), video, () -> {
            prefs.edit().putBoolean("videoPreview", !video).apply();
            TopView.videoEnabled = !video;
            reopenSettingsAt(videoRow);
        });
        boolean banner = prefs.getBoolean("bannerMusic", true);
        int bannerRow = m.labels.size();
        m.add(t(S.BANNER_MUSIC) + "  ·  " + t(banner ? S.ON : S.OFF), banner, () -> {
            prefs.edit().putBoolean("bannerMusic", !banner).apply();
            if (banner) stopBanner();
            reopenSettingsAt(bannerRow);
        });
        m.add(t(S.HIDDEN_FOLDERS) + (shelfState.hidden.isEmpty() ? "" : "  ·  " + shelfState.hidden.size()), false, () -> showHiddenMenu(m, 0));
        if (!hiddenGames.isEmpty())
            m.add(t(S.HIDDEN_GAMES) + "  ·  " + hiddenGames.size(), false, () -> showHiddenGamesMenu(m, 0));
        m.add(t(S.SET_HOME), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
        });
        m.add(t(S.WIFI_TRANSFER) + (wifi != null && wifi.running() ? "  ·  " + t(S.ON) : ""), false, this::showWifiMenu);
        int toolsRow = m.labels.size();
        m.add(t(S.TOOLS), false, () -> showToolsMenu(toolsRow));
        m.add(t(S.EXIT_TO_DESKTOP), false, this::onOpenDesktop);
        m.add(t(S.ANDROID_SETTINGS), false, () -> {
            shelf.closeMenu();
            try { startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception ignored) { }
        });
        m.add(t(S.PERMISSIONS) + (hasUsageAccess() ? "" : "  ·  !"), false, () -> showPermissionsMenu(m));
        m.add(t(S.ABOUT) + "  ·  Duo Launcher " + versionName(), false, null);
        m.sel = 0;   // open at the top, not on the first ticked row
        shelf.showMenu(m);
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
        for (String id : new String[]{"continue", "recent", "favorites", "add", "search", "stats", "settings", "desktop"}) {
            String n = prefs.getString("tool." + id + ".name", null);
            if (n != null) ShelfView.TOOL_NAMES.put(id, n);
            if (prefs.contains("tool." + id + ".color")) ShelfView.TOOL_COLORS.put(id, prefs.getInt("tool." + id + ".color", 0));
        }
    }

    @Override public void onEditTool(String id, String label, int color) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(label, null);
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
        m.add("－ " + t(S.HIDE_TILE) + (id.equals("settings") ? "  ·  " + t(S.START_SETTINGS_HINT) : ""), false, () -> {
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
        return m;
    }

    private void showWifiMenu() {
        if (wifi == null || !wifi.running()) {
            wifiReceived = 0;
            wifi = new WifiServer(wifiTargets(), name -> main.post(() -> {
                wifiReceived++;
                Toast.makeText(this, I18n.f(S.UPLOADED, name), Toast.LENGTH_SHORT).show();
            }));
            new Thread(() -> {
                try { wifi.start(); } catch (IOException e) { wifi = null; }
                main.post(this::showWifiMenu2);
            }, "wifi-start").start();
            return;
        }
        showWifiMenu2();
    }

    private void showWifiMenu2() {
        if (shelf == null) return;
        String url = WifiServer.address();
        if (wifi == null || url == null) {
            Toast.makeText(this, t(S.NONE_FOUND), Toast.LENGTH_SHORT).show();
            return;
        }
        top.showMessage(t(S.WIFI_OPEN) + "\n\n" + url);
        ShelfView.Menu m = new ShelfView.Menu(t(S.WIFI_TRANSFER), url);
        m.add(url, false, null);
        m.add(t(S.WIFI_STOP), false, () -> {
            wifi.stop();
            wifi = null;
            shelf.closeMenu();
            if (wifiReceived > 0) load();
            onFocus(shelf.selected());
        });
        shelf.showMenu(m);
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
            main.post(then);
        }, "tools").start();
    }

    private void showToolsMenu(int settingsRow) {
        ShelfView.Menu m = new ShelfView.Menu(t(S.TOOLS), null);
        m.parent = shelfState.menu;
        m.add(t(S.DUPLICATES), false, () -> runInBackground(() -> {
            List<Library.Game> dups = findDuplicates();
            return () -> {
                shelf.closeMenu();
                if (dups.isEmpty()) Toast.makeText(this, t(S.NONE_FOUND), Toast.LENGTH_SHORT).show();
                else shelf.showList(t(S.DUPLICATES), dups);
            };
        }));
        m.add(t(S.MISSING_COVERS), false, () -> runInBackground(() -> {
            List<Library.Game> miss = Covers.missing(lib);
            return () -> {
                shelf.closeMenu();
                if (miss.isEmpty()) Toast.makeText(this, t(S.NONE_FOUND), Toast.LENGTH_SHORT).show();
                else shelf.showList(t(S.MISSING_COVERS), miss);
            };
        }));
        m.add(t(S.FILL_COVERS), false, () -> runInBackground(() -> {
            int n = Covers.fetchMissing(this, lib, pct -> { });
            return () -> {
                Toast.makeText(this, I18n.f(S.COVERS_DONE, n), Toast.LENGTH_LONG).show();
                if (n > 0) load();
            };
        }));
        m.add(t(S.BACKUP_SAVES), false, () -> runInBackground(() -> {
            File out = Backups.backup(Backups.folder(volumeRoots()));
            return () -> Toast.makeText(this, I18n.f(S.BACKUP_DONE, out.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }));
        m.add(t(S.RESTORE_SAVES), false, () -> {
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
        m.add(t(S.EXPORT_THEME), false, () -> runInBackground(() -> {
            File out = Theme.export(this, new File(Backups.folder(volumeRoots()).getParentFile(), "exports"));
            return () -> Toast.makeText(this, I18n.f(S.EXPORTED, out.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }));
        m.add(t(S.DIAGNOSTICS), false, this::showDiagnostics);
        shelf.showMenu(m);
    }

    /** Same platform, same file size and the same name once tags and punctuation are ignored. */
    private List<Library.Game> findDuplicates() {
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

    private long lastScanMs;

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
        if (shelfState.menu != null) {
            shelfState.menu.sel = row;
            shelf.invalidate();
        }
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
        m.add(t(S.SET_HOME), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
        });
        m.add(t(S.DONE), false, () -> { pendingPermissions = false; if (first) shelf.closeMenu(); else onSettings(); });
        shelf.showMenu(m);
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
                if (firstRun) showPermissionsMenu(null); else onSettings();
                if (shelfState.menu != null) onFocus(shelf.selected());
            });
        }
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
        boolean customDark = Theme.isCustom(p.name, Theme.BACKGROUND_DARK);
        m.add(t(S.BACKGROUND_LIGHT) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.BACKGROUND));
        if (customBg) m.add(t(S.BACKGROUND_LIGHT) + " · " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.BACKGROUND);
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), null);
            showPlatformLookMenu(parent, p);
        });
        m.add(t(S.BACKGROUND_DARK) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.BACKGROUND_DARK));
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

    @Override public void onSearch() {
        if (lib == null) return;
        Library.Platform cur = shelfState.current;
        if (cur != shelfState.results) searchScope = cur == null || cur == shelfState.recent ? null : cur;
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        android.content.Context dc = new android.view.ContextThemeWrapper(this, theme);
        EditText field = new EditText(dc);
        field.setSingleLine(true);
        // keep the dialog visible above the keyboard instead of the full-screen landscape editor
        field.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_ACTION_DONE);
        field.setHint(t(S.SEARCH_HINT));
        field.setText(shelfState.query);
        field.setSelection(field.getText().length());
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { search(s.toString()); }
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
                StringBuilder ini = new StringBuilder(), joined = new StringBuilder();
                for (String w : full.split("[^a-z0-9]+")) {
                    if (w.isEmpty()) continue;
                    ini.append(w.charAt(0));
                    joined.append(w);
                }
                pinyin.put(g, new String[]{joined.toString(), ini.toString()});
            }
            main.post(() -> { indexing = false; if (shelfState.current == shelfState.results) search(shelfState.query); });
        }, "pinyin").start();
    }

    private void search(String q) {
        if (shelf == null || lib == null) return;
        String needle = q.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) return;
        String compact = needle.replaceAll("[^a-z0-9]", "");
        boolean latin = compact.length() == needle.replace(" ", "").length();
        java.util.LinkedHashSet<Library.Game> hits = new java.util.LinkedHashSet<>();
        List<Library.Platform> where = new ArrayList<>();
        if (searchScope != null) where.add(searchScope); else where.addAll(lib.platforms);
        for (Library.Platform p : where)
            for (Library.Game g : p.games) {
                if (g.title.toLowerCase(Locale.ROOT).contains(needle) || g.file.toLowerCase(Locale.ROOT).contains(needle)) { hits.add(g); continue; }
                // Latin letters also match Chinese titles by pinyin: "kdyg" or "koudai" finds 口袋妖怪
                String[] py = latin && !compact.isEmpty() ? pinyin.get(g) : null;
                if (py != null && (py[1].startsWith(compact) || py[1].contains(compact) && compact.length() >= 2 || py[0].contains(compact))) hits.add(g);
            }
        shelf.showResults(q.trim(), new ArrayList<>(hits));
    }

    // ---- custom folders: stored as cf.ids = "id,id" and cf.<id>.name|color|art|bg|items ----

    private static final int[] FOLDER_COLORS = {0xFF6C5CE7, 0xFFE8463E, 0xFFF2A93B, 0xFF2E9E5B, 0xFF1FA99B,
            0xFF2F8BFF, 0xFF3A4FA3, 0xFFD6458F, 0xFF6D8299, 0xFF37474F};
    private static final String[] ART = {"custom", "recent", "android", "apps", "search", "settings",
            "3ds", "nds", "gba", "gbc", "gb", "nes", "snes", "n64", "md", "ps1", "ps2", "ps3", "ps4", "ps5",
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
            for (String k : folderItems(id)) {
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

    /** Text input on the upper screen. */
    private void askText(String title, String initial, java.util.function.Consumer<String> done) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_ACTION_DONE);
        field.setText(initial);
        field.selectAll();
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        AlertDialog d = new AlertDialog.Builder(this, theme).setTitle(title).setView(field)
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
                for (String k : new String[]{"name", "color", "art", "bg", "items"}) e.remove(fk(id, k));
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
        m.add("＋ " + t(S.NEW_FOLDER), false, () -> askText(t(S.NEW_FOLDER), t(S.MY_FOLDER), name -> {
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
        String[] ids = {"continue", "recent", "favorites", "add", "search", "stats", "settings", "desktop"};
        S[] names = {S.CONTINUE, S.RECENT, S.FAVORITES, S.NEW_FOLDER, S.SEARCH, S.STATS, S.SETTINGS, S.ANDROID_DESKTOP};
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
        if (!prefs.getBoolean("bannerMusic", true) || !(e.equals("3ds") || e.equals("cci") || e.equals("cxi") || e.equals("app"))) return;
        if (TopView.wantsVideo(g)) return; // the video brings its own sound
        int seq = bannerSeq;
        main.postDelayed(() -> {
            if (seq != bannerSeq) return;
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
        } else if (item == ShelfView.SETTINGS || item == ShelfView.SEARCH || item == ShelfView.DESKTOP) {
            top.showTool(item);
        } else if (item instanceof Library.Game) {
            Library.Game g = (Library.Game) item;
            String emu = g.pkg != null ? null : Library.emulatorLabel(effectiveCmd(g));
            top.showGame(g, emu, playedTags(g));
        } else if (item instanceof Library.Platform) {
            Library.Platform p = (Library.Platform) item;
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)),
                    p == shelfState.recent ? "recent" : p == shelfState.favorites ? "favorites" : p == shelfState.results ? "search" : null);
        }
    }

    @Override public void onActivate(Library.Game g) {
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
                it = Launch.build(cmd, g);
                pkg = it.getComponent() != null ? it.getComponent().getPackageName() : "";
            }
            addRecent(g);
            prefs.edit().putString("running", g.key() + "\n" + System.currentTimeMillis() + "\n" + pkg).apply();
            ActivityOptions o = ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY);
            startActivity(it, o.toBundle());
        } catch (Exception e) {
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
        if (sfx != null) sfx.play(cue);
    }

    // ---- hidden games: keys in prefs "hiddenGames"; each platform keeps its full list for unhiding ----

    private void applyHiddenGames() {
        if (lib == null) return;
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            List<Library.Game> all = fullLists.computeIfAbsent(p, k -> new ArrayList<>(k.games));
            p.games.clear();
            for (Library.Game g : all) if (!hiddenGames.contains(g.key())) p.games.add(g);
        }
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

    @Override public void onDisplayModeChanged(Library.Game g) {
        top.reset();
        onFocus(shelf.selected());
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
        if (hasUsageAccess() && !parts[2].isEmpty()) {
            try { spent = foregroundTime(parts[2], start, now); } catch (Exception ignored) { }
        }
        spent = Math.max(0, Math.min(spent, MAX_SESSION));
        String key = parts[0];
        // per-day totals feed the "this week" chart on the statistics page
        String day = "day:" + new java.text.SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(new Date(now));
        prefs.edit().putLong("time:" + key, prefs.getLong("time:" + key, 0) + spent).putLong("last:" + key, now)
                .putLong(day, prefs.getLong(day, 0) + spent).apply();
    }

    private String[] playedTags(Library.Game g) {
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
        return new String[]{played, when};
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
        if (e.getAction() == KeyEvent.ACTION_DOWN) return shelf.onKey(code);
        return e.getAction() == KeyEvent.ACTION_UP && code != KeyEvent.KEYCODE_POWER;
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
        if (x != stickX && x != 0) shelf.onKey(x < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
        if (y != stickY && y != 0) shelf.onKey(y < 0 ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
        stickX = x;
        stickY = y;
        return true;
    }

    private static int axis(float v) { return v > 0.6f ? 1 : v < -0.6f ? -1 : 0; }
}
