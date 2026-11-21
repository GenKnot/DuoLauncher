package com.duo.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/** Lower screen: home folders and the zoomable icon grid, driven by touch or gamepad. */
final class ShelfView extends View {

    interface Listener {
        void onFocus(Object item);
        void onActivate(Library.Game g);
        void onChoose(Object item, String cmd);
        String chosenCmd(Object item);
        void onSettings();
        void onSearch();
        void onAddFolder();
        void onEditFolder(Library.Platform folder);
        void onAddToFolder(Library.Game g, Menu parent);
        void onRemoveFromFolder(Library.Platform folder, Library.Game g);
        void onHideFolder(Library.Platform folder);
        void onHideGame(Library.Game g);
    }

    /** Marker items for the Search, Settings and "new folder" tiles on the home page. */
    static final Object SETTINGS = new Object(), SEARCH = new Object(), ADD = new Object();

    /** A list overlay; each entry runs its action when picked. B goes back to the parent menu. */
    static final class Menu {
        final String title, subtitle;
        final List<String> labels = new ArrayList<>();
        final List<Boolean> marks = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();
        final List<Integer> swatches = new ArrayList<>();   // optional colour dot per row
        final List<Bitmap> images = new ArrayList<>();      // optional picture per row
        Menu parent;
        int sel, top;

        Menu(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        Menu add(String label, boolean mark, Runnable action) { return add(label, mark, null, null, action); }

        Menu add(String label, boolean mark, Integer swatch, Bitmap image, Runnable action) {
            labels.add(label);
            marks.add(mark);
            actions.add(action);
            swatches.add(swatch);
            images.add(image);
            if (mark && marks.indexOf(true) == marks.size() - 1) sel = marks.size() - 1;
            return this;
        }
    }

    /** Survives the view being recreated each time the presentation is shown. */
    static final class State {
        Library lib;
        final Library.Platform recent = new Library.Platform();
        Library.Platform current;
        final List<Object> items = new ArrayList<>();
        final Map<String, Integer> lastSel = new HashMap<>();
        int sel, zoom = 1;
        boolean byName;
        float scroll, scrollTarget;
        Menu menu;
        String message;

        /** Search results are shown as a pseudo-platform. */
        final Library.Platform results = new Library.Platform();
        String query = "";
        /** Names of platform folders the user hid from the home page. */
        final java.util.Set<String> hidden = new java.util.HashSet<>();

        State() {
            recent.name = "recent";
            results.name = "search";
        }
    }

    private static final int[] ZOOM = {4, 5, 6, 8};
    private static final int BLUE = 0xFF2F8BFF;
    private static final int MENU_ROWS = 8;

    final State st;
    private final Icons icons;
    private final Listener listener;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF(), r2 = new RectF();
    private final Rect src = new Rect();
    private final Path path = new Path();
    private final GestureDetector gd;
    private final OverScroller scroller;
    private final RectF hitHome = new RectF(), hitL = new RectF(), hitR = new RectF(), hitZoom = new RectF(), hitSort = new RectF(),
            hitSearch = new RectF();
    private final Runnable redraw = this::invalidate;

    ShelfView(Context c, State st, Icons icons, Listener l) {
        super(c);
        this.st = st;
        this.icons = icons;
        this.listener = l;
        scroller = new OverScroller(c);
        gd = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { scroller.forceFinished(true); return true; }

            @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                if (st.menu != null) return true;
                st.scroll = st.scrollTarget = clamp(st.scroll + dy);
                invalidate();
                return true;
            }

            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (st.menu != null) return true;
                scroller.fling(0, (int) st.scroll, 0, (int) -vy, 0, 0, 0, (int) maxScroll());
                postInvalidateOnAnimation();
                return true;
            }

