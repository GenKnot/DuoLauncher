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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A player's whole setup in one zip to keep or share: every setting (look, home layout, folders, hidden things,
 * emulator choices, AI services without their keys) plus their own pictures. Keys, passwords and the record of what
 * was played never go in, so a config can be handed to anyone; importing one keeps those of the person importing it.
 */
final class Config {
    private Config() { }

    static final String FILE = "duo-settings.json";

    /** Secrets and the player's own history: never exported, never overwritten by an import. */
    static boolean personal(String k) {
        if (k.endsWith(".key") || k.equals("admin.pw")) return true;
        for (String p : new String[]{"time:", "last:", "day:", "sessions:", "status:", "rating:", "note:", "howto:",
                "ra.", "update.", "running", "recent", "lastShot", "unbackedPlay", "searchHistory", "img.model.auto"})
            if (k.startsWith(p)) return true;
        return false;
    }

    /** Writes duo-config-&lt;date&gt;.zip into {@code dir}; {@code own} is the folder of the player's own pictures. */
    static File export(SharedPreferences prefs, File own, File dir) throws Exception {
        dir.mkdirs();
        JSONObject all = new JSONObject();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (personal(k) || v == null) continue;
            JSONObject o = new JSONObject();
            if (v instanceof Boolean) o.put("b", v);
            else if (v instanceof Integer) o.put("i", v);
            else if (v instanceof Long) o.put("l", v);
            else if (v instanceof Float) o.put("f", ((Float) v).doubleValue());
            else if (v instanceof Set) o.put("set", new JSONArray((Set<?>) v));
            else o.put("s", String.valueOf(v));
            all.put(k, o);
        }
        JSONObject root = new JSONObject().put("app", "Duo Launcher").put("version", 1).put("settings", all);
        String day = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date());
        File out = new File(dir, "duo-config-" + day + ".zip");
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(out))) {
            z.putNextEntry(new ZipEntry(FILE));
            z.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
            if (own != null && own.isDirectory()) addTree(z, own, "own/");
        }
        return out;
    }

    private static void addTree(ZipOutputStream z, File f, String path) throws IOException {
        File[] fs = f.listFiles();
        if (fs == null) return;
        for (File x : fs) {
            if (x.isDirectory()) { addTree(z, x, path + x.getName() + "/"); continue; }
            z.putNextEntry(new ZipEntry(path + x.getName()));
            try (InputStream in = new FileInputStream(x)) { Backups.copy(in, z); }
            z.closeEntry();
        }
    }

    /** Whether a zip is a config (it holds the settings file). */
    static boolean isConfig(File zip) {
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) if (e.getName().equals(FILE)) return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    /** Configs found in the given folders, newest first. */
    static List<File> find(List<File> dirs) {
        List<File> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (File d : dirs) {
            File[] fs = d == null ? null : d.listFiles((x, n) -> n.toLowerCase(Locale.ROOT).endsWith(".zip"));
            if (fs == null) continue;
            for (File f : fs) if (seen.add(f.getAbsolutePath()) && f.length() < 200L * 1024 * 1024 && isConfig(f)) out.add(f);
        }
        out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return out;
    }

    /**
     * Applies a config: its settings replace the current ones (keys, passwords and play history stay as they are),
     * and its pictures go into {@code own}. Returns how many settings were taken over.
     */
    static int apply(File zip, SharedPreferences prefs, File own) throws Exception {
        JSONObject settings = null;
        long total = 0;
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                String name = e.getName();
                if (name.contains("..") || name.startsWith("/") || name.contains("\\")) continue;
                if (name.equals(FILE)) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    Backups.copy(in, b);
                    settings = new JSONObject(b.toString("UTF-8")).optJSONObject("settings");
                } else if (name.startsWith("own/") && !e.isDirectory() && own != null) {
                    File f = new File(own, name.substring(4));
                    if (!f.getCanonicalPath().startsWith(own.getCanonicalPath() + File.separator)) continue;
                    f.getParentFile().mkdirs();
                    try (OutputStream o = new FileOutputStream(f)) {
                        byte[] buf = new byte[65536];
                        for (int n; (n = in.read(buf)) > 0; ) {
                            total += n;
                            if (total > 300L * 1024 * 1024) throw new IOException("too large");
                            o.write(buf, 0, n);
                        }
                    }
                }
            }
        }
        if (settings == null) throw new IOException("not a Duo Launcher config");
        SharedPreferences.Editor ed = prefs.edit();
        // settings the config does not have go back to their defaults, except the personal ones
        for (String k : prefs.getAll().keySet())
            if (!personal(k) && !settings.has(k) && !k.equals("ai.url") && !k.equals("ai.provider") && !k.equals("img.url")) ed.remove(k);
        int n = 0;
        // a key of this handheld must never be sent to an address that came from someone else's config
        boolean aiKey = !prefs.getString("ai.key", "").isEmpty(), imgKey = !prefs.getString("img.key", "").isEmpty();
        for (java.util.Iterator<String> it = settings.keys(); it.hasNext(); ) {
            String k = it.next();
            if (personal(k)) continue;
            if (aiKey && (k.equals("ai.url") || k.equals("ai.provider"))) continue;
            if (imgKey && k.equals("img.url")) continue;
            JSONObject o = settings.getJSONObject(k);
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
            n++;
        }
        ed.commit();
        return n;
    }
}
