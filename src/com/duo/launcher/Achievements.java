package com.duo.launcher;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RetroAchievements progress for the user's account (Web API key from retroachievements.org → Settings), read from
 * the games they played recently. Games are matched to the library by name: the official English title the AI
 * found ("original:"), else the shown title and the file name. Earning achievements happens in RetroArch itself.
 */
final class Achievements {
    static final class Game {
        String title, console;
        int got, total;
        String norm;
    }

    private static final String API = "https://retroachievements.org/API/";
    private static final List<Game> games = new ArrayList<>();
    static String error;

    static boolean configured(SharedPreferences p) {
        return !p.getString("ra.user", "").trim().isEmpty() && !p.getString("ra.key", "").trim().isEmpty();
    }

    static String norm(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("\\(.*?\\)|\\[.*?\\]", "").replaceAll("[^a-z0-9]", "");
    }

    /** The last list, from the preferences cache (fast, used when drawing). */
    static synchronized List<Game> cached(SharedPreferences p) {
        if (games.isEmpty()) parse(p.getString("ra.cache", "[]"));
        return new ArrayList<>(games);
    }

    private static void parse(String json) {
        games.clear();
        try {
            JSONArray a = new JSONArray(json);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Game g = new Game();
                g.title = o.optString("Title");
                g.console = o.optString("ConsoleName");
                g.total = o.optInt("NumPossibleAchievements", o.optInt("AchievementsTotal"));
                g.got = o.optInt("NumAchieved", o.optInt("NumAwarded"));
                g.norm = norm(g.title);
                if (!g.norm.isEmpty()) games.add(g);
            }
        } catch (Exception ignored) {
        }
    }

    /** Downloads the recently played games (at most once an hour unless forced). Runs on a background thread. */
    static boolean refresh(SharedPreferences p, boolean force) {
        if (!configured(p)) return false;
        if (!force && System.currentTimeMillis() - p.getLong("ra.time", 0) < 3600_000) return true;
        HttpURLConnection con = null;
        try {
            String u = URLEncoder.encode(p.getString("ra.user", "").trim(), "UTF-8"), k = URLEncoder.encode(p.getString("ra.key", "").trim(), "UTF-8");
            con = (HttpURLConnection) new URL(API + "API_GetUserRecentlyPlayedGames.php?u=" + u + "&y=" + k + "&c=50").openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(20000);
            int code = con.getResponseCode();
            if (code != 200) { error = "HTTP " + code; return false; }
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (InputStream in = con.getInputStream()) { Backups.copy(in, b); }
            String json = b.toString("UTF-8").trim();
            if (!json.startsWith("[")) { error = json.length() > 120 ? json.substring(0, 120) : json; return false; }
            synchronized (Achievements.class) { parse(json); }
            p.edit().putString("ra.cache", json).putLong("ra.time", System.currentTimeMillis()).apply();
            error = null;
            return true;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            return false;
        } finally {
            if (con != null) con.disconnect();
        }
    }

    /** The RetroAchievements entry for a library game, or null. */
    static Game find(SharedPreferences p, Library.Game g) {
        List<Game> all = cached(p);
        if (all.isEmpty()) return null;
        String[] names = {g.original, g.title, g.path == null ? null : g.baseName()};
        for (String n : names) {
            String k = norm(n);
            if (k.length() < 4) continue;
            for (Game a : all) if (a.norm.equals(k)) return a;
            for (Game a : all) if (a.norm.length() >= 6 && (k.contains(a.norm) || a.norm.contains(k))) return a;
        }
        return null;
    }
}
