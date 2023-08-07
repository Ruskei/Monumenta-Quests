package com.jupiterr.gem.mixin;

import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.puzzles.ButtonPressTrigger;
import com.jupiterr.gem.puzzles.PuzzleState;
import com.jupiterr.gem.puzzles.PuzzleTrigger;
import com.jupiterr.gem.puzzles.PuzzleTriggerTypes;
import com.jupiterr.gem.rendering.BlockHighlight;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(at = @At("HEAD"), method = "interactBlock")
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        World world = player.world;
        if (world.isClient && hand.equals(Hand.MAIN_HAND)) {
            BlockState blockState = world.getBlockState(hitResult.getBlockPos());
            //player could relog and progress is reset, so this should be accounted for
            if (GEMMod.currentPuzzle == null) {
                //add logic here once tested the other thing
            } else {
                PuzzleState puzzleState = GEMMod.currentPuzzle.getState();
                for (PuzzleTrigger trigger : puzzleState.triggers()) {
                    if (trigger.type().equals(PuzzleTriggerTypes.BUTTON_PRESS)) {
                        ButtonPressTrigger buttonTrigger = (ButtonPressTrigger) trigger;
                        if (blockState.contains(Properties.POWERED)) {
                            if (!blockState.get(Properties.POWERED)) {
                                System.out.println("comparing " + blockState.getBlock().getName() + " at " + blockState.getBlock() + " to trigger " + buttonTrigger.blockType() + " at " + buttonTrigger.pos());
                                if (blockState.isOf(buttonTrigger.blockType()) && hitResult.getBlockPos().equals(buttonTrigger.pos())) {
                                    for (PuzzleState state : GEMMod.puzzleStates) {
                                        if (trigger.next().equals(state.identifier())) {
                                            System.out.println("set state");
                                            GEMMod.currentPuzzle.setState(state);
                                        }
                                    }

                                    GEMMod.currentPuzzle.acceptAction(trigger);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
