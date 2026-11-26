package com.duo.launcher;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The game library in a browser (Wi-Fi transfer → Game library): every game with its pictures, play time and
 * status, in three looks (shelf, store, museum), and the player's edits – names, genres, status, rating, notes,
 * pictures – written to the same places the handheld uses. Only after the password, like the settings.
 */
final class WebLibrary {

    /** What the library page needs from the launcher. */
    interface Host {
        Library library();
        SharedPreferences prefs();
        /** Runs on the main thread and waits for it; false when it did not finish in time. */
        boolean onMain(Runnable r);
        /** Something changed: refresh the screens; with rescan, look at the pictures again. */
        void changed(boolean rescan);
        /** A game's picture was replaced: forget the old one that is still on screen. */
        void pictureChanged(Library.Game g, File dir);
        File mediaDir(Library.Game g);
        File cacheDir();
        String statusName(String id);
        /** The page itself (assets/web/library.html). */
        String pageHtml();
        /** A file from the APK's assets, or null. */
        byte[] asset(String path);
    }

    static final String[] STATUSES = {"playing", "wish", "done", "dropped"};
    static final String[] KINDS = {"box_front", "background", "logo"};

    private final Host host;
    /**
     * Each game's entry, kept for the library it was made from: looking up three pictures for two thousand games
     * takes seconds on a card, so it is done once (and again only for a game that is edited, or after a rescan).
     */
    private final Map<String, Integer> pics = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile Library picsOf;

    WebLibrary(Host host) { this.host = host; }

    /** Which pictures a game has (1 cover, 2 backdrop, 4 logo); everything else is read fresh each time. */
    private int pictures(Library.Game g) {
        Integer f = pics.get(g.key());
        if (f == null) {
            f = (g.asset("box_front") != null ? 1 : 0) | (g.asset("background") != null ? 2 : 0) | (g.asset("logo") != null ? 4 : 0);
            pics.put(g.key(), f);
        }
        return f;
    }

    /** Called from a background thread right after the page is switched on, so the first visit is quick. */
    void warmUp() { libraryJson(); }

    // ---- the page and the data ----

    /** The page with the interface words of the browser's language built in. */
    String page(int lang) {
        JSONObject t = new JSONObject();
        try {
            for (I18n.S s : I18n.S.values())
                if (s.name().startsWith("L_") || s.name().startsWith("REPORT_") || s.name().startsWith("STATUS_"))
                    t.put(s.name(), I18n.t(s, lang));
            t.put("TOTAL_PLAYED", I18n.t(I18n.S.TOTAL_PLAYED, lang));
            t.put("NOT_PLAYED", I18n.t(I18n.S.NOT_PLAYED, lang));
            t.put("GENRE", I18n.t(I18n.S.GENRE, lang));
            t.put("STATUS", I18n.t(I18n.S.STATUS, lang));
            t.put("NOTE", I18n.t(I18n.S.NOTE, lang));
            t.put("LAST_TIME", I18n.t(I18n.S.LAST_TIME, lang));
            t.put("SEARCH", I18n.t(I18n.S.SEARCH, lang));
        } catch (Exception ignored) { }
        String boot = "window.BOOT={lang:" + JSONObject.quote(I18n.CODES[Math.max(0, lang)]) + ",T:" + t + "};";
        return host.pageHtml().replace("/*BOOT*/", boot);
    }

    String libraryJson() {
        Library lib = host.library();
        SharedPreferences prefs = host.prefs();
        if (lib != picsOf) { pics.clear(); picsOf = lib; }
        // the lists are copied on the main thread, which changes them (custom folders, rescans)
        // copied into lists of their own on the main thread, and taken only once the copy is complete
        java.util.concurrent.atomic.AtomicReference<Object[]> copy = new java.util.concurrent.atomic.AtomicReference<>();
        if (lib != null && !host.onMain(() -> {
            List<Library.Platform> a = new ArrayList<>();
            List<List<Library.Game>> b = new ArrayList<>();
            for (Library.Platform p : lib.platforms) { a.add(p); b.add(new ArrayList<>(p.games)); }
            copy.set(new Object[]{a, b});
        })) return null;   // the handheld is busy: the page asks again
        @SuppressWarnings("unchecked") List<Library.Platform> ps = copy.get() == null ? new ArrayList<>() : (List<Library.Platform>) copy.get()[0];
        @SuppressWarnings("unchecked") List<List<Library.Game>> gs = copy.get() == null ? new ArrayList<>() : (List<List<Library.Game>>) copy.get()[1];
        JSONObject root = new JSONObject();
        try {
            JSONArray games = new JSONArray(), platforms = new JSONArray(), genres = new JSONArray(), statuses = new JSONArray();
            Set<Library.Game> seen = new HashSet<>();
            for (int i = 0; i < Math.min(ps.size(), gs.size()); i++) {
                Library.Platform p = ps.get(i);
                List<Library.Game> list = gs.get(i);
                {
                    if (p.isCustom() || list.isEmpty() || "APPS".equals(p.system) || "ANDROID".equals(p.system)) continue;
                    int n = 0;
                    for (Library.Game g : list) {
                        if (g.pkg != null || !seen.add(g)) continue;
                        games.put(game(g, prefs));
                        n++;
                    }
                    if (n == 0) continue;
                    platforms.put(new JSONObject().put("name", p.name).put("title", p.title()).put("color", hex(p.accent())).put("count", n)
                            .put("type", p.type()).put("bg", p.backdropType()));
                }
            }
            for (String id : Genres.IDS) genres.put(new JSONObject().put("id", id).put("name", Genres.name(id)));
            for (String s : STATUSES) statuses.put(new JSONObject().put("id", s).put("name", host.statusName(s)));
            root.put("games", games).put("platforms", platforms).put("genres", genres).put("statuses", statuses);
        } catch (Exception ignored) { }
        return root.toString();
    }

