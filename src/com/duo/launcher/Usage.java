package com.duo.launcher;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/**
 * What the launcher has asked of the player's AI services, day by day: requests, tokens, pictures and minutes of
 * speech, with a cost estimated from the services' published prices. Kept only on the handheld, so the player can see
 * that nothing runs behind their back. The estimate is a guide; the service's own bill is what counts.
 */
final class Usage {
    private Usage() { }

    static final String TEXT = "text", IMAGE = "image", STT = "stt", TTS = "tts";

    private static File file;
    private static JSONObject days;
    private static android.content.SharedPreferences prefs;

    static synchronized void init(Context c) {
        if (file != null) return;
        prefs = c.getSharedPreferences("duo", Context.MODE_PRIVATE);
        file = new File(c.getFilesDir(), "usage.json");
        try {
            days = file.isFile() ? new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)) : new JSONObject();
        } catch (Exception e) {
            days = new JSONObject();
        }
    }

    private static String day(Date d) { return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(d); }

    private static synchronized void add(String kind, long n, long in, long out, double seconds, double usd) {
        if (days == null) return;
        try {
            String today = day(new Date());
            JSONObject d = days.optJSONObject(today);
            if (d == null) { d = new JSONObject(); days.put(today, d); }
            JSONObject k = d.optJSONObject(kind);
            if (k == null) { k = new JSONObject(); d.put(kind, k); }
            k.put("n", k.optLong("n") + n);
            k.put("in", k.optLong("in") + in);
            k.put("out", k.optLong("out") + out);
            k.put("sec", k.optDouble("sec", 0) + seconds);
            k.put("usd", k.optDouble("usd", 0) + usd);
            if (usd < 0) k.put("unpriced", k.optLong("unpriced") + 1);
            // a little over a year is enough
            if (days.length() > 400) {
                String oldest = null;
                for (Iterator<String> it = days.keys(); it.hasNext(); ) { String s = it.next(); if (oldest == null || s.compareTo(oldest) < 0) oldest = s; }
                days.remove(oldest);
            }
            File tmp = new File(file.getPath() + ".tmp");
            try (FileOutputStream o = new FileOutputStream(tmp)) { o.write(days.toString().getBytes(StandardCharsets.UTF_8)); }
            if (!tmp.renameTo(file)) { file.delete(); tmp.renameTo(file); }
        } catch (Exception ignored) {
        }
    }

    // ---- prices (US dollars), from the services' published lists; unknown models are counted but not priced ----

    /**
     * The player's own price for a kind ("price.text" = "input/output" per million tokens, "price.image" per picture,
     * "price.stt" / "price.tts" per minute), or null to use the published one.
     */
    static double[] own(String kind) {
        if (prefs == null) return null;
        String v = prefs.getString("price." + kind, "").trim();
        if (v.isEmpty()) return null;
        try {
            String[] p = v.split("[/,\\s]+");
            double[] out = new double[p.length];
            for (int i = 0; i < p.length; i++) out[i] = Double.parseDouble(p[i].replace("$", ""));
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    /** The price used for a kind now: the player's own, else the published one for the service in use. */
    static double[] price(String kind, String model) {
        double[] o = own(kind);
        if (o != null && o.length > 0) return kind.equals(TEXT) && o.length == 1 ? new double[]{o[0], o[0]} : o;
        switch (kind) {
            case TEXT: return model == null ? null : textPrice(model);
            case IMAGE: return new double[]{0.06};
            case STT: return new double[]{model != null && model.startsWith("whisper") ? 0.006 : 0.003};
            default: return new double[]{"soniox".equals(model) ? 0.0117 : 0.015};
        }
    }

    /** Per million tokens: input, output. */
    static double[] textPrice(String model) {
        String m = model.toLowerCase(Locale.ROOT);
        if (m.contains("deepseek")) return new double[]{0.30, 1.20};
        if (m.contains("qwen")) return new double[]{0.40, 1.20};
        if (m.contains("gpt-4.1-nano")) return new double[]{0.10, 0.40};
        if (m.contains("gpt-4.1-mini")) return new double[]{0.40, 1.60};
        if (m.contains("gpt-4o-mini")) return new double[]{0.15, 0.60};
        if (m.contains("haiku")) return new double[]{0.10, 0.50};
        if (m.contains("sonnet")) return new double[]{2.00, 10.00};
        if (m.contains("opus")) return new double[]{4.00, 20.00};
        if (m.contains("gemini") && m.contains("flash")) return new double[]{0.30, 2.50};
        return null;
    }

    static void text(String model, long in, long out) {
        double[] p = price(TEXT, model);
        add(TEXT, 1, in, out, 0, p == null ? -1 : (in * p[0] + out * p[1]) / 1e6);
    }

    /** One picture: about 4 US cents square, 6 wide, at the medium quality the launcher asks for. */
    static void image(String size) {
        double[] o = own(IMAGE);
        add(IMAGE, 1, 0, 0, 0, o != null && o.length > 0 ? o[0] : size.startsWith("1024x1024") ? 0.04 : 0.06);
    }

    static void speechToText(String model, double seconds) {
        add(STT, 1, 0, 0, seconds, seconds / 60 * price(STT, model)[0]);
    }

    static void textToSpeech(boolean soniox, double seconds) {
        add(TTS, 1, 0, 0, seconds, seconds / 60 * price(TTS, soniox ? "soniox" : "openai")[0]);
    }

    // ---- reading ----

    /** Totals since a day ("yyyy-MM-dd", inclusive), for one kind or all (kind null): n, in, out, seconds, usd. */
    static synchronized double[] since(String from, String kind) {
        double[] t = new double[5];
        if (days == null) return t;
        for (Iterator<String> it = days.keys(); it.hasNext(); ) {
            String d = it.next();
            if (from != null && d.compareTo(from) < 0) continue;
            JSONObject day = days.optJSONObject(d);
            if (day == null) continue;
            for (String k : new String[]{TEXT, IMAGE, STT, TTS}) {
                if (kind != null && !kind.equals(k)) continue;
                JSONObject o = day.optJSONObject(k);
                if (o == null) continue;
                t[0] += o.optLong("n");
                t[1] += o.optLong("in");
                t[2] += o.optLong("out");
                t[3] += o.optDouble("sec", 0);
                t[4] += Math.max(0, o.optDouble("usd", 0));
            }
        }
        return t;
    }

    static String today() { return day(new Date()); }

    static String weekStart() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (c.getTime().after(new Date())) c.add(Calendar.DATE, -7);
        return day(c.getTime());
    }

    static String monthStart() { return new SimpleDateFormat("yyyy-MM-01", Locale.ROOT).format(new Date()); }

    static synchronized void clear() {
        if (days == null) return;
        days = new JSONObject();
        file.delete();
    }

    static String money(double usd) {
        if (usd <= 0) return "$0";
        if (usd < 0.01) return "< $0.01";
        return String.format(Locale.ROOT, "$%.2f", usd);
    }
}
