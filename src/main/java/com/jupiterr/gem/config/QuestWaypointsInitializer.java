package com.jupiterr.gem.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jupiterr.gem.*;
import com.jupiterr.gem.waypoints.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QuestWaypointsInitializer {
    public static void init() {
        GEMMod.questWaypoints.clear();

        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("mmquests.json");

            String jsonString = new String(Files.readAllBytes(path));

            Object obj = JsonParser.parseString(jsonString);

            JsonArray stringQuestList = (JsonArray) obj;

            stringQuestList.forEach(object -> parseWaypoint((JsonObject) object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseWaypoint(JsonObject object) {
        List<WaypointTrigger> nextWaypointTriggers = new ArrayList<>();
        JsonArray nextTriggersObject = object.get("next").getAsJsonArray();
        for (int i = 0; i < nextTriggersObject.size(); i++) {
            JsonObject triggerObject = nextTriggersObject.get(i).getAsJsonObject();
            WaypointTriggerTypes triggerType = WaypointTriggerTypes.valueOf(triggerObject.get("type").getAsString());
            if (triggerType.equals(WaypointTriggerTypes.CHAT)) {
                String regex = triggerObject.get("regex").getAsString();
                ChatWaypointTrigger trigger = new ChatWaypointTrigger(triggerObject.get("point").getAsString(), triggerType, regex);
                nextWaypointTriggers.add(trigger);
            } else if (triggerType.equals(WaypointTriggerTypes.LOCATION)) {
                Vec3d pos = new Vec3d(triggerObject.get("x").getAsFloat(), triggerObject.get("y").getAsFloat(), triggerObject.get("z").getAsFloat());
                LocationWaypointTrigger trigger = new LocationWaypointTrigger(triggerObject.get("point").getAsString(), triggerType, pos, triggerObject.get("radius").getAsFloat());
                nextWaypointTriggers.add(trigger);
            }
        }

        String identifier = object.get("identifier").getAsString();

        Waypoint toAdd = new Waypoint(identifier, null, "start", null, nextWaypointTriggers);

        if (!object.get("waypoint").isJsonNull()){
            JsonObject waypoint = object.get("waypoint").getAsJsonObject();
            if (waypoint.get("name").isJsonNull()) {
                toAdd = new Waypoint(identifier, null, null, null, nextWaypointTriggers);
            } else if (!waypoint.get("name").getAsString().equals("start")) {
                float x = waypoint.get("x").getAsFloat();
                float y = waypoint.get("y").getAsFloat();
                float z = waypoint.get("z").getAsFloat();
                String name = waypoint.get("name").getAsString();
                String texture = waypoint.get("texture").getAsString();

                toAdd = new Waypoint(identifier, new Vec3d(x, y, z), name, texture, nextWaypointTriggers);
            }
        }

        GEMMod.questWaypoints.add(toAdd);
    }
}
