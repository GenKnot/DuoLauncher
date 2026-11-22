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
    private ServerSocket server;

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
    }

    /** Session cookie value for browsers that entered the admin password; new each time the page is switched on. */
    private final String token = Long.toHexString(new java.security.SecureRandom().nextLong()) + Long.toHexString(System.nanoTime());

    WifiServer(Map<String, File> targets, Consumer<String> onReceived, AiSettings ai) {
        this.targets.putAll(targets);
        this.onReceived = onReceived;
        this.ai = ai;
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
            while ((line = readLine(in)) != null && !line.isEmpty()) {
                int k = line.indexOf(':');
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Content-Length"))
                    length = Long.parseLong(line.substring(k + 1).trim());
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Cookie")) cookie = line.substring(k + 1);
            }
            boolean admin = ai != null && cookie.contains("duo=" + token);
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
                    if (ok) reply(out, "200 OK", "text/html; charset=utf-8", page(true, null), "Set-Cookie: duo=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                    else reply(out, "200 OK", "text/html; charset=utf-8", page(false, "Wrong password · 密码不对"));
                    return;
                }
                if (!admin) {
                    reply(out, "403 Forbidden", "text/html; charset=utf-8", page(false, null));
                    return;
                }
                String key = f.getOrDefault("key", "").trim();
                ai.save(f.getOrDefault("provider", "deepseek"), f.getOrDefault("url", "").trim(), f.getOrDefault("model", "").trim(),
                        key.isEmpty() ? null : key);
                String rk = f.getOrDefault("ra_key", "").trim();
                ai.saveRa(f.getOrDefault("ra_user", "").trim(), rk.isEmpty() ? null : rk);
                String ik = f.getOrDefault("img_key", "").trim();
                ai.saveImage(f.getOrDefault("img_url", "").trim(), f.getOrDefault("img_model", "").trim(), ik.isEmpty() ? null : ik);
                reply(out, "200 OK", "text/html; charset=utf-8", page(true, "Saved · 已保存"));
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
                reply(out, "200 OK", "text/html; charset=utf-8", page(admin, null));
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

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;"); }

    /** Settings are locked until the admin password shown on the handheld is entered. */
    private String settings(boolean admin, String note) {
        if (ai == null) return "";
        String msg = note == null ? "" : "<p style='color:#2f8bff;font-weight:600'>" + esc(note) + "</p>";
        if (admin) return aiForm(msg);
        return "<form class=card method=post action=/login style='margin-top:18px'><h1>Settings · 设置</h1>" + msg
                + "<p style='font-size:14px;color:#5c6b7a'>Enter the admin password shown on the handheld (Settings → Wi-Fi transfer). "
                + "输入掌机上显示的管理密码（设置 → Wi-Fi 传输）。</p>"
                + "<input name=pw type=password inputmode=numeric autocomplete=off placeholder='••••••'>"
                + "<button style='margin-top:12px;background:#2f8bff;color:#fff;border:0'>Unlock · 解锁</button></form>";
    }

    /** Settings card for AI translation; the key field is never filled in, only its masked form is shown. */
    private String aiForm(String msg) {
        String[] c = ai.current(), ci = ai.currentImage(), cr = ai.currentRa();
        StringBuilder opts = new StringBuilder();
        for (String[] p : Ai.PROVIDERS)
            opts.append("<option value=\"").append(p[0]).append(p[0].equals(c[0]) ? "\" selected>" : "\">")
                    .append(esc(p[1] == null ? "Custom · 自定义" : p[1])).append("</option>");
        return "<form class=card method=post action=/ai style='margin-top:18px'><h1>AI translation · AI 翻译</h1>" + msg
                + "<p style='font-size:14px;color:#5c6b7a'>Cheat names are translated by this service when a key is set. "
                + "Leave address and model empty for the service's default. 填入密钥后用 AI 翻译金手指；地址和模型留空即用默认。</p>"
                + "<label>Service · 服务</label><select name=provider>" + opts + "</select>"
                + "<label>API key · 密钥" + (c[3].isEmpty() ? "" : " (" + esc(c[3]) + ")") + "</label>"
                + "<input name=key type=password autocomplete=off placeholder='" + (c[3].isEmpty() ? "sk-…" : "unchanged · 不修改") + "'>"
                + "<label>Model · 模型</label><input name=model value=\"" + esc(c[2]) + "\" placeholder=default>"
                + "<label>Address · 接口地址</label><input name=url value=\"" + esc(c[1]) + "\" placeholder=default>"
                + "<h1 style='margin-top:22px'>AI pictures · AI 生图</h1>"
                + "<label>Image API key (OpenAI) · 生图密钥" + (ci[2].isEmpty() ? "" : " (" + esc(ci[2]) + ")") + "</label>"
                + "<input name=img_key type=password autocomplete=off placeholder='" + (ci[2].isEmpty() ? "sk-…" : "unchanged · 不修改") + "'>"
                + "<label>Image model · 生图模型</label><input name=img_model value=\"" + esc(ci[1]) + "\" placeholder=auto>"
                + "<label>Image address · 生图接口地址</label><input name=img_url value=\"" + esc(ci[0]) + "\" placeholder=https://api.openai.com/v1>"
                + "<h1 style='margin-top:22px'>RetroAchievements</h1>"
                + "<label>User name · 用户名</label><input name=ra_user value=\"" + esc(cr[0]) + "\">"
                + "<label>Web API key · 密钥 (retroachievements.org → Settings)" + (cr[1].isEmpty() ? "" : " (" + esc(cr[1]) + ")") + "</label>"
                + "<input name=ra_key type=password autocomplete=off placeholder='" + (cr[1].isEmpty() ? "" : "unchanged · 不修改") + "'>"
                + "<button style='margin-top:16px;background:#2f8bff;color:#fff;border:0'>Save · 保存</button></form>";
    }

    private String page(boolean admin, String note) {
        if (ai != null && !admin) {
            // locked: only the password form, which unlocks both uploads and settings
            return "<!doctype html><html><head><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'>"
                    + "<title>Duo Launcher</title><style>"
                    + "body{font-family:system-ui,sans-serif;background:#eef3f7;color:#1f2a36;margin:0;padding:24px}"
                    + ".card{max-width:560px;margin:auto;background:#fff;border-radius:20px;padding:24px;box-shadow:0 6px 24px #0001}"
                    + "h1{font-size:22px;margin:0 0 16px}button,input{font-size:16px;padding:10px;border-radius:12px;border:1px solid #cdd6df;width:100%;box-sizing:border-box}"
                    + "</style></head><body><div class=card><h1>Duo Launcher · Wi-Fi</h1>"
                    + "<p style='font-size:14px;color:#5c6b7a'>Uploading files and changing settings need the password shown on the "
                    + "handheld. 上传文件和修改设置都要先输入掌机上显示的密码。</p></div>"
                    + settings(false, note) + "</body></html>";
        }
        StringBuilder opts = new StringBuilder();
        int i = 0;
        for (String label : targets.keySet()) opts.append("<option value=\"").append(i++).append("\">").append(esc(label)).append("</option>");
        return "<!doctype html><html><head><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'>"
                + "<title>Duo Launcher</title><style>"
                + "body{font-family:system-ui,sans-serif;background:#eef3f7;color:#1f2a36;margin:0;padding:24px}"
                + ".card{max-width:560px;margin:auto;background:#fff;border-radius:20px;padding:24px;box-shadow:0 6px 24px #0001}"
                + "h1{font-size:22px;margin:0 0 16px}select,button,input{font-size:16px;padding:10px;border-radius:12px;border:1px solid #cdd6df;width:100%;box-sizing:border-box}"
                + "label{display:block;font-size:14px;margin:12px 0 4px;color:#5c6b7a}"
                + "#drop{margin-top:14px;border:2px dashed #9fb3c8;border-radius:16px;padding:36px 12px;text-align:center;color:#5c6b7a;cursor:pointer}"
                + ".row{margin-top:10px;font-size:14px}.bar{height:8px;background:#e3e9ef;border-radius:4px;overflow:hidden}"
                + ".bar i{display:block;height:100%;width:0;background:#2f8bff}</style></head><body><div class=card>"
                + "<h1>Duo Launcher · Wi-Fi</h1><select id=to>" + opts + "</select>"
                + "<div id=drop>⬆ Drop files here or tap to choose<input id=f type=file multiple hidden></div><div id=list></div></div>"
                + settings(admin, note)
                + "<script>const d=document.getElementById('drop'),f=document.getElementById('f');"
                + "d.onclick=()=>f.click();f.onchange=()=>send([...f.files]);"
                + "d.ondragover=e=>{e.preventDefault()};d.ondrop=e=>{e.preventDefault();send([...e.dataTransfer.files])};"
                + "async function send(files){for(const file of files){const row=document.createElement('div');row.className='row';"
                + "row.innerHTML=file.name+'<div class=bar><i></i></div>';document.getElementById('list').prepend(row);"
                + "await new Promise(res=>{const x=new XMLHttpRequest();x.open('PUT','/upload?to='+document.getElementById('to').value+'&name='+encodeURIComponent(file.name));"
                + "x.upload.onprogress=e=>{row.querySelector('i').style.width=(100*e.loaded/e.total)+'%'};"
                + "x.onload=()=>{row.querySelector('i').style.width='100%';row.querySelector('i').style.background=x.status==200?'#2bae66':'#e5484d';res()};"
                + "x.onerror=()=>{row.querySelector('i').style.background='#e5484d';res()};x.send(file)})}}</script></body></html>";
    }
}
