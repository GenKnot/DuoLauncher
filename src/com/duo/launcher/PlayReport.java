package com.duo.launcher;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A month's or a year's play, worked out from the day totals in the prefs ("day:yyyyMMdd", which go back as far as
 * the launcher has counted) and the session log (which games, consoles, genres and times of day).
 */
final class PlayReport {

    static final class Row {
        final String name;
        final long ms;
        final int color;
        final Library.Game game;

        Row(String name, long ms, int color, Library.Game game) {
            this.name = name;
            this.ms = ms;
            this.color = color;
            this.game = game;
        }
    }

    /** A year (month == -1) or one month (0–11) of a year. */
    final int year, month;
    String title, prevTitle;
    long total, prevTotal;
    int daysPlayed, sessions, newGames, consoleCount;
    /** One value per day of the period, oldest first. */
    long[] days;
    /** The weekday column of the period's first day, counted from the locale's first day of the week. */
    int firstColumn;
    /** For a year: the twelve months, and their short names. */
    final long[] months = new long[12];
    final String[] monthNames = new String[12];
    final List<Row> games = new ArrayList<>(), consoles = new ArrayList<>(), genres = new ArrayList<>();
    long longest;
    String longestTitle;
    long bestDayMs;
    String bestDay;
    /** Morning (5–12), afternoon (12–18), evening (18–23), night (23–5), by when a session started. */
    final long[] slots = new long[4];
    /** Weekday names in the locale's order, short. */
    final String[] weekdays = new String[7];

    private PlayReport(int year, int month) {
        this.year = year;
        this.month = month;
    }

    boolean isYear() { return month < 0; }

    static final int[] GENRE_COLORS = {0xFF6366F1, 0xFFE07A3F, 0xFF2BAE66, 0xFFE8B52E, 0xFF14A38B, 0xFFE5484D, 0xFF9B5DE5, 0xFF3D8BFD};

