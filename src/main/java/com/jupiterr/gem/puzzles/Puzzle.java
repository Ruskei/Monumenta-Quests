package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.rendering.BlockHighlight;

import java.util.List;

public interface Puzzle {
    //identifier for knowing which puzzle to send the action too
    String getIdentifier();
    //next triggers, contains next state
    PuzzleState getState();
    void setState(PuzzleState state);
    //when something is triggered the "action" in the json will be accepted here then processed by each subclass individually
    void acceptAction(PuzzleTrigger trigger);
    //used to get what needs to be rendered. will probably almost definitely need to add more things here later on
    List<BlockHighlight> getHighlights();
}
