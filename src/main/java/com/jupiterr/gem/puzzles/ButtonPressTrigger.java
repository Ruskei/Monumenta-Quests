package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.rendering.BlockHighlight;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public record ButtonPressTrigger(String action, String next, PuzzleTriggerTypes type, BlockPos pos, Block blockType) implements PuzzleTrigger {
}
