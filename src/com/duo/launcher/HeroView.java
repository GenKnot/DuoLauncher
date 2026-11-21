package com.duo.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * The centrepiece of the upper screen. Draws one of:
 * a transparent logo with a soft drop shadow, an opaque picture as a rounded card with a moving
 * gloss highlight, a pixel-art icon on a white tile, or a console drawable.
 */
final class HeroView extends View {
    static final int NONE = 0, LOGO = 1, CARD = 2, PIXEL = 3, DRAWABLE = 4, FAN = 5;

    private int mode = NONE;
    private Bitmap[] fan = new Bitmap[0];
    private Bitmap bmp, shadow;
    private int shadowOx, shadowOy;
    private Drawable drawable;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint gloss = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Rect src = new Rect();
    private final Path clip = new Path();

    HeroView(Context c) { super(c); }

    int mode() { return mode; }

    void clear() { set(NONE, null, null); }

    void setBitmap(int mode, Bitmap b) { set(mode, b, null); }

    void setDrawable(Drawable d) { set(DRAWABLE, null, d); }

    /** Up to five square tiles fanned out like a hand of cards; null entries are skipped. */
    void setFan(Bitmap[] tiles) {
        fan = tiles;
        set(FAN, null, null);
    }

    private void set(int m, Bitmap b, Drawable d) {
        mode = m;
        bmp = b;
        drawable = d;
        shadow = null;
        if (m == LOGO && b != null) makeShadow(b);
        invalidate();
    }

    /** Blurred alpha silhouette of the logo, computed at reduced size. */
    private void makeShadow(Bitmap b) {
        float s = Math.min(1f, 400f / Math.max(b.getWidth(), b.getHeight()));
        Bitmap small = s < 1 ? Bitmap.createScaledBitmap(b, Math.max(1, (int) (b.getWidth() * s)), Math.max(1, (int) (b.getHeight() * s)), true) : b;
        Paint bp = new Paint();
        bp.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
        int[] off = new int[2];
        shadow = small.extractAlpha(bp, off);
        shadowOx = off[0];
        shadowOy = off[1];
        shadowScale = s;
    }

    private float shadowScale = 1;

    /** Fit the content into the view, centred, leaving room for shadows. */
    private void fit(float bw, float bh, float margin) {
        float w = getWidth() - margin * 2, h = getHeight() - margin * 2;
        float k = Math.min(w / bw, h / bh);
        float dw = bw * k, dh = bh * k;
        r.set((getWidth() - dw) / 2, (getHeight() - dh) / 2, (getWidth() + dw) / 2, (getHeight() + dh) / 2);
    }

    @Override protected void onDraw(Canvas c) {
        float u = getWidth() / 1120f;
        switch (mode) {
            case DRAWABLE:
                if (drawable != null) {
                    drawable.setBounds(0, 0, getWidth(), getHeight());
                    drawable.draw(c);
                }
                break;
            case LOGO: {
                fit(bmp.getWidth(), bmp.getHeight(), 30 * u);
                if (shadow != null) {
                    float k = r.width() / bmp.getWidth() / shadowScale;
                    c.save();
                    c.translate(r.left + shadowOx * k, r.top + shadowOy * k + 14 * u);
                    c.scale(k, k);
                    p.setColor(0x99000000);
                    c.drawBitmap(shadow, 0, 0, p);
                    c.restore();
                }
                src.set(0, 0, bmp.getWidth(), bmp.getHeight());
                c.drawBitmap(bmp, src, r, p);
                break;
            }
            case CARD: {
                fit(bmp.getWidth(), bmp.getHeight(), 40 * u);
                float rad = 22 * u;
                p.setColor(0xFF000000);
                p.setShadowLayer(34 * u, 0, 16 * u, 0x99000000);
                c.drawRoundRect(r, rad, rad, p);
                p.clearShadowLayer();
                clip.reset();
                clip.addRoundRect(r, rad, rad, Path.Direction.CW);
                c.save();
                c.clipPath(clip);
                src.set(0, 0, bmp.getWidth(), bmp.getHeight());
                c.drawBitmap(bmp, src, r, p);
                // gloss band follows the tilt
                float t = (getRotationY() + 10f) / 20f;
                float gx = r.left - r.width() * 0.6f + t * r.width() * 2.2f, bw = r.width() * 0.35f;
                gloss.setShader(new LinearGradient(gx - bw, r.top, gx + bw, r.bottom,
                        new int[]{0x00FFFFFF, 0x38FFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
                c.drawRect(r, gloss);
                c.restore();
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2 * u);
                p.setColor(0x40FFFFFF);
                c.drawRoundRect(r, rad, rad, p);
                p.setStyle(Paint.Style.FILL);
                break;
            }
            case PIXEL: {
                float side = Math.min(getWidth(), getHeight()) * 0.78f;
                r.set((getWidth() - side) / 2, (getHeight() - side) / 2, (getWidth() + side) / 2, (getHeight() + side) / 2);
                float rad = side * 0.16f;
                p.setColor(0xFFFFFFFF);
                p.setShadowLayer(30 * u, 0, 14 * u, 0x80000000);
                c.drawRoundRect(r, rad, rad, p);
                p.clearShadowLayer();
                r.inset(side * 0.12f, side * 0.12f);
                src.set(0, 0, bmp.getWidth(), bmp.getHeight());
                p.setFilterBitmap(false);
                c.drawBitmap(bmp, src, r, p);
                p.setFilterBitmap(true);
                break;
            }
            case FAN: {
                int n = 0;
                for (Bitmap b : fan) if (b != null) n++;
                if (n == 0) break;
                float side = getHeight() * 0.62f, rad = side * 0.14f;
                float cx = getWidth() / 2f, cy = getHeight() / 2f + side * 0.08f;
                // back cards first, the most recent game ends up in front, in the middle
                int[] order = {4, 3, 2, 1, 0};
                float[] angle = {0, -11, 11, -21, 21};
                float[] dx = {0, -0.62f, 0.62f, -1.12f, 1.12f};
                for (int oi : order) {
                    if (oi >= fan.length || fan[oi] == null) continue;
                    Bitmap b = fan[oi];
                    float s = oi == 0 ? 1f : oi < 3 ? 0.86f : 0.74f;
                    c.save();
                    c.translate(cx + dx[oi] * side, cy + (oi == 0 ? 0 : side * 0.06f * (oi < 3 ? 1 : 2)));
                    c.rotate(angle[oi]);
                    float hs = side * s / 2;
                    r.set(-hs, -hs, hs, hs);
                    p.setColor(0xFFFFFFFF);
                    p.setShadowLayer(26 * u, 0, 12 * u, 0x70000000);
                    c.drawRoundRect(r, rad * s, rad * s, p);
                    p.clearShadowLayer();
                    clip.reset();
                    clip.addRoundRect(r, rad * s, rad * s, Path.Direction.CW);
                    c.clipPath(clip);
                    if (b.hasAlpha()) r.inset(hs * 0.2f, hs * 0.2f);
                    src.set(0, 0, b.getWidth(), b.getHeight());
                    c.drawBitmap(b, src, r, p);
                    c.restore();
                }
                break;
            }
            default:
                break;
        }
    }
}
