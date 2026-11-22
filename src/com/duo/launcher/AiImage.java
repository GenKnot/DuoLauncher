package com.duo.launcher;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Pictures from an image model the user has a key for (OpenAI's /images/generations, or any service with the
 * same endpoint): folder icons and backdrops in the launcher's own style, and covers for games that have none.
 */
final class AiImage {
    static final String DEFAULT_URL = "https://api.openai.com/v1";
    /** Shown when no model is set: the newest image model the key can use is looked up and remembered. */
    static final String DEFAULT_MODEL = "auto";

    final String url, key;
    String model;
    String error;

    private AiImage(String url, String model, String key) {
        this.url = url;
        this.model = model;
        this.key = key;
    }

    /** The configured service, or null when no key is set. */
    static AiImage from(SharedPreferences prefs) {
        String key = prefs.getString("img.key", "").trim();
        if (key.isEmpty()) return null;
        String url = prefs.getString("img.url", "").trim(), model = prefs.getString("img.model", "").trim();
        if (model.isEmpty() || model.equals(DEFAULT_MODEL)) model = p(prefs).getString("img.model.auto", "");
        AiImage ai = new AiImage((url.isEmpty() ? DEFAULT_URL : url).replaceAll("/+$", ""), model, key);
        ai.prefs = prefs;
        return ai;
    }

    private static SharedPreferences p(SharedPreferences x) { return x; }

    private SharedPreferences prefs;

