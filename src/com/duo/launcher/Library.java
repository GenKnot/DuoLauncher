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
        final Set<String> listedFiles = new HashSet<>(); // every file a Pegasus metadata file names, incl. extra discs
        // user-made folders: own name, colour, icon art and backdrop art
        String label, art, bgArt;
        int color;
        /** For a smart folder, its rule ("new", "unplayed", "most"); its games are chosen, not added. */
        String smart;
        /** A genre folder with its own picture, drawn as a folder instead of fanned covers. */
        boolean ownIcon;
        /** Console folders shown inside this one; their games keep their own platform. */
        final List<Platform> mergedFrom = new ArrayList<>();

        String title() { return label != null ? label : platformTitle(name); }
        String type() { return art != null ? art : consoleType(system.isEmpty() ? name : system); }
        String backdropType() { return bgArt != null ? bgArt : type(); }
        int accent() { return color != 0 ? color : Palette.folder(type(), platformColor(type())); }
        boolean isCustom() { return system.equals("CUSTOM"); }
    }

    static final class Game {
        String title, file, developer = "", description = "", launch;
        String pkg;                  // set for installed Android games and apps instead of a ROM file
        File path, dir;              // dir: folder that relative asset and file paths are based on
        String volumeId = "primary", relDir = "";
        String treeDir;              // folder the emulator was granted, when it is above relDir (installed titles)
        String titleId;              // 3DS program ID as 16 hex digits, for installed titles
        /** Further discs of a multi-disc game (file names in the same folder), in order; empty for one disc. */
        final List<String> discs = new ArrayList<>();
        /** Official English title, when the launcher's own metadata knows it ("original:"). */
        String original;
        /** One of {@link Genres#IDS}, or null when not sorted yet. */
        String genre;
        /** Series (English and Chinese name, or Series.NONE for both), or null when not looked at yet. */
        String series, seriesZh;
        /** When the launcher first saw the game (ms), 0 when unknown. */
        long added;
        /** Name of the game without the disc tag, for its media folder ("Tales DiscA" → "Tales"). */
        String mediaBase;
        File ownMedia;               // DuoLauncher/<platform>/media/<name>/, looked at before anything else
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
            File f = ownMedia == null ? null : findImage(ownMedia, aliases(kind));
            if (f == null && dir != null) {
                String a = assets.get(kind);
                if (a != null) {
                    File x = a.startsWith("/") ? new File(a) : new File(dir, a);
                    if (x.isFile()) f = x;
                }
                if (f == null) f = findImage(new File(dir, "media/" + baseName()), aliases(kind));
                if (f == null && mediaBase != null) f = findImage(new File(dir, "media/" + mediaBase), aliases(kind));
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
            {"NES", "nes fc famicom fchd fchack neshack", "nes fds unf"},
            {"SNES", "snes sfc superfamicom supernintendo sfchack sfcmsu1 sneshack snesmsu1", "sfc smc"},
            {"N64", "n64 nintendo64", "n64 z64 v64"},
            {"NGC", "ngc gc gamecube nintendogamecube", "gcm gcz rvz ciso wia"},
            {"WII", "wii nintendowii wiiware", "wbfs wad rvz gcz ciso wia"},
            {"MD", "md genesis megadrive segagenesis mdhack mdhackpicodrive genesishack", "md gen smd"},
            {"PS1", "ps1 psx playstation", "pbp"},
            {"PS2", "ps2 playstation2", ""},
            {"PSP", "psp", "cso"},
            {"PSV", "psv psvita vita", "vpk psvita"},
            {"PS3", "ps3 playstation3", ""},
            {"SWITCH", "switch ns nintendoswitch", "nsp xci nsz xcz"},
            // the rest of the Pegasus G pack, with the folder names Pegasus, ES-DE and RetroArch use
            {"VB", "vb virtualboy nintendovirtualboy", "vb vboy"},
            {"SMS", "sms mastersystem segamastersystem", "sms"},
            {"GG", "gg gamegear segagamegear", "gg"},
            {"32X", "32x sega32x md32x mega32x", "32x"},
            {"SEGACD", "segacd megacd mdcd segamegacd", ""},
            {"SATURN", "saturn ss segasaturn", ""},
            {"DC", "dc dreamcast segadreamcast", "cdi gdi"},
            {"NAOMI", "naomi naomi2 atomiswave", ""},
            {"PCE", "pce pcengine tg16 turbografx16 turbografx", "pce"},
            {"PCECD", "pcecd pcenginecd tgcd turbografxcd", ""},
            {"PCFX", "pcfx", ""},
            {"NEOGEOCD", "neogeocd ngcd neocd", ""},
            {"NGPC", "ngpc ngp neogeopocket neogeopocketcolor", "ngp ngc"},
            {"WS", "ws wonderswan", "ws"},
            {"WSC", "wsc wonderswancolor", "wsc"},
            {"POKEMINI", "pokemini pokemonmini", "min"},
            {"LYNX", "lynx atarilynx", "lnx"},
            {"ATARI", "atari atari2600 a2600 2600", "a26"},
            {"GW", "gw gameandwatch gamewatch", "mgw"},
            {"3DO", "3do panasonic3do", ""},
            {"DOS", "dos msdos pc dosbox", "dosz"},
            {"ARCADE", "arcade mame fbneo fba neogeo cps1 cps2 cps3", ""},
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

    /** The short name on a home tile for folder keys that read badly as they are ("POKEMINI"); others unchanged. */
    static String shortLabel(String name) {
        switch (name) {
            case "SEGACD": return "MEGA-CD";
            case "PCECD": return "PCE-CD";
            case "PCFX": return "PC-FX";
            case "NEOGEOCD": return "NEOGEO CD";
            case "POKEMINI": return "POKÉ MINI";
            case "ATARI": return "2600";
            case "GW": return "G&W";
            case "ARCADE": return platformTitle(name);
            default: return name;
        }
    }

    /** The system an extension belongs to when only one system uses it, else null. */
    static String systemForExtension(String ext) {
        if (ext.isEmpty() || GENERIC.contains(ext) || SHARED.contains(ext)) return null;
        for (String[] s : SYSTEMS) for (String e : s[2].split(" ")) if (e.equals(ext)) return s[0];
        return null;
    }

    /** Extensions of more than one console, told apart by looking inside the file when the folder does not say. */
    private static final Set<String> SHARED = new HashSet<>(Arrays.asList("rvz", "gcz", "ciso", "wia"));

    /** The key of the folder for files no folder name or content could place; Y on one of them picks its console. */
    static final String UNSORTED = "UNSORTED";

    /** Consoles the player picked for single files (absolute path -> system key), from Settings. */
    static volatile Map<String, String> chosenSystems = new HashMap<>();

    /** Every console a file can be given by hand, in the order of the table. */
    static List<String> systemKeys() {
        List<String> out = new ArrayList<>();
        for (String[] s : SYSTEMS) out.add(s[0]);
        return out;
    }

    /**
     * The console of a file in a folder whose name says nothing (or says something that does not fit): the
     * player's own choice, then the extension, then the file's contents.
     */
    private static String systemOf(File f, String ext, String hint) {
        String chosen = chosenSystems.get(f.getAbsolutePath());
        if (chosen != null) return chosen;
        if (hint == null && SHARED.contains(ext)) {
            String s = Sniff.system(f, ext);
            if (s != null) return s;
        }
        String sys = systemForFile(ext, hint);
        if (sys == null && hint == null && GENERIC.contains(ext)) {
            sys = Sniff.system(f, ext);
            if (sys == null && !ext.equals("m3u")) sys = UNSORTED;
        }
        return sys;
    }

    private static boolean matchesExt(String system, String ext) {
        if (ext.isEmpty()) return false;
        for (String[] s : SYSTEMS) if (s[0].equals(system)) for (String e : s[2].split(" ")) if (e.equals(ext)) return true;
        return false;
    }

    // ---- artwork from other frontends ----

    /** ES-DE folders (with gamelists/ and downloaded_media/) and RetroArch thumbnail folders. */
    private static volatile List<File> esdeRoots = new ArrayList<>(), raThumbRoots = new ArrayList<>();

    private static void findFrontendFolders(Context ctx) {
        List<File> esde = new ArrayList<>(), ra = new ArrayList<>();
        List<File> volumes = new ArrayList<>();
        StorageManager sm = ctx.getSystemService(StorageManager.class);
        for (StorageVolume v : sm.getStorageVolumes()) if (v.getDirectory() != null) volumes.add(v.getDirectory());
        for (File vol : volumes) {
            for (String n : new String[]{"ES-DE", "ES-DE/ES-DE", "EmulationStation"}) {
                File d = new File(vol, n);
                if (new File(d, "downloaded_media").isDirectory() || new File(d, "gamelists").isDirectory()) esde.add(d);
            }
            File t = new File(vol, "RetroArch/thumbnails");
            if (t.isDirectory()) ra.add(t);
        }
        esdeRoots = esde;
        raThumbRoots = ra;
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
            case "NGC": return "Nintendo - GameCube";
            case "WII": return "Nintendo - Wii";
            case "MD": return "Sega - Mega Drive - Genesis";
            case "PS1": return "Sony - PlayStation";
            case "PS2": return "Sony - PlayStation 2";
            case "PSP": return "Sony - PlayStation Portable";
            case "VB": return "Nintendo - Virtual Boy";
            case "SMS": return "Sega - Master System - Mark III";
            case "GG": return "Sega - Game Gear";
            case "32X": return "Sega - 32X";
            case "SEGACD": return "Sega - Mega-CD - Sega CD";
            case "SATURN": return "Sega - Saturn";
            case "DC": return "Sega - Dreamcast";
            case "NAOMI": return "Sega - Naomi";
            case "PCE": return "NEC - PC Engine - TurboGrafx 16";
            case "PCECD": return "NEC - PC Engine CD - TurboGrafx-CD";
            case "PCFX": return "NEC - PC-FX";
            case "NEOGEOCD": return "SNK - Neo Geo CD";
            case "NGPC": return "SNK - Neo Geo Pocket Color";
            case "WS": return "Bandai - WonderSwan";
            case "WSC": return "Bandai - WonderSwan Color";
            case "POKEMINI": return "Nintendo - Pokemon Mini";
            case "LYNX": return "Atari - Lynx";
            case "ATARI": return "Atari - 2600";
            case "GW": return "Handheld Electronic Game";
            case "3DO": return "The 3DO Company - 3DO";
            case "DOS": return "DOS";
            case "ARCADE": return "FBNeo - Arcade Games";
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
            case "video": es = new String[]{"videos"}; ra = new String[0]; break;
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
        // two Pegasus folders that call themselves the same collection ("PS1" and a "PS1 hack" folder that says
        // "collection: PS1"): the one whose folder has another name is shown under its folder name
        Map<String, Integer> named = new HashMap<>();
        for (Platform p : lib.platforms) named.merge(p.name.toLowerCase(Locale.ROOT), 1, Integer::sum);
        for (Platform p : lib.platforms) {
            if (named.get(p.name.toLowerCase(Locale.ROOT)) < 2 || p.games.isEmpty() || p.games.get(0).dir == null) continue;
            String folder = p.games.get(0).dir.getName();
            if (folder.equalsIgnoreCase(p.name)) continue;
            if (p.system.isEmpty()) {
                String sys = systemForFolder(p.name);
                if (sys != null) p.system = sys;
            }
            p.name = folder;
        }
        addInstalled3ds(lib, raw);

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
        for (Platform p : lib.platforms) mergeDiscs(p);
        applyOwnMetadata(ctx, lib);
        GameDb.apply(ctx, lib);
        if (!NameClean.on.isEmpty()) {
            NameClean.apply(lib);
            // tidier names sort differently ("0153 - Zelda" was under 0); Pegasus collections keep their own order
            for (Platform p : lib.platforms) if (p.scanned) p.games.sort((a, b) -> col.compare(a.title, b.title));
        }
        for (Platform p : lib.platforms) for (Game g : p.games) lib.byKey.put(g.key(), g);
        lib.signature = lib.computeSignature();
        return lib;
    }

    // ---- multi-disc games: "Name Disc A" + "Name Disc B" (or Disc 1/2, CD1/CD2) become one entry ----

    private static final java.util.regex.Pattern DISC = java.util.regex.Pattern.compile(
            "(?i)^(.*?)[\\s_.-]*[(\\[]?\\s*(?:disc|disk|cd)\\s*([0-9]{1,2}|[a-f])(?![a-z0-9])(?:\\s*of\\s*\\d+)?\\s*[)\\]]?(.*)$");

    /** {name without the tag, disc number from 1}, or null when the name carries no disc tag. */
    static String[] discTag(String base) {
        java.util.regex.Matcher m = DISC.matcher(base);
        if (!m.matches() || m.group(1).trim().isEmpty()) return null;
        String d = m.group(2).toLowerCase(Locale.ROOT);
        int n = Character.isDigit(d.charAt(0)) ? Integer.parseInt(d) : d.charAt(0) - 'a' + 1;
        String rest = m.group(3).trim();
        return new String[]{(m.group(1).trim() + (rest.isEmpty() ? "" : " " + rest)).trim(), String.valueOf(n)};
    }

    private static void mergeDiscs(Platform p) {
        Map<String, List<Game>> groups = new java.util.LinkedHashMap<>();
        for (Game g : p.games) {
            if (g.file == null || g.dir == null || g.pkg != null || !g.discs.isEmpty()) continue;
            String[] t = discTag(g.baseName());
            if (t == null) continue;
            groups.computeIfAbsent(g.dir.getPath() + "\u0001" + t[0].toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(g);
        }
        for (List<Game> grp : groups.values()) {
            if (grp.size() < 2) continue;
            grp.sort((a, b) -> Integer.compare(Integer.parseInt(discTag(a.baseName())[1]), Integer.parseInt(discTag(b.baseName())[1])));
            Game first = grp.get(0);
            String[] t = discTag(first.baseName());
            first.mediaBase = t[0];
            String[] tt = discTag(first.title);
            if (tt != null) first.title = tt[0];
            for (int i = 1; i < grp.size(); i++) {
                first.discs.add(grp.get(i).file);
                if (first.description.isEmpty()) first.description = grp.get(i).description;
                p.games.remove(grp.get(i));
            }
        }
    }

    // ---- start-up cache: the last scan, so the launcher can show it before scanning again ----

    /** Changes whenever a game, its name, description or pictures change; compared to skip needless redraws. */
    String signature = "";
    private static final int CACHE_VERSION = 4;

    private String computeSignature() {
        long h = 1125899906842597L;
        for (Platform p : platforms) {
            h = 31 * h + p.name.hashCode() + p.launch.hashCode();
            for (Game g : p.games) {
                h = 31 * h + g.key().hashCode();
                h = 31 * h + (g.title + "\u0001" + g.description + "\u0001" + g.assets + "\u0001" + g.ownMedia + "\u0001" + g.launch + "\u0001" + g.discs + "\u0001" + g.genre + "\u0001" + g.series).hashCode();
            }
        }
        return Long.toHexString(h);
    }

    private static void str(java.io.DataOutputStream o, String s) throws IOException {
        o.writeBoolean(s != null);
        if (s != null) o.writeUTF(s);
    }

    private static String str(java.io.DataInputStream in) throws IOException { return in.readBoolean() ? in.readUTF() : null; }

    private static void file(java.io.DataOutputStream o, File f) throws IOException { str(o, f == null ? null : f.getPath()); }

    private static File file(java.io.DataInputStream in) throws IOException {
        String s = str(in);
        return s == null ? null : new File(s);
    }

    static void writeCache(File out, Library lib) {
        File tmp = new File(out.getPath() + ".tmp");
        try (java.io.DataOutputStream o = new java.io.DataOutputStream(new java.io.BufferedOutputStream(
                new java.util.zip.GZIPOutputStream(new java.io.FileOutputStream(tmp))))) {
            o.writeInt(CACHE_VERSION);
            o.writeUTF(lib.signature);
            o.writeInt(lib.platforms.size());
            for (Platform p : lib.platforms) {
                str(o, p.name); str(o, p.sortBy); str(o, p.launch); str(o, p.system);
                o.writeBoolean(p.scanned);
                o.writeInt(p.games.size());
                for (Game g : p.games) {
                    str(o, g.title); str(o, g.file); str(o, g.developer); str(o, g.description); str(o, g.launch); str(o, g.pkg);
                    file(o, g.path); file(o, g.dir); file(o, g.ownMedia);
                    str(o, g.volumeId); str(o, g.relDir); str(o, g.treeDir); str(o, g.titleId);
                    o.writeBoolean(g.scanned);
                    str(o, g.mediaBase);
                    str(o, g.original); str(o, g.genre); str(o, g.series); str(o, g.seriesZh);
                    o.writeInt(g.discs.size());
                    for (String d : g.discs) str(o, d);
                    o.writeInt(g.assets.size());
                    for (Map.Entry<String, String> a : g.assets.entrySet()) { str(o, a.getKey()); str(o, a.getValue()); }
                }
            }
        } catch (IOException e) {
            tmp.delete();
            return;
        }
        tmp.renameTo(out);
    }

    static Library readCache(File in) {
        if (!in.isFile()) return null;
        try (java.io.DataInputStream d = new java.io.DataInputStream(new java.io.BufferedInputStream(
                new java.util.zip.GZIPInputStream(new FileInputStream(in))))) {
            if (d.readInt() != CACHE_VERSION) return null;
            Library lib = new Library();
            lib.signature = d.readUTF();
            for (int i = d.readInt(); i > 0; i--) {
                Platform p = new Platform();
                p.name = str(d); p.sortBy = str(d); p.launch = str(d); p.system = str(d);
                p.scanned = d.readBoolean();
                for (int j = d.readInt(); j > 0; j--) {
                    Game g = new Game();
                    g.title = str(d); g.file = str(d); g.developer = str(d); g.description = str(d); g.launch = str(d); g.pkg = str(d);
                    g.path = file(d); g.dir = file(d); g.ownMedia = file(d);
                    g.volumeId = str(d); g.relDir = str(d); g.treeDir = str(d); g.titleId = str(d);
                    g.scanned = d.readBoolean();
                    g.mediaBase = str(d);
                    g.original = str(d); g.genre = str(d); g.series = str(d); g.seriesZh = str(d);
                    for (int k = d.readInt(); k > 0; k--) g.discs.add(str(d));
                    for (int k = d.readInt(); k > 0; k--) g.assets.put(str(d), str(d));
                    g.platform = p;
                    p.games.add(g);
                }
                lib.platforms.add(p);
            }
            for (Platform p : lib.platforms) for (Game g : p.games) lib.byKey.put(g.key(), g);
            return lib;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    // ---- the launcher's own metadata: <volume>/DuoLauncher/<platform>/ ----

    /**
     * Additions and corrections kept apart from other frontends' files, in Pegasus syntax:
     * DuoLauncher/<platform>/metadata.txt (entries matched by "file:" name or "titleid:") and
     * DuoLauncher/<platform>/media/<file name without extension>/logo|cover|background.png.
     * Whatever is set here wins over Pegasus, ES-DE and the game's own data.
     */
    private static void applyOwnMetadata(Context ctx, Library lib) {
        List<File> roots = new ArrayList<>();
        StorageManager sm = ctx.getSystemService(StorageManager.class);
        for (StorageVolume v : sm.getStorageVolumes()) {
            File r = v.getDirectory() == null ? null : new File(v.getDirectory(), "DuoLauncher");
            if (r != null && r.isDirectory()) roots.add(r);
        }
        if (roots.isEmpty()) return;
        for (Platform p : lib.platforms) {
            if (p.isCustom() || p.games.isEmpty()) continue;
            for (File root : roots) {
                File dir = new File(root, p.name);
                if (!dir.isDirectory() && !p.system.isEmpty()) dir = new File(root, p.system);
                if (!dir.isDirectory()) continue;
                Map<String, Game> byFile = new HashMap<>(), byId = new HashMap<>();
                for (Game g : p.games) {
                    if (g.path != null) byFile.put(g.path.getName(), g);
                    if (g.titleId != null) byId.put(g.titleId, g);
                    if (g.path != null) {
                        File m = new File(dir, "media/" + new File(g.baseName()).getName());
                        if (m.isDirectory()) { g.ownMedia = m; g.resolved.clear(); }
                    }
                }
                Map<String, String> genres = Genres.read(dir);
                for (Game g : p.games) {
                    String k = Genres.key(g), id = k == null ? null : genres.get(k);
                    if (id != null) g.genre = id;
                }
                Map<String, String[]> series = Series.read(dir);
                for (Game g : p.games) {
                    String k = Genres.key(g);
                    String[] s = k == null ? null : series.get(k);
                    if (s != null) { g.series = s[0]; g.seriesZh = s[1]; }
                }
                File meta = new File(dir, "metadata.txt");
                if (!meta.isFile()) continue;
                try {
                    for (Game e : parsePegasus(meta).games) {
                        Game g = e.titleId != null ? byId.get(e.titleId.toUpperCase(Locale.ROOT)) : null;
                        if (g == null && e.file != null) g = byFile.get(new File(e.file).getName());
                        if (g == null) continue;
                        if (e.title != null && !e.title.isEmpty()) g.title = e.title;
                        if (!e.description.isEmpty()) g.description = e.description;
                        if (!e.developer.isEmpty()) g.developer = e.developer;
                        if (e.original != null) g.original = e.original;
                        if (e.genre != null && g.genre == null) g.genre = e.genre;
                        for (Map.Entry<String, String> a : e.assets.entrySet())
                            g.assets.put(a.getKey(), new File(dir, a.getValue()).getAbsolutePath());
                        if (!e.assets.isEmpty()) g.resolved.clear();
                        // media/<own title>/ also works, handy for installed titles whose file is just 00000000.app
                        File m = new File(dir, "media/" + g.title);
                        if (g.ownMedia == null && m.isDirectory()) { g.ownMedia = m; g.resolved.clear(); }
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ---- 3DS titles installed into an emulator (from .cia files) ----

    /** Emulator user folders on internal storage, and how to start a title installed there. */
    private static final String[][] CTR_HOMES = {
            {"azahar", "io.github.azaharplus.android"}, {"citra-emu", "org.citra.emu"}};

    /**
     * Adds installed games (title/00040000/<id>/content/*.app) to the 3DS page. Titles that are also on the
     * card as a ROM file are left out; updates and DLC live under other title types and are skipped.
     */
    private static void addInstalled3ds(Library lib, Map<String, Platform> raw) {
        File prim = Environment.getExternalStorageDirectory();
        Platform p = raw.get("3DS");
        Set<Long> have = new HashSet<>();
        List<Game> known = new ArrayList<>();
        if (p != null) known.addAll(p.games);
        for (Platform q : lib.platforms) if ("3DS".equals(q.system)) known.addAll(q.games);
        for (Game g : known) {
            Object[] info = g.path == null ? null : Icons.ctrInfo(g.path);
            if (info != null) have.add((Long) info[0]);
        }
        for (String[] home : CTR_HOMES) {
            File[] id0s = new File(prim, home[0] + "/sdmc/Nintendo 3DS").listFiles(File::isDirectory);
            if (id0s == null) continue;
            for (File id0 : id0s) {
                File[] id1s = id0.listFiles(File::isDirectory);
                if (id1s == null) continue;
                for (File id1 : id1s) {
                    File[] titles = new File(id1, "title/00040000").listFiles(File::isDirectory);
                    if (titles == null) continue;
                    for (File t : titles) {
                        File content = new File(t, "content");
                        File[] apps = content.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".app"));
                        if (apps == null || apps.length == 0) continue;
                        File main = apps[0];
                        for (File a : apps) if (a.length() > main.length()) main = a;
                        Object[] info = Icons.ctrInfo(main);
                        if (info == null || info[1] == null || !have.add((Long) info[0])) continue;
                        if (p == null) {
                            p = new Platform();
                            p.name = p.system = "3DS";
                            p.scanned = true;
                            p.launch = defaultLaunch("3DS");
                            raw.put("3DS", p);
                        }
                        Game g = new Game();
                        g.file = main.getName();
                        g.title = (String) info[1];
                        g.platform = p;
                        g.scanned = true;
                        g.titleId = String.format(Locale.ROOT, "%016X", (Long) info[0]);
                        place(g, content);
                        if (home[1].equals(PKG_CITRA)) {
                            g.launch = CITRA;
                        } else {
                            g.treeDir = home[0]; // the emulator holds access to its whole user folder
                            g.launch = azahar(home[1]);
                        }
                        p.games.add(g);
                    }
                }
            }
        }
    }

    // ---- installed Android games and apps ----

    /** Emulators and launchers are not listed as games or apps. */
    private static final Set<String> HIDDEN_APPS = new HashSet<>(Arrays.asList(
            "io.github.azaharplus.android", "org.azahar_emu.azahar", "org.citra.emu", "com.antutu.ABenchMark", "com.dsemu.drastic",
            "com.retroarch.aarch64", "com.retroarch", "org.pegasus_frontend.android", "me.magnum.melonds",
            "org.ppsspp.ppsspp", "org.ppsspp.ppssppgold", "xyz.aethersx2.android", "com.github.stenzek.duckstation",
            "org.vita3k.emulator", "com.retroidpocket.gamelauncher", "com.android.launcher3", "com.teslacoilsw.launcher",
            "com.armsx2", "dev.eden.eden_emulator", "com.flycast.emulator", "org.dolphinemu.mmjr", "org.dolphinemu.dolphinemu",
            "org.mupen64plusae.v3.fzurita.pro", "org.mupen64plusae.v3.fzurita"));

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

    /** Set while a game runs: a scan under way waits, so it does not compete with the emulator for the card. */
    static volatile boolean paused;

    private void scan(File dir, String hint, Map<String, Platform> raw, Set<String> seen, int depth) {
        while (paused) {
            try { Thread.sleep(500); } catch (InterruptedException e) { return; }
        }
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
                for (Game g : p.games) {
                    place(g, dir);
                    useExistingFile(g, dir);
                }
                p.games.removeIf(g -> g.file == null || !g.path.isFile());
                // games copied in later without a metadata entry still show up
                Set<String> listed = new HashSet<>(p.listedFiles);
                List<Game> extra = new ArrayList<>();
                for (File f : fs) {
                    String n = f.getName();
                    if (f.isDirectory() || n.startsWith(".") || listed.contains(n)) continue;
                    if (systemForFile(extension(n), p.system.isEmpty() ? hint : p.system) == null) continue;
                    Game g = new Game();
                    g.file = n;
                    g.title = n.substring(0, n.length() - extension(n).length() - (extension(n).isEmpty() ? 0 : 1));
                    g.platform = p;
                    g.scanned = true;
                    place(g, dir);
                    extra.add(g);
                }
                if (!extra.isEmpty()) {
                    applyGamelists(dir, extra);
                    p.games.addAll(extra);
                }
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
            String sys = systemOf(f, extension(n), hint);
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

    /**
     * A Pegasus "files:" list may name the same game in several formats ("x.iso" and "x.cso") of which only one was
     * copied: the first file that exists is used, and listed files that are not there are no extra discs.
     */
    private static void useExistingFile(Game g, File dir) {
        if (g.file == null || g.discs.isEmpty()) return;
        g.discs.removeIf(d -> !new File(dir, d).isFile());
        if (g.path.isFile() || g.discs.isEmpty()) return;
        g.file = g.discs.remove(0);
        g.path = new File(dir, g.file);
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
                    for (String f : v.split("\n")) if (!f.trim().isEmpty()) p.listedFiles.add(new File(f.trim()).getName());
                    if (g != null && g.file == null) {
                        String f = firstLine(v);
                        if (!f.isEmpty()) g.file = f;
                        for (String more : v.split("\n")) {
                            String m = more.trim();
                            if (!m.isEmpty() && !m.equals(f)) g.discs.add(m);
                        }
                    }
                    break;
                case "titleid":
                case "title-id":
                    if (g != null) g.titleId = v.trim().toUpperCase(Locale.ROOT);
                    break;
                case "launch":
                case "command":
                    if (g == null) p.launch = v; else g.launch = v;
                    break;
                case "sort-by":
                case "sort_by":
                    if (g == null) p.sortBy = v;
                    break;
                case "original":
                    if (g != null) g.original = v.trim();
                    break;
                case "genre":
                case "genres":
                    if (g != null && g.genre == null) g.genre = Genres.fromText(firstLine(v));
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
                    // name.en / description.en ... : used instead when the launcher runs in that language
                    else if (g != null && (k.startsWith("name.") || k.startsWith("game.") || k.startsWith("description.")) && forThisLanguage(k)) {
                        if (k.startsWith("description.")) g.description = v; else g.title = firstLine(v);
                    }
            }
        }
        return p;
    }

    /** "name.en", "description.zh-tw", "name.zh" (either Chinese) ... against the current interface language. */
    private static boolean forThisLanguage(String key) {
        String lang = key.substring(key.indexOf('.') + 1), cur = I18n.code().toLowerCase(Locale.ROOT);
        return lang.equals(cur) || cur.startsWith(lang + "-");
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

    /** Pictures, and video snaps for the "video" kind (an image name never ends in a video extension). */
    private static boolean isImage(String n) {
        String s = n.toLowerCase(Locale.ROOT);
        return s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".webp")
                || s.endsWith(".mp4") || s.endsWith(".webm") || s.endsWith(".mkv");
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
            case "NGC": case "GC": case "GAMECUBE": return "GameCube";
            case "WII": return "Wii";
            case "MD": return "Mega Drive / Genesis";
            case "PS1": return "PlayStation";
            case "PS2": return "PlayStation 2";
            case "PS3": return "PlayStation 3";
            case "PS4": return "PlayStation 4";
            case "PS5": return "PlayStation 5";
            case "PSP": return "PlayStation Portable";
            case "PSV": return "PlayStation Vita";
            case "SWITCH": return "Nintendo Switch";
            case "VB": return "Virtual Boy";
            case "SMS": return "Master System";
            case "GG": return "Game Gear";
            case "32X": return "Mega Drive 32X";
            case "SEGACD": return "Mega-CD / Sega CD";
            case "SATURN": return "Sega Saturn";
            case "DC": return "Dreamcast";
            case "NAOMI": return "NAOMI";
            case "PCE": return "PC Engine";
            case "PCECD": return "PC Engine CD";
            case "PCFX": return "PC-FX";
            case "NEOGEOCD": return "Neo Geo CD";
            case "NGPC": return "Neo Geo Pocket Color";
            case "WS": return "WonderSwan";
            case "WSC": return "WonderSwan Color";
            case "POKEMINI": return "Pokémon Mini";
            case "LYNX": return "Atari Lynx";
            case "ATARI": return "Atari 2600";
            case "GW": return "Game & Watch";
            case "3DO": return "3DO";
            case "DOS": return "DOS";
            case "ARCADE": return I18n.t(I18n.S.T_ARCADE);
            case UNSORTED: return I18n.t(I18n.S.T_UNSORTED);
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
            case "NGC": case "GC": case "GAMECUBE": return "gc";
            case "WII": return "wii";
            case "XBOX360": case "X360": return "xbox360";
            case "XBOX": case "XBOXSERIES": case "XBOXSERIESX": case "XSX": return "xboxsx";
            case "ANDROID": return "android";
            case "APPS": return "apps";
            case "NES": case "SNES": case "N64": case "MD": case "PS1": case "PS2": case "PS3": case "PS4": case "PS5":
            case "PSP": case "PSV": case "SWITCH":
                return u.toLowerCase(Locale.ROOT);
            case "VB": case "SMS": case "GG": case "32X": case "SEGACD": case "SATURN": case "DC": case "NAOMI": case "PCE":
            case "PCECD": case "PCFX": case "NEOGEOCD": case "NGPC": case "WS": case "WSC": case "POKEMINI": case "LYNX":
            case "ATARI": case "GW": case "3DO": case "DOS": case "ARCADE":
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
            case "gc": return 0xFF5A4FCF;
            case "wii": return 0xFF3BA9D9;
            case "md": return 0xFF263238;
            case "ps1": return 0xFF607D8B;
            case "ps2": return 0xFF2D3C8F;
            case "ps3": return 0xFF1F2A44;
            case "ps4": return 0xFF1E4FA3;
            case "ps5": return 0xFF3A6FD8;
            case "psp": return 0xFF37474F;
            case "psv": return 0xFF29486B;
            case "switch": return 0xFFE4002B;
            case "vb": return 0xFFB71C1C;
            case "sms": return 0xFF1E3A8A;
            case "gg": return 0xFF374151;
            case "32x": return 0xFF4B5563;
            case "segacd": return 0xFF1F2937;
            case "saturn": return 0xFF4A4E69;
            case "dc": return 0xFFE8742E;
            case "naomi": return 0xFF0E7490;
            case "pce": return 0xFFD97706;
            case "pcecd": return 0xFFB45309;
            case "pcfx": return 0xFF6D28D9;
            case "neogeocd": return 0xFF9A3412;
            case "ngpc": return 0xFF0F766E;
            case "ws": return 0xFF64748B;
            case "wsc": return 0xFF0EA5E9;
            case "pokemini": return 0xFFE11D48;
            case "lynx": return 0xFF475569;
            case "atari": return 0xFF92400E;
            case "gw": return 0xFFCA8A04;
            case "3do": return 0xFF991B1B;
            case "dos": return 0xFF1D4ED8;
            case "arcade": return 0xFFDB2777;
            case "xbox360": return 0xFF6BB233;
            case "xboxsx": return 0xFF107C10;
            case "android": return 0xFF2E9E5B;
            case "apps": return 0xFF4A6FD9;
            default: return 0xFF4A90D9;
        }
    }

    // ---- emulators ----

    static final String PKG_AZAHAR_PLUS = "io.github.azaharplus.android", PKG_AZAHAR = "org.azahar_emu.azahar",
            PKG_CITRA = "org.citra.emu", PKG_CITRA_PERF = "com.antutu.ABenchMark", PKG_DRASTIC = "com.dsemu.drastic", PKG_RETROARCH = "com.retroarch.aarch64",
            PKG_ARMSX2 = "com.armsx2", PKG_AETHERSX2 = "xyz.aethersx2.android", PKG_VITA3K = "org.vita3k.emulator",
            PKG_EDEN = "dev.eden.eden_emulator", PKG_PPSSPP = "org.ppsspp.ppsspp",
            PKG_DUCKSTATION = "com.github.stenzek.duckstation", PKG_MUPEN = "org.mupen64plusae.v3.fzurita.pro",
            PKG_DOLPHIN_MMJR = "org.dolphinemu.mmjr", PKG_DOLPHIN = "org.dolphinemu.dolphinemu";

    // standalone emulators that take a content link get the game through RomProvider
    static final String ARMSX2 = "am start --user 0 -n com.armsx2/.MainActivity -a android.intent.action.VIEW -d {file.provider} --activity-clear-top";
    static final String AETHERSX2 = "am start --user 0 -n xyz.aethersx2.android/.EmulationActivity -a android.intent.action.MAIN -e bootPath {file.provider}";
    static final String VITA3K = "am start --user 0 -n org.vita3k.emulator/.Emulator -a android.intent.action.MAIN --esa AppStartParameters \"-r,{vita.titleid}\"";
    static final String EDEN = "am start --user 0 -n dev.eden.eden_emulator/org.yuzu.yuzu_emu.activities.EmulationActivity -a android.intent.action.VIEW -d {file.provider} --activity-clear-task";
    static final String PPSSPP = "am start --user 0 -n org.ppsspp.ppsspp/.PpssppActivity -a android.intent.action.VIEW -d {file.provider} --activity-clear-task";
    static final String DUCKSTATION = "am start --user 0 -n com.github.stenzek.duckstation/.EmulationActivity -a android.intent.action.MAIN -e bootPath {file.path}";
    static final String MUPEN = "am start --user 0 -n org.mupen64plusae.v3.fzurita.pro/paulscode.android.mupen64plusae.SplashActivity -a android.intent.action.VIEW -d {file.provider}";

    /** Dolphin starts a game given by path through its main activity ("AutoStartFile"). */
    private static String dolphin(String pkg) {
        return "am start --user 0 -n " + pkg + "/org.dolphinemu.dolphinemu.ui.main.MainActivity"
                + " -a android.intent.action.VIEW --es AutoStartFile \"{file.path}\" --activity-clear-top";
    }

    private static String azahar(String pkg) {
        return "am start --user 0 -n " + pkg + "/org.citra.citra_emu.activities.EmulationActivity"
                + " --activity-clear-task --activity-clear-top -d \"{file.uri}\"";
    }

    static final String CITRA = "am start --user 0 -n org.citra.emu/org.citra.emu.ui.EmulationActivity"
            + " -a android.intent.action.VIEW -e GamePath {file.path} --activity-clear-top";
    /**
     * The same Citra MMJ build repackaged under the AnTuTu benchmark's package name, which many Android builds run
     * at full CPU/GPU clocks.
     */
    static final String CITRA_PERF = CITRA.replace("-n org.citra.emu/", "-n " + "com.antutu.ABenchMark" + "/");
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
                o.add(new String[]{"Citra MMJ (" + I18n.t(I18n.S.PERF_MODE) + ")", CITRA_PERF, PKG_CITRA_PERF});
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
                o.add(new String[]{"Mupen64Plus FZ", MUPEN, PKG_MUPEN});
                break;
            case "NGC":
            case "WII":
                o.add(new String[]{"Dolphin MMJR", dolphin(PKG_DOLPHIN_MMJR), PKG_DOLPHIN_MMJR});
                o.add(new String[]{"Dolphin", dolphin(PKG_DOLPHIN), PKG_DOLPHIN});
                break;
            case "MD":
                o.add(new String[]{"RetroArch · Genesis Plus GX", retroArch("genesis_plus_gx"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · PicoDrive", retroArch("picodrive"), PKG_RETROARCH});
                break;
            case "PS1":
                o.add(new String[]{"RetroArch · PCSX ReARMed", retroArch("pcsx_rearmed"), PKG_RETROARCH});
                o.add(new String[]{"RetroArch · SwanStation", retroArch("swanstation"), PKG_RETROARCH});
                o.add(new String[]{"DuckStation", DUCKSTATION, PKG_DUCKSTATION});
                break;
            case "PS2":
                o.add(new String[]{"ARMSX2", ARMSX2, PKG_ARMSX2});
                o.add(new String[]{"AetherSX2", AETHERSX2, PKG_AETHERSX2});
                break;
            case "PSP":
                o.add(new String[]{"PPSSPP", PPSSPP, PKG_PPSSPP});
                o.add(new String[]{"RetroArch · PPSSPP", retroArch("ppsspp"), PKG_RETROARCH});
                break;
            case "PSV":
                o.add(new String[]{"Vita3K", VITA3K, PKG_VITA3K});
                break;
            case "SWITCH":
                o.add(new String[]{"Eden", EDEN, PKG_EDEN});
                break;
            // RetroArch cores for the rest of the Pegasus G pack (its own metadata usually brings launch lines too)
            case "VB": ra(o, "Beetle VB", "mednafen_vb"); break;
            case "SMS":
            case "GG": ra(o, "Genesis Plus GX", "genesis_plus_gx"); ra(o, "PicoDrive", "picodrive"); break;
            case "32X": ra(o, "PicoDrive", "picodrive"); break;
            case "SEGACD": ra(o, "Genesis Plus GX", "genesis_plus_gx"); ra(o, "PicoDrive", "picodrive"); break;
            case "SATURN": ra(o, "Yaba Sanshiro", "yabasanshiro"); ra(o, "Yabause", "yabause"); break;
            case "DC":
            case "NAOMI": ra(o, "Flycast", "flycast"); break;
            case "PCE":
            case "PCECD": ra(o, "Beetle PCE Fast", "mednafen_pce_fast"); ra(o, "Beetle PCE", "mednafen_pce"); break;
            case "PCFX": ra(o, "Beetle PC-FX", "mednafen_pcfx"); break;
            case "NEOGEOCD": ra(o, "NeoCD", "neocd"); break;
            case "NGPC": ra(o, "Beetle NeoPop", "mednafen_ngp"); ra(o, "RACE", "race"); break;
            case "WS":
            case "WSC": ra(o, "Beetle Cygne", "mednafen_wswan"); break;
            case "POKEMINI": ra(o, "PokeMini", "pokemini"); break;
            case "LYNX": ra(o, "Handy", "handy"); ra(o, "Beetle Lynx", "mednafen_lynx"); break;
            case "ATARI": ra(o, "Stella", "stella2014"); ra(o, "Stella (newer)", "stella"); break;
            case "GW": ra(o, "Game & Watch", "gw"); break;
            case "3DO": ra(o, "Opera", "opera"); break;
            case "DOS": ra(o, "DOSBox Pure", "dosbox_pure"); break;
            case "ARCADE": ra(o, "FinalBurn Neo", "fbneo"); ra(o, "MAME 2003-Plus", "mame2003_plus"); break;
            default:
                break;
        }
        return o;
    }

    private static void ra(List<String[]> o, String label, String core) {
        o.add(new String[]{"RetroArch · " + label, retroArch(core), PKG_RETROARCH});
    }

    /** Alternatives offered in the emulator menu: only those whose app is installed. {label, command} */
    static List<String[]> options(Platform p, PackageManager pm) {
        List<String[]> out = new ArrayList<>();
        for (String[] o : allOptions(p.system)) if (installed(pm, o[2])) out.add(new String[]{o[0], o[1]});
        return out;
    }

    /** Every emulator the launcher knows for a system, installed or not: {label, command, package}. */
    static List<String[]> knownOptions(String system) { return allOptions(system); }

    /** The app a launch command starts ("-n <package>/…"), or null for a command that names none. */
    static String packageOf(String cmd) {
        if (cmd == null) return null;
        Matcher m = COMPONENT.matcher(cmd);
        return m.find() ? m.group(1) : null;
    }

    static boolean installed(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, 0);
            // the AnTuTu package name only counts when it is really the repackaged Citra build
            if (pkg.equals(PKG_CITRA_PERF))
                pm.getActivityInfo(new android.content.ComponentName(pkg, "org.citra.emu.ui.EmulationActivity"), 0);
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
        if (cmd.contains(PKG_CITRA_PERF)) return "Citra MMJ (" + I18n.t(I18n.S.PERF_MODE) + ")";   // before Citra: same activity name
        if (cmd.contains(PKG_CITRA)) return "Citra MMJ";
        if (cmd.contains("drastic")) return "DraStic";
        if (cmd.contains(PKG_ARMSX2 + "/")) return "ARMSX2";
        if (cmd.contains(PKG_AETHERSX2)) return "AetherSX2";
        if (cmd.contains(PKG_VITA3K)) return "Vita3K";
        if (cmd.contains(PKG_EDEN)) return "Eden";
        if (cmd.contains(PKG_PPSSPP)) return "PPSSPP";
        if (cmd.contains(PKG_DUCKSTATION)) return "DuckStation";
        if (cmd.contains(PKG_MUPEN)) return "Mupen64Plus FZ";
        if (cmd.contains(PKG_DOLPHIN_MMJR + "/")) return "Dolphin MMJR";
        if (cmd.contains(PKG_DOLPHIN + "/")) return "Dolphin";
        if (cmd.contains("retroarch")) {
            Matcher m = CORE.matcher(cmd);
            return "RetroArch" + (m.find() ? " · " + m.group(1) : "");
        }
        Matcher m = COMPONENT.matcher(cmd);
        return m.find() ? m.group(1) : I18n.t(I18n.S.CUSTOM);
    }
}
