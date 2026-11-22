package com.duo.launcher;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * PSP cheats for PPSSPP: PSP/Cheats/<game ID>.ini in CWCheat form ("_S" game ID, "_G" name, "_C1"/"_C0" a cheat
 * that is on/off, "_L" code lines). Missing ones come from a cheat.db (PPSSPP's own copy on the device first, then
 * the CWCheat-Database-Plus+ download), the same format with one section per game.
 */
final class PspCheats {
    private static final String DB_URL = "https://raw.githubusercontent.com/Saramagrean/CWCheat-Database-Plus-/master/cheat.db";

    static File dir() {
        return new File(Environment.getExternalStorageDirectory(), "PSP/Cheats");
    }

    static boolean isPpsspp(String cmd) {
        return cmd != null && cmd.contains("org.ppsspp.ppsspp");
    }

    static File file(String id) {
        return new File(dir(), id.replace("-", "") + ".ini");
    }

    /** Lines before the first cheat ("_S", "_G"), kept as they are. */
    static final class Parsed {
        final List<String> head = new ArrayList<>();
        final List<Cheats.Cheat> cheats = new ArrayList<>();
    }

    static Parsed parse(String text) {
        Parsed p = new Parsed();
        Cheats.Cheat cur = null;
        for (String raw : text.replace("﻿", "").split("\r?\n")) {
            String line = raw.trim();
            if (line.startsWith("_C")) {
                cur = new Cheats.Cheat();
                cur.enabled = line.length() > 2 && line.charAt(2) != '0';
                cur.name = line.length() > 3 ? line.substring(3).trim() : "?";
                p.cheats.add(cur);
            } else if (line.isEmpty()) {
                // blank lines separate cheats
            } else if (cur == null) {
                p.head.add(line);
            } else if (line.startsWith("_L") || line.startsWith("_M") || line.startsWith("_N")) {
                cur.lines.add(line);
            } else {
                cur.lines.add("*" + line);   // anything else (comments) is kept as a note
            }
        }
        return p;
    }

    static String format(List<String> head, List<Cheats.Cheat> list) {
        StringBuilder sb = new StringBuilder();
        for (String h : head) sb.append(h).append('\n');
        sb.append('\n');
        for (Cheats.Cheat c : list) {
            sb.append(c.enabled ? "_C1 " : "_C0 ").append(c.name).append('\n');
            for (String l : c.lines) if (!l.startsWith("*")) sb.append(l).append('\n');
            sb.append('\n');
        }
        return sb.toString();
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

    private static Parsed read(File f) {
        try {
            return f.isFile() ? parse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)) : new Parsed();
        } catch (IOException e) {
            return new Parsed();
        }
    }

    /** The section for one game in a cheat.db, streamed so the 10 MB file is never held whole. */
    static List<Cheats.Cheat> fromDb(File db, String id) {
        List<Cheats.Cheat> out = new ArrayList<>();
        if (!db.isFile()) return out;
        String want = "_S " + id;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(db), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder section = null;
            while ((line = r.readLine()) != null) {
                String t = line.trim();
                if (t.startsWith("_S ")) {
                    if (section != null) break;
                    if (t.equalsIgnoreCase(want)) section = new StringBuilder();
                    continue;
                }
                if (section != null) section.append(t).append('\n');
            }
            if (section != null) out.addAll(parse(section.toString()).cheats);
        } catch (IOException ignored) {
        }
        return out;
    }

    /** The downloaded database, refreshed at most once a week. */
    private static File downloadedDb(Context ctx) {
        File f = new File(ctx.getFilesDir(), "psp-cheat.db");
        if (f.isFile() && System.currentTimeMillis() - f.lastModified() < 7L * 24 * 3600 * 1000) return f;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(DB_URL).openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(60000);
            if (con.getResponseCode() != 200) return f;
            File tmp = new File(f.getPath() + ".part");
            try (InputStream in = con.getInputStream(); OutputStream o = new FileOutputStream(tmp)) {
                Backups.copy(in, o);
            }
            if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f); }
        } catch (IOException ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
        return f;
    }

    static Cheats.Store store(Context ctx, Library.Game g, String id) {
        File own = file(id);
        return new Cheats.Store() {
            @Override public String id() { return own.getPath(); }

            @Override public List<Cheats.Cheat> load() { return read(own).cheats; }

            @Override public boolean save(List<Cheats.Cheat> list) {
                Parsed p = read(own);
                if (p.head.isEmpty()) { p.head.add("_S " + id); p.head.add("_G " + g.title); }
                return write(own, format(p.head, list));
            }

            @Override public boolean strictCodes() { return true; }

            @Override public Cheats.Cheat newCheat(String name, List<String> codes) {
                Cheats.Cheat c = new Cheats.Cheat();
                c.name = name;
                for (String l : codes) {
                    String[] w = l.split(" ");
                    c.lines.add("_L 0x" + w[0] + " 0x" + w[1]);
                }
                return c;
            }

            /** PPSSPP's own cheat.db on the device first, then the downloaded CWCheat database. */
            @Override public int download() {
                List<Cheats.Cheat> found = fromDb(new File(dir(), "cheat.db"), id);
                if (found.isEmpty()) found = fromDb(downloadedDb(ctx), id);
                if (found.isEmpty()) return -1;
                List<Cheats.Cheat> have = load();
                Set<String> names = new HashSet<>();
                for (Cheats.Cheat c : have) names.add(c.name);
                int added = 0;
                for (Cheats.Cheat c : found) {
                    if (c.lines.isEmpty() || !names.add(c.name)) continue;
                    c.enabled = false;
                    have.add(c);
                    added++;
                }
                if (added > 0 && !save(have)) return -1;
                return added;
            }
        };
    }

    /** Every PPSSPP cheat file that still has English names, for translating them all at once. */
    static List<Cheats.Store> all() {
        List<Cheats.Store> out = new ArrayList<>();
        File[] fs = dir().listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".ini"));
        if (fs == null) return out;
        for (File f : fs) {
            Parsed p = read(f);
            boolean english = false;
            for (Cheats.Cheat c : p.cheats) if (Translate.needs(c.name)) { english = true; break; }
            if (!english) continue;
            out.add(new Cheats.Store() {
                @Override public String id() { return f.getPath(); }
                @Override public List<Cheats.Cheat> load() { return read(f).cheats; }
                @Override public boolean save(List<Cheats.Cheat> list) { return write(f, format(read(f).head, list)); }
                @Override public boolean strictCodes() { return true; }
                @Override public Cheats.Cheat newCheat(String name, List<String> codes) { return null; }
                @Override public int download() { return -1; }
            });
        }
        return out;
    }
}
