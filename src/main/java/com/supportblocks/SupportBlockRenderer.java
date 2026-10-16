package com.supportblocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
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

import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Zeichnet unter jeder Entity einen Rahmen um den Block, auf dem sie steht –
 * analog zum Debug-Renderer "visualize_entity_supporting_block" aus 1.21.11.
 * Da wir die Entities selbst durchlaufen, funktioniert das auch bei unsichtbaren Entities.
 *
 * Einstellungen (Radius, Farben, durch Wände, ...) kommen aus {@link Config}.
 */
@EventBusSubscriber(modid = SupportBlocksMod.MOD_ID, value = Dist.CLIENT)
public final class SupportBlockRenderer {

    /** Höhe des Prüfbereichs unter den Füßen – derselbe Wert wie in Vanillas Entity#checkSupportingBlock. */
    private static final double FEET_EPSILON = 1.0E-6;

    /** Füllung minimal größer als der Block, gegen Flackern (Z-Fighting). */
    private static final double FILL_INFLATE = 0.002;

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

        Config.Targets targets = Config.TARGETS.get();
        // Den eigenen Spieler nur zeigen, wenn gewünscht und die Kamera nicht in ihm steckt
        boolean drawSelf = Config.INCLUDE_SELF.get() && camera.isDetached();
        boolean useBlockShape = Config.USE_BLOCK_SHAPE.get();
        boolean seeThrough = Config.SEE_THROUGH_WALLS.get();
        float fillOpacity = Config.FILL_OPACITY.get().floatValue();
        double radius = Config.RADIUS.get();
        double radiusSqr = radius * radius;
        int playerColor = Config.colorOf(Config.PLAYER_COLOR);
        int hostileColor = Config.colorOf(Config.HOSTILE_COLOR);
        int passiveColor = Config.colorOf(Config.PASSIVE_COLOR);
        int otherColor = Config.colorOf(Config.OTHER_COLOR);

        RenderType lineType = seeThrough ? SupportRenderTypes.LINES_SEE_THROUGH : RenderType.lines();
        RenderType fillType = seeThrough ? SupportRenderTypes.FILL_SEE_THROUGH : SupportRenderTypes.FILL;
        boolean drawFill = fillOpacity > 0.0F;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        // 1. Alle Ziele sammeln: Block, Form und Farbe
        List<Target> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == mc.player && !drawSelf) continue;
            if (!matches(targets, entity)) continue;
            if (entity.distanceToSqr(cam) > radiusSqr) continue;

            BlockPos pos = getSupportingBlock(level, entity);
            // Stehen mehrere Entities auf demselben Block, nur einmal zeichnen
            if (pos == null || !seen.add(pos)) continue;

            VoxelShape shape = useBlockShape ? getOutlineShape(level, pos, entity) : Shapes.block();
            int color = entity instanceof Player ? playerColor
                    : entity instanceof Enemy ? hostileColor
                    : entity instanceof LivingEntity ? passiveColor
                    : otherColor;
            found.add(new Target(pos, shape, color));
        }

        if (found.isEmpty()) return;

        // 2. Kamera-Drehung explizit setzen. In dieser Stage ist der PoseStack des Events leer und
        //    die globale Model-View-Matrix enthält die Drehung je nach Stelle nicht mehr –
        //    ohne das würden die Rahmen am Bildschirm "kleben" statt an den Blöcken.
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        try {
            // Koordinaten werden relativ zur Kamera berechnet (in double), damit es auch
            // weit weg vom Spawn nicht durch float-Ungenauigkeit zittert.
            PoseStack poseStack = new PoseStack();

            // 3. Erst alle Füllungen, dann alle Rahmen – so liegen die Rahmen sichtbar obendrauf
            if (drawFill) {
                VertexConsumer fill = buffers.getBuffer(fillType);
                for (Target t : found) drawFill(poseStack, fill, t.shape(), t.pos(), cam, t.color(), fillOpacity);
                buffers.endBatch(fillType);
            }
            VertexConsumer lines = buffers.getBuffer(lineType);
            for (Target t : found) drawShape(poseStack.last(), lines, t.shape(), t.pos(), cam, t.color());
            buffers.endBatch(lineType);
        } finally {
            // Immer zurücksetzen, sonst wäre der Rest der Welt falsch gedreht
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /** Ein zu markierender Block mit Form und Farbe. */
    private record Target(BlockPos pos, VoxelShape shape, int color) {}

    /** Prüft, ob die Entity zur Auswahl in der Config gehört. */
    private static boolean matches(Config.Targets targets, Entity entity) {
        return switch (targets) {
            case PLAYERS -> entity instanceof Player;
            case LIVING -> entity instanceof LivingEntity;
            case ALL -> true;
        };
    }

    /**
     * Ermittelt den Block, auf dem die Entity steht (nur wenn sie auf dem Boden ist).
     * <ol>
     *   <li>{@code mainSupportingBlockPos} – das Feld, das auch Vanilla dafür nutzt.
     *       Auf dem Client wird es meist nur für die eigene Spielfigur gepflegt.</li>
     *   <li>Für alle anderen Entities rechnen wir es so nach wie Vanilla
     *       ({@code Entity#checkSupportingBlock}): der Block, der den schmalen Bereich direkt
     *       unter den Füßen berührt und der Mitte am nächsten liegt. Dadurch stimmt das
     *       Ergebnis auch, wenn eine Entity an einer Blockkante steht.</li>
     *   <li>Fallback: {@code getOnPos()}.</li>
     * </ol>
     *
     * @return Blockposition oder {@code null}, wenn die Entity nicht auf etwas steht
     */
    private static BlockPos getSupportingBlock(ClientLevel level, Entity entity) {
        // Zuerst prüfen: mainSupportingBlockPos wird nicht bei jeder Entity zuverlässig geleert
        if (!entity.onGround()) return null;
        Optional<BlockPos> main = entity.mainSupportingBlockPos;
        if (main.isPresent()) return main.get();

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
    private static void drawShape(PoseStack.Pose pose, VertexConsumer lines, VoxelShape shape, BlockPos pos, Vec3 cam, int argb) {
        float a = ColorUtil.alpha(argb);
        float r = ColorUtil.red(argb);
        float g = ColorUtil.green(argb);
        float b = ColorUtil.blue(argb);
        // Blockposition relativ zur Kamera
        double ox = pos.getX() - cam.x, oy = pos.getY() - cam.y, oz = pos.getZ() - cam.z;

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

    /** Füllt alle Teilquader der Form halbtransparent in der Rahmenfarbe. */
    private static void drawFill(PoseStack poseStack, VertexConsumer fill, VoxelShape shape, BlockPos pos, Vec3 cam, int argb, float opacity) {
        float r = ColorUtil.red(argb);
        float g = ColorUtil.green(argb);
        float b = ColorUtil.blue(argb);
        float a = ColorUtil.alpha(argb) * opacity;
        for (AABB box : shape.toAabbs()) {
            // Relativ zur Kamera, minimal vergrößert gegen Flackern mit der Blockoberfläche
            AABB rel = box.move(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z).inflate(FILL_INFLATE);
            LevelRenderer.addChainedFilledBoxVertices(poseStack, fill,
                    rel.minX, rel.minY, rel.minZ, rel.maxX, rel.maxY, rel.maxZ, r, g, b, a);
        }
    }
}
