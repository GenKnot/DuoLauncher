package com.duo.launcher;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A player's setup in one zip, to keep or to share. What goes in is picked from six parts: the settings with the
 * player's own pictures (what a shared config has always held), the play record, statuses / ratings / notes, keys and
 * accounts, the AI record (usage and chats), and the launcher's game data from the DuoLauncher folders. "Share with
 * others" takes only the settings; "full backup" takes everything, so importing it gives back the same launcher.
 * Configs made by older versions (settings and pictures only) still import, and older versions read the settings
 * of a new config and ignore the rest.
 */
final class Config {
    private Config() { }

    static final String FILE = "duo-settings.json", PERSONAL = "duo-personal.json";

    enum Part { SETTINGS, PLAY, NOTES, KEYS, AI, CARD }

    /** Shared with others: no keys and nothing personal. */
    static final Set<Part> SHARE = EnumSet.of(Part.SETTINGS);
    static final Set<Part> FULL = EnumSet.allOf(Part.class);

    /** Which part a setting belongs to; null for bookkeeping that never leaves the handheld. */
    static Part part(String k) {
        if (k.endsWith(".key") || k.equals("admin.pw") || k.startsWith("ra.")) return Part.KEYS;
        for (String p : new String[]{"time:", "last:", "day:", "sessions:", "recent", "lastShot", "unbackedPlay"})
            if (k.startsWith(p)) return Part.PLAY;
        for (String p : new String[]{"status:", "rating:", "note:", "howto:", "searchHistory", "order:", "sysOf:"})
            if (k.startsWith(p)) return Part.NOTES;
        // where this handheld keeps its data and how it copies it are its own, never taken from someone's config
        if (k.startsWith("update.") || k.startsWith("copy.") || k.equals("dataHome") || k.equals("running") || k.equals("img.model.auto")) return null;
        return Part.SETTINGS;
    }

    /** Secrets and the player's own history: not part of the shared settings. */
    static boolean personal(String k) { return part(k) != Part.SETTINGS; }

    /** The app's own files of each part, by name in its files folder (a name ending in / is a folder). */
    private static Part filePart(String name) {
        if (name.equals("firstseen.txt") || name.equals("seennew.txt")) return Part.PLAY;
        if (name.equals("usage.json") || name.equals("translations.tsv") || name.startsWith("chats/")) return Part.AI;
        return null;
    }

    private static final String[] APP_FILES = {"firstseen.txt", "seennew.txt", "usage.json", "translations.tsv", "chats"};
    /** Folders inside DuoLauncher/ that are not game data (they have their own export, or are made again). */
    private static final Set<String> NOT_GAME_DATA = new HashSet<>(Arrays.asList("themes", "backups", "exports", "m3u", "profile"));

    /** The game data in the DuoLauncher folders: every folder of a console, and the cheat lists. */
    static List<File> gameData(List<File> launcherDirs) {
        List<File> out = new ArrayList<>();
        for (File d : launcherDirs) {
            File[] fs = d.listFiles(File::isDirectory);
            if (fs != null) for (File f : fs) if (!NOT_GAME_DATA.contains(f.getName().toLowerCase(Locale.ROOT))) out.add(f);
        }
        return out;
    }

