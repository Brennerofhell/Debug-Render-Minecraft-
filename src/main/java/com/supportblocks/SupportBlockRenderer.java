package com.supportblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Optional;

/**
 * Zeichnet unter jeder Entity einen Rahmen um den Block, auf dem sie steht –
 * analog zum Debug-Renderer "visualize_entity_supporting_block" aus 1.21.11.
 * Da wir die Entities selbst durchlaufen, funktioniert das auch bei unsichtbaren Entities.
 *
 * Einstellungen (Radius, Farben, durch Wände, ...) kommen aus {@link Config}.
 */
@EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SupportBlockRenderer {

    /** Höhe des Prüfbereichs unter den Füßen – derselbe Wert wie in Vanillas Entity#checkSupportingBlock. */
    private static final double FEET_EPSILON = 1.0E-6;

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

        boolean onlyPlayers = Config.ONLY_PLAYERS.get();
        // Den eigenen Spieler nur zeigen, wenn gewünscht und die Kamera nicht in ihm steckt
        boolean drawSelf = Config.INCLUDE_SELF.get() && camera.isDetached();
        boolean useBlockShape = Config.USE_BLOCK_SHAPE.get();
        double radius = Config.RADIUS.get();
        double radiusSqr = radius * radius;
        int playerColor = Config.parseColor(Config.PLAYER_COLOR.get(), 0xFF00FF00);
        int entityColor = Config.parseColor(Config.ENTITY_COLOR.get(), 0xFF00FF00);

        RenderType renderType = Config.SEE_THROUGH_WALLS.get() ? SupportRenderTypes.LINES_SEE_THROUGH : RenderType.lines();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(renderType);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        // Weltkoordinaten -> Kamerakoordinaten: Kamera-Position abziehen
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        PoseStack.Pose pose = poseStack.last();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity == mc.player && !drawSelf) continue;
            boolean isPlayer = entity instanceof Player;
            if (onlyPlayers && !isPlayer) continue;
            if (entity.distanceToSqr(cam) > radiusSqr) continue;

            BlockPos pos = getSupportingBlock(level, entity);
            if (pos == null) continue;

            VoxelShape shape = useBlockShape ? getOutlineShape(level, pos, entity) : Shapes.block();
            drawShape(pose, lines, shape, pos, isPlayer ? playerColor : entityColor);
        }

        poseStack.popPose();
        // Gepufferte Linien jetzt sofort zeichnen
        buffers.endBatch(renderType);
    }

    /**
     * Ermittelt den Block, auf dem die Entity steht.
     * <ol>
     *   <li>{@code mainSupportingBlockPos} – das Feld, das auch Vanilla dafür nutzt.
     *       Auf dem Client wird es meist nur für die eigene Spielfigur gepflegt.</li>
     *   <li>Für alle anderen Entities rechnen wir es so nach wie Vanilla
     *       ({@code Entity#checkSupportingBlock}): der Block, der den schmalen Bereich direkt
     *       unter den Füßen berührt und der Mitte am nächsten liegt. Dadurch stimmt das
     *       Ergebnis auch, wenn eine Entity an einer Blockkante steht.</li>
     *   <li>Fallback: {@code getOnPos()}, aber nur wenn die Entity auf dem Boden steht.</li>
     * </ol>
     *
     * @return Blockposition oder {@code null}, wenn die Entity nicht auf etwas steht
     */
    private static BlockPos getSupportingBlock(ClientLevel level, Entity entity) {
        Optional<BlockPos> main = entity.mainSupportingBlockPos;
        if (main.isPresent()) return main.get();
        if (!entity.onGround()) return null;

        AABB box = entity.getBoundingBox();
        AABB feet = new AABB(box.minX, box.minY - FEET_EPSILON, box.minZ, box.maxX, box.minY, box.maxZ);
        return level.findSupportingBlock(entity, feet).orElse(entity.getOnPos());
    }

    /** Umriss des Blocks (z. B. halbe Stufe); bei Blöcken ohne Umriss ein ganzer Würfel. */
    private static VoxelShape getOutlineShape(ClientLevel level, BlockPos pos, Entity entity) {
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos, CollisionContext.of(entity));
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    /** Zeichnet alle Kanten der Form als Linien (wie Vanillas Block-Auswahlrahmen). */
    private static void drawShape(PoseStack.Pose pose, VertexConsumer lines, VoxelShape shape, BlockPos pos, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255.0F;
        float r = ((argb >>> 16) & 0xFF) / 255.0F;
        float g = ((argb >>> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        double ox = pos.getX(), oy = pos.getY(), oz = pos.getZ();

        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            // Linien-Shader braucht die Richtung der Linie als "Normale"
            float nx = (float) (x2 - x1);
            float ny = (float) (y2 - y1);
            float nz = (float) (z2 - z1);
            float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= len;
            ny /= len;
            nz /= len;
            lines.addVertex(pose, (float) (x1 + ox), (float) (y1 + oy), (float) (z1 + oz))
                    .setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
            lines.addVertex(pose, (float) (x2 + ox), (float) (y2 + oy), (float) (z2 + oz))
                    .setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        });
    }
}
