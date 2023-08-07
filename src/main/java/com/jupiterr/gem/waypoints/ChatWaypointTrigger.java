package com.jupiterr.gem.waypoints;

public record ChatWaypointTrigger(String next, WaypointTriggerTypes type, String regex) implements WaypointTrigger {
}
