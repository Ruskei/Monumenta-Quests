package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.rendering.BlockHighlight;
import net.minecraft.util.math.Box;

import java.util.List;

public record BoundsTrigger(String action, String next, PuzzleTriggerTypes type, Box bounds) implements PuzzleTrigger{
}
