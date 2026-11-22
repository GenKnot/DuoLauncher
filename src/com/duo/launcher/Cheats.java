package com.duo.launcher;

import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * 3DS cheats in the format Azahar and Citra share: <emulator home>/cheats/<title ID>.txt with "[name]" blocks;
 * a "*citra_enabled" line inside a block turns it on. Turning a cheat on or off here writes the file for every
 * installed emulator, so switching emulators keeps the same cheats. Missing files can be fetched from the
 * Sharkive database (github.com/FlagBrew/Sharkive), which keeps one file per title ID.
 */
final class Cheats {
    private static final String SHARKIVE = "https://raw.githubusercontent.com/FlagBrew/Sharkive/master/3ds/";
    private static final String ENABLED = "*citra_enabled";

    static final class Cheat {
        String name;
        boolean enabled;
        final List<String> lines = new ArrayList<>();   // notes ("*...") and code lines, without the enabled marker
        final java.util.Map<String, String> props = new java.util.LinkedHashMap<>();   // RetroArch keys (code, handler, ...)
    }

    /** Where one game's cheats live: Azahar/Citra text files, or a RetroArch .cht. */
    interface Store {
        String id();
        List<Cheat> load();
        boolean save(List<Cheat> list);
        /** 3DS codes are always "XXXXXXXX YYYYYYYY"; RetroArch cores take many formats. */
        boolean strictCodes();
        Cheat newCheat(String name, List<String> codes);
        /** Adds cheats from a database; the number added, or -1 when there are none or the download failed. */
        int download();
    }

    /** The cheat store of a 3DS game, shared by every Azahar and Citra build on the device. */
    static Store citra(String tid) {
        return new Store() {
            @Override public String id() { return tid; }
            @Override public List<Cheat> load() { return Cheats.load(tid); }
            @Override public boolean save(List<Cheat> list) { return Cheats.save(tid, list); }
            @Override public boolean strictCodes() { return true; }
            @Override public Cheat newCheat(String name, List<String> codes) {
                Cheat c = new Cheat();
                c.name = name;
                c.lines.addAll(codes);
                return c;
            }
            @Override public int download() { return Cheats.download(tid); }
        };
    }

    /** Emulator homes that exist on this device: AzaharPlus / Azahar use "azahar", Citra MMJ "citra-emu". */
    static List<File> dirs() {
        List<File> out = new ArrayList<>();
        File ext = Environment.getExternalStorageDirectory();
        for (String home : new String[]{"azahar", "citra-emu"}) {
            File h = new File(ext, home);
            if (h.isDirectory()) out.add(new File(h, "cheats"));
        }
        return out;
    }

    /** 16-digit title ID of a 3DS game, from the launcher's data or the ROM header; null when unreadable (encrypted). */
    static String titleId(Library.Game g) {
        if (g.titleId != null && g.titleId.matches("[0-9A-F]{16}")) return g.titleId;
        if (g.path == null) return null;
        Object[] info = Icons.ctrInfo(g.path);
        if (info == null || info[0] == null) return null;
        long pid = (Long) info[0];
        return pid == 0 ? null : String.format(Locale.ROOT, "%016X", pid);
    }

    /** The newest cheat file among the emulators, or null. */
    static File file(String tid) {
        File best = null;
        for (File d : dirs()) {
            File f = new File(d, tid + ".txt");
            if (f.isFile() && (best == null || f.lastModified() > best.lastModified())) best = f;
        }
        return best;
    }

