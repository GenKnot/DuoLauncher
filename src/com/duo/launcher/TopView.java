package com.duo.launcher;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.duo.launcher.I18n.S;
import static com.duo.launcher.I18n.t;

/** Upper screen: platform showcase or the focused game's artwork, with a slow 3D tilt. */
final class TopView extends ViewGroup {
    private final Icons icons;
    private final ImageView bg;
    private final HeroView hero;
    private final View scrim;
    private final BackdropView dots;
    private final TextView title, desc, message;
    private final ChipsView chips, hints;
    private final StatusView status;
    private Object shown;
    private boolean dark;
    private final Runnable tick = new Runnable() {
        @Override public void run() { status.invalidate(); postDelayed(this, 30_000); }
    };

    TopView(Context c, Icons icons) {
        super(c);
        this.icons = icons;

        dots = new BackdropView(c);
        bg = new ImageView(c);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bg.setRenderEffect(RenderEffect.createBlurEffect(70, 70, Shader.TileMode.CLAMP));
        scrim = new View(c) {
            final Paint p = new Paint();
            @Override protected void onDraw(Canvas cv) {
                float w = getWidth(), h = getHeight();
                p.setShader(new LinearGradient(0, 0, 0, h, new int[]{0x55000000, 0x33000000, 0xDD000000},
                        new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
                cv.drawRect(0, 0, w, h, p);
                p.setShader(new RadialGradient(w / 2, h / 2, w * 0.75f, 0x00000000, 0x88000000, Shader.TileMode.CLAMP));
                cv.drawRect(0, 0, w, h, p);
            }
        };
        hero = new HeroView(c);
        title = text(c, Typeface.BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        desc = text(c, Typeface.NORMAL);
        desc.setMaxLines(2);
        desc.setEllipsize(TextUtils.TruncateAt.END);
        desc.setLineSpacing(0, 1.15f);
        message = text(c, Typeface.NORMAL);
        chips = new ChipsView(c, false);
        hints = new ChipsView(c, true);
        status = new StatusView(c);
        for (View v : new View[]{dots, bg, scrim, hero, title, chips, desc, hints, status, message}) addView(v);

        ObjectAnimator tilt = ObjectAnimator.ofFloat(hero, "rotationY", -10f, 10f);
        tilt.setDuration(4200);
        tilt.setRepeatCount(ValueAnimator.INFINITE);
        tilt.setRepeatMode(ValueAnimator.REVERSE);
        tilt.setInterpolator(new AccelerateDecelerateInterpolator());
        tilt.addUpdateListener(a -> { if (hero.mode() == HeroView.CARD) hero.invalidate(); });
        tilt.start();
        ObjectAnimator bob = ObjectAnimator.ofFloat(hero, "translationY", 0f, -12f);
        bob.setDuration(3000);
        bob.setRepeatCount(ValueAnimator.INFINITE);
        bob.setRepeatMode(ValueAnimator.REVERSE);
        bob.setInterpolator(new AccelerateDecelerateInterpolator());
        bob.start();
        setDark(false);
        showMessage(t(S.LOADING));
    }

    private static TextView text(Context c, int style) {
        TextView t = new TextView(c);
        t.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        t.setGravity(Gravity.CENTER);
        t.setIncludeFontPadding(false);
        return t;
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); post(tick); }
    @Override protected void onDetachedFromWindow() { removeCallbacks(tick); super.onDetachedFromWindow(); }

    // ---- layout on a 1920x1080 design canvas ----

    @Override protected void onMeasure(int ws, int hs) {
        int w = MeasureSpec.getSize(ws), h = MeasureSpec.getSize(hs);
        setMeasuredDimension(w, h);
        float u = w / 1920f;
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 62 * u);
        desc.setTextSize(TypedValue.COMPLEX_UNIT_PX, 27 * u);
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40 * u);
        chips.setTextSize(27 * u);
        hints.setTextSize(25 * u);
        int ex = MeasureSpec.EXACTLY, at = MeasureSpec.AT_MOST;
        for (View v : new View[]{dots, bg, scrim}) v.measure(MeasureSpec.makeMeasureSpec(w, ex), MeasureSpec.makeMeasureSpec(h, ex));
        hero.measure(MeasureSpec.makeMeasureSpec((int) (1120 * u), ex), MeasureSpec.makeMeasureSpec((int) (560 * u), ex));
        title.measure(MeasureSpec.makeMeasureSpec((int) (w - 300 * u), ex), MeasureSpec.makeMeasureSpec(h, at));
        desc.measure(MeasureSpec.makeMeasureSpec((int) (1240 * u), ex), MeasureSpec.makeMeasureSpec(h, at));
        message.measure(MeasureSpec.makeMeasureSpec((int) (w - 300 * u), ex), MeasureSpec.makeMeasureSpec(h, at));
        chips.measure(MeasureSpec.makeMeasureSpec(w, ex), MeasureSpec.makeMeasureSpec(h, at));
        hints.measure(MeasureSpec.makeMeasureSpec(w, ex), MeasureSpec.makeMeasureSpec(h, at));
        status.measure(MeasureSpec.makeMeasureSpec((int) (420 * u), ex), MeasureSpec.makeMeasureSpec((int) (56 * u), ex));
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l, h = b - t;
        float u = w / 1920f;
        for (View v : new View[]{dots, bg, scrim}) v.layout(0, 0, w, h);
        boolean hasDesc = desc.getVisibility() == VISIBLE && desc.getText().length() > 0;
        int hy = (int) ((hasDesc ? 96 : 120) * u);
        place(hero, (w - hero.getMeasuredWidth()) / 2, hy);
        hero.setPivotX(hero.getMeasuredWidth() / 2f);
        hero.setPivotY(hero.getMeasuredHeight() / 2f);
        hero.setCameraDistance(9000 * getResources().getDisplayMetrics().density);
        int y = hy + hero.getMeasuredHeight() + (int) (34 * u);
        place(title, (w - title.getMeasuredWidth()) / 2, y);
        y += title.getMeasuredHeight() + (int) (22 * u);
        place(chips, 0, y);
        y += chips.getMeasuredHeight() + (int) (22 * u);
        place(desc, (w - desc.getMeasuredWidth()) / 2, y);
        place(message, (w - message.getMeasuredWidth()) / 2, (h - message.getMeasuredHeight()) / 2);
        place(status, w - status.getMeasuredWidth() - (int) (40 * u), (int) (26 * u));
        place(hints, -(int) (40 * u), h - hints.getMeasuredHeight() - (int) (30 * u));
    }

