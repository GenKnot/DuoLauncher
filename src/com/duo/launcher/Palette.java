package com.duo.launcher;

/**
 * Light and dark colour sets; {@link #dark} is set from Settings (or the system) at start-up.
 * A theme pack can replace any of them with "color.<name>" / "dark.color.<name>" (see {@link Theme#color}).
 */
final class Palette {
    static boolean dark;
    /** Text size factor chosen in Settings (0.9 / 1 / 1.15 / 1.3), applied to all text on both screens. */
    static float textScale = 1f;
    static final float[] TEXT_SCALES = {0.9f, 1f, 1.15f, 1.3f};

    private static int c(String key, int light, int darkC) { return Theme.color(key, dark ? darkC : light); }

    static int shelfTop() { return c("shelf-top", 0xFFF3F6F9, 0xFF1B212A); }
    static int shelfBottom() { return c("shelf-bottom", 0xFFE3E9EF, 0xFF11151B); }
    static int card() { return c("card", 0xFFFFFFFF, 0xFF262D38); }
    static int cardSoft() { return c("card-soft", 0xFFEEF2F6, 0xFF323A47); }
    static int text() { return c("text", 0xFF1F2A36, 0xFFEEF2F6); }
    static int text2() { return c("text2", 0xFF5C6B7A, 0xFF9AA7B4); }
    static int text3() { return c("text3", 0xFF8090A0, 0xFF7C8896); }
    static int pill() { return c("pill", 0xFFFFFFFF, 0xFF2B323E); }
    static int pillText() { return c("pill-text", 0xFF34404D, 0xFFE2E8EE); }
    static int footer() { return c("footer", 0xFFFFFFFF, 0xFF161B22); }
    static int footerLine() { return c("footer-line", 0xFFD9E0E7, 0xFF2C333D); }
    static int key() { return c("key", 0xFF3A4654, 0xFFDDE3EA); }
    static int keyText() { return c("key-text", 0xFFFFFFFF, 0xFF161B22); }
    static int shadow() { return c("shadow", 0x30000000, 0x60000000); }
    /** Selection outline, highlighted menu row and active pills. */
    static int accent() { return c("accent", 0xFF2F8BFF, 0xFF2F8BFF); }

    /** Upper screen when a platform (not a game) is shown. */
    static int topBg() { return c("top-bg", 0xFFEEF3F7, 0xFF0F141C); }
    static int topTitle() { return c("top-title", 0xFF1B2632, 0xFFF2F5F8); }
    static int topText() { return c("top-text", 0xFF5C6B7A, 0xFFA9B4BF); }

    /** Upper-screen backdrop: its white becomes "paper", its black lines "ink" (theme: backdrop-paper / backdrop-ink). */
    static int backdropPaper() { return c("backdrop-paper", 0xFFFFFFFF, 0xFF13182E); }
    static int backdropInk() { return c("backdrop-ink", 0xFF000000, 0xFFEBF0FA); }

    /** Home-page folder colour; a theme can set "color.folder.<type>" (3ds, gba, recent, settings, ...). */
    static int folder(String type, int def) { return Theme.color("folder." + type, def); }
}
