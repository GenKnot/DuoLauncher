package com.duo.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the AI's chat replies aloud with the voice service the player picked in Settings: Soniox (its own key) or
 * OpenAI (the image service's key). A reply is cut at sentence ends into parts the services accept, fetched one after
 * another and played in order; {@link #stop()} silences it at once.
 */
final class Speaker {
    static final String OFF = "off", SONIOX = "soniox", OPENAI = "openai";
    static final String SONIOX_URL = "https://tts-rt.soniox.com/tts", SONIOX_VOICE = "Adrian", OPENAI_VOICE = "coral";

    private final Context ctx;
    private final SharedPreferences prefs;
    private final Handler main = new Handler(Looper.getMainLooper());
    /** Bumped by every new reply and by stop(), so older parts still on their way are dropped. */
    private volatile int turn;
    private MediaPlayer player;
    private final List<File> queue = new ArrayList<>();
    private boolean fetching;
    volatile String error;

    Speaker(Context ctx, SharedPreferences prefs) {
        this.ctx = ctx;
        this.prefs = prefs;
    }

    static String provider(SharedPreferences prefs) { return prefs.getString("tts.provider", OFF); }

    /** Speaking pace: 0 normal, 1 brisk (the default; the services read Chinese slowly), 2 fast. */
    static final float[] SPEEDS = {1.0f, 1.2f, 1.4f};

    static int pace(SharedPreferences prefs) { return Math.max(0, Math.min(2, prefs.getInt("tts.pace", 1))); }

    /** Whether the chosen service has the key it needs. */
    static boolean ready(SharedPreferences prefs) {
        switch (provider(prefs)) {
            case SONIOX: return !prefs.getString("soniox.key", "").trim().isEmpty();
            case OPENAI: return AiImage.from(prefs) != null;
            default: return false;
        }
    }

    /** Speaks text in the given language ("zh-CN", "en" …), replacing whatever was being said. */
    void say(String text, String lang, java.util.function.Consumer<String> onError) {
        stop();
        if (!ready(prefs) || text == null || text.trim().isEmpty()) return;
        int me = ++turn;
        List<String> parts = split(text.trim(), provider(prefs).equals(SONIOX) ? 350 : 900);
        fetching = true;
        new Thread(() -> {
            for (int i = 0; i < parts.size() && me == turn; i++) {
                byte[] audio = fetch(parts.get(i), lang);
                if (audio == null && i == 0 && me == turn && onError != null) {
                    String err = error;
                    main.post(() -> onError.accept(err));
                }
                if (audio == null || me != turn) break;
                File f = new File(ctx.getCacheDir(), "say-" + me + "-" + i + ".mp3");
                try (OutputStream o = new FileOutputStream(f)) { o.write(audio); } catch (Exception e) { break; }
                try {
                    android.media.MediaMetadataRetriever r = new android.media.MediaMetadataRetriever();
                    r.setDataSource(f.getPath());
                    String ms = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
                    r.release();
                    if (ms != null) Usage.textToSpeech(provider(prefs).equals(SONIOX), Long.parseLong(ms) / 1000.0);
                } catch (Exception ignored) {
                }
                main.post(() -> {
                    if (me != turn) { f.delete(); return; }
                    queue.add(f);
                    if (player == null) playNext(me);
                });
            }
            main.post(() -> { if (me == turn) fetching = false; });
        }, "speak").start();
    }

    void stop() {
        turn++;
        fetching = false;
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            player.release();
            player = null;
        }
        for (File f : queue) f.delete();
        queue.clear();
    }

    boolean speaking() { return player != null || fetching; }

    private void playNext(int me) {
        if (me != turn || queue.isEmpty()) return;
        File f = queue.remove(0);
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
            mp.setDataSource(f.getPath());
            mp.setOnCompletionListener(x -> {
                x.release();
                f.delete();
                if (player == x) player = null;
                playNext(me);
            });
            mp.prepare();
            mp.start();
            player = mp;
        } catch (Exception e) {
            mp.release();
            f.delete();
            playNext(me);
        }
    }

    /** Sentence-sized parts, each at most {@code max} characters (a service turns away or cuts long text). */
    static List<String> split(String text, int max) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        StringBuilder sentence = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sentence.append(ch);
            boolean end = "。！？!?；;\n".indexOf(ch) >= 0 || (ch == '.' && (i + 1 == text.length() || text.charAt(i + 1) == ' '));
            if (end || i + 1 == text.length()) {
                if (cur.length() + sentence.length() > max && cur.length() > 0) { out.add(cur.toString().trim()); cur.setLength(0); }
                while (sentence.length() > max) { out.add(sentence.substring(0, max)); sentence.delete(0, max); }
                cur.append(sentence);
                sentence.setLength(0);
            }
        }
        if (cur.toString().trim().length() > 0) out.add(cur.toString().trim());
        return out;
    }

    private byte[] fetch(String text, String lang) {
        try {
            String url, key;
            JSONObject body = new JSONObject();
            if (provider(prefs).equals(SONIOX)) {
                url = SONIOX_URL;
                key = prefs.getString("soniox.key", "").trim();
                body.put("model", "tts-rt-v2");
                body.put("language", lang.contains("-") ? lang.substring(0, lang.indexOf('-')) : lang);
                body.put("voice", prefs.getString("tts.voice.soniox", SONIOX_VOICE));
                body.put("audio_format", "mp3");
                body.put("speed", Math.min(1.3f, SPEEDS[pace(prefs)]));   // Soniox takes 0.7 to 1.3
                body.put("text", text);
            } else {
                AiImage img = AiImage.from(prefs);
                if (img == null) return null;
                String base = prefs.getString("img.url", "").trim();
                if (base.isEmpty()) base = AiImage.DEFAULT_URL;
                url = base.replaceAll("/+$", "") + "/audio/speech";
                key = prefs.getString("img.key", "").trim();
                body.put("model", "gpt-4o-mini-tts");
                body.put("voice", prefs.getString("tts.voice.openai", OPENAI_VOICE));
                body.put("input", text);
                body.put("response_format", "mp3");
                body.put("speed", SPEEDS[pace(prefs)]);
                // the newer voice models follow instructions rather than the speed number
                String[] how = {"Speak naturally, in a warm, friendly voice.", "Speak at a brisk, lively, natural pace, in a warm, friendly voice.",
                        "Speak quickly and energetically, like a lively friend, without dragging any words."};
                body.put("instructions", how[pace(prefs)]);
            }
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15_000);
            c.setReadTimeout(60_000);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Authorization", "Bearer " + key);
            c.setRequestProperty("Content-Type", "application/json");
            try (OutputStream o = c.getOutputStream()) { o.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
            int code = c.getResponseCode();
            InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            if (in != null) {
                byte[] buf = new byte[16384];
                for (int n; (n = in.read(buf)) > 0; ) b.write(buf, 0, n);
                in.close();
            }
            if (code >= 400) {
                String msg = b.toString("UTF-8");
                error = "HTTP " + code + (msg.length() > 160 ? " " + msg.substring(0, 160) : " " + msg);
                return null;
            }
            return b.size() > 0 ? b.toByteArray() : null;
        } catch (Exception e) {
            error = String.valueOf(e.getMessage());
            return null;
        }
    }
}
