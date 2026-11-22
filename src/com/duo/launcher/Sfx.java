package com.duo.launcher;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Short UI sounds and optional background music. A theme may bring its own files
 * (sounds/move|open|back|launch.ogg|wav|mp3 and music.ogg|mp3); otherwise soft tones are synthesised.
 */
final class Sfx {
    static final int MOVE = 0, OPEN = 1, BACK = 2, LAUNCH = 3;
    private static final String[] NAMES = {"move", "open", "back", "launch"};
    private static final float[] VOLUME = {0.35f, 0.5f, 0.5f, 0.6f};

    private final SoundPool pool;
    private final int[] ids = new int[NAMES.length];
    private MediaPlayer music;
    boolean enabled = true, musicEnabled = true;

    Sfx(Context c, File themeDir) {
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
        File gen = new File(c.getCacheDir(), "sfx");
        gen.mkdirs();
        for (int i = 0; i < NAMES.length; i++) {
            File own = themeFile(themeDir, "sounds/" + NAMES[i]);
            File f = own != null ? own : synth(gen, i);
            ids[i] = f == null ? 0 : pool.load(f.getPath(), 1);
        }
        File m = themeFile(themeDir, "music");
        if (m != null) {
            try {
                music = new MediaPlayer();
                music.setDataSource(m.getPath());
                music.setLooping(true);
                music.setVolume(0.45f, 0.45f);
                music.prepare();
            } catch (IOException | RuntimeException e) {
                music = null;
            }
        }
    }

    boolean hasMusic() { return music != null; }

    private static File themeFile(File dir, String base) {
        if (dir == null) return null;
        for (String ext : new String[]{".ogg", ".wav", ".mp3"}) {
            File f = new File(dir, base + ext);
            if (f.isFile()) return f;
        }
        return null;
    }

    void play(int cue) {
        if (enabled && ids[cue] != 0) pool.play(ids[cue], VOLUME[cue], VOLUME[cue], 1, 0, 1f);
    }

    void resumeMusic() {
        if (music != null && musicEnabled && !music.isPlaying()) music.start();
    }

    void pauseMusic() {
        if (music != null && music.isPlaying()) music.pause();
    }

    void release() {
        pool.release();
        if (music != null) music.release();
    }

    // ---- built-in tones: short, soft sine notes with a quick fade ----

    private static final int RATE = 44100;

    private static File synth(File dir, int cue) {
        File f = new File(dir, NAMES[cue] + "_v1.wav");
        if (f.isFile()) return f;
        float[][] notes; // {frequency, start ms, length ms}
        switch (cue) {
            case MOVE: notes = new float[][]{{1760, 0, 28}}; break;
            case OPEN: notes = new float[][]{{988, 0, 60}, {1480, 45, 90}}; break;
            case BACK: notes = new float[][]{{1480, 0, 60}, {988, 45, 90}}; break;
            default: notes = new float[][]{{784, 0, 90}, {1175, 70, 90}, {1568, 140, 220}}; break;
        }
        float end = 0;
        for (float[] n : notes) end = Math.max(end, n[1] + n[2]);
        short[] pcm = new short[(int) (RATE * (end + 20) / 1000)];
        for (float[] n : notes) {
            int start = (int) (RATE * n[1] / 1000), len = (int) (RATE * n[2] / 1000);
            for (int i = 0; i < len && start + i < pcm.length; i++) {
                double t = i / (double) RATE;
                double env = Math.min(1, i / (RATE * 0.004)) * Math.exp(-5.0 * i / len);
                double v = Math.sin(2 * Math.PI * n[0] * t) * 0.8 + Math.sin(4 * Math.PI * n[0] * t) * 0.15;
                int s = pcm[start + i] + (int) (v * env * 9000);
                pcm[start + i] = (short) Math.max(-32768, Math.min(32767, s));
            }
        }
        ByteBuffer b = ByteBuffer.allocate(44 + pcm.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36 + pcm.length * 2).put("WAVE".getBytes());
        b.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1).putInt(RATE).putInt(RATE * 2)
                .putShort((short) 2).putShort((short) 16);
        b.put("data".getBytes()).putInt(pcm.length * 2);
        for (short s : pcm) b.putShort(s);
        try (FileOutputStream o = new FileOutputStream(f)) {
            o.write(b.array());
            return f;
        } catch (IOException e) {
            return null;
        }
    }
}
