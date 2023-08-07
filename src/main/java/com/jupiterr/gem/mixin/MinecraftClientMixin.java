package com.jupiterr.gem.mixin;

import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.puzzles.*;
import com.jupiterr.gem.waypoints.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.jupiterr.gem.GEMMod.puzzleStates;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            List<Waypoint> toRemove = new ArrayList<>();
            List<Waypoint> toAdd = new ArrayList<>();

            for (Waypoint waypoint : GEMMod.renderedQuestWaypoints) {
                for (WaypointTrigger waypointTrigger : waypoint.nextWaypointTriggers()) {
                    if (waypointTrigger.type().equals(WaypointTriggerTypes.LOCATION)) {
                        LocationWaypointTrigger locationTrigger = (LocationWaypointTrigger) waypointTrigger;
                        Vec3d triggerPos = locationTrigger.location();
                        double distance = triggerPos.distanceTo(client.player.getPos());
                        if (distance <= locationTrigger.radius()) {
                            toRemove.add(waypoint);

                            for (Waypoint nextWaypoint : GEMMod.questWaypoints) {
                                if (nextWaypoint.identifier().equals(waypointTrigger.next())) {
                                    if (!GEMMod.renderedQuestWaypoints.contains(nextWaypoint)) {
                                        toAdd.add(nextWaypoint);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            GEMMod.renderedQuestWaypoints.removeAll(toRemove);
            GEMMod.renderedQuestWaypoints.addAll(toAdd);

            //if have a puzzle being rendered, continue that
            if (GEMMod.currentPuzzle == null) {
                outer:
                for (PuzzleState state : puzzleStates) {
                    for (PuzzleTrigger trigger : state.triggers()) {
                        if (trigger.action().equals("start")) {
                            if (trigger.type().equals(PuzzleTriggerTypes.BOUNDS_INCLUSIVE)) {
                                BoundsTrigger boundsTrigger = (BoundsTrigger) trigger;
                                if (boundsTrigger.bounds().contains(client.player.getPos())) {
                                    //do the trigger
                                    for (PuzzleState nextState : puzzleStates) {
                                        if (nextState.identifier().equals(boundsTrigger.next())) {
                                            if (nextState.puzzle().equals(state.puzzle())) {
                                                GEMMod.currentPuzzle = new ElderScrollsFirstPuzzle(nextState);
                                                GEMMod.currentPuzzle.acceptAction(trigger);
                                                break outer;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                PuzzleState state = GEMMod.currentPuzzle.getState();
                for (PuzzleTrigger trigger : state.triggers()) {
                    if (trigger.type().equals(PuzzleTriggerTypes.BOUNDS_EXCLUSIVE)) {
                        BoundsTrigger boundsTrigger = (BoundsTrigger) trigger;
                        if (!boundsTrigger.bounds().contains(client.player.getPos())) {
                            GEMMod.currentPuzzle.acceptAction(trigger);
                        }
                    }
                }
            }

        }
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    public void joinWorld(ClientWorld world, CallbackInfo ci) {
        System.out.println("joining world");
    }
}
