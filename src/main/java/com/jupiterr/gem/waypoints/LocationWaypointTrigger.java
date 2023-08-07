package com.jupiterr.gem.waypoints;

import net.minecraft.util.math.Vec3d;

public record LocationWaypointTrigger(String next, WaypointTriggerTypes type, Vec3d location, float radius) implements WaypointTrigger {
}
