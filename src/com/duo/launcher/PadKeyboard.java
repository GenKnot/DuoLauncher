package com.duo.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/**
 * An on-screen keyboard for the gamepad, drawn on the upper screen while the results fill the lower one.
 * Letters and digits only: Chinese titles are found by their pinyin or its initials ("mlao", "mla" → 马里奥).
 * Above the keys: recent searches and the genres, to search by genre with or without a name.
 */
final class PadKeyboard extends View {

    interface Host {
        void onQuery(String text, String genre);
        void onClose();
        void onSystemKeyboard(String text);
        void onCue(int cue);
        int resultCount();
    }

    private static final int CHAR = 0, BACKSPACE = 1, SPACE = 2, CLEAR = 3, SYSTEM = 4, DONE = 5, HISTORY = 6, GENRE = 7;

    private static final class Key {
        final int kind;
        final String label, value;
        final RectF r = new RectF();
        float weight = 1;

        Key(int kind, String label, String value) {
            this.kind = kind;
            this.label = label;
            this.value = value;
        }
    }

    private final Host host;
    private final List<List<Key>> rows = new ArrayList<>();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF(), field = new RectF();
    private String text;
    private String genre;
    private int row, col;
    private float laidW = -1, laidH = -1;

    /**
     * @param history recent searches, newest first
     * @param genres  genre ids that games in the library have
     */
    PadKeyboard(Context c, String text, String genre, List<String> history, List<String> genres, Host host) {
        super(c);
        this.host = host;
        this.text = text == null ? "" : text;
        this.genre = genre;
        setFocusable(false);

        List<Key> hist = new ArrayList<>();
        for (int i = 0; i < Math.min(6, history.size()); i++) hist.add(new Key(HISTORY, history.get(i), history.get(i)));
        if (!hist.isEmpty()) rows.add(hist);
        if (!genres.isEmpty()) {
            List<Key> gs = new ArrayList<>();
            gs.add(new Key(GENRE, t(S.ALL), null));
            for (String id : genres) gs.add(new Key(GENRE, Genres.name(id), id));
            rows.add(gs);
        }
        int first = rows.size();
        for (String line : new String[]{"1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm"}) {
            List<Key> r = new ArrayList<>();
            for (char ch : line.toCharArray()) r.add(new Key(CHAR, String.valueOf(ch).toUpperCase(), String.valueOf(ch)));
            rows.add(r);
        }
        Key bs = new Key(BACKSPACE, "⌫", null);
        bs.weight = 1.6f;
        rows.get(first + 2).add(bs);
        Key dash = new Key(CHAR, "-", "-");
        rows.get(first + 3).add(dash);
        Key dot = new Key(CHAR, ".", ".");
        rows.get(first + 3).add(dot);
        List<Key> actions = new ArrayList<>();
        Key clear = new Key(CLEAR, t(S.CLEAR_TEXT), null);
        clear.weight = 2;
        Key space = new Key(SPACE, t(S.SPACE_KEY), null);
        space.weight = 4;
        Key sys = new Key(SYSTEM, t(S.SYSTEM_KEYBOARD), null);
        sys.weight = 2.4f;
        Key done = new Key(DONE, t(S.DONE), null);
        done.weight = 2;
        actions.add(clear);
        actions.add(space);
        actions.add(sys);
        actions.add(done);
        rows.add(actions);
        row = first + 1;   // on "q"
        col = 0;
    }

    String text() { return text; }

    // ---- layout, on a 1920x1080 design ----

    private float u() { return getWidth() / 1920f; }

