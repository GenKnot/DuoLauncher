package com.duo.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

/**
 * Backdrop behind the console on the upper screen: the platform's background picture (inverted to
 * navy-and-white in dark mode) or, without one, oversized platform lettering. Slowly turning rings
 * and a halo in the platform colour sit on top either way.
 */
final class BackdropView extends View {
    private int accent = 0xFF4A90D9;
    private String label = "";
    private Bitmap picture;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint pic = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final RectF r = new RectF();
    private final Rect src = new Rect();
    private final DashPathEffect dash = new DashPathEffect(new float[]{4, 14}, 0);
    private static final ColorMatrixColorFilter INVERT = new ColorMatrixColorFilter(new ColorMatrix(new float[]{
            -0.85f, 0, 0, 0, 235,
            0, -0.85f, 0, 0, 240,
            0, 0, -0.80f, 0, 250,
            0, 0, 0, 1, 0}));

    BackdropView(Context c) { super(c); }

    void set(int accent, String label, Bitmap picture) {
        this.accent = accent;
        this.label = label;
        this.picture = picture;
        invalidate();
    }

    private static int mix(int a, int b, float t) {
        int ra = (a >> 16) & 255, ga = (a >> 8) & 255, ba = a & 255;
        int rb = (b >> 16) & 255, gb = (b >> 8) & 255, bb = b & 255;
        return 0xFF000000 | (int) (ra + (rb - ra) * t) << 16 | (int) (ga + (gb - ga) * t) << 8 | (int) (ba + (bb - ba) * t);
    }

    private int tint(int alpha) { return (alpha << 24) | (accent & 0xFFFFFF); }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), u = w / 1920f;
        float cx = w / 2, cy = h * 0.37f;
        boolean dark = Palette.dark;
        double time = SystemClock.uptimeMillis() / 1000.0;

        int base0 = dark ? 0xFF161D29 : 0xFFFCFDFE;
        int base1 = dark ? mix(accent, 0xFF0B0F15, 0.82f) : mix(accent, 0xFFFFFFFF, 0.8f);
        p.setShader(new RadialGradient(cx, cy, w * 0.75f, base0, base1, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        if (picture != null) {
            float k = Math.max(w / picture.getWidth(), h / picture.getHeight());
            float dw = picture.getWidth() * k, dh = picture.getHeight() * k;
            r.set((w - dw) / 2, (h - dh) / 2, (w + dw) / 2, (h + dh) / 2);
            src.set(0, 0, picture.getWidth(), picture.getHeight());
            pic.setColorFilter(dark ? INVERT : null);
            c.drawBitmap(picture, src, r, pic);
        } else {
            // oversized lettering along the bottom, drifting slowly
            p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            p.setTextAlign(Paint.Align.CENTER);
            float size = h * 0.78f;
            p.setTextSize(size);
            float tw = p.measureText(label);
            if (tw > w * 1.15f) p.setTextSize(size * w * 1.15f / tw);
            float drift = (float) Math.sin(time / 9.0) * 40 * u;
            p.setStyle(Paint.Style.FILL);
            p.setColor(tint(dark ? 0x18 : 0x10));
            c.drawText(label, cx + drift, h * 0.97f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.5f * u);
            p.setColor(tint(dark ? 0x40 : 0x30));
            c.drawText(label, cx + drift, h * 0.97f, p);
            p.setStyle(Paint.Style.FILL);
        }

        // rings around the console, fainter over a picture
        float ringAlpha = picture != null ? 0.5f : 1f;
        p.setStyle(Paint.Style.STROKE);
        c.save();
        c.rotate((float) (time * 3 % 360), cx, cy);
        for (int i = 0; i < 6; i++) {
            float rad = (300 + i * 115) * u;
            p.setStrokeWidth((i % 2 == 0 ? 2f : 3f) * u);
            p.setPathEffect(i % 2 == 0 ? null : dash);
            p.setColor(tint((int) ((0x26 - i * 4) * ringAlpha)));
            c.drawCircle(cx, cy, rad, p);
        }
        p.setPathEffect(null);
        c.restore();
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            double a = time * (0.12 + i * 0.05) + i * 2.1;
            float rad = (300 + i * 230) * u;
            p.setColor(tint((int) ((0x55 - i * 0x12) * ringAlpha)));
            c.drawCircle(cx + (float) Math.cos(a) * rad, cy + (float) Math.sin(a) * rad, (7 - i * 1.5f) * u, p);
        }

        // halo behind the console, and a fade at the bottom so the title stays readable
        int glow = dark ? 0x00000000 | (mix(accent, 0xFF1A2230, 0.7f) & 0xFFFFFF) : 0xFFFFFF;
        p.setShader(new RadialGradient(cx, cy, 520 * u, (dark ? 0x99000000 : 0xCC000000) | glow, glow, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        int fade = dark ? 0x0F141C : 0xFFFFFF;
        p.setShader(new LinearGradient(0, h * 0.55f, 0, h, fade, (picture != null ? 0xE6000000 : 0x99000000) | fade, Shader.TileMode.CLAMP));
        c.drawRect(0, h * 0.55f, w, h, p);
        p.setShader(null);

        if (getVisibility() == VISIBLE) postInvalidateDelayed(33);
    }
}
