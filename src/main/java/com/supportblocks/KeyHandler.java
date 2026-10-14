package com.supportblocks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Tastenbelegung zum Ein-/Ausschalten des Overlays.
 * Standard ist F8, änderbar unter Optionen → Steuerung → Tastenbelegung
 * (Kategorie "Support Blocks").
 */
public final class KeyHandler {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.supportblocks.toggle",          // Übersetzungsschlüssel (lang/*.json)
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,                     // Standardtaste
            "key.categories.supportblocks");      // Kategorie in den Einstellungen

    private KeyHandler() {}

    /** Registriert die Taste beim Mod-Event-Bus (läuft einmal beim Start). */
    @Mod.EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_KEY);
        }
    }

    /** Prüft jeden Client-Tick, ob die Taste gedrückt wurde. */
    @Mod.EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

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
}