    private void layoutKeys() {
        float w = getWidth(), h = getHeight();
        if (w == laidW && h == laidH) return;
        laidW = w;
        laidH = h;
        float u = u(), gap = 12 * u, left = 90 * u, right = w - 90 * u;
        float keyH = 78 * u, chipH = 56 * u, fieldH = 92 * u, hintH = 46 * u;
        // chip rows flow into several lines when they are wide
        List<float[]> heights = new ArrayList<>();
        float total = fieldH + gap;
        p.setTextSize(28 * u);
        for (List<Key> r : rows) {
            boolean chips = r.get(0).kind == HISTORY || r.get(0).kind == GENRE;
            if (!chips) { total += keyH + gap; continue; }
            float x = left;
            int lines = 1;
            for (Key k : r) {
                float kw = p.measureText(k.label) + 44 * u;
                if (x + kw > right && x > left) { lines++; x = left; }
                x += kw + gap;
            }
            total += lines * (chipH + gap);
        }
        total += hintH;
        float bottom = h - 24 * u, top = Math.max(24 * u, bottom - total - 40 * u);
        panel.set(50 * u, top, w - 50 * u, bottom);
        float y = top + 26 * u;
        field.set(left, y, right, y + fieldH);
        y += fieldH + gap;
        for (List<Key> r : rows) {
            boolean chips = r.get(0).kind == HISTORY || r.get(0).kind == GENRE;
            if (chips) {
                float x = left;
                for (Key k : r) {
                    float kw = p.measureText(k.label) + 44 * u;
                    if (x + kw > right && x > left) { x = left; y += chipH + gap; }
                    k.r.set(x, y, x + kw, y + chipH);
                    x += kw + gap;
                }
                y += chipH + gap;
            } else {
                float sum = 0;
                for (Key k : r) sum += k.weight;
                // letter rows keep the same key width; the last row stretches across
                float unit = r.get(0).kind == CHAR || r.get(0).kind == BACKSPACE ? (right - left - 10 * gap) / 11f : (right - left - (r.size() - 1) * gap) / sum;
                float rowW = 0;
                for (Key k : r) rowW += unit * k.weight;
                rowW += (r.size() - 1) * gap;
                float x = left + (right - left - rowW) / 2;
                for (Key k : r) {
                    k.r.set(x, y, x + unit * k.weight, y + keyH);
                    x += unit * k.weight + gap;
                }
                y += keyH + gap;
            }
        }
    }

