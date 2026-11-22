package com.duo.launcher;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Translation through a language model the user has a key for: any service with an OpenAI-style
 * /chat/completions endpoint (DeepSeek, Qwen, OpenAI, Gemini, a local server …) or Anthropic's Messages API.
 * The key stays in the app's own settings and is only sent to the address configured here.
 */
final class Ai {
    /** id, label, base address, default model, kind ("openai" or "anthropic"). */
    static final String[][] PROVIDERS = {
            {"deepseek", "DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat", "openai"},
            {"qwen", "通义千问 Qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus", "openai"},
            {"openai", "OpenAI", "https://api.openai.com/v1", "gpt-4.1-mini", "openai"},
            {"claude", "Claude", "https://api.anthropic.com/v1", "claude-haiku-5-5", "anthropic"},
            {"gemini", "Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", "gemini-2.5-flash", "openai"},
            {"custom", null, "", "", "openai"},
    };

    final String provider, url, model, key, kind;

    private Ai(String provider, String url, String model, String key, String kind) {
        this.provider = provider;
        this.url = url;
        this.model = model;
        this.key = key;
        this.kind = kind;
    }

    static String[] preset(String id) {
        for (String[] p : PROVIDERS) if (p[0].equals(id)) return p;
        return PROVIDERS[0];
    }

    /** The configured service, or null when no key is set. */
    static Ai from(SharedPreferences prefs) {
        String key = prefs.getString("ai.key", "").trim();
        if (key.isEmpty()) return null;
        String[] p = preset(prefs.getString("ai.provider", "deepseek"));
        String url = prefs.getString("ai.url", "").trim();
        String model = prefs.getString("ai.model", "").trim();
        if (url.isEmpty()) url = p[2];
        if (model.isEmpty()) model = p[3];
        if (url.isEmpty() || model.isEmpty()) return null;
        return new Ai(p[0], url.replaceAll("/+$", ""), model, key, p[4]);
    }

    /** "sk-…abcd" for showing which key is set without showing it. */
    static String masked(String key) {
        key = key.trim();
        if (key.isEmpty()) return "";
        return key.length() <= 8 ? "••••" : key.substring(0, 3) + "…" + key.substring(key.length() - 4);
    }

    private static final String[][] LANG_NAMES = {
            {"zh-CN", "Simplified Chinese"}, {"zh-TW", "Traditional Chinese (Taiwan)"}, {"ja", "Japanese"}, {"ko", "Korean"},
            {"fr", "French"}, {"es", "Spanish"}, {"de", "German"}, {"it", "Italian"}, {"pt", "Portuguese"}, {"ru", "Russian"},
    };

    static String languageName(String code) {
        for (String[] l : LANG_NAMES) if (l[0].equals(code)) return l[1];
        return code;
    }

    /** Last error, for the test button. */
    String error;

    /**
     * Translates cheat names in one request, so the model sees them together (consistent wording, game context).
     * The answers in the same order, or null when the service failed or answered something else.
     */
    List<String> translate(List<String> names, String language, String game) {
        if (names.isEmpty()) return new ArrayList<>();
        String system = "You translate the names of video game cheat codes for a game launcher. Translate each name from English into "
                + languageName(language) + ", using the short wording players of that language are used to for cheats "
                + "(for Simplified Chinese, e.g. 无限血量, 金钱最大, 一击必杀, 不遇敌, 主代码（必须开启）). Keep button names "
                + "(START, SELECT, L, R, A, B, X, Y, ZL, ZR), numbers, units and proper names that have no common translation as they are. "
                + "Use the official localised names of characters, items and places when they exist. Answer with only a JSON array of "
                + "strings: the translations, in the same order, exactly one for each input.";
        String user = (game == null || game.isEmpty() ? "" : "Game: " + game + "\n") + new JSONArray(names).toString();
        String answer = ask(system, user, Math.min(8000, 200 + names.size() * 60));
        if (answer == null) return null;
        int a = answer.indexOf('['), b = answer.lastIndexOf(']');
        if (a < 0 || b <= a) { error = "unexpected answer"; return null; }
        try {
            JSONArray arr = new JSONArray(answer.substring(a, b + 1));
            if (arr.length() != names.size()) { error = "answer has " + arr.length() + " of " + names.size(); return null; }
            List<String> out = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) out.add(arr.optString(i, "").trim());
            return out;
        } catch (Exception e) {
            error = "unexpected answer";
            return null;
        }
    }

    /** A plain request: the reply text, or null with {@link #error} set. */
    String chat(String system, String user, int maxTokens) { return ask(system, user, Math.min(8000, maxTokens)); }

    /** The model's reply text, or null (with {@link #error} set). */
    private String ask(String system, String user, int maxTokens) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("temperature", 0.2);
            JSONArray msgs = new JSONArray();
            boolean anthropic = kind.equals("anthropic");
            if (anthropic) body.put("system", system);
            else msgs.put(new JSONObject().put("role", "system").put("content", system));
            msgs.put(new JSONObject().put("role", "user").put("content", user));
            body.put("messages", msgs);

            HttpURLConnection con = (HttpURLConnection) new URL(url + (anthropic ? "/messages" : "/chat/completions")).openConnection();
            try {
                con.setConnectTimeout(15000);
                con.setReadTimeout(120000);
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                if (anthropic) {
                    con.setRequestProperty("x-api-key", key);
                    con.setRequestProperty("anthropic-version", "2023-06-01");
                } else {
                    con.setRequestProperty("Authorization", "Bearer " + key);
                }
                try (OutputStream o = con.getOutputStream()) {
                    o.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = con.getResponseCode();
                String text = read(code >= 400 ? con.getErrorStream() : con.getInputStream());
                if (code != 200) {
                    error = "HTTP " + code + message(text);
                    return null;
                }
                JSONObject j = new JSONObject(text);
                JSONObject use = j.optJSONObject("usage");
                if (use != null) Usage.text(model, use.optLong(anthropic ? "input_tokens" : "prompt_tokens"),
                        use.optLong(anthropic ? "output_tokens" : "completion_tokens"));
                if (anthropic) {
                    JSONArray content = j.getJSONArray("content");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < content.length(); i++) sb.append(content.getJSONObject(i).optString("text", ""));
                    return sb.toString();
                }
                return j.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "");
            } finally {
                con.disconnect();
            }
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            return null;
        }
    }

    /** The service's own error message, if it gave one. */
    private static String message(String body) {
        try {
            Object err = new JSONObject(body).opt("error");
            if (err instanceof JSONObject) return " · " + ((JSONObject) err).optString("message", "");
            if (err instanceof String) return " · " + err;
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String read(InputStream in) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (InputStream i = in) {
            Backups.copy(i, b);
        }
        return b.toString("UTF-8");
    }
}