    private static void place(View v, int x, int y) { v.layout(x, y, x + v.getMeasuredWidth(), y + v.getMeasuredHeight()); }

    private void setDark(boolean d) {
        dark = d;
        dots.setVisibility(d ? GONE : VISIBLE);
        bg.setVisibility(d ? VISIBLE : GONE);
        scrim.setVisibility(d ? VISIBLE : GONE);
        // d: a game's artwork is shown (always dark); otherwise follow the light/dark palette
        setBackgroundColor(d ? 0xFF0E1216 : Palette.topBg());
        title.setTextColor(d ? 0xFFFFFFFF : Palette.topTitle());
        desc.setTextColor(d ? 0xCCE6ECF2 : Palette.topText());
        message.setTextColor(Palette.topText());
        title.setShadowLayer(d ? 16 : 0, 0, 4, 0xAA000000);
        desc.setShadowLayer(d ? 8 : 0, 0, 2, 0x99000000);
        status.dark = d || Palette.dark;
        status.invalidate();
    }

    /** Fade and scale the new content in when the selection changes. */
    private void enter(Object item) {
        if (item == shown) return;
        shown = item;
        for (View v : new View[]{hero, title, chips, desc}) {
            v.animate().cancel();
            v.setAlpha(0f);
        }
        hero.setScaleX(0.93f);
        hero.setScaleY(0.93f);
        hero.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(320).setInterpolator(new DecelerateInterpolator(2f)).start();
        title.setTranslationY(16);
        title.animate().alpha(1f).translationY(0).setDuration(260).setStartDelay(40).start();
        chips.animate().alpha(1f).setDuration(260).setStartDelay(90).start();
        desc.animate().alpha(1f).setDuration(300).setStartDelay(130).start();
    }

