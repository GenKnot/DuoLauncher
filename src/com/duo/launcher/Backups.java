package com.duo.launcher;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Save files of the usual emulators, zipped into DuoLauncher/backups/ and unzipped back in place.
 * Paths in the zip are relative to internal storage, so a backup restores onto any device.
 */
final class Backups {
    /** Folders under internal storage that hold saves and save states. */
    private static final String[] SAVE_DIRS = {
            "DraStic/backup", "DraStic/savestates",
            "RetroArch/saves", "RetroArch/states",
            "PSP/SAVEDATA", "PSP/PPSSPP_STATE",
            "aethersx2DATA/memcards", "aethersx2DATA/sstates",
            "duckstation/memcards", "duckstation/savestates",
            "M64PlusFZ/GameData",
    };
    /** 3DS user folders: only title/<type>/<id>/data (the saves), never the installed game content. */
    private static final String[] CTR_HOMES = {"azahar", "citra-emu"};

    private static File internal() { return Environment.getExternalStorageDirectory(); }

    /** Backups live in the launcher's own folder, on the storage the player chose (see {@link Storage}). */
    static File folder(List<File> volumeRoots) { return new File(Storage.general(volumeRoots), "backups"); }

    static List<File> list(File dir) {
        File[] fs = dir.listFiles((d, n) -> n.endsWith(".zip"));
        List<File> out = new ArrayList<>(fs == null ? new ArrayList<>() : Arrays.asList(fs));
        out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return out;
    }

    /** Writes a new backup and returns it, or throws when nothing could be written. */
    static File backup(File dir) throws IOException { return backup(dir, "saves-"); }

    /** Automatic backups start with this, so that only they are pruned. */
    static final String AUTO = "auto-saves-";

    static File backup(File dir, String prefix) throws IOException {
        dir.mkdirs();
        File out = new File(dir, prefix + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date()) + ".zip");
        File part = new File(dir, out.getName() + ".part");
        File root = internal();
        int n = 0;
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(part))) {
            for (String d : SAVE_DIRS) n += add(z, root, new File(root, d), false);
            for (String home : CTR_HOMES) n += add(z, root, new File(root, home + "/sdmc/Nintendo 3DS"), true);
            if (n == 0) z.putNextEntry(new ZipEntry("DuoLauncher/empty-backup.txt"));
        } catch (IOException | RuntimeException e) {
            part.delete();
            throw e;
        }
        out.delete();
        if (!part.renameTo(out)) { part.delete(); throw new IOException("could not write " + out); }
        return out;
    }

    private static int add(ZipOutputStream z, File root, File f, boolean ctrDataOnly) throws IOException {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            int n = 0;
            if (fs != null) for (File x : fs) n += add(z, root, x, ctrDataOnly);
            return n;
        }
        if (!f.isFile()) return 0;
        String rel = f.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
        if (ctrDataOnly && !(rel.contains("/title/") && rel.contains("/data/"))) return 0;
        z.putNextEntry(new ZipEntry(rel));
        try (InputStream in = new FileInputStream(f)) { copy(in, z); }
        z.closeEntry();
        return 1;
    }

    /** Zips the given save files (all on internal storage) as {@code name}-date.zip; same layout as a full backup. */
    static File backupFiles(File dir, String name, List<File> files) throws IOException {
        dir.mkdirs();
        File out = new File(dir, name + "-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date()) + ".zip");
        File part = new File(dir, out.getName() + ".part");
        String root = internal().getAbsolutePath() + File.separator;
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(part))) {
            for (File f : files) {
                String p = f.getAbsolutePath();
                if (!p.startsWith(root) || !f.isFile()) continue;
                z.putNextEntry(new ZipEntry(p.substring(root.length())));
                try (InputStream in = new FileInputStream(f)) { copy(in, z); }
                z.closeEntry();
            }
        } catch (IOException | RuntimeException e) {
            part.delete();
            throw e;
        }
        out.delete();
        if (!part.renameTo(out)) { part.delete(); throw new IOException("could not write " + out); }
        return out;
    }

    /**
     * The save files of one game in the emulators' save folders: files named after the ROM, folders or files named
     * with its PSP game ID, and a 3DS title's data folder. PS2 memory cards hold every game and are not included.
     */
    static List<File> gameSaves(Library.Game g) {
        List<File> out = new ArrayList<>();
        if (g.path == null) return out;
        String base = new File(g.baseName()).getName().toLowerCase(Locale.ROOT);
        String psp = null;
        String n = g.path.getName().toLowerCase(Locale.ROOT);
        if (n.endsWith(".iso") || n.endsWith(".cso") || n.endsWith(".pbp")) {
            String id = PspId.of(g.path);
            if (id != null) psp = id.toLowerCase(Locale.ROOT).replace("-", "");
        }
        String ctr = g.titleId != null && g.titleId.length() == 16 ? g.titleId.substring(8).toLowerCase(Locale.ROOT) : null;
        File root = internal();
        java.util.ArrayDeque<File[]> todo = new java.util.ArrayDeque<>();
        for (String d : SAVE_DIRS) todo.add(new File[]{new File(root, d), null});
        if (ctr != null) for (String home : CTR_HOMES) todo.add(new File[]{new File(root, home + "/sdmc/Nintendo 3DS"), null});
        int seen = 0;
        while (!todo.isEmpty() && seen < 20000) {
            File[] e = todo.poll();
            File[] fs = e[0].listFiles();
            if (fs == null) continue;
            boolean all = e[1] != null;   // inside a folder that belongs to the game
            for (File f : fs) {
                seen++;
                String fn = f.getName().toLowerCase(Locale.ROOT);
                boolean mine = all || base.length() >= 3 && fn.startsWith(base) || psp != null && fn.replace("-", "").startsWith(psp)
                        || ctr != null && f.isDirectory() && fn.equals(ctr) && f.getParent().contains("/title/");
                if (f.isDirectory()) todo.add(new File[]{f, mine ? f : null});
                else if (mine) out.add(f);
            }
        }
        if (ctr != null) out.removeIf(f -> !f.getAbsolutePath().contains("/data/"));
        return out;
    }

    /** Unzips a backup over internal storage; returns how many files were restored. */
    static int restore(File zip) throws IOException {
        File root = internal();
        String rootPath = root.getCanonicalPath() + File.separator;
        int n = 0;
        try (ZipInputStream z = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                if (e.isDirectory() || e.getName().startsWith("DuoLauncher/")) continue;
                File out = new File(root, e.getName());
                if (!out.getCanonicalPath().startsWith(rootPath)) continue; // never write outside internal storage
                out.getParentFile().mkdirs();
                // the old save stays until the new copy is complete (a damaged zip must not empty it)
                File part = new File(out.getParentFile(), out.getName() + ".restoring");
                try (OutputStream o = new FileOutputStream(part)) {
                    copy(z, o);
                } catch (IOException ex) {
                    part.delete();
                    throw ex;
                }
                if (!part.renameTo(out)) {
                    out.delete();
                    if (!part.renameTo(out)) { part.delete(); continue; }
                }
                n++;
            }
        }
        return n;
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[64 * 1024];
        int r;
        while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
    }
}