    // ---- drawing ----

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) { laidW = -1; }

    @Override protected void onDraw(Canvas c) {
        layoutKeys();
        float u = u();
        boolean dark = Palette.dark;
        int fg = dark ? 0xFFFFFFFF : 0xFF26313D, sub = dark ? 0xAAFFFFFF : 0x99404A55;
        p.setShadowLayer(30 * u, 0, 8 * u, 0x55000000);
        p.setColor(dark ? 0xF01C2129 : 0xF5FFFFFF);
        c.drawRoundRect(panel, 36 * u, 36 * u, p);
        p.clearShadowLayer();

        // the search field
        p.setColor(dark ? 0x22FFFFFF : 0x0F000000);
        c.drawRoundRect(field, field.height() / 2, field.height() / 2, p);
        float ty = field.centerY() + 15 * u, x = field.left + 36 * u;
        p.setTextAlign(Paint.Align.LEFT);
        p.setFakeBoldText(true);
        p.setTextSize(42 * u);
        if (genre != null) {
            String g = Genres.name(genre);
            float gw = p.measureText(g) + 36 * u;
            RectF gr = new RectF(x - 10 * u, field.top + 14 * u, x - 10 * u + gw, field.bottom - 14 * u);
            p.setColor(Genres.color(genre));
            c.drawRoundRect(gr, gr.height() / 2, gr.height() / 2, p);
            p.setColor(0xFFFFFFFF);
            p.setTextSize(30 * u);
            c.drawText(g, gr.left + 18 * u, gr.centerY() + 11 * u, p);
            p.setTextSize(42 * u);
            x = gr.right + 18 * u;
        }
        String shown = status != null ? "🎤 " + status : text.isEmpty() && genre == null ? t(S.PAD_SEARCH_HINT) : text;
        p.setColor(text.isEmpty() ? sub : fg);
        p.setFakeBoldText(!text.isEmpty());
        c.drawText(shown, x, ty, p);
        if (!text.isEmpty() || genre != null) {
            float cx = x + (text.isEmpty() ? 0 : p.measureText(text) + 6 * u);
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                p.setColor(Palette.accent());
                c.drawRect(cx, field.top + 22 * u, cx + 4 * u, field.bottom - 22 * u, p);
            }
            postInvalidateDelayed(500);
        }
        p.setFakeBoldText(false);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setTextSize(30 * u);
        p.setColor(sub);
        if (!text.isEmpty() || genre != null) c.drawText(I18n.f(S.GAMES_COUNT, host.resultCount()), field.right - 36 * u, ty - 4 * u, p);

        for (int i = 0; i < rows.size(); i++) {
            List<Key> r = rows.get(i);
            for (int j = 0; j < r.size(); j++) drawKey(c, r.get(j), i == row && j == col, dark, fg);
        }

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(26 * u);
        p.setColor(sub);
        c.drawText(t(S.PAD_KEYS_HINT) + " · L " + t(S.VOICE), panel.centerX(), panel.bottom - 22 * u, p);
    }

    private void drawKey(Canvas c, Key k, boolean focus, boolean dark, int fg) {
        float u = u();
        RectF r = new RectF(k.r);
        if (focus) r.inset(-5 * u, -5 * u);
        boolean chip = k.kind == HISTORY || k.kind == GENRE;
        boolean on = k.kind == GENRE && (k.value == null ? genre == null : k.value.equals(genre));
        int bg;
        if (focus) bg = Palette.accent();
        else if (on) bg = k.value == null ? (dark ? 0x55FFFFFF : 0x33000000) : Genres.color(k.value);
        else if (k.kind == DONE) bg = dark ? 0x40FFFFFF : 0x26000000;
        else bg = dark ? 0x1FFFFFFF : 0x0D000000;
        if (focus) p.setShadowLayer(14 * u, 0, 5 * u, 0x55000000);
        p.setColor(bg);
        float rad = chip ? r.height() / 2 : 18 * u;
        c.drawRoundRect(r, rad, rad, p);
        p.clearShadowLayer();
        p.setTextAlign(Paint.Align.CENTER);
        p.setFakeBoldText(k.kind == CHAR || focus || on);
        p.setTextSize((chip ? 28 : k.kind == CHAR || k.kind == BACKSPACE ? 38 : 30) * u);
        p.setColor(focus || (on && k.value != null) ? 0xFFFFFFFF : fg);
        c.drawText(k.label, r.centerX(), r.centerY() + p.getTextSize() * 0.36f, p);
        p.setFakeBoldText(false);
    }

    // ---- input ----

    private Key focused() {
        List<Key> r = rows.get(row);
        return r.get(Math.min(col, r.size() - 1));
    }

    private void moveVertical(int d) {
        int n = row + d;
        if (n < 0 || n >= rows.size()) return;
        float cx = focused().r.centerX(), cy = focused().r.centerY();
        // within a chip row that wrapped onto several lines, step to the next line first
        List<Key> cur = rows.get(row);
        float best = Float.MAX_VALUE;
        int bi = -1;
        for (int j = 0; j < cur.size(); j++) {
            float dy = (cur.get(j).r.centerY() - cy) * d;
            if (dy <= 1) continue;
            float score = dy * 4 + Math.abs(cur.get(j).r.centerX() - cx);
            if (score < best) { best = score; bi = j; }
        }
        if (bi >= 0) { col = bi; return; }
        List<Key> next = rows.get(n);
        // entering a wrapped chip row from below: its last line
        float lineY = d < 0 ? next.get(next.size() - 1).r.centerY() : next.get(0).r.centerY();
        best = Float.MAX_VALUE;
        for (int j = 0; j < next.size(); j++) {
            if (Math.abs(next.get(j).r.centerY() - lineY) > 1) continue;
            float dx = Math.abs(next.get(j).r.centerX() - cx);
            if (dx < best) { best = dx; bi = j; }
        }
        row = n;
        col = Math.max(0, bi);
    }

    /** "Listening…" and the like while L is held for speech, or null. */
    private String status;

    void setStatus(String s) {
        status = s;
        invalidate();
    }

    /** Adds spoken words at the end. */
    void insert(String s) {
        status = null;
        text = text + s;
        host.onQuery(text, genre);
        invalidate();
    }

    boolean onKey(int code) {
        layoutKeys();
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_UP: moveVertical(-1); host.onCue(Sfx.MOVE); break;
            case KeyEvent.KEYCODE_DPAD_DOWN: moveVertical(1); host.onCue(Sfx.MOVE); break;
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                int size = rows.get(row).size();
                col = (Math.min(col, size - 1) - 1 + size) % size;
                host.onCue(Sfx.MOVE);
                break;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                int size = rows.get(row).size();
                col = (Math.min(col, size - 1) + 1) % size;
                host.onCue(Sfx.MOVE);
                break;
            }
            case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_DPAD_CENTER:
                press(focused());
                break;
            case KeyEvent.KEYCODE_BUTTON_B: case KeyEvent.KEYCODE_BACK: case KeyEvent.KEYCODE_ESCAPE: case KeyEvent.KEYCODE_DEL:
                if (text.isEmpty()) { host.onCue(Sfx.BACK); host.onClose(); return true; }
                press(new Key(BACKSPACE, "", null));
                break;
            case KeyEvent.KEYCODE_BUTTON_X: press(new Key(SPACE, "", null)); break;
            case KeyEvent.KEYCODE_BUTTON_Y: host.onSystemKeyboard(text); return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT: press(new Key(CLEAR, "", null)); break;
            case KeyEvent.KEYCODE_BUTTON_START: case KeyEvent.KEYCODE_BUTTON_THUMBR: host.onCue(Sfx.OPEN); host.onClose(); return true;
            default:
                // a hardware or Bluetooth keyboard types straight in
                if (code >= KeyEvent.KEYCODE_A && code <= KeyEvent.KEYCODE_Z) press(new Key(CHAR, "", String.valueOf((char) ('a' + code - KeyEvent.KEYCODE_A))));
                else if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) press(new Key(CHAR, "", String.valueOf((char) ('0' + code - KeyEvent.KEYCODE_0))));
                else return true;
        }
        invalidate();
        return true;
    }

    private void press(Key k) {
        String before = text, beforeGenre = genre;
        switch (k.kind) {
            case CHAR: if (text.length() < 40) text += k.value; break;
            case SPACE: if (!text.isEmpty() && !text.endsWith(" ")) text += " "; break;
            case BACKSPACE: if (!text.isEmpty()) text = text.substring(0, text.length() - 1); break;
            case CLEAR: text = ""; genre = null; break;
            case HISTORY: text = k.value; break;
            case GENRE: genre = k.value == null || k.value.equals(genre) ? null : k.value; break;
            case SYSTEM: host.onSystemKeyboard(text); return;
            case DONE: host.onCue(Sfx.OPEN); host.onClose(); return;
            default: break;
        }
        host.onCue(k.kind == BACKSPACE || k.kind == CLEAR ? Sfx.BACK : Sfx.MOVE);
        if (!text.equals(before) || !java.util.Objects.equals(genre, beforeGenre)) host.onQuery(text, genre);
        invalidate();
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        layoutKeys();
        for (int i = 0; i < rows.size(); i++)
            for (int j = 0; j < rows.get(i).size(); j++) {
                Key k = rows.get(i).get(j);
                if (!k.r.contains(e.getX(), e.getY())) continue;
                row = i;
                col = j;
                press(k);
                invalidate();
                return true;
            }
        if (!panel.contains(e.getX(), e.getY())) host.onClose();
        return true;
    }
}
