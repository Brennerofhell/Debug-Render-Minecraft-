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

    /** Welche Entities markiert werden. */
    public enum Targets {
        /** Nur Spieler */
        PLAYERS,
        /** Spieler und Mobs (alles Lebendige) */
        LIVING,
        /** Alle Entities, auch Items, Boote, Loren, Pfeile, ... */
        ALL
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Allgemein -------------------------------------------------------------

    public static final ModConfigSpec.BooleanValue ENABLED_ON_STARTUP = BUILDER
            .comment("Overlay beim Start bereits eingeschaltet?")
            .translation("supportblocks.configuration.enabledOnStartup")
            .define("enabledOnStartup", false);

    public static final ModConfigSpec.EnumValue<Targets> TARGETS = BUILDER
            .comment("Welche Entities markiert werden: PLAYERS = nur Spieler, LIVING = Spieler und Mobs, ALL = alle Entities")
            .translation("supportblocks.configuration.targets")
            .defineEnum("targets", Targets.ALL);

    public static final ModConfigSpec.BooleanValue INCLUDE_SELF = BUILDER
            .comment("Auch den eigenen Spieler markieren (nur in der Third-Person-Ansicht, F5)")
            .translation("supportblocks.configuration.includeSelf")
            .define("includeSelf", false);

    public static final ModConfigSpec.IntValue RADIUS = BUILDER
            .comment("Maximale Entfernung (in Blöcken) zwischen Kamera und Entity")
            .translation("supportblocks.configuration.radius")
            .defineInRange("radius", 64, 1, 256);

    // --- Darstellung -----------------------------------------------------------

    public static final ModConfigSpec.BooleanValue SEE_THROUGH_WALLS = BUILDER
            .comment("Rahmen auch durch Wände und Blöcke hindurch anzeigen")
            .translation("supportblocks.configuration.seeThroughWalls")
            .define("seeThroughWalls", false);

    public static final ModConfigSpec.BooleanValue USE_BLOCK_SHAPE = BUILDER
            .comment("true = echte Blockform umranden (Stufen, Treppen, Zäune, ...), false = immer ganzer Würfel")
            .translation("supportblocks.configuration.useBlockShape")
            .define("useBlockShape", true);

    public static final ModConfigSpec.DoubleValue FILL_OPACITY = BUILDER
            .comment("Deckkraft der Füllung in der Rahmenfarbe (0.0 = keine Füllung, 1.0 = deckend)")
            .translation("supportblocks.configuration.fillOpacity")
            .defineInRange("fillOpacity", 0.0, 0.0, 1.0);

    // --- Farben ----------------------------------------------------------------

    public static final ModConfigSpec.ConfigValue<String> PLAYER_COLOR = color(
            "playerColor", "Rahmenfarbe für Spieler", "#00FF00");

    public static final ModConfigSpec.ConfigValue<String> HOSTILE_COLOR = color(
            "hostileColor", "Rahmenfarbe für feindliche Mobs (Zombies, Creeper, ...)", "#00FF00");

    public static final ModConfigSpec.ConfigValue<String> PASSIVE_COLOR = color(
            "passiveColor", "Rahmenfarbe für friedliche Mobs (Tiere, Dorfbewohner, ...)", "#00FF00");

    public static final ModConfigSpec.ConfigValue<String> OTHER_COLOR = color(
            "otherColor", "Rahmenfarbe für alle anderen Entities (Items, Boote, ...)", "#00FF00");

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    private static ModConfigSpec.ConfigValue<String> color(String key, String comment, String defaultValue) {
        return BUILDER
                .comment(comment + " als Hex: #RRGGBB oder #AARRGGBB")
                .translation("supportblocks.configuration." + key)
                .define(key, defaultValue, value -> value instanceof String s && ColorUtil.isValid(s));
    }

    /** Liest eine Farbe aus der Config; bei Unsinn (sollte nicht vorkommen) Grün. */
    public static int colorOf(ModConfigSpec.ConfigValue<String> value) {
        return ColorUtil.parse(value.get(), 0xFF00FF00);
    }

    /** Beim ersten Laden der Datei den Startzustand des Overlays übernehmen. */
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            SupportBlocksMod.enabled = ENABLED_ON_STARTUP.get();
        }
    }
}
