package com.duo.launcher;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Game library read from Pegasus metadata (Roms/<platform>/metadata.pegasus.txt). */
final class Library {

    static final class Platform {
        String name = "", sortBy = "", launch = "";
        File dir;
        String volumeId = "primary", relDir = "";
        final List<Game> games = new ArrayList<>();

        String title() { return platformTitle(name); }
        String type() { return consoleType(name); }
    }

    static final class Game {
        String title, file, developer = "", description = "", launch;
        File path;
        Platform platform;
        final Map<String, String> assets = new HashMap<>();
        private final Map<String, File> resolved = new HashMap<>();

        String key() { return platform.name + "/" + file; }

        String ext() {
            int i = file.lastIndexOf('.');
            return i < 0 ? "" : file.substring(i + 1).toLowerCase(Locale.ROOT);
        }

        String baseName() {
            int i = file.lastIndexOf('.');
            return i < 0 ? file : file.substring(0, i);
        }

        /** Explicit assets.* entry first, then Pegasus auto-assets in media/<basename>/. */
        synchronized File asset(String kind) {
            if (resolved.containsKey(kind)) return resolved.get(kind);
            File f = null;
            String a = assets.get(kind);
            if (a != null) {
                File x = new File(platform.dir, a);
                if (x.isFile()) f = x;
            }
            if (f == null) {
                File[] fs = new File(platform.dir, "media/" + baseName()).listFiles();
                if (fs != null) {
                    outer:
                    for (String want : aliases(kind)) {
                        for (File x : fs) {
                            String n = x.getName();
                            int d = n.lastIndexOf('.');
                            if (d > 0 && n.substring(0, d).equalsIgnoreCase(want) && isImage(n)) { f = x; break outer; }
                        }
                    }
                }
            }
            resolved.put(kind, f);
            return f;
        }
    }

    final List<Platform> platforms = new ArrayList<>();
    final Map<String, Game> byKey = new HashMap<>();

    static Library load(Context ctx) {
        Library lib = new Library();
        StorageManager sm = ctx.getSystemService(StorageManager.class);
        for (StorageVolume v : sm.getStorageVolumes()) {
            File root = v.getDirectory();
            if (root == null) continue;
            File[] dirs = new File(root, "Roms").listFiles();
            if (dirs == null) continue;
            String vol = v.isPrimary() || v.getUuid() == null ? "primary" : v.getUuid();
            for (File d : dirs) {
                File meta = new File(d, "metadata.pegasus.txt");
                if (!d.isDirectory() || !meta.isFile()) continue;
                try {
                    Platform p = parse(meta);
                    p.dir = d;
                    p.volumeId = vol;
                    p.relDir = "Roms/" + d.getName();
                    List<Game> ok = new ArrayList<>();
                    for (Game g : p.games) {
                        if (g.file == null) continue;
                        g.path = new File(d, g.file);
                        if (g.path.isFile()) ok.add(g);
                    }
                    p.games.clear();
                    p.games.addAll(ok);
                    if (p.games.isEmpty()) continue;
                    lib.platforms.add(p);
                    for (Game g : p.games) lib.byKey.put(g.key(), g);
                } catch (IOException ignored) {
                }
            }
        }
        lib.platforms.sort((a, b) -> {
            int c = a.sortBy.compareTo(b.sortBy);
            return c != 0 ? c : a.name.compareTo(b.name);
        });
        return lib;
    }

