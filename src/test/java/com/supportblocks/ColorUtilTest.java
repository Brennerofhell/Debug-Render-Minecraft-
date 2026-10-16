package com.supportblocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorUtilTest {
    private static final int FALLBACK = 0x12345678;

    @Test
    void parsesRgbAsOpaque() {
        assertEquals(0xFF00FF00, ColorUtil.parse("#00FF00", FALLBACK));
        assertEquals(0xFFABCDEF, ColorUtil.parse("abcdef", FALLBACK));
    }

    @Test
    void parsesArgbWithAlpha() {
        assertEquals(0x8000FF00, ColorUtil.parse("#8000FF00", FALLBACK));
        assertEquals(0x00000000, ColorUtil.parse("#00000000", FALLBACK));
    }

    @Test
    void ignoresSurroundingWhitespaceAndCase() {
        assertEquals(0xFFFF0000, ColorUtil.parse("  #ff0000 ", FALLBACK));
    }

    @Test
    void rejectsInvalidInput() {
        for (String bad : new String[] {null, "", "#", "#12345", "#1234567", "#GG0000", "#-12345", "+12345", "#123456789"}) {
            assertEquals(FALLBACK, ColorUtil.parse(bad, FALLBACK), "parse(" + bad + ")");
            assertFalse(ColorUtil.isValid(bad), "isValid(" + bad + ")");
        }
    }

    @Test
    void isValidAcceptsColorsEqualToAnyFallback() {
        // Regression: Farben, die zufällig einem Fallback-Wert entsprechen, bleiben gültig
        assertTrue(ColorUtil.isValid("#00000000"));
        assertTrue(ColorUtil.isValid("#00000001"));
        assertTrue(ColorUtil.isValid("#00FF00"));
    }

    @Test
    void splitsChannels() {
        int argb = 0x80FF4000;
        assertEquals(128 / 255.0F, ColorUtil.alpha(argb), 1e-6);
        assertEquals(1.0F, ColorUtil.red(argb), 1e-6);
        assertEquals(64 / 255.0F, ColorUtil.green(argb), 1e-6);
        assertEquals(0.0F, ColorUtil.blue(argb), 1e-6);
    }
}