    private static boolean onInternal(File f) {
        return f.getAbsolutePath().startsWith(android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    static long size(File f) {
        if (f.isFile()) return f.length();
        long n = 0;
        File[] fs = f.listFiles();
        if (fs != null) for (File x : fs) n += size(x);
        return n;
    }

    /**
     * Writes duo-config-&lt;date&gt;.zip (or duo-backup-&lt;date&gt;.zip when it holds personal parts) into {@code dir}.
     * {@code own} is the folder of the player's pictures, {@code files} the app's files folder, {@code launcherDirs}
     * the DuoLauncher folders on the storages.
     */
    static File export(SharedPreferences prefs, File own, File files, List<File> launcherDirs, Set<Part> parts, File dir) throws Exception {
        dir.mkdirs();
        JSONObject settings = new JSONObject(), personal = new JSONObject();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            Part p = part(k);
            if (v == null || p == null || !parts.contains(p)) continue;
            (p == Part.SETTINGS ? settings : personal).put(k, value(v));
        }
        JSONArray names = new JSONArray();
        for (Part p : parts) names.put(p.name());
        // "settings" stays where older versions look for it
        JSONObject root = new JSONObject().put("app", "Duo Launcher").put("version", 2).put("parts", names).put("settings", settings);
        String day = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date());
        boolean shareable = parts.equals(SHARE) || parts.isEmpty();
        File out = new File(dir, (shareable ? "duo-config-" : "duo-backup-") + day + ".zip");
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(out))) {
            z.putNextEntry(new ZipEntry(FILE));
            z.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
            if (personal.length() > 0) {
                z.putNextEntry(new ZipEntry(PERSONAL));
                z.write(personal.toString(2).getBytes(StandardCharsets.UTF_8));
                z.closeEntry();
            }
            if (parts.contains(Part.SETTINGS) && own != null && own.isDirectory()) addTree(z, own, "own/");
            for (String n : APP_FILES) {
                File f = new File(files, n);
                Part p = filePart(f.isDirectory() ? n + "/" : n);
                if (p == null || !parts.contains(p) || !f.exists()) continue;
                if (f.isDirectory()) addTree(z, f, "files/" + n + "/");
                else addFile(z, f, "files/" + n);
            }
            // game data remembers whether it was on internal storage (cheat lists) or on a card (console folders)
            if (parts.contains(Part.CARD))
                for (File d : gameData(launcherDirs)) addTree(z, d, "card/" + (onInternal(d) ? "int/" : "sd/") + d.getName() + "/");
        }
        return out;
    }

    private static JSONObject value(Object v) throws Exception {
        JSONObject o = new JSONObject();
        if (v instanceof Boolean) o.put("b", v);
        else if (v instanceof Integer) o.put("i", v);
        else if (v instanceof Long) o.put("l", v);
        else if (v instanceof Float) o.put("f", ((Float) v).doubleValue());
        else if (v instanceof Set) o.put("set", new JSONArray((Set<?>) v));
        else o.put("s", String.valueOf(v));
        return o;
    }

    private static void put(SharedPreferences.Editor ed, String k, JSONObject o) throws Exception {
        if (o.has("b")) ed.putBoolean(k, o.getBoolean("b"));
        else if (o.has("i")) ed.putInt(k, o.getInt("i"));
        else if (o.has("l")) ed.putLong(k, o.getLong("l"));
        else if (o.has("f")) ed.putFloat(k, (float) o.getDouble("f"));
        else if (o.has("set")) {
            Set<String> s = new HashSet<>();
            JSONArray a = o.getJSONArray("set");
            for (int i = 0; i < a.length(); i++) s.add(a.getString(i));
            ed.putStringSet(k, s);
        } else ed.putString(k, o.optString("s", ""));
    }

    private static void addTree(ZipOutputStream z, File f, String path) throws IOException {
        File[] fs = f.listFiles();
        if (fs == null) return;
        for (File x : fs) {
            if (x.isDirectory()) addTree(z, x, path + x.getName() + "/");
            else addFile(z, x, path + x.getName());
        }
    }

    private static void addFile(ZipOutputStream z, File f, String name) throws IOException {
        z.putNextEntry(new ZipEntry(name));
        try (InputStream in = new FileInputStream(f)) { Backups.copy(in, z); }
        z.closeEntry();
    }

    /** Whether a zip is a config (it holds the settings file). */
    static boolean isConfig(File zip) {
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) if (e.getName().equals(FILE)) return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    /** The parts a config holds (a config from before parts existed holds the settings and pictures). */
    static Set<Part> partsIn(File zip) {
        Set<Part> out = EnumSet.noneOf(Part.class);
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                if (!e.getName().equals(FILE)) continue;
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                Backups.copy(in, b);
                JSONArray a = new JSONObject(b.toString("UTF-8")).optJSONArray("parts");
                if (a == null) out.add(Part.SETTINGS);
                for (int i = 0; a != null && i < a.length(); i++) {
                    try { out.add(Part.valueOf(a.getString(i))); } catch (IllegalArgumentException ignored) { }
                }
                break;
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    /** Configs found in the given folders, newest first. */
    static List<File> find(List<File> dirs) {
        List<File> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (File d : dirs) {
            File[] fs = d == null ? null : d.listFiles((x, n) -> n.toLowerCase(Locale.ROOT).endsWith(".zip"));
            if (fs == null) continue;
            for (File f : fs) if (seen.add(f.getAbsolutePath()) && isConfig(f)) out.add(f);
        }
        out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return out;
    }

    /** Copies the current zip entry to {@code f} if it lies inside {@code root}; counts what was written. */
    private static void extract(ZipInputStream in, File root, String rel, long[] total) throws IOException {
        File f = new File(root, rel);
        if (!f.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) return;
        f.getParentFile().mkdirs();
        try (OutputStream o = new FileOutputStream(f)) {
            byte[] buf = new byte[65536];
            for (int n; (n = in.read(buf)) > 0; ) {
                total[0] += n;
                if (total[0] > 8L * 1024 * 1024 * 1024) throw new IOException("too large");
                o.write(buf, 0, n);
            }
        }
    }

    /**
     * Takes over the chosen parts of a config: settings replace the current settings (keys and personal records stay
     * unless their own parts are taken too), personal records and files replace the ones of the same name, pictures
     * go into {@code own}, game data back where it was: the DuoLauncher folder on internal storage, or {@code cardRoot}
     * (the DuoLauncher folder on the card). Returns how many settings were set.
     */
    static int apply(File zip, SharedPreferences prefs, File own, File files, File cardRoot, Set<Part> want) throws Exception {
        File internalRoot = new File(android.os.Environment.getExternalStorageDirectory(), "DuoLauncher");
        JSONObject settings = null, personal = null;
        long[] total = {0};
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.contains("..") || name.startsWith("/") || name.contains("\\") || e.isDirectory()) continue;
                if (name.equals(FILE) || name.equals(PERSONAL)) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    Backups.copy(in, b);
                    JSONObject o = new JSONObject(b.toString("UTF-8"));
                    if (name.equals(FILE)) settings = o.optJSONObject("settings");
                    else personal = o;
                } else if (name.startsWith("own/") && own != null && want.contains(Part.SETTINGS)) {
                    extract(in, own, name.substring(4), total);
                } else if (name.startsWith("files/") && files != null) {
                    String rel = name.substring(6);
                    Part p = filePart(rel);
                    if (p != null && want.contains(p)) extract(in, files, rel, total);
                } else if (name.startsWith("card/") && want.contains(Part.CARD)) {
                    String rel = name.substring(5);
                    if (rel.startsWith("int/")) extract(in, internalRoot, rel.substring(4), total);
                    else if (rel.startsWith("sd/") && cardRoot != null) extract(in, cardRoot, rel.substring(3), total);
                }
            }
        }
        if (settings == null) throw new IOException("not a Duo Launcher config");
        SharedPreferences.Editor ed = prefs.edit();
        int n = 0;
        // a key of this handheld must never be sent to an address that came from someone else's config;
        // when the keys come along (one's own full backup), the addresses come with them
        boolean keysToo = want.contains(Part.KEYS) && personal != null && personal.length() > 0;
        if (want.contains(Part.SETTINGS)) {
            // settings the config does not have go back to their defaults, except the personal ones
            for (String k : prefs.getAll().keySet())
                if (!personal(k) && !settings.has(k) && !k.equals("ai.url") && !k.equals("ai.provider") && !k.equals("img.url")) ed.remove(k);
            boolean aiKey = !prefs.getString("ai.key", "").isEmpty() && !keysToo, imgKey = !prefs.getString("img.key", "").isEmpty() && !keysToo;
            for (java.util.Iterator<String> it = settings.keys(); it.hasNext(); ) {
                String k = it.next();
                if (personal(k)) continue;
                if (aiKey && (k.equals("ai.url") || k.equals("ai.provider"))) continue;
                if (imgKey && k.equals("img.url")) continue;
                put(ed, k, settings.getJSONObject(k));
                n++;
            }
        }
        if (personal != null) {
            for (java.util.Iterator<String> it = personal.keys(); it.hasNext(); ) {
                String k = it.next();
                Part p = part(k);
                if (p == null || p == Part.SETTINGS || !want.contains(p)) continue;
                put(ed, k, personal.getJSONObject(k));
                n++;
            }
        }
        ed.commit();
        return n;
    }
}
