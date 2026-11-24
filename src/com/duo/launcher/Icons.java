package com.duo.launcher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Async bitmap loading: icons read straight from NDS / 3DS ROMs, Pegasus artwork as fallback. */
final class Icons {
    private final File cacheDir;
    private final LruCache<String, Bitmap> mem;
    private final Set<String> pending = new HashSet<>(), missing = new HashSet<>();
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Handler main = new Handler(Looper.getMainLooper());

    private final PackageManager pm;

    Icons(Context c) {
        pm = c.getPackageManager();
        cacheDir = new File(c.getCacheDir(), "icons");
        cacheDir.mkdirs();
        mem = new LruCache<String, Bitmap>(128 * 1024 * 1024) {
            @Override protected int sizeOf(String k, Bitmap b) { return b.getAllocationByteCount(); }
        };
    }

    /** Drops the pictures held in memory (a game is starting); they come back from the disk cache when needed. */
    void trim() { mem.evictAll(); }

    /** Returns the bitmap if ready; otherwise starts loading and calls {@code done} on the main thread. */
    Bitmap get(String key, Callable<Bitmap> loader, Runnable done) {
        Bitmap b = mem.get(key);
        if (b != null || missing.contains(key)) return b;
        if (pending.add(key)) {
            pool.execute(() -> {
                Bitmap r = null;
                try { r = loader.call(); } catch (Throwable ignored) { }
                final Bitmap fr = r;
                main.post(() -> {
                    pending.remove(key);
                    if (fr != null) mem.put(key, fr); else missing.add(key);
                    if (done != null) done.run();
                });
            });
        }
        return null;
    }

    /** True once a load for this key finished without a bitmap. */
    boolean failed(String key) { return missing.contains(key); }

    Bitmap tile(Library.Game g, Runnable done) { return get("tile:" + g.key(), () -> loadTile(g), done); }

    /** The game's tile picture, loaded right here if it is not in memory yet (for a background thread). */
    Bitmap tileNow(Library.Game g) {
        Bitmap b = mem.get("tile:" + g.key());
        if (b != null) return b;
        try { return loadTile(g); } catch (Throwable e) { return null; }
    }

    Bitmap romIcon(Library.Game g, Runnable done) { return get("icon:" + g.key(), () -> romIconSync(g), done); }

    Bitmap image(File f, int maxPx, Runnable done) {
        if (f == null) return null;
        return get("img:" + maxPx + ":" + f.getPath(), () -> decode(f, maxPx), done);
    }

