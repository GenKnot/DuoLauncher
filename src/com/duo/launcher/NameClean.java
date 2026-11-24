package com.duo.launcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tidier names for games that only have their file name to go by ("0153 - Super_Game (USA, Europe) [!]" becomes
 * "Super Game"). Each rule can be switched on or off in Settings. Only the name shown changes: the file, and every
 * name another frontend or the player gave a game, stay as they are.
 */
final class NameClean {
    private NameClean() { }

    static final String REGION = "region", TAGS = "tags", NUMBER = "number", SPACES = "spaces", ARTICLE = "article",
            BRACKETS = "brackets";

    /** In the order the settings list them. */
    static final List<String> RULES = Arrays.asList(REGION, TAGS, NUMBER, SPACES, ARTICLE, BRACKETS);

    /** The rules switched on (from Settings), read by the scan. */
    static volatile Set<String> on = Collections.emptySet();

    private static final Set<String> REGIONS = new HashSet<>(Arrays.asList(
            "usa", "us", "u", "europe", "eu", "e", "japan", "jp", "j", "jpn", "world", "w", "asia", "korea", "k", "china",
            "ch", "taiwan", "hong kong", "hk", "australia", "a", "brazil", "b", "canada", "france", "f", "germany", "g",
            "spain", "s", "italy", "i", "netherlands", "sweden", "russia", "uk", "scandinavia", "latin america",
            "en", "fr", "de", "es", "it", "nl", "pt", "sv", "no", "da", "fi", "zh", "ja", "ko", "ru", "pl", "cs", "hu",
            "el", "tr", "ar", "ca", "zh-hans", "zh-hant", "chinese", "english", "japanese", "unl"));

    private static final Pattern PAREN = Pattern.compile("\\s*\\(([^()]*)\\)");
    private static final Pattern SQUARE = Pattern.compile("\\s*\\[[^\\[\\]]*\\]");
    private static final Pattern VERSION = Pattern.compile("(?i)\\s*\\((?:rev\\s*[0-9a-z.]+|v\\s*[0-9][0-9a-z.]*|alt\\s*\\d*)\\)");
    // list numbers of ROM packs ("0153 - ", "001 ", "12. "), not titles that start with a number ("1943 - ", "3-D …", "10-Yard …", "007 …")
    private static final Pattern NUMBERED = Pattern.compile("^#?(?:(?!007\\b)(?:00\\d{1,3}|0\\d{3,4})\\s*[-_.)]?|\\d{1,5}[._)])\\s+(?=\\S)");
    private static final Pattern ARTICLE_END = Pattern.compile("(?i)^(.+?),\\s*(the|a|an)(\\s*(?:[-:(\\[].*)?)$");

    /** The name a file would be shown under with the given rules ({@code base}: the file name without its type). */
    static String apply(String base, Set<String> rules) {
        if (base == null || rules.isEmpty()) return base;
        String s = base;
        if (rules.contains(NUMBER)) s = NUMBERED.matcher(s).replaceFirst("");
        if (rules.contains(SPACES)) {
            s = s.replace('_', ' ');
            // "Super.Mario.World": dots between words, when the name has no spaces of its own
            if (s.indexOf(' ') < 0 && s.indexOf('.') > 0) s = s.replaceAll("(?<=\\p{L})\\.(?=\\p{L})", " ");
        }
        if (rules.contains(BRACKETS)) {
            s = SQUARE.matcher(s).replaceAll("");
            s = PAREN.matcher(s).replaceAll("");
        } else {
            if (rules.contains(TAGS)) {
                s = SQUARE.matcher(s).replaceAll("");
                s = VERSION.matcher(s).replaceAll("");
            }
            if (rules.contains(REGION)) {
                Matcher m = PAREN.matcher(s);
                StringBuffer out = new StringBuffer();
                while (m.find()) m.appendReplacement(out, isRegion(m.group(1)) ? "" : Matcher.quoteReplacement(m.group()));
                m.appendTail(out);
                s = out.toString();
            }
        }
        if (rules.contains(ARTICLE)) {
            Matcher m = ARTICLE_END.matcher(s.trim());
            if (m.matches()) {
                String art = m.group(2);
                s = Character.toUpperCase(art.charAt(0)) + art.substring(1).toLowerCase(Locale.ROOT) + " " + m.group(1) + m.group(3);
            }
        }
        s = s.replaceAll("\\s{2,}", " ").replaceAll("[\\s\\-_.,]+$", "").trim();
        return s.isEmpty() ? base : s;
    }

    /** "(USA, Europe)", "(En,Fr,De)", "(Japan) " and the like: every part is a region or a language. */
    private static boolean isRegion(String inner) {
        String[] parts = inner.split("[,+/]");
        for (String p : parts) if (!REGIONS.contains(p.trim().toLowerCase(Locale.ROOT))) return false;
        return parts.length > 0;
    }

    /** The file-name title of a game (no type, no disc tag), or null when its name came from somewhere else. */
    static String fileBase(Library.Game g) {
        if (g.file == null || g.pkg != null || g.path == null) return null;
        String n = new File(g.file).getName();
        String ext = Library.extension(n);
        String base = ext.isEmpty() ? n : n.substring(0, n.length() - ext.length() - 1);
        String[] disc = Library.discTag(base);
        if (g.title.equals(base)) return base;
        if (disc != null && g.title.equals(disc[0])) return disc[0];
        return null;
    }

    /** Gives every game named after its file the tidier name. */
    static void apply(Library lib) {
        Set<String> rules = on;
        if (rules.isEmpty()) return;
        for (Library.Platform p : lib.platforms)
            for (Library.Game g : p.games) {
                String base = fileBase(g);
                if (base != null) g.title = apply(base, rules);
            }
    }

    private static final class File {
        private final String name;
        File(String path) { int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')); name = path.substring(i + 1); }
        String getName() { return name; }
    }
}
