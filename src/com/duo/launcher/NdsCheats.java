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
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * NDS cheats for DraStic. DraStic keeps a game's own ("custom") cheats as text in
 * DraStic/cheats/<game file name>.cht: "[name]+" for a cheat that is on, "[name]" for one that is off, then its
 * code lines. Unlike its built-in database these work for any dump, including translated ROMs.
 * Cheats come from DraStic's usrcheat.dat (the R4 database, matched by game code only, so patched ROMs match too)
 * or from libretro's database (matched by game code through a bundled index).
 */
final class NdsCheats {
    private static final String LIBRETRO = "https://raw.githubusercontent.com/libretro/libretro-database/master/cht/Nintendo%20-%20Nintendo%20DS/";

    static File home() {
        return new File(Environment.getExternalStorageDirectory(), "DraStic");
    }

    static boolean isDrastic(String cmd) {
        return cmd != null && cmd.contains("com.dsemu.drastic");
    }

    /** DraStic crashes when a game's custom cheats hold more than this many code lines in total. */
    static final int DRASTIC_MAX_LINES = 256;

    private static String base(Library.Game g) {
        String n = g.path.getName();
        int dot = n.lastIndexOf('.');
        return dot > 0 ? n.substring(0, dot) : n;
    }

    /** What DraStic reads: only the cheats that are switched on. */
    static File file(Library.Game g) {
        return new File(new File(home(), "cheats"), base(g) + ".cht");
    }

    /** The whole cheat list for a game, kept by the launcher (DraStic could not load it all). */
    static File library(Library.Game g) {
        return new File(Environment.getExternalStorageDirectory(), "DuoLauncher/cheats/NDS/" + base(g) + ".cht");
    }

    static int codeLines(List<Cheats.Cheat> list) {
        int n = 0;
        for (Cheats.Cheat c : list) if (c.enabled) for (String l : c.lines) if (!l.startsWith("*")) n++;
        return n;
    }

    private static List<Cheats.Cheat> read(File f) {
        try {
            return f.isFile() ? parse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)) : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // ---- DraStic's text format ----

    static List<Cheats.Cheat> parse(String text) {
        List<Cheats.Cheat> out = new ArrayList<>();
        Cheats.Cheat cur = null;
        for (String raw : text.replace("﻿", "").split("\r?\n")) {
            String line = raw.trim();
            if (line.startsWith("[") && (line.endsWith("]") || line.endsWith("]+"))) {
                cur = new Cheats.Cheat();
                cur.enabled = line.endsWith("+");
                cur.name = line.substring(1, line.lastIndexOf(']')).trim();
                out.add(cur);
            } else if (cur != null && !line.isEmpty()) {
                cur.lines.add(line);
            }
        }
        return out;
    }

