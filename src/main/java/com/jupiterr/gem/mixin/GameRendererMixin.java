package com.jupiterr.gem.mixin;

import com.jupiterr.gem.rendering.WaypointRenderer;
import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.waypoints.Waypoint;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    private int ticks;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void injected(float tickDelta, long limitTime, MatrixStack matrixStack, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer gameRenderer = client.gameRenderer;
        Camera camera = gameRenderer.getCamera();

        MatrixStack projMatrix = new MatrixStack();
        double d = gameRenderer.getFov(camera, tickDelta, true);
        projMatrix.peek().getPositionMatrix().mul(gameRenderer.getBasicProjectionMatrix(d));
        gameRenderer.tiltViewWhenHurt(projMatrix, tickDelta);

        float f = client.options.getDistortionEffectScale().getValue().floatValue();
        float g = MathHelper.lerp(tickDelta, client.player.lastNauseaStrength, client.player.nextNauseaStrength) * f * f;
        if (f > 0.0F) {
            int i = client.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
            float h = 5.0F / (g * g + 5.0F) - g * 0.04F;
            h *= h;
            RotationAxis rotationAxis = RotationAxis.of(new Vector3f(0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F));
            matrixStack.multiply(rotationAxis.rotationDegrees(((float)this.ticks + tickDelta) * (float)i));
            matrixStack.scale(1.0F / h, 1.0F, 1.0F);
            float j = -((float)this.ticks + tickDelta) * (float)i;
            matrixStack.multiply(rotationAxis.rotationDegrees(j));
        }

        Matrix4f matrix4f = projMatrix.peek().getPositionMatrix();
        gameRenderer.loadProjectionMatrix(matrix4f);

        MinecraftClient.getInstance().gameRenderer.loadProjectionMatrix(projMatrix.peek().getPositionMatrix());

        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL30.GL_ALWAYS);
        RenderSystem.enableBlend();

        MatrixStack fixedStack = new MatrixStack();
        fixedStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        fixedStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        for (Waypoint waypoint : GEMMod.renderedQuestWaypoints) {
            WaypointRenderer.renderWaypoint(waypoint, fixedStack);
        }

        for (Waypoint waypoint : GEMMod.waypoints) {
            WaypointRenderer.renderWaypoint(waypoint, fixedStack);
        }

        RenderSystem.disableBlend();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }
}