    private JSONObject game(Library.Game g, SharedPreferences prefs) throws Exception {
        String k = g.key();
        JSONObject o = new JSONObject();
        o.put("k", k).put("t", g.title).put("p", g.platform.name).put("pt", g.platform.title()).put("pc", hex(g.platform.accent()));
        if (g.original != null && !g.original.isEmpty() && !g.original.equals(g.title)) o.put("o", g.original);
        if (g.genre != null) o.put("g", g.genre);
        if (g.series != null && !Series.NONE.equals(g.series)) o.put("s", I18n.code().startsWith("zh") && g.seriesZh != null ? g.seriesZh : g.series);
        String st = prefs.getString("status:" + k, null);
        if (st != null) o.put("st", st);
        int r = prefs.getInt("rating:" + k, 0);
        if (r > 0) o.put("r", r);
        String note = prefs.getString("note:" + k, null);
        if (note != null) o.put("n", note);
        long ms = prefs.getLong("time:" + k, 0), last = prefs.getLong("last:" + k, 0);
        if (ms > 0) o.put("m", ms / 60_000);
        if (last > 0) o.put("l", last);
        if (g.added > 0) o.put("a", g.added);
        int pic = pictures(g);
        o.put("c", (pic & 1) != 0 ? 1 : 0);
        if ((pic & 2) != 0) o.put("b", 1);
        if ((pic & 4) != 0) o.put("lg", 1);
        // scanned games have no real developer, only their file name there
        String dev = g.developer == null ? "" : g.developer.trim();
        if (!dev.isEmpty() && !dev.equals(new File(g.baseName()).getName()) && !dev.equals(g.title)) o.put("dev", dev);
        return o;
    }

    String gameDetails(String key) {
        Library.Game g = find(key);
        JSONObject o = new JSONObject();
        if (g == null) return "{}";
        try {
            o.put("desc", g.description == null ? "" : g.description);
            o.put("file", g.path != null ? g.path.getAbsolutePath() : "");
            o.put("size", g.path != null ? g.path.length() : 0);
            o.put("discs", g.discs.size() + 1);
        } catch (Exception ignored) { }
        return o.toString();
    }

    private Library.Game find(String key) {
        Library lib = host.library();
        return lib == null || key == null ? null : lib.byKey.get(key);
    }

