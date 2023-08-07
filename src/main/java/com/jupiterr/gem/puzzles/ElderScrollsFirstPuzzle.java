package com.jupiterr.gem.puzzles;

import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.rendering.BlockHighlight;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ElderScrollsFirstPuzzle implements Puzzle{
    private List<BlockHighlight> blockHighlights;
    private PuzzleState state;

    public ElderScrollsFirstPuzzle(PuzzleState startState) {
        this.blockHighlights = new ArrayList<>();
        state = startState;
    }

    @Override
    public String getIdentifier() {
        return "elder_scrolls_first";
    }

    @Override
    public PuzzleState getState() {
        return state;
    }

    @Override
    public void setState(PuzzleState state) {
        this.state = state;
    }

    @Override
    public void acceptAction(PuzzleTrigger trigger) {
        if (trigger.type().equals(PuzzleTriggerTypes.BUTTON_PRESS)) {
            System.out.println("type button");
            click(state.highlights());
            return;
        }

        switch (trigger.action()) {
            case "start" -> startPuzzle(state.highlights());
            case "exit" -> exitPuzzle();
        }
    }

    private void startPuzzle(List<BlockHighlight> highlights) {
        blockHighlights.clear();
        System.out.println(highlights.stream().map(b -> b.getPos().toShortString()).reduce("", (i, v) -> i + ", " + v));
        blockHighlights.addAll(highlights);
    }

    private void click(List<BlockHighlight> highlights) {
        blockHighlights.clear();
        blockHighlights.addAll(highlights);
//        blockHighlights = highlights;
    }

    private void exitPuzzle() {
        blockHighlights.clear();
        GEMMod.currentPuzzle = null;
    }

    @Override
    public List<BlockHighlight> getHighlights() {
        return blockHighlights;
    }
}
