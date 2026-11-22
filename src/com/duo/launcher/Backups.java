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

    /** Backups live next to the launcher's own folder (SD card if it has one), else on internal storage. */
    static File folder(List<File> volumeRoots) {
        for (File v : volumeRoots) if (new File(v, "DuoLauncher").isDirectory()) return new File(v, "DuoLauncher/backups");
        return new File(internal(), "DuoLauncher/backups");
    }

    static List<File> list(File dir) {
        File[] fs = dir.listFiles((d, n) -> n.endsWith(".zip"));
        List<File> out = new ArrayList<>(fs == null ? new ArrayList<>() : Arrays.asList(fs));
        out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return out;
    }

    /** Writes a new backup and returns it, or throws when nothing could be written. */
    static File backup(File dir) throws IOException {
        dir.mkdirs();
        File out = new File(dir, "saves-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(new Date()) + ".zip");
        File root = internal();
        int n = 0;
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(out))) {
            for (String d : SAVE_DIRS) n += add(z, root, new File(root, d), false);
            for (String home : CTR_HOMES) n += add(z, root, new File(root, home + "/sdmc/Nintendo 3DS"), true);
            if (n == 0) z.putNextEntry(new ZipEntry("DuoLauncher/empty-backup.txt"));
        }
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
                try (OutputStream o = new FileOutputStream(out)) { copy(z, o); }
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
