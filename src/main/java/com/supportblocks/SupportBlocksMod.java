package com.supportblocks;

import net.minecraftforge.fml.common.Mod;

/**
 * Einstiegspunkt der Mod "Support Blocks".
 *
 * Die eigentliche Logik steckt in zwei Klassen, die sich per
 * {@code @Mod.EventBusSubscriber} selbst an den Event-Bussen anmelden:
 * <ul>
 *   <li>{@link KeyHandler} – Tastenbelegung (Standard F8) und Umschalten</li>
 *   <li>{@link SupportBlockRenderer} – zeichnet die grünen Rahmen</li>
 * </ul>
 * Beide sind nur auf dem Client aktiv ({@code Dist.CLIENT}).
 */
@Mod(SupportBlocksMod.MOD_ID)
public class SupportBlocksMod {
    public static final String MOD_ID = "supportblocks";

    /** Aktueller Zustand des Overlays. Wird per Taste umgeschaltet. */
    public static boolean enabled = false;

    public SupportBlocksMod() {
        // Nichts zu tun – alle Handler registrieren sich über Annotationen.
    }
}
