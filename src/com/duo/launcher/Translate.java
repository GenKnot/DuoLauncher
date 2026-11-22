package com.duo.launcher;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Translates short English cheat names into the interface language. Common cheat phrases come from a small
 * built-in glossary (instant, offline, consistent wording); anything else goes to the free MyMemory API
 * (api.mymemory.translated.net, no key, a few thousand characters a day per address), with Google's free web
 * endpoint as a second try when MyMemory gives the English back. With an AI service set up ({@link Ai}), a
 * game's names go to it together instead, and the free services are only the fallback. Results are cached in a file
 * (AI answers under "ai:<language>", so they replace the free ones).
 */
final class Translate {
    private static final String API = "https://api.mymemory.translated.net/get?q=";

    /** Interface language as MyMemory names it, or null when no translation is needed. */
    static String target() {
        String c = I18n.code();
        if (c.startsWith("en")) return null;
        return c;   // zh-CN, zh-TW, ja, ko, fr, es, de, it, pt, ru
    }

    /** Worth translating: mostly Latin letters, no CJK already. */
    static boolean needs(String s) {
        if (s.codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN
                || Character.UnicodeScript.of(cp) == Character.UnicodeScript.HIRAGANA
                || Character.UnicodeScript.of(cp) == Character.UnicodeScript.HANGUL)) return false;
        int letters = 0;
        for (char ch : s.toCharArray()) if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) letters++;
        return letters >= 4;
    }

    private static final Map<String, String> ZH = new HashMap<>();

    static {
        String[] g = {
                "infinite health", "无限血量", "infinite hp", "无限血量", "max hp", "血量最大", "max health", "血量最大",
                "inf health", "无限血量", "inf hp", "无限血量", "infinite life", "无限生命", "infinite lives", "无限生命",
                "inf lives", "无限生命", "max lives", "生命最大", "infinite magic", "无限魔力", "infinite mp", "无限魔力",
                "max mp", "魔力最大", "max magic", "魔力最大", "infinite money", "无限金钱", "max money", "金钱最大",
                "inf money", "无限金钱", "infinite gold", "无限金币", "max gold", "金币最大", "infinite coins", "无限金币",
                "max coins", "金币最大", "infinite rupees", "无限卢比", "inf rupees", "无限卢比", "max rupees", "卢比最大",
                "infinite ammo", "无限弹药", "inf ammo", "无限弹药", "max ammo", "弹药最大", "infinite items", "无限道具",
                "infinite item", "无限道具", "max items", "道具最大", "all items", "全道具", "have all items", "拥有全部道具",
                "infinite time", "无限时间", "inf time", "无限时间", "stop timer", "时间停止", "freeze timer", "时间冻结",
                "max exp", "经验值最大", "max experience", "经验值最大", "exp multiplier", "经验倍增", "max level", "等级最大",
                "level 99", "等级 99", "max stats", "能力值最大", "max all stats", "全能力值最大", "one hit kill", "一击必杀",
                "one hit kills", "一击必杀", "invincible", "无敌", "invincibility", "无敌", "moon jump", "月球跳",
                "speed hack", "加速", "walk through walls", "穿墙", "no random battles", "不遇敌", "no encounters", "不遇敌",
                "always shiny", "必定闪光", "shiny pokemon", "闪光宝可梦", "infinite stamina", "无限体力", "max stamina", "体力最大",
                "infinite energy", "无限能量", "infinite hearts", "无限心", "all hearts", "全部心", "have all masks", "拥有全部面具",
                "no masks", "没有面具", "infinite arrows", "无限箭", "infinite bombs", "无限炸弹", "max score", "分数最大",
                "unlock all", "全部解锁", "unlock everything", "全部解锁", "all characters", "全角色", "unlock all characters", "解锁全角色",
                "enable magic", "开启魔法", "all hearts collected", "心之容器全满", "all heart pieces", "全部心之碎片",
                "max hearts", "心之容器最大", "all equipment", "全装备", "max items held", "道具持有数最大", "infinite items held", "道具不减",
                "all badges", "全徽章", "all pokemon", "全宝可梦", "rare candy", "神奇糖果", "master ball", "大师球",
                "master code", "主代码（必须开启）", "master code must be on", "主代码（必须开启）", "anti dma", "防 DMA（配合主代码）",
                "no random battle", "不遇敌", "no random battles", "不遇敌", "walk through walls", "穿墙", "max money", "金钱最大",
                "all weapons", "全武器", "have all weapons", "拥有全部武器", "60fps", "60 帧", "60 fps", "60 帧", "30fps", "30 帧",
        };
        for (int i = 0; i < g.length; i += 2) ZH.put(g[i], g[i + 1]);
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private final File cacheFile;
    private final Map<String, String> cache = new HashMap<>();
    private final Map<String, String> aiCache = new HashMap<>();
    private final String to;
    private final Ai ai;
    private boolean outOfQuota, aiFailed;
    /** Game name sent along to the AI as context. */
    String context;

    Translate(File cacheFile, String to) { this(cacheFile, to, null); }

    Translate(File cacheFile, String to, Ai ai) {
        this.cacheFile = cacheFile;
        this.to = to;
        this.ai = ai;
        try {
            for (String line : new String(Files.readAllBytes(cacheFile.toPath()), StandardCharsets.UTF_8).split("\n")) {
                String[] f = line.split("\t");
                if (f.length == 3 && f[0].equals(to)) cache.put(f[1], f[2]);
                else if (f.length == 3 && f[0].equals("ai:" + to)) aiCache.put(f[1], f[2]);
            }
        } catch (IOException ignored) {
        }
    }

    /** Button hints in front ("(START+L) ") and version tags at the end (" v1.2", " v1.0 & v1.1") stay as they are. */
    private static final java.util.regex.Pattern PARTS = java.util.regex.Pattern.compile(
            "^((?:[(（\\[][A-Z0-9+/ ,.&-]+[)）\\]]\\s*)*)(.*?)((?:\\s*[(（]?v\\d+(?:\\.\\d+)*[a-z]?(?:\\s*[&/,]\\s*v?\\d+(?:\\.\\d+)*)*[)）]?)?)$");

    /** {head, core, tail}: button hints and version tags split off the part to translate. */
    private static String[] split(String text) {
        java.util.regex.Matcher m = PARTS.matcher(text.trim());
        if (m.matches() && !m.group(2).trim().isEmpty()) return new String[]{m.group(1), m.group(2).trim(), m.group(3)};
        return new String[]{"", text.trim(), ""};
    }

    boolean usesAi() { return ai != null && !aiFailed; }

    /** The AI's last error, or null. */
    String aiError() { return ai == null ? null : ai.error; }

    /**
     * Sends the names that have no AI answer yet to the AI, in batches, so that the following {@link #translate}
     * calls find them cached. After a failed request the AI is left alone for the rest of this run.
     */
    void prefetch(java.util.Collection<String> texts) {
        if (!usesAi()) return;
        java.util.LinkedHashMap<String, String> todo = new java.util.LinkedHashMap<>();   // key -> core text
        for (String t : texts) {
            String core = split(t)[1];
            String k = key(core);
            if (needs(core) && !aiCache.containsKey(k)) todo.put(k, core);
        }
        List<String> keys = new ArrayList<>(todo.keySet());
        for (int i = 0; i < keys.size() && !aiFailed; i += 60) {
            List<String> part = keys.subList(i, Math.min(keys.size(), i + 60));
            List<String> src = new ArrayList<>();
            for (String k : part) src.add(todo.get(k));
            List<String> out = ai.translate(src, to, context);
            if (out == null) { aiFailed = true; break; }
            for (int j = 0; j < part.size(); j++) if (!out.get(j).isEmpty()) remember("ai:" + to, aiCache, part.get(j), out.get(j));
        }
    }

    private void remember(String lang, Map<String, String> map, String k, String v) {
        map.put(k, v);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(cacheFile, true), StandardCharsets.UTF_8)) {
            w.write(lang + "\t" + k + "\t" + v.replace('\t', ' ').replace('\n', ' ') + "\n");
        } catch (IOException ignored) {
        }
    }

    /** The translation, or null when it cannot be translated now (offline, daily limit reached). */
    String translate(String text) {
        String[] p = split(text);
        String t = translateCore(p[1]);
        return t == null ? null : p[0] + t + p[2];
    }

    private String translateCore(String text) {
        String k = key(text);
        if (ai != null) {
            if (aiCache.containsKey(k)) return aiCache.get(k);
            if (usesAi() && needs(text)) {
                prefetch(java.util.Collections.singletonList(text));
                if (aiCache.containsKey(k)) return aiCache.get(k);
            }
        }
        if (to.equals("zh-CN") && ZH.containsKey(k)) return ZH.get(k);   // the glossary is Simplified Chinese
        if (cache.containsKey(k) && latin(cache.get(k)) == 0) return cache.get(k);   // older answers with English left get another try
        if (!needs(text)) return text;                                  // nothing left to translate (numbers, buttons)
        String a = outOfQuota ? null : myMemory(text);
        // MyMemory sometimes hands the English back or leaves words in it; Google's free endpoint is the second try
        String out = a;
        if (a == null || latin(a) > 0) {
            String b = google(text);
            if (b != null && (a == null || latin(b) < latin(a))) out = b;
        }
        if (out != null) remember(to, cache, k, out);
        return out;
    }

    /** Longest run of Latin letters, a rough measure of English left in a translation. */
    static int latin(String s) {
        int best = 0, run = 0;
        for (char ch : s.toCharArray()) {
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) best = Math.max(best, ++run); else run = 0;
        }
        return best >= 4 ? best : 0;
    }

    /** Names a translator returned as "original (translation)": the translation, else null. */
    static String repair(String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([^（(]*[A-Za-z][^（(]*)[（(](.+)[)）]$").matcher(name.trim());
        if (!m.matches() || !needs(m.group(1))) return null;
        boolean cjk = m.group(2).codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN
                || Character.UnicodeScript.of(cp) == Character.UnicodeScript.HIRAGANA || Character.UnicodeScript.of(cp) == Character.UnicodeScript.KATAKANA
                || Character.UnicodeScript.of(cp) == Character.UnicodeScript.HANGUL);
        if (!cjk || latin(m.group(2)) > 0) return null;   // "Refill Hearts (Max)" is not a translation
        return m.group(2).trim();
    }

    private static String get(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);
            if (con.getResponseCode() != 200) return null;
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (InputStream in = con.getInputStream()) {
                Backups.copy(in, b);
            }
            return b.toString("UTF-8");
        } finally {
            con.disconnect();
        }
    }

    private static String clean(String t, String text) {
        if (t == null) return null;
        t = t.trim();
        // sometimes the answer is "original (translation)": keep the translation
        if (t.startsWith(text) && t.length() > text.length() + 2) {
            String rest = t.substring(text.length()).trim();
            if ((rest.startsWith("(") || rest.startsWith("（")) && (rest.endsWith(")") || rest.endsWith("）")))
                t = rest.substring(1, rest.length() - 1).trim();
        }
        if (t.isEmpty() || t.equalsIgnoreCase(text) || t.toUpperCase(Locale.ROOT).contains("MYMEMORY WARNING")) return null;
        return t;
    }

    private String myMemory(String text) {
        try {
            String body = get(API + URLEncoder.encode(text, "UTF-8") + "&langpair=en|" + to);
            if (body == null) return null;
            JSONObject j = new JSONObject(body);
            if (j.optBoolean("quotaFinished")) { outOfQuota = true; return null; }
            if (j.optInt("responseStatus") != 200) return null;
            return clean(j.getJSONObject("responseData").optString("translatedText", ""), text);
        } catch (Exception e) {
            return null;
        }
    }

    private String google(String text) {
        try {
            String body = get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&dt=t&tl="
                    + URLEncoder.encode(to, "UTF-8") + "&q=" + URLEncoder.encode(text, "UTF-8"));
            if (body == null) return null;
            org.json.JSONArray parts = new org.json.JSONArray(body).getJSONArray(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) sb.append(parts.getJSONArray(i).optString(0, ""));
            return clean(sb.toString(), text);
        } catch (Exception e) {
            return null;
        }
    }
}
