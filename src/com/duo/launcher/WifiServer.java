package com.duo.launcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A small web page on the local network for sending games, covers and themes to the device:
 * the browser PUTs each file to /upload?to=<n>&name=<file>, streamed straight to the chosen folder.
 */
final class WifiServer {
    static final int PORT = 8080;

    private final Map<String, File> targets = new LinkedHashMap<>();   // label -> folder
    private final Consumer<String> onReceived;
    private final AiSettings ai;
    /** Exports and save backups the page offers for download (after the password), newest first. */
    private final java.util.function.Supplier<List<File>> downloads;
    private ServerSocket server;
    /** The game library pages and their data (set by the launcher), or null. */
    volatile WebLibrary library;

    /** The AI translation settings, filled in from a computer or phone instead of typing a long key on the handheld. */
    interface AiSettings {
        /** provider id, address, model, masked key */
        String[] current();
        /** key is null to keep the one already set */
        void save(String provider, String url, String model, String key);
        /** The admin password that unlocks the settings on this page (shown on the handheld). */
        String password();
        /** RetroAchievements: user name, masked Web API key */
        String[] currentRa();
        void saveRa(String user, String key);
        /** image service: address, model, masked key */
        String[] currentImage();
        void saveImage(String url, String model, String key);
        /** AI voice replies: provider (off, soniox, openai), voice, masked Soniox key */
        String[] currentVoice();
        void saveVoice(String provider, String voice, String key);
    }

    /** Session cookie value for browsers that entered the admin password; new each time the page is switched on. */
    private final String token = Long.toHexString(new java.security.SecureRandom().nextLong()) + Long.toHexString(System.nanoTime());

    WifiServer(Map<String, File> targets, Consumer<String> onReceived, AiSettings ai, java.util.function.Supplier<List<File>> downloads) {
        this.targets.putAll(targets);
        this.onReceived = onReceived;
        this.ai = ai;
        this.downloads = downloads;
    }

    boolean running() { return server != null && !server.isClosed(); }

    /** Only one password guess at a time, each one slow: a 6-digit password cannot be tried out in parallel. */
    private final Object guessLock = new Object();

    void start() throws IOException {
        // uploads cut off last time leave hidden ".name.part" files behind
        for (File dir : targets.values()) {
            File[] parts = dir.listFiles((d, n) -> n.startsWith(".") && n.endsWith(".part"));
            if (parts != null) for (File f : parts) f.delete();
        }
        server = new ServerSocket(PORT);
        Thread t = new Thread(() -> {
            while (running()) {
                try {
                    Socket s = server.accept();
                    new Thread(() -> handle(s), "wifi-client").start();
                } catch (IOException e) {
                    break;
                }
            }
        }, "wifi-server");
        t.setDaemon(true);
        t.start();
    }

    void stop() {
        try { if (server != null) server.close(); } catch (IOException ignored) { }
        server = null;
    }

