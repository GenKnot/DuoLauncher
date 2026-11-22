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
    private ServerSocket server;

    WifiServer(Map<String, File> targets, Consumer<String> onReceived) {
        this.targets.putAll(targets);
        this.onReceived = onReceived;
    }

    boolean running() { return server != null && !server.isClosed(); }

    void start() throws IOException {
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
            String req = readLine(in);
            if (req == null) return;
            String[] parts = req.split(" ");
            if (parts.length < 2) return;
            String method = parts[0], path = parts[1];
            long length = -1;
            String line;
            while ((line = readLine(in)) != null && !line.isEmpty()) {
                int k = line.indexOf(':');
                if (k > 0 && line.substring(0, k).trim().equalsIgnoreCase("Content-Length"))
                    length = Long.parseLong(line.substring(k + 1).trim());
            }
            if (method.equals("PUT") && path.startsWith("/upload?")) {
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
                reply(out, "200 OK", "text/html; charset=utf-8", page());
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
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 " + status + "\r\nContent-Type: " + type + "\r\nContent-Length: " + b.length
                + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(b);
        out.flush();
    }

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;"); }

    private String page() {
        StringBuilder opts = new StringBuilder();
        int i = 0;
        for (String label : targets.keySet()) opts.append("<option value=\"").append(i++).append("\">").append(esc(label)).append("</option>");
        return "<!doctype html><html><head><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'>"
                + "<title>Duo Launcher</title><style>"
                + "body{font-family:system-ui,sans-serif;background:#eef3f7;color:#1f2a36;margin:0;padding:24px}"
                + ".card{max-width:560px;margin:auto;background:#fff;border-radius:20px;padding:24px;box-shadow:0 6px 24px #0001}"
                + "h1{font-size:22px;margin:0 0 16px}select,button{font-size:16px;padding:10px;border-radius:12px;border:1px solid #cdd6df;width:100%}"
                + "#drop{margin-top:14px;border:2px dashed #9fb3c8;border-radius:16px;padding:36px 12px;text-align:center;color:#5c6b7a;cursor:pointer}"
                + ".row{margin-top:10px;font-size:14px}.bar{height:8px;background:#e3e9ef;border-radius:4px;overflow:hidden}"
                + ".bar i{display:block;height:100%;width:0;background:#2f8bff}</style></head><body><div class=card>"
                + "<h1>Duo Launcher · Wi-Fi</h1><select id=to>" + opts + "</select>"
                + "<div id=drop>⬆ Drop files here or tap to choose<input id=f type=file multiple hidden></div><div id=list></div></div>"
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
