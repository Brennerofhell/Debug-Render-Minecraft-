package com.supportblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

/**
 * Zeichnet unter jeder Entity einen grünen Rahmen um den Block, auf dem sie steht –
 * analog zum Debug-Renderer "visualize_entity_supporting_block" aus 1.21.11.
 * Da wir die Entities selbst durchlaufen, funktioniert das auch bei unsichtbaren Entities.
 */
@EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SupportBlockRenderer {

    /** true = nur Spieler markieren, false = alle Entities (Mobs, Items, Boote, ...). */
    private static final boolean ONLY_PLAYERS = false;

    /** Maximale Entfernung (in Blöcken) zwischen Kamera und Entity. */
    private static final double RADIUS = 64.0;

    /** Leicht vergrößern, damit der Rahmen nicht mit den Blockkanten flackert (Z-Fighting). */
    private static final double INFLATE = 0.002;

    private SupportBlockRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Nach den durchsichtigen Blöcken zeichnen, damit Wasser/Glas den Rahmen nicht überdeckt
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!SupportBlocksMod.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;

        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        double radiusSqr = RADIUS * RADIUS;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        // Weltkoordinaten -> Kamerakoordinaten: Kamera-Position abziehen
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (Entity entity : level.entitiesForRendering()) {
            if (entity == mc.player) continue;                          // eigenen Spieler auslassen
            if (ONLY_PLAYERS && !(entity instanceof Player)) continue;
            if (entity.distanceToSqr(cam) > radiusSqr) continue;

            BlockPos pos = getSupportingBlock(entity);
            if (pos == null) continue;

            AABB box = new AABB(pos).inflate(INFLATE);
            LevelRenderer.renderLineBox(poseStack, lines, box, 0.0F, 1.0F, 0.0F, 1.0F); // grün
        }

        poseStack.popPose();
        // Gepufferte Linien jetzt sofort zeichnen
        buffers.endBatch(RenderType.lines());
    }

    /**
     * Ermittelt den Block, auf dem die Entity steht.
     * 1. {@code mainSupportingBlockPos} – das Feld, das auch Vanilla dafür nutzt.
     * 2. Fallback: {@code getOnPos()}, aber nur wenn die Entity auf dem Boden steht.
     *    (Auf dem Client wird das Feld meist nur für die eigene Spielfigur gepflegt,
     *    daher greift für andere Entities häufig der Fallback.)
     *
     * @return Blockposition oder {@code null}, wenn die Entity nicht auf etwas steht
     */
    private static BlockPos getSupportingBlock(Entity entity) {
        Optional<BlockPos> main = entity.mainSupportingBlockPos;
        if (main.isPresent()) return main.get();
        if (entity.onGround()) return entity.getOnPos();
        return null;
    }
}
