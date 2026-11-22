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
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/** Upper screen for the statistics tile: total play time, the last seven days, platforms and top games. */
final class StatsView extends View {

    static final class Stats {
        long total;
        final long[] week = new long[7];          // oldest first, today last
        final String[] weekLabels = new String[7];
        final List<String> platformNames = new ArrayList<>();
        final List<Long> platformTimes = new ArrayList<>();
        final List<Integer> platformColors = new ArrayList<>();
        final List<Library.Game> top = new ArrayList<>();
        final List<Long> topTimes = new ArrayList<>();
    }

    private final Icons icons;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF r = new RectF();
    private final Rect src = new Rect();
    private final Path path = new Path();
    private Stats s;

    StatsView(Context c, Icons icons) {
        super(c);
        this.icons = icons;
        setVisibility(GONE);
    }

    void set(Stats stats) {
        s = stats;
        invalidate();
    }

    static String duration(long ms) {
        long min = ms / 60_000;
        return min >= 60 ? I18n.f(S.PLAYED_HM, min / 60, min % 60) : I18n.f(S.PLAYED_M, min);
    }

    private void text(Canvas c, String str, float x, float y, float size, int color, boolean bold, Paint.Align a) {
        p.setTextSize(size * Palette.textScale);
        p.setColor(color);
        p.setTypeface(Theme.font(bold));
        p.setTextAlign(a);
        c.drawText(str, x, y, p);
    }

    private String fit(String str, float maxW, float size) {
        p.setTextSize(size * Palette.textScale);
        if (p.measureText(str) <= maxW) return str;
        while (str.length() > 1 && p.measureText(str + "…") > maxW) str = str.substring(0, str.length() - 1);
        return str + "…";
    }

