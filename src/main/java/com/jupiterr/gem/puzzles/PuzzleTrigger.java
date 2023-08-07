package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.rendering.BlockHighlight;

import java.util.List;

public interface PuzzleTrigger {
    String action();
    String next();
    PuzzleTriggerTypes type();
}