    /** The newest "gpt-image…" (else any "…image…") model listed for this key, or null. */
    private String newestImageModel() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url + "/models").openConnection();
            String text;
            try {
                con.setConnectTimeout(15000);
                con.setReadTimeout(30000);
                con.setRequestProperty("Authorization", "Bearer " + key);
                if (con.getResponseCode() != 200) return null;
                text = read(con.getInputStream());
            } finally {
                con.disconnect();
            }
            org.json.JSONArray data = new JSONObject(text).getJSONArray("data");
            String best = null, any = null;
            for (int i = 0; i < data.length(); i++) {
                String id = data.getJSONObject(i).optString("id");
                if (id.startsWith("gpt-image") && (best == null || id.compareTo(best) > 0)) best = id;
                if (id.contains("image") && (any == null || id.compareTo(any) > 0)) any = id;
            }
            return best != null ? best : any;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- prompts, matching the bundled artwork ----

    static String iconPrompt(String subject) {
        return "A single app icon object in a cute glossy 3D clay / vinyl-toy style, soft studio lighting, gentle shadow under it, "
                + "slightly exaggerated rounded proportions, three-quarter view from slightly above, centred with a small margin, "
                + "representing: " + subject + ". The objects stand on their own: no app-icon plate, no rounded square, no tile "
                + "or badge behind them. Plain pure white background (left fully transparent when the service can). "
                + "No text, no letters, no logos. Square image.";
    }

    static String backdropPrompt(String subject) {
        return "Wide 3:2 monochrome decorative background for a game launcher, entirely white and light grey, no colour. Embossed "
                + "white paper-cut art: deep layered paper clouds and swirling waves sweep in from the four corners with soft grey "
                + "drop shadows. Two or three small objects per corner drawn with thin soft mid-grey outlines and light shading, on "
                + "the theme: " + subject + ". Scattered tiny pixel stars and pixel hearts, small tilted squares, dotted curves and "
                + "four-point sparkles. A large calm empty pure-white oval in the centre. No text, no logos, no characters, no dark areas.";
    }

    /**
     * For redrawing a light backdrop as its dark-mode cut: the same picture as clean line art, which the launcher
     * turns into light lines on the dark theme colour. Soft shading would become grey haze there.
     */
    static String darkCutPrompt() {
        return "Redraw this exact picture as clean line art for a dark theme. Keep every object, cloud, wave, star, heart and "
                + "dotted curve exactly where it is, at the same size, but draw everything only as crisp, even, thin mid-grey "
                + "outlines on a pure white background. Remove all soft shading, drop shadows, grey fills and textures; inner "
                + "details as thin lines too. Keep the large empty white centre completely empty: no frame, ring, oval or border "
                + "around it or across it. No new objects, no text, no colour.";
    }

    static String coverPrompt(String title, String original, String platform) {
        String name = original != null && !original.isEmpty() ? original + " (" + title + ")" : title;
        return "Portrait game cover illustration for the video game \"" + name + "\" on " + platform + ": a vivid, polished key-art "
                + "style painting of its world, mood and main theme, as on a game box. No text, no title lettering, no logos, "
                + "no rating labels, no platform banner. 2:3 portrait.";
    }

    static String gameBackdropPrompt(String title, String original, String platform) {
        String name = original != null && !original.isEmpty() ? original + " (" + title + ")" : title;
        return "Wide 3:2 landscape background art for the video game \"" + name + "\" on " + platform + ": a calm, atmospheric "
                + "painted scene of a typical place from its world, rich colour and soft light, with no characters in the foreground. "
                + "No text, no letters, no logos, no interface. Landscape image.";
    }

    static String logoPrompt(String original) {
        return "A video game title logo that spells exactly \"" + original + "\", in an original bold, stylised and colourful "
                + "lettering design with depth, outline and a little shine, as on a game's title screen. Only the title words, "
                + "spelled correctly, centred, nothing else. Plain pure white background. Wide image.";
    }

    // ---- the request ----

    /** A picture, or null with {@link #error} set. size: "1024x1024", "1536x1024" or "1024x1536". */
    Bitmap generate(String prompt, String size) { return generate(prompt, size, false); }

    /**
     * transparent: ask for a picture with a see-through background (icons and logos), so that white parts of the
     * object itself are not mistaken for background and cut away (the white dice faces were).
     */
    Bitmap generate(String prompt, String size, boolean transparent) {
        if (model.isEmpty()) {
            String m = newestImageModel();
            if (m == null) { error = "no image model available for this key"; return null; }
            model = m;
            if (prefs != null) prefs.edit().putString("img.model.auto", m).apply();
        }
        try {
            JSONObject body = new JSONObject().put("model", model).put("prompt", prompt).put("size", size).put("n", 1);
            if (model.startsWith("gpt-image")) body.put("quality", "medium");
            if (transparent && model.startsWith("gpt-image")) body.put("background", "transparent").put("output_format", "png");
            HttpURLConnection con = (HttpURLConnection) new URL(url + "/images/generations").openConnection();
            String text;
            int code;
            try {
                con.setConnectTimeout(15000);
                con.setReadTimeout(180000);
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + key);
                try (OutputStream o = con.getOutputStream()) {
                    o.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                code = con.getResponseCode();
                text = read(code >= 400 ? con.getErrorStream() : con.getInputStream());
            } finally {
                con.disconnect();
            }
            JSONObject j = new JSONObject(text);
            if (code != 200) {
                JSONObject err = j.optJSONObject("error");
                error = "HTTP " + code + (err != null ? " · " + err.optString("message", "") : "");
                android.util.Log.e("DuoLauncher", "image request failed: " + error);
                return null;
            }
            JSONObject first = j.getJSONArray("data").getJSONObject(0);
            byte[] png;
            if (first.has("b64_json")) {
                png = android.util.Base64.decode(first.getString("b64_json"), android.util.Base64.DEFAULT);
            } else {
                HttpURLConnection d = (HttpURLConnection) new URL(first.getString("url")).openConnection();
                try (InputStream in = d.getInputStream()) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    Backups.copy(in, b);
                    png = b.toByteArray();
                } finally {
                    d.disconnect();
                }
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(png, 0, png.length);
            if (bmp == null) error = "not an image";
            else Usage.image(size);
            return bmp;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            android.util.Log.e("DuoLauncher", "image request failed: " + error);
            return null;
        }
    }

    /** A picture redrawn from another one (OpenAI's /images/edits), or null with {@link #error} set. */
    Bitmap edit(Bitmap source, String prompt, String size) {
        if (model.isEmpty()) {
            String m = newestImageModel();
            if (m == null) { error = "no image model available for this key"; return null; }
            model = m;
        }
        String bound = "----duo" + Long.toHexString(System.nanoTime());
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url + "/images/edits").openConnection();
            String text;
            int code;
            try {
                con.setConnectTimeout(15000);
                con.setReadTimeout(240000);
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);
                con.setRequestProperty("Authorization", "Bearer " + key);
                try (OutputStream o = con.getOutputStream()) {
                    StringBuilder f = new StringBuilder();
                    String[][] fields = model.startsWith("gpt-image")
                            ? new String[][]{{"model", model}, {"prompt", prompt}, {"size", size}, {"n", "1"}, {"quality", "medium"}}
                            : new String[][]{{"model", model}, {"prompt", prompt}, {"size", size}, {"n", "1"}};
                    for (String[] kv : fields)
                        f.append("--").append(bound).append("\r\nContent-Disposition: form-data; name=\"").append(kv[0]).append("\"\r\n\r\n").append(kv[1]).append("\r\n");
                    f.append("--").append(bound).append("\r\nContent-Disposition: form-data; name=\"image\"; filename=\"source.png\"\r\nContent-Type: image/png\r\n\r\n");
                    o.write(f.toString().getBytes(StandardCharsets.UTF_8));
                    o.write(png(source));
                    o.write(("\r\n--" + bound + "--\r\n").getBytes(StandardCharsets.UTF_8));
                }
                code = con.getResponseCode();
                text = read(code >= 400 ? con.getErrorStream() : con.getInputStream());
            } finally {
                con.disconnect();
            }
            JSONObject j = new JSONObject(text);
            if (code != 200) {
                JSONObject err = j.optJSONObject("error");
                error = "HTTP " + code + (err != null ? " · " + err.optString("message", "") : "");
                return null;
            }
            JSONObject first = j.getJSONArray("data").getJSONObject(0);
            byte[] bytes = android.util.Base64.decode(first.optString("b64_json", ""), android.util.Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp == null) error = "not an image";
            else Usage.image(size);
            return bmp;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            return null;
        }
    }

    /** Makes the plain white background around an icon transparent (flood fill from the edges) and trims it. */
    static Bitmap cutOut(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        // already see-through around the object (asked for a transparent background): only trim it
        boolean clear = true;
        for (int i : new int[]{0, w - 1, (h - 1) * w, h * w - 1, w / 2, (h - 1) * w + w / 2}) if ((px[i] >>> 24) > 16) clear = false;
        if (clear) {
            int minX = w, minY = h, maxX = -1, maxY = -1;
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if ((px[y * w + x] >>> 24) > 16) {
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
            return maxX < minX ? src : Bitmap.createBitmap(src, minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
        boolean[] seen = new boolean[w * h];
        int[] stack = new int[w * h];
        int sp = 0;
        for (int x = 0; x < w; x++) { stack[sp++] = x; stack[sp++] = (h - 1) * w + x; }
        for (int y = 0; y < h; y++) { stack[sp++] = y * w; stack[sp++] = y * w + w - 1; }
        while (sp > 0) {
            int i = stack[--sp];
            if (seen[i]) continue;
            seen[i] = true;
            int c = px[i], r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
            if (r < 232 || g < 232 || b < 232) continue;   // not background
            px[i] = 0;
            int x = i % w, y = i / w;
            if (x > 0 && !seen[i - 1]) stack[sp++] = i - 1;
            if (x < w - 1 && !seen[i + 1]) stack[sp++] = i + 1;
            if (y > 0 && !seen[i - w]) stack[sp++] = i - w;
            if (y < h - 1 && !seen[i + w]) stack[sp++] = i + w;
        }
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if (px[y * w + x] != 0) {
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(px, 0, w, 0, 0, w, h);
        if (maxX < minX) return out;
        return Bitmap.createBitmap(out, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    static byte[] png(Bitmap b) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    private static String read(InputStream in) throws IOException {
        if (in == null) return "{}";
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (InputStream i = in) {
            Backups.copy(i, b);
        }
        return b.toString("UTF-8");
    }
}
