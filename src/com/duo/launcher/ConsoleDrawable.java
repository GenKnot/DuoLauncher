package com.duo.launcher;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Vector stand-in for a console photo (placeholder until real artwork is supplied). */
final class ConsoleDrawable extends Drawable {
    private final String type;
    private final boolean shadow;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    ConsoleDrawable(String type, boolean shadow) {
        this.type = type;
        this.shadow = shadow;
    }

    @Override public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width(), h = b.height();
        // unit box aspect per console
        float aspect;
        switch (type) {
            case "3ds": case "nds": aspect = 1.12f; break;
            case "gba": aspect = 0.58f; break;
            case "gb": case "gbc": aspect = 1.62f; break;
            case "ps2": aspect = 0.62f; break;
            default: aspect = 0.7f;
        }
        float avail = shadow ? h * 0.9f : h;
        float u = Math.min(w, avail / aspect);
        float ox = b.left + (w - u) / 2, oy = b.top + (avail - u * aspect) / 2;
        if (shadow) {
            float cy = oy + u * aspect + h * 0.04f;
            p.setShader(new RadialGradient(b.centerX(), cy, u * 0.6f, 0x40000000, 0x00000000, Shader.TileMode.CLAMP));
            c.save();
            c.scale(1f, 0.12f, b.centerX(), cy);
            c.drawCircle(b.centerX(), cy, u * 0.6f, p);
            c.restore();
            p.setShader(null);
        }
        c.save();
        c.translate(ox, oy);
        switch (type) {
            case "3ds": clam(c, u, 0xFF3FB6C8, true); break;
            case "nds": clam(c, u, 0xFFC9D0D8, false); break;
            case "gba": gba(c, u, 0xFF5B4BC4); break;
            case "gbc": gb(c, u, 0xFF1FA99B, 0xFFB8D8B0); break;
            case "gb": gb(c, u, 0xFFC8C4BC, 0xFF9BBC0F); break;
            case "ps2": ps2(c, u); break;
            default: body(c, 0, 0, u, u * 0.7f, u * 0.08f, 0xFF4A90D9);
        }
        c.restore();
    }

    private static int shade(int color, float f) {
        int a = color >>> 24, rr = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;
        if (f > 1) {
            rr += (int) ((255 - rr) * (f - 1)); g += (int) ((255 - g) * (f - 1)); b += (int) ((255 - b) * (f - 1));
        } else {
            rr = (int) (rr * f); g = (int) (g * f); b = (int) (b * f);
        }
        return a << 24 | rr << 16 | g << 8 | b;
    }

    private void body(Canvas c, float l, float t, float rt, float bt, float rad, int color) {
        r.set(l, t, rt, bt);
        p.setShader(new LinearGradient(0, t, 0, bt, shade(color, 1.18f), shade(color, 0.86f), Shader.TileMode.CLAMP));
        c.drawRoundRect(r, rad, rad, p);
        p.setShader(null);
    }

    private void screen(Canvas c, float l, float t, float rt, float bt, float rad, int color) {
        r.set(l, t, rt, bt);
        p.setShader(new LinearGradient(l, t, rt, bt, shade(color, 1.25f), color, Shader.TileMode.CLAMP));
        c.drawRoundRect(r, rad, rad, p);
        p.setShader(null);
    }

    private void dpad(Canvas c, float cx, float cy, float s, int color) {
        p.setColor(color);
        r.set(cx - s, cy - s / 3, cx + s, cy + s / 3);
        c.drawRoundRect(r, s / 6, s / 6, p);
        r.set(cx - s / 3, cy - s, cx + s / 3, cy + s);
        c.drawRoundRect(r, s / 6, s / 6, p);
    }

    private void dot(Canvas c, float cx, float cy, float rad, int color) {
        p.setColor(color);
        c.drawCircle(cx, cy, rad, p);
    }

    private void clam(Canvas c, float u, int color, boolean is3ds) {
        int dark = 0xFF1C2530;
        body(c, 0, 0, u, u * 0.54f, u * 0.06f, color);
        float sx = is3ds ? 0.1f : 0.22f;
        screen(c, u * sx, u * 0.07f, u * (1 - sx), u * 0.47f, u * 0.015f, dark);
        p.setColor(shade(color, 0.7f));
        r.set(u * 0.04f, u * 0.52f, u * 0.96f, u * 0.6f);
        c.drawRoundRect(r, u * 0.03f, u * 0.03f, p);
        body(c, 0, u * 0.58f, u, u * 1.12f, u * 0.06f, color);
        screen(c, u * 0.26f, u * 0.64f, u * 0.74f, u * 1.0f, u * 0.012f, dark);
        int btn = shade(color, 0.62f);
        if (is3ds) {
            dot(c, u * 0.13f, u * 0.73f, u * 0.055f, 0xFF4A4F57);
            dpad(c, u * 0.13f, u * 0.94f, u * 0.05f, btn);
        } else {
            dpad(c, u * 0.13f, u * 0.8f, u * 0.055f, btn);
        }
        float bx = u * 0.87f, by = is3ds ? u * 0.76f : u * 0.8f, d = u * 0.045f;
        dot(c, bx, by - d, u * 0.022f, btn);
        dot(c, bx, by + d, u * 0.022f, btn);
        dot(c, bx - d, by, u * 0.022f, btn);
        dot(c, bx + d, by, u * 0.022f, btn);
    }

    private void gba(Canvas c, float u, int color) {
        body(c, 0, 0, u, u * 0.58f, u * 0.2f, color);
        screen(c, u * 0.27f, u * 0.07f, u * 0.73f, u * 0.43f, u * 0.03f, 0xFF262A33);
        screen(c, u * 0.31f, u * 0.11f, u * 0.69f, u * 0.39f, u * 0.006f, 0xFF7E9CA8);
        int btn = shade(color, 0.6f);
        dpad(c, u * 0.14f, u * 0.3f, u * 0.055f, btn);
        dot(c, u * 0.82f, u * 0.33f, u * 0.035f, btn);
        dot(c, u * 0.9f, u * 0.25f, u * 0.035f, btn);
    }

    private void gb(Canvas c, float u, int color, int lcd) {
        body(c, 0, 0, u, u * 1.62f, u * 0.07f, color);
        screen(c, u * 0.09f, u * 0.09f, u * 0.91f, u * 0.74f, u * 0.04f, 0xFF5D6370);
        screen(c, u * 0.22f, u * 0.17f, u * 0.78f, u * 0.64f, u * 0.008f, lcd);
        int btn = shade(color, 0.55f);
        dpad(c, u * 0.25f, u * 1.06f, u * 0.09f, 0xFF3A3D44);
        dot(c, u * 0.78f, u * 0.98f, u * 0.07f, 0xFF9C2B5E);
        dot(c, u * 0.6f, u * 1.08f, u * 0.07f, 0xFF9C2B5E);
        p.setColor(btn);
        r.set(u * 0.33f, u * 1.32f, u * 0.45f, u * 1.36f);
        c.drawRoundRect(r, u * 0.02f, u * 0.02f, p);
        r.set(u * 0.5f, u * 1.32f, u * 0.62f, u * 1.36f);
        c.drawRoundRect(r, u * 0.02f, u * 0.02f, p);
    }

    private void ps2(Canvas c, float u) {
        body(c, 0, u * 0.1f, u, u * 0.62f, u * 0.03f, 0xFF2A2C33);
        p.setColor(0xFF1A1B20);
        r.set(u * 0.04f, u * 0.36f, u * 0.96f, u * 0.38f);
        c.drawRect(r, p);
        p.setColor(0xFF3A62D8);
        r.set(u * 0.78f, u * 0.17f, u * 0.92f, u * 0.2f);
        c.drawRoundRect(r, u * 0.01f, u * 0.01f, p);
    }

    @Override public void setAlpha(int a) { p.setAlpha(a); }
    @Override public void setColorFilter(ColorFilter cf) { p.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
