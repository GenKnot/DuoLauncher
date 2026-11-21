package com.duo.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The game library. Folders are scanned directly: platforms are recognised from folder names and
 * file extensions. A folder that contains a Pegasus {@code metadata.pegasus.txt} is read through it
 * instead, so its titles, artwork and launch commands are kept.
 */
final class Library {

    static final class Platform {
        String name = "", sortBy = "", launch = "", system = "";
        boolean scanned;             // built by scanning folders rather than from Pegasus metadata
        final List<Game> games = new ArrayList<>();
        // user-made folders: own name, colour, icon art and backdrop art
        String label, art, bgArt;
        int color;

        String title() { return label != null ? label : platformTitle(name); }
        String type() { return art != null ? art : consoleType(system.isEmpty() ? name : system); }
        String backdropType() { return bgArt != null ? bgArt : type(); }
        int accent() { return color != 0 ? color : platformColor(type()); }
        boolean isCustom() { return system.equals("CUSTOM"); }
    }

    static final class Game {
        String title, file, developer = "", description = "", launch;
        String pkg;                  // set for installed Android games and apps instead of a ROM file
        File path, dir;              // dir: folder that relative asset and file paths are based on
        String volumeId = "primary", relDir = "";
        Platform platform;
        final Map<String, String> assets = new HashMap<>();
        private final Map<String, File> resolved = new HashMap<>();

        /** Stable id for settings and recents; scanned games include their folder since names may repeat. */
        String key() { return scanned ? platform.name + "/" + relDir + "/" + file : platform.name + "/" + file; }

        boolean scanned;             // found by scanning rather than listed in Pegasus metadata

        String ext() { return extension(file); }

        String baseName() {
            int i = file.lastIndexOf('.');
            return i < 0 ? file : file.substring(0, i);
        }

        /**
         * Artwork lookup, first hit wins: explicit assets.* / gamelist entry, Pegasus media/<basename>/,
         * covers|images|boxart/ next to the ROM, ES-DE downloaded_media, RetroArch thumbnails.
         */
        synchronized File asset(String kind) {
            if (resolved.containsKey(kind)) return resolved.get(kind);
            File f = null;
            if (dir != null) {
                String a = assets.get(kind);
                if (a != null) {
                    File x = a.startsWith("/") ? new File(a) : new File(dir, a);
                    if (x.isFile()) f = x;
                }
                if (f == null) f = findImage(new File(dir, "media/" + baseName()), aliases(kind));
                if (f == null && kind.equals("box_front")) {
                    String base = new File(baseName()).getName();
                    for (String sub : new String[]{"covers", "images", "boxart"}) {
                        f = exact(new File(dir, sub), base, base + "-image", base + "-thumb");
                        if (f != null) break;
                    }
                }
                if (f == null) f = externalArt(this, kind);
            }
            resolved.put(kind, f);
            return f;
        }
    }

    final List<Platform> platforms = new ArrayList<>();
    final Map<String, Game> byKey = new HashMap<>();

    // ---- systems ----

    /** key, folder-name aliases, extensions recognised without a folder hint. */
    private static final String[][] SYSTEMS = {
            {"3DS", "3ds n3ds nintendo3ds", "3ds cci cxi cia 3dsx"},
            {"NDS", "nds ds nintendods", "nds dsi"},
            {"GBA", "gba gameboyadvance", "gba"},
            {"GBC", "gbc gameboycolor", "gbc"},
            {"GB", "gb gameboy", "gb"},
            {"NES", "nes fc famicom", "nes fds unf"},
            {"SNES", "snes sfc superfamicom supernintendo", "sfc smc"},
            {"N64", "n64 nintendo64", "n64 z64 v64"},
            {"MD", "md genesis megadrive segagenesis", "md gen smd"},
            {"PS1", "ps1 psx playstation", "pbp"},
            {"PS2", "ps2 playstation2", ""},
            {"PSP", "psp", "cso"},
            {"PSV", "psv psvita vita", "vpk"},
            {"PS3", "ps3 playstation3", ""},
            {"SWITCH", "switch ns nintendoswitch", "nsp xci"},
    };
    /** Extensions accepted only when the folder name tells the system. */
    private static final Set<String> GENERIC = new HashSet<>(Arrays.asList("zip", "7z", "chd", "cue", "iso", "m3u"));
    private static final Set<String> SKIP_DIRS = new HashSet<>(Arrays.asList(
            "media", "images", "videos", "covers", "boxart", "screenshots", "manuals", "marquees", "wheel",
            "saves", "states", "savestates", "bios", "system", "cheats", "downloaded_media"));

