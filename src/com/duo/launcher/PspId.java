package com.duo.launcher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * The game ID of a PSP game ("ULJM-05500"), read from UMD_DATA.BIN or PSP_GAME/PARAM.SFO inside an ISO or a
 * CSO (block-compressed ISO), or from the PARAM.SFO inside an EBOOT.PBP. Null when it cannot be read.
 */
final class PspId {
    private static final int SECTOR = 2048;

    /** Reads 2048-byte sectors of a plain or compressed image. */
    private interface Image extends AutoCloseable {
        byte[] sector(long lba) throws IOException;
        @Override void close() throws IOException;
    }

    static String of(File f) {
        String n = f.getName().toLowerCase(Locale.ROOT);
        try {
            if (n.endsWith(".pbp")) return fromPbp(f);
            if (n.endsWith(".iso") || n.endsWith(".cso")) {
                try (Image img = n.endsWith(".cso") ? cso(f) : iso(f)) {
                    byte[] umd = read(img, "UMD_DATA.BIN", 16);
                    if (umd != null) {
                        String id = new String(umd, 0, 10, StandardCharsets.US_ASCII);
                        if (id.matches("[A-Z]{4}-\\d{5}")) return id;
                    }
                    byte[] sfo = read(img, "PSP_GAME/PARAM.SFO", 4096);
                    return sfo == null ? null : discId(sfo);
                }
            }
        } catch (IOException | RuntimeException ignored) {
        }
        return null;
    }

    // ---- images ----

    private static Image iso(File f) throws IOException {
        RandomAccessFile r = new RandomAccessFile(f, "r");
        return new Image() {
            @Override public byte[] sector(long lba) throws IOException {
                byte[] b = new byte[SECTOR];
                r.seek(lba * SECTOR);
                r.readFully(b);
                return b;
            }
            @Override public void close() throws IOException { r.close(); }
        };
    }

    private static long u32(byte[] b, int p) {
        return (b[p] & 0xFFL) | (b[p + 1] & 0xFFL) << 8 | (b[p + 2] & 0xFFL) << 16 | (b[p + 3] & 0xFFL) << 24;
    }

    private static int u16(byte[] b, int p) {
        return (b[p] & 0xFF) | (b[p + 1] & 0xFF) << 8;
    }

    /** CSO: "CISO" header, then one index entry per block; the top bit marks a block stored without compression. */
    private static Image cso(File f) throws IOException {
        RandomAccessFile r = new RandomAccessFile(f, "r");
        byte[] h = new byte[24];
        r.readFully(h);
        if (h[0] != 'C' || h[1] != 'I' || h[2] != 'S' || h[3] != 'O') { r.close(); throw new IOException("not a CSO"); }
        long blockSize = u32(h, 16);
        int align = h[21] & 0xFF;
        if (blockSize != SECTOR) { r.close(); throw new IOException("block size"); }
        return new Image() {
            @Override public byte[] sector(long lba) throws IOException {
                byte[] ix = new byte[8];
                r.seek(24 + lba * 4);
                r.readFully(ix);
                long a = u32(ix, 0), b = u32(ix, 4);
                boolean plain = (a & 0x80000000L) != 0;
                long start = (a & 0x7FFFFFFFL) << align, end = (b & 0x7FFFFFFFL) << align;
                byte[] raw = new byte[(int) Math.min(end - start, SECTOR + 64)];
                r.seek(start);
                r.readFully(raw);
                if (plain) return java.util.Arrays.copyOf(raw, SECTOR);
                Inflater inf = new Inflater(true);
                try {
                    inf.setInput(raw);
                    byte[] out = new byte[SECTOR];
                    int got = inf.inflate(out);
                    if (got != SECTOR) throw new IOException("short block");
                    return out;
                } catch (DataFormatException e) {
                    throw new IOException(e);
                } finally {
                    inf.end();
                }
            }
            @Override public void close() throws IOException { r.close(); }
        };
    }

    // ---- ISO 9660 ----

    /** The first bytes (at most max) of a file, by a path of upper-case names; null when it is not there. */
    private static byte[] read(Image img, String path, int max) throws IOException {
        byte[] pvd = img.sector(16);
        if (pvd[0] != 1 || pvd[1] != 'C' || pvd[2] != 'D') return null;
        long lba = u32(pvd, 156 + 2), size = u32(pvd, 156 + 10);
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            long[] hit = find(img, lba, size, parts[i]);
            if (hit == null) return null;
            lba = hit[0];
            size = hit[1];
        }
        int want = (int) Math.min(size, max);
        byte[] out = new byte[want];
        for (int done = 0; done < want; done += SECTOR) {
            byte[] s = img.sector(lba + done / SECTOR);
            System.arraycopy(s, 0, out, done, Math.min(SECTOR, want - done));
        }
        return out;
    }

    private static long[] find(Image img, long lba, long size, String name) throws IOException {
        for (long off = 0; off < size; off += SECTOR) {
            byte[] s = img.sector(lba + off / SECTOR);
            int p = 0;
            while (p < SECTOR) {
                int len = s[p] & 0xFF;
                if (len == 0) break;
                int nameLen = s[p + 32] & 0xFF;
                String n = new String(s, p + 33, nameLen, StandardCharsets.US_ASCII);
                int semi = n.indexOf(';');
                if (semi >= 0) n = n.substring(0, semi);
                if (n.equalsIgnoreCase(name)) return new long[]{u32(s, p + 2), u32(s, p + 10)};
                p += len;
            }
        }
        return null;
    }

    // ---- PARAM.SFO and EBOOT.PBP ----

    static String discId(byte[] sfo) {
        if (sfo.length < 20 || sfo[0] != 0 || sfo[1] != 'P' || sfo[2] != 'S' || sfo[3] != 'F') return null;
        int keys = (int) u32(sfo, 8), data = (int) u32(sfo, 12), count = (int) u32(sfo, 16);
        for (int i = 0; i < count; i++) {
            int e = 20 + i * 16;
            int k = keys + u16(sfo, e);
            int end = k;
            while (end < sfo.length && sfo[end] != 0) end++;
            if (!new String(sfo, k, end - k, StandardCharsets.US_ASCII).equals("DISC_ID")) continue;
            int len = (int) u32(sfo, e + 4), at = data + (int) u32(sfo, e + 12);
            String id = new String(sfo, at, Math.max(0, len - 1), StandardCharsets.US_ASCII).trim();
            if (id.matches("[A-Z]{4}\\d{5}")) return id.substring(0, 4) + "-" + id.substring(4);
            return id.matches("[A-Z]{4}-\\d{5}") ? id : null;
        }
        return null;
    }

    private static String fromPbp(File f) throws IOException {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            byte[] h = new byte[40];
            r.readFully(h);
            if (h[1] != 'P' || h[2] != 'B' || h[3] != 'P') return null;
            long sfo = u32(h, 8), next = u32(h, 12);
            byte[] b = new byte[(int) Math.min(next - sfo, 8192)];
            r.seek(sfo);
            r.readFully(b);
            return discId(b);
        }
    }

    /** For a quick check on a computer: java PspId.java game.iso */
    public static void main(String[] args) {
        for (String a : args) System.out.println(a + " -> " + of(new File(a)));
    }
}
