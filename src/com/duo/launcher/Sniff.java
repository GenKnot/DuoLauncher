package com.duo.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tells which console a file is for by looking inside it, for file types several consoles share (.iso, .rvz, .ciso,
 * .chd, .cue, .zip, .m3u) when the folder name does not say. Only a few bytes are read, so a big card stays quick to
 * scan. Returns a system key of {@link Library} or null when the file gives no clear answer.
 */
final class Sniff {
    private Sniff() { }

    static String system(File f, String ext) {
        try {
            switch (ext) {
                case "iso": return iso(f);
                case "rvz": case "wia": return wia(f);
                case "ciso": return discAt(f, 0x8000);
                case "chd": return chd(f);
                case "cue": return cue(f);
                case "m3u": return m3u(f);
                case "zip": return zip(f);
                default: return null;
            }
        } catch (IOException | RuntimeException | StackOverflowError | OutOfMemoryError e) {
            return null;
        }
    }

    private static byte[] read(File f, long at, int n) throws IOException {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            if (r.length() < at + n) return null;
            byte[] b = new byte[n];
            r.seek(at);
            r.readFully(b);
            return b;
        }
    }

    private static String ascii(byte[] b, int from, int n) {
        return b == null || b.length < from + n ? "" : new String(b, from, n, StandardCharsets.ISO_8859_1);
    }

    private static long u32(byte[] b, int at) {
        return ((b[at] & 0xFFL) << 24) | ((b[at + 1] & 0xFFL) << 16) | ((b[at + 2] & 0xFFL) << 8) | (b[at + 3] & 0xFFL);
    }

    /** A GameCube / Wii disc image whose disc header starts at {@code at}: the magic words tell them apart. */
    private static String discAt(File f, long at) throws IOException {
        byte[] h = read(f, at, 0x20);
        if (h == null) return null;
        if (u32(h, 0x18) == 0x5D1C9EA3L) return "WII";
        if (u32(h, 0x1C) == 0xC2339F3DL) return "NGC";
        return null;
    }

    /** ISO images: GameCube / Wii, PSP, PS2 / PS1, Saturn, Mega-CD and 3DO each mark their discs differently. */
    private static String iso(File f) throws IOException {
        String gc = discAt(f, 0);
        if (gc != null) return gc;
        byte[] head = read(f, 0, 0x30);
        if (head != null) {
            if (ascii(head, 0, 15).equals("SEGA SEGASATURN")) return "SATURN";
            if (ascii(head, 0, 14).equals("SEGADISCSYSTEM") || ascii(head, 0, 11).equals("SEGA MEGA C")) return "SEGACD";
            if ((head[0] & 0xFF) == 0x01 && ascii(head, 1, 5).equals("ZZZZZ")) return "3DO";
        }
        // ISO 9660: the system identifier of the primary volume descriptor
        byte[] pvd = read(f, 0x8000, 0x28);
        if (pvd == null || !ascii(pvd, 1, 5).equals("CD001")) return null;
        String sys = ascii(pvd, 8, 32).trim();
        if (sys.startsWith("PSP GAME")) return "PSP";
        if (sys.startsWith("PLAYSTATION")) return f.length() > 900L * 1024 * 1024 ? "PS2" : "PS1";
        return null;
    }

    /** RVZ / WIA keep a disc type in their header: 1 GameCube, 2 Wii. */
    private static String wia(File f) throws IOException {
        byte[] h = read(f, 0, 0x4C);
        if (h == null) return null;
        String magic = ascii(h, 0, 3);
        if (!magic.equals("RVZ") && !magic.equals("WIA")) return null;
        long type = u32(h, 0x48);
        return type == 2 ? "WII" : type == 1 ? "NGC" : null;
    }

    /** CHD: a GD-ROM is a Dreamcast disc, a DVD a PS2 one; CDs are left to the folder name. */
    private static String chd(File f) throws IOException {
        byte[] h = read(f, 0, 0x10);
        if (h == null || !ascii(h, 0, 8).equals("MComprHD")) return null;
        long len = Math.min(f.length(), 1 << 16);
        byte[] b = read(f, 0, (int) len);
        String s = ascii(b, 0, b.length);
        if (s.contains("CHGD") || s.contains("CHGT")) return "DC";
        if (s.contains("DVD ")) return "PS2";
        return null;
    }

    /** A CUE sheet: look at the first data track's start, where PS1, Saturn and Mega-CD discs say what they are. */
    private static String cue(File f) throws IOException {
        File bin = null;
        // a real CUE sheet is a few lines; read at most 64 KB so a big file misnamed .cue costs nothing
        byte[] head = read(f, 0, (int) Math.min(f.length(), 65536));
        if (head == null) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(head), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                String t = line.trim();
                if (t.toUpperCase(Locale.ROOT).startsWith("FILE")) {
                    int a = t.indexOf('"'), b = t.lastIndexOf('"');
                    String name = a >= 0 && b > a ? t.substring(a + 1, b) : t.substring(4).trim().split("\\s+")[0];
                    bin = new File(f.getParentFile(), name);
                    break;
                }
            }
        }
        if (bin == null || !bin.isFile()) return null;
        byte[] h = read(bin, 0, 0x30);
        if (h != null) {
            if (ascii(h, 0x10, 15).equals("SEGA SEGASATURN")) return "SATURN";
            if (ascii(h, 0x10, 14).equals("SEGADISCSYSTEM")) return "SEGACD";
            if ((h[0x10] & 0xFF) == 0x01 && ascii(h, 0x11, 5).equals("ZZZZZ")) return "3DO";
        }
        // raw 2352-byte sectors: the volume descriptor of sector 16 sits at 16 * 2352 + 24
        byte[] pvd = read(bin, 16L * 2352 + 24, 0x28);
        if (pvd != null && ascii(pvd, 1, 5).equals("CD001") && ascii(pvd, 8, 32).trim().startsWith("PLAYSTATION")) return "PS1";
        return null;
    }

    /** A disc list: whatever its first disc is. */
    private static String m3u(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                File d = new File(f.getParentFile(), t);
                if (!d.isFile()) return null;
                String e = Library.extension(d.getName());
                if (e.equalsIgnoreCase("m3u")) return null;   // a list naming a list (or itself) says nothing
                String byExt = Library.systemForExtension(e);
                return byExt != null ? byExt : system(d, e);
            }
        }
        return null;
    }

    /** A zip holding one game: the type of the file inside (arcade sets, with many parts, are left alone). */
    private static String zip(File f) throws IOException {
        String found = null;
        int files = 0;
        try (ZipFile z = new ZipFile(f)) {
            for (Enumeration<? extends ZipEntry> en = z.entries(); en.hasMoreElements(); ) {
                ZipEntry e = en.nextElement();
                if (e.isDirectory()) continue;
                if (++files > 4) return null;
                String s = Library.systemForExtension(Library.extension(e.getName()));
                if (s != null) {
                    if (found != null && !found.equals(s)) return null;
                    found = s;
                }
            }
        }
        return found;
    }
}
