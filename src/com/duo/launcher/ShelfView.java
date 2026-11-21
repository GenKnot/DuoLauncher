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

/** Lower screen: home folders and the zoomable icon grid, driven by touch or gamepad. */
final class ShelfView extends View {

    interface Listener {
        void onFocus(Object item);
        void onActivate(Library.Game g);
        void onChoose(Object item, String cmd);
        String chosenCmd(Object item);
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
        Object menuFor;
        List<String[]> menu;
        int menuSel;
        String message = "正在读取游戏…";

        State() { recent.name = "最近玩过"; }
    }

    private static final int[] ZOOM = {4, 5, 6, 8};
    private static final String[] ZOOM_NAME = {"大", "中", "小", "迷你"};
    private static final int BLUE = 0xFF2F8BFF;

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
        setFocusable(false);
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
        st.message = lib.platforms.isEmpty() ? "没有找到 Roms/*/metadata.pegasus.txt" : null;
        setRecent(recent);
        if (st.current == null) goHome(); else fireFocus();
    }

    void setRecent(List<Library.Game> recent) {
        st.recent.games.clear();
        st.recent.games.addAll(recent);
        if (st.current == null && st.lib != null) {
            Object keep = selected();
            rebuildHome();
            int i = st.items.indexOf(keep);
            st.sel = Math.max(0, i);
            invalidate();
        }
    }

    private void rebuildHome() {
        st.items.clear();
        if (!st.recent.games.isEmpty()) st.items.add(st.recent);
        st.items.addAll(st.lib.platforms);
    }

    void goHome() {
        if (st.lib == null) return;
        if (st.current != null) st.lastSel.put(st.current.name, st.sel);
        st.current = null;
        rebuildHome();
        st.sel = clampSel(st.lastSel.containsKey("#home") ? st.lastSel.get("#home") : 0);
        jumpTo();
        fireFocus();
    }

    void open(Library.Platform pl) {
        if (st.current == null) st.lastSel.put("#home", st.sel); else st.lastSel.put(st.current.name, st.sel);
        st.current = pl;
        st.items.clear();
        List<Library.Game> gs = new ArrayList<>(pl.games);
        if (st.byName && pl != st.recent) {
            Collator col = Collator.getInstance(Locale.CHINA);
            gs.sort((a, b) -> col.compare(a.title, b.title));
        }
        st.items.addAll(gs);
        st.sel = clampSel(st.lastSel.containsKey(pl.name) ? st.lastSel.get(pl.name) : 0);
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
        invalidate();
    }

    private void move(int dx, int dy) {
        if (st.items.isEmpty()) return;
        int c = cols(), n = st.items.size(), i = st.sel;
        if (dx != 0) i += dx;
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
        if (o instanceof Library.Platform) open((Library.Platform) o);
        else if (o instanceof Library.Game) listener.onActivate((Library.Game) o);
    }

    private void back() {
        if (st.current != null) goHome();
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

    private void openMenu() {
        Object o = selected();
        Library.Platform pl = o instanceof Library.Game ? ((Library.Game) o).platform : (Library.Platform) o;
        if (pl == null || pl == st.recent) return;
        List<String[]> m = new ArrayList<>();
        if (o instanceof Library.Game) m.add(new String[]{"跟随平台设置", null});
        else m.add(new String[]{"天马G 默认（" + Library.emulatorLabel(pl.launch) + "）", null});
        m.addAll(Library.options(pl));
        String cur = listener.chosenCmd(o);
        st.menuSel = 0;
        for (int i = 1; i < m.size(); i++) if (m.get(i)[1].equals(cur)) st.menuSel = i;
        st.menu = m;
        st.menuFor = o;
        invalidate();
    }

    private void chooseMenu() {
        listener.onChoose(st.menuFor, st.menu.get(st.menuSel)[1]);
        st.menu = null;
        fireFocus();
        invalidate();
    }

    private void fireFocus() {
        listener.onFocus(selected());
        invalidate();
    }

    boolean onKey(int code) {
        if (st.lib == null) return false;
        if (st.menu != null) {
            switch (code) {
                case KeyEvent.KEYCODE_DPAD_UP: st.menuSel = Math.max(0, st.menuSel - 1); invalidate(); return true;
                case KeyEvent.KEYCODE_DPAD_DOWN: st.menuSel = Math.min(st.menu.size() - 1, st.menuSel + 1); invalidate(); return true;
                case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_BUTTON_START:
                    chooseMenu(); return true;
                case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE: case KeyEvent.KEYCODE_BUTTON_Y:
                    st.menu = null; invalidate(); return true;
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
                back(); return true;
            case KeyEvent.KEYCODE_BUTTON_X: zoom(); return true;
            case KeyEvent.KEYCODE_BUTTON_Y: openMenu(); return true;
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

        if (st.message != null || st.lib == null) {
            text(c, st.message == null ? "正在读取游戏…" : st.message, w / 2, h / 2, 34 * u, 0xFF5C6B7A, false, Paint.Align.CENTER);
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
            if (i == st.sel) continue;
            drawItem(c, i, false);
        }
        if (st.sel < st.items.size()) drawItem(c, st.sel, true);
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
            int col = 0xFF000000 | (g.title.hashCode() & 0x7F7F7F) | 0x404040;
            p.setColor(col);
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
        p.setColor(ConsoleShade.darken(color));
        c.drawRoundRect(r2, rad, rad, p);
        r2.set(r.left, r.top + h * 0.12f, r.right, r.bottom);
        p.setShadowLayer(10 * u(), 0, 4 * u(), 0x30000000);
        p.setShader(new LinearGradient(0, r2.top, 0, r2.bottom, ConsoleShade.lighten(color), color, Shader.TileMode.CLAMP));
        c.drawRoundRect(r2, rad, rad, p);
        p.setShader(null);
        p.clearShadowLayer();
        ConsoleDrawable d = new ConsoleDrawable(pl.type(), false);
        d.setBounds((int) (r.left + w * 0.42f), (int) (r.top + h * 0.2f), (int) (r.right - w * 0.06f), (int) (r.bottom - h * 0.3f));
        d.draw(c);
        text(c, pl.name, r.left + w * 0.07f, r.bottom - h * 0.12f, h * 0.16f, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, String.valueOf(pl.games.size()), r.right - w * 0.07f, r.bottom - h * 0.12f, h * 0.12f, 0xDDFFFFFF, true, Paint.Align.RIGHT);
    }

    private void drawRecent(Canvas c, float rad) {
        card(c, r, rad, 0xFFFFFFFF);
        float w = r.width(), h = r.height();
        text(c, "★ 最近玩过", r.left + w * 0.07f, r.top + h * 0.2f, h * 0.12f, 0xFF1F2A36, true, Paint.Align.LEFT);
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
        hitHome.set(x, y, x + 150 * u, y + hh);
        pill(c, hitHome, "⌂ 主页", st.current == null);
        x = hitHome.right + 14 * u;
        if (st.current != null) {
            String s = st.current.name;
            float tw = measure(s, hh * 0.45f) + 60 * u;
            r2.set(x, y, x + tw, y + hh);
            pill(c, r2, s, true);
            x = r2.right + 14 * u;
        }
        hitR.set(x, y + 6 * u, x + 54 * u, y + hh - 6 * u);
        pill(c, hitR, "R", false);
        String right = st.current == null ? st.lib.platforms.size() + " 个平台" : st.items.size() + " 款";
        text(c, right, getWidth() - pad(), y + hh * 0.68f, hh * 0.42f, 0xFF5C6B7A, true, Paint.Align.RIGHT);
    }

    private void drawFooter(Canvas c) {
        float u = u(), top = getHeight() - footerH(), cy = top + footerH() / 2;
        p.setColor(0xFFFFFFFF);
        c.drawRect(0, top, getWidth(), getHeight(), p);
        p.setColor(0xFFD9E0E7);
        c.drawRect(0, top, getWidth(), top + 2 * u, p);
        float x = pad();
        boolean game = selected() instanceof Library.Game;
        x = hintItem(c, x, cy, "A", game ? "开始游戏" : "打开");
        if (st.current != null) x = hintItem(c, x, cy, "B", "返回");
        if (selected() != st.recent) x = hintItem(c, x, cy, "Y", "模拟器");
        if (st.current != null) {
            float x0 = x;
            x = hintItem(c, x, cy, "X", "大小 · " + ZOOM_NAME[st.zoom]);
            hitZoom.set(x0, top, x, getHeight());
            if (st.current != st.recent) {
                x0 = x;
                x = hintItem(c, x, cy, "SEL", st.byName ? "排序 · 名称" : "排序 · 默认");
                hitSort.set(x0, top, x, getHeight());
            }
        } else {
            hitZoom.setEmpty();
            hitSort.setEmpty();
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

    private float menuRowH() { return 78 * u(); }

    private void menuCard(RectF out) {
        float u = u(), mw = 820 * u, mh = 110 * u + st.menu.size() * menuRowH() + 24 * u;
        out.set((getWidth() - mw) / 2, (getHeight() - mh) / 2, (getWidth() + mw) / 2, (getHeight() + mh) / 2);
    }

    private void drawMenu(Canvas c) {
        float u = u();
        p.setColor(0x88000000);
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        RectF card = new RectF();
        menuCard(card);
        card(c, card, 28 * u, 0xFFFFFFFF);
        String who = st.menuFor instanceof Library.Game ? ((Library.Game) st.menuFor).title : ((Library.Platform) st.menuFor).name + " 平台默认";
        text(c, "选择模拟器", card.left + 36 * u, card.top + 56 * u, 34 * u, 0xFF1F2A36, true, Paint.Align.LEFT);
        text(c, ellipsize(who, card.width() * 0.5f, 24 * u), card.right - 36 * u, card.top + 56 * u, 24 * u, 0xFF8090A0, false, Paint.Align.RIGHT);
        String cur = st.menuFor == null ? null : currentCmdFor(st.menuFor);
        for (int i = 0; i < st.menu.size(); i++) {
            float y = card.top + 96 * u + i * menuRowH();
            r2.set(card.left + 20 * u, y, card.right - 20 * u, y + menuRowH() - 8 * u);
            if (i == st.menuSel) {
                p.setColor(BLUE);
                c.drawRoundRect(r2, 16 * u, 16 * u, p);
            }
            String cmd = st.menu.get(i)[1];
            boolean on = cmd == null ? cur == null : cmd.equals(cur);
            int fg = i == st.menuSel ? 0xFFFFFFFF : 0xFF1F2A36;
            text(c, st.menu.get(i)[0], r2.left + 28 * u, r2.centerY() + 10 * u, 28 * u, fg, i == st.menuSel, Paint.Align.LEFT);
            if (on) text(c, "✓ 当前", r2.right - 28 * u, r2.centerY() + 9 * u, 24 * u, fg, true, Paint.Align.RIGHT);
        }
    }

    private String currentCmdFor(Object o) { return listener.chosenCmd(o); }

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

    // ---- touch ----

    @Override public boolean onTouchEvent(MotionEvent e) { return gd.onTouchEvent(e) || super.onTouchEvent(e); }

    private void tap(float x, float y) {
        if (st.lib == null) return;
        if (st.menu != null) {
            RectF card = new RectF();
            menuCard(card);
            if (!card.contains(x, y)) { st.menu = null; invalidate(); return; }
            int i = (int) ((y - card.top - 96 * u()) / menuRowH());
            if (i >= 0 && i < st.menu.size()) { st.menuSel = i; chooseMenu(); }
            return;
        }
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

    /** Small colour helpers shared with folder drawing. */
    static final class ConsoleShade {
        static int darken(int c) { return mul(c, 0.72f); }
        static int lighten(int c) {
            int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
            return 0xFF000000 | (r + (255 - r) / 4) << 16 | (g + (255 - g) / 4) << 8 | (b + (255 - b) / 4);
        }
        static int mul(int c, float f) {
            return 0xFF000000 | (int) (((c >> 16) & 255) * f) << 16 | (int) (((c >> 8) & 255) * f) << 8 | (int) ((c & 255) * f);
        }
    }
}
