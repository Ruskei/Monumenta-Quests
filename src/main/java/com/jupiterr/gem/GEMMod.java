package com.jupiterr.gem;

import com.jupiterr.gem.config.ConfigInitializer;
import com.jupiterr.gem.config.PuzzleStatesInitializer;
import com.jupiterr.gem.config.QuestWaypointsInitializer;
import com.jupiterr.gem.puzzles.Puzzle;
import com.jupiterr.gem.puzzles.PuzzleState;
import com.jupiterr.gem.rendering.BlockHighlight;
import com.jupiterr.gem.rendering.CustomRenderLayer;
import com.jupiterr.gem.waypoints.Waypoint;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GEMMod implements ClientModInitializer {
    public static boolean skipCheck = false;
    public static boolean skipGG = false;

    public static HashMap<String, String> emojis;

    public static String[] welcomeMessages = {
            "Welcome to the rice fields!",
            "Welcome to minecraft dark souls!",
            "Welcome to pain and suffering",
            "Welcome to crippling addiction!",
            "we;cp,e!",
            "Welcome to wynncraft+++!",
            "Welcome to walmart!",
            "Welcome to goofy ahha!",
            "Welcome to... to what? I don't even know anymore.",
            "Welcome to getting obliterated by kaul for the 50th time!",
            "Welcome to the 3 Regions!",
            "Welcome to the rest of your life!",
            "Welcome to hell!",
            "Welcome to SCP-3008!",
            "Welcome to SCP-3125!",
            "Welcome!",
            "Welcome to Target!",
            "Welcome to Stonkco!",
            "Welcome to Monumenta!",
            "Welcome to Sierhaven!",
            "Welcome to Project Epic!"
    };

    public static List<Waypoint> waypoints;

    public static List<Waypoint> questWaypoints;
    public static List<Waypoint> renderedQuestWaypoints;

    public static Waypoint pendingPoint;

    public static List<PuzzleState> puzzleStates;
    public static Puzzle currentPuzzle;

    @Override
    public void onInitializeClient() {
        pendingPoint = null;
        waypoints = new ArrayList<>();

        questWaypoints = new ArrayList<>();
        renderedQuestWaypoints = new ArrayList<>();

        puzzleStates = new ArrayList<>();

        ConfigInitializer.initConfig();
        QuestWaypointsInitializer.init();
        PuzzleStatesInitializer.init();

        emojis = new HashMap<>();
        emojis.put(":skull:", "☠");
        emojis.put(":smile:", "☺");
        emojis.put(":frown:", "☹");
        emojis.put(":tm:", "™");

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("setwaypoint")
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
                            .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
                                    .then(ClientCommandManager.argument("z", FloatArgumentType.floatArg())
                                            .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                                    .then(ClientCommandManager.argument("texture", StringArgumentType.string())
                                                            .executes((context -> GEMCommands.setWaypoint(context, FloatArgumentType.getFloat(context, "x"), FloatArgumentType.getFloat(context, "y"), FloatArgumentType.getFloat(context, "z"), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "texture")))))
                                            )))));

            dispatcher.register(ClientCommandManager.literal("delwaypoint")
                    .then(ClientCommandManager.argument("name", StringArgumentType.string())
                            .executes((context) -> GEMCommands.delWaypoint(context, StringArgumentType.getString(context, "name")))
                    ));

            dispatcher.register(ClientCommandManager.literal("clearquestpoint")
                    .executes(GEMCommands::clearQuestPoint)
            );

            dispatcher.register(ClientCommandManager.literal("reloadmaps")
                    .executes(GEMCommands::reloadMaps)
            );

            dispatcher.register(ClientCommandManager.literal("newpoint")
                    .then(ClientCommandManager.argument("identifier", StringArgumentType.string())
                            .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                    .executes((context -> GEMCommands.newPoint(context, StringArgumentType.getString(context, "identifier"), StringArgumentType.getString(context, "name"))))
                            )));

            dispatcher.register(ClientCommandManager.literal("attachchat")
                    .then(ClientCommandManager.argument("point", StringArgumentType.string())
                            .then(ClientCommandManager.argument("regex", StringArgumentType.string())
                                    .executes((context) -> GEMCommands.attachChat(context, StringArgumentType.getString(context, "point"), StringArgumentType.getString(context, "regex")))))
            );

            dispatcher.register(ClientCommandManager.literal("attachloc")
                    .then(ClientCommandManager.argument("point", StringArgumentType.string())
                            .then(ClientCommandManager.argument("radius", FloatArgumentType.floatArg())
                                    .executes((context -> GEMCommands.attachLoc(context, StringArgumentType.getString(context, "point"), FloatArgumentType.getFloat(context, "radius"))))))
            );

            dispatcher.register(ClientCommandManager.literal("writepoint")
                    .executes(GEMCommands::writePoint));
        }));

        WorldRenderEvents.END.register(context -> {
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL30.GL_EQUAL);
            RenderSystem.enableBlend();

            Camera camera = context.camera();
            MatrixStack matrixStack = context.matrixStack();
            Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
            Matrix3f normalMatrix = matrixStack.peek().getNormalMatrix();

            //puzzle highlights
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(bufferBuilder);
            VertexConsumer vertexConsumer = immediate.getBuffer(CustomRenderLayer.OUTLINE);
            if (currentPuzzle != null) {
                for (BlockHighlight blockHighlight : currentPuzzle.getHighlights()) {
                    Vec3d targetPos = new Vec3d(blockHighlight.getPos().getX(), blockHighlight.getPos().getY(), blockHighlight.getPos().getZ());
                    Vec3d transformedPos = targetPos.subtract(camera.getPos());
                    double x = transformedPos.x;
                    double y = transformedPos.y;
                    double z = transformedPos.z;

                    MinecraftClient client = MinecraftClient.getInstance();
                    World world = client.world;

                    BlockState state = world.getBlockState(blockHighlight.getPos());
                    VoxelShape voxelShape = state.getOutlineShape(world, blockHighlight.getPos());

                    voxelShape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                        float xDifference = (float) (x2 - x1);
                        float yDifference = (float) (y2 - y1);
                        float zDifference = (float) (z2 - z1);
                        float distance = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
                        xDifference /= distance;
                        yDifference /= distance;
                        zDifference /= distance;

                        vertexConsumer.vertex(positionMatrix, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).color(blockHighlight.getR(), blockHighlight.getB(), blockHighlight.getB(), blockHighlight.getA()).normal(normalMatrix, xDifference, yDifference, zDifference).next();
                        vertexConsumer.vertex(positionMatrix, (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).color(blockHighlight.getR(), blockHighlight.getB(), blockHighlight.getB(), blockHighlight.getA()).normal(normalMatrix, xDifference, yDifference, zDifference).next();
                    });
                }
            }

            RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            immediate.draw();

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        });
    }
}

