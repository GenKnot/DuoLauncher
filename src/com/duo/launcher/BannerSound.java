package com.duo.launcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * The short tune a 3DS game plays on the HOME menu: the CWAV inside the ExeFS "banner" (CBMD).
 * Decrypted images only. Supports the three encodings used there: PCM8, PCM16 and DSP-ADPCM.
 */
final class BannerSound {
    final short[] pcm;      // interleaved
    final int rate, channels;

    private BannerSound(short[] pcm, int rate, int channels) {
        this.pcm = pcm;
        this.rate = rate;
        this.channels = channels;
    }

    private static int u16(byte[] b, int o) { return (b[o] & 0xFF) | (b[o + 1] & 0xFF) << 8; }

    private static int u32(byte[] b, int o) { return u16(b, o) | u16(b, o + 2) << 16; }

    private static boolean magic(byte[] b, int o, String m) {
        return new String(b, o, 4, StandardCharsets.US_ASCII).equals(m);
    }

    /** Reads and decodes the banner tune of a .cci/.3ds/.cxi/.app file, or null if there is none. */
    static BannerSound load(File f) {
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            byte[] h = new byte[0x200];
            r.readFully(h);
            long ncch;
            if (magic(h, 0x100, "NCSD")) ncch = (u32(h, 0x120) & 0xFFFFFFFFL) * 0x200;
            else if (magic(h, 0x100, "NCCH")) ncch = 0;
            else return null;
            byte[] n = new byte[0x200];
            r.seek(ncch);
            r.readFully(n);
            if (!magic(n, 0x100, "NCCH") || (n[0x18F] & 0x04) == 0) return null;
            long exefs = ncch + (u32(n, 0x1A0) & 0xFFFFFFFFL) * 0x200;
            byte[] e = new byte[0x200];
            r.seek(exefs);
            r.readFully(e);
            for (int i = 0; i < 10; i++) {
                if (!new String(e, i * 16, 8, StandardCharsets.US_ASCII).replace("\0", "").equals("banner")) continue;
                int size = u32(e, i * 16 + 12);
                if (size <= 0x88 || size > 8 << 20) return null;
                byte[] bn = new byte[size];
                r.seek(exefs + 0x200 + u32(e, i * 16 + 8));
                r.readFully(bn);
                if (!magic(bn, 0, "CBMD")) return null;
                return decode(bn, u32(bn, 0x84));
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static BannerSound decode(byte[] b, int cw) {
        if (!magic(b, cw, "CWAV")) return null;
        int info = cw + u32(b, cw + 0x18), data = cw + u32(b, cw + 0x24) + 8;
        if (!magic(b, info, "INFO")) return null;
        int enc = b[info + 8] & 0xFF, rate = u32(b, info + 0xC), frames = u32(b, info + 0x14);
        int table = info + 0x1C, ch = u32(b, table);
        if (ch < 1 || ch > 2 || frames <= 0 || frames > rate * 30) return null;
        short[] out = new short[frames * ch];
        for (int c = 0; c < ch; c++) {
            int ci = table + u32(b, table + 4 + c * 8 + 4);
            int src = data + u32(b, ci + 4);
            switch (enc) {
                case 0: // PCM8
                    for (int s = 0; s < frames; s++) out[s * ch + c] = (short) (b[src + s] << 8);
                    break;
                case 1: // PCM16
                    for (int s = 0; s < frames; s++) out[s * ch + c] = (short) u16(b, src + s * 2);
                    break;
                case 2: { // DSP-ADPCM: 8-byte frames of 14 samples
                    int adpcm = ci + u32(b, ci + 12);
                    short[] coef = new short[16];
                    for (int k = 0; k < 16; k++) coef[k] = (short) u16(b, adpcm + k * 2);
                    int h1 = 0, h2 = 0, s = 0, p = src;
                    while (s < frames) {
                        int hdr = b[p++] & 0xFF, pred = (hdr >> 4) & 7, scale = 1 << (hdr & 15);
                        int c1 = coef[pred * 2], c2 = coef[pred * 2 + 1];
                        for (int k = 0; k < 14 && s < frames; k++) {
                            int by = b[p + k / 2] & 0xFF;
                            int nib = (k & 1) == 0 ? by >> 4 : by & 15;
                            if (nib >= 8) nib -= 16;
                            int v = (((nib * scale) << 11) + 1024 + c1 * h1 + c2 * h2) >> 11;
                            v = Math.max(-32768, Math.min(32767, v));
                            h2 = h1;
                            h1 = v;
                            out[s++ * ch + c] = (short) v;
                        }
                        p += 7;
                    }
                    break;
                }
                default:
                    return null;
            }
        }
        return new BannerSound(out, rate, ch);
    }

    /** Plays the whole tune once; the caller stops and releases the track. */
    AudioTrack play() {
        AudioTrack t = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.length * 2)
                .build();
        t.write(pcm, 0, pcm.length);
        t.play();
        return t;
    }
}
