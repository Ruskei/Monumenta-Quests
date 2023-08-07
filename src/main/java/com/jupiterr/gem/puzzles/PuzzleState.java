package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.rendering.BlockHighlight;

import java.util.List;

public record PuzzleState(String identifier, String puzzle, List<PuzzleTrigger> triggers, List<BlockHighlight> highlights) {
}
