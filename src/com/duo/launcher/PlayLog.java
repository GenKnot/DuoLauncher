package com.duo.launcher;

import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * Every play session, one line each, in the app's files folder (playlog.tsv): start, end, time played, game key,
 * title and console. It is what the monthly and yearly reports are made from; it travels with the play time in
 * exports and the automatic copy.
 */
final class PlayLog {
    private PlayLog() { }

    static final String FILE = "playlog.tsv";

    /** One thread for every change to the log, so a clear can never run before the add of the session it clears. */
    private static final java.util.concurrent.ExecutorService QUEUE = java.util.concurrent.Executors.newSingleThreadExecutor();

    static void later(Runnable r) { QUEUE.execute(r); }

    static final class Entry {
        final long start, end, spent;
        final String key, title, platform;

        Entry(long start, long end, long spent, String key, String title, String platform) {
            this.start = start;
            this.end = end;
            this.spent = spent;
            this.key = key;
            this.title = title;
            this.platform = platform;
        }
    }

    private static String clean(String s) { return s == null ? "" : s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '); }

    static synchronized void add(File dir, Entry e) {
        // already there (brought in by a backfill that ran first)
        for (Entry x : read(dir)) if (x.key.equals(e.key) && x.start == e.start) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(dir, FILE), true), StandardCharsets.UTF_8)) {
            w.write(e.start + "\t" + e.end + "\t" + e.spent + "\t" + clean(e.key) + "\t" + clean(e.title) + "\t" + clean(e.platform) + "\n");
        } catch (IOException ignored) {
            // the report misses one session; play time itself is kept in the prefs
        }
    }

    static synchronized List<Entry> read(File dir) {
        List<Entry> out = new ArrayList<>();
        File f = new File(dir, FILE);
        if (!f.isFile()) return out;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                String[] p = line.split("\t", -1);
                if (p.length < 6) continue;
                try {
                    out.add(new Entry(Long.parseLong(p[0]), Long.parseLong(p[1]), Long.parseLong(p[2]), p[3], p[4], p[5]));
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException ignored) { }
        out.sort((a, b) -> Long.compare(a.start, b.start));
        return out;
    }

    /** Removes one game's sessions (when its play history is cleared). */
    static synchronized void remove(File dir, String key) {
        List<Entry> all = read(dir);
        all.removeIf(e -> e.key.equals(key));
        write(dir, all);
    }

    private static void write(File dir, List<Entry> all) {
        File f = new File(dir, FILE), tmp = new File(dir, FILE + ".tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            for (Entry e : all)
                w.write(e.start + "\t" + e.end + "\t" + e.spent + "\t" + clean(e.key) + "\t" + clean(e.title) + "\t" + clean(e.platform) + "\n");
        } catch (IOException e) {
            tmp.delete();
            return;
        }
        if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f); }
    }

    /**
     * The first time: the sessions the launcher already remembers (the last 40 of each game) become the start of
     * the log, so this month's and this year's reports are not empty for someone who has played before.
     */
    static synchronized void backfill(File dir, SharedPreferences prefs, Map<String, Library.Game> byKey) {
        backfill(dir, prefs, byKey, null, 0);
    }

    /**
     * Once: the remembered sessions join whatever the log already has (sessions it has are not added twice);
     * {@code skipKey}/{@code skipStart} is a session about to be added by the caller.
     */
    static synchronized void backfill(File dir, SharedPreferences prefs, Map<String, Library.Game> byKey, String skipKey, long skipStart) {
        if (prefs.getBoolean("playlog.made", false)) return;
        List<Entry> all = read(dir);
        Set<String> have = new HashSet<>();
        for (Entry e : all) have.add(e.key + "@" + e.start);
        if (skipKey != null) have.add(skipKey + "@" + skipStart);
        for (Map.Entry<String, ?> kv : prefs.getAll().entrySet()) {
            if (!kv.getKey().startsWith("sessions:") || !(kv.getValue() instanceof String)) continue;
            String key = kv.getKey().substring(9);
            Library.Game g = byKey == null ? null : byKey.get(key);
            for (String x : ((String) kv.getValue()).split(",")) {
                int d = x.indexOf('-'), t = x.indexOf('~');
                if (d <= 0) continue;
                try {
                    long a = Long.parseLong(x.substring(0, d)), b = Long.parseLong(x.substring(d + 1, t < 0 ? x.length() : t));
                    long spent = t < 0 ? Math.max(0, Math.min(b - a, 24L * 3600_000)) : Long.parseLong(x.substring(t + 1));
                    if (have.add(key + "@" + a)) all.add(new Entry(a, b, spent, key, g == null ? key : g.title, g == null ? "" : g.platform.title()));
                } catch (NumberFormatException ignored) { }
            }
        }
        all.sort((a, b) -> Long.compare(a.start, b.start));
        write(dir, all);
        prefs.edit().putBoolean("playlog.made", true).apply();
    }
}
