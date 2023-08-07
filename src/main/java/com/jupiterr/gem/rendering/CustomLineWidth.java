package com.jupiterr.gem.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;

import java.util.OptionalDouble;

@Environment(EnvType.CLIENT)
public class CustomLineWidth extends RenderPhase.LineWidth {

    public CustomLineWidth() {
        super(OptionalDouble.empty());
    }

    @Override
    public void startDrawing() {
        super.startDrawing();
        RenderSystem.lineWidth(5);
    }

    @Override
    public void endDrawing() {
        super.endDrawing();
        RenderSystem.lineWidth(5);
    }
}