package com.duo.launcher;

/** Light and dark colour sets; {@link #dark} is set from Settings (or the system) at start-up. */
final class Palette {
    static boolean dark;

    static int shelfTop() { return dark ? 0xFF1B212A : 0xFFF3F6F9; }
    static int shelfBottom() { return dark ? 0xFF11151B : 0xFFE3E9EF; }
    static int card() { return dark ? 0xFF262D38 : 0xFFFFFFFF; }
    static int cardSoft() { return dark ? 0xFF323A47 : 0xFFEEF2F6; }
    static int text() { return dark ? 0xFFEEF2F6 : 0xFF1F2A36; }
    static int text2() { return dark ? 0xFF9AA7B4 : 0xFF5C6B7A; }
    static int text3() { return dark ? 0xFF7C8896 : 0xFF8090A0; }
    static int pill() { return dark ? 0xFF2B323E : 0xFFFFFFFF; }
    static int pillText() { return dark ? 0xFFE2E8EE : 0xFF34404D; }
    static int footer() { return dark ? 0xFF161B22 : 0xFFFFFFFF; }
    static int footerLine() { return dark ? 0xFF2C333D : 0xFFD9E0E7; }
    static int key() { return dark ? 0xFFDDE3EA : 0xFF3A4654; }
    static int keyText() { return dark ? 0xFF161B22 : 0xFFFFFFFF; }
    static int shadow() { return dark ? 0x60000000 : 0x30000000; }

    /** Upper screen when a platform (not a game) is shown. */
    static int topBg() { return dark ? 0xFF0F141C : 0xFFEEF3F7; }
    static int topTitle() { return dark ? 0xFFF2F5F8 : 0xFF1B2632; }
    static int topText() { return dark ? 0xFFA9B4BF : 0xFF5C6B7A; }
}
