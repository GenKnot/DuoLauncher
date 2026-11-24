package com.duo.launcher;

import java.nio.charset.StandardCharsets;

/**
 * A small QR code encoder for the Wi-Fi page address: byte mode, error correction level M,
 * versions 1-10 (up to 213 bytes, far more than "http://192.168.1.23:8080"). No Android classes,
 * so it can be checked on a computer.
 */
final class QrCode {
    private QrCode() { }

    // error correction level M, index = version
    private static final int[] ECC_PER_BLOCK = {0, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26};
    private static final int[] BLOCKS = {0, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5};
    private static final int FORMAT_M = 0;

    /** The modules, [row][column], true for dark; null when the text is too long. */
    static boolean[][] encode(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        int ver = 1;
        for (; ver <= 10; ver++) if (4 + countBits(ver) + 8 * data.length <= dataCodewords(ver) * 8) break;
        if (ver > 10) return null;
        int cap = dataCodewords(ver) * 8;
        Bits b = new Bits();
        b.add(4, 4);                      // byte mode
        b.add(data.length, countBits(ver));
        for (byte x : data) b.add(x & 0xFF, 8);
        b.add(0, Math.min(4, cap - b.n));
        b.add(0, (8 - b.n % 8) % 8);
        for (int pad = 0xEC; b.n < cap; pad ^= 0xEC ^ 0x11) b.add(pad, 8);

        Qr q = new Qr(ver);
        q.drawFunctions();
        q.drawCodewords(interleave(b.bytes(), ver));
        // the mask with the lowest penalty, as the standard asks
        int best = 0, bestScore = Integer.MAX_VALUE;
        for (int m = 0; m < 8; m++) {
            q.applyMask(m);
            q.drawFormat(m);
            int s = q.penalty();
            if (s < bestScore) { best = m; bestScore = s; }
            q.applyMask(m);   // undo (XOR)
        }
        q.applyMask(best);
        q.drawFormat(best);
        return q.mod;
    }

    private static int countBits(int ver) { return ver <= 9 ? 8 : 16; }

    private static int rawModules(int ver) {
        int r = (16 * ver + 128) * ver + 64;
        if (ver >= 2) {
            int n = ver / 7 + 2;
            r -= (25 * n - 10) * n - 55;
            if (ver >= 7) r -= 36;
        }
        return r;
    }

    private static int dataCodewords(int ver) { return rawModules(ver) / 8 - ECC_PER_BLOCK[ver] * BLOCKS[ver]; }

    private static final class Bits {
        final java.util.ArrayList<Boolean> v = new java.util.ArrayList<>();
        int n;

        void add(int val, int len) {
            for (int i = len - 1; i >= 0; i--) { v.add(((val >>> i) & 1) != 0); n++; }
        }

        byte[] bytes() {
            byte[] out = new byte[n / 8];
            for (int i = 0; i < n; i++) if (v.get(i)) out[i >>> 3] |= 1 << (7 - (i & 7));
            return out;
        }
    }

    /** Splits the data into blocks, adds each block's error correction and interleaves them. */
    private static byte[] interleave(byte[] data, int ver) {
        int blocks = BLOCKS[ver], ecc = ECC_PER_BLOCK[ver], raw = rawModules(ver) / 8;
        int shortBlocks = blocks - raw % blocks, shortLen = raw / blocks;
        byte[][] bl = new byte[blocks][];
        byte[] div = divisor(ecc);
        for (int i = 0, k = 0; i < blocks; i++) {
            int len = shortLen - ecc + (i < shortBlocks ? 0 : 1);
            byte[] dat = java.util.Arrays.copyOfRange(data, k, k + len);
            k += len;
            byte[] rem = remainder(dat, div);
            byte[] block = new byte[shortLen + 1];
            System.arraycopy(dat, 0, block, 0, len);
            System.arraycopy(rem, 0, block, block.length - ecc, ecc);
            bl[i] = block;
        }
        byte[] out = new byte[raw];
        int o = 0;
        for (int i = 0; i < bl[0].length; i++)
            for (int j = 0; j < blocks; j++)
                if (i != shortLen - ecc || j >= shortBlocks) out[o++] = bl[j][i];
        return out;
    }

