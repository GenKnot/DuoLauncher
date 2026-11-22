package com.duo.launcher;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RetroArch cheats: RetroArch loads <cheat folder>/<core name>/<game file name>.cht when a game starts, and with
 * "Auto-apply cheats during game load" switched on it applies the enabled ones. A game without such a file gets
 * one copied from another core's folder or from the Chinese packs Retroid ships ("000 中文金手指"), matched by
 * name, or downloaded from libretro's database (GBA, matched by the game code in the ROM header).
 */
final class RaCheats {
    private static final String LIBRETRO = "https://raw.githubusercontent.com/libretro/libretro-database/master/cht/";
    private static final Pattern CORE = Pattern.compile("cores/([A-Za-z0-9_]+?)_libretro");
    private static final Pattern LINE = Pattern.compile("^cheat(\\d+)_([a-z_0-9]+)\\s*=\\s*\"?(.*?)\"?\\s*$");

    /** Folder names RetroArch uses for each core (its library name). */
    private static final Map<String, String> FOLDERS = new HashMap<>();

    static {
        String[] m = {"mgba", "mGBA", "gpsp", "gpSP", "vbam", "VBA-M", "vba_next", "VBA Next", "gambatte", "Gambatte",
                "sameboy", "SameBoy", "gearboy", "Gearboy", "fceumm", "FCEUmm", "nestopia", "Nestopia", "mesen", "Mesen",
                "snes9x", "Snes9x", "snes9x2010", "Snes9x 2010", "bsnes", "bsnes", "genesis_plus_gx", "Genesis Plus GX",
                "picodrive", "PicoDrive", "mupen64plus_next", "Mupen64Plus-Next", "parallel_n64", "ParaLLEl N64",
                "pcsx_rearmed", "PCSX-ReARMed", "swanstation", "SwanStation", "mednafen_psx", "Beetle PSX",
                "melondsds", "melonDS DS", "melonds", "melonDS", "desmume", "DeSmuME", "ppsspp", "PPSSPP"};
        for (int i = 0; i < m.length; i += 2) FOLDERS.put(m[i], m[i + 1]);
    }

    /** libretro database folder per platform, for downloads. */
    private static final Map<String, String> SYSTEMS = new HashMap<>();

    static {
        SYSTEMS.put("GBA", "Nintendo - Game Boy Advance");
    }

    static File root() {
        return new File(Environment.getExternalStorageDirectory(), "RetroArch/cheats");
    }

    static boolean isRetroArch(String cmd) {
        return cmd != null && cmd.contains("com.retroarch") && CORE.matcher(cmd).find();
    }

    static String folder(String cmd) {
        Matcher m = CORE.matcher(cmd);
        if (!m.find()) return null;
        String f = FOLDERS.get(m.group(1));
        return f != null ? f : m.group(1);
    }

    /** RetroArch names the file after the game file without its extension. */
    static File file(Library.Game g, String cmd) {
        String f = folder(cmd);
        if (f == null || g.path == null) return null;
        String n = g.path.getName();
        int dot = n.lastIndexOf('.');
        return new File(new File(root(), f), (dot > 0 ? n.substring(0, dot) : n) + ".cht");
    }

    // ---- reading and writing .cht ----

    static List<Cheats.Cheat> parse(String text) {
        Map<Integer, Cheats.Cheat> byIndex = new java.util.TreeMap<>();
        for (String raw : text.replace("\uFEFF", "").split("\r?\n")) {
            Matcher m = LINE.matcher(raw.trim());
            if (!m.matches()) continue;
            int i = Integer.parseInt(m.group(1));
            Cheats.Cheat c = byIndex.get(i);
            if (c == null) { c = new Cheats.Cheat(); c.name = ""; byIndex.put(i, c); }
            String k = m.group(2), v = m.group(3).replace("\\\"", "\"");
            if (k.equals("desc")) c.name = v;
            else if (k.equals("enable")) c.enabled = v.equalsIgnoreCase("true");
            else if (k.startsWith("note")) c.lines.add("*" + v);
            else c.props.put(k, v);
        }
        List<Cheats.Cheat> out = new ArrayList<>(byIndex.values());
        for (Cheats.Cheat c : out) if (c.name.isEmpty()) c.name = c.props.getOrDefault("code", "?");
        return out;
    }

