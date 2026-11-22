package com.duo.launcher;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * What the launcher already knows about games, so that the AI is asked once per game for everyone: genre, series and
 * official English title by platform and file name, bundled as assets/gamedb.tsv. ROM packs share their file names,
 * so a game copied in later usually finds its entry at once. The launcher also writes the user's whole library as
 * DuoLauncher/catalog.tsv (the source of the bundled table, and readable by people and the AI recommendations).
 */
final class GameDb {
    private GameDb() { }

    private static Map<String, String[]> db;

    /** The platform part of a key: "PS2", "GBA" (also for a "GBA hack" folder whose system is GBA). */
    static String sys(Library.Platform p) {
        return (p.system.isEmpty() ? p.name : p.system).toUpperCase(Locale.ROOT);
    }

    /** The file part of a key: the file name in lower case, or a 3DS title ID. */
    static String file(Library.Game g) {
        String k = Genres.key(g);
        return k == null ? null : k.toLowerCase(Locale.ROOT);
    }

    private static synchronized Map<String, String[]> table(Context c) {
        if (db != null) return db;
        Map<String, String[]> m = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open("gamedb.tsv"), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                if (line.startsWith("#")) continue;
                String[] f = line.split("\t", -1);
                if (f.length >= 6) m.put(f[0] + "\t" + f[1], new String[]{f[2], f[3], f[4], f[5]});
            }
        } catch (IOException ignored) {
        }
        return db = m;
    }

    /** Fills in genre, series and English title where neither the user's files nor other frontends gave one. */
    static void apply(Context c, Library lib) {
        Map<String, String[]> t = table(c);
        if (t.isEmpty()) return;
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            String sys = sys(p);
            for (Library.Game g : p.games) {
                if (g.pkg != null) continue;
                String f = file(g);
                String[] e = f == null ? null : t.get(sys + "\t" + f);
                if (e == null) continue;
                if (g.genre == null && !e[0].isEmpty() && Genres.index(e[0]) >= 0) g.genre = e[0];
                if (g.series == null && !e[1].isEmpty()) { g.series = e[1]; g.seriesZh = e[2].isEmpty() ? e[1] : e[2]; }
                if ((g.original == null || g.original.isEmpty()) && !e[3].isEmpty()) g.original = e[3];
            }
        }
    }

    private static String clean(String s) { return s == null ? "" : s.replace('\t', ' ').replace('\n', ' ').trim(); }

    /**
     * Writes every game of the library to DuoLauncher/catalog.tsv on the card of the first game: platform key, file,
     * title, English title, genre, series (English, Chinese), platform name, minutes played, status and rating.
     */
    static void writeCatalog(Library lib, android.content.SharedPreferences prefs) {
        File out = null;
        StringBuilder b = new StringBuilder("# Duo Launcher catalog\tsystem\tfile\ttitle\toriginal\tgenre\tseries\tseries_zh\tplatform\tminutes\tstatus\trating\n");
        java.util.Set<Library.Game> seen = new java.util.HashSet<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            for (Library.Game g : p.games) {
                if (g.pkg != null || g.path == null || !seen.add(g)) continue;
                if (out == null) out = new File(GameText.ownDir(g.platform, g).getParentFile(), "catalog.tsv");
                String f = file(g);
                if (f == null) continue;
                b.append(sys(g.platform)).append('\t').append(clean(f)).append('\t').append(clean(g.title)).append('\t')
                        .append(clean(g.original)).append('\t').append(g.genre == null ? "" : g.genre).append('\t')
                        .append(g.series == null ? "" : clean(g.series)).append('\t').append(g.seriesZh == null ? "" : clean(g.seriesZh)).append('\t')
                        .append(clean(g.platform.title())).append('\t').append(prefs.getLong("time:" + g.key(), 0) / 60_000).append('\t')
                        .append(prefs.getString("status:" + g.key(), "")).append('\t').append(prefs.getInt("rating:" + g.key(), 0)).append('\n');
            }
        }
        if (out == null) return;
        File tmp = new File(out.getPath() + ".tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            w.write(b.toString());
        } catch (IOException e) {
            tmp.delete();
            return;
        }
        if (!tmp.renameTo(out)) { out.delete(); tmp.renameTo(out); }
    }
}
