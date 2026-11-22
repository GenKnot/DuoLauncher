package com.duo.launcher;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewOutlineProvider;

import java.io.File;

/** A game's video snap on the upper screen, looping quietly over its cover art. */
final class VideoPane extends TextureView implements TextureView.SurfaceTextureListener {
    private MediaPlayer player;
    private Surface surface;
    private File pending;
    private int videoW, videoH;
    private Runnable onShow, onHide;

    VideoPane(Context c) {
        super(c);
        setSurfaceTextureListener(this);
        setOpaque(false);
        setAlpha(0f);
        setVisibility(GONE);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) {
                // round the corners of the area the picture actually covers
                float[] r = videoRect();
                o.setRoundRect((int) r[0], (int) r[1], (int) r[2], (int) r[3], 24 * getWidth() / 1120f);
            }
        });
        setClipToOutline(true);
    }

    /** onShow/onHide let the owner fade the cover art out and back in. */
    void setCallbacks(Runnable onShow, Runnable onHide) {
        this.onShow = onShow;
        this.onHide = onHide;
    }

    void play(File f) {
        stop();
        pending = f;
        setVisibility(VISIBLE);
        if (surface != null) start();
    }

    void stop() {
        pending = null;
        animate().cancel();
        setAlpha(0f);
        if (player != null) {
            try { player.stop(); } catch (IllegalStateException ignored) { }
            player.release();
            player = null;
            if (onHide != null) onHide.run();
        }
        setVisibility(GONE);
    }

    private void start() {
        File f = pending;
        if (f == null) return;
        MediaPlayer mp = new MediaPlayer();
        player = mp;
        try {
            mp.setDataSource(f.getPath());
            mp.setSurface(surface);
            mp.setLooping(true);
            mp.setVolume(0.35f, 0.35f);
            mp.setOnVideoSizeChangedListener((m, w, h) -> { videoW = w; videoH = h; fit(); });
            mp.setOnPreparedListener(m -> {
                if (player != m) return;
                m.start();
                animate().alpha(1f).setDuration(450).start();
                if (onShow != null) onShow.run();
            });
            mp.setOnErrorListener((m, what, extra) -> { stop(); return true; });
            mp.prepareAsync();
        } catch (Exception e) {
            stop();
        }
    }

    /** Left, top, right, bottom of the letterboxed video inside this view. */
    private float[] videoRect() {
        float w = getWidth(), h = getHeight();
        if (videoW <= 0 || videoH <= 0 || w <= 0 || h <= 0) return new float[]{0, 0, w, h};
        float k = Math.min(w / videoW, h / videoH), dw = videoW * k, dh = videoH * k;
        return new float[]{(w - dw) / 2, (h - dh) / 2, (w + dw) / 2, (h + dh) / 2};
    }

    private void fit() {
        float w = getWidth(), h = getHeight();
        if (videoW <= 0 || w <= 0) return;
        float[] r = videoRect();
        Matrix m = new Matrix();
        m.setScale((r[2] - r[0]) / w, (r[3] - r[1]) / h, w / 2, h / 2);
        setTransform(m);
        invalidateOutline();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) { super.onSizeChanged(w, h, ow, oh); fit(); }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture st, int w, int h) {
        surface = new Surface(st);
        if (pending != null && player == null) start();
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st, int w, int h) { fit(); }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
        if (player != null) { player.release(); player = null; }
        if (surface != null) { surface.release(); surface = null; }
        return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture st) { }
}
