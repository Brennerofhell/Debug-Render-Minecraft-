package com.supportblocks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * Einstiegspunkt der Mod "Support Blocks".
 *
 * {@code dist = Dist.CLIENT}: Die Mod wird nur auf dem Client geladen; auf einem
 * Server (auch wenn die .jar versehentlich dort liegt) passiert nichts.
 *
 * Die eigentliche Logik steckt in zwei Klassen, die sich per
 * {@code @EventBusSubscriber} selbst an den Event-Bussen anmelden:
 * <ul>
 *   <li>{@link KeyHandler} – Tastenbelegung (Standard F8) und Umschalten</li>
 *   <li>{@link SupportBlockRenderer} – zeichnet die grünen Rahmen</li>
 * </ul>
 */
@Mod(value = SupportBlocksMod.MOD_ID, dist = Dist.CLIENT)
public class SupportBlocksMod {
    public static final String MOD_ID = "supportblocks";

    /** Aktueller Zustand des Overlays. Wird per Taste umgeschaltet. */
    public static boolean enabled = false;

    public SupportBlocksMod() {
        // Nichts zu tun – alle Handler registrieren sich über Annotationen.
    }
}
