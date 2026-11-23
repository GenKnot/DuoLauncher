package com.duo.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/**
 * A conversation with the AI, drawn on the upper screen while the lower screen shows the games it picks or finds.
 * The newest message sits at the bottom; L2 / R2 scroll back through older ones.
 */
final class ChatPanel extends View {

    static final class Msg {
        final boolean me;
        final String text;
        final List<String> games;
        StaticLayout layout, gamesLayout;
        float laidW = -1;

        Msg(boolean me, String text, List<String> games) {
            this.me = me;
            this.text = text;
            this.games = games == null ? new ArrayList<>() : games;
        }
    }

    private final List<Msg> msgs;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF(), r = new RectF();
    private String status;
    /** How far the view is scrolled up from the newest message, in pixels. */
    private float scroll, maxScroll;
    private boolean voice;

    /** @param msgs the conversation, kept by the caller so it survives closing and reopening the panel */
    ChatPanel(Context c, List<Msg> msgs, boolean voice) {
        super(c);
        this.msgs = msgs;
        this.voice = voice;
        setFocusable(false);
    }

    void add(Msg m) {
        msgs.add(m);
        scroll = 0;
        invalidate();
    }

    void setStatus(String s) {
        status = s;
        scroll = 0;
        invalidate();
    }

    void setVoice(boolean v) { voice = v; invalidate(); }

    /** -1 shows older messages, 1 newer ones. */
    void scrollBy(int dir) {
        float body = panel.height() * 0.5f;
        scroll = Math.max(0, Math.min(maxScroll, scroll - dir * body));
        invalidate();
    }

    private float u() { return getWidth() / 1920f; }

    @Override protected void onDraw(Canvas c) {
        float u = u(), ts = u * Palette.textScale, w = getWidth(), h = getHeight();
        boolean dark = Palette.dark;
        int fg = dark ? 0xFFFFFFFF : 0xFF26313D, sub = dark ? 0xAAFFFFFF : 0x99404A55;
        // below the clock and battery in the corner; opaque, so the game page behind does not show through
        panel.set(50 * u, 96 * u, w - 50 * u, h - 24 * u);
        p.setShadowLayer(30 * u, 0, 8 * u, 0x55000000);
        p.setColor(dark ? 0xFF1C2129 : 0xFFFFFFFF);
        c.drawRoundRect(panel, 36 * u, 36 * u, p);
        p.clearShadowLayer();

        // header
        float left = panel.left + 44 * u, right = panel.right - 44 * u;
        p.setTextAlign(Paint.Align.LEFT);
        p.setFakeBoldText(true);
        p.setTextSize(44 * ts);
        p.setColor(fg);
        float headY = panel.top + 70 * u;
        c.drawText(t(S.CHAT_TITLE), left, headY, p);
        p.setFakeBoldText(false);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setTextSize(26 * ts);
        p.setColor(sub);
        c.drawText(t(S.CHAT_PICKS_BELOW), right, headY - 4 * u, p);

        // footer: the keys
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(27 * ts);
        String keys = (voice ? t(S.CHAT_KEY_SPEAK) + " · " : "") + t(S.CHAT_KEY_TYPE) + " · " + t(S.CHAT_KEY_NEW) + " · "
                + t(S.CHAT_KEY_HISTORY) + " · " + t(S.CHAT_KEY_SCROLL) + " · " + t(S.CHAT_KEY_CLOSE);
        c.drawText(keys, panel.centerX(), panel.bottom - 28 * u, p);

        float bodyTop = headY + 30 * u, bodyBottom = panel.bottom - 72 * u;
        float maxW = (right - left) * 0.78f, pad = 26 * u;
        tp.setTextSize(32 * ts);

        c.save();
        c.clipRect(panel.left, bodyTop, panel.right, bodyBottom);
        if (msgs.isEmpty() && status == null) {
            // what can be asked
            tp.setColor(sub);
            String hello = t(S.CHAT_HELLO) + "\n\n" + t(S.CHAT_EXAMPLES);
            StaticLayout l = StaticLayout.Builder.obtain(hello, 0, hello.length(), tp, (int) (right - left)).setLineSpacing(8 * u, 1).build();
            c.translate(left, bodyTop + 30 * u);
            l.draw(c);
            c.restore();
            maxScroll = 0;
            return;
        }
        // lay out from the newest message upwards
        float gap = 22 * u, y = bodyBottom + scroll;
        float total = 0;
        if (status != null) {
            float bh = drawBubble(c, new Msg(false, status, null), left, right, maxW, pad, y, true, fg, sub, dark);
            y -= bh + gap;
            total += bh + gap;
        }
        for (int i = msgs.size() - 1; i >= 0; i--) {
            float bh = drawBubble(c, msgs.get(i), left, right, maxW, pad, y, false, fg, sub, dark);
            y -= bh + gap;
            total += bh + gap;
        }
        c.restore();
        maxScroll = Math.max(0, total - (bodyBottom - bodyTop));
    }

    /** Draws one message with its bottom at {@code bottom}; returns its height. */
    private float drawBubble(Canvas c, Msg m, float left, float right, float maxW, float pad, float bottom, boolean pending,
                             int fg, int sub, boolean dark) {
        float u = u(), ts = u * Palette.textScale;
        int textW = (int) (maxW - 2 * pad);
        if (m.layout == null || m.laidW != textW) {
            tp.setTextSize(32 * ts);
            m.layout = StaticLayout.Builder.obtain(m.text, 0, m.text.length(), new TextPaint(tp), textW).setLineSpacing(6 * u, 1).build();
            m.gamesLayout = null;
            if (!m.games.isEmpty()) {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < m.games.size(); i++) b.append(i == 0 ? "" : "\n").append("🎮 ").append(m.games.get(i));
                tp.setTextSize(28 * ts);
                m.gamesLayout = StaticLayout.Builder.obtain(b, 0, b.length(), new TextPaint(tp), textW).setLineSpacing(4 * u, 1).build();
            }
            m.laidW = textW;
        }
        float contentW = widest(m.layout);
        if (m.gamesLayout != null) contentW = Math.max(contentW, widest(m.gamesLayout));
        float bw = contentW + 2 * pad;
        float bh = m.layout.getHeight() + 2 * pad * 0.8f + (m.gamesLayout == null ? 0 : m.gamesLayout.getHeight() + 16 * u);
        float x = m.me ? right - bw : left;
        r.set(x, bottom - bh, x + bw, bottom);
        if (r.bottom < 0 || r.top > getHeight()) return bh;
        p.setColor(m.me ? Palette.accent() : dark ? 0x22FFFFFF : 0x10000000);
        c.drawRoundRect(r, 28 * u, 28 * u, p);
        c.save();
        c.translate(x + pad, r.top + pad * 0.8f);
        tp.setTextSize(32 * ts);
        tp.setColor(m.me ? 0xFFFFFFFF : pending ? sub : fg);
        m.layout.getPaint().setColor(tp.getColor());
        m.layout.draw(c);
        if (m.gamesLayout != null) {
            c.translate(0, m.layout.getHeight() + 16 * u);
            m.gamesLayout.getPaint().setColor(m.me ? 0xDDFFFFFF : sub);
            m.gamesLayout.draw(c);
        }
        c.restore();
        return bh;
    }

    private static float widest(Layout l) {
        float w = 0;
        for (int i = 0; i < l.getLineCount(); i++) w = Math.max(w, l.getLineWidth(i));
        return w;
    }
}
