package com.jupiterr.gem.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.puzzles.*;
import com.jupiterr.gem.rendering.BlockHighlight;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PuzzleStatesInitializer {
    public static void init() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("mmpuzzles.json");

            String jsonString = new String(Files.readAllBytes(path));

            Object obj = JsonParser.parseString(jsonString);

            JsonArray stringQuestList = (JsonArray) obj;

            stringQuestList.forEach(object -> parseState((JsonObject) object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseState(JsonObject stateObject) {
        String identifier = stateObject.get("identifier").getAsString();
        String puzzle = stateObject.get("puzzle").getAsString();

        List<PuzzleTrigger> triggers = new ArrayList<>();
        JsonArray triggersObject = stateObject.getAsJsonArray("next");

        for (int i = 0; i < triggersObject.size(); i++) {
            JsonObject triggerObject = triggersObject.get(i).getAsJsonObject();
            String action = triggerObject.get("action").getAsString();
            PuzzleTriggerTypes type = PuzzleTriggerTypes.valueOf(triggerObject.get("type").getAsString());
            String next = triggerObject.get("state").getAsString();

            if (type.equals(PuzzleTriggerTypes.BOUNDS_EXCLUSIVE) || type.equals(PuzzleTriggerTypes.BOUNDS_INCLUSIVE)) {
                Box bounds = new Box(
                        triggerObject.get("x1").getAsFloat(),
                        triggerObject.get("y1").getAsFloat(),
                        triggerObject.get("z1").getAsFloat(),
                        triggerObject.get("x2").getAsFloat(),
                        triggerObject.get("y2").getAsFloat(),
                        triggerObject.get("z2").getAsFloat());
                triggers.add(new BoundsTrigger(action, next, type, bounds));
                System.out.println(bounds);
            } else if (type.equals(PuzzleTriggerTypes.BUTTON_PRESS)) {
                float x = triggerObject.get("x").getAsFloat();
                float y = triggerObject.get("y").getAsFloat();
                float z = triggerObject.get("z").getAsFloat();

                triggers.add(new ButtonPressTrigger(action, next, type, new BlockPos((int)x, (int)y, (int)z), Registries.BLOCK.get(new Identifier("minecraft", triggerObject.get("block").getAsString()))));
            }
        }

        List<BlockHighlight> blockHighlights = new ArrayList<>();
        JsonArray highlights = stateObject.getAsJsonArray("highlights");
        for (int j = 0; j < highlights.size(); j++) {
            JsonObject highlight = highlights.get(j).getAsJsonObject();
            float x = highlight.get("x").getAsFloat();
            float y = highlight.get("y").getAsFloat();
            float z = highlight.get("z").getAsFloat();

            blockHighlights.add(new BlockHighlight(identifier, new BlockPos((int)x, (int)y, (int)z), 255, 255, 255, 255));
        }

        PuzzleState state = new PuzzleState(identifier, puzzle, triggers, blockHighlights);
        GEMMod.puzzleStates.add(state);
    }
}
