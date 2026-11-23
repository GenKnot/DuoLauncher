package com.duo.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Saved AI conversations, one JSON file each in the app's own files folder (files/chats). They stay on the handheld:
 * the config export only copies settings and themes, never this folder. Settings → AI can turn saving off and clear them.
 */
final class ChatHistory {
    private ChatHistory() { }

    static final class Entry {
        final String id;
        final long updated;
        final String title;
        final int count;

        Entry(String id, long updated, String title, int count) {
            this.id = id;
            this.updated = updated;
            this.title = title;
            this.count = count;
        }
    }

    private static File dir(File filesDir) { return new File(filesDir, "chats"); }

    /** Writes (or rewrites) one conversation. */
    static void save(File filesDir, String id, List<ChatPanel.Msg> msgs) {
        if (id == null || msgs.isEmpty()) return;
        try {
            JSONArray arr = new JSONArray();
            for (ChatPanel.Msg m : msgs) {
                JSONArray games = new JSONArray();
                for (String g : m.games) games.put(g);
                arr.put(new JSONObject().put("me", m.me).put("text", m.text).put("games", games));
            }
            JSONObject o = new JSONObject().put("updated", System.currentTimeMillis()).put("msgs", arr);
            File d = dir(filesDir);
            d.mkdirs();
            File tmp = new File(d, id + ".tmp");
            try (OutputStream out = new FileOutputStream(tmp)) { out.write(o.toString().getBytes(StandardCharsets.UTF_8)); }
            if (!tmp.renameTo(new File(d, id + ".json"))) {
                new File(d, id + ".json").delete();
                tmp.renameTo(new File(d, id + ".json"));
            }
        } catch (Exception ignored) {
        }
    }

    /** How many conversations are saved (without reading them). */
    static int count(File filesDir) {
        File[] fs = dir(filesDir).listFiles((d, n) -> n.endsWith(".json"));
        return fs == null ? 0 : fs.length;
    }

    /** One writer, so two saves of the same conversation never share the temporary file. */
    static final java.util.concurrent.ExecutorService WRITER = java.util.concurrent.Executors.newSingleThreadExecutor();

    /** All saved conversations, newest first. */
    static List<Entry> list(File filesDir) {
        List<Entry> out = new ArrayList<>();
        File[] fs = dir(filesDir).listFiles((d, n) -> n.endsWith(".json"));
        if (fs == null) return out;
        for (File f : fs) {
            JSONObject o = read(f);
            if (o == null) continue;
            JSONArray arr = o.optJSONArray("msgs");
            if (arr == null || arr.length() == 0) continue;
            String title = "";
            for (int i = 0; i < arr.length() && title.isEmpty(); i++) {
                JSONObject m = arr.optJSONObject(i);
                if (m != null && m.optBoolean("me")) title = m.optString("text", "");
            }
            if (title.isEmpty() && arr.optJSONObject(0) != null) title = arr.optJSONObject(0).optString("text", "");
            title = title.replace('\n', ' ');
            if (title.length() > 40) title = title.substring(0, 40) + "…";
            out.add(new Entry(f.getName().substring(0, f.getName().length() - 5), o.optLong("updated", f.lastModified()), title, arr.length()));
        }
        out.sort((a, b) -> Long.compare(b.updated, a.updated));
        return out;
    }

    static List<ChatPanel.Msg> load(File filesDir, String id) {
        List<ChatPanel.Msg> out = new ArrayList<>();
        JSONObject o = read(new File(dir(filesDir), id + ".json"));
        JSONArray arr = o == null ? null : o.optJSONArray("msgs");
        for (int i = 0; arr != null && i < arr.length(); i++) {
            JSONObject m = arr.optJSONObject(i);
            if (m == null) continue;
            List<String> games = new ArrayList<>();
            JSONArray g = m.optJSONArray("games");
            for (int j = 0; g != null && j < g.length(); j++) games.add(g.optString(j));
            out.add(new ChatPanel.Msg(m.optBoolean("me"), m.optString("text", ""), games));
        }
        return out;
    }

    static void delete(File filesDir, String id) { new File(dir(filesDir), id + ".json").delete(); }

    static int clearAll(File filesDir) {
        int n = 0;
        File[] fs = dir(filesDir).listFiles();
        if (fs != null) for (File f : fs) if (f.delete() && f.getName().endsWith(".json")) n++;
        return n;
    }

    private static JSONObject read(File f) {
        if (!f.isFile()) return null;
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[(int) f.length()];
            int off = 0;
            for (int r; off < b.length && (r = in.read(b, off, b.length - off)) > 0; ) off += r;
            return new JSONObject(new String(b, 0, off, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }
}