    void showMessage(String m) {
        shown = null;
        setDark(false);
        hero.clear();
        title.setText("");
        chips.set(false);
        desc.setText("");
        hints.set(false);
        message.setText(m);
        message.setVisibility(VISIBLE);
        requestLayout();
    }

    void showPlatform(Library.Platform p, String emu, boolean recent) {
        message.setVisibility(GONE);
        setDark(false);
        enter(p);
        if (recent) {
            refreshFan(p);
            dots.set(0xFFF2A93B, t(S.RECENT), null);
        } else {
            hero.setDrawable(ConsoleDrawable.of(p, true));
            dots.set(Library.platformColor(p.type()), p.name, Theme.background(p));
        }
        boolean native_ = p.system.equals("ANDROID") || p.system.equals("APPS");
        title.setText(recent ? t(S.RECENT) : p.title());
        if (recent || native_ || emu == null) chips.set(Palette.dark, I18n.f(S.GAMES_COUNT, p.games.size()));
        else chips.set(Palette.dark, I18n.f(S.GAMES_COUNT, p.games.size()), "▶ " + emu);
        desc.setText("");
        desc.setVisibility(GONE);
        if (recent || native_) hints.set(Palette.dark, "A|" + t(S.OPEN));
        else hints.set(Palette.dark, "A|" + t(S.OPEN), "Y|" + t(S.PLATFORM_EMULATOR));
        requestLayout();
    }

    /** The Settings or Search tile on the home page. */
    void showTool(Object which) {
        boolean settings = which == ShelfView.SETTINGS;
        message.setVisibility(GONE);
        setDark(false);
        enter(which);
        hero.setDrawable(new ConsoleDrawable(settings ? "gear" : "search", null, true));
        dots.set(settings ? 0xFF6D8299 : 0xFF2F8BFF, settings ? t(S.SETTINGS) : t(S.SEARCH), null);
        title.setText(settings ? t(S.SETTINGS) : t(S.SEARCH));
        if (settings) chips.set(Palette.dark, t(S.SETTINGS_HINT), I18n.name());
        else chips.set(Palette.dark, t(S.SEARCH_HINT));
        desc.setText("");
        desc.setVisibility(GONE);
        hints.set(Palette.dark, "A|" + t(S.OPEN));
        requestLayout();
    }

    /**
     * @param emu    emulator label, or null for Android games and apps
     * @param played play-time and last-played tags, empty when never played from here
     */
    void showGame(Library.Game g, String emu, String... played) {
        message.setVisibility(GONE);
        boolean fresh = shown != g;
        setDark(true);
        enter(g);
        title.setText(g.title);
        List<String> tags = new ArrayList<>();
        tags.add(g.platform.title());
        if (emu != null) tags.add("▶ " + emu);
        for (String s : played) if (s != null) tags.add(s);
        chips.set(true, tags.toArray(new String[0]));
        desc.setText(g.description.replace('\n', ' '));
        desc.setVisibility(g.description.isEmpty() ? GONE : VISIBLE);
        if (emu != null) hints.set(true, "A|" + t(S.PLAY), "Y|" + t(S.CHOOSE_EMULATOR));
        else hints.set(true, "A|" + t(S.OPEN));
        if (fresh) {
            bg.setImageDrawable(null);
            hero.clear();
        }
        refreshGameImages(g);
        requestLayout();
    }

