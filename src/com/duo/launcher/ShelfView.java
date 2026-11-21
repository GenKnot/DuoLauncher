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
    }

    /** Marker item for the Settings tile on the home page. */
    static final Object SETTINGS = new Object();

    /** A list overlay; each entry runs its action when picked. B goes back to the parent menu. */
    static final class Menu {
        final String title, subtitle;
        final List<String> labels = new ArrayList<>();
        final List<Boolean> marks = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();
        Menu parent;
        int sel, top;

        Menu(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        Menu add(String label, boolean mark, Runnable action) {
            labels.add(label);
            marks.add(mark);
            actions.add(action);
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

        State() { recent.name = "recent"; }
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
    private final RectF hitHome = new RectF(), hitL = new RectF(), hitR = new RectF(), hitZoom = new RectF(), hitSort = new RectF();
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
        st.message = lib.platforms.isEmpty() ? t(S.NO_GAMES) : null;
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
        st.items.addAll(st.lib.platforms);
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
        List<Library.Platform> ps = st.lib.platforms;
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

    private void openEmulatorMenu() {
        Object o = selected();
        if (!(o instanceof Library.Game) && !(o instanceof Library.Platform)) return;
        Library.Platform pl = o instanceof Library.Game ? ((Library.Game) o).platform : (Library.Platform) o;
        if (pl == st.recent) return;
        String who = o instanceof Library.Game ? ((Library.Game) o).title : I18n.f(S.PLATFORM_DEFAULT, pl.name);
        Menu m = new Menu(t(S.CHOOSE_EMULATOR), who);
        String cur = listener.chosenCmd(o);
        String def = o instanceof Library.Game ? t(S.FOLLOW_PLATFORM) : I18n.f(S.PEGASUS_DEFAULT, Library.emulatorLabel(pl.launch));
        m.add(def, cur == null, () -> { listener.onChoose(o, null); closeMenu(); });
        for (String[] opt : Library.options(pl)) {
            m.add(opt[0], opt[1].equals(cur), () -> { listener.onChoose(o, opt[1]); closeMenu(); });
        }
        showMenu(m);
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
        p.setShader(new LinearGradient(0, 0, 0, h, 0xFFF3F6F9, 0xFFE3E9EF, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        if (st.lib == null || st.message != null) {
            String m = st.message != null ? st.message : t(S.LOADING);
            float y = h / 2;
            for (String line : m.split("\n")) {
                text(c, line, w / 2, y, 30 * u, 0xFF5C6B7A, false, Paint.Align.CENTER);
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
        p.setShadowLayer(10 * u(), 0, 4 * u(), 0x30000000);
        c.drawRoundRect(rr, rad, rad, p);
        p.clearShadowLayer();
    }

    private void drawGame(Canvas c, Library.Game g, float rad) {
        card(c, r, rad, 0xFFFFFFFF);
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

    private void drawFolder(Canvas c, Library.Platform pl, float rad) {
        int color = Library.platformColor(pl.type());
        float w = r.width(), h = r.height();
        r2.set(r.left, r.top, r.left + w * 0.42f, r.top + h * 0.3f);
        p.setColor(mul(color, 0.72f));
        c.drawRoundRect(r2, rad, rad, p);
        r2.set(r.left, r.top + h * 0.12f, r.right, r.bottom);
        p.setShadowLayer(10 * u(), 0, 4 * u(), 0x30000000);
        p.setShader(new LinearGradient(0, r2.top, 0, r2.bottom, lighten(color), color, Shader.TileMode.CLAMP));
        c.drawRoundRect(r2, rad, rad, p);
        p.setShader(null);
        p.clearShadowLayer();
        ConsoleDrawable d = ConsoleDrawable.of(pl, false);
        d.setBounds((int) (r.left + w * 0.4f), (int) (r.top + h * 0.18f), (int) (r.right - w * 0.05f), (int) (r.bottom - h * 0.28f));
        d.draw(c);
        text(c, pl.name, r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(pl.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawRecent(Canvas c, float rad) {
        card(c, r, rad, 0xFFFFFFFF);
        float w = r.width(), h = r.height();
        text(c, "★ " + t(S.RECENT), r.left + w * 0.07f, r.top + h * 0.2f, h * 0.12f, 0xFF1F2A36, true, Paint.Align.LEFT);
        float s = w * 0.27f, gap = w * 0.035f, x = r.left + w * 0.07f, y = r.top + h * 0.32f;
        for (int i = 0; i < Math.min(3, st.recent.games.size()); i++) {
            r2.set(x, y, x + s, y + s);
            Bitmap b = icons.tile(st.recent.games.get(i), redraw);
            p.setColor(0xFFEEF2F6);
            c.drawRoundRect(r2, s * 0.16f, s * 0.16f, p);
            if (b != null) {
                path.reset();
                path.addRoundRect(r2, s * 0.16f, s * 0.16f, Path.Direction.CW);
                c.save();
                c.clipPath(path);
                src.set(0, 0, b.getWidth(), b.getHeight());
                c.drawBitmap(b, src, r2, p);
                c.restore();
            }
            x += s + gap;
        }
    }

    private void drawSettings(Canvas c, float rad) {
        card(c, r, rad, 0xFFFFFFFF);
        float w = r.width(), h = r.height();
        ConsoleDrawable gear = new ConsoleDrawable("gear", null, false);
        float gs = h * 0.42f;
        gear.setBounds((int) (r.centerX() - gs / 2), (int) (r.top + h * 0.14f), (int) (r.centerX() + gs / 2), (int) (r.top + h * 0.14f + gs));
        gear.draw(c);
        text(c, t(S.SETTINGS), r.centerX(), r.bottom - h * 0.2f, h * 0.13f, 0xFF1F2A36, true, Paint.Align.CENTER);
        text(c, I18n.name(), r.centerX(), r.bottom - h * 0.07f, h * 0.085f, 0xFF7B8896, false, Paint.Align.CENTER);
    }

    private void pill(Canvas c, RectF rr, String s, boolean on) {
        p.setColor(on ? BLUE : 0xFFFFFFFF);
        p.setShadowLayer(6 * u(), 0, 2 * u(), 0x22000000);
        c.drawRoundRect(rr, rr.height() / 2, rr.height() / 2, p);
        p.clearShadowLayer();
        text(c, s, rr.centerX(), rr.centerY() + rr.height() * 0.17f, rr.height() * 0.45f, on ? 0xFFFFFFFF : 0xFF34404D, true, Paint.Align.CENTER);
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
            String s = st.current == st.recent ? t(S.RECENT) : st.current.name;
            r2.set(x, y, x + measure(s, hh * 0.45f) + 60 * u, y + hh);
            pill(c, r2, s, true);
            x = r2.right + 14 * u;
        }
        hitR.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitR, "R", false);
        String right = st.current == null ? I18n.f(S.PLATFORMS_COUNT, st.lib.platforms.size()) : I18n.f(S.GAMES_COUNT, st.items.size());
        text(c, right, getWidth() - pad(), y + hh * 0.68f, hh * 0.42f, 0xFF5C6B7A, true, Paint.Align.RIGHT);
    }

    private void drawFooter(Canvas c) {
        float u = u(), top = getHeight() - footerH(), cy = top + footerH() / 2;
        p.setColor(0xFFFFFFFF);
        c.drawRect(0, top, getWidth(), getHeight(), p);
        p.setColor(0xFFD9E0E7);
        c.drawRect(0, top, getWidth(), top + 2 * u, p);
        float x = pad();
        Object sel = selected();
        x = hintItem(c, x, cy, "A", sel instanceof Library.Game ? t(S.PLAY) : t(S.OPEN));
        if (st.current != null) x = hintItem(c, x, cy, "B", t(S.BACK));
        if (sel instanceof Library.Game || (sel instanceof Library.Platform && sel != st.recent)) x = hintItem(c, x, cy, "Y", t(S.EMULATOR));
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
        p.setColor(0xFF3A4654);
        c.drawRoundRect(r2, rad, rad, p);
        text(c, key, r2.centerX(), cy + 7 * u, key.length() > 1 ? 16 * u : 20 * u, 0xFFFFFFFF, true, Paint.Align.CENTER);
        float tx = r2.right + 10 * u;
        text(c, label, tx, cy + 9 * u, 25 * u, 0xFF34404D, false, Paint.Align.LEFT);
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
        card(c, cardR, 28 * u, 0xFFFFFFFF);
        text(c, m.title, cardR.left + 36 * u, cardR.top + 56 * u, 32 * u, 0xFF1F2A36, true, Paint.Align.LEFT);
        if (m.subtitle != null)
            text(c, ellipsize(m.subtitle, cardR.width() * 0.45f, 23 * u), cardR.right - 36 * u, cardR.top + 56 * u, 23 * u, 0xFF8090A0, false, Paint.Align.RIGHT);
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
            int fg = on ? 0xFFFFFFFF : 0xFF1F2A36;
            text(c, ellipsize(m.labels.get(i), r2.width() - 180 * u, 27 * u), r2.left + 28 * u, r2.centerY() + 10 * u, 27 * u, fg, on, Paint.Align.LEFT);
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