            @Override public boolean onSingleTapUp(MotionEvent e) { tap(e.getX(), e.getY()); return true; }
        });
    }

    // ---- state changes ----

    void setLibrary(Library lib, List<Library.Game> recent) {
        st.lib = lib;
        st.message = null;
        setRecent(recent);
        if (st.current == null) goHome(); else fireFocus();
    }

    void setRecent(List<Library.Game> recent) {
        st.recent.games.clear();
        st.recent.games.addAll(recent);
        if (st.current == null && st.lib != null) {
            Object keep = selected();
            rebuildHome();
            st.sel = Math.max(0, st.items.indexOf(keep));
            invalidate();
        }
    }

    private void rebuildHome() {
        st.items.clear();
        if (!st.recent.games.isEmpty()) st.items.add(st.recent);
        for (Library.Platform p : st.lib.platforms) if (!st.hidden.contains(p.name)) st.items.add(p);
        st.items.add(ADD);
        st.items.add(SEARCH);
        st.items.add(SETTINGS);
    }

    void goHome() {
        if (st.lib == null) return;
        if (st.current != null) st.lastSel.put(st.current.name, st.sel);
        st.current = null;
        rebuildHome();
        Integer s = st.lastSel.get("#home");
        st.sel = clampSel(s != null ? s : 0);
        jumpTo();
        fireFocus();
    }

    void open(Library.Platform pl) {
        st.lastSel.put(st.current == null ? "#home" : st.current.name, st.sel);
        st.current = pl;
        st.items.clear();
        List<Library.Game> gs = new ArrayList<>(pl.games);
        if (st.byName && pl != st.recent) {
            Collator col = Collator.getInstance(Locale.forLanguageTag(I18n.code()));
            gs.sort((a, b) -> col.compare(a.title, b.title));
        }
        st.items.addAll(gs);
        Integer s = st.lastSel.get(pl.name);
        st.sel = clampSel(s != null ? s : 0);
        jumpTo();
        fireFocus();
    }

    private void cycle(int d) {
        if (st.lib == null || st.lib.platforms.isEmpty()) return;
        List<Library.Platform> ps = new ArrayList<>();
        for (Library.Platform p : st.lib.platforms) if (!st.hidden.contains(p.name)) ps.add(p);
        if (ps.isEmpty()) return;
        int i = st.current == null ? -1 : ps.indexOf(st.current);
        int n = i + d;
        if (n < -1) n = ps.size() - 1;
        if (n >= ps.size()) n = -1;
        if (n == -1) goHome(); else open(ps.get(n));
    }

    private int cols() { return st.current == null ? 3 : ZOOM[st.zoom]; }

    private int clampSel(int i) { return Math.max(0, Math.min(i, st.items.size() - 1)); }

    Object selected() { return st.sel >= 0 && st.sel < st.items.size() ? st.items.get(st.sel) : null; }

    private void setSel(int i) {
        i = clampSel(i);
        if (i == st.sel) return;
        st.sel = i;
        ensureVisible();
        fireFocus();
    }

    private void move(int dx, int dy) {
        if (st.items.isEmpty()) return;
        int c = cols(), n = st.items.size(), i = st.sel + dx;
        if (dy != 0) {
            int ni = i + dy * c;
            if (ni >= n) ni = (i / c < (n - 1) / c) ? n - 1 : i; // partial last row
            if (ni < 0) ni = i;
            i = ni;
        }
        setSel(i);
    }

    private void activate() {
        Object o = selected();
        if (o == SETTINGS) listener.onSettings();
        else if (o == SEARCH) listener.onSearch();
        else if (o == ADD) listener.onAddFolder();
        else if (o instanceof Library.Platform) open((Library.Platform) o);
        else if (o instanceof Library.Game) listener.onActivate((Library.Game) o);
    }

    private void zoom() {
        if (st.current == null) return;
        st.zoom = (st.zoom + 1) % ZOOM.length;
        jumpTo();
        invalidate();
    }

    private void sort() {
        if (st.current == null || st.current == st.recent) return;
        Object keep = selected();
        st.byName = !st.byName;
        Library.Platform cur = st.current;
        st.lastSel.put(cur.name, 0);
        st.current = null;
        open(cur);
        int i = st.items.indexOf(keep);
        if (i >= 0) { st.sel = i; jumpTo(); }
        invalidate();
    }

    // ---- menus ----

    void showMenu(Menu m) {
        st.menu = m;
        menuScrollToSel();
        invalidate();
    }

    void closeMenu() {
        st.menu = null;
        fireFocus();
    }

    private void menuBack() {
        st.menu = st.menu.parent;
        if (st.menu == null) fireFocus(); else invalidate();
    }

    private void menuPick() {
        Menu m = st.menu;
        Runnable a = m.actions.get(m.sel);
        if (a != null) a.run();
        invalidate();
    }

    private void menuScrollToSel() {
        Menu m = st.menu;
        if (m == null) return;
        if (m.sel < m.top) m.top = m.sel;
        if (m.sel >= m.top + MENU_ROWS) m.top = m.sel - MENU_ROWS + 1;
    }

    /** Y: emulator choice (and folder actions) for a game, emulator default for a platform, or edit a custom folder. */
    private void openEmulatorMenu() {
        Object o = selected();
        if (o instanceof Library.Platform) {
            Library.Platform pl = (Library.Platform) o;
            if (pl.isCustom()) { listener.onEditFolder(pl); return; }
            if (pl == st.recent || pl == st.results) return;
            Menu m = new Menu(isNative(pl) ? pl.title() : t(S.CHOOSE_EMULATOR), isNative(pl) ? null : I18n.f(S.PLATFORM_DEFAULT, pl.name));
            if (!isNative(pl)) {
                String cur = listener.chosenCmd(o);
                m.add(I18n.f(S.PEGASUS_DEFAULT, Library.emulatorLabel(pl.launch)), cur == null, () -> { listener.onChoose(o, null); closeMenu(); });
                for (String[] opt : Library.options(pl, getContext().getPackageManager()))
                    m.add(opt[0], opt[1].equals(cur), () -> { listener.onChoose(o, opt[1]); closeMenu(); });
            }
            m.add("－ " + t(S.HIDE_FOLDER), false, () -> { closeMenu(); listener.onHideFolder(pl); });
            showMenu(m);
            return;
        }
        if (!(o instanceof Library.Game)) return;
        Library.Game g = (Library.Game) o;
        boolean emulated = g.pkg == null;
        Menu m = new Menu(emulated ? t(S.CHOOSE_EMULATOR) : g.title, emulated ? g.title : g.platform.title());
        if (emulated) {
            String cur = listener.chosenCmd(g);
            m.add(t(S.FOLLOW_PLATFORM), cur == null, () -> { listener.onChoose(g, null); closeMenu(); });
            for (String[] opt : Library.options(g.platform, getContext().getPackageManager()))
                m.add(opt[0], opt[1].equals(cur), () -> { listener.onChoose(g, opt[1]); closeMenu(); });
        }
        m.add("＋ " + t(S.ADD_TO_FOLDER), false, () -> listener.onAddToFolder(g, m));
        Library.Platform here = st.current;
        if (here != null && here.isCustom())
            m.add("－ " + t(S.REMOVE_FROM_FOLDER), false, () -> { closeMenu(); listener.onRemoveFromFolder(here, g); });
        m.add("－ " + t(S.HIDE_GAME), false, () -> { closeMenu(); listener.onHideGame(g); });
        showMenu(m);
    }

    /** Custom folders are rebuilt as new objects on every change; find them again by name. */
    private int indexOfName(String name) {
        for (int i = 0; i < st.items.size(); i++)
            if (st.items.get(i) instanceof Library.Platform && ((Library.Platform) st.items.get(i)).name.equals(name)) return i;
        return -1;
    }

    /** Moves the selection to an item on the current page, if it is there. */
    void select(Object item) {
        int i = st.items.indexOf(item);
        if (i < 0) return;
        st.sel = i;
        ensureVisible();
        fireFocus();
    }

    /** Rebuilds the current page after folders were created, edited or deleted. */
    void refresh() {
        if (st.lib == null) return;
        if (st.current == null) {
            Object keep = selected();
            rebuildHome();
            int i = keep instanceof Library.Platform && ((Library.Platform) keep).isCustom()
                    ? indexOfName(((Library.Platform) keep).name) : st.items.indexOf(keep);
            st.sel = clampSel(i >= 0 ? i : st.sel);
        } else if (st.current.isCustom()) {
            Library.Platform cur = st.current;
            Library.Platform fresh = null;
            for (Library.Platform p : st.lib.platforms) if (p.name.equals(cur.name)) fresh = p;
            if (fresh == null) { goHome(); return; }
            int sel = st.sel;
            st.current = null;
            open(fresh);
            st.sel = clampSel(sel);
        } else if (st.current != st.recent && st.current != st.results) {
            // a game on this page was hidden: rebuild the page, keeping the cursor near where it was
            Library.Platform cur = st.current;
            int sel = st.sel;
            st.current = null;
            open(cur);
            st.sel = clampSel(sel);
        } else if (st.current == st.recent) {
            st.items.clear();
            st.items.addAll(st.recent.games);
            st.sel = clampSel(st.sel);
        }
        fireFocus();
    }

    private void fireFocus() {
        listener.onFocus(selected());
        invalidate();
    }

    boolean onKey(int code) {
        if (st.lib == null) return false;
        if (st.menu != null) {
            Menu m = st.menu;
            switch (code) {
                case KeyEvent.KEYCODE_DPAD_UP: m.sel = Math.max(0, m.sel - 1); menuScrollToSel(); invalidate(); return true;
                case KeyEvent.KEYCODE_DPAD_DOWN: m.sel = Math.min(m.labels.size() - 1, m.sel + 1); menuScrollToSel(); invalidate(); return true;
                case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_BUTTON_START:
                    menuPick(); return true;
                case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                    menuBack(); return true;
                default: return true;
            }
        }
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_LEFT: move(-1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: move(1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_UP: move(0, -1); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN: move(0, 1); return true;
            case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_BUTTON_START:
                activate(); return true;
            case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE:
                if (st.current != null) goHome();
                return true;
            case KeyEvent.KEYCODE_BUTTON_X: zoom(); return true;
            case KeyEvent.KEYCODE_BUTTON_Y: openEmulatorMenu(); return true;
            case KeyEvent.KEYCODE_BUTTON_L1: case KeyEvent.KEYCODE_PAGE_UP: cycle(-1); return true;
            case KeyEvent.KEYCODE_BUTTON_R1: case KeyEvent.KEYCODE_PAGE_DOWN: cycle(1); return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT: sort(); return true;
            case KeyEvent.KEYCODE_BUTTON_THUMBR: case KeyEvent.KEYCODE_SEARCH: listener.onSearch(); return true;
            default: return false;
        }
    }

    // ---- geometry (designed on a 1280x960 canvas) ----

    private float u() { return getWidth() / 1280f; }
    private float headerH() { return 96 * u(); }
    private float footerH() { return 76 * u(); }
    private float pad() { return 30 * u(); }
    private float cellW() { return (getWidth() - 2 * pad()) / cols(); }
    private float cellH() { return st.current == null ? cellW() * 0.8f : cellW(); }
    private float viewH() { return getHeight() - headerH() - footerH(); }
    private float contentH() { int rows = (st.items.size() + cols() - 1) / cols(); return rows * cellH() + 40 * u(); }
    private float maxScroll() { return Math.max(0, contentH() - viewH()); }
    private float clamp(float s) { return Math.max(0, Math.min(maxScroll(), s)); }

    private void tileRect(int i, RectF out) {
        int c = i % cols(), row = i / cols();
        float x = pad() + c * cellW(), y = headerH() + 18 * u() + row * cellH() - st.scroll;
        float g = cellW() * (st.current == null ? 0.04f : 0.07f);
        out.set(x + g, y + g, x + cellW() - g, y + cellH() - g);
    }

    private void ensureVisible() {
        if (getWidth() == 0) return;
        float top = (st.sel / cols()) * cellH(), bottom = top + cellH() + 40 * u();
        float t = st.scrollTarget;
        if (top < t) t = top;
        if (bottom > t + viewH()) t = bottom - viewH();
        st.scrollTarget = clamp(t);
        postInvalidateOnAnimation();
    }

    private void jumpTo() {
        st.scrollTarget = st.scroll = 0;
        ensureVisible();
        st.scroll = st.scrollTarget;
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) { jumpTo(); }

    @Override public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            st.scroll = st.scrollTarget = clamp(scroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }

    // ---- drawing ----

    @Override protected void onDraw(Canvas c) {
        float u = u(), w = getWidth(), h = getHeight();
        p.setShader(new LinearGradient(0, 0, 0, h, Palette.shelfTop(), Palette.shelfBottom(), Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        if (st.lib == null || st.message != null) {
            String m = st.message != null ? st.message : t(S.LOADING);
            float y = h / 2;
            for (String line : m.split("\n")) {
                text(c, line, w / 2, y, 30 * u, Palette.text2(), false, Paint.Align.CENTER);
                y += 46 * u;
            }
            if (st.menu != null) drawMenu(c);
            return;
        }

        if (Math.abs(st.scroll - st.scrollTarget) > 0.5f) {
            st.scroll += (st.scrollTarget - st.scroll) * 0.28f;
            postInvalidateOnAnimation();
        } else {
            st.scroll = st.scrollTarget;
        }

        c.save();
        c.clipRect(0, headerH(), w, h - footerH());
        int c0 = cols();
        int firstRow = (int) Math.max(0, (st.scroll - 18 * u) / cellH()), lastRow = (int) ((st.scroll + viewH()) / cellH()) + 1;
        for (int i = firstRow * c0; i < Math.min(st.items.size(), (lastRow + 1) * c0); i++) {
            if (i != st.sel) drawItem(c, i, false);
        }
        if (st.sel < st.items.size()) drawItem(c, st.sel, st.menu == null);
        c.restore();
        if (st.lib.platforms.isEmpty() || (st.current == st.results && st.items.isEmpty())) {
            // explain why the page is empty
            float y = headerH() + (st.items.isEmpty() ? 160 * u : cellH() + 90 * u);
            String msg = st.current == st.results ? t(S.NO_RESULTS) : t(S.NO_GAMES);
            for (String line : msg.split("\n")) {
                text(c, line, w / 2, y, 28 * u, Palette.text2(), false, Paint.Align.CENTER);
                y += 44 * u;
            }
        }

        drawHeader(c);
        drawFooter(c);
        if (st.menu != null) drawMenu(c);
    }

    private void drawItem(Canvas c, int i, boolean selected) {
        tileRect(i, r);
        if (r.bottom < headerH() || r.top > getHeight() - footerH()) return;
        Object o = st.items.get(i);
        c.save();
        if (selected) c.scale(1.07f, 1.07f, r.centerX(), r.centerY());
        float rad = r.width() * (st.current == null ? 0.07f : 0.16f);
        if (o == st.recent) drawRecent(c, rad);
        else if (o == SETTINGS) drawSettings(c, rad);
        else if (o == SEARCH) drawSearchTile(c, rad);
        else if (o == ADD) drawAddTile(c, rad);
        else if (o instanceof Library.Platform) drawFolder(c, (Library.Platform) o, rad);
        else drawGame(c, (Library.Game) o, rad);
        if (selected) {
            float a = 0.65f + 0.35f * (float) Math.sin(SystemClock.uptimeMillis() / 180.0);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6 * u());
            p.setColor(BLUE);
            p.setAlpha((int) (255 * a));
            r2.set(r);
            r2.inset(-4 * u(), -4 * u());
            c.drawRoundRect(r2, rad + 4 * u(), rad + 4 * u(), p);
            p.setStyle(Paint.Style.FILL);
            p.setAlpha(255);
            postInvalidateDelayed(40);
        }
        c.restore();
    }

    private void card(Canvas c, RectF rr, float rad, int color) {
        p.setColor(color);
        p.setShadowLayer(10 * u(), 0, 4 * u(), Palette.shadow());
        c.drawRoundRect(rr, rad, rad, p);
        p.clearShadowLayer();
    }

    private void drawGame(Canvas c, Library.Game g, float rad) {
        card(c, r, rad, Palette.card());
        Bitmap b = icons.tile(g, redraw);
        path.reset();
        path.addRoundRect(r, rad, rad, Path.Direction.CW);
        c.save();
        c.clipPath(path);
        if (b != null) {
            r2.set(r);
            if (b.hasAlpha()) r2.inset(r.width() * 0.1f, r.height() * 0.1f);
            src.set(0, 0, b.getWidth(), b.getHeight());
            c.drawBitmap(b, src, r2, p);
        } else {
            p.setColor(0xFF000000 | (g.title.hashCode() & 0x7F7F7F) | 0x404040);
            c.drawRect(r, p);
            String s = g.title.length() > 4 ? g.title.substring(0, 4) : g.title;
            text(c, s, r.centerX(), r.centerY() + r.height() * 0.08f, r.height() * 0.2f, 0xFFFFFFFF, true, Paint.Align.CENTER);
        }
        c.restore();
    }

    /** Colours of the home-page tool folders. */
    private static final int RECENT_COLOR = 0xFFF0943A, SEARCH_COLOR = 0xFF2F8BFF, SETTINGS_COLOR = 0xFF5E7186, ADD_COLOR = 0xFF9B7BE0;

    /** Folder body (tab + front) in {@code color}, drawn into {@link #r}. */
    private void folderShape(Canvas c, float rad, int color) {
        float w = r.width(), h = r.height();
        r2.set(r.left, r.top, r.left + w * 0.42f, r.top + h * 0.3f);
        p.setColor(mul(color, 0.72f));
        c.drawRoundRect(r2, rad, rad, p);
        r2.set(r.left, r.top + h * 0.12f, r.right, r.bottom);
        p.setShadowLayer(10 * u(), 0, 4 * u(), Palette.shadow());
        p.setShader(new LinearGradient(0, r2.top, 0, r2.bottom, lighten(color), color, Shader.TileMode.CLAMP));
        c.drawRoundRect(r2, rad, rad, p);
        p.setShader(null);
        p.clearShadowLayer();
    }

    /** Picture in the folder's top-right area, label bottom-left, small note bottom-right. */
    private void folderContent(Canvas c, Bitmap pic, String fallback, String label, String note) {
        float w = r.width(), h = r.height();
        ConsoleDrawable d = new ConsoleDrawable(fallback, pic, false);
        d.setBounds((int) (r.left + w * 0.4f), (int) (r.top + h * 0.18f), (int) (r.right - w * 0.05f), (int) (r.bottom - h * 0.28f));
        d.draw(c);
        text(c, ellipsize(label, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        if (note != null)
            text(c, note, r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawFolder(Canvas c, Library.Platform pl, float rad) {
        folderShape(c, rad, pl.accent());
        float w = r.width(), h = r.height();
        ConsoleDrawable d = ConsoleDrawable.of(pl, false);
        d.setBounds((int) (r.left + w * 0.4f), (int) (r.top + h * 0.18f), (int) (r.right - w * 0.05f), (int) (r.bottom - h * 0.28f));
        d.draw(c);
        String label = isNative(pl) || pl.isCustom() ? pl.title() : pl.name;
        text(c, ellipsize(label, w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(pl.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawRecent(Canvas c, float rad) {
        folderShape(c, rad, RECENT_COLOR);
        float w = r.width(), h = r.height();
        // the three latest games fanned out in the picture area
        int n = Math.min(3, st.recent.games.size());
        float s = h * 0.36f, cx = r.left + w * 0.66f, cy = r.top + h * 0.42f;
        float[] dx = {-0.42f, 0.42f, 0f}, rot = {-12, 12, 0};
        int[] order = n == 3 ? new int[]{2, 1, 0} : n == 2 ? new int[]{1, 0} : new int[]{0};
        for (int i : order) {
            Bitmap b = icons.tile(st.recent.games.get(i), redraw);
            int slot = n == 1 ? 2 : i == 0 ? 2 : i == 1 ? 0 : 1;
            c.save();
            c.translate(cx + dx[slot] * s, cy);
            c.rotate(rot[slot]);
            r2.set(-s / 2, -s / 2, s / 2, s / 2);
            p.setColor(0xFFFFFFFF);
            p.setShadowLayer(6 * u(), 0, 3 * u(), 0x40000000);
            c.drawRoundRect(r2, s * 0.16f, s * 0.16f, p);
            p.clearShadowLayer();
            if (b != null) {
                path.reset();
                path.addRoundRect(r2, s * 0.16f, s * 0.16f, Path.Direction.CW);
                c.clipPath(path);
                if (b.hasAlpha()) r2.inset(s * 0.1f, s * 0.1f);
                src.set(0, 0, b.getWidth(), b.getHeight());
                c.drawBitmap(b, src, r2, p);
            }
            c.restore();
        }
        text(c, ellipsize(t(S.RECENT), w * 0.7f, h * 0.16f), r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(st.recent.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawSettings(Canvas c, float rad) {
        folderShape(c, rad, SETTINGS_COLOR);
        folderContent(c, Theme.toolIcon("settings"), "gear", t(S.SETTINGS), null);
    }

    private void drawAddTile(Canvas c, float rad) {
        folderShape(c, rad, ADD_COLOR);
        // dashed inner outline, so it reads as "make a new one"
        float inset = 8 * u();
        r2.set(r.left + inset, r.top + r.height() * 0.12f + inset, r.right - inset, r.bottom - inset);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f * u());
        p.setPathEffect(new android.graphics.DashPathEffect(new float[]{12 * u(), 9 * u()}, 0));
        p.setColor(0x99FFFFFF);
        c.drawRoundRect(r2, rad * 0.8f, rad * 0.8f, p);
        p.setPathEffect(null);
        p.setStyle(Paint.Style.FILL);
        folderContent(c, Theme.toolIcon("custom"), "apps", t(S.NEW_FOLDER), "+");
    }

    /** Installed Android games and apps, which run without an emulator. */
    private static boolean isNative(Library.Platform p) { return p.system.equals("ANDROID") || p.system.equals("APPS"); }

    private void drawSearchTile(Canvas c, float rad) {
        folderShape(c, rad, SEARCH_COLOR);
        folderContent(c, Theme.toolIcon("search"), "search", t(S.SEARCH), "R3");
    }

    /** Shows search results as their own page; called again as the query changes. */
    void showResults(String query, List<Library.Game> games) {
        st.query = query;
        st.results.games.clear();
        st.results.games.addAll(games);
        if (st.current != st.results) {
            open(st.results);
        } else {
            st.items.clear();
            st.items.addAll(games);
            st.sel = clampSel(0);
            jumpTo();
            fireFocus();
        }
    }

    private void pill(Canvas c, RectF rr, String s, boolean on) {
        p.setColor(on ? BLUE : Palette.pill());
        p.setShadowLayer(6 * u(), 0, 2 * u(), 0x22000000);
        c.drawRoundRect(rr, rr.height() / 2, rr.height() / 2, p);
        p.clearShadowLayer();
        text(c, s, rr.centerX(), rr.centerY() + rr.height() * 0.17f, rr.height() * 0.45f, on ? 0xFFFFFFFF : Palette.pillText(), true, Paint.Align.CENTER);
    }

    private void drawHeader(Canvas c) {
        float u = u(), y = 22 * u, hh = 52 * u, x = pad();
        hitL.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitL, "L", false);
        x = hitL.right + 14 * u;
        String home = "⌂ " + t(S.HOME);
        hitHome.set(x, y, x + Math.max(150 * u, measure(home, hh * 0.45f) + 60 * u), y + hh);
        pill(c, hitHome, home, st.current == null);
        x = hitHome.right + 14 * u;
        if (st.current != null) {
            String s = st.current == st.recent ? t(S.RECENT)
                    : st.current == st.results ? I18n.f(S.SEARCH_RESULTS, st.query) : st.current.title();
            s = ellipsize(s, 420 * u, hh * 0.45f);
            r2.set(x, y, x + measure(s, hh * 0.45f) + 60 * u, y + hh);
            pill(c, r2, s, true);
            x = r2.right + 14 * u;
        }
        hitR.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitR, "R", false);
        // search button at the right edge, count just left of it
        float sw = measure("⌕ " + t(S.SEARCH), hh * 0.45f) + 50 * u;
        hitSearch.set(getWidth() - pad() - sw, y, getWidth() - pad(), y + hh);
        pill(c, hitSearch, "⌕ " + t(S.SEARCH), false);
        int shown = 0;
        for (Library.Platform pl : st.lib.platforms) if (!st.hidden.contains(pl.name)) shown++;
        String right = st.current == null ? I18n.f(S.PLATFORMS_COUNT, shown) : I18n.f(S.GAMES_COUNT, st.items.size());
        float rx = hitSearch.left - 20 * u;
        if (rx - measure(right, hh * 0.42f) > hitR.right + 10 * u)
            text(c, right, rx, y + hh * 0.68f, hh * 0.42f, Palette.text2(), true, Paint.Align.RIGHT);
    }

    private void drawFooter(Canvas c) {
        float u = u(), top = getHeight() - footerH(), cy = top + footerH() / 2;
        p.setColor(Palette.footer());
        c.drawRect(0, top, getWidth(), getHeight(), p);
        p.setColor(Palette.footerLine());
        c.drawRect(0, top, getWidth(), top + 2 * u, p);
        float x = pad();
        Object sel = selected();
        x = hintItem(c, x, cy, "A", sel instanceof Library.Game ? t(S.PLAY) : t(S.OPEN));
        if (st.current != null) x = hintItem(c, x, cy, "B", t(S.BACK));
        if (sel instanceof Library.Game) {
            x = hintItem(c, x, cy, "Y", ((Library.Game) sel).pkg == null ? t(S.EMULATOR) : t(S.ADD_TO_FOLDER));
        } else if (sel instanceof Library.Platform && ((Library.Platform) sel).isCustom()) {
            x = hintItem(c, x, cy, "Y", t(S.EDIT_FOLDER));
        } else if (sel instanceof Library.Platform && sel != st.recent && sel != st.results) {
            x = hintItem(c, x, cy, "Y", isNative((Library.Platform) sel) ? t(S.HIDE_FOLDER) : t(S.EMULATOR));
        }
        hitZoom.setEmpty();
        hitSort.setEmpty();
        if (st.current != null) {
            String[] z = {t(S.ZOOM_L), t(S.ZOOM_M), t(S.ZOOM_S), t(S.ZOOM_XS)};
            float x0 = x;
            x = hintItem(c, x, cy, "X", t(S.SIZE) + " · " + z[st.zoom]);
            hitZoom.set(x0, top, x, getHeight());
            if (st.current != st.recent) {
                x0 = x;
                x = hintItem(c, x, cy, "SEL", t(S.SORT) + " · " + (st.byName ? t(S.SORT_NAME) : t(S.SORT_DEFAULT)));
                hitSort.set(x0, top, x, getHeight());
            }
        }
    }

    private float hintItem(Canvas c, float x, float cy, String key, String label) {
        float u = u(), rad = 17 * u, kw = key.length() > 1 ? 56 * u : rad * 2;
        r2.set(x, cy - rad, x + kw, cy + rad);
        p.setColor(Palette.key());
        c.drawRoundRect(r2, rad, rad, p);
        text(c, key, r2.centerX(), cy + 7 * u, key.length() > 1 ? 16 * u : 20 * u, Palette.keyText(), true, Paint.Align.CENTER);
        float tx = r2.right + 10 * u;
        text(c, label, tx, cy + 9 * u, 25 * u, Palette.pillText(), false, Paint.Align.LEFT);
        return tx + measure(label, 25 * u) + 34 * u;
    }

    private float menuRowH() { return 74 * u(); }

    private void menuCard(RectF out) {
        float u = u(), mw = 860 * u;
        int rows = Math.min(MENU_ROWS, st.menu.labels.size());
        float mh = 110 * u + rows * menuRowH() + 24 * u;
        out.set((getWidth() - mw) / 2, (getHeight() - mh) / 2, (getWidth() + mw) / 2, (getHeight() + mh) / 2);
    }

    private void drawMenu(Canvas c) {
        float u = u();
        Menu m = st.menu;
        p.setColor(0x88000000);
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        RectF cardR = new RectF();
        menuCard(cardR);
        card(c, cardR, 28 * u, Palette.card());
        text(c, m.title, cardR.left + 36 * u, cardR.top + 56 * u, 32 * u, Palette.text(), true, Paint.Align.LEFT);
        if (m.subtitle != null)
            text(c, ellipsize(m.subtitle, cardR.width() * 0.45f, 23 * u), cardR.right - 36 * u, cardR.top + 56 * u, 23 * u, Palette.text3(), false, Paint.Align.RIGHT);
        int rows = Math.min(MENU_ROWS, m.labels.size());
        for (int k = 0; k < rows; k++) {
            int i = m.top + k;
            float y = cardR.top + 94 * u + k * menuRowH();
            r2.set(cardR.left + 20 * u, y, cardR.right - 20 * u, y + menuRowH() - 8 * u);
            boolean on = i == m.sel;
            if (on) {
                p.setColor(BLUE);
                c.drawRoundRect(r2, 16 * u, 16 * u, p);
            }
            int fg = on ? 0xFFFFFFFF : Palette.text();
            float tx = r2.left + 28 * u;
            Integer sw = m.swatches.get(i);
            Bitmap img = m.images.get(i);
            if (sw != null) {
                p.setColor(sw);
                c.drawCircle(tx + 18 * u, r2.centerY(), 18 * u, p);
                tx += 52 * u;
            }
            if (img != null) {
                float ih = menuRowH() - 22 * u, iw = ih * img.getWidth() / img.getHeight();
                RectF ir = new RectF(tx, r2.centerY() - ih / 2, tx + iw, r2.centerY() + ih / 2);
                src.set(0, 0, img.getWidth(), img.getHeight());
                c.drawBitmap(img, src, ir, p);
                tx = ir.right + 16 * u;
            }
            text(c, ellipsize(m.labels.get(i), r2.right - tx - 80 * u, 27 * u), tx, r2.centerY() + 10 * u, 27 * u, fg, on, Paint.Align.LEFT);
            if (m.marks.get(i)) text(c, "✓", r2.right - 28 * u, r2.centerY() + 11 * u, 30 * u, on ? 0xFFFFFFFF : BLUE, true, Paint.Align.RIGHT);
        }
        if (m.top > 0) text(c, "▲", cardR.centerX(), cardR.top + 92 * u, 18 * u, 0xFF8090A0, false, Paint.Align.CENTER);
        if (m.top + rows < m.labels.size()) text(c, "▼", cardR.centerX(), cardR.bottom - 8 * u, 18 * u, 0xFF8090A0, false, Paint.Align.CENTER);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color, boolean bold, Paint.Align a) {
        tp.setTextSize(size);
        tp.setColor(color);
        tp.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tp.setTextAlign(a);
        c.drawText(s, x, y, tp);
    }

    private float measure(String s, float size) {
        tp.setTextSize(size);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        return tp.measureText(s);
    }

    private String ellipsize(String s, float maxW, float size) {
        tp.setTextSize(size);
        if (tp.measureText(s) <= maxW) return s;
        while (s.length() > 1 && tp.measureText(s + "…") > maxW) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    private static int mul(int c, float f) {
        return 0xFF000000 | (int) (((c >> 16) & 255) * f) << 16 | (int) (((c >> 8) & 255) * f) << 8 | (int) ((c & 255) * f);
    }

    private static int lighten(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return 0xFF000000 | (r + (255 - r) / 4) << 16 | (g + (255 - g) / 4) << 8 | (b + (255 - b) / 4);
    }

    // ---- touch ----

    @Override public boolean onTouchEvent(MotionEvent e) { return gd.onTouchEvent(e) || super.onTouchEvent(e); }

    private void tap(float x, float y) {
        if (st.menu != null) {
            RectF cardR = new RectF();
            menuCard(cardR);
            if (!cardR.contains(x, y)) { menuBack(); return; }
            int k = (int) ((y - cardR.top - 94 * u()) / menuRowH());
            int i = st.menu.top + k;
            if (k >= 0 && k < MENU_ROWS && i < st.menu.labels.size()) { st.menu.sel = i; menuPick(); }
            return;
        }
        if (st.lib == null) return;
        if (hitHome.contains(x, y)) { goHome(); return; }
        if (hitL.contains(x, y)) { cycle(-1); return; }
        if (hitR.contains(x, y)) { cycle(1); return; }
        if (hitZoom.contains(x, y)) { zoom(); return; }
        if (hitSort.contains(x, y)) { sort(); return; }
        if (hitSearch.contains(x, y)) { listener.onSearch(); return; }
        if (y < headerH() || y > getHeight() - footerH()) return;
        for (int i = 0; i < st.items.size(); i++) {
            tileRect(i, r);
            if (r.contains(x, y)) {
                if (i == st.sel) activate(); else setSel(i);
                return;
            }
        }
    }
}
