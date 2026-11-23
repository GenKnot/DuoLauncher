package com.duo.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Game names and descriptions in the interface language, written by the AI service into the launcher's own
 * metadata (DuoLauncher/<platform>/metadata.txt, as name.<lang> / description.<lang>), together with each
 * game's official English title ("original:"), so games can later be matched to databases by their real name.
 */
final class GameText {
    private GameText() { }

    private static boolean cjk(String s) {
        return s.codePoints().anyMatch(cp -> {
            Character.UnicodeScript u = Character.UnicodeScript.of(cp);
            return u == Character.UnicodeScript.HAN || u == Character.UnicodeScript.HIRAGANA
                    || u == Character.UnicodeScript.KATAKANA || u == Character.UnicodeScript.HANGUL;
        });
    }

    private static int latin(String s) {
        int n = 0;
        for (char c : s.toCharArray()) if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) n++;
        return n;
    }

    /** Text that is not written in the script of this language (Chinese names under English, English under Chinese …). */
    static boolean foreign(String text, String lang) {
        if (text == null || text.trim().isEmpty()) return false;
        boolean eastAsian = lang.startsWith("zh") || lang.startsWith("ja") || lang.startsWith("ko");
        if (eastAsian) return !cjk(text) && latin(text) >= 4;
        return cjk(text);
    }

    /** Games of the platform whose name or description is not in the language yet. */
    static List<Library.Game> todo(Library.Platform p, String lang) {
        List<Library.Game> out = new ArrayList<>();
        for (Library.Game g : p.games) {
            if (g.pkg != null || g.path == null) continue;
            // a game without any description gets a short one too, and one without its official English title,
            // which covers, cheats and the AI use to find it
            if (foreign(g.title, lang) || foreign(g.description, lang) || g.description.trim().isEmpty()
                    || g.original == null || g.original.trim().isEmpty())
                out.add(g);
        }
        return out;
    }

    /** The launcher's own folder for the platform, on the card the games are on. */
    static File ownDir(Library.Platform p, Library.Game g) {
        String path = g.path.getAbsolutePath();
        File vol;
        if (path.startsWith("/storage/emulated/0/")) vol = new File("/storage/emulated/0");
        else if (path.startsWith("/storage/")) {
            int s = path.indexOf('/', 9);
            vol = new File(s < 0 ? path : path.substring(0, s));
        } else vol = g.path.getParentFile();
        return new File(vol, "DuoLauncher/" + p.name);
    }

    /**
     * Translates the games in batches; returns how many were written. progress gets the number done so far.
     * Stops at the first failed request (the error is left in ai.error).
     */
    static int translate(Ai ai, Library.Platform p, List<Library.Game> games, String lang, IntConsumer progress) {
        String key = lang.toLowerCase(Locale.ROOT);
        String system = "You write game library entries for a game launcher. For each game you get its platform, ROM file name, "
                + "current title and description (possibly in another language, possibly a fan translation). Identify the game, then give: "
                + "\"original\" = its official English release title (as on the box; if it never had an English release, its official "
                + "title romanised), \"title\" = its title in " + Ai.languageName(lang) + " (the official localised title in that "
                + "language when one exists, otherwise a natural translation; no tags like (CN), 汉化版, (简), (USA)), and "
                + "\"description\" = one or two sentences about the game in " + Ai.languageName(lang) + " (translate the given "
                + "description if there is one, otherwise write a short neutral one). Answer only with a JSON array of objects "
                + "{\"i\": number, \"original\": string, \"title\": string, \"description\": string}, one for each input.";
        int done = 0, failedInRow = 0;
        skipped = 0;
        gaveUp = false;
        for (int start = 0; start < games.size(); start += 15) {
            List<Library.Game> part = games.subList(start, Math.min(games.size(), start + 15));
            // a batch that fails (a timeout, an answer that is not JSON) is tried again, then skipped, so one bad
            // batch does not stop a whole library; three failed batches in a row (a wrong key, no network) end the run
            int n = -1;
            for (int attempt = 0; attempt < 3 && n == -1; attempt++) n = batch(ai, p, part, lang, key, system);
            if (n == -2) {   // could not write the metadata file: another try would only cost another AI call
                ai.error = I18n.t(I18n.S.WRITE_FAILED);
                gaveUp = true;
                return done;
            }
            if (n < 0) {
                skipped += part.size();
                if (++failedInRow >= 3) { gaveUp = true; return done; }
            } else {
                failedInRow = 0;
                done += n;
                skipped += part.size() - n;
            }
            progress.accept(Math.min(games.size(), start + part.size()));
        }
        ai.error = null;   // what failed was skipped and is counted in skipped
        return done;
    }

    /** Games of the last translate call that were left out, and whether it stopped early. */
    static int skipped;
    static boolean gaveUp;

    /** One request for up to 15 games; the number written, -1 when the AI failed, -2 when the file could not be written. */
    private static int batch(Ai ai, Library.Platform p, List<Library.Game> part, String lang, String key, String system) {
        JSONArray in = new JSONArray();
        try {
            for (int i = 0; i < part.size(); i++) {
                Library.Game g = part.get(i);
                in.put(new JSONObject().put("i", i).put("platform", Library.platformTitle(p.system.isEmpty() ? p.name : p.system))
                        .put("file", g.path.getName()).put("title", g.title).put("description", g.description));
            }
        } catch (Exception e) {
            return -1;
        }
        String answer = ai.chat(system, in.toString(), 400 + part.size() * 220);
        if (answer == null) return -1;
        int a = answer.indexOf('['), b = answer.lastIndexOf(']');
        if (a < 0 || b <= a) { ai.error = "unexpected answer"; return -1; }
        StringBuilder sb = new StringBuilder();
        int done = 0;
        try {
            JSONArray out = new JSONArray(answer.substring(a, b + 1));
            for (int j = 0; j < out.length(); j++) {
                JSONObject o = out.getJSONObject(j);
                int i = o.optInt("i", -1);
                if (i < 0 || i >= part.size()) continue;
                Library.Game g = part.get(i);
                String title = clean(o.optString("title")), desc = clean(o.optString("description")), orig = clean(o.optString("original"));
                if (title.isEmpty()) continue;
                sb.append("\ngame: ").append(clean(g.title)).append('\n');
                if (g.titleId != null && g.path.getName().endsWith(".app")) sb.append("titleid: ").append(g.titleId).append('\n');
                else sb.append("file: ").append(g.path.getName()).append('\n');
                // a game whose name and description already read well only gets its English title: the
                // description it has (often long, from the ROM set) is kept rather than replaced by a summary
                boolean onlyOriginal = !foreign(g.title, lang) && !foreign(g.description, lang) && !g.description.trim().isEmpty();
                if (!onlyOriginal) {
                    sb.append("name.").append(key).append(": ").append(title).append('\n');
                    if (!desc.isEmpty()) sb.append("description.").append(key).append(": ").append(desc).append('\n');
                }
                if (!orig.isEmpty()) sb.append("original: ").append(orig).append('\n');
                done++;
            }
        } catch (Exception e) {
            ai.error = "unexpected answer";
            return -1;
        }
        if (sb.length() > 0 && !append(ownDir(p, part.get(0)), sb.toString())) return -2;
        return done;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static boolean append(File dir, String text) {
        dir.mkdirs();
        File f = new File(dir, "metadata.txt");
        boolean fresh = !f.isFile();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8)) {
            if (fresh) w.write("# Duo Launcher's own game information (Pegasus format); it wins over other sources.\n"
                    + "# name.<language> / description.<language> are used when the launcher runs in that language.\n");
            w.write(text);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
