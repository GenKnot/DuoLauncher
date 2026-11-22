package com.duo.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Screensaver for the upper screen: game artwork with a slow zoom and cross-fade, plus a large clock. */
final class SaverView extends View {
    private static final long SLIDE = 8000, FADE = 1500;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect src = new Rect();
    private final RectF dst = new RectF();
    private final List<Library.Game> pool = new ArrayList<>();
    private Bitmap cur, prev;
    /** Tiny copies drawn stretched behind pictures that are too small or too square to fill the screen. */
    private final java.util.Map<Bitmap, Bitmap> soft = new java.util.WeakHashMap<>();
    private String curTitle = "", prevTitle = "";
    private long shownAt;
    private int next;
    private boolean running;

    SaverView(Context c) {
        super(c);
        setVisibility(GONE);
    }

    void start(List<Library.Game> games) {
        pool.clear();
        pool.addAll(games);
        Collections.shuffle(pool);
        next = 0;
        cur = prev = null;
        running = true;
        setAlpha(0f);
        setVisibility(VISIBLE);
        animate().alpha(1f).setDuration(900).start();
        advance();
    }

    void stop() {
        running = false;
        main.removeCallbacksAndMessages(null);
        animate().cancel();
        setVisibility(GONE);
        cur = prev = null;
    }

    /** Loads the next picture off the main thread and fades it in. */
    private void advance() {
        if (!running || pool.isEmpty()) return;
        Library.Game g = pool.get(next++ % pool.size());
        new Thread(() -> {
            Bitmap b = null;
            for (String kind : new String[]{"background", "box_front"}) {
                File f = g.asset(kind);
                if (f == null) continue;
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(f.getPath(), o);
                o.inJustDecodeBounds = false;
                o.inSampleSize = o.outWidth > 2600 ? 2 : 1;
                b = BitmapFactory.decodeFile(f.getPath(), o);
                if (b != null) {
                    if (!fillsScreen(b)) soft.put(b, Bitmap.createScaledBitmap(b, 24, Math.max(1, 24 * b.getHeight() / b.getWidth()), true));
                    break;
                }
            }
            Bitmap fb = b;
            main.post(() -> {
                if (!running) return;
                if (fb == null) { advance(); return; }  // nothing to show for this one, try the next
                prev = cur;
                prevTitle = curTitle;
                cur = fb;
                curTitle = g.title;
                shownAt = SystemClock.uptimeMillis();
                invalidate();
                main.postDelayed(this::advance, SLIDE);
            });
        }, "saver").start();
    }

    /** Wide, large pictures (title screens, fan art) fill the screen; box art is shown whole over a soft blur of itself. */
    private static boolean fillsScreen(Bitmap b) {
        return b.getWidth() >= 900 && b.getWidth() >= b.getHeight() * 1.3f;
    }

