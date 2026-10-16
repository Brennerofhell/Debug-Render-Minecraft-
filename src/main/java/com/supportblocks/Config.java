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

    public static final ModConfigSpec.BooleanValue INCLUDE_SELF = BUILDER
            .comment("Auch den eigenen Spieler markieren (nur in der Third-Person-Ansicht, F5)")
            .translation("supportblocks.configuration.includeSelf")
            .define("includeSelf", false);

    public static final ModConfigSpec.IntValue RADIUS = BUILDER
            .comment("Maximale Entfernung (in Blöcken) zwischen Kamera und Entity")
            .translation("supportblocks.configuration.radius")
            .defineInRange("radius", 64, 1, 256);

    public static final ModConfigSpec.BooleanValue SEE_THROUGH_WALLS = BUILDER
            .comment("Rahmen auch durch Wände und Blöcke hindurch anzeigen")
            .translation("supportblocks.configuration.seeThroughWalls")
            .define("seeThroughWalls", false);

    public static final ModConfigSpec.BooleanValue USE_BLOCK_SHAPE = BUILDER
            .comment("true = echte Blockform umranden (Stufen, Treppen, Zäune, ...), false = immer ganzer Würfel")
            .translation("supportblocks.configuration.useBlockShape")
            .define("useBlockShape", true);

    public static final ModConfigSpec.ConfigValue<String> PLAYER_COLOR = BUILDER
            .comment("Rahmenfarbe für Spieler als Hex: #RRGGBB oder #AARRGGBB")
            .translation("supportblocks.configuration.playerColor")
            .define("playerColor", "#00FF00", Config::isValidColor);

    public static final ModConfigSpec.ConfigValue<String> ENTITY_COLOR = BUILDER
            .comment("Rahmenfarbe für alle anderen Entities als Hex: #RRGGBB oder #AARRGGBB")
            .translation("supportblocks.configuration.entityColor")
            .define("entityColor", "#00FF00", Config::isValidColor);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    /** Beim ersten Laden der Datei den Startzustand des Overlays übernehmen. */
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            SupportBlocksMod.enabled = ENABLED_ON_STARTUP.get();
        }
    }

    private static boolean isValidColor(Object value) {
        return value instanceof String s && parseColor(s, -1) != -1;
    }

    /**
     * Wandelt "#RRGGBB" oder "#AARRGGBB" in einen ARGB-int um.
     * Ohne Alpha-Anteil ist die Farbe voll deckend.
     *
     * @return ARGB-Farbe oder {@code fallback} bei ungültiger Eingabe
     */
    public static int parseColor(String text, int fallback) {
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6 && hex.length() != 8) return fallback;
        try {
            long value = Long.parseLong(hex, 16);
            return hex.length() == 6 ? (int) (0xFF000000L | value) : (int) value;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
