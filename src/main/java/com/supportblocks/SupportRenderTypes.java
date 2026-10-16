package com.supportblocks;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

/**
 * Eigene Render-Typen der Mod.
 */
public final class SupportRenderTypes {

    /**
     * Wie {@link RenderType#lines()}, aber ohne Tiefentest:
     * Die Linien sind auch durch Wände hindurch sichtbar.
     */
    public static final RenderType LINES_SEE_THROUGH = RenderType.create(
            "supportblocks_lines_see_through",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false));

    /** Halbtransparente Füllung, die von Blöcken verdeckt wird. */
    public static final RenderType FILL = fill("supportblocks_fill", RenderStateShard.LEQUAL_DEPTH_TEST);

    /** Halbtransparente Füllung, auch durch Wände sichtbar. */
    public static final RenderType FILL_SEE_THROUGH = fill("supportblocks_fill_see_through", RenderStateShard.NO_DEPTH_TEST);

    private SupportRenderTypes() {}

    private static RenderType fill(String name, RenderStateShard.DepthTestStateShard depthTest) {
        return RenderType.create(
                name,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.TRIANGLE_STRIP,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        // Nur Farbe schreiben, damit die Füllung nichts dahinter "verschluckt"
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setDepthTestState(depthTest)
                        .createCompositeState(false));
    }
}