    static String format(List<Cheats.Cheat> cheats) {
        StringBuilder sb = new StringBuilder("cheats = ").append(cheats.size()).append("\n\n");
        for (int i = 0; i < cheats.size(); i++) {
            Cheats.Cheat c = cheats.get(i);
            sb.append("cheat").append(i).append("_desc = \"").append(c.name.replace("\"", "'")).append("\"\n");
            for (Map.Entry<String, String> e : c.props.entrySet())
                sb.append("cheat").append(i).append('_').append(e.getKey()).append(" = \"").append(e.getValue()).append("\"\n");
            int n = 0;
            for (String l : c.lines) if (l.startsWith("*"))
                sb.append("cheat").append(i).append("_note").append(n++ == 0 ? "" : String.valueOf(n)).append(" = \"")
                        .append(l.substring(1).replace("\"", "'")).append("\"\n");
            sb.append("cheat").append(i).append("_enable = ").append(c.enabled).append("\n\n");
        }
        return sb.toString();
    }

    private static List<Cheats.Cheat> read(File f) {
        try {
            return parse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static boolean write(File f, String text) {
        f.getParentFile().mkdirs();
        File tmp = new File(f.getPath() + ".part");
        try (OutputStream o = new FileOutputStream(tmp)) {
            o.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return false;
        }
        return tmp.renameTo(f) || (f.delete() && tmp.renameTo(f));
    }

    // ---- finding cheats for a game that has none yet ----

    static String norm(String s) {
        s = s.replaceAll("\\.(cht|zip|7z|gba|gbc|gb|nes|fds|sfc|smc|n64|z64|v64|md|gen|bin|cue|chd|iso|pbp|nds)$", "");
        s = s.replaceAll("[(（\\[【][^)）\\]】]*[)）\\]】]", "");
        s = s.replaceAll("汉化版|中文版|完美版|修正版|简体中文|繁体中文|简体|繁体", "");
        return s.replaceAll("[\\s\\-_:：·!！,，.。~～'’]", "").toLowerCase(Locale.ROOT);
    }

    /** A file with the same game name in another core's folder, or a matching file in the Chinese packs. */
    static File local(Library.Game g, String cmd) {
        File own = file(g, cmd);
        if (own == null) return null;
        File[] cores = root().listFiles(File::isDirectory);
        if (cores == null) return null;
        for (File d : cores) {
            File f = new File(d, own.getName());
            if (f.isFile() && !f.equals(own)) return f;
        }
        String want = norm(own.getName());
        if (want.isEmpty()) return null;
        File packs = new File(root(), "000 中文金手指");
        String sys = g.platform.system.toUpperCase(Locale.ROOT);
        File[] dirs = packs.listFiles(File::isDirectory);
        if (dirs == null) return null;
        for (File d : dirs) {
            if (!d.getName().toUpperCase(Locale.ROOT).startsWith(sys)) continue;
            File[] fs = d.listFiles();
            if (fs != null) for (File f : fs) if (norm(f.getName()).equals(want)) return f;
        }
        for (File d : cores) {   // the same title spelled a little differently in another core's folder
            File[] fs = d.listFiles();
            if (fs != null) for (File f : fs) if (f.getName().endsWith(".cht") && norm(f.getName()).equals(want)) return f;
        }
        return null;
    }

    private static Map<String, String> index(Context c, String sys) {
        Map<String, String> m = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open("cheats/" + sys + ".tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = line.split("\t");
                if (f.length == 2) m.put(f[0], f[1]);
            }
        } catch (IOException ignored) {
        }
        return m;
    }

    private static String fetch(String url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            if (con.getResponseCode() != 200) return null;
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (InputStream in = con.getInputStream()) {
                Backups.copy(in, b);
            }
            return b.toString("UTF-8");
        } catch (IOException e) {
            return null;
        } finally {
            if (con != null) con.disconnect();
        }
    }