    static String format(List<Cheats.Cheat> list) {
        StringBuilder sb = new StringBuilder();
        for (Cheats.Cheat c : list) {
            sb.append('[').append(c.name.replace("[", "(").replace("]", ")")).append(']').append(c.enabled ? "+" : "").append('\n');
            for (String l : c.lines) if (!l.startsWith("*")) sb.append(l).append('\n');
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Patches written for flash carts (anti-piracy bypasses, overlay checks, Wi-Fi connection fixes). DraStic does
     * not need them and crashes on load with some switched on, so they are left out of downloads.
     */
    private static final java.util.regex.Pattern CART_ONLY = java.util.regex.Pattern.compile(
            "(?i)anti.?piracy|\\bA\\.?P\\.?\\s*(patch|fix|bypass)|overlay check|wfc|bypass|no\\$gba|nitro ?hax|flash ?cart|反盗版|防盗版|去除保护|破解保护");

    static boolean cartOnly(String name) {
        return CART_ONLY.matcher(name).find();
    }

    // ---- DraStic's usrcheat.dat (R4 cheat database) ----

    private static String cstr(byte[] b, int p, int[] next) {
        int e = p;
        while (e < b.length && b[e] != 0) e++;
        next[0] = e + 1;
        return new String(b, p, e - p, StandardCharsets.UTF_8);
    }

    private static int u32(byte[] b, int p) {
        return (b[p] & 0xFF) | (b[p + 1] & 0xFF) << 8 | (b[p + 2] & 0xFF) << 16 | (b[p + 3] & 0xFF) << 24;
    }

    /**
     * Cheats for a game code from usrcheat.dat. Several entries can share a code (regions, translations); the one
     * whose title is marked Chinese wins, then the one with most cheats. Entries whose data lies past the end of
     * the file (a cut-off copy) are skipped.
     */
    static List<Cheats.Cheat> fromUsrcheat(String code) {
        return fromUsrcheat(code, null);
    }

    /** As above; cut[0] counts this game's entries that lie past the end of a cut-off usrcheat.dat. */
    static List<Cheats.Cheat> fromUsrcheat(String code, int[] cut) {
        List<Cheats.Cheat> best = new ArrayList<>();
        File f = new File(home(), "usrcheat.dat");
        if (!f.isFile() || code == null) return best;
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            long size = r.length();
            byte[] head = new byte[16];
            r.readFully(head, 0, 12);
            if (!new String(head, 0, 12, StandardCharsets.US_ASCII).equals("R4 CheatCode")) return best;
            List<Long> offsets = new ArrayList<>();
            // the index (16 bytes a game) is read a block at a time: one seek per game made this slow
            byte[] block = new byte[64 * 1024];
            scan:
            for (long p = 0x100; p + 16 <= size; p += block.length) {
                int got = (int) Math.min(block.length, size - p) / 16 * 16;
                r.seek(p);
                r.readFully(block, 0, got);
                for (int i = 0; i < got; i += 16) {
                    long off = u32(block, i + 8) & 0xFFFFFFFFL;
                    if (off == 0) break scan;
                    if (!new String(block, i, 4, StandardCharsets.US_ASCII).equals(code)) continue;
                    if (off < size) offsets.add(off);
                    else if (cut != null) cut[0]++;
                }
            }
            int bestScore = -1;
            for (long off : offsets) {
                int len = (int) Math.min(256 * 1024, size - off);
                byte[] b = new byte[len];
                r.seek(off);
                r.readFully(b);
                int[] next = new int[1];
                String title = cstr(b, 0, next);
                List<Cheats.Cheat> list = game(b, (next[0] + 3) & ~3);
                int score = list.size() + (title.contains("中") || title.contains("汉化") ? 100000 : 0);
                if (!list.isEmpty() && score > bestScore) { bestScore = score; best = list; }
            }
        } catch (IOException | RuntimeException ignored) {
        }
        return best;
    }

    private static List<Cheats.Cheat> game(byte[] b, int p) {
        List<Cheats.Cheat> out = new ArrayList<>();
        int count = u32(b, p) & 0x0FFFFFFF;
        p += 4 + 32;   // item count, then the master code
        String folder = null;
        int left = 0;
        int[] next = new int[1];
        for (int i = 0; i < count && p + 4 <= b.length; i++) {
            int w = u32(b, p);
            p += 4;
            if ((w & 0x10000000) != 0) {   // a folder; the next (low bits) cheats belong to it
                folder = cstr(b, p, next);
                cstr(b, next[0], next);
                p = (next[0] + 3) & ~3;
                left = w & 0xFFFFFF;
                continue;
            }
            int start = p, words = w & 0xFFFFFF;
            String name = cstr(b, p, next);
            String note = cstr(b, next[0], next);
            int q = (next[0] + 3) & ~3;
            if (q + 4 > b.length) break;
            int n = u32(b, q);
            Cheats.Cheat c = new Cheats.Cheat();
            c.name = (left > 0 && folder != null && !folder.isEmpty() ? folder + " / " : "") + name;
            if (!note.isEmpty()) c.lines.add("*" + note);
            for (int k = 0; k + 1 < n && q + 8 + 4 * k + 4 <= b.length; k += 2)
                c.lines.add(String.format(Locale.ROOT, "%08X %08X", u32(b, q + 4 + 4 * k), u32(b, q + 8 + 4 * k)));
            if (left > 0) left--;
            p = start + words * 4;
            if (c.lines.size() > (note.isEmpty() ? 0 : 1)) out.add(c);
        }
        return out;
    }

    // ---- libretro's database ----

    private static String index(Context c, String code) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open("cheats/NDS.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) if (line.startsWith(code + "\t")) return line.substring(5);
        } catch (IOException ignored) {
        }
        return null;
    }