    private void card(Canvas c, RectF r, int color, float u) {
        p.setColor(color);
        p.setShadowLayer(24 * u, 0, 6 * u, Palette.dark ? 0x66000000 : 0x22304060);
        c.drawRoundRect(r, 32 * u, 32 * u, p);
        p.clearShadowLayer();
    }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), u = w / 1920f;
        int paper = Palette.backdropPaper(), ink = Palette.backdropInk();
        c.drawColor(paper);
        // the same kind of line-art backdrop as the other pages, in the theme's paper and ink colours
        Bitmap bg = Theme.toolBackground("stats");
        if (bg == null) bg = Theme.toolBackground("recent");
        if (bg != null) {
            float k = Math.max(w / bg.getWidth(), h / bg.getHeight());
            float dw = bg.getWidth() * k, dh = bg.getHeight() * k;
            r.set((w - dw) / 2, (h - dh) / 2, (w + dw) / 2, (h + dh) / 2);
            src.set(0, 0, bg.getWidth(), bg.getHeight());
            boolean plain = !Theme.isTintable(bg) || paper == 0xFFFFFFFF && ink == 0xFF000000;
            p.setColorFilter(plain ? null : BackdropView.duotone(paper, ink));
            p.setAlpha(Palette.dark ? 150 : 120);   // a soft texture behind the cards, not a picture to look at
            c.drawBitmap(bg, src, r, p);
            p.setAlpha(255);
            p.setColorFilter(null);
        }
        if (s == null) return;
        int title = Palette.topTitle(), sub = Palette.topText(), accent = Palette.accent();
        int card = Palette.dark ? 0xEE1E2530 : 0xF5FFFFFF;

        // header: title and the total, with the accent as a side stripe
        r.set(100 * u, 90 * u, 940 * u, 320 * u);
        card(c, r, card, u);
        p.setColor(accent);
        c.drawRoundRect(new RectF(r.left + 36 * u, r.top + 44 * u, r.left + 46 * u, r.bottom - 44 * u), 5 * u, 5 * u, p);
        text(c, t(S.STATS), r.left + 76 * u, r.top + 90 * u, 52 * u, title, true, Paint.Align.LEFT);
        text(c, t(S.TOTAL_PLAYED), r.left + 78 * u, r.top + 140 * u, 28 * u, sub, false, Paint.Align.LEFT);
        text(c, s.total > 0 ? duration(s.total).replaceFirst("^\\D+", "") : t(S.NO_PLAY_YET), r.left + 76 * u, r.top + 200 * u, 50 * u, accent, true, Paint.Align.LEFT);

        // this week: seven bars
        r.set(100 * u, 350 * u, 940 * u, 750 * u);
        card(c, r, card, u);
        text(c, t(S.THIS_WEEK), r.left + 36 * u, r.top + 62 * u, 32 * u, title, true, Paint.Align.LEFT);
        long max = 1;
        for (long v : s.week) max = Math.max(max, v);
        float bw = 64 * u, gap = (r.width() - 72 * u - 7 * bw) / 6, base = r.bottom - 70 * u, top = r.top + 110 * u;
        for (int i = 0; i < 7; i++) {
            float x = r.left + 36 * u + i * (bw + gap), bh = (base - top) * s.week[i] / max;
            RectF bar = new RectF(x, base - Math.max(bh, 8 * u), x + bw, base);
            p.setColor(i == 6 ? accent : (accent & 0x00FFFFFF) | 0x88000000);
            c.drawRoundRect(bar, 14 * u, 14 * u, p);
            text(c, s.weekLabels[i], x + bw / 2, base + 46 * u, 26 * u, sub, i == 6, Paint.Align.CENTER);
        }

        // platforms: one stacked bar with a legend
        r.set(100 * u, 780 * u, 940 * u, 990 * u);
        card(c, r, card, u);
        long sum = 0;
        for (long v : s.platformTimes) sum += v;
        float bx = r.left + 36 * u, bxEnd = r.right - 36 * u, by = r.top + 40 * u;
        if (sum > 0) {
            float x = bx;
            for (int i = 0; i < s.platformTimes.size(); i++) {
                float seg = (bxEnd - bx) * s.platformTimes.get(i) / sum;
                p.setColor(s.platformColors.get(i));
                c.drawRect(x, by, x + seg, by + 26 * u, p);
                x += seg;
            }
            float lx = bx, ly = by + 90 * u;
            for (int i = 0; i < Math.min(4, s.platformNames.size()); i++) {
                p.setColor(s.platformColors.get(i));
                c.drawCircle(lx + 10 * u, ly - 9 * u, 10 * u, p);
                String label = fit(s.platformNames.get(i), 160 * u, 24 * u) + " " + Math.round(100.0 * s.platformTimes.get(i) / sum) + "%";
                text(c, label, lx + 28 * u, ly, 24 * u, sub, false, Paint.Align.LEFT);
                lx += 28 * u + p.measureText(label) + 44 * u;
                if (lx > bxEnd - 120 * u) break;
            }
        }

        // most played
        r.set(1000 * u, 90 * u, 1820 * u, 990 * u);
        card(c, r, card, u);
        text(c, t(S.TOP_GAMES), r.left + 40 * u, r.top + 70 * u, 34 * u, title, true, Paint.Align.LEFT);
        float rowH = 148 * u, y = r.top + 110 * u;
        if (s.top.isEmpty()) text(c, t(S.NO_PLAY_YET), r.centerX(), r.centerY(), 30 * u, sub, false, Paint.Align.CENTER);
        for (int i = 0; i < s.top.size(); i++) {
            Library.Game g = s.top.get(i);
            RectF ic = new RectF(r.left + 40 * u, y, r.left + 40 * u + rowH - 24 * u, y + rowH - 24 * u);
            Bitmap b = icons.tile(g, this::invalidate);
            c.save();
            path.reset();
            path.addRoundRect(ic, 18 * u, 18 * u, Path.Direction.CW);
            c.clipPath(path);
            if (b != null) {
                src.set(0, 0, b.getWidth(), b.getHeight());
                c.drawBitmap(b, src, ic, p);
            } else {
                p.setColor(Palette.cardSoft());
                c.drawRect(ic, p);
            }
            c.restore();
            float tx = ic.right + 28 * u, maxW = r.right - tx - 40 * u;
            text(c, (i + 1) + "  " + fit(g.title, maxW - 40 * u, 32 * u), tx, y + 48 * u, 32 * u, title, true, Paint.Align.LEFT);
            text(c, g.platform.title() + "  ·  " + duration(s.topTimes.get(i)), tx, y + 94 * u, 25 * u, sub, false, Paint.Align.LEFT);
            y += rowH;
        }
    }
}
