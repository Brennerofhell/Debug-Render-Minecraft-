package com.supportblocks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-Konfiguration der Mod.
 *
 * Gespeichert in {@code .minecraft/config/supportblocks-client.toml}.
 * Im Spiel änderbar unter Mods → Support Blocks → Konfiguration;
 * Änderungen wirken sofort, ein Neustart ist nicht nötig.
 */
@EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED_ON_STARTUP = BUILDER
            .comment("Overlay beim Start bereits eingeschaltet?")
            .translation("supportblocks.configuration.enabledOnStartup")
            .define("enabledOnStartup", false);

    public static final ModConfigSpec.BooleanValue ONLY_PLAYERS = BUILDER
            .comment("true = nur Spieler markieren, false = alle Entities (Mobs, Items, Boote, ...)")
            .translation("supportblocks.configuration.onlyPlayers")
            .define("onlyPlayers", false);

    public static final ModConfigSpec.IntValue RADIUS = BUILDER
            .comment("Maximale Entfernung (in Blöcken) zwischen Kamera und Entity")
            .translation("supportblocks.configuration.radius")
            .defineInRange("radius", 64, 1, 256);

    public static final ModConfigSpec.DoubleValue RED = BUILDER
            .comment("Rahmenfarbe: Rot-Anteil (0.0 - 1.0)")
            .translation("supportblocks.configuration.red")
            .defineInRange("red", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue GREEN = BUILDER
            .comment("Rahmenfarbe: Grün-Anteil (0.0 - 1.0)")
            .translation("supportblocks.configuration.green")
            .defineInRange("green", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue BLUE = BUILDER
            .comment("Rahmenfarbe: Blau-Anteil (0.0 - 1.0)")
            .translation("supportblocks.configuration.blue")
            .defineInRange("blue", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ALPHA = BUILDER
            .comment("Rahmenfarbe: Deckkraft (0.0 - 1.0)")
            .translation("supportblocks.configuration.alpha")
            .defineInRange("alpha", 1.0, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    /** Beim ersten Laden der Datei den Startzustand des Overlays übernehmen. */
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            SupportBlocksMod.enabled = ENABLED_ON_STARTUP.get();
        }
    }
}