    private static int mul(int x, int y) {
        int z = 0;
        for (int i = 7; i >= 0; i--) {
            z = (z << 1) ^ ((z >>> 7) * 0x11D);
            z ^= ((y >>> i) & 1) * x;
        }
        return z & 0xFF;
    }

    private static byte[] divisor(int degree) {
        byte[] r = new byte[degree];
        r[degree - 1] = 1;
        int root = 1;
        for (int i = 0; i < degree; i++) {
            for (int j = 0; j < r.length; j++) {
                r[j] = (byte) mul(r[j] & 0xFF, root);
                if (j + 1 < r.length) r[j] ^= r[j + 1];
            }
            root = mul(root, 0x02);
        }
        return r;
    }

    private static byte[] remainder(byte[] data, byte[] div) {
        byte[] r = new byte[div.length];
        for (byte b : data) {
            int f = (b ^ r[0]) & 0xFF;
            System.arraycopy(r, 1, r, 0, r.length - 1);
            r[r.length - 1] = 0;
            for (int i = 0; i < r.length; i++) r[i] ^= mul(div[i] & 0xFF, f);
        }
        return r;
    }

    private static final class Qr {
        final int ver, size;
        final boolean[][] mod, fn;

        Qr(int ver) {
            this.ver = ver;
            size = ver * 4 + 17;
            mod = new boolean[size][size];
            fn = new boolean[size][size];
        }

        void set(int x, int y, boolean dark) {
            mod[y][x] = dark;
            fn[y][x] = true;
        }

        void drawFunctions() {
            for (int i = 0; i < size; i++) {
                set(6, i, i % 2 == 0);
                set(i, 6, i % 2 == 0);
            }
            finder(3, 3);
            finder(size - 4, 3);
            finder(3, size - 4);
            int[] al = alignment();
            int n = al.length;
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (!(i == 0 && j == 0 || i == 0 && j == n - 1 || i == n - 1 && j == 0)) align(al[i], al[j]);
            drawFormat(0);   // reserves the format areas; drawn for real once the mask is chosen
            if (ver >= 7) {
                int rem = ver;
                for (int i = 0; i < 12; i++) rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
                int bits = ver << 12 | rem;
                for (int i = 0; i < 18; i++) {
                    boolean bit = ((bits >>> i) & 1) != 0;
                    int a = size - 11 + i % 3, b = i / 3;
                    set(a, b, bit);
                    set(b, a, bit);
                }
            }
        }

        private void finder(int x, int y) {
            for (int dy = -4; dy <= 4; dy++)
                for (int dx = -4; dx <= 4; dx++) {
                    int d = Math.max(Math.abs(dx), Math.abs(dy)), xx = x + dx, yy = y + dy;
                    if (xx >= 0 && xx < size && yy >= 0 && yy < size) set(xx, yy, d != 2 && d != 4);
                }
        }

