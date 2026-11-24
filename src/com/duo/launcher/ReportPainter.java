package com.duo.launcher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.function.Function;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/**
 * Draws the pages of a play report on a 1920 × 1080 canvas (scaled by {@code u}): the upper screen shows one page
 * at a time, and "save as picture" stacks them all.
 */
final class ReportPainter {
    static final int PAGES = 5;

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF r = new RectF(), q = new RectF();
    private final Rect src = new Rect();
    private final Path path = new Path();
    private final Function<Library.Game, Bitmap> cover;
    private float u;
    private int title, sub, accent, card, soft;

    ReportPainter(Function<Library.Game, Bitmap> cover) { this.cover = cover; }

    static String pageName(int page) {
        switch (page) {
            case 0: return t(S.REPORT_P_OVERVIEW);
            case 1: return t(S.REPORT_P_DAYS);
            case 2: return t(S.REPORT_P_GAMES);
            case 3: return t(S.REPORT_P_CONSOLES);
            default: return t(S.REPORT_P_HABITS);
        }
    }

    void draw(Canvas c, float w, PlayReport rep, int page) {
        u = w / 1920f;
        title = Palette.topTitle();
        sub = Palette.topText();
        accent = Palette.accent();
        card = Palette.dark ? 0xEE1E2530 : 0xF5FFFFFF;
        soft = Palette.dark ? 0xFF2A3240 : 0xFFEDF1F6;
        boolean empty = rep.total == 0 && rep.sessions == 0;
        if (empty && page > 0) { emptyPage(c, rep); return; }
        switch (page) {
            case 0: overview(c, rep, empty); break;
            case 1: if (rep.isYear()) yearDays(c, rep); else monthDays(c, rep); break;
            case 2: games(c, rep); break;
            case 3: consoles(c, rep); break;
            default: habits(c, rep); break;
        }
    }

    // ---- pages ----

