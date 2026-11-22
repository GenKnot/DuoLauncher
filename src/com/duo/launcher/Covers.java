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
        progress.accept(100);
        return added;
    }
}