    /** The cheat store for one game started through RetroArch. */
    static Cheats.Store store(Context ctx, Library.Game g, String cmd) {
        File own = file(g, cmd);
        return new Cheats.Store() {
            @Override public String id() { return own.getPath(); }

            @Override public List<Cheats.Cheat> load() {
                return own.isFile() ? read(own) : new ArrayList<>();
            }

            @Override public boolean save(List<Cheats.Cheat> list) { return write(own, format(list)); }

            @Override public boolean strictCodes() { return false; }

            @Override public Cheats.Cheat newCheat(String name, List<String> codes) {
                Cheats.Cheat c = new Cheats.Cheat();
                c.name = name;
                c.props.put("code", String.join("+", codes));
                return c;
            }

            /** Copies cheats from the Chinese packs or another core first, then libretro's database. */
            @Override public int download() {
                List<Cheats.Cheat> have = load();
                List<Cheats.Cheat> found = new ArrayList<>();
                File src = local(g, cmd);
                if (src != null) found.addAll(read(src));
                String sys = SYSTEMS.get(g.platform.system.toUpperCase(Locale.ROOT));
                String code = Covers.code(g);
                if (found.isEmpty() && sys != null && code != null) {
                    String name = index(ctx, g.platform.system.toUpperCase(Locale.ROOT)).get(code);
                    if (name != null) {
                        try {
                            String text = fetch(LIBRETRO + URLEncoder.encode(sys, "UTF-8").replace("+", "%20") + "/"
                                    + URLEncoder.encode(name, "UTF-8").replace("+", "%20"));
                            if (text != null) found.addAll(parse(text));
                        } catch (IOException ignored) {
                        }
                    }
                }
                if (found.isEmpty()) return -1;
                java.util.Set<String> names = new java.util.HashSet<>();
                for (Cheats.Cheat c : have) names.add(c.name);
                int added = 0;
                for (Cheats.Cheat c : found) {
                    if (!c.props.containsKey("code") || !names.add(c.name)) continue;
                    c.enabled = false;
                    have.add(c);
                    added++;
                }
                if (added > 0 && !save(have)) return -1;
                return added;
            }
        };
    }

    /** Every .cht file in the core folders that still has English names, for translating them all at once. */
    static List<Cheats.Store> all() {
        List<Cheats.Store> out = new ArrayList<>();
        File[] cores = root().listFiles(f -> f.isDirectory() && !f.getName().startsWith("000"));
        if (cores == null) return out;
        for (File d : cores) {
            File[] fs = d.listFiles((dir, n) -> n.endsWith(".cht"));
            if (fs == null) continue;
            for (File f : fs) if (mayNeedTranslation(f)) out.add(new Cheats.Store() {
                @Override public String id() { return f.getPath(); }
                @Override public List<Cheats.Cheat> load() { return read(f); }
                @Override public boolean save(List<Cheats.Cheat> list) { return write(f, format(list)); }
                @Override public boolean strictCodes() { return false; }
                @Override public Cheats.Cheat newCheat(String name, List<String> codes) { return null; }
                @Override public int download() { return -1; }
            });
        }
        return out;
    }

    /** Cheap check before parsing: does the file hold any English cheat names at all? */
    static boolean mayNeedTranslation(File f) {
        try {
            String t = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("_desc\\s*=\\s*\"([^\"]*)\"").matcher(t);
            while (m.find()) if (Translate.needs(m.group(1))) return true;
        } catch (IOException ignored) {
        }
        return false;
    }
}