    private void overview(Canvas c, PlayReport rep, boolean empty) {
        // the big coloured card: period, total, days and sessions, the change against the period before
        r.set(100 * u, 90 * u, (rep.games.isEmpty() ? 1820 : 1180) * u, 990 * u);
        int deep = mix(accent, 0xFF000000, 0.35f);
        p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom, accent, deep, Shader.TileMode.CLAMP));
        p.setShadowLayer(28 * u, 0, 8 * u, Palette.dark ? 0x66000000 : 0x33304060);
        c.drawRoundRect(r, 40 * u, 40 * u, p);
        p.clearShadowLayer();
        p.setShader(null);
        // soft circles for depth
        p.setColor(0x14FFFFFF);
        c.drawCircle(r.right - 120 * u, r.top + 140 * u, 260 * u, p);
        c.drawCircle(r.left + 160 * u, r.bottom - 60 * u, 180 * u, p);
        float x = r.left + 70 * u, maxW = r.width() - 140 * u;
        text(c, t(rep.isYear() ? S.REPORT_YEAR : S.REPORT_MONTH), x, r.top + 110 * u, 34 * u, 0xD9FFFFFF, false, Paint.Align.LEFT);
        text(c, fit(rep.title, maxW, 92 * u), x, r.top + 220 * u, 92 * u, 0xFFFFFFFF, true, Paint.Align.LEFT);
        if (empty) {
            text(c, fit(t(S.REPORT_EMPTY), maxW, 40 * u), x, r.top + 420 * u, 40 * u, 0xFFFFFFFF, false, Paint.Align.LEFT);
            return;
        }
        text(c, t(S.TOTAL_PLAYED), x, r.top + 380 * u, 32 * u, 0xD9FFFFFF, false, Paint.Align.LEFT);
        text(c, fit(StatsView.span(rep.total), maxW, 128 * u), x, r.top + 520 * u, 128 * u, 0xFFFFFFFF, true, Paint.Align.LEFT);
        text(c, fit(I18n.f(S.REPORT_DAYS_SESSIONS, rep.daysPlayed, rep.sessions), maxW, 38 * u), x, r.top + 620 * u, 38 * u, 0xF2FFFFFF, false, Paint.Align.LEFT);
        if (rep.prevTotal > 0) {
            long pct = Math.round(100.0 * (rep.total - rep.prevTotal) / rep.prevTotal);
            String cmp = pct == 0 ? I18n.f(S.REPORT_SAME, rep.prevTitle)
                    : I18n.f(pct > 0 ? S.REPORT_MORE : S.REPORT_LESS, rep.prevTitle, Math.abs(pct));
            // a pill under the numbers
            p.setTextSize(32 * u * Palette.textScale);
            p.setTypeface(Theme.font(true));
            float tw = p.measureText(cmp);
            q.set(x, r.top + 680 * u, x + Math.min(tw + 64 * u, maxW), r.top + 756 * u);
            p.setColor(0x33FFFFFF);
            c.drawRoundRect(q, 38 * u, 38 * u, p);
            text(c, fit(cmp, maxW - 64 * u, 32 * u), q.left + 32 * u, q.top + 50 * u, 32 * u, 0xFFFFFFFF, true, Paint.Align.LEFT);
        }
        if (rep.newGames > 0)
            text(c, fit(I18n.f(S.REPORT_NEW_GAMES, rep.newGames), maxW, 32 * u), x, r.bottom - 70 * u, 32 * u, 0xD9FFFFFF, false, Paint.Align.LEFT);
        if (rep.games.isEmpty()) return;

        // the favourite: the most played game, as a big cover
        PlayReport.Row fav = rep.games.get(0);
        r.set(1220 * u, 90 * u, 1820 * u, 990 * u);
        card(c, r);
        text(c, t(rep.isYear() ? S.REPORT_FAV_YEAR : S.REPORT_FAV_MONTH), r.centerX(), r.top + 84 * u, 32 * u, sub, false, Paint.Align.CENTER);
        q.set(r.left + 90 * u, r.top + 130 * u, r.right - 90 * u, r.top + 130 * u + (r.width() - 180 * u));
        picture(c, q, fav.game, fav.name, 36 * u);
        text(c, fit(fav.name, r.width() - 80 * u, 40 * u), r.centerX(), q.bottom + 80 * u, 40 * u, title, true, Paint.Align.CENTER);
        text(c, StatsView.span(fav.ms), r.centerX(), q.bottom + 136 * u, 34 * u, accent, true, Paint.Align.CENTER);
    }

    private void monthDays(Canvas c, PlayReport rep) {
        // a calendar: one square a day, darker the longer you played
        r.set(100 * u, 90 * u, 1180 * u, 990 * u);
        card(c, r);
        text(c, rep.title, r.left + 50 * u, r.top + 80 * u, 40 * u, title, true, Paint.Align.LEFT);
        long max = 1;
        for (long v : rep.days) max = Math.max(max, v);
        float gx = r.left + 50 * u, gy = r.top + 140 * u, gw = r.width() - 100 * u, cell = gw / 7, gap = 10 * u;
        for (int i = 0; i < 7; i++) text(c, rep.weekdays[i], gx + cell * i + cell / 2, gy + 30 * u, 26 * u, sub, false, Paint.Align.CENTER);
        float rowH = Math.min(cell, (r.bottom - 40 * u - (gy + 60 * u)) / 6);
        for (int d = 0; d < rep.days.length; d++) {
            int col = (rep.firstColumn + d) % 7, row = (rep.firstColumn + d) / 7;
            q.set(gx + col * cell + gap / 2, gy + 60 * u + row * rowH + gap / 2, gx + (col + 1) * cell - gap / 2, gy + 60 * u + (row + 1) * rowH - gap / 2);
            long v = rep.days[d];
            p.setColor(v == 0 ? soft : level(v, max));
            c.drawRoundRect(q, 16 * u, 16 * u, p);
            text(c, String.valueOf(d + 1), q.centerX(), q.centerY() + 11 * u, 30 * u, v > max / 2 ? 0xFFFFFFFF : v > 0 ? title : sub, v > 0, Paint.Align.CENTER);
        }
        sideFacts(c, rep);
    }

    private void yearDays(Canvas c, PlayReport rep) {
        // twelve months as bars, the busiest one marked
        r.set(100 * u, 90 * u, 1180 * u, 990 * u);
        card(c, r);
        text(c, t(S.REPORT_BY_MONTH), r.left + 50 * u, r.top + 80 * u, 40 * u, title, true, Paint.Align.LEFT);
        long max = 1;
        int best = 0;
        for (int i = 0; i < 12; i++) { if (rep.months[i] > max) { max = rep.months[i]; best = i; } }
        float base = r.bottom - 100 * u, top = r.top + 150 * u, slot = (r.width() - 100 * u) / 12, bw = slot * 0.62f;
        for (int i = 0; i < 12; i++) {
            float x = r.left + 50 * u + i * slot + (slot - bw) / 2, h = (base - top) * rep.months[i] / max;
            q.set(x, base - Math.max(h, 8 * u), x + bw, base);
            if (rep.months[i] > 0 && i == best) p.setShader(new LinearGradient(0, q.top, 0, q.bottom, accent, mix(accent, 0xFF000000, 0.3f), Shader.TileMode.CLAMP));
            else p.setColor(rep.months[i] > 0 ? (accent & 0x00FFFFFF) | 0x80000000 : soft);
            c.drawRoundRect(q, 14 * u, 14 * u, p);
            p.setShader(null);
            text(c, fit(rep.monthNames[i], slot, 24 * u), x + bw / 2, base + 46 * u, 24 * u, i == best && rep.months[i] > 0 ? title : sub, i == best, Paint.Align.CENTER);
        }
        sideFacts(c, rep);
    }

    /** Right column of the days page: the busiest day and how many days you played. */
    private void sideFacts(Canvas c, PlayReport rep) {
        r.set(1220 * u, 90 * u, 1820 * u, 520 * u);
        card(c, r);
        text(c, t(S.REPORT_BEST_DAY), r.left + 50 * u, r.top + 80 * u, 30 * u, sub, false, Paint.Align.LEFT);
        text(c, fit(rep.bestDay == null ? "–" : rep.bestDay, r.width() - 100 * u, 64 * u), r.left + 50 * u, r.top + 190 * u, 64 * u, title, true, Paint.Align.LEFT);
        text(c, rep.bestDayMs > 0 ? StatsView.span(rep.bestDayMs) : "", r.left + 50 * u, r.top + 270 * u, 40 * u, accent, true, Paint.Align.LEFT);
        r.set(1220 * u, 560 * u, 1820 * u, 990 * u);
        card(c, r);
        text(c, t(S.REPORT_DAYS_PLAYED), r.left + 50 * u, r.top + 80 * u, 30 * u, sub, false, Paint.Align.LEFT);
        text(c, rep.daysPlayed + " / " + rep.days.length, r.left + 50 * u, r.top + 200 * u, 76 * u, title, true, Paint.Align.LEFT);
        // a thin progress bar of days played
        q.set(r.left + 50 * u, r.top + 260 * u, r.right - 50 * u, r.top + 284 * u);
        p.setColor(soft);
        c.drawRoundRect(q, 12 * u, 12 * u, p);
        q.right = q.left + q.width() * rep.daysPlayed / Math.max(1, rep.days.length);
        p.setColor(accent);
        c.drawRoundRect(q, 12 * u, 12 * u, p);
    }

    private void games(Canvas c, PlayReport rep) {
        r.set(100 * u, 90 * u, 1820 * u, 990 * u);
        card(c, r);
        text(c, t(S.REPORT_P_GAMES), r.left + 60 * u, r.top + 84 * u, 42 * u, title, true, Paint.Align.LEFT);
        if (rep.games.isEmpty()) { text(c, t(S.REPORT_NO_GAMES), r.centerX(), r.centerY(), 34 * u, sub, false, Paint.Align.CENTER); return; }
        long max = rep.games.get(0).ms;
        float rowH = 140 * u, y = r.top + 125 * u;
        for (int i = 0; i < rep.games.size(); i++) {
            PlayReport.Row g = rep.games.get(i);
            text(c, String.valueOf(i + 1), r.left + 90 * u, y + 82 * u, 48 * u, i == 0 ? accent : sub, true, Paint.Align.CENTER);
            q.set(r.left + 150 * u, y + 10 * u, r.left + 150 * u + 120 * u, y + 130 * u);
            picture(c, q, g.game, g.name, 20 * u);
            float tx = q.right + 36 * u, barMax = r.right - tx - 280 * u;
            text(c, fit(g.name, barMax + 200 * u, 36 * u), tx, y + 56 * u, 36 * u, title, true, Paint.Align.LEFT);
            q.set(tx, y + 84 * u, tx + Math.max(18 * u, barMax * g.ms / Math.max(1, max)), y + 108 * u);
            p.setColor(i == 0 ? accent : (accent & 0x00FFFFFF) | 0x88000000);
            c.drawRoundRect(q, 12 * u, 12 * u, p);
            text(c, StatsView.span(g.ms), r.right - 60 * u, y + 108 * u, 32 * u, sub, true, Paint.Align.RIGHT);
            y += rowH + 8 * u;
        }
    }

    private void consoles(Canvas c, PlayReport rep) {
        // consoles as a ring with a legend
        r.set(100 * u, 90 * u, 940 * u, 990 * u);
        card(c, r);
        text(c, t(S.REPORT_CONSOLES), r.left + 50 * u, r.top + 84 * u, 40 * u, title, true, Paint.Align.LEFT);
        long sum = 0;
        for (PlayReport.Row x : rep.consoles) sum += x.ms;
        float cx = r.centerX(), cy = r.top + 320 * u, rad = 170 * u;
        q.set(cx - rad, cy - rad, cx + rad, cy + rad);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(58 * u);
        p.setStrokeCap(Paint.Cap.BUTT);
        if (sum == 0) { p.setColor(soft); c.drawOval(q, p); }
        float start = -90;
        for (PlayReport.Row x : rep.consoles) {
            float sweep = 360f * x.ms / Math.max(1, sum);
            p.setColor(x.color);
            c.drawArc(q, start, Math.max(0.5f, sweep - (rep.consoles.size() > 1 ? 1.2f : 0)), false, p);
            start += sweep;
        }
        p.setStyle(Paint.Style.FILL);
        if (!rep.consoles.isEmpty()) {
            text(c, String.valueOf(rep.consoleCount), cx, cy + 14 * u, 72 * u, title, true, Paint.Align.CENTER);
            text(c, fit(t(S.REPORT_CONSOLES), rad * 1.3f, 26 * u), cx, cy + 56 * u, 26 * u, sub, false, Paint.Align.CENTER);
        }
        float ly = cy + rad + 100 * u;
        for (int i = 0; i < rep.consoles.size() && ly < r.bottom - 20 * u; i++) {
            PlayReport.Row x = rep.consoles.get(i);
            p.setColor(x.color);
            c.drawCircle(r.left + 70 * u, ly - 11 * u, 13 * u, p);
            text(c, fit(x.name, r.width() - 330 * u, 30 * u), r.left + 100 * u, ly, 30 * u, title, false, Paint.Align.LEFT);
            text(c, Math.round(100.0 * x.ms / Math.max(1, sum)) + "%", r.right - 50 * u, ly, 30 * u, sub, true, Paint.Align.RIGHT);
            ly += 48 * u;
        }

        // genres as bars
        r.set(980 * u, 90 * u, 1820 * u, 990 * u);
        card(c, r);
        text(c, t(S.REPORT_GENRES), r.left + 50 * u, r.top + 84 * u, 40 * u, title, true, Paint.Align.LEFT);
        if (rep.genres.isEmpty()) { text(c, fit(t(S.REPORT_NO_GENRES), r.width() - 100 * u, 28 * u), r.centerX(), r.centerY(), 28 * u, sub, false, Paint.Align.CENTER); return; }
        long gmax = rep.genres.get(0).ms, gsum = 0;
        for (PlayReport.Row x : rep.genres) gsum += x.ms;
        float y = r.top + 170 * u;
        for (PlayReport.Row x : rep.genres) {
            text(c, fit(x.name, r.width() - 260 * u, 32 * u), r.left + 50 * u, y, 32 * u, title, true, Paint.Align.LEFT);
            text(c, Math.round(100.0 * x.ms / Math.max(1, gsum)) + "%", r.right - 50 * u, y, 30 * u, sub, true, Paint.Align.RIGHT);
            q.set(r.left + 50 * u, y + 26 * u, r.right - 50 * u, y + 52 * u);
            p.setColor(soft);
            c.drawRoundRect(q, 13 * u, 13 * u, p);
            q.right = q.left + Math.max(26 * u, q.width() * x.ms / Math.max(1, gmax));
            p.setColor(x.color);
            c.drawRoundRect(q, 13 * u, 13 * u, p);
            y += 128 * u;
        }
    }

    private void habits(Canvas c, PlayReport rep) {
        // when you play: four times of day, the biggest one named
        r.set(100 * u, 90 * u, 1820 * u, 640 * u);
        card(c, r);
        int top = 0;
        for (int i = 1; i < 4; i++) if (rep.slots[i] > rep.slots[top]) top = i;
        S[] kind = {S.REPORT_KIND_MORNING, S.REPORT_KIND_AFTERNOON, S.REPORT_KIND_EVENING, S.REPORT_KIND_NIGHT};
        S[] slot = {S.REPORT_MORNING, S.REPORT_AFTERNOON, S.REPORT_EVENING, S.REPORT_NIGHT};
        long sum = rep.slots[0] + rep.slots[1] + rep.slots[2] + rep.slots[3];
        text(c, t(S.REPORT_WHEN), r.left + 60 * u, r.top + 80 * u, 32 * u, sub, false, Paint.Align.LEFT);
        text(c, fit(sum > 0 ? t(kind[top]) : "–", r.width() - 120 * u, 60 * u), r.left + 60 * u, r.top + 160 * u, 60 * u, title, true, Paint.Align.LEFT);
        float cw = (r.width() - 120 * u - 3 * 30 * u) / 4;
        for (int i = 0; i < 4; i++) {
            float x = r.left + 60 * u + i * (cw + 30 * u);
            q.set(x, r.top + 220 * u, x + cw, r.bottom - 50 * u);
            boolean hi = sum > 0 && i == top;
            if (hi) p.setShader(new LinearGradient(q.left, q.top, q.right, q.bottom, accent, mix(accent, 0xFF000000, 0.3f), Shader.TileMode.CLAMP));
            else p.setColor(soft);
            c.drawRoundRect(q, 28 * u, 28 * u, p);
            p.setShader(null);
            sky(c, i, q.left + 80 * u, q.top + 80 * u, 32 * u, hi ? 0xFFFFFFFF : accent);
            text(c, fit(t(slot[i]), cw - 80 * u, 30 * u), q.left + 40 * u, q.top + 180 * u, 30 * u, hi ? 0xD9FFFFFF : sub, true, Paint.Align.LEFT);
            text(c, sum > 0 ? Math.round(100.0 * rep.slots[i] / sum) + "%" : "–", q.left + 40 * u, q.bottom - 40 * u, 64 * u, hi ? 0xFFFFFFFF : title, true, Paint.Align.LEFT);
        }

        // three facts
        String[][] cells = {
                {t(S.LONGEST_SESSION), rep.longest > 0 ? StatsView.span(rep.longest) : "–", rep.longestTitle == null ? "" : rep.longestTitle},
                {t(S.REPORT_BEST_DAY), rep.bestDay == null ? "–" : rep.bestDay, rep.bestDayMs > 0 ? StatsView.span(rep.bestDayMs) : ""},
                {t(S.REPORT_NEW_TITLE), String.valueOf(rep.newGames), t(S.REPORT_NEW_SUB)}};
        float cw3 = (1720 * u - 2 * 30 * u) / 3;
        for (int i = 0; i < 3; i++) {
            float x = 100 * u + i * (cw3 + 30 * u);
            r.set(x, 680 * u, x + cw3, 990 * u);
            card(c, r);
            text(c, fit(cells[i][0], cw3 - 100 * u, 30 * u), r.left + 50 * u, r.top + 70 * u, 30 * u, sub, false, Paint.Align.LEFT);
            text(c, fit(cells[i][1], cw3 - 100 * u, 60 * u), r.left + 50 * u, r.top + 170 * u, 60 * u, title, true, Paint.Align.LEFT);
            text(c, fit(cells[i][2], cw3 - 100 * u, 30 * u), r.left + 50 * u, r.top + 245 * u, 30 * u, accent, false, Paint.Align.LEFT);
        }
    }

    private void emptyPage(Canvas c, PlayReport rep) {
        r.set(100 * u, 90 * u, 1820 * u, 990 * u);
        card(c, r);
        text(c, rep.title, r.centerX(), r.centerY() - 30 * u, 56 * u, title, true, Paint.Align.CENTER);
        text(c, t(S.REPORT_EMPTY), r.centerX(), r.centerY() + 50 * u, 34 * u, sub, false, Paint.Align.CENTER);
    }

    // ---- the whole report as one picture ----

    /** All pages stacked, at {@code scale} of the screen size, with a small footer. */
    static Bitmap picture(PlayReport rep, Function<Library.Game, Bitmap> cover, float scale) {
        int w = Math.round(1920 * scale), ph = Math.round(1080 * scale), foot = Math.round(110 * scale);
        Bitmap out = Bitmap.createBitmap(w, ph * PAGES + foot, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        c.drawColor(Palette.dark ? 0xFF13182E : 0xFFF3F6FA);
        ReportPainter rp = new ReportPainter(cover);
        for (int i = 0; i < PAGES; i++) {
            c.save();
            c.translate(0, i * ph);
            c.clipRect(0, 0, w, ph);
            rp.draw(c, w, rep, i);
            c.restore();
        }
        rp.u = w / 1920f;
        rp.text(c, "Duo Launcher  ·  " + rep.title, w / 2f, ph * PAGES + foot * 0.55f, 30 * rp.u, Palette.topText(), true, Paint.Align.CENTER);
        return out;
    }

    // ---- small helpers ----

    private void sky(Canvas c, int i, float x, float y, float rad, int color) {
        p.setColor(color);
        if (i == 0 || i == 1) {
            // a sun: rays, morning half-risen
            c.drawCircle(x, y, rad * (i == 0 ? 0.7f : 0.8f), p);
            p.setStrokeWidth(5 * u);
            p.setStrokeCap(Paint.Cap.ROUND);
            for (int k = 0; k < 8; k++) {
                double a = Math.PI * k / 4;
                c.drawLine(x + (float) Math.cos(a) * rad * 1.1f, y + (float) Math.sin(a) * rad * 1.1f,
                        x + (float) Math.cos(a) * rad * 1.45f, y + (float) Math.sin(a) * rad * 1.45f, p);
            }
        } else {
            // a moon: a circle with a bite; stars at night
            path.reset();
            path.addCircle(x, y, rad, Path.Direction.CW);
            Path bite = new Path();
            bite.addCircle(x + rad * 0.55f, y - rad * 0.35f, rad * 0.85f, Path.Direction.CW);
            path.op(bite, Path.Op.DIFFERENCE);
            c.drawPath(path, p);
            if (i == 3) {
                c.drawCircle(x + rad * 1.3f, y - rad * 0.9f, 5 * u, p);
                c.drawCircle(x + rad * 1.7f, y + rad * 0.1f, 4 * u, p);
            }
        }
    }

    private void picture(Canvas c, RectF box, Library.Game g, String name, float radius) {
        Bitmap b = g == null ? null : cover.apply(g);
        c.save();
        path.reset();
        path.addRoundRect(box, radius, radius, Path.Direction.CW);
        c.clipPath(path);
        if (b != null) {
            src.set(0, 0, b.getWidth(), b.getHeight());
            c.drawBitmap(b, src, box, p);
        } else {
            p.setColor(0xFF000000 | ((name == null ? 0 : name.hashCode()) & 0x7F7F7F) | 0x404040);
            c.drawRect(box, p);
            String s = name == null ? "" : name.length() > 4 ? name.substring(0, 4) : name;
            text(c, s, box.centerX(), box.centerY() + box.height() * 0.07f, box.height() * 0.2f, 0xFFFFFFFF, true, Paint.Align.CENTER);
        }
        c.restore();
    }

    private void card(Canvas c, RectF r) {
        p.setColor(card);
        p.setShadowLayer(24 * u, 0, 6 * u, Palette.dark ? 0x66000000 : 0x22304060);
        c.drawRoundRect(r, 36 * u, 36 * u, p);
        p.clearShadowLayer();
    }

    /** Five shades of the accent, by how long was played that day. */
    private int level(long v, long max) {
        float f = (float) v / max;
        int a = f > 0.75f ? 0xFF : f > 0.5f ? 0xCC : f > 0.25f ? 0x99 : 0x66;
        return (accent & 0x00FFFFFF) | (a << 24);
    }

    private static int mix(int a, int b, float f) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | Math.round(ar + (br - ar) * f) << 16 | Math.round(ag + (bg - ag) * f) << 8 | Math.round(ab + (bb - ab) * f);
    }

    private void text(Canvas c, String str, float x, float y, float size, int color, boolean bold, Paint.Align a) {
        p.setTextSize(size * Palette.textScale);
        p.setColor(color);
        p.setTypeface(Theme.font(bold));
        p.setTextAlign(a);
        c.drawText(str, x, y, p);
    }

    private String fit(String str, float maxW, float size) {
        if (str == null) return "";
        p.setTextSize(size * Palette.textScale);
        if (p.measureText(str) <= maxW) return str;
        while (str.length() > 1 && p.measureText(str + "…") > maxW) str = str.substring(0, str.length() - 1);
        return str + "…";
    }
}
