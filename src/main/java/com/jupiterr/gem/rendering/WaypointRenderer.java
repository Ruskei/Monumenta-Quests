package com.jupiterr.gem.rendering;

import com.jupiterr.gem.waypoints.Waypoint;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class WaypointRenderer {
    public static void renderWaypoint(Waypoint waypoint, MatrixStack stack) {
        if (waypoint.name() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        Camera camera = client.gameRenderer.getCamera();
        Vec3d targetPosition = waypoint.pos();
        Vec3d transformedPosition = targetPosition.subtract(camera.getPos()).normalize();
        float distance = (float) targetPosition.distanceTo(camera.getPos());
        float size = calcSize(distance, 0.0375f);

        MatrixStack stackCopy = new MatrixStack();
        stackCopy.loadIdentity();
        stackCopy.multiplyPositionMatrix(stack.peek().getPositionMatrix());

        stackCopy.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
        stackCopy.multiply(camera.getRotation());
        stackCopy.scale(-size, -size, 1);

        Matrix4f positionMatrix = stackCopy.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float textHeight = 18;

        Text nameText = Text.of(waypoint.name());
        Text distanceText = Text.of(((int) (distance * 10f)) / 10f + "m");

        float nameWidth = (float) (-textRenderer.getWidth(nameText) / 2);
        float distanceWidth = (float) (-textRenderer.getWidth(distanceText) / 2);
        float boxWidth = Math.min(nameWidth, distanceWidth);

        float spaceThickness = 2;
        float frameThickness = 1;

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        //render transparent inside
        drawColoredRect(buffer, positionMatrix, boxWidth - spaceThickness, -spaceThickness, -boxWidth + spaceThickness, textHeight + spaceThickness - 1, 0, 0, 0, 127);
        //frame
        //bottom
        drawColoredRect(buffer, positionMatrix, boxWidth - (frameThickness + spaceThickness), textHeight + spaceThickness - 1, -boxWidth + (frameThickness + spaceThickness), textHeight + (frameThickness + spaceThickness) - 1, 255, 255, 255, 255);
        //top
        drawColoredRect(buffer, positionMatrix, boxWidth - (frameThickness + spaceThickness), -(frameThickness + spaceThickness), -boxWidth + (frameThickness + spaceThickness), -spaceThickness, 255, 255, 255, 255);
        //right
        drawColoredRect(buffer, positionMatrix, -boxWidth + spaceThickness, -spaceThickness, -boxWidth + (frameThickness + spaceThickness), textHeight + spaceThickness - 1, 255, 255, 255, 255);
        //left
        drawColoredRect(buffer, positionMatrix, boxWidth - spaceThickness, -spaceThickness, boxWidth - (frameThickness + spaceThickness), textHeight + spaceThickness - 1, 255, 255, 255, 255);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);

        drawTexturedRect(tessellator, positionMatrix, -12.5f, -30, 12.5f, -5, 255, 255, 255, 255, new Identifier("gem", "textures/" + waypoint.texture()));

        RenderSystem.disableBlend();
//        RenderSystem.depthMask(true);
//        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

        int nameTextWidth = textRenderer.getWidth(nameText);
        int distanceTextWidth = textRenderer.getWidth(distanceText);

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);

        textRenderer.draw(nameText.asOrderedText(), -(nameTextWidth / 2f), 0, 16777215, false, stackCopy.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        textRenderer.draw(distanceText.asOrderedText(), -(distanceTextWidth / 2f), 10, 16777215, false, stackCopy.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);

        immediate.draw();

        RenderSystem.enableBlend();
    }

    public static void drawColoredRect(BufferBuilder buffer, Matrix4f positionMatrix, float x1, float y1, float x2, float y2, int r, int g, int b, int a) {
        buffer.vertex(positionMatrix, x1, y1, 0).color(r, g, b, a).next();
        buffer.vertex(positionMatrix, x2, y1, 0).color(r, g, b, a).next();
        buffer.vertex(positionMatrix, x2, y2, 0).color(r, g, b, a).next();
        buffer.vertex(positionMatrix, x1, y2, 0).color(r, g, b, a).next();
    }

    public static void drawTexturedRect(Tessellator tessellator, Matrix4f positionMatrix, float x1, float y1, float x2, float y2, int r, int g, int b, int a, Identifier texture) {
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        buffer.vertex(positionMatrix, x1, y1, 0).color(r, g, b, a).texture(0f, 0f).next();
        buffer.vertex(positionMatrix, x1, y2, 0).color(r, g, b, a).texture(0f, 1f).next();
        buffer.vertex(positionMatrix, x2, y2, 0).color(r, g, b, a).texture(1f, 1f).next();
        buffer.vertex(positionMatrix, x2, y1, 0).color(r, g, b, a).texture(1f, 0f).next();

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        tessellator.draw();
    }

    private static float calcSize(float distance, float scaleFactor) {
        return distance < 15 ? scaleFactor / distance : scaleFactor / 15;
    }
}
