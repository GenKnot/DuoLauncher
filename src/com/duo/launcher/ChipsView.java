package com.duo.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** A row of rounded "pill" labels; an entry may start with a button key drawn as a badge ("A|开始游戏"). */
final class ChipsView extends View {
    private String[] chips = new String[0];
    private boolean alignRight, dark;
    private float size = 28;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    ChipsView(Context c, boolean alignRight) {
        super(c);
        this.alignRight = alignRight;
    }

    void set(boolean dark, String... chips) {
        this.dark = dark;
        this.chips = chips;
        requestLayout();
        invalidate();
    }

    void setTextSize(float px) { size = px; requestLayout(); }

    private float padX() { return size * 0.75f; }
    private float gap() { return size * 0.5f; }
    private float h() { return size * 1.9f; }

    private float chipWidth(String s) {
        int bar = s.indexOf('|');
        tp.setTextSize(size);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        if (bar < 0) return tp.measureText(s) + padX() * 2;
        float key = badgeW(s.substring(0, bar));
        tp.setTextSize(size);
        tp.setTypeface(Typeface.DEFAULT);
        return key + size * 0.45f + tp.measureText(s.substring(bar + 1)) + padX() * 1.4f;
    }

    private float badgeW(String key) {
        tp.setTextSize(size * 0.72f);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        return Math.max(size * 1.2f, tp.measureText(key) + size * 0.6f);
    }

    private float total() {
        float w = 0;
        for (String s : chips) w += chipWidth(s) + gap();
        return Math.max(0, w - gap());
    }

    @Override protected void onMeasure(int ws, int hs) {
        setMeasuredDimension(resolveSize((int) Math.ceil(total()) + 2, ws), (int) Math.ceil(h()) + 2);
    }

    @Override protected void onDraw(Canvas c) {
        float x = alignRight ? getWidth() - total() - 1 : (getWidth() - total()) / 2;
        float top = 1, bottom = 1 + h(), cy = (top + bottom) / 2;
        int bg = dark ? 0x33FFFFFF : 0x14203040, fg = dark ? 0xFFFFFFFF : 0xFF2A3644;
        for (String s : chips) {
            float w = chipWidth(s);
            r.set(x, top, x + w, bottom);
            p.setColor(bg);
            c.drawRoundRect(r, h() / 2, h() / 2, p);
            int bar = s.indexOf('|');
            tp.setColor(fg);
            if (bar < 0) {
                tp.setTextSize(size);
                tp.setTypeface(Typeface.DEFAULT_BOLD);
                tp.setTextAlign(Paint.Align.CENTER);
                c.drawText(s, r.centerX(), cy + size * 0.36f, tp);
            } else {
                String key = s.substring(0, bar), label = s.substring(bar + 1);
                float bw = badgeW(key), bh = size * 1.2f, bx = x + padX() * 0.7f;
                r.set(bx, cy - bh / 2, bx + bw, cy + bh / 2);
                p.setColor(dark ? 0xFFFFFFFF : 0xFF2A3644);
                c.drawRoundRect(r, bh / 2, bh / 2, p);
                tp.setTextSize(size * 0.72f);
                tp.setTypeface(Typeface.DEFAULT_BOLD);
                tp.setTextAlign(Paint.Align.CENTER);
                tp.setColor(dark ? 0xFF202830 : 0xFFFFFFFF);
                c.drawText(key, r.centerX(), cy + size * 0.26f, tp);
                tp.setTextSize(size);
                tp.setTypeface(Typeface.DEFAULT);
                tp.setTextAlign(Paint.Align.LEFT);
                tp.setColor(fg);
                c.drawText(label, r.right + size * 0.45f, cy + size * 0.36f, tp);
            }
            x += w + gap();
        }
    }
}
