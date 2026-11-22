package com.duo.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * Game series (Ys, Castlevania, Super Robot Wars…), so that one series' games from every platform can be shown together.
 * Kept in the launcher's own folder as DuoLauncher/<platform>/series.txt, one "file<TAB>English name<TAB>Chinese name" per
 * line; "-" for both names marks a game that belongs to no series, so it is not asked about again.
 */
final class Series {
    private Series() { }

    /** A game outside any series. */
    static final String NONE = "-";

    /** The name series folders are grouped by: the English name without case, spaces or punctuation. */
    static String norm(String english) {
        return english == null ? "" : english.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    /** The series name to show for a game in the interface language, or null. */
    static String label(Library.Game g) {
        if (g.series == null || g.series.equals(NONE)) return null;
        boolean zh = I18n.code().startsWith("zh");
        return zh && g.seriesZh != null && !g.seriesZh.equals(NONE) ? g.seriesZh : g.series;
    }

    /** {english, chinese} per game key. */
    static Map<String, String[]> read(File dir) {
        Map<String, String[]> out = new LinkedHashMap<>();
        File f = new File(dir, "series.txt");
        if (!f.isFile()) return out;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                if (line.startsWith("#")) continue;
                String[] c = line.split("\t");
                if (c.length < 3 || c[0].isEmpty()) continue;
                out.put(c[0], new String[]{c[1].trim(), c[2].trim()});
            }
        } catch (IOException ignored) {
        }
        return out;
    }

    static boolean write(File dir, Map<String, String[]> all) {
        dir.mkdirs();
        File f = new File(dir, "series.txt"), tmp = new File(dir, "series.txt.tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            w.write("# Duo Launcher: series of each game (file name, tab, English series name, tab, Chinese name; - for none)\n");
            for (Map.Entry<String, String[]> e : all.entrySet()) w.write(e.getKey() + "\t" + e.getValue()[0] + "\t" + e.getValue()[1] + "\n");
        } catch (IOException e) {
            tmp.delete();
            return false;
        }
        if (tmp.renameTo(f)) return true;
        f.delete();
        return tmp.renameTo(f);
    }

    /** Games of the platform not yet looked at. */
    static List<Library.Game> todo(Library.Platform p) {
        List<Library.Game> out = new ArrayList<>();
        for (Library.Game g : p.games) if (g.pkg == null && g.path != null && g.series == null) out.add(g);
        return out;
    }

    /** Series names already given to games of the library, so that the AI keeps using the same spelling. */
    static Set<String> known(Library lib) {
        Set<String> out = new LinkedHashSet<>();
        if (lib != null) for (Library.Game g : lib.byKey.values()) if (g.series != null && !g.series.equals(NONE)) out.add(g.series + " / " + g.seriesZh);
        return out;
    }

    /**
     * Asks the AI service which series each game belongs to, 30 at a time, and writes them to series.txt.
     * Returns how many were done; stops at the first failed request (ai.error).
     */
    static int classify(Ai ai, Library.Platform p, List<Library.Game> games, Set<String> known, IntConsumer progress) {
        String system = "You group video games into series (franchises) for a game launcher. For each game you get its platform, "
                + "ROM file name and title (possibly Chinese, possibly a fan translation or hack). Identify the game and say which "
                + "series it belongs to: the English series name in its usual short form (for example \"Ys\", \"Castlevania\", "
                + "\"Super Robot Wars\", \"The Legend of Heroes\", \"Final Fantasy\", \"Pokemon\", \"Mario\", \"Monster Hunter\", "
                + "\"God of War\", \"Tales of\"), and the usual Simplified Chinese name of the series (伊苏, 恶魔城, 超级机器人大战, "
                + "英雄传说, 最终幻想, 宝可梦, 马里奥, 怪物猎人, 战神, 传说系列). Spin-offs join their main series (Mario Kart and Mario "
                + "Party are Mario). Use null for a game that is not part of a series with at least two games. When one of the known "
                + "series fits, use its names exactly as given. Answer only with a JSON array of objects "
                + "{\"i\": number, \"series\": string or null, \"zh\": string or null}, one for each input.";
        File dir = games.isEmpty() ? null : GameText.ownDir(p, games.get(0));
        Map<String, String[]> all = dir == null ? new LinkedHashMap<>() : read(dir);
        int done = 0;
        for (int start = 0; start < games.size(); start += 30) {
            List<Library.Game> part = games.subList(start, Math.min(games.size(), start + 30));
            JSONArray in = new JSONArray();
            try {
                for (int i = 0; i < part.size(); i++) {
                    Library.Game g = part.get(i);
                    JSONObject o = new JSONObject().put("i", i).put("platform", Library.platformTitle(p.system.isEmpty() ? p.name : p.system))
                            .put("file", g.path.getName()).put("title", g.title);
                    if (g.original != null && !g.original.isEmpty()) o.put("english title", g.original);
                    in.put(o);
                }
            } catch (Exception e) {
                break;
            }
            String user = (known.isEmpty() ? "" : "Known series: " + String.join("; ", known) + "\n\n") + in;
            String answer = ai.chat(system, user, 200 + part.size() * 45);
            if (answer == null) break;
            int a = answer.indexOf('['), b = answer.lastIndexOf(']');
            if (a < 0 || b <= a) { ai.error = "unexpected answer"; break; }
            try {
                JSONArray out = new JSONArray(answer.substring(a, b + 1));
                for (int j = 0; j < out.length(); j++) {
                    JSONObject o = out.getJSONObject(j);
                    int i = o.optInt("i", -1);
                    if (i < 0 || i >= part.size()) continue;
                    String en = o.isNull("series") ? "" : o.optString("series", "").trim();
                    String zh = o.isNull("zh") ? "" : o.optString("zh", "").trim();
                    if (en.isEmpty() || en.equalsIgnoreCase("null") || norm(en).isEmpty()) { en = NONE; zh = NONE; }
                    else if (zh.isEmpty() || zh.equalsIgnoreCase("null")) zh = en;
                    en = en.replace('\t', ' ');
                    zh = zh.replace('\t', ' ');
                    Library.Game g = part.get(i);
                    String k = Genres.key(g);
                    if (k == null) continue;
                    all.put(k, new String[]{en, zh});
                    g.series = en;
                    g.seriesZh = zh;
                    if (!en.equals(NONE)) known.add(en + " / " + zh);
                    done++;
                }
            } catch (Exception e) {
                ai.error = "unexpected answer";
                break;
            }
            all.putAll(read(dir));
            if (!write(dir, all)) { ai.error = "could not write " + dir; break; }
            progress.accept(Math.min(games.size(), start + part.size()));
        }
        return done;
    }
}
