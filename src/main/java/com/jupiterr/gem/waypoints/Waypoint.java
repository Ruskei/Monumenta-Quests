package com.jupiterr.gem.waypoints;

import net.minecraft.util.math.Vec3d;

import java.util.List;

public record Waypoint(String identifier, Vec3d pos, String name, String texture, List<WaypointTrigger> nextWaypointTriggers) {
}
