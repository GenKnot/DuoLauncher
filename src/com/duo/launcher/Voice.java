package com.duo.launcher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Speech to text for the gamepad keyboard and the AI questions: the launcher records while L is held, then sends the
 * recording to the image service's /audio/transcriptions (OpenAI or a compatible service), since the device has no
 * speech recognizer of its own.
 */
final class Voice {
    private static final int RATE = 16000, MAX_SECONDS = 60;
    private AudioRecord rec;
    private Thread reader;
    private volatile boolean on;
    private final ByteArrayOutputStream pcm = new ByteArrayOutputStream();
    String error;

    /** Starts recording; false when the microphone cannot be opened (no permission, or in use). */
    boolean start() {
        try {
            int min = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            rec = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    Math.max(min, RATE));
            if (rec.getState() != AudioRecord.STATE_INITIALIZED) { rec.release(); rec = null; return false; }
            rec.startRecording();
        } catch (Exception e) {
            error = e.getMessage();
            return false;
        }
        on = true;
        reader = new Thread(() -> {
            byte[] buf = new byte[4096];
            while (on && pcm.size() < RATE * 2 * MAX_SECONDS) {
                int n = rec.read(buf, 0, buf.length);
                if (n > 0) synchronized (pcm) { pcm.write(buf, 0, n); }
            }
        }, "voice");
        reader.start();
        return true;
    }

    /** Stops recording; the recording as a WAV file, or null when it is too short to hold a word. */
    byte[] stop() {
        on = false;
        try { if (reader != null) reader.join(1000); } catch (InterruptedException ignored) { }
        if (rec != null) {
            try { rec.stop(); } catch (Exception ignored) { }
            rec.release();
            rec = null;
        }
        byte[] raw;
        synchronized (pcm) { raw = pcm.toByteArray(); }
        if (raw.length < RATE * 2 * 4 / 10) return null;
        return wav(raw);
    }

    private static byte[] wav(byte[] raw) {
        ByteArrayOutputStream o = new ByteArrayOutputStream(raw.length + 44);
        int byteRate = RATE * 2;
        o.write('R'); o.write('I'); o.write('F'); o.write('F');
        le32(o, 36 + raw.length);
        o.write('W'); o.write('A'); o.write('V'); o.write('E');
        o.write('f'); o.write('m'); o.write('t'); o.write(' ');
        le32(o, 16); le16(o, 1); le16(o, 1); le32(o, RATE); le32(o, byteRate); le16(o, 2); le16(o, 16);
        o.write('d'); o.write('a'); o.write('t'); o.write('a');
        le32(o, raw.length);
        o.write(raw, 0, raw.length);
        return o.toByteArray();
    }

    private static void le32(ByteArrayOutputStream o, int v) { for (int i = 0; i < 4; i++) o.write(v >> (8 * i)); }

    private static void le16(ByteArrayOutputStream o, int v) { o.write(v); o.write(v >> 8); }

    /** The words in the recording, or null (then {@link #error} says why). */
    String transcribe(AiImage ai, byte[] wav) {
        String text = post(ai, wav, "gpt-4o-mini-transcribe");
        if (text == null && error != null && error.toLowerCase().contains("model")) text = post(ai, wav, "whisper-1");
        return text;
    }

    private String post(AiImage ai, byte[] wav, String model) {
        String boundary = "duo" + System.nanoTime();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(ai.url + "/audio/transcriptions").openConnection();
            con.setConnectTimeout(15_000);
            con.setReadTimeout(60_000);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + ai.key);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream out = con.getOutputStream()) {
                String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\n" + model + "\r\n"
                        + "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"voice.wav\"\r\n"
                        + "Content-Type: audio/wav\r\n\r\n";
                out.write(head.getBytes(StandardCharsets.UTF_8));
                out.write(wav);
                out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            InputStream in = code < 400 ? con.getInputStream() : con.getErrorStream();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            if (in != null) {
                byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) > 0; ) b.write(buf, 0, n);
                in.close();
            }
            String body = b.toString("UTF-8");
            if (code >= 400) {
                String msg = body;
                try { msg = new JSONObject(body).getJSONObject("error").optString("message", body); } catch (Exception ignored) { }
                error = "HTTP " + code + ": " + (msg.length() > 160 ? msg.substring(0, 160) : msg);
                return null;
            }
            Usage.speechToText(model, Math.max(0, wav.length - 44) / 32000.0);   // 16 kHz, 16-bit mono
            String text = new JSONObject(body).optString("text", "").trim();
            if (text.isEmpty()) { error = "nothing heard"; return null; }
            error = null;
            return text;
        } catch (Exception e) {
            error = e.getMessage();
            return null;
        }
    }
}
