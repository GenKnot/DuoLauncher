package com.duo.launcher;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns a Pegasus-style "am start ..." launch line into an Intent. */
final class Launch {

    static List<String> tokenize(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inTok = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (q != 0) {
                if (c == q) q = 0; else cur.append(c);
                continue;
            }
            if (c == '"' || c == '\'') { q = c; inTok = true; continue; }
            if (Character.isWhitespace(c)) {
                if (inTok) { out.add(cur.toString()); cur.setLength(0); inTok = false; }
                continue;
            }
            cur.append(c);
            inTok = true;
        }
        if (inTok) out.add(cur.toString());
        return out;
    }

    /** Placeholders are replaced per token, so paths with spaces stay one argument. */
    /**
     * Retroid's system keeps RetroArch's real settings (hotkeys, shaders, overlays) in retroarch<W>x<H>.cfg for each
     * screen, and leaves retroarch.cfg nearly empty; set at start-up on those handhelds, null elsewhere.
     */
    static String retroArchConfig;

    static String subst(String t, Library.Game g) {
        if (retroArchConfig != null && t.endsWith("/com.retroarch.aarch64/files/retroarch.cfg"))
            return t.substring(0, t.length() - "retroarch.cfg".length()) + retroArchConfig;
        if (t.indexOf('{') < 0) return t;
        String rel = g.relDir.isEmpty() ? g.file : g.relDir + "/" + g.file;
        String tree = g.treeDir != null ? g.treeDir : g.relDir;
        String uri = "content://com.android.externalstorage.documents/tree/" + Uri.encode(g.volumeId + ":" + tree)
                + "/document/" + Uri.encode(g.volumeId + ":" + rel);
        return t.replace("{file.path}", g.path.getAbsolutePath())
                .replace("{file.provider}", RomProvider.uriFor(g.path).toString())
                .replace("{vita.titleid}", vitaTitleId(g))
                .replace("{file.uri}", uri)
                .replace("{file.documenturi}", uri)
                .replace("{file.name}", g.path.getName())
                .replace("{file.basename}", g.baseName())
                .replace("{file.dir}", g.path.getParent());
    }

    private static final Pattern VITA_ID = Pattern.compile("[A-Z]{4}\\d{5}");

    /**
     * Vita3K starts installed games by title ID (PCSE00000). It is taken from the file name, or from the
     * file itself for ES-DE style .psvita stubs that only hold the ID.
     */
    static String vitaTitleId(Library.Game g) {
        Matcher m = VITA_ID.matcher(g.path.getName().toUpperCase(Locale.ROOT));
        if (m.find()) return m.group();
        if (g.path.length() < 64) {
            try (FileInputStream in = new FileInputStream(g.path)) {
                byte[] b = new byte[64];
                int n = in.read(b);
                if (n > 0) {
                    m = VITA_ID.matcher(new String(b, 0, n, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT));
                    if (m.find()) return m.group();
                }
            } catch (IOException ignored) {
            }
        }
        return g.baseName();
    }

    static Intent build(String cmd, Library.Game g) {
        List<String> t = tokenize(cmd);
        Intent it = new Intent();
        String data = null, type = null;
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        for (int i = 0; i < t.size(); i++) {
            String a = t.get(i);
            switch (a) {
                case "--user": i++; break;
                case "-n": it.setComponent(ComponentName.unflattenFromString(subst(t.get(++i), g))); break;
                case "-a": it.setAction(t.get(++i)); break;
                case "-c": it.addCategory(t.get(++i)); break;
                case "-d": data = subst(t.get(++i), g); break;
                case "-t": type = t.get(++i); break;
                case "-e":
                case "--es": { String k = t.get(++i); it.putExtra(k, subst(t.get(++i), g)); break; }
                case "--esa": { String k = t.get(++i); it.putExtra(k, subst(t.get(++i), g).split(",")); break; }
                case "--ez": { String k = t.get(++i); it.putExtra(k, Boolean.parseBoolean(t.get(++i))); break; }
                case "--ei": { String k = t.get(++i); it.putExtra(k, Integer.parseInt(t.get(++i))); break; }
                case "--el": { String k = t.get(++i); it.putExtra(k, Long.parseLong(t.get(++i))); break; }
                case "--activity-clear-task": flags |= Intent.FLAG_ACTIVITY_CLEAR_TASK; break;
                case "--activity-clear-top": flags |= Intent.FLAG_ACTIVITY_CLEAR_TOP; break;
                case "--activity-single-top": flags |= Intent.FLAG_ACTIVITY_SINGLE_TOP; break;
                case "--activity-no-history": flags |= Intent.FLAG_ACTIVITY_NO_HISTORY; break;
                case "--activity-reorder-to-front": flags |= Intent.FLAG_ACTIVITY_REORDER_TO_FRONT; break;
                case "--activity-exclude-from-recents": flags |= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS; break;
                default: break; // "am", "start" and unknown switches
            }
        }
        if (data != null) {
            if (type != null) it.setDataAndType(Uri.parse(data), type); else it.setData(Uri.parse(data));
        } else if (type != null) {
            it.setType(type);
        }
        if (cmd.contains("{file.provider}")) {
            // the emulator gets read access to this one file only, through the link in data or an extra
            Uri rom = RomProvider.uriFor(g.path);
            it.setClipData(ClipData.newRawUri("", rom));
            flags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
        }
        it.addFlags(flags);
        return it;
    }
}
