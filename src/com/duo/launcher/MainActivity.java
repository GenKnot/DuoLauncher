package com.duo.launcher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Presentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

public final class MainActivity extends Activity implements ShelfView.Listener {
    private static final int MAX_RECENT = 20;
    private static final int PICK_IMAGE = 1;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ShelfView.State shelfState = new ShelfView.State();
    private SharedPreferences prefs;
    private Icons icons;
    private TopView top;
    private ShelfView shelf;
    private Presentation pres;
    private Library lib;
    private boolean loading, askedPermission;
    private int stickX, stickY;
    private String pickPlatform, pickKind;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // emulators such as DraStic take plain file paths in extras; allow file:// data too
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        prefs = getSharedPreferences("duo", MODE_PRIVATE);
        I18n.set(prefs.getString("lang", I18n.systemDefault()));
        Theme.init(this);
        icons = new Icons(this);
        top = new TopView(this, icons);
        setContentView(top);
    }

    @Override protected void onResume() {
        super.onResume();
        hideBars();
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
        loading = true;
        top.showMessage(t(S.LOADING));
        new Thread(() -> {
            Library l = Library.load(this);
            main.post(() -> {
                loading = false;
                lib = l;
                shelfState.message = null;
                if (shelf != null) shelf.setLibrary(l, recentGames());
                else shelfState.lib = l;
                if (l.platforms.isEmpty()) top.showMessage(t(S.NO_GAMES));
                if (!prefs.contains("lang")) showLanguageMenu(null, true);
            });
        }, "library").start();
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
        m.add(t(S.LANGUAGE) + "  ·  " + I18n.name(), false, () -> showLanguageMenu(m, false));
        m.add(t(S.APPEARANCE), false, () -> showAppearanceMenu(m));
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
        if (fp != null) main.post(() -> showPlatformLookMenu(parent, fp));
    }

    private String versionName() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    // ---- shelf callbacks ----

    @Override public void onFocus(Object item) {
        if (item == ShelfView.SETTINGS) {
            top.showSettings();
        } else if (item instanceof Library.Game) {
            Library.Game g = (Library.Game) item;
            top.showGame(g, Library.emulatorLabel(effectiveCmd(g)));
        } else if (item instanceof Library.Platform) {
            Library.Platform p = (Library.Platform) item;
            top.showPlatform(p, Library.emulatorLabel(platformCmd(p)), p == shelfState.recent);
        }
    }

    @Override public void onActivate(Library.Game g) {
        try {
            Intent it = Launch.build(effectiveCmd(g), g);
            addRecent(g);
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