    static Platform parse(File meta) throws IOException {
        List<String[]> pairs = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(meta), StandardCharsets.UTF_8))) {
            String line, key = null;
            StringBuilder val = new StringBuilder();
            boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) {
                    if (line.startsWith("﻿")) line = line.substring(1);
                    first = false;
                }
                if (line.startsWith("#")) continue;
                if (!line.isEmpty() && Character.isWhitespace(line.charAt(0))) {
                    String t = line.trim();
                    if (key != null && !t.isEmpty()) {
                        if (val.length() > 0) val.append('\n');
                        val.append(t.equals(".") ? "" : t);
                    }
                    continue;
                }
                if (key != null) {
                    pairs.add(new String[]{key, val.toString()});
                    key = null;
                }
                int c = line.indexOf(':');
                if (line.trim().isEmpty() || c < 0) continue;
                key = line.substring(0, c).trim().toLowerCase(Locale.ROOT);
                val.setLength(0);
                val.append(line.substring(c + 1).trim());
            }
            if (key != null) pairs.add(new String[]{key, val.toString()});
        }

        Platform p = new Platform();
        p.name = meta.getParentFile().getName();
        boolean named = false;
        Game g = null;
        for (String[] kv : pairs) {
            String k = kv[0], v = kv[1];
            switch (k) {
                case "collection":
                    if (g == null && !named) { p.name = v; named = true; }
                    break;
                case "game":
                    g = new Game();
                    g.title = v;
                    g.platform = p;
                    p.games.add(g);
                    break;
                case "file":
                case "files":
                    if (g != null && g.file == null) {
                        String f = firstLine(v);
                        if (!f.isEmpty()) g.file = f;
                    }
                    break;
                case "launch":
                case "command":
                    if (g == null) p.launch = v; else g.launch = v;
                    break;
                case "sort-by":
                case "sort_by":
                    if (g == null) p.sortBy = v;
                    break;
                case "developer":
                case "developers":
                    if (g != null) g.developer = v;
                    break;
                case "description":
                case "summary":
                    if (g != null && (g.description.isEmpty() || k.equals("description"))) g.description = v;
                    break;
                default:
                    if (g != null && k.startsWith("assets.")) g.assets.put(k.substring(7), firstLine(v));
            }
        }
        return p;
    }

    private static String firstLine(String v) {
        for (String s : v.split("\n")) if (!s.trim().isEmpty()) return s.trim();
        return "";
    }

    private static String[] aliases(String kind) {
        switch (kind) {
            case "box_front": return new String[]{"boxFront", "box_front", "boxart", "cover"};
            case "logo": return new String[]{"logo", "wheel", "marquee"};
            case "background": return new String[]{"background", "fanart", "screenshot"};
            default: return new String[]{kind};
        }
    }

    private static boolean isImage(String n) {
        String s = n.toLowerCase(Locale.ROOT);
        return s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".webp");
    }

    static String platformTitle(String n) {
        switch (n.toUpperCase(Locale.ROOT)) {
            case "3DS": return I18n.t(I18n.S.T_3DS);
            case "NDS": return I18n.t(I18n.S.T_NDS);
            case "GBA": return "Game Boy Advance";
            case "GBA HACK": return I18n.t(I18n.S.T_GBA_HACK);
            case "GBA VIB": return I18n.t(I18n.S.T_GBA_VIB);
            case "GBC": return "Game Boy Color";
            case "GB": return "Game Boy";
            case "PS2": return "PlayStation 2";
            default: return n;
        }
    }

    static String consoleType(String n) {
        String u = n.toUpperCase(Locale.ROOT);
        if (u.equals("3DS")) return "3ds";
        if (u.equals("NDS")) return "nds";
        if (u.startsWith("GBA")) return "gba";
        if (u.equals("GBC")) return "gbc";
        if (u.equals("GB")) return "gb";
        if (u.equals("PS2")) return "ps2";
        return "generic";
    }

    static int platformColor(String type) {
        switch (type) {
            case "3ds": return 0xFFE8463E;
            case "nds": return 0xFF8E9EB0;
            case "gba": return 0xFF5B4BC4;
            case "gbc": return 0xFF1FA99B;
            case "gb": return 0xFF8E9A3A;
            case "ps2": return 0xFF2D3C8F;
            default: return 0xFF4A90D9;
        }
    }

    // ---- emulators ----

    static final String AZAHAR = "am start --user 0 -n io.github.azaharplus.android/org.citra.citra_emu.activities.EmulationActivity"
            + " --activity-clear-task --activity-clear-top -d \"{file.uri}\"";
    static final String CITRA = "am start --user 0 -n org.citra.emu/org.citra.emu.ui.EmulationActivity"
            + " -a android.intent.action.VIEW -e GamePath {file.path} --activity-clear-top";

    static String retroArch(String core) {
        return "am start --user 0 -n com.retroarch.aarch64/com.retroarch.browser.retroactivity.RetroActivityFuture"
                + " -e ROM {file.path} -e LIBRETRO /data/data/com.retroarch.aarch64/cores/" + core + "_libretro_android.so"
                + " -e CONFIGFILE /storage/emulated/0/Android/data/com.retroarch.aarch64/files/retroarch.cfg";
    }

    /** Alternative emulators offered in the per-game / per-platform menu: {label, launch command}. */
    static List<String[]> options(Platform p) {
        List<String[]> o = new ArrayList<>();
        switch (p.type()) {
            case "3ds":
                o.add(new String[]{"AzaharPlus", AZAHAR});
                o.add(new String[]{"Citra MMJ", CITRA});
                break;
            case "gb":
            case "gbc":
                o.add(new String[]{"RetroArch · Gambatte", retroArch("gambatte")});
                o.add(new String[]{"RetroArch · mGBA", retroArch("mgba")});
                break;
            case "gba":
                o.add(new String[]{"RetroArch · mGBA", retroArch("mgba")});
                o.add(new String[]{"RetroArch · gpSP", retroArch("gpsp")});
                o.add(new String[]{"RetroArch · gpSP Rumble", retroArch("gpsp_rumble")});
                break;
            default:
                break;
        }
        return o;
    }

    private static final Pattern CORE = Pattern.compile("cores/([A-Za-z0-9_]+?)_libretro");
    private static final Pattern COMPONENT = Pattern.compile("-n\\s+([\\w.]+)/");

    static String emulatorLabel(String cmd) {
        if (cmd == null || cmd.isEmpty()) return I18n.t(I18n.S.NOT_SET);
        if (cmd.contains("azahar")) return "AzaharPlus";
        if (cmd.contains("org.citra.emu")) return "Citra MMJ";
        if (cmd.contains("drastic")) return "DraStic";
        if (cmd.contains("retroarch")) {
            Matcher m = CORE.matcher(cmd);
            return "RetroArch" + (m.find() ? " · " + m.group(1) : "");
        }
        Matcher m = COMPONENT.matcher(cmd);
        return m.find() ? m.group(1) : I18n.t(I18n.S.CUSTOM);
    }
}