    private static List<Cheats.Cheat> fromLibretro(Context ctx, String code) {
        List<Cheats.Cheat> out = new ArrayList<>();
        String name = code == null ? null : index(ctx, code);
        if (name == null) return out;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(LIBRETRO + URLEncoder.encode(name, "UTF-8").replace("+", "%20")).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            if (con.getResponseCode() != 200) return out;
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (InputStream in = con.getInputStream()) {
                Backups.copy(in, b);
            }
            for (Cheats.Cheat c : RaCheats.parse(b.toString("UTF-8"))) {
                String codeText = c.props.get("code");
                if (codeText == null) continue;
                String[] w = codeText.replace(" ", "+").split("\\+");
                Cheats.Cheat d = new Cheats.Cheat();
                d.name = c.name;
                for (int k = 0; k + 1 < w.length; k += 2)
                    if (w[k].matches("[0-9A-Fa-f]{8}") && w[k + 1].matches("[0-9A-Fa-f]{8}"))
                        d.lines.add(w[k].toUpperCase(Locale.ROOT) + " " + w[k + 1].toUpperCase(Locale.ROOT));
                if (!d.lines.isEmpty()) out.add(d);
            }
        } catch (IOException ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return out;
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

    static Cheats.Store store(Context ctx, Library.Game g) {
        File own = file(g), lib = library(g);
        return new Cheats.Store() {
            private String note;

            @Override public String id() { return lib.getPath(); }

            /**
             * With nothing here yet: how many cheats DraStic's own list has for the game (DraStic shows those in
             * game, so an empty list here would look wrong), or that its usrcheat.dat is cut off before them.
             */
            @Override public String note() { return note; }

            /**
             * The launcher's list, with on/off taken from DraStic's file; cheats added inside DraStic join the list,
             * so the two never disagree.
             */
            @Override public List<Cheats.Cheat> load() {
                List<Cheats.Cheat> all = read(lib), on = read(own);
                java.util.Map<String, Cheats.Cheat> drastic = new java.util.LinkedHashMap<>();
                for (Cheats.Cheat c : on) drastic.put(c.name, c);
                Set<String> names = new HashSet<>();
                for (Cheats.Cheat c : all) {
                    names.add(c.name);
                    Cheats.Cheat d = drastic.get(c.name);
                    c.enabled = d != null && d.enabled;
                }
                for (Cheats.Cheat d : on) if (names.add(d.name)) all.add(d);
                note = null;
                if (all.isEmpty()) {
                    int[] cut = new int[1];
                    List<Cheats.Cheat> there = fromUsrcheat(Covers.code(g), cut);
                    there.removeIf(c -> cartOnly(c.name));
                    if (!there.isEmpty()) note = I18n.f(I18n.S.NDS_CHEATS_IN_DRASTIC, there.size());
                    else if (cut[0] > 0) note = I18n.t(I18n.S.NDS_USRCHEAT_CUT);
                }
                return all;
            }

            /** Everything to the launcher's list; only what is on to DraStic, refused when DraStic could not take it. */
            @Override public boolean save(List<Cheats.Cheat> list) {
                if (codeLines(list) > DRASTIC_MAX_LINES) return false;
                List<Cheats.Cheat> on = new ArrayList<>();
                for (Cheats.Cheat c : list) if (c.enabled) on.add(c);
                return write(lib, format(list)) && write(own, format(on));
            }

            @Override public boolean strictCodes() { return true; }

            @Override public Cheats.Cheat newCheat(String name, List<String> codes) {
                Cheats.Cheat c = new Cheats.Cheat();
                c.name = name;
                c.lines.addAll(codes);
                return c;
            }

            /** usrcheat.dat first (often Chinese), libretro's database when that has nothing. */
            @Override public int download() {
                String code = Covers.code(g);
                List<Cheats.Cheat> found = fromUsrcheat(code);
                if (found.isEmpty()) found = fromLibretro(ctx, code);
                found.removeIf(c -> cartOnly(c.name));
                if (found.isEmpty()) return -1;
                List<Cheats.Cheat> have = load();
                Set<String> names = new HashSet<>();
                for (Cheats.Cheat c : have) names.add(c.name);
                int added = 0;
                for (Cheats.Cheat c : found) {
                    if (!names.add(c.name)) continue;
                    c.enabled = false;
                    have.add(c);
                    added++;
                }
                if (added > 0 && !save(have)) return -1;
                return added;
            }
        };
    }

    /** Every NDS cheat list (the launcher's) that still has English names, for translating them all at once. */
    static List<Cheats.Store> all() {
        List<Cheats.Store> out = new ArrayList<>();
        File[] fs = new File(Environment.getExternalStorageDirectory(), "DuoLauncher/cheats/NDS").listFiles((d, n) -> n.endsWith(".cht"));
        if (fs == null) return out;
        for (File f : fs) {
            boolean english = false;
            for (Cheats.Cheat c : read(f)) if (Translate.needs(c.name)) { english = true; break; }
            if (!english) continue;
            File own = new File(new File(home(), "cheats"), f.getName());
            out.add(new Cheats.Store() {
                @Override public String id() { return f.getPath(); }
                @Override public List<Cheats.Cheat> load() {
                    List<Cheats.Cheat> all = read(f), on = read(own);
                    Set<String> enabled = new HashSet<>();
                    for (Cheats.Cheat c : on) if (c.enabled) enabled.add(c.name);
                    for (Cheats.Cheat c : all) c.enabled = enabled.contains(c.name);
                    return all;
                }
                @Override public boolean save(List<Cheats.Cheat> list) {
                    List<Cheats.Cheat> onNow = new ArrayList<>();
                    for (Cheats.Cheat c : list) if (c.enabled) onNow.add(c);
                    return write(f, format(list)) && write(own, format(onNow));
                }
                @Override public boolean strictCodes() { return true; }
                @Override public Cheats.Cheat newCheat(String name, List<String> codes) { return null; }
                @Override public int download() { return -1; }
            });
        }
        return out;
    }
}