    static List<Cheat> load(String tid) {
        File f = file(tid);
        if (f == null) return new ArrayList<>();
        try {
            return parse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    static List<Cheat> parse(String text) {
        List<Cheat> out = new ArrayList<>();
        Cheat cur = null;
        boolean inNote = false;
        for (String raw : text.replace("﻿", "").split("\r?\n")) {
            String line = raw.trim();
            if (inNote) {                                 // Sharkive / CTRPF notes in { } may span lines
                if (cur != null && !line.replace("}", "").isEmpty()) cur.lines.add("*" + line.replace("}", "").trim());
                if (line.contains("}")) inNote = false;
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                cur = new Cheat();
                cur.name = line.substring(1, line.length() - 1).trim();
                out.add(cur);
            } else if (cur == null || line.isEmpty()) {
                // text before the first cheat and blank lines are dropped
            } else if (line.equalsIgnoreCase(ENABLED)) {
                cur.enabled = true;
            } else if (line.startsWith("{")) {
                String note = line.substring(1).replace("}", "").trim();
                if (!note.isEmpty()) cur.lines.add("*" + note);
                inNote = !line.contains("}");
            } else {
                cur.lines.add(line);
            }
        }
        return out;
    }

    static String format(List<Cheat> cheats) {
        StringBuilder sb = new StringBuilder();
        for (Cheat c : cheats) {
            sb.append('[').append(c.name).append("]\n");
            if (c.enabled) sb.append(ENABLED).append('\n');
            for (String l : c.lines) sb.append(l).append('\n');
            sb.append('\n');
        }
        return sb.toString();
    }

    private static boolean writtenHere(File f) {
        try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            return text.equals(format(parse(text)));
        } catch (IOException e) {
            return false;
        }
    }

    /** Title IDs of every game that has a cheat file in any emulator folder. */
    static List<String> allTitleIds() {
        Set<String> ids = new java.util.TreeSet<>();
        for (File d : dirs()) {
            File[] fs = d.listFiles();
            if (fs != null) for (File x : fs) {
                String n = x.getName();
                if (n.matches("[0-9A-Fa-f]{16}\\.txt")) ids.add(n.substring(0, 16).toUpperCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(ids);
    }

    /** The English name a translated cheat keeps as its first note, or null. */
    static String original(Cheat c) {
        if (c.lines.isEmpty() || !c.lines.get(0).startsWith("*")) return null;
        String o = c.lines.get(0).substring(1).trim();
        return Translate.needs(o) ? o : null;
    }

    /** Still English, a translator's "original (translation)", or a translation with English words left in it. */
    static boolean untranslated(Cheat c) {
        return Translate.needs(c.name) || Translate.repair(c.name) != null || (original(c) != null && Translate.latin(c.name) > 0);
    }

    /** Translates every untranslated cheat name of one game; returns {translated, left untranslated}. */
    static int[] translate(Store st, Translate tr) {
        List<Cheat> list = st.load();
        int n = 0, left = 0;
        boolean ai = tr.usesAi();
        if (ai) {
            // the whole game in one go: names never translated, and (to improve them) ones translated before
            List<String> src = new ArrayList<>();
            for (Cheat c : list) {
                String o = original(c);
                if (o != null) src.add(o);
                else if (Translate.needs(c.name)) src.add(c.name);
            }
            String given = tr.context;   // the game's name when one game is translated; else the file name
            if (given == null) tr.context = new File(st.id()).getName().replaceAll("\\.[A-Za-z0-9]+$", "");
            tr.prefetch(src);
            tr.context = given;
            ai = tr.usesAi();
        }
        for (Cheat c : list) {
            String fixed = Translate.repair(c.name);   // an earlier "original (translation)" answer
            if (fixed != null) { c.name = fixed; n++; continue; }
            String o = original(c);
            if (o != null && ai) {   // translated before: the AI's answer replaces it
                String z = tr.translate(o);
                if (z != null && !z.equals(c.name)) { c.name = z; n++; }
                continue;
            }
            if (o != null && Translate.latin(c.name) > 0) {   // translated before, English left in it: try again from the original
                String z = tr.translate(o);
                if (z != null && Translate.latin(z) < Translate.latin(c.name)) { c.name = z; n++; }   // otherwise it is as good as it gets (Mega, START)
                continue;
            }
            if (!Translate.needs(c.name)) continue;
            String z = tr.translate(c.name);
            if (z == null) { left++; continue; }
            c.lines.add(0, "*" + c.name);   // the original name stays as a note
            c.name = z;
            n++;
        }
        if (n > 0) st.save(list);
        return new int[]{n, left};
    }

    /** Writes the cheats for every installed emulator. */
    static boolean save(String tid, List<Cheat> cheats) {
        boolean ok = false;
        byte[] data = format(cheats).getBytes(StandardCharsets.UTF_8);
        for (File d : dirs()) {
            d.mkdirs();
            File tmp = new File(d, tid + ".txt.part"), f = new File(d, tid + ".txt"), bak = new File(d, tid + ".txt.bak");
            // the first time a file someone wrote by hand is rewritten, keep the original next to it
            // (a file in exactly the form written here came from the launcher and needs no copy)
            if (f.isFile() && !bak.exists() && !writtenHere(f)) {
                try (InputStream in = new java.io.FileInputStream(f); OutputStream o = new FileOutputStream(bak)) {
                    Backups.copy(in, o);
                } catch (IOException ignored) {
                }
            }
            try (OutputStream o = new FileOutputStream(tmp)) {
                o.write(data);
            } catch (IOException e) {
                continue;
            }
            ok |= tmp.renameTo(f) || (f.delete() && tmp.renameTo(f));
        }
        return ok;
    }

    /**
     * Fetches the game's cheats from Sharkive and adds the ones not already there (existing cheats and their
     * on/off state stay as they are). Returns how many were added; 0 when the database has nothing new;
     * -1 when the database has no file for this game or the download failed.
     */
    static int download(String tid) {
        String text;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(SHARKIVE + tid + ".txt").openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            if (con.getResponseCode() != 200) return -1;
            try (InputStream in = con.getInputStream()) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                Backups.copy(in, b);
                text = b.toString("UTF-8");
            }
        } catch (IOException e) {
            return -1;
        } finally {
            if (con != null) con.disconnect();
        }
        List<Cheat> have = load(tid), fetched = parse(text);
        Set<String> names = new HashSet<>();
        for (Cheat c : have) names.add(c.name.toLowerCase(Locale.ROOT));
        int added = 0;
        for (Cheat c : fetched) {
            if (c.lines.isEmpty() || !names.add(c.name.toLowerCase(Locale.ROOT))) continue;   // headings without codes, duplicates
            c.enabled = false;
            have.add(c);
            added++;
        }
        if (added > 0 && !save(tid, have)) return -1;
        return added;
    }
}
