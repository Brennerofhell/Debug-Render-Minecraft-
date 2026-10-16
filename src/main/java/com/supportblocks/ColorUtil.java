package com.supportblocks;

/**
 * Hilfsfunktionen für Farben im Format "#RRGGBB" / "#AARRGGBB".
 * Bewusst ohne Minecraft-Abhängigkeiten, damit sie sich einfach testen lassen.
 */
public final class ColorUtil {
    private ColorUtil() {}

    /**
     * Wandelt "#RRGGBB" oder "#AARRGGBB" (das "#" ist optional) in einen ARGB-int um.
     * Ohne Alpha-Anteil ist die Farbe voll deckend.
     *
     * @return ARGB-Farbe oder {@code fallback} bei ungültiger Eingabe
     */
    public static int parse(String text, int fallback) {
        if (text == null) return fallback;
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6 && hex.length() != 8) return fallback;
        for (int i = 0; i < hex.length(); i++) {
            if (Character.digit(hex.charAt(i), 16) < 0) return fallback;
        }
        long value = Long.parseLong(hex, 16);
        return hex.length() == 6 ? (int) (0xFF000000L | value) : (int) value;
    }

    /** true, wenn {@code text} eine gültige Farbe ist. */
    public static boolean isValid(String text) {
        // Zwei verschiedene Fallbacks: Nur bei ungültiger Eingabe kommen beide zurück.
        return parse(text, 0) == parse(text, 1);
    }

    public static float alpha(int argb) { return ((argb >>> 24) & 0xFF) / 255.0F; }
    public static float red(int argb)   { return ((argb >>> 16) & 0xFF) / 255.0F; }
    public static float green(int argb) { return ((argb >>> 8) & 0xFF) / 255.0F; }
    public static float blue(int argb)  { return (argb & 0xFF) / 255.0F; }
}
