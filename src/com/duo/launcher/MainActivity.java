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

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
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
        Theme.init(this);
        icons = new Icons(this);
        top = new TopView(this, icons);
        setContentView(top);
    }

    @Override protected void onResume() {
        super.onResume();
        hideBars();
        applyDisplayMode();
        finishPlaySession();
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
            if (shelfState.menu == null) onFocus(shelf.selected());
            if (!prefs.contains("lang") && shelfState.menu == null) showLanguageMenu(null, true);
        }
    }

    @Override protected void onPause() {
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
        };
        pres.show();
    }

    private void load() {
        if (loading) { reloadPending = true; return; }
        loading = true;
        if (lib == null) top.showMessage(t(S.LOADING));
        List<String> folders = folders();
        boolean autoFind = prefs.getBoolean("autoFind", true);
        Set<String> apps = new HashSet<>(prefs.getStringSet("apps", new HashSet<>()));
        new Thread(() -> {
            Library l = Library.load(this, folders, autoFind, apps);
            main.post(() -> {
                boolean rescan = lib != null;
                loading = false;
                lib = l;
                shelfState.message = null;
                shelfState.current = null;
                applyCustomFolders();
                if (shelf != null) shelf.setLibrary(l, recentGames());
                else shelfState.lib = l;
                int n = 0;
                for (Library.Platform p : l.platforms) n += p.games.size();
                if (n == 0) Toast.makeText(this, t(S.NO_GAMES), Toast.LENGTH_LONG).show();
                else if (rescan && !reloadPending) Toast.makeText(this, I18n.f(S.SCAN_DONE, n), Toast.LENGTH_SHORT).show();
                if (!prefs.contains("lang")) showLanguageMenu(null, true);
                if (reloadPending) { reloadPending = false; load(); }
            });
        }, "library").start();
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
        m.add(t(S.SET_HOME), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); } catch (Exception ignored) { }
        });
        if (!hasUsageAccess()) m.add(t(S.USAGE_ACCESS), false, () -> {
            try { startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)); } catch (Exception ignored) { }
        });
        m.add(t(S.ABOUT) + "  ·  Duo Launcher " + versionName(), false, null);
        shelf.showMenu(m);
    }

    private void showLanguageMenu(ShelfView.Menu parent, boolean firstRun) {
        if (shelf == null) return;
        ShelfView.Menu m = new ShelfView.Menu(firstRun ? t(S.WELCOME) : t(S.LANGUAGE), firstRun ? t(S.LANGUAGE) : null);
        m.parent = parent;
        for (int i = 0; i < I18n.CODES.length; i++) {
            String code = I18n.CODES[i];
            m.add(I18n.NAMES[i], i == I18n.index(), () -> {
                I18n.set(code);
                prefs.edit().putString("lang", code).apply();
                if (firstRun) shelf.closeMenu(); else onSettings();
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
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), false);
            showPlatformLookMenu(parent, p);
        });
        m.add(t(S.BACKGROUND_IMAGE) + " · " + t(S.PICK_IMAGE), false, () -> pickImage(p, Theme.BACKGROUND));
        if (customBg) m.add(t(S.BACKGROUND_IMAGE) + " · " + t(S.RESET_DEFAULT), false, () -> {
            Theme.reset(p.name, Theme.BACKGROUND);
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), false);
            showPlatformLookMenu(parent, p);
        });
        shelf.showMenu(m);
        // preview this platform on the upper screen while its look is being edited
        top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), false);
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

    @Override public void onSearch() {
        if (lib == null) return;
        EditText field = new EditText(this);
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
        int theme = Palette.dark ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        AlertDialog d = new AlertDialog.Builder(this, theme)
                .setTitle(t(S.SEARCH))
                .setView(field)
                .setPositiveButton(t(S.DONE), null)
                .create();
        d.setOnShowListener(x -> {
            field.requestFocus();
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });
        field.setOnEditorActionListener((v, action, ev) -> {
            d.dismiss();
            return true;
        });
        d.show();
    }

    private void search(String q) {
        if (shelf == null || lib == null) return;
        String needle = q.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) return;
        List<Library.Game> hits = new ArrayList<>();
        for (Library.Platform p : lib.platforms)
            for (Library.Game g : p.games)
                if (g.title.toLowerCase(Locale.ROOT).contains(needle) || g.file.toLowerCase(Locale.ROOT).contains(needle)) hits.add(g);
        shelf.showResults(q.trim(), hits);
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
                if (g != null) p.games.add(g);
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

    @Override public void onRemoveFromFolder(Library.Platform folder, Library.Game g) {
        String id = idOf(folder);
        List<String> items = folderItems(id);
        items.remove(g.key());
        prefs.edit().putString(fk(id, "items"), String.join("\n", items)).apply();
        foldersChanged();
    }

    // ---- shelf callbacks ----

    @Override public void onFocus(Object item) {
        if (item == ShelfView.SETTINGS || item == ShelfView.SEARCH) {
            top.showTool(item);
        } else if (item instanceof Library.Game) {
            Library.Game g = (Library.Game) item;
            String emu = g.pkg != null ? null : Library.emulatorLabel(effectiveCmd(g));
            top.showGame(g, emu, playedTags(g));
        } else if (item instanceof Library.Platform) {
            Library.Platform p = (Library.Platform) item;
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), p == shelfState.recent || p == shelfState.results);
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
            if (g != null) out.add(g);
        }
        return out;
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
        prefs.edit().putLong("time:" + key, prefs.getLong("time:" + key, 0) + spent).putLong("last:" + key, now).apply();
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
        if (e.getAction() == KeyEvent.ACTION_DOWN) return shelf.onKey(code);
        return e.getAction() == KeyEvent.ACTION_UP && code != KeyEvent.KEYCODE_POWER;
    }

    private boolean handleMotion(MotionEvent e) {
        if (shelf == null || (e.getSource() & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK
                || e.getAction() != MotionEvent.ACTION_MOVE) return false;
        int x = axis(e.getAxisValue(MotionEvent.AXIS_X)), y = axis(e.getAxisValue(MotionEvent.AXIS_Y));
        if (x == 0) x = axis(e.getAxisValue(MotionEvent.AXIS_HAT_X));
        if (y == 0) y = axis(e.getAxisValue(MotionEvent.AXIS_HAT_Y));
        if (x != stickX && x != 0) shelf.onKey(x < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
        if (y != stickY && y != 0) shelf.onKey(y < 0 ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
        stickX = x;
        stickY = y;
        return true;
    }

    private static int axis(float v) { return v > 0.6f ? 1 : v < -0.6f ? -1 : 0; }
}
