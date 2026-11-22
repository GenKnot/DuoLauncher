package com.duo.launcher;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Finds games without any picture and fetches box art and a title screen from libretro's thumbnail
 * repositories, matched by the game code in the ROM header (bundled tables assets/covers/*.tsv).
 * Pictures go to <volume>/DuoLauncher/<platform>/media/<file name>/cover.png and background.png.
 */
final class Covers {
    private static final String RAW = "https://raw.githubusercontent.com/libretro-thumbnails/";

    static boolean hasPicture(Library.Game g) {
        return g.pkg != null || g.asset("box_front") != null || g.asset("background") != null || g.asset("logo") != null;
    }

    static List<Library.Game> missing(Library lib) {
        List<Library.Game> out = new ArrayList<>();
        java.util.Set<Library.Game> seen = new java.util.HashSet<>();
        for (Library.Platform p : lib.platforms) {
            if (p.isCustom()) continue;
            for (Library.Game g : p.games) if (seen.add(g) && g.path != null && !hasPicture(g)) out.add(g);
        }
        return out;
    }

    /** 4-character game code from the ROM header: NDS at 0x0C, GBA at 0xAC; null for other files. */
    static String code(Library.Game g) {
        String e = g.ext();
        if (e.equals("zip")) return zipCode(g);
        long off = e.equals("nds") ? 0x0C : e.equals("gba") ? 0xAC : -1;
        if (off < 0) return null;
        try (RandomAccessFile r = new RandomAccessFile(g.path, "r")) {
            byte[] b = new byte[4];
            r.seek(off);
            r.readFully(b);
            String s = new String(b, StandardCharsets.US_ASCII);
            return s.matches("[A-Z0-9]{4}") ? s : null;
        } catch (IOException ex) {
            return null;
        }
    }

    /** The same header read from the first .gba / .nds file inside a zip. */
    private static String zipCode(Library.Game g) {
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(g.path)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> en = z.entries();
            while (en.hasMoreElements()) {
                java.util.zip.ZipEntry ze = en.nextElement();
                String n = ze.getName().toLowerCase(Locale.ROOT);
                int off = n.endsWith(".nds") ? 0x0C : n.endsWith(".gba") ? 0xAC : -1;
                if (off < 0) continue;
                try (InputStream in = z.getInputStream(ze)) {
                    byte[] b = new byte[off + 4];
                    int got = 0;
                    while (got < b.length) {
                        int r = in.read(b, got, b.length - got);
                        if (r < 0) return null;
                        got += r;
                    }
                    String s = new String(b, off, 4, StandardCharsets.US_ASCII);
                    return s.matches("[A-Z0-9]{4}") ? s : null;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static Map<String, String[]> table(Context c, String sys) {
        Map<String, String[]> m = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open("covers/" + sys + ".tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = line.split("\t");
                if (f.length == 3) m.put(f[0], new String[]{f[1], f[2]});
            }
        } catch (IOException ignored) {
        }
        return m;
    }

    private static boolean download(String url, File out) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            if (con.getResponseCode() != 200) return false;
            out.getParentFile().mkdirs();
            File tmp = new File(out.getPath() + ".part");
            try (InputStream in = con.getInputStream(); OutputStream o = new FileOutputStream(tmp)) { Backups.copy(in, o); }
            return tmp.renameTo(out);
        } catch (IOException e) {
            return false;
        } finally {
            if (con != null) con.disconnect();
        }
    }

    /** The DuoLauncher folder on the same storage as the game. */
    private static File ownRoot(Library.Game g) {
        String p = g.path.getAbsolutePath();
        if (p.startsWith("/storage/")) {
            String rest = p.substring(9);
            int s = rest.indexOf('/');
            String vol = s < 0 ? rest : rest.substring(0, s);
            if (!vol.equals("emulated")) return new File("/storage/" + vol, "DuoLauncher");
        }
        return new File(android.os.Environment.getExternalStorageDirectory(), "DuoLauncher");
    }

    /** Fetches pictures for every NDS/GBA game without one; reports progress (0..100). Returns how many got a cover. */
    static int fetchMissing(Context c, Library lib, IntConsumer progress) {
        Map<String, Map<String, String[]>> tables = new HashMap<>();
        tables.put("NDS", table(c, "NDS"));
        tables.put("GBA", table(c, "GBA"));
        String[][] repos = {{"NDS", "Nintendo_-_Nintendo_DS"}, {"GBA", "Nintendo_-_Game_Boy_Advance"}};
        List<Library.Game> todo = missing(lib);
        int done = 0, added = 0;
        for (Library.Game g : todo) {
            progress.accept(100 * done++ / Math.max(1, todo.size()));
            String sys = g.platform.system.toUpperCase(Locale.ROOT);
            if (!tables.containsKey(sys)) continue;
            String code = code(g);
            String[] hit = code == null ? null : tables.get(sys).get(code);
            if (hit == null) continue;
            String repo = sys.equals("NDS") ? repos[0][1] : repos[1][1];
            File dir = new File(ownRoot(g), g.platform.name + "/media/" + new File(g.baseName()).getName());
            try {
                String name = URLEncoder.encode(hit[0] + ".png", "UTF-8").replace("+", "%20");
                if (download(RAW + repo + "/master/Named_Boxarts/" + name, new File(dir, "cover.png"))) {
                    added++;
                    if (hit[1].equals("1")) download(RAW + repo + "/master/Named_Titles/" + name, new File(dir, "background.png"));
                }
            } catch (IOException ignored) {
            }
        }
        added += fetchByName(lib);
        progress.accept(100);
        return added;
    }

    // ---- box art by name, for platforms without a game-code table (PlayStation, Nintendo 64, GameCube…) ----

    private static final String TREE = "https://api.github.com/repos/libretro-thumbnails/%s/git/trees/master?recursive=1";

    private static String repoName(String sys) {
        switch (sys) {
            case "PS1": return "Sony - PlayStation";
            case "PS2": return "Sony - PlayStation 2";
            case "PSP": return "Sony - PlayStation Portable";
            case "N64": return "Nintendo - Nintendo 64";
            case "NGC": case "GC": case "GAMECUBE": return "Nintendo - GameCube";
            case "WII": return "Nintendo - Wii";
            case "3DS": return "Nintendo - Nintendo 3DS";
            case "NDS": return "Nintendo - Nintendo DS";
            case "GBA": return "Nintendo - Game Boy Advance";
            case "GBC": return "Nintendo - Game Boy Color";
            case "GB": return "Nintendo - Game Boy";
            case "SNES": return "Nintendo - Super Nintendo Entertainment System";
            case "NES": return "Nintendo - Nintendo Entertainment System";
            case "MD": return "Sega - Mega Drive - Genesis";
            default: return null;
        }
    }

    /** "Final Fantasy X (USA) (Disc 1)" → "finalfantasyx": what names are matched by. */
    static String nameKey(String s) {
        String t = s.toLowerCase(Locale.ROOT).replaceAll("\\([^)]*\\)|\\[[^\\]]*\\]", " ").replace("&amp;", "and").replace("&", "and");
        t = t.replaceAll("^the\\s+|,\\s*the\\b", " ");
        return t.replaceAll("[^a-z0-9]+", "");
    }

    /** The GitHub repository of a thumbnail set: "Sony - PlayStation 2" → Sony_-_PlayStation_2. */
    private static String repoId(String repo) { return repo.replace(' ', '_'); }

    /** Every box-art file name of one libretro thumbnail set (from the repository's file list), or empty. */
    private static List<String> listing(String repo) {
        List<String> out = new ArrayList<>();
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(String.format(Locale.ROOT, TREE, repoId(repo))).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(90000);
            con.setRequestProperty("User-Agent", "DuoLauncher");
            con.setRequestProperty("Accept", "application/vnd.github+json");
            if (con.getResponseCode() != 200) return out;
            java.util.regex.Pattern path = java.util.regex.Pattern.compile("\"path\"\\s*:\\s*\"Named_Boxarts/([^\"]+\\.png)\"");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder all = new StringBuilder();
                char[] buf = new char[65536];
                for (int n; (n = r.read(buf)) > 0; ) all.append(buf, 0, n);
                java.util.regex.Matcher m = path.matcher(all);
                while (m.find()) out.add(m.group(1));
            }
        } catch (IOException | RuntimeException ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return out;
    }

    /** The best box-art file for a name: the same title, USA first, then Europe, then any. */
    private static String pick(Map<String, List<String>> byKey, String name) {
        if (name == null) return null;
        List<String> hits = byKey.get(nameKey(name));
        if (hits == null || hits.isEmpty()) return null;
        for (String region : new String[]{"(USA", "(World", "(Europe", "(Japan"})
            for (String h : hits) if (h.contains(region) && !h.contains("(Beta") && !h.contains("(Demo")) return h;
        return hits.get(0);
    }

    /** Box art for games still without any picture, matched by their English title or an English file name. */
    private static int fetchByName(Library lib) {
        Map<String, Map<String, List<String>>> sets = new HashMap<>();
        int added = 0;
        for (Library.Game g : missing(lib)) {
            String sys = (g.platform.system.isEmpty() ? g.platform.name : g.platform.system).toUpperCase(Locale.ROOT);
            String repo = repoName(sys);
            if (repo == null) continue;
            Map<String, List<String>> byKey = sets.get(repo);
            if (byKey == null) {
                byKey = new HashMap<>();
                for (String f : listing(repo)) {
                    String n = f.substring(0, f.length() - 4);
                    byKey.computeIfAbsent(nameKey(n), k -> new ArrayList<>()).add(n);
                }
                sets.put(repo, byKey);
            }
            if (byKey.isEmpty()) continue;
            String base = new File(g.baseName()).getName();
            String hit = pick(byKey, g.original);
            if (hit == null && base.matches("[\\x20-\\x7E]+")) hit = pick(byKey, base);
            if (hit == null && g.title.matches("[\\x20-\\x7E]+")) hit = pick(byKey, g.title);
            if (hit == null) continue;
            File dir = new File(ownRoot(g), g.platform.name + "/media/" + base);
            try {
                String url = RAW + repoId(repo) + "/master/Named_Boxarts/" + URLEncoder.encode(hit + ".png", "UTF-8").replace("+", "%20");
                if (download(url, new File(dir, "cover.png"))) added++;
            } catch (IOException ignored) {
            }
        }
        return added;
    }
}