    /** http://<this device's Wi-Fi address>:8080, or null when not on a network. */
    static String address() {
        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nis) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses()))
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) return "http://" + a.getHostAddress() + ":" + PORT;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder b = new StringBuilder();
        int c;
        while ((c = in.read()) != -1 && c != '\n') if (c != '\r') b.append((char) c);
        return c == -1 && b.length() == 0 ? null : b.toString();
    }

    private void handle(Socket s) {
        try (Socket sock = s; InputStream in = sock.getInputStream(); OutputStream out = new BufferedOutputStream(sock.getOutputStream())) {
            sock.setSoTimeout(60_000);   // a client that stops sending does not hold a thread forever
            String req = readLine(in);
            if (req == null) return;
            String[] parts = req.split(" ");
            if (parts.length < 2) return;
            String method = parts[0], path = parts[1];
            long length = -1;
            String line, cookie = "";
            LANG.set(I18n.forBrowser(null));
            while ((line = readLine(in)) != null && !line.isEmpty()) {
                int k = line.indexOf(':');
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Content-Length"))
                    length = Long.parseLong(line.substring(k + 1).trim());
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Cookie")) cookie = line.substring(k + 1);
                // the page speaks the browser's first language the launcher has, else English
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Accept-Language"))
                    LANG.set(I18n.forBrowser(line.substring(k + 1)));
            }
            boolean admin = ai != null && cookie.contains("duo=" + token);
            WebLibrary lib = library;
            if (lib != null && (path.equals("/library") || path.startsWith("/library?") || path.startsWith("/api/") || path.startsWith("/img?") || path.startsWith("/art?"))) {
                // the library, like the settings, only after the password
                if (!admin) {
                    if (path.startsWith("/library")) reply(out, "303 See Other", "text/plain", "", "Location: /");
                    else reply(out, "403 Forbidden", "text/plain", "enter the password first");
                    return;
                }
                Map<String, String> q = path.indexOf('?') < 0 ? new LinkedHashMap<>() : query(path.substring(path.indexOf('?') + 1));
                String route = path.indexOf('?') < 0 ? path : path.substring(0, path.indexOf('?'));
                // /library?lang=zh-CN: a language other than the browser's
                if (q.containsKey("lang")) { int li = java.util.Arrays.asList(I18n.CODES).indexOf(q.get("lang")); if (li >= 0) LANG.set(li); }
                if (method.equals("GET") && route.equals("/library")) { reply(out, "200 OK", "text/html; charset=utf-8", lib.page(lang())); return; }
                if (method.equals("GET") && route.equals("/api/library")) {
                    String json = lib.libraryJson();
                    if (json == null) reply(out, "503 Service Unavailable", "text/plain", "busy, try again");
                    else reply(out, "200 OK", "application/json; charset=utf-8", json);
                    return;
                }
                if (method.equals("GET") && route.equals("/api/game")) { reply(out, "200 OK", "application/json; charset=utf-8", lib.gameDetails(q.get("k"))); return; }
                if (method.equals("GET") && route.equals("/img")) {
                    int wd;
                    try { wd = Integer.parseInt(q.getOrDefault("w", "360")); } catch (NumberFormatException e) { wd = 360; }
                    byte[] img = lib.picture(q.get("k"), q.getOrDefault("kind", "box_front"), wd);
                    if (img == null) { reply(out, "404 Not Found", "text/plain", "no picture"); return; }
                    boolean png = img.length > 4 && img[1] == 'P' && img[2] == 'N' && img[3] == 'G';
                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + (png ? "image/png" : "image/jpeg") + "\r\nContent-Length: " + img.length
                            + "\r\nCache-Control: private, max-age=600\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(img);
                    out.flush();
                    return;
                }
                if (method.equals("GET") && route.equals("/art")) {
                    byte[] a = lib.art(q.get("f"));
                    if (a == null) { reply(out, "404 Not Found", "text/plain", "no picture"); return; }
                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + (q.get("f").endsWith(".png") ? "image/png" : "image/jpeg") + "\r\nContent-Length: " + a.length
                            + "\r\nCache-Control: private, max-age=86400\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(a);
                    out.flush();
                    return;
                }
                if (method.equals("PUT") && route.equals("/api/picture")) {
                    String err = lib.savePicture(q.get("k"), q.getOrDefault("kind", "box_front"), in, length);
                    if (err == null) reply(out, "200 OK", "application/json; charset=utf-8", lib.gameJson(q.get("k")));
                    else reply(out, "400 Bad Request", "text/plain", err);
                    return;
                }
                if (method.equals("POST") && (route.equals("/api/game") || route.equals("/api/batch")) && length >= 0 && length < 512 * 1024) {
                    byte[] body = new byte[(int) length];
                    int got = 0;
                    while (got < body.length) {
                        int r = in.read(body, got, body.length - got);
                        if (r < 0) break;
                        got += r;
                    }
                    Map<String, String> f = WebLibrary.form(body, got);
                    String[] keys = route.equals("/api/game") ? new String[]{f.getOrDefault("k", "")} : f.getOrDefault("keys", "").split("\n");
                    f.remove("k");
                    f.remove("keys");
                    String err = lib.edit(keys, f);
                    if (err == null) reply(out, "200 OK", "application/json; charset=utf-8", keys.length == 1 ? lib.gameJson(keys[0]) : "{}");
                    else reply(out, "400 Bad Request", "text/plain", err);
                    return;
                }
                reply(out, "404 Not Found", "text/plain", "not found");
                return;
            }
            if (method.equals("POST") && (path.equals("/ai") || path.equals("/login")) && ai != null && length >= 0 && length < 16384) {
                byte[] body = new byte[(int) length];
                int got = 0;
                while (got < body.length) {
                    int r = in.read(body, got, body.length - got);
                    if (r < 0) break;
                    got += r;
                }
                Map<String, String> f = query(new String(body, 0, got, StandardCharsets.UTF_8).replace("+", "%20"));
                if (path.equals("/login")) {
                    String pw = ai.password();
                    boolean ok;
                    synchronized (guessLock) {
                        ok = pw != null && !pw.isEmpty() && pw.equals(f.getOrDefault("pw", "").trim());
                        if (!ok) Thread.sleep(1500);   // slows down guessing, one guess at a time
                    }
                    if (ok) // back to the page itself, so a reload does not send the password again
                        reply(out, "303 See Other", "text/plain", "", "Location: /\r\nSet-Cookie: duo=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                    else reply(out, "200 OK", "text/html; charset=utf-8", page(false, w(I18n.S.W_WRONG_PASSWORD_TRY_AGAIN), false));
                    return;
                }
                if (!admin) {
                    reply(out, "403 Forbidden", "text/html; charset=utf-8", page(false, null, false));
                    return;
                }
                String key = f.getOrDefault("key", "").trim();
                ai.save(f.getOrDefault("provider", "deepseek"), f.getOrDefault("url", "").trim(), f.getOrDefault("model", "").trim(),
                        key.isEmpty() ? null : key);
                String rk = f.getOrDefault("ra_key", "").trim();
                ai.saveRa(f.getOrDefault("ra_user", "").trim(), rk.isEmpty() ? null : rk);
                String ik = f.getOrDefault("img_key", "").trim();
                ai.saveImage(f.getOrDefault("img_url", "").trim(), f.getOrDefault("img_model", "").trim(), ik.isEmpty() ? null : ik);
                String sk = f.getOrDefault("soniox_key", "").trim();
                String tp = f.getOrDefault("tts", "off");
                if (!tp.equals("soniox") && !tp.equals("openai")) tp = "off";
                ai.saveVoice(tp, f.getOrDefault("tts_voice", "").trim(), sk.isEmpty() ? null : sk);
                reply(out, "200 OK", "text/html; charset=utf-8", page(true, w(I18n.S.W_SAVED), true));
                return;
            }
            if (method.equals("GET") && path.startsWith("/download?")) {
                // exports and backups can hold API keys and play history: only after the password
                if (ai == null || !admin || downloads == null) {
                    reply(out, "403 Forbidden", "text/plain", "enter the password first");
                    return;
                }
                // by name, so a backup made after the page was loaded cannot shift which file a link fetches;
                // only a name from the list itself is accepted
                String n = query(path.substring(path.indexOf('?') + 1)).get("f");
                File hit = null;
                if (n != null) for (File f : downloads.get()) if (f.getName().equals(n) && f.isFile()) { hit = f; break; }
                if (hit == null) {
                    reply(out, "404 Not Found", "text/plain", "not found");
                    return;
                }
                send(out, hit);
                return;
            }
            if (method.equals("PUT") && path.startsWith("/upload?")) {
                // uploads need the password shown on the handheld, like the settings
                if (ai != null && !admin) {
                    reply(out, "403 Forbidden", "text/plain", "enter the password first");
                    return;
                }
                Map<String, String> q = query(path.substring(path.indexOf('?') + 1));
                File dir = null;
                int i = 0;
                for (File f : targets.values()) if (String.valueOf(i++).equals(q.get("to"))) dir = f;
                String name = q.get("name") == null ? "" : new File(q.get("name")).getName();
                if (dir == null || name.isEmpty() || name.startsWith(".") || length < 0) {
                    reply(out, "400 Bad Request", "text/plain", "bad request");
                    return;
                }
                dir.mkdirs();
                File tmp = new File(dir, "." + name + ".part");
                try (OutputStream o = new FileOutputStream(tmp)) {
                    byte[] buf = new byte[256 * 1024];
                    long left = length;
                    while (left > 0) {
                        int r = in.read(buf, 0, (int) Math.min(buf.length, left));
                        if (r < 0) throw new IOException("connection closed");
                        o.write(buf, 0, r);
                        left -= r;
                    }
                }
                File dst = new File(dir, name);
                if (dst.exists()) dst.delete();
                if (!tmp.renameTo(dst)) throw new IOException("rename failed");
                onReceived.accept(name);
                reply(out, "200 OK", "text/plain", "ok");
            } else {
                reply(out, "200 OK", "text/html; charset=utf-8", page(admin, null, false));
            }
        } catch (Exception ignored) {
        }
    }

    private static Map<String, String> query(String q) throws IOException {
        Map<String, String> m = new LinkedHashMap<>();
        for (String kv : q.split("&")) {
            int e = kv.indexOf('=');
            if (e > 0) m.put(kv.substring(0, e), URLDecoder.decode(kv.substring(e + 1), "UTF-8"));
        }
        return m;
    }

    private static void reply(OutputStream out, String status, String type, String body) throws IOException {
        reply(out, status, type, body, null);
    }

    private static void reply(OutputStream out, String status, String type, String body, String header) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 " + status + "\r\nContent-Type: " + type + "\r\nContent-Length: " + b.length
                + "\r\nCache-Control: no-store" + (header == null ? "" : "\r\n" + header)
                + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(b);
        out.flush();
    }

    /** Streams a file as a download under its own name. */
    private static void send(OutputStream out, File f) throws IOException {
        String name = f.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/zip\r\nContent-Length: " + f.length()
                + "\r\nContent-Disposition: attachment; filename=\"" + name + "\"\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        try (InputStream in = new java.io.FileInputStream(f)) {
            byte[] buf = new byte[256 * 1024];
            int r;
            while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
        }
        out.flush();
    }

    // ---- the page: in the browser's language (any of the launcher's 11), light or dark like the phone ----

    /** The language of the page being built: an index into I18n.CODES, or -1 for English. */
    private static final ThreadLocal<Integer> LANG = ThreadLocal.withInitial(() -> -1);

    /** The page's language: the browser's, else English. */
    private static int lang() { int i = LANG.get(); return i < 0 ? 2 : i; }

    private static String w(I18n.S s) { return I18n.t(s, lang()); }

    private static final String CSS = ":root{--bg:#eef3f7;--card:#fff;--text:#1f2a36;--sub:#5c6b7a;--line:#e3e9ef;--field:#f6f8fb;--accent:#2f8bff}"
            + "@media(prefers-color-scheme:dark){:root{--bg:#11151b;--card:#1b222b;--text:#e8edf2;--sub:#94a2b1;--line:#2b343f;--field:#141a21}}"
            + "*{box-sizing:border-box}"
            + "body{font-family:system-ui,-apple-system,'PingFang SC','Microsoft YaHei',sans-serif;background:var(--bg);color:var(--text);margin:0;padding:20px 16px 36px;-webkit-text-size-adjust:100%}"
            + ".wrap{max-width:560px;margin:auto}"
            + "header{display:flex;align-items:center;gap:14px;margin:6px 4px 20px}"
            + ".mark{width:52px;height:52px;border-radius:16px;background:linear-gradient(135deg,#6c8cff,#9b5de5);display:grid;place-items:center;flex:none;box-shadow:0 6px 16px #6c5ce740}"
            + "header h1{font-size:21px;margin:0}header p{margin:3px 0 0;font-size:13px;color:var(--sub)}"
            + ".card{background:var(--card);border-radius:22px;padding:22px;margin-bottom:16px;box-shadow:0 6px 24px #0000000d}.libcard{display:block;text-decoration:none;color:inherit;border:2px solid #9b5de533}.libcard:hover{border-color:#9b5de5}"
            + "h2{font-size:17px;margin:0 0 6px;display:flex;align-items:center;gap:12px}"
            + ".sub{font-size:14px;color:var(--sub);line-height:1.6;margin:8px 0 4px}"
            + "label{display:block;font-size:13px;margin:16px 0 6px;color:var(--sub)}"
            + "input,select{font:inherit;font-size:16px;padding:12px 14px;border-radius:14px;border:1px solid var(--line);background:var(--field);color:var(--text);width:100%}"
            + "input:focus,select:focus{outline:none;border-color:var(--accent);box-shadow:0 0 0 3px #2f8bff33}"
            + "button{font:inherit;font-size:16px;font-weight:600;padding:14px;border-radius:14px;border:0;background:var(--accent);color:#fff;width:100%;margin-top:18px;cursor:pointer}"
            + "button:active{transform:scale(.99)}"
            + ".pin{font-size:26px;letter-spacing:.45em;text-align:center;padding:14px 14px 14px calc(14px + .45em)}"
            + ".note{padding:11px 14px;border-radius:12px;font-size:14px;margin:0 0 16px}"
            + ".ok{background:#2bae661f;color:#1f9d5a}.bad{background:#e5484d1f;color:#d93c41}"
            + ".feat{display:grid;gap:14px;margin:4px 0 0;padding:0;list-style:none}"
            + ".feat li{display:flex;gap:12px;align-items:flex-start;font-size:14px;line-height:1.5;color:var(--sub)}"
            + ".feat b{display:block;color:var(--text);font-size:15px;font-weight:600}"
            + ".dot{width:36px;height:36px;border-radius:12px;display:grid;place-items:center;flex:none}"
            + "#drop{margin-top:16px;border:2px dashed var(--line);border-radius:18px;padding:34px 14px;text-align:center;color:var(--sub);cursor:pointer;line-height:1.6}"
            + "#drop:hover,#drop.on{border-color:var(--accent);color:var(--accent)}"
            + ".row{margin-top:12px;font-size:14px;word-break:break-all}"
            + ".bar{height:8px;background:var(--line);border-radius:4px;overflow:hidden;margin-top:6px}"
            + ".bar i{display:block;height:100%;width:0;background:var(--accent)}"
            + ".dl{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:13px 0;border-top:1px solid var(--line);color:var(--accent);text-decoration:none;word-break:break-all;font-size:15px}"
            + ".dl span{color:var(--sub);font-size:13px;white-space:nowrap}"
            + ".group{border-top:1px solid var(--line);margin-top:24px;padding-top:20px}"
            + "footer{text-align:center;font-size:12px;color:var(--sub);margin-top:6px}footer a{color:inherit}";

    /** A line icon (24-unit box, round strokes). */
    private static String svg(String body, int size, String color) {
        return "<svg width=" + size + " height=" + size + " viewBox='0 0 24 24' fill=none stroke='" + color
                + "' stroke-width=2 stroke-linecap=round stroke-linejoin=round>" + body + "</svg>";
    }

    /** An icon on a soft tile of its colour. */
    private static String dot(String body, String color) {
        return "<span class=dot style='background:" + color + "1f'>" + svg(body, 20, color) + "</span>";
    }

    private static final String I_HANDHELD = "<rect x='6.5' y='2.5' width='11' height='19' rx='2.5'/><path d='M6.5 12h11M10 17.5h4'/>";
    private static final String I_UPLOAD = "<path d='M12 15V4M7 9l5-5 5 5M4 15v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3'/>";
    private static final String I_KEY = "<circle cx='8' cy='15' r='4'/><path d='M11 12l9-9M17 6l3 3M14 9l2 2'/>";
    private static final String I_BOX = "<path d='M12 4v11M7 10l5 5 5-5M4 15v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3'/>";
    private static final String I_LOCK = "<rect x='5' y='11' width='14' height='10' rx='2.5'/><path d='M8 11V8a4 4 0 0 1 8 0v3'/>";
    private static final String I_TEXT = "<path d='M4 6h16M4 12h10M4 18h7M17 14l2 5 2-5M17.8 17h2.4'/>";
    private static final String I_IMAGE = "<rect x='3.5' y='4.5' width='17' height='15' rx='2.5'/><circle cx='9' cy='10' r='1.6'/><path d='M4 17l5-4 4 3 3-2 4 3'/>";
    private static final String I_VOICE = "<path d='M12 4v16M8 8v8M16 8v8M4 11v2M20 11v2'/>";
    private static final String I_TROPHY = "<path d='M8 4h8v6a4 4 0 0 1-8 0zM12 14v4M8 20h8M8 6H5a3 3 0 0 0 3 5M16 6h3a3 3 0 0 1-3 5'/>";

    private static String head() {
        return "<!doctype html><html lang=" + I18n.CODES[lang()] + "><head><meta charset=utf-8>"
                + "<meta name=viewport content='width=device-width,initial-scale=1'><meta name=color-scheme content='light dark'>"
                + "<title>Duo Launcher · " + w(I18n.S.W_WI_FI_TRANSFER) + "</title><style>" + CSS + "</style></head><body><div class=wrap>"
                + "<header><span class=mark>" + svg(I_HANDHELD, 28, "#fff") + "</span><div><h1>Duo Launcher</h1><p>"
                + w(I18n.S.W_WI_FI_TRANSFER_ON) + "</p></div></header>";
    }

    private static String foot() {
        return "<footer>Duo Launcher · <a href='https://github.com/GenKnot/DuoLauncher' target=_blank rel=noopener>GitHub</a></footer></div>";
    }

    /** What the page is for, as three short points. */
    private static String features() {
        return "<ul class=feat>"
                + "<li>" + dot(I_UPLOAD, "#2f8bff") + "<div><b>" + w(I18n.S.W_SEND_GAMES_AND_THEMES) + "</b>"
                + w(I18n.S.W_STRAIGHT_INTO_THE_RIGHT) + "</div></li>"
                + "<li>" + dot(I_KEY, "#9b5de5") + "<div><b>" + w(I18n.S.W_TYPE_YOUR_KEYS_HERE) + "</b>"
                + w(I18n.S.W_AI_AND_RETROACHIEVEMENTS_KEYS) + "</div></li>"
                + "<li>" + dot(I_BOX, "#2bae66") + "<div><b>" + w(I18n.S.W_CONFIGS_AND_BACKUPS) + "</b>"
                + w(I18n.S.W_SEND_A_CONFIG_TO) + "</div></li>"
                + "</ul>";
    }

    private static String note(String text, boolean ok) {
        return text == null ? "" : "<p class='note " + (ok ? "ok" : "bad") + "'>" + esc(text) + "</p>";
    }

    /** The downloads card: every export and save backup, newest first, with its size and date. */
    private String downloadsCard() {
        if (downloads == null) return "";
        List<File> files = downloads.get();
        StringBuilder b = new StringBuilder("<div class=card><h2>" + dot(I_BOX, "#2bae66") + w(I18n.S.W_DOWNLOADS) + "</h2>"
                + "<p class=sub>" + w(I18n.S.W_EXPORTED_CONFIGS_AND_THEMES) + "</p>");
        if (files.isEmpty()) b.append("<p class=sub>").append(w(I18n.S.W_NOTHING_YET)).append("</p>");
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ROOT);
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            String size = f.length() >= 1L << 20 ? (f.length() >> 20) + " MB" : Math.max(1, f.length() >> 10) + " KB";
            b.append("<a class=dl href='/download?f=").append(esc(android.net.Uri.encode(f.getName()))).append("'>").append(esc(f.getName()))
                    .append("<span>").append(size).append(" · ").append(df.format(new java.util.Date(f.lastModified()))).append("</span></a>");
        }
        return b.append("</div>").toString();
    }

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;"); }

    private static String keyField(String name, String masked, String empty) {
        return "<input name=" + name + " type=password autocomplete=off placeholder='"
                + (masked.isEmpty() ? empty : w(I18n.S.W_UNCHANGED)) + "'>";
    }

    private static String keyLabel(String label, String masked) {
        return "<label>" + label + (masked.isEmpty() ? "" : " · " + esc(masked)) + "</label>";
    }

    /** The settings form; key fields are never filled in, only their masked form is shown. */
    private String aiForm() {
        String[] c = ai.current(), ci = ai.currentImage(), cr = ai.currentRa(), cv = ai.currentVoice();
        StringBuilder tts = new StringBuilder();
        for (String[] o : new String[][]{{"off", w(I18n.S.W_OFF)}, {"soniox", "Soniox"}, {"openai", w(I18n.S.W_OPENAI_USES_THE_PICTURE)}})
            tts.append("<option value=").append(o[0]).append(o[0].equals(cv[0]) ? " selected>" : ">").append(o[1]).append("</option>");
        StringBuilder opts = new StringBuilder();
        for (String[] p : Ai.PROVIDERS)
            opts.append("<option value=\"").append(p[0]).append(p[0].equals(c[0]) ? "\" selected>" : "\">")
                    .append(esc(p[1] == null ? w(I18n.S.W_CUSTOM) : p[1])).append("</option>");
        return "<form class=card method=post action=/ai>"
                + "<h2>" + dot(I_TEXT, "#2f8bff") + w(I18n.S.W_AI_TRANSLATION) + "</h2>"
                + "<p class=sub>" + w(I18n.S.W_TRANSLATES_GAME_NAMES_DESCRIPTIONS) + "</p>"
                + "<label>" + w(I18n.S.W_SERVICE) + "</label><select name=provider>" + opts + "</select>"
                + keyLabel(w(I18n.S.W_API_KEY), c[3]) + keyField("key", c[3], "sk-…")
                + "<label>" + w(I18n.S.W_MODEL) + "</label><input name=model value=\"" + esc(c[2]) + "\" placeholder='" + w(I18n.S.W_DEFAULT) + "'>"
                + "<label>" + w(I18n.S.W_ADDRESS) + "</label><input name=url value=\"" + esc(c[1]) + "\" placeholder='" + w(I18n.S.W_DEFAULT) + "'>"
                + "<div class=group><h2>" + dot(I_IMAGE, "#e07a3f") + w(I18n.S.W_AI_PICTURES) + "</h2>"
                + "<p class=sub>" + w(I18n.S.W_DRAWS_COVERS_ICONS_AND) + "</p>"
                + keyLabel(w(I18n.S.W_PICTURE_API_KEY_OPENAI), ci[2]) + keyField("img_key", ci[2], "sk-…")
                + "<label>" + w(I18n.S.W_PICTURE_MODEL) + "</label><input name=img_model value=\"" + esc(ci[1]) + "\" placeholder='" + w(I18n.S.W_AUTOMATIC) + "'>"
                + "<label>" + w(I18n.S.W_PICTURE_ADDRESS) + "</label><input name=img_url value=\"" + esc(ci[0]) + "\" placeholder=https://api.openai.com/v1></div>"
                + "<div class=group><h2>" + dot(I_VOICE, "#14a38b") + w(I18n.S.W_AI_VOICE_REPLIES) + "</h2>"
                + "<p class=sub>" + w(I18n.S.W_READS_THE_AI_CHAT) + "</p>"
                + "<label>" + w(I18n.S.W_VOICE_SERVICE) + "</label><select name=tts>" + tts + "</select>"
                + keyLabel(w(I18n.S.W_SONIOX_API_KEY), cv[2]) + keyField("soniox_key", cv[2], "")
                + "<label>" + w(I18n.S.W_VOICE) + "</label><input name=tts_voice value=\"" + esc(cv[1]) + "\" placeholder='Soniox: Adrian · OpenAI: coral'></div>"
                + "<div class=group><h2>" + dot(I_TROPHY, "#f2a93b") + "RetroAchievements</h2>"
                + "<p class=sub>" + w(I18n.S.W_SHOWS_YOUR_ACHIEVEMENT_PROGRESS) + "</p>"
                + "<label>" + w(I18n.S.W_USER_NAME) + "</label><input name=ra_user value=\"" + esc(cr[0]) + "\" autocomplete=off>"
                + keyLabel("Web API key", cr[1]) + keyField("ra_key", cr[1], "") + "</div>"
                + "<button>" + w(I18n.S.W_SAVE) + "</button></form>";
    }

    private String page(boolean admin, String note, boolean ok) {
        if (ai != null && !admin) {
            // locked: only the password, which unlocks uploads, settings and downloads
            return head()
                    + "<form class=card method=post action=/login>" + note(note, ok)
                    + "<h2>" + dot(I_LOCK, "#2f8bff") + w(I18n.S.W_ENTER_THE_ADMIN_PASSWORD) + "</h2>"
                    + "<p class=sub>" + w(I18n.S.W_IT_S_SHOWN_ON) + "</p>"
                    + "<input class=pin name=pw type=password inputmode=numeric autocomplete=off autofocus placeholder='••••••' aria-label='"
                    + w(I18n.S.W_ADMIN_PASSWORD) + "'>"
                    + "<button>" + w(I18n.S.W_UNLOCK) + "</button></form>"
                    + "<div class=card><h2>" + w(I18n.S.W_WHAT_YOU_CAN_DO) + "</h2>" + features() + "</div>"
                    + foot() + "</body></html>";
        }
        StringBuilder opts = new StringBuilder();
        int i = 0;
        for (String label : targets.keySet()) opts.append("<option value=\"").append(i++).append("\">").append(esc(label)).append("</option>");
        return head() + note(note, ok)
                + "<div class=card><h2>" + dot(I_UPLOAD, "#2f8bff") + w(I18n.S.W_SEND_FILES) + "</h2>"
                + "<p class=sub>" + w(I18n.S.W_PICK_THE_FOLDER_THEN) + "</p>"
                + "<label>" + w(I18n.S.W_SEND_TO) + "</label><select id=to>" + opts + "</select>"
                + "<div id=drop>" + svg(I_UPLOAD, 28, "currentColor") + "<br>" + w(I18n.S.W_DROP_FILES_HERE_OR)
                + "<input id=f type=file multiple hidden></div><div id=list></div></div>"
                + (library != null ? "<a class='card libcard' href=/library><h2>" + dot(I_BOX, "#9b5de5") + w(I18n.S.L_OPEN) + "</h2><p class=sub>"
                        + w(I18n.S.L_OPEN_SUB) + "</p></a>" : "")
                + (ai != null ? aiForm() + downloadsCard() : "")
                + foot()
                + "<script>const d=document.getElementById('drop'),f=document.getElementById('f');"
                + "d.onclick=()=>f.click();f.onchange=()=>send([...f.files]);"
                + "d.ondragover=e=>{e.preventDefault();d.classList.add('on')};d.ondragleave=()=>d.classList.remove('on');"
                + "d.ondrop=e=>{e.preventDefault();d.classList.remove('on');send([...e.dataTransfer.files])};"
                + "async function send(files){for(const file of files){const row=document.createElement('div');row.className='row';"
                + "row.textContent=file.name;const bar=document.createElement('div');bar.className='bar';bar.innerHTML='<i></i>';row.append(bar);"
                + "document.getElementById('list').prepend(row);const i=bar.querySelector('i');"
                + "await new Promise(res=>{const x=new XMLHttpRequest();x.open('PUT','/upload?to='+document.getElementById('to').value+'&name='+encodeURIComponent(file.name));"
                + "x.upload.onprogress=e=>{i.style.width=(100*e.loaded/e.total)+'%'};"
                + "x.onload=()=>{i.style.width='100%';i.style.background=x.status==200?'#2bae66':'#e5484d';res()};"
                + "x.onerror=()=>{i.style.background='#e5484d';res()};x.send(file)})}}</script></body></html>";
    }
}
