package com.duo.launcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-platform artwork. Lookup order: the user's own picture (Settings) -> bundled asset -> drawn fallback.
 * Kinds: "console" (the console picture) and "background" (art behind it on the upper screen).
 */
final class Theme {
    static final String CONSOLE = "console", BACKGROUND = "background";

    private static File dir;
    private static AssetManager assets;
    private static final Map<String, Bitmap> cache = new HashMap<>();
    private static final Map<String, Boolean> absent = new HashMap<>();

    static void init(Context c) {
        dir = new File(c.getFilesDir(), "theme");
        assets = c.getAssets();
    }

    private static File file(String platform, String kind) {
        return new File(dir, platform.replaceAll("[^A-Za-z0-9._-]", "_") + "/" + kind + ".png");
    }

    static boolean isCustom(String platform, String kind) { return file(platform, kind).isFile(); }

    static Bitmap console(Library.Platform p) { return get(p, CONSOLE, "consoles/" + p.type() + ".png"); }

    static Bitmap background(Library.Platform p) { return get(p, BACKGROUND, "backgrounds/" + p.type() + ".png"); }

    private static Bitmap get(Library.Platform p, String kind, String asset) {
        String key = p.name + "/" + kind;
        Bitmap b = cache.get(key);
        if (b != null || absent.containsKey(key)) return b;
        File f = file(p.name, kind);
        if (f.isFile()) b = BitmapFactory.decodeFile(f.getPath());
        if (b == null) {
            try (InputStream in = assets.open(asset)) {
                b = BitmapFactory.decodeStream(in);
            } catch (IOException ignored) {
            }
        }
        if (b != null) cache.put(key, b); else absent.put(key, true);
        return b;
    }

    /** Stores a user picture, scaled down so the longest side is at most 1600px. */
    static boolean save(String platform, String kind, InputStream in) throws IOException {
        Bitmap b = BitmapFactory.decodeStream(in);
        if (b == null) return false;
        float k = Math.min(1f, 1600f / Math.max(b.getWidth(), b.getHeight()));
        if (k < 1f) b = Bitmap.createScaledBitmap(b, Math.round(b.getWidth() * k), Math.round(b.getHeight() * k), true);
        File f = file(platform, kind);
        f.getParentFile().mkdirs();
        try (FileOutputStream o = new FileOutputStream(f)) {
            b.compress(Bitmap.CompressFormat.PNG, 100, o);
        }
        forget(platform, kind);
        return true;
    }

    static void reset(String platform, String kind) {
        file(platform, kind).delete();
        forget(platform, kind);
    }

    private static void forget(String platform, String kind) {
        cache.remove(platform + "/" + kind);
        absent.remove(platform + "/" + kind);
    }
}
