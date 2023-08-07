package com.jupiterr.gem.rendering;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockHighlight {
    private final String identifier;
    private final BlockPos pos;
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public BlockHighlight(String identifier, BlockPos pos, int r, int g, int b, int a) {
        this.identifier = identifier;
        this.pos = pos;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getA() {
        return a;
    }
}
