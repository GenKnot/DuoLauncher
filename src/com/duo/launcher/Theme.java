package com.duo.launcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-platform artwork. Lookup order: the user's own picture (Settings) -> bundled asset -> drawn fallback.
 * Kinds: "console" (the console picture) and "background" (art behind it on the upper screen).
 */
final class Theme {
    static final String CONSOLE = "console", BACKGROUND = "background";

    /** The player's own pictures (console, folder and tool icons, backdrops). */
    static File dir;
    private static AssetManager assets;
    private static final Map<String, Bitmap> cache = new HashMap<>();
    private static final Map<String, Boolean> absent = new HashMap<>();

    // ---- theme packs: <volume>/DuoLauncher/themes/<folder>/theme.txt plus any of the bundled asset paths ----

    private static File pack;                                   // active theme folder, null = built-in
    private static final Map<String, String> props = new HashMap<>();
    private static Typeface font, fontBold;

    // ---- colour themes: a main colour and a background colour, every other shade worked out from them ----

    /** Built-in colour themes: {main colour, background colour}; names are I18n THEME_BLUE.. in the same order. */
    static final int[][] PRESETS = {
            {0xFF2F8BFF, 0xFF4F8FD8}, {0xFFE5484D, 0xFFD9605F}, {0xFF2BAE66, 0xFF4FA37A},
            {0xFF8B5CF6, 0xFF8A72C9}, {0xFFF08A24, 0xFFD99155}, {0xFFE8618C, 0xFFD9819D}, {0xFF5E6C7B, 0xFF8090A0}};
    /** Swatches offered for the custom colour theme. */
    static final int[] SWATCHES = {0xFF2F8BFF, 0xFF1FA2C9, 0xFF14B8A6, 0xFF2BAE66, 0xFF84CC16, 0xFFEAB308, 0xFFF08A24,
            0xFFE5484D, 0xFFE8618C, 0xFFD946EF, 0xFF8B5CF6, 0xFF6366F1, 0xFF5E6C7B, 0xFF3F3F46, 0xFFA16207};

