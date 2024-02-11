package io.github.akashiikun.cauldronconcrete.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConcretePowderBlock.class)
public interface ConcretePowderBlockAccessor {
    @Accessor("hardenedState")
    Block getHardenedState();
}