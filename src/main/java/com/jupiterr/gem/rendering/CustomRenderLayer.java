package com.jupiterr.gem.rendering;

import com.jupiterr.gem.mixin.RenderLayerInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

@Environment(EnvType.CLIENT)
public class CustomRenderLayer extends RenderLayer {

    public static final RenderLayer OUTLINE = RenderLayerInvoker.of("gem_outline", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256, true, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(LINES_PROGRAM)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .lineWidth(new CustomLineWidth())
//                    .depthTest(DepthTest.ALWAYS_DEPTH_TEST)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .target(ITEM_TARGET)
                    .writeMaskState(ALL_MASK)
                    .cull(DISABLE_CULLING)
                    .build(true));

//    public static final RenderLayer LINE = RenderLayerInvoker.of("lines", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256,
//            RenderLayer.MultiPhaseParameters.builder()
//                    .shader(LINES_SHADER)
//                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
//                    .layering(VIEW_OFFSET_Z_LAYERING)
//                    .transparency(TRANSLUCENT_TRANSPARENCY)
//                    .target(ITEM_TARGET)
//                    .writeMaskState(ALL_MASK)
//                    .cull(DISABLE_CULLING)
//                    .build(false));

    private CustomRenderLayer() {
        super("colormeoutlines", VertexFormats.POSITION, VertexFormat.DrawMode.LINES, 0, false, false, null, null);
    }
}