    private static String norm(String s) { return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""); }

    static String extension(String f) {
        int i = f.lastIndexOf('.');
        return i < 0 ? "" : f.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    private static String systemForFolder(String folderName) {
        String n = norm(folderName);
        for (String[] s : SYSTEMS) for (String a : s[1].split(" ")) if (n.equals(a)) return s[0];
        return null;
    }

    private static String systemForFile(String ext, String hint) {
        if (hint != null && (GENERIC.contains(ext) || matchesExt(hint, ext))) return hint;
        if (hint != null && hint.equals("PS1") && ext.equals("bin")) return null; // cue/bin pairs: list the .cue only
        if (ext.isEmpty()) return null;
        for (String[] s : SYSTEMS) for (String e : s[2].split(" ")) if (e.equals(ext)) return s[0];
        return null;
    }

    private static boolean matchesExt(String system, String ext) {
        if (ext.isEmpty()) return false;
        for (String[] s : SYSTEMS) if (s[0].equals(system)) for (String e : s[2].split(" ")) if (e.equals(ext)) return true;
        return false;
    }

    // ---- artwork from other frontends ----

    /** ES-DE folders (with gamelists/ and downloaded_media/) and RetroArch thumbnail folders. */
    private static final List<File> esdeRoots = new ArrayList<>(), raThumbRoots = new ArrayList<>();

    private static void findFrontendFolders(Context ctx) {
        esdeRoots.clear();
        raThumbRoots.clear();
        List<File> volumes = new ArrayList<>();
        StorageManager sm = ctx.getSystemService(StorageManager.class);
        for (StorageVolume v : sm.getStorageVolumes()) if (v.getDirectory() != null) volumes.add(v.getDirectory());
        for (File vol : volumes) {
            for (String n : new String[]{"ES-DE", "ES-DE/ES-DE", "EmulationStation"}) {
                File d = new File(vol, n);
                if (new File(d, "downloaded_media").isDirectory() || new File(d, "gamelists").isDirectory()) esdeRoots.add(d);
            }
            File t = new File(vol, "RetroArch/thumbnails");
            if (t.isDirectory()) raThumbRoots.add(t);
        }
    }

    /** RetroArch playlist (and thumbnail folder) name for a system. */
    private static String raPlaylist(String system) {
        switch (system) {
            case "3DS": return "Nintendo - Nintendo 3DS";
            case "NDS": return "Nintendo - Nintendo DS";
            case "GBA": return "Nintendo - Game Boy Advance";
            case "GBC": return "Nintendo - Game Boy Color";
            case "GB": return "Nintendo - Game Boy";
            case "NES": return "Nintendo - Nintendo Entertainment System";
            case "SNES": return "Nintendo - Super Nintendo Entertainment System";
            case "N64": return "Nintendo - Nintendo 64";
            case "MD": return "Sega - Mega Drive - Genesis";
            case "PS1": return "Sony - PlayStation";
            case "PS2": return "Sony - PlayStation 2";
            case "PSP": return "Sony - PlayStation Portable";
            default: return null;
        }
    }

    private static File externalArt(Game g, String kind) {
        if (esdeRoots.isEmpty() && raThumbRoots.isEmpty()) return null;
        String base = new File(g.baseName()).getName(), folder = g.dir.getName();
        String[] es, ra;
        switch (kind) {
            case "box_front": es = new String[]{"covers", "3dboxes", "miximages"}; ra = new String[]{"Named_Boxarts"}; break;
            case "logo": es = new String[]{"marquees"}; ra = new String[0]; break;
            case "background": es = new String[]{"fanart", "screenshots", "titlescreens"}; ra = new String[]{"Named_Snaps", "Named_Titles"}; break;
            default: return null;
        }
        for (File root : esdeRoots)
            for (String s : es) {
                File f = exact(new File(root, "downloaded_media/" + folder + "/" + s), base);
                if (f != null) return f;
            }
        String pl = raPlaylist(g.platform.system);
        if (pl != null) {
            String name = base.replaceAll("[&*/:`<>?\\\\|]", "_");
            for (File root : raThumbRoots)
                for (String s : ra) {
                    File f = exact(new File(root, pl + "/" + s), name);
                    if (f != null) return f;
                }
        }
        return null;
    }

    /** {@code folder/<name>.png|jpg|jpeg|webp} for the first name that exists. */
    private static File exact(File folder, String... names) {
        if (!folder.isDirectory()) return null;
        for (String n : names)
            for (String ext : new String[]{".png", ".jpg", ".jpeg", ".webp"}) {
                File f = new File(folder, n + ext);
                if (f.isFile()) return f;
            }
        return null;
    }

    /** Titles, descriptions and art from an EmulationStation / ES-DE gamelist.xml for games in {@code dir}. */
    private static void applyGamelists(File dir, List<Game> games) {
        if (games.isEmpty()) return;
        List<File> lists = new ArrayList<>();
        lists.add(new File(dir, "gamelist.xml"));
        for (File root : esdeRoots) lists.add(new File(root, "gamelists/" + dir.getName() + "/gamelist.xml"));
        Map<String, Game> byFile = new HashMap<>();
        for (Game g : games) byFile.put(g.file, g);
        for (File gl : lists) {
            if (!gl.isFile()) continue;
            try (FileInputStream in = new FileInputStream(gl)) {
                XmlPullParser x = Xml.newPullParser();
                x.setInput(in, null);
                Map<String, String> cur = null;
                String tag = null;
                for (int ev = x.getEventType(); ev != XmlPullParser.END_DOCUMENT; ev = x.next()) {
                    if (ev == XmlPullParser.START_TAG) {
                        tag = x.getName();
                        if (tag.equals("game")) cur = new HashMap<>();
                    } else if (ev == XmlPullParser.TEXT && cur != null && tag != null) {
                        String t = x.getText().trim();
                        if (!t.isEmpty()) cur.put(tag, cur.containsKey(tag) ? cur.get(tag) + t : t);
                    } else if (ev == XmlPullParser.END_TAG) {
                        if (x.getName().equals("game") && cur != null) {
                            String path = cur.get("path");
                            Game g = path == null ? null : byFile.get(path.replaceFirst("^\\./", ""));
                            if (g != null) {
                                if (cur.containsKey("name")) g.title = cur.get("name");
                                if (cur.containsKey("desc")) g.description = cur.get("desc");
                                File base = gl.getParentFile();
                                String img = cur.containsKey("image") ? cur.get("image") : cur.get("thumbnail");
                                if (img != null) g.assets.put("box_front", new File(base, img.replaceFirst("^\\./", "")).getAbsolutePath());
                                if (cur.containsKey("marquee")) g.assets.put("logo", new File(base, cur.get("marquee").replaceFirst("^\\./", "")).getAbsolutePath());
                            }
                            cur = null;
                        }
                        tag = null;
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    // ---- loading ----

    /**
     * @param folders  folders chosen by the user
     * @param autoFind also look for a "Roms" folder at the top of every storage volume
     */
    static Library load(Context ctx, List<String> folders, boolean autoFind, Set<String> shownApps) {
        Library lib = new Library();
        findFrontendFolders(ctx);
        Map<String, Platform> raw = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        List<File> roots = new ArrayList<>();
        if (autoFind) {
            StorageManager sm = ctx.getSystemService(StorageManager.class);
            for (StorageVolume v : sm.getStorageVolumes()) {
                File root = v.getDirectory();
                File[] top = root == null ? null : root.listFiles();
                if (top == null) continue;
                for (File d : top) if (d.isDirectory() && norm(d.getName()).equals("roms")) roots.add(d);
            }
        }
        for (String f : folders) roots.add(new File(f));
        for (File r : roots) lib.scan(r, systemForFolder(r.getName()), raw, seen, 0);

        // scanned games join a Pegasus collection of the same name instead of making a second folder
        Collator col = Collator.getInstance(Locale.forLanguageTag(I18n.code()));
        for (Platform p : raw.values()) {
            p.games.sort((a, b) -> col.compare(a.title, b.title));
            Platform same = null;
            for (Platform q : lib.platforms) if (q.name.equalsIgnoreCase(p.name)) same = q;
            if (same == null) {
                lib.platforms.add(p);
            } else {
                for (Game g : p.games) g.platform = same;
                same.games.addAll(p.games);
            }
        }
        lib.platforms.removeIf(p -> p.games.isEmpty());
        lib.platforms.sort((a, b) -> {
            int c = Integer.compare(order(a), order(b));
            if (c != 0) return c;
            c = a.sortBy.compareTo(b.sortBy);
            return c != 0 ? c : a.name.compareTo(b.name);
        });
        addAndroid(ctx, lib, shownApps, col);
        for (Platform p : lib.platforms) for (Game g : p.games) lib.byKey.put(g.key(), g);
        return lib;
    }

    // ---- installed Android games and apps ----

    /** Emulators and launchers are not listed as games or apps. */
    private static final Set<String> HIDDEN_APPS = new HashSet<>(Arrays.asList(
            "io.github.azaharplus.android", "org.azahar_emu.azahar", "org.citra.emu", "com.dsemu.drastic",
            "com.retroarch.aarch64", "com.retroarch", "org.pegasus_frontend.android", "me.magnum.melonds",
            "org.ppsspp.ppsspp", "org.ppsspp.ppssppgold", "xyz.aethersx2.android", "com.github.stenzek.duckstation",
            "org.vita3k.emulator", "com.retroidpocket.gamelauncher", "com.android.launcher3", "com.teslacoilsw.launcher"));

    private static boolean isGame(ApplicationInfo ai) {
        return ai.category == ApplicationInfo.CATEGORY_GAME || (ai.flags & ApplicationInfo.FLAG_IS_GAME) != 0;
    }

    /** Launchable apps that are not games, for the "choose apps" setting: {label, package}. */
    static List<String[]> launchableApps(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        List<String[]> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            if (isGame(ai) || HIDDEN_APPS.contains(ai.packageName) || ai.packageName.equals(ctx.getPackageName())) continue;
            if (seen.add(ai.packageName)) out.add(new String[]{ri.loadLabel(pm).toString(), ai.packageName});
        }
        Collator col = Collator.getInstance(Locale.forLanguageTag(I18n.code()));
        out.sort((a, b) -> col.compare(a[0], b[0]));
        return out;
    }

    private static void addAndroid(Context ctx, Library lib, Set<String> shownApps, Collator col) {
        PackageManager pm = ctx.getPackageManager();
        Platform games = new Platform(), apps = new Platform();
        games.name = games.system = "ANDROID";
        apps.name = apps.system = "APPS";
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            String pkg = ai.packageName;
            if (HIDDEN_APPS.contains(pkg) || pkg.equals(ctx.getPackageName()) || !seen.add(pkg)) continue;
            Platform target = isGame(ai) ? games : shownApps.contains(pkg) ? apps : null;
            if (target == null) continue;
            Game g = new Game();
            g.pkg = pkg;
            g.file = pkg;
            g.title = ri.loadLabel(pm).toString();
            g.platform = target;
            target.games.add(g);
        }
        for (Platform p : new Platform[]{games, apps}) {
            p.games.sort((a, b) -> col.compare(a.title, b.title));
            if (!p.games.isEmpty()) lib.platforms.add(p);
        }
    }

    /** Display order: by system, Pegasus collections next to the system they belong to. */
    private static int order(Platform p) {
        String sys = p.system.isEmpty() ? systemForFolder(p.name.split(" ")[0]) : p.system;
        for (int i = 0; i < SYSTEMS.length; i++) if (SYSTEMS[i][0].equals(sys)) return i;
        return SYSTEMS.length;
    }

    private void scan(File dir, String hint, Map<String, Platform> raw, Set<String> seen, int depth) {
        String canon;
        try { canon = dir.getCanonicalPath(); } catch (IOException e) { canon = dir.getAbsolutePath(); }
        if (!seen.add(canon)) return;
        File[] fs = dir.listFiles();
        if (fs == null) return;

        File meta = new File(dir, "metadata.pegasus.txt");
        if (meta.isFile()) {
            try {
                Platform p = parsePegasus(meta);
                p.system = hint != null ? hint : systemFromGames(p);
                for (Game g : p.games) place(g, dir);
                p.games.removeIf(g -> g.file == null || !g.path.isFile());
                if (!p.games.isEmpty()) platforms.add(p);
            } catch (IOException ignored) {
            }
            return;
        }

        List<Game> here = new ArrayList<>();
        for (File f : fs) {
            String n = f.getName();
            if (n.startsWith(".")) continue;
            if (f.isDirectory()) {
                if (depth < 3 && !SKIP_DIRS.contains(n.toLowerCase(Locale.ROOT))) {
                    String sub = systemForFolder(n);
                    scan(f, sub != null ? sub : hint, raw, seen, depth + 1);
                }
                continue;
            }
            String sys = systemForFile(extension(n), hint);
            if (sys == null) continue;
            Platform p = raw.get(sys);
            if (p == null) {
                p = new Platform();
                p.name = sys;
                p.system = sys;
                p.scanned = true;
                p.launch = defaultLaunch(sys);
                raw.put(sys, p);
            }
            Game g = new Game();
            g.file = n;
            g.title = n.substring(0, n.length() - extension(n).length() - (extension(n).isEmpty() ? 0 : 1));
            g.platform = p;
            g.scanned = true;
            place(g, dir);
            p.games.add(g);
            here.add(g);
        }
        applyGamelists(dir, here);
    }

    /** Ties a game to the folder it lives in (for relative paths and content URIs). */
    private static void place(Game g, File dir) {
        g.dir = dir;
        if (g.file != null) g.path = new File(dir, g.file);
        String abs = dir.getAbsolutePath(), prim = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (abs.startsWith(prim + "/") || abs.equals(prim)) {
            g.volumeId = "primary";
            g.relDir = abs.length() > prim.length() ? abs.substring(prim.length() + 1) : "";
        } else if (abs.startsWith("/storage/")) {
            String rest = abs.substring(9);
            int s = rest.indexOf('/');
            g.volumeId = s < 0 ? rest : rest.substring(0, s);
            g.relDir = s < 0 ? "" : rest.substring(s + 1);
        }
    }

    private static String systemFromGames(Platform p) {
        for (Game g : p.games) {
            if (g.file == null) continue;
            String s = systemForFile(extension(g.file), null);
            if (s != null) return s;
        }
        return "";
    }

    // ---- Pegasus metadata ----

    static Platform parsePegasus(File meta) throws IOException {
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

    private static File findImage(File folder, String[] names) {
        File[] fs = folder.listFiles();
        if (fs == null) return null;
        for (String want : names) {
            for (File x : fs) {
                String n = x.getName();
                int d = n.lastIndexOf('.');
                if (d > 0 && n.substring(0, d).equalsIgnoreCase(want) && isImage(n)) return x;
            }
        }
        return null;
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

    // ---- presentation ----

    static String platformTitle(String n) {
        switch (n.toUpperCase(Locale.ROOT)) {
            case "3DS": return I18n.t(I18n.S.T_3DS);
            case "NDS": return I18n.t(I18n.S.T_NDS);
            case "GBA": return "Game Boy Advance";
            case "GBA HACK": return I18n.t(I18n.S.T_GBA_HACK);
            case "GBA VIB": return I18n.t(I18n.S.T_GBA_VIB);
            case "GBC": return "Game Boy Color";
            case "GB": return "Game Boy";
            case "NES": return "NES / Famicom";
            case "SNES": return "Super Nintendo";
            case "N64": return "Nintendo 64";
            case "MD": return "Mega Drive / Genesis";
            case "PS1": return "PlayStation";
            case "PS2": return "PlayStation 2";
            case "PS3": return "PlayStation 3";
            case "PS4": return "PlayStation 4";
            case "PS5": return "PlayStation 5";
            case "PSP": return "PlayStation Portable";
            case "PSV": return "PlayStation Vita";
            case "SWITCH": return "Nintendo Switch";
            case "XBOX360": return "Xbox 360";
            case "XBOX": return "Xbox";
            case "ANDROID": return I18n.t(I18n.S.ANDROID_GAMES);
            case "APPS": return I18n.t(I18n.S.APPS);
            default: return n;
        }
    }

    static String consoleType(String n) {
        String u = n.toUpperCase(Locale.ROOT).replace(" ", "");
        if (u.startsWith("GBA")) return "gba";
        switch (u) {
            case "3DS": return "3ds";
            case "NDS": return "nds";
            case "GBC": return "gbc";
            case "GB": return "gb";
            case "XBOX360": case "X360": return "xbox360";
            case "XBOX": case "XBOXSERIES": case "XBOXSERIESX": case "XSX": return "xboxsx";
            case "ANDROID": return "android";
            case "APPS": return "apps";
            case "NES": case "SNES": case "N64": case "MD": case "PS1": case "PS2": case "PS3": case "PS4": case "PS5":
            case "PSP": case "PSV": case "SWITCH":
                return u.toLowerCase(Locale.ROOT);
            default: return "generic";
        }
    }

    static int platformColor(String type) {
        switch (type) {
            case "3ds": return 0xFFE8463E;
            case "nds": return 0xFF8E9EB0;
            case "gba": return 0xFF5B4BC4;
            case "gbc": return 0xFF1FA99B;
            case "gb": return 0xFF8E9A3A;
            case "nes": return 0xFFC0392B;
            case "snes": return 0xFF7D5BA6;
            case "n64": return 0xFF2E7D32;
            case "md": return 0xFF263238;
            case "ps1": return 0xFF607D8B;
            case "ps2": return 0xFF2D3C8F;
            case "ps3": return 0xFF1F2A44;
            case "ps4": return 0xFF1E4FA3;
            case "ps5": return 0xFF3A6FD8;
            case "psp": return 0xFF37474F;
            case "psv": return 0xFF29486B;
            case "switch": return 0xFFE4002B;
            case "xbox360": return 0xFF6BB233;
            case "xboxsx": return 0xFF107C10;
            case "android": return 0xFF2E9E5B;
            case "apps": return 0xFF4A6FD9;
            default: return 0xFF4A90D9;
        }
    }

    // ---- emulators ----

    static final String PKG_AZAHAR_PLUS = "io.github.azaharplus.android", PKG_AZAHAR = "org.azahar_emu.azahar",
            PKG_CITRA = "org.citra.emu", PKG_DRASTIC = "com.dsemu.drastic", PKG_RETROARCH = "com.retroarch.aarch64";

    private static String azahar(String pkg) {
        return "am start --user 0 -n " + pkg + "/org.citra.citra_emu.activities.EmulationActivity"
                + " --activity-clear-task --activity-clear-top -d \"{file.uri}\"";
    }

    static final String CITRA = "am start --user 0 -n org.citra.emu/org.citra.emu.ui.EmulationActivity"
            + " -a android.intent.action.VIEW -e GamePath {file.path} --activity-clear-top";
    /** The DraStic build shipped for Retroid handhelds expects the path behind a "/sf" prefix. */
    static final String DRASTIC_SF = "am start --user 0 -n com.dsemu.drastic/.DraSticActivity -a android.intent.action.MAIN -e GAMEPATH \"/sf{file.path}\"";
    static final String DRASTIC = "am start --user 0 -n com.dsemu.drastic/.DraSticActivity -a android.intent.action.MAIN -e GAMEPATH \"{file.path}\"";

    static String retroArch(String core) {
        return "am start --user 0 -n com.retroarch.aarch64/com.retroarch.browser.retroactivity.RetroActivityFuture"
                + " -e ROM {file.path} -e LIBRETRO /data/data/com.retroarch.aarch64/cores/" + core + "_libretro_android.so"
                + " -e CONFIGFILE /storage/emulated/0/Android/data/com.retroarch.aarch64/files/retroarch.cfg";
    }

    static String defaultLaunch(String system) {
        List<String[]> o = allOptions(system);
        return o.isEmpty() ? "" : o.get(0)[1];
    }

    /** Every emulator we know how to start for a system, preferred first: {label, launch command, package}. */
    private static List<String[]> allOptions(String system) {
        List<String[]> o = new ArrayList<>();
        switch (system) {
            case "3DS":
                o.add(new String[]{"AzaharPlus", azahar(PKG_AZAHAR_PLUS), PKG_AZAHAR_PLUS});
                o.add(new String[]{"Azahar", azahar(PKG_AZAHAR), PKG_AZAHAR});
                o.add(new String[]{"Citra MMJ", CITRA, PKG_CITRA});
                break;
            case "NDS":
                o.add(new String[]{"DraStic", DRASTIC_SF, PKG_DRASTIC});
                o.add(new String[]{"DraStic (plain path)", DRASTIC, PKG_DRASTIC});
                o.add(new String[]{"RetroArch · melonDS DS", retroArch("melondsds"), PKG_RETROARCH});
                break;
            case "GB":
            case "GBC":
                o.add(new String[]{"RetroArch · Gambatte", retroArch("gambatte"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · mGBA", retroArch("mgba"), PKG_RETROARCH});
                break;
            case "GBA":
                o.add(new String[]{"RetroArch · mGBA", retroArch("mgba"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · gpSP", retroArch("gpsp"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · gpSP Rumble", retroArch("gpsp_rumble"), PKG_RETROARCH});
                break;
            case "NES":
                o.add(new String[]{"RetroArch · FCEUmm", retroArch("fceumm"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · Mesen", retroArch("mesen"), PKG_RETROARCH});
                break;
            case "SNES":
                o.add(new String[]{"RetroArch · Snes9x", retroArch("snes9x"), PKG_RETROARCH});
                break;
            case "N64":
                o.add(new String[]{"RetroArch · Mupen64Plus-Next", retroArch("mupen64plus_next_gles3"), PKG_RETROARCH});
                break;
            case "MD":
                o.add(new String[]{"RetroArch · Genesis Plus GX", retroArch("genesis_plus_gx"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · PicoDrive", retroArch("picodrive"), PKG_RETROARCH});
                break;
            case "PS1":
                o.add(new String[]{"RetroArch · PCSX ReARMed", retroArch("pcsx_rearmed"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · SwanStation", retroArch("swanstation"), PKG_RETROARCH});
                break;
            case "PSP":
                o.add(new String[]{"RetroArch · PPSSPP", retroArch("ppsspp"), PKG_RETROARCH});
                break;
            default:
                break;
        }
        return o;
    }

    /** Alternatives offered in the emulator menu: only those whose app is installed. {label, command} */
    static List<String[]> options(Platform p, PackageManager pm) {
        List<String[]> out = new ArrayList<>();
        for (String[] o : allOptions(p.system)) if (installed(pm, o[2])) out.add(new String[]{o[0], o[1]});
        return out;
    }

    static boolean installed(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static final Pattern CORE = Pattern.compile("cores/([A-Za-z0-9_]+?)_libretro");
    private static final Pattern COMPONENT = Pattern.compile("-n\\s+([\\w.]+)/");

    static String emulatorLabel(String cmd) {
        if (cmd == null || cmd.isEmpty()) return I18n.t(I18n.S.NOT_SET);
        if (cmd.contains(PKG_AZAHAR_PLUS)) return "AzaharPlus";
        if (cmd.contains(PKG_AZAHAR)) return "Azahar";
        if (cmd.contains(PKG_CITRA)) return "Citra MMJ";
        if (cmd.contains("drastic")) return "DraStic";
        if (cmd.contains("retroarch")) {
            Matcher m = CORE.matcher(cmd);
            return "RetroArch" + (m.find() ? " · " + m.group(1) : "");
        }
        Matcher m = COMPONENT.matcher(cmd);
        return m.find() ? m.group(1) : I18n.t(I18n.S.CUSTOM);
    }
}
