package com.supportblocks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Einstiegspunkt der Mod "Support Blocks".
 *
 * {@code dist = Dist.CLIENT}: Die Mod wird nur auf dem Client geladen; auf einem
 * Server (auch wenn die .jar versehentlich dort liegt) passiert nichts.
 *
 * Aufbau:
 * <ul>
 *   <li>{@link Config} – Einstellungen (Radius, Farbe, nur Spieler, ...)</li>
 *   <li>{@link KeyHandler} – Tastenbelegung (Standard F8) und Umschalten</li>
 *   <li>{@link SupportBlockRenderer} – zeichnet die Rahmen</li>
 * </ul>
 */
@Mod(value = SupportBlocksMod.MOD_ID, dist = Dist.CLIENT)
public class SupportBlocksMod {
    public static final String MOD_ID = "supportblocks";

    /** Aktueller Zustand des Overlays. Wird per Taste umgeschaltet. */
    public static boolean enabled = false;

    public SupportBlocksMod(ModContainer container) {
        // Client-Config registrieren -> config/supportblocks-client.toml
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        // "Konfiguration"-Knopf in der Mod-Liste mit NeoForges eingebautem Editor
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