    static PlayReport build(SharedPreferences prefs, List<PlayLog.Entry> log, Map<String, Library.Game> byKey, int year, int month) {
        PlayReport r = new PlayReport(year, month);
        Locale loc = Locale.forLanguageTag(I18n.code());
        Calendar from = Calendar.getInstance(), to = Calendar.getInstance();
        from.clear();
        from.set(year, Math.max(0, month), 1);
        to.setTimeInMillis(from.getTimeInMillis());
        if (month < 0) to.add(Calendar.YEAR, 1); else to.add(Calendar.MONTH, 1);
        long a = from.getTimeInMillis(), b = to.getTimeInMillis();
        r.title = title(loc, year, month);

        // the day totals: the period's days, and the period before for the comparison
        int n = (int) Math.round((b - a) / 86_400_000.0);
        r.days = new long[n];
        SimpleDateFormat key = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
        SimpleDateFormat dayName = new SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(loc, "MMMd"), loc);
        Calendar c = (Calendar) from.clone();
        for (int i = 0; i < n; i++) {
            long v = prefs.getLong("day:" + key.format(c.getTime()), 0);
            r.days[i] = v;
            r.total += v;
            if (v > 0) r.daysPlayed++;
            if (v > r.bestDayMs) { r.bestDayMs = v; r.bestDay = dayName.format(c.getTime()); }
            if (month < 0) r.months[c.get(Calendar.MONTH)] += v;
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        int first = from.getFirstDayOfWeek();
        r.firstColumn = (from.get(Calendar.DAY_OF_WEEK) - first + 7) % 7;
        SimpleDateFormat wd = new SimpleDateFormat("EEEEE", loc), mn = new SimpleDateFormat("LLL", loc);
        Calendar w = Calendar.getInstance();
        w.set(Calendar.DAY_OF_WEEK, first);
        for (int i = 0; i < 7; i++) { r.weekdays[i] = wd.format(w.getTime()); w.add(Calendar.DAY_OF_YEAR, 1); }
        Calendar m = Calendar.getInstance();
        m.clear();
        m.set(2000, 0, 1);
        for (int i = 0; i < 12; i++) { r.monthNames[i] = mn.format(m.getTime()); m.add(Calendar.MONTH, 1); }

        Calendar pa = (Calendar) from.clone();
        if (month < 0) pa.add(Calendar.YEAR, -1); else pa.add(Calendar.MONTH, -1);
        r.prevTitle = month < 0 ? String.valueOf(year - 1) : title(loc, pa.get(Calendar.YEAR), pa.get(Calendar.MONTH));
        for (Calendar d = (Calendar) pa.clone(); d.getTimeInMillis() < a; d.add(Calendar.DAY_OF_YEAR, 1))
            r.prevTotal += prefs.getLong("day:" + key.format(d.getTime()), 0);

        // the session log: games, consoles, genres, times of day, the longest session, games played for the first time
        Map<String, Long> perGame = new HashMap<>(), perConsole = new HashMap<>(), perGenre = new HashMap<>();
        Map<String, Integer> consoleColor = new HashMap<>();
        Map<String, String> titles = new HashMap<>();
        Set<String> before = new HashSet<>(), during = new HashSet<>();
        for (PlayLog.Entry e : log) {
            // by when the session ended, like the day totals (a session past midnight counts on the next day)
            if (e.end < a) { before.add(e.key); continue; }
            if (e.end >= b) continue;
            r.sessions++;
            during.add(e.key);
            Library.Game g = byKey == null ? null : byKey.get(e.key);
            titles.put(e.key, g != null ? g.title : e.title);
            perGame.merge(e.key, e.spent, Long::sum);
            String console = g != null ? g.platform.title() : e.platform.isEmpty() ? "?" : e.platform;
            perConsole.merge(console, e.spent, Long::sum);
            if (g != null) consoleColor.put(console, g.platform.accent());
            if (g != null && g.genre != null) perGenre.merge(g.genre, e.spent, Long::sum);
            if (e.spent > r.longest) { r.longest = e.spent; r.longestTitle = titles.get(e.key); }
            Calendar t = Calendar.getInstance();
            t.setTimeInMillis(e.start);
            int h = t.get(Calendar.HOUR_OF_DAY);
            r.slots[h >= 5 && h < 12 ? 0 : h >= 12 && h < 18 ? 1 : h >= 18 && h < 23 ? 2 : 3] += e.spent;
        }
        for (String k : during) if (!before.contains(k)) r.newGames++;
        r.consoleCount = perConsole.size();
        for (Map.Entry<String, Long> e : sorted(perGame))
            if (r.games.size() < 5) r.games.add(new Row(titles.get(e.getKey()), e.getValue(), 0, byKey == null ? null : byKey.get(e.getKey())));
        long otherC = 0;
        for (Map.Entry<String, Long> e : sorted(perConsole)) {
            if (r.consoles.size() < 5) {
                Integer col = consoleColor.get(e.getKey());
                r.consoles.add(new Row(e.getKey(), e.getValue(), col != null ? col : GENRE_COLORS[r.consoles.size() % GENRE_COLORS.length], null));
            } else otherC += e.getValue();
        }
        if (otherC > 0) r.consoles.add(new Row(I18n.t(I18n.S.REPORT_OTHER), otherC, 0xFF9AA5B1, null));
        for (Map.Entry<String, Long> e : sorted(perGenre))
            if (r.genres.size() < 6) r.genres.add(new Row(Genres.name(e.getKey()), e.getValue(), GENRE_COLORS[r.genres.size() % GENRE_COLORS.length], null));
        return r;
    }

    private static List<Map.Entry<String, Long>> sorted(Map<String, Long> m) {
        List<Map.Entry<String, Long>> l = new ArrayList<>(m.entrySet());
        l.sort((x, y) -> Long.compare(y.getValue(), x.getValue()));
        return l;
    }

    static String title(Locale loc, int year, int month) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, Math.max(0, month), 1);
        String pattern = android.text.format.DateFormat.getBestDateTimePattern(loc, month < 0 ? "y" : "yMMMM");
        return new SimpleDateFormat(pattern, loc).format(c.getTime());
    }

    /** The months (yyyy*12+m) and years that have any play, newest first. */
    static List<int[]> periods(SharedPreferences prefs) {
        Set<Integer> ms = new HashSet<>();
        for (String k : prefs.getAll().keySet()) {
            if (!k.startsWith("day:") || k.length() != 12) continue;
            try {
                int y = Integer.parseInt(k.substring(4, 8)), mo = Integer.parseInt(k.substring(8, 10)) - 1;
                if (prefs.getLong(k, 0) > 0) ms.add(y * 12 + mo);
            } catch (NumberFormatException ignored) { }
        }
        List<Integer> sorted = new ArrayList<>(ms);
        sorted.sort((x, y) -> Integer.compare(y, x));
        List<int[]> out = new ArrayList<>();
        int lastYear = Integer.MIN_VALUE;
        for (int v : sorted) {
            int y = v / 12;
            if (y != lastYear) { out.add(new int[]{y, -1}); lastYear = y; }
            out.add(new int[]{y, v % 12});
        }
        return out;
    }
}