    private Bitmap appIcon(String pkg) {
        try {
            Drawable d = pm.getApplicationIcon(pkg);
            Bitmap b = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, 192, 192);
            d.draw(c);
            return b;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private Bitmap loadTile(Library.Game g) {
        if (g.pkg != null) return appIcon(g.pkg);
        Bitmap b = romIconSync(g);
        if (b != null) return b;
        File f = g.asset("box_front");
        if (f == null) f = g.asset("background");
        if (f != null) {
            Bitmap s = decode(f, 360);
            if (s != null) return centerCropSquare(trimBorder(s), 256);
        }
        return null;
    }

    Bitmap romIconSync(Library.Game g) {
        if (g.pkg != null) return appIcon(g.pkg);
        if (g.path == null) return null;
        String id = Integer.toHexString(g.path.getAbsolutePath().hashCode()) + "_" + g.path.length();
        File cf = new File(cacheDir, id + ".png"), none = new File(cacheDir, id + ".none");
        Bitmap raw = null;
        if (cf.isFile()) {
            raw = BitmapFactory.decodeFile(cf.getPath());
        } else if (!none.exists()) {
            try {
                String e = g.ext();
                if (e.equals("nds")) raw = nds(g.path);
                else if (e.equals("3ds") || e.equals("cci") || e.equals("cxi") || e.equals("app")) raw = ctr(g.path);
            } catch (Throwable ignored) { }
            try {
                if (raw != null) {
                    try (FileOutputStream o = new FileOutputStream(cf)) { raw.compress(Bitmap.CompressFormat.PNG, 100, o); }
                } else {
                    none.createNewFile();
                }
            } catch (IOException ignored) { }
        }
        if (raw == null) return null;
        // NDS icons are 32px pixel art: scale with nearest-neighbour; 3DS icons are 48px.
        boolean pixel = raw.getWidth() <= 32;
        return Bitmap.createScaledBitmap(raw, 192, 192, !pixel);
    }

    static Bitmap decode(File f, int maxPx) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), o);
        if (o.outWidth <= 0) return null;
        int s = 1;
        while (Math.max(o.outWidth, o.outHeight) / (s * 2) >= maxPx) s *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = s;
        return BitmapFactory.decodeFile(f.getPath(), o);
    }

    /**
     * Cuts off a plain white (or transparent) frame baked into a cover picture, at most a quarter from each side, so
     * that every cover can fill its tile the same way.
     */
    static Bitmap trimBorder(Bitmap s) {
        int w = s.getWidth(), h = s.getHeight();
        int top = 0, bottom = h - 1, left = 0, right = w - 1;
        while (top < h / 4 && plainRow(s, top)) top++;
        while (bottom > h * 3 / 4 && plainRow(s, bottom)) bottom--;
        while (left < w / 4 && plainCol(s, left, top, bottom)) left++;
        while (right > w * 3 / 4 && plainCol(s, right, top, bottom)) right--;
        if (top == 0 && left == 0 && bottom == h - 1 && right == w - 1) return s;
        return Bitmap.createBitmap(s, left, top, right - left + 1, bottom - top + 1);
    }

    private static boolean blank(int c) {
        if ((c >>> 24) < 16) return true;
        return ((c >> 16) & 255) > 236 && ((c >> 8) & 255) > 236 && (c & 255) > 236;
    }

    private static boolean plainRow(Bitmap s, int y) {
        int w = s.getWidth(), step = Math.max(1, w / 48);
        for (int x = 0; x < w; x += step) if (!blank(s.getPixel(x, y))) return false;
        return true;
    }

    private static boolean plainCol(Bitmap s, int x, int y0, int y1) {
        int step = Math.max(1, (y1 - y0) / 48);
        for (int y = y0; y <= y1; y += step) if (!blank(s.getPixel(x, y))) return false;
        return true;
    }

    static Bitmap centerCropSquare(Bitmap s, int size) {
        int m = Math.min(s.getWidth(), s.getHeight());
        Bitmap c = Bitmap.createBitmap(s, (s.getWidth() - m) / 2, (s.getHeight() - m) / 2, m, m);
        return Bitmap.createScaledBitmap(c, size, size, true);
    }

    // ---- ROM formats ----

    private static int le16(byte[] b, int o) { return (b[o] & 0xFF) | (b[o + 1] & 0xFF) << 8; }

    private static long le32(byte[] b, int o) {
        return ((b[o] & 0xFF) | (b[o + 1] & 0xFF) << 8 | (b[o + 2] & 0xFF) << 16 | (long) (b[o + 3] & 0xFF) << 24);
    }

    private static boolean magic(byte[] b, int o, String m) {
        for (int i = 0; i < m.length(); i++) if (b[o + i] != m.charAt(i)) return false;
        return true;
    }

    private static int c5(int v) { return (v << 3) | (v >> 2); }

    /** NDS banner icon: 32x32, 4bpp in 8x8 tiles, RGB555 palette with index 0 transparent. */
    static Bitmap nds(File f) throws IOException {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            byte[] h = new byte[0x70];
            r.readFully(h);
            long off = le32(h, 0x68);
            if (off == 0 || off + 0x240 > r.length()) return null;
            byte[] b = new byte[0x240];
            r.seek(off);
            r.readFully(b);
            int[] pal = new int[16];
            for (int i = 1; i < 16; i++) {
                int c = le16(b, 0x220 + i * 2);
                pal[i] = 0xFF000000 | c5(c & 31) << 16 | c5((c >> 5) & 31) << 8 | c5((c >> 10) & 31);
            }
            int[] px = new int[32 * 32];
            for (int ty = 0; ty < 4; ty++)
                for (int tx = 0; tx < 4; tx++)
                    for (int y = 0; y < 8; y++)
                        for (int x = 0; x < 8; x++) {
                            int v = b[0x20 + (ty * 4 + tx) * 32 + y * 4 + x / 2] & 0xFF;
                            px[(ty * 8 + y) * 32 + tx * 8 + x] = pal[(x & 1) == 0 ? v & 15 : v >> 4];
                        }
            return Bitmap.createBitmap(px, 32, 32, Bitmap.Config.ARGB_8888);
        }
    }

    /** SMDH title slots: 0 Japanese, 1 English, 6 Simplified Chinese, 11 Traditional Chinese, ... */
    private static int smdhLanguage() {
        switch (I18n.code()) {
            case "zh-CN": return 6;
            case "zh-TW": return 11;
            case "ja": return 0;
            case "fr": return 2;
            case "de": return 3;
            case "it": return 4;
            case "es": return 5;
            case "ko": return 7;
            case "pt": return 9;
            case "ru": return 10;
            default: return 1;
        }
    }

    /**
     * Program ID and display title of a decrypted 3DS image (NCSD or NCCH, e.g. an installed title's .app).
     * Returns null when the header is missing or encrypted; the title is null when there is no SMDH.
     */
    static Object[] ctrInfo(File f) {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            byte[] h = new byte[0x200];
            r.readFully(h);
            long ncch;
            if (magic(h, 0x100, "NCSD")) ncch = le32(h, 0x120) * 0x200;
            else if (magic(h, 0x100, "NCCH")) ncch = 0;
            else return null;
            byte[] n = new byte[0x200];
            r.seek(ncch);
            r.readFully(n);
            if (!magic(n, 0x100, "NCCH")) return null;
            long pid = (le32(n, 0x118) & 0xFFFFFFFFL) | (le32(n, 0x11C) & 0xFFFFFFFFL) << 32;
            if ((n[0x18F] & 0x04) == 0) return new Object[]{pid, null};
            long exefs = ncch + le32(n, 0x1A0) * 0x200;
            byte[] e = new byte[0x200];
            r.seek(exefs);
            r.readFully(e);
            for (int i = 0; i < 10; i++) {
                if (!new String(e, i * 16, 8, StandardCharsets.US_ASCII).replace("\0", "").equals("icon")) continue;
                byte[] s = new byte[0x2008];
                r.seek(exefs + 0x200 + le32(e, i * 16 + 8));
                r.readFully(s);
                if (!magic(s, 0, "SMDH")) break;
                // preferred language first, then any filled slot
                int want = smdhLanguage();
                String title = null;
                for (int k = -1; k < 16 && title == null; k++) {
                    int lang = k < 0 ? want : k;
                    String t = new String(s, 8 + lang * 0x200, 0x80, StandardCharsets.UTF_16LE);
                    int z = t.indexOf('\0');
                    t = (z < 0 ? t : t.substring(0, z)).trim();
                    if (!t.isEmpty()) title = t;
                }
                return new Object[]{pid, title};
            }
            return new Object[]{pid, null};
        } catch (IOException e) {
            return null;
        }
    }

    /** 3DS (.cci/.3ds NCSD or bare NCCH): SMDH large icon from the ExeFS, decrypted ROMs only. */
    static Bitmap ctr(File f) throws IOException {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            byte[] h = new byte[0x200];
            r.readFully(h);
            long ncch;
            if (magic(h, 0x100, "NCSD")) ncch = le32(h, 0x120) * 0x200;
            else if (magic(h, 0x100, "NCCH")) ncch = 0;
            else return null;
            byte[] n = new byte[0x200];
            r.seek(ncch);
            r.readFully(n);
            if (!magic(n, 0x100, "NCCH") || (n[0x18F] & 0x04) == 0) return null; // missing or encrypted
            long exefs = ncch + le32(n, 0x1A0) * 0x200;
            byte[] e = new byte[0x200];
            r.seek(exefs);
            r.readFully(e);
            for (int i = 0; i < 10; i++) {
                String name = new String(e, i * 16, 8, StandardCharsets.US_ASCII).replace("\0", "");
                if (!name.equals("icon")) continue;
                if (le32(e, i * 16 + 12) < 0x36C0) return null;
                byte[] s = new byte[0x36C0];
                r.seek(exefs + 0x200 + le32(e, i * 16 + 8));
                r.readFully(s);
                if (!magic(s, 0, "SMDH")) return null;
                int[] px = new int[48 * 48];
                for (int t = 0; t < 36; t++) {
                    int tx = t % 6, ty = t / 6;
                    for (int k = 0; k < 64; k++) {
                        int x = (k & 1) | ((k >> 1) & 2) | ((k >> 2) & 4);
                        int y = ((k >> 1) & 1) | ((k >> 2) & 2) | ((k >> 3) & 4);
                        int c = le16(s, 0x24C0 + (t * 64 + k) * 2);
                        int g6 = (c >> 5) & 63;
                        px[(ty * 8 + y) * 48 + tx * 8 + x] =
                                0xFF000000 | c5((c >> 11) & 31) << 16 | ((g6 << 2) | (g6 >> 4)) << 8 | c5(c & 31);
                    }
                }
                return Bitmap.createBitmap(px, 48, 48, Bitmap.Config.ARGB_8888);
            }
            return null;
        }
    }
}