    private void drawPicture(Canvas c, Bitmap b, float age, int alpha) {
        float w = getWidth(), h = getHeight();
        Bitmap blur = soft.get(b);
        if (blur != null) {
            float zoom = 1.04f + 0.06f * Math.min(1f, age / (SLIDE + FADE));
            float kb = Math.max(w / blur.getWidth(), h / blur.getHeight()) * 1.2f;
            float bw = blur.getWidth() * kb, bh = blur.getHeight() * kb;
            dst.set((w - bw) / 2, (h - bh) / 2, (w + bw) / 2, (h + bh) / 2);
            src.set(0, 0, blur.getWidth(), blur.getHeight());
            p.setAlpha(alpha);
            c.drawBitmap(blur, src, dst, p);
            p.setColor(0x55000000);
            p.setAlpha(alpha * 0x55 / 255);
            c.drawRect(0, 0, w, h, p);
            float k = Math.min(w * 0.62f / b.getWidth(), h * 0.72f / b.getHeight()) * zoom;
            float dw = b.getWidth() * k, dh = b.getHeight() * k, cy = h * 0.44f;
            dst.set((w - dw) / 2, cy - dh / 2, (w + dw) / 2, cy + dh / 2);
            src.set(0, 0, b.getWidth(), b.getHeight());
            p.setColor(0xFF000000);
            p.setAlpha(alpha);
            c.drawBitmap(b, src, dst, p);
            p.setAlpha(255);
            return;
        }
        float k = Math.max(w / b.getWidth(), h / b.getHeight()) * (1.04f + 0.08f * Math.min(1f, age / (SLIDE + FADE)));
        float dw = b.getWidth() * k, dh = b.getHeight() * k;
        dst.set((w - dw) / 2, (h - dh) / 2, (w + dw) / 2, (h + dh) / 2);
        src.set(0, 0, b.getWidth(), b.getHeight());
        p.setAlpha(alpha);
        c.drawBitmap(b, src, dst, p);
        p.setAlpha(255);
    }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), u = w / 1920f;
        c.drawColor(0xFF000000);
        long now = SystemClock.uptimeMillis(), age = now - shownAt;
        float fade = Math.min(1f, age / (float) FADE);
        if (prev != null && fade < 1f) drawPicture(c, prev, age + SLIDE, 255);
        if (cur != null) drawPicture(c, cur, age, (int) (255 * fade));
        // OLED: the clock and the name never stay in one place. Each slide moves them to another corner,
        // and everything drifts slowly by a few dozen pixels on top of that.
        int corner = (next + 3) % 4;                     // changes with every slide
        boolean topSide = corner == 1 || corner == 2, clockRight = corner == 0 || corner == 1;
        double tt = now / 60000.0;
        float dx = (float) (Math.sin(tt * 2 * Math.PI / 4.3) * 40 * u), dy = (float) (Math.cos(tt * 2 * Math.PI / 3.1) * 24 * u);
        if (topSide) p.setShader(new LinearGradient(0, 0, 0, h * 0.55f, 0xCC000000, 0x00000000, Shader.TileMode.CLAMP));
        else p.setShader(new LinearGradient(0, h * 0.45f, 0, h, 0x00000000, 0xCC000000, Shader.TileMode.CLAMP));
        c.drawRect(0, topSide ? 0 : h * 0.45f, w, topSide ? h * 0.55f : h, p);
        p.setShader(null);
        float a = fade < 1f ? fade : 1f;
        p.setColor(0xFFFFFFFF);
        p.setShadowLayer(12 * u, 0, 4 * u, 0x99000000);
        float clockX = clockRight ? w - 80 * u + dx : 80 * u + dx;
        float clockY = topSide ? 210 * u + dy : h - 110 * u + dy;
        p.setTextAlign(clockRight ? Paint.Align.RIGHT : Paint.Align.LEFT);
        p.setTypeface(Theme.font(true));
        p.setTextSize(150 * u);
        p.setAlpha((int) (255 * a));
        String time = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.forLanguageTag(I18n.code())).format(new Date());
        c.drawText(time, clockX, clockY, p);
        p.setTextSize(34 * u);
        p.setTypeface(Theme.font(false));
        String day = DateFormat.getDateInstance(DateFormat.FULL, Locale.forLanguageTag(I18n.code())).format(new Date());
        c.drawText(day, clockX + (clockRight ? -4 : 4) * u, clockY + 54 * u, p);
        // the game's name sits in the opposite corner on the same side
        p.setTextAlign(clockRight ? Paint.Align.LEFT : Paint.Align.RIGHT);
        p.setTextSize(40 * u);
        p.setTypeface(Theme.font(true));
        p.setAlpha((int) (220 * a));
        c.drawText(curTitle, clockRight ? 80 * u - dx : w - 80 * u - dx, topSide ? 120 * u - dy : h - 70 * u - dy, p);
        p.setAlpha(255);
        p.setTextAlign(Paint.Align.LEFT);
        p.clearShadowLayer();
        if (running) postInvalidateDelayed(fade < 1f ? 16 : 50);
    }
}
