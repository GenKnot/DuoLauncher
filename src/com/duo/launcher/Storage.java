package com.duo.launcher;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Where the launcher writes its own data (the DuoLauncher folder: console data, pictures, backups, exports,
 * themes, the copy of settings and records). The player picks one of three places; reading always looks on
 * every storage, so data written before a change keeps working where it is.
 */
final class Storage {
    private Storage() { }

    /** Next to the games: each console's data on the storage its games are on (the original behaviour). */
    static final String AUTO = "auto";
    /** Always on the removable card, so it travels with the card. */
    static final String CARD = "card";
    /** Always on the handheld's own storage, so it stays when cards are swapped. */
    static final String INTERNAL = "internal";

    /** The chosen place, set from the prefs at start-up and whenever it changes. */
    static volatile String mode = AUTO;
    /** The removable card's root, or null when none is mounted. */
    private static volatile File card;

    static void init(Context c) {
        File found = null;
        StorageManager sm = c.getSystemService(StorageManager.class);
        for (StorageVolume v : sm.getStorageVolumes())
            if (v.isRemovable() && v.getDirectory() != null && Environment.MEDIA_MOUNTED.equals(v.getState())) { found = v.getDirectory(); break; }
        card = found;
    }

    static File internal() { return Environment.getExternalStorageDirectory(); }

    static File card() { return card; }

    /** The DuoLauncher folder for data that belongs to a game on {@code gameVolume} (a storage root). */
    static File root(File gameVolume) {
        switch (mode) {
            case CARD: return new File(card != null ? card : internal(), "DuoLauncher");
            case INTERNAL: return new File(internal(), "DuoLauncher");
            default: return new File(gameVolume != null ? gameVolume : internal(), "DuoLauncher");
        }
    }

    /** The DuoLauncher folder for data that belongs to no game (backups, exports, themes, the settings copy). */
    static File general(List<File> volumeRoots) {
        if (!mode.equals(AUTO)) return root(null);
        // next to the games: the card that already has the launcher's folder, else any storage that has it
        String in = internal().getAbsolutePath();
        for (File v : volumeRoots)
            if (!v.getAbsolutePath().equals(in) && new File(v, "DuoLauncher").isDirectory()) return new File(v, "DuoLauncher");
        for (File v : volumeRoots) if (new File(v, "DuoLauncher").isDirectory()) return new File(v, "DuoLauncher");
        return new File(internal(), "DuoLauncher");
    }

    /** The storage root a file is on ("/storage/4A21-0000" or the internal storage). */
    static File volumeOf(File f) {
        String p = f.getAbsolutePath(), in = internal().getAbsolutePath();
        if (p.startsWith(in + "/") || p.equals(in)) return internal();
        if (p.startsWith("/storage/")) {
            int s = p.indexOf('/', 9);
            return new File(s < 0 ? p : p.substring(0, s));
        }
        File parent = f.getParentFile();
        return parent != null ? parent : f;
    }

    /** Every DuoLauncher folder that exists on a storage other than {@code target}'s. */
    static List<File> others(List<File> volumeRoots, File target) {
        List<File> out = new ArrayList<>();
        for (File v : volumeRoots) {
            File d = new File(v, "DuoLauncher");
            if (d.isDirectory() && !d.getAbsolutePath().equals(target.getAbsolutePath())) out.add(d);
        }
        return out;
    }

    /**
     * Moves everything from {@code from} into {@code to}, file by file; a file that already exists there stays
     * where it is (both are still read). Returns {moved, left}.
     */
    static int[] move(File from, File to) {
        int[] n = {0, 0};
        File[] top = from.listFiles();
        if (top == null) return n;
        for (File f : top) {
            // NDS cheat lists and multi-disc playlists are read from a fixed place on internal storage
            if (STAY.contains(f.getName().toLowerCase(java.util.Locale.ROOT))) continue;
            File dst = new File(to, f.getName());
            if (f.isDirectory()) {
                moveTree(f, dst, n);
                String[] left = f.list();
                if (left != null && left.length == 0) f.delete();
            } else moveFile(f, dst, n);
        }
        return n;
    }

    private static final java.util.Set<String> MERGE = new java.util.HashSet<>(java.util.Arrays.asList("genres.txt", "series.txt"));

    /** Adds the lines of {@code from} whose game (the first field) {@code to} does not have yet; {@code to} wins. */
    private static boolean mergeLines(File from, File to) {
        java.nio.charset.Charset u = java.nio.charset.StandardCharsets.UTF_8;
        try {
            List<String> have = java.nio.file.Files.readAllLines(to.toPath(), u), out = new ArrayList<>(have);
            java.util.Set<String> keys = new java.util.HashSet<>();
            for (String l : have) keys.add(l.split("	", 2)[0]);
            for (String l : java.nio.file.Files.readAllLines(from.toPath(), u))
                if (!l.trim().isEmpty() && keys.add(l.split("	", 2)[0])) out.add(l);
            File tmp = new File(to.getPath() + ".tmp");
            java.nio.file.Files.write(tmp.toPath(), out, u);
            if (!tmp.renameTo(to)) { to.delete(); if (!tmp.renameTo(to)) return false; }
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private static final java.util.Set<String> STAY = new java.util.HashSet<>(java.util.Arrays.asList("cheats", "m3u"));

    /** How much {@link #move} would move from these folders. */
    static long movable(List<File> froms) {
        long n = 0;
        for (File from : froms) {
            File[] top = from.listFiles();
            if (top != null) for (File f : top) if (!STAY.contains(f.getName().toLowerCase(java.util.Locale.ROOT))) n += Config.size(f);
        }
        return n;
    }

    private static void moveTree(File from, File to, int[] n) {
        File[] fs = from.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            File dst = new File(to, f.getName());
            if (f.isDirectory()) {
                moveTree(f, dst, n);
                String[] left = f.list();
                if (left != null && left.length == 0) f.delete();
                continue;
            }
            moveFile(f, dst, n);
        }
    }

    private static void moveFile(File f, File dst, int[] n) {
        if (dst.exists()) {
            // genre and series lists are rewritten whole from one place: merge them, so nothing is left behind
            if (MERGE.contains(f.getName()) && mergeLines(f, dst)) { f.delete(); n[0]++; } else n[1]++;
            return;
        }
        dst.getParentFile().mkdirs();
        // across storages a rename fails: copy, check the size, then delete
        if (!f.renameTo(dst)) {
            try (java.io.InputStream in = new java.io.FileInputStream(f); java.io.OutputStream o = new java.io.FileOutputStream(dst)) {
                Backups.copy(in, o);
            } catch (java.io.IOException e) {
                dst.delete();
                n[1]++;
                return;
            }
            if (dst.length() != f.length()) { dst.delete(); n[1]++; return; }
            f.delete();
        }
        n[0]++;
    }
}
