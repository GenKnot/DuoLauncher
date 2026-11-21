package com.duo.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

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
    static String subst(String t, Library.Game g) {
        if (t.indexOf('{') < 0) return t;
        String rel = g.relDir.isEmpty() ? g.file : g.relDir + "/" + g.file;
        String uri = "content://com.android.externalstorage.documents/tree/" + Uri.encode(g.volumeId + ":" + g.relDir)
                + "/document/" + Uri.encode(g.volumeId + ":" + rel);
        return t.replace("{file.path}", g.path.getAbsolutePath())
                .replace("{file.uri}", uri)
                .replace("{file.documenturi}", uri)
                .replace("{file.name}", g.path.getName())
                .replace("{file.basename}", g.baseName())
                .replace("{file.dir}", g.path.getParent());
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
        it.addFlags(flags);
        return it;
    }
}
