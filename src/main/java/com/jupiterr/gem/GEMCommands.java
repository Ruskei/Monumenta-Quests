package com.jupiterr.gem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jupiterr.gem.config.PuzzleStatesInitializer;
import com.jupiterr.gem.config.QuestWaypointsInitializer;
import com.jupiterr.gem.waypoints.*;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class GEMCommands {
    public static int setWaypoint(CommandContext<FabricClientCommandSource> context, float x, float y, float z, String name, String texture) {
        GEMMod.waypoints.add(new Waypoint("", new Vec3d(x, y, z), name, texture , null));
        return 1;
    }

    public static int delWaypoint(CommandContext<FabricClientCommandSource> context, String name) {
        GEMMod.waypoints.removeIf(waypoint -> waypoint.name().equals(name));
        return 1;
    }

    public static int clearQuestPoint(CommandContext<FabricClientCommandSource> context) {
        GEMMod.renderedQuestWaypoints.clear();
        return 1;
    }

    public static int reloadMaps(CommandContext<FabricClientCommandSource> context) {
        QuestWaypointsInitializer.init();

        GEMMod.puzzleStates.clear();
        PuzzleStatesInitializer.init();
        GEMMod.currentPuzzle = null;
        return 1;
    }

    public static int newPoint(CommandContext<FabricClientCommandSource> context, String identifier, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos pos = client.world.getSpawnPos();
        GEMMod.pendingPoint = new Waypoint(identifier, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), name, "t_lm.png", new ArrayList<>());
        return 1;
    }

    public static int attachChat(CommandContext<FabricClientCommandSource> context, String point, String regex) {
        GEMMod.pendingPoint.nextWaypointTriggers().add(new ChatWaypointTrigger(point, WaypointTriggerTypes.CHAT, regex));
        return 1;
    }

    public static int attachLoc(CommandContext<FabricClientCommandSource> context, String point, float r) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos pos = client.world.getSpawnPos();
        GEMMod.pendingPoint.nextWaypointTriggers().add(new LocationWaypointTrigger(point, WaypointTriggerTypes.LOCATION, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), r));
        return 1;
    }

    public static int writePoint(CommandContext<FabricClientCommandSource> context) {
        try {
            JsonObject pointObject = new JsonObject();
            pointObject.addProperty("identifier", GEMMod.pendingPoint.identifier());
            JsonObject waypointObject = new JsonObject();
            waypointObject.addProperty("x", GEMMod.pendingPoint.pos().x);
            waypointObject.addProperty("y", GEMMod.pendingPoint.pos().y);
            waypointObject.addProperty("z", GEMMod.pendingPoint.pos().z);
            waypointObject.addProperty("name", GEMMod.pendingPoint.name());
            waypointObject.addProperty("texture", GEMMod.pendingPoint.texture());

            pointObject.add("waypoint", waypointObject);

            JsonArray nextPoints = new JsonArray();

            for (WaypointTrigger waypointTrigger : GEMMod.pendingPoint.nextWaypointTriggers()) {
                JsonObject nextPoint = new JsonObject();
                nextPoint.addProperty("point", waypointTrigger.next());
                nextPoint.addProperty("type", waypointTrigger.type().toString());

                if (waypointTrigger.type() == WaypointTriggerTypes.CHAT) {
                    nextPoint.addProperty("regex", ((ChatWaypointTrigger) waypointTrigger).regex());
                } else if (waypointTrigger.type() == WaypointTriggerTypes.LOCATION) {
                    LocationWaypointTrigger locationTrigger = (LocationWaypointTrigger) waypointTrigger;
                    nextPoint.addProperty("x", locationTrigger.location().x);
                    nextPoint.addProperty("y", locationTrigger.location().y);
                    nextPoint.addProperty("z", locationTrigger.location().z);
                    nextPoint.addProperty("radius", locationTrigger.radius());
                }

                nextPoints.add(nextPoint);
            }

            pointObject.add("next", nextPoints);

            Path path = FabricLoader.getInstance().getConfigDir().resolve("mmquests.json");
            String jsonString = new String(Files.readAllBytes(path));
            Object obj = JsonParser.parseString(jsonString);
            JsonArray stringQuestList = (JsonArray) obj;
            stringQuestList.add(pointObject);
            Files.write(path, stringQuestList.toString().getBytes());
            GEMMod.pendingPoint = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