        private void align(int x, int y) {
            for (int dy = -2; dy <= 2; dy++)
                for (int dx = -2; dx <= 2; dx++) set(x + dx, y + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
        }

        private int[] alignment() {
            if (ver == 1) return new int[0];
            int n = ver / 7 + 2, step = (ver * 4 + n * 2 + 1) / (n * 2 - 2) * 2;
            int[] r = new int[n];
            r[0] = 6;
            for (int i = n - 1, pos = size - 7; i >= 1; i--, pos -= step) r[i] = pos;
            return r;
        }

        void drawFormat(int mask) {
            int data = FORMAT_M << 3 | mask, rem = data;
            for (int i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
            int bits = (data << 10 | rem) ^ 0x5412;
            for (int i = 0; i <= 5; i++) set(8, i, bit(bits, i));
            set(8, 7, bit(bits, 6));
            set(8, 8, bit(bits, 7));
            set(7, 8, bit(bits, 8));
            for (int i = 9; i < 15; i++) set(14 - i, 8, bit(bits, i));
            for (int i = 0; i < 8; i++) set(size - 1 - i, 8, bit(bits, i));
            for (int i = 8; i < 15; i++) set(8, size - 15 + i, bit(bits, i));
            set(8, size - 8, true);
        }

        private static boolean bit(int x, int i) { return ((x >>> i) & 1) != 0; }

        void drawCodewords(byte[] data) {
            int i = 0;
            for (int right = size - 1; right >= 1; right -= 2) {
                if (right == 6) right = 5;
                for (int vert = 0; vert < size; vert++)
                    for (int j = 0; j < 2; j++) {
                        int x = right - j;
                        boolean up = ((right + 1) & 2) == 0;
                        int y = up ? size - 1 - vert : vert;
                        if (!fn[y][x] && i < data.length * 8) {
                            mod[y][x] = bit(data[i >>> 3], 7 - (i & 7));
                            i++;
                        }
                    }
            }
        }

        void applyMask(int m) {
            for (int y = 0; y < size; y++)
                for (int x = 0; x < size; x++) {
                    if (fn[y][x]) continue;
                    boolean inv;
                    switch (m) {
                        case 0: inv = (x + y) % 2 == 0; break;
                        case 1: inv = y % 2 == 0; break;
                        case 2: inv = x % 3 == 0; break;
                        case 3: inv = (x + y) % 3 == 0; break;
                        case 4: inv = (x / 3 + y / 2) % 2 == 0; break;
                        case 5: inv = x * y % 2 + x * y % 3 == 0; break;
                        case 6: inv = (x * y % 2 + x * y % 3) % 2 == 0; break;
                        default: inv = ((x + y) % 2 + x * y % 3) % 2 == 0; break;
                    }
                    if (inv) mod[y][x] = !mod[y][x];
                }
        }

        int penalty() {
            int s = 0, dark = 0;
            for (int pass = 0; pass < 2; pass++)
                for (int a = 0; a < size; a++) {
                    int run = 0;
                    boolean last = false;
                    for (int b = 0; b < size; b++) {
                        boolean v = pass == 0 ? mod[a][b] : mod[b][a];
                        if (b > 0 && v == last) run++;
                        else {
                            if (run >= 5) s += 3 + run - 5;
                            run = 1;
                        }
                        last = v;
                        // finder-like 1:1:3:1:1 with four light modules on one side
                        if (b >= 10) {
                            boolean f = at(pass, a, b - 4) && !at(pass, a, b - 5) && at(pass, a, b - 6) && at(pass, a, b - 7)
                                    && at(pass, a, b - 8) && !at(pass, a, b - 9) && at(pass, a, b - 10);
                            if (f && (light4(pass, a, b - 3) || light4(pass, a, b - 14))) s += 40;
                        }
                    }
                    if (run >= 5) s += 3 + run - 5;
                }
            for (int y = 0; y < size - 1; y++)
                for (int x = 0; x < size - 1; x++) {
                    boolean v = mod[y][x];
                    if (v == mod[y][x + 1] && v == mod[y + 1][x] && v == mod[y + 1][x + 1]) s += 3;
                }
            for (boolean[] row : mod) for (boolean v : row) if (v) dark++;
            int total = size * size, k = (Math.abs(dark * 20 - total * 10) + total - 1) / total - 1;
            return s + k * 10;
        }

        /** The module at (line a, position b) of a row (pass 0) or column (pass 1); outside the code is light. */
        private boolean at(int pass, int a, int b) {
            if (b < 0 || b >= size) return false;
            return pass == 0 ? mod[a][b] : mod[b][a];
        }

        private boolean light4(int pass, int a, int from) {
            for (int i = from; i < from + 4; i++) if (at(pass, a, i)) return false;
            return true;
        }
    }
}