    private void refreshFan(Library.Platform recent) {
        if (shown != recent) return;
        Bitmap[] tiles = new Bitmap[Math.min(5, recent.games.size())];
        for (int i = 0; i < tiles.length; i++) tiles[i] = icons.tile(recent.games.get(i), () -> refreshFan(recent));
        hero.setFan(tiles);
    }

    private void refreshGameImages(Library.Game g) {
        if (shown != g) return;
        Runnable again = () -> refreshGameImages(g);
        File box = g.asset("box_front"), back = g.asset("background"), logo = g.asset("logo");

        if (bg.getDrawable() == null) {
            Bitmap bb = icons.image(back != null ? back : box, 640, again);
            if (bb == null && back == null && box == null) bb = icons.romIcon(g, again);
            if (bb != null) {
                bg.setImageBitmap(bb);
                bg.setAlpha(0f);
                bg.animate().alpha(1f).setDuration(350).start();
            }
        }
        if (hero.mode() == HeroView.NONE) {
            if (logo != null) {
                Bitmap b = icons.image(logo, 1200, again);
                if (b != null) { hero.setBitmap(HeroView.LOGO, b); return; }
                if (!icons.failed("img:1200:" + logo.getPath())) return;
            }
            if (box != null) {
                Bitmap b = icons.image(box, 1000, again);
                if (b != null) { hero.setBitmap(HeroView.CARD, b); return; }
                if (!icons.failed("img:1000:" + box.getPath())) return;
            }
            Bitmap b = icons.romIcon(g, again);
            if (b != null) hero.setBitmap(HeroView.PIXEL, b);
            else if (icons.failed("icon:" + g.key())) hero.setDrawable(ConsoleDrawable.of(g.platform, true));
        }
    }

    /** Clock and battery, top right. */
    private static final class StatusView extends View {
        boolean dark;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();

        StatusView(Context c) { super(c); }

        @Override protected void onDraw(Canvas c) {
            float h = getHeight(), w = getWidth();
            int fg = dark ? 0xFFFFFFFF : 0xFF3A4654;
            Intent bat = getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int pct = 0;
            boolean charging = false;
            if (bat != null) {
                int lv = bat.getIntExtra(BatteryManager.EXTRA_LEVEL, 0), sc = bat.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                pct = lv * 100 / Math.max(1, sc);
                int st = bat.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL;
            }
            // battery body
            float bh = h * 0.5f, bw = bh * 2.1f, bx = w - bw - h * 0.08f, by = (h - bh) / 2;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(h * 0.05f);
            p.setColor(fg);
            r.set(bx, by, bx + bw, by + bh);
            c.drawRoundRect(r, bh * 0.25f, bh * 0.25f, p);
            p.setStyle(Paint.Style.FILL);
            r.set(bx + bw + h * 0.02f, by + bh * 0.3f, bx + bw + h * 0.08f, by + bh * 0.7f);
            c.drawRoundRect(r, h * 0.02f, h * 0.02f, p);
            float in = h * 0.07f;
            p.setColor(pct <= 15 && !charging ? 0xFFE5484D : charging ? 0xFF3FBF6F : fg);
            r.set(bx + in, by + in, bx + in + (bw - in * 2) * pct / 100f, by + bh - in);
            c.drawRoundRect(r, bh * 0.12f, bh * 0.12f, p);
            // text
            p.setColor(fg);
            p.setTextAlign(Paint.Align.RIGHT);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(h * 0.5f);
            String s = (charging ? "⚡" : "") + pct + "%";
            c.drawText(s, bx - h * 0.25f, h * 0.68f, p);
            float tw = p.measureText(s);
            p.setTextSize(h * 0.52f);
            String time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.forLanguageTag(I18n.code())).format(new Date());
            c.drawText(time, bx - h * 0.25f - tw - h * 0.5f, h * 0.68f, p);
        }
    }
}
