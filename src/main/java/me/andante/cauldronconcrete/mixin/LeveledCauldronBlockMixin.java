package me.andante.cauldronconcrete.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LeveledCauldronBlock.class)
@Debug(export = true)
public abstract class LeveledCauldronBlockMixin extends AbstractCauldronBlock {
    public LeveledCauldronBlockMixin(Settings settings, Map<Item, CauldronBehavior> behaviorMap) {
        super(settings, behaviorMap);
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"))
    private void convertConcreteItems(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!world.isClient && entity instanceof ItemEntity item && item.getStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ConcretePowderBlock block && this.isEntityTouchingFluid(state, pos, entity)) {
            // play splash sound
            Vec3d itemVelocity = item.getVelocity();

            float volume = 0.4F + ((float)Math.sqrt(itemVelocity.x * itemVelocity.x * 0.2 + itemVelocity.y * itemVelocity.y + itemVelocity.z * itemVelocity.z * 0.2) * 0.2F);
            float pitch = 1.0F + (item.world.random.nextFloat() - item.world.random.nextFloat()) * 0.4F;

            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, volume, pitch);

            // bubble + splash particles
            var itemPos = item.getPos();

            // i spent hours adjusting these values
            ((ServerWorld)world).spawnParticles(ParticleTypes.BUBBLE, itemPos.x, MathHelper.floor(itemPos.y) + 0.95, itemPos.z, 10, 0.1, 0, 0.1, 0.1);
            ((ServerWorld)world).spawnParticles(ParticleTypes.SPLASH, itemPos.x, MathHelper.floor(itemPos.y) + 1.0, itemPos.z, 20, 0.1, 0, 0.1, 0.025);

            // convert powder to concrete
            NbtCompound tag = item.getStack().writeNbt(new NbtCompound());
            tag.putString("id", Registry.ITEM.getId(((ConcretePowderBlockAccessor) block).getHardenedState().getBlock().asItem()).toString());

            item.setStack(ItemStack.fromNbt(tag));
        }
    }
}
