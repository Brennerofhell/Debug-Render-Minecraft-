package com.supportblocks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Tastenbelegung zum Ein-/Ausschalten des Overlays.
 * Standard ist F8, änderbar unter Optionen → Steuerung → Tastenbelegung
 * (Kategorie "Support Blocks").
 */
@EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT)
public final class KeyHandler {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.supportblocks.toggle",          // Übersetzungsschlüssel (lang/*.json)
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,                     // Standardtaste
            "key.categories.supportblocks");      // Kategorie in den Einstellungen

    private KeyHandler() {}

    /** Registriert die Taste (läuft einmal beim Start, NeoForge wählt den Mod-Bus automatisch). */
    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    /** Prüft nach jedem Client-Tick, ob die Taste gedrückt wurde. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        // consumeClick() liefert jeden Tastendruck genau einmal
        while (TOGGLE_KEY.consumeClick()) {
            SupportBlocksMod.enabled = !SupportBlocksMod.enabled;
            if (mc.player != null) {
                Component msg = Component.literal("Support-Blöcke: ")
                        .append(SupportBlocksMod.enabled
                                ? Component.literal("AN").withStyle(ChatFormatting.GREEN)
                                : Component.literal("AUS").withStyle(ChatFormatting.RED));
                // true = Anzeige in der Actionbar statt im Chat
                mc.player.displayClientMessage(msg, true);
            }
        }
    }
}