    private static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + ((((b >> 16) & 255) - ((a >> 16) & 255)) * t));
        int g = (int) (((a >> 8) & 255) + ((((b >> 8) & 255) - ((a >> 8) & 255)) * t));
        int bl = (int) ((a & 255) + (((b & 255) - (a & 255)) * t));
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    private static void put(String key, int color) { props.put(key, String.format("#%08X", color)); }

    /** Fills every colour key of a full theme from a main and a background colour, for light and dark mode. */
    private static void derive(int accent, int base) {
        int white = 0xFFFFFFFF, black = 0xFF000000;
        put("color.accent", accent);
        put("color.shelf-top", mix(base, white, 0.93f));
        put("color.shelf-bottom", mix(base, white, 0.83f));
        put("color.card-soft", mix(base, white, 0.9f));
        put("color.pill-text", mix(base, black, 0.62f));
        put("color.text", mix(base, black, 0.78f));
        put("color.text2", mix(base, black, 0.45f));
        put("color.key", mix(base, black, 0.5f));
        put("color.footer-line", mix(base, white, 0.72f));
        put("color.top-bg", mix(base, white, 0.9f));
        put("color.top-title", mix(base, black, 0.75f));
        put("color.top-text", mix(base, black, 0.45f));
        put("color.backdrop-paper", mix(base, white, 0.9f));
        put("color.backdrop-ink", mix(base, black, 0.55f));
        put("dark.color.accent", mix(accent, white, 0.12f));
        put("dark.color.shelf-top", mix(base, black, 0.82f));
        put("dark.color.shelf-bottom", mix(base, black, 0.9f));
        put("dark.color.card", mix(base, black, 0.76f));
        put("dark.color.card-soft", mix(base, black, 0.7f));
        put("dark.color.pill", mix(base, black, 0.72f));
        put("dark.color.footer", mix(base, black, 0.87f));
        put("dark.color.footer-line", mix(base, black, 0.7f));
        put("dark.color.text", mix(base, white, 0.9f));
        put("dark.color.text2", mix(base, white, 0.6f));
        put("dark.color.pill-text", mix(base, white, 0.85f));
        put("dark.color.key", mix(base, white, 0.8f));
        put("dark.color.key-text", mix(base, black, 0.85f));
        put("dark.color.top-title", mix(base, white, 0.92f));
        put("dark.color.top-text", mix(base, white, 0.65f));
        put("dark.color.top-bg", mix(base, black, 0.88f));
        put("dark.color.backdrop-paper", mix(base, black, 0.84f));
        put("dark.color.backdrop-ink", mix(base, white, 0.82f));
    }

    static void init(Context c) {
        dir = new File(c.getFilesDir(), "theme");
        assets = c.getAssets();
        android.content.SharedPreferences prefs = c.getSharedPreferences("duo", Context.MODE_PRIVATE);
        String chosen = prefs.getString("theme", "");
        pack = null;
        for (File t : packs(c)) if (t.getName().equals(chosen)) pack = t;
        props.clear();
        if (chosen.startsWith("preset:")) {
            try {
                int[] pr = PRESETS[Integer.parseInt(chosen.substring(7))];
                derive(pr[0], pr[1]);
            } catch (RuntimeException ignored) {
            }
        } else if (chosen.equals("custom")) {
            derive(prefs.getInt("custom.accent", PRESETS[0][0]), prefs.getInt("custom.base", PRESETS[0][1]));
        }
        font = fontBold = null;
        if (pack != null) {
            File txt = new File(pack, "theme.txt");
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(txt), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    int k = line.indexOf(':');
                    if (line.startsWith("#") || k < 0) continue;
                    props.put(line.substring(0, k).trim().toLowerCase(java.util.Locale.ROOT), line.substring(k + 1).trim());
                }
                // a pack may give just "base-color: #RRGGBB" (and "color.accent"); the rest is worked out
                String base = props.get("base-color");
                if (base != null) {
                    Map<String, String> own = new HashMap<>(props);
                    int accent = color("accent", 0xFF2F8BFF);
                    try {
                        derive(accent, (int) Long.parseLong("FF" + base.replace("#", "").substring(0, 6), 16));
                    } catch (RuntimeException badColour) {
                        // "base-color: blue" or "#fff": keep the pack's other colours
                    }
                    props.putAll(own);
                }
            } catch (IOException ignored) {
            }
            File f = new File(pack, "font.ttf"), fb = new File(pack, "font-bold.ttf");
            try { if (f.isFile()) font = Typeface.createFromFile(f); } catch (RuntimeException ignored) { }
            try { if (fb.isFile()) fontBold = Typeface.createFromFile(fb); } catch (RuntimeException ignored) { }
        }
        cache.clear();
        absent.clear();
    }

    /**
     * Installed theme folders: every DuoLauncher/themes/<name>/ holding a theme.txt, on any volume.
     * A theme shared as <name>.zip in that folder is unpacked into <name>/ the first time it is seen.
     */
    static java.util.List<File> packs(Context c) {
        java.util.List<File> out = new java.util.ArrayList<>();
        android.os.storage.StorageManager sm = c.getSystemService(android.os.storage.StorageManager.class);
        for (android.os.storage.StorageVolume v : sm.getStorageVolumes()) {
            File themes = v.getDirectory() == null ? null : new File(v.getDirectory(), "DuoLauncher/themes");
            File[] zips = themes == null ? null : themes.listFiles((d, n) -> n.toLowerCase(java.util.Locale.ROOT).endsWith(".zip"));
            if (zips != null) for (File z : zips) {
                File dest = new File(themes, z.getName().substring(0, z.getName().length() - 4));
                if (dest.exists() || z.length() > 64L << 20) continue;
                File tmp = new File(themes, "." + dest.getName() + ".unpacking");
                deleteTree(tmp);
                if (unzip(z, tmp) && !tmp.renameTo(dest)) deleteTree(tmp);
                else deleteTree(tmp);
            }
            File[] fs = themes == null ? null : themes.listFiles();
            if (fs == null) continue;
            java.util.Arrays.sort(fs);
            for (File t : fs) if (new File(t, "theme.txt").isFile()) out.add(t);
        }
        return out;
    }

    static File packDir() { return pack; }

    /** Unpacks a theme zip; a zip holding a single top folder is flattened into {@code dest}. */
    private static void deleteTree(File f) {
        File[] fs = f.listFiles();
        if (fs != null) for (File x : fs) deleteTree(x);
        f.delete();
    }

    private static boolean unzip(File zip, File dest) {
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(zip)) {
            String root = dest.getCanonicalPath() + File.separator;
            String strip = null;
            java.util.List<? extends java.util.zip.ZipEntry> es = java.util.Collections.list(z.entries());
            for (java.util.zip.ZipEntry e : es) if (e.getName().endsWith("theme.txt")) strip = e.getName().substring(0, e.getName().length() - 9);
            if (strip == null) return false; // not a theme
            for (java.util.zip.ZipEntry e : es) {
                if (e.isDirectory() || !e.getName().startsWith(strip)) continue;
                File out = new File(dest, e.getName().substring(strip.length()));
                if (!out.getCanonicalPath().startsWith(root)) continue;
                out.getParentFile().mkdirs();
                try (InputStream in = z.getInputStream(e); java.io.OutputStream o = new FileOutputStream(out)) { Backups.copy(in, o); }
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Writes the active look as a shareable theme zip into {@code dir}: a theme pack is zipped as it is,
     * a colour theme becomes a theme.txt with its colours.
     */
    static File export(Context c, File dir) throws IOException {
        dir.mkdirs();
        String name = pack != null ? pack.getName() : "my-colours";
        File out = new File(dir, name + ".zip");
        try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(new FileOutputStream(out))) {
            // the user's own pictures (console and folder icons, backdrops, tool icons) go along under own/
            if (Theme.dir != null && Theme.dir.isDirectory()) addTree(z, Theme.dir, name + "/own/");
            if (pack != null) {
                addTree(z, pack, name + "/");
            } else {
                StringBuilder b = new StringBuilder("# Duo Launcher theme, exported from Settings\nname: My colours\n");
                java.util.List<String> keys = new java.util.ArrayList<>(props.keySet());
                java.util.Collections.sort(keys);
                for (String k : keys) b.append(k).append(": ").append(props.get(k)).append('\n');
                z.putNextEntry(new java.util.zip.ZipEntry(name + "/theme.txt"));
                z.write(b.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                z.closeEntry();
            }
        }
        return out;
    }

    private static void addTree(java.util.zip.ZipOutputStream z, File f, String path) throws IOException {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) for (File x : fs) addTree(z, x, path + (x.isDirectory() ? x.getName() + "/" : ""));
            return;
        }
        z.putNextEntry(new java.util.zip.ZipEntry(path + f.getName()));
        try (InputStream in = new java.io.FileInputStream(f)) { Backups.copy(in, z); }
        z.closeEntry();
    }

    /** Display name of a theme folder (its "name:" line, else the folder name). */
    static String packName(File t) {
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(new File(t, "theme.txt")), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) if (line.toLowerCase(java.util.Locale.ROOT).startsWith("name:")) return line.substring(5).trim();
        } catch (IOException ignored) {
        }
        return t.getName();
    }

    /**
     * A colour from the theme ("color.<key>: #RRGGBB" or "#AARRGGBB"; "dark.color.<key>" wins in dark mode),
     * or {@code def} when the theme does not set it.
     */
    static int color(String key, int def) {
        String v;
        if (Palette.dark) {
            // a light-mode text or background colour would be unreadable on dark, so only the accent and
            // folder colours carry over when the theme gives no dark value
            v = props.get("dark.color." + key);
            if (v == null && (key.equals("accent") || key.startsWith("folder."))) v = props.get("color." + key);
        } else {
            v = props.get("color." + key);
        }
        if (v == null) return def;
        try {
            int c = (int) Long.parseLong(v.replace("#", ""), 16);
            return v.replace("#", "").length() <= 6 ? 0xFF000000 | c : c;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    static Typeface font(boolean bold) {
        if (bold) return fontBold != null ? fontBold : font != null ? Typeface.create(font, Typeface.BOLD) : Typeface.DEFAULT_BOLD;
        return font != null ? font : Typeface.DEFAULT;
    }

    /** Opens a bundled asset, or the theme's file of the same path (also .jpg for .png and the other way round). */
    private static InputStream open(String path) throws IOException {
        if (pack != null) {
            String base = path.replaceAll("\\.(png|jpg)$", "");
            for (String p : new String[]{path, base + ".png", base + ".jpg", base + ".webp"}) {
                File f = new File(pack, p);
                if (f.isFile()) return new java.io.FileInputStream(f);
            }
        }
        return assets.open(path);
    }

    private static File file(String platform, String kind) {
        return new File(dir, platform.replaceAll("[^A-Za-z0-9._-]", "_") + "/" + kind + ".png");
    }

    static boolean isCustom(String platform, String kind) { return file(platform, kind).isFile(); }

    /** A bundled picture by path inside assets (e.g. "logo.png", "consoles/search.png"), cached. */
    static Bitmap asset(String path) {
        String key = "asset/" + path;
        Bitmap b = cache.get(key);
        if (b != null || absent.containsKey(key)) return b;
        try (InputStream in = open(path)) {
            b = BitmapFactory.decodeStream(in);
        } catch (IOException ignored) {
        }
        if (b != null) cache.put(key, b); else absent.put(key, true);
        return b;
    }

    /** Icon and backdrop of a home-page tool such as "search", "settings" or "recent". */
    static Bitmap toolIcon(String name) {
        // the user's own picture for a home tool ("tool-random"), else the theme's or the bundled one
        String key = "tool-" + name + "/" + CONSOLE;
        Bitmap b = cache.get(key);
        if (b != null) return b;
        if (!absent.containsKey(key)) {
            File f = file("tool-" + name, CONSOLE);
            if (f.isFile()) b = BitmapFactory.decodeFile(f.getPath());
            if (b != null) { cache.put(key, b); return b; }
            absent.put(key, true);
        }
        return asset("consoles/" + name + ".png");
    }

    /**
     * Backdrops come in two cuts: the light one may be shaded, while dark mode prefers a pure
     * line-art version under backgrounds/dark/ (shading turns into grey haze once inverted).
     */
    static Bitmap toolBackground(String name) { return backdrop("tool:" + name, name); }

    static Bitmap console(Library.Platform p) { return get(p, CONSOLE, "consoles/" + p.type() + ".png"); }

    static final String BACKGROUND_DARK = "background-dark";

    /** Built-in line-art backdrops get the theme's paper/ink colours; photos and own pictures are shown as they are. */
    private static final java.util.Set<Bitmap> tintable = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    static boolean isTintable(Bitmap b) { return b != null && tintable.contains(b); }

    /**
     * Upper-screen backdrop, first found wins. Dark mode: own dark picture, own picture, theme's
     * backgrounds/dark/, theme's backgrounds/, built-in. Light mode: own picture, theme's backgrounds/, built-in.
     */
    static Bitmap background(Library.Platform p) { return backdrop(p.name, p.backdropType()); }

    private static Bitmap backdrop(String owner, String t) {
        boolean dark = Palette.dark;
        String key = owner + "/" + (dark ? BACKGROUND_DARK : BACKGROUND);
        Bitmap b = cache.get(key);
        if (b != null || absent.containsKey(key)) return b;
        List<File> own = new ArrayList<>();
        if (!owner.startsWith("tool:")) {
            if (dark) own.add(file(owner, BACKGROUND_DARK));
            own.add(file(owner, BACKGROUND));
        }
        for (File f : own) if (b == null && f.isFile()) b = BitmapFactory.decodeFile(f.getPath());
        if (b == null && pack != null) {
            List<String> names = new ArrayList<>();
            if (dark) names.add("backgrounds/dark/" + t);
            names.add("backgrounds/" + t);
            for (String n : names) {
                if (b != null) break;
                for (String ext : new String[]{".jpg", ".png", ".webp"}) {
                    File f = new File(pack, n + ext);
                    if (f.isFile()) { b = BitmapFactory.decodeFile(f.getPath()); break; }
                }
            }
            if (b != null && "yes".equalsIgnoreCase(props.get("tint-backdrops"))) tintable.add(b);
        }
        if (b == null) {
            // built-in: dark mode has its own cut with the soft shading removed (it would turn into grey haze)
            String[] names = dark ? new String[]{"backgrounds/dark/" + t + ".jpg", "backgrounds/" + t + ".jpg"}
                    : new String[]{"backgrounds/" + t + ".jpg", "backgrounds/" + t + ".png"};
            for (String n : names) {
                try (InputStream in = assets.open(n)) {
                    b = BitmapFactory.decodeStream(in);
                } catch (IOException ignored) {
                }
                if (b != null) { tintable.add(b); break; }
            }
        }
        if (b != null) cache.put(key, b); else absent.put(key, true);
        return b;
    }

    /** Drops cached pictures of a platform, after its chosen artwork changed. */
    static void forgetPlatform(String platform) {
        forget(platform, CONSOLE);
        forget(platform, BACKGROUND);
        forget(platform, BACKGROUND_DARK);
    }

    /** The user's own picture for this kind, else the theme's or the built-in asset. */
    private static Bitmap get(Library.Platform p, String kind, String... assetNames) {
        String key = p.name + "/" + kind;
        Bitmap b = cache.get(key);
        if (b != null || absent.containsKey(key)) return b;
        File f = file(p.name, kind);
        if (f.isFile()) b = BitmapFactory.decodeFile(f.getPath());
        for (String asset : assetNames) {
            if (b != null) break;
            try (InputStream in = open(asset)) {
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
        // a new light picture can also be what dark mode shows, so drop both
        for (String k : new String[]{kind, BACKGROUND, BACKGROUND_DARK}) {
            cache.remove(platform + "/" + k);
            absent.remove(platform + "/" + k);
        }
    }
}