    private static String hex(int c) { return String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF); }

    /** The launcher's own console icons and backdrops, for the Duo look ("consoles/gba.png", "backgrounds/dark/gba.jpg"). */
    byte[] art(String f) {
        if (f == null || !f.matches("(consoles|backgrounds(/dark)?)/[a-z0-9_-]+\\.(png|jpg)")) return null;
        return host.asset(f);
    }

    // ---- pictures: shrunk once, kept in the cache folder ----

    /** A game's picture at about {@code width} pixels, as JPEG (PNG for logos, which have transparency), or null. */
    byte[] picture(String key, String kind, int width) {
        Library.Game g = find(key);
        if (g == null || !java.util.Arrays.asList(KINDS).contains(kind)) return null;
        File src = g.asset(kind);
        if (src == null || !src.isFile()) return null;
        width = Math.max(64, Math.min(width, 1600));
        boolean png = kind.equals("logo");
        File dir = new File(host.cacheDir(), "web");
        dir.mkdirs();
        String id = Integer.toHexString((src.getAbsolutePath() + "|" + src.lastModified() + "|" + width).hashCode());
        File cached = new File(dir, id + (png ? ".png" : ".jpg"));
        try {
            if (cached.isFile()) return java.nio.file.Files.readAllBytes(cached.toPath());
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(src.getPath(), o);
            int sample = 1;
            while (o.outWidth / (sample * 2) >= width) sample *= 2;
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            Bitmap b = BitmapFactory.decodeFile(src.getPath(), o);
            if (b == null) return null;
            if (b.getWidth() > width) {
                Bitmap s = Bitmap.createScaledBitmap(b, width, Math.max(1, b.getHeight() * width / b.getWidth()), true);
                if (s != b) b.recycle();
                b = s;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            b.compress(png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 86, out);
            b.recycle();
            byte[] bytes = out.toByteArray();
            try (OutputStream f = new FileOutputStream(cached)) { f.write(bytes); }
            return bytes;
        } catch (IOException | OutOfMemoryError e) {
            return null;
        }
    }

    /** Saves an uploaded picture as the game's own cover, backdrop or logo (as PNG, in its media folder). */
    String savePicture(String key, String kind, InputStream in, long length) throws IOException {
        Library.Game g = find(key);
        if (g == null || !java.util.Arrays.asList(KINDS).contains(kind)) return "bad request";
        if (length <= 0 || length > 25L * 1024 * 1024) return "too large";
        byte[] data = new byte[(int) length];
        int got = 0;
        while (got < data.length) {
            int r = in.read(data, got, data.length - got);
            if (r < 0) break;
            got += r;
        }
        if (got < data.length) return "upload cut off";
        // a small file can still be a picture of 20000 × 20000 pixels: shrink while decoding
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, got, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return "not a picture";
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / o2.inSampleSize > 3000) o2.inSampleSize *= 2;
        Bitmap b;
        try { b = BitmapFactory.decodeByteArray(data, 0, got, o2); } catch (OutOfMemoryError e) { return "picture too large"; }
        if (b == null) return "not a picture";
        File dir = host.mediaDir(g);
        dir.mkdirs();
        String name = kind.equals("box_front") ? "cover.png" : kind.equals("background") ? "background.png" : "logo.png";
        File tmp = new File(dir, "." + name + ".part");
        try (OutputStream o = new FileOutputStream(tmp)) { b.compress(Bitmap.CompressFormat.PNG, 100, o); }
        b.recycle();
        File dst = new File(dir, name);
        if (dst.exists()) dst.delete();
        if (!tmp.renameTo(dst)) return "could not save";
        pics.remove(key);
        // the game looks in its own media folder from now on, without waiting for a rescan
        g.useOwnMedia(dir);
        // a cover put in by hand also takes the place of the icon inside the ROM on the tile
        if (kind.equals("box_front")) new File(dir, Library.COVER_ON_TILE).createNewFile();
        host.pictureChanged(g, dir);
        host.changed(true);
        return null;
    }

    // ---- edits ----

    /** Applies the fields that were sent (title, desc, genre, status, rating, note) to one or more games. */
    String edit(String[] keys, Map<String, String> f) {
        StringBuilder err = new StringBuilder();
        SharedPreferences prefs = host.prefs();
        synchronized (this) {
            SharedPreferences.Editor e = prefs.edit();
            for (String key : keys) {
                Library.Game g = find(key);
                if (g == null) { err.append("unknown game"); continue; }
                if (f.containsKey("genre")) {
                    String id = f.get("genre");
                    if (id == null || id.isEmpty()) Genres.set(g, null);
                    else if (java.util.Arrays.asList(Genres.IDS).contains(id)) Genres.set(g, id);
                }
                if (f.containsKey("status")) {
                    String st = f.get("status");
                    if (st == null || st.isEmpty() || !java.util.Arrays.asList(STATUSES).contains(st)) e.remove("status:" + key);
                    else e.putString("status:" + key, st);
                }
                if (f.containsKey("rating")) {
                    int r;
                    try { r = Integer.parseInt(f.get("rating")); } catch (NumberFormatException x) { r = 0; }
                    if (r <= 0 || r > 5) e.remove("rating:" + key); else e.putInt("rating:" + key, r);
                }
                if (f.containsKey("note")) {
                    String n = f.get("note") == null ? "" : f.get("note").trim();
                    if (n.isEmpty()) e.remove("note:" + key); else e.putString("note:" + key, n);
                }
                String title = f.get("title") == null ? null : f.get("title").replace('\n', ' ').trim();
                String desc = f.get("desc") == null ? null : f.get("desc").replace("\r", "").trim();
                if (keys.length == 1 && (title != null && !title.isEmpty() && !title.equals(g.title) || desc != null && !desc.equals(g.description))) {
                    if (!GameText.setOwn(g, I18n.code(), title != null && !title.isEmpty() ? title : null, desc)) err.append("could not save");
                    else {
                        if (title != null && !title.isEmpty()) g.title = title;
                        if (desc != null) g.description = desc;
                    }
                }
            }
            e.apply();
        }
        host.changed(false);
        return err.length() == 0 ? null : err.toString();
    }

    /** One game as JSON after an edit. */
    String gameJson(String key) {
        Library.Game g = find(key);
        try {
            if (g == null) return "{}";
            return game(g, host.prefs()).toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    static Map<String, String> form(byte[] body, int len) throws IOException {
        Map<String, String> m = new LinkedHashMap<>();
        for (String kv : new String(body, 0, len, StandardCharsets.UTF_8).split("&")) {
            int e = kv.indexOf('=');
            if (e > 0) m.put(java.net.URLDecoder.decode(kv.substring(0, e), "UTF-8"), java.net.URLDecoder.decode(kv.substring(e + 1), "UTF-8"));
        }
        return m;
    }
}
